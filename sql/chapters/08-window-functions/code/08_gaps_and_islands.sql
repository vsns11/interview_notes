-- 8.9  Gaps and islands
-- Step 1: the trick. For consecutive dates, date - row_number is constant.
SELECT user_id, login_date,
       row_number() OVER (PARTITION BY user_id ORDER BY login_date) AS rn,
       login_date - row_number() OVER (PARTITION BY user_id ORDER BY login_date)::int AS grp
FROM logins
WHERE user_id = 1
ORDER BY login_date;

-- Step 2: group by that key to get each streak ("island")
WITH marked AS (
    SELECT user_id, login_date,
           login_date - row_number() OVER (PARTITION BY user_id ORDER BY login_date)::int AS grp
    FROM logins
)
SELECT user_id, min(login_date) AS streak_start, max(login_date) AS streak_end,
       count(*) AS days
FROM marked
GROUP BY user_id, grp
ORDER BY user_id, streak_start;

-- Longest streak per user
WITH marked AS (
    SELECT user_id, login_date,
           login_date - row_number() OVER (PARTITION BY user_id ORDER BY login_date)::int AS grp
    FROM logins
), streaks AS (
    SELECT user_id, grp, count(*) AS days, min(login_date) AS from_day
    FROM marked GROUP BY user_id, grp
)
SELECT DISTINCT ON (user_id) user_id, days AS longest_streak, from_day
FROM streaks
ORDER BY user_id, days DESC, from_day;

-- Gaps: missing dates between consecutive rows, with LEAD
SELECT user_id,
       login_date + 1                                        AS gap_start,
       next_login - 1                                        AS gap_end,
       next_login - login_date - 1                           AS missing_days
FROM (
    SELECT user_id, login_date,
           lead(login_date) OVER (PARTITION BY user_id ORDER BY login_date) AS next_login
    FROM logins
) t
WHERE next_login - login_date > 1
ORDER BY user_id, gap_start;

-- The general form: flag where a new island starts, then running-sum the flags.
-- Works for any "consecutive" rule, e.g. sessions = events less than 30 min apart.
WITH ev(user_id, at) AS (VALUES
    (7, TIMESTAMP '2025-05-01 09:00'), (7, '2025-05-01 09:10'), (7, '2025-05-01 09:25'),
    (7, '2025-05-01 11:00'), (7, '2025-05-01 11:05'),
    (7, '2025-05-01 15:30')
), flagged AS (
    SELECT user_id, at,
           CASE WHEN at - lag(at) OVER (PARTITION BY user_id ORDER BY at) <= INTERVAL '30 minutes'
                THEN 0 ELSE 1 END AS new_session
    FROM ev
)
SELECT user_id, at, new_session,
       sum(new_session) OVER (PARTITION BY user_id ORDER BY at) AS session_no
FROM flagged
ORDER BY at;
