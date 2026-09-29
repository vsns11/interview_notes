"""Membership test cost: list (O(n) scan) vs set / dict (O(1) average hash lookup)."""
import timeit
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt

IMG = Path(__file__).resolve().parent.parent / "img" / "list_vs_set_lookup.png"


def main() -> None:
    sizes = [1_000, 10_000, 100_000, 1_000_000]
    rows = []
    print(f"{'n':>10} {'list (µs)':>12} {'set (µs)':>10} {'dict (µs)':>10}")
    for n in sizes:
        data = list(range(n))
        s, d = set(data), dict.fromkeys(data)
        target = n - 1                                 # worst case for the list: last element
        k = 200
        t_list = min(timeit.repeat(lambda: target in data, number=k, repeat=3)) / k * 1e6
        t_set = min(timeit.repeat(lambda: target in s, number=k, repeat=3)) / k * 1e6
        t_dict = min(timeit.repeat(lambda: target in d, number=k, repeat=3)) / k * 1e6
        rows.append((n, t_list, t_set, t_dict))
        print(f"{n:>10,} {t_list:>12.2f} {t_set:>10.3f} {t_dict:>10.3f}")

    fig, ax = plt.subplots(figsize=(7, 4))
    ax.loglog([r[0] for r in rows], [r[1] for r in rows], "o-", label="x in list  (O(n))")
    ax.loglog([r[0] for r in rows], [r[2] for r in rows], "s-", label="x in set   (O(1) avg)")
    ax.loglog([r[0] for r in rows], [r[3] for r in rows], "^-", label="x in dict  (O(1) avg)")
    ax.set_xlabel("collection size n (log)")
    ax.set_ylabel("time per lookup, µs (log)")
    ax.set_title("Membership test: linear scan vs hash lookup")
    ax.grid(alpha=0.3, which="both")
    ax.legend()
    fig.tight_layout()
    fig.savefig(IMG, dpi=120)
    print("saved", IMG.name)


if __name__ == "__main__":
    main()
