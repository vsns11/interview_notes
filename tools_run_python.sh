#!/usr/bin/env bash
# Run every Python example of a book: bash tools_run_python.sh <book-dir>   (uses ../.venv-books)
set -uo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"            # absolute, so it still resolves after cd into code/
cd "$ROOT/$1" || exit 1
PY="$ROOT/.venv-books/bin/python"
[ -x "$PY" ] || { echo "missing interpreter: $PY"; exit 1; }
fail=0
for f in $(find chapters -path '*/code/*' -name '*.py' | sort); do
  grep -q '__main__' "$f" || continue
  grep -q 'tools_run_python.sh: skip' "$f" && { echo "SKIP $f"; continue; }
  if (cd "$(dirname "$f")" && perl -e 'alarm 120; exec @ARGV or die "exec failed: $!\n"' "$PY" "$(basename "$f")" >/dev/null 2>/tmp/py.err); then echo "OK   $f"; else echo "FAIL $f"; head -5 /tmp/py.err; fail=1; fi
done
exit $fail
