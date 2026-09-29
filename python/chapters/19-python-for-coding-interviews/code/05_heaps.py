"""19.5 Heaps with heapq: min-heap, max-heap trick, top-k, merge k lists, median."""
import heapq
from collections import Counter


def kth_largest(nums: list[int], k: int) -> int:
    """Keep a min-heap of size k: its root is the k-th largest. O(n log k)."""
    heap: list[int] = []
    for x in nums:
        heapq.heappush(heap, x)
        if len(heap) > k:
            heapq.heappop(heap)       # drop the smallest
    return heap[0]


def top_k_frequent(words: list[str], k: int) -> list[str]:
    counts = Counter(words)
    # ties broken alphabetically: key = (-count, word)
    return heapq.nsmallest(k, counts, key=lambda w: (-counts[w], w))


def merge_k_sorted(lists: list[list[int]]) -> list[int]:
    heap = [(lst[0], i, 0) for i, lst in enumerate(lists) if lst]
    heapq.heapify(heap)                       # O(k)
    out = []
    while heap:
        val, i, j = heapq.heappop(heap)       # i breaks ties -> no comparing lists
        out.append(val)
        if j + 1 < len(lists[i]):
            heapq.heappush(heap, (lists[i][j + 1], i, j + 1))
    return out


class RunningMedian:
    """Two heaps: `low` is a max-heap (stored negated), `high` a min-heap."""

    def __init__(self):
        self.low: list[int] = []    # negated values
        self.high: list[int] = []

    def add(self, x: int) -> None:
        heapq.heappush(self.low, -x)
        heapq.heappush(self.high, -heapq.heappop(self.low))   # move the biggest low up
        if len(self.high) > len(self.low):
            heapq.heappush(self.low, -heapq.heappop(self.high))

    def median(self) -> float:
        if len(self.low) > len(self.high):
            return float(-self.low[0])
        return (-self.low[0] + self.high[0]) / 2


def max_capital(k: int, capital0: int, profits: list[int], costs: list[int]) -> int:
    """Two heaps: `locked` = min-heap by cost, `ready` = max-heap (negated) by profit.

    Pick at most k projects; each needs `cost` capital to start and adds `profit`.
    Greedy: always take the most profitable project you can afford right now.
    """
    locked = sorted(zip(costs, profits))          # sorted list works as a min-heap
    heapq.heapify(locked)
    ready: list[int] = []
    capital = capital0
    for _ in range(k):
        while locked and locked[0][0] <= capital:  # unlock everything affordable
            _, profit = heapq.heappop(locked)
            heapq.heappush(ready, -profit)
        if not ready:
            break                                  # nothing affordable -> stop early
        capital += -heapq.heappop(ready)
    return capital


def k_closest_points(points: list[tuple[int, int]], k: int) -> list[tuple[int, int]]:
    return heapq.nsmallest(k, points, key=lambda p: p[0] ** 2 + p[1] ** 2)


if __name__ == "__main__":
    h = [5, 1, 8, 3, 2]
    heapq.heapify(h)                          # in place, O(n)
    print("heapified:", h, "smallest:", h[0])
    print("pop order:", [heapq.heappop(h) for _ in range(len(h))])

    # max-heap trick: push negatives
    mx: list[int] = []
    for x in [5, 1, 8, 3]:
        heapq.heappush(mx, -x)
    print("max via negation:", -heapq.heappop(mx))

    tasks = [(2, "write tests"), (1, "fix prod bug"), (3, "refactor")]
    heapq.heapify(tasks)
    print("priority queue first:", heapq.heappop(tasks))
    print("nlargest(2):", heapq.nlargest(2, [4, 9, 1, 7]))

    assert kth_largest([3, 2, 1, 5, 6, 4], 2) == 5
    assert top_k_frequent(["a", "b", "a", "c", "b", "a", "d"], 2) == ["a", "b"]
    assert top_k_frequent(["x", "y"], 1) == ["x"]       # tie -> alphabetical
    assert merge_k_sorted([[1, 4, 5], [1, 3, 4], [2, 6], []]) == [1, 1, 2, 3, 4, 4, 5, 6]
    rm = RunningMedian()
    medians = []
    for x in [5, 15, 1, 3]:
        rm.add(x)
        medians.append(rm.median())
    print("running medians:", medians)
    assert medians == [5.0, 10.0, 5.0, 4.0]
    assert max_capital(2, 0, [1, 2, 3], [0, 1, 1]) == 4     # 0 -> +1 -> +3
    assert max_capital(3, 0, [1, 2, 3], [0, 1, 2]) == 6
    assert max_capital(1, 0, [5], [1]) == 0                  # cannot afford anything
    print("max capital (k=2):", max_capital(2, 0, [1, 2, 3], [0, 1, 1]))
    assert k_closest_points([(1, 3), (-2, 2), (5, 8), (0, 1)], 2) == [(0, 1), (-2, 2)]
    print("all asserts passed")
