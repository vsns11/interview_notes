-- 3.3  String functions
\pset null '(null)'
SELECT concat('Hello', ' ', 'World') AS concat, 'Hello' || ' ' || 'World' AS pipes,
       concat_ws('-', '2024', '01', '01') AS concat_ws;

-- NULL handling differs: concat skips NULL, || returns NULL
SELECT concat('a', NULL, 'b') AS concat_null, 'a' || NULL || 'b' AS pipes_null,
       concat_ws(', ', 'Pune', NULL, 'India') AS ws_null;

SELECT substring('SQL Functions' FROM 1 FOR 3) AS std_form, substring('SQL Functions', 5) AS from_5,
       substr('SQL Functions', 5, 4) AS substr, left('Database', 3) AS left_3,
       right('Postgres', 3) AS right_3, left('Database', -2) AS left_neg;

SELECT length('Data') AS length, char_length('Data') AS char_length, octet_length('Data') AS bytes,
       length('  padded  ') AS with_spaces;

-- Characters vs bytes in a UTF8 database (the book's databases are UTF8 with the C collation)
SELECT current_setting('server_encoding') AS encoding, char_length('café') AS chars,
       octet_length('café') AS bytes, upper('café') AS upper_c_collation,
       upper('café' COLLATE "en-x-icu") AS upper_icu;

SELECT upper('hello') AS upper, lower('WORLD') AS lower, initcap('noise-cancelling headphones') AS initcap;

SELECT replace('I love SQL', 'SQL', 'Postgres') AS replace, translate('2025/01/03', '/', '-') AS translate,
       reverse('stressed') AS reverse, repeat('La', 3) AS repeat;

SELECT '[' || trim('   SQL   ') || ']' AS trim, '[' || ltrim('   SQL   ') || ']' AS ltrim,
       '[' || rtrim('   SQL   ') || ']' AS rtrim, trim(BOTH '*' FROM '**SQL**') AS trim_chars;

SELECT position('abc' IN 'xxabcdef') AS position, strpos('Hello World', 'World') AS strpos,
       position('zzz' IN 'Hello') AS not_found;

SELECT lpad('SQL', 5, '*') AS lpad, rpad('SQL', 5, '*') AS rpad, lpad('7', 3, '0') AS zero_pad,
       lpad('Postgres', 3) AS lpad_truncates;

-- Splitting: split_part (negative positions need PostgreSQL 14+)
SELECT split_part('www.example.com', '.', 1) AS first, split_part('www.example.com', '.', -1) AS last,
       array_to_string((string_to_array('www.example.com', '.'))[1:2], '.') AS first_two;

-- format(): printf-style building (%s text, %I identifier, %L literal)
SELECT format('%s earns %s', first_name, salary) AS sentence,
       format('SELECT * FROM %I WHERE name = %L', 'my table', 'O''Brien') AS safe_sql
FROM employees WHERE emp_id = 7;

-- Regular expressions
SELECT regexp_replace('(022) 555-0199', '[^0-9]', '', 'g') AS digits_only,
       (regexp_match('order #A-1042 shipped', '#([A-Z])-(\d+)'))[2] AS order_number;

-- Exercise-style: full names, and the domain of each email
SELECT first_name || ' ' || last_name AS full_name,
       split_part(email, '@', 2) AS domain
FROM employees WHERE emp_id IN (1, 3, 5) ORDER BY emp_id;

-- Cleaning messy customer data
SELECT customer_id, email AS raw, lower(trim(email)) AS clean, initcap(city) AS city_fixed
FROM customers WHERE last_name = 'Shah' ORDER BY customer_id;
