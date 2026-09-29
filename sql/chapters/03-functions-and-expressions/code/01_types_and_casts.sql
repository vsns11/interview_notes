-- 3.1  Data types, pg_typeof, CAST and ::
\pset null '(null)'
SELECT pg_typeof(42) AS int_lit, pg_typeof(42.0) AS dec_lit, pg_typeof(4.2e1) AS sci_lit,
       pg_typeof('hi') AS str_lit, pg_typeof(true) AS bool_lit, pg_typeof(now()) AS now_type;

-- Column types come from the table definition
SELECT pg_typeof(salary) AS salary, pg_typeof(hire_date) AS hire_date, pg_typeof(email) AS email
FROM employees LIMIT 1;

-- Three spellings of a conversion
SELECT CAST('2025-03-18' AS date) AS std_cast, '2025-03-18'::date AS pg_cast, date '2025-03-18' AS typed_literal;
SELECT CAST(100 AS numeric(5,2)) AS to_numeric, 3.99::integer AS rounds_not_truncates,
       '42'::integer + 1 AS text_to_int, 123::text || 'abc' AS int_to_text;

-- Exact numeric vs floating point
SELECT 0.1 + 0.2 = 0.3 AS numeric_equal,
       0.1::double precision + 0.2::double precision = 0.3 AS float_equal,
       0.1::double precision + 0.2::double precision AS float_sum;

-- numeric(p,s) rounds to the declared scale on the way in
SELECT 12.345::numeric(5,2) AS rounded_to_scale;

-- Errors a strict type system gives you
\set ON_ERROR_STOP 0
SELECT 2147483647::integer + 1;
SELECT 'abc'::integer;
SELECT 1000::numeric(5,2);
SELECT first_name + 1 FROM employees LIMIT 1;
\set ON_ERROR_STOP 1

-- Booleans accept several spellings
SELECT 'yes'::boolean AS yes, 'f'::boolean AS f, 'on'::boolean AS on_;
