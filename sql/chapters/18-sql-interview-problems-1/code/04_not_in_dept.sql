-- P4 Employees who do NOT work in department 3
\pset null '(null)'
CREATE TABLE staff (id int PRIMARY KEY, name text, dept_id int);
INSERT INTO staff VALUES (1,'Ana',1),(2,'Ben',3),(3,'Chen',2),(4,'Dara',NULL),(5,'Eli',3);
\echo '--- naive: dept_id <> 3 (Dara, who has no department, vanishes)'
SELECT name FROM staff WHERE dept_id <> 3 ORDER BY name;
\echo '--- NULL-safe: IS DISTINCT FROM'
SELECT name FROM staff WHERE dept_id IS DISTINCT FROM 3 ORDER BY name;
