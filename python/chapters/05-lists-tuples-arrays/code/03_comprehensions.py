"""List comprehensions vs loops, filtering, nesting, and when a generator is better."""
import timeit


def loop_vs_comprehension() -> None:
    numbers = [1, 2, 3, 4, 5]
    squared = []
    for n in numbers:                          # the traditional way
        squared.append(n ** 2)
    print("loop:", squared)
    print("comp:", [n ** 2 for n in numbers])
    print("even squares:", [n ** 2 for n in numbers if n % 2 == 0])
    print("if/else in expr:", ["even" if n % 2 == 0 else "odd" for n in numbers])
    print([f"Number {n} squared is {n ** 2}." for n in numbers[:2]])


def nested() -> None:
    matrix = [[1, 2, 3], [4, 5, 6]]
    flat = [x for row in matrix for x in row]          # same order as nested for-loops
    transposed = [[row[i] for row in matrix] for i in range(3)]
    pairs = [(c, n) for c in "ab" for n in (1, 2)]
    print(flat, transposed, pairs)


def generator_expression() -> None:
    total = sum(n * n for n in range(1_000_000))       # no list materialised
    print("sum of squares:", total)


def speed() -> None:
    setup = "data = list(range(10_000))"
    loop = """
out = []
for x in data:
    out.append(x * 2)
"""
    comp = "out = [x * 2 for x in data]"
    t_loop = min(timeit.repeat(loop, setup, number=200, repeat=3))
    t_comp = min(timeit.repeat(comp, setup, number=200, repeat=3))
    print(f"loop+append: {t_loop * 1000:.1f} ms   comprehension: {t_comp * 1000:.1f} ms "
          f"(ratio {t_loop / t_comp:.1f}x; machine-dependent)")


if __name__ == "__main__":
    for f in (loop_vs_comprehension, nested, generator_expression, speed):
        print(f"--- {f.__name__}")
        f()
