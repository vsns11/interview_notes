"""19.17 Sort & search: merge sort on a linked list, quicksort, quickselect, Dutch national flag."""
from __future__ import annotations

import random
from dataclasses import dataclass


@dataclass
class ListNode:
    val: int
    next: ListNode | None = None


def from_list(values):
    dummy = tail = ListNode(0)
    for v in values:
        tail.next = ListNode(v); tail = tail.next
    return dummy.next


def to_list(head):
    out = []
    while head:
        out.append(head.val); head = head.next
    return out


def sort_list(head: ListNode | None) -> ListNode | None:
    """Merge sort on a linked list: split at the middle (fast/slow), sort halves, merge.
    O(n log n) time, O(log n) recursion stack, no extra arrays, and stable."""
    if head is None or head.next is None:
        return head
    slow, fast = head, head.next               # slow stops at the END of the left half
    while fast and fast.next:
        slow, fast = slow.next, fast.next.next
    right, slow.next = slow.next, None         # cut the list in two
    a, b = sort_list(head), sort_list(right)
    dummy = tail = ListNode(0)
    while a and b:
        if a.val <= b.val:                     # <= keeps equal keys in order (stable)
            tail.next, a = a, a.next
        else:
            tail.next, b = b, b.next
        tail = tail.next
    tail.next = a or b
    return dummy.next


def partition(nums: list[int], lo: int, hi: int, rng: random.Random) -> int:
    """Lomuto partition of nums[lo..hi] around a random pivot; returns the pivot's final index."""
    p = rng.randint(lo, hi)
    nums[p], nums[hi] = nums[hi], nums[p]      # move pivot to the end
    pivot, store = nums[hi], lo
    for i in range(lo, hi):
        if nums[i] < pivot:
            nums[i], nums[store] = nums[store], nums[i]
            store += 1
    nums[store], nums[hi] = nums[hi], nums[store]
    return store


def quicksort(nums: list[int], seed: int = 0) -> None:
    """In place. Average O(n log n); a random pivot makes the O(n^2) worst case unlikely."""
    rng = random.Random(seed)
    stack = [(0, len(nums) - 1)]               # explicit stack: no recursion limit
    while stack:
        lo, hi = stack.pop()
        if lo < hi:
            p = partition(nums, lo, hi, rng)
            stack.append((lo, p - 1))
            stack.append((p + 1, hi))


def kth_largest(nums: list[int], k: int, seed: int = 0) -> int:
    """Quickselect: partition, then continue into ONE side only. Average O(n)."""
    nums = nums[:]                             # don't mutate the caller's list
    rng = random.Random(seed)
    target = len(nums) - k                     # k-th largest = index n-k in sorted order
    lo, hi = 0, len(nums) - 1
    while True:
        p = partition(nums, lo, hi, rng)
        if p == target:
            return nums[p]
        if p < target:
            lo = p + 1
        else:
            hi = p - 1


def sort_colors(nums: list[int]) -> None:
    """Dutch national flag (values 0, 1, 2), one pass, in place.
    Invariant: [0, low) = 0s, [low, mid) = 1s, (high, end] = 2s, [mid, high] unknown."""
    low, mid, high = 0, 0, len(nums) - 1
    while mid <= high:
        if nums[mid] == 0:
            nums[low], nums[mid] = nums[mid], nums[low]
            low += 1; mid += 1
        elif nums[mid] == 1:
            mid += 1
        else:                                  # 2: swap to the back; don't advance mid,
            nums[mid], nums[high] = nums[high], nums[mid]   # the swapped-in value is unseen
            high -= 1


if __name__ == "__main__":
    head = sort_list(from_list([4, 2, 1, 3, 5, 2]))
    print("merge-sorted list:", to_list(head))
    assert to_list(head) == [1, 2, 2, 3, 4, 5]
    assert sort_list(None) is None and to_list(sort_list(from_list([7]))) == [7]

    rng = random.Random(42)
    for _ in range(200):                       # randomised check against sorted()
        xs = [rng.randint(-50, 50) for _ in range(rng.randint(0, 40))]
        ys = xs[:]
        quicksort(ys)
        assert ys == sorted(xs)
        if xs:
            k = rng.randint(1, len(xs))
            assert kth_largest(xs, k) == sorted(xs, reverse=True)[k - 1]
    data = [9, 4, 7, 1, 8, 2]
    quicksort(data)
    print("quicksort:", data, "| 2nd largest of [3,2,1,5,6,4]:", kth_largest([3, 2, 1, 5, 6, 4], 2))
    assert kth_largest([3, 2, 3, 1, 2, 4, 5, 5, 6], 4) == 4

    colors = [2, 0, 2, 1, 1, 0]
    sort_colors(colors)
    print("sort colors:", colors)
    assert colors == [0, 0, 1, 1, 2, 2]
    for xs in ([], [2], [2, 0], [1, 1, 1], [2, 2, 0, 0, 1]):
        ys = xs[:]; sort_colors(ys); assert ys == sorted(xs)
    print("all asserts passed")
