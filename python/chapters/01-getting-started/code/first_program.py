"""Chapter 1 - your first program and the basic syntax rules, all in one runnable file."""

import keyword
import sys


def hello() -> None:
    # print() writes its arguments to standard output, followed by a newline
    print("Hello, World!")
    # arguments are joined by sep (default " ") and finished with end (default "\n")
    print("Python", sys.version_info.major, sys.version_info.minor, sep=".", end="  <- running version\n")


def indentation_demo(temperature: int) -> str:
    # the body of `if` / `else` is the indented block; the block ends when indentation returns
    if temperature > 30:
        label = "hot"
    else:
        label = "mild"
    return label


def line_continuation_demo() -> tuple[int, int]:
    # explicit continuation with a backslash (works, but fragile: a trailing space breaks it)
    total_backslash = 1 + \
        2 + \
        3
    # implicit continuation inside brackets - the style PEP 8 prefers
    total_brackets = (
        1
        + 2
        + 3
    )
    return total_backslash, total_brackets


def quotes_demo() -> None:
    single = 'It said "hi"'            # single quotes let you use " inside
    double = "It's sunny"              # double quotes let you use ' inside
    triple = """Line one
Line two"""                            # triple quotes span lines
    print(single, "|", double, "|", triple.splitlines())


def fake_input(prompt: str) -> str:
    """Stand-in for input() so the script runs unattended; input() always returns a str."""
    answer = "Ada"
    print(prompt + answer)
    return answer


def main() -> None:
    hello()
    print("indentation:", indentation_demo(35), indentation_demo(20))
    print("continuation:", line_continuation_demo())
    quotes_demo()

    user_name = fake_input("Please enter your name: ")   # real code: input("...")
    print("Hello, " + user_name + "!")

    # several statements on one line with ';' - legal but discouraged by PEP 8
    x = "foo"; sys.stdout.write(x + "\n")

    # identifiers: letters, digits, underscore, not starting with a digit, not a keyword
    for name in ["total", "_private", "user2", "2user", "user-name", "class", "café"]:
        ok = name.isidentifier() and not keyword.iskeyword(name)
        print(f"{name!r:12} valid identifier? {ok}")


if __name__ == "__main__":
    main()
