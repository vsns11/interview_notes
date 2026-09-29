-- P26 Sales reps who never took an order from the company called 'RED'
CREATE TABLE sales_rep (rep_id int PRIMARY KEY, name text);
CREATE TABLE company (com_id int PRIMARY KEY, name text, city text);
CREATE TABLE orders (order_id int PRIMARY KEY, order_date date, com_id int, rep_id int, amount int);
INSERT INTO sales_rep VALUES (1,'Jo'),(2,'Kai'),(3,'Lu'),(4,'Mo'),(5,'Ned');
INSERT INTO company VALUES (1,'RED','Boston'),(2,'ORANGE','Paris'),(3,'YELLOW','Lima');
INSERT INTO orders VALUES (1,'2024-01-01',3,4,10000),(2,'2024-02-01',2,5,5000),
 (3,'2024-03-01',1,1,50000),(4,'2024-04-01',1,4,25000);
\echo '--- solution: NOT EXISTS'
SELECT r.name
FROM sales_rep r
WHERE NOT EXISTS (
  SELECT 1 FROM orders o JOIN company c ON c.com_id = o.com_id
  WHERE o.rep_id = r.rep_id AND c.name = 'RED')
ORDER BY r.name;
\echo '--- NOT IN gives the same answer ... until a RED order has no rep'
SELECT r.name FROM sales_rep r
WHERE r.rep_id NOT IN (SELECT o.rep_id FROM orders o JOIN company c USING (com_id) WHERE c.name = 'RED')
ORDER BY r.name;
INSERT INTO orders VALUES (5,'2024-05-01',1,NULL,700);
\echo '--- NOT IN after inserting a RED order with rep_id NULL: zero rows'
SELECT r.name FROM sales_rep r
WHERE r.rep_id NOT IN (SELECT o.rep_id FROM orders o JOIN company c USING (com_id) WHERE c.name = 'RED')
ORDER BY r.name;
\echo '--- NOT EXISTS is unaffected'
SELECT r.name FROM sales_rep r
WHERE NOT EXISTS (SELECT 1 FROM orders o JOIN company c USING (com_id)
                  WHERE o.rep_id = r.rep_id AND c.name = 'RED')
ORDER BY r.name;
