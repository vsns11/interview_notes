-- P24 Accounts with no transaction during 2020, by name
CREATE TABLE account (account_id int PRIMARY KEY, account_name text);
CREATE TABLE txn (txn_id int PRIMARY KEY, account_id int, txn_date date);
INSERT INTO account VALUES (1,'Harbor'),(2,'Juniper'),(3,'Kestrel'),(4,'Lotus');
INSERT INTO txn VALUES (10,1,'2020-05-05'),(11,2,'2019-07-01'),(12,2,'2021-02-02'),(13,4,'2020-12-31');
\echo '--- solution: LEFT JOIN with the date filter in ON, keep the misses'
SELECT a.account_name
FROM account a
LEFT JOIN txn t
       ON t.account_id = a.account_id
      AND t.txn_date >= DATE '2020-01-01' AND t.txn_date < DATE '2021-01-01'
WHERE t.txn_id IS NULL
ORDER BY a.account_name;
\echo '--- bug: date filter moved to WHERE -- it can never be true when t is NULL, so 0 rows'
SELECT a.account_name
FROM account a
LEFT JOIN txn t ON t.account_id = a.account_id
WHERE t.txn_id IS NULL
  AND t.txn_date >= DATE '2020-01-01' AND t.txn_date < DATE '2021-01-01'
ORDER BY a.account_name;
\echo '--- alternative: EXCEPT (set difference of ids)'
SELECT account_name FROM account
WHERE account_id IN (SELECT account_id FROM account
                     EXCEPT
                     SELECT account_id FROM txn
                     WHERE txn_date >= DATE '2020-01-01' AND txn_date < DATE '2021-01-01')
ORDER BY account_name;
