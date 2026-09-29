"""Chapter 2 - a tour of every built-in data type category."""


def main() -> None:
    samples = {
        "text": "Hello, World!",
        "int": 10,
        "float": 10.5,
        "complex": 2 + 3j,
        "list": ["soccer", "basketball", "tennis"],
        "tuple": ("red", "green", "blue"),
        "range": range(5),
        "dict": {"name": "Alice", "age": 25, "city": "New York"},
        "set": {"soccer", "basketball"},
        "frozenset": frozenset(["red", "green"]),
        "bool": True,
        "bytes": bytes([50, 100, 150]),
        "bytearray": bytearray([50, 100, 150]),
        "memoryview": memoryview(b"abc"),
        "None": None,
    }
    for label, obj in samples.items():
        mutable = label in {"list", "dict", "set", "bytearray"}
        print(f"{label:10} -> {type(obj).__name__:10} mutable={mutable}")

    # numeric arithmetic
    print("int + 5:", 10 + 5, "| float + 0.5:", 10.5 + 0.5, "| complex:", (2 + 3j) + (1 - 2j))

    # range is lazy: it stores start/stop/step, not every number
    r = range(5)
    print("range(5) as list:", list(r), "| 3 in r:", 3 in r)

    # dict: access and modify by key
    user = {"name": "Alice", "age": 25}
    user["age"] += 1
    user["city"] = "Paris"
    print("dict:", user, "| get missing key:", user.get("email", "n/a"))

    # set: unique items; adding a duplicate does nothing
    sports = {"soccer", "basketball", "tennis"}
    sports.add("volleyball")
    sports.add("soccer")
    print("set size:", len(sports), "| sorted:", sorted(sports))
    colors = frozenset(["red", "green", "blue"])
    print("frozenset usable as dict key:", {colors: "palette"}[colors])

    # binary types
    data = bytearray([50, 100, 150])
    data[1] = 105                          # bytearray is mutable
    view = memoryview(data)[1:3]           # a window onto data - no copy
    data[2] = 200                          # change the underlying buffer ...
    print("bytes:", list(bytes([50, 100, 150])), "| bytearray:", list(data), "| view:", list(view))
    try:
        bytes([256])
    except ValueError as err:
        print("bytes range 0-255:", err)
    print("text <-> bytes:", "café".encode("utf-8"), b"caf\xc3\xa9".decode("utf-8"))

    # None as "no value"
    val = None
    print("No value provided" if val is None else f"Value: {val}")
    print("function without return gives:", print_nothing())

    # isinstance respects inheritance; bool IS an int subclass
    print("isinstance(True, int):", isinstance(True, int), "| True + True =", True + True)


def print_nothing():
    pass


if __name__ == "__main__":
    main()
