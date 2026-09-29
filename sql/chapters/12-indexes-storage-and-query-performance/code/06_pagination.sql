-- 12.10 OFFSET pagination vs keyset (seek) pagination.
SET max_parallel_workers_per_gather = 0;
SET jit = off;
CREATE INDEX orders_created_id_idx ON orders (created_at, id);
ANALYZE orders;

-- plan_stats(query) is defined in 00_schema.sql: it runs EXPLAIN (ANALYZE, BUFFERS)
-- and returns the execution time and the number of 8 KB pages touched.

-- Page N of the "newest first" order history, 20 rows per page.
SELECT 'OFFSET, page ' || page AS method, s.*
FROM (VALUES (1), (100), (10000), (49999)) AS p(page),
     plan_stats(format(
       'SELECT id, created_at, amount FROM orders
        ORDER BY created_at DESC, id DESC LIMIT 20 OFFSET %s', (page - 1) * 20)) AS s;

-- Keyset: remember the last row of the previous page and seek past it.
-- Fetch the last row of page 49,998 (psql \gset stores it in variables).
SELECT created_at AS last_ts, id AS last_id FROM orders
ORDER BY created_at DESC, id DESC LIMIT 1 OFFSET 49998 * 20 - 1 \gset
SELECT :'last_ts' AS last_ts, :last_id AS last_id;

SELECT 'keyset, page 49999' AS method, s.*
FROM plan_stats(format(
  'SELECT id, created_at, amount FROM orders
   WHERE (created_at, id) < (%L, %s)
   ORDER BY created_at DESC, id DESC LIMIT 20', :'last_ts', :last_id)) AS s;

-- Both methods return exactly the same 20 rows (EXCEPT finds no difference):
SELECT count(*) AS rows_that_differ FROM (
  (SELECT id FROM orders ORDER BY created_at DESC, id DESC LIMIT 20 OFFSET 49998 * 20)
  EXCEPT
  (SELECT id FROM orders WHERE (created_at, id) < (:'last_ts', :last_id)
   ORDER BY created_at DESC, id DESC LIMIT 20)) d;

EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id, created_at, amount FROM orders
WHERE (created_at, id) < (:'last_ts', :last_id)
ORDER BY created_at DESC, id DESC LIMIT 20;
