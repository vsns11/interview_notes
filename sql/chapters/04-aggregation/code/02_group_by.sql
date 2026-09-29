-- 4.2  GROUP BY: one output row per group
\pset null '(null)'

-- Headcount and payroll per department (NULL dept forms its own group)
SELECT dept_id, count(*) AS headcount, sum(salary) AS payroll
FROM employees
GROUP BY dept_id
ORDER BY dept_id;

-- Group by two columns: each distinct (dept, title) pair is a group
SELECT dept_id, job_title, count(*) AS n, round(avg(salary)) AS avg_salary
FROM employees
WHERE dept_id IN (1, 2)
GROUP BY dept_id, job_title
ORDER BY dept_id, job_title;

-- Group by an expression: orders per month
SELECT date_trunc('month', placed_at)::date AS month, count(*) AS orders
FROM orders
GROUP BY date_trunc('month', placed_at)
ORDER BY month;

-- PostgreSQL also accepts an output-column alias or position in GROUP BY
SELECT extract(year FROM hire_date) AS hire_year, count(*) AS hires
FROM employees
GROUP BY hire_year
ORDER BY 1;

-- Revenue per product category (a join feeds the grouping)
SELECT p.category, sum(oi.quantity) AS units,
       round(sum(oi.quantity * oi.unit_price * (1 - oi.discount)), 2) AS revenue
FROM order_items oi JOIN products p ON p.product_id = oi.product_id
GROUP BY p.category
ORDER BY revenue DESC;

-- GROUP BY with no aggregate behaves like DISTINCT
SELECT status FROM orders GROUP BY status ORDER BY status;

-- A column that is neither grouped nor aggregated is an error
\set ON_ERROR_STOP 0
SELECT dept_id, first_name, count(*) FROM employees GROUP BY dept_id;
\set ON_ERROR_STOP 1

-- ...unless you group by the table's primary key (functional dependency)
SELECT d.dept_id, d.name, count(e.emp_id) AS headcount
FROM departments d JOIN employees e ON e.dept_id = d.dept_id
GROUP BY d.dept_id
ORDER BY d.dept_id;
