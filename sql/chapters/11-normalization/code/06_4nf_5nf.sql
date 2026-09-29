-- 11.12-11.13: a multivalued dependency (4NF) and a join dependency (5NF).

-- 4NF: a restaurant's dishes and its delivery zones are independent lists.
CREATE TABLE menu_zone (restaurant text, dish text, zone text, PRIMARY KEY (restaurant, dish, zone));
INSERT INTO menu_zone VALUES
  ('Saffron','Biryani','North'), ('Saffron','Biryani','Centre'),
  ('Saffron','Dal','North'),     ('Saffron','Dal','Centre'),
  ('Nori','Ramen','East');

\echo '--- restaurant ->> dish | zone: every dish is paired with every zone (a full cross product)'
SELECT restaurant, count(DISTINCT dish) AS dishes, count(DISTINCT zone) AS zones, count(*) AS rows
FROM menu_zone GROUP BY restaurant ORDER BY restaurant;

\echo '--- adding one dish to Saffron needs one row PER ZONE; forget one and the table lies'
INSERT INTO menu_zone VALUES ('Saffron','Paneer tikka','North');     -- forgot Centre
SELECT restaurant, dish, count(*) AS zones_listed FROM menu_zone WHERE restaurant='Saffron'
GROUP BY 1,2 ORDER BY 2;
DELETE FROM menu_zone WHERE dish = 'Paneer tikka';

\echo '--- 4NF split: one table per independent list; the join rebuilds the original exactly'
CREATE TABLE restaurant_dish AS SELECT DISTINCT restaurant, dish FROM menu_zone;
CREATE TABLE restaurant_zone AS SELECT DISTINCT restaurant, zone FROM menu_zone;
SELECT (SELECT count(*) FROM restaurant_dish) AS dish_rows, (SELECT count(*) FROM restaurant_zone) AS zone_rows,
       (SELECT count(*) FROM (SELECT restaurant, dish, zone FROM restaurant_dish JOIN restaurant_zone USING (restaurant)
                              EXCEPT TABLE menu_zone) x) AS invented_rows;

-- 5NF: supplier-part-project under a cyclic business rule.
CREATE TABLE spj (supplier text, part text, project text, PRIMARY KEY (supplier, part, project));
INSERT INTO spj VALUES ('Acme','bolt','Bridge'), ('Acme','bolt','Tower'), ('Acme','nut','Tower'), ('Brix','bolt','Tower');
CREATE VIEW sp AS SELECT DISTINCT supplier, part    FROM spj;
CREATE VIEW pj AS SELECT DISTINCT part, project     FROM spj;
CREATE VIEW sj AS SELECT DISTINCT supplier, project FROM spj;

\echo '--- rows INVENTED by each two-way join (none of these is true)'
SELECT 'sp+pj' AS pieces, supplier, part, project FROM (SELECT * FROM sp JOIN pj USING (part) EXCEPT SELECT part, supplier, project FROM spj) a
UNION ALL
SELECT 'sp+sj', supplier, part, project FROM (SELECT * FROM sp JOIN sj USING (supplier) EXCEPT TABLE spj) b
UNION ALL
SELECT 'pj+sj', supplier, part, project FROM (SELECT * FROM pj JOIN sj USING (project) EXCEPT SELECT project, part, supplier FROM spj) c
ORDER BY 1;

\echo '--- the three-way join invents nothing: 5NF stores sp, pj and sj instead of spj'
SELECT count(*) AS invented_rows FROM (
  SELECT supplier, part, project FROM sp JOIN pj USING (part) JOIN sj USING (supplier, project)
  EXCEPT TABLE spj) x;
