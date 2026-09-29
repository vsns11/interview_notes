-- 14.8  Security: roles, GRANT / REVOKE, column privileges, row-level security (RLS)
-- Roles are shared by the whole server (not per database), so this file uses a ch14_ prefix
-- and removes its roles at the end.
SET client_min_messages = warning;
DROP ROLE IF EXISTS ch14_reporting, ch14_analyst, ch14_app, ch14_priya, ch14_arjun;
RESET client_min_messages;

-- (a) Roles: a "group" role that holds privileges, and login roles that are members of it
CREATE ROLE ch14_reporting NOLOGIN;                          -- a group: nobody logs in as it
CREATE ROLE ch14_analyst  LOGIN IN ROLE ch14_reporting;      -- a user who inherits its privileges
CREATE ROLE ch14_app      LOGIN;                             -- the application's own account

-- (b) GRANT: the reporting group may read two tables; the app may read/write orders
GRANT SELECT ON orders, order_items TO ch14_reporting;
GRANT SELECT, INSERT, UPDATE ON orders TO ch14_app;
GRANT SELECT (customer_id, first_name, city, country) ON customers TO ch14_reporting;  -- column-level

SELECT grantee, table_name, string_agg(privilege_type, ', ' ORDER BY privilege_type) AS privileges
FROM   information_schema.role_table_grants
WHERE  grantee LIKE 'ch14_%'
GROUP  BY 1, 2 ORDER BY 1, 2;

-- (c) Try it: SET ROLE switches the current user for this session
SET ROLE ch14_analyst;
SELECT current_user, count(*) AS orders_visible FROM orders;
SELECT first_name, city FROM customers WHERE customer_id = 1;           -- allowed columns
DO $$ BEGIN
    PERFORM email FROM customers;                                        -- email was not granted
EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'analyst: %', SQLERRM;
END $$;
DO $$ BEGIN
    DELETE FROM orders WHERE order_id = 114;                             -- no DELETE privilege
EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'analyst: %', SQLERRM;
END $$;
RESET ROLE;

-- (d) REVOKE takes a privilege away again
REVOKE SELECT ON order_items FROM ch14_reporting;
SET ROLE ch14_analyst;
DO $$ BEGIN
    PERFORM 1 FROM order_items;
EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'after revoke: %', SQLERRM;
END $$;
RESET ROLE;

-- (e) Row-level security: each sales rep sees only the orders they sold
CREATE ROLE ch14_priya LOGIN;
CREATE ROLE ch14_arjun LOGIN;
CREATE TABLE rep_logins (role_name text PRIMARY KEY, emp_id int NOT NULL REFERENCES employees);
INSERT INTO rep_logins VALUES ('ch14_priya', 7), ('ch14_arjun', 8);
GRANT SELECT ON rep_logins TO ch14_priya, ch14_arjun;
GRANT SELECT, UPDATE ON orders TO ch14_priya, ch14_arjun;

ALTER TABLE orders ENABLE ROW LEVEL SECURITY;          -- from now on: no policy = no rows

CREATE POLICY rep_sees_own_orders ON orders
    FOR ALL                                            -- SELECT, INSERT, UPDATE, DELETE
    TO ch14_priya, ch14_arjun
    USING      (sales_rep_id = (SELECT emp_id FROM rep_logins WHERE role_name = current_user))
    WITH CHECK (sales_rep_id = (SELECT emp_id FROM rep_logins WHERE role_name = current_user));

CREATE POLICY reporting_reads_all ON orders FOR SELECT TO ch14_reporting USING (true);

SET ROLE ch14_priya;
SELECT current_user, order_id, sales_rep_id, status FROM orders ORDER BY order_id;
UPDATE orders SET status = 'shipped' WHERE order_id = 103;     -- Arjun's order: invisible, 0 rows
DO $$ BEGIN
    UPDATE orders SET sales_rep_id = 8 WHERE order_id = 101;    -- would hand the row to Arjun
EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'priya: %', SQLERRM;
END $$;
RESET ROLE;

SET ROLE ch14_arjun;
SELECT current_user, count(*) AS my_orders FROM orders;
RESET ROLE;

SET ROLE ch14_analyst;                                         -- member of ch14_reporting
SELECT current_user, count(*) AS all_orders FROM orders;
RESET ROLE;

SET ROLE ch14_app;                                             -- has privileges but no policy
SELECT current_user, count(*) AS app_sees FROM orders;
RESET ROLE;

-- (f) Tenant isolation with a session setting (common in multi-tenant SaaS)
CREATE TABLE notes (id int PRIMARY KEY, tenant text NOT NULL, body text NOT NULL);
INSERT INTO notes VALUES (1, 'acme', 'renew contract'), (2, 'globex', 'send invoice'), (3, 'acme', 'call Bob');
ALTER TABLE notes ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON notes
    USING (tenant = current_setting('app.tenant', true));    -- true = missing setting gives NULL, not error
GRANT SELECT ON notes TO ch14_app;

SET ROLE ch14_app;
SET app.tenant = 'acme';
SELECT * FROM notes ORDER BY id;
SET app.tenant = 'globex';
SELECT * FROM notes ORDER BY id;
RESET app.tenant;
SELECT count(*) AS no_tenant_set FROM notes;
RESET ROLE;

-- Superusers and the table owner bypass RLS (unless ALTER TABLE ... FORCE ROW LEVEL SECURITY)
SELECT current_user, count(*) AS owner_sees FROM notes;

-- Clean up the server-wide roles
DROP OWNED BY ch14_reporting, ch14_analyst, ch14_app, ch14_priya, ch14_arjun;
DROP ROLE ch14_analyst, ch14_reporting, ch14_app, ch14_priya, ch14_arjun;
