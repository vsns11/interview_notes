"""A tour of the most useful str methods, grouped by job."""


def case_methods() -> None:
    s = "pYthon is FUN"
    print(s.upper(), "|", s.lower(), "|", s.capitalize(), "|", s.title(), "|", s.swapcase())
    print("they're bill's".title())            # title() treats ' as a word break
    print("Straße".lower(), "Straße".casefold())  # casefold is the aggressive, caseless form


def alignment() -> None:
    print("[" + "Menu".center(16, "-") + "]")
    print("[" + "Menu".ljust(10) + "]", "[" + "Menu".rjust(10, ".") + "]", "42".zfill(5))


def split_strip_join() -> None:
    text = "  Hello, Python! Welcome to programming.  "
    print(text.strip().split())                # any whitespace, runs collapsed
    print(text.strip().rsplit(" ", 1))         # split once, from the right
    print("a,,b".split(","), "a  b".split(" "), "a  b".split())
    print("k=v=w".partition("="), "k=v=w".rpartition("="), "abc".partition("="))
    print("line1\nline2\r\nline3".splitlines())
    print("xxhixx".strip("x"), "--hi".lstrip("-"), "hi--".rstrip("-"))
    print("report_2024.csv".removesuffix(".csv"), "Mr. Smith".removeprefix("Mr. "))
    print("abcabc".strip("ab"))                # strip removes a SET of chars, not a prefix
    print("-".join(["2024", "03", "09"]), ", ".join(str(n) for n in [1, 2, 3]))


def boolean_checks() -> None:
    samples = ["Hello123", "Hello", "123", "½", "²", "   ", "Hello World", "my_var", "3var"]
    print(f"{'value':<14}{'alnum':>6}{'alpha':>6}{'decim':>6}{'digit':>6}{'numer':>6}{'space':>6}{'title':>6}{'ident':>6}")
    for v in samples:
        flags = [v.isalnum(), v.isalpha(), v.isdecimal(), v.isdigit(), v.isnumeric(),
                 v.isspace(), v.istitle(), v.isidentifier()]
        print(f"{v!r:<14}" + "".join(f"{str(x)[0]:>6}" for x in flags))
    print("Python Programming".startswith("Python"), "file.tar.gz".endswith((".gz", ".zip")))


def find_replace() -> None:
    t = "hello world, hello universe, hello multiverse"
    print(t.count("hello"), t.find("hello"), t.rfind("hello"), t.find("bye"))
    print(t.find("hello", 1))                  # search starting at index 1
    try:
        t.index("bye")
    except ValueError as e:
        print("ValueError:", e)
    print(t.replace("hello", "hi", 2))         # only first 2 occurrences


def translate_demo() -> None:
    table = str.maketrans({"a": "4", "e": "3", "o": "0", "!": None})  # None = delete
    print("hello, leet coders!".translate(table))


if __name__ == "__main__":
    for f in (case_methods, alignment, split_strip_join, boolean_checks, find_replace, translate_demo):
        print(f"--- {f.__name__}")
        f()
