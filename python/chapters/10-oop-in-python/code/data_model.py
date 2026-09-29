"""The Python data model: dunder methods and operator overloading."""
import math
from functools import total_ordering


@total_ordering
class Vector:
    __match_args__ = ("x", "y")

    def __init__(self, x: float, y: float) -> None:
        self.x, self.y = x, y

    # --- text representations
    def __repr__(self) -> str:                 # for developers: unambiguous
        return f"Vector({self.x!r}, {self.y!r})"

    def __str__(self) -> str:                  # for end users: readable
        return f"({self.x}, {self.y})"

    # --- equality, hashing, ordering
    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Vector):
            return NotImplemented              # let Python try the other side
        return (self.x, self.y) == (other.x, other.y)

    def __hash__(self) -> int:                 # equal objects -> equal hashes
        return hash((self.x, self.y))

    def __lt__(self, other: "Vector") -> bool:  # total_ordering fills <=, >, >=
        if not isinstance(other, Vector):
            return NotImplemented
        return abs(self) < abs(other)

    # --- arithmetic operators
    def __add__(self, other: "Vector") -> "Vector":
        if not isinstance(other, Vector):
            return NotImplemented
        return Vector(self.x + other.x, self.y + other.y)

    def __mul__(self, k: float) -> "Vector":   # v * 3
        if not isinstance(k, (int, float)):
            return NotImplemented
        return Vector(self.x * k, self.y * k)

    __rmul__ = __mul__                          # 3 * v  (reflected)

    def __abs__(self) -> float:
        return math.hypot(self.x, self.y)

    def __bool__(self) -> bool:
        return bool(self.x or self.y)

    # --- make it iterable so tuple unpacking works: x, y = v
    def __iter__(self):
        yield self.x
        yield self.y


class Playlist:
    """A sequence-like container."""

    def __init__(self, *songs: str) -> None:
        self._songs = list(songs)

    def __len__(self) -> int:
        return len(self._songs)

    def __getitem__(self, index):              # supports ints AND slices
        result = self._songs[index]
        return Playlist(*result) if isinstance(index, slice) else result

    def __contains__(self, song: str) -> bool:
        return song in self._songs

    def __repr__(self) -> str:
        return f"Playlist{tuple(self._songs)}"


class Multiplier:
    """A callable object: an instance that behaves like a function with state."""

    def __init__(self, factor: int) -> None:
        self.factor = factor
        self.calls = 0

    def __call__(self, x: int) -> int:
        self.calls += 1
        return x * self.factor


class Timer:
    """A context manager: __enter__ runs at `with`, __exit__ always runs after."""

    def __enter__(self):
        print("  enter: start timer")
        return self                             # bound to the `as` target

    def __exit__(self, exc_type, exc, tb) -> bool:
        print(f"  exit: stop timer (exception={exc_type.__name__ if exc_type else None})")
        return False                            # False -> don't swallow exceptions


class OnlyEq:
    def __eq__(self, other):
        return True


if __name__ == "__main__":
    v, w = Vector(3, 4), Vector(1, 1)
    print("repr:", repr(v), "| str:", str(v), "| f-string:", f"{v}")
    print("v + w =", v + w, "| v * 2 =", v * 2, "| 2 * v =", 2 * v)
    print("abs(v) =", abs(v), "| bool(Vector(0,0)) =", bool(Vector(0, 0)))
    print("v == Vector(3,4):", v == Vector(3, 4), "| v is Vector(3,4):", v is Vector(3, 4))
    print("w < v:", w < v, "| v >= w:", v >= w, "(>= from total_ordering)")
    print("set dedups equal vectors:", {Vector(1, 2), Vector(1, 2)})
    print("sorted:", sorted([v, w, Vector(0, 2)]))
    x, y = v
    print("unpacked:", x, y)
    match v:
        case Vector(3, y_val):
            print("pattern matched, y =", y_val)
    try:
        v + 5
    except TypeError as e:
        print("TypeError:", e)

    p = Playlist("intro", "verse", "chorus", "outro")
    print("len:", len(p), "| p[0]:", p[0], "| p[-1]:", p[-1], "| p[1:3]:", p[1:3])
    print("'chorus' in p:", "chorus" in p, "| iterate via __getitem__:", [s.upper() for s in p])

    triple = Multiplier(3)
    print("callable:", triple(5), triple(7), "calls =", triple.calls, "| callable():", callable(triple))

    with Timer() as t:
        print("  inside block, t is a Timer:", isinstance(t, Timer))
    try:
        with Timer():
            raise KeyError("boom")
    except KeyError:
        print("  KeyError still propagated")

    print("__hash__ of class defining only __eq__:", OnlyEq.__hash__)
