-- 16.5 Graph queries in SQL: people + "knows" edges, walked with recursive CTEs.
CREATE TABLE person (id int PRIMARY KEY, name text NOT NULL);
CREATE TABLE knows  (src int REFERENCES person, dst int REFERENCES person, PRIMARY KEY (src, dst));

INSERT INTO person VALUES (1,'Ava'),(2,'Bilal'),(3,'Chen'),(4,'Dana'),(5,'Eli'),(6,'Fatima'),(7,'Goran');
-- friendships are symmetric, so store both directions
INSERT INTO knows SELECT a, b FROM (VALUES (1,2),(1,3),(2,4),(3,4),(4,5),(5,6),(6,7)) e(a,b)
UNION ALL          SELECT b, a FROM (VALUES (1,2),(1,3),(2,4),(3,4),(4,5),(5,6),(6,7)) e(a,b);

-- 1 hop: Ava's friends
SELECT p.name FROM knows k JOIN person p ON p.id = k.dst WHERE k.src = 1 ORDER BY p.name;

-- 2 hops: friend-of-friend suggestions for Ava (not already friends, not herself)
SELECT DISTINCT p.name AS suggestion
FROM knows k1 JOIN knows k2 ON k2.src = k1.dst JOIN person p ON p.id = k2.dst
WHERE k1.src = 1 AND k2.dst <> 1
  AND k2.dst NOT IN (SELECT dst FROM knows WHERE src = 1);

-- N hops: degrees of separation from Ava to everyone (breadth-first with a path to avoid cycles)
WITH RECURSIVE walk(id, depth, path) AS (
  SELECT 1, 0, ARRAY[1]
  UNION ALL
  SELECT k.dst, w.depth + 1, w.path || k.dst
  FROM walk w JOIN knows k ON k.src = w.id
  WHERE k.dst <> ALL (w.path) AND w.depth < 6
)
SELECT p.name, min(depth) AS degrees
FROM walk JOIN person p USING (id)
GROUP BY p.name ORDER BY degrees, p.name;

-- Shortest path Ava -> Goran, shown as names
WITH RECURSIVE walk(id, path) AS (
  SELECT 1, ARRAY[1]
  UNION ALL
  SELECT k.dst, w.path || k.dst FROM walk w JOIN knows k ON k.src = w.id
  WHERE k.dst <> ALL (w.path) AND cardinality(w.path) < 7
)
SELECT (SELECT string_agg(p.name, ' -> ' ORDER BY ord)
        FROM unnest(path) WITH ORDINALITY u(pid, ord) JOIN person p ON p.id = u.pid) AS shortest_path
FROM walk WHERE id = 7 ORDER BY cardinality(path) LIMIT 1;
