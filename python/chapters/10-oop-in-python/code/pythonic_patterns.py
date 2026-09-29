"""Pythonic takes on classic patterns: Strategy with functions, Singleton via module."""
from typing import Callable
import functools

# ---------- Strategy: in Python a strategy is usually just a function
Discount = Callable[[float], float]


def no_discount(total: float) -> float:
    return total


def ten_percent(total: float) -> float:
    return total * 0.9


def bulk(total: float) -> float:
    return total - 20 if total > 200 else total


def checkout(total: float, strategy: Discount = no_discount) -> float:
    return round(strategy(total), 2)


# ---------- Singleton: a module is imported once and cached in sys.modules,
# so module-level objects are already "single instances". If you need a lazily
# created shared object, a cached factory function is the simplest option.
class _Settings:
    def __init__(self) -> None:
        self.debug = False


@functools.cache
def get_settings() -> _Settings:
    return _Settings()


if __name__ == "__main__":
    for s in (no_discount, ten_percent, bulk, lambda t: t * 0.5):
        print(f"{getattr(s, '__name__', '?'):12} -> {checkout(250, s)}")

    get_settings().debug = True
    print("same object:", get_settings() is get_settings(), "| debug:", get_settings().debug)

    import json, sys
    import json as again
    print("module cached once:", json is again, "| in sys.modules:", "json" in sys.modules)
