"""A G-Counter and PN-Counter CRDT: replicas count independently, then merge in any order.

Run:  .venv-books/bin/python system-design-patterns/modules/16-case-studies-infrastructure/code/crdt_counter.py
Standard library only.
"""
import random


class GCounter:
    """Grow-only counter: one slot per replica; merge = element-wise max."""

    def __init__(self, replica: str):
        self.replica, self.slots = replica, {}

    def incr(self, n: int = 1):
        self.slots[self.replica] = self.slots.get(self.replica, 0) + n

    def value(self) -> int:
        return sum(self.slots.values())

    def merge(self, other: "GCounter"):
        for r, v in other.slots.items():
            self.slots[r] = max(self.slots.get(r, 0), v)


class PNCounter:
    """Increments and decrements (like / unlike) = two G-Counters."""

    def __init__(self, replica: str):
        self.p, self.n = GCounter(replica), GCounter(replica)

    def incr(self):
        self.p.incr()

    def decr(self):
        self.n.incr()

    def value(self) -> int:
        return self.p.value() - self.n.value()

    def merge(self, other: "PNCounter"):
        self.p.merge(other.p)
        self.n.merge(other.n)


random.seed(7)
regions = {name: PNCounter(name) for name in ("us", "eu", "ap")}
truth = 0
for _ in range(10_000):                    # users like/unlike, each hitting their nearest region
    r = random.choice(list(regions.values()))
    if random.random() < 0.8:
        r.incr(); truth += 1
    else:
        r.decr(); truth -= 1

print("before sync:", {k: v.value() for k, v in regions.items()}, "true total:", truth)

# gossip in a random order, with a duplicate delivery thrown in -- merge must not care
pairs = [("us", "eu"), ("eu", "ap"), ("ap", "us"), ("us", "eu"), ("eu", "ap")]
random.shuffle(pairs)
for a, b in pairs:
    regions[a].merge(regions[b])
    regions[b].merge(regions[a])
print("after sync :", {k: v.value() for k, v in regions.items()})
assert all(v.value() == truth for v in regions.values())

# why not just "add the other replica's number"? replaying a message double counts:
naive = regions["us"].value() + regions["eu"].value()
print(f"naive 'add the other total' would report {naive} (double counted)")
# ... whereas merging the same state twice changes nothing (idempotent):
before = regions["us"].value(); regions["us"].merge(regions["eu"]); regions["us"].merge(regions["eu"])
assert regions["us"].value() == before
print("merge is commutative, associative and idempotent -> all replicas converge to", truth)
