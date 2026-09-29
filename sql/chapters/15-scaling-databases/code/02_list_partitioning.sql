-- 15.2 List partitioning: customers split by region (discrete values).
CREATE TABLE customers_by_region (
  customer_id int  NOT NULL,
  name        text NOT NULL,
  region      text NOT NULL,
  country     text NOT NULL
) PARTITION BY LIST (region);

CREATE TABLE customers_na   PARTITION OF customers_by_region FOR VALUES IN ('NA');
CREATE TABLE customers_eu   PARTITION OF customers_by_region FOR VALUES IN ('EU');
CREATE TABLE customers_apac PARTITION OF customers_by_region FOR VALUES IN ('APAC');

INSERT INTO customers_by_region SELECT * FROM customers;

SELECT tableoid::regclass AS partition, string_agg(name, ', ' ORDER BY name) AS customers
FROM customers_by_region GROUP BY 1 ORDER BY 1;

EXPLAIN (COSTS OFF)
SELECT name FROM customers_by_region WHERE region = 'EU';

-- A value with no matching partition (and no DEFAULT) is rejected:
DO $$
BEGIN
  INSERT INTO customers_by_region VALUES (13, 'Mateo', 'LATAM', 'Brazil');
EXCEPTION WHEN check_violation THEN
  RAISE NOTICE 'rejected: %', SQLERRM;
END $$;
