-- Case study: e-commerce (schema "shop" in 00_schema.sql)
SET search_path = shop;
SET timezone = 'UTC';

\echo '--- Q1: order history for one customer, newest first (uses the (customer_id, placed_at) index)'
SELECT o.order_id, o.status, o.placed_at::date AS placed, o.total,
       count(*) AS lines, sum(oi.quantity) AS items
FROM orders o JOIN order_items oi USING (order_id)
WHERE o.customer_id = 1
GROUP BY o.order_id
ORDER BY o.placed_at DESC;

\echo '--- Q2: place an order atomically: check + decrement stock, snapshot the price, update the total'
BEGIN;
INSERT INTO orders (customer_id, ship_to, placed_at)
VALUES (2, 2, '2026-05-05 12:00+00') RETURNING order_id \gset
WITH cart(product_id, qty) AS (VALUES (1::bigint, 2), (3::bigint, 1)),
reserved AS (                     -- only succeeds for products with enough stock
  UPDATE products p SET stock = p.stock - c.qty
  FROM cart c
  WHERE p.product_id = c.product_id AND p.stock >= c.qty AND p.deleted_at IS NULL
  RETURNING p.product_id, c.qty, p.price
)
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT :order_id, product_id, qty, price FROM reserved
RETURNING order_id, product_id, quantity, unit_price;
-- (the application compares the row count with the cart size and issues ROLLBACK if short)
UPDATE orders SET total = (SELECT sum(quantity * unit_price) FROM order_items WHERE order_id = :order_id)
WHERE order_id = :order_id RETURNING order_id, total;
COMMIT;
SELECT sku, stock FROM products WHERE product_id IN (1, 3) ORDER BY sku;

\echo '--- Q3: a CHECK constraint stops overselling (speaker has stock 0)'
\set ON_ERROR_STOP off
UPDATE products SET stock = stock - 1 WHERE sku = 'SP-200';
\set ON_ERROR_STOP on

\echo '--- Q4: best sellers by revenue, excluding cancelled orders (deleted products still count)'
SELECT p.name, p.deleted_at IS NOT NULL AS discontinued,
       sum(oi.quantity) AS units, sum(oi.quantity * oi.unit_price) AS revenue
FROM order_items oi
JOIN orders o   USING (order_id)
JOIN products p USING (product_id)
WHERE o.status <> 'cancelled'
GROUP BY p.product_id
ORDER BY revenue DESC;

\echo '--- Q5: every product under "Electronics" (recursive walk down the category tree)'
WITH RECURSIVE tree AS (
  SELECT category_id, name, 1 AS depth FROM categories WHERE name = 'Electronics'
  UNION ALL
  SELECT c.category_id, c.name, t.depth + 1 FROM categories c JOIN tree t ON c.parent_id = t.category_id
)
SELECT t.name AS category, t.depth, p.name AS product, p.price
FROM tree t JOIN products p USING (category_id)
WHERE p.deleted_at IS NULL
ORDER BY t.depth, p.name;

\echo '--- Q6: paid orders whose payments do not add up to the order total (reconciliation)'
SELECT o.order_id, o.total, coalesce(sum(pay.amount), 0) AS paid
FROM orders o LEFT JOIN payments pay USING (order_id)
WHERE o.status IN ('paid', 'shipped', 'delivered')
GROUP BY o.order_id
HAVING coalesce(sum(pay.amount), 0) <> o.total
ORDER BY o.order_id;
