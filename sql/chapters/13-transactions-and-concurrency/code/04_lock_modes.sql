-- 13.10 - which locks does a statement take? Look at pg_locks for our own backend.
-- Table-level ("relation") locks are PostgreSQL's intention-style locks: an UPDATE takes
-- ROW EXCLUSIVE on the table (plus row locks stored in the row itself), which conflicts
-- with SHARE / EXCLUSIVE table locks but not with other ROW EXCLUSIVE holders.
BEGIN;
SELECT * FROM accounts WHERE id = 1;                          -- plain read
UPDATE products SET stock = stock - 1 WHERE id = 1;           -- write
SELECT * FROM on_call WHERE doctor = 'dr_rao' FOR SHARE;      -- shared row lock
LOCK TABLE jobs IN SHARE MODE;                                -- explicit table lock
SELECT c.relname, l.mode, l.granted
FROM pg_locks l JOIN pg_class c ON c.oid = l.relation
WHERE l.pid = pg_backend_pid() AND c.relnamespace = 'public'::regnamespace
ORDER BY c.relname, l.mode;
COMMIT;

-- Advisory locks: application-defined locks on a number you choose (e.g. "only one
-- nightly-report job at a time"). They mean nothing to the database itself.
SELECT pg_try_advisory_lock(42) AS got_it_first_time;
SELECT count(*) AS advisory_locks_held FROM pg_locks WHERE locktype = 'advisory' AND pid = pg_backend_pid();
SELECT pg_advisory_unlock(42) AS released;
