#!/usr/bin/env bash
# Compile (javac --release 25) and run every class with a main method under chapters/*/code of the Java book.
# Files containing "tools_run: skip" are compiled but not run. Each code/ dir compiles as one unit.
# Jars in ROOT/.java-books/lib/ (JUnit, Mockito, AssertJ, JDBC driver) are on the compile and run class path.
set -uo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"; cd "$ROOT/${1:-java}" || exit 1
fail=0; OUT=$(mktemp -d); LIB="$ROOT/.java-books/lib/*"
for d in chapters/*/code; do
  [ -d "$d" ] || continue
  files=$(find "$d" -name '*.java'); [ -n "$files" ] || continue
  o="$OUT/$(basename "$(dirname "$d")")"; mkdir -p "$o"
  if ! javac --release 25 -cp "$LIB" -d "$o" $files 2>/tmp/jc.err; then echo "FAIL compile $d"; head -8 /tmp/jc.err; fail=1; continue; fi
  for f in $files; do
    grep -qE 'static void main\s*\(' "$f" || continue
    grep -q 'tools_run: skip' "$f" && { echo "SKIP $f"; continue; }
    pkg=$(sed -n 's/^package \([a-zA-Z0-9_.]*\);.*/\1/p' "$f" | head -1); cls=$(basename "$f" .java); fq=${pkg:+$pkg.}$cls
    if (cd "$(dirname "$f")" && perl -e 'alarm 120; exec @ARGV' java -cp "$o:$LIB" "$fq" >/dev/null 2>/tmp/jr.err); then echo "OK   $f"; else echo "FAIL $f"; head -5 /tmp/jr.err; fail=1; fi
  done
done
rm -rf "$OUT"; exit $fail
