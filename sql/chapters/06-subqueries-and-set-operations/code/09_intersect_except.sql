-- 6.9-6.10  INTERSECT and EXCEPT
\pset null '(null)'

-- Cities where we have both customers and an office (note the NULL row!)
SELECT city FROM customers
INTERSECT
SELECT location FROM departments
ORDER BY city;

-- Employees who are on a project AND manage someone
SELECT emp_id FROM employee_projects
INTERSECT
SELECT manager_id FROM employees
ORDER BY emp_id;

-- EXCEPT: products that were never ordered
SELECT product_id FROM products
EXCEPT
SELECT product_id FROM order_items;

-- Order matters: A EXCEPT B is not B EXCEPT A
SELECT city FROM customers EXCEPT SELECT location FROM departments ORDER BY 1;
SELECT location FROM departments EXCEPT SELECT city FROM customers ORDER BY 1;

-- ALL variants keep duplicate counts (bag semantics)
CREATE TABLE a (x int);
CREATE TABLE b (x int);
INSERT INTO a VALUES (1), (1), (1), (2), (3);
INSERT INTO b VALUES (1), (1), (2), (2), (4);
SELECT x FROM a INTERSECT     SELECT x FROM b ORDER BY x;
SELECT x FROM a INTERSECT ALL SELECT x FROM b ORDER BY x;
SELECT x FROM a EXCEPT        SELECT x FROM b ORDER BY x;
SELECT x FROM a EXCEPT ALL    SELECT x FROM b ORDER BY x;
SELECT x FROM a UNION         SELECT x FROM b ORDER BY x;
SELECT x FROM a UNION ALL     SELECT x FROM b ORDER BY x;

-- Set operators compare whole rows and treat NULLs as equal
SELECT first_name, email FROM employees WHERE dept_id = 1
EXCEPT
SELECT first_name, email FROM employees WHERE emp_id IN (2, 5)
ORDER BY 1;

-- Without INTERSECT / EXCEPT (e.g. MySQL before 8.0.31): EXISTS and NOT EXISTS
-- INTERSECT-like: customer cities that also have an office (DISTINCT, NULL-safe comparison)
SELECT DISTINCT c.city FROM customers c
WHERE EXISTS (SELECT 1 FROM departments d WHERE d.location IS NOT DISTINCT FROM c.city)
ORDER BY c.city;
-- EXCEPT-like: customers whose city has no office (returns whole customer rows)
SELECT c.customer_id, c.first_name, c.city FROM customers c
WHERE NOT EXISTS (SELECT 1 FROM departments d WHERE d.location = c.city)
ORDER BY c.customer_id;
-- ...the same with LEFT JOIN + IS NULL
SELECT c.customer_id, c.first_name, c.city FROM customers c
LEFT JOIN (SELECT DISTINCT location FROM departments) d ON d.location = c.city
WHERE d.location IS NULL
ORDER BY c.customer_id;
