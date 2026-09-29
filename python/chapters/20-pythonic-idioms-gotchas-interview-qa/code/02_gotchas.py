"""20.2 Top Python gotchas: demo of the bug, then the fix."""
import copy
import math
import subprocess
import sys
import tempfile
import textwrap
from collections import defaultdict
from decimal import Decimal
from fractions import Fraction
from pathlib import Path


def header(n: int, title: str) -> None:
    print(f"\n[{n}] {title}")


# 1 mutable default arguments
def add_item_bad(item, bucket=[]):
    bucket.append(item)
    return bucket


def add_item_good(item, bucket=None):
    if bucket is None:
        bucket = []
    bucket.append(item)
    return bucket


# 8 class vs instance attributes
class DogBad:
    tricks = []                     # ONE list shared by every instance

    def __init__(self, name):
        self.name = name


class DogGood:
    species = "canis familiaris"    # shared, immutable: fine as a class attribute

    def __init__(self, name):
        self.name = name
        self.tricks = []            # per-instance


if __name__ == "__main__":
    header(1, "mutable default argument")
    print("bad :", add_item_bad("a"), add_item_bad("b"), "| defaults:", add_item_bad.__defaults__)
    print("good:", add_item_good("a"), add_item_good("b"))

    header(2, "late-binding closures")
    fns = [lambda: i for i in range(3)]
    print("bad :", [f() for f in fns])
    fns = [lambda i=i: i for i in range(3)]          # bind now via a default
    print("good:", [f() for f in fns])

    header(3, "is vs ==")
    a = [1, 2, 3]
    b = [1, 2, 3]
    print("a == b:", a == b, "| a is b:", a is b)
    x = None
    print("x is None:", x is None, "(the right way to test for None)")

    header(4, "integer caching (CPython detail)")
    small_a, small_b = int("256"), int("256")
    big_a, big_b = int("257"), int("257")      # built at runtime: no constant folding
    print("256 is 256:", small_a is small_b, "| 257 is 257:", big_a is big_b,
          "| 257 == 257:", big_a == big_b)

    header(5, "modifying a list while iterating")
    nums = [1, 2, 2, 3, 4]
    for n in nums:
        if n % 2 == 0:
            nums.remove(n)
    print("bad :", nums)
    nums = [1, 2, 2, 3, 4]
    print("good:", [n for n in nums if n % 2], "| or iterate a copy: for n in nums[:]")
    d = {"a": 1, "b": 2}
    try:
        for k in d:
            del d[k]
    except RuntimeError as e:
        print("dict:", e)
    d = {"a": 1, "b": 2}
    for k in list(d):
        if d[k] == 1:
            del d[k]
    print("dict good:", d)

    header(6, "shallow copy")
    config = {"name": "svc", "ports": [80]}
    shallow = config.copy()
    deep = copy.deepcopy(config)
    shallow["ports"].append(443)
    print("original after shallow edit:", config, "| deep copy:", deep)
    alias = config                               # not a copy at all
    print("alias is config:", alias is config)

    header(7, "shared mutable values: dict.fromkeys and defaultdict reads")
    bad = dict.fromkeys(["a", "b"], [])
    bad["a"].append(1)
    print("fromkeys bad :", bad)
    good = {k: [] for k in ["a", "b"]}
    good["a"].append(1)
    print("fromkeys good:", good)
    dd = defaultdict(list)
    if dd["missing"]:                            # a READ inserts the key
        pass
    print("defaultdict after a read:", dict(dd), "| use `'missing' in dd` or dd.get()")

    header(8, "class vs instance attributes")
    r, s = DogBad("rex"), DogBad("sam")
    r.tricks.append("roll")
    print("bad : sam.tricks =", s.tricks)
    r, s = DogGood("rex"), DogGood("sam")
    r.tricks.append("roll")
    print("good: sam.tricks =", s.tricks)
    r.species = "wolf"                           # creates an instance attribute
    print("assignment shadows:", r.species, "|", s.species, "|", DogGood.species)

    header(9, "circular imports")
    with tempfile.TemporaryDirectory() as tmp:
        Path(tmp, "mod_a.py").write_text(textwrap.dedent("""
            from mod_b import helper_b      # runs mod_b before mod_a has finished
            def helper_a():
                return "a"
        """))
        Path(tmp, "mod_b.py").write_text(textwrap.dedent("""
            from mod_a import helper_a      # mod_a is half-initialised here
            def helper_b():
                return "b"
        """))
        res = subprocess.run([sys.executable, "-c", "import mod_a"], cwd=tmp,
                             capture_output=True, text=True)
        print("bad :", res.stderr.strip().splitlines()[-1].split(" (")[0])  # wording varies by version
        Path(tmp, "mod_b.py").write_text(textwrap.dedent("""
            import mod_a                    # import the module, look up names later
            def helper_b():
                return "b" + mod_a.helper_a()
        """))
        res = subprocess.run([sys.executable, "-c", "import mod_a, mod_b; print(mod_b.helper_b())"],
                             cwd=tmp, capture_output=True, text=True)
        print("good:", res.stdout.strip())

    header(10, "floating point")
    print("0.1 + 0.2 =", 0.1 + 0.2, "| == 0.3:", 0.1 + 0.2 == 0.3)
    print("math.isclose:", math.isclose(0.1 + 0.2, 0.3))
    print("Decimal('0.1') + Decimal('0.2') =", Decimal("0.1") + Decimal("0.2"),
          "| Decimal(0.1) =", str(Decimal(0.1))[:22] + "...")
    print("Fraction(1, 10) + Fraction(2, 10) =", Fraction(1, 10) + Fraction(2, 10))
    print("round(2.675, 2) =", round(2.675, 2), "| round(0.5), round(1.5) =", round(0.5), round(1.5))
    print("float('nan') == float('nan'):", float("nan") == float("nan"))

    header(11, "bare except")
    def risky():
        raise KeyboardInterrupt            # simulates Ctrl+C
    try:
        try:
            risky()
        except:                            # noqa: E722 - swallows Ctrl+C / SystemExit too
            print("bad : bare except swallowed KeyboardInterrupt")
    except KeyboardInterrupt:
        pass
    try:
        try:
            risky()
        except Exception:
            print("never printed")
    except KeyboardInterrupt:
        print("good: `except Exception` let KeyboardInterrupt through")

    header(12, "shadowing built-ins")
    list = [3, 1, 2]                       # noqa: A001 - deliberately bad
    try:
        list("abc")
    except TypeError as e:
        print("bad :", e)
    del list                               # removes the shadow; builtin visible again
    print("good:", list("abc"))

    header(13, "bonus: `and`/`or` return operands, .sort() returns None")
    print("'' or 'default' ->", "" or "default", "| 0 and 1 ->", 0 and 1)
    print("sorted() returns list, .sort() returns", [3, 1].sort())
