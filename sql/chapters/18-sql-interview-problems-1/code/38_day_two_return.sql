-- P38 Fraction of players who came back the day right after their first login (2 decimals)
CREATE TABLE activity (player_id int, device_id int, played_on date, games int,
                       PRIMARY KEY (player_id, played_on));
INSERT INTO activity VALUES
 (1,2,'2024-03-01',5),(1,2,'2024-03-02',6),(2,3,'2024-06-25',1),(3,1,'2024-03-02',0),
 (3,4,'2024-03-04',5),(4,1,'2024-02-28',2),(4,1,'2024-02-29',1),(4,1,'2024-03-01',0);
\echo '--- solution'
WITH first_day AS (
  SELECT player_id, min(played_on) AS first_on FROM activity GROUP BY player_id
)
SELECT round(count(a.player_id)::numeric / count(*), 2) AS fraction
FROM first_day f
LEFT JOIN activity a ON a.player_id = f.player_id AND a.played_on = f.first_on + 1;
\echo '--- bug: integer division'
WITH first_day AS (SELECT player_id, min(played_on) AS first_on FROM activity GROUP BY player_id)
SELECT count(a.player_id) / count(*) AS fraction
FROM first_day f LEFT JOIN activity a ON a.player_id = f.player_id AND a.played_on = f.first_on + 1;
