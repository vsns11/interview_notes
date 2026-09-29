-- Chapter 10 - Data modeling: the bike-rental shop used in 10.1-10.4.
-- Physical model (PostgreSQL 14) of the logical design:
--   customer 1 --- N rental N --- 1 bike N --- 1 bike_model
-- bike_model is the "title" (what kind of bike), bike is the physical object.

SET client_min_messages = warning;
DROP TABLE IF EXISTS rental, bike, bike_model, customer CASCADE;

CREATE TABLE customer (
  customer_id   bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,  -- surrogate key
  full_name     varchar(120) NOT NULL,
  email         varchar(254) NOT NULL UNIQUE,                     -- natural key kept as alternate key
  customer_type varchar(10)  NOT NULL CHECK (customer_type IN ('casual','member')),
  joined_on     date         NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE bike_model (
  model_id    bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  model_name  varchar(80)  NOT NULL UNIQUE,
  kind        varchar(20)  NOT NULL CHECK (kind IN ('city','road','mountain','e-bike')),
  hourly_rate numeric(6,2) NOT NULL CHECK (hourly_rate > 0)       -- money: numeric, never float
);

CREATE TABLE bike (
  bike_id    bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  model_id   bigint      NOT NULL REFERENCES bike_model(model_id), -- N:1, FK on the many side
  frame_no   varchar(20) NOT NULL UNIQUE,                           -- stamped on the frame
  bought_on  date        NOT NULL
);

CREATE TABLE rental (
  rental_id   bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  customer_id bigint      NOT NULL REFERENCES customer(customer_id),
  bike_id     bigint      NOT NULL REFERENCES bike(bike_id),
  out_at      timestamp   NOT NULL,
  due_at      timestamp   NOT NULL,
  back_at     timestamp,                          -- NULL = bike still out (meaningful NULL)
  CHECK (due_at > out_at),
  CHECK (back_at IS NULL OR back_at >= out_at)
);
CREATE INDEX rental_customer_idx ON rental(customer_id);   -- "rentals of this customer"
CREATE INDEX rental_bike_idx     ON rental(bike_id);       -- "who has this bike"

INSERT INTO customer (full_name, email, customer_type, joined_on) VALUES
  ('Meera Nair',   'meera@example.com', 'member', '2025-03-01'),
  ('Tom Okafor',   'tom@example.com',   'casual', '2026-05-20'),
  ('Lena Fischer', 'lena@example.com',  'member', '2024-11-11');

INSERT INTO bike_model (model_name, kind, hourly_rate) VALUES
  ('Urban 3',   'city',   4.00),
  ('Ridge 29',  'mountain', 7.50),
  ('Volt City', 'e-bike', 9.00);

INSERT INTO bike (model_id, frame_no, bought_on) VALUES
  (1, 'FR-1001', '2024-02-01'), (1, 'FR-1002', '2024-02-01'), (1, 'FR-1003', '2025-06-10'),
  (2, 'FR-2001', '2024-04-15'), (3, 'FR-3001', '2025-01-09');

INSERT INTO rental (customer_id, bike_id, out_at, due_at, back_at) VALUES
  (1, 1, '2026-09-01 09:00', '2026-09-01 12:00', '2026-09-01 11:40'),
  (2, 1, '2026-09-02 10:00', '2026-09-02 11:00', '2026-09-02 11:55'),
  (1, 4, '2026-09-27 08:00', '2026-09-27 18:00', NULL),
  (3, 5, '2026-09-27 09:30', '2026-09-27 10:30', NULL);
