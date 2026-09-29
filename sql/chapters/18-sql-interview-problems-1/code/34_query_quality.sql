-- P34 Per search query: quality = avg(rating / position), poor % = share of rows with rating < 3
CREATE TABLE search_log (query_name text, result text, position int, rating int);
INSERT INTO search_log VALUES
 ('Dog','Golden Retriever',1,5),('Dog','German Shepherd',2,5),('Dog','Mule',200,1),
 ('Cat','Shirazi',5,2),('Cat','Siamese',3,3),('Cat','Sphynx',7,4);
\echo '--- solution'
SELECT query_name,
       round(avg(rating::numeric / position), 2)                              AS quality,
       round(100.0 * count(*) FILTER (WHERE rating < 3) / count(*), 2)        AS poor_query_percentage
FROM search_log
GROUP BY query_name
ORDER BY query_name;
\echo '--- bug: integer division truncates each ratio before averaging'
SELECT query_name, avg(rating / position) AS int_quality,
       count(*) FILTER (WHERE rating < 3) / count(*) * 100 AS int_poor_pct
FROM search_log GROUP BY query_name ORDER BY query_name;
