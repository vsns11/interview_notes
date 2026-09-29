-- P9 Largest number that appears exactly once (NULL if none)
\pset null '(null)'
CREATE TABLE nums (n int);
INSERT INTO nums VALUES (8),(8),(3),(3),(1),(4),(5),(6),(6);
\echo '--- solution'
SELECT max(n) AS num
FROM (SELECT n FROM nums GROUP BY n HAVING count(*) = 1) AS singles;
\echo '--- make every value a duplicate: the aggregate still returns one row (NULL)'
INSERT INTO nums VALUES (1),(4),(5);
SELECT max(n) AS num
FROM (SELECT n FROM nums GROUP BY n HAVING count(*) = 1) AS singles;
\echo '--- ORDER BY ... LIMIT 1 returns NO row instead'
SELECT n AS num FROM nums GROUP BY n HAVING count(*) = 1 ORDER BY n DESC LIMIT 1;
