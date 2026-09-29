-- 14.9  SQL injection inside the database: dynamic SQL done wrong and right

-- VULNERABLE: user input is glued into the SQL text
CREATE FUNCTION find_customers_bad(p_last_name text)
RETURNS TABLE (customer_id int, first_name text, email text)
LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY EXECUTE
        'SELECT customer_id, first_name, email FROM customers WHERE last_name = '''
        || p_last_name || '''';
END;
$$;

SELECT * FROM find_customers_bad('Carter');                     -- normal use: 1 row
SELECT * FROM find_customers_bad($$nobody' OR '1'='1$$);         -- attack: every row leaks

-- SAFE: the value travels separately from the SQL text (EXECUTE ... USING)
CREATE FUNCTION find_customers_good(p_last_name text)
RETURNS TABLE (customer_id int, first_name text, email text)
LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY EXECUTE
        'SELECT customer_id, first_name, email FROM customers WHERE last_name = $1'
        USING p_last_name;
END;
$$;

SELECT * FROM find_customers_good('Carter');
SELECT count(*) AS rows_leaked FROM find_customers_good($$nobody' OR '1'='1$$);

-- Identifiers (table/column names) cannot be parameters: quote them with format('%I') and
-- check them against an allow-list; literals get %L
CREATE FUNCTION count_rows(p_table text) RETURNS bigint
LANGUAGE plpgsql AS $$
DECLARE n bigint;
BEGIN
    IF p_table NOT IN ('orders', 'customers', 'products') THEN
        RAISE EXCEPTION 'table % is not allowed', p_table;
    END IF;
    EXECUTE format('SELECT count(*) FROM %I', p_table) INTO n;
    RETURN n;
END;
$$;
SELECT count_rows('orders') AS orders;
SELECT format('SELECT * FROM %I WHERE name = %L', 'Order Items', $$O'Brien$$) AS what_format_builds;

-- Server-side prepared statement: parse once, run many times with different values
PREPARE orders_of(int) AS
    SELECT order_id, status FROM orders WHERE customer_id = $1 ORDER BY order_id;
EXECUTE orders_of(2);
EXECUTE orders_of(5);
DEALLOCATE orders_of;
