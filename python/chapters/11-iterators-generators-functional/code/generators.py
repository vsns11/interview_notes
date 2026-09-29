"""Generator functions, generator expressions, yield from, send/close."""


def countdown(n: int):
    print("  (generator body starts)")
    while n > 0:
        yield n                          # pause here, hand n to the caller
        n -= 1
    return "liftoff"                     # becomes StopIteration.value


def read_lines(lines):
    """A pipeline stage: filter blank lines and strip whitespace, lazily."""
    for line in lines:
        line = line.strip()
        if line:
            yield line


def parse(lines):
    for line in lines:
        name, score = line.split(",")
        yield name, int(score)


def flatten(nested):
    """Recursive flattening with yield from."""
    for item in nested:
        if isinstance(item, list):
            yield from flatten(item)     # delegate to a sub-generator
        else:
            yield item


def inner():
    yield 1
    yield 2
    return "inner-done"


def outer():
    result = yield from inner()          # yield from returns the sub-generator's return value
    yield f"got {result!r}"


def running_average():
    """A coroutine-style generator: receives values via send()."""
    total, count, avg = 0.0, 0, None
    try:
        while True:
            value = yield avg            # yield out the avg, receive the next value
            total += value
            count += 1
            avg = total / count
    except GeneratorExit:
        print("  closing running_average (cleanup here)")
        raise


def fib():
    a, b = 0, 1
    while True:                          # infinite, but lazy: only computes on demand
        yield a
        a, b = b, a + b


if __name__ == "__main__":
    g = countdown(3)
    print("created:", type(g).__name__, "(nothing printed yet -> lazy)")
    print("next:", next(g))
    print("rest:", list(g))
    g2 = countdown(1)
    next(g2)
    try:
        next(g2)
    except StopIteration as stop:
        print("return value via StopIteration.value:", stop.value)

    raw = ["alice,90\n", "\n", "bob,72\n", "   ", "cara,85"]
    pipeline = parse(read_lines(raw))
    print("pipeline:", list(pipeline))

    squares = (x * x for x in range(5))
    print("genexp sum:", sum(squares), "| sum(x for x in ...) needs no extra parens:",
          sum(x for x in range(5)))

    print("flatten:", list(flatten([1, [2, [3, 4]], 5])))
    print("outer:", list(outer()))

    avg = running_average()
    next(avg)                            # "prime" it: run to the first yield
    for v in (10, 20, 60):
        print("  send", v, "->", avg.send(v))
    avg.close()

    from itertools import islice
    print("first 10 fib:", list(islice(fib(), 10)))
