-- 12.1 Where rows physically live: files, 8 KB pages, tuples, TOAST.
CREATE EXTENSION IF NOT EXISTS pageinspect;

-- How big are the table and its primary-key index on disk?
SELECT relname,
       pg_size_pretty(pg_relation_size(oid))        AS main_fork,
       pg_relation_size(oid) / current_setting('block_size')::int AS pages,
       reltuples::bigint                            AS est_rows
FROM pg_class
WHERE relname IN ('orders', 'orders_pkey', 'customers')
ORDER BY relname;

-- Which file holds the table? (path relative to the data directory)
SELECT pg_relation_filepath('orders') LIKE 'base/%' AS lives_under_base_dir;

-- ctid = (page number, slot in page): the physical address of a row version.
SELECT ctid, id, status, amount FROM orders WHERE id IN (1, 2, 3, 124, 125) ORDER BY id;

-- Rows per page (8 KB page / ~60-byte rows)
SELECT count(*) AS rows_on_page_0 FROM orders WHERE (ctid::text::point)[0] = 0;

-- Look inside page 0: the header, then the first few line pointers.
SELECT lower, upper, special, pagesize FROM page_header(get_raw_page('orders', 0));
SELECT lp, lp_off, lp_len, t_xmin <> 0 AS has_xmin, t_ctid
FROM heap_page_items(get_raw_page('orders', 0))
LIMIT 4;

-- Large values are compressed and/or moved out of line (TOAST).
CREATE TABLE docs (id int PRIMARY KEY, body text);
INSERT INTO docs VALUES
  (1, repeat('abc', 10)),                 -- tiny: stored inline as is
  (2, repeat('abcdefgh', 5000)),          -- 40 KB, very repetitive: compresses well
  (3, (SELECT string_agg(md5(g::text), '') FROM generate_series(1, 1500) g)); -- 48 KB of hex
SELECT id, octet_length(body) AS logical_bytes, pg_column_size(body) AS stored_bytes
FROM docs ORDER BY id;
SELECT pg_size_pretty(pg_relation_size(reltoastrelid)) AS toast_table_size
FROM pg_class WHERE relname = 'docs';

-- B-tree anatomy of the primary key: metapage says how tall the tree is.
SELECT level AS root_level, fastlevel FROM bt_metap('orders_pkey');
SELECT type, live_items, avg_item_size, free_size FROM bt_page_stats('orders_pkey', 1);
