-- 13.2 - an error inside a transaction aborts ALL of it (atomicity + consistency).
-- This file shows errors on purpose, so it turns off psql's stop-on-error.
\set ON_ERROR_STOP off
\set VERBOSITY terse

-- chen has 50. Transfer 80 from chen to asha: the credit runs first and succeeds,
-- then the debit breaks the CHECK (balance >= 0) rule.
BEGIN;
UPDATE accounts SET balance = balance + 80 WHERE id = 1;   -- credit asha: OK
UPDATE accounts SET balance = balance - 80 WHERE id = 3;   -- debit chen: violates CHECK
SELECT balance FROM accounts WHERE id = 1;                  -- refused: txn is aborted
COMMIT;                                                     -- turns into ROLLBACK
SELECT id, owner, balance FROM accounts ORDER BY id;        -- nothing changed

-- A savepoint lets you recover from an expected error without losing earlier work.
BEGIN;
UPDATE accounts SET balance = balance + 5 WHERE id = 2;
SAVEPOINT before_risky;
INSERT INTO accounts VALUES (1, 'dup', 1);                  -- duplicate primary key
ROLLBACK TO SAVEPOINT before_risky;
COMMIT;
SELECT id, owner, balance FROM accounts WHERE id = 2;
