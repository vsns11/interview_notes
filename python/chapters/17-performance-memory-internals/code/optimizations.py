"""Common optimisations measured: join vs +=, comprehension vs loop, local lookups, lru_cache,
numpy vectorisation, generators, __slots__. Saves img/optimizations.png."""
import math
import timeit
import tracemalloc
from functools import lru_cache
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

N = 100_000


def best(fn, number: int = 5) -> float:
    return min(timeit.repeat(fn, number=number, repeat=5)) / number


# 1. building a string
def concat_plus():
    s = ""
    for i in range(N):
        s += str(i)                       # CPython often optimises this in place, but not guaranteed
    return s


def concat_join():
    return "".join(str(i) for i in range(N))


# 2. loop + append vs comprehension
def loop_append():
    out = []
    for i in range(N):
        out.append(i * i)
    return out


def comprehension():
    return [i * i for i in range(N)]


# 3. global/attribute lookup vs local alias
def global_lookup():
    total = 0.0
    for i in range(N):
        total += math.sqrt(i)             # looks up 'math' then '.sqrt' each iteration
    return total


def local_lookup():
    sqrt = math.sqrt                      # one lookup, then a fast local
    total = 0.0
    for i in range(N):
        total += sqrt(i)
    return total


# 4. numpy vectorisation
def python_sum_squares():
    return sum(i * i for i in range(N))


def numpy_sum_squares():
    a = np.arange(N, dtype=np.int64)
    return int((a * a).sum())


# 5. caching
def fib(n):
    return n if n < 2 else fib(n - 1) + fib(n - 2)


@lru_cache(maxsize=None)
def fib_cached(n):
    return n if n < 2 else fib_cached(n - 1) + fib_cached(n - 2)


# 6. __slots__
class PointDict:
    def __init__(self, x, y):
        self.x, self.y = x, y


class PointSlots:
    __slots__ = ("x", "y")                # fixed attributes, no per-instance __dict__

    def __init__(self, x, y):
        self.x, self.y = x, y


def peak_mb(fn) -> float:
    tracemalloc.start()
    keep = fn()
    peak = tracemalloc.get_traced_memory()[1]
    tracemalloc.stop()
    del keep
    return peak / 1e6


if __name__ == "__main__":
    assert concat_plus() == concat_join() and loop_append() == comprehension()
    assert math.isclose(global_lookup(), local_lookup())
    assert python_sum_squares() == numpy_sum_squares()

    pairs = [("string += vs join", concat_plus, concat_join),
             ("for+append vs listcomp", loop_append, comprehension),
             ("math.sqrt vs local sqrt", global_lookup, local_lookup),
             ("python sum vs numpy", python_sum_squares, numpy_sum_squares)]
    labels, speedups = [], []
    for label, slow, fast in pairs:
        ts, tf = best(slow), best(fast)
        labels.append(label); speedups.append(ts / tf)
        print(f"{label:25} {ts*1000:8.2f} ms -> {tf*1000:7.2f} ms   x{ts/tf:5.1f}")

    t_fib = best(lambda: fib(25), number=1)
    fib_cached.cache_clear()
    t_cached = best(lambda: (fib_cached.cache_clear(), fib_cached(25)), number=1)
    labels.append("fib(25) vs lru_cache"); speedups.append(t_fib / t_cached)
    print(f"{'fib(25) vs lru_cache':25} {t_fib*1000:8.2f} ms -> {t_cached*1000:7.3f} ms   x{t_fib/t_cached:5.0f}"
          f"   {fib_cached.cache_info()}")

    m_list = peak_mb(lambda: sum([i * i for i in range(1_000_000)]))
    m_gen = peak_mb(lambda: sum(i * i for i in range(1_000_000)))
    print(f"sum over 1M: list peak {m_list:.1f} MB vs generator peak {m_gen*1000:.1f} KB")

    m_dict = peak_mb(lambda: [PointDict(i, i) for i in range(100_000)])
    m_slots = peak_mb(lambda: [PointSlots(i, i) for i in range(100_000)])
    print(f"100k objects: regular class {m_dict:.1f} MB vs __slots__ {m_slots:.1f} MB")
    p = PointSlots(1, 2)
    try:
        p.z = 3
    except AttributeError as e:
        print(f"__slots__ blocks new attributes: {e}")

    fig, ax = plt.subplots(figsize=(7, 3.6))
    ax.barh(labels, speedups, color="#30638e")
    ax.set_xscale("log")
    ax.set_xlim(0.8, max(speedups) * 8)
    ax.set_xlabel("speed-up (x, log scale)")
    for y, v in enumerate(speedups):
        ax.text(v, y, f" x{v:.1f}", va="center")
    ax.set_title("Measured speed-ups of common Python optimisations")
    fig.tight_layout()
    out = Path(__file__).resolve().parent.parent / "img" / "optimizations.png"
    fig.savefig(out, dpi=120)
    print(f"saved {out.name}")
