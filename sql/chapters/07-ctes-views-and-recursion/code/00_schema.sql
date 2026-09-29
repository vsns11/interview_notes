-- The SQL Handbook sample database: "company"
-- A small fictional company that sells office gear. Used by the early chapters
-- (01 onward). Chapters copy this file into their own code/00_schema.sql.
--
-- Deliberate quirks, so later examples have something to find:
--   * employees:  the CEO and one new hire have no department (dept_id NULL);
--                 two engineers and two sales reps share a salary (ties);
--                 some emails and commission_pct values are NULL;
--                 one email is stored in mixed case.
--   * departments: 'Research' has no employees; 'Support' has no location.
--   * customers:  Anita Shah was entered twice (same person, messy email);
--                 city is NULL for one customer and written 'mumbai' for another;
--                 two customers have never ordered.
--   * products:   one product was never ordered; one has no category.
--   * orders:     pending / cancelled orders have shipped_at NULL;
--                 online orders have no sales rep (sales_rep_id NULL).

SET client_min_messages = warning;   -- hide "does not exist, skipping" notices

DROP TABLE IF EXISTS order_items, orders, products, customers,
                     employee_projects, projects, employees, departments CASCADE;

CREATE TABLE departments (
    dept_id   integer PRIMARY KEY,
    name      text    NOT NULL UNIQUE,
    location  text,                       -- NULL = not decided yet
    budget    numeric(12,2) NOT NULL CHECK (budget >= 0)
);

CREATE TABLE employees (
    emp_id          integer PRIMARY KEY,
    first_name      text    NOT NULL,
    last_name       text    NOT NULL,
    email           text    UNIQUE,                         -- may be NULL
    job_title       text    NOT NULL,
    dept_id         integer REFERENCES departments(dept_id),-- NULL = unassigned
    manager_id      integer REFERENCES employees(emp_id),   -- NULL = top of tree
    salary          numeric(10,2) NOT NULL CHECK (salary > 0),
    commission_pct  numeric(4,2),                            -- only some sales staff
    hire_date       date    NOT NULL
);

CREATE TABLE projects (
    project_id  integer PRIMARY KEY,
    name        text    NOT NULL,
    dept_id     integer NOT NULL REFERENCES departments(dept_id),
    start_date  date    NOT NULL,
    end_date    date,                                        -- NULL = still running
    budget      numeric(12,2)
);

CREATE TABLE employee_projects (
    emp_id      integer REFERENCES employees(emp_id),
    project_id  integer REFERENCES projects(project_id),
    role        text    NOT NULL,
    hours       integer NOT NULL DEFAULT 0,
    PRIMARY KEY (emp_id, project_id)
);

CREATE TABLE customers (
    customer_id  integer PRIMARY KEY,
    first_name   text NOT NULL,
    last_name    text NOT NULL,
    email        text,
    city         text,
    country      text NOT NULL,
    signup_date  date NOT NULL
);

CREATE TABLE products (
    product_id    integer PRIMARY KEY,
    name          text    NOT NULL UNIQUE,
    category      text,                                     -- NULL = uncategorised
    unit_price    numeric(8,2) NOT NULL CHECK (unit_price >= 0),
    discontinued  boolean NOT NULL DEFAULT false
);

CREATE TABLE orders (
    order_id      integer PRIMARY KEY,
    customer_id   integer NOT NULL REFERENCES customers(customer_id),
    sales_rep_id  integer REFERENCES employees(emp_id),     -- NULL = online order
    placed_at     timestamp NOT NULL,
    status        text NOT NULL CHECK (status IN ('pending','shipped','delivered','cancelled')),
    shipped_at    timestamp                                  -- NULL until shipped
);

CREATE TABLE order_items (
    order_id    integer REFERENCES orders(order_id),
    product_id  integer REFERENCES products(product_id),
    quantity    integer NOT NULL CHECK (quantity > 0),
    unit_price  numeric(8,2) NOT NULL,                       -- price at time of sale
    discount    numeric(3,2) NOT NULL DEFAULT 0,             -- 0.10 = 10 % off
    PRIMARY KEY (order_id, product_id)
);

INSERT INTO departments VALUES
 (1, 'Engineering', 'Bengaluru', 900000),
 (2, 'Sales',       'Mumbai',    400000),
 (3, 'Marketing',   'Mumbai',    250000),
 (4, 'Finance',     'Pune',      200000),
 (5, 'Support',     NULL,        150000),
 (6, 'Research',    'Pune',      300000);

INSERT INTO employees VALUES
 ( 1, 'Asha',   'Rao',     'asha.rao@company.example',      'CEO',                 NULL, NULL, 250000, NULL, '2015-01-05'),
 ( 2, 'Vikram', 'Menon',   'vikram.menon@company.example',  'Engineering Manager',    1,    1, 185000, NULL, '2018-03-12'),
 ( 3, 'Neha',   'Gupta',   'Neha.Gupta@Company.example',    'Senior Engineer',        1,    2, 142000, NULL, '2019-07-01'),
 ( 4, 'Rahul',  'Verma',   'rahul.verma@company.example',   'Engineer',               1,    2,  98000, NULL, '2021-01-18'),
 ( 5, 'Meera',  'Iyer',    NULL,                            'Engineer',               1,    2,  98000, NULL, '2022-06-06'),
 ( 6, 'Daniel', 'Brooks',  'daniel.brooks@company.example', 'Sales Manager',          2,    1, 150000, 0.05, '2017-09-25'),
 ( 7, 'Priya',  'Nair',    'priya.nair@company.example',    'Sales Rep',              2,    6,  72000, 0.10, '2020-02-10'),
 ( 8, 'Arjun',  'Singh',   'arjun.singh@company.example',   'Sales Rep',              2,    6,  68000, 0.10, '2023-11-20'),
 ( 9, 'Sara',   'Khan',    'sara.khan@company.example',     'Sales Rep',              2,    6,  68000, NULL, '2024-04-01'),
 (10, 'Lucas',  'Martin',  'lucas.martin@company.example',  'Marketing Lead',         3,    1, 120000, NULL, '2019-05-15'),
 (11, 'Ananya', 'Das',     NULL,                            'Content Writer',         3,   10,  61000, NULL, '2022-08-29'),
 (12, 'Kiran',  'Patel',   'kiran.patel@company.example',   'Finance Manager',        4,    1, 135000, NULL, '2016-12-01'),
 (13, 'Omar',   'Farouk',  'omar.farouk@company.example',   'Accountant',             4,   12,  76000, NULL, '2021-10-11'),
 (14, 'Li',     'Wei',     'li.wei@company.example',        'Support Engineer',       5,    2,  64000, NULL, '2023-02-14'),
 (15, 'Rohan',  'Joshi',   'rohan.joshi@company.example',   'Intern',              NULL,    2,  30000, NULL, '2025-06-02');

INSERT INTO projects VALUES
 (1, 'Checkout Revamp',   1, '2024-09-01', '2025-03-31', 120000),
 (2, 'Mobile App',        1, '2025-01-15', NULL,         200000),
 (3, 'Spring Campaign',   3, '2025-02-01', '2025-04-30',  40000),
 (4, 'Year-End Audit',    4, '2024-11-01', '2025-01-31',  15000),
 (5, 'Helpdesk Chatbot',  5, '2025-04-01', NULL,            NULL);

INSERT INTO employee_projects VALUES
 (2, 1, 'Lead',      120), (3, 1, 'Developer', 400), (4, 1, 'Developer', 350),
 (3, 2, 'Lead',      300), (5, 2, 'Developer', 280), (15, 2, 'Tester',    90),
 (10, 3, 'Lead',     150), (11, 3, 'Writer',    200), (7, 3, 'Advisor',    20),
 (12, 4, 'Lead',      80), (13, 4, 'Analyst',   160),
 (14, 5, 'Developer', 60), (4, 5, 'Advisor',    15);

INSERT INTO customers VALUES
 ( 1, 'Anita',   'Shah',     'anita.shah@mail.example',      'Mumbai',    'India', '2024-11-02'),
 ( 2, 'Ben',     'Carter',   'ben.carter@mail.example',      'London',    'UK',    '2024-12-15'),
 ( 3, 'Chen',    'Li',       'chen.li@mail.example',         'Bengaluru', 'India', '2025-01-08'),
 ( 4, 'Divya',   'Menon',    NULL,                           'Delhi',     'India', '2025-01-20'),
 ( 5, 'Ethan',   'Walker',   'ethan.walker@mail.example',    'New York',  'USA',   '2025-02-03'),
 ( 6, 'Fatima',  'Qureshi',  'fatima.q@mail.example',        NULL,        'India', '2025-02-14'),
 ( 7, 'George',  'Hill',     'george.hill@mail.example',     'London',    'UK',    '2025-03-01'),
 ( 8, 'Anita',   'Shah',     '  Anita.Shah@Mail.example ',   'mumbai',    'India', '2025-03-09'),
 ( 9, 'Hana',    'Sato',     'hana.sato@mail.example',       'New York',  'USA',   '2025-04-22'),
 (10, 'Ivan',    'Petrov',   'ivan.petrov@mail.example',     'Delhi',     'India', '2025-05-30');

INSERT INTO products VALUES
 (1, 'Laptop Pro 14',               'Electronics', 1299.00, false),
 (2, 'Wireless Mouse',              'Electronics',   24.99, false),
 (3, 'USB-C Hub',                   'Accessories',   49.50, false),
 (4, 'Mechanical Keyboard',         'Accessories',   89.00, false),
 (5, 'Standing Desk',               'Furniture',    499.00, false),
 (6, 'Ergonomic Chair',             'Furniture',    349.99, false),
 (7, 'Noise-Cancelling Headphones', 'Electronics',  199.95, false),
 (8, 'Desk Lamp',                   'Furniture',     35.00, true),
 (9, 'Gift Card',                   NULL,            50.00, false);

INSERT INTO orders VALUES
 (101, 1,    7, '2025-01-03 10:15', 'delivered', '2025-01-04 09:00'),
 (102, 2, NULL, '2025-01-17 22:40', 'delivered', '2025-01-19 11:30'),
 (103, 3,    8, '2025-01-31 23:55', 'delivered', '2025-02-02 08:10'),
 (104, 1, NULL, '2025-02-11 09:05', 'delivered', '2025-02-12 16:45'),
 (105, 4,    7, '2025-02-28 14:20', 'cancelled', NULL),
 (106, 5, NULL, '2025-03-05 08:00', 'delivered', '2025-03-07 10:00'),
 (107, 6,    9, '2025-03-18 19:30', 'shipped',   '2025-03-20 12:00'),
 (108, 2,    7, '2025-04-01 12:00', 'delivered', '2025-04-03 09:15'),
 (109, 8, NULL, '2025-04-09 16:45', 'delivered', '2025-04-10 10:20'),
 (110, 7,    8, '2025-04-25 11:10', 'shipped',   '2025-04-28 15:00'),
 (111, 3, NULL, '2025-05-06 07:55', 'cancelled', NULL),
 (112, 5,    9, '2025-05-20 21:05', 'shipped',   '2025-05-22 13:40'),
 (113, 1,    7, '2025-06-02 10:30', 'pending',   NULL),
 (114, 6, NULL, '2025-06-15 18:25', 'pending',   NULL);

INSERT INTO order_items VALUES
 (101, 1, 1, 1299.00, 0.00), (101, 2, 1,  24.99, 0.00),
 (102, 7, 2,  199.95, 0.10),
 (103, 4, 1,   89.00, 0.00), (103, 3, 2,  49.50, 0.00),
 (104, 5, 1,  479.00, 0.00), (104, 6, 1, 349.99, 0.05),
 (105, 1, 2, 1299.00, 0.00),
 (106, 2, 3,   24.99, 0.00), (106, 9, 2,  50.00, 0.00),
 (107, 6, 2,  349.99, 0.10),
 (108, 1, 1, 1249.00, 0.00), (108, 7, 1, 199.95, 0.00), (108, 3, 1, 49.50, 0.00),
 (109, 4, 2,   89.00, 0.15),
 (110, 5, 1,  499.00, 0.00),
 (111, 2, 1,   24.99, 0.00),
 (112, 7, 1,  199.95, 0.00), (112, 2, 2,  24.99, 0.00),
 (113, 1, 1, 1299.00, 0.05), (113, 4, 1,  89.00, 0.05),
 (114, 3, 4,   49.50, 0.00);

-- ---------------------------------------------------------------------------
-- Chapter 7 addition: a tiny flight network for graph recursion (7.3).
-- It contains a cycle (Mumbai -> Delhi -> Mumbai) on purpose.
DROP TABLE IF EXISTS routes CASCADE;
CREATE TABLE routes (
    from_city  text    NOT NULL,
    to_city    text    NOT NULL,
    minutes    integer NOT NULL CHECK (minutes > 0),
    PRIMARY KEY (from_city, to_city)
);
INSERT INTO routes VALUES
 ('Mumbai',    'Delhi',     130),
 ('Delhi',     'Mumbai',    135),
 ('Mumbai',    'Bengaluru', 105),
 ('Bengaluru', 'Chennai',    60),
 ('Delhi',     'Kolkata',   140),
 ('Chennai',   'Kolkata',   135),
 ('Kolkata',   'Guwahati',   75);
