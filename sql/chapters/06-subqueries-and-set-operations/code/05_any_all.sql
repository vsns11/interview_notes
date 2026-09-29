-- 6.5  Comparing with ANY / SOME / ALL
\pset null '(null)'

-- More than ANY Sales salary = more than the smallest one
SELECT first_name, salary FROM employees
WHERE salary > ANY (SELECT salary FROM employees WHERE dept_id = 2)
  AND dept_id <> 2
ORDER BY salary;

-- More than ALL Sales salaries = more than the largest one
SELECT first_name, salary FROM employees
WHERE salary > ALL (SELECT salary FROM employees WHERE dept_id = 2)
ORDER BY salary;

-- = ANY is the same as IN; <> ALL is the same as NOT IN (and shares its NULL trap)
SELECT count(*) AS eq_any FROM products WHERE category = ANY (SELECT DISTINCT category FROM products WHERE unit_price > 300);
SELECT count(*) AS ne_all_with_null FROM products
WHERE unit_price <> ALL (SELECT unit_price FROM order_items UNION ALL SELECT NULL);

-- ALL over an empty set is TRUE; ANY over an empty set is FALSE
SELECT (5 > ALL (SELECT salary FROM employees WHERE dept_id = 6)) AS all_empty,
       (5 > ANY (SELECT salary FROM employees WHERE dept_id = 6)) AS any_empty;

-- PostgreSQL also accepts arrays on the right-hand side
SELECT name FROM products WHERE product_id = ANY (ARRAY[1, 3, 5]) ORDER BY product_id;
