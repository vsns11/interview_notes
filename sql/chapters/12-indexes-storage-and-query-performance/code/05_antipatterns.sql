-- 12.9 Queries that silently stop using an index, and how to fix them.
SET max_parallel_workers_per_gather = 0;
SET jit = off;
CREATE INDEX orders_created_idx ON orders (created_at);
CREATE INDEX customers_email_idx ON customers (email);
ANALYZE;

-- (a) Function / cast wrapped around the indexed column: index unusable.
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT count(*) FROM orders WHERE created_at::date = DATE '2024-06-01';

-- Fix: leave the column bare, move the work to the constant side (half-open range).
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT count(*) FROM orders
WHERE created_at >= DATE '2024-06-01' AND created_at < DATE '2024-06-02';

-- (b) Arithmetic on the column is the same problem.
EXPLAIN (COSTS OFF) SELECT * FROM orders WHERE id + 1 = 500001;
EXPLAIN (COSTS OFF) SELECT * FROM orders WHERE id = 500001 - 1;

-- (c) LIKE with a fixed prefix can use a B-tree (this database uses the C collation;
--     with other collations create the index with text_pattern_ops).
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id, email FROM customers WHERE email LIKE 'User4242%';

-- (d) Leading wildcard: the B-tree is sorted by the START of the string, useless here.
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id, email FROM customers WHERE email LIKE '%4242@corp%';

-- Fix: a trigram GIN index (pg_trgm) indexes every 3-character piece of the text.
CREATE EXTENSION IF NOT EXISTS pg_trgm;
SELECT show_trgm('User42');
CREATE INDEX customers_email_trgm_idx ON customers USING gin (email gin_trgm_ops);
ANALYZE customers;
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id, email FROM customers WHERE email LIKE '%4242@corp%';
SELECT id, email FROM customers WHERE email LIKE '%4242@corp%' ORDER BY id;

-- (e) OR across two columns: if either side lacks an index the whole query
--     falls back to a sequential scan...
EXPLAIN (COSTS OFF)
SELECT id FROM customers WHERE email = 'User7@shop.io' OR name = 'ben #9';
-- ...if both sides are indexed, the planner ORs two bitmaps together.
EXPLAIN (COSTS OFF)
SELECT id FROM customers WHERE email = 'User7@shop.io' OR id = 99;
