-- 11.6: the three anomalies, demonstrated on order_line_wide.

\echo '--- the same facts are stored many times'
SELECT customer_id, customer_name, customer_city, count(*) AS rows_holding_it
FROM order_line_wide GROUP BY 1,2,3 ORDER BY 1;

\echo '--- INSERT anomaly: a new product nobody has ordered yet has nowhere to go'
DO $$ BEGIN
  INSERT INTO order_line_wide (order_id, product_id, order_date, customer_id, customer_name,
                               customer_city, product_name, list_price, qty)
  VALUES (NULL, 'P5', current_date, 'none', 'none', 'none', 'Milk jug', 12.00, 1);
EXCEPTION WHEN not_null_violation THEN RAISE NOTICE 'refused: %', SQLERRM; END $$;

\echo '--- UPDATE anomaly: Nadia moves to Porto, but the update misses one of her rows'
UPDATE order_line_wide SET customer_city = 'Porto'
WHERE customer_id = 'C1' AND order_id = 'O3';          -- a careless WHERE clause
SELECT customer_id, array_agg(DISTINCT customer_city) AS cities_on_file
FROM order_line_wide WHERE customer_id = 'C1' GROUP BY customer_id;

\echo '--- DELETE anomaly: cancel order O4 and customer C3 and product P4 vanish with it'
DELETE FROM order_line_wide WHERE order_id = 'O4';
SELECT (SELECT count(*) FROM order_line_wide WHERE customer_id = 'C3') AS rows_about_priya,
       (SELECT count(*) FROM order_line_wide WHERE product_id  = 'P4') AS rows_about_tea_tin;
