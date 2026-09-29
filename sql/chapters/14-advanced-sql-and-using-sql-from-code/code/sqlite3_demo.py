"""Chapter 14 - the same ideas with Python's built-in sqlite3 module (no server needed).

Run:  python3 sql/chapters/14-advanced-sql-and-using-sql-from-code/code/sqlite3_demo.py
"""
import sqlite3

conn = sqlite3.connect(":memory:")          # a file path gives a persistent database
conn.row_factory = sqlite3.Row              # rows readable by column name
conn.execute("PRAGMA foreign_keys = ON")    # SQLite leaves FK checks off unless asked

conn.executescript("""
    CREATE TABLE products (
        product_id INTEGER PRIMARY KEY,     -- alias for SQLite's rowid
        name       TEXT NOT NULL UNIQUE,
        price      REAL NOT NULL CHECK (price >= 0),
        specs      TEXT                     -- JSON stored as text
    );
""")

# executemany with ? placeholders; 'with conn' commits on success, rolls back on exception
rows = [("Wireless Mouse", 24.99, '{"brand":"Clicko","wireless":true}'),
        ("USB-C Hub", 49.50, '{"brand":"Nimbus","ports":4}'),
        ("Desk Lamp", 35.00, None)]
with conn:
    conn.executemany("INSERT INTO products (name, price, specs) VALUES (?, ?, ?)", rows)

# named parameters
for r in conn.execute("SELECT product_id, name, price FROM products WHERE price < :max ORDER BY price",
                      {"max": 40}):
    print(dict(r))

# a failing transaction is rolled back as a whole
try:
    with conn:
        conn.execute("UPDATE products SET price = price * 2")
        conn.execute("INSERT INTO products (name, price) VALUES ('USB-C Hub', 10)")   # UNIQUE fails
except sqlite3.IntegrityError as e:
    print("rolled back:", e)
print("hub price:", conn.execute("SELECT price FROM products WHERE name = ?", ("USB-C Hub",)).fetchone()[0])

# injection: the same attack as in the PostgreSQL demo
evil = "x' OR '1'='1"
print("f-string  rows:", len(conn.execute(f"SELECT * FROM products WHERE name = '{evil}'").fetchall()))
print("parameter rows:", len(conn.execute("SELECT * FROM products WHERE name = ?", (evil,)).fetchall()))

# JSON functions are built into modern SQLite (->> needs SQLite 3.38+)
print("sqlite", sqlite3.sqlite_version, "| brands:",
      [r[0] for r in conn.execute("SELECT json_extract(specs, '$.brand') FROM products "
                                  "WHERE specs IS NOT NULL ORDER BY 1")])
conn.close()
