"""Bloom filter: measured false-positive rate vs the textbook formula.

A Bloom filter is a bit array of m bits plus k hash functions. add(x) sets k bits;
might_contain(x) says "maybe" if all k bits are set, "no" if any is 0.
Deterministic: hashes come from SHA-256, keys are fixed strings.
Run:  python3 bloom_filter_sim.py
"""
import hashlib
import math


class BloomFilter:
    def __init__(self, m_bits: int, k_hashes: int):
        self.m, self.k = m_bits, k_hashes
        self.bits = bytearray((m_bits + 7) // 8)

    def _positions(self, key: str):
        # Double hashing (Kirsch-Mitzenmacher): h_i = h1 + i*h2 mod m
        d = hashlib.sha256(key.encode()).digest()
        h1 = int.from_bytes(d[:8], "big")
        h2 = int.from_bytes(d[8:16], "big") | 1
        return [(h1 + i * h2) % self.m for i in range(self.k)]

    def add(self, key: str):
        for p in self._positions(key):
            self.bits[p // 8] |= 1 << (p % 8)

    def might_contain(self, key: str) -> bool:
        return all(self.bits[p // 8] >> (p % 8) & 1 for p in self._positions(key))


def theory(m, n, k):
    return (1 - math.exp(-k * n / m)) ** k


n = 10_000                                  # keys inserted
probes = [f"absent-{i}" for i in range(50_000)]  # keys never inserted
print(f"n = {n} keys inserted, {len(probes)} absent keys probed\n")
print(f"{'bits/key':>8} {'m bits':>8} {'k':>3} {'measured FP':>12} {'theory FP':>10} {'memory':>9}")
for bits_per_key in (4, 8, 10, 16):
    m = bits_per_key * n
    k_opt = max(1, round(m / n * math.log(2)))   # optimal k = (m/n) ln 2
    bf = BloomFilter(m, k_opt)
    for i in range(n):
        bf.add(f"present-{i}")
    assert all(bf.might_contain(f"present-{i}") for i in range(n))   # never a false negative
    fp = sum(bf.might_contain(p) for p in probes) / len(probes)
    print(f"{bits_per_key:>8} {m:>8} {k_opt:>3} {fp:>12.4%} {theory(m, n, k_opt):>10.4%} {m / 8 / 1024:>7.1f}KB")

print("\nEffect of k at a fixed 8 bits/key:")
m = 8 * n
for k in (1, 2, 4, 6, 8, 12):
    bf = BloomFilter(m, k)
    for i in range(n):
        bf.add(f"present-{i}")
    fp = sum(bf.might_contain(p) for p in probes) / len(probes)
    print(f"  k={k:>2}  measured {fp:7.3%}   theory {theory(m, n, k):7.3%}")
