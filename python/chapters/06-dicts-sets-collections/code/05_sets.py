"""Sets: define, add/update, remove/discard, copy, loop, operations, methods, frozenset."""


def define() -> None:
    fruits = {"apple", "banana", "cherry", "apple"}
    colors = set(["red", "blue", "red", "green"])
    print(sorted(fruits), sorted(colors), len(fruits))   # sorted() only for stable printing
    empty_set, empty_dict = set(), {}
    print(type(empty_set).__name__, type(empty_dict).__name__)
    print(set("hello"), set(range(3)))                     # str order varies run to run


def add_remove_copy_loop() -> None:
    nums = {1, 2, 3}
    nums.add(4); nums.add(2)                   # adding an existing item: no effect
    nums.update([5, 6], (7,))                  # any iterables
    print(nums)
    nums.remove(1)
    nums.discard(60)                           # absent: silently ignored
    try:
        nums.remove(60)
    except KeyError as e:
        print("KeyError:", e)
    print(nums)
    backup = nums.copy()
    nums.clear()
    print(nums, backup)
    for n in backup:                           # order is not guaranteed
        print(n, end=" ")
    print()
    print("pop() ->", backup.pop(), backup)    # removes an ARBITRARY element


def operations() -> None:
    a, b = {1, 2, 3, 4}, {3, 4, 5, 6}
    print("union        a | b :", a | b, a.union(b))
    print("intersection a & b :", a & b, a.intersection(b))
    print("difference   a - b :", a - b, a.difference(b), "  b - a:", b - a)
    print("sym. diff    a ^ b :", a ^ b, a.symmetric_difference(b))
    print("method accepts any iterable:", a.union([9, 10]), " operator needs sets")
    try:
        a | [9]
    except TypeError as e:
        print("TypeError:", e)
    print("multi-arg:", a.intersection(b, {4, 99}))


def comparisons() -> None:
    small, big, other = {1, 2}, {1, 2, 3}, {7}
    print(small <= big, small.issubset(big), small < big, big >= small, big.issuperset(small))
    print(small.isdisjoint(other), small < small, small <= small)


def in_place_updates() -> None:
    s = {1, 2, 3, 4}
    s.intersection_update({2, 3, 9});         print("&= :", s)
    s.difference_update({3});                 print("-= :", s)
    s.symmetric_difference_update({2, 8});    print("^= :", s)
    s |= {100};                               print("|= :", s)


def frozen() -> None:
    fs = frozenset([1, 2, 3])
    try:
        fs.add(4)
    except AttributeError as e:
        print("AttributeError:", e)
    groups = {frozenset({"alice", "bob"}): "team A"}   # usable as a dict key
    print(groups[frozenset({"bob", "alice"})])
    print(fs | {9}, type(fs | {9}).__name__)            # result type follows the left operand


def dedupe_keep_order() -> None:
    items = ["b", "a", "b", "c", "a"]
    print("set          :", len(set(items)), "unique, but original order is not kept")
    print(list(dict.fromkeys(items)))                    # keeps first-seen order


if __name__ == "__main__":
    for f in (define, add_remove_copy_loop, operations, comparisons, in_place_updates, frozen, dedupe_keep_order):
        print(f"--- {f.__name__}")
        f()
