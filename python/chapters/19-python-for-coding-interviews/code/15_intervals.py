"""19.15 Intervals: merge, insert, intersect two lists, minimum meeting rooms, erase overlaps."""
import heapq
from itertools import accumulate


def merge(intervals: list[list[int]]) -> list[list[int]]:
    out: list[list[int]] = []
    for start, end in sorted(intervals):             # sort by start
        if out and start <= out[-1][1]:              # overlaps the last kept one
            out[-1][1] = max(out[-1][1], end)
        else:
            out.append([start, end])
    return out


def insert(intervals: list[list[int]], new: list[int]) -> list[list[int]]:
    """`intervals` is sorted and non-overlapping. Three phases, O(n), no re-sort."""
    out, i, n = [], 0, len(intervals)
    s, e = new
    while i < n and intervals[i][1] < s:             # 1. entirely before `new`
        out.append(intervals[i]); i += 1
    while i < n and intervals[i][0] <= e:            # 2. overlapping -> absorb
        s, e = min(s, intervals[i][0]), max(e, intervals[i][1]); i += 1
    out.append([s, e])
    out.extend(intervals[i:])                        # 3. entirely after
    return out


def intersect(a: list[list[int]], b: list[list[int]]) -> list[list[int]]:
    """Two sorted, internally disjoint lists: two pointers; advance the one that ends first."""
    i = j = 0
    out = []
    while i < len(a) and j < len(b):
        lo, hi = max(a[i][0], b[j][0]), min(a[i][1], b[j][1])
        if lo <= hi:
            out.append([lo, hi])
        if a[i][1] < b[j][1]:
            i += 1
        else:
            j += 1
    return out


def min_rooms_heap(meetings: list[tuple[int, int]]) -> int:
    """Min-heap of end times of meetings in progress; its size peak = rooms needed."""
    ends: list[int] = []
    for start, end in sorted(meetings):
        if ends and ends[0] <= start:                # earliest room is free again
            heapq.heapreplace(ends, end)
        else:
            heapq.heappush(ends, end)
    return len(ends)


def min_rooms_sweep(meetings: list[tuple[int, int]]) -> int:
    """Sweep line: +1 at each start, -1 at each end; ends sort before starts at a tie."""
    events = sorted([(s, 1) for s, _ in meetings] + [(e, -1) for _, e in meetings])
    return max(accumulate(d for _, d in events), default=0)


def erase_overlaps(intervals: list[list[int]]) -> int:
    """Fewest removals to make the rest non-overlapping = n - max non-overlapping (sort by end)."""
    kept, last_end = 0, float("-inf")
    for s, e in sorted(intervals, key=lambda iv: iv[1]):
        if s >= last_end:
            kept, last_end = kept + 1, e
    return len(intervals) - kept


if __name__ == "__main__":
    assert merge([[1, 3], [8, 10], [2, 6], [15, 18]]) == [[1, 6], [8, 10], [15, 18]]
    assert merge([[1, 4], [4, 5]]) == [[1, 5]]                   # touching counts as overlap
    print("merged:", merge([[1, 3], [8, 10], [2, 6], [15, 18]]))

    assert insert([[1, 3], [6, 9]], [2, 5]) == [[1, 5], [6, 9]]
    assert insert([[1, 2], [3, 5], [6, 7], [8, 10], [12, 16]], [4, 8]) == [[1, 2], [3, 10], [12, 16]]
    assert insert([], [5, 7]) == [[5, 7]]
    print("insert [4,8]:", insert([[1, 2], [3, 5], [6, 7], [8, 10], [12, 16]], [4, 8]))

    a = [[0, 2], [5, 10], [13, 23], [24, 25]]
    b = [[1, 5], [8, 12], [15, 24], [25, 26]]
    got = intersect(a, b)
    print("intersection:", got)
    assert got == [[1, 2], [5, 5], [8, 10], [15, 23], [24, 24], [25, 25]]

    day = [(9, 10), (9, 12), (10, 11), (11, 13), (12, 13)]
    assert min_rooms_heap(day) == min_rooms_sweep(day) == 2
    assert min_rooms_heap([(0, 30), (5, 10), (15, 20)]) == 2
    assert min_rooms_sweep([(1, 5), (2, 6), (3, 7)]) == 3 and min_rooms_sweep([]) == 0
    print("rooms needed:", min_rooms_heap(day))

    assert erase_overlaps([[1, 2], [2, 3], [3, 4], [1, 3]]) == 1
    assert erase_overlaps([[1, 2], [1, 2], [1, 2]]) == 2
    print("all asserts passed")
