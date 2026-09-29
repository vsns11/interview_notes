"""Iterable vs iterator: the protocol behind every for loop."""


class Countdown:
    """An ITERABLE: can produce a fresh iterator each time (re-iterable)."""

    def __init__(self, start: int) -> None:
        self.start = start

    def __iter__(self) -> "CountdownIterator":
        return CountdownIterator(self.start)


class CountdownIterator:
    """An ITERATOR: remembers its position, returns itself from __iter__."""

    def __init__(self, current: int) -> None:
        self.current = current

    def __iter__(self) -> "CountdownIterator":
        return self

    def __next__(self) -> int:
        if self.current <= 0:
            raise StopIteration          # signals "no more items"
        self.current -= 1
        return self.current + 1


def manual_for(iterable) -> list:
    """What `for x in iterable:` does under the hood."""
    out = []
    it = iter(iterable)                  # 1. ask for an iterator
    while True:
        try:
            x = next(it)                 # 2. pull the next item
        except StopIteration:            # 3. stop when exhausted
            break
        out.append(x)
    return out


if __name__ == "__main__":
    c = Countdown(3)
    print("for loop:", [x for x in c], "| again:", list(c))      # iterable: re-usable
    print("manual_for:", manual_for(c))

    it = iter([10, 20, 30])
    print("next:", next(it), next(it), next(it), "| default:", next(it, "done"))
    print("iter(it) is it:", iter(it) is it)

    gen = (n * n for n in range(3))      # generators are iterators -> one pass only
    print("first pass:", list(gen), "| second pass:", list(gen))

    # iter(callable, sentinel): call until the sentinel value appears
    chunks = iter(["a", "b", "", "c"])
    print("iter(callable, sentinel):", list(iter(lambda: next(chunks), "")))

    # dict iterators are invalidated by size changes
    d = {"a": 1, "b": 2}
    try:
        for k in d:
            d["c"] = 3
    except RuntimeError as e:
        print("RuntimeError:", e)
