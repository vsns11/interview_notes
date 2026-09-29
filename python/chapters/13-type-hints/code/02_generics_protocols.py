"""TypeVar & generics (old and 3.12 syntax), Callable, ParamSpec, Protocol."""
import time
from collections.abc import Callable, Iterable, Sequence
from functools import wraps
from typing import ParamSpec, Protocol, TypeVar, runtime_checkable

# ---- Generics, pre-3.12 style ------------------------------------------------
T = TypeVar("T")


def first_old(items: Sequence[T]) -> T:
    return items[0]


# ---- Generics, 3.12+ style (PEP 695): type parameters in brackets -------------
def first[T](items: Sequence[T]) -> T:
    return items[0]


def biggest[N: (int, float)](a: N, b: N) -> N:   # constrained: int OR float
    return a if a > b else b


class Stack[T]:
    def __init__(self) -> None:
        self._items: list[T] = []

    def push(self, item: T) -> None:
        self._items.append(item)

    def pop(self) -> T:
        return self._items.pop()

    def __len__(self) -> int:
        return len(self._items)


# ---- Callable: typing a function parameter ------------------------------------
def apply_all(fn: Callable[[int], int], xs: Iterable[int]) -> list[int]:
    return [fn(x) for x in xs]


# ---- ParamSpec: a decorator that keeps the wrapped signature ------------------
P = ParamSpec("P")
R = TypeVar("R")


def timed(fn: Callable[P, R]) -> Callable[P, R]:
    @wraps(fn)
    def wrapper(*args: P.args, **kwargs: P.kwargs) -> R:
        start = time.perf_counter()
        try:
            return fn(*args, **kwargs)
        finally:
            print(f"  {fn.__name__} took {time.perf_counter() - start:.4f}s")
    return wrapper


@timed
def add(a: int, b: int) -> int:
    return a + b


# ---- Protocol: structural typing ("if it quacks...") ---------------------------
@runtime_checkable
class SupportsClose(Protocol):
    def close(self) -> None: ...


class DbConnection:              # note: does NOT inherit from SupportsClose
    def close(self) -> None:
        print("  db closed")


class TempFile:
    def close(self) -> None:
        print("  temp file removed")


def close_all(things: Iterable[SupportsClose]) -> None:
    for t in things:
        t.close()


if __name__ == "__main__":
    print("first_old([3,4]) :", first_old([3, 4]))
    print("first('xyz')     :", first("xyz"))
    print("biggest(2, 7)    :", biggest(2, 7))
    s: Stack[int] = Stack()
    s.push(1); s.push(2)
    print("Stack pop        :", s.pop(), "| len", len(s))
    print("type params      :", Stack.__type_params__, first.__type_params__)
    print("apply_all        :", apply_all(lambda x: x * x, [1, 2, 3]))
    print("add(2, 3) ->")
    print("  result", add(2, 3))
    print("close_all ->")
    close_all([DbConnection(), TempFile()])
    print("isinstance(DbConnection(), SupportsClose):",
          isinstance(DbConnection(), SupportsClose))
    print("isinstance(42, SupportsClose)            :",
          isinstance(42, SupportsClose))
