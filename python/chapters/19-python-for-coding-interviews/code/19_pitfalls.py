"""19.19 Interview pitfalls: each one demonstrated, then fixed."""
import copy
import sys


def append_bad(x, acc=[]):          # default list created ONCE, at def time
    acc.append(x)
    return acc


def append_good(x, acc=None):
    if acc is None:
        acc = []
    acc.append(x)
    return acc


def depth(k):
    return 0 if k == 0 else 1 + depth(k - 1)


if __name__ == "__main__":
    # 1. mutable default argument
    print("1.", append_bad(1), append_bad(2), "<- shared list")
    print("   fixed:", append_good(1), append_good(2))

    # 2. grid built with * shares the same row object
    bad = [[0] * 3] * 2
    bad[0][0] = 9
    good = [[0] * 3 for _ in range(2)]
    good[0][0] = 9
    print("2. [[0]*3]*2 ->", bad, "| comprehension ->", good)

    # 3. shallow vs deep copy
    grid = [[1, 2], [3, 4]]
    shallow = grid[:]            # same as list(grid) / grid.copy()
    deep = copy.deepcopy(grid)
    grid[0][0] = 99
    print("3. shallow sees change:", shallow[0][0], "| deep does not:", deep[0][0])

    # 4. appending the SAME list object in backtracking
    path, results = [], []
    for x in (1, 2):
        path.append(x)
        results.append(path)          # bug: stores a reference
    print("4. stored references:", results, "| use path[:] ->", [[1], [1, 2]])

    # 5. recursion depth
    print("5. default limit:", sys.getrecursionlimit())
    try:
        depth(3000)
    except RecursionError:
        print("   depth(3000) -> RecursionError; use an explicit stack or raise the limit")

    # 6. integer division and modulo with negatives
    print("6. 7 // 2 =", 7 // 2, "| -7 // 2 =", -7 // 2, "| int(-7 / 2) =", int(-7 / 2),
          "| -7 % 3 =", -7 % 3)
    print("   7 / 2 =", 7 / 2, "(always float) | round(2.5) =", round(2.5), "| round(3.5) =", round(3.5))

    # 7. float comparisons
    import math
    print("7. 0.1 + 0.2 == 0.3:", 0.1 + 0.2 == 0.3, "| isclose:", math.isclose(0.1 + 0.2, 0.3))

    # 8. is vs ==
    a = [1, 2]
    b = [1, 2]
    print("8. a == b:", a == b, "| a is b:", a is b, "| use `is` only for None/sentinels")

    # 9. modifying a list while iterating
    nums = [1, 2, 2, 3]
    for n in nums:
        if n == 2:
            nums.remove(n)
    print("9. remove while iterating ->", nums, "| comprehension ->", [n for n in [1, 2, 2, 3] if n != 2])

    # 10. string building in a loop
    parts = [str(i) for i in range(5)]
    print("10. ''.join(parts) =", "".join(parts), "(O(n); repeated += can be O(n^2))")

    # 11. sort() returns None
    xs = [3, 1, 2]
    print("11. xs.sort() returns", xs.sort(), "- xs is now", xs, "| sorted() returns a new list")

    # 12. shadowing built-ins
    list_ = [1, 2]        # trailing underscore instead of `list = [...]`
    print("12. list still callable:", list("ab"), list_)
