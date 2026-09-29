-- P30 Engagement level per user for February 2020: avg < 20 Low, 20..60 Medium, > 60 High
CREATE TABLE app_user (user_id int PRIMARY KEY, user_name text);
CREATE TABLE engagement (user_id int, score int, day date, PRIMARY KEY (user_id, day));
INSERT INTO app_user VALUES (1,'Ravi'),(2,'Sofia'),(3,'Theo');
INSERT INTO engagement VALUES
 (1,10,'2020-02-03'),(1,90,'2020-02-29'),          -- Feb 29 exists in 2020 (leap year)
 (2,15,'2020-02-10'),(2,25,'2020-02-11'),(2,5,'2020-03-01'),
 (3,70,'2020-02-14'),(3,65,'2020-02-15');
\echo '--- solution'
SELECT u.user_name,
       CASE WHEN avg(e.score) < 20  THEN 'Low'
            WHEN avg(e.score) <= 60 THEN 'Medium'
            ELSE 'High' END AS engagement_level
FROM app_user u
JOIN engagement e ON e.user_id = u.user_id
WHERE e.day >= DATE '2020-02-01' AND e.day < DATE '2020-03-01'
GROUP BY u.user_id, u.user_name
ORDER BY u.user_name;
\echo '--- bug: hard-coding the 28th drops Ravi''s Feb 29 score'
SELECT u.user_name, avg(e.score) AS avg_score
FROM app_user u JOIN engagement e ON e.user_id = u.user_id
WHERE e.day BETWEEN '2020-02-01' AND '2020-02-28'
GROUP BY u.user_id, u.user_name ORDER BY u.user_name;
