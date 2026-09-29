-- 10.7 and 10.9: a ternary relationship, and two ways to store a weak entity.

-- Ternary: a supplier supplies a part FOR a specific site. One table, three FKs.
CREATE TABLE supplier (supplier_id int PRIMARY KEY, name text NOT NULL);
CREATE TABLE part     (part_id     int PRIMARY KEY, name text NOT NULL);
CREATE TABLE site     (site_id     int PRIMARY KEY, city text NOT NULL);
CREATE TABLE supplies (
  supplier_id int REFERENCES supplier, part_id int REFERENCES part, site_id int REFERENCES site,
  PRIMARY KEY (supplier_id, part_id, site_id)
);
INSERT INTO supplier VALUES (1,'Acme'),(2,'Bolt Co');
INSERT INTO part VALUES (1,'bolt'),(2,'panel');
INSERT INTO site VALUES (1,'Leeds'),(2,'Pune');
INSERT INTO supplies VALUES (1,1,1),(1,2,2),(2,1,2);

\echo '--- the ternary fact: who supplies what, where'
SELECT s.name AS supplier, p.name AS part, t.city
FROM supplies x JOIN supplier s USING (supplier_id) JOIN part p USING (part_id) JOIN site t USING (site_id)
ORDER BY 1,2,3;

\echo '--- keep only binary pairs (supplier-part, supplier-site) and join them back:'
WITH sp AS (SELECT DISTINCT supplier_id, part_id FROM supplies),
     ss AS (SELECT DISTINCT supplier_id, site_id FROM supplies)
SELECT s.name AS supplier, p.name AS part, t.city,
       EXISTS (SELECT 1 FROM supplies x WHERE (x.supplier_id,x.part_id,x.site_id)=(sp.supplier_id,sp.part_id,ss.site_id)) AS really_true
FROM sp JOIN ss USING (supplier_id)
JOIN supplier s USING (supplier_id) JOIN part p ON p.part_id = sp.part_id JOIN site t ON t.site_id = ss.site_id
ORDER BY 1,2,3;

-- Weak entity, option A: composite key (owner key + partial key).
CREATE TABLE building (building_id int PRIMARY KEY, name text NOT NULL);
CREATE TABLE room_a (
  building_id int NOT NULL REFERENCES building ON DELETE CASCADE,
  room_no     varchar(6) NOT NULL,                 -- partial key (discriminator)
  seats       int,
  PRIMARY KEY (building_id, room_no)
);
-- Weak entity, option B: surrogate key + UNIQUE on (owner key, partial key).
CREATE TABLE room_b (
  room_id     int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  building_id int NOT NULL REFERENCES building ON DELETE CASCADE,
  room_no     varchar(6) NOT NULL,
  seats       int,
  UNIQUE (building_id, room_no)                    -- without this, duplicates slip in
);
INSERT INTO building VALUES (1,'Main'),(2,'Annex');
INSERT INTO room_a VALUES (1,'101',30),(2,'101',12);          -- same room_no, different owners: fine
INSERT INTO room_b (building_id, room_no, seats) VALUES (1,'101',30),(2,'101',12);

\echo '--- both options refuse a second room 101 in building 1'
DO $$ BEGIN INSERT INTO room_a VALUES (1,'101',5);
EXCEPTION WHEN unique_violation THEN RAISE NOTICE 'room_a: %', SQLERRM; END $$;
DO $$ BEGIN INSERT INTO room_b (building_id, room_no, seats) VALUES (1,'101',5);
EXCEPTION WHEN unique_violation THEN RAISE NOTICE 'room_b: %', SQLERRM; END $$;

\echo '--- existence dependency: delete the Annex, its rooms go too'
DELETE FROM building WHERE building_id = 2;
SELECT (SELECT count(*) FROM room_a) AS room_a_rows, (SELECT count(*) FROM room_b) AS room_b_rows;
