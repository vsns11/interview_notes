-- P16 Per customer: number of distinct products bought and the sorted list
CREATE TABLE purchase (bought_on date, customer_id int, product text);
INSERT INTO purchase VALUES
 ('2024-01-05',1,'Tea'),('2024-01-09',1,'Mug'),('2024-02-01',1,'Tea'),
 ('2024-01-07',2,'Coffee'),('2024-03-02',3,'Mug'),('2024-03-02',3,'Coffee'),('2024-03-03',3,'Tea');
\echo '--- solution (text list)'
SELECT customer_id,
       count(DISTINCT product) AS num_products,
       string_agg(DISTINCT product, ',' ORDER BY product) AS products
FROM purchase
GROUP BY customer_id
ORDER BY customer_id;
\echo '--- alternative: a real array instead of a comma string'
SELECT customer_id, array_agg(DISTINCT product ORDER BY product) AS products
FROM purchase GROUP BY customer_id ORDER BY customer_id;
