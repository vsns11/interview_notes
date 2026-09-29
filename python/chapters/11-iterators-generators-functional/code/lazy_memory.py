"""Memory of a list vs a generator, and the time/memory profile of lazy evaluation."""
import sys
import time
import tracemalloc
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import matplotlib.ticker
from pathlib import Path


def peak_bytes(fn) -> int:
    tracemalloc.start()
    fn()
    _, peak = tracemalloc.get_traced_memory()
    tracemalloc.stop()
    return peak


if __name__ == "__main__":
    n = 1_000_000
    lst = [x * x for x in range(n)]
    gen = (x * x for x in range(n))
    print(f"getsizeof list of {n:,}: {sys.getsizeof(lst)/1e6:.1f} MB (pointers only)")
    print(f"getsizeof generator:      {sys.getsizeof(gen)} bytes")
    del lst

    sizes = [10_000, 100_000, 300_000, 1_000_000]
    list_peaks, gen_peaks = [], []
    for s in sizes:
        list_peaks.append(peak_bytes(lambda: sum([x * x for x in range(s)])) / 1e6)
        gen_peaks.append(peak_bytes(lambda: sum(x * x for x in range(s))) / 1e6)
    for s, l, g in zip(sizes, list_peaks, gen_peaks):
        print(f"n={s:>9,}  sum(list comp) peak {l:6.2f} MB | sum(gen expr) peak {g*1000:5.1f} KB")

    # early exit: a generator stops work as soon as the answer is known
    t0 = time.perf_counter()
    any([x > 10 for x in range(n)])
    t_list = time.perf_counter() - t0
    t0 = time.perf_counter()
    any(x > 10 for x in range(n))
    t_gen = time.perf_counter() - t0
    print(f"any() list comp built all {n:,} items; gen stopped at item 12 -> gen faster: {t_gen < t_list}")

    out = Path(__file__).resolve().parent.parent / "img"
    out.mkdir(exist_ok=True)
    fig, ax = plt.subplots(figsize=(7, 4))
    ax.plot(sizes, list_peaks, marker="o", label="sum([x*x for x in range(n)])  (list)")
    ax.plot(sizes, gen_peaks, marker="o", label="sum(x*x for x in range(n))  (generator)")
    ax.xaxis.set_major_formatter(matplotlib.ticker.FuncFormatter(lambda v, _: f"{int(v):,}"))
    ax.set_xlabel("n (items)")
    ax.set_ylabel("peak memory (MB)")
    ax.set_title("Peak memory: materialised list vs lazy generator")
    ax.grid(alpha=0.3)
    ax.legend()
    fig.tight_layout()
    fig.savefig(out / "list_vs_generator_memory.png", dpi=120)
    print("saved img/list_vs_generator_memory.png")
