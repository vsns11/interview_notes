-- 6.1-6.2  Nested (non-correlated) subqueries that return one value
\pset null '(null)'

-- Who earns more than the company average? The inner query runs once.
SELECT first_name, salary
FROM employees
WHERE salary > (SELECT avg(salary) FROM employees)
ORDER BY salary DESC;

-- A scalar subquery in SELECT: each salary next to the average
SELECT first_name, salary,
       round((SELECT avg(salary) FROM employees)) AS company_avg,
       round(salary - (SELECT avg(salary) FROM employees)) AS diff
FROM employees
WHERE dept_id = 1
ORDER BY emp_id;

-- The most recently hired employee(s)
SELECT first_name, hire_date FROM employees
WHERE hire_date = (SELECT max(hire_date) FROM employees);

-- Second-highest salary: the largest salary below the maximum
SELECT max(salary) AS second_highest
FROM employees
WHERE salary < (SELECT max(salary) FROM employees);

-- A scalar subquery that finds no row yields NULL
SELECT (SELECT salary FROM employees WHERE emp_id = 999) AS missing;

-- A "scalar" subquery that returns 2+ rows is an error
\set ON_ERROR_STOP 0
SELECT first_name FROM employees
WHERE salary = (SELECT salary FROM employees WHERE dept_id = 1);
\set ON_ERROR_STOP 1
