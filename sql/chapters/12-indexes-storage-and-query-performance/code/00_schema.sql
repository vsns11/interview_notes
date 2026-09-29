-- Chapter 12 sample data: a small online shop, big enough (1,000,000 orders)
-- for the planner to care about indexes. Fully deterministic: every value is
-- computed from the row number g, so every run produces identical data.
CREATE TABLE customers (
  id         int  PRIMARY KEY,
  name       text NOT NULL,
  email      text NOT NULL,
  country    text NOT NULL,
  signup_on  date NOT NULL
);
INSERT INTO customers
SELECT g,
       (ARRAY['asha','ben','chen','dara','eli','fatima','goro','hana'])[g % 8 + 1] || ' #' || g,
       'User' || g || '@' || (ARRAY['mail.com','shop.io','corp.net'])[g % 3 + 1],
       (ARRAY['IN','US','DE','BR','JP','NG','FR','GB','CA','AU'])[g % 10 + 1],
       DATE '2020-01-01' + (g % 1500)
FROM generate_series(1, 100000) AS g;

CREATE TABLE orders (
  id          bigint        PRIMARY KEY,
  customer_id int           NOT NULL REFERENCES customers(id),
  status      text          NOT NULL,
  amount      numeric(10,2) NOT NULL,
  created_at  timestamp     NOT NULL,
  note        text
);
INSERT INTO orders
SELECT g,
       (g::bigint * 7919) % 100000 + 1,                          -- spreads orders over all customers
       CASE WHEN g % 100 < 2 THEN 'pending'                -- 2 %  pending
            WHEN g % 100 < 5 THEN 'refunded'               -- 3 %  refunded
            ELSE 'shipped' END,                            -- 95 % shipped
       ((g::bigint * 37) % 50000) / 100.0 + 1,                     -- 1.00 .. 500.99
       TIMESTAMP '2024-01-01' + g * INTERVAL '31 seconds', -- grows with id (~1 year)
       CASE WHEN g % 10 = 0 THEN 'gift wrap please' END    -- mostly NULL
FROM generate_series(1, 1000000) AS g;

-- Helper used by several files: run a query under EXPLAIN ANALYZE and return its execution time and
-- the number of 8 KB buffers (pages) it touched.
CREATE FUNCTION plan_stats(q text,
                           OUT exec_ms numeric, OUT buffers bigint)
LANGUAGE plpgsql AS $$
DECLARE j json;
BEGIN
  EXECUTE 'EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) ' || q INTO j;
  exec_ms  := round((j->0->>'Execution Time')::numeric, 2);
  buffers  := (j->0->'Plan'->>'Shared Hit Blocks')::bigint
            + (j->0->'Plan'->>'Shared Read Blocks')::bigint;
END $$;

VACUUM ANALYZE customers;
VACUUM ANALYZE orders;
