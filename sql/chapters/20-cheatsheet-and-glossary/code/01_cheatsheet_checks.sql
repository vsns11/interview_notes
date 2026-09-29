-- Chapter 20 — every concrete behaviour quoted on the cheat sheets, asserted on PostgreSQL 14.
-- Each DO block calls chk(area, claim, got, expected); a mismatch raises an exception
-- and the runner reports FAIL. The last query lists what was verified.

-- 1. NULL and three-valued logic (§20.2, Chapter 2)
DO $$ BEGIN
  PERFORM chk('NULL', 'NULL = NULL is NULL (unknown)',            (NULL = NULL)::text, NULL);
  PERFORM chk('NULL', 'NULL AND FALSE = FALSE',                   (NULL AND FALSE)::text, 'false');
  PERFORM chk('NULL', 'NULL OR TRUE = TRUE',                      (NULL OR TRUE)::text, 'true');
  PERFORM chk('NULL', 'NOT NULL is NULL',                         (NOT NULL::boolean)::text, NULL);
  PERFORM chk('NULL', 'NULL IS NOT DISTINCT FROM NULL is TRUE',   (NULL IS NOT DISTINCT FROM NULL)::text, 'true');
  PERFORM chk('NULL', '1 IN (1, NULL) is TRUE',                   (1 IN (1, NULL))::text, 'true');
  PERFORM chk('NULL', '2 NOT IN (1, NULL) is NULL, not TRUE',     (2 NOT IN (1, NULL))::text, NULL);
  PERFORM chk('NULL', 'NOT IN over a list holding NULL keeps 0 rows',
              (SELECT count(*) FROM customers WHERE id NOT IN (SELECT customer_id FROM banned))::text, '0');
  PERFORM chk('NULL', 'NOT EXISTS keeps the 2 unbanned customers',
              (SELECT count(*) FROM customers c WHERE NOT EXISTS
                 (SELECT 1 FROM banned b WHERE b.customer_id = c.id))::text, '2');
  PERFORM chk('NULL', 'WHERE points <> 20 also drops the NULL row (2 kept)',
              (SELECT count(*) FROM scores WHERE points <> 20)::text, '2');
  PERFORM chk('NULL', 'IS DISTINCT FROM 20 keeps the NULL row too',
              (SELECT count(*) FROM scores WHERE points IS DISTINCT FROM 20)::text, '3');
  PERFORM chk('NULL', 'CASE without ELSE yields NULL',            (CASE WHEN false THEN 'x' END), NULL);
  PERFORM chk('NULL', 'COALESCE returns first non-NULL',          COALESCE(NULL, NULL, 'z'), 'z');
  PERFORM chk('NULL', 'NULLIF(0, 0) is NULL',                     NULLIF(0, 0)::text, NULL);
  PERFORM chk('NULL', 'a || NULL is NULL',                        ('a' || NULL), NULL);
  PERFORM chk('NULL', 'concat(a, NULL) skips the NULL',           concat('a', NULL), 'a');
  PERFORM chk('NULL', 'empty string is not NULL in PostgreSQL',   ('' IS NULL)::text, 'false');
  PERFORM chk('NULL', 'greatest() ignores NULLs in PostgreSQL',   greatest(1, NULL, 3)::text, '3');
END $$;

-- 2. Arithmetic, types and functions (§20.3, Chapter 3)
DO $$ DECLARE msg text; BEGIN
  PERFORM chk('Functions', '7 / 2 = 3 (integer division truncates)',  (7 / 2)::text, '3');
  PERFORM chk('Functions', '-7 / 2 = -3 (truncates toward zero)',     (-7 / 2)::text, '-3');
  PERFORM chk('Functions', '-7 % 2 = -1 (sign of the dividend)',      (-7 % 2)::text, '-1');
  PERFORM chk('Functions', '7 / 2.0 = 3.5',                           (7 / 2.0)::numeric(4,1)::text, '3.5');
  PERFORM chk('Functions', '7::numeric / 2 = 3.5',                    (7::numeric / 2)::numeric(4,1)::text, '3.5');
  PERFORM chk('Functions', 'round(2.5) numeric = 3 (half away from zero)', round(2.5)::text, '3');
  PERFORM chk('Functions', 'round(2.5::float8) = 2 (half to even)',   round(2.5::float8)::text, '2');
  PERFORM chk('Functions', 'log(100) = 2 (base 10 in PostgreSQL)',    log(100)::numeric(3,1)::text, '2.0');
  PERFORM chk('Functions', 'ln(1) = 0',                               ln(1)::text, '0');
  PERFORM chk('Functions', 'length(é) = 1 char, octet_length = 2 bytes',
              length('é') || '/' || octet_length('é'), '1/2');
  PERFORM chk('Functions', 'substring is 1-based',                    substring('SQL rocks' FROM 1 FOR 3), 'SQL');
  PERFORM chk('Functions', 'split_part(a,b,c , 2) = b',               split_part('a,b,c', ',', 2), 'b');
  PERFORM chk('Functions', 'date - date = integer days',              (date '2024-03-01' - date '2024-02-01')::text, '29');
  PERFORM chk('Functions', 'Jan 31 + 1 month clamps to Feb 29 (2024)',
              (date '2024-01-31' + interval '1 month')::date::text, '2024-02-29');
  PERFORM chk('Functions', 'date_trunc month',                        date_trunc('month', timestamp '2024-05-17 10:00')::date::text, '2024-05-01');
  PERFORM chk('Functions', 'extract(dow) Sunday = 0',                 extract(dow FROM date '2024-06-02')::text, '0');
  BEGIN
    PERFORM 1 / 0;
    msg := 'no error';
  EXCEPTION WHEN division_by_zero THEN msg := 'division_by_zero';
  END;
  PERFORM chk('Functions', '1 / 0 raises an error (22012)',           msg, 'division_by_zero');
  PERFORM chk('Functions', '1 / NULLIF(0, 0) is NULL instead',        (1 / NULLIF(0, 0))::text, NULL);
END $$;

-- 3. Aggregation (§20.4, Chapter 4)
DO $$ BEGIN
  PERFORM chk('Aggregation', 'count(*) counts all 5 rows',             (SELECT count(*) FROM scores)::text, '5');
  PERFORM chk('Aggregation', 'count(points) skips the NULL',           (SELECT count(points) FROM scores)::text, '4');
  PERFORM chk('Aggregation', 'count(DISTINCT points) = 3',             (SELECT count(DISTINCT points) FROM scores)::text, '3');
  PERFORM chk('Aggregation', 'avg ignores NULL: 80/4 = 20',            (SELECT avg(points) FROM scores)::numeric(5,2)::text, '20.00');
  PERFORM chk('Aggregation', 'avg of int returns numeric, not int',    (SELECT pg_typeof(avg(points)) FROM scores)::text, 'numeric');
  PERFORM chk('Aggregation', 'sum over zero rows is NULL',             (SELECT sum(points) FROM scores WHERE false)::text, NULL);
  PERFORM chk('Aggregation', 'count over zero rows is 0',              (SELECT count(*) FROM scores WHERE false)::text, '0');
  PERFORM chk('Aggregation', 'aggregate without GROUP BY over 0 rows returns 1 row',
              (SELECT count(*) FROM (SELECT max(points) FROM scores WHERE false) t)::text, '1');
  PERFORM chk('Aggregation', 'GROUP BY puts NULLs in one group',
              (SELECT count(*) FROM (SELECT points FROM scores GROUP BY points) t)::text, '4');
  PERFORM chk('Aggregation', 'FILTER counts only matching rows',
              (SELECT count(*) FILTER (WHERE team = 'blue') FROM scores)::text, '3');
  PERFORM chk('Aggregation', 'ROLLUP(team) = 2 groups + 1 grand total',
              (SELECT count(*) FROM (SELECT team, sum(points) FROM scores GROUP BY ROLLUP (team)) t)::text, '3');
  PERFORM chk('Aggregation', 'string_agg with ORDER BY',
              (SELECT string_agg(name, ',' ORDER BY name DESC) FROM scores WHERE team = 'red'), 'Bo,Al');
END $$;

-- 4. Joins, subqueries and set operations (§20.5, §20.6, Chapters 5–6)
DO $$ BEGIN
  PERFORM chk('Joins', 'CROSS JOIN 3 x 2 = 6 rows',
              (SELECT count(*) FROM customers CROSS JOIN (VALUES (1), (2)) v(x))::text, '6');
  PERFORM chk('Joins', 'LEFT JOIN keeps all 3 customers',
              (SELECT count(*) FROM customers c LEFT JOIN banned b ON b.customer_id = c.id)::text, '3');
  PERFORM chk('Joins', 'right-table filter in WHERE turns LEFT JOIN into inner (1 row)',
              (SELECT count(*) FROM customers c LEFT JOIN banned b ON b.customer_id = c.id
                WHERE b.customer_id = 1)::text, '1');
  PERFORM chk('Joins', 'same filter in ON keeps all 3 rows',
              (SELECT count(*) FROM customers c LEFT JOIN banned b ON b.customer_id = c.id
                AND b.customer_id = 1)::text, '3');
  PERFORM chk('Joins', 'NULL keys never match in a join',
              (SELECT count(*) FROM banned a JOIN banned b ON a.customer_id = b.customer_id)::text, '1');
  PERFORM chk('Subqueries', 'scalar subquery over 0 rows is NULL',
              (SELECT (SELECT name FROM customers WHERE id = 99)), NULL);
  PERFORM chk('Subqueries', '5 > ALL(empty) is TRUE',
              (5 > ALL (SELECT id FROM customers WHERE false))::text, 'true');
  PERFORM chk('Subqueries', '5 > ANY(empty) is FALSE',
              (5 > ANY (SELECT id FROM customers WHERE false))::text, 'false');
  PERFORM chk('Set ops', 'UNION removes duplicates',
              (SELECT count(*) FROM (SELECT 1 UNION SELECT 1) t)::text, '1');
  PERFORM chk('Set ops', 'UNION ALL keeps duplicates',
              (SELECT count(*) FROM (SELECT 1 UNION ALL SELECT 1) t)::text, '2');
  PERFORM chk('Set ops', 'set operations treat NULLs as equal',
              (SELECT count(*) FROM (SELECT NULL::int INTERSECT SELECT NULL::int) t)::text, '1');
  PERFORM chk('Set ops', 'EXCEPT is not commutative (A-B has 1 row)',
              (SELECT count(*) FROM (VALUES (1), (2) EXCEPT VALUES (2), (3), (4)) t)::text, '1');
  PERFORM chk('Set ops', 'EXCEPT is not commutative (B-A has 2 rows)',
              (SELECT count(*) FROM (VALUES (2), (3), (4) EXCEPT VALUES (1), (2)) t)::text, '2');
  PERFORM chk('DISTINCT', 'DISTINCT treats NULLs as one value',
              (SELECT count(*) FROM (SELECT DISTINCT x FROM (VALUES (NULL::int), (NULL)) v(x)) t)::text, '1');
END $$;

-- 5. Sorting and paging (§20.2)
DO $$ BEGIN
  PERFORM chk('ORDER BY', 'ASC puts NULLs last in PostgreSQL',
              (SELECT name FROM scores ORDER BY points, name DESC LIMIT 1 OFFSET 4), 'Ed');
  PERFORM chk('ORDER BY', 'DESC puts NULLs first in PostgreSQL',
              (SELECT name FROM scores ORDER BY points DESC LIMIT 1), 'Ed');
  PERFORM chk('ORDER BY', 'NULLS LAST overrides it',
              (SELECT name FROM scores ORDER BY points DESC NULLS LAST LIMIT 1), 'Al');
  PERFORM chk('ORDER BY', 'FETCH FIRST 1 ROWS WITH TIES keeps both 20s',
              (SELECT count(*) FROM (SELECT * FROM scores WHERE points < 30
                 ORDER BY points DESC FETCH FIRST 1 ROWS WITH TIES) t)::text, '2');
  PERFORM chk('WHERE', 'BETWEEN is inclusive on both ends',
              (SELECT count(*) FROM scores WHERE points BETWEEN 10 AND 20)::text, '3');
  PERFORM chk('WHERE', 'LIKE is case-sensitive',                ('Anna' LIKE 'a%')::text, 'false');
  PERFORM chk('WHERE', 'ILIKE is not',                          ('Anna' ILIKE 'a%')::text, 'true');
  PERFORM chk('WHERE', 'NOT binds tighter than AND, AND tighter than OR',
              (true OR true AND false)::text, 'true');
END $$;

-- 6. Window functions and frames (§20.8, Chapter 8)
DO $$ BEGIN
  PERFORM chk('Windows', 'row_number / rank / dense_rank on 30,20,20,10',
              (SELECT string_agg(format('%s:%s/%s/%s', name, rn, rk, drk), ' ' ORDER BY rn)
                 FROM (SELECT name,
                              row_number() OVER (ORDER BY points DESC, name) AS rn,
                              rank()       OVER (ORDER BY points DESC)       AS rk,
                              dense_rank() OVER (ORDER BY points DESC)       AS drk
                         FROM scores WHERE points IS NOT NULL) t),
              'Al:1/1/1 Bo:2/2/2 Cy:3/2/2 Di:4/4/3');
  PERFORM chk('Windows', 'default frame with ORDER BY is RANGE: peers share the running total',
              (SELECT string_agg(s::text, ',' ORDER BY day, amount)
                 FROM (SELECT day, amount, sum(amount) OVER (ORDER BY day) AS s FROM sales) t),
              '10,40,40,45');
  PERFORM chk('Windows', 'ROWS frame gives a strict running total',
              (SELECT string_agg(s::text, ',' ORDER BY day, amount)
                 FROM (SELECT day, amount, sum(amount) OVER (ORDER BY day, amount
                         ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS s FROM sales) t),
              '10,20,40,45');
  PERFORM chk('Windows', 'no ORDER BY in OVER = whole partition',
              (SELECT DISTINCT sum(amount) OVER () FROM sales)::text, '45');
  PERFORM chk('Windows', 'last_value with default frame = current row (peer group end)',
              (SELECT string_agg(lv::text, ',' ORDER BY day)
                 FROM (SELECT DISTINCT day, last_value(day) OVER (ORDER BY day) AS lv FROM sales) t),
              '1,2,3');
  PERFORM chk('Windows', 'last_value with a full frame = true last',
              (SELECT DISTINCT last_value(day) OVER (ORDER BY day
                 ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) FROM sales)::text, '3');
  PERFORM chk('Windows', 'lag of first row is NULL, default arg replaces it',
              (SELECT format('%s|%s', lag(day) OVER w, lag(day, 1, 0) OVER w)
                 FROM sales WINDOW w AS (ORDER BY day, amount) ORDER BY day, amount LIMIT 1), '|0');
  PERFORM chk('Windows', 'ntile(3) over 7 rows -> bucket sizes 3,2,2',
              (SELECT string_agg(c::text, ',' ORDER BY b) FROM (SELECT b, count(*) AS c FROM
                 (SELECT ntile(3) OVER (ORDER BY g) AS b FROM generate_series(1, 7) g) x GROUP BY b) y),
              '3,2,2');
  PERFORM chk('Windows', 'percent_rank of first row = 0',
              (SELECT percent_rank() OVER (ORDER BY g) FROM generate_series(1, 4) g LIMIT 1)::text, '0');
  PERFORM chk('Windows', 'cume_dist counts the row and its peers: 1,2,2,3 -> 0.25,0.75,0.75,1',
              (SELECT string_agg(c::text, ',' ORDER BY g) FROM (SELECT g, cume_dist() OVER (ORDER BY g) AS c
                 FROM unnest(ARRAY[1, 2, 2, 3]) g) t), '0.25,0.75,0.75,1');
  PERFORM chk('Windows', 'GROUPS 1 PRECEDING spans the previous peer group',
              (SELECT string_agg(s::text, ',' ORDER BY day, amount)
                 FROM (SELECT day, amount, sum(amount) OVER (ORDER BY day
                         GROUPS BETWEEN 1 PRECEDING AND CURRENT ROW) AS s FROM sales) t),
              '10,40,40,35');
  PERFORM chk('Windows', 'gaps-and-islands: value - row_number is constant per run',
              (SELECT count(DISTINCT d - rn) FROM (SELECT d, row_number() OVER (ORDER BY d) AS rn
                 FROM unnest(ARRAY[1, 2, 3, 7, 8, 10]) d) t)::text, '3');
END $$;

-- 7. CTEs and recursion (§20.7, Chapter 7)
DO $$ BEGIN
  PERFORM chk('CTEs', 'recursive CTE sums 1..10 = 55',
              (WITH RECURSIVE n(i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM n WHERE i < 10)
               SELECT sum(i) FROM n)::text, '55');
  PERFORM chk('CTEs', 'generate_series over dates is inclusive (7 days)',
              (SELECT count(*) FROM generate_series(date '2024-01-01', date '2024-01-07', interval '1 day'))::text, '7');
  PERFORM chk('CTEs', 'generate_series over dates returns timestamptz',
              (SELECT pg_typeof(g)::text FROM generate_series(date '2024-01-01', date '2024-01-02', interval '1 day') g LIMIT 1),
              'timestamp with time zone');
END $$;

-- 8. DDL, DML, constraints and upsert (§20.9, Chapter 9)
CREATE TABLE t_users (id int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      email text UNIQUE,
                      age int CHECK (age >= 0),
                      visits int NOT NULL DEFAULT 0);
DO $$ DECLARE n int; BEGIN
  INSERT INTO t_users (email, age) VALUES (NULL, NULL), (NULL, NULL);
  PERFORM chk('Constraints', 'UNIQUE allows several NULLs',
              (SELECT count(*) FROM t_users WHERE email IS NULL)::text, '2');
  PERFORM chk('Constraints', 'CHECK passes when the expression is NULL', 'inserted', 'inserted');
  INSERT INTO t_users (email, age) VALUES ('a@x.io', 30);
  INSERT INTO t_users (email, age) VALUES ('a@x.io', 31)
    ON CONFLICT (email) DO UPDATE SET visits = t_users.visits + 1, age = EXCLUDED.age;
  PERFORM chk('Upsert', 'ON CONFLICT DO UPDATE updated the row (visits 1, age from EXCLUDED)',
              (SELECT visits || '/' || age FROM t_users WHERE email = 'a@x.io'), '1/31');
  INSERT INTO t_users (email, age) VALUES ('a@x.io', 99) ON CONFLICT DO NOTHING;
  PERFORM chk('Upsert', 'ON CONFLICT DO NOTHING left it alone',
              (SELECT age::text FROM t_users WHERE email = 'a@x.io'), '31');
  UPDATE t_users SET age = age + 1 WHERE email = 'a@x.io' RETURNING age INTO n;
  PERFORM chk('DML', 'RETURNING hands back the new value', n::text, '32');
  BEGIN
    INSERT INTO t_users (email, age) VALUES ('b@x.io', -1);
    n := -1;
  EXCEPTION WHEN check_violation THEN n := 1;
  END;
  PERFORM chk('Constraints', 'CHECK rejects FALSE (age -1)', n::text, '1');
  -- transactional DDL: TRUNCATE inside a sub-transaction that fails is rolled back
  BEGIN
    TRUNCATE t_users;
    RAISE EXCEPTION 'undo';
  EXCEPTION WHEN raise_exception THEN NULL;
  END;
  PERFORM chk('DDL', 'TRUNCATE is transactional in PostgreSQL (rows came back)',
              (SELECT count(*) FROM t_users)::text, '3');
END $$;

-- 9. Transactions (§20.12, Chapter 13)
DO $$ BEGIN
  PERFORM chk('Transactions', 'default isolation is read committed',
              current_setting('transaction_isolation'), 'read committed');
  PERFORM chk('Transactions', 'now() is fixed for the whole transaction',
              (now() = transaction_timestamp())::text, 'true');
END $$;

-- 10. Advanced types: JSONB, arrays, full-text search (§20.13, Chapter 14)
DO $$ BEGIN
  PERFORM chk('JSONB', '->> returns text',                 pg_typeof('{"a":1}'::jsonb ->> 'a')::text, 'text');
  PERFORM chk('JSONB', '-> returns jsonb',                 pg_typeof('{"a":1}'::jsonb -> 'a')::text, 'jsonb');
  PERFORM chk('JSONB', '@> containment',                   ('{"a":1,"b":[1,2]}'::jsonb @> '{"b":[2]}')::text, 'true');
  PERFORM chk('JSONB', 'duplicate keys: last one wins',    ('{"a":1,"a":2}'::jsonb ->> 'a'), '2');
  PERFORM chk('JSONB', 'jsonb normalises key order',       ('{"b":1,"a":2}'::jsonb)::text, '{"a": 2, "b": 1}');
  PERFORM chk('Arrays', 'arrays are 1-based',              (ARRAY[10, 20, 30])[1]::text, '10');
  PERFORM chk('Arrays', 'out-of-range subscript is NULL',  (ARRAY[10, 20, 30])[9]::text, NULL);
  PERFORM chk('Arrays', '= ANY(array)',                     (20 = ANY (ARRAY[10, 20]))::text, 'true');
  PERFORM chk('FTS', 'english stemming: running matches run',
              (to_tsvector('english', 'She was running') @@ to_tsquery('english', 'run'))::text, 'true');
  PERFORM chk('FTS', 'stop words are dropped',
              to_tsvector('english', 'the cat')::text, '''cat'':2');
END $$;

-- What was verified
SELECT area, count(*) AS claims FROM check_log GROUP BY area ORDER BY min(id);
SELECT count(*) AS total_claims_verified FROM check_log;
