"""Decorators: plain, with arguments, stacked, class decorators, functools.wraps, lru_cache."""
import functools
import time


# ---------- 1. a decorator is a function that takes a function and returns a function
def shout(func):
    def wrapper(*args, **kwargs):
        return func(*args, **kwargs).upper()
    return wrapper


def greet(name):
    """Return a greeting."""
    return f"hello {name}"


greet_loud = shout(greet)          # what @shout does, by hand


# ---------- 2. functools.wraps keeps the original name/docstring
def timed(func):
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        start = time.perf_counter()
        try:
            return func(*args, **kwargs)
        finally:
            elapsed = (time.perf_counter() - start) * 1000
            print(f"  [timed] {func.__name__} took {elapsed:.1f} ms")
    return wrapper


@timed
def slow_add(a, b):
    """Add slowly."""
    time.sleep(0.02)
    return a + b


# ---------- 3. decorator WITH arguments = a factory returning a decorator (3 levels)
def retry(times: int = 3, exceptions=(Exception,)):
    def decorator(func):
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            for attempt in range(1, times + 1):
                try:
                    return func(*args, **kwargs)
                except exceptions as e:
                    print(f"  [retry] attempt {attempt} failed: {e}")
                    if attempt == times:
                        raise
        return wrapper
    return decorator


calls = {"n": 0}


@retry(times=3, exceptions=(ConnectionError,))
def flaky_fetch():
    calls["n"] += 1
    if calls["n"] < 3:
        raise ConnectionError("timeout")
    return "data"


# ---------- 4. stacking: applied bottom-up, run top-down
def tag(name):
    def decorator(func):
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            return f"<{name}>{func(*args, **kwargs)}</{name}>"
        return wrapper
    return decorator


@tag("b")
@tag("i")
def text():
    return "hi"


# ---------- 5. a decorator that keeps state (and registers functions)
REGISTRY = {}


def register(func):
    REGISTRY[func.__name__] = func          # side effect at DEFINITION time
    return func                             # return unchanged


@register
def export_csv(): ...


@register
def export_json(): ...


def count_calls(func):
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        wrapper.calls += 1
        return func(*args, **kwargs)
    wrapper.calls = 0                        # state as a function attribute
    return wrapper


@count_calls
def ping():
    return "pong"


# ---------- 6. class decorator: takes a class, returns a (modified) class
def add_repr(cls):
    def __repr__(self):
        fields = ", ".join(f"{k}={v!r}" for k, v in vars(self).items())
        return f"{cls.__name__}({fields})"
    cls.__repr__ = __repr__
    return cls


@add_repr
class Point:
    def __init__(self, x, y):
        self.x, self.y = x, y


# ---------- 7. a class used AS a decorator (callable instance)
class CountCalls:
    def __init__(self, func):
        functools.update_wrapper(self, func)
        self.func, self.calls = func, 0

    def __call__(self, *args, **kwargs):
        self.calls += 1
        return self.func(*args, **kwargs)


@CountCalls
def square(x):
    return x * x


# ---------- 8. lru_cache / cache
@functools.lru_cache(maxsize=None)
def fib(n: int) -> int:
    return n if n < 2 else fib(n - 1) + fib(n - 2)


def fib_plain(n: int) -> int:
    return n if n < 2 else fib_plain(n - 1) + fib_plain(n - 2)


if __name__ == "__main__":
    print("by hand:", greet_loud("ann"))
    print("without wraps, name is:", greet_loud.__name__)
    print("slow_add:", slow_add(2, 3), "| name:", slow_add.__name__, "| doc:", slow_add.__doc__,
          "| original:", slow_add.__wrapped__.__name__)
    print("flaky_fetch ->", flaky_fetch())
    print("stacked:", text())
    print("registry:", list(REGISTRY))
    ping(); ping()
    print("ping.calls:", ping.calls)
    print("class decorator:", Point(1, 2))
    square(3); square(4)
    print("CountCalls:", square.calls, "| name:", square.__name__)

    t0 = time.perf_counter(); fib_plain(25); t_plain = time.perf_counter() - t0
    t0 = time.perf_counter(); fib(25); t_cached = time.perf_counter() - t0
    print(f"fib(25) plain {t_plain*1000:.0f} ms vs cached {t_cached*1000:.2f} ms -> cached faster: {t_cached < t_plain}")
    print("cache_info:", fib.cache_info())
    try:
        functools.lru_cache(lambda xs: sum(xs))([1, 2])
    except TypeError as e:
        print("unhashable arg:", e)
