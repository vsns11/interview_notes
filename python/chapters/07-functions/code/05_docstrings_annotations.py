"""Chapter 7 - docstrings, type annotations, introspection."""
import inspect
from collections.abc import Callable, Iterable


def average(values: Iterable[float], *, default: float | None = None) -> float | None:
    """Return the arithmetic mean of *values*.

    Args:
        values: numbers to average.
        default: returned when *values* is empty.

    Returns:
        The mean, or *default* for empty input.

    >>> average([1, 2, 3])
    2.0
    """
    items = list(values)
    if not items:
        return default
    return sum(items) / len(items)


def apply(fn: Callable[[int], int], x: int) -> int:
    return fn(x)


type Pair = tuple[int, int]          # 3.12+ type alias statement


def swap(p: Pair) -> Pair:
    return p[1], p[0]


if __name__ == "__main__":
    print("first docstring line:", average.__doc__.splitlines()[0])
    print("signature:", inspect.signature(average))
    print("return annotation:", inspect.signature(average).return_annotation)
    print("average([1, 2, 3]) =", average([1, 2, 3]))
    print("average([], default=0.0) =", average([], default=0.0))
    # Annotations are NOT enforced at runtime:
    print("swap(('x', 'y')) =", swap(("x", "y")), "<- str, not int, and no error")
    print("apply(lambda n: n + 1, 41) =", apply(lambda n: n + 1, 41))
    print("swap((1, 2)) =", swap((1, 2)))

    import doctest
    failures, tests = doctest.testmod(verbose=False)
    print(f"doctest: {tests} test(s), {failures} failure(s)")
