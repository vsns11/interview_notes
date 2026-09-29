-- 14.7  Pivot (rows -> columns) and unpivot (columns -> rows)

-- The long ("tall") data: revenue per category per month
CREATE VIEW monthly_category_revenue AS
SELECT coalesce(p.category, '(none)')               AS category,
       to_char(o.placed_at, 'YYYY-MM')              AS month,
       sum(oi.quantity * oi.unit_price * (1 - oi.discount)) AS revenue
FROM   orders o JOIN order_items oi USING (order_id) JOIN products p USING (product_id)
WHERE  o.status <> 'cancelled'
GROUP  BY 1, 2;

SELECT * FROM monthly_category_revenue WHERE month <= '2025-02' ORDER BY month, category;

-- (a) Pivot with FILTER (PostgreSQL, SQLite 3.30+): one aggregate per output column
SELECT category,
       round(sum(revenue) FILTER (WHERE month = '2025-01'), 2) AS jan,
       round(sum(revenue) FILTER (WHERE month = '2025-02'), 2) AS feb,
       round(sum(revenue) FILTER (WHERE month = '2025-03'), 2) AS mar,
       round(sum(revenue) FILTER (WHERE month = '2025-04'), 2) AS apr,
       round(sum(revenue), 2)                                  AS all_months
FROM   monthly_category_revenue
GROUP  BY category ORDER BY category;

-- (b) The same pivot with CASE (portable to every database)
SELECT category,
       round(sum(CASE WHEN month = '2025-01' THEN revenue ELSE 0 END), 2) AS jan,
       round(sum(CASE WHEN month = '2025-02' THEN revenue ELSE 0 END), 2) AS feb
FROM   monthly_category_revenue
GROUP  BY category ORDER BY category;

-- (c) Order status counts per customer as columns
SELECT c.first_name,
       count(*) FILTER (WHERE o.status = 'delivered') AS delivered,
       count(*) FILTER (WHERE o.status = 'shipped')   AS shipped,
       count(*) FILTER (WHERE o.status = 'pending')   AS pending,
       count(*) FILTER (WHERE o.status = 'cancelled') AS cancelled
FROM   customers c JOIN orders o USING (customer_id)
GROUP  BY c.customer_id, c.first_name ORDER BY c.customer_id;

-- (d) Unpivot: turn the columns of a wide table back into rows with LATERAL (VALUES ...)
CREATE TABLE quarterly_targets (region text PRIMARY KEY, q1 int, q2 int, q3 int, q4 int);
INSERT INTO quarterly_targets VALUES ('India', 120, 150, 170, 200), ('UK', 60, 65, NULL, 80);

SELECT t.region, v.quarter, v.target
FROM   quarterly_targets t
CROSS  JOIN LATERAL (VALUES ('Q1', t.q1), ('Q2', t.q2), ('Q3', t.q3), ('Q4', t.q4)) AS v(quarter, target)
WHERE  v.target IS NOT NULL
ORDER  BY t.region, v.quarter;

-- (e) Dynamic pivot: when the column list is not known in advance, build one JSON object per row
SELECT category, jsonb_object_agg(month, round(revenue, 2) ORDER BY month) AS by_month
FROM   monthly_category_revenue GROUP BY category ORDER BY category;
