"""dataclasses, enums, and __slots__."""
import tracemalloc
from dataclasses import dataclass, field, asdict, replace, FrozenInstanceError
from enum import Enum, IntEnum, StrEnum, Flag, auto


# ---------- dataclasses
@dataclass
class Item:
    name: str
    price: float
    qty: int = 1
    tags: list[str] = field(default_factory=list)   # never `tags: list = []`
    _cache: dict = field(default_factory=dict, repr=False, compare=False)

    def __post_init__(self) -> None:                  # validation after generated __init__
        if self.price < 0:
            raise ValueError("price must be >= 0")

    @property
    def total(self) -> float:
        return self.price * self.qty


@dataclass(frozen=True, order=True)
class Version:
    major: int
    minor: int
    patch: int = 0


@dataclass(slots=True, kw_only=True)
class Config:
    host: str
    port: int = 8080


# ---------- enums
class Color(Enum):
    RED = auto()
    GREEN = auto()
    BLUE = auto()


class HttpStatus(IntEnum):
    OK = 200
    NOT_FOUND = 404


class Env(StrEnum):
    DEV = auto()          # StrEnum + auto() -> lower-case member name
    PROD = auto()


class Perm(Flag):
    READ = auto()
    WRITE = auto()
    EXEC = auto()


# ---------- __slots__
class PointDict:
    def __init__(self, x, y):
        self.x, self.y = x, y


class PointSlots:
    __slots__ = ("x", "y")

    def __init__(self, x, y):
        self.x, self.y = x, y


def measure(cls, n=100_000) -> int:
    tracemalloc.start()
    objs = [cls(i, i) for i in range(n)]
    current, _ = tracemalloc.get_traced_memory()
    tracemalloc.stop()
    del objs
    return current


if __name__ == "__main__":
    a = Item("pen", 1.5, 4, ["office"])
    print(a, "| total:", a.total)
    print("eq ignores _cache:", a == Item("pen", 1.5, 4, ["office"]))
    print("asdict:", asdict(a))
    try:
        Item("bad", -1)
    except ValueError as e:
        print("post_init:", e)
    try:
        @dataclass
        class Bad:
            tags: list = []
    except ValueError as e:
        print("mutable default:", e)

    v1, v2 = Version(1, 4), Version(1, 10)
    print("order compares field-by-field:", v1 < v2, "| sorted:", sorted([v2, v1, Version(0, 9)]))
    print("frozen is hashable:", {v1: "stable"}[Version(1, 4)])
    try:
        v1.major = 2
    except FrozenInstanceError as e:
        print("FrozenInstanceError:", e)
    print("replace():", replace(v1, patch=7))
    c = Config(host="localhost")
    print(c, "| has __dict__:", hasattr(c, "__dict__"))

    print(Color.RED, Color.RED.name, Color.RED.value, Color["GREEN"], Color(3))
    print("members:", list(Color), "| identity compare:", Color.RED is Color(1))
    print("IntEnum == int:", HttpStatus.OK == 200, "| Enum == int:", Color.RED == 1)
    print("StrEnum:", Env.PROD, Env.PROD == "prod")
    rw = Perm.READ | Perm.WRITE
    print("Flag:", rw, "| WRITE in rw:", Perm.WRITE in rw, "| EXEC in rw:", Perm.EXEC in rw)

    d, s = measure(PointDict), measure(PointSlots)
    print(f"100k objects: dict-based {d/1e6:.1f} MB vs slots {s/1e6:.1f} MB")
    ps = PointSlots(1, 2)
    try:
        ps.z = 3
    except AttributeError as e:
        print("slots block new attrs:", e)
