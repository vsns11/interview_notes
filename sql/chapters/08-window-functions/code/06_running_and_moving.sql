-- 8.7  Running totals, moving averages, share of total, growth
\pset null '(null)'
SELECT store, day, amount,
       sum(amount) OVER (PARTITION BY store ORDER BY day
                         ROWS UNBOUNDED PRECEDING)                         AS running_total,
       round(avg(amount) OVER (PARTITION BY store ORDER BY day
                         ROWS BETWEEN 2 PRECEDING AND CURRENT ROW), 1)     AS moving_avg_3,
       count(*) OVER (PARTITION BY store ORDER BY day
                         ROWS BETWEEN 2 PRECEDING AND CURRENT ROW)         AS rows_in_avg,
       round(100.0 * sum(amount) OVER (PARTITION BY store ORDER BY day ROWS UNBOUNDED PRECEDING)
                   / sum(amount) OVER (PARTITION BY store), 1)             AS pct_of_week_so_far
FROM daily_sales
ORDER BY store, day;

-- Only show a 3-day average once 3 days exist
SELECT day, amount,
       CASE WHEN count(*) OVER w = 3 THEN round(avg(amount) OVER w, 1) END AS full_3day_avg
FROM daily_sales
WHERE store = 'Mumbai'
WINDOW w AS (ORDER BY day ROWS BETWEEN 2 PRECEDING AND CURRENT ROW)
ORDER BY day;

-- Windows over a GROUP BY result: aggregates first, then windows over the groups.
-- sum(sum(x)) OVER (...) = a window sum over the per-month sums.
SELECT date_trunc('month', o.placed_at)::date AS month,
       sum(oi.quantity * oi.unit_price * (1 - oi.discount))::numeric(10,2) AS revenue,
       sum(sum(oi.quantity * oi.unit_price * (1 - oi.discount)))
           OVER (ORDER BY date_trunc('month', o.placed_at)::date)::numeric(10,2) AS running_revenue
FROM orders o JOIN order_items oi USING (order_id)
WHERE o.status <> 'cancelled'
GROUP BY 1
ORDER BY 1;

-- Month-over-month growth is easier to read with a CTE
WITH monthly AS (
    SELECT date_trunc('month', o.placed_at)::date AS month,
           sum(oi.quantity * oi.unit_price * (1 - oi.discount)) AS revenue
    FROM orders o JOIN order_items oi USING (order_id)
    WHERE o.status <> 'cancelled'
    GROUP BY 1
)
SELECT month, round(revenue, 2) AS revenue,
       round(lag(revenue) OVER (ORDER BY month), 2) AS prev_month,
       round(100.0 * (revenue / lag(revenue) OVER (ORDER BY month) - 1), 1) AS mom_growth_pct
FROM monthly
ORDER BY month;
