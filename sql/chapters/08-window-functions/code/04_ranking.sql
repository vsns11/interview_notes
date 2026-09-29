-- 8.5  Ranking functions and how they treat ties
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

-- Ranking inside groups: rank employees within each department
SELECT dept_id, first_name, salary,
       dense_rank() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rank_in_dept
FROM employees
WHERE dept_id IS NOT NULL
ORDER BY dept_id, rank_in_dept, first_name;

-- NTILE splits rows into buckets as evenly as possible (sizes differ by at most 1)
SELECT quartile, count(*) AS people, min(salary), max(salary)
FROM (SELECT salary, ntile(4) OVER (ORDER BY salary) AS quartile FROM employees) t
GROUP BY quartile
ORDER BY quartile;

-- 2nd highest distinct salary (NULL if there isn't one)
SELECT max(salary) FROM (
    SELECT salary, dense_rank() OVER (ORDER BY salary DESC) AS dr FROM employees
) t WHERE dr = 2;
