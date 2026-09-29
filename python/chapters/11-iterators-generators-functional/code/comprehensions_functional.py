"""Comprehensions, map/filter/reduce, any/all, zip(strict=True)."""
from functools import reduce
import operator

if __name__ == "__main__":
    nums = [1, 2, 3, 4, 5, 6]

    # --- the four comprehensions
    print("list:", [n * n for n in nums if n % 2 == 0])
    print("dict:", {n: n * n for n in nums if n > 3})
    print("set:", {n % 3 for n in nums})
    print("gen :", sum(n * n for n in nums))

    # conditional expression (if-else goes BEFORE for) vs filter (if goes AFTER)
    print("labels:", ["even" if n % 2 == 0 else "odd" for n in nums[:4]])

    # nested: read the for-clauses left to right like nested loops
    matrix = [[1, 2, 3], [4, 5, 6]]
    print("flatten:", [x for row in matrix for x in row])
    print("transpose:", [[row[i] for row in matrix] for i in range(3)])

    # walrus: compute once, filter and keep
    data = ["3", "x", "10", "7"]
    print("walrus:", [v for s in data if s.isdigit() and (v := int(s)) > 5])

    # dict inversion and zip
    prices = {"apple": 3, "pear": 2}
    print("invert:", {v: k for k, v in prices.items()})

    # --- map / filter / reduce vs comprehensions
    print("map:", list(map(str.upper, ["a", "b"])), "| comp:", [s.upper() for s in ["a", "b"]])
    print("filter:", list(filter(None, [0, 1, "", "hi", None])), "(filter(None, ...) keeps truthy)")
    print("reduce sum:", reduce(operator.add, nums), "| sum():", sum(nums))
    print("reduce product:", reduce(operator.mul, nums, 1))
    print("map over two iterables:", list(map(operator.add, [1, 2], [10, 20])))

    # --- any / all short-circuit
    def check(x):
        print(f"  check({x})", end="")
        return x > 2
    result = any(check(x) for x in [1, 3, 5])
    print("\n  any ->", result, "(stopped after 3; 5 never checked)")
    print("all([]) =", all([]), "| any([]) =", any([]))

    # --- zip, strict
    names, scores = ["a", "b", "c"], [90, 80]
    print("zip truncates silently:", list(zip(names, scores)))
    try:
        list(zip(names, scores, strict=True))
    except ValueError as e:
        print("strict=True:", e)
    pairs = [("a", 1), ("b", 2)]
    letters, numbers = zip(*pairs)           # "unzip"
    print("unzip:", letters, numbers)

    # --- late-binding closure trap and fix
    funcs = [lambda: i for i in range(3)]
    print("late binding:", [f() for f in funcs])
    funcs = [lambda i=i: i for i in range(3)]
    print("fixed with default arg:", [f() for f in funcs])

    # --- sorted / min / max with key
    people = [("ann", 31), ("bob", 25), ("cy", 31)]
    print("sorted by age desc, then name:", sorted(people, key=lambda p: (-p[1], p[0])))
    print("max by age (first wins on tie):", max(people, key=operator.itemgetter(1)))
