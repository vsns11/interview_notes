-- 9.3  Keys and constraints
\pset null '(null)'
CREATE TABLE members (
    member_id  integer PRIMARY KEY,                       -- primary key
    email      text    NOT NULL UNIQUE,                   -- candidate key -> alternate key
    phone      text    UNIQUE,                            -- unique key, NULL allowed
    full_name  text    NOT NULL,
    plan       text    NOT NULL DEFAULT 'free'
                       CONSTRAINT plan_is_known CHECK (plan IN ('free', 'pro', 'team')),
    joined_on  date    NOT NULL DEFAULT current_date,
    left_on    date,
    CONSTRAINT left_after_joined CHECK (left_on IS NULL OR left_on >= joined_on)   -- table-level check
);

INSERT INTO members (member_id, email, phone, full_name, joined_on)
VALUES (1, 'ravi@mail.example', NULL, 'Ravi Kumar', '2025-01-10'),
       (2, 'lena@mail.example', NULL, 'Lena Ortiz', '2025-02-01');     -- two NULL phones: fine
SELECT member_id, email, phone, plan FROM members ORDER BY member_id;

\set ON_ERROR_STOP 0
INSERT INTO members (member_id, email, full_name) VALUES (1, 'x@mail.example', 'Dup Id');     -- PK duplicate
INSERT INTO members (member_id, email, full_name, joined_on) VALUES (NULL, 'y@mail.example', 'No Id', '2025-03-01');   -- PK is NOT NULL
INSERT INTO members (member_id, email, full_name) VALUES (3, 'ravi@mail.example', 'Dup');     -- UNIQUE
INSERT INTO members (member_id, email, full_name, plan, joined_on) VALUES (4, 'z@mail.example', 'Z', 'gold', '2025-03-01');  -- CHECK
INSERT INTO members (member_id, email, full_name, joined_on, left_on)
VALUES (5, 'w@mail.example', 'W', '2025-03-01', '2025-01-01');                                 -- table CHECK
\set ON_ERROR_STOP 1

-- A CHECK passes when its expression is NULL (unknown), not only when true
INSERT INTO members (member_id, email, full_name, left_on) VALUES (6, 'n@mail.example', 'N', NULL);

-- Composite primary key + foreign keys with different ON DELETE actions
CREATE TABLE clubs (
    club_id  integer PRIMARY KEY,
    name     text NOT NULL UNIQUE
);
CREATE TABLE club_memberships (
    club_id    integer REFERENCES clubs(club_id)     ON DELETE CASCADE,   -- delete the club -> delete memberships
    member_id  integer REFERENCES members(member_id) ON DELETE RESTRICT,  -- can't delete a member who has memberships
    role       text NOT NULL DEFAULT 'member',
    PRIMARY KEY (club_id, member_id)                                       -- composite key
);
CREATE TABLE club_events (
    event_id   integer PRIMARY KEY,
    club_id    integer REFERENCES clubs ON DELETE SET NULL,               -- keep the event, forget the club
    title      text NOT NULL
);

INSERT INTO clubs VALUES (10, 'Chess'), (20, 'Hiking');
INSERT INTO club_memberships (club_id, member_id) VALUES (10, 1), (10, 2), (20, 1);
INSERT INTO club_events VALUES (100, 10, 'Blitz night'), (101, 20, 'Sunrise trek');

\set ON_ERROR_STOP 0
INSERT INTO club_memberships (club_id, member_id) VALUES (10, 1);       -- composite PK duplicate
INSERT INTO club_memberships (club_id, member_id) VALUES (30, 1);       -- FK: no club 30
DELETE FROM members WHERE member_id = 2;                                  -- RESTRICT
\set ON_ERROR_STOP 1

DELETE FROM clubs WHERE club_id = 10;                                     -- CASCADE + SET NULL
SELECT * FROM club_memberships ORDER BY club_id, member_id;
SELECT * FROM club_events ORDER BY event_id;

-- Every constraint is in the catalog (p=primary, u=unique, c=check, f=foreign key)
SELECT conrelid::regclass AS table_name, conname, contype
FROM pg_constraint
WHERE conrelid IN ('members'::regclass, 'club_memberships'::regclass)
ORDER BY 1, 3, 2;

-- Deferrable FK: checked at COMMIT instead of after each statement
CREATE TABLE people (
    person_id  integer PRIMARY KEY,
    partner_id integer REFERENCES people DEFERRABLE INITIALLY DEFERRED
);
BEGIN;
INSERT INTO people VALUES (1, 2);   -- person 2 doesn't exist yet: allowed for now
INSERT INTO people VALUES (2, 1);
COMMIT;                             -- checked here: both exist
SELECT * FROM people ORDER BY person_id;
