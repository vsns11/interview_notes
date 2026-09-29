-- P35 Click-through rate per ad = clicks*100 / (clicks + views), 0 when both are 0; 2 decimals
CREATE TABLE ad_action (ad_id int, user_id int, action text CHECK (action IN ('Clicked','Viewed','Ignored')),
                        PRIMARY KEY (ad_id, user_id));
INSERT INTO ad_action VALUES
 (1,1,'Clicked'),(2,2,'Clicked'),(3,3,'Viewed'),(5,5,'Ignored'),(1,7,'Ignored'),
 (2,7,'Viewed'),(3,5,'Clicked'),(1,4,'Viewed'),(2,11,'Viewed'),(1,2,'Clicked');
\echo '--- solution: NULLIF avoids division by zero, COALESCE turns the NULL into 0'
SELECT ad_id,
       round(COALESCE(100.0 * count(*) FILTER (WHERE action = 'Clicked')
                      / NULLIF(count(*) FILTER (WHERE action IN ('Clicked','Viewed')), 0), 0), 2) AS ctr
FROM ad_action
GROUP BY ad_id
ORDER BY ctr DESC, ad_id;
