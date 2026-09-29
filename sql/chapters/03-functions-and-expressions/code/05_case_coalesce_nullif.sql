-- 3.5  Conditional expressions: CASE, COALESCE, NULLIF, GREATEST/LEAST
\pset null '(null)'
-- Searched CASE: first true condition wins
SELECT name, unit_price,
       CASE WHEN unit_price >= 300 THEN 'premium'
            WHEN unit_price >= 50  THEN 'standard'
            ELSE 'budget' END AS price_band
FROM products ORDER BY unit_price DESC;

-- Simple CASE: compare one expression to values
SELECT order_id, status,
       CASE status WHEN 'pending' THEN 'Awaiting dispatch'
                   WHEN 'shipped' THEN 'On the way'
                   WHEN 'delivered' THEN 'Done'
       END AS customer_message
FROM orders WHERE order_id IN (101, 105, 107, 113) ORDER BY order_id;

-- COALESCE: first non-NULL argument
SELECT first_name, email, coalesce(email, 'no email on file') AS contact,
       commission_pct, coalesce(commission_pct, 0) AS commission_or_zero
FROM employees WHERE emp_id IN (5, 6, 9, 11) ORDER BY emp_id;

-- Total pay with commission: without COALESCE, NULL poisons the sum
SELECT first_name, salary, commission_pct,
       salary * (1 + commission_pct)              AS naive_total,
       salary * (1 + coalesce(commission_pct, 0)) AS total
FROM employees WHERE dept_id = 2 ORDER BY emp_id;

-- NULLIF: turn a value into NULL, most often to avoid division by zero
SELECT a, b, a / NULLIF(b, 0) AS safe_ratio
FROM (VALUES (10, 2), (10, 0)) AS t(a, b);

SELECT email, NULLIF(trim(email), '') AS email_or_null
FROM (VALUES ('x@y.example'), ('   '), (NULL)) AS t(email);

-- CASE inside other clauses: count-like flags per row, safe project end dates
SELECT name, start_date, end_date,
       coalesce(end_date, date '2025-12-31') AS planned_or_open_end,
       CASE WHEN end_date IS NULL THEN 'running' ELSE 'finished' END AS state
FROM projects ORDER BY project_id;

-- GREATEST / LEAST ignore NULLs in PostgreSQL
SELECT greatest(1, NULL, 3) AS greatest, least(1, NULL, 3) AS least;
