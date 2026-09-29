"""Chapter 7 - recursion, the recursion limit, memoization."""
import functools
import sys
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt


def factorial(n):
    if n <= 1:                 # base case (also handles 0! = 1)
        return 1
    return n * factorial(n - 1)   # recursive case: smaller problem


def factorial_traced(n, depth=0):
    pad = "  " * depth
    print(f"{pad}factorial({n})")
    result = 1 if n <= 1 else n * factorial_traced(n - 1, depth + 1)
    print(f"{pad}-> {result}")
    return result


calls = 0


def fib_naive(n):
    global calls
    calls += 1
    if n < 2:
        return n
    return fib_naive(n - 1) + fib_naive(n - 2)


@functools.cache
def fib_memo(n):
    if n < 2:
        return n
    return fib_memo(n - 1) + fib_memo(n - 2)


def fib_iter(n):
    a, b = 0, 1
    for _ in range(n):
        a, b = b, a + b
    return a


def flatten(nested):
    """Recursion over nested data: lists inside lists."""
    out = []
    for item in nested:
        if isinstance(item, list):
            out.extend(flatten(item))
        else:
            out.append(item)
    return out


def depth_of(n):
    return 0 if n == 0 else 1 + depth_of(n - 1)


if __name__ == "__main__":
    print("== factorial ==")
    print("factorial(5) =", factorial(5), "| factorial(0) =", factorial(0))
    factorial_traced(3)

    print("\n== fibonacci: naive vs memoized ==")
    ns = list(range(2, 26))
    naive_calls = []
    for n in ns:
        calls = 0
        fib_naive(n)
        naive_calls.append(calls)
    print("fib(7) =", fib_naive(7))
    calls_at = dict(zip(ns, naive_calls))
    print("naive calls for n=10, 20, 25:", calls_at[10], calls_at[20], calls_at[25])
    fib_memo.cache_clear()
    fib_memo(25)
    print("memoized:", fib_memo.cache_info())
    print("fib_iter(90) =", fib_iter(90))

    print("\n== nested data ==")
    print(flatten([1, [2, [3, [4]], 5], [], 6]))

    print("\n== recursion limit ==")
    print("sys.getrecursionlimit() =", sys.getrecursionlimit())
    print("depth_of(500) =", depth_of(500))
    try:
        depth_of(100_000)
    except RecursionError as exc:
        print("RecursionError:", exc)

    img = Path(__file__).resolve().parent.parent / "img" / "fib_calls.png"
    fig, ax = plt.subplots(figsize=(7, 4))
    ax.plot(ns, naive_calls, marker="o", ms=3, label="naive fib(n) calls")
    ax.plot(ns, [n + 1 for n in ns], marker="s", ms=3, label="memoized: real computations (n+1)")
    ax.set_yscale("log")
    ax.set_xlabel("n")
    ax.set_ylabel("function calls (log scale)")
    ax.set_title("Naive recursion explodes, memoization stays linear")
    ax.legend()
    ax.grid(alpha=0.3)
    fig.tight_layout()
    fig.savefig(img, dpi=120)
    print("\nsaved", img.name)
