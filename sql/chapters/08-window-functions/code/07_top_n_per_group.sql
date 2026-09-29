-- 8.8  Top-N per group and de-duplication
\pset null '(null)'

-- Window functions are computed after WHERE, so this is an error:
\set ON_ERROR_STOP 0
SELECT first_name, salary FROM employees
WHERE row_number() OVER (ORDER BY salary DESC) <= 3;
\set ON_ERROR_STOP 1

-- Top 2 earners per department: compute the rank in a CTE, filter outside.
-- Both departments have a tie for 3rd place (98000 in Engineering, 68000 in Sales).
WITH ranked AS (
    SELECT d.name AS dept, e.first_name, e.salary,
           row_number() OVER (PARTITION BY e.dept_id ORDER BY e.salary DESC, e.emp_id) AS rn,
           rank()       OVER (PARTITION BY e.dept_id ORDER BY e.salary DESC)            AS rnk,
           dense_rank() OVER (PARTITION BY e.dept_id ORDER BY e.salary DESC)            AS drnk
    FROM employees e JOIN departments d USING (dept_id)
)
SELECT dept, first_name, salary, rn, rnk, drnk
FROM ranked
WHERE dept IN ('Engineering', 'Sales') AND rnk <= 3
ORDER BY dept, rn;

-- "Exactly N rows" (row_number) vs "N places, ties included" (rank) vs "top N distinct values" (dense_rank)
WITH ranked AS (
    SELECT dept_id, first_name, salary,
           row_number() OVER (PARTITION BY dept_id ORDER BY salary DESC, emp_id) AS rn,
           rank()       OVER (PARTITION BY dept_id ORDER BY salary DESC)         AS rnk,
           dense_rank() OVER (PARTITION BY dept_id ORDER BY salary DESC)         AS drnk
    FROM employees WHERE dept_id = 2
)
SELECT 'row_number <= 3' AS rule, string_agg(first_name, ', ' ORDER BY rn) AS kept FROM ranked WHERE rn <= 3
UNION ALL
SELECT 'rank <= 3',       string_agg(first_name, ', ' ORDER BY rn) FROM ranked WHERE rnk <= 3
UNION ALL
SELECT 'dense_rank <= 2', string_agg(first_name, ', ' ORDER BY rn) FROM ranked WHERE drnk <= 2;

-- Latest order per customer (the "dedupe, keep newest" pattern)
SELECT customer_id, order_id, placed_at, status
FROM (
    SELECT o.*, row_number() OVER (PARTITION BY customer_id ORDER BY placed_at DESC) AS rn
    FROM orders o
) t
WHERE rn = 1
ORDER BY customer_id;

-- Deleting duplicate customers, keeping the lowest id per normalised email
WITH dups AS (
    SELECT customer_id,
           row_number() OVER (PARTITION BY lower(trim(email)) ORDER BY customer_id) AS rn
    FROM customers
    WHERE email IS NOT NULL
)
SELECT customer_id AS would_delete FROM dups WHERE rn > 1;

-- PostgreSQL shortcut for top-1 per group: DISTINCT ON
SELECT DISTINCT ON (dept_id) dept_id, first_name, salary
FROM employees
WHERE dept_id IS NOT NULL
ORDER BY dept_id, salary DESC, emp_id;
