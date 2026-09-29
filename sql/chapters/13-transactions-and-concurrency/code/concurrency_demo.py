"""Chapter 13 — two real sessions (A and B) interleaved on the private PostgreSQL (port 5544).

Run:  ../../../../.venv-books/bin/python concurrency_demo.py [scenario ...]
      (no argument = every scenario; names: mvcc dirty nonrepeatable phantom lostupdate
       writeskew deadlock skiplocked)

Each scenario starts from a fresh copy of 00_schema.sql in database sql13_conc.
A statement that blocks is started in a background thread; a third "monitor" connection
confirms it is really waiting on a lock (pg_stat_activity) before the other session continues.
"""
import re
import sys
import threading
import time
from pathlib import Path

import psycopg

HERE = Path(__file__).resolve().parent
BASE = "host=/tmp port=5544 user=postgres"
DB = "sql13_conc"


def fresh_db():
    with psycopg.connect(BASE + " dbname=postgres", autocommit=True) as c:
        c.execute(f"DROP DATABASE IF EXISTS {DB} WITH (FORCE)")
        c.execute(f"CREATE DATABASE {DB} ENCODING 'UTF8' TEMPLATE template0")
    with psycopg.connect(f"{BASE} dbname={DB}", autocommit=True) as c:
        c.execute("SET client_min_messages = warning")
        c.execute((HERE / "00_schema.sql").read_text())


def fmt(rows):
    if len(rows) == 1 and len(rows[0]) == 1:
        return str(rows[0][0])
    return ", ".join("(" + ", ".join(str(v) for v in r) + ")" for r in rows)


class Session:
    names = {}                                   # pid -> "A"/"B" (to print readable lock errors)

    def __init__(self, name):
        self.name = name
        self.conn = psycopg.connect(f"{BASE} dbname={DB}", autocommit=True)  # we send BEGIN/COMMIT ourselves
        self.pid = self.conn.info.backend_pid
        Session.names[self.pid] = name

    def _exec(self, sql):
        try:
            cur = self.conn.execute(sql)
            if cur.description:
                return fmt(cur.fetchall())
            return cur.statusmessage
        except psycopg.Error as e:
            msg = f"ERROR {e.sqlstate}: {e.diag.message_primary}"
            if e.diag.message_detail:
                detail = e.diag.message_detail
                for pid, n in Session.names.items():          # replace process ids with A / B
                    detail = re.sub(rf"\b(process) {pid}\b", rf"\1 <{n}>", detail, flags=re.I)
                detail = re.sub(r"transaction \d+", "transaction <xid>", detail)
                msg += "\n     DETAIL: " + detail.replace("\n", "\n             ")
            return msg

    def run(self, sql):
        print(f"  {self.name}| {sql}  -> {self._exec(sql)}")

    def start(self, sql):
        """Send a statement that we expect to block; returns a handle to finish later."""
        print(f"  {self.name}| {sql}")
        box = {}
        t = threading.Thread(target=lambda: box.setdefault("r", self._exec(sql)))
        t.start()
        return self, t, box

    def close(self):
        self.conn.close()


class Monitor:
    def __init__(self):
        self.conn = psycopg.connect(f"{BASE} dbname={DB}", autocommit=True)

    def wait_until_blocked(self, s, timeout=5.0):
        end = time.time() + timeout
        while time.time() < end:
            row = self.conn.execute(
                "SELECT wait_event_type, wait_event, pg_blocking_pids(pid) "
                "FROM pg_stat_activity WHERE pid = %s", (s.pid,)).fetchone()
            if row and row[0] == "Lock":
                who = ",".join(Session.names.get(p, "?") for p in row[2])
                print(f"     .. {s.name} is WAITING (wait_event={row[1]}, blocked by {who})")
                return
            time.sleep(0.05)
        print(f"     .. {s.name} did not block")

    def lock_rows(self, s):
        rows = self.conn.execute(
            "SELECT locktype, mode, granted FROM pg_locks WHERE pid = %s "
            "AND locktype IN ('transactionid','tuple') ORDER BY granted DESC, locktype", (s.pid,)).fetchall()
        print(f"     .. pg_locks for {s.name}: {fmt(rows)}")

    def query(self, label, sql):
        print(f"  {label}| {sql}  -> {fmt(self.conn.execute(sql).fetchall())}")

    def close(self):
        self.conn.close()


def finish(handle):
    s, t, box = handle
    t.join()
    print(f"     -> ({s.name} resumes) {box['r']}")


def scenario(title):
    def deco(fn):
        def wrapper():
            print(f"\n=== {title} ===")
            fresh_db()
            a, b, m = Session("A"), Session("B"), Monitor()
            try:
                fn(a, b, m)
            finally:
                for x in (a, b, m):
                    x.close()
                Session.names.clear()
        return wrapper
    return deco


# ----------------------------------------------------------------------------- scenarios
@scenario("MVCC: xmin / xmax seen from two sessions")
def mvcc(a, b, m):
    a.run("SELECT xmin, xmax, balance FROM accounts WHERE id = 1")
    a.run("BEGIN")
    a.run("UPDATE accounts SET balance = 450 WHERE id = 1")
    a.run("SELECT txid_current()")
    a.run("SELECT xmin, xmax, balance FROM accounts WHERE id = 1")   # A sees its own new version
    b.run("SELECT xmin, xmax, balance FROM accounts WHERE id = 1")   # B sees the old version, xmax = A
    a.run("COMMIT")
    b.run("SELECT xmin, xmax, balance FROM accounts WHERE id = 1")   # now B sees A's version


@scenario("Dirty read and dirty write: impossible in PostgreSQL")
def dirty(a, b, m):
    a.run("BEGIN")
    a.run("UPDATE accounts SET balance = 0 WHERE id = 1")            # not committed
    b.run("BEGIN ISOLATION LEVEL READ UNCOMMITTED")
    b.run("SHOW transaction_isolation")
    b.run("SELECT balance FROM accounts WHERE id = 1")               # still the committed 500
    h = b.start("UPDATE accounts SET balance = balance + 1 WHERE id = 1")  # dirty write? it must wait
    m.wait_until_blocked(b)
    a.run("ROLLBACK")
    finish(h)
    b.run("COMMIT")
    m.query("M", "SELECT balance FROM accounts WHERE id = 1")


def _read_skew(a, b, level):
    a.run(f"BEGIN ISOLATION LEVEL {level}")
    a.run("SELECT balance FROM accounts WHERE id = 1")
    # B transfers 100 from account 1 to account 2 and commits in between
    b.run("BEGIN")
    b.run("UPDATE accounts SET balance = balance - 100 WHERE id = 1")
    b.run("UPDATE accounts SET balance = balance + 100 WHERE id = 2")
    b.run("COMMIT")
    a.run("SELECT balance FROM accounts WHERE id = 1")               # same row read again
    a.run("SELECT balance FROM accounts WHERE id = 2")
    a.run("COMMIT")


@scenario("Non-repeatable read / read skew: READ COMMITTED")
def nonrepeatable_rc(a, b, m):
    _read_skew(a, b, "READ COMMITTED")


@scenario("Non-repeatable read / read skew: REPEATABLE READ")
def nonrepeatable_rr(a, b, m):
    _read_skew(a, b, "REPEATABLE READ")


def _phantom(a, b, level):
    a.run(f"BEGIN ISOLATION LEVEL {level}")
    a.run("SELECT count(*) FROM accounts WHERE balance >= 100")
    b.run("INSERT INTO accounts VALUES (4, 'dina', 900)")            # autocommit
    a.run("SELECT count(*) FROM accounts WHERE balance >= 100")
    a.run("COMMIT")


@scenario("Phantom read: READ COMMITTED")
def phantom_rc(a, b, m):
    _phantom(a, b, "READ COMMITTED")


@scenario("Phantom read: REPEATABLE READ (PostgreSQL prevents it)")
def phantom_rr(a, b, m):
    _phantom(a, b, "REPEATABLE READ")


@scenario("Lost update 1: read-modify-write in the application (READ COMMITTED)")
def lost_naive(a, b, m):
    a.run("BEGIN")
    a.run("SELECT stock FROM products WHERE id = 1")                 # app reads 10
    b.run("BEGIN")
    b.run("SELECT stock FROM products WHERE id = 1")                 # app reads 10
    a.run("UPDATE products SET stock = 9 WHERE id = 1")              # app computed 10 - 1
    a.run("COMMIT")
    b.run("UPDATE products SET stock = 9 WHERE id = 1")              # app computed 10 - 1
    b.run("COMMIT")
    m.query("M", "SELECT stock FROM products WHERE id = 1")          # two sales, stock only -1


@scenario("Lost update fix 1: atomic UPDATE ... SET stock = stock - 1")
def lost_atomic(a, b, m):
    a.run("BEGIN")
    a.run("UPDATE products SET stock = stock - 1 WHERE id = 1 RETURNING stock")
    b.run("BEGIN")
    h = b.start("UPDATE products SET stock = stock - 1 WHERE id = 1 RETURNING stock")
    m.wait_until_blocked(b)
    m.lock_rows(b)
    a.run("COMMIT")
    finish(h)                                                        # re-reads the committed 9
    b.run("COMMIT")
    m.query("M", "SELECT stock FROM products WHERE id = 1")


@scenario("Lost update fix 2: pessimistic SELECT ... FOR UPDATE")
def lost_for_update(a, b, m):
    a.run("BEGIN")
    a.run("SELECT stock FROM products WHERE id = 1 FOR UPDATE")
    b.run("BEGIN")
    h = b.start("SELECT stock FROM products WHERE id = 1 FOR UPDATE")
    m.wait_until_blocked(b)
    a.run("UPDATE products SET stock = 9 WHERE id = 1")
    a.run("COMMIT")
    finish(h)                                                        # B now reads 9, not 10
    b.run("UPDATE products SET stock = 8 WHERE id = 1")
    b.run("COMMIT")
    m.query("M", "SELECT stock FROM products WHERE id = 1")


@scenario("Lost update fix 3: REPEATABLE READ detects the conflict")
def lost_rr(a, b, m):
    a.run("BEGIN ISOLATION LEVEL REPEATABLE READ")
    a.run("SELECT stock FROM products WHERE id = 1")
    b.run("BEGIN ISOLATION LEVEL REPEATABLE READ")
    b.run("SELECT stock FROM products WHERE id = 1")
    a.run("UPDATE products SET stock = 9 WHERE id = 1")
    a.run("COMMIT")
    b.run("UPDATE products SET stock = 9 WHERE id = 1")              # refused
    b.run("ROLLBACK")
    print("  (B retries the whole transaction)")
    b.run("BEGIN ISOLATION LEVEL REPEATABLE READ")
    b.run("SELECT stock FROM products WHERE id = 1")
    b.run("UPDATE products SET stock = 8 WHERE id = 1")
    b.run("COMMIT")
    m.query("M", "SELECT stock FROM products WHERE id = 1")


@scenario("Lost update fix 4: optimistic locking with a version column (compare-and-set)")
def lost_optimistic(a, b, m):
    a.run("SELECT stock, version FROM products WHERE id = 1")        # (10, 1) — no transaction held open
    b.run("SELECT stock, version FROM products WHERE id = 1")        # (10, 1)
    a.run("UPDATE products SET stock = 9, version = version + 1 WHERE id = 1 AND version = 1")
    b.run("UPDATE products SET stock = 9, version = version + 1 WHERE id = 1 AND version = 1")  # 0 rows
    print("  (B sees UPDATE 0 -> someone changed the row; re-read and retry)")
    b.run("SELECT stock, version FROM products WHERE id = 1")
    b.run("UPDATE products SET stock = 8, version = version + 1 WHERE id = 1 AND version = 2")
    m.query("M", "SELECT stock, version FROM products WHERE id = 1")


def _write_skew(a, b, level):
    q = "SELECT count(*) FROM on_call WHERE shift = 'night' AND on_duty"
    a.run(f"BEGIN ISOLATION LEVEL {level}")
    b.run(f"BEGIN ISOLATION LEVEL {level}")
    a.run(q)                                                         # 2 -> "someone else covers"
    b.run(q)                                                         # 2 -> "someone else covers"
    a.run("UPDATE on_call SET on_duty = false WHERE doctor = 'dr_rao'")
    b.run("UPDATE on_call SET on_duty = false WHERE doctor = 'dr_kim'")
    a.run("COMMIT")
    b.run("COMMIT")


@scenario("Write skew: REPEATABLE READ lets both doctors go off duty")
def writeskew_rr(a, b, m):
    _write_skew(a, b, "REPEATABLE READ")
    m.query("M", "SELECT count(*) FROM on_call WHERE shift = 'night' AND on_duty")


@scenario("Write skew: SERIALIZABLE aborts one of them")
def writeskew_ser(a, b, m):
    _write_skew(a, b, "SERIALIZABLE")
    m.query("M", "SELECT count(*) FROM on_call WHERE shift = 'night' AND on_duty")


@scenario("Write skew fix without SERIALIZABLE: lock the rows you base the decision on")
def writeskew_lock(a, b, m):
    q = "SELECT doctor FROM on_call WHERE shift = 'night' AND on_duty FOR UPDATE"
    a.run("BEGIN")
    b.run("BEGIN")
    a.run(q)
    h = b.start(q)
    m.wait_until_blocked(b)
    a.run("UPDATE on_call SET on_duty = false WHERE doctor = 'dr_rao'")
    a.run("COMMIT")
    finish(h)                                                        # B now sees only dr_kim
    print("  (B's app sees only one doctor left, so it refuses to go off duty)")
    b.run("ROLLBACK")
    m.query("M", "SELECT count(*) FROM on_call WHERE shift = 'night' AND on_duty")


@scenario("Deadlock: PostgreSQL detects it and aborts one transaction")
def deadlock(a, b, m):
    a.run("SET deadlock_timeout = '10s'")   # make B the one that runs the deadlock check (predictable victim)
    b.run("SET deadlock_timeout = '200ms'")
    a.run("BEGIN")
    b.run("BEGIN")
    a.run("UPDATE accounts SET balance = balance - 10 WHERE id = 1")   # A locks row 1
    b.run("UPDATE accounts SET balance = balance - 10 WHERE id = 2")   # B locks row 2
    h = a.start("UPDATE accounts SET balance = balance + 10 WHERE id = 2")  # A waits for B
    m.wait_until_blocked(a)
    b.run("UPDATE accounts SET balance = balance + 10 WHERE id = 1")   # B waits for A -> cycle
    b.run("ROLLBACK")
    finish(h)
    a.run("COMMIT")
    m.query("M", "SELECT id, balance FROM accounts WHERE id IN (1, 2) ORDER BY id")


@scenario("Job queue: FOR UPDATE SKIP LOCKED (and NOWAIT)")
def skiplocked(a, b, m):
    claim = ("SELECT id, payload FROM jobs WHERE status = 'queued' "
             "ORDER BY id LIMIT 2 FOR UPDATE SKIP LOCKED")
    a.run("BEGIN")
    a.run(claim)                                                     # worker A gets 1, 2
    b.run("BEGIN")
    b.run(claim)                                                     # worker B gets 3, 4 at once
    b.run("SELECT id FROM jobs WHERE id = 1 FOR UPDATE NOWAIT")      # fail fast instead of waiting
    b.run("ROLLBACK")
    # the usual one-statement form: claim + mark in a single UPDATE
    b.run("UPDATE jobs SET status = 'running', claimed_by = 'B' WHERE id IN ("
          "SELECT id FROM jobs WHERE status = 'queued' ORDER BY id LIMIT 2 "
          "FOR UPDATE SKIP LOCKED) RETURNING id")
    a.run("UPDATE jobs SET status = 'done', claimed_by = 'A' WHERE id IN (1, 2)")
    a.run("COMMIT")
    m.query("M", "SELECT id, status, claimed_by FROM jobs ORDER BY id")


ALL = {
    "mvcc": [mvcc], "dirty": [dirty], "nonrepeatable": [nonrepeatable_rc, nonrepeatable_rr],
    "phantom": [phantom_rc, phantom_rr],
    "lostupdate": [lost_naive, lost_atomic, lost_for_update, lost_rr, lost_optimistic],
    "writeskew": [writeskew_rr, writeskew_ser, writeskew_lock],
    "deadlock": [deadlock], "skiplocked": [skiplocked],
}

if __name__ == "__main__":
    for name in sys.argv[1:] or list(ALL):
        for fn in ALL[name]:
            fn()
    with psycopg.connect(BASE + " dbname=postgres", autocommit=True) as c:
        c.execute(f"DROP DATABASE IF EXISTS {DB} WITH (FORCE)")
