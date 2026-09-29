-- 9.10  DELETE
\pset null '(null)'
-- Delete matching rows; RETURNING shows what was removed
DELETE FROM order_items
WHERE order_id = 105
RETURNING order_id, product_id, quantity;

-- Parent rows that are still referenced cannot be deleted (FK, default NO ACTION)
\set ON_ERROR_STOP 0
DELETE FROM customers WHERE customer_id = 1;
\set ON_ERROR_STOP 1

-- DELETE ... USING: delete based on another table (PostgreSQL join-delete)
DELETE FROM order_items oi
USING orders o
WHERE o.order_id = oi.order_id
  AND o.status = 'cancelled'
RETURNING oi.order_id, oi.product_id;

-- The portable form: a subquery
DELETE FROM orders
WHERE status = 'cancelled'
  AND order_id NOT IN (SELECT order_id FROM order_items)
RETURNING order_id;

-- Customers who never ordered
DELETE FROM customers c
WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.customer_id = c.customer_id)
RETURNING customer_id, first_name;

-- DELETE without WHERE removes every row (but keeps the table). Practise in a transaction.
BEGIN;
DELETE FROM order_items;
SELECT count(*) AS items_inside_tx FROM order_items;
ROLLBACK;
SELECT count(*) AS items_after_rollback FROM order_items;

-- Soft delete: mark rows instead of removing them
ALTER TABLE products ADD COLUMN deleted_at timestamp;
UPDATE products SET deleted_at = '2025-06-30 12:00' WHERE name = 'Desk Lamp';
CREATE VIEW active_products AS SELECT * FROM products WHERE deleted_at IS NULL;
SELECT count(*) AS all_products, (SELECT count(*) FROM active_products) AS active FROM products;
