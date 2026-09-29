"""Chapter 8 - reproduce the classic import errors in a temp folder, then fix them."""
import re
import subprocess
import sys
import tempfile
import textwrap
from pathlib import Path


def run_case(title, files, main="main.py"):
    with tempfile.TemporaryDirectory() as tmp:
        root = Path(tmp)
        for rel, body in files.items():
            p = root / rel
            p.parent.mkdir(parents=True, exist_ok=True)
            p.write_text(textwrap.dedent(body).lstrip())
        r = subprocess.run([sys.executable, main], cwd=root, capture_output=True, text=True)
        result = r.stdout.strip() if r.returncode == 0 else r.stderr.strip().splitlines()[-1]
        result = re.sub(r"/[^\s']*/", ".../", result)  # hide machine paths
        print(f"{title:<34} -> {result}")


if __name__ == "__main__":
    run_case("1. missing module", {"main.py": "import reqests\n"})

    run_case("2. name not in module", {"main.py": "from math import square_root\n"})

    run_case("3. shadowing the stdlib", {
        "random.py": "print('oops, my own random.py')\n",
        "main.py": "import random\nprint(random.randint(1, 6))\n",
    })

    circular = {
        "orders.py": """
            from customers import get_customer
            def get_order(i):
                return {"id": i, "customer": get_customer(1)}
        """,
        "customers.py": """
            from orders import get_order
            def get_customer(i):
                return {"id": i}
            def latest_order_for(i):
                return get_order(99)
        """,
        "main.py": "import orders\nprint(orders.get_order(7))\n",
    }
    run_case("4. circular import (from-import)", circular)

    fixed = dict(circular)
    fixed["customers.py"] = """
        import orders                      # import the MODULE, not the name
        def get_customer(i):
            return {"id": i}
        def latest_order_for(i):
            return orders.get_order(99)    # attribute looked up at CALL time
    """
    run_case("4b. fixed: import module, use attr", fixed)

    fixed2 = dict(circular)
    fixed2["customers.py"] = """
        def get_customer(i):
            return {"id": i}
        def latest_order_for(i):
            from orders import get_order   # deferred (function-level) import
            return get_order(99)
    """
    run_case("4c. fixed: import inside function", fixed2)
