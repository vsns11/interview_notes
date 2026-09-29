"""heapq (priority queues, top-k) and bisect (binary search on sorted lists)."""
import bisect
import heapq


def heap_basics() -> None:
    h = [5, 1, 8, 3, 2]
    heapq.heapify(h)                          # O(n), in place; h[0] is the smallest
    print("heapified:", h, "min:", h[0])
    heapq.heappush(h, 0)
    print("pops:", [heapq.heappop(h) for _ in range(3)], "left:", h)
    print(heapq.nsmallest(2, [7, 3, 9, 1]), heapq.nlargest(2, [7, 3, 9, 1]))
    print("max-heap trick (negate):", -heapq.heappop(heapify_neg([3, 9, 4])))


def heapify_neg(xs):
    h = [-x for x in xs]
    heapq.heapify(h)
    return h


def priority_queue() -> None:
    tasks = []
    counter = 0                                # tie-breaker keeps insertion order & avoids comparing payloads
    for prio, name in [(2, "write docs"), (1, "fix prod bug"), (3, "refactor"), (1, "reply to CEO")]:
        heapq.heappush(tasks, (prio, counter, name))
        counter += 1
    while tasks:
        prio, _, name = heapq.heappop(tasks)
        print(f"  p{prio}: {name}")


def merge_sorted() -> None:
    print(list(heapq.merge([1, 4, 9], [2, 3, 10], [5])))


def bisect_demo() -> None:
    scores = [10, 20, 20, 30, 40]
    print(bisect.bisect_left(scores, 20), bisect.bisect_right(scores, 20), bisect.bisect(scores, 25))
    bisect.insort(scores, 25)
    print(scores)

    def contains(sorted_list, x):
        i = bisect.bisect_left(sorted_list, x)
        return i < len(sorted_list) and sorted_list[i] == x
    print(contains(scores, 30), contains(scores, 31))

    def grade(score, cutoffs=(60, 70, 80, 90), letters="FDCBA"):
        return letters[bisect.bisect(cutoffs, score)]
    print([grade(s) for s in (33, 60, 77, 89, 90, 100)])

    people = [("Ann", 25), ("Bob", 31), ("Cid", 40)]           # sorted by age
    i = bisect.bisect_left(people, 30, key=lambda p: p[1])      # key= is 3.10+
    print("first person aged >= 30:", people[i])


if __name__ == "__main__":
    for f in (heap_basics, priority_queue, merge_sorted, bisect_demo):
        print(f"--- {f.__name__}")
        f()
