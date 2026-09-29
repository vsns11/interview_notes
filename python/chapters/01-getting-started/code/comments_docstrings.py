"""Chapter 1 - comments vs docstrings, PEP 20, and keywords.

This first string in the file is the MODULE docstring; it is stored in __doc__.
"""

import contextlib
import io
import keyword


def area(width: float, height: float) -> float:
    """Return the area of a rectangle.

    Args:
        width: size along x, in metres.
        height: size along y, in metres.
    """
    # A comment explains WHY; the code already says WHAT.
    return width * height  # inline comment: two spaces before the '#'


def main() -> None:
    # single-line comments are thrown away by the tokenizer: they never reach the bytecode
    print("module doc first line:", __doc__.splitlines()[0])
    print("function doc first line:", area.__doc__.splitlines()[0])

    """A bare triple-quoted string here is NOT a comment.
    It is an expression statement that is evaluated and discarded (the compiler
    usually optimises it away). Use # lines for comments."""

    print("area(3, 4) =", area(3, 4))

    # PEP 20 - The Zen of Python lives inside the interpreter
    buffer = io.StringIO()
    with contextlib.redirect_stdout(buffer):
        import this  # noqa: F401  (printing is a side effect of the import)
    lines = [ln for ln in buffer.getvalue().splitlines() if ln and not ln.startswith("The Zen")]
    print(f"Zen of Python has {len(lines)} aphorisms, e.g.:")
    for ln in lines[:3] + [lines[6]]:
        print("  ", ln)

    print(f"{len(keyword.kwlist)} hard keywords, e.g.", keyword.kwlist[:6])
    print("soft keywords (only special in some places):", keyword.softkwlist)


if __name__ == "__main__":
    main()
