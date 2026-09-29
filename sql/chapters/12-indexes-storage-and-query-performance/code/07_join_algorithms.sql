-- 12.11 The three join algorithms: nested loop, hash join, merge join.
SET max_parallel_workers_per_gather = 0;
SET jit = off;
CREATE INDEX orders_customer_idx ON orders (customer_id);
ANALYZE orders;

-- (a) Few outer rows + index on the inner side -> Nested Loop.
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT c.name, o.id, o.amount
FROM customers c JOIN orders o ON o.customer_id = c.id
WHERE c.id IN (10, 20, 30);

-- (b) Big join, no useful order -> Hash Join (build a hash table on the smaller input).
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT c.country, sum(o.amount)
FROM customers c JOIN orders o ON o.customer_id = c.id
GROUP BY c.country;

-- (c) ORDER BY + LIMIT: the planner walks customers_pkey in order (Nested Loop) and stops early;
--     a Merge Join appears only when forced in (d).
EXPLAIN (COSTS OFF)
SELECT c.id, o.id
FROM customers c JOIN orders o ON o.customer_id = c.id
ORDER BY c.id
LIMIT 5;

-- (d) Force each algorithm in turn (enable_* settings are for experiments only!)
--     and compare, for a small and for a large join.
CREATE FUNCTION force_join(method text, q text,
                           OUT join_node text, OUT exec_ms numeric)
LANGUAGE plpgsql AS $$
DECLARE line text;
BEGIN
  PERFORM set_config('enable_nestloop',  (method = 'nestloop')::text, true);
  PERFORM set_config('enable_hashjoin',  (method = 'hash')::text,     true);
  PERFORM set_config('enable_mergejoin', (method = 'merge')::text,    true);
  FOR line IN EXECUTE 'EXPLAIN (ANALYZE, COSTS OFF, TIMING OFF) ' || q LOOP
    IF join_node IS NULL AND line ~ '(Nested Loop|Hash Join|Merge Join)' THEN
      join_node := substring(line from '(Nested Loop|Hash Join|Merge Join)');
    END IF;
    IF line LIKE 'Execution Time:%' THEN
      exec_ms := round(substring(line from '[0-9.]+')::numeric, 1);
    END IF;
  END LOOP;
END $$;

BEGIN;  -- set_config(..., true) is transaction-local
SELECT 'small: 3 customers' AS join_size, m AS forced, f.*
FROM unnest(ARRAY['nestloop', 'hash', 'merge']) AS m,
     force_join(m, 'SELECT c.name, o.amount FROM customers c
                    JOIN orders o ON o.customer_id = c.id WHERE c.id IN (10, 20, 30)') AS f;
SELECT 'large: all 1M orders' AS join_size, m AS forced, f.*
FROM unnest(ARRAY['nestloop', 'hash', 'merge']) AS m,
     force_join(m, 'SELECT c.country, sum(o.amount) FROM customers c
                    JOIN orders o ON o.customer_id = c.id GROUP BY c.country') AS f;
COMMIT;
