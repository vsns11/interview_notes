-- Chapter 20 — the SQLite column of the dialect table (§20.15), checked on SQLite 3.43.
-- Each claim is inserted into a table whose CHECK requires ok = 1, so a wrong claim
-- fails the insert and the runner reports FAIL.
CREATE TABLE claim (what TEXT, ok INTEGER CHECK (ok = 1));
CREATE TABLE s (name TEXT, pts INTEGER);
INSERT INTO s VALUES ('a', 30), ('b', 20), ('c', NULL);
CREATE TABLE u (email TEXT UNIQUE);
INSERT INTO u VALUES (NULL), (NULL);             -- UNIQUE allows several NULLs

INSERT INTO claim VALUES
 ('7/2 = 3 (integer division)',               7 / 2 = 3),
 ('7/2.0 = 3.5',                              7 / 2.0 = 3.5),
 ('1/0 is NULL, not an error',                (1 / 0) IS NULL),
 ('|| concatenates',                          'a' || 'b' = 'ab'),
 ('a || NULL is NULL',                        ('a' || NULL) IS NULL),
 ('empty string is not NULL',                 '' IS NOT NULL),
 ('LIKE is case-insensitive for ASCII',       'Anna' LIKE 'a%'),
 ('NULLs sort first in ASC',                  (SELECT name FROM s ORDER BY pts LIMIT 1) = 'c'),
 ('NULLS LAST is accepted (3.30+)',           (SELECT name FROM s ORDER BY pts NULLS LAST LIMIT 1) = 'b'),
 ('IS DISTINCT FROM exists (3.39+)',          (NULL IS NOT DISTINCT FROM NULL) = 1),
 ('IS works as NULL-safe equality',           (NULL IS NULL) = 1),
 ('UNIQUE allows several NULLs',              (SELECT count(*) FROM u) = 2),
 ('SELECT without FROM works',                (SELECT 1) = 1),
 ('group_concat aggregates strings',          (SELECT group_concat(name, ',') FROM (SELECT name FROM s ORDER BY name)) = 'a,b,c'),
 ('window functions exist',                   (SELECT max(r) FROM (SELECT rank() OVER (ORDER BY pts DESC) r FROM s)) = 3),
 ('GROUPS frames exist (3.28+)',              (SELECT sum(x) OVER (ORDER BY x GROUPS BETWEEN 1 PRECEDING AND CURRENT ROW) FROM (SELECT 1 x) ) = 1),
 ('dynamic typing: text accepted in an INTEGER column', 1);

CREATE TABLE typed (n INTEGER);
INSERT INTO typed VALUES ('hello');             -- no error without STRICT
INSERT INTO claim SELECT 'typeof stored text = text', typeof(n) = 'text' FROM typed;

-- UPSERT (3.24+) and RETURNING (3.35+)
CREATE TABLE kv (k TEXT PRIMARY KEY, v INTEGER);
INSERT INTO kv VALUES ('x', 1);
INSERT INTO kv VALUES ('x', 5) ON CONFLICT (k) DO UPDATE SET v = v + excluded.v;
INSERT INTO claim SELECT 'ON CONFLICT DO UPDATE', v = 6 FROM kv;
-- FULL OUTER JOIN (3.39+)
INSERT INTO claim SELECT 'FULL OUTER JOIN exists',
  (SELECT count(*) FROM (SELECT 1 a) l FULL JOIN (SELECT 2 a) r ON l.a = r.a) = 2;
-- recursive CTE
INSERT INTO claim SELECT 'WITH RECURSIVE sums 1..10',
  (WITH RECURSIVE n(i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM n WHERE i < 10) SELECT sum(i) FROM n) = 55;
-- transactional DDL
BEGIN; CREATE TABLE gone (x); ROLLBACK;
INSERT INTO claim SELECT 'DDL rolls back',
  (SELECT count(*) FROM sqlite_master WHERE name = 'gone') = 0;

-- more cells: FILTER (3.30), iif (3.32), UPDATE ... FROM (3.33), row values, quoting, multi-arg max
INSERT INTO claim SELECT 'aggregate FILTER clause (3.30+)', (SELECT count(*) FILTER (WHERE pts > 25) FROM s) = 1;
INSERT INTO claim SELECT 'iif() (3.32+)', iif(1 > 0, 'y', 'n') = 'y';
INSERT INTO claim SELECT 'row values in IN', (1, 2) IN (SELECT 1, 2);
INSERT INTO claim SELECT 'max(a, NULL) is NULL', max(1, NULL) IS NULL;
CREATE TABLE "Quoted" ("Name" TEXT);
INSERT INTO claim SELECT 'backtick and bracket quoting accepted', (SELECT count(*) FROM `Quoted`) + (SELECT count(*) FROM [Quoted]) = 0;
INSERT INTO kv VALUES ('y', 0);
UPDATE kv SET v = s.pts FROM s WHERE s.name = 'a' AND kv.k = 'y';
INSERT INTO claim SELECT 'UPDATE ... FROM (3.33+)', v = 30 FROM kv WHERE k = 'y';

SELECT count(*) || ' SQLite claims verified' FROM claim;
