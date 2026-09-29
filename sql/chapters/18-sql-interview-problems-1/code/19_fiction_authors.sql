-- P19 Fiction titles per author (authors with none are left out); most first, then by name
CREATE TABLE book (book_id int PRIMARY KEY, title text, genre text);
CREATE TABLE book_author (author_id int, book_id int REFERENCES book, author_name text,
                          PRIMARY KEY (author_id, book_id));
INSERT INTO book VALUES (1,'Salt Roads','Fiction'),(2,'Tiny Atoms','Science'),
 (3,'Glass River','Fiction'),(4,'Old Maps','History'),(5,'Moth Hour','Fiction');
INSERT INTO book_author VALUES (10,1,'Iris Vale'),(10,3,'Iris Vale'),(11,2,'Omar Reyes'),
 (11,5,'Omar Reyes'),(12,4,'Lena Park'),(13,5,'Iris Vale');   -- a second, different Iris Vale
\echo '--- solution: group by the id (names are not unique)'
SELECT a.author_id, a.author_name, count(*) AS fiction_books_count
FROM book_author a
JOIN book b ON b.book_id = a.book_id
WHERE b.genre = 'Fiction'
GROUP BY a.author_id, a.author_name
ORDER BY fiction_books_count DESC, a.author_name, a.author_id;
\echo '--- grouping by name only merges two different people'
SELECT a.author_name, count(*) AS fiction_books_count
FROM book_author a JOIN book b ON b.book_id = a.book_id
WHERE b.genre = 'Fiction'
GROUP BY a.author_name
ORDER BY fiction_books_count DESC, a.author_name;
