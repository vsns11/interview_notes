-- 9.6  DROP and TRUNCATE (and how they differ from DELETE)
\pset null '(null)'
CREATE TABLE audit_log (
    log_id  bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    msg     text NOT NULL
);
INSERT INTO audit_log (msg) SELECT 'event ' || g FROM generate_series(1, 5) g;

-- TRUNCATE empties the table but keeps it (structure, indexes, grants)
TRUNCATE audit_log;
SELECT count(*) AS rows_after_truncate FROM audit_log;
INSERT INTO audit_log (msg) VALUES ('after truncate') RETURNING log_id;   -- identity continues

-- RESTART IDENTITY resets the counter too
TRUNCATE audit_log RESTART IDENTITY;
INSERT INTO audit_log (msg) VALUES ('after restart') RETURNING log_id;

-- In PostgreSQL, TRUNCATE is transactional: it can be rolled back
BEGIN;
TRUNCATE audit_log;
SELECT count(*) AS inside_tx FROM audit_log;
ROLLBACK;
SELECT count(*) AS after_rollback FROM audit_log;

-- A table referenced by a foreign key can't be truncated alone
\set ON_ERROR_STOP 0
TRUNCATE customers;
\set ON_ERROR_STOP 1
BEGIN;
TRUNCATE customers CASCADE;          -- also truncates orders and order_items (they reference it)
SELECT (SELECT count(*) FROM customers) AS customers,
       (SELECT count(*) FROM orders) AS orders,
       (SELECT count(*) FROM order_items) AS items;
ROLLBACK;

-- DROP removes the table itself
DROP TABLE audit_log;
\set ON_ERROR_STOP 0
SELECT * FROM audit_log;
DROP TABLE audit_log;                -- error: already gone
\set ON_ERROR_STOP 1
DROP TABLE IF EXISTS audit_log;      -- notice only

-- Dependencies: DROP refuses while other objects need the table ...
CREATE VIEW big_spenders AS SELECT customer_id FROM orders GROUP BY customer_id HAVING count(*) > 2;
\set ON_ERROR_STOP 0
DROP TABLE orders;
\set ON_ERROR_STOP 1
-- ... CASCADE drops the dependents too (here: the view and order_items' foreign key)
BEGIN;
DROP TABLE orders CASCADE;
SELECT count(*) AS order_items_still_there FROM order_items;
ROLLBACK;

-- DROP COLUMN / DROP SCHEMA / DROP DATABASE follow the same pattern
CREATE SCHEMA tmp_area;
CREATE TABLE tmp_area.t (x int);
DROP SCHEMA tmp_area CASCADE;
