-- 8.6  LAG / LEAD / FIRST_VALUE / LAST_VALUE / NTH_VALUE
\pset null '(null)'

-- Day-over-day change per store
SELECT store, day, amount,
       lag(amount)  OVER w          AS prev_day,
       amount - lag(amount) OVER w  AS change,
       lead(amount) OVER w          AS next_day,
       lag(amount, 2, 0) OVER w     AS two_days_ago_or_0
FROM daily_sales
WHERE day <= '2025-03-04'
WINDOW w AS (PARTITION BY store ORDER BY day)
ORDER BY store, day;

-- FIRST_VALUE works as expected, LAST_VALUE surprises with the default frame
SELECT day, amount,
       first_value(amount) OVER (ORDER BY day)    AS first_amt,
       last_value(amount)  OVER (ORDER BY day)    AS last_default_frame,
       last_value(amount)  OVER (ORDER BY day
            ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_whole_partition,
       nth_value(amount, 2) OVER (ORDER BY day
            ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS second_amt
FROM daily_sales
WHERE store = 'Pune'
ORDER BY day;

-- Compare each employee with the best-paid person in the department
SELECT dept_id, first_name, salary,
       first_value(first_name) OVER w AS top_earner,
       first_value(salary) OVER w - salary AS gap_to_top
FROM employees
WHERE dept_id IN (1, 3)
WINDOW w AS (PARTITION BY dept_id ORDER BY salary DESC)
ORDER BY dept_id, salary DESC;

-- Time between a customer's orders
SELECT customer_id, order_id, placed_at::date AS placed,
       placed_at::date - lag(placed_at::date) OVER (PARTITION BY customer_id ORDER BY placed_at) AS days_since_prev
FROM orders
WHERE customer_id IN (1, 2, 5)
ORDER BY customer_id, placed_at;
