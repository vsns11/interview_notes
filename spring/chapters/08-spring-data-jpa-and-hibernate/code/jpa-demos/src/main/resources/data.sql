INSERT INTO author (name) VALUES ('Ursula K. Le Guin'), ('Terry Pratchett'), ('Octavia E. Butler'), ('Isaac Asimov');
INSERT INTO book (title, price, author_id, version) VALUES
  ('A Wizard of Earthsea', 9.99, 1, 0), ('The Dispossessed', 12.50, 1, 0),
  ('Guards! Guards!', 8.99, 2, 0), ('Small Gods', 10.99, 2, 0), ('Going Postal', 11.50, 2, 0),
  ('Kindred', 13.00, 3, 0),
  ('Foundation', 7.99, 4, 0), ('I, Robot', 6.99, 4, 0);
INSERT INTO tag (name) VALUES ('fantasy'), ('sci-fi'), ('classic');
INSERT INTO book_tag (book_id, tag_id) VALUES (1, 1), (1, 3), (2, 2), (3, 1), (4, 1), (6, 2), (7, 2), (7, 3), (8, 2);
INSERT INTO review (stars, book_id) VALUES (5, 1), (4, 3), (5, 3), (4, 6), (5, 7), (3, 8);
