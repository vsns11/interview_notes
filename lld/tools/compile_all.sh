#!/usr/bin/env bash
# Compile (Java 21) and run every chapter's code/ folder. Run from lld/:  bash tools/compile_all.sh
set -uo pipefail
cd "$(dirname "$0")/.."
fail=0
for d in chapters/*/code; do
  files=$(find "$d" -name '*.java')
  [ -z "$files" ] && continue
  out=$(mktemp -d)
  if javac --release 21 -Xlint:none -d "$out" $files 2>/tmp/javac.err; then
    mains=$(grep -rl 'static void main' "$d" --include=*.java)
    for m in $mains; do
      cls=$(grep -m1 '^package ' "$m" | sed 's/package \(.*\);/\1/').$(basename "$m" .java)
      if perl -e 'alarm 20; exec @ARGV' java -cp "$out" "$cls" >/dev/null 2>/tmp/java.err; then echo "OK   $cls"; else echo "FAIL $cls"; head -5 /tmp/java.err; fail=1; fi
    done
  else echo "COMPILE FAIL $d"; head -20 /tmp/javac.err; fail=1; fi
  rm -rf "$out"
done
exit $fail
