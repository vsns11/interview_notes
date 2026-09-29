-- 9.5  ALTER TABLE: change a table that already holds data
\pset null '(null)'
CREATE TABLE staff (
    id         integer PRIMARY KEY,
    first_name text,
    last_name  text
);
INSERT INTO staff VALUES (1, 'Tara', 'Mendes'), (2, 'Omar', 'Mendes'), (3, 'Yuki', 'Tanaka');

-- Add columns (with and without a default)
ALTER TABLE staff ADD COLUMN salary integer;
ALTER TABLE staff ADD COLUMN country text NOT NULL DEFAULT 'US';
SELECT * FROM staff ORDER BY id;

-- Rename a column, rename the table
ALTER TABLE staff RENAME COLUMN first_name TO given_name;
ALTER TABLE staff RENAME TO team;

-- Change a type; USING says how to convert existing values
UPDATE team SET salary = 50000 + id * 1000;
ALTER TABLE team ALTER COLUMN salary TYPE numeric(10,2) USING salary::numeric(10,2);
\set ON_ERROR_STOP 0
ALTER TABLE team ALTER COLUMN last_name TYPE integer;                  -- no automatic cast text -> integer
\set ON_ERROR_STOP 1

-- Defaults and NOT NULL
ALTER TABLE team ALTER COLUMN country SET DEFAULT 'IN';
\set ON_ERROR_STOP 0
ALTER TABLE team ADD COLUMN email text NOT NULL;                       -- existing rows would be NULL
\set ON_ERROR_STOP 1
ALTER TABLE team ADD COLUMN email text;
UPDATE team SET email = lower(given_name) || '@corp.example';
ALTER TABLE team ALTER COLUMN email SET NOT NULL;

-- Constraints: add, and add-without-checking-old-rows (NOT VALID) then VALIDATE
ALTER TABLE team ADD CONSTRAINT team_email_uq UNIQUE (email);
INSERT INTO team (id, given_name, last_name, salary, email) VALUES (4, 'Leo', 'Brandt', -5, 'leo@corp.example');
\set ON_ERROR_STOP 0
ALTER TABLE team ADD CONSTRAINT salary_positive CHECK (salary > 0);   -- fails: Leo's row breaks it
\set ON_ERROR_STOP 1
ALTER TABLE team ADD CONSTRAINT salary_positive CHECK (salary > 0) NOT VALID;  -- new rows checked from now on
\set ON_ERROR_STOP 0
ALTER TABLE team VALIDATE CONSTRAINT salary_positive;                -- still fails until the data is fixed
\set ON_ERROR_STOP 1
UPDATE team SET salary = 48000 WHERE id = 4;
ALTER TABLE team VALIDATE CONSTRAINT salary_positive;

-- Several changes in one statement (one table rewrite/lock instead of many)
ALTER TABLE team
    DROP COLUMN country,
    ADD COLUMN hired_on date,
    ALTER COLUMN last_name SET NOT NULL;

SELECT * FROM team ORDER BY id;

SELECT conname, pg_get_constraintdef(oid) AS definition
FROM pg_constraint WHERE conrelid = 'team'::regclass ORDER BY conname;
