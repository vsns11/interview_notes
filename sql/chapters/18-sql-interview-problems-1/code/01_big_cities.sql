-- P1 Big cities: area above 3000 km2 OR population above 5 million (invented towns)
\pset null '(null)'
CREATE TABLE city (name text PRIMARY KEY, country text, area_km2 int, population int);
INSERT INTO city VALUES
 ('Aldport','Avalon',4100,900000), ('Brimstead','Avalon',850,6200000),
 ('Coralbay','Kestria',3000,5000000), ('Dunmore','Kestria',120,300000),
 ('Eastvale','Lorien',NULL,7100000), ('Fernhill','Lorien',NULL,40000);
\echo '--- solution'
SELECT name FROM city
WHERE area_km2 > 3000 OR population > 5000000
ORDER BY name;
\echo '--- alternative: UNION of two simple filters'
SELECT name FROM city WHERE area_km2 > 3000
UNION
SELECT name FROM city WHERE population > 5000000
ORDER BY name;
