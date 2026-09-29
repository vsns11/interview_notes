-- 2.3  BETWEEN, IN, LIKE / ILIKE, regular expressions
\pset null '(null)'
-- BETWEEN is inclusive at both ends
SELECT name, unit_price FROM products WHERE unit_price BETWEEN 49.50 AND 199.95 ORDER BY unit_price;

-- Date BETWEEN on a timestamp column: the trap
SELECT order_id, placed_at FROM orders
WHERE placed_at BETWEEN '2025-01-01' AND '2025-01-31'
ORDER BY order_id;

-- The safe half-open form: >= start AND < next start
SELECT order_id, placed_at FROM orders
WHERE placed_at >= '2025-01-01' AND placed_at < '2025-02-01'
ORDER BY order_id;

-- IN / NOT IN a list
SELECT order_id, status FROM orders WHERE status IN ('pending', 'cancelled') ORDER BY order_id;
SELECT first_name, job_title FROM employees WHERE job_title NOT IN ('Engineer', 'Sales Rep', 'Intern') ORDER BY emp_id;

-- LIKE: % = any run of characters, _ = exactly one character
SELECT name FROM products WHERE name LIKE '%Desk%' ORDER BY name;
SELECT first_name FROM employees WHERE first_name LIKE '_a%' ORDER BY first_name;

-- LIKE is case-sensitive in PostgreSQL; ILIKE is not
SELECT email FROM employees WHERE email LIKE 'neha%';
SELECT email FROM employees WHERE email ILIKE 'neha%';

-- Searching for a literal % or _ : escape it
SELECT label FROM (VALUES ('10% off'), ('100 off'), ('10_off')) AS t(label)
WHERE label LIKE '10\%%' ORDER BY label;
SELECT label FROM (VALUES ('10% off'), ('100 off'), ('10_off')) AS t(label)
WHERE label LIKE '10!_%' ESCAPE '!' ORDER BY label;

-- POSIX regular expressions (PostgreSQL): ~ case-sensitive, ~* case-insensitive
SELECT name FROM products WHERE name ~ '^[A-Z][a-z]+ [A-Z]' ORDER BY name;
