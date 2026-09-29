"""String formatting: f-strings, the format-spec mini-language, str.format, and % (legacy)."""
from datetime import date


def fstrings() -> None:
    name, age = "Asha", 31
    print(f"{name} is {age} years old.")
    print(f"Next year: {age + 1}, upper: {name.upper()}")
    print(f"{name!r} vs {name!s}")                # !r calls repr(), !s calls str()
    width = 10
    print(f"[{name:>{width}}]")                    # nested replacement field for the width
    total = 42
    print(f"{total = }")                           # self-documenting (3.8+)
    print(f"{{literal braces}}")


def format_spec() -> None:
    pi, n, ratio = 3.14159265, 1234567, 0.4567
    rows = [
        ("{:.2f}", f"{pi:.2f}"),         # 2 decimals
        ("{:10.3f}", f"{pi:10.3f}"),     # width 10, right-aligned numbers
        ("{:<8}|", f"{'ab':<8}|"),       # left-align in 8
        ("{:^8}|", f"{'ab':^8}|"),       # centre
        ("{:*>8}", f"{'ab':*>8}"),       # fill char '*', right-align
        ("{:,}", f"{n:,}"),              # thousands separator
        ("{:_}", f"{n:_}"),
        ("{:.1%}", f"{ratio:.1%}"),      # percentage
        ("{:e}", f"{n:e}"),              # scientific
        ("{:+d}", f"{42:+d}"),           # always show sign
        ("{:05d}", f"{42:05d}"),         # zero-pad
        ("{:b} {:o} {:x} {:X}", f"{255:b} {255:o} {255:x} {255:X}"),
        ("{:#x}", f"{255:#x}"),          # with 0x prefix
        ("{:%Y-%m-%d}", f"{date(2024, 3, 9):%Y-%m-%d}"),  # objects define their own spec
    ]
    for spec, out in rows:
        print(f"{spec:<22}-> {out!r}")


def str_format_and_percent() -> None:
    print("{} + {} = {}".format(2, 3, 5))
    print("{1} before {0}".format("A", "B"))
    print("{name} has {n} cats".format(name="Li", n=2))
    user = {"name": "Li", "n": 2}
    print("{name} has {n} cats".format(**user))
    template = "Dear {who}, your balance is {amt:,.2f}"   # reusable template stored as data
    print(template.format(who="Sam", amt=12345.6))
    print("%s is %d years, pi~%.2f" % ("Asha", 31, 3.14159))  # old printf style


if __name__ == "__main__":
    for f in (fstrings, format_spec, str_format_and_percent):
        print(f"--- {f.__name__}")
        f()
