"""A list is a dynamic array: watch it over-allocate, and time O(1) vs O(n) operations."""
import sys
import timeit
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt

IMG = Path(__file__).resolve().parent.parent / "img"
PTR = 8  # bytes per pointer on a 64-bit build


def growth() -> tuple[list[int], list[int]]:
    lst: list[int] = []
    empty = sys.getsizeof(lst)
    lens, caps = [], []
    last_cap = -1
    print(" len  capacity  (capacity = slots allocated)")
    for i in range(70):
        cap = (sys.getsizeof(lst) - empty) // PTR
        lens.append(len(lst))
        caps.append(cap)
        if cap != last_cap:
            print(f"{len(lst):>4}  {cap:>8}")
            last_cap = cap
        lst.append(i)
    return lens, caps


def timings() -> None:
    """Each statement does a push and a pop, so the list size stays at n."""
    print(f"{'n':>9} {'append()+pop()':>16} {'insert(0)+pop(0)':>18}   (µs per pair)")
    for n in [10_000, 100_000, 1_000_000]:
        # pre-grow once so a one-off resize of the list is not part of the measurement
        setup = f"lst = list(range({n})); lst.append(0); lst.pop()"
        k = 1000
        t_end = min(timeit.repeat("lst.append(1); lst.pop()", setup, number=k, repeat=5)) / k
        t_front = min(timeit.repeat("lst.insert(0, 1); lst.pop(0)", setup, number=k, repeat=5)) / k
        print(f"{n:>9,} {t_end * 1e6:>16.3f} {t_front * 1e6:>18.3f}")


def plot(lens: list[int], caps: list[int]) -> None:
    fig, ax = plt.subplots(figsize=(7, 4))
    ax.step(lens, caps, where="post", label="capacity (allocated slots)")
    ax.plot(lens, lens, "--", label="len (used slots)")
    ax.set_xlabel("len(list) while appending")
    ax.set_ylabel("slots")
    ax.set_title("CPython list over-allocation: capacity grows in steps")
    ax.legend()
    ax.grid(alpha=0.3)
    fig.tight_layout()
    fig.savefig(IMG / "list_growth.png", dpi=120)
    print("saved list_growth.png")


if __name__ == "__main__":
    lens, caps = growth()
    timings()
    plot(lens, caps)
