-- P37 Average selling price per product, weighted by units, using the price valid on the sale date
CREATE TABLE price (product_id int, start_d date, end_d date, price int,
                    PRIMARY KEY (product_id, start_d, end_d));
CREATE TABLE units_sold (product_id int, bought_on date, units int);
INSERT INTO price VALUES (1,'2024-02-17','2024-02-28',5),(1,'2024-03-01','2024-03-22',20),
 (2,'2024-02-01','2024-02-20',15),(2,'2024-02-21','2024-03-31',30),(3,'2024-01-01','2024-12-31',9);
INSERT INTO units_sold VALUES (1,'2024-02-25',100),(1,'2024-03-01',15),(2,'2024-02-10',200),(2,'2024-03-22',30);
\echo '--- solution'
SELECT p.product_id,
       round(COALESCE(sum(u.units * p.price)::numeric / sum(u.units), 0), 2) AS average_price
FROM price p
LEFT JOIN units_sold u
       ON u.product_id = p.product_id
      AND u.bought_on BETWEEN p.start_d AND p.end_d
GROUP BY p.product_id
ORDER BY p.product_id;
\echo '--- bug: plain AVG(price) ignores how many units sold at each price'
SELECT p.product_id, round(avg(p.price), 2) AS naive
FROM price p JOIN units_sold u ON u.product_id = p.product_id AND u.bought_on BETWEEN p.start_d AND p.end_d
GROUP BY p.product_id ORDER BY p.product_id;
