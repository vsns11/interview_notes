"""Chapter 3 - for, while, break/continue/pass, loop-else, and everyday loop idioms."""


def main() -> None:
    # --- for over any iterable -------------------------------------------------------
    for fruit in ["apple", "banana", "cherry"]:
        print(fruit, end=" ")
    print()
    for ch in "hey":
        print(ch.upper(), end="")
    print()

    # --- dictionaries -----------------------------------------------------------------------
    info = {"name": "John", "age": 30, "city": "New York"}
    for key in info:                          # iterating a dict gives its KEYS
        print(key, "->", info[key])
    for key, value in info.items():           # preferred: key and value together
        print(f"{key}={value}", end=" ")
    print("| values:", list(info.values()))

    # --- nested loops -----------------------------------------------------------------------
    matrix = [[1, 2, 3], [4, 5, 6]]
    for row in matrix:
        for num in row:
            print(num, end=" ")
        print()

    # --- range -------------------------------------------------------------------------------
    print(list(range(10)), list(range(1, 11)), list(range(1, 11, 2)), list(range(10, 0, -3)))

    # --- enumerate and zip -------------------------------------------------------------
    names = ["ann", "bob", "cy"]
    scores = [91, 78, 85]
    for i, name in enumerate(names, start=1):
        print(f"{i}. {name}", end="  ")
    print()
    for name, score in zip(names, scores, strict=True):   # strict: lengths must match (3.10+)
        print(f"{name}:{score}", end="  ")
    print()
    try:
        list(zip([1, 2], [1], strict=True))
    except ValueError as err:
        print("zip strict:", err)
    print("plain zip stops at the shortest:", list(zip([1, 2, 3], "ab")))

    # --- while ------------------------------------------------------------------------------
    count = 0
    while count < 5:
        print(f"Count is {count}", end="; ")
        count += 1
    print()

    # a while loop when the number of steps is unknown: halve until below 1
    n, steps = 100.0, 0
    while n >= 1:
        n /= 2
        steps += 1
    print("halvings until < 1:", steps)

    # --- break / continue / pass ------------------------------------------------------
    for number in [1, 2, 3, 4, 5, 6]:
        if number == 4:
            print("Number 4 found, stopping loop.")
            break
    odds = []
    for number in [1, 2, 3, 4, 5, 6]:
        if number % 2 == 0:
            continue
        odds.append(number)
    print("continue kept odds:", odds)
    for number in [1, 2, 3]:
        if number % 2 == 0:
            pass              # placeholder: does nothing, the print still runs
        print(number, end=" ")
    print()

    # break only leaves the INNERMOST loop
    for i in range(3):
        for j in range(3):
            if j == 1:
                break
            print(f"({i},{j})", end=" ")
    print()

    # --- for-else and while-else --------------------------------------------------------
    for number in [1, 2, 3, 4, 5]:
        if number == 3:
            print("Number 3 found, stopping search.")
            break
    else:
        print("Number not found.")          # skipped: the loop hit break

    for number in [1, 2, 4, 5]:
        if number == 3:
            print("found 3")
            break
    else:
        print("3 not in list (else ran: no break)")

    count = 0
    while count < 5:
        if count == 2:
            print("Breaking at count 2.")
            break
        count += 1
    else:
        print("Loop was uninterrupted.")

    print("is 29 prime?", is_prime(29), "| is 21 prime?", is_prime(21))

    # --- idioms -----------------------------------------------------------------------------
    nums = [4, -1, 7, 0, 3]
    print("sum/min/max:", sum(nums), min(nums), max(nums))
    print("any negative?", any(x < 0 for x in nums), "| all < 10?", all(x < 10 for x in nums))
    print("squares of positives:", [x * x for x in nums if x > 0])
    print("reversed:", list(reversed(nums)), "| sorted desc:", sorted(nums, reverse=True))
    print("first > 5:", next((x for x in nums if x > 5), None))

    counts: dict[str, int] = {}
    for word in "the cat and the hat".split():
        counts[word] = counts.get(word, 0) + 1
    print("word counts:", counts)

    # don't remove from a list while looping over it - build a new one instead
    data = [1, 2, 2, 3]
    buggy = data.copy()
    for x in buggy:
        if x == 2:
            buggy.remove(x)
    print("remove-while-iterating left:", buggy, "| filtered correctly:", [x for x in data if x != 2])

    # dict size can't change during iteration
    try:
        d = {"a": 1, "b": 2}
        for k in d:
            d["c"] = 3
    except RuntimeError as err:
        print("RuntimeError:", err)


def is_prime(n: int) -> bool:
    if n < 2:
        return False
    for d in range(2, int(n ** 0.5) + 1):
        if n % d == 0:
            return False
    return True


if __name__ == "__main__":
    main()
