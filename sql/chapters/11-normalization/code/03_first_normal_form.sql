-- 11.7: two 1NF violations and how to repair them in SQL.

-- Violation 1: a list packed into one cell.
CREATE TABLE volunteer_packed (vol_id int PRIMARY KEY, name text, languages text);
INSERT INTO volunteer_packed VALUES
  (1, 'Hana',  'Japanese, English'),
  (2, 'Diego', 'Spanish'),
  (3, 'Ama',   'Twi, English, French');

\echo '--- who speaks English? LIKE is the only tool, and it is fragile'
SELECT name FROM volunteer_packed WHERE languages LIKE '%English%' ORDER BY vol_id;

\echo '--- repair: one row per value (string_to_table needs PostgreSQL 14+)'
CREATE TABLE volunteer_language AS
SELECT vol_id, trim(lang) AS language
FROM volunteer_packed, string_to_table(languages, ',') AS lang;
ALTER TABLE volunteer_language ADD PRIMARY KEY (vol_id, language);
SELECT * FROM volunteer_language ORDER BY vol_id, language;

\echo '--- now counting and exact matching are ordinary SQL'
SELECT language, count(*) AS speakers FROM volunteer_language GROUP BY language ORDER BY speakers DESC, language;

-- Violation 2: a repeating group written sideways.
CREATE TABLE guest_sideways (guest_id int PRIMARY KEY, name text, phone1 text, phone2 text, phone3 text);
INSERT INTO guest_sideways VALUES
  (10, 'Ivo',   '555-0101', '555-0102', NULL),
  (11, 'Zara',  NULL,       NULL,       NULL),
  (12, 'Malik', '555-0301', NULL,       '555-0303');

\echo '--- unpivot the phone columns into rows; guests without a phone produce no rows'
SELECT g.guest_id, g.name, p.phone
FROM guest_sideways g
CROSS JOIN LATERAL (VALUES (g.phone1), (g.phone2), (g.phone3)) AS p(phone)
WHERE p.phone IS NOT NULL
ORDER BY g.guest_id, p.phone;
