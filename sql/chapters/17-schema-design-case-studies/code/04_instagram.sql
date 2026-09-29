-- Case study: photo-sharing feed (schema "insta" in 00_schema.sql)
SET search_path = insta;
SET timezone = 'UTC';

\echo '--- Q1: home feed for ana (user 1), fan-out on read: newest posts by people she follows'
SELECT p.post_id, u.username, p.caption, p.created_at, p.like_count
FROM follows f
JOIN posts p ON p.user_id = f.followee_id
JOIN users u ON u.user_id = p.user_id
WHERE f.follower_id = 1
ORDER BY p.created_at DESC, p.post_id DESC
LIMIT 2;

\echo '--- Q2: next page with keyset pagination (continue after the last row we showed)'
SELECT p.post_id, u.username, p.caption, p.created_at
FROM follows f
JOIN posts p ON p.user_id = f.followee_id
JOIN users u ON u.user_id = p.user_id
WHERE f.follower_id = 1
  AND (p.created_at, p.post_id) < ('2026-05-02 07:30+00', 1003)
ORDER BY p.created_at DESC, p.post_id DESC
LIMIT 2;

\echo '--- Q3: like a post: insert the like and bump the counter in one transaction; a repeat like is a no-op'
BEGIN;
WITH ins AS (
  INSERT INTO likes (user_id, post_id, created_at) VALUES (4, 1005, '2026-05-03 13:00+00')
  ON CONFLICT DO NOTHING RETURNING post_id
)
UPDATE posts SET like_count = like_count + 1 WHERE post_id IN (SELECT post_id FROM ins)
RETURNING post_id, like_count;
WITH ins AS (
  INSERT INTO likes (user_id, post_id, created_at) VALUES (4, 1005, '2026-05-03 13:01+00')
  ON CONFLICT DO NOTHING RETURNING post_id
)
UPDATE posts SET like_count = like_count + 1 WHERE post_id IN (SELECT post_id FROM ins)
RETURNING post_id, like_count;
COMMIT;

\echo '--- Q4: mutual follows ("friends") of bo (user 2)'
SELECT u.username
FROM follows a
JOIN follows b ON b.follower_id = a.followee_id AND b.followee_id = a.follower_id
JOIN users u ON u.user_id = a.followee_id
WHERE a.follower_id = 2;

\echo '--- Q5: comment thread under post 1003, replies indented (recursive CTE)'
WITH RECURSIVE thread AS (
  SELECT comment_id, user_id, body, 0 AS depth, ARRAY[comment_id] AS path
  FROM comments WHERE post_id = 1003 AND parent_id IS NULL
  UNION ALL
  SELECT c.comment_id, c.user_id, c.body, t.depth + 1, t.path || c.comment_id
  FROM comments c JOIN thread t ON c.parent_id = t.comment_id
)
SELECT repeat('  ', depth) || u.username || ': ' || body AS comment
FROM thread t JOIN users u USING (user_id)
ORDER BY path;

\echo '--- Q6: trending hashtags over the last 3 days (posts per tag)'
SELECT h.tag, count(*) AS posts, sum(p.like_count) AS likes
FROM post_hashtags ph
JOIN hashtags h USING (tag_id)
JOIN posts p USING (post_id)
WHERE p.created_at >= timestamptz '2026-05-04 00:00+00' - interval '3 days'
GROUP BY h.tag
ORDER BY posts DESC, likes DESC;

\echo '--- Q7: are the denormalized counters still correct? (a nightly check)'
SELECT p.post_id, p.like_count, count(l.user_id) AS actual
FROM posts p LEFT JOIN likes l USING (post_id)
GROUP BY p.post_id
HAVING p.like_count <> count(l.user_id);
