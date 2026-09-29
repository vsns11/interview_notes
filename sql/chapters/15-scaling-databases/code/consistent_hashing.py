"""15.3 Sharding: how many keys move when we add a 5th shard?
Compares naive  hash(key) % N  with a consistent-hash ring (with and without virtual nodes)."""
import bisect
import hashlib
from collections import Counter


def h(s: str) -> int:
    """Stable 32-bit hash (Python's built-in hash() is randomised per process)."""
    return int.from_bytes(hashlib.md5(s.encode()).digest()[:4], "big")


def mod_n(key: str, shards: list[str]) -> str:
    return shards[h(key) % len(shards)]


class Ring:
    def __init__(self, shards: list[str], vnodes: int = 1):
        self.points = sorted((h(f"{s}#{v}"), s) for s in shards for v in range(vnodes))
        self.hashes = [p for p, _ in self.points]

    def lookup(self, key: str) -> str:
        i = bisect.bisect(self.hashes, h(key)) % len(self.points)  # next point clockwise
        return self.points[i][1]


keys = [f"user:{i}" for i in range(100_000)]
old, new = ["s1", "s2", "s3", "s4"], ["s1", "s2", "s3", "s4", "s5"]

def report(name, place_old, place_new):
    before = {k: place_old(k) for k in keys}
    after = {k: place_new(k) for k in keys}
    moved = sum(before[k] != after[k] for k in keys)
    load = Counter(after.values())
    spread = max(load.values()) / min(load.values())
    print(f"{name:<28} moved {moved / len(keys):6.1%}   max/min shard load {spread:4.2f}")

report("hash % N", lambda k: mod_n(k, old), lambda k: mod_n(k, new))
r_old, r_new = Ring(old, 1), Ring(new, 1)
report("ring, 1 point per shard", r_old.lookup, r_new.lookup)
r_old, r_new = Ring(old, 200), Ring(new, 200)
report("ring, 200 vnodes per shard", r_old.lookup, r_new.lookup)
print("ideal fraction to move when going 4 -> 5 shards: 1/5 = 20.0%")
