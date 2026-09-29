"""20.3 Runnable evidence for the interview Q&A answers."""
import asyncio
import copy
import dis
import functools
import gc
import pickle
import sys
import time
import weakref
from concurrent.futures import ProcessPoolExecutor, ThreadPoolExecutor
from dataclasses import dataclass
from typing import Protocol


def section(title: str) -> None:
    print(f"\n== {title}")


# ---------- decorators ----------
def retry(times: int):
    """Decorator factory: @retry(3) -> decorator -> wrapper."""
    def decorator(func):
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            for attempt in range(1, times + 1):
                try:
                    return func(*args, **kwargs)
                except ValueError:
                    print(f"   attempt {attempt} failed")
            raise RuntimeError("gave up")
        return wrapper
    return decorator


calls = {"n": 0}


@retry(times=3)
def flaky() -> str:
    """Fails twice, then works."""
    calls["n"] += 1
    if calls["n"] < 3:
        raise ValueError
    return "ok"


# ---------- __new__ vs __init__ ----------
class Singleton:
    _instance = None

    def __new__(cls, *args, **kwargs):
        if cls._instance is None:
            print("   __new__: creating the object")
            cls._instance = super().__new__(cls)
        return cls._instance

    def __init__(self, value):
        print(f"   __init__: setting value={value}")
        self.value = value


# ---------- MRO ----------
class A:
    def hello(self):
        return "A"


class B(A):
    def hello(self):
        return "B>" + super().hello()


class C(A):
    def hello(self):
        return "C>" + super().hello()


class D(B, C):
    def hello(self):
        return "D>" + super().hello()


# ---------- static/class methods, property, slots ----------
class Temperature:
    __slots__ = ("_celsius",)          # no per-instance __dict__

    def __init__(self, celsius: float):
        self.celsius = celsius         # goes through the property setter

    @property
    def celsius(self) -> float:
        return self._celsius

    @celsius.setter
    def celsius(self, value: float) -> None:
        if value < -273.15:
            raise ValueError("below absolute zero")
        self._celsius = value

    @classmethod
    def from_fahrenheit(cls, f: float) -> "Temperature":   # alternative constructor
        return cls((f - 32) * 5 / 9)

    @staticmethod
    def is_freezing(celsius: float) -> bool:                # plain function in the class namespace
        return celsius <= 0

    def __repr__(self) -> str:
        return f"Temperature({self._celsius:.1f})"


# ---------- typing / protocol ----------
class HasArea(Protocol):
    def area(self) -> float: ...


@dataclass(frozen=True)
class Square:
    side: float

    def area(self) -> float:
        return self.side ** 2


def total_area(shapes: list[HasArea]) -> float:
    return sum(s.area() for s in shapes)


# ---------- GIL demo ----------
def cpu_task(n: int) -> int:
    return sum(i * i for i in range(n))


def gil_demo() -> None:
    work = [6_000_000] * 4
    t0 = time.perf_counter()
    for w in work:
        cpu_task(w)
    serial = time.perf_counter() - t0
    t0 = time.perf_counter()
    with ThreadPoolExecutor(4) as ex:
        list(ex.map(cpu_task, work))
    threads = time.perf_counter() - t0
    t0 = time.perf_counter()
    with ProcessPoolExecutor(4) as ex:
        list(ex.map(cpu_task, work))
    procs = time.perf_counter() - t0
    gil = getattr(sys, "_is_gil_enabled", lambda: True)()
    print(f"   GIL enabled: {gil}")
    print(f"   CPU-bound x4  serial {serial:.2f}s | 4 threads {threads:.2f}s | 4 processes {procs:.2f}s")


async def fake_io(name: str, delay: float) -> str:
    await asyncio.sleep(delay)          # yields to the event loop while "waiting"
    return name


async def asyncio_demo() -> None:
    t0 = time.perf_counter()
    results = await asyncio.gather(*(fake_io(f"req{i}", 0.2) for i in range(10)))
    print(f"   10 x 0.2s waits concurrently -> {time.perf_counter() - t0:.2f}s, {results[:3]}...")


if __name__ == "__main__":
    section("bytecode: CPython compiles to bytecode, then interprets it")
    dis.dis(lambda x: x + 1)

    section("mutable vs immutable, list vs tuple")
    t = (1, [2, 3])
    t[1].append(4)                     # the tuple can't change, its list can
    print("   tuple holding a list:", t)
    try:
        hash(t)
    except TypeError as e:
        print("   hash(tuple with list):", e)
    print("   sizes: list", sys.getsizeof([1, 2, 3]), "bytes | tuple", sys.getsizeof((1, 2, 3)), "bytes")

    section("pass by object reference")
    def mutate(lst):
        lst.append("changed")          # mutation is visible to the caller
        lst = ["rebound"]              # rebinding the local name is not
    data = ["orig"]
    mutate(data)
    print("   after mutate:", data)

    section("*args / **kwargs")
    def show(*args, **kwargs):
        return args, kwargs
    print("   show(1, 2, a=3) ->", show(1, 2, a=3), "| show(*[1, 2], **{'b': 4}) ->", show(*[1, 2], **{"b": 4}))

    section("decorators")
    print("   ", flaky(), "| name kept by wraps:", flaky.__name__, "|", flaky.__doc__)

    section("generators vs iterators")
    def countdown(n):
        while n > 0:
            yield n
            n -= 1
    gen = countdown(3)
    print("   iter(gen) is gen:", iter(gen) is gen, "| values:", list(gen), "| second pass:", list(gen))
    squares_list = [i * i for i in range(10_000)]
    squares_gen = (i * i for i in range(10_000))
    print("   list", sys.getsizeof(squares_list), "bytes vs generator", sys.getsizeof(squares_gen), "bytes")

    section("__new__ vs __init__")
    s1 = Singleton(1)
    s2 = Singleton(2)
    print("   same object:", s1 is s2, "| value:", s1.value)

    section("MRO / super()")
    print("   D.__mro__:", [k.__name__ for k in D.__mro__], "| D().hello():", D().hello())

    section("classmethod / staticmethod / property / __slots__")
    t = Temperature.from_fahrenheit(212)
    print("   ", t, "| is_freezing(-3):", Temperature.is_freezing(-3))
    try:
        t.colour = "red"
    except AttributeError as e:
        print("   __slots__ blocks new attributes:", e)
    try:
        t.celsius = -300
    except ValueError as e:
        print("   property validation:", e)

    section("copy vs deepcopy")
    orig = {"tags": ["a"]}
    sh, dp = copy.copy(orig), copy.deepcopy(orig)
    orig["tags"].append("b")
    print("   shallow:", sh, "| deep:", dp)

    section("dict: insertion order, hashing")
    d = {"b": 1, "a": 2}
    d["c"] = 3
    print("   order kept:", list(d), "| hash('a') is an int:", isinstance(hash("a"), int))

    section("memory: reference counting + cycle collector")
    class Node:
        pass
    x = Node()
    print("   refcount (includes getrefcount's own arg):", sys.getrefcount(x))
    probe = weakref.ref(x)
    del x
    print("   freed immediately by refcounting:", probe() is None)
    a, b = Node(), Node()
    a.other, b.other = b, a            # reference cycle
    probe = weakref.ref(a)
    del a, b
    print("   cycle alive before gc.collect():", probe() is not None)
    gc.collect()
    print("   cycle freed after gc.collect():", probe() is None)

    section("pickling")
    blob = pickle.dumps({"model": "v1", "weights": [0.1, 0.2]})
    print("   round trip:", pickle.loads(blob), "|", len(blob), "bytes")
    print("   pickle a lambda:", end=" ")
    try:
        pickle.dumps(lambda: 1)
    except (pickle.PicklingError, AttributeError) as e:
        print(type(e).__name__)

    section("typing is not enforced at runtime; Protocol = structural typing")
    def double(n: int) -> int:
        return n * 2
    print("   double('ab') runs anyway:", double("ab"))  # type: ignore[arg-type]
    print("   total_area:", total_area([Square(2), Square(3)]))

    section("GIL: threads vs processes on CPU-bound work")
    gil_demo()

    section("asyncio: many waits overlap on one thread")
    asyncio.run(asyncio_demo())
