"""Aliasing, shallow vs deep copies, and the [[0]*3]*3 trap."""
import copy


def aliasing() -> None:
    a = [1, 2, 3]
    b = a                       # NOT a copy: two names, one list
    b.append(4)
    print(a, b, a is b)

    def add_item(lst):          # functions receive a reference too
        lst.append("x")
    add_item(a)
    print(a)


def shallow_vs_deep() -> None:
    original = [[1, 2], [3, 4]]
    shallow = original.copy()               # same as list(original), original[:], copy.copy(original)
    deep = copy.deepcopy(original)
    original[0].append(99)                  # mutate an INNER list
    original.append([5])                    # mutate the OUTER list
    print("original:", original)
    print("shallow :", shallow, "<- shares inner lists")
    print("deep    :", deep, "<- fully independent")
    print(shallow[0] is original[0], deep[0] is original[0])


def grid_trap() -> None:
    bad = [[0] * 3] * 3                     # 3 references to ONE row
    bad[0][0] = 1
    print("bad :", bad, "ids:", len({id(r) for r in bad}), "distinct row(s)")
    good = [[0] * 3 for _ in range(3)]      # 3 separate rows
    good[0][0] = 1
    print("good:", good, "ids:", len({id(r) for r in good}), "distinct row(s)")


def mutable_default() -> None:
    def bad_append(item, bucket=[]):         # default evaluated ONCE at def time
        bucket.append(item)
        return bucket

    def good_append(item, bucket=None):
        if bucket is None:
            bucket = []
        bucket.append(item)
        return bucket
    print(bad_append(1), bad_append(2))
    print(good_append(1), good_append(2))


if __name__ == "__main__":
    for f in (aliasing, shallow_vs_deep, grid_trap, mutable_default):
        print(f"--- {f.__name__}")
        f()
