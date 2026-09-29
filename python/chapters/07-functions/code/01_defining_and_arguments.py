"""Chapter 7 - defining functions and every kind of argument."""


def say_hi():
    """A function with no parameters and no return statement."""
    print("hi from say_hi()")


def cube(x):
    """Return x cubed."""
    return x ** 3


def todo():
    pass  # placeholder body - keeps the syntax valid


def book_flight(origin, destination, seat="economy", *, meal="standard"):
    """origin/destination: positional-or-keyword; seat: default; meal: keyword-only."""
    return f"{origin}->{destination} [{seat}, meal={meal}]"


def clamp(value, low, high, /):
    """All three parameters are positional-only (before the /)."""
    return max(low, min(value, high))


def total(*numbers, start=0):
    """*numbers collects extra positional args into a tuple."""
    print("  numbers is a", type(numbers).__name__, numbers)
    return sum(numbers, start)


def make_tag(tag, /, text, **attrs):
    """**attrs collects extra keyword args into a dict."""
    attr_str = "".join(f' {k}="{v}"' for k, v in attrs.items())
    return f"<{tag}{attr_str}>{text}</{tag}>"


def min_max_mean(values):
    """Return several values - really one tuple."""
    return min(values), max(values), sum(values) / len(values)


def append_bad(item, bucket=[]):          # BUG: default evaluated once
    bucket.append(item)
    return bucket


def append_good(item, bucket=None):       # FIX: None sentinel
    if bucket is None:
        bucket = []
    bucket.append(item)
    return bucket


def try_call(label, fn):
    try:
        fn()
    except TypeError as exc:
        print(f"  {label}: TypeError: {exc}")


if __name__ == "__main__":
    print("== basics ==")
    say_hi()
    print("say_hi() returned:", say_hi())
    print("cube(3) =", cube(3))
    print("todo() returned:", todo())

    print("\n== positional, keyword, default, keyword-only ==")
    print(book_flight("SFO", "JFK"))
    print(book_flight(destination="JFK", origin="SFO", seat="aisle"))
    print(book_flight("SFO", "JFK", meal="vegan"))
    try_call("missing arg", lambda: book_flight("SFO"))
    try_call("meal given positionally", lambda: book_flight("SFO", "JFK", "aisle", "vegan"))
    try_call("same arg twice", lambda: book_flight("SFO", origin="LAX"))

    print("\n== positional-only ==")
    print("clamp(15, 0, 10) =", clamp(15, 0, 10))
    try_call("clamp(value=15, ...)", lambda: clamp(value=15, low=0, high=10))

    print("\n== *args / **kwargs ==")
    print("total(1, 2, 3) =", total(1, 2, 3))
    print("total() =", total())
    print(make_tag("a", "docs", href="https://docs.python.org", target="_blank"))

    print("\n== unpacking at the call site ==")
    nums = [4, 5, 6]
    opts = {"seat": "window", "meal": "kosher"}
    print("total(*nums) =", total(*nums))
    print(book_flight("SFO", "JFK", **opts))

    print("\n== returning several values ==")
    result = min_max_mean([3, 9, 6])
    print("raw result:", result, type(result).__name__)
    lo, hi, avg = result
    print(f"lo={lo} hi={hi} avg={avg}")

    print("\n== mutable default pitfall ==")
    print(append_bad("a"), append_bad("b"))       # same list twice!
    print("default is now:", append_bad.__defaults__)
    print(append_good("a"), append_good("b"))
