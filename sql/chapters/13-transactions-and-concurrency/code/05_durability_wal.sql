-- 13.5 - durability: every change is first appended to the write-ahead log (WAL).
-- The WAL position (LSN) moves forward when we commit a change.
SELECT name, setting FROM pg_settings
WHERE name IN ('fsync', 'synchronous_commit', 'wal_level', 'full_page_writes', 'checkpoint_timeout')
ORDER BY name;

CREATE TEMP TABLE lsn_marks (label text, lsn pg_lsn);
INSERT INTO lsn_marks VALUES ('before', pg_current_wal_lsn());
INSERT INTO accounts SELECT g, 'user' || g, 100 FROM generate_series(10, 1009) g;   -- 1,000 rows
INSERT INTO lsn_marks VALUES ('after', pg_current_wal_lsn());

SELECT pg_size_pretty(pg_wal_lsn_diff(
         (SELECT lsn FROM lsn_marks WHERE label = 'after'),
         (SELECT lsn FROM lsn_marks WHERE label = 'before'))) AS wal_written_for_1000_rows;

-- Trade durability for speed per transaction (a crash may lose the last few commits,
-- but never corrupts the database):
BEGIN;
SET LOCAL synchronous_commit = off;
UPDATE accounts SET balance = balance + 1 WHERE id = 10;
COMMIT;
