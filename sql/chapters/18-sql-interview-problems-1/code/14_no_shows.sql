-- P14 How many appointments each patient missed (only patients with at least one)
CREATE TABLE appointment (id int PRIMARY KEY, patient_id int, doctor_id int, appt_date date,
  status text CHECK (status IN ('Completed','Cancelled','No-Show')));
INSERT INTO appointment VALUES
 (1,11,1,'2024-02-01','Completed'),(2,11,1,'2024-02-08','No-Show'),
 (3,12,2,'2024-02-02','No-Show'),  (4,12,2,'2024-02-09','No-Show'),
 (5,13,1,'2024-02-03','Cancelled'),(6,11,2,'2024-03-01','No-Show');
\echo '--- solution: filter first, then group'
SELECT patient_id, count(*) AS no_shows
FROM appointment
WHERE status = 'No-Show'
GROUP BY patient_id
ORDER BY patient_id;
\echo '--- alternative: FILTER + HAVING (handy when you need several counts at once)'
SELECT patient_id,
       count(*) FILTER (WHERE status = 'No-Show') AS no_shows,
       count(*) AS booked
FROM appointment
GROUP BY patient_id
HAVING count(*) FILTER (WHERE status = 'No-Show') > 0
ORDER BY patient_id;
