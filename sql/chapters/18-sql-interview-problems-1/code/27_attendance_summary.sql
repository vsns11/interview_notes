-- P27 Days present / absent / late for every employee (zeros for people with no records)
\pset null '(null)'
CREATE TABLE employee (employee_id int PRIMARY KEY, employee_name text);
CREATE TABLE attendance (employee_id int, att_date date, status text);
INSERT INTO employee VALUES (1,'Aiko'),(2,'Bram'),(3,'Cleo');
INSERT INTO attendance VALUES
 (1,'2024-04-01','Present'),(1,'2024-04-02','Late'),(1,'2024-04-03','Present'),
 (2,'2024-04-01','Absent'),(2,'2024-04-02','Present'),(2,'2024-04-02','Present');  -- duplicate kept
\echo '--- solution: FILTER (PostgreSQL / standard SQL)'
SELECT e.employee_id, e.employee_name,
       count(*) FILTER (WHERE a.status = 'Present') AS days_present,
       count(*) FILTER (WHERE a.status = 'Absent')  AS days_absent,
       count(*) FILTER (WHERE a.status = 'Late')    AS days_late
FROM employee e
LEFT JOIN attendance a ON a.employee_id = e.employee_id
GROUP BY e.employee_id, e.employee_name
ORDER BY e.employee_id;
\echo '--- portable CASE form; note the missing ELSE 0 gives NULL for Cleo'
SELECT e.employee_id,
       sum(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) AS present_else0,
       sum(CASE WHEN a.status = 'Present' THEN 1 END)        AS present_no_else
FROM employee e LEFT JOIN attendance a ON a.employee_id = e.employee_id
GROUP BY e.employee_id ORDER BY e.employee_id;
