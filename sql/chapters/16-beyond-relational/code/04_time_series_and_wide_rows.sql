-- 16.4/16.8 Time-series in Postgres: wide-column-style key (server, ts), BRIN index,
-- time bucketing with date_bin (Postgres 14+), and retention by dropping old partitions.
SET timezone = 'UTC';
CREATE TABLE metrics (
  server_id text        NOT NULL,       -- like a wide-column "partition key"
  ts        timestamptz NOT NULL,       -- like a "clustering key": rows sorted by time
  cpu_pct   real,
  mem_mb    int,
  PRIMARY KEY (server_id, ts)
) PARTITION BY RANGE (ts);
CREATE TABLE metrics_2024_12_05 PARTITION OF metrics FOR VALUES FROM ('2024-12-05') TO ('2024-12-06');
CREATE TABLE metrics_2024_12_06 PARTITION OF metrics FOR VALUES FROM ('2024-12-06') TO ('2024-12-07');

INSERT INTO metrics
SELECT s, t,
       -- deterministic "noise": busier in the afternoon, db-1 busier than the web servers
       10 + 2 * extract(hour FROM t)::int + (CASE s WHEN 'db-1' THEN 20 ELSE 0 END)
          + ((extract(epoch FROM t)::bigint / 60 * 31 + ascii(right(s, 1))) % 15),
       1024 + ((extract(epoch FROM t)::bigint / 60 * 17 + ascii(left(s, 1))) % 900)
FROM unnest(ARRAY['web-1','web-2','db-1']) s,
     generate_series(timestamptz '2024-12-05 00:00', '2024-12-06 23:59', interval '1 minute') t;
CREATE INDEX metrics_ts_brin ON metrics USING brin (ts);
ANALYZE metrics;

SELECT count(*) AS readings FROM metrics;

-- "Last 30 minutes of web-1" -> one partition, primary-key range scan
EXPLAIN (COSTS OFF)
SELECT ts, cpu_pct FROM metrics
WHERE server_id = 'web-1' AND ts >= '2024-12-06 23:30' AND ts < '2024-12-07 00:00';

-- Downsample: 6-hour buckets per server
SELECT server_id, date_bin('6 hours', ts, timestamptz '2024-12-06') AS bucket,
       round(avg(cpu_pct)::numeric, 1) AS avg_cpu, max(mem_mb) AS peak_mem
FROM metrics WHERE ts >= '2024-12-06'
GROUP BY 1, 2 ORDER BY 1, 2 LIMIT 6;

-- Index sizes: BRIN stores one summary per block range, so it is tiny.
SELECT indexrelid::regclass AS index, pg_size_pretty(pg_relation_size(indexrelid)) AS size
FROM pg_index WHERE indrelid = 'metrics_2024_12_06'::regclass ORDER BY 1;

-- Retention policy: dropping a day is instant (no giant DELETE, no vacuum debt).
DROP TABLE metrics_2024_12_05;
SELECT min(ts)::date AS oldest_day_kept, count(*) AS readings FROM metrics;
