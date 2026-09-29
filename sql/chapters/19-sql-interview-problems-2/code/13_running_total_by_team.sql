-- 19.4.1 Running points total per team, day by day
CREATE TABLE team_points (player text, team char(1), day date, points int, PRIMARY KEY (team, day));
INSERT INTO team_points VALUES
 ('Lia','A','2026-05-01',10),('Max','B','2026-05-01',7),
 ('Noa','A','2026-05-03',4), ('Oli','B','2026-05-02',9),
 ('Pia','A','2026-05-06',6);

SELECT team, day,
       sum(points) OVER (PARTITION BY team ORDER BY day) AS running_total
FROM   team_points
ORDER  BY team, day;

-- Alternative (pre-window style): self-join on "same team, earlier or same day"
SELECT a.team, a.day, sum(b.points) AS running_total
FROM   team_points a
JOIN   team_points b ON b.team = a.team AND b.day <= a.day
GROUP  BY a.team, a.day
ORDER  BY a.team, a.day;
