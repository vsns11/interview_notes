"""Building a big string: repeated += vs list + ''.join vs io.StringIO.

Timings depend on your machine; the ratio is what matters.
Note: CPython has an in-place optimisation that sometimes makes `s += piece` fast when
only ONE reference to s exists. It is an implementation detail (PyPy etc. lack it), and it
breaks as soon as another name refers to the same string - shown in `plus_equal_shared`.
"""
import io
import timeit
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt

PIECE = "word "


def plus_equal(n: int) -> str:
    s = ""
    for _ in range(n):
        s += PIECE
    return s


def plus_equal_shared(n: int) -> str:
    s = ""
    alias = []
    for _ in range(n):
        alias.append(s)        # keep a second reference -> defeats the CPython shortcut
        s += PIECE
        alias.pop()
    return s


def join_list(n: int) -> str:
    parts = []
    for _ in range(n):
        parts.append(PIECE)
    return "".join(parts)


def string_io(n: int) -> str:
    buf = io.StringIO()
    for _ in range(n):
        buf.write(PIECE)
    return buf.getvalue()


def bench(fn, n: int) -> float:
    reps = 3
    return min(timeit.repeat(lambda: fn(n), number=1, repeat=reps)) * 1000  # ms


if __name__ == "__main__":
    assert plus_equal(10) == join_list(10) == string_io(10) == plus_equal_shared(10)
    sizes = [2_000, 5_000, 10_000, 20_000, 40_000]
    fns = [plus_equal, plus_equal_shared, join_list, string_io]
    results = {f.__name__: [bench(f, n) for n in sizes] for f in fns}
    print(f"{'n':>7} " + " ".join(f"{name:>18}" for name in results))
    for i, n in enumerate(sizes):
        print(f"{n:>7} " + " ".join(f"{results[name][i]:>15.2f} ms" for name in results))

    img = Path(__file__).resolve().parent.parent / "img" / "join_vs_concat.png"
    fig, ax = plt.subplots(figsize=(7, 4))
    for name, ys in results.items():
        ax.plot(sizes, ys, marker="o", label=name)
    ax.set_xlabel("number of pieces appended")
    ax.set_ylabel("time (ms, best of 3)")
    ax.set_title("Building a string: += with a shared reference is quadratic")
    ax.legend()
    ax.grid(alpha=0.3)
    fig.tight_layout()
    fig.savefig(img, dpi=120)
    print("saved", img.name)
