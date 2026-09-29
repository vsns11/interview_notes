-- Chapter 15 sample data: a small shop with customers in three regions and 3,000 orders (2022-2024).
-- Plain (unpartitioned) tables; later files copy this data into partitioned tables.
CREATE TABLE customers (
  customer_id int PRIMARY KEY,
  name        text NOT NULL,
  region      text NOT NULL,          -- 'NA', 'EU' or 'APAC'
  country     text NOT NULL
);
INSERT INTO customers VALUES
 (1,'Asha','APAC','India'),   (2,'Ben','NA','USA'),       (3,'Chloe','EU','France'),
 (4,'Dmitri','EU','Germany'), (5,'Elena','NA','Canada'),  (6,'Farid','APAC','Singapore'),
 (7,'Grace','NA','USA'),      (8,'Hiro','APAC','Japan'),  (9,'Ines','EU','Spain'),
 (10,'Jack','NA','USA'),      (11,'Kavya','APAC','India'),(12,'Liam','EU','Ireland');

CREATE TABLE orders (
  order_id    int PRIMARY KEY,
  customer_id int NOT NULL REFERENCES customers,
  order_date  date NOT NULL,
  amount      numeric(10,2) NOT NULL
);
-- deterministic pseudo-random data: no random() so the output is the same on every run
INSERT INTO orders
SELECT i,
       (i % 12) + 1,
       date '2022-01-01' + (i * 7 % 1096),
       ((i * 37) % 500) + 10
FROM generate_series(1, 3000) AS i;
ANALYZE customers; ANALYZE orders;
