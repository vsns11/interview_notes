-- 9.11  UPSERT: INSERT ... ON CONFLICT (PostgreSQL 9.5+)
\pset null '(null)'
CREATE TABLE inventory (
    sku         text PRIMARY KEY,
    qty         integer NOT NULL CHECK (qty >= 0),
    updated_at  timestamp NOT NULL
);
INSERT INTO inventory VALUES ('MOUSE-1', 10, '2025-06-01 08:00'), ('HUB-2', 4, '2025-06-01 08:00');

-- DO NOTHING: ignore rows whose key already exists
INSERT INTO inventory VALUES ('MOUSE-1', 99, '2025-06-02 08:00'), ('LAMP-3', 7, '2025-06-02 08:00')
ON CONFLICT (sku) DO NOTHING
RETURNING sku, qty;

-- DO UPDATE: EXCLUDED is the row you tried to insert
INSERT INTO inventory AS i VALUES
    ('MOUSE-1', 5, '2025-06-03 08:00'),
    ('KEYB-4',  2, '2025-06-03 08:00')
ON CONFLICT (sku) DO UPDATE
SET qty        = i.qty + EXCLUDED.qty,          -- add to the existing stock
    updated_at = EXCLUDED.updated_at
RETURNING sku, qty, updated_at, (xmax <> 0) AS was_update;

-- A WHERE on DO UPDATE: only overwrite if the incoming data is newer
INSERT INTO inventory AS i VALUES ('HUB-2', 1, '2025-05-01 00:00')    -- older than what we have
ON CONFLICT (sku) DO UPDATE
SET qty = EXCLUDED.qty, updated_at = EXCLUDED.updated_at
WHERE EXCLUDED.updated_at > i.updated_at
RETURNING sku, qty;
SELECT * FROM inventory ORDER BY sku;

-- The same key twice in ONE statement is an error
\set ON_ERROR_STOP 0
INSERT INTO inventory VALUES ('PEN-5', 1, '2025-06-04'), ('PEN-5', 2, '2025-06-04')
ON CONFLICT (sku) DO UPDATE SET qty = EXCLUDED.qty;
\set ON_ERROR_STOP 1

-- A conflict target can also name a constraint
INSERT INTO inventory VALUES ('HUB-2', 3, '2025-06-05 08:00')
ON CONFLICT ON CONSTRAINT inventory_pkey DO UPDATE SET qty = EXCLUDED.qty
RETURNING sku, qty;

-- MERGE is PostgreSQL 15+; this server is 14, so it is a syntax error here
\set ON_ERROR_STOP 0
MERGE INTO inventory i USING (VALUES ('HUB-2', 9)) AS s(sku, qty) ON i.sku = s.sku
WHEN MATCHED THEN UPDATE SET qty = s.qty;
\set ON_ERROR_STOP 1
SELECT version() LIKE 'PostgreSQL 14%' AS running_pg14;
