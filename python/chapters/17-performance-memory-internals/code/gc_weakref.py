"""Reference counting vs the cyclic garbage collector, weak references and finalizers."""
import gc
import weakref


class Node:
    def __init__(self, name: str):
        self.name = name
        self.other = None

    def __repr__(self) -> str:
        return f"Node({self.name})"


def refcount_frees_immediately() -> None:
    n = Node("solo")
    weakref.finalize(n, print, "  solo freed as soon as last reference went away")
    del n                                     # refcount -> 0 -> freed right here
    print("  (after del solo)")


def cycle_needs_gc() -> None:
    gc.collect()                              # start from a clean slate
    gc.disable()                              # so we can watch it happen
    a, b = Node("a"), Node("b")
    a.other, b.other = b, a                   # reference cycle
    weakref.finalize(a, print, "  cycle freed by gc.collect()")
    del a, b                                  # refcounts stay at 1 -> not freed yet
    print("  (after del a, b: cycle still alive)")
    found = gc.collect()
    print(f"  gc.collect() found {found} unreachable objects")
    gc.enable()


def weak_references() -> None:
    class Big:
        pass
    obj = Big()
    r = weakref.ref(obj)                      # does NOT keep obj alive
    print(f"  weakref alive: {r() is obj}")
    del obj
    print(f"  weakref after del: {r()}")

    cache = weakref.WeakValueDictionary()     # entries vanish when the value dies
    x = Big()
    cache["x"] = x
    print(f"  cache keys while x alive: {list(cache)}")
    del x
    print(f"  cache keys after del x: {list(cache)}")


if __name__ == "__main__":
    print(f"gc thresholds: {gc.get_threshold()}, gc enabled: {gc.isenabled()}")
    refcount_frees_immediately()
    cycle_needs_gc()
    weak_references()
