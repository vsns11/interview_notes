"""Chapter 1 - watch CPython turn source text into bytecode and run it."""

import dis
import importlib.util
import pathlib
import py_compile
import sys
import tempfile

SOURCE = """\
price = 20
qty = 3
total = price * qty
print("total =", total)
"""


def main() -> None:
    print("implementation:", sys.implementation.name, sys.version.split()[0])

    # Step 1+2: tokenize + parse + compile -> a code object (the whole module at once)
    code = compile(SOURCE, "<demo>", "exec")
    print("code object:", type(code).__name__, "| constants:", code.co_consts, "| names:", code.co_names)

    # Step 3: look at the bytecode the virtual machine will execute
    print("--- bytecode ---")
    dis.dis(code)

    # Step 4: the VM executes the code object
    print("--- run ---")
    exec(code, {})

    # A syntax error anywhere stops the WHOLE file before any line runs
    broken = 'print("line 1 runs?")\nif True print("oops")\n'
    try:
        compile(broken, "<broken>", "exec")
    except SyntaxError as err:
        print("SyntaxError at line", err.lineno, "- so line 1 never ran")

    # Imported modules are cached as .pyc files in __pycache__
    with tempfile.TemporaryDirectory() as tmp:
        src = pathlib.Path(tmp, "shop.py")
        src.write_text(SOURCE)
        pyc = py_compile.compile(str(src))
        print("cached bytecode file:", pathlib.Path(pyc).relative_to(tmp))
        print("cache tag for this interpreter:", sys.implementation.cache_tag)
        print("magic number (bytecode version) bytes:", importlib.util.MAGIC_NUMBER.hex())


if __name__ == "__main__":
    main()
