"""Chapter 21: check the concrete results quoted on the cheat sheets.

Every assert below corresponds to a value printed in README.md. If a future Python
version changes behaviour, this script fails and points at the line to update.
"""
import copy
import heapq
import math
import re
from collections import ChainMap, Counter, OrderedDict, defaultdict, deque, namedtuple
from dataclasses import dataclass
from decimal import Decimal
from fractions import Fraction
from functools import cache, cmp_to_key, partial, reduce
from itertools import (accumulate, batched, chain, combinations, count, cycle, islice,
                       pairwise, permutations, repeat, takewhile)


def sheet_21_2_types_operators() -> None:
    falsy = [None, False, 0, 0.0, 0j, Decimal(0), Fraction(0), "", [], (), {}, set(), range(0)]
    assert not any(falsy)
    assert True + 1 == 2 and isinstance(True, int)
    assert int("42") == 42 and int("ff", 16) == 255 and int(3.9) == 3
    assert list("ab") == ["a", "b"] and ord("A") == 65 and chr(65) == "A"
    assert 7 / 2 == 3.5 and -7 // 2 == -4 and -7 % 3 == 2 and -2 ** 2 == -4
    assert ("" or "x") == "x" and ~5 == -6
    assert round(2.5) == 2 and round(3.5) == 4
    assert divmod(-7, 2) == (-4, 1) and pow(2, 10, 1000) == 24
    assert 0.1 + 0.2 != 0.3 and math.isclose(0.1 + 0.2, 0.3)
    assert Decimal("0.1") + Decimal("0.2") == Decimal("0.3")
    x = 5
    assert 0 < x < 10


def sheet_21_4_strings() -> None:
    s = "  Hello, World  "
    assert s.strip() == "Hello, World"
    assert "report.py".removesuffix(".py") == "report"
    assert "a,b,c".split(",", 1) == ["a", "b,c"]
    assert "key: value".partition(":") == ("key", ":", " value")
    assert "abc".find("z") == -1
    try:
        "abc".index("z")
        raise AssertionError("index should raise")
    except ValueError:
        pass
    assert "42".zfill(5) == "00042"
    assert "abc".translate(str.maketrans("abc", "xyz")) == "xyz"
    assert "Straße".casefold() == "strasse"
    assert f"{3.14159:.2f}" == "3.14" and f"{1234567:,}" == "1,234,567"
    assert f"{0.256:.1%}" == "25.6%" and f"{42:05d}" == "00042"
    assert f"{'hi':>6}" == "    hi" and f"{'hi':*^6}" == "**hi**"
    assert f"{255:#x}" == "0xff" and f"{255:b}" == "11111111"
    assert f"{1234.5:e}" == "1.234500e+03"
    x = 3
    assert f"{x=}" == "x=3"
    m = re.search(r"(?P<year>\d{4})-(?P<month>\d{2})", "due 2026-09-28")
    assert m and m["year"] == "2026" and m.group("month") == "09"
    assert re.sub(r"\s+", " ", "a   b\t c") == "a b c"


def sheet_21_5_methods() -> None:
    a = [3, 1, 2]
    assert a.append(4) is None and a == [3, 1, 2, 4]
    a.extend([5, 6])
    a.insert(0, 0)
    assert a.pop() == 6 and a.pop(0) == 0
    assert a.sort() is None and a == [1, 2, 3, 4, 5]
    assert a[::-1] == [5, 4, 3, 2, 1] and a[::2] == [1, 3, 5]
    a[1:3] = [9, 9, 9]
    assert a == [1, 9, 9, 9, 4, 5]
    bad = [[0] * 2] * 2
    bad[0][0] = 1
    assert bad == [[1, 0], [1, 0]]

    P = namedtuple("P", "x y")
    p = P(1, 2)
    assert p._asdict() == {"x": 1, "y": 2} and p._replace(x=5) == P(5, 2)

    d = {"a": 1}
    assert d.get("z") is None and d.setdefault("b", 2) == 2
    assert (d | {"a": 9}) == {"a": 9, "b": 2}
    d["c"] = 3
    assert d.popitem() == ("c", 3)
    shared = dict.fromkeys(["x", "y"], [])
    shared["x"].append(1)
    assert shared["y"] == [1]
    assert d.keys() & {"a", "q"} == {"a"}

    s, t = {1, 2, 3}, {3, 4}
    assert s | t == {1, 2, 3, 4} and s & t == {3} and s - t == {1, 2} and s ^ t == {1, 2, 4}
    assert {1, 2} <= s and {1} < s and s.isdisjoint({9})
    s.discard(99)                      # no error
    assert hash(frozenset({1, 2})) == hash(frozenset({2, 1}))


def sheet_21_6_iteration() -> None:
    xs = [3, -1, 2]
    assert [x for x in xs if x > 0] == [3, 2]
    assert ["pos" if x > 0 else "neg" for x in xs] == ["pos", "neg", "pos"]
    assert [y for row in [[1, 2], [3]] for y in row] == [1, 2, 3]
    it = iter([1, 2])
    assert iter(it) is it and next(it) == 1 and next(it) == 2 and next(it, "done") == "done"

    def gen():
        yield 1
        yield from [2, 3]
    g = gen()
    assert list(g) == [1, 2, 3] and list(g) == []


def sheet_21_7_functions() -> None:
    def f(a, /, b, *args, c, **kwargs):
        return a, b, args, c, kwargs
    assert f(1, 2, 3, c=4, d=5) == (1, 2, (3,), 4, {"d": 5})
    try:
        f(a=1, b=2, c=3)              # a is positional-only
        raise AssertionError
    except TypeError:
        pass

    @cache
    def fib(n):
        return n if n < 2 else fib(n - 1) + fib(n - 2)
    assert fib(50) == 12586269025
    assert partial(pow, 2)(10) == 1024
    assert reduce(lambda a, b: a * b, [1, 2, 3, 4], 1) == 24


def sheet_21_8_oop() -> None:
    class A:
        def who(self):
            return "A"

    class B(A):
        def who(self):
            return "B" + super().who()

    class C(A):
        def who(self):
            return "C" + super().who()

    class D(B, C):
        pass

    assert [k.__name__ for k in D.__mro__] == ["D", "B", "C", "A", "object"]
    assert D().who() == "BCA"

    class OnlyEq:
        def __eq__(self, other):
            return True
    assert OnlyEq.__hash__ is None      # __eq__ without __hash__ -> unhashable

    class Secret:
        def __init__(self):
            self.__token = 1
    assert "_Secret__token" in vars(Secret())   # name mangling

    @dataclass(frozen=True, slots=True)
    class Point:
        x: float
        y: float = 0.0
    assert Point(1) == Point(1, 0.0) and hash(Point(1)) == hash(Point(1, 0.0))


def sheet_21_9_exceptions() -> None:
    assert issubclass(KeyboardInterrupt, BaseException) and not issubclass(KeyboardInterrupt, Exception)
    assert issubclass(KeyError, LookupError) and issubclass(IndexError, LookupError)
    assert issubclass(ZeroDivisionError, ArithmeticError) and issubclass(OverflowError, ArithmeticError)
    assert issubclass(UnicodeError, ValueError) and issubclass(UnboundLocalError, NameError)
    assert issubclass(FileNotFoundError, OSError) and issubclass(TimeoutError, OSError)
    assert issubclass(ModuleNotFoundError, ImportError)
    assert issubclass(RecursionError, RuntimeError) and issubclass(NotImplementedError, RuntimeError)
    try:
        try:
            {}["k"]
        except KeyError as err:
            raise ValueError("bad config") from err
    except ValueError as e:
        assert isinstance(e.__cause__, KeyError)


def sheet_21_11_tools() -> None:
    assert list(islice(count(10, 5), 3)) == [10, 15, 20]
    assert list(islice(cycle("ab"), 3)) == ["a", "b", "a"] and list(repeat(0, 3)) == [0, 0, 0]
    assert list(chain([1, 2], [3])) == [1, 2, 3]
    assert list(takewhile(lambda x: x < 3, [1, 2, 5, 1])) == [1, 2]
    assert list(accumulate([1, 2, 3])) == [1, 3, 6]
    assert list(pairwise("abc")) == [("a", "b"), ("b", "c")]
    assert list(batched("abcde", 2)) == [("a", "b"), ("c", "d"), ("e",)]
    assert len(list(permutations("abc", 2))) == 6
    assert ["".join(c) for c in combinations("abc", 2)] == ["ab", "ac", "bc"]
    assert sorted([3, 1, 2], key=cmp_to_key(lambda a, b: b - a)) == [3, 2, 1]

    c = Counter("mississippi")
    assert c["z"] == 0 and c.most_common(1)[0][1] == 4
    dd = defaultdict(list)
    _ = dd["missing"]
    assert "missing" in dd                       # a read inserted the key
    q = deque(range(5), maxlen=3)
    assert list(q) == [2, 3, 4]
    od = OrderedDict(a=1, b=2)
    od.move_to_end("a")
    assert list(od) == ["b", "a"]
    assert ChainMap({"x": 1}, {"x": 2, "y": 3})["x"] == 1

    h = [5, 1, 8]
    heapq.heapify(h)
    assert h[0] == 1 and heapq.nlargest(2, [4, 9, 1, 7]) == [9, 7]


def sheet_21_18_copy() -> None:
    grid = [[1], [2]]
    shallow, deep = grid.copy(), copy.deepcopy(grid)
    grid[0].append(9)
    assert shallow[0] == [1, 9] and deep[0] == [1]


def sheet_21_19_numpy_pandas() -> None:
    import numpy as np
    import pandas as pd
    a = np.array([[1, 2, 3], [4, 5, 6]])
    assert a.shape == (2, 3) and a.sum(axis=0).tolist() == [5, 7, 9]
    assert a.mean(axis=1).tolist() == [2.0, 5.0]
    view = a[:, 1]
    view[0] = 99
    assert a[0, 1] == 99                          # slices are views
    (a + np.array([10, 20, 30])).tolist()         # broadcasting works
    df = pd.DataFrame({"k": ["a", "b", "c"], "v": [1, 2, 3]}, index=[10, 20, 30])
    assert df.loc[10:20, "v"].tolist() == [1, 2]  # label slice includes the end
    assert df.iloc[0:2, 1].tolist() == [1, 2]     # position slice excludes the end
    s = pd.Series([1, 2], index=pd.to_datetime(["2026-01-15", "2026-02-15"]))
    assert s.resample("ME").sum().tolist() == [1, 2]


if __name__ == "__main__":
    checks = [sheet_21_2_types_operators, sheet_21_4_strings, sheet_21_5_methods,
              sheet_21_6_iteration, sheet_21_7_functions, sheet_21_8_oop,
              sheet_21_9_exceptions, sheet_21_11_tools, sheet_21_18_copy,
              sheet_21_19_numpy_pandas]
    for check in checks:
        check()
        print(f"OK  {check.__name__}")
    print(f"all {len(checks)} cheat-sheet checks passed")
