-- 9.4  Auto-generated keys: identity columns, sequences, generated columns
\pset null '(null)'
CREATE TABLE tickets (
    ticket_id  bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    subject    text NOT NULL,
    price      numeric(8,2) NOT NULL,
    qty        integer NOT NULL DEFAULT 1,
    total      numeric(10,2) GENERATED ALWAYS AS (price * qty) STORED    -- computed column (PG 12+)
);

INSERT INTO tickets (subject, price) VALUES ('Printer jam', 10);
INSERT INTO tickets (subject, price, qty) VALUES ('VPN access', 5, 3), ('New laptop', 1200, 1)
RETURNING ticket_id, total;

\set ON_ERROR_STOP 0
INSERT INTO tickets (ticket_id, subject, price) VALUES (99, 'Manual id', 1);   -- ALWAYS refuses
INSERT INTO tickets (subject, price, total) VALUES ('Fake total', 1, 500);     -- can't write a generated column
\set ON_ERROR_STOP 1
-- ...unless you insist
INSERT INTO tickets (ticket_id, subject, price) OVERRIDING SYSTEM VALUE VALUES (99, 'Manual id', 1);

-- Sequences never go back: a rolled-back insert leaves a gap
BEGIN;
INSERT INTO tickets (subject, price) VALUES ('Will be rolled back', 1);
ROLLBACK;
INSERT INTO tickets (subject, price) VALUES ('After rollback', 2);
SELECT ticket_id, subject, total FROM tickets ORDER BY ticket_id;

-- The older way: serial = integer + a sequence + a DEFAULT nextval(...)
CREATE TABLE legacy (id serial PRIMARY KEY, label text);
INSERT INTO legacy (label) VALUES ('a'), ('b');
INSERT INTO legacy (id, label) VALUES (3, 'manual');          -- serial allows this...
\set ON_ERROR_STOP 0
INSERT INTO legacy (label) VALUES ('c');                      -- ...and then the sequence collides
\set ON_ERROR_STOP 1
SELECT column_default FROM information_schema.columns WHERE table_name = 'legacy' AND column_name = 'id';

-- A standalone sequence
CREATE SEQUENCE invoice_no START 1000 INCREMENT 10;
SELECT nextval('invoice_no') AS first, nextval('invoice_no') AS second, currval('invoice_no') AS current;

-- UUID keys (gen_random_uuid() is built in since PostgreSQL 13)
CREATE TABLE api_keys (key_id uuid PRIMARY KEY DEFAULT gen_random_uuid(), owner text);
INSERT INTO api_keys (owner) VALUES ('svc-billing');
SELECT length(key_id::text) AS uuid_text_length, owner FROM api_keys;
