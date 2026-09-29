-- P36 Each book's share of all loans (its loans / total loans), 2 decimals, highest first
CREATE TABLE book (book_id int PRIMARY KEY, title text);
CREATE TABLE loan (loan_id int PRIMARY KEY, book_id int REFERENCES book, user_id int, loan_date date);
INSERT INTO book VALUES (1,'Dune Tide'),(2,'Ember'),(3,'Fjord'),(4,'Gale');
INSERT INTO loan VALUES (1,1,100,'2024-01-01'),(2,1,101,'2024-01-03'),(3,2,100,'2024-01-04'),
 (4,1,102,'2024-01-09'),(5,3,103,'2024-01-10'),(6,2,104,'2024-01-12');
\echo '--- solution'
SELECT b.book_id,
       round(count(l.loan_id)::numeric / NULLIF((SELECT count(*) FROM loan), 0), 2) AS lf
FROM book b
LEFT JOIN loan l ON l.book_id = b.book_id
GROUP BY b.book_id
ORDER BY lf DESC, b.book_id;
\echo '--- same with a window over the grouped rows (one pass, no scalar subquery)'
SELECT b.book_id,
       round(count(l.loan_id)::numeric / NULLIF(sum(count(l.loan_id)) OVER (), 0), 2) AS lf
FROM book b LEFT JOIN loan l ON l.book_id = b.book_id
GROUP BY b.book_id
ORDER BY lf DESC, b.book_id;
