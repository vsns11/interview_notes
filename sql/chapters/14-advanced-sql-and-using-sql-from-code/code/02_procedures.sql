-- 14.2  Stored procedures: CALL, INOUT parameters, transaction control inside a procedure

-- A procedure that "archives" old delivered orders in batches and COMMITs after each batch.
CREATE TABLE orders_archive (LIKE orders INCLUDING ALL);

CREATE PROCEDURE archive_delivered(p_before date, p_batch int, INOUT moved int DEFAULT 0)
LANGUAGE plpgsql AS $$
DECLARE
    n int;
BEGIN
    LOOP
        WITH picked AS (
            SELECT order_id FROM orders
            WHERE  status = 'delivered' AND placed_at < p_before
              AND  order_id NOT IN (SELECT order_id FROM orders_archive)
            ORDER  BY order_id
            LIMIT  p_batch
        )
        INSERT INTO orders_archive
        SELECT o.* FROM orders o JOIN picked USING (order_id);
        GET DIAGNOSTICS n = ROW_COUNT;     -- rows affected by the last statement
        EXIT WHEN n = 0;
        moved := moved + n;
        RAISE NOTICE 'batch of % committed', n;
        COMMIT;                            -- allowed in a procedure called outside a transaction block
    END LOOP;
END;
$$;

CALL archive_delivered('2025-04-01', 2);          -- INOUT value comes back as a result row

SELECT order_id, status, placed_at::date FROM orders_archive ORDER BY order_id;

-- COMMIT inside a procedure fails if the caller already opened a transaction:
BEGIN;
DO $$ DECLARE m int := 0; BEGIN
    CALL archive_delivered('2025-12-31', 2, m);
EXCEPTION WHEN invalid_transaction_termination THEN
    RAISE NOTICE 'caught: %', SQLERRM;
END $$;
ROLLBACK;

-- prokind: f = function, p = procedure
SELECT prokind, count(*) FROM pg_proc WHERE pronamespace = 'public'::regnamespace GROUP BY prokind;
