-- 2.6  ORDER BY
\pset null '(null)'
SELECT first_name, salary FROM employees ORDER BY salary DESC;

-- Several keys: department first, then highest pay, then name as tie-breaker
SELECT dept_id, first_name, salary FROM employees
ORDER BY dept_id, salary DESC, first_name;

-- NULLs sort LAST in ascending order (PostgreSQL); move them
SELECT name, location FROM departments ORDER BY location;
SELECT name, location FROM departments ORDER BY location NULLS FIRST;
SELECT name, location FROM departments ORDER BY location DESC NULLS LAST;

-- Order by an alias, by an expression, by position
SELECT name, unit_price * 1.18 AS gross FROM products ORDER BY gross DESC LIMIT 3;
SELECT first_name, last_name FROM employees ORDER BY length(last_name) DESC, last_name LIMIT 3;
SELECT first_name, hire_date FROM employees ORDER BY 2 LIMIT 3;

-- Custom order with CASE (status workflow order, not alphabetical)
SELECT order_id, status FROM orders
ORDER BY CASE status WHEN 'pending' THEN 1 WHEN 'shipped' THEN 2
                     WHEN 'delivered' THEN 3 ELSE 4 END, order_id;

-- Text ordering depends on collation: this database uses "C" (byte order)
SELECT city FROM customers WHERE city IS NOT NULL ORDER BY city;
SELECT city FROM customers WHERE city IS NOT NULL ORDER BY lower(city), city;
