-- P25 Viewing sessions during which the viewer saw no ad (interval is inclusive)
CREATE TABLE playback (session_id int PRIMARY KEY, customer_id int, start_t int, end_t int);
CREATE TABLE ad (ad_id int PRIMARY KEY, customer_id int, shown_at int);
INSERT INTO playback VALUES (1,1,1,5),(2,1,15,23),(3,2,10,12),(4,2,17,28),(5,3,2,8);
INSERT INTO ad VALUES (1,1,5),(2,2,17),(3,2,20),(4,3,30),(5,1,9);
\echo '--- solution'
SELECT p.session_id
FROM playback p
WHERE NOT EXISTS (
  SELECT 1 FROM ad a
  WHERE a.customer_id = p.customer_id
    AND a.shown_at BETWEEN p.start_t AND p.end_t)
ORDER BY p.session_id;
