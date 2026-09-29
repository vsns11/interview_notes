-- 4.4  Conditional aggregation: FILTER and CASE
\pset null '(null)'

-- One pass, many counts: order status breakdown per customer (FILTER, PostgreSQL 9.4+)
SELECT customer_id,
       count(*)                                       AS total,
       count(*) FILTER (WHERE status = 'delivered')   AS delivered,
       count(*) FILTER (WHERE status = 'cancelled')   AS cancelled,
       count(*) FILTER (WHERE sales_rep_id IS NULL)   AS online
FROM orders
GROUP BY customer_id
ORDER BY customer_id;

-- The portable form: CASE inside the aggregate (works in every database)
SELECT customer_id,
       count(*)                                          AS total,
       sum(CASE WHEN status = 'delivered' THEN 1 ELSE 0 END) AS delivered,
       count(CASE WHEN status = 'cancelled' THEN 1 END)      AS cancelled
FROM orders
GROUP BY customer_id
ORDER BY customer_id;

-- Ratios: share of each department's payroll paid to people hired before 2020
SELECT dept_id,
       sum(salary) AS payroll,
       round(100.0 * sum(salary) FILTER (WHERE hire_date < '2020-01-01') / sum(salary), 1) AS pct_veterans
FROM employees
WHERE dept_id IS NOT NULL
GROUP BY dept_id
ORDER BY dept_id;

-- Pivot-style report: units sold per category, one column per status
SELECT p.category,
       sum(oi.quantity) FILTER (WHERE o.status = 'delivered') AS delivered_units,
       sum(oi.quantity) FILTER (WHERE o.status = 'shipped')   AS shipped_units,
       sum(oi.quantity) FILTER (WHERE o.status = 'pending')   AS pending_units
FROM order_items oi
JOIN orders o   ON o.order_id = oi.order_id
JOIN products p ON p.product_id = oi.product_id
GROUP BY p.category
ORDER BY p.category NULLS LAST;
