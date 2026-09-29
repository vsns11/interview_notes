"""sort() vs sorted(), key functions, reverse, stability, multi-key sorts."""
from operator import attrgetter, itemgetter


def sort_vs_sorted() -> None:
    nums = [5, 2, 9, 1]
    new = sorted(nums)                  # returns a NEW list, works on any iterable
    print(new, nums)
    result = nums.sort()                # sorts IN PLACE, returns None
    print(nums, result)
    print(sorted("banana"), sorted({3, 1, 2}), sorted({"b": 1, "a": 2}))


def keys() -> None:
    words = ["banana", "Apple", "cherry", "Date"]
    print(sorted(words))                              # uppercase first (code points)
    print(sorted(words, key=str.casefold))            # case-insensitive
    print(sorted(words, key=len))                     # by length
    print(sorted(words, key=len, reverse=True))
    people = [("Ann", 31), ("Bob", 25), ("Cid", 31)]
    print(sorted(people, key=itemgetter(1)))          # same as key=lambda p: p[1]


def stability_multikey() -> None:
    people = [("Ann", "HR", 31), ("Bob", "IT", 25), ("Cid", "HR", 25), ("Dee", "IT", 31)]
    by_age = sorted(people, key=itemgetter(2))
    print("by age (ties keep original order):", [p[0] for p in by_age])
    # multi-key: dept ascending, then age descending
    print("tuple key :", [p[0] for p in sorted(people, key=lambda p: (p[1], -p[2]))])
    # same result via two stable passes: sort by the SECONDARY key first
    tmp = sorted(people, key=itemgetter(2), reverse=True)
    print("two passes:", [p[0] for p in sorted(tmp, key=itemgetter(1))])


def mixed_types() -> None:
    try:
        sorted([3, "a", 1])
    except TypeError as e:
        print("TypeError:", e)


if __name__ == "__main__":
    for f in (sort_vs_sorted, keys, stability_multikey, mixed_types):
        print(f"--- {f.__name__}")
        f()
