-- P11 First login date per player
CREATE TABLE activity (player_id int, device_id int, played_on date, games int,
                       PRIMARY KEY (player_id, played_on));
INSERT INTO activity VALUES
 (1,2,'2024-03-01',5),(1,2,'2024-03-02',6),(2,3,'2024-06-25',1),
 (3,1,'2024-03-02',0),(3,4,'2024-07-03',5),(1,5,'2024-02-27',2);
\echo '--- solution'
SELECT player_id, min(played_on) AS first_login
FROM activity
GROUP BY player_id
ORDER BY player_id;
