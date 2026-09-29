"""Column encodings used by storage engines: RLE, dictionary, delta, bit-packing.

Encodes one column of 100,000 values each way and reports the size.
Deterministic. Run:  python3 compression_demo.py
"""
import zlib

N = 100_000
status = ["shipped"] * 60_000 + ["pending"] * 25_000 + ["refunded"] * 15_000     # sorted column
country = [["IN", "US", "DE", "BR", "JP"][(i * 7) % 5] for i in range(N)]       # low cardinality
ts = [1_700_000_000 + i * 31 + (i % 7) for i in range(N)]                        # growing numbers


def rle(values):
    """Run-length encoding: [(value, run_length), ...]"""
    out = []
    for v in values:
        if out and out[-1][0] == v:
            out[-1][1] += 1
        else:
            out.append([v, 1])
    return out


def dictionary(values):
    """Dictionary encoding: distinct values once + a small integer code per row."""
    d = {v: i for i, v in enumerate(dict.fromkeys(values))}
    return list(d), [d[v] for v in values]


def delta(values):
    """Delta encoding: first value, then differences to the previous value."""
    return values[0], [b - a for a, b in zip(values, values[1:])]


def bits_needed(max_value):
    return max(1, max_value.bit_length())


raw_status = sum(len(s) for s in status)
runs = rle(status)
print(f"{'status   raw text':32s}: {raw_status:>9,} bytes")
print(f"{'status   RLE (' + str(len(runs)) + ' runs)':32s}: {sum(len(v) + 4 for v, _ in runs):>9,} bytes  -> {runs}")

raw_country = 2 * N
dict_vals, codes = dictionary(country)
bits = bits_needed(len(dict_vals) - 1)
print(f"{'country  raw text':32s}: {raw_country:>9,} bytes")
print(f"{'country  dictionary + ' + str(bits) + '-bit codes':32s}: {2 * len(dict_vals) + N * bits // 8:>9,} bytes  "
      f"(dictionary {dict_vals}, codes {codes[:6]}...)")

raw_ts = 8 * N
first, deltas = delta(ts)
dbits = bits_needed(max(deltas))
print(f"{'ts       raw 8-byte ints':32s}: {raw_ts:>9,} bytes")
print(f"{'ts       delta + ' + str(dbits) + '-bit packing':32s}: {8 + (N - 1) * dbits // 8:>9,} bytes  "
      f"(first {first}, deltas {deltas[:6]}...)")

gz = len(zlib.compress(",".join(map(str, ts)).encode(), 6))
print(f"{'ts       zlib on the text':32s}: {gz:>9,} bytes  (no knowledge of the data type)")
