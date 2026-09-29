-- 19.7.5 Departments with at least two people paid above their department average
CREATE TABLE payroll (emp_id int PRIMARY KEY, name text, dept text, salary int);
INSERT INTO payroll VALUES
 (1,'A','Ops',50),(2,'B','Ops',60),(3,'C','Ops',70),(4,'D','Ops',80),   -- avg 65: C, D above
 (5,'E','Law',90),(6,'F','Law',40),(7,'G','Law',41),                     -- avg 57: only E
 (8,'H','Art',30);

SELECT dept
FROM  (SELECT dept, salary, avg(salary) OVER (PARTITION BY dept) AS dept_avg FROM payroll) t
WHERE  salary > dept_avg
GROUP  BY dept
HAVING count(*) >= 2
ORDER  BY dept;

-- Alternative: correlated subquery per row
SELECT p.dept
FROM   payroll p
WHERE  p.salary > (SELECT avg(q.salary) FROM payroll q WHERE q.dept = p.dept)
GROUP  BY p.dept
HAVING count(*) >= 2
ORDER  BY p.dept;
