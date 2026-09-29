-- P23 Sellers with no sales at all in 2020, by name
CREATE TABLE seller (seller_id int PRIMARY KEY, seller_name text);
CREATE TABLE orders (order_id int PRIMARY KEY, sold_on date, cost int, customer_id int, seller_id int);
INSERT INTO seller VALUES (1,'Quill Co'),(2,'Reed Ltd'),(3,'Sable Inc'),(4,'Tern LLC');
INSERT INTO orders VALUES (1,'2020-03-01',120,101,1),(2,'2020-12-31',80,102,1),
 (3,'2019-11-20',60,101,2),(4,'2021-01-01',50,103,3),(5,'2020-06-15',30,104,3);
\echo '--- solution: NOT EXISTS with a half-open date range'
SELECT s.seller_name
FROM seller s
WHERE NOT EXISTS (
  SELECT 1 FROM orders o
  WHERE o.seller_id = s.seller_id
    AND o.sold_on >= DATE '2020-01-01' AND o.sold_on < DATE '2021-01-01')
ORDER BY s.seller_name;
