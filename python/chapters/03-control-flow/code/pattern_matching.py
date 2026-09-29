"""Chapter 3 - structural pattern matching (match/case, Python 3.10+)."""

from dataclasses import dataclass
from enum import Enum


class Color(Enum):
    RED = "red"
    GREEN = "green"


@dataclass
class Point:
    x: float
    y: float


@dataclass
class Circle:
    center: Point
    radius: float


def http_status(code: int) -> str:
    match code:
        case 200:
            return "Success"
        case 404:
            return "Not Found"
        case 500:
            return "Server Error"
        case 301 | 302 | 307 | 308:          # OR pattern
            return "Redirect"
        case _:                              # wildcard: matches anything, binds nothing
            return "Unknown status code"


def day_kind(day: str) -> str:
    match day:
        case "Monday" | "Friday":
            return "Start/end of the work week"
        case "Tuesday" | "Wednesday" | "Thursday":
            return "Midweek"
        case _:
            return "Weekend"


def first_color(colors: list[str]) -> str:
    match colors:
        case []:
            return "empty list"
        case ["red", *others]:               # sequence pattern with a star capture
            return f"red first, {len(others)} others"
        case ["blue", *_]:
            return "blue first"
        case [single]:
            return f"only one color: {single}"
        case _:
            return "different first color"


def command(text: str) -> str:
    match text.split():
        case ["go", ("north" | "south" | "east" | "west") as direction]:
            return f"moving {direction}"
        case ["take", item]:
            return f"picked up {item}"
        case ["drop", *items] if items:      # guard: extra condition after the pattern
            return f"dropped {', '.join(items)}"
        case ["quit" | "exit"]:
            return "bye"
        case _:
            return f"unknown command: {text!r}"


def person_label(person: tuple[int, str]) -> str:
    match person:
        case (age, "Engineer") if age > 20:
            return f"Adult Engineer aged {age}"
        case (age, profession):
            return f"{profession} aged {age}"


def handle_event(event: dict) -> str:
    match event:                                         # mapping patterns ignore extra keys
        case {"type": "click", "pos": (x, y)}:
            return f"click at {x},{y}"
        case {"type": "key", "key": str(k)} if len(k) == 1:
            return f"key {k!r}"
        case {"type": "resize", **rest}:
            return f"resize with {rest}"
        case {"type": other}:
            return f"unhandled event type {other!r}"
        case _:
            return "not an event"


def shape_info(shape: object) -> str:
    match shape:                                         # class patterns
        case Point(x=0, y=0):
            return "the origin"
        case Point(x, 0):                                # positional via __match_args__
            return f"on the x-axis at {x}"
        case Point():
            return "some other point"
        case Circle(center=Point(x=0, y=0), radius=r):   # nested patterns
            return f"circle of radius {r} at the origin"
        case Circle(radius=r) if r <= 0:
            return "invalid circle"
        case int(n) | float(n):                          # built-in types capture the whole value
            return f"a number {n}"
        case str() as s:
            return f"a string of length {len(s)}"
        case _:
            return "unknown shape"


def color_name(c: Color) -> str:
    match c:
        case Color.RED:          # dotted name = value pattern (compared with ==)
            return "stop"
        case Color.GREEN:
            return "go"


def main() -> None:
    print([http_status(c) for c in (200, 404, 500, 302, 418)])
    print(day_kind("Tuesday"), "|", day_kind("Friday"), "|", day_kind("Sunday"))
    for colors in ([], ["red", "blue", "green"], ["blue", "red"], ["pink"], ["pink", "red"]):
        print(colors, "->", first_color(colors))
    for cmd in ("go north", "take lamp", "drop key coin", "drop", "quit", "dance"):
        print(f"{cmd!r:16} -> {command(cmd)}")
    print(person_label((25, "Engineer")), "|", person_label((19, "Engineer")))
    for ev in ({"type": "click", "pos": (3, 4), "button": 1}, {"type": "key", "key": "q"},
               {"type": "resize", "w": 800, "h": 600}, {"type": "scroll"}, ["click"]):
        print(ev, "->", handle_event(ev))
    for s in (Point(0, 0), Point(5, 0), Point(1, 2), Circle(Point(0, 0), 2), Circle(Point(1, 1), -1),
              7, 2.5, "hello", [1]):
        print(repr(s), "->", shape_info(s))
    print("Point.__match_args__ =", Point.__match_args__)
    print(color_name(Color.RED), color_name(Color.GREEN))

    # strings are NOT matched by sequence patterns
    match "ab":
        case [a, b]:
            print("matched as a sequence")
        case str():
            print("'ab' is matched as a str, not as a sequence of characters")

    # the classic bug: a bare name is a CAPTURE pattern, not a comparison
    buggy = "RED = 'red'\nmatch 'blue':\n    case RED:\n        print('matched?!', RED)\n"
    exec(buggy, {})                       # prints: matched?! blue  (RED was re-bound)
    try:
        compile(buggy + "    case _:\n        pass\n", "<buggy>", "exec")
    except SyntaxError as err:
        print("SyntaxError:", err.msg)


if __name__ == "__main__":
    main()
