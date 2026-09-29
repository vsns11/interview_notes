-- P42 Businesses that beat the per-event-type average on MORE THAN ONE event type
CREATE TABLE event (business_id int, event_type text, occurrences int, PRIMARY KEY (business_id, event_type));
INSERT INTO event VALUES
 (1,'reviews',7),(3,'reviews',3),(1,'ads',11),(2,'ads',7),(3,'ads',6),
 (1,'page views',3),(2,'page views',12),(4,'coupons',9);
\echo '--- step: attach each type''s average to every row with a window'
SELECT business_id, event_type, occurrences,
       round(avg(occurrences) OVER (PARTITION BY event_type), 2) AS type_avg
FROM event ORDER BY event_type, business_id;
\echo '--- solution'
SELECT business_id
FROM (SELECT business_id, occurrences,
             avg(occurrences) OVER (PARTITION BY event_type) AS type_avg
      FROM event) AS e
WHERE occurrences > type_avg
GROUP BY business_id
HAVING count(*) > 1
ORDER BY business_id;
