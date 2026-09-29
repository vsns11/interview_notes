-- P28 Per month and country: approved count/amount and chargeback count/amount.
--     A chargeback is reported in the month it arrived, with the original amount.
CREATE TABLE txn (id int PRIMARY KEY, country text, state text CHECK (state IN ('approved','declined')),
                  amount int, trans_date date);
CREATE TABLE chargeback (trans_id int REFERENCES txn, cb_date date);
INSERT INTO txn VALUES
 (101,'US','approved',1000,'2024-05-18'),(102,'US','declined',2000,'2024-05-19'),
 (103,'US','approved',3000,'2024-06-10'),(104,'US','declined',4000,'2024-06-13'),
 (105,'US','approved',5000,'2024-06-15'),(106,'DE','declined',700,'2024-07-01');
INSERT INTO chargeback VALUES (102,'2024-05-29'),(101,'2024-06-30'),(105,'2024-09-18');
\echo '--- solution: stack both event kinds with UNION ALL, then conditional aggregation'
WITH events AS (
  SELECT to_char(trans_date, 'YYYY-MM') AS month, country, 'approved' AS kind, amount
  FROM txn WHERE state = 'approved'
  UNION ALL
  SELECT to_char(c.cb_date, 'YYYY-MM'), t.country, 'chargeback', t.amount
  FROM chargeback c JOIN txn t ON t.id = c.trans_id
)
SELECT month, country,
       count(*)    FILTER (WHERE kind = 'approved')   AS approved_count,
       COALESCE(sum(amount) FILTER (WHERE kind = 'approved'), 0)   AS approved_amount,
       count(*)    FILTER (WHERE kind = 'chargeback') AS chargeback_count,
       COALESCE(sum(amount) FILTER (WHERE kind = 'chargeback'), 0) AS chargeback_amount
FROM events
GROUP BY month, country
ORDER BY month, country;
