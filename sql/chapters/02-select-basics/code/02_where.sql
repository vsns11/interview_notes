-- 2.2  WHERE: filter rows
\pset null '(null)'
SELECT name, category, unit_price
FROM products
WHERE category = 'Electronics';

-- Comparison on numbers and dates
SELECT first_name, hire_date FROM employees WHERE hire_date >= '2023-01-01' ORDER BY hire_date;

-- AND binds tighter than OR: this is NOT what it looks like
SELECT first_name, dept_id, salary
FROM employees
WHERE dept_id = 1 OR dept_id = 2 AND salary > 100000
ORDER BY emp_id;

-- Parentheses say what you mean
SELECT first_name, dept_id, salary
FROM employees
WHERE (dept_id = 1 OR dept_id = 2) AND salary > 100000
ORDER BY emp_id;

-- NOT
SELECT name FROM products WHERE NOT discontinued AND NOT unit_price < 100 ORDER BY name;

-- Case matters in string equality
SELECT count(*) AS exact_mumbai FROM customers WHERE city = 'Mumbai';
