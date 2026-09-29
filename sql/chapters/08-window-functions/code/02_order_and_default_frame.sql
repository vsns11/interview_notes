-- 8.3  ORDER BY inside OVER turns an aggregate into a running aggregate
SELECT store, day, amount,
       sum(amount) OVER (PARTITION BY store ORDER BY day) AS running_total,
       count(*)    OVER (PARTITION BY store ORDER BY day) AS days_so_far
FROM daily_sales
WHERE store = 'Mumbai'
ORDER BY day;

-- The default frame is RANGE ... CURRENT ROW, which includes PEERS (ties).
-- Arjun and Sara earn the same, so they get the same "running" total:
SELECT first_name, salary,
       sum(salary) OVER (ORDER BY salary)                    AS default_range,
       sum(salary) OVER (ORDER BY salary
                         ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS rows_frame,
       sum(salary) OVER (ORDER BY salary, emp_id)           AS unique_order
FROM employees
WHERE dept_id = 2
ORDER BY salary, emp_id;
