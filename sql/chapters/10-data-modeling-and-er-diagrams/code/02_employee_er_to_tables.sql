-- 10.10-10.13: the employee-management ER diagram converted to tables, rule by rule.
-- Create parents before children (a FK cannot point at a table that does not exist yet).

CREATE TABLE department (                      -- strong entity -> table
  dept_id   int PRIMARY KEY,
  dept_name varchar(60) NOT NULL UNIQUE
);

CREATE TABLE payroll (                         -- strong entity -> table
  payroll_id     int PRIMARY KEY,
  monthly_salary numeric(10,2) NOT NULL CHECK (monthly_salary >= 0),  -- stored (typed in)
  cash_advance   numeric(10,2) NOT NULL DEFAULT 0,
  tax            numeric(10,2) NOT NULL DEFAULT 0
  -- net_pay is DERIVED -> no column (computed in the view below)
);

CREATE TABLE employee (
  emp_id         int PRIMARY KEY,                               -- key attribute
  first_name     varchar(40) NOT NULL,                          -- composite attribute full_name
  middle_initial char(1),                                       --   split into
  last_name      varchar(40) NOT NULL,                          --   one column per part
  birth_date     date NOT NULL,                                 -- simple; age is DERIVED
  dept_id        int  NOT NULL REFERENCES department(dept_id),  -- 1:N, total participation -> NOT NULL
  supervisor_id  int  REFERENCES employee(emp_id),              -- unary 1:N, partial -> NULL allowed
  payroll_id     int  NOT NULL UNIQUE REFERENCES payroll(payroll_id)  -- 1:1 -> FK + UNIQUE
);

CREATE TABLE employee_phone (                   -- multivalued attribute -> own table
  emp_id int         NOT NULL REFERENCES employee(emp_id) ON DELETE CASCADE,
  phone  varchar(20) NOT NULL,
  PRIMARY KEY (emp_id, phone)
);

CREATE TABLE project (
  project_id int PRIMARY KEY,
  title      varchar(80) NOT NULL
);

CREATE TABLE works_on (                          -- M:N -> junction table
  emp_id     int NOT NULL REFERENCES employee(emp_id),
  project_id int NOT NULL REFERENCES project(project_id),
  weekly_hours numeric(4,1) NOT NULL CHECK (weekly_hours > 0),  -- relationship attribute
  PRIMARY KEY (emp_id, project_id)
);

CREATE TABLE dependent (                          -- weak entity
  emp_id       int NOT NULL REFERENCES employee(emp_id) ON DELETE CASCADE,  -- owner key
  dep_name     varchar(40) NOT NULL,                                       -- partial key
  relationship varchar(20) NOT NULL,
  PRIMARY KEY (emp_id, dep_name)                                           -- owner key + partial key
);

INSERT INTO department VALUES (10, 'Engineering'), (20, 'Finance');
INSERT INTO payroll VALUES (501, 5200, 0, 950), (502, 4100, 300, 700), (503, 6100, 0, 1200);
INSERT INTO employee VALUES
  (1, 'Aisha', NULL, 'Khan',  '1985-04-12', 10, NULL, 503),
  (2, 'Ravi',  'K',  'Menon', '1992-11-30', 10, 1,    501),
  (3, 'Sofia', NULL, 'Rossi', '1998-07-02', 20, 1,    502);
INSERT INTO employee_phone VALUES (1, '+44 20 7946 0001'), (2, '+91 98450 00002'), (2, '+91 80 4000 0003');
INSERT INTO project VALUES (100, 'Payments API'), (200, 'Audit 2026');
INSERT INTO works_on VALUES (1, 100, 10), (2, 100, 30), (2, 200, 8), (3, 200, 35);
INSERT INTO dependent VALUES (2, 'Anu', 'daughter'), (2, 'Kiran', 'spouse');

-- Derived attributes live in a view, computed on read (fixed "as of" date so output is repeatable).
CREATE VIEW employee_card AS
SELECT e.emp_id,
       concat_ws(' ', e.first_name, e.middle_initial, e.last_name)         AS full_name,
       date_part('year', age(date '2026-09-28', e.birth_date))::int        AS age,
       p.monthly_salary - p.cash_advance - p.tax                          AS net_pay,
       (SELECT string_agg(phone, ', ' ORDER BY phone) FROM employee_phone ph
         WHERE ph.emp_id = e.emp_id)                                       AS phones
FROM employee e JOIN payroll p USING (payroll_id);

\echo '--- employee_card (derived: full_name, age, net_pay; multivalued: phones)'
SELECT * FROM employee_card ORDER BY emp_id;

\echo '--- the unary relationship: who reports to whom'
SELECT e.first_name AS employee, s.first_name AS supervisor
FROM employee e LEFT JOIN employee s ON s.emp_id = e.supervisor_id ORDER BY e.emp_id;

\echo '--- 1:1 is enforced by UNIQUE: a second employee on payroll 501 is refused'
DO $$ BEGIN
  INSERT INTO employee VALUES (4, 'Omar', NULL, 'Haddad', '1990-01-01', 20, 1, 501);
EXCEPTION WHEN unique_violation THEN RAISE NOTICE 'refused: %', SQLERRM;
END $$;

\echo '--- weak entity: deleting the owner removes its dependents (ON DELETE CASCADE)'
DELETE FROM works_on WHERE emp_id = 2;
UPDATE employee SET supervisor_id = NULL WHERE supervisor_id = 2;
DELETE FROM employee WHERE emp_id = 2;
SELECT count(*) AS dependents_left, (SELECT count(*) FROM employee_phone WHERE emp_id = 2) AS phones_left
FROM dependent;
