-- P6 Comments longer than 100 characters
CREATE TABLE comment (id int PRIMARY KEY, body text);
INSERT INTO comment VALUES
 (1, 'Nice post!'),
 (2, repeat('ab ', 34)),            -- 102 characters
 (3, repeat('x', 100)),             -- exactly 100: not "more than"
 (4, repeat('é', 60));              -- 60 characters but 120 bytes in UTF-8
\echo '--- solution'
SELECT id FROM comment WHERE char_length(body) > 100 ORDER BY id;
\echo '--- characters vs bytes'
SELECT id, char_length(body) AS chars, octet_length(body) AS bytes FROM comment ORDER BY id;
