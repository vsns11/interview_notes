-- P15 For each day: number of different products sold and their names, alphabetical
\pset null '(null)'
CREATE TABLE sales_log (sold_on date, product text);
INSERT INTO sales_log VALUES
 ('2024-06-01','Pen'),('2024-06-01','Ink'),('2024-06-01','Pen'),
 ('2024-06-02','Stapler'),('2024-06-03','Pen'),('2024-06-03','Clip'),
 ('2024-06-03','Ink'),('2024-06-03',NULL);
\echo '--- solution'
SELECT sold_on,
       count(DISTINCT product)                          AS num_sold,
       string_agg(DISTINCT product, ',' ORDER BY product) AS products
FROM sales_log
GROUP BY sold_on
ORDER BY sold_on;
