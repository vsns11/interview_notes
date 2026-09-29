-- 13.14 - MVCC made visible: every row version carries xmin (creator txn) and xmax (deleter txn).
-- pageinspect lets us look inside the table's first 8 KB page and see OLD versions too.
CREATE EXTENSION IF NOT EXISTS pageinspect;
SET client_min_messages = warning;

-- a tiny table so the page is easy to read
CREATE TABLE wallet (id int PRIMARY KEY, balance int);
INSERT INTO wallet VALUES (1, 500), (2, 300);

-- System columns: ctid = (page, slot) where this version lives.
SELECT ctid, xmin, xmax, id, balance FROM wallet ORDER BY id;

-- Two committed updates of row 1 (think: +200, then -100).
UPDATE wallet SET balance = balance + 200 WHERE id = 1;
UPDATE wallet SET balance = balance - 100 WHERE id = 1;

-- A normal SELECT sees only the version that is visible to it ...
SELECT ctid, xmin, xmax, id, balance FROM wallet ORDER BY id;

-- ... but the page still holds all three versions of row 1 until VACUUM removes the dead ones.
-- t_xmin = creating txn, t_xmax = txn that replaced it (0 = still current), t_ctid = pointer to newer version.
SELECT lp AS slot, t_xmin, t_xmax, t_ctid
FROM heap_page_items(get_raw_page('wallet', 0)) ORDER BY lp;

-- A rolled-back update: the new version is written, then simply never becomes visible.
BEGIN;
UPDATE wallet SET balance = 0 WHERE id = 2;
SELECT txid_current() AS my_txid;
ROLLBACK;
SELECT ctid, xmin, xmax, id, balance FROM wallet WHERE id = 2;   -- xmax is left set, but that txn aborted

-- VACUUM reclaims versions no transaction can see any more.
VACUUM wallet;
SELECT lp AS slot, lp_flags, t_xmin, t_xmax, t_ctid
FROM heap_page_items(get_raw_page('wallet', 0)) ORDER BY lp;
