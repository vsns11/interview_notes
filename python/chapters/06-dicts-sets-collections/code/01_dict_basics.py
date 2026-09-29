"""Dictionaries: define, access, update, remove, copy, loop, operators, comprehensions."""


def define_access() -> None:
    ages = {"Alice": 22, "Bob": 25, "Eve": 19}
    print(ages, len(ages))
    print(ages["Alice"], ages.get("Bob"), ages.get("Charlie"), ages.get("Charlie", "Not Found"))
    try:
        ages["Charlie"]
    except KeyError as e:
        print("KeyError:", e)
    other_ways = dict(a=1, b=2), dict([("x", 1), ("y", 2)]), dict(zip("pq", [7, 8]))
    print(*other_ways)
    print({(59.9, 10.7): "Oslo", 42: "int key", None: "None key"})
    try:
        {["a", "list"]: 1}
    except TypeError as e:
        print("TypeError:", e)


def update_remove() -> None:
    ages = {"Alice": 22, "Bob": 25, "Eve": 19}
    ages["Alice"] = 23                          # overwrite
    ages["Dave"] = 20                           # insert (goes to the end)
    print(ages)
    removed = ages.pop("Eve");  print("pop:", removed, ages)
    del ages["Bob"];            print("del:", ages)
    print(ages.pop("Zed", "no such key"))       # default avoids KeyError


def copy_and_loop() -> None:
    original = {"Alice": 23, "Charlie": 21, "tags": ["x"]}
    copied = original.copy()                    # shallow
    copied["Alice"] = 24
    copied["tags"].append("y")                  # shared inner list!
    print(original, copied, sep="\n")
    for key in original:                        # iterates keys
        print(key, end=" ")
    print()
    for key, value in {"Alice": 23, "Charlie": 21}.items():
        print(f"{key} is {value} years old")
    print(list(original.keys()), list(original.values())[:2])


def operators_and_order() -> None:
    d = {"Alice": 23, "Charlie": 21}
    print("Alice" in d, "Bob" not in d, 23 in d, 23 in d.values())
    a, b = {"x": 1, "y": 2}, {"y": 20, "z": 3}
    print("a | b :", a | b)                     # 3.9+: right side wins on conflicts
    a |= {"w": 0};  print("a |= :", a)
    print("{**a, **b}:", {**a, **b})
    print({"a": 1, "b": 2} == {"b": 2, "a": 1}) # equality ignores order
    views = d.keys()
    d["New"] = 0
    print(views)                                # views are live


def comprehensions() -> None:
    words = ["apple", "banana", "cherry"]
    print({w: len(w) for w in words})
    prices = {"apple": 1.2, "banana": 0.5, "cherry": 3.0}
    print({k: v for k, v in prices.items() if v >= 1})
    print({v: k for k, v in {"a": 1, "b": 2}.items()})    # invert
    print(sorted({c for c in "mississippi"}))              # set comprehension (sorted for stable output)


def mutate_while_iterating() -> None:
    d = {"a": 1, "b": 0, "c": 0}
    try:
        for k in d:
            if d[k] == 0:
                del d[k]
    except RuntimeError as e:
        print("RuntimeError:", e)
    d = {"a": 1, "b": 0, "c": 0}
    for k in list(d):                           # iterate over a snapshot of keys
        if d[k] == 0:
            del d[k]
    print(d)


if __name__ == "__main__":
    for f in (define_access, update_remove, copy_and_loop, operators_and_order,
              comprehensions, mutate_while_iterating):
        print(f"--- {f.__name__}")
        f()
