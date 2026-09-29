-- 15.2 Vertical partitioning: split rarely-read wide columns into a 1:1 side table.
CREATE TABLE product_core (            -- hot: read on every listing page
  product_id int PRIMARY KEY,
  name       text NOT NULL,
  price      numeric(10,2) NOT NULL
);
CREATE TABLE product_details (         -- cold: read only on the product page
  product_id  int PRIMARY KEY REFERENCES product_core ON DELETE CASCADE,
  description text,
  spec_sheet  text
);
INSERT INTO product_core
SELECT i, 'Product ' || i, (i % 90) + 9.99 FROM generate_series(1, 1000) i;
INSERT INTO product_details
SELECT i, repeat('Long marketing copy. ', 40), repeat('spec;', 100) FROM generate_series(1, 1000) i;
ANALYZE product_core; ANALYZE product_details;

-- The listing query touches only the narrow table:
SELECT pg_size_pretty(pg_total_relation_size('product_core'))    AS core_size,
       pg_size_pretty(pg_total_relation_size('product_details')) AS details_size;

-- A view hides the split when you do need everything (costs a join):
CREATE VIEW product_full AS
SELECT c.product_id, c.name, c.price, d.description, d.spec_sheet
FROM product_core c LEFT JOIN product_details d USING (product_id);

SELECT product_id, name, price, left(description, 21) AS description_start
FROM product_full WHERE product_id = 42;
