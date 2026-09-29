"""Context managers: class-based, @contextmanager, suppress, ExitStack."""
import contextlib
import io
import os
import tempfile
import time


class Timer:
    def __enter__(self):
        self.start = time.perf_counter()
        return self

    def __exit__(self, exc_type, exc, tb):
        self.elapsed = time.perf_counter() - self.start
        return False                         # propagate exceptions


class Suppress:
    """Our own version of contextlib.suppress: returning True swallows."""

    def __init__(self, *excs):
        self.excs = excs

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc, tb):
        return exc_type is not None and issubclass(exc_type, self.excs)


@contextlib.contextmanager
def cd(path):
    """Temporarily change directory; always restore it."""
    old = os.getcwd()
    os.chdir(path)
    try:
        yield path                           # value for `as`; body of `with` runs here
    finally:
        os.chdir(old)                        # runs even if the body raises


@contextlib.contextmanager
def transaction(log):
    log.append("BEGIN")
    try:
        yield
    except Exception:
        log.append("ROLLBACK")
        raise
    else:
        log.append("COMMIT")


class Resource:
    def __init__(self, name, log):
        self.name, self.log = name, log

    def __enter__(self):
        self.log.append(f"open {self.name}")
        return self

    def __exit__(self, *exc):
        self.log.append(f"close {self.name}")
        return False


if __name__ == "__main__":
    with Timer() as t:
        sum(range(100_000))
    print(f"Timer: elapsed > 0: {t.elapsed > 0}")

    with Suppress(ZeroDivisionError):
        1 / 0
    print("Suppress: survived ZeroDivisionError")
    with contextlib.suppress(FileNotFoundError):
        os.remove("/definitely/not/here.txt")
    print("contextlib.suppress: survived FileNotFoundError")

    tmp = tempfile.mkdtemp()
    before = os.getcwd()
    with cd(tmp):
        inside = os.getcwd()
    print("cd restored:", os.getcwd() == before, "| was inside tmp:", os.path.realpath(inside) == os.path.realpath(tmp))

    log = []
    with transaction(log):
        log.append("insert row")
    try:
        with transaction(log):
            log.append("bad insert")
            raise ValueError("constraint")
    except ValueError:
        pass
    print("transaction log:", log)

    # ExitStack: a dynamic number of context managers, closed in reverse order
    log = []
    names = ["a.txt", "b.txt", "c.txt"]
    with contextlib.ExitStack() as stack:
        files = [stack.enter_context(Resource(n, log)) for n in names]
        stack.callback(log.append, "callback ran")
        log.append(f"using {len(files)} resources")
    print("ExitStack:", log)

    # a failing __enter__ in the middle: already-opened ones are still closed
    log = []

    class Boom(Resource):
        def __enter__(self):
            raise OSError("cannot open")

    try:
        with contextlib.ExitStack() as stack:
            stack.enter_context(Resource("ok1", log))
            stack.enter_context(Boom("bad", log))
    except OSError as e:
        print("ExitStack partial failure:", log, "|", e)

    buf = io.StringIO()
    with contextlib.redirect_stdout(buf):
        print("captured!")
    print("redirect_stdout captured:", repr(buf.getvalue()))

    with contextlib.nullcontext("x") as v:
        print("nullcontext gives:", v)

    # multiple managers in one with-statement (parenthesised form, 3.10+)
    log = []
    with (Resource("r1", log) as r1, Resource("r2", log) as r2):
        pass
    print("two managers:", log)
