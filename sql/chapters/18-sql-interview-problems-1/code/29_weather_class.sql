-- P29 Classify each country's November 2019 weather: avg <= 15 Cold, avg >= 25 Hot, else Warm
CREATE TABLE country (country_id int PRIMARY KEY, country_name text);
CREATE TABLE weather (country_id int, weather_state int, day date, PRIMARY KEY (country_id, day));
INSERT INTO country VALUES (2,'Arland'),(3,'Borea'),(5,'Calidra'),(7,'Dune');
INSERT INTO weather VALUES
 (2,15,'2019-11-01'),(2,12,'2019-10-28'),(2,12,'2019-10-27'),
 (3,-2,'2019-11-10'),(3,0,'2019-11-11'),(3,3,'2019-11-12'),
 (5,16,'2019-11-07'),(5,18,'2019-11-09'),(5,21,'2019-11-23'),
 (7,25,'2019-11-28'),(7,22,'2019-12-01'),(7,20,'2019-12-02');
\echo '--- solution'
SELECT c.country_name,
       CASE WHEN avg(w.weather_state) <= 15 THEN 'Cold'
            WHEN avg(w.weather_state) >= 25 THEN 'Hot'
            ELSE 'Warm' END AS weather_type
FROM weather w
JOIN country c ON c.country_id = w.country_id
WHERE w.day >= DATE '2019-11-01' AND w.day < DATE '2019-12-01'
GROUP BY c.country_id, c.country_name
ORDER BY c.country_name;
