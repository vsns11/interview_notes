-- 9.2  CREATE: databases, schemas, tables
\pset null '(null)'

-- A database (cannot run inside a transaction block)
CREATE DATABASE school_demo;
SELECT datname FROM pg_database WHERE datname = 'school_demo';
DROP DATABASE school_demo;

-- A schema is a namespace inside a database
CREATE SCHEMA school;

CREATE TABLE school.students (
    student_id   integer      PRIMARY KEY,
    first_name   varchar(50)  NOT NULL,
    last_name    varchar(50)  NOT NULL,
    roll_number  integer      UNIQUE,
    email        text,
    enrolled_on  date         NOT NULL DEFAULT current_date
);

-- IF NOT EXISTS: no error if the table is already there (just a notice)
CREATE TABLE IF NOT EXISTS school.students (student_id integer);

\set ON_ERROR_STOP 0
-- A trailing comma before ")" is a syntax error
CREATE TABLE school.bad (
    id   integer PRIMARY KEY,
    name text,
);
\set ON_ERROR_STOP 1

-- CREATE TABLE ... AS: new table from a query (data + column types, NO constraints)
CREATE TABLE school.sales_staff AS
SELECT emp_id, first_name, salary FROM employees WHERE dept_id = 2;
SELECT * FROM school.sales_staff ORDER BY emp_id;

-- CREATE TABLE ... (LIKE ...): copy the structure (and constraints with INCLUDING ALL), no data
CREATE TABLE school.products_copy (LIKE products INCLUDING ALL);
SELECT count(*) AS rows_copied FROM school.products_copy;

-- Temporary table: visible only to this session, dropped at session end
CREATE TEMP TABLE scratch (id int, note text);
INSERT INTO scratch VALUES (1, 'only I can see this');
SELECT n.nspname AS schema, c.relname AS table_name, c.relpersistence
FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
WHERE c.relname IN ('scratch', 'students', 'sales_staff');
