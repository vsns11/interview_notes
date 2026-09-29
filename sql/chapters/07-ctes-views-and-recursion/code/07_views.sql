-- 7.7  Views: saved queries with a name
\pset null '(null)'

-- A view that hides salary and joins in the department name
CREATE VIEW staff_directory AS
SELECT e.emp_id, e.first_name || ' ' || e.last_name AS full_name,
       e.job_title, d.name AS department, e.email
FROM employees e
LEFT JOIN departments d USING (dept_id);

SELECT full_name, job_title, department FROM staff_directory
WHERE department = 'Sales' ORDER BY full_name;

-- A view stores the query, not the data: changes show up immediately
UPDATE employees SET job_title = 'Senior Sales Rep' WHERE emp_id = 7;
SELECT full_name, job_title FROM staff_directory WHERE emp_id = 7;

-- CREATE OR REPLACE may only ADD columns at the end (same names/types before)
CREATE OR REPLACE VIEW staff_directory AS
SELECT e.emp_id, e.first_name || ' ' || e.last_name AS full_name,
       e.job_title, d.name AS department, e.email, e.hire_date
FROM employees e
LEFT JOIN departments d USING (dept_id);

\set ON_ERROR_STOP 0
-- ...removing a column this way is refused
CREATE OR REPLACE VIEW staff_directory AS
SELECT e.emp_id, e.job_title FROM employees e;
-- ...and the base table cannot be dropped while the view depends on it
DROP TABLE departments;
\set ON_ERROR_STOP 1

-- A simple view (one table, no GROUP BY/DISTINCT/joins) is automatically updatable
CREATE VIEW furniture AS
SELECT product_id, name, unit_price, category
FROM products
WHERE category = 'Furniture';

UPDATE furniture SET unit_price = 329.99 WHERE name = 'Ergonomic Chair';
INSERT INTO furniture VALUES (10, 'Bookshelf', 120.00, 'Furniture');
-- Nothing stops a row that falls OUTSIDE the view's WHERE...
INSERT INTO furniture VALUES (11, 'Webcam', 59.00, 'Electronics');
SELECT product_id, name, category FROM products WHERE product_id >= 10;
SELECT count(*) AS visible_in_view FROM furniture WHERE product_id >= 10;

-- ...unless you add WITH CHECK OPTION
CREATE OR REPLACE VIEW furniture AS
SELECT product_id, name, unit_price, category
FROM products
WHERE category = 'Furniture'
WITH CHECK OPTION;

\set ON_ERROR_STOP 0
INSERT INTO furniture VALUES (12, 'Monitor Arm', 79.00, 'Accessories');
\set ON_ERROR_STOP 1

-- A view with a join/aggregate is read-only
CREATE VIEW dept_payroll AS
SELECT d.name AS department, count(e.emp_id) AS headcount, sum(e.salary) AS payroll
FROM departments d LEFT JOIN employees e USING (dept_id)
GROUP BY d.name;
SELECT * FROM dept_payroll ORDER BY payroll DESC NULLS LAST;
\set ON_ERROR_STOP 0
UPDATE dept_payroll SET headcount = 0 WHERE department = 'Sales';
\set ON_ERROR_STOP 1

-- Where views are recorded in the catalog
SELECT table_name, is_updatable
FROM information_schema.views
WHERE table_schema = 'public'
ORDER BY table_name;

-- DROP VIEW (CASCADE would also drop views built on this one)
DROP VIEW dept_payroll;
