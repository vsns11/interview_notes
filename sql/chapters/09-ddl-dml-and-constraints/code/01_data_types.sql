-- 9.1  Data types: choosing a type is choosing a rule
\pset null '(null)'
SET TimeZone = 'UTC';   -- make timestamptz output the same on every machine
CREATE TABLE type_demo (
    qty        integer,          -- 4 bytes, about +/- 2.1 billion
    views      bigint,           -- 8 bytes
    rank_no    smallint,         -- 2 bytes, -32768..32767
    price      numeric(10,2),    -- exact decimal: 10 digits, 2 after the point
    ratio      double precision, -- approximate (binary floating point)
    code       char(5),          -- fixed length, padded with spaces
    name       varchar(10),      -- variable length, max 10 characters
    notes      text,             -- variable length, no limit
    born_on    date,
    starts_at  time,
    created    timestamp,        -- date + time, no time zone
    created_tz timestamptz,      -- date + time, stored as an instant (UTC)
    active     boolean,
    birth_year smallint CHECK (birth_year BETWEEN 1900 AND 2100)  -- PostgreSQL has no YEAR type
);

INSERT INTO type_demo VALUES
 (3, 9000000000, 7, 19.999, 0.1::float8 + 0.2::float8, 'AB', 'Tara', 'long text ...',
  '1994-09-22', '09:30', '2025-06-01 10:00', '2025-06-01 10:00+05:30', true, 1994);

SELECT price, ratio, '[' || code || ']' AS code_padded, length(code) AS len_code,
       octet_length(code) AS bytes_code, created, created_tz
FROM type_demo;

-- Exact vs approximate
SELECT 0.1::numeric + 0.2::numeric = 0.3 AS numeric_exact,
       0.1::float8  + 0.2::float8  = 0.3 AS float_exact;

-- The type rejects bad data
\set ON_ERROR_STOP 0
INSERT INTO type_demo (name) VALUES ('Christopher Columbus');
INSERT INTO type_demo (rank_no) VALUES (40000);
INSERT INTO type_demo (price) VALUES (123456789.00);
INSERT INTO type_demo (born_on) VALUES ('2025-02-30');
INSERT INTO type_demo (birth_year) VALUES (1850);
\set ON_ERROR_STOP 1

-- Inspect the column types from the catalog
SELECT column_name, data_type, character_maximum_length AS max_len,
       numeric_precision AS prec, numeric_scale AS scale
FROM information_schema.columns
WHERE table_name = 'type_demo'
ORDER BY ordinal_position;
