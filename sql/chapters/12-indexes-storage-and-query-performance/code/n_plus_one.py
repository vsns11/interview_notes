"""N+1 queries vs one set-based query, on the book's private PostgreSQL (port 5544).

Creates its own database ch_12_py (dropped and recreated each run).
Run:  ../../../../.venv-books/bin/python n_plus_one.py
Timings are machine-dependent; the query COUNT is the point.
"""
import time
import psycopg

BASE = "host=/tmp port=5544 user=postgres"
DB = "ch_12_py"

with psycopg.connect(BASE + " dbname=postgres", autocommit=True) as c:
    c.execute(f"DROP DATABASE IF EXISTS {DB}")
    c.execute(f"CREATE DATABASE {DB}")

with psycopg.connect(f"{BASE} dbname={DB}", autocommit=True) as conn:
    conn.execute("""
        CREATE TABLE customers (id int PRIMARY KEY, name text NOT NULL);
        CREATE TABLE orders (id int PRIMARY KEY, customer_id int NOT NULL REFERENCES customers,
                             amount numeric(10,2) NOT NULL);
        INSERT INTO customers SELECT g, 'customer ' || g FROM generate_series(1, 5000) g;
        INSERT INTO orders SELECT g, g % 5000 + 1, (g % 400) + 0.99 FROM generate_series(1, 50000) g;
        CREATE INDEX ON orders (customer_id);
        ANALYZE;""")

    def n_plus_one():
        """What a naive ORM loop does: 1 query for the list + 1 query per customer."""
        queries = 1
        customers = conn.execute("SELECT id, name FROM customers ORDER BY id LIMIT 500").fetchall()
        totals = {}
        for cid, _name in customers:
            row = conn.execute("SELECT coalesce(sum(amount), 0) FROM orders WHERE customer_id = %s",
                               (cid,)).fetchone()
            queries += 1
            totals[cid] = row[0]
        return queries, totals

    def batched_in_list():
        """Two queries: the list, then all children at once with = ANY(array)."""
        customers = conn.execute("SELECT id, name FROM customers ORDER BY id LIMIT 500").fetchall()
        ids = [cid for cid, _ in customers]
        rows = conn.execute("""SELECT customer_id, sum(amount) FROM orders
                               WHERE customer_id = ANY(%s) GROUP BY customer_id""", (ids,)).fetchall()
        totals = {cid: 0 for cid in ids}
        totals.update(dict(rows))
        return 2, totals

    def single_join():
        """One query: let the database join and aggregate."""
        rows = conn.execute("""SELECT c.id, coalesce(sum(o.amount), 0)
                               FROM (SELECT id FROM customers ORDER BY id LIMIT 500) c
                               LEFT JOIN orders o ON o.customer_id = c.id
                               GROUP BY c.id""").fetchall()
        return 1, dict(rows)

    results = {}
    for fn in (n_plus_one, batched_in_list, single_join):
        fn()                                   # warm-up (caches, plans)
        t0 = time.perf_counter()
        queries, totals = fn()
        ms = (time.perf_counter() - t0) * 1000
        results[fn.__name__] = totals
        # Estimated extra cost if each round trip crossed a network with 1 ms RTT:
        print(f"{fn.__name__:16s} queries={queries:4d}  local time={ms:7.1f} ms"
              f"  + network @1ms RTT ~ {queries * 1.0:6.0f} ms")

    same = results["n_plus_one"] == results["batched_in_list"] == results["single_join"]
    print("all three return the same totals:", same)
