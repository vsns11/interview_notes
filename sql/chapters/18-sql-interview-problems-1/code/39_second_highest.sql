-- P39 Second highest distinct salary, or NULL if there is none
\pset null '(null)'
CREATE TABLE emp (id int PRIMARY KEY, salary int);
INSERT INTO emp VALUES (1,300),(2,300),(3,200),(4,100);
\echo '--- solution: scalar subquery -> always exactly one row'
SELECT (SELECT DISTINCT salary FROM emp ORDER BY salary DESC OFFSET 1 LIMIT 1) AS second_highest_salary;
\echo '--- alternative: DENSE_RANK wrapped in an aggregate so "no row" becomes NULL'
SELECT max(salary) AS second_highest_salary
FROM (SELECT salary, dense_rank() OVER (ORDER BY salary DESC) AS dr FROM emp) AS r
WHERE dr = 2;
\echo '--- edge: only one distinct salary'
DELETE FROM emp WHERE salary < 300;
SELECT (SELECT DISTINCT salary FROM emp ORDER BY salary DESC OFFSET 1 LIMIT 1) AS second_highest_salary;
\echo '--- the bare window query returns no row at all here'
SELECT salary FROM (SELECT salary, dense_rank() OVER (ORDER BY salary DESC) AS dr FROM emp) AS r WHERE dr = 2;
