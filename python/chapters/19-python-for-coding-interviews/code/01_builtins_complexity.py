"""19.1 Built-ins and their cost: measure the operations interviewers ask about.

Times membership tests (list vs set) and front-pops (list.pop(0) vs deque.popleft)
for growing n, prints a small table and saves img/complexity.png.
"""
import timeit
from collections import deque
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt

SIZES = [1_000, 5_000, 20_000, 50_000]


def membership_times(n: int) -> tuple[float, float]:
    data_list = list(range(n))
    data_set = set(data_list)
    target = n - 1  # worst case for the list: the last element
    t_list = timeit.timeit(lambda: target in data_list, number=200) / 200
    t_set = timeit.timeit(lambda: target in data_set, number=200) / 200
    return t_list, t_set


def pop_front_times(n: int) -> tuple[float, float]:
    def drain_list():
        items = list(range(n))
        while items:
            items.pop(0)  # O(n) each: every other element shifts left

    def drain_deque():
        items = deque(range(n))
        while items:
            items.popleft()  # O(1) each

    return (timeit.timeit(drain_list, number=1),
            timeit.timeit(drain_deque, number=1))


def sanity_checks() -> None:
    # The operations below are what the cheat table in the README describes.
    a = [3, 1, 2]
    a.append(4)                     # O(1) amortised
    assert a == [3, 1, 2, 4]
    assert a.pop() == 4             # O(1) from the end
    a.insert(0, 9)                  # O(n) - shifts everything
    assert a == [9, 3, 1, 2]
    assert sorted(a) == [1, 2, 3, 9]  # O(n log n), returns a new list
    assert a[1:3] == [3, 1]         # slicing copies: O(k)
    d = {"x": 1}
    d["y"] = 2                      # O(1) average
    assert d.get("z", 0) == 0
    s = {1, 2, 3}
    assert s & {2, 3, 4} == {2, 3}  # O(min(len)) average
    assert "".join(["a", "b", "c"]) == "abc"  # O(total length)


if __name__ == "__main__":
    sanity_checks()
    print(f"{'n':>7} | {'x in list':>11} | {'x in set':>9} | {'pop(0) all':>11} | {'popleft all':>11}")
    rows = []
    for n in SIZES:
        tl, ts = membership_times(n)
        pl, pd = pop_front_times(n)
        rows.append((n, tl, ts, pl, pd))
        print(f"{n:>7} | {tl * 1e6:>8.1f} us | {ts * 1e6:>6.2f} us | {pl * 1e3:>8.2f} ms | {pd * 1e3:>8.2f} ms")

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(10, 3.8))
    ns = [r[0] for r in rows]
    ax1.plot(ns, [r[1] * 1e6 for r in rows], "o-", label="x in list  O(n)")
    ax1.plot(ns, [r[2] * 1e6 for r in rows], "s-", label="x in set  O(1)")
    ax1.set_title("Membership test")
    ax1.set_xlabel("n")
    ax1.set_ylabel("microseconds per test")
    ax1.legend()
    ax2.plot(ns, [r[3] * 1e3 for r in rows], "o-", label="list.pop(0) n times  O(n^2)")
    ax2.plot(ns, [r[4] * 1e3 for r in rows], "s-", label="deque.popleft n times  O(n)")
    ax2.set_title("Draining a queue from the front")
    ax2.set_xlabel("n")
    ax2.set_ylabel("milliseconds total")
    ax2.legend()
    fig.tight_layout()
    out = Path(__file__).resolve().parent.parent / "img" / "complexity.png"
    fig.savefig(out, dpi=120)
    print("saved", out.name)
    print("all asserts passed")
