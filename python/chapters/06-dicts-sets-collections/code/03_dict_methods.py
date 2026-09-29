"""Every dict method in action: get, setdefault, update, pop, popitem, fromkeys, views, |."""


def basic_methods() -> None:
    info = {"name": "John", "age": 28, "city": "New York"}
    print(info.get("name"), info.get("phone"))
    info.update({"age": 29, "phone": "123-456-7890"})       # overwrite + add
    info.update(zip_code="10001")                            # keyword form
    print(info)
    info.clear()
    print(info)


def fromkeys_pop_items() -> None:
    new = dict.fromkeys(["a", "b", "c"], 1)
    print(new)
    print(new.pop("b"), new, new.items())
    shared = dict.fromkeys(["x", "y"], [])                   # ONE list shared by all keys!
    shared["x"].append(1)
    print("fromkeys trap:", shared)
    d = {"first": 1, "second": 2, "third": 3}
    print(d.popitem(), d)                                    # LIFO: last inserted


def setdefault_vs_get() -> None:
    words = ["apple", "avocado", "banana", "blueberry", "cherry"]
    by_letter: dict[str, list[str]] = {}
    for w in words:
        by_letter.setdefault(w[0], []).append(w)             # insert [] only if missing
    print(by_letter)
    counts: dict[str, int] = {}
    for ch in "hello":
        counts[ch] = counts.get(ch, 0) + 1                   # get with default for counting
    print(counts)


def views() -> None:
    stock = {"apple": 3, "pear": 0}
    k, v, it = stock.keys(), stock.values(), stock.items()
    stock["fig"] = 7
    print(k, v, it, sep="\n")
    print(k & {"apple", "kiwi"}, sorted(k - {"pear"}))      # keys views support set ops
    print(sorted(stock, key=stock.get, reverse=True))        # keys sorted by value
    print(max(stock.items(), key=lambda kv: kv[1]))


if __name__ == "__main__":
    for f in (basic_methods, fromkeys_pop_items, setdefault_vs_get, views):
        print(f"--- {f.__name__}")
        f()
