"""A tour of itertools (Python 3.12+ for batched)."""
import itertools as it
import operator

if __name__ == "__main__":
    print("count:", list(it.islice(it.count(10, 5), 4)))
    print("cycle:", list(it.islice(it.cycle("AB"), 5)))
    print("repeat:", list(it.repeat("x", 3)))
    print("chain:", list(it.chain([1, 2], (3,), "ab")))
    print("chain.from_iterable:", list(it.chain.from_iterable([[1, 2], [3]])))
    print("islice(range(10), 2, 8, 2):", list(it.islice(range(10), 2, 8, 2)))

    words = ["apple", "avocado", "banana", "blueberry", "cherry", "apricot"]
    unsorted = {k: list(g) for k, g in it.groupby(words, key=lambda w: w[0])}
    print("groupby UNSORTED (apricot lost/overwritten):", unsorted)
    grouped = {k: list(g) for k, g in it.groupby(sorted(words), key=lambda w: w[0])}
    print("groupby sorted:", grouped)

    print("product:", list(it.product("ab", [1, 2])))
    print("product repeat=2 of 01:", ["".join(p) for p in it.product("01", repeat=2)])
    print("permutations(abc, 2):", ["".join(p) for p in it.permutations("abc", 2)])
    print("combinations(abc, 2):", ["".join(p) for p in it.combinations("abc", 2)])
    print("combinations_with_replacement(ab, 2):",
          ["".join(p) for p in it.combinations_with_replacement("ab", 2)])

    print("accumulate (running sum):", list(it.accumulate([3, 1, 4, 1, 5])))
    print("accumulate max:", list(it.accumulate([3, 1, 4, 1, 5], max)))
    print("accumulate product, initial=1:", list(it.accumulate([2, 3, 4], operator.mul, initial=1)))

    print("pairwise:", list(it.pairwise([1, 4, 9, 16])))
    diffs = [b - a for a, b in it.pairwise([1, 4, 9, 16])]
    print("differences via pairwise:", diffs)
    print("batched:", list(it.batched(range(7), 3)))

    print("takewhile <5:", list(it.takewhile(lambda x: x < 5, [1, 3, 6, 2])))
    print("dropwhile <5:", list(it.dropwhile(lambda x: x < 5, [1, 3, 6, 2])))
    print("filterfalse even:", list(it.filterfalse(lambda x: x % 2 == 0, range(6))))
    print("compress:", list(it.compress("ABCD", [1, 0, 1, 0])))
    print("starmap pow:", list(it.starmap(pow, [(2, 3), (3, 2)])))
    print("zip_longest:", list(it.zip_longest("ab", [1, 2, 3], fillvalue="-")))
    a, b = it.tee(iter([1, 2, 3]))
    print("tee:", list(a), list(b))
