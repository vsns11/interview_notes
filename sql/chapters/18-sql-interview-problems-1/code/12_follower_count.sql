-- P12 Number of followers per user
CREATE TABLE app_user (user_id int PRIMARY KEY);
CREATE TABLE follows (user_id int, follower_id int, PRIMARY KEY (user_id, follower_id));
INSERT INTO app_user VALUES (1),(2),(3),(4);
INSERT INTO follows VALUES (1,2),(1,3),(2,1),(3,1),(3,2),(3,4);
\echo '--- solution'
SELECT user_id, count(*) AS followers_count
FROM follows
GROUP BY user_id
ORDER BY user_id;
\echo '--- every user, even with zero followers: count the right-hand column, not *'
SELECT u.user_id, count(f.follower_id) AS good, count(*) AS wrong
FROM app_user u LEFT JOIN follows f USING (user_id)
GROUP BY u.user_id
ORDER BY u.user_id;
