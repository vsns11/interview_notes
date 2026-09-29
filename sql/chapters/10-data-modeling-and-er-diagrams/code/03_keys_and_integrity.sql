-- 10.15-10.16: keys and the integrity constraints the database enforces for you.
-- Each attempted violation runs inside a DO block that catches the error and prints it.

CREATE TABLE room (
  room_id  int PRIMARY KEY,                                    -- entity integrity: unique + NOT NULL
  code     varchar(10) NOT NULL UNIQUE,                        -- alternate (candidate) key
  capacity int NOT NULL CHECK (capacity BETWEEN 1 AND 500)     -- domain constraint
);
CREATE TABLE lecturer (
  lecturer_id int PRIMARY KEY,
  name        varchar(60) NOT NULL,
  email       varchar(120) UNIQUE,                             -- UNIQUE but nullable
  room_id     int REFERENCES room(room_id) ON DELETE SET NULL  -- optional link
);
CREATE TABLE booking (
  room_id  int  NOT NULL REFERENCES room(room_id) ON DELETE RESTRICT,
  day      date NOT NULL,
  slot     int  NOT NULL CHECK (slot BETWEEN 1 AND 8),
  PRIMARY KEY (room_id, day, slot)                             -- composite key
);
INSERT INTO room VALUES (1, 'A-101', 40), (2, 'B-12', 12), (3, 'LAB-3', 25);
INSERT INTO lecturer VALUES (1, 'Dr Park', 'park@uni.example', 1), (2, 'Dr Silva', NULL, 2), (3, 'Dr Wu', NULL, NULL);
INSERT INTO booking VALUES (1, '2026-10-05', 1), (1, '2026-10-05', 2);

\echo '--- 1. domain: capacity 900 is outside the allowed values'
DO $$ BEGIN INSERT INTO room VALUES (4, 'C-1', 900);
EXCEPTION WHEN check_violation THEN RAISE NOTICE '%', SQLERRM; END $$;

\echo '--- 2. entity integrity: a primary key may not be NULL'
DO $$ BEGIN INSERT INTO room VALUES (NULL, 'C-2', 10);
EXCEPTION WHEN not_null_violation THEN RAISE NOTICE '%', SQLERRM; END $$;

\echo '--- 3. key constraint: the alternate key code must be unique'
DO $$ BEGIN INSERT INTO room VALUES (5, 'A-101', 10);
EXCEPTION WHEN unique_violation THEN RAISE NOTICE '%', SQLERRM; END $$;

\echo '--- 4. referential integrity: no orphan rows'
DO $$ BEGIN INSERT INTO booking VALUES (99, '2026-10-05', 1);
EXCEPTION WHEN foreign_key_violation THEN RAISE NOTICE '%', SQLERRM; END $$;

\echo '--- 5. UNIQUE lets several rows have NULL (Dr Silva and Dr Wu both have no email)'
SELECT name, email FROM lecturer WHERE email IS NULL ORDER BY name;

\echo '--- 6. ON DELETE RESTRICT: room 1 still has bookings'
DO $$ BEGIN DELETE FROM room WHERE room_id = 1;
EXCEPTION WHEN foreign_key_violation THEN RAISE NOTICE '%', SQLERRM; END $$;

\echo '--- 7. ON DELETE SET NULL: deleting room 2 empties Dr Silva''s room_id'
DELETE FROM room WHERE room_id = 2;
SELECT name, room_id FROM lecturer ORDER BY lecturer_id;

\echo '--- 8. NULL is not equal to anything: = NULL finds nothing, IS NULL works'
SELECT (SELECT count(*) FROM lecturer WHERE room_id = NULL)  AS eq_null,
       (SELECT count(*) FROM lecturer WHERE room_id IS NULL) AS is_null,
       count(room_id) AS count_col, count(*) AS count_star
FROM lecturer;

\echo '--- 9. data can only DISPROVE a key: is {name} unique in today''s rows?'
SELECT name, count(*) FROM lecturer GROUP BY name HAVING count(*) > 1;   -- 0 rows today, still not a key
