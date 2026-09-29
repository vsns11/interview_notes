-- 12.8 Covering (INCLUDE), partial and expression indexes.
SET max_parallel_workers_per_gather = 0;
SET jit = off;

-- (a) Covering index: key = customer_id, payload = amount (not part of the sort key).
CREATE INDEX orders_cust_idx ON orders (customer_id);
ANALYZE orders;
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT sum(amount) FROM orders WHERE customer_id BETWEEN 5000 AND 5999;

CREATE INDEX orders_cust_incl_idx ON orders (customer_id) INCLUDE (amount);
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT sum(amount) FROM orders WHERE customer_id BETWEEN 5000 AND 5999;

-- (b) Partial index: only the 2 % of rows a work queue cares about.
CREATE INDEX orders_pending_idx ON orders (created_at) WHERE status = 'pending';
CREATE INDEX orders_status_created_idx ON orders (status, created_at);   -- full-size rival
SELECT indexrelid::regclass AS index_name, pg_size_pretty(pg_relation_size(indexrelid)) AS size
FROM pg_index WHERE indexrelid::regclass::text IN ('orders_pending_idx', 'orders_status_created_idx');

EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id, created_at FROM orders
WHERE status = 'pending' ORDER BY created_at LIMIT 5;

-- A query whose WHERE does not imply the index predicate cannot use it:
DROP INDEX orders_status_created_idx;
EXPLAIN (COSTS OFF)
SELECT id FROM orders WHERE status = 'refunded' ORDER BY created_at LIMIT 5;

-- (c) Partial UNIQUE index: "at most one pending order per customer" would be
--     CREATE UNIQUE INDEX ... ON orders (customer_id) WHERE status = 'pending';
--     (not created here: our generated data has several pending orders per customer)
SELECT count(*) AS customers_with_2plus_pending
FROM (SELECT customer_id FROM orders WHERE status = 'pending'
      GROUP BY customer_id HAVING count(*) > 1) t;

-- (d) Expression index: case-insensitive e-mail lookup.
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id FROM customers WHERE lower(email) = 'user4242@mail.com';

CREATE INDEX customers_lower_email_idx ON customers (lower(email));
ANALYZE customers;   -- also gathers statistics on the expression lower(email)
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id FROM customers WHERE lower(email) = 'user4242@mail.com';

-- The expression must match exactly: upper() cannot use the lower() index.
EXPLAIN (COSTS OFF)
SELECT id FROM customers WHERE upper(email) = 'USER4242@MAIL.COM';
