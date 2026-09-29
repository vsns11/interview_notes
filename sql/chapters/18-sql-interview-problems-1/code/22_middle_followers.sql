-- P22 Users who both follow someone and are followed; show how many followers they have
CREATE TABLE follow (followee text, follower text, PRIMARY KEY (followee, follower));
INSERT INTO follow VALUES ('amy','bo'),('bo','cy'),('bo','dee'),('cy','eve'),('cy','bo'),('dee','amy');
\echo '--- solution: EXISTS keeps it one row per follow edge'
SELECT f.followee AS follower, count(*) AS num
FROM follow f
WHERE EXISTS (SELECT 1 FROM follow g WHERE g.follower = f.followee)
GROUP BY f.followee
ORDER BY f.followee;
\echo '--- self-join version: the join multiplies rows, so COUNT(DISTINCT) is required'
SELECT a.follower, count(DISTINCT b.follower) AS num, count(*) AS inflated
FROM follow a
JOIN follow b ON b.followee = a.follower
GROUP BY a.follower
ORDER BY a.follower;
