-- P3 Products that are organic AND in compostable packaging
CREATE TABLE inventory (
  product_id int PRIMARY KEY,
  organic char(1) NOT NULL CHECK (organic IN ('Y','N')),
  compostable char(1) NOT NULL CHECK (compostable IN ('Y','N')));
INSERT INTO inventory VALUES (10,'Y','Y'),(11,'Y','N'),(12,'N','Y'),(13,'Y','Y'),(14,'N','N');
\echo '--- solution'
SELECT product_id FROM inventory
WHERE organic = 'Y' AND compostable = 'Y'
ORDER BY product_id;
\echo '--- the CHECK constraint rejects lower-case flags'
\set ON_ERROR_STOP off
INSERT INTO inventory VALUES (15,'y','Y');
\set ON_ERROR_STOP on
