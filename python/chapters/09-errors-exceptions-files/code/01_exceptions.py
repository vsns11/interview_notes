"""Chapter 9 - exceptions: hierarchy, try/except/else/finally, raise, custom, chaining,
EAFP vs LBYL, ExceptionGroup / except*, assert, add_note."""
import traceback


def show_tree(cls, depth=0, max_depth=2, only=None):
    print("  " + "    " * depth + cls.__name__)
    if depth < max_depth:
        for sub in cls.__subclasses__():
            if only is None or sub.__name__ in only:
                show_tree(sub, depth + 1, max_depth, only)


def parse_age(text):
    try:
        age = int(text)
    except ValueError:
        print(f"  except: {text!r} is not a number")
        return None
    else:
        print(f"  else:   parsed {age}")
        return age
    finally:
        print("  finally: always runs")


# ---- custom exceptions -------------------------------------------------------
class PaymentError(Exception):
    """Base class for this module's errors."""


class CardDeclined(PaymentError):
    def __init__(self, card_last4, reason):
        super().__init__(f"card ending {card_last4} declined: {reason}")
        self.card_last4 = card_last4
        self.reason = reason


class InsufficientFunds(PaymentError):
    pass


def charge(balance, amount):
    if amount <= 0:
        raise ValueError(f"amount must be positive, got {amount}")
    if amount > balance:
        raise InsufficientFunds(f"need {amount}, have {balance}")
    return balance - amount


# ---- chaining ------------------------------------------------------------------
class ConfigError(Exception):
    pass


def load_port(cfg):
    try:
        return int(cfg["port"])
    except (KeyError, ValueError) as exc:
        raise ConfigError("invalid 'port' setting") from exc


def reraise_after_logging(x):
    try:
        return 1 / x
    except ZeroDivisionError:
        print("  logging the error, then re-raising")
        raise                           # bare raise keeps the original traceback


if __name__ == "__main__":
    print("== part of the built-in hierarchy ==")
    show_tree(BaseException, max_depth=1,
              only={"Exception", "KeyboardInterrupt", "SystemExit", "GeneratorExit",
                    "BaseExceptionGroup"})
    print("  ...under Exception (selection):")
    show_tree(LookupError, max_depth=1, only={"IndexError", "KeyError"})
    show_tree(ArithmeticError, max_depth=1)
    show_tree(OSError, max_depth=1, only={"FileNotFoundError", "PermissionError",
                                          "IsADirectoryError", "TimeoutError"})
    print("  issubclass(ModuleNotFoundError, ImportError):",
          issubclass(ModuleNotFoundError, ImportError))

    print("\n== try / except / else / finally ==")
    parse_age("42")
    parse_age("forty")

    print("\n== raising and catching custom exceptions ==")
    for bal, amt in [(100, 30), (100, 500), (100, -5)]:
        try:
            print(f"  charge({bal}, {amt}) -> {charge(bal, amt)}")
        except PaymentError as exc:          # catches any subclass
            print(f"  {type(exc).__name__}: {exc}")
        except ValueError as exc:
            print(f"  ValueError: {exc}")
    try:
        raise CardDeclined("4242", "expired")
    except PaymentError as exc:
        print(f"  caught {type(exc).__name__}, attrs: last4={exc.card_last4} reason={exc.reason}")

    print("\n== re-raising ==")
    try:
        reraise_after_logging(0)
    except ZeroDivisionError as exc:
        print("  caller still sees:", repr(exc))

    print("\n== exception chaining: raise ... from ... ==")
    for cfg in [{"port": "8080"}, {}, {"port": "eighty"}]:
        try:
            print("  port =", load_port(cfg))
        except ConfigError as exc:
            print(f"  ConfigError: {exc}; __cause__ = {exc.__cause__!r}")
    try:
        try:
            {}["missing"]
        except KeyError:
            raise RuntimeError("while handling")   # implicit chaining
    except RuntimeError as exc:
        print("  implicit: __context__ =", repr(exc.__context__))
    print("  formatted traceback of an explicit chain (last lines):")
    try:
        load_port({})
    except ConfigError:
        lines = traceback.format_exc().strip().splitlines()
        for ln in lines:
            if not ln.startswith(" "):
                print("    |", ln)

    print("\n== EAFP vs LBYL ==")
    stock = {"apple": 3}
    # LBYL: look before you leap
    qty = stock["pear"] if "pear" in stock else 0
    print("  LBYL:", qty)
    # EAFP: easier to ask forgiveness than permission
    try:
        qty = stock["pear"]
    except KeyError:
        qty = 0
    print("  EAFP:", qty, "| idiomatic: stock.get('pear', 0) =", stock.get("pear", 0))

    print("\n== add_note (3.11+) ==")
    try:
        try:
            int("x1")
        except ValueError as exc:
            exc.add_note("while reading row 7 of prices.csv")
            raise
    except ValueError as exc:
        print("  ", exc, "| notes:", exc.__notes__)

    print("\n== ExceptionGroup and except* (3.11+) ==")
    def run_all():
        errors = []
        for task, fn in [("a", lambda: 1 / 0), ("b", lambda: int("z")),
                         ("c", lambda: 42), ("d", lambda: [][1])]:
            try:
                fn()
            except Exception as exc:
                exc.add_note(f"task {task}")
                errors.append(exc)
        if errors:
            raise ExceptionGroup("some tasks failed", errors)

    try:
        run_all()
    except* ZeroDivisionError as eg:
        print("  math errors: ", [(repr(e), e.__notes__) for e in eg.exceptions])
    except* (ValueError, IndexError) as eg:
        print("  input errors:", [repr(e) for e in eg.exceptions])

    print("\n== assert ==")
    def mean(xs):
        assert len(xs) > 0, "mean() of empty list"
        return sum(xs) / len(xs)
    print("  mean([2, 4]) =", mean([2, 4]))
    try:
        mean([])
    except AssertionError as exc:
        print("  AssertionError:", exc, "(disabled entirely by python -O)")
