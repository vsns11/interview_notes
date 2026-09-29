-- 14.4  JSON and JSONB: storing, querying, updating, indexing semi-structured data

-- json keeps the text exactly; jsonb parses it (drops duplicate keys & whitespace, reorders keys)
SELECT '{"b":1, "a":2, "a":3}'::json  AS as_json,
       '{"b":1, "a":2, "a":3}'::jsonb AS as_jsonb;

-- Product "specs" differ per category, so they go in a jsonb column
ALTER TABLE products ADD COLUMN specs jsonb NOT NULL DEFAULT '{}';
UPDATE products SET specs = CASE product_id
  WHEN 1 THEN '{"brand":"Nimbus","ram_gb":16,"ports":["usb-c","hdmi"],"screen":{"inches":14,"type":"OLED"}}'
  WHEN 2 THEN '{"brand":"Clicko","wireless":true,"ports":["usb-a"]}'
  WHEN 3 THEN '{"brand":"Nimbus","ports":["usb-c","usb-a","hdmi","ethernet"]}'
  WHEN 5 THEN '{"brand":"Liftr","height_cm":{"min":70,"max":120},"motor":"dual"}'
  WHEN 7 THEN '{"brand":"Hushh","wireless":true,"battery_h":30}'
  ELSE '{}' END::jsonb;

-- Reading: -> returns jsonb, ->> returns text, #>> follows a path
SELECT name,
       specs -> 'brand'                AS brand_json,
       specs ->> 'brand'               AS brand_text,
       specs #>> '{screen,type}'       AS screen_type,
       (specs ->> 'ram_gb')::int       AS ram_gb,
       specs -> 'ports' -> 0           AS first_port,
       jsonb_array_length(specs -> 'ports') AS n_ports
FROM   products WHERE specs ? 'ports' ORDER BY product_id;

-- Filtering: containment @>, key exists ?, any-key ?|, all-keys ?&
SELECT name FROM products WHERE specs @> '{"brand":"Nimbus"}'            ORDER BY 1;
SELECT name FROM products WHERE specs -> 'ports' ? 'hdmi'                 ORDER BY 1;
SELECT name FROM products WHERE specs ?| array['motor','battery_h']       ORDER BY 1;
SELECT name FROM products WHERE specs @> '{"wireless":true}' AND specs ?& array['brand','battery_h'];

-- SQL/JSON path (PostgreSQL 12+)
SELECT name, jsonb_path_query_first(specs, '$.height_cm.max') AS max_height
FROM   products WHERE specs @@ '$.height_cm.max > 100';

-- Updating: jsonb_set, || (merge), - (remove key), #- (remove path)
UPDATE products SET specs = jsonb_set(specs, '{ram_gb}', '32')            WHERE product_id = 1;
UPDATE products SET specs = specs || '{"color":"black"}'                   WHERE product_id = 2;
UPDATE products SET specs = specs - 'wireless'                             WHERE product_id = 7;
UPDATE products SET specs = specs #- '{screen,type}'                       WHERE product_id = 1;
SELECT product_id, specs FROM products WHERE product_id IN (1, 2, 7) ORDER BY 1;

-- Building JSON from rows (e.g. an API response) and exploding JSON back into rows
SELECT jsonb_build_object(
         'order_id', o.order_id,
         'status',   o.status,
         'items',    jsonb_agg(jsonb_build_object('product', p.name, 'qty', oi.quantity)
                               ORDER BY p.name)) AS order_doc
FROM   orders o JOIN order_items oi USING (order_id) JOIN products p USING (product_id)
WHERE  o.order_id = 108
GROUP  BY o.order_id, o.status;

SELECT key, value FROM products, jsonb_each(specs) WHERE product_id = 5;

SELECT *
FROM   jsonb_to_recordset('[{"sku":"A1","qty":2},{"sku":"B7","qty":5}]'::jsonb)
       AS t(sku text, qty int);

-- Indexing: a GIN index serves @>, ?, ?|, ?& and @@
VACUUM products;                -- prune old row versions left by the UPDATEs above, so the new index is usable at once
CREATE INDEX products_specs_gin ON products USING gin (specs);
ANALYZE products;
SET enable_seqscan = off;       -- tiny table: force the planner to show the index path
EXPLAIN (COSTS OFF) SELECT name FROM products WHERE specs @> '{"brand":"Nimbus"}';
-- A B-tree expression index serves equality/range on one extracted key
CREATE INDEX products_brand_idx ON products ((specs ->> 'brand'));
EXPLAIN (COSTS OFF) SELECT name FROM products WHERE specs ->> 'brand' = 'Nimbus';
RESET enable_seqscan;
