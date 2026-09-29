-- 4.3  HAVING: filter groups after aggregation
\pset null '(null)'

-- Departments with more than two people
SELECT dept_id, count(*) AS headcount
FROM employees
GROUP BY dept_id
HAVING count(*) > 2
ORDER BY dept_id;

-- WHERE (rows) and HAVING (groups) together:
-- among non-cancelled orders, customers who ordered at least twice
SELECT customer_id, count(*) AS orders
FROM orders
WHERE status <> 'cancelled'
GROUP BY customer_id
HAVING count(*) >= 2
ORDER BY customer_id;

-- HAVING can use an aggregate that is not in the SELECT list
SELECT dept_id
FROM employees
GROUP BY dept_id
HAVING max(salary) > 140000
ORDER BY dept_id;

-- Several conditions: departments whose average salary is > 90k and have 2+ people
SELECT dept_id, count(*) AS n, round(avg(salary)) AS avg_salary
FROM employees
WHERE dept_id IS NOT NULL
GROUP BY dept_id
HAVING avg(salary) > 90000 AND count(*) >= 2
ORDER BY avg_salary DESC;

-- An aggregate in WHERE is an error (WHERE runs before grouping)
\set ON_ERROR_STOP 0
SELECT dept_id, count(*) FROM employees WHERE count(*) > 2 GROUP BY dept_id;
\set ON_ERROR_STOP 1

-- A non-aggregate condition in HAVING works, but belongs in WHERE
SELECT dept_id, count(*) FROM employees GROUP BY dept_id HAVING dept_id = 2;
SELECT dept_id, count(*) FROM employees WHERE dept_id = 2 GROUP BY dept_id;

-- HAVING without GROUP BY: the whole table is one group
SELECT count(*) AS products FROM products HAVING count(*) > 5;
SELECT count(*) AS products FROM products HAVING count(*) > 50;
