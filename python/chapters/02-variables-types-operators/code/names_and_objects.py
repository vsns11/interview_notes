"""Chapter 2 - variables are names bound to objects."""


def main() -> None:
    # --- creating, printing, multiple assignment ---------------------------------
    first_name, age, height = "John", 30, 5.11     # tuple unpacking: 3 names in one line
    print(first_name, age, height)
    a = b = c = 0                                   # chained assignment: 3 names, ONE object
    x, y = 1, 2
    x, y = y, x                                     # swap without a temp variable
    print("swap:", x, y, "| chained:", a, b, c)
    head, *rest = [10, 20, 30, 40]                  # starred unpacking
    print("head:", head, "rest:", rest)

    # --- case sensitivity ----------------------------------------------------------
    Age, age, AGE = 25, 30, 35
    print("Age/age/AGE:", Age, age, AGE)

    # --- a name can be re-bound to any type (dynamic typing) ---------------------------
    value = 42
    print(type(value).__name__, end=" -> ")
    value = "forty-two"
    print(type(value).__name__)

    # --- ...but objects never change type silently (strong typing) ---------------------
    try:
        "Age: " + 30  # type: ignore[operator]
    except TypeError as err:
        print("TypeError:", err)

    # --- aliasing: two names, one list ------------------------------------------------
    team = ["ann", "bob"]
    squad = team                   # no copy is made
    squad.append("cy")             # mutate through one name ...
    print("team:", team, "| same object?", team is squad)   # ... visible through the other

    # --- rebinding is not mutation ------------------------------------------------------
    n = 10
    m = n
    m += 1                         # ints are immutable: += builds a NEW int and rebinds m
    print("n:", n, "m:", m)

    # --- identity vs equality -----------------------------------------------------------
    list_a = [1, 2, 3]
    list_b = [1, 2, 3]
    list_c = list_a
    print("a == b:", list_a == list_b, "| a is b:", list_a is list_b, "| a is c:", list_a is list_c)
    print("id(a) == id(c):", id(list_a) == id(list_c))

    # --- mutable vs immutable ------------------------------------------------------------
    word = "cat"
    try:
        word[0] = "b"  # type: ignore[index]
    except TypeError as err:
        print("str is immutable:", err)
    t = ([1], [2])
    t[0].append(99)                # the tuple cannot change, but a list INSIDE it can
    print("tuple holding a list:", t)

    # --- del removes the NAME, not necessarily the object -------------------------------
    player_score = 120
    alias = player_score
    del player_score
    try:
        print(player_score)  # noqa: F821
    except NameError as err:
        print(type(err).__name__, "(a NameError subclass, inside a function):", err)
    print("object still reachable via alias:", alias)
    try:
        exec("player = 'Alice'\ndel player\nprint(player)", {})   # same thing at module level
    except NameError as err:
        print("NameError at module level:", err)

    # --- None ------------------------------------------------------------------------------
    result = None
    print("result is None:", result is None)


if __name__ == "__main__":
    main()
