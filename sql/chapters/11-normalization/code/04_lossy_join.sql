-- 11.10: a decomposition must be lossless. Splitting on a non-key column invents rows.

CREATE TABLE staff (emp text PRIMARY KEY, dept text NOT NULL, city text NOT NULL);
INSERT INTO staff VALUES ('Ana','Sales','Leeds'), ('Ben','Support','Leeds'), ('Cai','Sales','York');

\echo '--- bad split: (emp, city) and (dept, city). The shared column city is a key of neither piece.'
WITH ec AS (SELECT DISTINCT emp, city FROM staff),
     dc AS (SELECT DISTINCT dept, city FROM staff)
SELECT ec.emp, dc.dept, ec.city,
       (ec.emp, dc.dept, ec.city) IN (SELECT emp, dept, city FROM staff) AS real_row
FROM ec JOIN dc USING (city) ORDER BY 1, 2;

\echo '--- good split: (emp, dept) and (emp, city). The shared column emp is a key of both.'
WITH ed AS (SELECT emp, dept FROM staff), ec AS (SELECT emp, city FROM staff)
SELECT (SELECT count(*) FROM (SELECT emp, dept, city FROM ed JOIN ec USING (emp)
                              EXCEPT SELECT * FROM staff) x) AS invented_rows;
