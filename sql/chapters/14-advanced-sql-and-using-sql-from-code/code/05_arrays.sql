-- 14.5  Arrays: a column holding a list of values of one type

ALTER TABLE products ADD COLUMN tags text[] NOT NULL DEFAULT '{}';
UPDATE products SET tags = CASE product_id
    WHEN 1 THEN ARRAY['laptop','work','premium']
    WHEN 2 THEN '{mouse,wireless,budget}'          -- text literal form
    WHEN 4 THEN ARRAY['keyboard','work']
    WHEN 5 THEN ARRAY['desk','work','premium']
    WHEN 7 THEN ARRAY['audio','wireless','premium']
    ELSE '{}' END;

-- Indexing is 1-based; slices; length; out-of-range gives NULL (no error)
SELECT name, tags, tags[1] AS first_tag, tags[2:3] AS slice,
       cardinality(tags) AS n, tags[9] AS no_such
FROM   products WHERE product_id IN (1, 2, 9) ORDER BY product_id;

-- Searching: = ANY(array), contains @>, overlaps &&
SELECT name FROM products WHERE 'premium' = ANY (tags)                  ORDER BY 1;
SELECT name FROM products WHERE tags @> ARRAY['work','premium']         ORDER BY 1;
SELECT name FROM products WHERE tags && ARRAY['wireless','keyboard']    ORDER BY 1;

-- Modifying: append, remove, concatenate
SELECT array_append(ARRAY['a','b'], 'c')         AS appended,
       array_remove(ARRAY['a','b','a'], 'a')     AS removed,
       ARRAY[1,2] || ARRAY[3]                     AS concatenated,
       array_position(ARRAY['x','y','z'], 'y')   AS pos;

-- unnest turns an array into rows (WITH ORDINALITY adds the position) ...
SELECT p.name, t.tag, t.pos
FROM   products p, unnest(p.tags) WITH ORDINALITY AS t(tag, pos)
WHERE  p.product_id = 1;

-- ... and array_agg turns rows back into an array: tag popularity
SELECT tag, count(*) AS products, array_agg(name ORDER BY name) AS which
FROM   products, unnest(tags) AS tag
GROUP  BY tag HAVING count(*) > 1 ORDER BY products DESC, tag;

-- Passing a list as ONE parameter (handy from application code): = ANY($1)
PREPARE by_ids(int[]) AS SELECT product_id, name FROM products WHERE product_id = ANY ($1) ORDER BY 1;
EXECUTE by_ids('{3,5,7}');

-- string <-> array
SELECT string_to_array('red,green,blue', ',') AS arr, array_to_string(ARRAY['a','b'], ' | ') AS str;

-- GIN index for @> / && / <@ on arrays
VACUUM products;                -- prune old row versions left by the UPDATEs above, so the new index is usable at once
CREATE INDEX products_tags_gin ON products USING gin (tags);
ANALYZE products;
SET enable_seqscan = off;
EXPLAIN (COSTS OFF) SELECT name FROM products WHERE tags @> ARRAY['premium'];
RESET enable_seqscan;
