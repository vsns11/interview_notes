-- 3.2  Number functions
\pset null '(null)'
SELECT abs(-10) AS abs, round(9.876, 1) AS round_1, round(9.876) AS round_0,
       round(1234.567, -2) AS round_neg, trunc(9.876, 1) AS trunc_1,
       ceil(4.3) AS ceil, floor(4.8) AS floor, ceil(-4.3) AS ceil_neg, floor(-4.3) AS floor_neg;

-- Halfway cases: numeric rounds away from zero, double precision rounds to even
SELECT round(2.5) AS num_2_5, round(-2.5) AS num_neg_2_5,
       round(2.5::double precision) AS dbl_2_5, round(3.5::double precision) AS dbl_3_5;

SELECT sqrt(25) AS sqrt, power(2, 3) AS power, 2 ^ 3 AS caret, mod(17, 5) AS mod,
       17 % 5 AS pct, mod(-17, 5) AS mod_neg, div(17, 5) AS int_div;

SELECT exp(2) AS exp_2, ln(10) AS ln_10, log(100) AS log_100, log10(100) AS log10_100,
       log(2, 8) AS log_base2_of_8;

SELECT sign(-15) AS sign, pi() AS pi, degrees(1.047) AS degrees, radians(60) AS radians;

SELECT greatest(3, 9, 4) AS greatest, least(3, 9, 4) AS least, gcd(12, 18) AS gcd, lcm(4, 6) AS lcm;

-- Random numbers: seed the generator to make them repeatable
SELECT setseed(0.42);
SELECT round(random()::numeric, 4) AS r1, round(random()::numeric, 4) AS r2;
SELECT floor(random() * 6)::int + 1 AS dice_roll;

-- Formatting for humans: to_char
SELECT to_char(1234567, 'FM9,999,999.00') AS with_commas,
       to_char(0.0725, 'FM990.00%') AS naive_pct,
       to_char(0.0725 * 100, 'FM990.00') || '%' AS pct,
       to_char(42, '00000') AS zero_padded;

-- Real use: line totals rounded to 2 decimals, and the average price per category bucket
SELECT order_id, product_id,
       quantity * unit_price * (1 - discount)            AS exact,
       round(quantity * unit_price * (1 - discount), 2)  AS rounded
FROM order_items WHERE order_id IN (104, 107, 109) ORDER BY order_id, product_id;

-- Salary per month, rounded up to the next hundred
SELECT first_name, salary, ceil(salary / 12 / 100) * 100 AS monthly_rounded_up
FROM employees WHERE emp_id <= 4 ORDER BY emp_id;
