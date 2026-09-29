-- 5.4-5.6  LEFT, RIGHT and FULL OUTER JOIN on the same pair of tables
\pset null '(null)'

-- The two inputs. Note: Asha and Rohan have no department; Research (6) has no employees.
SELECT emp_id, first_name, dept_id FROM employees WHERE emp_id IN (1, 2, 14, 15) ORDER BY emp_id;
SELECT dept_id, name FROM departments ORDER BY dept_id;

-- INNER: only matched pairs (13 rows)
SELECT e.first_name, e.dept_id AS e_dept, d.dept_id AS d_dept, d.name
FROM employees e INNER JOIN departments d ON d.dept_id = e.dept_id
ORDER BY d.dept_id, e.emp_id;

-- LEFT: every employee; unmatched ones get NULLs on the department side (15 rows)
SELECT e.first_name, e.dept_id AS e_dept, d.dept_id AS d_dept, d.name
FROM employees e LEFT JOIN departments d ON d.dept_id = e.dept_id
ORDER BY d.dept_id NULLS LAST, e.emp_id;

-- RIGHT: every department; Research gets NULLs on the employee side (14 rows)
SELECT e.first_name, e.dept_id AS e_dept, d.dept_id AS d_dept, d.name
FROM employees e RIGHT JOIN departments d ON d.dept_id = e.dept_id
ORDER BY d.dept_id, e.emp_id;

-- FULL: everything from both sides (16 rows)
SELECT e.first_name, e.dept_id AS e_dept, d.dept_id AS d_dept, d.name
FROM employees e FULL JOIN departments d ON d.dept_id = e.dept_id
ORDER BY d.dept_id NULLS LAST, e.emp_id;

-- Row counts side by side
SELECT (SELECT count(*) FROM employees e JOIN departments d ON d.dept_id = e.dept_id)       AS inner_rows,
       (SELECT count(*) FROM employees e LEFT JOIN departments d ON d.dept_id = e.dept_id)  AS left_rows,
       (SELECT count(*) FROM employees e RIGHT JOIN departments d ON d.dept_id = e.dept_id) AS right_rows,
       (SELECT count(*) FROM employees e FULL JOIN departments d ON d.dept_id = e.dept_id)  AS full_rows;

-- RIGHT JOIN is a LEFT JOIN with the tables swapped: same 14 rows
SELECT count(*) AS left_swapped FROM departments d LEFT JOIN employees e ON d.dept_id = e.dept_id;

-- Customers with their order count, including customers who never ordered
SELECT c.customer_id, c.first_name, count(o.order_id) AS orders
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.customer_id
GROUP BY c.customer_id, c.first_name
ORDER BY orders, c.customer_id;

-- FULL JOIN to reconcile two lists: planned vs actual hours
CREATE TABLE planned (emp_id int, hours int);
CREATE TABLE actual  (emp_id int, hours int);
INSERT INTO planned VALUES (3, 300), (4, 200), (5, 250);
INSERT INTO actual  VALUES (3, 310), (5, 250), (7, 40);
SELECT coalesce(p.emp_id, a.emp_id) AS emp_id, p.hours AS planned, a.hours AS actual,
       CASE WHEN p.emp_id IS NULL THEN 'unplanned work'
            WHEN a.emp_id IS NULL THEN 'no work logged'
            WHEN p.hours <> a.hours THEN 'different'
            ELSE 'ok' END AS verdict
FROM planned p FULL JOIN actual a ON a.emp_id = p.emp_id
ORDER BY 1;
