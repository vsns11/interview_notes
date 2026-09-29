"""Static hints vs runtime validation: a tiny validator built on get_type_hints
(this is the idea pydantic implements properly)."""
from dataclasses import dataclass
from types import NoneType, UnionType
from typing import get_args, get_origin, get_type_hints


def check(value: object, tp: object) -> bool:
    origin = get_origin(tp)
    if tp is NoneType or tp is None:
        return value is None
    if origin is UnionType:
        return any(check(value, arg) for arg in get_args(tp))
    if origin is list:
        (item_tp,) = get_args(tp)
        return isinstance(value, list) and all(check(v, item_tp) for v in value)
    if isinstance(tp, type):
        return isinstance(value, tp) and not (tp is int and isinstance(value, bool))
    raise TypeError(f"unsupported type {tp!r}")


def validated(cls):
    hints = get_type_hints(cls)
    original_init = cls.__init__

    def __init__(self, *args, **kwargs):
        original_init(self, *args, **kwargs)
        for name, tp in hints.items():
            if not check(getattr(self, name), tp):
                raise TypeError(f"{name}={getattr(self, name)!r} is not {tp}")
    cls.__init__ = __init__
    return cls


@validated
@dataclass
class Signup:
    email: str
    age: int
    referrer: str | None = None
    tags: list[str] | None = None


if __name__ == "__main__":
    print(Signup("a@x.io", 30, tags=["beta"]))
    for bad in [dict(email="b@x.io", age="30"),
                dict(email="c@x.io", age=30, tags=["ok", 5])]:
        try:
            Signup(**bad)
        except TypeError as e:
            print("rejected:", e)
