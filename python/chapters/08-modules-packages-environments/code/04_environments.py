"""Chapter 8 - virtual environments, installed-package metadata, pyproject.toml."""
import importlib.metadata as md
import subprocess
import sys
import tempfile
import tomllib
import venv
from pathlib import Path

PYPROJECT = """
[build-system]
requires = ["hatchling"]
build-backend = "hatchling.build"

[project]
name = "shop"
version = "0.1.0"
requires-python = ">=3.12"
dependencies = ["requests>=2.31,<3", "pydantic>=2"]

[project.optional-dependencies]
dev = ["pytest>=8", "ruff"]

[project.scripts]
shop = "shop.cli:main"
"""


def in_venv() -> bool:
    return sys.prefix != sys.base_prefix


if __name__ == "__main__":
    print("== which interpreter am I? ==")
    print("  Python", sys.version.split()[0])
    print("  running inside a venv:", in_venv())
    print("  sys.prefix folder name:", Path(sys.prefix).name)

    print("\n== installed distributions (importlib.metadata) ==")
    for dist in ("numpy", "pandas", "scikit-learn"):
        try:
            print(f"  {dist:<13}", md.version(dist))
        except md.PackageNotFoundError:
            print(f"  {dist:<13} not installed")
    print("  numpy requires python:", md.metadata("numpy")["Requires-Python"])

    print("\n== a fresh, empty venv is isolated ==")
    with tempfile.TemporaryDirectory() as tmp:
        env_dir = Path(tmp) / ".venv"
        venv.create(env_dir, with_pip=False)          # same as: python -m venv .venv --without-pip
        bindir = "Scripts" if sys.platform == "win32" else "bin"
        py = env_dir / bindir / ("python.exe" if sys.platform == "win32" else "python")
        print("  created:", sorted(p.name for p in env_dir.iterdir()))
        check = ("import sys; print('in venv:', sys.prefix != sys.base_prefix)\n"
                 "try:\n    import numpy\nexcept ModuleNotFoundError as e:\n    print(e)")
        out = subprocess.run([str(py), "-c", check], capture_output=True, text=True).stdout
        for line in out.strip().splitlines():
            print("  new venv ->", line)

    print("\n== reading pyproject.toml (tomllib, 3.11+) ==")
    cfg = tomllib.loads(PYPROJECT)
    proj = cfg["project"]
    print("  name/version :", proj["name"], proj["version"])
    print("  dependencies :", proj["dependencies"])
    print("  dev extras   :", proj["optional-dependencies"]["dev"])
    print("  console script 'shop' ->", proj["scripts"]["shop"])
    print("  build backend:", cfg["build-system"]["build-backend"])
