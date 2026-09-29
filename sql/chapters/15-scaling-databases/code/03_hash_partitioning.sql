-- 15.2 Hash partitioning: spread orders evenly over 4 partitions by order_id.
CREATE TABLE orders_hashed (
  order_id    int  NOT NULL,
  customer_id int  NOT NULL,
  order_date  date NOT NULL,
  amount      numeric(10,2) NOT NULL
) PARTITION BY HASH (order_id);

CREATE TABLE orders_h0 PARTITION OF orders_hashed FOR VALUES WITH (MODULUS 4, REMAINDER 0);
CREATE TABLE orders_h1 PARTITION OF orders_hashed FOR VALUES WITH (MODULUS 4, REMAINDER 1);
CREATE TABLE orders_h2 PARTITION OF orders_hashed FOR VALUES WITH (MODULUS 4, REMAINDER 2);
CREATE TABLE orders_h3 PARTITION OF orders_hashed FOR VALUES WITH (MODULUS 4, REMAINDER 3);

INSERT INTO orders_hashed SELECT * FROM orders;
ANALYZE orders_hashed;

SELECT tableoid::regclass AS partition, count(*) AS rows
FROM orders_hashed GROUP BY 1 ORDER BY 1;

-- Equality on the hash key prunes to one partition ...
EXPLAIN (COSTS OFF) SELECT * FROM orders_hashed WHERE order_id = 2024;
-- ... but a range on the hash key cannot be pruned (hash destroys order).
EXPLAIN (COSTS OFF) SELECT count(*) FROM orders_hashed WHERE order_id BETWEEN 100 AND 110;
