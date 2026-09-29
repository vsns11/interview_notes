-- 9.7  Index DDL in brief (chapter 12 explains how and when indexes help)
\pset null '(null)'
CREATE INDEX idx_employees_last_name ON employees (last_name);
CREATE INDEX idx_orders_customer_placed ON orders (customer_id, placed_at DESC);   -- composite

-- A UNIQUE index can enforce rules a UNIQUE constraint can't, e.g. case-insensitive emails.
-- It fails here because the sample data really has a duplicate (customer 1 and 8):
\set ON_ERROR_STOP 0
CREATE UNIQUE INDEX idx_customers_email_ci ON customers (lower(trim(email)));
\set ON_ERROR_STOP 1
UPDATE customers SET email = NULL WHERE customer_id = 8;        -- clean the duplicate first
CREATE UNIQUE INDEX idx_customers_email_ci ON customers (lower(trim(email)));

-- Every PRIMARY KEY / UNIQUE constraint created an index automatically
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename IN ('employees', 'customers')
ORDER BY tablename, indexname;

-- PostgreSQL has no permanently "clustered" index; CLUSTER re-sorts the table once
CLUSTER orders USING idx_orders_customer_placed;

-- CONCURRENTLY builds/drops without blocking writes (cannot run inside a transaction)
DROP INDEX CONCURRENTLY idx_employees_last_name;
CREATE INDEX CONCURRENTLY idx_employees_last_name ON employees (last_name);
DROP INDEX IF EXISTS idx_employees_last_name;
