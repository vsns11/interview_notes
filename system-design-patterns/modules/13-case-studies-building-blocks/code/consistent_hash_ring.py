"""Consistent-hash ring with virtual nodes and an N=3 preference list (Dynamo-style KV store).

Run: .venv-books/bin/python consistent_hash_ring.py
"""
import bisect
import hashlib
import statistics


def h(s):
    return int.from_bytes(hashlib.md5(s.encode()).digest()[:8], "big")


class Ring:
    def __init__(self, nodes, vnodes):
        self.vnodes = vnodes
        self.points = []                  # sorted list of (hash, node)
        for n in nodes:
            self.add(n)

    def add(self, node):
        for v in range(self.vnodes):
            bisect.insort(self.points, (h(f"{node}#{v}"), node))

    def preference_list(self, key, n=3):
        """First n DISTINCT physical nodes clockwise from the key's hash."""
        i = bisect.bisect(self.points, (h(key), ""))
        out = []
        for j in range(len(self.points)):
            node = self.points[(i + j) % len(self.points)][1]
            if node not in out:
                out.append(node)
                if len(out) == n:
                    break
        return out

    def owner(self, key):
        return self.preference_list(key, 1)[0]


if __name__ == "__main__":
    keys = [f"user:{i}" for i in range(100_000)]
    nodes = [f"node{i}" for i in range(10)]

    for v in (1, 16, 256):
        ring = Ring(nodes, v)
        counts = {n: 0 for n in nodes}
        for k in keys:
            counts[ring.owner(k)] += 1
        c = list(counts.values())
        print(f"vnodes={v:>3}: keys per node min {min(c):>6,} max {max(c):>6,} "
              f"(stdev {statistics.pstdev(c) / statistics.mean(c):.0%} of mean)")

    ring = Ring(nodes, 256)
    before = {k: ring.owner(k) for k in keys}
    ring.add("node10")
    moved = sum(1 for k in keys if ring.owner(k) != before[k])
    to_new = sum(1 for k in keys if ring.owner(k) == "node10")
    print(f"add node10: {moved:,} of {len(keys):,} keys moved ({moved / len(keys):.1%}; "
          f"ideal 1/11 = {1 / 11:.1%}); all moved keys went to node10: {moved == to_new}")

    mod_moved = sum(1 for k in keys if h(k) % 10 != h(k) % 11)
    print(f"same change with hash % N: {mod_moved / len(keys):.1%} of keys move")

    print("preference list for 'user:42':", ring.preference_list("user:42"))
