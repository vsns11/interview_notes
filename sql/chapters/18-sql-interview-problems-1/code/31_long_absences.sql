-- P31 Absence periods longer than three days (start and end dates both count as days off)
CREATE TABLE absence (employee_id int PRIMARY KEY, employee_name text, start_date date, end_date date,
                      CHECK (start_date <= end_date));
INSERT INTO absence VALUES (1,'Hana','2024-03-01','2024-03-05'),(2,'Ivo','2024-03-10','2024-03-12'),
 (3,'Jade','2024-02-27','2024-03-01'),(4,'Kurt','2024-04-01','2024-04-01');
\echo '--- solution: date - date is an integer number of days in PostgreSQL'
SELECT employee_name, start_date, end_date, end_date - start_date + 1 AS days_off
FROM absence
WHERE end_date - start_date + 1 > 3
ORDER BY employee_name;
