-- 1.x  First queries against the sample database
\pset null '(null)'
SELECT version();

-- A whole (small) table
SELECT * FROM departments;

-- Two tables linked only by a shared value: which products did customer 1 buy?
SELECT c.first_name, c.last_name, o.order_id, p.name AS product
FROM customers   c
JOIN orders      o  ON o.customer_id = c.customer_id
JOIN order_items oi ON oi.order_id   = o.order_id
JOIN products    p  ON p.product_id  = oi.product_id
WHERE c.customer_id = 1
ORDER BY o.order_id, p.name;
