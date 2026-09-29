-- 11.11: a table in 3NF but not BCNF, its BCNF decomposition, and the rule the split loses.
-- Rules: doctor -> specialty  (each doctor has one specialty)
--        {patient, specialty} -> doctor  (a patient sees one doctor per specialty)

CREATE TABLE visit_3nf (
  patient text, specialty text, doctor text,
  PRIMARY KEY (patient, specialty)
);
INSERT INTO visit_3nf VALUES
  ('Ali','Cardiology','Dr Osei'), ('Ali','Dermatology','Dr Lind'),
  ('Beth','Cardiology','Dr Osei'), ('Beth','Neurology','Dr Tan'),
  ('Chen','Cardiology','Dr Moreau');

\echo '--- the fact "Dr Osei is a cardiologist" is stored once per patient'
SELECT doctor, specialty, count(*) AS copies FROM visit_3nf GROUP BY 1,2 ORDER BY copies DESC, doctor;

\echo '--- nothing stops the copies disagreeing (update anomaly)'
UPDATE visit_3nf SET specialty = 'Oncology'
WHERE doctor = 'Dr Osei' AND patient = 'Ali';            -- Dr Osei switches field; only one row edited
SELECT doctor, array_agg(DISTINCT specialty) AS specialties FROM visit_3nf GROUP BY doctor HAVING count(DISTINCT specialty) > 1;
UPDATE visit_3nf SET specialty = 'Cardiology' WHERE doctor = 'Dr Osei';  -- undo

\echo '--- BCNF split on the offending FD doctor -> specialty'
CREATE TABLE doctor_specialty AS SELECT DISTINCT doctor, specialty FROM visit_3nf;
ALTER TABLE doctor_specialty ADD PRIMARY KEY (doctor);
CREATE TABLE patient_doctor AS SELECT patient, doctor FROM visit_3nf;
ALTER TABLE patient_doctor ADD PRIMARY KEY (patient, doctor),
                           ADD FOREIGN KEY (doctor) REFERENCES doctor_specialty;
SELECT (SELECT count(*) FROM (SELECT patient, specialty, doctor FROM patient_doctor JOIN doctor_specialty USING (doctor)
                              EXCEPT SELECT * FROM visit_3nf) x) AS invented_rows,
       (SELECT count(*) FROM (SELECT * FROM visit_3nf EXCEPT
                              SELECT patient, specialty, doctor FROM patient_doctor JOIN doctor_specialty USING (doctor)) y) AS missing_rows;

\echo '--- the cost: {patient, specialty} -> doctor is no longer checked by any one table'
INSERT INTO patient_doctor VALUES ('Ali', 'Dr Moreau');     -- accepted!
SELECT patient, specialty, array_agg(doctor ORDER BY doctor) AS doctors
FROM patient_doctor JOIN doctor_specialty USING (doctor)
GROUP BY patient, specialty HAVING count(*) > 1;
DELETE FROM patient_doctor WHERE (patient, doctor) = ('Ali', 'Dr Moreau');

\echo '--- one practical fix: keep the 3NF shape, but tie its copies to the master list with a composite FK'
ALTER TABLE doctor_specialty ADD UNIQUE (doctor, specialty);
ALTER TABLE visit_3nf ADD FOREIGN KEY (doctor, specialty)
      REFERENCES doctor_specialty (doctor, specialty) ON UPDATE CASCADE;
DO $$ BEGIN INSERT INTO visit_3nf VALUES ('Dan', 'Neurology', 'Dr Osei');   -- Osei is not a neurologist
EXCEPTION WHEN foreign_key_violation THEN RAISE NOTICE 'refused: %', SQLERRM; END $$;
UPDATE doctor_specialty SET specialty = 'Cardiac surgery' WHERE doctor = 'Dr Osei';  -- one edit...
SELECT * FROM visit_3nf WHERE doctor = 'Dr Osei' ORDER BY patient;                   -- ...every copy follows
