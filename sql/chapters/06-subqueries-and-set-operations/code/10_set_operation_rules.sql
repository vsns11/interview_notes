-- 6.11  Precedence, ORDER BY/LIMIT placement, and EXCEPT vs NOT EXISTS
\pset null '(null)'

-- INTERSECT binds tighter than UNION / EXCEPT
SELECT 1 AS v UNION SELECT 2 INTERSECT SELECT 3;          -- = 1 UNION (2 INTERSECT 3)
(SELECT 1 AS v UNION SELECT 2) INTERSECT SELECT 3;        -- parentheses change it

-- ORDER BY and LIMIT apply to the whole combined result
SELECT first_name, salary FROM employees WHERE dept_id = 1
UNION ALL
SELECT first_name, salary FROM employees WHERE dept_id = 2
ORDER BY salary DESC
LIMIT 3;

-- To limit each branch, wrap it in parentheses
(SELECT first_name, salary FROM employees WHERE dept_id = 1 ORDER BY salary DESC LIMIT 1)
UNION ALL
(SELECT first_name, salary FROM employees WHERE dept_id = 2 ORDER BY salary DESC LIMIT 1);

-- Only output column names/positions may be used in that final ORDER BY
\set ON_ERROR_STOP 0
SELECT first_name FROM employees UNION SELECT first_name FROM customers ORDER BY upper(first_name);
\set ON_ERROR_STOP 1
SELECT * FROM (SELECT first_name FROM employees UNION SELECT first_name FROM customers) t
ORDER BY upper(first_name) LIMIT 3;

-- EXCEPT vs NOT EXISTS: EXCEPT returns only the compared columns and removes duplicates
SELECT customer_id FROM customers EXCEPT SELECT customer_id FROM orders ORDER BY 1;
SELECT c.customer_id, c.first_name, c.city FROM customers c
WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.customer_id = c.customer_id)
ORDER BY 1;
