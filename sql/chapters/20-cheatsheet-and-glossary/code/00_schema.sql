-- Chapter 20 — tiny fixtures + a checker used by 01_cheatsheet_checks.sql.
-- chk(area, claim, got, expected) records one claim from the cheat sheets and
-- RAISES AN EXCEPTION if the server's answer differs (NULL-safe comparison).

CREATE TABLE check_log (
  id       int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  area     text NOT NULL,
  claim    text NOT NULL,
  got      text,
  expected text
);

CREATE FUNCTION chk(p_area text, p_claim text, p_got text, p_expected text)
RETURNS void LANGUAGE plpgsql AS $$
BEGIN
  IF p_got IS DISTINCT FROM p_expected THEN
    RAISE EXCEPTION 'cheat-sheet claim FAILED [%] %: got %, expected %',
      p_area, p_claim, coalesce(p_got, 'NULL'), coalesce(p_expected, 'NULL');
  END IF;
  INSERT INTO check_log (area, claim, got, expected)
  VALUES (p_area, p_claim, coalesce(p_got, 'NULL'), coalesce(p_expected, 'NULL'));
END $$;

-- scores with a tie (Bo and Cy) and a NULL
CREATE TABLE scores (name text PRIMARY KEY, team text, points int);
INSERT INTO scores VALUES
  ('Al', 'red', 30), ('Bo', 'red', 20), ('Cy', 'blue', 20),
  ('Di', 'blue', 10), ('Ed', 'blue', NULL);

-- running-total fixture: two rows share day 2 (peers)
CREATE TABLE sales (day int, amount int);
INSERT INTO sales VALUES (1, 10), (2, 10), (2, 20), (3, 5);

-- anti-join fixture: the ban list contains a NULL
CREATE TABLE customers (id int PRIMARY KEY, name text);
INSERT INTO customers VALUES (1, 'Ann'), (2, 'Ben'), (3, 'Cat');
CREATE TABLE banned (customer_id int);
INSERT INTO banned VALUES (1), (NULL);
