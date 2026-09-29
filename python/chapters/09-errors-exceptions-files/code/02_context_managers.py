"""Chapter 9 - context managers: with, __enter__/__exit__, contextlib."""
import contextlib
import time


class Timer:
    """Class-based context manager."""

    def __enter__(self):
        self.start = time.perf_counter()
        print("  __enter__: timer started")
        return self                       # bound to the name after 'as'

    def __exit__(self, exc_type, exc, tb):
        self.elapsed = time.perf_counter() - self.start
        print(f"  __exit__: exc_type={exc_type.__name__ if exc_type else None}")
        return False                      # False/None -> let exceptions propagate


@contextlib.contextmanager
def transaction(log):
    """Generator-based context manager: code before yield = enter, after = exit."""
    log.append("BEGIN")
    try:
        yield log
    except Exception:
        log.append("ROLLBACK")
        raise
    else:
        log.append("COMMIT")


class Resource:
    def __init__(self, name):
        self.name = name
        print(f"  open {name}")

    def close(self):
        print(f"  close {self.name}")


if __name__ == "__main__":
    print("== class-based: normal exit ==")
    with Timer() as t:
        sum(range(10_000))
    print("  elapsed measured:", t.elapsed > 0)

    print("\n== class-based: exception inside the block ==")
    try:
        with Timer():
            raise KeyError("boom")
    except KeyError as exc:
        print("  exception still reached caller:", repr(exc))

    print("\n== @contextmanager ==")
    log = []
    with transaction(log):
        log.append("UPDATE accounts ...")
    try:
        with transaction(log):
            log.append("UPDATE bad ...")
            raise ValueError("constraint failed")
    except ValueError:
        pass
    print("  log:", log)

    print("\n== contextlib.suppress ==")
    with contextlib.suppress(FileNotFoundError):
        open("/definitely/not/here.txt")
    print("  missing file ignored")

    print("\n== contextlib.closing + ExitStack (cleanup in reverse order) ==")
    with contextlib.ExitStack() as stack:
        for name in ("db", "cache", "queue"):
            stack.enter_context(contextlib.closing(Resource(name)))
        print("  ...working with all three...")

    print("\n== several managers in one with (3.10+ parentheses) ==")
    with (
        Timer() as outer,
        contextlib.suppress(ZeroDivisionError),
    ):
        1 / 0
    print("  suppressed ZeroDivisionError; outer timer exited normally")
