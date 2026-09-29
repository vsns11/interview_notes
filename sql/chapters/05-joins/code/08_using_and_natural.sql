-- 5.10 USING and NATURAL JOIN
\pset null '(null)'

-- USING (col): join on same-named columns; the column appears once in SELECT *
SELECT order_id, customer_id, first_name, status
FROM orders JOIN customers USING (customer_id)
WHERE order_id <= 103
ORDER BY order_id;

-- In a FULL JOIN, the USING column is COALESCE(left, right)
SELECT dept_id, first_name, name
FROM employees FULL JOIN departments USING (dept_id)
WHERE first_name IS NULL OR name IS NULL
ORDER BY dept_id NULLS LAST;

-- NATURAL JOIN matches on EVERY same-named column. employees and departments
-- share dept_id -- fine -- but projects and departments share dept_id, name AND budget:
SELECT count(*) AS natural_rows FROM projects NATURAL JOIN departments;
SELECT count(*) AS using_rows   FROM projects JOIN departments USING (dept_id);
