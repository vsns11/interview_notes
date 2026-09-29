-- P20 How many times each student sat each subject's exam (every student x subject pair, zeros included)
CREATE TABLE student (student_id int PRIMARY KEY, student_name text);
CREATE TABLE subject (subject_name text PRIMARY KEY);
CREATE TABLE exam_sitting (student_id int, subject_name text);
INSERT INTO student VALUES (1,'Noor'),(2,'Kofi'),(6,'Lin');
INSERT INTO subject VALUES ('Art'),('Math'),('Physics');
INSERT INTO exam_sitting VALUES (1,'Math'),(1,'Physics'),(1,'Math'),(2,'Art'),(1,'Math'),(2,'Math');
\echo '--- solution: build the grid with CROSS JOIN, then LEFT JOIN the facts'
SELECT st.student_id, st.student_name, su.subject_name,
       count(e.subject_name) AS attended_exams
FROM student st
CROSS JOIN subject su
LEFT JOIN exam_sitting e
       ON e.student_id = st.student_id AND e.subject_name = su.subject_name
GROUP BY st.student_id, st.student_name, su.subject_name
ORDER BY st.student_id, su.subject_name;
