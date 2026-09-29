"""Strings 101: creating, indexing, immutability, slicing, operators, looping, escapes."""


def creating() -> None:
    single = 'Hi there'
    double = "It's raining"                      # apostrophe inside double quotes
    triple = """Line one
Line two with "quotes" and 'apostrophes'"""
    print(single, "|", double)
    print(triple)
    print(type("x"), len("x"))                   # a single character is just a str of length 1


def indexing() -> None:
    word = "Learning Python"
    print(word[0], word[9], word[-1], word[-6])  # L P n P
    try:
        word[100]
    except IndexError as e:
        print("IndexError:", e)


def immutability() -> None:
    s = "Hello, world!"
    try:
        s[0] = "J"                               # not allowed
    except TypeError as e:
        print("TypeError:", e)
    before = id(s)
    s = s[:7] + "Python!"                        # build a NEW string, rebind the name
    print(s, "| same object?", id(s) == before)


def slicing() -> None:
    text = "Hello, Python!"
    print(repr(text[7:13]))    # 'Python'
    print(repr(text[-7:-1]))   # 'Python'
    print(repr(text[:5]), repr(text[7:]))
    print(repr(text[::2]))     # every 2nd char: 'Hlo yhn'
    print(repr(text[::-1]))    # reversed
    print(repr(text[3:100]))   # slices never raise IndexError -> clipped


def operators() -> None:
    print("Ha" * 3, "+".join(["a", "b"]), "Py" + "thon")
    print("fox" in "The quick brown fox", "cat" not in "The quick brown fox")
    print("apple" < "banana", "Zebra" < "apple", "10" < "9")  # compares code points


def looping() -> None:
    for ch in "abc":
        print(ch, end=" ")
    print()
    for i, ch in enumerate("abc"):
        print(f"{i}:{ch}", end=" ")
    print()


def escapes_and_raw() -> None:
    print("Tab\tseparated\nnew line, quote: \" backslash: \\")
    print("\x41\101\u00e9\N{GREEK SMALL LETTER PI}")   # A A é π
    path = r"C:\new\table"                              # raw: backslashes kept literally
    print(path, len(path), len("C:\new\table"))         # raw has 12 chars, cooked has 10


if __name__ == "__main__":
    for f in (creating, indexing, immutability, slicing, operators, looping, escapes_and_raw):
        print(f"--- {f.__name__}")
        f()
