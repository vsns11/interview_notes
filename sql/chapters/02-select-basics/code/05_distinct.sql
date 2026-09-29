-- 2.5  DISTINCT
\pset null '(null)'
SELECT category FROM products ORDER BY category;
SELECT DISTINCT category FROM products ORDER BY category;

-- DISTINCT applies to the whole row of selected columns
SELECT DISTINCT country, city FROM customers ORDER BY country, city;

-- Messy data: DISTINCT only removes EXACT duplicates
SELECT DISTINCT first_name, last_name, email FROM customers WHERE last_name = 'Shah';
SELECT DISTINCT first_name, last_name, lower(trim(email)) AS email FROM customers WHERE last_name = 'Shah';

-- Counting distinct values (NULLs are not counted)
SELECT count(*) AS rows, count(city) AS with_city, count(DISTINCT city) AS distinct_cities,
       count(DISTINCT lower(city)) AS distinct_cities_ci
FROM customers;

-- PostgreSQL-only: DISTINCT ON keeps the first row per group (by ORDER BY)
SELECT DISTINCT ON (dept_id) dept_id, first_name, salary
FROM employees
WHERE dept_id IS NOT NULL
ORDER BY dept_id, salary DESC, emp_id;
