-- 11.16: two controlled ways to put redundancy back for reads, and their cost.

CREATE TABLE product (product_id text PRIMARY KEY, name text NOT NULL, list_price numeric(8,2) NOT NULL);
CREATE TABLE orders  (order_id text PRIMARY KEY, customer text NOT NULL,
                      item_count int NOT NULL DEFAULT 0);           -- DENORMALIZED: derivable from order_item
CREATE TABLE order_item (
  order_id   text REFERENCES orders ON DELETE CASCADE,
  product_id text REFERENCES product,
  qty        int  NOT NULL CHECK (qty > 0),
  price_paid numeric(8,2) NOT NULL,   -- NOT redundancy: the price at order time is its own fact
  PRIMARY KEY (order_id, product_id)
);

-- Option 1: a stored summary column, kept correct by a trigger (the database does the bookkeeping).
CREATE FUNCTION bump_item_count() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    UPDATE orders SET item_count = item_count + NEW.qty WHERE order_id = NEW.order_id;
  ELSIF TG_OP = 'DELETE' THEN
    UPDATE orders SET item_count = item_count - OLD.qty WHERE order_id = OLD.order_id;
  ELSE
    UPDATE orders SET item_count = item_count - OLD.qty + NEW.qty WHERE order_id = NEW.order_id;
  END IF;
  RETURN NULL;
END $$;
CREATE TRIGGER order_item_count AFTER INSERT OR UPDATE OF qty OR DELETE ON order_item
FOR EACH ROW EXECUTE FUNCTION bump_item_count();

INSERT INTO product VALUES ('P1','Kettle',29.00), ('P2','Mug',6.50);
INSERT INTO orders (order_id, customer) VALUES ('O1','Nadia'), ('O2','Jonas');
INSERT INTO order_item VALUES ('O1','P1',1,29.00), ('O1','P2',4,6.50), ('O2','P1',2,27.00);
UPDATE order_item SET qty = 3 WHERE (order_id, product_id) = ('O1','P2');

\echo '--- stored item_count vs the value recomputed from order_item'
SELECT o.order_id, o.item_count AS stored, sum(oi.qty) AS recomputed
FROM orders o JOIN order_item oi USING (order_id) GROUP BY o.order_id, o.item_count ORDER BY 1;

\echo '--- the list price changes later; price_paid on old orders must not'
UPDATE product SET list_price = 31.00 WHERE product_id = 'P1';
SELECT oi.order_id, p.name, oi.price_paid, p.list_price AS todays_price
FROM order_item oi JOIN product p USING (product_id) WHERE product_id = 'P1' ORDER BY 1;

-- Option 2: a materialized view = a stored, pre-joined copy for reporting. Stale until refreshed.
CREATE MATERIALIZED VIEW sales_by_product AS
SELECT p.name, sum(oi.qty) AS units, sum(oi.qty * oi.price_paid) AS revenue
FROM order_item oi JOIN product p USING (product_id) GROUP BY p.name;

INSERT INTO order_item VALUES ('O2','P2',10,6.50);
\echo '--- after a new sale, before REFRESH (stale copy)'
SELECT * FROM sales_by_product ORDER BY name;
REFRESH MATERIALIZED VIEW sales_by_product;
\echo '--- after REFRESH'
SELECT * FROM sales_by_product ORDER BY name;
