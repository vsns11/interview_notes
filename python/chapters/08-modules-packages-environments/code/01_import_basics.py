"""Chapter 8 - import forms, module objects, the module cache, __name__."""
import importlib
import math                                   # whole module
import math as m                              # module with an alias
import sys
from math import sqrt                         # one name
from math import sqrt as square_root          # one name with an alias
from os import path as os_path


def describe_module(mod):
    where = getattr(mod, "__file__", None)
    where = "compiled into the interpreter" if where is None else "file ending " + where.rsplit("/", 1)[-1]
    return f"{mod.__name__:<5} type={type(mod).__name__}  {where}"


if __name__ == "__main__":
    print("== four import forms ==")
    print("math.sqrt(81)       =", math.sqrt(81))
    print("sqrt(64)            =", sqrt(64))
    print("square_root(m.pi)   =", square_root(m.pi))
    print("m is math           =", m is math)
    print("os_path.join('a','b') =", os_path.join("a", "b"))

    print("\n== a module is an object with a namespace ==")
    print(describe_module(sys))
    print(describe_module(importlib.import_module('json')))
    print("names in math starting with 'is':",
          [n for n in dir(math) if n.startswith("is")])

    print("\n== the module cache: sys.modules ==")
    print("'math' in sys.modules:", "math" in sys.modules)
    again = importlib.import_module("math")
    print("second import returns the same object:", again is math)
    json_mod = importlib.import_module("json")     # import by string name
    print("json.dumps via import_module:", json_mod.dumps({"a": 1}))

    print("\n== __name__ ==")
    print("this file's __name__ =", __name__)
    print("math.__name__        =", math.__name__)
