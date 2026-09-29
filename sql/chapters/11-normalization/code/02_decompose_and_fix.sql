-- 11.10: decompose order_line_wide into 3NF, prove it is lossless, re-run the anomalies.

CREATE TABLE customer AS SELECT DISTINCT customer_id, customer_name, customer_city FROM order_line_wide;
CREATE TABLE product  AS SELECT DISTINCT product_id, product_name, list_price     FROM order_line_wide;
CREATE TABLE orders   AS SELECT DISTINCT order_id, order_date, customer_id        FROM order_line_wide;
CREATE TABLE order_item AS SELECT order_id, product_id, qty                       FROM order_line_wide;

ALTER TABLE customer   ADD PRIMARY KEY (customer_id);
ALTER TABLE product    ADD PRIMARY KEY (product_id);
ALTER TABLE orders     ADD PRIMARY KEY (order_id),
                       ADD FOREIGN KEY (customer_id) REFERENCES customer;
ALTER TABLE order_item ADD PRIMARY KEY (order_id, product_id),
                       ADD FOREIGN KEY (order_id)   REFERENCES orders ON DELETE CASCADE,
                       ADD FOREIGN KEY (product_id) REFERENCES product;

\echo '--- row counts after the split'
SELECT (SELECT count(*) FROM customer) AS customers, (SELECT count(*) FROM product) AS products,
       (SELECT count(*) FROM orders) AS orders, (SELECT count(*) FROM order_item) AS order_items;

\echo '--- lossless? join the pieces back and compare with the original, both directions'
CREATE VIEW rejoined AS
SELECT oi.order_id, oi.product_id, o.order_date, o.customer_id, c.customer_name, c.customer_city,
       p.product_name, p.list_price, oi.qty
FROM order_item oi JOIN orders o USING (order_id)
JOIN customer c USING (customer_id) JOIN product p USING (product_id);
SELECT (SELECT count(*) FROM (TABLE order_line_wide EXCEPT TABLE rejoined) a) AS missing_rows,
       (SELECT count(*) FROM (TABLE rejoined EXCEPT TABLE order_line_wide) b) AS invented_rows;

\echo '--- INSERT: a product with no orders is now fine'
INSERT INTO product VALUES ('P5', 'Milk jug', 12.00);
\echo '--- UPDATE: Nadia moves once, in one row'
UPDATE customer SET customer_city = 'Porto' WHERE customer_id = 'C1';
SELECT DISTINCT customer_city FROM rejoined WHERE customer_id = 'C1';
\echo '--- DELETE: cancel O4; Priya and the tea tin survive'
DELETE FROM orders WHERE order_id = 'O4';
SELECT (SELECT count(*) FROM customer WHERE customer_id = 'C3') AS priya_rows,
       (SELECT count(*) FROM product  WHERE product_id  = 'P4') AS tea_tin_rows,
       (SELECT count(*) FROM order_item WHERE order_id = 'O4') AS o4_items;
