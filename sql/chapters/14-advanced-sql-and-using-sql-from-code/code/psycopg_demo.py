"""Chapter 14 - using PostgreSQL from Python with psycopg 3.

Run (from the repo root):
    bash tools_pg.sh start
    .venv-books/bin/python sql/chapters/14-advanced-sql-and-using-sql-from-code/code/psycopg_demo.py

Creates a fresh database ch_14_code on the book's private server (port 5544, socket /tmp)
and loads 00_schema.sql into it.
"""
import pathlib
import queue
from contextlib import contextmanager

import psycopg
from psycopg import sql
from psycopg.rows import dict_row

HERE = pathlib.Path(__file__).parent
SERVER = "host=/tmp port=5544 user=postgres"
DSN = SERVER + " dbname=ch_14_code"


def fresh_database():
    # CREATE DATABASE cannot run inside a transaction, so use autocommit for it
    with psycopg.connect(SERVER + " dbname=postgres", autocommit=True) as admin:
        admin.execute("DROP DATABASE IF EXISTS ch_14_code")
        admin.execute("CREATE DATABASE ch_14_code ENCODING 'UTF8' TEMPLATE template0")
    with psycopg.connect(DSN) as conn:
        conn.execute((HERE / "00_schema.sql").read_text())   # no parameters -> many statements allowed


def section(title):
    print(f"\n== {title} ==")


def basics(conn):
    section("1. query, fetch, parameters")
    cur = conn.execute("SELECT count(*) FROM customers")
    print("customers:", cur.fetchone()[0])

    # %s placeholders: psycopg sends the values separately from the SQL text
    cur = conn.execute(
        "SELECT first_name, city FROM customers WHERE country = %s AND signup_date >= %s ORDER BY customer_id",
        ("India", "2025-01-01"),
    )
    for row in cur.fetchall():
        print("  ", row)

    # named placeholders + rows as dicts
    with conn.cursor(row_factory=dict_row) as cur:
        cur.execute("SELECT name, unit_price FROM products WHERE unit_price > %(min)s ORDER BY unit_price DESC",
                    {"min": 300})
        print("dict rows:", cur.fetchall())


def injection(conn):
    section("2. SQL injection: string building vs parameters")
    evil = "nobody' OR '1'='1"
    bad_sql = f"SELECT count(*) FROM customers WHERE last_name = '{evil}'"      # NEVER do this
    print("built SQL :", bad_sql)
    print("f-string  :", conn.execute(bad_sql).fetchone()[0], "rows")
    print("parameter :", conn.execute("SELECT count(*) FROM customers WHERE last_name = %s",
                                      (evil,)).fetchone()[0], "rows")
    # identifiers (table/column names) are composed with psycopg.sql, never with %s
    col = "city"
    q = sql.SQL("SELECT {} FROM customers WHERE customer_id = %s").format(sql.Identifier(col))
    print("composed  :", q.as_string(conn), "->", conn.execute(q, (3,)).fetchone()[0])


def transactions(conn):
    section("3. transactions")
    # conn.transaction(): BEGIN, then COMMIT when the block ends normally or ROLLBACK if it raises
    try:
        with conn.transaction():
            conn.execute("UPDATE products SET unit_price = unit_price * 0.9 WHERE category = 'Furniture'")
            conn.execute("UPDATE products SET unit_price = -1 WHERE product_id = 2")   # CHECK fails
    except psycopg.errors.CheckViolation as e:
        print("rolled back:", e.diag.message_primary)
    price = conn.execute("SELECT unit_price FROM products WHERE product_id = 5").fetchone()[0]
    print("desk price after rollback:", price)

    with conn.transaction():
        conn.execute("UPDATE products SET unit_price = 479.00 WHERE product_id = 5")
    print("desk price after commit:", conn.execute(
        "SELECT unit_price FROM products WHERE product_id = 5").fetchone()[0])


def batches(conn):
    section("4. batch insert, COPY, streaming")
    rows = [(20 + i, f"Batch{i}", "Buyer", None, "Pune", "India", "2025-07-01") for i in range(3)]
    with conn.transaction(), conn.cursor() as cur:
        cur.executemany("INSERT INTO customers VALUES (%s, %s, %s, %s, %s, %s, %s)", rows)
        print("executemany rowcount:", cur.rowcount)

    # COPY is the fastest bulk path (one stream instead of one statement per row)
    with conn.transaction(), conn.cursor() as cur:
        with cur.copy("COPY customers (customer_id, first_name, last_name, country, signup_date) FROM STDIN") as cp:
            for i in range(30, 33):
                cp.write_row((i, f"Copy{i}", "Buyer", "India", "2025-07-02"))
    print("customers now:", conn.execute("SELECT count(*) FROM customers").fetchone()[0])

    # a named (server-side) cursor streams a big result in chunks instead of loading it all
    with conn.transaction(), conn.cursor(name="big_scan") as cur:
        cur.itersize = 4                                    # rows fetched per round trip
        cur.execute("SELECT g FROM generate_series(1, 10) g")
        print("streamed:", [r[0] for r in cur])


class TinyPool:
    """Teaching-sized connection pool: open N connections once, lend them out, take them back."""

    def __init__(self, dsn, size):
        self._idle = queue.Queue()
        for _ in range(size):
            self._idle.put(psycopg.connect(dsn, autocommit=True))

    @contextmanager
    def connection(self):
        conn = self._idle.get()          # blocks if every connection is lent out
        try:
            yield conn
        finally:
            self._idle.put(conn)         # return it instead of closing it

    def close(self):
        while not self._idle.empty():
            self._idle.get().close()


def pooling():
    section("5. connection pooling")
    pids = set()
    for _ in range(20):                                   # connect per request
        with psycopg.connect(DSN) as c:
            pids.add(c.execute("SELECT pg_backend_pid()").fetchone()[0])
    print("no pool : 20 requests used", len(pids), "server processes")

    pool, pids = TinyPool(DSN, size=2), set()
    for _ in range(20):
        with pool.connection() as c:
            pids.add(c.execute("SELECT pg_backend_pid()").fetchone()[0])
    pool.close()
    print("pool(2) : 20 requests used", len(pids), "server processes")


def main():
    fresh_database()
    # autocommit=True: each statement commits on its own, and conn.transaction() blocks are
    # real BEGIN ... COMMIT/ROLLBACK units (without it, psycopg opens a transaction implicitly
    # and conn.transaction() becomes a SAVEPOINT inside it)
    with psycopg.connect(DSN, autocommit=True) as conn:
        print("connected to", conn.info.dbname, "on port", conn.info.port,
              "| server", conn.info.server_version // 10000)
        basics(conn)
        injection(conn)
        transactions(conn)
        batches(conn)
    pooling()


if __name__ == "__main__":
    main()
