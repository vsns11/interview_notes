"""Chapter 7 - first-class functions, lambda, map/filter/sorted, functools."""
import functools
import operator
import time


def shout(text):
    return text.upper() + "!"


def apply_twice(fn, value):          # higher-order: takes a function
    return fn(fn(value))


def timed(fn):
    """Decorator that reports how long a call took (wraps keeps metadata)."""
    @functools.wraps(fn)
    def wrapper(*args, **kwargs):
        t0 = time.perf_counter()
        result = fn(*args, **kwargs)
        ms = (time.perf_counter() - t0) * 1000
        print(f"  [{fn.__name__} ran in {'under' if ms < 500 else 'over'} 500 ms]")
        return result
    return wrapper


def no_wraps(fn):
    def wrapper(*args, **kwargs):
        return fn(*args, **kwargs)
    return wrapper


@timed
def slow_sum(n):
    """Add 0..n-1 the slow way."""
    return sum(range(n))


@no_wraps
def documented():
    """I have a docstring."""


if __name__ == "__main__":
    print("== functions are objects ==")
    yell = shout                                  # assign to a name
    print(" ", yell("hello"), "| name:", yell.__name__)
    table = {"up": str.upper, "low": str.lower}  # store in a dict
    print(" ", table["up"]("mixed"), table["low"]("MiXeD"))
    print(" ", apply_twice(shout, "hey"))         # pass as argument

    print("\n== lambda, map, filter, comprehension ==")
    nums = [5, -2, 8, -7, 3]
    print("  map abs:      ", list(map(abs, nums)))
    print("  filter > 0:   ", list(filter(lambda n: n > 0, nums)))
    print("  comprehension:", [n * n for n in nums if n > 0])
    print("  map is lazy:  ", map(abs, nums).__class__.__name__)

    print("\n== sorted / min / max with key ==")
    people = [("Ana", 31), ("bob", 25), ("Cy", 31), ("dee", 19)]
    print("  by age:            ", sorted(people, key=lambda p: p[1]))
    print("  by age desc, name: ", sorted(people, key=lambda p: (-p[1], p[0])))
    print("  case-insensitive:  ", sorted(["bob", "Ana", "dee", "Cy"], key=str.lower))
    print("  itemgetter(1):     ", sorted(people, key=operator.itemgetter(1))[0])
    print("  oldest:            ", max(people, key=operator.itemgetter(1)))

    print("\n== functools.partial ==")
    int_from_binary = functools.partial(int, base=2)
    print("  int_from_binary('1011') =", int_from_binary("1011"))

    print("\n== functools.reduce ==")
    print("  product of 1..5 =", functools.reduce(operator.mul, range(1, 6)))
    print("  reduce on [] with initial 0 =", functools.reduce(operator.add, [], 0))

    print("\n== decorators and functools.wraps ==")
    print("  slow_sum(1_000_000) =", slow_sum(1_000_000))
    print("  with wraps:    name =", slow_sum.__name__, "| doc =", slow_sum.__doc__)
    print("  without wraps: name =", documented.__name__, "| doc =", documented.__doc__)

    print("\n== lru_cache / cache ==")
    @functools.lru_cache(maxsize=128)
    def square_slowly(n):
        time.sleep(0.05)
        return n * n
    for n in (4, 4, 5, 4):
        square_slowly(n)
    print(" ", square_slowly.cache_info())
    square_slowly.cache_clear()
    print("  after cache_clear:", square_slowly.cache_info())

    @functools.cache
    def expensive(x):
        return x * 10
    try:
        expensive([1, 2])
    except TypeError as exc:
        print("  unhashable argument ->", exc)
