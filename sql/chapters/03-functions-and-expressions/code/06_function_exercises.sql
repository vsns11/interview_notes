-- 3.6  Worked exercises combining functions (aggregates preview chapter 4)
\pset null '(null)'
-- E1: total value of delivered orders, rounded to 2 decimals and shown with thousands separators
SELECT to_char(round(sum(quantity * unit_price * (1 - discount)), 2), 'FM999,999,990.00') AS total_sales
FROM order_items oi
JOIN orders o USING (order_id)
WHERE o.status = 'delivered';

-- E2: square root of the number of order lines, natural log of total revenue
SELECT round(sqrt(count(*))::numeric, 3) AS sqrt_lines,
       round(ln(sum(quantity * unit_price * (1 - discount))), 4) AS ln_revenue
FROM order_items;

-- E3: customer display name "Shah, A." and email domain in lower case
SELECT customer_id,
       last_name || ', ' || left(first_name, 1) || '.' AS display_name,
       lower(split_part(trim(email), '@', 2))          AS domain
FROM customers ORDER BY customer_id LIMIT 5;

-- E4: revenue per year and month (not cancelled)
SELECT extract(YEAR FROM o.placed_at)  AS year,
       extract(MONTH FROM o.placed_at) AS month,
       round(sum(oi.quantity * oi.unit_price * (1 - oi.discount)), 2) AS revenue
FROM orders o
JOIN order_items oi USING (order_id)
WHERE o.status <> 'cancelled'
GROUP BY 1, 2
ORDER BY 1, 2;

-- E5: the same, grouped by the first day of each month (one sortable column)
SELECT to_char(date_trunc('month', o.placed_at), 'YYYY-MM') AS month,
       count(DISTINCT o.order_id) AS orders
FROM orders o
WHERE o.status <> 'cancelled'
GROUP BY date_trunc('month', o.placed_at)
ORDER BY date_trunc('month', o.placed_at);
