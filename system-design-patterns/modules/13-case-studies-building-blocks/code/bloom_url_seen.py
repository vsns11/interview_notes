"""Bloom filter as a crawler's "URL already seen?" check: sizing formula + measured false-positive rate.

Run: .venv-books/bin/python bloom_url_seen.py
"""
import hashlib
import math


class Bloom:
    def __init__(self, n_expected, fp_rate):
        self.m = math.ceil(-n_expected * math.log(fp_rate) / (math.log(2) ** 2))  # bits
        self.k = max(1, round(self.m / n_expected * math.log(2)))                  # hash functions
        self.bits = bytearray((self.m + 7) // 8)

    def _positions(self, item):
        d = hashlib.sha256(item.encode()).digest()
        a, b = int.from_bytes(d[:8], "big"), int.from_bytes(d[8:16], "big")
        return [(a + i * b) % self.m for i in range(self.k)]   # double hashing

    def add(self, item):
        for p in self._positions(item):
            self.bits[p >> 3] |= 1 << (p & 7)

    def __contains__(self, item):
        return all(self.bits[p >> 3] >> (p & 7) & 1 for p in self._positions(item))


if __name__ == "__main__":
    for n, fp in ((10_000_000_000, 0.01), (10_000_000_000, 0.001)):
        m = -n * math.log(fp) / (math.log(2) ** 2)
        print(f"{n:,} URLs at {fp:.1%} false positives -> {m / 8 / 1e9:.1f} GB, "
              f"k = {round(m / n * math.log(2))} hashes")

    bf = Bloom(200_000, 0.01)
    for i in range(200_000):
        bf.add(f"https://site{i % 5000}.example/p/{i}")
    assert all(f"https://site{i % 5000}.example/p/{i}" in bf for i in range(200_000))  # no false negatives
    fps = sum(f"https://other{i}.example/q" in bf for i in range(100_000))
    print(f"demo: m={bf.m:,} bits ({bf.m / 8 / 1024:.0f} KiB), k={bf.k}; "
          f"measured false-positive rate {fps / 100_000:.2%} (target 1%); false negatives: 0")
