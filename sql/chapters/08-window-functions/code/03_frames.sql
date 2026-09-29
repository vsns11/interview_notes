-- 8.4  Frames: which rows each calculation sees.
-- string_agg(...) OVER (frame) lists the days inside each row's frame.

-- ROWS: count physical rows
SELECT day, amount,
       string_agg(to_char(day, 'DD'), ',') OVER w AS frame_days,
       sum(amount) OVER w                          AS frame_sum
FROM daily_sales
WHERE store = 'Mumbai'
WINDOW w AS (ORDER BY day ROWS BETWEEN 2 PRECEDING AND CURRENT ROW)
ORDER BY day;

-- Centered frame: 1 before and 1 after
SELECT day, amount,
       string_agg(to_char(day, 'DD'), ',') OVER w AS frame_days,
       round(avg(amount) OVER w, 1)                AS centered_avg
FROM daily_sales
WHERE store = 'Mumbai'
WINDOW w AS (ORDER BY day ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING)
ORDER BY day;

-- ROWS vs RANGE on a series with a missing day (Pune is closed on the 4th)
SELECT day, amount,
       string_agg(to_char(day, 'DD'), ',') OVER (ORDER BY day
            ROWS BETWEEN 2 PRECEDING AND CURRENT ROW)                   AS rows_2,
       string_agg(to_char(day, 'DD'), ',') OVER (ORDER BY day
            RANGE BETWEEN INTERVAL '2 days' PRECEDING AND CURRENT ROW)  AS range_2_days
FROM daily_sales
WHERE store = 'Pune'
ORDER BY day;

-- GROUPS counts groups of peers (equal ORDER BY values) - PostgreSQL 11+
SELECT day, amount,
       string_agg(amount::int::text, ',') OVER (ORDER BY amount
            ROWS   BETWEEN 1 PRECEDING AND CURRENT ROW) AS rows_1,
       string_agg(amount::int::text, ',') OVER (ORDER BY amount
            RANGE  BETWEEN 150 PRECEDING AND CURRENT ROW) AS range_150,
       string_agg(amount::int::text, ',') OVER (ORDER BY amount
            GROUPS BETWEEN 1 PRECEDING AND CURRENT ROW) AS groups_1
FROM daily_sales
WHERE store = 'Pune'
ORDER BY amount, day;

-- EXCLUDE (PostgreSQL 11+): leave rows out of the frame
SELECT day, amount,
       round(avg(amount) OVER (ORDER BY day ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
                               EXCLUDE CURRENT ROW), 1) AS avg_of_other_days
FROM daily_sales
WHERE store = 'Mumbai'
ORDER BY day;
