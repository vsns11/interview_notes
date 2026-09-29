"""Run mypy programmatically on bad_types.py and print its report."""
from pathlib import Path

from mypy import api

if __name__ == "__main__":
    target = Path(__file__).with_name("bad_types.py")
    stdout, stderr, status = api.run([str(target), "--strict",
                                      "--no-error-summary", "--no-color-output"])
    for line in stdout.splitlines():
        print(line.split("code/")[-1])          # shorten the path
    print("exit status:", status)
