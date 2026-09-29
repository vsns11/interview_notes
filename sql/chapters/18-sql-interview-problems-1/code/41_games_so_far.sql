-- P41 For every player and day: games played up to and including that day
CREATE TABLE activity (player_id int, device_id int, played_on date, games int,
                       PRIMARY KEY (player_id, played_on));
INSERT INTO activity VALUES
 (1,2,'2024-03-01',5),(1,2,'2024-05-02',6),(1,3,'2024-06-25',1),(3,1,'2024-03-02',0),(3,4,'2024-07-03',5);
\echo '--- solution: running total with a window'
SELECT player_id, played_on,
       sum(games) OVER (PARTITION BY player_id ORDER BY played_on) AS games_played_so_far
FROM activity
ORDER BY player_id, played_on;
\echo '--- pre-window alternative: self-join on earlier-or-same days'
SELECT a.player_id, a.played_on, sum(b.games) AS games_played_so_far
FROM activity a
JOIN activity b ON b.player_id = a.player_id AND b.played_on <= a.played_on
GROUP BY a.player_id, a.played_on
ORDER BY a.player_id, a.played_on;
