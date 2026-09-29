-- 12.12 Planner statistics: what ANALYZE collects and why estimates matter.
SET max_parallel_workers_per_gather = 0;
SET jit = off;

-- What the planner knows about each column of orders (collected by ANALYZE).
SELECT attname, null_frac, n_distinct, round(correlation::numeric, 3) AS correlation
FROM pg_stats WHERE tablename = 'orders' ORDER BY attname;

-- Most common values + their frequencies (used for status = '...' estimates).
SELECT most_common_vals, most_common_freqs
FROM pg_stats WHERE tablename = 'orders' AND attname = 'status';

-- Estimated rows (planner) vs actual rows (execution): they agree when stats are fresh.
EXPLAIN (ANALYZE, COSTS ON, TIMING OFF, SUMMARY OFF)
SELECT * FROM orders WHERE status = 'refunded';

-- Stale statistics: stats say 'error' is rare (0.1 %), then an incident floods
-- the table with errors and nobody re-analyzes (autovacuum is off for the demo).
CREATE TABLE events (id int, kind text) WITH (autovacuum_enabled = false);
INSERT INTO events SELECT g, CASE WHEN g % 1000 = 0 THEN 'error' ELSE 'click' END
FROM generate_series(1, 200000) g;
CREATE INDEX events_kind_idx ON events (kind);
ANALYZE events;
INSERT INTO events SELECT g, 'error' FROM generate_series(200001, 400000) g;
EXPLAIN (ANALYZE, TIMING OFF, SUMMARY OFF) SELECT * FROM events WHERE kind = 'error';
ANALYZE events;
EXPLAIN (ANALYZE, TIMING OFF, SUMMARY OFF) SELECT * FROM events WHERE kind = 'error';

-- Correlated columns: city decides state, but the planner assumes independence.
CREATE TABLE addresses AS
SELECT g AS id, g % 100 AS city, (g % 100) / 10 AS state FROM generate_series(1, 100000) g;
ANALYZE addresses;
EXPLAIN (ANALYZE, TIMING OFF, SUMMARY OFF)
SELECT * FROM addresses WHERE city = 42 AND state = 4;

CREATE STATISTICS addr_city_state (dependencies) ON city, state FROM addresses;
ANALYZE addresses;
EXPLAIN (ANALYZE, TIMING OFF, SUMMARY OFF)
SELECT * FROM addresses WHERE city = 42 AND state = 4;

-- How fine-grained the histograms are:
SHOW default_statistics_target;
