"""Chapter 9 - csv, json and pickle (with the pickle security caveat)."""
import csv
import io
import json
import pickle
import tempfile
from dataclasses import asdict, dataclass
from datetime import date
from decimal import Decimal
from pathlib import Path


@dataclass
class Order:
    id: int
    customer: str
    total: float
    placed: date


def to_json_default(obj):
    """Teach json how to encode types it doesn't know."""
    if isinstance(obj, date):
        return obj.isoformat()
    if isinstance(obj, Decimal):
        return str(obj)
    raise TypeError(f"{type(obj).__name__} is not JSON serializable")


class Sneaky:
    """Shows why unpickling untrusted data is dangerous: __reduce__ tells pickle
    to CALL a function on load. Here it's a harmless print - it could be os.system."""

    def __reduce__(self):
        return (print, ("  !!! code ran during pickle.loads() !!!",))


if __name__ == "__main__":
    orders = [Order(1, "Ana, Inc.", 19.5, date(2026, 9, 1)),
              Order(2, 'Bob "B" Ltd', 5.25, date(2026, 9, 2))]

    with tempfile.TemporaryDirectory() as tmp:
        base = Path(tmp)

        print("== csv: write with DictWriter, read with DictReader ==")
        path = base / "orders.csv"
        with open(path, "w", newline="", encoding="utf-8") as f:   # newline="" per csv docs
            w = csv.DictWriter(f, fieldnames=["id", "customer", "total", "placed"])
            w.writeheader()
            for o in orders:
                w.writerow(asdict(o))
        print("  raw file:")
        for line in path.read_text(encoding="utf-8").splitlines():
            print("   ", line)
        with open(path, newline="", encoding="utf-8") as f:
            rows = list(csv.DictReader(f))
        print("  first row:", rows[0])
        print("  total is a", type(rows[0]["total"]).__name__, "-> convert yourself:",
              sum(float(r["total"]) for r in rows))
        print("  naive split(',') breaks on quoted commas:",
              path.read_text(encoding="utf-8").splitlines()[1].split(","))

        print("\n== csv.reader on other dialects ==")
        tsv = io.StringIO("a\tb\n1\t2\n")
        print("  tab-separated:", list(csv.reader(tsv, delimiter="\t")))

        print("\n== json: dumps/loads and dump/load ==")
        payload = {"orders": [asdict(o) for o in orders], "paid": True,
                   "discount": None, "tax": Decimal("0.08")}
        text = json.dumps(payload, default=to_json_default, indent=2, ensure_ascii=False)
        print("  first lines of dumps():")
        for line in text.splitlines()[:8]:
            print("   ", line)
        back = json.loads(text)
        print("  loads() types: paid ->", type(back["paid"]).__name__,
              "| discount ->", back["discount"],
              "| placed ->", type(back["orders"][0]["placed"]).__name__)
        jpath = base / "orders.json"
        with open(jpath, "w", encoding="utf-8") as f:
            json.dump(payload, f, default=to_json_default)
        with open(jpath, encoding="utf-8") as f:
            print("  load() from file, customers:", [o["customer"] for o in json.load(f)["orders"]])
        print("  tuple becomes list:", json.loads(json.dumps({"pt": (1, 2)})))
        print("  int keys become str:", json.loads(json.dumps({1: "a"})))
        try:
            json.dumps({"when": date(2026, 1, 1)})
        except TypeError as exc:
            print("  without default=:", exc)
        try:
            json.loads("{'single': 'quotes'}")
        except json.JSONDecodeError as exc:
            print("  JSONDecodeError:", exc.msg, "at pos", exc.pos)

        print("\n== pickle: any Python object, Python-only, UNSAFE on untrusted input ==")
        blob = pickle.dumps(orders)
        restored = pickle.loads(blob)
        print("  round trip equal:", restored == orders, "| type:", type(restored[0]).__name__,
              "| placed:", restored[0].placed)
        with open(base / "orders.pkl", "wb") as f:                 # binary mode!
            pickle.dump(orders, f)
        with open(base / "orders.pkl", "rb") as f:
            print("  load() from file:", len(pickle.load(f)), "orders")
        evil = pickle.dumps(Sneaky())
        print("  loading a crafted pickle...")
        result = pickle.loads(evil)
        print("  loads() returned:", result)
