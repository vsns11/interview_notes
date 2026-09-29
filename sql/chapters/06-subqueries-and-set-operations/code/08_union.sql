-- 6.8  UNION and UNION ALL
\pset null '(null)'

-- One contact list from two tables (UNION removes duplicates)
SELECT first_name, last_name, 'employee' AS kind FROM employees WHERE dept_id = 3
UNION
SELECT first_name, last_name, 'customer' FROM customers WHERE country = 'UK'
ORDER BY kind, last_name;

-- Cities we have customers in, plus cities we have offices in
SELECT city FROM customers WHERE city IS NOT NULL
UNION
SELECT location FROM departments WHERE location IS NOT NULL
ORDER BY city;

-- UNION vs UNION ALL: duplicates kept, no sort/hash needed
SELECT count(*) AS union_rows     FROM (SELECT country FROM customers UNION     SELECT 'India') t;
SELECT count(*) AS union_all_rows FROM (SELECT country FROM customers UNION ALL SELECT 'India') t;

-- UNION ALL to stack two event types into one timeline
SELECT placed_at AS at, 'order placed'  AS event, order_id FROM orders   WHERE customer_id = 1
UNION ALL
SELECT shipped_at,      'order shipped',          order_id FROM orders   WHERE customer_id = 1 AND shipped_at IS NOT NULL
ORDER BY at;

-- UNION treats two NULLs as duplicates (unlike =)
SELECT NULL::text AS v UNION SELECT NULL::text;

-- Rules: same number of columns, compatible types; names come from the first query
\set ON_ERROR_STOP 0
SELECT first_name, last_name FROM employees UNION SELECT first_name FROM customers;
SELECT emp_id FROM employees UNION SELECT first_name FROM customers;
\set ON_ERROR_STOP 1
