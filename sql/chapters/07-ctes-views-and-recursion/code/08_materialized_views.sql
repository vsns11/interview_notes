-- 7.8  Materialized views: a saved RESULT that you refresh
\pset null '(null)'
CREATE MATERIALIZED VIEW product_sales AS
SELECT p.product_id, p.name,
       coalesce(sum(oi.quantity), 0) AS units_sold,
       coalesce(round(sum(oi.quantity * oi.unit_price * (1 - oi.discount)), 2), 0) AS revenue
FROM products p
LEFT JOIN order_items oi USING (product_id)          -- every order line
GROUP BY p.product_id, p.name;

SELECT name, units_sold, revenue FROM product_sales ORDER BY revenue DESC LIMIT 3;

-- New data arrives...
INSERT INTO orders VALUES (115, 9, NULL, '2025-06-20 09:00', 'pending', NULL);
INSERT INTO order_items VALUES (115, 5, 3, 499.00, 0.00);

-- ...but the materialized view still shows the old snapshot
SELECT name, units_sold, revenue FROM product_sales WHERE name = 'Standing Desk';

REFRESH MATERIALIZED VIEW product_sales;
SELECT name, units_sold, revenue FROM product_sales WHERE name = 'Standing Desk';

-- CONCURRENTLY lets readers keep reading during the refresh, but needs a unique index
\set ON_ERROR_STOP 0
REFRESH MATERIALIZED VIEW CONCURRENTLY product_sales;
\set ON_ERROR_STOP 1
CREATE UNIQUE INDEX product_sales_pk ON product_sales (product_id);
REFRESH MATERIALIZED VIEW CONCURRENTLY product_sales;

-- Materialized views can be indexed like tables
CREATE INDEX product_sales_revenue ON product_sales (revenue DESC);

-- They are listed separately from ordinary views
SELECT matviewname, ispopulated FROM pg_matviews;
