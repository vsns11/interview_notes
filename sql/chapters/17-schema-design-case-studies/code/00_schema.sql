-- Chapter 17 — four runnable case-study schemas, each in its own PostgreSQL schema:
--   shop  (e-commerce)   tix (ticket booking)   lib (library)   insta (photo-sharing feed)
-- All sample data is invented for this book. Timestamps are fixed so output is deterministic.

------------------------------------------------------------------ shop
CREATE SCHEMA shop;
SET search_path = shop;

CREATE TYPE order_status AS ENUM ('pending', 'paid', 'shipped', 'delivered', 'cancelled');

CREATE TABLE customers (
  customer_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  email       text NOT NULL UNIQUE,
  full_name   text NOT NULL,
  created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE addresses (
  address_id  bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  customer_id bigint NOT NULL REFERENCES customers ON DELETE CASCADE,
  line1 text NOT NULL, city text NOT NULL, postcode text NOT NULL, country char(2) NOT NULL
);

CREATE TABLE categories (
  category_id int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name        text NOT NULL,
  parent_id   int REFERENCES categories          -- self-reference: a category tree
);

CREATE TABLE products (
  product_id  bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  sku         text NOT NULL UNIQUE,
  name        text NOT NULL,
  category_id int NOT NULL REFERENCES categories,
  price       numeric(10,2) NOT NULL CHECK (price >= 0),
  stock       int NOT NULL CHECK (stock >= 0),
  deleted_at  timestamptz                         -- soft delete: old orders still point here
);

CREATE TABLE orders (
  order_id    bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  customer_id bigint NOT NULL REFERENCES customers,
  ship_to     bigint NOT NULL REFERENCES addresses,
  status      order_status NOT NULL DEFAULT 'pending',
  placed_at   timestamptz NOT NULL,
  total       numeric(12,2) NOT NULL DEFAULT 0     -- denormalized sum of order_items
);

CREATE TABLE order_items (
  order_id   bigint REFERENCES orders ON DELETE CASCADE,
  product_id bigint REFERENCES products,
  quantity   int NOT NULL CHECK (quantity > 0),
  unit_price numeric(10,2) NOT NULL,              -- price snapshot at purchase time
  PRIMARY KEY (order_id, product_id)
);

CREATE TABLE payments (
  payment_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id   bigint NOT NULL REFERENCES orders,
  amount     numeric(12,2) NOT NULL CHECK (amount > 0),
  method     text NOT NULL CHECK (method IN ('card', 'wallet', 'cod')),
  paid_at    timestamptz NOT NULL
);

CREATE TABLE reviews (
  customer_id bigint REFERENCES customers,
  product_id  bigint REFERENCES products,
  rating      smallint NOT NULL CHECK (rating BETWEEN 1 AND 5),
  body        text,
  PRIMARY KEY (customer_id, product_id)            -- one review per customer per product
);

CREATE INDEX ON orders (customer_id, placed_at DESC);
CREATE INDEX ON order_items (product_id);
CREATE INDEX ON products (category_id) WHERE deleted_at IS NULL;

INSERT INTO customers (email, full_name, created_at) VALUES
  ('mira@example.com', 'Mira Shah',  '2026-01-05'),
  ('tom@example.com',  'Tom Becker', '2026-02-11'),
  ('lin@example.com',  'Lin Zhao',   '2026-03-20');
INSERT INTO addresses (customer_id, line1, city, postcode, country) VALUES
  (1, '12 Park Rd', 'Leeds', 'LS1 4AB', 'GB'),
  (2, '8 Hauptstr', 'Berlin', '10115', 'DE'),
  (3, '3 Nanjing Rd', 'Shanghai', '200001', 'CN');
INSERT INTO categories (name, parent_id) VALUES
  ('Electronics', NULL), ('Audio', 1), ('Headphones', 2), ('Books', NULL);
INSERT INTO products (sku, name, category_id, price, stock, deleted_at) VALUES
  ('HP-100', 'Studio headphones', 3,  89.00, 12, NULL),
  ('SP-200', 'Bluetooth speaker', 2,  45.50,  0, NULL),
  ('BK-300', 'SQL for Humans',    4,  29.99, 40, NULL),
  ('HP-OLD', 'Wired earbuds',     3,   9.99,  0, '2026-04-01');
INSERT INTO orders (customer_id, ship_to, status, placed_at) VALUES
  (1, 1, 'delivered', '2026-03-01 10:00+00'),
  (1, 1, 'paid',      '2026-05-02 09:30+00'),
  (2, 2, 'shipped',   '2026-05-03 14:00+00'),
  (3, 3, 'cancelled', '2026-05-04 18:45+00');
INSERT INTO order_items VALUES
  (1, 4, 2,  9.99), (1, 3, 1, 29.99),
  (2, 1, 1, 89.00), (2, 3, 2, 27.00),
  (3, 2, 1, 45.50),
  (4, 1, 1, 89.00);
UPDATE orders o SET total = s.t
FROM (SELECT order_id, sum(quantity * unit_price) t FROM order_items GROUP BY order_id) s
WHERE s.order_id = o.order_id;
INSERT INTO payments (order_id, amount, method, paid_at) VALUES
  (1, 49.97, 'card', '2026-03-01 10:01+00'),
  (2, 143.00, 'wallet', '2026-05-02 09:31+00'),
  (3, 30.00, 'card', '2026-05-03 14:02+00');   -- short payment, caught by the reconciliation query
INSERT INTO reviews VALUES (1, 3, 5, 'Clear and practical'), (1, 4, 3, NULL), (2, 2, 4, 'Loud');

------------------------------------------------------------------ tix
CREATE SCHEMA tix;
SET search_path = tix;

CREATE TABLE venues (venue_id int PRIMARY KEY, name text NOT NULL, city text NOT NULL);
CREATE TABLE halls  (hall_id int PRIMARY KEY, venue_id int NOT NULL REFERENCES venues, name text NOT NULL);
CREATE TABLE seats (                               -- the physical seat, never changes
  seat_id  int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  hall_id  int NOT NULL REFERENCES halls,
  row_label char(1) NOT NULL, seat_no int NOT NULL,
  tier     text NOT NULL CHECK (tier IN ('standard', 'premium')),
  UNIQUE (hall_id, row_label, seat_no)
);
CREATE TABLE events (event_id int PRIMARY KEY, title text NOT NULL, duration_min int NOT NULL);
CREATE TABLE shows (
  show_id  int PRIMARY KEY,
  event_id int NOT NULL REFERENCES events,
  hall_id  int NOT NULL REFERENCES halls,
  starts_at timestamptz NOT NULL,
  UNIQUE (hall_id, starts_at)
);
CREATE TABLE users (user_id int PRIMARY KEY, email text NOT NULL UNIQUE);
CREATE TABLE bookings (
  booking_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id    int NOT NULL REFERENCES users,
  show_id    int NOT NULL REFERENCES shows,
  status     text NOT NULL CHECK (status IN ('pending', 'confirmed', 'cancelled')),
  created_at timestamptz NOT NULL
);
-- One row per (show, seat): the inventory that users compete for.
CREATE TABLE show_seats (
  show_id    int REFERENCES shows,
  seat_id    int REFERENCES seats,
  price      numeric(8,2) NOT NULL,
  status     text NOT NULL DEFAULT 'free' CHECK (status IN ('free', 'held', 'sold')),
  held_by    int REFERENCES users,
  hold_until timestamptz,
  booking_id bigint REFERENCES bookings,
  PRIMARY KEY (show_id, seat_id),
  CONSTRAINT held_needs_owner_and_expiry CHECK ((status = 'held') = (hold_until IS NOT NULL AND held_by IS NOT NULL)),
  CONSTRAINT sold_needs_booking CHECK ((status = 'sold') = (booking_id IS NOT NULL))
);
CREATE INDEX ON show_seats (show_id) WHERE status = 'free';

INSERT INTO venues VALUES (1, 'Riverside Cinema', 'Pune');
INSERT INTO halls  VALUES (1, 1, 'Hall A');
INSERT INTO seats (hall_id, row_label, seat_no, tier)
SELECT 1, r, n, CASE WHEN r = 'C' THEN 'premium' ELSE 'standard' END
FROM unnest(ARRAY['A','B','C']) r, generate_series(1, 4) n;
INSERT INTO events VALUES (1, 'The Long Query', 128);
INSERT INTO shows  VALUES (10, 1, 1, '2026-06-01 18:00+00'), (11, 1, 1, '2026-06-01 21:30+00');
INSERT INTO users  VALUES (1, 'asha@example.com'), (2, 'ben@example.com'), (3, 'chen@example.com');
INSERT INTO show_seats (show_id, seat_id, price)
SELECT s.show_id, se.seat_id, CASE se.tier WHEN 'premium' THEN 350 ELSE 200 END
FROM shows s CROSS JOIN seats se;

------------------------------------------------------------------ lib
CREATE SCHEMA lib;
SET search_path = lib;

CREATE TABLE authors (author_id int PRIMARY KEY, name text NOT NULL);
CREATE TABLE titles (                               -- the abstract book (one ISBN)
  isbn      char(13) PRIMARY KEY,
  title     text NOT NULL,
  published smallint
);
CREATE TABLE title_authors (                        -- M:N junction with ordering
  isbn      char(13) REFERENCES titles,
  author_id int REFERENCES authors,
  position  smallint NOT NULL,
  PRIMARY KEY (isbn, author_id)
);
CREATE TABLE copies (                               -- the physical item on the shelf
  copy_id  int PRIMARY KEY,
  isbn     char(13) NOT NULL REFERENCES titles,
  branch   text NOT NULL,
  condition text NOT NULL DEFAULT 'good',
  withdrawn_at date                                 -- soft delete for lost / discarded copies
);
CREATE TABLE members (
  member_id int PRIMARY KEY, name text NOT NULL,
  max_loans smallint NOT NULL DEFAULT 3,
  joined_on date NOT NULL
);
CREATE TABLE loans (
  loan_id    int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  copy_id    int NOT NULL REFERENCES copies,
  member_id  int NOT NULL REFERENCES members,
  loaned_on  date NOT NULL,
  due_on     date NOT NULL,
  returned_on date,
  CHECK (due_on > loaned_on),
  CHECK (returned_on IS NULL OR returned_on >= loaned_on)
);
-- A copy can be out on at most ONE open loan: a partial unique index enforces it.
CREATE UNIQUE INDEX one_open_loan_per_copy ON loans (copy_id) WHERE returned_on IS NULL;
CREATE INDEX ON loans (member_id) WHERE returned_on IS NULL;
CREATE TABLE fines (                                -- at most one fine per loan (1:1)
  loan_id  int PRIMARY KEY REFERENCES loans,
  amount   numeric(8,2) NOT NULL CHECK (amount > 0),   -- stored: the daily rate may change later
  paid_on  date
);
CREATE TABLE holds (                                -- reservation queue per title
  isbn      char(13) REFERENCES titles,
  member_id int REFERENCES members,
  placed_at timestamptz NOT NULL,
  PRIMARY KEY (isbn, member_id)
);

INSERT INTO authors VALUES (1, 'R. Kumar'), (2, 'J. Ortiz'), (3, 'S. Novak');
INSERT INTO titles VALUES
  ('9780000000011', 'Tables and Keys', 2019),
  ('9780000000028', 'Joins at Scale',  2022),
  ('9780000000035', 'Query Plans Explained', 2024);
INSERT INTO title_authors VALUES
  ('9780000000011', 1, 1), ('9780000000028', 2, 1), ('9780000000028', 3, 2), ('9780000000035', 1, 1);
INSERT INTO copies VALUES
  (101, '9780000000011', 'Central', 'good', NULL),
  (102, '9780000000011', 'North',   'worn', NULL),
  (201, '9780000000028', 'Central', 'good', NULL),
  (301, '9780000000035', 'Central', 'good', NULL),
  (302, '9780000000035', 'Central', 'good', '2026-02-01');
INSERT INTO members VALUES (1, 'Priya', 3, '2025-09-01'), (2, 'Omar', 2, '2026-01-15'), (3, 'Eva', 3, '2026-03-02');
INSERT INTO loans (copy_id, member_id, loaned_on, due_on, returned_on) VALUES
  (101, 1, '2026-04-01', '2026-04-15', '2026-04-14'),
  (101, 2, '2026-04-20', '2026-05-04', NULL),       -- overdue on 2026-05-10
  (201, 1, '2026-05-01', '2026-05-15', NULL),
  (301, 3, '2026-03-10', '2026-03-24', '2026-03-30'),
  (301, 1, '2026-04-02', '2026-04-16', '2026-04-20');
INSERT INTO holds VALUES ('9780000000028', 3, '2026-05-02 10:00+00'), ('9780000000028', 2, '2026-05-03 12:00+00');

------------------------------------------------------------------ insta
CREATE SCHEMA insta;
SET search_path = insta;

CREATE TABLE users (
  user_id   bigint PRIMARY KEY,
  username  text NOT NULL UNIQUE,
  is_private boolean NOT NULL DEFAULT false,
  follower_count int NOT NULL DEFAULT 0,          -- denormalized counters
  following_count int NOT NULL DEFAULT 0
);
CREATE TABLE follows (
  follower_id bigint REFERENCES users,
  followee_id bigint REFERENCES users,
  created_at  timestamptz NOT NULL,
  PRIMARY KEY (follower_id, followee_id),
  CHECK (follower_id <> followee_id)
);
CREATE INDEX ON follows (followee_id);             -- "who follows me?"
CREATE TABLE posts (
  post_id    bigint PRIMARY KEY,
  user_id    bigint NOT NULL REFERENCES users,
  caption    text,
  created_at timestamptz NOT NULL,
  like_count int NOT NULL DEFAULT 0,
  comment_count int NOT NULL DEFAULT 0
);
CREATE INDEX ON posts (user_id, created_at DESC, post_id DESC);   -- profile grid + feed merge
CREATE TABLE media (
  post_id  bigint REFERENCES posts ON DELETE CASCADE,
  position smallint,
  url      text NOT NULL,                          -- object-store key, never the bytes
  kind     text NOT NULL CHECK (kind IN ('image', 'video')),
  PRIMARY KEY (post_id, position)
);
CREATE TABLE likes (
  user_id bigint REFERENCES users,
  post_id bigint REFERENCES posts ON DELETE CASCADE,
  created_at timestamptz NOT NULL,
  PRIMARY KEY (post_id, user_id)                   -- a user likes a post at most once
);
CREATE TABLE comments (
  comment_id bigint PRIMARY KEY,
  post_id    bigint NOT NULL REFERENCES posts ON DELETE CASCADE,
  user_id    bigint NOT NULL REFERENCES users,
  parent_id  bigint REFERENCES comments,           -- threaded replies
  body       text NOT NULL,
  created_at timestamptz NOT NULL
);
CREATE TABLE direct_messages (                     -- sender and recipient: two FKs to users
  message_id   bigint PRIMARY KEY,
  sender_id    bigint NOT NULL REFERENCES users,
  recipient_id bigint NOT NULL REFERENCES users,
  body         text NOT NULL,
  sent_at      timestamptz NOT NULL
);
CREATE INDEX ON direct_messages (least(sender_id, recipient_id), greatest(sender_id, recipient_id), sent_at);
CREATE TABLE notifications (
  notification_id bigint PRIMARY KEY,
  user_id    bigint NOT NULL REFERENCES users,     -- who is notified
  actor_id   bigint REFERENCES users,              -- who caused it
  kind       text NOT NULL CHECK (kind IN ('like', 'comment', 'follow', 'message')),
  post_id    bigint REFERENCES posts ON DELETE CASCADE,   -- what to open when tapped
  created_at timestamptz NOT NULL,
  read_at    timestamptz
);
CREATE INDEX ON notifications (user_id, created_at DESC);
CREATE TABLE user_groups (                          -- "group" is a reserved word
  group_id   bigint PRIMARY KEY,
  name       text NOT NULL,
  created_by bigint NOT NULL REFERENCES users
);
CREATE TABLE group_members (                        -- multivalued "members" -> junction table
  group_id  bigint REFERENCES user_groups ON DELETE CASCADE,
  user_id   bigint REFERENCES users,
  joined_at timestamptz NOT NULL,
  PRIMARY KEY (group_id, user_id)
);
CREATE INDEX ON group_members (user_id);            -- "which groups has this user joined?"
CREATE TABLE hashtags (tag_id int PRIMARY KEY, tag text NOT NULL UNIQUE);
CREATE TABLE post_hashtags (
  post_id bigint REFERENCES posts ON DELETE CASCADE,
  tag_id  int REFERENCES hashtags,
  PRIMARY KEY (tag_id, post_id)
);

INSERT INTO users (user_id, username) VALUES (1, 'ana'), (2, 'bo'), (3, 'cy'), (4, 'dee');
INSERT INTO follows VALUES
  (1, 2, '2026-01-01'), (1, 3, '2026-01-02'), (2, 1, '2026-01-03'),
  (3, 2, '2026-01-04'), (4, 2, '2026-01-05'), (4, 1, '2026-01-06');
UPDATE users u SET follower_count  = (SELECT count(*) FROM follows WHERE followee_id = u.user_id),
                   following_count = (SELECT count(*) FROM follows WHERE follower_id = u.user_id);
INSERT INTO posts (post_id, user_id, caption, created_at) VALUES
  (1001, 2, 'Sunrise run #running',        '2026-05-01 06:10+00'),
  (1002, 3, 'Fresh bread #baking',         '2026-05-01 08:00+00'),
  (1003, 2, 'Trail views #running #hills', '2026-05-02 07:30+00'),
  (1004, 4, 'New desk setup',              '2026-05-02 09:00+00'),
  (1005, 3, 'Sourdough attempt 2 #baking', '2026-05-03 10:15+00'),
  (1006, 1, 'Hello world',                 '2026-05-03 11:00+00');
INSERT INTO media VALUES
  (1001, 1, 'media/2/1001-1.jpg', 'image'), (1003, 1, 'media/2/1003-1.jpg', 'image'),
  (1003, 2, 'media/2/1003-2.mp4', 'video'), (1005, 1, 'media/3/1005-1.jpg', 'image');
INSERT INTO hashtags VALUES (1, 'running'), (2, 'baking'), (3, 'hills');
INSERT INTO post_hashtags (tag_id, post_id) VALUES (1, 1001), (1, 1003), (3, 1003), (2, 1002), (2, 1005);
INSERT INTO likes VALUES
  (1, 1001, '2026-05-01 07:00+00'), (3, 1001, '2026-05-01 07:05+00'), (4, 1003, '2026-05-02 08:00+00'),
  (1, 1003, '2026-05-02 08:10+00'), (2, 1002, '2026-05-01 09:00+00'), (1, 1005, '2026-05-03 11:30+00');
INSERT INTO comments VALUES
  (1, 1003, 1, NULL, 'Where is this?', '2026-05-02 08:11+00'),
  (2, 1003, 2, 1,    'Peak District!', '2026-05-02 08:20+00'),
  (3, 1003, 1, 2,    'Adding it to my list', '2026-05-02 08:25+00'),
  (4, 1005, 2, NULL, 'Looks great',    '2026-05-03 12:00+00');
UPDATE posts p SET like_count    = (SELECT count(*) FROM likes    WHERE post_id = p.post_id),
                   comment_count = (SELECT count(*) FROM comments WHERE post_id = p.post_id);

RESET search_path;
