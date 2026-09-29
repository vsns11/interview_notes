-- 1.x  psql meta-commands also work inside a script
\dt
\d departments
\x on
SELECT * FROM employees WHERE emp_id = 7;
\x off
