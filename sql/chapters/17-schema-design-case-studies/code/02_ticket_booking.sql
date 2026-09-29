-- Case study: ticket booking with seat holds (schema "tix" in 00_schema.sql)
-- "Now" is pinned to a fixed instant so the output is deterministic.
SET search_path = tix;
SET timezone = 'UTC';
\set now '''2026-05-20 10:00:00+00'''

\echo '--- Q1: seat map for show 10, rows B and C'
SELECT se.row_label || se.seat_no AS seat, se.tier, ss.price, ss.status
FROM show_seats ss JOIN seats se USING (seat_id)
WHERE ss.show_id = 10 AND se.row_label IN ('B', 'C')
ORDER BY se.row_label, se.seat_no;

\echo '--- Q2: Asha holds B2 and B3 for 10 minutes. The WHERE clause is the concurrency guard.'
UPDATE show_seats ss
SET status = 'held', held_by = 1, hold_until = timestamptz :now + interval '10 minutes'
FROM seats se
WHERE ss.seat_id = se.seat_id AND ss.show_id = 10
  AND se.row_label = 'B' AND se.seat_no IN (2, 3)
  AND (ss.status = 'free' OR (ss.status = 'held' AND ss.hold_until < :now))
RETURNING se.row_label || se.seat_no AS seat, ss.status, ss.hold_until;

\echo '--- Q3: Ben asks for B3 and B4 a moment later: only B4 is still free, so only 1 row changes'
UPDATE show_seats ss
SET status = 'held', held_by = 2, hold_until = timestamptz :now + interval '10 minutes'
FROM seats se
WHERE ss.seat_id = se.seat_id AND ss.show_id = 10
  AND se.row_label = 'B' AND se.seat_no IN (3, 4)
  AND (ss.status = 'free' OR (ss.status = 'held' AND ss.hold_until < :now))
RETURNING se.row_label || se.seat_no AS seat, ss.held_by;
\echo '    (the app sees 1 of 2 seats, releases B4 and tells Ben to pick again)'
UPDATE show_seats SET status = 'free', held_by = NULL, hold_until = NULL
WHERE show_id = 10 AND held_by = 2;

\echo '--- Q4: Asha pays: create the booking and turn her unexpired holds into sold seats in one transaction'
BEGIN;
INSERT INTO bookings (user_id, show_id, status, created_at)
VALUES (1, 10, 'confirmed', :now) RETURNING booking_id \gset
UPDATE show_seats
SET status = 'sold', booking_id = :booking_id, held_by = NULL, hold_until = NULL
WHERE show_id = 10 AND held_by = 1 AND status = 'held' AND hold_until >= :now
RETURNING seat_id, status, booking_id;
COMMIT;

\echo '--- Q5: "give me any 2 free premium seats" for many buyers at once: FOR UPDATE SKIP LOCKED'
WITH pick AS (
  SELECT ss.show_id, ss.seat_id FROM show_seats ss JOIN seats se USING (seat_id)
  WHERE ss.show_id = 10 AND ss.status = 'free' AND se.tier = 'premium'
  ORDER BY ss.seat_id LIMIT 2
  FOR UPDATE OF ss SKIP LOCKED
)
UPDATE show_seats ss SET status = 'held', held_by = 3, hold_until = timestamptz :now + interval '10 minutes'
FROM pick WHERE ss.show_id = pick.show_id AND ss.seat_id = pick.seat_id
RETURNING ss.seat_id, ss.held_by;

\echo '--- Q6: a background job releases expired holds (pretend 15 minutes have passed)'
UPDATE show_seats SET status = 'free', held_by = NULL, hold_until = NULL
WHERE status = 'held' AND hold_until < timestamptz :now + interval '15 minutes'
RETURNING show_id, seat_id;

\echo '--- Q7: the constraints make impossible states unrepresentable'
\set ON_ERROR_STOP off
UPDATE show_seats SET status = 'sold' WHERE show_id = 11 AND seat_id = 1;   -- sold without a booking
\set ON_ERROR_STOP on

\echo '--- Q8: occupancy per show'
SELECT show_id,
       count(*) FILTER (WHERE status = 'sold') AS sold,
       count(*) FILTER (WHERE status = 'held') AS held,
       count(*) AS capacity,
       sum(price) FILTER (WHERE status = 'sold') AS revenue
FROM show_seats GROUP BY show_id ORDER BY show_id;
