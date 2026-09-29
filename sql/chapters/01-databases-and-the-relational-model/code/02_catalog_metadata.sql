-- 1.x  A database describes itself: ask the catalog about a table
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'departments'
ORDER BY ordinal_position;

-- Every table in the sample database, with its row count estimate source
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

-- The same knowledge, from PostgreSQL's own catalog (pg_catalog)
SELECT conname AS constraint_name, contype AS kind, conrelid::regclass AS on_table
FROM pg_constraint
WHERE conrelid = 'employees'::regclass
ORDER BY conname;

-- Physical settings are visible too: the page size and the buffer pool size
SHOW block_size;
SHOW shared_buffers;
