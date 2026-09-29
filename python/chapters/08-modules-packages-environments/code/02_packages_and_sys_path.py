"""Chapter 8 - build a tiny package on disk, then import it several ways.

Everything happens in a temporary directory that is deleted at the end.
"""
import subprocess
import sys
import tempfile
import textwrap
from pathlib import Path

FILES = {
    "shop/__init__.py": '''
        """shop - a tiny demo package."""
        from .pricing import price_with_tax      # re-export the public API
        __all__ = ["price_with_tax"]
        __version__ = "0.1.0"
        print(f"  [running shop/__init__.py, __name__={__name__!r}]")
    ''',
    "shop/utils.py": '''
        def round_money(x):
            return round(x + 1e-9, 2)
    ''',
    "shop/pricing.py": '''
        from .utils import round_money           # relative import (same package)
        from shop.utils import round_money as rm # absolute import (same thing)

        TAX = 0.08

        def price_with_tax(amount):
            return round_money(amount * (1 + TAX))

        if __name__ == "__main__":
            print("  pricing run directly: 10 ->", price_with_tax(10))
    ''',
    "shop/__main__.py": '''
        from shop import price_with_tax
        print("  python -m shop says: 19.99 ->", price_with_tax(19.99))
    ''',
    "plugins/extra.py": '''
        NAME = "extra plugin (namespace package, no __init__.py)"
    ''',
}


def build(root: Path) -> None:
    for rel, body in FILES.items():
        p = root / rel
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(textwrap.dedent(body).lstrip())


def run(args, cwd):
    r = subprocess.run([sys.executable, *args], cwd=cwd, capture_output=True, text=True)
    if r.returncode:
        return r.stderr.strip().splitlines()[-1]
    lines = ["  " + ln.strip() for ln in r.stdout.strip().splitlines()]
    lines += ["  stderr: " + ln.split(": ", 1)[-1] for ln in r.stderr.strip().splitlines()]
    return "\n".join(lines)


if __name__ == "__main__":
    with tempfile.TemporaryDirectory() as tmp:
        root = Path(tmp)
        build(root)
        print("== package layout ==")
        for p in sorted(root.rglob("*.py")):
            print("  ", p.relative_to(root))

        print("\n== import it: put its parent folder on sys.path ==")
        sys.path.insert(0, str(root))
        import shop                          # runs shop/__init__.py once
        import shop                          # cached: __init__ does NOT run again
        from shop import price_with_tax
        print("  price_with_tax(100) =", price_with_tax(100))
        print("  shop.__version__    =", shop.__version__)
        print("  shop.__path__ is a list of folders:", [Path(p).name for p in shop.__path__])
        import shop.pricing as pricing
        print("  pricing.__name__    =", pricing.__name__, "| __package__ =", pricing.__package__)

        import plugins.extra
        print("  namespace package:", plugins.extra.NAME)
        print("  plugins has __file__?", getattr(plugins, "__file__", None))

        print("\n== star import only takes names in __all__ ==")
        ns = {}
        exec("from shop import *", ns)
        print("  names brought in:", sorted(k for k in ns if not k.startswith("__")))

        print("\n== running modules from the command line ==")
        print("python shop/pricing.py  ->", run(["shop/pricing.py"], root))
        print("python -m shop.pricing  ->\n" + run(["-m", "shop.pricing"], root))
        print("python -m shop          ->\n" + run(["-m", "shop"], root))
        sys.path.remove(str(root))

    print("\n== where Python searches (first entries of sys.path) ==")
    print("  sys.path[0] is the script's folder:", Path(sys.path[0]).name)
