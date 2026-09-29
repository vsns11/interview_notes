-- P7 Employees whose work e-mail is valid: local part starts with a letter, then
--    letters/digits/_/./- ; domain must be exactly northwind.dev
CREATE TABLE employee (id int PRIMARY KEY, name text, email text);
INSERT INTO employee VALUES
 (1,'Ana','ana.k@northwind.dev'),  (2,'Bob','_bob@northwind.dev'),
 (3,'Cyd','9lives@northwind.dev'), (4,'Dee','dee@northwindXdev'),
 (5,'Eve','Eve-2@northwind.dev'),  (6,'Fay','fay@northwind.dev.evil.com'),
 (7,'Gus','gus@NORTHWIND.DEV'),    (8,'Hal','hal#1@northwind.dev');
\echo '--- solution'
SELECT id, name, email FROM employee
WHERE email ~ '^[A-Za-z][A-Za-z0-9_.-]*@northwind\.dev$'
ORDER BY id;
\echo '--- sloppy pattern: unescaped dot and no anchors'
SELECT id, email FROM employee
WHERE email ~ '[A-Za-z][A-Za-z0-9_.-]*@northwind.dev'
ORDER BY id;
\echo '--- case-insensitive domain: ~* operator'
SELECT id, email FROM employee
WHERE email ~* '^[a-z][a-z0-9_.-]*@northwind\.dev$'
ORDER BY id;
