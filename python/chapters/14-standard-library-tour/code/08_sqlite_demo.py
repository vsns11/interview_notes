"""sqlite3: create, insert with placeholders, query, transactions, Row."""
import sqlite3

if __name__ == "__main__":
    con = sqlite3.connect(":memory:")          # a file path works the same way
    con.row_factory = sqlite3.Row              # rows accessible by column name
    con.execute("""CREATE TABLE expense (
                     id INTEGER PRIMARY KEY,
                     category TEXT NOT NULL,
                     amount REAL NOT NULL CHECK (amount > 0))""")
    rows = [("food", 12.5), ("travel", 40.0), ("food", 7.25), ("books", 18.0)]
    with con:                                  # commits, or rolls back on error
        con.executemany("INSERT INTO expense (category, amount) VALUES (?, ?)", rows)

    cat = "food"                               # never build SQL with f-strings
    total = con.execute("SELECT SUM(amount) AS s FROM expense WHERE category = ?",
                        (cat,)).fetchone()["s"]
    print("food total:", total)
    for r in con.execute("""SELECT category, COUNT(*) AS n, ROUND(SUM(amount), 2) AS s
                            FROM expense GROUP BY category ORDER BY s DESC"""):
        print(f"  {r['category']:<7} n={r['n']} sum={r['s']}")

    try:
        with con:
            con.execute("INSERT INTO expense (category, amount) VALUES ('gift', 5)")
            con.execute("INSERT INTO expense (category, amount) VALUES ('bad', -1)")
    except sqlite3.IntegrityError as e:
        print("rolled back:", e)
    print("rows now:", con.execute("SELECT COUNT(*) FROM expense").fetchone()[0])
    con.close()
