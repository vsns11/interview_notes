"""Fixed-size vs content-defined chunking: how many chunks must be re-uploaded after a 5-byte insert.

Run: .venv-books/bin/python chunking.py   (fixed random seed, so the output is the same on every run)
"""
import hashlib, random

random.seed(7)
GEAR = [random.getrandbits(32) for _ in range(256)]   # one random 32-bit value per byte value

def fixed_chunks(data, size=4096):
    return [data[i:i + size] for i in range(0, len(data), size)]

def cdc_chunks(data, mask=(1 << 12) - 1, min_len=1024, max_len=16384):
    """Content-defined chunking with a 'gear' rolling hash:
    cut where the low 12 bits of the hash are zero (~ every 4 KiB on average)."""
    chunks, start, h = [], 0, 0
    for i, b in enumerate(data):
        h = ((h << 1) + GEAR[b]) & 0xFFFFFFFF
        n = i - start + 1
        if (n >= min_len and (h & mask) == 0) or n >= max_len:
            chunks.append(data[start:i + 1]); start, h = i + 1, 0
    if start < len(data):
        chunks.append(data[start:])
    return chunks

def ids(chunks):
    return [hashlib.sha256(c).hexdigest() for c in chunks]

original = bytes(random.getrandbits(8) for _ in range(1_000_000))       # a 1 MB file
edited = original[:500_000] + b"HELLO" + original[500_000:]             # insert 5 bytes mid-file

for name, fn in [("fixed 4 KiB", fixed_chunks), ("content-defined ~4 KiB", cdc_chunks)]:
    old, new = set(ids(fn(original))), ids(fn(edited))
    changed = [h for h in new if h not in old]
    size = sum(len(c) for c in fn(edited) if hashlib.sha256(c).hexdigest() in set(changed))
    print(f"{name:24s} chunks={len(new):4d}  new chunks to upload={len(changed):4d}  bytes to upload={size:,}")
