-- 10.4: the model decides which questions can be answered.
-- Because "bike" (physical object) is separate from "bike_model", these are short queries.

\echo '--- Q1: which bikes are out right now, and who has them?'
SELECT b.frame_no, m.model_name, c.full_name, r.due_at
FROM rental r
JOIN bike b       ON b.bike_id = r.bike_id
JOIN bike_model m ON m.model_id = b.model_id
JOIN customer c   ON c.customer_id = r.customer_id
WHERE r.back_at IS NULL
ORDER BY b.frame_no;

\echo '--- Q2: which physical bikes have never been rented?'
SELECT b.frame_no, m.model_name
FROM bike b JOIN bike_model m USING (model_id)
WHERE NOT EXISTS (SELECT 1 FROM rental r WHERE r.bike_id = b.bike_id)
ORDER BY b.frame_no;

\echo '--- Q3: how many bikes of each model exist, and how many are out?'
SELECT m.model_name,
       count(b.bike_id)                                   AS bikes,
       count(r.rental_id)                                 AS out_now
FROM bike_model m
LEFT JOIN bike b   ON b.model_id = m.model_id
LEFT JOIN rental r ON r.bike_id = b.bike_id AND r.back_at IS NULL
GROUP BY m.model_name
ORDER BY m.model_name;

\echo '--- Q4: late returns, with a late fee of 2.00 per started hour (fee is derived, not stored)'
SELECT r.rental_id, c.full_name,
       ceil(extract(epoch FROM (r.back_at - r.due_at)) / 3600) * 2.00 AS late_fee
FROM rental r JOIN customer c USING (customer_id)
WHERE r.back_at > r.due_at;
