-- 12.7 Composite (multi-column) indexes: column order and the leftmost prefix.
SET max_parallel_workers_per_gather = 0;
SET jit = off;

-- One index, two columns: first sorted by customer_id, then by created_at inside it.
CREATE INDEX orders_cust_created_idx ON orders (customer_id, created_at);
ANALYZE orders;

-- (a) Uses the leading column: fast.
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id, created_at FROM orders WHERE customer_id = 777;

-- (b) Both columns: equality on the first, sort/range on the second. The index
--     already holds customer 777's orders in created_at order, so "latest 3"
--     is read straight off the end of that slice: no Sort node.
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id, created_at FROM orders
WHERE customer_id = 777
ORDER BY created_at DESC
LIMIT 3;

-- (c) Only the SECOND column: the index is sorted by customer_id first, so rows for
--     one day are scattered across the whole index -> no efficient lookup.
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT count(*) FROM orders
WHERE created_at >= '2024-06-01' AND created_at < '2024-06-01 00:10';

-- (d) Fix for (c): an index that LEADS with created_at.
CREATE INDEX orders_created_idx ON orders (created_at);
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT count(*) FROM orders
WHERE created_at >= '2024-06-01' AND created_at < '2024-06-01 00:10';

-- (e) Column order matters for mixed equality + range filters. Compare the index
--     pages read by the same query with (customer_id, created_at) versus the
--     reversed order (created_at, customer_id).
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id FROM orders
WHERE customer_id = 777 AND created_at >= '2024-06-01';

DROP INDEX orders_cust_created_idx, orders_created_idx;
CREATE INDEX orders_created_cust_idx ON orders (created_at, customer_id);
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id FROM orders
WHERE customer_id = 777 AND created_at >= '2024-06-01';
