-- 15.2 Range partitioning: one partition per year, plus a DEFAULT catch-all.
CREATE TABLE orders_by_year (
  order_id    int  NOT NULL,
  customer_id int  NOT NULL,
  order_date  date NOT NULL,
  amount      numeric(10,2) NOT NULL,
  PRIMARY KEY (order_id, order_date)          -- a PK must include the partition key
) PARTITION BY RANGE (order_date);

CREATE TABLE orders_2022 PARTITION OF orders_by_year FOR VALUES FROM ('2022-01-01') TO ('2023-01-01');
CREATE TABLE orders_2023 PARTITION OF orders_by_year FOR VALUES FROM ('2023-01-01') TO ('2024-01-01');
CREATE TABLE orders_2024 PARTITION OF orders_by_year FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');
CREATE TABLE orders_other PARTITION OF orders_by_year DEFAULT;   -- anything outside the ranges

INSERT INTO orders_by_year SELECT * FROM orders;
INSERT INTO orders_by_year VALUES (9001, 1, '2025-03-15', 99);   -- no 2025 partition -> DEFAULT
ANALYZE orders_by_year;

-- Where did the rows land? tableoid tells us the physical partition of each row.
SELECT tableoid::regclass AS partition, count(*) AS rows,
       min(order_date) AS first_day, max(order_date) AS last_day
FROM orders_by_year GROUP BY 1 ORDER BY 1;

-- Partition pruning: the planner only scans the 2023 partition.
EXPLAIN (COSTS OFF)
SELECT sum(amount) FROM orders_by_year
WHERE order_date >= '2023-03-01' AND order_date < '2023-04-01';

-- No filter on the partition key -> every partition must be scanned.
EXPLAIN (COSTS OFF)
SELECT sum(amount) FROM orders_by_year WHERE customer_id = 7;

-- Run-time pruning: with a parameter the value is unknown at plan time,
-- so Postgres prunes when the statement executes ("Subplans Removed").
SET plan_cache_mode = force_generic_plan;
PREPARE by_day(date) AS SELECT count(*) FROM orders_by_year WHERE order_date = $1;
EXPLAIN (COSTS OFF) EXECUTE by_day('2023-05-05');
RESET plan_cache_mode;

-- Maintenance: archive a year by DETACHing it (instant, no row-by-row DELETE),
-- then make room for 2025 by moving the stray row out of DEFAULT first.
ALTER TABLE orders_by_year DETACH PARTITION orders_2022;
DELETE FROM orders_other WHERE order_date >= '2025-01-01' AND order_date < '2026-01-01';
CREATE TABLE orders_2025 PARTITION OF orders_by_year FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');
INSERT INTO orders_by_year VALUES (9001, 1, '2025-03-15', 99);

SELECT tableoid::regclass AS partition, count(*) AS rows
FROM orders_by_year GROUP BY 1 ORDER BY 1;
SELECT count(*) AS rows_in_detached_2022 FROM orders_2022;   -- still a normal table
