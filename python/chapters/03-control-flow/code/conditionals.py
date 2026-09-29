"""Chapter 3 - if / elif / else, conditional expressions, truthiness in conditions."""


def describe(x: int) -> str:
    if x > 20:
        return "x is greater than 20"
    elif x > 10:
        return "x is greater than 10 but not more than 20"
    else:
        return "x is 10 or less"


def grade(score: int) -> str:
    # order matters: the first true branch wins, so test the strictest condition first
    if not 0 <= score <= 100:
        raise ValueError(f"score out of range: {score}")
    if score >= 90:
        return "A"
    elif score >= 80:
        return "B"
    elif score >= 70:
        return "C"
    return "F"                       # early return makes a final `else` unnecessary


def shipping_cost(total: float, is_member: bool) -> float:
    # guard clauses keep nesting shallow
    if total <= 0:
        return 0.0
    if is_member or total >= 50:
        return 0.0
    return 4.99


def main() -> None:
    x = 10
    if x > 5:
        print("x is greater than 5")

    x = 3
    if x > 5:
        print("x is greater than 5")
    else:
        print("x is not greater than 5")

    for x in (25, 15, 5):
        print(x, "->", describe(x))

    print("grades:", [grade(s) for s in (95, 85, 72, 40)])
    print("shipping:", shipping_cost(30, False), shipping_cost(30, True), shipping_cost(80, False))

    # conditional expression: value_if_true if condition else value_if_false
    n = 7
    parity = "even" if n % 2 == 0 else "odd"
    print(f"{n} is {parity}")

    # truthiness: empty containers, 0, None are false
    cart: list[str] = []
    if not cart:
        print("cart is empty")
    username = None
    print("hello,", username or "guest")

    # `is None` vs falsy: 0 is a real value, not a missing one
    discount = 0
    print("falsy check says missing:", not discount, "| is-None check says missing:", discount is None)

    # a boolean flag with has_access
    has_access = True
    print("Access granted." if has_access else "Access denied.")

    # combining conditions; `in` replaces long `or` chains
    day = "sat"
    if day in {"sat", "sun"}:
        print("weekend")
    temp, raining = 22, False
    if 18 <= temp <= 26 and not raining:
        print("good weather for a walk")


if __name__ == "__main__":
    main()
