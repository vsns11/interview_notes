-- P2 Odd-id movies whose description is not 'boring', best rating first
\pset null '(null)'
CREATE TABLE movie (id int PRIMARY KEY, title text, description text, rating numeric(4,2));
INSERT INTO movie VALUES
 (1,'Night Train','thriller',8.10), (2,'Paper Moon','drama',7.40),
 (3,'Grey Days','boring',3.20),     (4,'Sky Race','action',6.90),
 (5,'Deep Blue','documentary',8.90),(7,'Untitled',NULL,5.00),
 (-3,'Rewind','comedy',6.00);
\echo '--- solution'
SELECT * FROM movie
WHERE id % 2 = 1 AND description <> 'boring'
ORDER BY rating DESC;
\echo '--- edge cases: negative id (-3 % 2 = -1) and NULL description'
SELECT * FROM movie
WHERE id % 2 <> 0 AND description IS DISTINCT FROM 'boring'
ORDER BY rating DESC;
