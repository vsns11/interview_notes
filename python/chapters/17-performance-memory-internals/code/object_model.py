"""CPython object model: identity, type, value, reference counts, sizes, small-int cache, interning."""
import sys


def show_refcounts() -> None:
    data = [1, 2, 3]
    print(f"refcount of new list: {sys.getrefcount(data)}  (exact numbers are an implementation detail)")
    alias = data
    holder = {"k": data}
    print(f"after alias + dict entry: {sys.getrefcount(data)}")
    del alias, holder
    print(f"after del: {sys.getrefcount(data)}")
    print(f"refcount of None: {sys.getrefcount(None)}  (immortal object, 3.12+)")


def show_identity() -> None:
    a = [1, 2]
    b = a
    c = [1, 2]
    print(f"a is b: {a is b}, a == c: {a == c}, a is c: {a is c}")
    print(f"type(a): {type(a).__name__}, type(type(a)): {type(type(a)).__name__}, "
          f"isinstance(len, object): {isinstance(len, object)}")
    x = 10
    before = id(x)
    x += 1                                   # ints are immutable: a NEW object is bound
    print(f"int += changes id: {before != id(x)}")
    lst = [1]
    before = id(lst)
    lst += [2]                               # lists are mutable: same object, extended in place
    print(f"list += keeps id: {before == id(lst)}")


def show_sizes() -> None:
    items = [("int 0", 0), ("int 1", 1), ("int 2**30", 2**30), ("int 2**100", 2**100),
             ("float", 1.0), ("str ''", ""), ("str 'abc'", "abc"), ("str 'é'", "é"),
             ("tuple ()", ()), ("list []", []), ("list 3 items", [1, 2, 3]),
             ("dict {}", {}), ("set()", set()), ("None", None)]
    for label, obj in items:
        print(f"  getsizeof({label:12}) = {sys.getsizeof(obj):4} bytes")


def show_caching() -> None:
    a, b = int("256"), int("256")            # built at run time, not constants
    c, d = int("257"), int("257")
    print(f"256 is 256: {a is b}   257 is 257: {c is d}   (small ints -5..256 are cached)")
    s1 = "".join(["hello", "_", "world"])     # built at run time: not interned automatically
    s2 = "hello_world"
    print(f"runtime str is literal: {s1 is s2}, after sys.intern: {sys.intern(s1) is s2}")


def show_copying() -> None:
    import copy
    grid = [[0, 0], [0, 0]]
    alias = grid                              # same object
    shallow = grid.copy()                     # new outer list, SAME inner lists
    deep = copy.deepcopy(grid)                # new everything
    grid[0][0] = 99
    print(f"alias {alias[0]}, shallow {shallow[0]}, deep {deep[0]}")
    bad = [[0] * 2] * 2                       # two references to ONE inner list
    bad[0][0] = 1
    print(f"[[0]*2]*2 after bad[0][0]=1 -> {bad}")

    def append_to(item, bucket=[]):           # default evaluated once, shared across calls
        bucket.append(item)
        return bucket
    append_to(1)
    print(f"mutable default after two calls: {append_to(2)}")


if __name__ == "__main__":
    show_refcounts()
    show_identity()
    show_sizes()
    show_caching()
    show_copying()
