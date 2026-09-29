"""Chapter 2 - every operator family, precedence, short-circuiting, walrus, chaining."""

import re


def main() -> None:
    a, b = 10, 3
    print("arith:", a + b, a - b, a * b, a / b, a % b, a ** b, a // b)
    print("compare:", a == b, a != b, a > b, a < b, a >= b, a <= b)

    # augmented assignment walk-through (same sequence as the lesson's example)
    v = 10
    v += 3; print("v += 3  ->", v)     # 13
    v -= 2; print("v -= 2  ->", v)     # 11
    v *= 2; print("v *= 2  ->", v)     # 22
    v /= 2; print("v /= 2  ->", v)     # 11.0  (true division always gives a float)
    v %= 4; print("v %= 4  ->", v)     # 3.0
    v **= 2; print("v **= 2 ->", v)    # 9.0
    v //= 2; print("v //= 2 ->", v)    # 4.0
    v = int(v)                         # bitwise operators need ints
    v &= 3; print("v &= 3  ->", v)     # 0b100 & 0b011 = 0
    v |= 8; print("v |= 8  ->", v)     # 0b0000 | 0b1000 = 8
    v ^= 6; print("v ^= 6  ->", v)     # 0b1000 ^ 0b0110 = 14
    v <<= 1; print("v <<= 1 ->", v)    # 28
    v >>= 2; print("v >>= 2 ->", v)    # 7

    # += mutates lists in place, but rebinds for immutable types
    nums = [1, 2]
    alias = nums
    nums += [3]
    print("list += mutates in place:", alias)

    # logical operators return one of their OPERANDS, and short-circuit
    print("True and False:", True and False, "| True or False:", True or False, "| not True:", not True)
    print("'' or 'guest' ->", "" or "guest", "| 0 and 1/0 ->", 0 and 1 / 0)

    # bitwise
    x, y = 2, 3
    print("bitwise:", x & y, x | y, x ^ y, ~x, x << 1, x >> 1, f"| {x:04b} & {y:04b} = {x & y:04b}")

    # membership
    items = [1, 2, 3, 4, 5]
    print("membership:", 3 in items, 6 not in items, "ell" in "hello", "age" in {"age": 1})

    # identity
    l1, l2 = [1, 2, 3], [1, 2, 3]
    l3 = l1
    print("identity:", l1 is l2, l1 is l3, l1 is not l2)

    # precedence - the lesson's expression, then the same thing fully parenthesised
    a, b, c = 10, 20, 30
    result = a + b * c ** 2 / 10 - 5 <= b or b % a == 0 and c > b
    explicit = ((a + ((b * (c ** 2)) / 10) - 5) <= b) or ((b % a == 0) and (c > b))
    print("precedence:", result, explicit, "| left side value:", a + b * c ** 2 / 10 - 5)
    print("-2 ** 2 =", -2 ** 2, "| (-2) ** 2 =", (-2) ** 2, "| 2 ** 3 ** 2 =", 2 ** 3 ** 2)
    print("not 1 == 2 ->", not 1 == 2, "| 1 < 2 == 2 ->", 1 < 2 == 2)

    # chained comparisons: a < b < c means (a < b) and (b < c), b evaluated once
    age = 25
    print("18 <= age < 65:", 18 <= age < 65)

    # walrus := assigns inside an expression (3.8+)
    log = ["ok", "error: disk full", "ok"]
    for line in log:
        if (m := re.match(r"error: (.*)", line)) is not None:
            print("walrus captured:", m.group(1))
    data = [3, 8, 1]
    if (count := len(data)) > 2:
        print(f"walrus: {count} items")

    # float equality and the ternary-like conditional expression
    print("0.1*3 == 0.3:", 0.1 * 3 == 0.3, "| abs diff:", abs(0.1 * 3 - 0.3) < 1e-9)
    print("even" if 10 % 2 == 0 else "odd")


if __name__ == "__main__":
    main()
