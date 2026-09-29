-- 19.4.3 Average score-per-round and share of "improved and above 50" reviews
CREATE TABLE review_rounds (person text, rnd int, score int);
INSERT INTO review_rounds VALUES
 ('Kim',1,40),('Kim',2,55),('Kim',3,70),('Kim',4,65),
 ('Lou',1,80),('Lou',2,45);

WITH prev AS (
  SELECT person, rnd, score,
         lag(score) OVER (PARTITION BY person ORDER BY rnd) AS prev_score
  FROM   review_rounds
)
SELECT person,
       round(avg(score::numeric / rnd), 2)                                AS performance,
       round(100.0 * count(*) FILTER (WHERE score > 50 AND score > prev_score)
             / count(*), 2)                                               AS improvement_pct
FROM   prev
GROUP  BY person
ORDER  BY person;

-- Pitfall: integer / integer truncates in PostgreSQL
SELECT person, avg(score / rnd) AS truncated_performance
FROM   review_rounds GROUP BY person ORDER BY person;
