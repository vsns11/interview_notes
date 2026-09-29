-- 14.1  User-defined functions: SQL functions, PL/pgSQL functions, set-returning functions

-- (a) A plain SQL function: one expression, marked IMMUTABLE (same input -> same output, no table access)
CREATE FUNCTION line_total(qty integer, price numeric, disc numeric DEFAULT 0)
RETURNS numeric
LANGUAGE sql IMMUTABLE
RETURN round(qty * price * (1 - disc), 2);          -- SQL-standard body (PostgreSQL 14+)

SELECT order_id, product_id, line_total(quantity, unit_price, discount) AS total
FROM   order_items WHERE order_id = 108;

-- (b) A PL/pgSQL function with variables, IF, a query INTO a variable and RAISE
CREATE FUNCTION customer_tier(p_customer_id integer)
RETURNS text
LANGUAGE plpgsql STABLE                               -- reads tables, does not change them
AS $$
DECLARE
    v_spent numeric;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM customers WHERE customer_id = p_customer_id) THEN
        RAISE EXCEPTION 'customer % does not exist', p_customer_id
              USING ERRCODE = 'no_data_found';
    END IF;

    SELECT coalesce(sum(line_total(oi.quantity, oi.unit_price, oi.discount)), 0)
    INTO   v_spent
    FROM   orders o JOIN order_items oi USING (order_id)
    WHERE  o.customer_id = p_customer_id AND o.status <> 'cancelled';

    RETURN CASE WHEN v_spent >= 1500 THEN 'gold'
                WHEN v_spent >= 300  THEN 'silver'
                ELSE 'bronze' END;
END;
$$;

SELECT customer_id, first_name, customer_tier(customer_id) AS tier
FROM   customers ORDER BY customer_id LIMIT 5;

-- (c) Catching an error inside PL/pgSQL (BEGIN ... EXCEPTION ... END)
CREATE FUNCTION safe_tier(p_customer_id integer) RETURNS text
LANGUAGE plpgsql STABLE AS $$
BEGIN
    RETURN customer_tier(p_customer_id);
EXCEPTION
    WHEN no_data_found THEN
        RETURN 'unknown (' || SQLERRM || ')';
END;
$$;

SELECT safe_tier(3) AS known, safe_tier(999) AS missing;

-- (d) A set-returning function: RETURNS TABLE, used in FROM like a table
CREATE FUNCTION top_products(p_limit integer DEFAULT 3)
RETURNS TABLE (product text, units bigint, revenue numeric)
LANGUAGE sql STABLE AS $$
    SELECT p.name, sum(oi.quantity), sum(line_total(oi.quantity, oi.unit_price, oi.discount))
    FROM   order_items oi
    JOIN   orders   o USING (order_id)
    JOIN   products p USING (product_id)
    WHERE  o.status <> 'cancelled'
    GROUP  BY p.name
    ORDER  BY 3 DESC
    LIMIT  p_limit
$$;

SELECT * FROM top_products();          -- default argument
SELECT * FROM top_products(p_limit => 1);   -- named argument

-- (e) A loop over a query (FOR row IN query LOOP) with RETURN NEXT
CREATE FUNCTION salary_bands(p_step numeric)
RETURNS TABLE (band text, headcount integer)
LANGUAGE plpgsql STABLE AS $$
DECLARE
    r record;
BEGIN
    FOR r IN SELECT floor(salary / p_step) * p_step AS lo, count(*)::int AS n
             FROM employees GROUP BY 1 ORDER BY 1
    LOOP
        band := r.lo || ' - ' || (r.lo + p_step - 1);
        headcount := r.n;
        RETURN NEXT;                       -- emit one output row
    END LOOP;
END;
$$;

SELECT * FROM salary_bands(50000);

-- (f) An anonymous block: DO runs PL/pgSQL once, without creating a function
DO $$
DECLARE n int;
BEGIN
    SELECT count(*) INTO n FROM orders WHERE status = 'pending';
    RAISE NOTICE 'pending orders: %', n;
END;
$$;

-- (g) Where functions live: the catalog
SELECT proname, prokind, provolatile, pg_get_function_arguments(oid) AS args
FROM   pg_proc
WHERE  pronamespace = 'public'::regnamespace
ORDER  BY proname;
