-- 15.9 Write-ahead log: LSNs grow as we write; a CHECKPOINT records a redo start point.
SHOW wal_level;
SHOW synchronous_commit;
SHOW checkpoint_timeout;
SHOW max_wal_size;
SHOW full_page_writes;

CREATE TEMP TABLE lsn_marks (label text, lsn pg_lsn);
INSERT INTO lsn_marks VALUES ('before', pg_current_wal_insert_lsn());

CREATE TABLE ledger (id int PRIMARY KEY, account text, delta numeric);
INSERT INTO ledger SELECT i, 'acct-' || (i % 5), (i % 7) - 3 FROM generate_series(1, 5000) i;

INSERT INTO lsn_marks VALUES ('after 5,000 inserts', pg_current_wal_insert_lsn());

-- The WAL position only moves forward; the gap is the volume of log written.
SELECT a.label AS from_mark, b.label AS to_mark,
       pg_wal_lsn_diff(b.lsn, a.lsn) > 0                    AS wal_grew,
       pg_wal_lsn_diff(b.lsn, a.lsn) > 5000 * 50            AS more_than_50_bytes_per_row
FROM lsn_marks a, lsn_marks b
WHERE a.label = 'before' AND b.label = 'after 5,000 inserts';

-- A checkpoint flushes dirty pages and moves the redo start point forward.
CHECKPOINT;
SELECT redo_lsn >= (SELECT lsn FROM lsn_marks WHERE label = 'after 5,000 inserts')
       AS redo_point_moved_past_our_inserts
FROM pg_control_checkpoint();
