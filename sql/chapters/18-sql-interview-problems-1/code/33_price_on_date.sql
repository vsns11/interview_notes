-- P33 Every product's price on 2024-03-15; before its first change a product costs 10
CREATE TABLE price_change (product_id int, new_price int, effective_on date,
                           PRIMARY KEY (product_id, effective_on));
INSERT INTO price_change VALUES
 (1,20,'2024-03-14'),(2,50,'2024-03-14'),(1,30,'2024-03-15'),
 (1,35,'2024-03-16'),(2,65,'2024-03-17'),(3,20,'2024-03-18');
\echo '--- solution: latest change on or before the date (DISTINCT ON), default via COALESCE'
WITH latest AS (
  SELECT DISTINCT ON (product_id) product_id, new_price
  FROM price_change
  WHERE effective_on <= DATE '2024-03-15'
  ORDER BY product_id, effective_on DESC
)
SELECT p.product_id, COALESCE(l.new_price, 10) AS price
FROM (SELECT DISTINCT product_id FROM price_change) AS p
LEFT JOIN latest l USING (product_id)
ORDER BY p.product_id;
\echo '--- alternative: correlated scalar subquery with LIMIT 1'
SELECT p.product_id,
       COALESCE((SELECT c.new_price FROM price_change c
                 WHERE c.product_id = p.product_id AND c.effective_on <= DATE '2024-03-15'
                 ORDER BY c.effective_on DESC LIMIT 1), 10) AS price
FROM (SELECT DISTINCT product_id FROM price_change) AS p
ORDER BY p.product_id;
