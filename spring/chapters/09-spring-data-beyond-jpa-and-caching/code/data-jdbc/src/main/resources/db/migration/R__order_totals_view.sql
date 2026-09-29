-- Repeatable migration: re-applied whenever its checksum changes (views, functions)
CREATE OR REPLACE VIEW order_totals AS
SELECT o.id AS order_id, c.name AS customer, SUM(l.quantity * l.unit_price) AS total
FROM purchase_order o
JOIN customer c ON c.id = o.customer_id
JOIN order_line l ON l.purchase_order_id = o.id
GROUP BY o.id, c.name;
