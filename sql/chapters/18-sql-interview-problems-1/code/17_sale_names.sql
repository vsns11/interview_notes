-- P17 Show product name, year and unit price for every sale, ordered by year
CREATE TABLE product (product_id int PRIMARY KEY, name text);
CREATE TABLE sale (sale_id int, product_id int REFERENCES product, yr int, qty int, unit_price int,
                   PRIMARY KEY (sale_id, yr));
INSERT INTO product VALUES (100,'Kettle'),(200,'Toaster'),(300,'Blender');
INSERT INTO sale VALUES (1,100,2023,10,25),(2,100,2022,12,27),(7,200,2023,15,40);
\echo '--- solution'
SELECT p.name AS product_name, s.yr AS year, s.unit_price AS price
FROM sale s
JOIN product p ON p.product_id = s.product_id
ORDER BY s.yr, s.sale_id;
