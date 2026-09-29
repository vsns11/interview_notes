-- 9.9  UPDATE
\pset null '(null)'
-- Raise Sales (dept 2) salaries by 10 %; RETURNING shows the new values
UPDATE employees
SET salary = salary * 1.10
WHERE dept_id = 2
RETURNING emp_id, first_name, salary;

-- Several columns at once; the right-hand sides all see the OLD row values
UPDATE products
SET unit_price   = unit_price * 0.9,
    discontinued = unit_price * 0.9 < 40          -- uses the old unit_price
WHERE category = 'Furniture'
RETURNING name, unit_price, discontinued;

-- CASE inside SET: different changes for different rows in one pass
UPDATE orders
SET status = CASE WHEN shipped_at IS NULL THEN 'cancelled' ELSE 'delivered' END
WHERE status IN ('pending', 'shipped') AND placed_at < '2025-06-10'
RETURNING order_id, status;

-- UPDATE ... FROM: use another table to decide the new value (PostgreSQL join-update)
ALTER TABLE customers ADD COLUMN lifetime_orders integer NOT NULL DEFAULT 0;
UPDATE customers c
SET lifetime_orders = t.n
FROM (SELECT customer_id, count(*) AS n FROM orders GROUP BY customer_id) t
WHERE t.customer_id = c.customer_id;
SELECT customer_id, first_name, lifetime_orders FROM customers ORDER BY customer_id LIMIT 4;

-- Scalar subquery in SET (standard SQL, works everywhere)
UPDATE departments d
SET budget = (SELECT coalesce(sum(salary), 0) * 1.5 FROM employees e WHERE e.dept_id = d.dept_id)
WHERE d.dept_id = 5
RETURNING name, budget;

-- The classic accident: UPDATE without WHERE changes EVERY row. Practise inside a transaction.
BEGIN;
UPDATE employees SET salary = 1;
SELECT count(*) AS rows_now_paid_1 FROM employees WHERE salary = 1;
ROLLBACK;
SELECT count(*) AS rows_paid_1_after_rollback FROM employees WHERE salary = 1;

-- Updates must still satisfy constraints
\set ON_ERROR_STOP 0
UPDATE employees SET dept_id = 99 WHERE emp_id = 4;
UPDATE products SET unit_price = -1 WHERE product_id = 1;
\set ON_ERROR_STOP 1
