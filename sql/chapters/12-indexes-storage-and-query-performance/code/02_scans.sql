-- 12.6 Seq scan vs index scan vs index-only scan vs bitmap scan, on 1,000,000 orders.
-- Timings are machine-dependent; compare the shape of the plans and the buffer counts.
SET max_parallel_workers_per_gather = 0;   -- keep plans simple to read
SET jit = off;

-- (a) No index on customer_id yet: the only option is to read every page.
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id, amount FROM orders WHERE customer_id = 4242;

CREATE INDEX orders_customer_idx ON orders (customer_id);
ANALYZE orders;

-- (b) Same query, now ~10 matching rows scattered over the table.
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id, amount FROM orders WHERE customer_id = 4242;

-- (c) A primary-key lookup: classic Index Scan (walk tree, then fetch 1 heap row).
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT * FROM orders WHERE id = 500000;

-- (d) Only the indexed column is needed -> Index Only Scan (heap skipped, thanks
--     to the visibility map that VACUUM maintained in 00_schema.sql).
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT count(*) FROM orders WHERE customer_id BETWEEN 1000 AND 1999;

-- (e) Low selectivity: 95 % of rows are 'shipped'. Even with an index the
--     planner prefers a sequential scan.
CREATE INDEX orders_status_idx ON orders (status);
ANALYZE orders;
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT sum(amount) FROM orders WHERE status = 'shipped';

-- (f) A medium-sized result (1 % of rows = 10,000) scattered over many pages:
--     Bitmap Index Scan collects row addresses, sorts them by page, then the
--     Bitmap Heap Scan reads each needed page once, in physical order.
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT sum(amount) FROM orders WHERE customer_id BETWEEN 1000 AND 1999;

-- (g) Two indexes combined with BitmapAnd.
EXPLAIN (COSTS OFF)
SELECT * FROM orders WHERE status = 'refunded' AND customer_id < 2000;

-- Selectivity table: fraction of rows each predicate keeps.
SELECT status, round(100.0 * count(*) / sum(count(*)) OVER (), 1) AS pct
FROM orders GROUP BY status ORDER BY pct DESC;
