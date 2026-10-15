# Chapter 8 — Window Functions: Rankings, Running Totals and Row-to-Row Comparisons

`GROUP BY` gives "total per department" but discards the rows. Often you need both: *each* employee **and** the department average; *each* day **and** the running total; *each* order **and** the customer's previous order. **Window functions** compute across related rows — the *window* — while keeping every row. They replace awkward self-joins and correlated subqueries (re-run per outer row) and underlie many interview questions (top-N per group, running totals, second-highest salary, streaks, month-over-month growth).

**Sections:** 8.1 `OVER()` · 8.2 `PARTITION BY` · 8.3 `ORDER BY` in a window · 8.4 Frames: `ROWS` / `RANGE` / `GROUPS`, `EXCLUDE` · 8.5 Ranking · 8.6 Value functions · 8.7 Running & moving calculations · 8.8 Top-N per group & dedup (de-duplication: removing extra copies) · 8.9 Gaps and islands · 8.10 Named windows, `FILTER`, evaluation order, performance.

**Setup.** The `company` sample database from [chapter 1](../01-databases-and-the-relational-model/README.md#111-the-sample-database) plus two small tables added in [`code/00_schema.sql`](code/00_schema.sql):

| Table | Rows | Why |
|---|---|---|
| `daily_sales(store, day, amount)` | Mumbai 1–7 March 2025; Pune the same week **except 4 March** (closed) | running totals, moving averages, frames, a gap |
| `logins(user_id, login_date)` | two users' login days in May 2025 with breaks | streaks (gaps and islands) |

Deliberate ties: Mumbai's 3 and 4 March are both 1500, two Pune days are 650; Rahul and Meera earn 98 000, Arjun and Sara 68 000.

---

## 8.1 What a window function is

### What it is
A **window function** looks, for each row, at a set of related rows (its **window**) and returns one value — **without merging the rows**. It is recognised by `OVER (...)`:

```sql
function_name(args) OVER (
    [PARTITION BY ...]      -- split rows into independent groups
    [ORDER BY ...]          -- order rows inside each group
    [frame clause]          -- which rows around the current one to use
)
```

### The problem it solves
Compare `GROUP BY` with a window:
```sql
-- code/01_window_basics.sql
SELECT dept_id, round(avg(salary)) AS dept_avg
FROM employees WHERE dept_id IN (1, 2)
GROUP BY dept_id ORDER BY dept_id;
```
Output:
```
 dept_id | dept_avg
---------+----------
       1 |   130750
       2 |    89500
(2 rows)
```
Two rows; the employees are gone. With windows every employee stays, aggregates beside them:
```sql
SELECT first_name, dept_id, salary,
       round(avg(salary) OVER ())                     AS company_avg,
       round(avg(salary) OVER (PARTITION BY dept_id)) AS dept_avg,
       count(*)          OVER (PARTITION BY dept_id)  AS dept_size
FROM employees
WHERE dept_id IN (1, 2)
ORDER BY dept_id, salary DESC;
```
Output:
```
 first_name | dept_id |  salary   | company_avg | dept_avg | dept_size
------------+---------+-----------+-------------+----------+-----------
 Vikram     |       1 | 185000.00 |      110125 |   130750 |         4
 Neha       |       1 | 142000.00 |      110125 |   130750 |         4
 ...
 Daniel     |       2 | 150000.00 |      110125 |    89500 |         4
 Priya      |       2 |  72000.00 |      110125 |    89500 |         4
 ...
(8 rows)
```
`OVER ()` means all rows of the result (the 8 left after `WHERE`), so `company_avg` is constant; `OVER (PARTITION BY dept_id)` means "rows with my `dept_id`". Without windows each extra column needs a join to a grouped subquery or a correlated subquery.

### How it works
After `FROM`, `WHERE`, `GROUP BY`, `HAVING`, rows are split into **partitions** (`PARTITION BY`) and sorted within them (`ORDER BY`); per row the function reads its window (the partition, or a **frame** in it — 8.3/8.4) and adds one value as a new column — row count unchanged. Then `DISTINCT`, the final `ORDER BY` and `LIMIT` run.

Two families work with `OVER`:

| Family | Functions | Notes |
|---|---|---|
| **Aggregate functions** used as windows | `sum`, `avg`, `count`, `min`, `max`, `string_agg`, `array_agg`, `bool_and`, … | any normal aggregate becomes a window function when you add `OVER` |
| **Dedicated window functions** | `row_number`, `rank`, `dense_rank`, `percent_rank`, `cume_dist`, `ntile`, `lag`, `lead`, `first_value`, `last_value`, `nth_value` | only valid with `OVER`; the ranking ones take no arguments |

### Dialect notes
| | Window functions available since | Frame support |
|---|---|---|
| PostgreSQL | 8.4 (2009) | `ROWS`, `RANGE` with offsets, `GROUPS`, `EXCLUDE` (offsets/`GROUPS`/`EXCLUDE` since 11) |
| MySQL | 8.0 (2018) | `ROWS`, `RANGE`; no `GROUPS`, no `EXCLUDE` |
| SQLite | 3.25 (2018) | `ROWS`, `RANGE`; `GROUPS` and `EXCLUDE` since 3.28 |
| SQL Server | ranking 2005; aggregate `ORDER BY` + frames 2012 | `ROWS`; `RANGE` only with `UNBOUNDED`/`CURRENT ROW`; no `GROUPS`/`EXCLUDE` |
| Oracle | 8i (1999) | full, plus extensions |

---

## 8.2 PARTITION BY: per-group values next to each row

### What it is
`PARTITION BY expr` splits rows into independent groups like `GROUP BY`, without collapsing them; the function restarts in each partition.

### Typical uses
```sql
SELECT first_name, dept_id, salary,
       round(100.0 * salary / sum(salary) OVER (PARTITION BY dept_id), 1) AS pct_of_dept_payroll,
       salary - round(avg(salary) OVER (PARTITION BY dept_id))            AS vs_dept_avg
FROM employees
WHERE dept_id IN (1, 2)
ORDER BY dept_id, salary DESC;
```
Output:
```
 first_name | dept_id |  salary   | pct_of_dept_payroll | vs_dept_avg
------------+---------+-----------+---------------------+-------------
 Vikram     |       1 | 185000.00 |                35.4 |    54250.00
 Neha       |       1 | 142000.00 |                27.2 |    11250.00
 Rahul      |       1 |  98000.00 |                18.7 |   -32750.00
 Meera      |       1 |  98000.00 |                18.7 |   -32750.00
 ...
(8 rows)
```
"Percent of group total" and "difference from group average" are the two classic uses (`100.0 *` forces decimal division, [chapter 3](../03-functions-and-expressions/README.md)).

**Partitioning rules**
- Several expressions are allowed: `PARTITION BY store, date_trunc('month', day)`.
- `NULL`s form **one** partition (like `GROUP BY`):

```sql
SELECT first_name, dept_id, count(*) OVER (PARTITION BY dept_id) AS same_dept
FROM employees
WHERE dept_id IS NULL OR dept_id = 4
ORDER BY dept_id NULLS LAST;
```
Output:
```
 first_name | dept_id | same_dept
------------+---------+-----------
 Kiran      |       4 |         2
 Omar       |       4 |         2
 Asha       |  (null) |         2
 Rohan      |  (null) |         2
(4 rows)
```
- Window functions in one `SELECT` can use different partitions (8.1 mixed `OVER ()` and `OVER (PARTITION BY dept_id)`).

### Common mistakes
- **Expecting fewer rows** — a window never removes rows; one row per group is `GROUP BY` (or filter afterwards, 8.8).
- **Non-grouped columns in a grouped query's window** — windows see the *grouped* rows, so arguments must be grouped columns or aggregates (8.7: `sum(sum(x)) OVER ()`).

---

## 8.3 ORDER BY inside OVER: running calculations and the default frame

### What it is
`ORDER BY` inside `OVER` (1) sets the order for ranking and offset functions (`row_number`, `lag`, …) and (2) changes an **aggregate's** default window from the whole partition to "partition start **up to the current row**" — `sum` becomes a **running total**, `count` a running count, `max` a running maximum.

```sql
-- code/02_order_and_default_frame.sql
SELECT store, day, amount,
       sum(amount) OVER (PARTITION BY store ORDER BY day) AS running_total,
       count(*)    OVER (PARTITION BY store ORDER BY day) AS days_so_far
FROM daily_sales
WHERE store = 'Mumbai'
ORDER BY day;
```
Output:
```
 store  |    day     | amount  | running_total | days_so_far
--------+------------+---------+---------------+-------------
 Mumbai | 2025-03-01 | 1200.00 |       1200.00 |           1
 Mumbai | 2025-03-02 |  900.00 |       2100.00 |           2
 Mumbai | 2025-03-03 | 1500.00 |       3600.00 |           3
 ...
 Mumbai | 2025-03-07 | 1800.00 |       8700.00 |           7
(7 rows)
```

### The default frame and the tie trap
With `ORDER BY` and no frame written, the default is:

```sql
RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
```

`RANGE` works on *values*: "current row" means the current row **and all its peers** (same `ORDER BY` value), so tied rows are summed together and share one "running" total:

```sql
SELECT first_name, salary,
       sum(salary) OVER (ORDER BY salary)                    AS default_range,
       sum(salary) OVER (ORDER BY salary
                         ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS rows_frame,
       sum(salary) OVER (ORDER BY salary, emp_id)           AS unique_order
FROM employees
WHERE dept_id = 2
ORDER BY salary, emp_id;
```
Output:
```
 first_name |  salary   | default_range | rows_frame | unique_order
------------+-----------+---------------+------------+--------------
 Arjun      |  68000.00 |     136000.00 |   68000.00 |     68000.00
 Sara       |  68000.00 |     136000.00 |  136000.00 |    136000.00
 Priya      |  72000.00 |     208000.00 |  208000.00 |    208000.00
 Daniel     | 150000.00 |     358000.00 |  358000.00 |    358000.00
(4 rows)
```
- `default_range`: peers Arjun and Sara both see 68 000 + 68 000.
- `rows_frame`: physical rows, one at a time — but which tied row is "first" is **undefined**; Sara could get 68 000 next time.
- `unique_order`: a tie-breaker (`emp_id`) removes peers, so every frame type gives the same **deterministic** (identical every run) result — the best fix.

| `OVER (...)` contains | Default window for aggregates |
|---|---|
| nothing / only `PARTITION BY` | the whole partition |
| `ORDER BY` | `RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW` (start → current row + its peers) |

### Common mistakes
- **Running totals that "jump"** on duplicates (the peer effect) — order by a unique key, or write `ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW`.
- **Confusing the window's `ORDER BY` with the query's** — the one in `OVER` controls only the calculation; output order needs the final `ORDER BY`.

---

## 8.4 Frames: ROWS, RANGE, GROUPS and EXCLUDE

### What it is
The **frame** is the part of the partition that an aggregate (and `first_value`/`last_value`/`nth_value`) sees for the current row; it slides as the row moves. Ranking functions and `lag`/`lead` ignore it.

```sql
{ ROWS | RANGE | GROUPS } BETWEEN frame_start AND frame_end [EXCLUDE ...]

frame_start / frame_end:
    UNBOUNDED PRECEDING      -- first row of the partition
    n PRECEDING              -- n rows / n units of value / n peer groups before
    CURRENT ROW
    n FOLLOWING
    UNBOUNDED FOLLOWING      -- last row of the partition
```
Short form: `ROWS 2 PRECEDING` means `ROWS BETWEEN 2 PRECEDING AND CURRENT ROW`.

| Mode | `n PRECEDING` means | Peers (ties) | Needs |
|---|---|---|---|
| `ROWS` | *n physical rows* before | treated as separate rows | any `ORDER BY` |
| `RANGE` | rows whose **value** is within *n* of the current value (`n` can be a number or an `interval`) | always included together | exactly one `ORDER BY` column of a numeric/date/time type when an offset is used |
| `GROUPS` (PostgreSQL 11+) | *n peer groups* before (a group = rows with equal `ORDER BY` values) | included together | an `ORDER BY` |

### Frame diagrams
`string_agg(...) OVER (frame)` lists the days in each frame — a trick to *see* a frame (the `frame_days` column).

**`ROWS BETWEEN 2 PRECEDING AND CURRENT ROW`** — a sliding window of up to 3 rows:
```sql
-- code/03_frames.sql
SELECT day, amount,
       string_agg(to_char(day, 'DD'), ',') OVER w AS frame_days,
       sum(amount) OVER w                          AS frame_sum
FROM daily_sales
WHERE store = 'Mumbai'
WINDOW w AS (ORDER BY day ROWS BETWEEN 2 PRECEDING AND CURRENT ROW)
ORDER BY day;
```
Output:
```
    day     | amount  | frame_days | frame_sum
------------+---------+------------+-----------
 2025-03-01 | 1200.00 | 01         |   1200.00
 2025-03-02 |  900.00 | 01,02      |   2100.00
 2025-03-03 | 1500.00 | 01,02,03   |   3600.00
 2025-03-04 | 1500.00 | 02,03,04   |   3900.00
 2025-03-05 |  700.00 | 03,04,05   |   3700.00
 2025-03-06 | 1100.00 | 04,05,06   |   3300.00
 2025-03-07 | 1800.00 | 05,06,07   |   3600.00
(7 rows)
```
The first two frames are *shorter* — no earlier rows. (`WINDOW w AS (...)` names a reusable window, 8.10.)

**Centered: `ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING`** — smoothing that looks both ways:
```sql
SELECT day, amount,
       string_agg(to_char(day, 'DD'), ',') OVER w AS frame_days,
       round(avg(amount) OVER w, 1)                AS centered_avg
FROM daily_sales
WHERE store = 'Mumbai'
WINDOW w AS (ORDER BY day ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING)
ORDER BY day;
```
Output:
```
    day     | amount  | frame_days | centered_avg
------------+---------+------------+--------------
 2025-03-01 | 1200.00 | 01,02      |       1050.0
 2025-03-02 |  900.00 | 01,02,03   |       1200.0
 ...
 2025-03-07 | 1800.00 | 06,07      |       1450.0
(7 rows)
```

**`ROWS` vs `RANGE` when a day is missing.** Pune has no 4 March, so "previous 2 rows" and "previous 2 days" differ:
```sql
SELECT day, amount,
       string_agg(to_char(day, 'DD'), ',') OVER (ORDER BY day
            ROWS BETWEEN 2 PRECEDING AND CURRENT ROW)                   AS rows_2,
       string_agg(to_char(day, 'DD'), ',') OVER (ORDER BY day
            RANGE BETWEEN INTERVAL '2 days' PRECEDING AND CURRENT ROW)  AS range_2_days
FROM daily_sales
WHERE store = 'Pune'
ORDER BY day;
```
Output:
```
    day     | amount |  rows_2  | range_2_days
------------+--------+----------+--------------
 2025-03-01 | 400.00 | 01       | 01
 2025-03-02 | 650.00 | 01,02    | 01,02
 2025-03-03 | 500.00 | 01,02,03 | 01,02,03
 2025-03-05 | 800.00 | 02,03,05 | 03,05
 2025-03-06 | 650.00 | 03,05,06 | 05,06
 2025-03-07 | 300.00 | 05,06,07 | 05,06,07
(6 rows)
```
`ROWS 2 PRECEDING` takes three rows whatever their dates; `RANGE INTERVAL '2 days' PRECEDING` takes dates from current − 2 days to current. For "average of the last 3 *calendar* days" `RANGE` is correct and `ROWS` silently reaches back to 2 March (or fill missing days with `generate_series` first — [chapter 7](../07-ctes-views-and-recursion/README.md#76-generate_series-making-rows-postgresql)).

**`ROWS` vs `RANGE` vs `GROUPS` with ties.** Ordering Pune by `amount` (two days have 650):
```sql
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
```
Output:
```
    day     | amount | rows_1  |  range_150  |  groups_1
------------+--------+---------+-------------+-------------
 2025-03-07 | 300.00 | 300     | 300         | 300
 2025-03-01 | 400.00 | 300,400 | 300,400     | 300,400
 2025-03-03 | 500.00 | 400,500 | 400,500     | 400,500
 2025-03-02 | 650.00 | 500,650 | 500,650,650 | 500,650,650
 2025-03-06 | 650.00 | 650,650 | 500,650,650 | 500,650,650
 2025-03-05 | 800.00 | 650,800 | 650,650,800 | 650,650,800
(6 rows)
```
- `ROWS 1 PRECEDING`: the previous physical row — the two 650 rows get *different* frames.
- `RANGE 150 PRECEDING`: amounts from current − 150 to current, **all peers included** (for 650: 500, 650, 650).
- `GROUPS 1 PRECEDING`: the current peer group plus the one before (650: {500}, {650, 650}; 800: {650, 650}, {800}).

**`EXCLUDE`** (PostgreSQL 11+) removes rows from the frame: `EXCLUDE CURRENT ROW`, `EXCLUDE GROUP` (current row and peers), `EXCLUDE TIES` (peers, not the current row), `EXCLUDE NO OTHERS` (default). Average of all *other* days:
```sql
SELECT day, amount,
       round(avg(amount) OVER (ORDER BY day ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
                               EXCLUDE CURRENT ROW), 1) AS avg_of_other_days
FROM daily_sales
WHERE store = 'Mumbai'
ORDER BY day;
```
Output:
```
    day     | amount  | avg_of_other_days
------------+---------+-------------------
 2025-03-01 | 1200.00 |            1250.0
 2025-03-02 |  900.00 |            1300.0
 ...
 2025-03-07 | 1800.00 |            1150.0
(7 rows)
```
(Week total 8700; for 1 March: (8700 − 1200) / 6 = 1250.)

### Choosing a frame
| You want | Frame |
|---|---|
| Running total (deterministic) | `ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW` + unique `ORDER BY` |
| Last *N* rows (e.g. last 7 records) | `ROWS BETWEEN N-1 PRECEDING AND CURRENT ROW` |
| Last *N* days of time, with gaps in data | `RANGE BETWEEN INTERVAL 'N-1 days' PRECEDING AND CURRENT ROW` |
| Whole partition (e.g. for `last_value`) | `ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING` (or no `ORDER BY`) |
| Current value and the next distinct value(s) | `GROUPS BETWEEN CURRENT ROW AND 1 FOLLOWING` |

### Common mistakes
- **`ROWS 6 PRECEDING` for "last 7 days" with missing dates** — use `RANGE` with an interval.
- **Expecting `row_number`, `rank`, `lag`, `lead`, `ntile` to obey the frame** — they ignore it.
- **Frame start after frame end** (`BETWEEN CURRENT ROW AND 1 PRECEDING`) — an error.
- **Porting `GROUPS`/`EXCLUDE`/`RANGE n PRECEDING` to SQL Server or MySQL** — unsupported (8.1 dialect table).

### In the interview
**30-second answer:** "The frame says which rows around the current one an aggregate sees. `ROWS` counts physical rows, `RANGE` uses the ordering value — so ties are always included and you can say 'within 7 days' — and `GROUPS` counts groups of tied rows. The default with `ORDER BY` is `RANGE UNBOUNDED PRECEDING TO CURRENT ROW`, which is why running totals can repeat on ties."

- *Moving 7-day average with missing days?* → `avg(x) OVER (ORDER BY day RANGE BETWEEN INTERVAL '6 days' PRECEDING AND CURRENT ROW)`, or fill the calendar and use `ROWS 6 PRECEDING`.

### Self-check
1. *Pune, current row 5 March: days in `ROWS 2 PRECEDING`? In `RANGE INTERVAL '2 days' PRECEDING`?* → 2, 3, 5 March; 3, 5 March.
2. *Which frame mode treats tied rows as separate?* → `ROWS`.

---

## 8.5 Ranking functions

### What it is
Ranking functions number rows by position in the window's `ORDER BY`; they differ in **how they treat ties**.

| Function | Returns | Ties |
|---|---|---|
| `row_number()` | 1, 2, 3, … unique per row | tied rows get different numbers — **in no guaranteed order** |
| `rank()` | position with gaps ("Olympic" ranking) | same rank; next rank skips (1, 2, 2, 4) |
| `dense_rank()` | position without gaps | same rank; next rank is +1 (1, 2, 2, 3) |
| `percent_rank()` | (rank − 1) / (rows in partition − 1), from 0 to 1 | same value |
| `cume_dist()` | (rows ordered before the current row + its peers) / rows in partition, from >0 to 1 | same value |
| `ntile(n)` | bucket number 1…n, buckets as equal as possible | ties can be split across buckets |

```sql
-- code/04_ranking.sql
SELECT first_name, salary,
       row_number()  OVER w AS row_number,
       rank()        OVER w AS rank,
       dense_rank()  OVER w AS dense_rank,
       round(percent_rank() OVER w :: numeric, 2) AS pct_rank,
       round(cume_dist()    OVER w :: numeric, 2) AS cume_dist,
       ntile(3)      OVER w AS ntile_3
FROM employees
WHERE dept_id IN (1, 2)
WINDOW w AS (ORDER BY salary DESC)
ORDER BY salary DESC, row_number;
```
Output:
```
 first_name |  salary   | row_number | rank | dense_rank | pct_rank | cume_dist | ntile_3
------------+-----------+------------+------+------------+----------+-----------+---------
 Vikram     | 185000.00 |          1 |    1 |          1 |     0.00 |      0.13 |       1
 Daniel     | 150000.00 |          2 |    2 |          2 |     0.14 |      0.25 |       1
 Neha       | 142000.00 |          3 |    3 |          3 |     0.29 |      0.38 |       1
 Rahul      |  98000.00 |          4 |    4 |          4 |     0.43 |      0.63 |       2
 Meera      |  98000.00 |          5 |    4 |          4 |     0.43 |      0.63 |       2
 Priya      |  72000.00 |          6 |    6 |          5 |     0.71 |      0.75 |       2
 Arjun      |  68000.00 |          7 |    7 |          6 |     0.86 |      1.00 |       3
 Sara       |  68000.00 |          8 |    7 |          6 |     0.86 |      1.00 |       3
(8 rows)
```
- Rahul and Meera tie at 98 000: `rank` 4, 4, then Priya **6** (5 is "used up"); `dense_rank` 4, 4, then **5**.
- `row_number` gave Rahul 4, Meera 5 — arbitrary; it could flip. Always add a tie-breaker (`ORDER BY salary DESC, emp_id`).
- `percent_rank` for Priya: (6 − 1) / (8 − 1) = 0.71. `cume_dist` for Rahul/Meera: 5 of 8 rows are ≥ 98 000 in this order → 0.63.
- `ntile(3)` over 8 rows: 8 / 3 = 2 remainder 2, so buckets of 3, 3, 2.

**Ranking within groups** — add `PARTITION BY`:
```sql
SELECT dept_id, first_name, salary,
       dense_rank() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rank_in_dept
FROM employees
WHERE dept_id IS NOT NULL
ORDER BY dept_id, rank_in_dept, first_name;
```
Output:
```
 dept_id | first_name |  salary   | rank_in_dept
---------+------------+-----------+--------------
       1 | Vikram     | 185000.00 |            1
       1 | Neha       | 142000.00 |            2
       1 | Meera      |  98000.00 |            3
       1 | Rahul      |  98000.00 |            3
 ...
       5 | Li         |  64000.00 |            1
(13 rows)
```

**`ntile` for quartiles** (four equal-sized quarters of the sorted values) — window in a subquery, `GROUP BY` outside:
```sql
SELECT quartile, count(*) AS people, min(salary), max(salary)
FROM (SELECT salary, ntile(4) OVER (ORDER BY salary) AS quartile FROM employees) t
GROUP BY quartile
ORDER BY quartile;
```
Output:
```
 quartile | people |    min    |    max
----------+--------+-----------+-----------
        1 |      4 |  30000.00 |  68000.00
        2 |      4 |  68000.00 |  98000.00
        3 |      4 |  98000.00 | 142000.00
        4 |      3 | 150000.00 | 250000.00
(4 rows)
```
68 000 and 98 000 each land in two buckets: `ntile` fills by row count and **splits ties**. To keep ties together use `percent_rank`/`cume_dist`, or `percentile_cont` (an ordered-set aggregate, reading input sorted, like a median) for exact quartile boundaries ([chapter 4](../04-aggregation/README.md)).

### Which ranking function? (the question interviewers love)
| Question | Use |
|---|---|
| "Exactly one row per …" / pagination (splitting results into pages) / dedup | `row_number()` with a unique tie-breaker |
| "Top 3 places, ties share a place, next place skips" (sports) | `rank()` |
| "The 2nd highest *distinct* salary" / "top 3 salary levels" | `dense_rank()` |
| "Top 10 %" | `percent_rank() <= 0.1` or `cume_dist() <= 0.1` (depending on how ties should count) |
| "Split into 4 equal groups" | `ntile(4)` |

**Nth highest value** — the classic: `dense_rank` makes ties count once.
```sql
-- 2nd highest distinct salary (NULL if there isn't one)
SELECT max(salary) FROM (
    SELECT salary, dense_rank() OVER (ORDER BY salary DESC) AS dr FROM employees
) t WHERE dr = 2;
```
Output:
```
    max
-----------
 185000.00
(1 row)
```
Asha earns 250 000, so Vikram's 185 000 is second. `max()` returns one row even if several share that salary, and `NULL` rather than no rows when there is no 2nd value.

### Self-check
1. *Salaries 100, 90, 90, 80: rank of 80? dense_rank?* → 4; 3.
2. *`ntile(4)` over 10 rows — bucket sizes?* → 3, 3, 2, 2.

---

## 8.6 Value (offset) functions: LAG, LEAD, FIRST_VALUE, LAST_VALUE, NTH_VALUE

### What it is
Value functions return a value **from another row** of the window.

| Function | Returns | Uses the frame? |
|---|---|---|
| `lag(expr [, offset [, default]])` | `expr` from `offset` rows **before** (default 1) | no — relative to the partition's order |
| `lead(expr [, offset [, default]])` | `expr` from `offset` rows **after** | no |
| `first_value(expr)` | `expr` from the frame's first row | yes |
| `last_value(expr)` | `expr` from the frame's last row | yes |
| `nth_value(expr, n)` | `expr` from the frame's *n*-th row (1-based), NULL if the frame is shorter | yes |

When there is no such row (the first row has no "previous"), `lag`/`lead` return `NULL`, or the `default` you pass.

**Day-over-day change:**
```sql
-- code/05_value_functions.sql
SELECT store, day, amount,
       lag(amount)  OVER w          AS prev_day,
       amount - lag(amount) OVER w  AS change,
       lead(amount) OVER w          AS next_day,
       lag(amount, 2, 0) OVER w     AS two_days_ago_or_0
FROM daily_sales
WHERE day <= '2025-03-04'
WINDOW w AS (PARTITION BY store ORDER BY day)
ORDER BY store, day;
```
Output:
```
 store  |    day     | amount  | prev_day | change  | next_day | two_days_ago_or_0
--------+------------+---------+----------+---------+----------+-------------------
 Mumbai | 2025-03-01 | 1200.00 |   (null) |  (null) |   900.00 |                 0
 Mumbai | 2025-03-02 |  900.00 |  1200.00 | -300.00 |  1500.00 |                 0
 ...
 Mumbai | 2025-03-04 | 1500.00 |  1500.00 |    0.00 |   (null) |            900.00
 Pune   | 2025-03-01 |  400.00 |   (null) |  (null) |   650.00 |                 0
 Pune   | 2025-03-02 |  650.00 |   400.00 |  250.00 |   500.00 |                 0
 Pune   | 2025-03-03 |  500.00 |   650.00 | -150.00 |   (null) |            400.00
(7 rows)
```
`PARTITION BY store` stops `lag` crossing into the other store (Pune's first day has no previous). `WHERE` runs first, so `lead` on 4 March sees nothing (5 March was filtered). `lag` means previous **row**, not previous **day** — in Pune the row before 5 March is 3 March; fill gaps or check the date difference (8.9).

**The `last_value` trap.** With `ORDER BY`, the default frame ends at the current row, so `last_value` returns the current row:
```sql
SELECT day, amount,
       first_value(amount) OVER (ORDER BY day)    AS first_amt,
       last_value(amount)  OVER (ORDER BY day)    AS last_default_frame,
       last_value(amount)  OVER (ORDER BY day
            ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_whole_partition,
       nth_value(amount, 2) OVER (ORDER BY day
            ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS second_amt
FROM daily_sales
WHERE store = 'Pune'
ORDER BY day;
```
Output:
```
    day     | amount | first_amt | last_default_frame | last_whole_partition | second_amt
------------+--------+-----------+--------------------+----------------------+------------
 2025-03-01 | 400.00 |    400.00 |             400.00 |               300.00 |     650.00
 2025-03-02 | 650.00 |    400.00 |             650.00 |               300.00 |     650.00
 2025-03-03 | 500.00 |    400.00 |             500.00 |               300.00 |     650.00
 ...
 2025-03-07 | 300.00 |    400.00 |             300.00 |               300.00 |     650.00
(6 rows)
```
Fix: extend the frame to `UNBOUNDED FOLLOWING`, or reverse the order with `first_value(amount) OVER (ORDER BY day DESC)` (no frame needed). `first_value(first_name) OVER (PARTITION BY dept_id ORDER BY salary DESC)` puts each department's top earner beside every employee ("gap to the leader").

**Time between events** — days between a customer's orders (a retention metric: how often customers come back):
```sql
SELECT customer_id, order_id, placed_at::date AS placed,
       placed_at::date - lag(placed_at::date) OVER (PARTITION BY customer_id ORDER BY placed_at) AS days_since_prev
FROM orders
WHERE customer_id IN (1, 2, 5)
ORDER BY customer_id, placed_at;
```
Output:
```
 customer_id | order_id |   placed   | days_since_prev
-------------+----------+------------+-----------------
           1 |      101 | 2025-01-03 |          (null)
           1 |      104 | 2025-02-11 |              39
           1 |      113 | 2025-06-02 |             111
           2 |      102 | 2025-01-17 |          (null)
           2 |      108 | 2025-04-01 |              74
 ...
(7 rows)
```
Subtracting two `date`s gives an integer number of days in PostgreSQL.

### Dialect notes
- `IGNORE NULLS` (`lag(x) IGNORE NULLS OVER (...)`, "last non-null value") is standard and supported by Oracle, SQL Server 2022, BigQuery and Snowflake (cloud data warehouses). PostgreSQL gains it in **version 19** (in beta as of September 2026); PostgreSQL 14–18 and MySQL lack it — emulate with a running `count(x)` as group key plus `first_value` per group, or a correlated subquery.
- `nth_value(...) FROM LAST` (counting from the end of the frame) exists in Oracle and a few warehouses, not in PostgreSQL, MySQL or SQL Server.

### Common mistakes
- `last_value` without a full frame; `lag` without `PARTITION BY` (crosses groups); `lag` treated as "previous period" when periods can be missing.
- The first row's `amount - lag(amount)` is `NULL`; pass a `default` or use `coalesce` if you want 0.

### In the interview
**30-second answer:** "`LAG` and `LEAD` read a column from a previous or next row in the window's order, per partition, which replaces a self-join on 'row n−1'. `FIRST_VALUE`, `LAST_VALUE` and `NTH_VALUE` read from positions in the frame — and because the default frame ends at the current row, `LAST_VALUE` needs `ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING`."

- *Year-over-year growth per product?* → `lag(revenue) OVER (PARTITION BY product ORDER BY year)`, then `(revenue - prev) / prev`.
- *How did people do this before window functions?* → A self-join on `t2.day = t1.day - 1` (breaks with gaps) or a correlated subquery `(SELECT ... WHERE t2.day < t1.day ORDER BY day DESC LIMIT 1)`.

### Self-check
1. *`lag(x, 1, 0)` on the first row?* → 0.
2. *Which ignore the frame: `lag`, `first_value`, `lead`, `nth_value`?* → `lag` and `lead`.

---

## 8.7 Running totals, moving averages and growth

### What it is
Everyday analytics = aggregate windows with the right frame:

| Metric | Window |
|---|---|
| Running total | `sum(x) OVER (PARTITION BY g ORDER BY t ROWS UNBOUNDED PRECEDING)` |
| Moving average of last 3 rows | `avg(x) OVER (PARTITION BY g ORDER BY t ROWS BETWEEN 2 PRECEDING AND CURRENT ROW)` |
| Cumulative share | running total / `sum(x) OVER (PARTITION BY g)` |
| Share of total | `x / sum(x) OVER (PARTITION BY g)` |
| Growth vs previous period | `x / lag(x) OVER (ORDER BY t) - 1` |

```sql
-- code/06_running_and_moving.sql
SELECT store, day, amount,
       sum(amount) OVER (PARTITION BY store ORDER BY day
                         ROWS UNBOUNDED PRECEDING)                         AS running_total,
       round(avg(amount) OVER (PARTITION BY store ORDER BY day
                         ROWS BETWEEN 2 PRECEDING AND CURRENT ROW), 1)     AS moving_avg_3,
       count(*) OVER (PARTITION BY store ORDER BY day
                         ROWS BETWEEN 2 PRECEDING AND CURRENT ROW)         AS rows_in_avg,
       round(100.0 * sum(amount) OVER (PARTITION BY store ORDER BY day ROWS UNBOUNDED PRECEDING)
                   / sum(amount) OVER (PARTITION BY store), 1)             AS pct_of_week_so_far
FROM daily_sales
ORDER BY store, day;
```
Output:
```
 store  |    day     | amount  | running_total | moving_avg_3 | rows_in_avg | pct_of_week_so_far
--------+------------+---------+---------------+--------------+-------------+--------------------
 Mumbai | 2025-03-01 | 1200.00 |       1200.00 |       1200.0 |           1 |               13.8
 Mumbai | 2025-03-02 |  900.00 |       2100.00 |       1050.0 |           2 |               24.1
 Mumbai | 2025-03-03 | 1500.00 |       3600.00 |       1200.0 |           3 |               41.4
 ...
 Mumbai | 2025-03-07 | 1800.00 |       8700.00 |       1200.0 |           3 |              100.0
 ...
 Pune   | 2025-03-03 |  500.00 |       1550.00 |        516.7 |           3 |               47.0
 Pune   | 2025-03-05 |  800.00 |       2350.00 |        650.0 |           3 |               71.2
 ...
 Pune   | 2025-03-07 |  300.00 |       3300.00 |        583.3 |           3 |              100.0
(13 rows)
```
The first two moving averages use only 1 and 2 rows (`rows_in_avg`) — a "warm-up" that can mislead on a chart; hide it with `CASE WHEN count(*) OVER w = 3 THEN avg(amount) OVER w END` (in the code file). Pune's 5 March average uses 2, 3, 5 March because the frame is `ROWS` (8.4 for `RANGE`).

### Windows on top of GROUP BY
Windows run **after** `GROUP BY`, so they work on grouped rows — monthly revenue plus its running total in one query: the inner `sum` is per month, the outer `sum(...) OVER` adds those up:
```sql
SELECT date_trunc('month', o.placed_at)::date AS month,
       sum(oi.quantity * oi.unit_price * (1 - oi.discount))::numeric(10,2) AS revenue,
       sum(sum(oi.quantity * oi.unit_price * (1 - oi.discount)))
           OVER (ORDER BY date_trunc('month', o.placed_at)::date)::numeric(10,2) AS running_revenue
FROM orders o JOIN order_items oi USING (order_id)
WHERE o.status <> 'cancelled'
GROUP BY 1
ORDER BY 1;
```
Output:
```
   month    | revenue | running_revenue
------------+---------+-----------------
 2025-01-01 | 1871.90 |         1871.90
 2025-02-01 |  811.49 |         2683.39
 ...
 2025-06-01 | 1516.60 |         7403.62
(6 rows)
```
`sum(sum(...))` looks odd but is correct; often clearer is aggregating in a CTE (named subquery written with `WITH`, [chapter 7](../07-ctes-views-and-recursion/README.md)) and windowing outside — here **month-over-month growth**:
```sql
WITH monthly AS (
    SELECT date_trunc('month', o.placed_at)::date AS month,
           sum(oi.quantity * oi.unit_price * (1 - oi.discount)) AS revenue
    FROM orders o JOIN order_items oi USING (order_id)
    WHERE o.status <> 'cancelled'
    GROUP BY 1
)
SELECT month, round(revenue, 2) AS revenue,
       round(lag(revenue) OVER (ORDER BY month), 2) AS prev_month,
       round(100.0 * (revenue / lag(revenue) OVER (ORDER BY month) - 1), 1) AS mom_growth_pct
FROM monthly
ORDER BY month;
```
Output:
```
   month    | revenue | prev_month | mom_growth_pct
------------+---------+------------+----------------
 2025-01-01 | 1871.90 |     (null) |         (null)
 2025-02-01 |  811.49 |    1871.90 |          -56.6
 2025-03-01 |  804.95 |     811.49 |           -0.8
 ...
 2025-06-01 | 1516.60 |     249.93 |          506.8
(6 rows)
```
If a month can have *no* orders, generate the months first ([chapter 7](../07-ctes-views-and-recursion/README.md#76-generate_series-making-rows-postgresql)) or `lag` compares with two months back. Guard with `NULLIF(prev, 0)` if the previous value can be zero.

### Real-world examples
Account balance after each transaction (running `sum` ordered by time and id), 7-day moving average of daily active users, stock level as a running sum of `+received −shipped`.

---

## 8.8 Top-N per group and de-duplication

### The problem
**Top-N-per-group** ("top 2 earners per department", "latest order per customer", "best seller per category") is the most common window interview problem. `LIMIT` limits the whole result, not each group; `GROUP BY` returns only the aggregate, not the rest of the row.

### Why you need a subquery or CTE
Windows are computed *after* `WHERE`, so you can't filter on them at the same level:
```sql
-- code/07_top_n_per_group.sql
SELECT first_name, salary FROM employees
WHERE row_number() OVER (ORDER BY salary DESC) <= 3;
```
Output:
```
ERROR:  window functions are not allowed in WHERE
LINE 2: WHERE row_number() OVER (ORDER BY salary DESC) <= 3;
              ^
```
(Nor in `GROUP BY` or `HAVING`.) The pattern: **rank in a CTE or subquery, filter in the outer query.**

### How it works
In a CTE add `row_number()` / `rank()` / `dense_rank()` `OVER (PARTITION BY group ORDER BY measure DESC)`; outside, `WHERE rank_col <= N`; choose the function by how ties should behave.

```sql
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
```
Output:
```
    dept     | first_name |  salary   | rn | rnk | drnk
-------------+------------+-----------+----+-----+------
 Engineering | Vikram     | 185000.00 |  1 |   1 |    1
 ...
 Engineering | Rahul      |  98000.00 |  3 |   3 |    3
 Engineering | Meera      |  98000.00 |  4 |   3 |    3
 Sales       | Daniel     | 150000.00 |  1 |   1 |    1
 Sales       | Priya      |  72000.00 |  2 |   2 |    2
 Sales       | Arjun      |  68000.00 |  3 |   3 |    3
 Sales       | Sara       |  68000.00 |  4 |   3 |    3
(8 rows)
```
With ties the three filters differ — Sales:
```sql
-- ranked = Sales only; then:
SELECT 'row_number <= 3' AS rule, string_agg(first_name, ', ' ORDER BY rn) AS kept FROM ranked WHERE rn <= 3
UNION ALL
SELECT 'rank <= 3',       string_agg(first_name, ', ' ORDER BY rn) FROM ranked WHERE rnk <= 3
UNION ALL
SELECT 'dense_rank <= 2', string_agg(first_name, ', ' ORDER BY rn) FROM ranked WHERE drnk <= 2;
```
Output:
```
      rule       |            kept
-----------------+----------------------------
 row_number <= 3 | Daniel, Priya, Arjun
 rank <= 3       | Daniel, Priya, Arjun, Sara
 dense_rank <= 2 | Daniel, Priya
(3 rows)
```
| Filter | Meaning | Rows per group |
|---|---|---|
| `row_number() <= N` | exactly N rows; ties broken by your tie-breaker | exactly N (or fewer) |
| `rank() <= N` | everyone whose position is within the top N; ties included | can exceed N |
| `dense_rank() <= N` | everyone having one of the top N *distinct* values | can far exceed N |

Ask in an interview: "If two people tie for 3rd, should both be returned?"

**Latest row per key (de-duplication)** — `N = 1`, newest first:
```sql
SELECT customer_id, order_id, placed_at, status
FROM (
    SELECT o.*, row_number() OVER (PARTITION BY customer_id ORDER BY placed_at DESC) AS rn
    FROM orders o
) t
WHERE rn = 1
ORDER BY customer_id;
```
Output:
```
 customer_id | order_id |      placed_at      |  status
-------------+----------+---------------------+-----------
           1 |      113 | 2025-06-02 10:30:00 | pending
           2 |      108 | 2025-04-01 12:00:00 | delivered
           3 |      111 | 2025-05-06 07:55:00 | cancelled
 ...
           8 |      109 | 2025-04-09 16:45:00 | delivered
(8 rows)
```
**Duplicates to delete** — number rows within each duplicate group; `rn > 1` are extra copies. The duplicate Anita Shah (messy email) after normalising:
```sql
WITH dups AS (
    SELECT customer_id,
           row_number() OVER (PARTITION BY lower(trim(email)) ORDER BY customer_id) AS rn
    FROM customers
    WHERE email IS NOT NULL
)
SELECT customer_id AS would_delete FROM dups WHERE rn > 1;
```
Output:
```
 would_delete
--------------
            8
(1 row)
```
Delete with `DELETE FROM customers WHERE customer_id IN (SELECT customer_id FROM dups WHERE rn > 1)` after re-pointing that customer's orders ([chapter 9](../09-ddl-dml-and-constraints/README.md): `DELETE` and foreign keys — columns that must match a key in another table).

### Alternatives
| Approach | Top-1 | Top-N | Notes |
|---|---|---|---|
| Window + filter (above) | yes | yes | portable, flexible about ties |
| `DISTINCT ON (group) ... ORDER BY group, measure DESC` | yes | no | PostgreSQL only, concise |
| `LATERAL (... ORDER BY ... LIMIT N)` per group ([ch. 6](../06-subqueries-and-set-operations/README.md#67-lateral-subqueries)) | yes | yes | fastest when an index (sorted lookup structure) on (group, measure) exists and groups are few |
| Correlated subquery `WHERE salary = (SELECT max(...) ...)` | yes (with ties) | awkward | classic pre-window approach |
| `QUALIFY rn <= N` | yes | yes | filter on windows without a subquery — Snowflake, BigQuery, DuckDB, Teradata; **not** PostgreSQL / MySQL / SQL Server |

Top-1 shortcut: `SELECT DISTINCT ON (dept_id) dept_id, first_name, salary FROM employees WHERE dept_id IS NOT NULL ORDER BY dept_id, salary DESC, emp_id`.

### In the interview
**30-second answer:** "Rank rows inside each group with a window function in a CTE — `PARTITION BY` the group, `ORDER BY` the measure descending — then filter `rank <= N` in the outer query, because window functions can't appear in `WHERE`. I'd choose `row_number`, `rank` or `dense_rank` depending on whether ties should be included, and add a tie-breaker for determinism."

- *Top 3 salaries per department with ties?* → `dense_rank() <= 3` (three distinct salary levels) or `rank() <= 3` (three places).
- *How to make it fast on a big table?* → Index `(dept_id, salary DESC)`; PostgreSQL can then read each partition pre-sorted; for few groups a `LATERAL ... LIMIT N` uses the index directly.

### Self-check
1. *Keep only the newest row per `user_id`?* → `row_number() OVER (PARTITION BY user_id ORDER BY created_at DESC, id DESC) = 1` in a subquery.

---

## 8.9 Gaps and islands

### What it is
An **island** is a run of consecutive values (login days, free seat numbers, readings above a threshold); a **gap** is a missing stretch between islands. Longest streak, missing invoice numbers and sessionizing events are all gaps-and-islands problems.

### How it works — the row-number trick
In a run of consecutive dates both the date and `row_number()` rise by 1 per row, so their **difference is constant within a run** and changes at a skipped day — it labels the island. Compute `rn = row_number() OVER (PARTITION BY user_id ORDER BY login_date)`, `grp = login_date - rn` (date minus integer is a date), then `GROUP BY user_id, grp` with `min`/`max`/`count`.

```sql
-- code/08_gaps_and_islands.sql
SELECT user_id, login_date,
       row_number() OVER (PARTITION BY user_id ORDER BY login_date) AS rn,
       login_date - row_number() OVER (PARTITION BY user_id ORDER BY login_date)::int AS grp
FROM logins
WHERE user_id = 1
ORDER BY login_date;
```
Output:
```
 user_id | login_date | rn |    grp
---------+------------+----+------------
       1 | 2025-05-01 |  1 | 2025-04-30
       1 | 2025-05-02 |  2 | 2025-04-30
       1 | 2025-05-03 |  3 | 2025-04-30
       1 | 2025-05-06 |  4 | 2025-05-02
       1 | 2025-05-07 |  5 | 2025-05-02
       1 | 2025-05-10 |  6 | 2025-05-04
(6 rows)
```
`grp` has no meaning beyond being a shared label. (`::int` because `row_number()` is `bigint` and PostgreSQL defines `date - integer`, not `date - bigint`.)

**Each island (streak):**
```sql
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
```
Output:
```
 user_id | streak_start | streak_end | days
---------+--------------+------------+------
       1 | 2025-05-01   | 2025-05-03 |    3
       1 | 2025-05-06   | 2025-05-07 |    2
       1 | 2025-05-10   | 2025-05-10 |    1
       2 | 2025-05-02   | 2025-05-05 |    4
       2 | 2025-05-09   | 2025-05-10 |    2
(5 rows)
```
**Longest streak per user** — one more step (`DISTINCT ON` keeps the top row per user):
```sql
WITH marked AS (...same as above...),
streaks AS (
    SELECT user_id, grp, count(*) AS days, min(login_date) AS from_day
    FROM marked GROUP BY user_id, grp
)
SELECT DISTINCT ON (user_id) user_id, days AS longest_streak, from_day
FROM streaks
ORDER BY user_id, days DESC, from_day;
```
Output:
```
 user_id | longest_streak |  from_day
---------+----------------+------------
       1 |              3 | 2025-05-01
       2 |              4 | 2025-05-02
(2 rows)
```
Duplicate dates per user break the trick — de-duplicate first or use `dense_rank`.

**Gaps with `lead`** — a difference above 1 to the next row is a gap:
```sql
SELECT user_id,
       login_date + 1              AS gap_start,
       next_login - 1              AS gap_end,
       next_login - login_date - 1 AS missing_days
FROM (
    SELECT user_id, login_date,
           lead(login_date) OVER (PARTITION BY user_id ORDER BY login_date) AS next_login
    FROM logins
) t
WHERE next_login - login_date > 1
ORDER BY user_id, gap_start;
```
Output:
```
 user_id | gap_start  |  gap_end   | missing_days
---------+------------+------------+--------------
       1 | 2025-05-04 | 2025-05-05 |            2
       1 | 2025-05-08 | 2025-05-09 |            2
       2 | 2025-05-06 | 2025-05-08 |            3
(3 rows)
```
The same query with integer ids finds missing invoice or ticket numbers.

### The general method: flag and running sum
The row-number trick needs "consecutive" = "+1". For any other rule (events < 30 minutes apart, above a threshold, same status as previous): (1) **flag** each island's first row with `lag` (`CASE WHEN <does NOT continue the previous row> THEN 1 ELSE 0 END`); (2) a **running sum** of flags numbers the islands 1, 2, 3, ….

**Sessionization** — a new session after more than 30 minutes idle:
```sql
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
```
Output:
```
 user_id |         at          | new_session | session_no
---------+---------------------+-------------+------------
       7 | 2025-05-01 09:00:00 |           1 |          1
       7 | 2025-05-01 09:10:00 |           0 |          1
       7 | 2025-05-01 09:25:00 |           0 |          1
       7 | 2025-05-01 11:00:00 |           1 |          2
       7 | 2025-05-01 11:05:00 |           0 |          2
       7 | 2025-05-01 15:30:00 |           1 |          3
(6 rows)
```
The first event's `lag` is `NULL`, so the comparison isn't true and `ELSE 1` starts a session, as intended. Group by `(user_id, session_no)` for start, end, length. (The `VALUES` CTE makes inline rows; the first `TIMESTAMP` literal sets the type.)

### Real-world examples
App streaks and contribution graphs, web-analytics sessions (30 minutes idle is a common cut-off), periods of CPU load above 90 %, missing cheque or invoice numbers.

### In the interview
**30-second answer:** "For consecutive dates or integers, subtract `row_number()` from the value; the difference is constant within each run, so grouping by it gives the islands with `min`, `max`, `count`. For general rules, flag each row that starts a new island using `lag`, then a running `sum` of the flags gives an island id. Gaps come from `lead(value) - value > 1`."

- *Users who logged in 3+ consecutive days?* → Islands query, then `HAVING count(*) >= 3`, select distinct users.
- *What if a user logs in twice on the same day?* → De-duplicate to one row per day first, or use `dense_rank`.

### Self-check
1. *Missing numbers between consecutive ids?* → `lead(id) - id > 1` → gap from `id + 1` to `lead(id) - 1`.

---

## 8.10 Named windows, FILTER, evaluation order and performance

### The WINDOW clause
Define a shared window once with `WINDOW name AS (...)` (after `HAVING`, before `ORDER BY`) and write `OVER name`. `OVER (w ROWS 2 PRECEDING)` adds a frame to named window `w` (which must have no frame of its own).
```sql
-- code/09_named_windows_filter_plan.sql
SELECT store, day, amount,
       sum(amount) OVER w_run    AS running_total,
       max(amount) OVER w_run    AS best_so_far,
       rank()      OVER w_amount AS amount_rank
FROM daily_sales
WHERE store = 'Pune'
WINDOW w_run    AS (PARTITION BY store ORDER BY day),
       w_amount AS (PARTITION BY store ORDER BY amount DESC)
ORDER BY day;
```
Output:
```
 store |    day     | amount | running_total | best_so_far | amount_rank
-------+------------+--------+---------------+-------------+-------------
 Pune  | 2025-03-01 | 400.00 |        400.00 |      400.00 |           5
 Pune  | 2025-03-02 | 650.00 |       1050.00 |      650.00 |           2
 ...
 Pune  | 2025-03-07 | 300.00 |       3300.00 |      800.00 |           6
(6 rows)
```
`max(...) OVER w_run` is a running maximum ("best day so far"). `WINDOW` is supported by PostgreSQL, MySQL 8, SQLite and SQL Server 2022 (not earlier).

### FILTER on window aggregates
`FILTER (WHERE ...)` ([chapter 4](../04-aggregation/README.md#44-conditional-aggregation-filter-and-case)) works on aggregate windows too (PostgreSQL, SQLite; elsewhere `count(CASE WHEN cond THEN 1 END) OVER (...)`):
```sql
SELECT day, amount,
       count(*) FILTER (WHERE amount >= 1500) OVER (ORDER BY day) AS big_days_so_far
FROM daily_sales
WHERE store = 'Mumbai'
ORDER BY day;
```
Output:
```
    day     | amount  | big_days_so_far
------------+---------+-----------------
 2025-03-01 | 1200.00 |               0
 2025-03-02 |  900.00 |               0
 2025-03-03 | 1500.00 |               1
 2025-03-04 | 1500.00 |               2
 ...
 2025-03-07 | 1800.00 |               3
(7 rows)
```

### Where windows fit in the logical order
```mermaid
flowchart LR
    A["FROM and JOIN"] --> B["WHERE"] --> C["GROUP BY"] --> D["HAVING"] --> E["Window functions"] --> F["SELECT list and DISTINCT"] --> G["ORDER BY"] --> H["LIMIT / OFFSET"]
```
Consequences:
- **`WHERE` changes the window**: with `WHERE dept_id = 1`, `rank() OVER (ORDER BY salary DESC)` ranks only Engineering (Vikram 1, Neha 2, Rahul and Meera 3). For company-wide ranks shown for Engineering, rank in a subquery **without** the filter, filter outside.
- Windows **can't** be used in `WHERE`, `GROUP BY` or `HAVING` (they don't exist yet) → wrap in a subquery/CTE (8.8).
- Windows **can** be used in `ORDER BY` (`ORDER BY rank() OVER (...)`) and can take aggregates as arguments in grouped queries (8.7).
- `DISTINCT` runs after windows, so `SELECT DISTINCT dept_id, count(*) OVER (PARTITION BY dept_id)` works (one row per department).
- `LIMIT` runs last, so `row_number() ... LIMIT 5` numbers *all* rows first.

### Performance
PostgreSQL runs windows in a **WindowAgg** node (a step of the plan `EXPLAIN` prints) reading rows sorted by `PARTITION BY` + `ORDER BY` keys:
```sql
EXPLAIN (COSTS OFF)
SELECT store, day, sum(amount) OVER (PARTITION BY store ORDER BY day)
FROM daily_sales;

EXPLAIN (COSTS OFF)
SELECT first_name, rank() OVER (PARTITION BY dept_id ORDER BY salary DESC)
FROM employees;
```
Output:
```
                       QUERY PLAN
--------------------------------------------------------
 WindowAgg
   ->  Index Scan using daily_sales_pkey on daily_sales
(2 rows)

               QUERY PLAN
----------------------------------------
 WindowAgg
   ->  Sort
         Sort Key: dept_id, salary DESC
         ->  Seq Scan on employees
(4 rows)
```
- In the first plan the primary key `(store, day)` (uniquely identifying each row, backed by an index) already delivers the needed order — **no sort**. The second must sort by `dept_id, salary DESC`.
- **Index tip:** an index on `(partition columns, order columns)` can remove the sort — e.g. `CREATE INDEX ON employees (dept_id, salary DESC)` ([chapter 12](../12-indexes-storage-and-query-performance/README.md#1211-composite-indexes-and-column-order)).
- **Different** `PARTITION BY`/`ORDER BY` need separate sorts; identical definitions share one pass — reuse named windows.
- Sliding `sum`/`count`/`avg` frames are computed incrementally (adjusting the previous result), but `min`/`max` over a moving frame (and `EXCLUDE` frames) may re-scan the frame — expensive for large frames.
- A window over millions of rows sorts them all; filter in `WHERE` first when logic allows.

---

## Chapter 8 TL;DR

- **Window function** = `f(...) OVER (PARTITION BY … ORDER BY … frame)`: computed per row over related rows; **row count never changes**.
- **`PARTITION BY`** = groups without collapsing → share of total, difference from group average.
- **`ORDER BY` in `OVER`** makes aggregates running; the default frame `RANGE UNBOUNDED PRECEDING … CURRENT ROW` includes ties — use a unique order or `ROWS`.
- **Frames**: `ROWS` (physical rows), `RANGE` (value distance, intervals, ties included), `GROUPS` (peer groups), `EXCLUDE` — PostgreSQL 11+ for offsets/`GROUPS`/`EXCLUDE`.
- **Ranking**: `row_number` (unique), `rank` (gaps), `dense_rank` (no gaps), `ntile`, `percent_rank`, `cume_dist`. Always decide how ties behave.
- **Value functions**: `lag`/`lead` (ignore frame), `first_value`/`last_value`/`nth_value` (use frame — give `last_value` a full frame). `IGNORE NULLS` only from PostgreSQL 19.
- **Patterns**: running total, moving average, growth vs `lag`, top-N per group (rank in CTE, filter outside), dedup (`row_number() = 1`), gaps & islands (`value − row_number()`, or flag + running sum).
- **Order of evaluation**: after `WHERE`/`GROUP BY`/`HAVING`, before `DISTINCT`/`ORDER BY`/`LIMIT`. No `QUALIFY` in PostgreSQL.
- **Performance**: one sort per distinct window; an index on (partition, order) columns can remove it.

Next: [chapter 9 — DDL, DML and constraints](../09-ddl-dml-and-constraints/README.md): creating and changing tables, keys and constraints, and inserting, updating and deleting data safely.
