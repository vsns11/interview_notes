"""Abstract base classes, duck typing, Protocols, and composition vs inheritance."""
from abc import ABC, abstractmethod
from collections.abc import Sized, Iterable
from typing import Protocol, runtime_checkable


# ---------- ABC: a contract enforced at instantiation time
class Shape(ABC):
    @abstractmethod
    def area(self) -> float: ...

    def describe(self) -> str:                # concrete method shared by subclasses
        return f"{type(self).__name__} with area {self.area():.2f}"


class Square(Shape):
    def __init__(self, side: float) -> None:
        self.side = side

    def area(self) -> float:
        return self.side ** 2


class Broken(Shape):                          # forgot to implement area()
    pass


# ---------- duck typing: no base class needed, only the behaviour
class Duck:
    def speak(self) -> str:
        return "Quack"


class Robot:
    def speak(self) -> str:
        return "Beep"


def make_it_speak(thing) -> str:
    return thing.speak()


# ---------- Protocol: duck typing that a type checker can verify
@runtime_checkable
class Speaker(Protocol):
    def speak(self) -> str: ...


def announce(s: Speaker) -> str:              # mypy/pyright check structure, not ancestry
    return s.speak().upper()


# ---------- composition: HAS-A instead of IS-A
class Engine:
    def __init__(self, hp: int) -> None:
        self.hp = hp

    def start(self) -> str:
        return f"{self.hp}hp engine started"


class Car:
    def __init__(self, engine: Engine) -> None:
        self.engine = engine                  # inject the part; easy to swap in tests

    def drive(self) -> str:
        return self.engine.start() + ", driving"


class ElectricMotor:
    def start(self) -> str:
        return "silent motor started"


class Deck:                                   # never inherits from Sized, still "is" Sized
    def __len__(self) -> int:
        return 52


if __name__ == "__main__":
    print(Square(3).describe())
    try:
        Shape()
    except TypeError as e:
        print("TypeError:", e)
    try:
        Broken()
    except TypeError as e:
        print("TypeError:", e)

    print([make_it_speak(t) for t in (Duck(), Robot())])
    print(announce(Robot()), "| isinstance(Duck(), Speaker):", isinstance(Duck(), Speaker),
          "| isinstance(42, Speaker):", isinstance(42, Speaker))

    print("Deck is Sized:", isinstance(Deck(), Sized), "| Deck is Iterable:", isinstance(Deck(), Iterable))

    print(Car(Engine(150)).drive())
    print(Car(ElectricMotor()).drive())       # duck typing + composition = easy swap
