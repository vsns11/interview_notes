"""Chapter 2 - implicit and explicit type conversion."""


def main() -> None:
    # implicit: int widened to float (bool -> int -> float -> complex)
    result = 6 + 1.5
    print("6 + 1.5 =", result, type(result).__name__)
    print("True + 2 =", True + 2, "| 2 + 1j =", 2 + 1j)

    # explicit conversions from the course table
    table = [
        ("str(10)", str(10)),
        ('int("20")', int("20")),
        ("float(4)", float(4)),
        ("int(True), int(False)", (int(True), int(False))),
        ("int(2.8)", int(2.8)),
        ('float("3.5")', float("3.5")),
        ("bool(0)", bool(0)),
        ("complex(2, 3)", complex(2, 3)),
        ('list("abc")', list("abc")),
        ('tuple("abc")', tuple("abc")),
        ('sorted(set("abac"))', sorted(set("abac"))),
        ("dict([(1,'one'),(2,'two')])", dict([(1, "one"), (2, "two")])),
        ("bin(3), hex(255), oct(8)", (bin(3), hex(255), oct(8))),
        ('int("ff", 16), int("0b11", 0)', (int("ff", 16), int("0b11", 0))),
        ('int("  42\\n"), int("1_000")', (int("  42\n"), int("1_000"))),
        ("ord('A'), chr(66)", (ord("A"), chr(66))),
    ]
    for expr, value in table:
        print(f"{expr:32} -> {value!r}")

    # conversions that fail loudly
    for bad in ['int("3.5")', 'int("abc")', 'float("1,5")', "int(None)"]:
        try:
            eval(bad)
        except (ValueError, TypeError) as err:
            print(f"{bad:12} -> {type(err).__name__}: {err}")

    # the classic surprise: every non-empty string is truthy
    print('bool("False") =', bool("False"), '| bool("") =', bool(""))
    # the right way to parse "yes/no" text
    text = "False"
    print("parsed:", text.strip().lower() in {"true", "1", "yes"})

    # str() vs repr()
    print("str:", str("hi\n"), end="")
    print("repr:", repr("hi\n"))

    # combining text and numbers: prefer f-strings over str() + concatenation
    age = 30
    print("Age: " + str(age), "|", f"Age: {age}", "|", f"pi ~ {3.14159:.2f}")


if __name__ == "__main__":
    main()
