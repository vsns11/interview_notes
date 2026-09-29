"""Hashing: what makes a key usable, how equal objects must hash equal, and what breaks."""
import sys


def hashable_or_not() -> None:
    for obj in [42, 3.0, "hi", (1, 2), frozenset({1}), None, True, [1], {1: 2}, {1}, (1, [2])]:
        try:
            h = hash(obj)
            shown = "<salted: differs every run>" if isinstance(obj, str) else h
            print(f"{obj!r:<16} hashable    hash={shown}")
        except TypeError as e:
            print(f"{obj!r:<16} UNHASHABLE  ({e})")


def equal_means_same_key() -> None:
    d = {1: "int one"}
    d[1.0] = "float one"            # 1 == 1.0 and hash(1) == hash(1.0) -> SAME key
    d[True] = "bool true"           # True == 1 as well
    print(d, hash(1) == hash(1.0) == hash(True))


def small_int_hash_and_strings() -> None:
    print("hash(12345) =", hash(12345), "| hash(-1) =", hash(-1))
    print("str hashes are salted per process (PYTHONHASHSEED); int hashes are not")


class BadKey:
    """Mutable object whose hash depends on mutable state: a bug waiting to happen."""
    def __init__(self, v):
        self.v = v
    def __eq__(self, other):
        return isinstance(other, BadKey) and self.v == other.v
    def __hash__(self):
        return hash(self.v)


def mutated_key() -> None:
    k = BadKey(1)
    d = {k: "found me"}
    k.v = 2                                         # hash changes while inside the dict
    print("lookup with same object:", d.get(k, "LOST"))
    print("lookup with old value  :", d.get(BadKey(1), "LOST"))


def dict_growth() -> None:
    d = {}
    last = sys.getsizeof(d)
    print(" len  bytes")
    for i in range(50):
        d[i] = i
        size = sys.getsizeof(d)
        if size != last:
            print(f"{len(d):>4}  {size}")
            last = size


if __name__ == "__main__":
    for f in (hashable_or_not, equal_means_same_key, small_int_hash_and_strings, mutated_key, dict_growth):
        print(f"--- {f.__name__}")
        f()
