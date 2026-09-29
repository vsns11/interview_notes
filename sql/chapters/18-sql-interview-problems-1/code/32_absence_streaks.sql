-- P32 Students absent on 3 or more consecutive calendar days; report each streak's first day
CREATE TABLE attendance (student_id int, att_date date, status text CHECK (status IN ('Present','Absent')),
                         PRIMARY KEY (student_id, att_date));
INSERT INTO attendance VALUES
 (1,'2024-01-01','Absent'),(1,'2024-01-02','Absent'),(1,'2024-01-03','Absent'),(1,'2024-01-04','Present'),
 (1,'2024-01-05','Absent'),(1,'2024-01-06','Absent'),(1,'2024-01-07','Absent'),(1,'2024-01-08','Absent'),
 (2,'2024-01-01','Absent'),(2,'2024-01-02','Absent'),(2,'2024-01-04','Absent'),(2,'2024-01-05','Absent'),
 (3,'2024-01-30','Absent'),(3,'2024-01-31','Absent'),(3,'2024-02-01','Absent');
\echo '--- step 1: date minus row number is constant inside a run of consecutive days'
SELECT student_id, att_date,
       row_number() OVER w AS rn,
       att_date - (row_number() OVER w)::int AS grp
FROM attendance
WHERE status = 'Absent' AND student_id = 1
WINDOW w AS (PARTITION BY student_id ORDER BY att_date)
ORDER BY att_date;
\echo '--- solution'
WITH absent AS (
  SELECT student_id, att_date,
         att_date - (row_number() OVER (PARTITION BY student_id ORDER BY att_date))::int AS grp
  FROM attendance
  WHERE status = 'Absent'
)
SELECT student_id, min(att_date) AS start_date, count(*) AS days
FROM absent
GROUP BY student_id, grp
HAVING count(*) >= 3
ORDER BY student_id, start_date;
