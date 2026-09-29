-- 7.3  Recursive CTEs: the mechanics
-- Count from 1 to 5
WITH RECURSIVE counter(n) AS (
    SELECT 1                 -- anchor: the starting row(s)
    UNION ALL
    SELECT n + 1             -- recursive term: runs on the rows produced last time
    FROM counter
    WHERE n < 5              -- stop condition
)
SELECT n FROM counter;

-- Carry several values along: Fibonacci numbers and a running factorial
WITH RECURSIVE fib(i, a, b, fact) AS (
    SELECT 1, 0::bigint, 1::bigint, 1::numeric
    UNION ALL
    SELECT i + 1, b, a + b, fact * (i + 1)
    FROM fib
    WHERE i < 10
)
SELECT i, a AS fibonacci, fact AS factorial FROM fib;

-- Walk a date range one week at a time
WITH RECURSIVE weeks(week_start) AS (
    SELECT DATE '2025-01-06'
    UNION ALL
    SELECT (week_start + 7)::date FROM weeks WHERE week_start < DATE '2025-02-03'
)
SELECT week_start, to_char(week_start, 'Dy') AS weekday FROM weeks;

-- A safety net while experimenting: LIMIT stops a runaway recursion
WITH RECURSIVE forever(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM forever      -- no stop condition!
)
SELECT n FROM forever LIMIT 3;

-- UNION (not ALL) removes duplicates, so a repeating state ends the loop
WITH RECURSIVE mod_cycle(n) AS (
    SELECT 1
    UNION
    SELECT (n * 2) % 7 FROM mod_cycle
)
SELECT n FROM mod_cycle;
