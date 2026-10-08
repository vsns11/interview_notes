# SQL Handbook

One place to learn SQL and relational databases, from your first `SELECT` to indexes, transactions, scaling and schema design, and to prepare for SQL and database interviews, without switching between platforms. Each topic combines, in one explanation:
- **the DesignGurus courses** you own: *Grokking SQL for Tech Interviews*, *Grokking Relational Database Design and Modeling for Software Engineers* and *Grokking Database Fundamentals for Tech Interviews*,
- **classic books**: *Database System Concepts* (Silberschatz, Korth, Sudarshan), *SQL Antipatterns* (Bill Karwin), *Use The Index, Luke* (Markus Winand) and *Designing Data-Intensive Applications* (Martin Kleppmann),
- **official documentation**: the PostgreSQL manual first, plus the MySQL, SQL Server, SQLite and Oracle docs for dialect differences, used to fill gaps and check facts,
- **real-world examples** from shops, banks, booking sites and social apps.

PostgreSQL is the main dialect. Where other databases behave differently, small tables show MySQL, SQL Server, SQLite and Oracle side by side. Every chapter is written for a first-time learner and defines each term the first time it appears. Every SQL example runs on a private PostgreSQL 14 server (or in SQLite), and its **real output** appears under it.

> Personal study notes. Course-derived material is paraphrased in my own words, not copied. The course's SQL problems are restated with my own wording, sample data and solutions. Not for redistribution.

## Who it's for
| Reader | What you get |
|---|---|
| Complete beginners | SQL taught from zero: what a database is, then reading, filtering, grouping and joining data (Ch 1–6), every term defined |
| Analysts and developers who know basic SQL | The parts people skip: NULL logic, CTEs and recursion, window functions, upsert, and the dialect traps (Ch 2–9) |
| Backend developers | Constraints, indexes and `EXPLAIN`, transactions and isolation, JSONB, full-text search, row-level security, SQL from Python and Java, pooling and ORMs (Ch 9, 12–14) |
| System-design learners | How storage, B-trees, MVCC and WAL work, then partitioning, sharding, replication, CAP, consensus, NoSQL and full schema case studies (Ch 12–17) |
| Interview candidates | 83 worked SQL problems with a method for each (Ch 18–19), plus cheat sheets, 50 interview Q&As and a 298-term glossary (Ch 20) |

## How to use it
Every topic follows the same rhythm: *what it is → why it matters → how it works → code with real output → trade-offs → common mistakes → real-world use → in the interview → self-check → TL;DR*. To review, skim the TL;DRs. To check you really learned a topic, answer the self-check questions without looking back.

**Beginner path (read in order):** 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8, then 9 → 10 → 11. Start the private server as §1.10 shows, type the queries yourself in `psql`, and change them to see what happens. Then practise with Chapter 18. Keep Chapter 20 open as a quick reference.

**Backend developer path:**
1. Skim the TL;DRs of Ch 2–6, then read **Ch 7–8** (CTEs, views, window functions) properly.
2. **Ch 9**: data types, keys and constraints, identity columns, safe `ALTER`, upsert with `ON CONFLICT`, backups.
3. **Ch 12**: how rows sit in pages, B-tree indexes, composite column order, reading `EXPLAIN (ANALYZE, BUFFERS)`, N+1 queries and keyset pagination.
4. **Ch 13**: transactions, isolation levels and anomalies, lost updates, deadlocks, `FOR UPDATE SKIP LOCKED` job queues and retries on 40001.
5. **Ch 14**: functions, triggers, JSONB, full-text search, roles and row-level security, SQL injection, psycopg and JDBC, connection pooling, ORMs vs raw SQL. Then the runnable case studies in **Ch 17**.

**Database internals and system design path:**
1. **Ch 1** (inside a DBMS: query processor, buffer pool, storage engine, transaction, lock and recovery managers) and **Ch 10–11** (modeling and normalization).
2. **Ch 12** (pages, file organization, compression, B+ trees, hashing, Bloom filters, GIN/GiST/BRIN, join algorithms, statistics).
3. **Ch 13** (ACID, WAL and durability, two-phase locking, timestamp ordering, MVCC and VACUUM).
4. **Ch 15** (partitioning, sharding, consistent hashing, distributed queries, replication, consistency models, CAP and PACELC, consensus, 2PC, recovery, failover) and **Ch 16** (key-value, document, wide-column and graph stores, LSM trees, NewSQL, time-series, choosing a database).
5. **Ch 17**: thirteen schema designs, from a hospital to Airbnb. Continue with the [DDIA notes](../ddia/README.md) and [System Design Patterns](../system-design-patterns/README.md).

**SQL interview path (you already write some SQL):**
1. Read the TL;DRs of Ch 2–8 and fix any gaps, especially NULL logic (§2.5), `LEFT JOIN` filters (Ch 5), `NOT IN` vs `NOT EXISTS` (Ch 6) and window frames (Ch 8).
2. Learn the method in **§18.1**, then solve **Ch 18** (42 warm-up to medium problems) before reading each solution.
3. Solve **Ch 19** (41 medium and hard problems: top-N per group, gaps and islands, pivots, relational division, retention).
4. For design questions, read the "In the interview" sections of Ch 10–13 and a few case studies in Ch 17.
5. The night before, review **Ch 20**: the pattern catalogue (§20.18), the 50 questions and answers (§20.19) and the dialect table (§20.16).

```mermaid
flowchart LR
    A["Part I - Querying basics: Ch 1-4"] --> B["Part II - Combining and advanced queries: Ch 5-8"]
    B --> C["Part III - Building and designing: Ch 9-11"]
    C --> D["Part IV - Performance and correctness: Ch 12-14"]
    D --> E["Part V - Scale and beyond: Ch 15-17"]
    E --> F["Part VI - Interviews and revision: Ch 18-20"]
```

## The private PostgreSQL server
The book never uses a PostgreSQL server you may already have. `tools_pg.sh` (in the notes root, one level above `sql/`) runs a **private PostgreSQL 14** that belongs only to this book:
- its data lives in `~/.pg-books-system-design/` (in your home folder, deliberately outside the iCloud-synced Documents folder, because syncing a live database folder can corrupt it),
- it listens on port **5544** through a Unix socket in `/tmp` only (no TCP), while a normal install uses port 5432,
- it is initialised with `UTF8` encoding, and the runners create every database with `ENCODING 'UTF8' TEMPLATE template0`, so text such as `café` or `मुंबई` is stored and measured correctly.

Run these from the notes root (`system_design_notes/`). §1.10 explains each step.

```bash
bash tools_pg.sh start                         # first run also creates ~/.pg-books-system-design/
bash tools_pg.sh psql -d postgres -c "CREATE DATABASE company ENCODING 'UTF8' TEMPLATE template0"
bash tools_pg.sh psql -d company -q -f sql/chapters/01-databases-and-the-relational-model/code/00_schema.sql
bash tools_pg.sh psql -d company               # interactive psql; \q to leave
bash tools_pg.sh stop                          # when you are done
```
`tools_pg.sh psql` is `psql -h /tmp -p 5544 -U postgres -X`, so extra arguments (`-d`, `-c`, `-f`) pass straight through, and `-X` ignores any personal `~/.psqlrc`. Requirements: PostgreSQL 14 client and server binaries on the `PATH` (`initdb`, `pg_ctl`, `psql`; checked with Homebrew's 14.15) and `sqlite3` (3.43).

## The shared sample database
Chapters 1–9 and 14 query one small dataset, **`company`**: a fictional firm that sells office equipment, with `departments`, `employees` (and their managers), `projects` and `employee_projects` on the staff side, and `customers`, `products`, `orders` and `order_items` on the shop side. §1.11 draws its ER diagram and explains every table. Each of those chapters has its own copy in `code/00_schema.sql`, sometimes with a few extra tables for that chapter.

The later chapters need different data, so they bring their own `00_schema.sql`: a bike-rental shop (Ch 10), one wide unnormalized order table (Ch 11), a shop with one million generated orders so the planner cares about indexes (Ch 12), bank accounts, stock and on-call rotas (Ch 13), orders in three regions (Ch 15), four case-study schemas `shop`, `tix`, `lib` and `insta` (Ch 17), and small fixtures for the cheat-sheet checks (Ch 20). Every interview problem in Ch 18–19 creates its own tables.

## What's in here
| Path | Purpose |
|---|---|
| `SQL-Handbook-Vol1.pdf`, `SQL-Handbook-Vol2.pdf` | The whole book in two PDFs (each under 600 pages), with a cover and a table of contents. Vol 1: Ch 1–11. Vol 2: Ch 12–20. |
| `interview/INTERVIEW.md` + `interview/SQL-Interview-Edition.pdf` | **Interview Edition**: the whole book in ~51 pages for fast revision — per chapter must-know facts, key tables, likely questions with short answers, gotchas, the Ch 18–19 pattern tables with 19 representative problems, plus a top-25 question list and a last-minute checklist. |
| `capstones/CAPSTONES.md` + `capstones/SQL-Capstone-Projects.pdf` | **Capstone Projects**: 40 build-it-yourself design briefs, beginner to advanced (personal library and movie ratings → e-commerce checkout, SKIP LOCKED queues, row-level security, JSONB → serializable ledger, partitioning, Citus, CDC/outbox, zero-downtime migrations, PITR, failover), each with requirements, architecture, an ER diagram, milestones, deep dives and interview questions, all on PostgreSQL 14 with newer features marked. |
| `chapters/NN-name/README.md` + `ChapterNN-Notes.pdf` | One chapter (Markdown source and its PDF). |
| `chapters/NN-name/code/` | Runnable examples: `00_schema.sql` (the chapter's data), numbered `NN_topic.sql` files, `*.sqlite.sql` files for SQLite, and Python, Java or shell demos. |
| `chapters/NN-name/img/` | Diagrams from the courses and charts made by the chapter's scripts. |
| `tools/` | `regen_pdfs.sh` (rebuild all PDFs) and `md2pdf.mjs` (Markdown → PDF). See [tools/README.md](tools/README.md). |

## Chapters
| # | Chapter | In one line | PDF |
|---|---|---|---|
| **Part I — Querying basics** | | | |
| 01 | [Databases and the Relational Model](chapters/01-databases-and-the-relational-model/README.md) | Why databases replaced files, what a DBMS does and its parts, architectures, relations and keys, relational vs NoSQL, SQL's sublanguages, the private server and the sample database | `Chapter01-Notes.pdf` |
| 02 | [SELECT Basics](chapters/02-select-basics/README.md) | Choosing columns, `WHERE`, operators, `BETWEEN`/`IN`/`LIKE`/regex, NULL and three-valued logic, `DISTINCT`, `ORDER BY`, `LIMIT`/`OFFSET`/`FETCH`, the logical processing order | `Chapter02-Notes.pdf` |
| 03 | [Functions and Expressions](chapters/03-functions-and-expressions/README.md) | Types and casts, integer division, number, string and date/time functions, time zones, `CASE`, `COALESCE`, `NULLIF`, `GREATEST`/`LEAST`, worked exercises | `Chapter03-Notes.pdf` |
| 04 | [Aggregation](chapters/04-aggregation/README.md) | Aggregate functions and NULLs, `GROUP BY`, `HAVING`, conditional aggregation with `FILTER` and `CASE`, query order, `GROUPING SETS`/`ROLLUP`/`CUBE` | `Chapter04-Notes.pdf` |
| **Part II — Combining and advanced queries** | | | |
| 05 | [Joins](chapters/05-joins/README.md) | Cartesian product, inner, left, right, full and self joins, semi- and anti-joins, fan-out and other pitfalls, `USING`/`NATURAL`, multi-table joins | `Chapter05-Notes.pdf` |
| 06 | [Subqueries and Set Operations](chapters/06-subqueries-and-set-operations/README.md) | Scalar, `IN`, correlated and `EXISTS` subqueries, `ANY`/`ALL`, derived tables, `LATERAL`, `UNION`, `INTERSECT`, `EXCEPT`, choosing the right tool | `Chapter06-Notes.pdf` |
| 07 | [CTEs, Views and Recursion](chapters/07-ctes-views-and-recursion/README.md) | `WITH`, data-modifying CTEs, recursive CTEs for series, trees and graphs with cycles, `generate_series`, views and materialized views | `Chapter07-Notes.pdf` |
| 08 | [Window Functions](chapters/08-window-functions/README.md) | `OVER`, `PARTITION BY`, frames (`ROWS`/`RANGE`/`GROUPS`), ranking, `lag`/`lead`/`first_value`, running totals, top-N per group, gaps and islands | `Chapter08-Notes.pdf` |
| **Part III — Building and designing** | | | |
| 09 | [DDL, DML and Constraints](chapters/09-ddl-dml-and-constraints/README.md) | Data types, `CREATE`, keys and constraints, identity, sequences and UUIDs, `ALTER`, `DROP`/`TRUNCATE`, `INSERT`/`UPDATE`/`DELETE`, upsert and `MERGE`, backup and restore | `Chapter09-Notes.pdf` |
| 10 | [Data Modeling and ER Diagrams](chapters/10-data-modeling-and-er-diagrams/README.md) | Conceptual, logical and physical models, the ER model and notations, attributes, cardinality and participation, weak entities, keys, integrity, ER → tables | `Chapter10-Notes.pdf` |
| 11 | [Normalization](chapters/11-normalization/README.md) | Functional dependencies, Armstrong's rules, closures and candidate keys, minimal cover, anomalies, 1NF → 5NF, lossless decomposition, exercises, denormalization | `Chapter11-Notes.pdf` |
| **Part IV — Performance and correctness** | | | |
| 12 | [Indexes, Storage and Query Performance](chapters/12-indexes-storage-and-query-performance/README.md) | Pages and heaps, compression, B+ trees, hash indexes, Bloom filters, GIN/GiST/BRIN, reading `EXPLAIN`, composite/covering/partial indexes, anti-patterns, keyset paging, join algorithms, statistics | `Chapter12-Notes.pdf` |
| 13 | [Transactions and Concurrency](chapters/13-transactions-and-concurrency/README.md) | ACID, isolation anomalies and levels shown on two live sessions, lost updates, write skew and SSI, locks and 2PL, deadlocks, `SKIP LOCKED`, timestamp ordering, MVCC | `Chapter13-Notes.pdf` |
| 14 | [Advanced SQL and Using SQL from Code](chapters/14-advanced-sql-and-using-sql-from-code/README.md) | Functions, procedures, triggers, JSONB, arrays, full-text search, pivots, roles and row-level security, SQL injection, psycopg, sqlite3, JDBC, pooling, ORMs | `Chapter14-Notes.pdf` |
| **Part V — Scale and beyond** | | | |
| 15 | [Scaling Databases](chapters/15-scaling-databases/README.md) | Partitioning (range, list, hash), sharding and consistent hashing, distributed queries, replication, consistency models, CAP/PACELC, consensus, 2PC, recovery, high availability | `Chapter15-Notes.pdf` |
| 16 | [Beyond Relational](chapters/16-beyond-relational/README.md) | Why NoSQL, key-value, document, wide-column and graph stores, in-memory, NewSQL, time-series and spatial databases, MapReduce, Hadoop, Spark and streaming, choosing a database | `Chapter16-Notes.pdf` |
| 17 | [Schema Design Case Studies](chapters/17-schema-design-case-studies/README.md) | A design template applied to thirteen systems (hospital, hotel, bank, Instagram, e-commerce, food delivery, library, tickets, warehouse, Quora, TikTok, Airbnb, Snapchat), four of them runnable | `Chapter17-Notes.pdf` |
| **Part VI — Interviews and revision** | | | |
| 18 | [SQL Interview Problems I](chapters/18-sql-interview-problems-1/README.md) | A five-step method, then 42 warm-up to medium problems: filtering, aggregation, joins, anti-joins, conditional aggregation, dates, ratios, first windows | `Chapter18-Notes.pdf` |
| 19 | [SQL Interview Problems II](chapters/19-sql-interview-problems-2/README.md) | 41 medium and hard problems: Nth highest and top-N, running totals, gaps and islands, static and dynamic pivots, relational division, retention, rates | `Chapter19-Notes.pdf` |
| 20 | [Cheat Sheets, Interview Q&A and Glossary](chapters/20-cheatsheet-and-glossary/README.md) | One sheet per area (behaviours machine-checked), decision tables, a five-database dialect table, the pattern catalogue, 50 interview Q&As, a 298-term glossary | `Chapter20-Notes.pdf` |

Each chapter's PDF sits next to its README: `chapters/NN-name/ChapterNN-Notes.pdf`.

## Colour code (in the PDFs)
Section headings are coloured by their role, so you can scan a page for the part you need:

| Colour | Headings |
|---|---|
| Green | **What it is**, analogies, overviews |
| Red / coral | **The problem it solves** |
| Blue | **How it works**, walkthroughs |
| Amber | **Trade-offs**, comparisons, **When not to use it** |
| Bright red | **Common mistakes**, pitfalls |
| Violet | **Real-world examples** |
| Pink | **In the interview**, 30-second answers (boxed), follow-ups |
| Teal | **Self-check** |
| Yellow | **TL;DR** (boxed) |

SQL and other code blocks are syntax-highlighted, query output appears in plain blocks exactly as `psql` printed it, and Mermaid diagrams (including `erDiagram` schemas) are rendered as images.

## Running the code
Run everything from the notes root (`system_design_notes/`). No example needs network access, and every program finishes in seconds (Chapter 12's schema takes about 5 seconds to generate its million rows).

```bash
# every SQL file in the book: prints OK / FAIL / SKIP per file
bash tools_run_sql.sh sql

# only some chapters (the second argument is a glob on the chapter folder names)
bash tools_run_sql.sh sql '12-*'
bash tools_run_sql.sh sql '0[4-6]-*'

# one file, with its output, against a database you created
bash tools_pg.sh psql -d company -f sql/chapters/02-select-basics/code/02_where.sql
```
- **What the runner does:** for each `code/NN_*.sql` file it drops and recreates a database named after the chapter (`ch_02`, `ch_12`, …) in `UTF8`, loads that chapter's `00_schema.sql`, runs the file with `ON_ERROR_STOP`, and prints `OK` or `FAIL` with the first error lines. Files named `*.sqlite.sql` run in `sqlite3 :memory:` instead. Files containing `tools_run: skip` are reported as SKIP. Running without a glob resets every chapter's database. At the last full run: 211 OK, 0 FAIL.
- **Python demos** use the shared virtual environment `.venv-books/` in the notes root (Python 3.14 with psycopg 3.3 and matplotlib). The pure-Python ones (`bloom_filter_sim.py`, `compression_demo.py`, `fd_tools.py`, `sqlite3_demo.py`, the Chapter 15 simulations) also run with any `python3`. Run them from where each chapter says, for example:
  ```bash
  cd sql/chapters/13-transactions-and-concurrency/code
  ../../../../.venv-books/bin/python concurrency_demo.py            # two live sessions + a lock monitor
  cd -   # back to the notes root
  .venv-books/bin/python sql/chapters/14-advanced-sql-and-using-sql-from-code/code/psycopg_demo.py
  python3 sql/chapters/14-advanced-sql-and-using-sql-from-code/code/sqlite3_demo.py
  ```
  Other demos: `n_plus_one.py` (Ch 12), `consistent_hashing.py`, `quorum.py`, `leader_election.py`, `two_phase_commit.py` and `wal_recovery.py` (Ch 15), `mapreduce_word_count.py` (Ch 16).
- **Java (JDBC):** Chapter 14's `JdbcDemo.java` runs in source-file mode with the PostgreSQL JDBC driver and the `junixsocket` jars (for the Unix socket) from `.java-books/lib/` (JDK 25 or later):
  ```bash
  java --enable-native-access=ALL-UNNAMED -cp '.java-books/lib/*' \
       sql/chapters/14-advanced-sql-and-using-sql-from-code/code/JdbcDemo.java
  ```
- **Backup and restore:** `bash sql/chapters/09-ddl-dml-and-constraints/code/12_backup_restore.sh` runs `pg_dump` and `pg_restore` on temporary databases and removes them when it ends.

Timings in the outputs (`EXPLAIN ANALYZE` times, the N+1 demo, pooling) depend on the machine and on what is cached; buffer counts and plan shapes are the stable things to compare. Transaction IDs and some sampled statistics also change from run to run.

## Rebuilding the PDFs
Requires Node 18+ and Google Chrome (the path is set in `tools/md2pdf.mjs`).

```bash
# from sql/
bash tools/regen_pdfs.sh      # every ChapterNN-Notes.pdf + SQL-Handbook-Vol1.pdf and -Vol2.pdf

# one chapter only
node tools/md2pdf.mjs chapters/12-indexes-storage-and-query-performance/Chapter12-Notes.pdf chapters/12-indexes-storage-and-query-performance/README.md
```
`md2pdf.mjs` reports any Mermaid errors or broken images.

## Sources
- **Courses:** DesignGurus, *Grokking SQL for Tech Interviews*, *Grokking Relational Database Design and Modeling for Software Engineers* and *Grokking Database Fundamentals for Tech Interviews*. Their concepts, examples, problems and exercises are covered in my own words, with my own sample data and solutions; no course text is reproduced. Course diagrams are marked "(course diagram)" in their captions.
- **Books:** Silberschatz, Korth and Sudarshan, *Database System Concepts* (storage, indexing, transactions, recovery); Bill Karwin, *SQL Antipatterns*; Markus Winand, *Use The Index, Luke* (use-the-index-luke.com); Martin Kleppmann, *Designing Data-Intensive Applications* (replication, partitioning, consistency, batch processing).
- **Documentation:** the PostgreSQL 14 manual (and release notes for later versions, which are marked "PostgreSQL 15+", "18+" and so on); the MySQL 8, SQL Server, SQLite and Oracle documentation for the dialect tables; the psycopg 3 and PostgreSQL JDBC driver docs.
- **Web:** used to check facts and version details (which feature arrived in which release, default settings, how named products work).

## Companion books
| Book | Read it for |
|---|---|
| [DDIA notes](../ddia/README.md) | *Designing Data-Intensive Applications* chapter by chapter: the theory behind Ch 13, 15 and 16 (replication, partitioning, transactions, consistency and consensus, batch and stream processing) |
| [System Design Patterns](../system-design-patterns/README.md) | Patterns for whole systems (caching, queues, sharding, idempotency) built on the databases in this book |
| [Java Handbook](../java/README.md) | The language behind the JDBC examples in Ch 14, with collections, concurrency and testing |
| [Python Handbook](../python/README.md) | The language behind the psycopg and sqlite3 examples, and the simulations in Ch 12, 13 and 15 |
| [LLD Handbook](../lld/README.md) | Object-oriented design, design patterns and machine-coding case studies (Java 21), the class-level counterpart of the schema designs in Ch 10 and 17 |
