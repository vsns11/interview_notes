-- 14.6  Full-text search: tsvector, tsquery, @@, ranking, highlighting, GIN index

-- How text becomes searchable: words are lower-cased, stop words dropped, words reduced to stems
SELECT to_tsvector('english', 'The chairs were shipped quickly and the shipping was free') AS doc;
SELECT to_tsquery('english', 'shipping & chair')                                     AS q1,
       plainto_tsquery('english', 'fast shipping chairs')                            AS q2,
       websearch_to_tsquery('english', '"standing desk" -cheap or lamp')             AS q3;

-- A help-centre table; the tsvector is a generated column (PostgreSQL 12+),
-- title words weighted A (most important), body words weighted B
CREATE TABLE help_articles (
    id     int PRIMARY KEY,
    title  text NOT NULL,
    body   text NOT NULL,
    search tsvector GENERATED ALWAYS AS (
             setweight(to_tsvector('english', title), 'A') ||
             setweight(to_tsvector('english', body),  'B')) STORED
);
INSERT INTO help_articles (id, title, body) VALUES
 (1, 'Returning a chair',           'Chairs can be returned within 30 days. Shipping for returns is free.'),
 (2, 'Standing desk assembly',      'Attach the legs, then connect the dual motor. A standing desk needs two people.'),
 (3, 'Shipping times',              'Most orders ship in two days. Furniture such as a desk or chair ships in a week.'),
 (4, 'Keyboard not connecting',     'Check the USB-C cable or re-pair the wireless receiver.'),
 (5, 'Cheap desk lamps discontinued','The old desk lamp is discontinued; a new lamp arrives next month.');

-- Match with @@ and order by relevance (ts_rank: frequency + weights)
SELECT id, title, round(ts_rank(search, q)::numeric, 3) AS rank
FROM   help_articles, websearch_to_tsquery('english', 'chair shipping') AS q
WHERE  search @@ q
ORDER  BY rank DESC, id;

-- Prefix matching (autocomplete) with :*  and phrase search with <->
SELECT id, title FROM help_articles WHERE search @@ to_tsquery('english', 'assem:*');
SELECT id, title FROM help_articles WHERE search @@ websearch_to_tsquery('english', '"standing desk"');
SELECT id, title FROM help_articles WHERE search @@ websearch_to_tsquery('english', 'desk -lamp') ORDER BY id;

-- Highlight the matched words in a snippet
SELECT id, ts_headline('english', body, websearch_to_tsquery('english', 'desk chair'),
                       'StartSel=[, StopSel=], MaxWords=12, MinWords=5') AS snippet
FROM   help_articles
WHERE  search @@ websearch_to_tsquery('english', 'desk chair')
ORDER  BY id;

-- Why LIKE is not enough: no stemming, so the body text "returned"/"returns" does not match "returning"
-- (and a pattern with a leading % cannot use a B-tree index)
SELECT count(*) FILTER (WHERE body ILIKE '%returning%')                                  AS like_hits,
       count(*) FILTER (WHERE to_tsvector('english', body) @@ to_tsquery('english', 'returning')) AS fts_hits
FROM   help_articles;

-- GIN index on the tsvector
CREATE INDEX help_articles_search_gin ON help_articles USING gin (search);
ANALYZE help_articles;
SET enable_seqscan = off;
EXPLAIN (COSTS OFF)
SELECT id FROM help_articles WHERE search @@ websearch_to_tsquery('english', 'shipping');
RESET enable_seqscan;
