-- 19.4.2 Last person in the queue who still fits in a 500 kg lift
CREATE TABLE lift_queue (person_id int PRIMARY KEY, name text, kg int, turn int UNIQUE);
INSERT INTO lift_queue VALUES
 (1,'Rae',90,3),(2,'Sid',120,1),(3,'Tom',200,2),(4,'Uma',60,5),(5,'Vik',85,4);

WITH boarding AS (
  SELECT name, turn, sum(kg) OVER (ORDER BY turn) AS load_kg
  FROM   lift_queue
)
SELECT name, load_kg
FROM   boarding
WHERE  load_kg <= 500
ORDER  BY turn DESC
LIMIT  1;

-- Alternative: correlated subquery computes each running sum
SELECT q.name
FROM   lift_queue q
WHERE  (SELECT sum(kg) FROM lift_queue p WHERE p.turn <= q.turn) <= 500
ORDER  BY q.turn DESC
LIMIT  1;
