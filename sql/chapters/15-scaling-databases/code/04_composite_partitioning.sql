-- 15.2 Composite (sub-)partitioning: server logs by month (RANGE), then by server (HASH).
SET timezone = 'UTC';
CREATE TABLE server_logs (
  server_id int         NOT NULL,
  logged_at timestamptz NOT NULL,
  level     text        NOT NULL,
  message   text
) PARTITION BY RANGE (logged_at);

CREATE TABLE logs_2024_01 PARTITION OF server_logs
  FOR VALUES FROM ('2024-01-01 00:00+00') TO ('2024-02-01 00:00+00') PARTITION BY HASH (server_id);
CREATE TABLE logs_2024_01_h0 PARTITION OF logs_2024_01 FOR VALUES WITH (MODULUS 2, REMAINDER 0);
CREATE TABLE logs_2024_01_h1 PARTITION OF logs_2024_01 FOR VALUES WITH (MODULUS 2, REMAINDER 1);

CREATE TABLE logs_2024_02 PARTITION OF server_logs
  FOR VALUES FROM ('2024-02-01 00:00+00') TO ('2024-03-01 00:00+00') PARTITION BY HASH (server_id);
CREATE TABLE logs_2024_02_h0 PARTITION OF logs_2024_02 FOR VALUES WITH (MODULUS 2, REMAINDER 0);
CREATE TABLE logs_2024_02_h1 PARTITION OF logs_2024_02 FOR VALUES WITH (MODULUS 2, REMAINDER 1);

INSERT INTO server_logs
SELECT (i % 6) + 1,
       timestamptz '2024-01-01 00:00+00' + (i * interval '37 minutes'),
       CASE WHEN i % 10 = 0 THEN 'ERROR' ELSE 'INFO' END,
       'event ' || i
FROM generate_series(0, 2000) AS i
WHERE timestamptz '2024-01-01 00:00+00' + (i * interval '37 minutes') < '2024-03-01 00:00+00';
ANALYZE server_logs;

SELECT tableoid::regclass AS leaf_partition, count(*) AS rows,
       string_agg(DISTINCT server_id::text, ',') AS servers
FROM server_logs GROUP BY 1 ORDER BY 1;

-- Filter on BOTH keys -> exactly one leaf partition is scanned.
EXPLAIN (COSTS OFF)
SELECT count(*) FROM server_logs
WHERE logged_at >= '2024-02-10 00:00+00' AND logged_at < '2024-02-11 00:00+00'
  AND server_id = 3;

-- The partition tree, as Postgres sees it:
SELECT relid, parentrelid, isleaf, level FROM pg_partition_tree('server_logs');
