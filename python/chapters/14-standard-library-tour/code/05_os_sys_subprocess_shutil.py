"""os, sys, subprocess, shutil and pathlib working together (in a temp dir)."""
import os
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

if __name__ == "__main__":
    print("python      :", sys.version_info[:2], "| platform:", sys.platform)
    print("argv[0]     :", Path(sys.argv[0]).name)
    print("HOME set    :", "HOME" in os.environ,
          "| APP_ENV =", os.environ.get("APP_ENV", "dev (default)"))
    print("cpu_count>0 :", (os.cpu_count() or 0) > 0)

    with tempfile.TemporaryDirectory() as tmp:
        root = Path(tmp)
        (root / "src" / "pkg").mkdir(parents=True)
        (root / "src" / "pkg" / "a.py").write_text("print('hi')\n")
        (root / "src" / "notes.txt").write_text("x")
        shutil.copytree(root / "src", root / "backup")
        print("copied      :", sorted(p.relative_to(root).as_posix()
                                      for p in (root / "backup").rglob("*")))
        print("py files    :", [p.name for p in root.rglob("*.py")])
        archive = shutil.make_archive(str(root / "bundle"), "zip", root / "src")
        print("archive     :", Path(archive).name, Path(archive).stat().st_size > 0)
        shutil.rmtree(root / "backup")
        print("after rmtree:", sorted(p.name for p in root.iterdir()))

        # subprocess: run a child process safely (list args, no shell)
        r = subprocess.run([sys.executable, str(root / "src" / "pkg" / "a.py")],
                           capture_output=True, text=True, check=True, timeout=10)
        print("child stdout:", r.stdout.strip(), "| returncode", r.returncode)
        try:
            subprocess.run([sys.executable, "-c", "import sys; sys.exit(3)"],
                           check=True)
        except subprocess.CalledProcessError as e:
            print("failed child:", "returncode", e.returncode)

    print("which python:", shutil.which("python3") is not None)
    print("disk free GB:", shutil.disk_usage("/").free // 2**30 > 0)
