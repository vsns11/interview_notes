"""Replication vs erasure coding: storage overhead, durability, and a toy XOR repair.

Run:  .venv-books/bin/python system-design-patterns/modules/16-case-studies-infrastructure/code/erasure_vs_replication.py
Standard library only.
"""
from math import comb
from functools import reduce

# p = probability that one given disk fails *before* a lost copy/fragment is repaired
# (a small window: failures are independent and repair is fast). Illustrative value only.
P_FAIL = 0.001


def loss_probability(n: int, tolerated: int, p: float = P_FAIL) -> float:
    """Data is lost if more than `tolerated` of the n pieces fail in the same repair window."""
    return sum(comb(n, i) * p**i * (1 - p) ** (n - i) for i in range(tolerated + 1, n + 1))


schemes = [
    # name, pieces stored (n), pieces needed to read (k)
    ("3x replication", 3, 1),
    ("RS(6,3)  6 data + 3 parity", 9, 6),
    ("RS(8,4)  8 data + 4 parity", 12, 8),
    ("RS(10,4) 10 data + 4 parity", 14, 10),
]

print(f"{'scheme':30} {'raw bytes per 1 byte':>21} {'can lose':>9} {'P(loss) per window':>20}")
for name, n, k in schemes:
    overhead = n / k
    tolerated = n - k
    print(f"{name:30} {overhead:>20.2f}x {tolerated:>9} {loss_probability(n, tolerated):>20.2e}")

# What does that mean for 1 PB of user data?
print()
for name, n, k in schemes:
    print(f"1 PB of objects under {name:28} -> {n / k:.2f} PB of raw disk")

# ---- toy erasure code: k data chunks + 1 XOR parity chunk (RAID-5 style, tolerates 1 loss) ----
print()
data = b"HELLO-OBJECT-STORE!!"          # 20 bytes
k = 4
size = len(data) // k
chunks = [data[i * size:(i + 1) * size] for i in range(k)]
xor = lambda a, b: bytes(x ^ y for x, y in zip(a, b))
parity = reduce(xor, chunks)
print("chunks :", chunks, "parity:", parity.hex())

lost = 2                                  # the disk holding chunk 2 dies
survivors = [c for i, c in enumerate(chunks) if i != lost] + [parity]
rebuilt = reduce(xor, survivors)          # XOR of everything else gives back the missing chunk
print(f"lost chunk {lost}, rebuilt ->", rebuilt)
assert rebuilt == chunks[lost]
assert b"".join(chunks[:lost] + [rebuilt] + chunks[lost + 1:]) == data
print("object reassembled OK; repair had to read", len(survivors), "chunks to rebuild 1")
