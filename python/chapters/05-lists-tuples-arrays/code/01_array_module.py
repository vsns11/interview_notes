"""The array module: compact, typed sequences of numbers."""
import array
import copy
import sys


def create_and_index() -> None:
    a = array.array("i", [10, 20, 30, 40, 50])      # 'i' = C signed int
    print(a, a.typecode, a.itemsize, "bytes per item")
    print(a[0], a[1], a[-1], a[-2])
    for x in a:
        print(x, end=" ")
    print()
    try:
        a.append(3.5)                                # wrong type is rejected
    except TypeError as e:
        print("TypeError:", e)
    try:
        array.array("b", [200])                      # 'b' holds -128..127
    except OverflowError as e:
        print("OverflowError:", e)


def operations() -> None:
    a = array.array("i", [1, 2, 3, 4, 5])
    a.insert(1, 10);            print("insert(1, 10):", a.tolist())
    removed = a.pop(1);         print("pop(1):", a.tolist(), "removed", removed)
    print("index(3):", a.index(3))
    a[2] = 20;                  print("a[2] = 20:", a.tolist())
    a.append(4);                print("append(4):", a.tolist())
    a.extend([5, 4, 6]);        print("extend:", a.tolist())
    print("count(4):", a.count(4))
    a.remove(4);                print("remove(4) (first 4 only):", a.tolist())
    a.reverse();                print("reverse:", a.tolist())
    print("sorted(a) -> list:", sorted(a))           # arrays have no .sort()
    a = array.array("i", sorted(a));  print("back to array:", a)
    b = copy.copy(a)                                  # no .copy() method: use copy.copy or a[:]
    b[0] = 99
    print("copy independent:", a[0], b[0])
    del a[:]                                          # empties it (array.clear() exists from 3.13)
    print("emptied:", a)


def memory() -> None:
    n = 1_000_000
    lst = list(range(n))
    arr = array.array("i", range(n))
    list_total = sys.getsizeof(lst) + sum(sys.getsizeof(x) for x in lst)
    print(f"list of {n:,} ints : {list_total / 1e6:6.1f} MB (pointers + int objects)")
    print(f"array('i')          : {sys.getsizeof(arr) / 1e6:6.1f} MB (raw 4-byte ints)")
    print("tobytes (little-endian here):", array.array("h", [1, 2]).tobytes())


if __name__ == "__main__":
    for f in (create_and_index, operations, memory):
        print(f"--- {f.__name__}")
        f()
