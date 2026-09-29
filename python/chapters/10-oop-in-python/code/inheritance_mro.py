"""Inheritance, super(), the MRO (C3 linearization), mixins, and name mangling."""
import json


# ---------- single inheritance + super()
class Employee:
    def __init__(self, name: str, salary: int) -> None:
        self.name, self.salary = name, salary

    def pay(self) -> int:
        return self.salary

    def __repr__(self) -> str:
        return f"{type(self).__name__}({self.name!r})"


class Manager(Employee):
    def __init__(self, name: str, salary: int, bonus: int) -> None:
        super().__init__(name, salary)      # reuse the parent's initialiser
        self.bonus = bonus

    def pay(self) -> int:                    # override, then extend
        return super().pay() + self.bonus


# ---------- diamond + cooperative super()
class Base:
    def greet(self) -> list[str]:
        return ["Base"]


class Left(Base):
    def greet(self) -> list[str]:
        return ["Left"] + super().greet()


class Right(Base):
    def greet(self) -> list[str]:
        return ["Right"] + super().greet()


class Child(Left, Right):
    def greet(self) -> list[str]:
        return ["Child"] + super().greet()


# ---------- mixins: small classes that add one capability
class JsonMixin:
    def to_json(self) -> str:
        return json.dumps(self.__dict__, sort_keys=True)


class ReprMixin:
    def __repr__(self) -> str:
        fields = ", ".join(f"{k}={v!r}" for k, v in self.__dict__.items())
        return f"{type(self).__name__}({fields})"


class Product(JsonMixin, ReprMixin):
    def __init__(self, sku: str, price: float) -> None:
        self.sku, self.price = sku, price


# ---------- C3 failure: an inconsistent hierarchy
class X: ...
class Y: ...
class A(X, Y): ...
class B(Y, X): ...


# ---------- name mangling
class Account:
    def __init__(self) -> None:
        self._internal = "convention only"
        self.__secret = "mangled"            # stored as _Account__secret


class SubAccount(Account):
    def __init__(self) -> None:
        super().__init__()
        self.__secret = "child's own"         # stored as _SubAccount__secret (no clash)


if __name__ == "__main__":
    m = Manager("Mia", 100, 20)
    print(m, "pays", m.pay(), "| isinstance Employee:", isinstance(m, Employee),
          "| issubclass:", issubclass(Manager, Employee))

    print("Child MRO:", [c.__name__ for c in Child.__mro__])
    print("Child().greet():", Child().greet())

    p = Product("A-1", 9.5)
    print("mixins:", p, p.to_json())
    print("Product MRO:", [c.__name__ for c in Product.mro()])

    try:
        class C(A, B): ...
    except TypeError as e:
        print("C3 refuses:", e)

    s = SubAccount()
    print("instance dict:", s.__dict__)
    try:
        s.__secret
    except AttributeError as e:
        print("AttributeError:", e)
