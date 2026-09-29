-- 4.5  Logical query processing order, proved by what is (not) allowed
\pset null '(null)'

-- A full query that uses every clause
SELECT   dept_id, count(*) AS headcount, round(avg(salary)) AS avg_salary
FROM     employees
WHERE    hire_date >= '2018-01-01'
GROUP BY dept_id
HAVING   count(*) >= 2
ORDER BY avg_salary DESC
LIMIT    3;

\set ON_ERROR_STOP 0
-- WHERE runs before SELECT, so it cannot see a SELECT alias
SELECT emp_id, salary * 12 AS yearly FROM employees WHERE yearly > 2000000;
\set ON_ERROR_STOP 1

-- ORDER BY runs after SELECT, so it can
SELECT emp_id, salary * 12 AS yearly FROM employees ORDER BY yearly DESC LIMIT 3;

-- Fix for WHERE: repeat the expression, or wrap the query (derived table)
SELECT * FROM (SELECT emp_id, salary * 12 AS yearly FROM employees) t
WHERE yearly > 2000000;

-- DISTINCT runs after SELECT but before ORDER BY; LIMIT runs last
SELECT DISTINCT job_title FROM employees ORDER BY job_title LIMIT 4;

-- ORDER BY may even use an aggregate that is not selected
SELECT dept_id FROM employees WHERE dept_id IS NOT NULL
GROUP BY dept_id ORDER BY sum(salary) DESC;
