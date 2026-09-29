-- P10 Total units sold per product
\pset null '(null)'
CREATE TABLE product (product_id int PRIMARY KEY, name text);
CREATE TABLE sale (sale_id int, product_id int REFERENCES product, yr int, qty int, unit_price int,
                   PRIMARY KEY (sale_id, yr));
INSERT INTO product VALUES (100,'Kettle'),(200,'Toaster'),(300,'Blender');
INSERT INTO sale VALUES (1,100,2022,10,25),(2,100,2023,12,27),(7,200,2023,15,40),(8,100,2024,3,30);
\echo '--- solution'
SELECT product_id, sum(qty) AS total_quantity
FROM sale
GROUP BY product_id
ORDER BY product_id;
\echo '--- include products that never sold (LEFT JOIN + COALESCE)'
SELECT p.product_id, COALESCE(sum(s.qty), 0) AS total_quantity
FROM product p LEFT JOIN sale s USING (product_id)
GROUP BY p.product_id
ORDER BY p.product_id;
