-- 2.8  Arithmetic and other operators
\pset null '(null)'
SELECT 5 + 3 AS add, 10 - 4 AS sub, 3 * 7 AS mul, 20 / 5 AS div, 17 % 3 AS modulo;

-- Integer division truncates; make one side numeric to keep the fraction
SELECT 7 / 2 AS int_div, 7 / 2.0 AS num_div, 7::numeric / 2 AS cast_div, -7 / 2 AS neg_int_div;

-- Exponent, square root, absolute value, factorial-free
SELECT 2 ^ 10 AS power, |/ 25 AS sqrt, @ -5 AS abs;

-- Precedence: * and / before + and -
SELECT 2 + 3 * 4 AS no_parens, (2 + 3) * 4 AS parens;

-- String concatenation
SELECT first_name || ' ' || last_name AS full_name FROM employees WHERE emp_id <= 3;

-- Operators on columns: line totals
SELECT order_id, product_id, quantity, unit_price, discount,
       quantity * unit_price * (1 - discount) AS line_total
FROM order_items WHERE order_id IN (102, 104) ORDER BY order_id, product_id;
