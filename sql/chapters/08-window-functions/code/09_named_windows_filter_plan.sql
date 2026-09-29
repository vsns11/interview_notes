-- 8.10  WINDOW clause, FILTER, evaluation order and the plan
-- Named window reused by several functions; a second window extends it
SELECT store, day, amount,
       sum(amount) OVER w_run    AS running_total,
       max(amount) OVER w_run    AS best_so_far,
       rank()      OVER w_amount AS amount_rank
FROM daily_sales
WHERE store = 'Pune'
WINDOW w_run    AS (PARTITION BY store ORDER BY day),
       w_amount AS (PARTITION BY store ORDER BY amount DESC)
ORDER BY day;

-- FILTER works on window aggregates too: running count of big days
SELECT day, amount,
       count(*) FILTER (WHERE amount >= 1500) OVER (ORDER BY day) AS big_days_so_far
FROM daily_sales
WHERE store = 'Mumbai'
ORDER BY day;

-- Windows see rows AFTER WHERE / GROUP BY / HAVING: the filter changes the result
SELECT first_name, salary, rank() OVER (ORDER BY salary DESC) AS rank_all
FROM employees WHERE dept_id = 1
ORDER BY salary DESC;

-- The plan: sort by PARTITION BY + ORDER BY keys, then WindowAgg
EXPLAIN (COSTS OFF)
SELECT store, day, sum(amount) OVER (PARTITION BY store ORDER BY day)
FROM daily_sales;

EXPLAIN (COSTS OFF)
SELECT first_name, rank() OVER (PARTITION BY dept_id ORDER BY salary DESC)
FROM employees;
