"""Tuples: define, access, 'update', loop, operators, slice, unpack, join, methods, namedtuple."""
import itertools
import sys
import timeit
from collections import namedtuple
from typing import NamedTuple


def basics() -> None:
    t = (1, 2, 3, 4, 5)
    packed = "apple", "banana", "cherry"          # the commas make the tuple
    single, not_a_tuple, empty = (42,), (42), ()
    print(t, packed, type(single).__name__, type(not_a_tuple).__name__, empty)
    print(t[0], t[2], t[-1], t[1:4], t[::-1])
    try:
        t[1] = 20
    except TypeError as e:
        print("TypeError:", e)
    as_list = list(t); as_list[1] = 20; t2 = tuple(as_list)   # "update" = build a new tuple
    print("updated copy:", t2)
    for item in t:
        print(item, end=" ")
    print()
    a, b = (1, 2, 3), (4, 5, 6)
    print(a + b, a * 2, 3 in a, 7 not in a, (1, 2) < (1, 3))


def immutability_is_shallow() -> None:
    t = ([1, 2], "x")
    t[0].append(3)                                # the list INSIDE can still change
    print(t)
    try:
        hash(t)
    except TypeError as e:
        print("unhashable:", e)
    print("hashable tuple:", hash((1, "x")) == hash((1, "x")))


def unpacking() -> None:
    a, b, c = (1, "Hello", 3.14)
    print(a, b, c)
    try:
        w, x, y, z = (1, "Hello", 3.14)
    except ValueError as e:
        print("ValueError:", e)
    first, *middle, last = (1, 2, 3, 4, 5, 6)
    print(first, middle, last)
    head, *tail = [10]
    print(head, tail)
    x, y = 1, 2
    x, y = y, x                                   # swap
    print(x, y)
    for name, (lat, lon) in [("Oslo", (59.9, 10.7)), ("Lima", (-12.0, -77.0))]:
        print(f"{name}: {lat}, {lon}")
    _, _, ext = "archive.tar.gz".partition(".")   # _ = "don't care"
    print(ext)


def joining() -> None:
    parts = [(1, 2), (3, 4), (5, 6)]
    out = ()
    for p in parts:
        out += p                                  # new tuple every time: O(n^2) overall
    print("loop     :", out)
    print("+        :", (1, 2, 3) + (4, 5, 6))
    print("sum      :", sum(parts, ()))           # also O(n^2): repeated +
    print("genexpr  :", tuple(x for p in parts for x in p))
    print("chain    :", tuple(itertools.chain.from_iterable(parts)))
    big = [(i, i) for i in range(3000)]
    t_sum = min(timeit.repeat(lambda: sum(big, ()), number=3, repeat=3)) / 3
    t_chain = min(timeit.repeat(lambda: tuple(itertools.chain.from_iterable(big)), number=3, repeat=3)) / 3
    print(f"3000 tuples: sum {t_sum * 1000:.2f} ms vs chain {t_chain * 1000:.3f} ms")


def methods() -> None:
    fruit = ("apple", "banana", "cherry", "apple", "cherry")
    print(fruit.count("apple"), fruit.count("cherry"), fruit.index("banana"), fruit.index("cherry"))


def records() -> None:
    Point = namedtuple("Point", ["x", "y"])
    p = Point(3, 4)
    print(p, p.x, p[1], p._asdict(), p._replace(x=10))
    x, y = p                                      # still a tuple

    class Employee(NamedTuple):                   # typed, class-style syntax
        name: str
        dept: str
        salary: int = 0
    e = Employee("Ann", "HR")
    print(e, e.salary)
    print("sizes:", sys.getsizeof((1, 2, 3)), "tuple vs", sys.getsizeof([1, 2, 3]), "list (bytes)")


if __name__ == "__main__":
    for f in (basics, immutability_is_shallow, unpacking, joining, methods, records):
        print(f"--- {f.__name__}")
        f()
