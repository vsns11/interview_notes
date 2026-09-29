"""Measure the big-O of common operations: membership in list vs set, pop(0) vs deque.popleft.
Saves img/complexity.png."""
import timeit
from collections import deque
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

SIZES = [1_000, 10_000, 100_000, 1_000_000]


def per_op_us(stmt: str, setup: str, number: int) -> float:
    return min(timeit.repeat(stmt, setup, number=number, repeat=3)) / number * 1e6


if __name__ == "__main__":
    rows = []
    for n in SIZES:
        setup = f"from collections import deque; n={n}; L=list(range(n)); S=set(L); D=deque(L)"
        rows.append({
            "n": n,
            "x in list": per_op_us("(n-1) in L", setup, 200),          # worst case: last item
            "x in set": per_op_us("(n-1) in S", setup, 200_000),
            "list.pop(0)+append": per_op_us("L.append(L.pop(0))", setup, 2_000),
            "deque.popleft+append": per_op_us("D.append(D.popleft())", setup, 200_000),
        })
    cols = [c for c in rows[0] if c != "n"]
    print(f"{'n':>10} " + " ".join(f"{c:>21}" for c in cols) + "   (microseconds per op)")
    for r in rows:
        print(f"{r['n']:>10,} " + " ".join(f"{r[c]:>21.3f}" for c in cols))

    fig, ax = plt.subplots(figsize=(7, 4))
    styles = {"x in list": "#d1495b", "list.pop(0)+append": "#edae49",
              "x in set": "#00798c", "deque.popleft+append": "#30638e"}
    for c in cols:
        ax.plot(SIZES, [r[c] for r in rows], marker="o", label=c, color=styles[c])
    ax.set_xscale("log"); ax.set_yscale("log")
    ax.set_xlabel("container size n"); ax.set_ylabel("time per operation (µs, log)")
    ax.set_title("O(n) operations grow with n, O(1) operations stay flat")
    ax.legend()
    fig.tight_layout()
    out = Path(__file__).resolve().parent.parent / "img" / "complexity.png"
    fig.savefig(out, dpi=120)
    print(f"saved {out.name}")
