-- P40 The device each player used on their first login
CREATE TABLE activity (player_id int, device_id int, played_on date, games int,
                       PRIMARY KEY (player_id, played_on));
INSERT INTO activity VALUES
 (1,2,'2024-03-01',5),(1,5,'2024-03-02',6),(2,3,'2024-06-25',1),(3,1,'2024-03-02',0),(3,4,'2024-07-03',5);
\echo '--- solution: DISTINCT ON (PostgreSQL)'
SELECT DISTINCT ON (player_id) player_id, device_id
FROM activity
ORDER BY player_id, played_on;
\echo '--- portable: ROW_NUMBER'
SELECT player_id, device_id
FROM (SELECT player_id, device_id,
             row_number() OVER (PARTITION BY player_id ORDER BY played_on) AS rn
      FROM activity) AS x
WHERE rn = 1
ORDER BY player_id;
\echo '--- portable: match (player, first date) pairs'
SELECT player_id, device_id
FROM activity
WHERE (player_id, played_on) IN (SELECT player_id, min(played_on) FROM activity GROUP BY player_id)
ORDER BY player_id;
