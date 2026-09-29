-- Chapter 11 - Normalization: one wide, unnormalized table (the running example).
-- Business rules (functional dependencies):
--   order_id              -> order_date, customer_id
--   customer_id           -> customer_name, customer_city
--   product_id            -> product_name, list_price
--   {order_id,product_id} -> qty
-- Candidate key: {order_id, product_id}
SET client_min_messages = warning;
DROP TABLE IF EXISTS order_line_wide CASCADE;

CREATE TABLE order_line_wide (
  order_id      text NOT NULL,
  product_id    text NOT NULL,
  order_date    date NOT NULL,
  customer_id   text NOT NULL,
  customer_name text NOT NULL,
  customer_city text NOT NULL,
  product_name  text NOT NULL,
  list_price    numeric(8,2) NOT NULL,
  qty           int  NOT NULL CHECK (qty > 0),
  PRIMARY KEY (order_id, product_id)
);

INSERT INTO order_line_wide VALUES
  ('O1','P1','2026-09-01','C1','Nadia Lopez','Lisbon','Kettle', 29.00, 1),
  ('O1','P2','2026-09-01','C1','Nadia Lopez','Lisbon','Mug',     6.50, 4),
  ('O2','P1','2026-09-03','C2','Jonas Berg', 'Oslo',  'Kettle', 29.00, 1),
  ('O3','P3','2026-09-05','C1','Nadia Lopez','Lisbon','Teapot', 18.00, 2),
  ('O4','P4','2026-09-06','C3','Priya Das',  'Kochi', 'Tea tin', 9.00, 3);
