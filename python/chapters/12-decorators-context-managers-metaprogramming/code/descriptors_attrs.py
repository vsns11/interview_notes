"""Descriptors (how properties work), __getattr__ vs __getattribute__, monkey patching."""
import functools


# ---------- a reusable validating descriptor
class Positive:
    def __set_name__(self, owner, name):          # called at class creation (3.6+)
        self.private = "_" + name

    def __get__(self, obj, objtype=None):
        if obj is None:                            # accessed on the class
            return self
        return getattr(obj, self.private)

    def __set__(self, obj, value):
        if value <= 0:
            raise ValueError(f"{self.private[1:]} must be > 0, got {value}")
        setattr(obj, self.private, value)


class Order:
    qty = Positive()                               # data descriptors live on the CLASS
    price = Positive()

    def __init__(self, qty, price):
        self.qty = qty                             # goes through Positive.__set__
        self.price = price


# ---------- a minimal re-implementation of property
class my_property:
    def __init__(self, fget, fset=None):
        self.fget, self.fset = fget, fset

    def __get__(self, obj, objtype=None):
        return self if obj is None else self.fget(obj)

    def __set__(self, obj, value):
        if self.fset is None:
            raise AttributeError("read-only")
        self.fset(obj, value)

    def setter(self, fset):
        return type(self)(self.fget, fset)


class Celsius:
    def __init__(self, c):
        self._c = c

    @my_property
    def fahrenheit(self):
        return self._c * 9 / 5 + 32


# ---------- non-data descriptor: instance dict wins (how cached_property works)
class Report:
    @functools.cached_property
    def data(self):
        print("  (computing data once)")
        return [1, 2, 3]


# ---------- __getattr__ vs __getattribute__
class Proxy:
    """Delegate unknown attributes to a wrapped object."""

    def __init__(self, target):
        self._target = target

    def __getattr__(self, name):                   # only called when normal lookup FAILS
        print(f"  __getattr__({name!r}) -> delegating")
        return getattr(self._target, name)


class Logged:
    def __init__(self):
        self.x = 1

    def __getattribute__(self, name):             # called for EVERY attribute access
        if not name.startswith("__"):
            print(f"  __getattribute__({name!r})")
        return super().__getattribute__(name)     # must use super() to avoid recursion


class Greeter:
    def hello(self):
        return "hello"


if __name__ == "__main__":
    o = Order(2, 9.99)
    print("Order:", o.qty, o.price, "| instance dict:", vars(o))
    try:
        o.qty = 0
    except ValueError as e:
        print("descriptor refused:", e)
    print("on the class:", type(Order.qty).__name__, "object (the descriptor itself)")

    c = Celsius(100)
    print("my_property:", c.fahrenheit)
    try:
        c.fahrenheit = 0
    except AttributeError as e:
        print("AttributeError:", e)

    r = Report()
    print("first:", r.data)
    print("second:", r.data, "| stored in instance dict:", "data" in vars(r))

    # functions are non-data descriptors -> bound methods
    g = Greeter()
    f = Greeter.__dict__["hello"]
    print("function has __get__:", hasattr(f, "__get__"), "| f.__get__(g)():", f.__get__(g)())
    print("data descriptor?", hasattr(Positive, "__set__"), "| cached_property has __set__?",
          hasattr(functools.cached_property, "__set__"))

    p = Proxy("hello world")
    print("proxy.upper():", p.upper())
    print("proxy._target found normally (no __getattr__ call):", p._target)

    lg = Logged()
    print("value:", lg.x)

    # monkey patching: replacing an attribute at run time
    original = Greeter.hello
    Greeter.hello = lambda self: "patched!"
    print("monkey patched:", Greeter().hello(), "| existing instance too:", g.hello())
    Greeter.hello = original                       # always restore
    print("restored:", g.hello())
