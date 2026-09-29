"""19.6 Sorting with keys and binary search (hand-written and bisect)."""
import bisect
import random
from collections import Counter
from itertools import accumulate
from functools import cmp_to_key
from math import ceil
from operator import itemgetter


def merge_intervals(intervals: list[list[int]]) -> list[list[int]]:
    out: list[list[int]] = []
    for start, end in sorted(intervals):          # sort by start
        if out and start <= out[-1][1]:
            out[-1][1] = max(out[-1][1], end)     # overlap -> extend
        else:
            out.append([start, end])
    return out


def largest_number(nums: list[int]) -> str:
    """Arrange numbers to form the largest number - needs a pairwise comparator."""
    def cmp(a: str, b: str) -> int:
        return -1 if a + b > b + a else (1 if a + b < b + a else 0)
    s = "".join(sorted(map(str, nums), key=cmp_to_key(cmp)))
    return "0" if s[0] == "0" else s


def binary_search(nums: list[int], target: int) -> int:
    lo, hi = 0, len(nums) - 1
    while lo <= hi:
        mid = (lo + hi) // 2          # Python ints don't overflow
        if nums[mid] == target:
            return mid
        if nums[mid] < target:
            lo = mid + 1
        else:
            hi = mid - 1
    return -1


def lower_bound(nums: list[int], target: int) -> int:
    """First index with nums[i] >= target (== bisect_left)."""
    lo, hi = 0, len(nums)             # half-open [lo, hi)
    while lo < hi:
        mid = (lo + hi) // 2
        if nums[mid] < target:
            lo = mid + 1
        else:
            hi = mid
    return lo


def search_rotated(nums: list[int], target: int) -> int:
    lo, hi = 0, len(nums) - 1
    while lo <= hi:
        mid = (lo + hi) // 2
        if nums[mid] == target:
            return mid
        if nums[lo] <= nums[mid]:                 # left half sorted
            if nums[lo] <= target < nums[mid]:
                hi = mid - 1
            else:
                lo = mid + 1
        else:                                     # right half sorted
            if nums[mid] < target <= nums[hi]:
                lo = mid + 1
            else:
                hi = mid - 1
    return -1


def min_eating_speed(piles: list[int], hours: int) -> int:
    """Binary search on the answer: smallest speed that finishes in time."""
    def ok(speed: int) -> bool:
        return sum(ceil(p / speed) for p in piles) <= hours
    lo, hi = 1, max(piles)
    while lo < hi:
        mid = (lo + hi) // 2
        if ok(mid):
            hi = mid          # mid works -> try smaller
        else:
            lo = mid + 1
    return lo


class WeightedPicker:
    """Pick index i with probability w[i] / sum(w): prefix sums + binary search.

    w = [1, 3, 2] -> prefix = [1, 4, 6]. Draw r in [0, 6): r < 1 -> 0,
    1 <= r < 4 -> 1, 4 <= r < 6 -> 2. bisect_right finds the slot in O(log n).
    """

    def __init__(self, weights: list[int], seed: int | None = None):
        self.prefix = list(accumulate(weights))
        self.rng = random.Random(seed)

    def pick(self) -> int:
        r = self.rng.randrange(self.prefix[-1])        # integer in [0, total)
        return bisect.bisect_right(self.prefix, r)


if __name__ == "__main__":
    people = [("ana", 31), ("bo", 25), ("cy", 31), ("di", 25)]
    print("by age then name:", sorted(people, key=lambda p: (p[1], p[0])))
    print("age desc, name asc:", sorted(people, key=lambda p: (-p[1], p[0])))
    print("itemgetter(1) (stable):", sorted(people, key=itemgetter(1)))
    words = ["banana", "Apple", "cherry"]
    print("case-insensitive:", sorted(words, key=str.lower))
    print("by length desc:", sorted(words, key=len, reverse=True))

    assert merge_intervals([[1, 3], [8, 10], [2, 6], [15, 18]]) == [[1, 6], [8, 10], [15, 18]]
    assert largest_number([3, 30, 34, 5, 9]) == "9534330"
    assert largest_number([0, 0]) == "0"

    a = [1, 3, 3, 3, 7, 9]
    assert binary_search(a, 7) == 4 and binary_search(a, 4) == -1
    assert lower_bound(a, 3) == bisect.bisect_left(a, 3) == 1
    assert bisect.bisect_right(a, 3) == 4
    print("count of 3s via bisect:", bisect.bisect_right(a, 3) - bisect.bisect_left(a, 3))
    b = a.copy()
    bisect.insort(b, 5)
    print("insort 5:", b)
    # bisect with key= (Python 3.10+): search a list sorted by a field
    recs = sorted(people, key=itemgetter(1))
    print("first age>=31 at index", bisect.bisect_left(recs, 31, key=itemgetter(1)))

    assert search_rotated([4, 5, 6, 7, 0, 1, 2], 0) == 4
    assert search_rotated([4, 5, 6, 7, 0, 1, 2], 3) == -1
    assert min_eating_speed([3, 6, 7, 11], 8) == 4
    assert min_eating_speed([30, 11, 23, 4, 20], 5) == 30

    picker = WeightedPicker([1, 3, 2], seed=7)
    assert picker.prefix == [1, 4, 6]
    freq = Counter(picker.pick() for _ in range(60_000))
    shares = [round(freq[i] / 60_000, 2) for i in range(3)]
    print("weighted pick shares (expect ~0.17, 0.50, 0.33):", shares)
    assert all(abs(s - e) < 0.02 for s, e in zip(shares, [1 / 6, 3 / 6, 2 / 6]))
    print("all asserts passed")
