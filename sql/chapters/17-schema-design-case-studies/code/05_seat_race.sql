-- Two buyers race for the same seat, on two real concurrent sessions (A and B) opened with dblink.
-- Default isolation (READ COMMITTED). Shows why the guarded UPDATE is enough to prevent double-selling.
SET timezone = 'UTC';
CREATE EXTENSION IF NOT EXISTS dblink;
\set race_update 'UPDATE tix.show_seats SET status = ''held'', held_by = %s, hold_until = ''2026-05-20 10:10+00'' WHERE show_id = 10 AND seat_id = 9 AND status = ''free'' RETURNING seat_id, held_by'

SELECT dblink_connect('a', format('host=/tmp port=5544 user=postgres dbname=%s', current_database())) AS a,
       dblink_connect('b', format('host=/tmp port=5544 user=postgres dbname=%s', current_database())) AS b;
SELECT dblink_exec('a', 'BEGIN') AS a_begin, dblink_exec('b', 'BEGIN') AS b_begin;

\echo '--- A: holds seat 9 (C1) but has not committed yet'
SELECT * FROM dblink('a', format(:'race_update', 1)) AS t(seat_id int, held_by int);

\echo '--- B: sends the same UPDATE for user 2; it must wait for A''s row lock'
SELECT dblink_send_query('b', format(:'race_update', 2)) AS sent;
SELECT pg_sleep(0.5);
SELECT dblink_is_busy('b') AS b_still_running,
       (SELECT wait_event_type FROM pg_stat_activity
         WHERE query LIKE 'UPDATE tix.show_seats%held_by = 2%' AND state = 'active') AS b_waits_on;

\echo '--- A commits. B wakes up, re-checks status = ''free'' on the new row version, and updates nothing'
SELECT dblink_exec('a', 'COMMIT') AS a_commit;
SELECT count(*) AS rows_b_changed FROM dblink_get_result('b') AS t(seat_id int, held_by int);
SELECT * FROM dblink_get_result('b') AS t(seat_id int, held_by int);   -- drain: required by dblink
SELECT dblink_exec('b', 'COMMIT') AS b_commit;

\echo '--- final state of the seat: held by user 1 only'
SELECT seat_id, status, held_by FROM tix.show_seats WHERE show_id = 10 AND seat_id = 9;
SELECT dblink_disconnect('a'), dblink_disconnect('b');
