"""A tiny stock-exchange core: sequencer -> matching engine -> event log.

Teaching goals (Module 17, "Design a Stock Exchange"):
  1. Price-time priority: best price first; at the same price, first come first served.
  2. The sequencer stamps every inbound command with a gap-free sequence number
     and appends it to a log BEFORE the engine sees it.
  3. The engine is single-threaded and deterministic, so replaying the same log
     rebuilds exactly the same book and exactly the same fills (that is how a
     hot standby stays in sync and how a crashed engine recovers).

Prices are integer "ticks" (cents), never floats. Run:  python order_book.py
"""
from __future__ import annotations

import hashlib
import heapq
import json
import random
import time
from collections import deque
from dataclasses import dataclass


@dataclass
class Order:
    oid: int
    trader: str
    side: str          # "B" buy or "S" sell
    price: int | None  # ticks; None = market order
    qty: int
    remaining: int
    active: bool = True


class Sequencer:
    """Gives every command a gap-free number and records it (the input log)."""

    def __init__(self) -> None:
        self.next_seq = 1
        self.log: list[dict] = []

    def submit(self, cmd: dict) -> dict:
        stamped = {"seq": self.next_seq, **cmd}
        self.next_seq += 1
        self.log.append(stamped)          # in real life: fsync / replicate first
        return stamped


class MatchingEngine:
    """One order book for one symbol. Only ever called from one thread."""

    def __init__(self) -> None:
        self.levels = {"B": {}, "S": {}}          # price -> deque[Order] (FIFO)
        self.heaps = {"B": [], "S": []}           # best price on top (bids negated)
        self.orders: dict[int, Order] = {}
        self.events: list[tuple] = []             # output log (fills, acks, ...)
        self.last_seq = 0

    # ---- helpers -------------------------------------------------------
    def _best_price(self, side: str) -> int | None:
        heap, levels = self.heaps[side], self.levels[side]
        while heap:
            p = -heap[0] if side == "B" else heap[0]
            q = levels.get(p)
            while q and not q[0].active:          # lazy removal of cancelled/filled
                q.popleft()
            if q:
                return p
            heapq.heappop(heap)                   # empty level: drop it
            levels.pop(p, None)
        return None

    def _rest(self, o: Order) -> None:
        lv = self.levels[o.side]
        if o.price not in lv:
            lv[o.price] = deque()
            heapq.heappush(self.heaps[o.side], -o.price if o.side == "B" else o.price)
        lv[o.price].append(o)

    # ---- the one entry point -------------------------------------------
    def apply(self, cmd: dict) -> None:
        assert cmd["seq"] == self.last_seq + 1, "gap in the sequence: stop and recover"
        self.last_seq = cmd["seq"]
        if cmd["type"] == "cancel":
            o = self.orders.get(cmd["oid"])
            if o and o.active:
                o.active = False
                self.events.append(("CANCELLED", o.oid, o.remaining))
            else:
                self.events.append(("CANCEL_REJECT", cmd["oid"]))
            return

        o = Order(cmd["oid"], cmd["trader"], cmd["side"], cmd.get("price"),
                  cmd["qty"], cmd["qty"])
        self.orders[o.oid] = o
        self.events.append(("ACK", o.oid))
        other = "S" if o.side == "B" else "B"
        while o.remaining > 0:
            best = self._best_price(other)
            if best is None:
                break
            crosses = (o.price is None or
                       (o.side == "B" and best <= o.price) or
                       (o.side == "S" and best >= o.price))
            if not crosses:
                break
            maker = self.levels[other][best][0]   # oldest order at the best price
            qty = min(o.remaining, maker.remaining)
            o.remaining -= qty
            maker.remaining -= qty
            # trade price = the resting (maker) order's price
            self.events.append(("FILL", o.oid, maker.oid, best, qty))
            if maker.remaining == 0:
                maker.active = False
        if o.remaining == 0:
            o.active = False
        elif o.price is None:                     # market remainder never rests
            o.active = False
            self.events.append(("EXPIRED", o.oid, o.remaining))
        else:
            self._rest(o)

    def depth(self, side: str, n: int = 3) -> list[tuple[int, int]]:
        """Level-2 market data: (price, total quantity) for the best n levels."""
        rows = []
        for p, q in self.levels[side].items():
            total = sum(x.remaining for x in q if x.active)
            if total:
                rows.append((p, total))
        rows.sort(reverse=(side == "B"))
        return rows[:n]


def digest(events: list[tuple]) -> str:
    return hashlib.sha256(json.dumps(events).encode()).hexdigest()[:16]


def main() -> None:
    seq, eng = Sequencer(), MatchingEngine()

    def send(**cmd):
        eng.apply(seq.submit(cmd))

    print("=== 1. Build a small book (prices in cents) ===")
    send(type="new", oid=1, trader="ana",  side="S", price=10_050, qty=100)
    send(type="new", oid=2, trader="ben",  side="S", price=10_050, qty=50)   # same price, later
    send(type="new", oid=3, trader="cai",  side="S", price=10_100, qty=200)
    send(type="new", oid=4, trader="dee",  side="B", price=10_000, qty=300)
    send(type="new", oid=5, trader="eli",  side="B", price=9_950,  qty=100)
    print("asks:", eng.depth("S"))
    print("bids:", eng.depth("B"))

    print("\n=== 2. Aggressive buy 180 @ 101.00 walks the ask side ===")
    before = len(eng.events)
    send(type="new", oid=6, trader="fay", side="B", price=10_100, qty=180)
    for e in eng.events[before:]:
        print(" ", e)
    fills = [e for e in eng.events[before:] if e[0] == "FILL"]
    assert [f[2] for f in fills] == [1, 2, 3], "time priority: ana(1) before ben(2), then cai(3)"
    assert sum(f[4] for f in fills) == 180
    print("asks:", eng.depth("S"))

    print("\n=== 3. Cancel, then a market sell ===")
    send(type="cancel", oid=5)
    before = len(eng.events)
    send(type="new", oid=7, trader="gus", side="S", price=None, qty=400)
    for e in eng.events[before:]:
        print(" ", e)
    assert eng.events[-1] == ("EXPIRED", 7, 100), "only dee's 300 was left to hit"

    print("\n=== 4. Determinism: replay the sequencer's log into a fresh engine ===")
    rng = random.Random(42)
    oid = 100
    for _ in range(20_000):                      # extra random traffic
        oid += 1
        if rng.random() < 0.15 and oid > 110:
            send(type="cancel", oid=rng.randrange(101, oid))
        else:
            send(type="new", oid=oid, trader="t%d" % rng.randrange(50),
                 side=rng.choice("BS"), price=10_000 + rng.randrange(-20, 21) * 5,
                 qty=rng.randrange(1, 10) * 10)
    replica = MatchingEngine()
    for cmd in seq.log:
        replica.apply(cmd)
    print("commands in log :", len(seq.log))
    print("primary digest  :", digest(eng.events))
    print("replica digest  :", digest(replica.events))
    assert digest(eng.events) == digest(replica.events)
    assert eng.depth("B", 5) == replica.depth("B", 5)

    print("\n=== 5. Rough single-thread throughput (this laptop, pure Python) ===")
    s2, e2 = Sequencer(), MatchingEngine()
    cmds = [s2.submit({"type": "new", "oid": i, "trader": "x", "side": rng.choice("BS"),
                       "price": 10_000 + rng.randrange(-20, 21) * 5,
                       "qty": rng.randrange(1, 10) * 10}) for i in range(200_000)]
    t0 = time.perf_counter()
    for c in cmds:
        e2.apply(c)
    dt = time.perf_counter() - t0
    n_fills = sum(1 for e in e2.events if e[0] == "FILL")
    print(f"200,000 orders, {n_fills:,} fills in {dt:.2f}s  (~{200_000/dt:,.0f} orders/s)")
    print("\nall asserts passed")


if __name__ == "__main__":
    main()
