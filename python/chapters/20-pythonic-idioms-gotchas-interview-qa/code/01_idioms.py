"""20.1 Pythonic idioms, each shown next to the non-Pythonic version."""
import tempfile
from collections import defaultdict
from contextlib import contextmanager, suppress
from pathlib import Path
import time


def eafp_vs_lbyl() -> None:
    config = {"port": "8080"}
    # LBYL - Look Before You Leap
    if "host" in config:
        host = config["host"]
    else:
        host = "localhost"
    # EAFP - Easier to Ask Forgiveness than Permission
    try:
        port = int(config["port"])
    except (KeyError, ValueError):
        port = 80
    print("EAFP/LBYL:", host, port, "| .get:", config.get("host", "localhost"))
    with suppress(FileNotFoundError):          # "ignore this one error" idiom
        Path("/definitely/not/here.txt").unlink()
    print("suppress(FileNotFoundError): no crash")


def unpacking() -> None:
    a, b = 1, 2
    a, b = b, a                                 # swap, no temp variable
    first, *middle, last = [10, 20, 30, 40, 50]
    (x, y), z = (1, 2), 3                      # nested
    point = {"x": 3, "y": 4}
    merged = {**point, "z": 5}                 # dict unpacking
    both = [*range(3), *"ab"]
    print("swap:", a, b, "| star:", first, middle, last, "| nested:", x, y, z)
    print("dict merge:", merged, "| point | {'z': 5} ->", point | {"z": 5}, "| list:", both)
    for name, score in [("ann", 91), ("bob", 78)]:   # unpack in the loop header
        pass
    _, ext = "report.final.pdf".rsplit(".", 1)       # _ = "don't care"
    print("rsplit + _:", ext)


def enumerate_zip() -> None:
    names = ["ann", "bob", "cy"]
    scores = [91, 78, 85]
    for i in range(len(names)):                 # non-Pythonic
        pass
    for rank, name in enumerate(names, start=1):
        print(f"  {rank}. {name}")
    print("zip:", list(zip(names, scores)), "| dict(zip):", dict(zip(names, scores)))
    try:
        list(zip(names, [1, 2], strict=True))   # 3.10+: catch length mismatches
    except ValueError as e:
        print("zip(strict=True):", e)
    rows = [(1, "a"), (2, "b")]
    nums, letters = zip(*rows)                  # "unzip"
    print("unzip:", nums, letters)


def comprehensions() -> None:
    squares = [n * n for n in range(6)]
    evens = [n for n in range(10) if n % 2 == 0]
    labels = ["even" if n % 2 == 0 else "odd" for n in range(4)]
    by_len = {w: len(w) for w in ["hi", "hello"]}
    initials = {w[0] for w in ["apple", "avocado", "banana"]}
    total = sum(n * n for n in range(1_000_000))   # generator: no big list
    flat = [x for row in [[1, 2], [3]] for x in row]
    print("list:", squares, "| filter:", evens, "| if-else:", labels)
    print("dict:", by_len, "| set:", initials, "| gen sum:", total, "| flatten:", flat)


def truthiness() -> None:
    falsy = [None, False, 0, 0.0, 0j, "", [], (), {}, set(), range(0)]
    print("all falsy:", not any(falsy))
    items: list[int] = []
    if not items:                               # not: if len(items) == 0
        print("empty list is falsy")
    count = 0
    print("`if count:` is False for 0 -> use `if count is not None:` when 0 is valid:",
          count is not None)


def fstrings() -> None:
    name, price, ratio = "Widget", 1234.5678, 0.4567
    print(f"{name:<8}|{price:>12,.2f}|{ratio:.1%}|{42:05d}|{255:#x}|{255:b}")
    print(f"{price=}")                          # 3.8+: self-documenting
    print(f"{name!r} {'nested quotes ok'}")


@contextmanager
def timer(label: str):
    start = time.perf_counter()
    try:
        yield                                  # body of the with-block runs here
    finally:
        print(f"{label}: done ({'fast' if time.perf_counter() - start < 1 else 'slow'})")


def files_and_pathlib() -> None:
    with tempfile.TemporaryDirectory() as tmp:
        base = Path(tmp)
        notes = base / "notes" / "day1.txt"     # / joins paths
        notes.parent.mkdir(parents=True, exist_ok=True)
        with open(notes, "w", encoding="utf-8") as f:   # closed even on error
            f.write("line one\nline two\n")
        with notes.open(encoding="utf-8") as f:
            for line_no, line in enumerate(f, 1):      # stream line by line
                print(f"  {line_no}: {line.rstrip()}")
        (base / "b.csv").write_text("a,b\n1,2\n", encoding="utf-8")
        print("name/stem/suffix:", notes.name, notes.stem, notes.suffix)
        print("glob *.csv:", [p.name for p in base.glob("*.csv")])
        print("rglob *.txt:", [p.relative_to(base).as_posix() for p in base.rglob("*.txt")])
        print("exists:", notes.exists(), "| read_text:", repr(notes.read_text(encoding="utf-8")[:8]))


def misc() -> None:
    counts = defaultdict(int)
    for w in "the cat the hat".split():
        counts[w] += 1
    print("defaultdict count:", dict(counts))
    x = 5
    print("chained comparison 0 < x < 10:", 0 < x < 10)
    print("conditional expression:", "big" if x > 3 else "small")
    print("any/all:", any(n > 4 for n in [1, 5]), all(n > 0 for n in [1, 5]))
    print("max with key/default:", max(["aa", "b"], key=len), max([], default=None))
    for n in [1, 3, 5]:
        if n % 2 == 0:
            print("found even")
            break
    else:
        print("for-else: loop finished without break -> no even number")
    if (m := len("walrus")) > 3:                 # 3.8+ assignment expression
        print("walrus:", m)
    print("join:", ", ".join(["a", "b", "c"]))


if __name__ == "__main__":
    eafp_vs_lbyl()
    unpacking()
    enumerate_zip()
    comprehensions()
    truthiness()
    fstrings()
    with timer("context manager"):
        sum(range(1000))
    files_and_pathlib()
    misc()
