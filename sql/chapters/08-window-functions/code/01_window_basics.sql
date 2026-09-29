-- 8.1 / 8.2  OVER() and PARTITION BY
\pset null '(null)'

-- GROUP BY collapses rows: one row per department
SELECT dept_id, round(avg(salary)) AS dept_avg
FROM employees WHERE dept_id IN (1, 2)
GROUP BY dept_id ORDER BY dept_id;

-- A window function keeps every row and adds the aggregate beside it
SELECT first_name, dept_id, salary,
       round(avg(salary) OVER ())                     AS company_avg,
       round(avg(salary) OVER (PARTITION BY dept_id)) AS dept_avg,
       count(*)          OVER (PARTITION BY dept_id)  AS dept_size
FROM employees
WHERE dept_id IN (1, 2)
ORDER BY dept_id, salary DESC;

-- Typical uses: share of a total, difference from the group average
SELECT first_name, dept_id, salary,
       round(100.0 * salary / sum(salary) OVER (PARTITION BY dept_id), 1) AS pct_of_dept_payroll,
       salary - round(avg(salary) OVER (PARTITION BY dept_id))            AS vs_dept_avg
FROM employees
WHERE dept_id IN (1, 2)
ORDER BY dept_id, salary DESC;

-- NULL is a partition of its own (Asha and Rohan have no department)
SELECT first_name, dept_id, count(*) OVER (PARTITION BY dept_id) AS same_dept
FROM employees
WHERE dept_id IS NULL OR dept_id = 4
ORDER BY dept_id NULLS LAST;
