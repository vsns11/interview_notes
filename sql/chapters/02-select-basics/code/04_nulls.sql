-- 2.4  NULL and three-valued logic
\pset null '(null)'
-- = NULL never matches anything
SELECT count(*) AS eq_null  FROM employees WHERE email = NULL;
SELECT count(*) AS is_null  FROM employees WHERE email IS NULL;
SELECT first_name, last_name FROM employees WHERE email IS NULL ORDER BY emp_id;

-- What NULL comparisons return
SELECT NULL = NULL AS null_eq_null, NULL <> 1 AS null_ne_1, NULL IS NULL AS is_null,
       1 + NULL AS plus, 'a' || NULL AS concat;

-- The truth tables of AND / OR / NOT with UNKNOWN (shown as NULL)
SELECT a, b, a AND b AS "a AND b", a OR b AS "a OR b", NOT a AS "NOT a"
FROM (VALUES (true), (false), (NULL::boolean)) AS x(a)
CROSS JOIN (VALUES (true), (false), (NULL::boolean)) AS y(b);

-- WHERE keeps only TRUE rows: both queries miss the NULL-commission reps
SELECT first_name, commission_pct FROM employees WHERE job_title = 'Sales Rep' AND commission_pct =  0.10 ORDER BY emp_id;
SELECT first_name, commission_pct FROM employees WHERE job_title = 'Sales Rep' AND commission_pct <> 0.10 ORDER BY emp_id;

-- IS DISTINCT FROM: a NULL-safe "not equal"
SELECT first_name, commission_pct FROM employees
WHERE job_title = 'Sales Rep' AND commission_pct IS DISTINCT FROM 0.10 ORDER BY emp_id;

-- NOT IN with a NULL in the list returns nothing
SELECT count(*) AS not_in_with_null FROM departments WHERE dept_id NOT IN (1, 2, NULL);
SELECT count(*) AS not_in_no_null   FROM departments WHERE dept_id NOT IN (1, 2);

-- NULL is not zero and not the empty string
SELECT count(*) AS rows, count(commission_pct) AS non_null_commissions FROM employees;
SELECT '' IS NULL AS empty_string_is_null;

-- Declaring nullability: columns accept NULL unless you say NOT NULL
CREATE TABLE students (
    id    integer PRIMARY KEY,
    email text NULL,          -- explicit "NULL allowed" (the default anyway)
    name  text NOT NULL       -- a value is required
);
INSERT INTO students VALUES (1, NULL, 'Ravi');
\set ON_ERROR_STOP 0
INSERT INTO students VALUES (2, 'kim@school.example', NULL);
\set ON_ERROR_STOP 1
SELECT * FROM students WHERE email IS NULL;
