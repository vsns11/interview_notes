-- 12.3 File organization in practice: heap order, correlation and CLUSTER.
SET max_parallel_workers_per_gather = 0;
SET jit = off;
CREATE INDEX orders_customer_idx ON orders (customer_id);
ANALYZE orders;

-- Heap file: rows sit in insertion order, so one customer's orders are spread out.
SELECT correlation FROM pg_stats WHERE tablename = 'orders' AND attname = 'customer_id';
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT sum(amount) FROM orders WHERE customer_id BETWEEN 1000 AND 1999;

-- CLUSTER rewrites the table in index order (a one-off sort; later inserts are
-- NOT kept in order, and it takes an exclusive lock while it runs).
CLUSTER orders USING orders_customer_idx;
ANALYZE orders;
SELECT correlation FROM pg_stats WHERE tablename = 'orders' AND attname = 'customer_id';
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT sum(amount) FROM orders WHERE customer_id BETWEEN 1000 AND 1999;

-- fillfactor leaves free space in each page so updates can stay on the same page
-- (heap-only tuples, see chapter 13). Default 100 for tables.
CREATE TABLE ff100 (id int, v text);
CREATE TABLE ff70  (id int, v text) WITH (fillfactor = 70);
INSERT INTO ff100 SELECT g, 'x' FROM generate_series(1, 100000) g;
INSERT INTO ff70  SELECT g, 'x' FROM generate_series(1, 100000) g;
SELECT 'ff100' AS t, pg_relation_size('ff100') / 8192 AS pages
UNION ALL SELECT 'ff70', pg_relation_size('ff70') / 8192;
