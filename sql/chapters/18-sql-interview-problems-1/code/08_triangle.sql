-- P8 Can three lengths form a triangle?
\pset null '(null)'
CREATE TABLE segment (a int, b int, c int);
INSERT INTO segment VALUES (3,4,5),(10,2,3),(5,5,5),(1,2,3),(7,NULL,4);
\echo '--- solution'
SELECT a, b, c,
       CASE WHEN a + b > c AND a + c > b AND b + c > a THEN 'Yes' ELSE 'No' END AS triangle
FROM segment;
\echo '--- alternative: the longest side must be shorter than the other two combined'
SELECT a, b, c,
       CASE WHEN 2 * greatest(a, b, c) < a + b + c THEN 'Yes'
            WHEN a IS NULL OR b IS NULL OR c IS NULL THEN 'Unknown'
            ELSE 'No' END AS triangle
FROM segment;
