-- 3.4  Date and time functions
\pset null '(null)'
SET TIME ZONE 'UTC';   -- make timestamptz output the same on every machine
-- "Now" functions: output depends on when you run it
SELECT current_date AS today, current_time AS time_with_tz, localtime AS local_time,
       now() AS now_tstz, localtimestamp AS local_ts;

-- now() is frozen for the whole transaction; clock_timestamp() keeps moving
BEGIN;
SELECT now() AS tx_start, clock_timestamp() AS wall_clock;
SELECT pg_sleep(0.2);
SELECT now() AS tx_start_again, clock_timestamp() AS wall_clock_later;
COMMIT;

-- Taking the date part of a timestamp
SELECT placed_at, placed_at::date AS just_date, date_trunc('day', placed_at) AS truncated
FROM orders WHERE order_id = 103;

-- Date arithmetic
SELECT date '2024-01-01' + 3 AS plus_3_days, date '2024-01-01' + interval '3 days' AS plus_interval,
       date '2024-01-01' - interval '1 month' AS minus_month, date '2024-01-05' - date '2024-01-01' AS days_between;

-- Month arithmetic clamps to the last day of the month
SELECT date '2025-01-31' + interval '1 month' AS jan31_plus_month,
       date '2024-02-29' + interval '1 year' AS leap_plus_year,
       date '2025-01-31' + interval '1 month' - interval '1 month' AS round_trip;

-- Pulling fields out
SELECT extract(YEAR FROM date '2024-01-01') AS year, extract(MONTH FROM date '2024-01-01') AS month,
       extract(DAY FROM date '2024-01-01') AS day, extract(DOW FROM date '2024-01-01') AS dow_0_sun,
       extract(ISODOW FROM date '2024-01-01') AS isodow_1_mon, extract(QUARTER FROM date '2024-08-15') AS quarter,
       date_part('week', date '2024-01-01') AS iso_week;

-- Truncating to a unit (the start of the period)
SELECT placed_at, date_trunc('month', placed_at) AS month_start, date_trunc('week', placed_at) AS week_start,
       date_trunc('hour', placed_at) AS hour_start
FROM orders WHERE order_id = 107;

-- Formatting dates as text
SELECT to_char(date '2024-01-01', 'FMDay, FMMonth FMDD, YYYY') AS long_form,
       to_char(timestamp '2025-03-18 19:30', 'DD Mon YYYY HH12:MI AM') AS short_form,
       to_char(timestamp '2025-03-18 19:30', 'YYYY-"Q"Q') AS quarter_label;

-- Parsing text into dates
SELECT to_date('18/03/2025', 'DD/MM/YYYY') AS parsed_date,
       to_timestamp('2025-03-18 19:30', 'YYYY-MM-DD HH24:MI') AS parsed_ts,
       make_date(2025, 3, 18) AS made_date;

-- Differences between timestamps
SELECT order_id, placed_at, shipped_at,
       shipped_at - placed_at AS time_to_ship,
       round(extract(EPOCH FROM shipped_at - placed_at) / 3600, 1) AS hours_to_ship,
       shipped_at::date - placed_at::date AS calendar_days
FROM orders WHERE shipped_at IS NOT NULL AND order_id <= 104 ORDER BY order_id;

-- age(): a human-style years/months/days interval
SELECT first_name, hire_date, age(date '2025-07-01', hire_date) AS tenure_on_2025_07_01
FROM employees WHERE emp_id IN (1, 9, 15) ORDER BY emp_id;

-- Expected delivery = order date + 10 days (exercise)
SELECT order_id, placed_at::date AS order_date, (placed_at + interval '10 days')::date AS expected_by
FROM orders WHERE order_id IN (101, 105, 113) ORDER BY order_id;

-- Time zones: timestamptz stores an instant; display follows the session TimeZone
SET TIME ZONE 'UTC';
SELECT timestamptz '2025-03-18 19:30:00+05:30' AS shown_in_utc;
SET TIME ZONE 'Asia/Kolkata';
SELECT timestamptz '2025-03-18 19:30:00+05:30' AS shown_in_ist,
       timestamptz '2025-03-18 19:30:00+05:30' AT TIME ZONE 'America/New_York' AS wall_clock_ny,
       timestamp '2025-03-18 19:30' AT TIME ZONE 'Europe/London' AS london_wall_clock_as_instant;

-- A series of dates (handy for calendars; more in chapter 7)
SELECT d::date AS day, to_char(d, 'Dy') AS weekday
FROM generate_series(date '2025-03-01', date '2025-03-05', interval '1 day') AS d;
