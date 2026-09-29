-- 12.13 Beyond the B-tree: hash, BRIN, GIN, GiST and bloom indexes.
SET max_parallel_workers_per_gather = 0;
SET jit = off;

-- (a) Same column, three index types: compare sizes.
CREATE INDEX o_created_btree ON orders USING btree (created_at);
CREATE INDEX o_created_brin  ON orders USING brin  (created_at);
CREATE INDEX o_cust_btree    ON orders USING btree (customer_id);
CREATE INDEX o_cust_hash     ON orders USING hash  (customer_id);
SELECT c.relname AS index_name, am.amname AS type,
       pg_size_pretty(pg_relation_size(c.oid)) AS size
FROM pg_class c JOIN pg_am am ON am.oid = c.relam
WHERE c.relname IN ('o_created_btree', 'o_created_brin', 'o_cust_btree', 'o_cust_hash')
ORDER BY pg_relation_size(c.oid) DESC;

-- (b) A hash index answers only "=".
DROP INDEX o_cust_btree;
EXPLAIN (COSTS OFF) SELECT * FROM orders WHERE customer_id = 42;
EXPLAIN (COSTS OFF) SELECT * FROM orders WHERE customer_id < 42;

-- (c) BRIN keeps min/max per block range (128 pages): tiny, great when the column
--     follows the physical order (created_at does: correlation = 1).
DROP INDEX o_created_btree;
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT count(*) FROM orders WHERE created_at >= '2024-03-01' AND created_at < '2024-03-02';

-- (d) GIN: an inverted index for "contains" queries on arrays / jsonb / text search.
CREATE TABLE posts (id int PRIMARY KEY, tags text[], meta jsonb);
INSERT INTO posts
SELECT g,
       ARRAY[(ARRAY['sql','java','css','react','go'])[g % 5 + 1],
             (ARRAY['beginner','advanced'])[g % 2 + 1]],
       jsonb_build_object('lang', (ARRAY['en','de','hi'])[g % 3 + 1], 'likes', g % 500)
FROM generate_series(1, 200000) g;
CREATE INDEX posts_tags_gin ON posts USING gin (tags);
CREATE INDEX posts_meta_gin ON posts USING gin (meta jsonb_path_ops);
ANALYZE posts;
EXPLAIN (COSTS OFF) SELECT count(*) FROM posts WHERE tags @> ARRAY['go', 'advanced'];
SELECT count(*) FROM posts WHERE tags @> ARRAY['go', 'advanced'];
EXPLAIN (COSTS OFF) SELECT count(*) FROM posts WHERE meta @> '{"lang": "hi", "likes": 7}';

-- (e) GiST: overlap / nearest-neighbour searches, e.g. "no two bookings of the
--     same room may overlap" as an exclusion constraint.
CREATE EXTENSION IF NOT EXISTS btree_gist;
CREATE TABLE bookings (
  room   int,
  during tsrange,
  EXCLUDE USING gist (room WITH =, during WITH &&)
);
INSERT INTO bookings VALUES (101, '[2024-05-01 14:00, 2024-05-03 11:00)');
INSERT INTO bookings VALUES (102, '[2024-05-01 14:00, 2024-05-03 11:00)');  -- other room: fine
\set ON_ERROR_STOP 0
INSERT INTO bookings VALUES (101, '[2024-05-02 14:00, 2024-05-04 11:00)');  -- overlaps!
\set ON_ERROR_STOP 1

-- (f) The bloom extension: one small index that answers equality on ANY subset of
--     columns, with false positives removed by a recheck on the table.
CREATE EXTENSION IF NOT EXISTS bloom;
CREATE TABLE wide AS
SELECT g AS id, g % 97 AS a, g % 89 AS b, g % 83 AS c, g % 79 AS d, g % 73 AS e
FROM generate_series(1, 300000) g;
CREATE INDEX wide_bloom ON wide USING bloom (a, b, c, d, e) WITH (length = 80);
ANALYZE wide;
SELECT pg_size_pretty(pg_relation_size('wide_bloom')) AS bloom_index_size,
       pg_size_pretty(pg_relation_size('wide'))       AS table_size;
EXPLAIN (ANALYZE, COSTS OFF, TIMING OFF, SUMMARY OFF)
SELECT * FROM wide WHERE c = 5 AND e = 9;
