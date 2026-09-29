"""Hotel reservation: the last-room race and the overbooking limit.

Part 1 races 20 guests for the last 3 rooms on a 3-night stay (SQLite, one
connection per thread) twice:
  - naive: read the counter, decide in Python, write the new value back
  - safe : one conditional UPDATE per night inside a transaction; if any night
           is full, roll back all nights
Part 2 picks an overbooking limit from a no-show rate with the
"critical ratio" rule used in revenue management.

Run:  python hotel_inventory.py
"""
from __future__ import annotations

import os
import sqlite3
import tempfile
import threading
import time
from math import comb

NIGHTS = ["2026-06-10", "2026-06-11", "2026-06-12"]


def fresh_db(path: str) -> None:
    con = sqlite3.connect(path)
    con.executescript("""
        DROP TABLE IF EXISTS room_inventory;
        CREATE TABLE room_inventory (
            room_type_id INTEGER, night TEXT, total INTEGER, reserved INTEGER,
            PRIMARY KEY (room_type_id, night));
    """)
    con.executemany("INSERT INTO room_inventory VALUES (7, ?, 40, 37)", [(n,) for n in NIGHTS])
    con.commit()
    con.close()


def book_naive(path: str, results: list) -> None:
    con = sqlite3.connect(path, timeout=10, isolation_level=None)
    rows = con.execute("SELECT night, total, reserved FROM room_inventory "
                       "WHERE room_type_id = 7 ORDER BY night").fetchall()
    if all(r < t for _, t, r in rows):            # "looks free" ...
        time.sleep(0.01)                          # ... app does other work (pricing, etc.)
        for night, _, r in rows:                  # writes back a value computed earlier
            con.execute("UPDATE room_inventory SET reserved = ? WHERE room_type_id = 7 "
                        "AND night = ?", (r + 1, night))
        results.append("booked")
    else:
        results.append("sold out")
    con.close()


def book_safe(path: str, results: list) -> None:
    con = sqlite3.connect(path, timeout=10, isolation_level=None)
    time.sleep(0.01)
    con.execute("BEGIN IMMEDIATE")                # take the write lock up front
    ok = True
    for night in NIGHTS:                          # same order for everyone: no deadlocks
        cur = con.execute("UPDATE room_inventory SET reserved = reserved + 1 "
                          "WHERE room_type_id = 7 AND night = ? AND reserved < total",
                          (night,))
        if cur.rowcount != 1:                     # this night is full
            ok = False
            break
    con.execute("COMMIT" if ok else "ROLLBACK")
    results.append("booked" if ok else "sold out")
    con.close()


def race(fn, path: str) -> tuple[int, list]:
    fresh_db(path)
    results: list = []
    ts = [threading.Thread(target=fn, args=(path, results)) for _ in range(20)]
    for t in ts:
        t.start()
    for t in ts:
        t.join()
    con = sqlite3.connect(path)
    reserved = [r for (r,) in con.execute(
        "SELECT reserved FROM room_inventory ORDER BY night")]
    con.close()
    return results.count("booked"), reserved


def overbooking_limit(capacity: int, show_prob: float, price: int, walk_cost: int):
    """Largest number of bookings B such that P(shows > capacity) stays below
    price / (price + walk_cost): past that point, one more booking costs more
    in expected walked guests than it earns."""
    ratio = price / (price + walk_cost)
    best = capacity
    for b in range(capacity, capacity * 2):
        p_over = sum(comb(b, k) * show_prob**k * (1 - show_prob)**(b - k)
                     for k in range(capacity + 1, b + 1))
        if p_over > ratio:
            break
        best = b
    exp_walk = sum(comb(best, k) * show_prob**k * (1 - show_prob)**(best - k) * (k - capacity)
                   for k in range(capacity + 1, best + 1))
    return ratio, best, exp_walk


def main() -> None:
    path = os.path.join(tempfile.mkdtemp(), "hotel.db")
    print("=== Part 1: 20 guests, 3 rooms left on each of 3 nights ===")
    n, res = race(book_naive, path)
    print(f"naive read-then-write : {n:2d} bookings accepted, reserved per night = {res} (total 40)")
    assert n > 3, "the race should oversell (lost updates)"
    n, res = race(book_safe, path)
    print(f"conditional UPDATE    : {n:2d} bookings accepted, reserved per night = {res} (total 40)")
    assert n == 3 and res == [40, 40, 40]

    print("\n=== Part 2: how far to overbook a 200-room hotel ===")
    for show, price, walk in [(0.92, 150, 400), (0.92, 150, 1200), (0.97, 150, 400)]:
        ratio, b, ew = overbooking_limit(200, show, price, walk)
        print(f"show rate {show:.0%}, room ${price}, walk cost ${walk:>4}: "
              f"threshold {ratio:.3f} -> accept up to {b} bookings "
              f"(+{b-200}, {100*(b-200)/200:.1f}%), expected walked guests {ew:.2f}")
    print("\nall asserts passed")


if __name__ == "__main__":
    main()
