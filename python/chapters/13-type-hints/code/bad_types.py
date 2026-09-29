"""A file with deliberate type errors - checked by 04_run_mypy.py, not run."""
from typing import Final, Literal, NewType

UserId = NewType("UserId", int)


def get_user(uid: UserId) -> str:
    return f"user#{uid}"


def find(user_id: int) -> str | None:
    return None


def shout(s: str) -> str:
    return s.upper()


def area(w: float, h: float) -> float:
    return w * h


def mode(m: Literal["r", "w"]) -> None: ...


LIMIT: Final = 10

if __name__ == "__main__":
    area("3", 4)                       # 1 wrong argument type
    name = find(1)
    print(name.upper())                # 2 Optional not narrowed
    get_user(42)                       # 3 plain int is not a UserId
    mode("x")                          # 4 not an allowed literal
    LIMIT = 11                         # 5 reassigning a Final
    count: int = "three"               # 6 wrong assignment
    shout(name)                        # 7 str | None passed where str needed
# tools_run_python.sh: skip (deliberate type errors - run 04_run_mypy.py instead)
