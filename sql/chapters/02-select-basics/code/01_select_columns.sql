-- 2.1  SELECT: all columns, chosen columns, expressions, aliases
\pset null '(null)'
SELECT * FROM products;

SELECT first_name, last_name, job_title FROM employees;

-- Expressions and column aliases
SELECT name,
       unit_price,
       unit_price * 1.18        AS price_with_gst,
       'per unit'               AS note,
       unit_price * 1.18 AS "Price incl. GST"
FROM products
WHERE product_id <= 3;

-- Table alias; alias with AS is optional (but clearer)
SELECT e.first_name, e.salary / 12 monthly
FROM employees AS e
WHERE e.emp_id IN (1, 2);

-- SELECT without FROM: a calculator
SELECT 2 + 3 AS five, now()::date >= DATE '2025-01-01' AS after_2025;

-- Logical order: WHERE runs before SELECT, so it cannot see a SELECT alias...
\set ON_ERROR_STOP 0
SELECT name, unit_price * 1.18 AS gross FROM products WHERE gross > 500;
\set ON_ERROR_STOP 1
-- ...repeat the expression instead (ORDER BY, which runs after SELECT, CAN use the alias)
SELECT name, unit_price * 1.18 AS gross FROM products WHERE unit_price * 1.18 > 500 ORDER BY gross DESC;
