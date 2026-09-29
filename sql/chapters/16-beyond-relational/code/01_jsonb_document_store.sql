-- 16.3 A document store inside Postgres: one JSONB column, varied shapes, GIN index.
CREATE TABLE products (
  id  bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  doc jsonb NOT NULL
);

INSERT INTO products (doc) VALUES
 ('{"sku":"LAP-13","type":"laptop","name":"Aero 13","price":999,
    "brand":"Nimbus","warranty_years":2,"specs":{"ram_gb":16,"cpu":"8-core"}}'),
 ('{"sku":"TSH-01","type":"tshirt","name":"Plain Tee","price":15,
    "sizes":["S","M","L"],"material":"cotton"}'),
 ('{"sku":"LAP-15","type":"laptop","name":"Aero 15","price":1399,
    "brand":"Nimbus","warranty_years":3,"specs":{"ram_gb":32,"cpu":"12-core"}}'),
 ('{"sku":"BK-042","type":"book","name":"Data at Scale","price":45,
    "authors":["R. Rao","M. Chen"],"pages":412}');

-- Each document carries its own fields: list the keys per product.
SELECT doc->>'sku' AS sku, (SELECT string_agg(k, ',' ORDER BY k) FROM jsonb_object_keys(doc) k) AS keys
FROM products ORDER BY id;

-- Query into nested fields: -> returns jsonb, ->> returns text, #>> follows a path.
SELECT doc->>'name' AS name, (doc->>'price')::numeric AS price,
       doc#>>'{specs,ram_gb}' AS ram_gb
FROM products WHERE doc->>'type' = 'laptop' ORDER BY price;

-- Containment (@>): "documents that contain this sub-document". Arrays match by element.
SELECT doc->>'name' AS name FROM products WHERE doc @> '{"sizes":["M"]}';
SELECT doc->>'name' AS name FROM products WHERE doc @> '{"brand":"Nimbus","specs":{"ram_gb":32}}';

-- Key existence (?), and a JSON path query with a filter.
SELECT doc->>'name' AS name FROM products WHERE doc ? 'authors';
SELECT jsonb_path_query(doc, '$.authors[*]') AS author FROM products WHERE doc->>'sku' = 'BK-042';

-- Partial update of one field without rewriting the document by hand.
UPDATE products SET doc = jsonb_set(doc, '{price}', '1299') WHERE doc->>'sku' = 'LAP-15';
UPDATE products SET doc = doc || '{"on_sale":true}' WHERE doc->>'sku' = 'TSH-01';
UPDATE products SET doc = doc - 'warranty_years' WHERE doc->>'sku' = 'LAP-13';
SELECT doc->>'sku' AS sku, doc->'price' AS price, doc->'on_sale' AS on_sale,
       doc->'warranty_years' AS warranty
FROM products ORDER BY id;

-- Indexing: a GIN index serves @>, ? and jsonb_path queries on any key.
INSERT INTO products (doc)
SELECT jsonb_build_object('sku', 'GEN-' || i, 'type', (ARRAY['laptop','tshirt','book','phone'])[1 + i % 4],
                          'name', 'Item ' || i, 'price', 5 + i % 500)
FROM generate_series(1, 20000) i;
VACUUM products;   -- prune old row versions from the UPDATEs above so the new indexes are usable at once
CREATE INDEX products_doc_gin ON products USING gin (doc jsonb_path_ops);
ANALYZE products;
EXPLAIN (COSTS OFF) SELECT count(*) FROM products WHERE doc @> '{"sku":"GEN-777"}';

-- A B-tree expression index serves equality/range on ONE extracted field.
CREATE INDEX products_price_idx ON products (((doc->>'price')::numeric));
ANALYZE products;
EXPLAIN (COSTS OFF) SELECT doc->>'sku' FROM products WHERE (doc->>'price')::numeric > 503;

-- Guard rails a pure document DB may not give you: a CHECK on required keys.
ALTER TABLE products ADD CONSTRAINT doc_has_sku_and_price
  CHECK (doc ? 'sku' AND jsonb_typeof(doc->'price') = 'number');
DO $$
BEGIN
  INSERT INTO products (doc) VALUES ('{"name":"No SKU","price":"cheap"}');
EXCEPTION WHEN check_violation THEN
  RAISE NOTICE 'rejected: %', SQLERRM;
END $$;
