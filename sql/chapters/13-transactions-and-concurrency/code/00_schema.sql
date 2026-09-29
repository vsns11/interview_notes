-- Chapter 13 - Transactions and concurrency: small sample schema.
-- accounts : bank balances (atomicity, isolation anomalies, deadlock)
-- products : stock counter + version column (lost update, optimistic locking)
-- on_call  : doctors on call (write skew)
-- jobs     : a work queue (SELECT ... FOR UPDATE SKIP LOCKED)

DROP TABLE IF EXISTS accounts, products, on_call, jobs;

CREATE TABLE accounts (
  id      int PRIMARY KEY,
  owner   text NOT NULL,
  balance numeric(10,2) NOT NULL CHECK (balance >= 0)   -- a rule every transaction must respect
);
INSERT INTO accounts VALUES (1, 'asha', 500), (2, 'ben', 300), (3, 'chen', 50);

CREATE TABLE products (
  id      int PRIMARY KEY,
  name    text NOT NULL,
  stock   int  NOT NULL CHECK (stock >= 0),
  version int  NOT NULL DEFAULT 1                       -- used for optimistic locking
);
INSERT INTO products VALUES (1, 'concert ticket', 10, 1), (2, 'poster', 3, 1);

CREATE TABLE on_call (
  doctor  text PRIMARY KEY,
  shift   text NOT NULL,
  on_duty boolean NOT NULL
);
INSERT INTO on_call VALUES ('dr_rao', 'night', true), ('dr_kim', 'night', true), ('dr_lee', 'day', true);

CREATE TABLE jobs (
  id         int PRIMARY KEY,
  payload    text NOT NULL,
  status     text NOT NULL DEFAULT 'queued',            -- queued | running | done
  claimed_by text
);
INSERT INTO jobs (id, payload) SELECT g, 'email #' || g FROM generate_series(1, 6) g;
