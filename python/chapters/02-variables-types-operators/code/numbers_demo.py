"""Chapter 2 - int, float, complex, Decimal, Fraction, random, plus a float-spacing plot."""

import math
import random
import sys
from decimal import ROUND_HALF_UP, Decimal
from fractions import Fraction
from pathlib import Path


def main() -> None:
    # --- int: arbitrary precision ------------------------------------------------------
    a, b = 10, 3
    print("sum:", a + b)
    big = 2 ** 100
    print("2**100 =", big, "| bits:", big.bit_length())
    print("factorial(25) =", math.factorial(25))
    print("readable literal 1_000_000 =", 1_000_000, "| hex/bin/oct:", 0xFF, 0b101, 0o17)
    print("int->str digit limit (CPython 3.11+):", sys.get_int_max_str_digits())

    # --- float: IEEE-754 double -------------------------------------------------------------
    total = 0.1 + 0.2
    print("0.1 + 0.2 =", total, "| == 0.3?", total == 0.3, "| isclose?", math.isclose(total, 0.3))
    print("exact value stored for 0.1:", Decimal(0.1))
    print("max float:", sys.float_info.max, "| epsilon:", sys.float_info.epsilon)
    print("digits trusted (dig):", sys.float_info.dig)
    print("2**53 + 1 as float:", float(2 ** 53 + 1), "(lost the +1)")
    print("inf/nan:", float("inf") > 1e308, float("nan") == float("nan"))
    print("round(2.5), round(3.5):", round(2.5), round(3.5), "| round(2.675, 2):", round(2.675, 2))

    # --- Decimal: base-10, exact for money ---------------------------------------------
    price = Decimal("0.10") + Decimal("0.20")
    print("Decimal 0.10+0.20 =", price, "| == 0.30?", price == Decimal("0.30"))
    print("Decimal from float (wrong way):", Decimal(0.1))
    print("round half up 2.675 ->", Decimal("2.675").quantize(Decimal("0.01"), rounding=ROUND_HALF_UP))

    # --- Fraction: exact rationals --------------------------------------------------------
    third = Fraction(1, 3)
    running = 0.0
    for _ in range(10):
        running += 0.1                     # error accumulates with each +=
    print("1/3 + 1/6 =", third + Fraction(1, 6), "| 0.1 added ten times:", running,
          "| ten Fraction(1, 10) == 1:", sum([Fraction(1, 10)] * 10) == 1)
    print("sum([0.1]*10) (3.12+ uses compensated summation):", sum([0.1] * 10), "| math.fsum:", math.fsum([0.1] * 10))

    # --- complex ----------------------------------------------------------------------------
    z = 1 + 2j
    print("z.real:", z.real, "z.imag:", z.imag, "|z|:", abs(3 + 4j), "conjugate:", z.conjugate())

    # --- division family ----------------------------------------------------------------
    print("7/2 =", 7 / 2, "| 7//2 =", 7 // 2, "| -7//2 =", -7 // 2, "| -7%2 =", -7 % 2,
          "| divmod(17,5) =", divmod(17, 5))
    print("int(-2.7) truncates:", int(-2.7), "| math.floor(-2.7):", math.floor(-2.7))

    # --- random (seeded so output is repeatable) --------------------------------------------
    random.seed(42)
    print("randint(1,10):", random.randint(1, 10), "| random():", round(random.random(), 4),
          "| choice:", random.choice(["red", "green", "blue"]))

    plot_float_spacing()


def plot_float_spacing() -> None:
    """Save a log-log plot of math.ulp(x): the gap between x and the next float."""
    import matplotlib

    matplotlib.use("Agg")
    import matplotlib.pyplot as plt

    xs = [10.0 ** k for k in range(-6, 19)]
    gaps = [math.ulp(x) for x in xs]
    fig, ax = plt.subplots(figsize=(7, 4))
    ax.loglog(xs, gaps, marker="o", color="#2a6fdb")
    ax.axvline(2.0 ** 53, color="#d1495b", linestyle="--", label="2**53: gap reaches 2")
    ax.set_xlabel("magnitude of x")
    ax.set_ylabel("gap to next float, math.ulp(x)")
    ax.set_title("Floats get sparser as numbers get bigger")
    ax.grid(True, which="major", alpha=0.3)
    ax.legend()
    out = Path(__file__).resolve().parent.parent / "img" / "float_spacing.png"
    fig.tight_layout()
    fig.savefig(out, dpi=120)
    print("saved", out.name, "| ulp(1.0) =", math.ulp(1.0), "| ulp(1e16) =", math.ulp(1e16))


if __name__ == "__main__":
    main()
