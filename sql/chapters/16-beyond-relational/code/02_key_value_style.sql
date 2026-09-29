-- 16.2 Key-value access pattern in SQL: get/put/delete by key, with expiry (TTL).
-- UNLOGGED skips the WAL: faster writes, but the table is emptied after a crash (cache-like).
CREATE UNLOGGED TABLE kv (
  k          text PRIMARY KEY,
  v          jsonb NOT NULL,
  expires_at timestamptz
);

-- PUT (insert or overwrite) = UPSERT
INSERT INTO kv VALUES ('session:42', '{"user":42,"cart":["LAP-13"]}', now() + interval '30 minutes')
ON CONFLICT (k) DO UPDATE SET v = EXCLUDED.v, expires_at = EXCLUDED.expires_at;
INSERT INTO kv VALUES ('session:42', '{"user":42,"cart":["LAP-13","TSH-01"]}', now() + interval '30 minutes')
ON CONFLICT (k) DO UPDATE SET v = EXCLUDED.v, expires_at = EXCLUDED.expires_at;
INSERT INTO kv VALUES ('weather:paris', '{"temp_c":18}', now() - interval '1 minute');  -- already expired
INSERT INTO kv VALUES ('config:theme', '"dark"', NULL);                                  -- never expires

-- GET: only unexpired keys
SELECT k, v FROM kv WHERE k = 'session:42' AND (expires_at IS NULL OR expires_at > now());
SELECT count(*) AS weather_hits FROM kv
WHERE k = 'weather:paris' AND (expires_at IS NULL OR expires_at > now());

-- Leaderboard (what a Redis sorted set does): top scores by rank
CREATE TABLE step_counts (user_name text PRIMARY KEY, steps int NOT NULL);
INSERT INTO step_counts VALUES ('ana',12050),('bo',8300),('cy',15010),('di',8300),('ed',4020);
SELECT * FROM (
  SELECT rank() OVER (ORDER BY steps DESC) AS rank, user_name, steps FROM step_counts
) ranked WHERE rank <= 3 ORDER BY rank, user_name;   -- ties share a rank

-- Periodic cleanup of expired keys (Redis does this for you)
DELETE FROM kv WHERE expires_at <= now() RETURNING k AS evicted;
SELECT relpersistence AS persistence FROM pg_class WHERE relname = 'kv';  -- u = unlogged
