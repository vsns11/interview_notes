-- P13 When was each bike last used? Most recently used first.
\pset null '(null)'
CREATE TABLE ride (ride_id int PRIMARY KEY, bike_no text, started_at timestamp, ended_at timestamp);
INSERT INTO ride VALUES
 (1,'B-10','2024-05-01 08:00','2024-05-01 08:30'),
 (2,'B-20','2024-05-01 09:00','2024-05-01 09:50'),
 (3,'B-10','2024-05-03 17:10','2024-05-03 17:45'),
 (4,'B-30','2024-04-28 12:00','2024-04-28 12:20'),
 (5,'B-20','2024-05-04 07:00',NULL);              -- ride still in progress
\echo '--- solution'
SELECT bike_no, max(ended_at) AS last_used
FROM ride
GROUP BY bike_no
ORDER BY last_used DESC;
\echo '--- edge: MAX skips the NULL end time, so flag bikes that are out right now'
SELECT bike_no, max(ended_at) AS last_finished, bool_or(ended_at IS NULL) AS out_now
FROM ride
GROUP BY bike_no
ORDER BY last_finished DESC;
