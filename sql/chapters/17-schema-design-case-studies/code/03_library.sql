-- Case study: library management (schema "lib" in 00_schema.sql). "Today" is 2026-05-10.
SET search_path = lib;
SET timezone = 'UTC';
\set today '''2026-05-10'''

\echo '--- Q1: catalogue search: each title, its authors in order, and how many copies are on the shelf now'
SELECT t.title,
       string_agg(a.name, ', ' ORDER BY ta.position) AS authors,
       (SELECT count(*) FROM copies c
         WHERE c.isbn = t.isbn AND c.withdrawn_at IS NULL
           AND NOT EXISTS (SELECT 1 FROM loans l WHERE l.copy_id = c.copy_id AND l.returned_on IS NULL)
       ) AS available
FROM titles t
JOIN title_authors ta USING (isbn)
JOIN authors a USING (author_id)
GROUP BY t.isbn
ORDER BY t.title;

\echo '--- Q2: overdue loans and the fine owed at 0.25 per day'
SELECT m.name, t.title, l.due_on,
       date :today - l.due_on AS days_late,
       (date :today - l.due_on) * 0.25 AS fine
FROM loans l
JOIN members m USING (member_id)
JOIN copies c USING (copy_id)
JOIN titles t USING (isbn)
WHERE l.returned_on IS NULL AND l.due_on < date :today
ORDER BY days_late DESC;

\echo '--- Q3: the partial unique index refuses a second open loan for copy 101'
\set ON_ERROR_STOP off
INSERT INTO loans (copy_id, member_id, loaned_on, due_on) VALUES (101, 3, :today, date :today + 14);
\set ON_ERROR_STOP on

\echo '--- Q4: check-out that respects the member''s loan limit (Omar: limit 2, has 1 open loan)'
INSERT INTO loans (copy_id, member_id, loaned_on, due_on)
SELECT 102, 2, :today, date :today + 14
WHERE (SELECT count(*) FROM loans WHERE member_id = 2 AND returned_on IS NULL)
      < (SELECT max_loans FROM members WHERE member_id = 2)
RETURNING loan_id, copy_id, member_id, due_on;

\echo '--- Q5: who is next in the hold queue for "Joins at Scale"?'
SELECT m.name, h.placed_at,
       row_number() OVER (PARTITION BY h.isbn ORDER BY h.placed_at) AS queue_pos
FROM holds h JOIN members m USING (member_id)
WHERE h.isbn = '9780000000028'
ORDER BY queue_pos;

\echo '--- Q6: most borrowed titles (all-time loans, withdrawn copies included)'
SELECT t.title, count(l.loan_id) AS loans,
       round(avg(coalesce(l.returned_on, date :today) - l.loaned_on), 1) AS avg_days_kept
FROM titles t
LEFT JOIN copies c USING (isbn)
LEFT JOIN loans l USING (copy_id)
GROUP BY t.isbn
ORDER BY loans DESC, t.title;
