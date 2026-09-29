-- 14.3  Triggers: updated_at, validation, audit log, statement-level trigger

-- (a) BEFORE UPDATE row trigger that stamps updated_at
ALTER TABLE products ADD COLUMN updated_at timestamptz NOT NULL DEFAULT '2025-01-01 00:00+00';

CREATE FUNCTION set_updated_at() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at := now();      -- change the row before it is written
    RETURN NEW;                   -- a BEFORE row trigger must return the row to store (or NULL to skip it)
END;
$$;

CREATE TRIGGER products_updated_at
BEFORE UPDATE ON products
FOR EACH ROW
WHEN (OLD.* IS DISTINCT FROM NEW.*)          -- skip no-op updates
EXECUTE FUNCTION set_updated_at();

UPDATE products SET unit_price = 22.99 WHERE product_id = 2;
UPDATE products SET unit_price = 35.00 WHERE product_id = 8;   -- same value: WHEN is false, no stamp
SELECT product_id, unit_price, updated_at > '2025-01-02' AS stamped_now
FROM   products WHERE product_id IN (2, 8) ORDER BY product_id;

-- (b) Validation trigger: reject a rule that a CHECK constraint cannot express (it needs another table)
CREATE FUNCTION forbid_discontinued() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    IF (SELECT discontinued FROM products WHERE product_id = NEW.product_id) THEN
        RAISE EXCEPTION 'product % is discontinued', NEW.product_id;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER order_items_check_product
BEFORE INSERT OR UPDATE OF product_id ON order_items
FOR EACH ROW EXECUTE FUNCTION forbid_discontinued();

DO $$ BEGIN
    INSERT INTO order_items VALUES (114, 8, 1, 35.00, 0);        -- Desk Lamp is discontinued
EXCEPTION WHEN raise_exception THEN RAISE NOTICE 'rejected: %', SQLERRM;
END $$;

-- (c) Generic audit trigger: one function for any table, rows stored as JSONB
CREATE TABLE audit_log (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    table_name  text        NOT NULL,
    op          text        NOT NULL,             -- INSERT / UPDATE / DELETE
    changed_by  text        NOT NULL DEFAULT current_user,
    changed_at  timestamptz NOT NULL DEFAULT now(),
    old_row     jsonb,
    new_row     jsonb
);

CREATE FUNCTION audit_row() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO audit_log (table_name, op, old_row, new_row)
    VALUES (TG_TABLE_NAME, TG_OP,
            CASE WHEN TG_OP IN ('UPDATE','DELETE') THEN to_jsonb(OLD) END,
            CASE WHEN TG_OP IN ('INSERT','UPDATE') THEN to_jsonb(NEW) END);
    RETURN NULL;                  -- return value of an AFTER trigger is ignored
END;
$$;

CREATE TRIGGER customers_audit
AFTER INSERT OR UPDATE OR DELETE ON customers
FOR EACH ROW EXECUTE FUNCTION audit_row();

INSERT INTO customers VALUES (11, 'Jia', 'Wong', 'jia.wong@mail.example', 'Singapore', 'Singapore', '2025-06-20');
UPDATE customers SET city = 'Mumbai' WHERE customer_id = 8;
DELETE FROM customers WHERE customer_id = 11;

SELECT id, table_name, op, changed_by,
       old_row ->> 'city' AS old_city, new_row ->> 'city' AS new_city
FROM   audit_log ORDER BY id;

-- Only the columns that changed (compare OLD and NEW key by key)
SELECT n.key, o.value AS before, n.value AS after
FROM   audit_log a,
       jsonb_each(a.new_row) n
JOIN   jsonb_each(a.old_row) o USING (key)
WHERE  a.op = 'UPDATE' AND n.value IS DISTINCT FROM o.value;

-- (d) Statement-level trigger with a transition table: fires once per statement
CREATE FUNCTION log_bulk_price_change() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    RAISE NOTICE '% rows changed price, average change %',
        (SELECT count(*) FROM new_rows),
        (SELECT round(avg(n.unit_price - o.unit_price), 2)
           FROM new_rows n JOIN old_rows o USING (product_id));
    RETURN NULL;
END;
$$;

CREATE TRIGGER products_bulk_price
AFTER UPDATE ON products
REFERENCING OLD TABLE AS old_rows NEW TABLE AS new_rows
FOR EACH STATEMENT EXECUTE FUNCTION log_bulk_price_change();

UPDATE products SET unit_price = unit_price * 1.10 WHERE category = 'Furniture';

-- (e) Listing triggers
SELECT event_object_table AS tbl, trigger_name, action_timing AS timing,
       string_agg(event_manipulation, ',' ORDER BY event_manipulation) AS events, action_orientation AS per
FROM   information_schema.triggers
GROUP  BY 1, 2, 3, 5 ORDER BY 1, 2;
