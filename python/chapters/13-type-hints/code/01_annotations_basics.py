"""Annotation syntax, built-in generics, Optional/Union, Any - and proof that
annotations are NOT enforced at runtime."""
from typing import Any, Optional, Union, get_type_hints


def greet(name: str, times: int = 1) -> str:
    return ", ".join([f"hi {name}"] * times)


# Variable annotations (PEP 526). A bare annotation creates no variable.
port: int = 8080
scores: dict[str, list[int]] = {"asha": [90, 85]}
maybe_user: str | None = None          # modern spelling (3.10+)
old_style: Optional[str] = None         # same type, older spelling
id_or_name: Union[int, str] = 42        # same as int | str


def find_user(user_id: int) -> str | None:
    users = {1: "asha", 2: "ben"}
    return users.get(user_id)           # may be None -> caller must check


def load(raw: Any) -> object:
    # Any switches checking OFF: raw.whatever() would be accepted by mypy.
    return raw


if __name__ == "__main__":
    print("greet annotations:", greet.__annotations__)
    print("get_type_hints    :", get_type_hints(greet))
    print("Optional[str] == str | None ->", Optional[str] == (str | None))

    # Annotations are only metadata: Python does not check them.
    print("greet(123, 2)     :", greet(123, 2))   # int passed for str - runs fine

    name = find_user(3)
    if name is not None:               # "narrowing": after this, name is str
        print(name.upper())
    else:
        print("find_user(3)      : None (handled)")

    print("isinstance works with unions:", isinstance(5, int | str))
    print("list[int] is a real object  :", list[int], type(list[int]).__name__)
