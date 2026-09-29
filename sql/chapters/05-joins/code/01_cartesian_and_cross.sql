-- 5.1  Join theory: the Cartesian product and CROSS JOIN
\pset null '(null)'

-- Two tiny tables to see every row
CREATE TABLE sizes  (size text);
CREATE TABLE colors (color text);
INSERT INTO sizes  VALUES ('S'), ('M'), ('L');
INSERT INTO colors VALUES ('red'), ('blue');

-- CROSS JOIN = every size paired with every color: 3 x 2 = 6 rows
SELECT size, color FROM sizes CROSS JOIN colors ORDER BY size, color;

-- The old comma syntax means the same thing
SELECT count(*) AS pairs FROM sizes, colors;

-- A join is a Cartesian product filtered by a condition.
-- Without the condition: 15 employees x 6 departments = 90 rows (almost all nonsense)
SELECT count(*) AS all_pairs FROM employees, departments;
-- With the condition we keep only the matching pairs
SELECT count(*) AS matching_pairs FROM employees e, departments d WHERE e.dept_id = d.dept_id;

-- A useful CROSS JOIN: every (department, month) slot for a report grid
SELECT d.name, m.month
FROM departments d
CROSS JOIN (VALUES ('2025-01'), ('2025-02')) AS m(month)
WHERE d.dept_id <= 2
ORDER BY d.name, m.month;
