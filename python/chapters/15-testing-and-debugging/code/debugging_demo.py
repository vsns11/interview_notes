"""Reading tracebacks, exception chaining, a scripted pdb session, and
post-mortem debugging - all non-interactive so the output can be shown."""
import io
import os
import pdb
import sys
import traceback


def parse_order(line: str) -> dict:
    sku, qty, price = line.split(",")
    return {"sku": sku, "qty": int(qty), "price": float(price)}


def load_orders(lines: list[str]) -> list[dict]:
    orders = []
    for n, line in enumerate(lines, start=1):
        try:
            orders.append(parse_order(line))
        except ValueError as e:
            raise RuntimeError(f"bad order on line {n}: {line!r}") from e
    return orders


def average_value(orders: list[dict]) -> float:
    total = 0.0
    for o in orders:
        total += o["qty"] * o["price"]
    return total / len(orders)          # bug when orders is empty


if __name__ == "__main__":
    # 1) A chained traceback: read it bottom-up, then follow "direct cause".
    try:
        load_orders(["mug,2,4.50", "pen,two,1.20"])
    except RuntimeError:
        tb = traceback.format_exc()
        print(tb.replace(os.path.dirname(os.path.abspath(__file__)) + os.sep, ""))

    # 2) A scripted pdb session (normally you type these commands yourself).
    commands = "\n".join([
        "b average_value",     # set a breakpoint on the function
        "c",                   # continue until it is hit
        "p orders",            # print a variable
        "n",                   # next line
        "n",
        "p total, len(orders)",
        "c",
    ]) + "\n"
    out = io.StringIO()
    debugger = pdb.Pdb(stdin=io.StringIO(commands), stdout=out, nosigint=True)
    debugger.prompt = "(Pdb) "
    try:
        debugger.runcall(lambda: average_value([]))
    except ZeroDivisionError as e:
        print("pdb transcript:")
        for ln in out.getvalue().splitlines():
            if "bdb.py" not in ln:
                print("   ", ln.replace(os.path.dirname(os.path.abspath(__file__)) + os.sep, ""))
        print("then it raised:", repr(e))

    # 3) Post-mortem: inspect the frame where an exception happened.
    try:
        average_value([])
    except ZeroDivisionError:
        tb = sys.exc_info()[2]
        while tb.tb_next:              # walk to the innermost frame
            tb = tb.tb_next
        print("post-mortem locals:", {k: v for k, v in tb.tb_frame.f_locals.items()})

    # 4) breakpoint() obeys PYTHONBREAKPOINT; "0" disables it (e.g. in CI).
    os.environ["PYTHONBREAKPOINT"] = "0"
    breakpoint()                       # does nothing now
    print("breakpoint() skipped because PYTHONBREAKPOINT=0")
