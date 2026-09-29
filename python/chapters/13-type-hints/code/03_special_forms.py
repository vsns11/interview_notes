"""TypedDict, Literal, Final, NewType, type aliases and dataclasses."""
from dataclasses import dataclass, field
from typing import Final, Literal, NewType, NotRequired, TypedDict, get_args

# ---- type alias (3.12 'type' statement) and the older plain assignment --------
type JSON = dict[str, "JSON"] | list["JSON"] | str | int | float | bool | None
Vector = list[float]                     # old-style implicit alias


# ---- TypedDict: a dict with known keys ----------------------------------------
class Movie(TypedDict):
    title: str
    year: int
    rating: NotRequired[float]           # key may be absent


# ---- Literal: only these exact values -----------------------------------------
Mode = Literal["r", "w", "a"]


def open_log(path: str, mode: Mode = "r") -> str:
    return f"open {path} in {mode!r}"


# ---- Final: must not be reassigned --------------------------------------------
MAX_RETRIES: Final = 3

# ---- NewType: a distinct name for the checker, zero runtime cost --------------
UserId = NewType("UserId", int)
OrderId = NewType("OrderId", int)


def get_user(uid: UserId) -> str:
    return f"user#{uid}"


# ---- dataclass: annotations become fields -------------------------------------
@dataclass(frozen=True, slots=True)
class Point:
    x: float
    y: float
    tags: tuple[str, ...] = ()


@dataclass
class Cart:
    owner: str
    items: list[str] = field(default_factory=list)


def scale(v: Vector, k: float) -> Vector:
    return [k * c for c in v]


if __name__ == "__main__":
    m: Movie = {"title": "Arrival", "year": 2016}
    print("TypedDict value  :", m, "| type at runtime:", type(m).__name__)
    print("Movie keys       :", Movie.__required_keys__, Movie.__optional_keys__)
    print("Literal args     :", get_args(Mode))
    print(open_log("app.log", "a"))
    print("NewType          :", get_user(UserId(7)), "| UserId(7) is int:",
          type(UserId(7)) is int)
    print("type alias       :", JSON, "->", type(JSON).__name__)
    print("alias value      :", JSON.__value__)
    print("scale            :", scale([1.0, 2.0], 3))
    p = Point(1.0, 2.0)
    print("dataclass        :", p, "| fields:",
          list(Point.__dataclass_fields__))
    c1, c2 = Cart("a"), Cart("b")
    c1.items.append("pen")
    print("default_factory  :", c1.items, c2.items)
    try:
        p.x = 5  # type: ignore[misc]
    except AttributeError as e:          # FrozenInstanceError subclasses it
        print("frozen           :", type(e).__name__)
