-- 16.8 Spatial queries with Postgres built-in geometry (no PostGIS needed for this demo).
-- point(x, y) on a flat plane; units here are kilometres on a small city grid.
CREATE TABLE store (id int PRIMARY KEY, name text, loc point NOT NULL);
INSERT INTO store VALUES
 (1,'Central',   point(0, 0)),  (2,'Riverside', point(3, 4)),  (3,'Airport', point(12, -5)),
 (4,'Old Town',  point(-2, 1)), (5,'Harbour',   point(6, 8)),  (6,'Mall',    point(1, -1));
INSERT INTO store SELECT 100 + i, 'Kiosk ' || i, point((i * 7919 % 400) / 10.0 - 20, (i * 104729 % 400) / 10.0 - 20)
FROM generate_series(1, 5000) i;
CREATE INDEX store_loc_gist ON store USING gist (loc);    -- R-tree-like index
ANALYZE store;

-- Proximity: 3 nearest named stores to a customer at (2, 2); <-> is distance
SELECT name, round((loc <-> point(2, 2))::numeric, 2) AS km
FROM store WHERE id < 100 ORDER BY loc <-> point(2, 2) LIMIT 3;

-- Radius query: named stores within 5 km of (0, 0)
SELECT name FROM store WHERE id < 100 AND loc <@ circle(point(0, 0), 5) ORDER BY name;

-- Area query: which named stores fall inside a delivery zone (a box)?
SELECT name FROM store WHERE id < 100 AND loc <@ box(point(2, 3), point(7, 9)) ORDER BY name;

-- The GiST index serves nearest-neighbour search directly (KNN ordering):
EXPLAIN (COSTS OFF) SELECT id FROM store ORDER BY loc <-> point(2, 2) LIMIT 3;
