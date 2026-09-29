-- 15.4 Distributed query processing, made visible with postgres_fdw.
-- We pretend schema "shard_b" lives on another server: postgres_fdw connects back to this
-- same private server (socket /tmp, port 5544, db ch_15) so the demo needs only one Postgres.
-- In production the OPTIONS would name a genuinely different host.
CREATE EXTENSION IF NOT EXISTS postgres_fdw;

-- The "remote" node holds the orders.
CREATE SCHEMA shard_b;
CREATE TABLE shard_b.orders AS SELECT * FROM orders;
ALTER TABLE shard_b.orders ADD PRIMARY KEY (order_id);
CREATE INDEX ON shard_b.orders (customer_id);
ANALYZE shard_b.orders;

CREATE SERVER node_b FOREIGN DATA WRAPPER postgres_fdw
  OPTIONS (host '/tmp', port '5544', dbname 'ch_15');
CREATE USER MAPPING FOR CURRENT_USER SERVER node_b OPTIONS (user 'postgres');

-- The local node sees the remote table through a foreign table.
CREATE SCHEMA remote;
IMPORT FOREIGN SCHEMA shard_b LIMIT TO (orders) FROM SERVER node_b INTO remote;

-- 1) Predicate push-down: the WHERE clause travels to the remote node ("Remote SQL").
EXPLAIN (VERBOSE, COSTS OFF)
SELECT order_id, amount FROM remote.orders WHERE amount > 500;

-- 2) Aggregate push-down: only one row per group crosses the network.
EXPLAIN (VERBOSE, COSTS OFF)
SELECT customer_id, count(*), sum(amount) FROM remote.orders GROUP BY customer_id;

-- 3) Cross-node join: customers are local, orders are remote.
--    "Orders placed by customers from the USA" -> local filter, then remote lookup.
SELECT c.name, count(*) AS orders, sum(o.amount) AS total
FROM customers c JOIN remote.orders o ON o.customer_id = c.customer_id
WHERE c.country = 'USA'
GROUP BY c.name ORDER BY c.name;

EXPLAIN (VERBOSE, COSTS OFF)
SELECT c.name, o.order_id
FROM customers c JOIN remote.orders o ON o.customer_id = c.customer_id
WHERE c.country = 'USA';

-- 4) The shipped plan above pulls every remote order across the network.
--    Better: first find the USA customer ids locally, then ship only those ids
--    to the remote node (a "semi-join" strategy).
EXPLAIN (VERBOSE, COSTS OFF)
SELECT o.order_id, o.customer_id
FROM remote.orders o
WHERE o.customer_id = ANY (ARRAY(SELECT customer_id FROM customers WHERE country = 'USA'));

SELECT count(*) AS usa_orders
FROM remote.orders o
WHERE o.customer_id = ANY (ARRAY(SELECT customer_id FROM customers WHERE country = 'USA'));
