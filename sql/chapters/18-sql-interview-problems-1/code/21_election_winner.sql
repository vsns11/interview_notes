-- P21 Name of the candidate with the most votes
CREATE TABLE candidate (id int PRIMARY KEY, name text);
CREATE TABLE vote (id int GENERATED ALWAYS AS IDENTITY PRIMARY KEY, candidate_id int REFERENCES candidate);
INSERT INTO candidate VALUES (1,'Maya'),(2,'Tariq'),(3,'Uma'),(4,'Victor');
INSERT INTO vote (candidate_id) VALUES (2),(4),(3),(2),(5-1),(2);
\echo '--- solution (exactly one winner is guaranteed)'
SELECT c.name
FROM candidate c
JOIN vote v ON v.candidate_id = c.id
GROUP BY c.id, c.name
ORDER BY count(*) DESC
LIMIT 1;
\echo '--- edge: a tie. LIMIT 1 hides it; FETCH ... WITH TIES shows everyone tied for first'
INSERT INTO vote (candidate_id) VALUES (4);
SELECT c.name, count(*) AS votes
FROM candidate c JOIN vote v ON v.candidate_id = c.id
GROUP BY c.id, c.name
ORDER BY votes DESC
FETCH FIRST 1 ROWS WITH TIES;
