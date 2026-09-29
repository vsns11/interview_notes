-- P5 People who finished at least one task they assigned to themselves
CREATE TABLE task_log (task_id int, assigner_id int, assignee_id int, done_on date);
INSERT INTO task_log VALUES
 (1,7,7,'2024-01-02'),(1,7,7,'2024-01-02'),   -- duplicate row
 (2,7,8,'2024-01-03'),(3,9,9,'2024-01-05'),(4,8,7,'2024-01-06'),(5,9,9,'2024-01-07');
\echo '--- solution'
SELECT DISTINCT assigner_id AS employee_id
FROM task_log
WHERE assigner_id = assignee_id
ORDER BY employee_id;
\echo '--- without DISTINCT: duplicates leak through'
SELECT assigner_id AS employee_id FROM task_log WHERE assigner_id = assignee_id ORDER BY 1;
