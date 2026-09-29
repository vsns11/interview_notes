-- 1.x  The DBMS enforces the rules for every application
\set ON_ERROR_STOP 0

-- Referential integrity: customer 99 does not exist
INSERT INTO orders (order_id, customer_id, placed_at, status)
VALUES (200, 99, '2025-07-01 10:00', 'pending');

-- Deleting a customer who still has orders is refused
DELETE FROM customers WHERE customer_id = 1;

-- Domain / CHECK rules
INSERT INTO orders (order_id, customer_id, placed_at, status)
VALUES (201, 1, '2025-07-01 10:00', 'lost');

-- Entity integrity: primary key must be unique and not NULL
INSERT INTO departments VALUES (1, 'Legal', 'Pune', 1000);
INSERT INTO departments VALUES (NULL, 'Legal', 'Pune', 1000);

\set ON_ERROR_STOP 1
SELECT count(*) AS orders_still FROM orders;
