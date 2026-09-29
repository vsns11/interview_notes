#!/usr/bin/env bash
# Run every chapters/*/code/*.{js,mjs,jsx,tsx,ts} of a web book (js | react | html-css) with Node.
# .jsx/.tsx/.ts files are bundled with esbuild first (React available from .web-books). Files with
# "tools_run: skip" (e.g. browser-only scripts, modules imported by others) are skipped.
set -uo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"; cd "$ROOT/$1" || exit 1
W="$ROOT/.web-books"; export NODE_PATH="$W/node_modules"; fail=0; T=$(mktemp -d)
for f in $(find chapters -path '*/code/*' \( -name '*.js' -o -name '*.mjs' -o -name '*.jsx' -o -name '*.ts' -o -name '*.tsx' \) -not -path '*/node_modules/*' | sort); do
  grep -q 'tools_run: skip' "$f" && { echo "SKIP $f"; continue; }
  run="$f"
  case "$f" in *.jsx|*.ts|*.tsx)
    run="$(dirname "$f")/.__run_$(basename "$f").mjs"
    "$W/node_modules/.bin/esbuild" "$f" --bundle --platform=node --format=esm --jsx=automatic --log-level=error \
      --packages=external --external:*tools_dom.mjs --outfile="$run" --resolve-extensions=.tsx,.ts,.jsx,.js,.mjs 2>/tmp/js.err || { echo "FAIL build $f"; head -5 /tmp/js.err; fail=1; continue; };;
  esac
  if (cd "$(dirname "$f")" && perl -e 'alarm 120; exec @ARGV' node "$(basename "$run")" >/dev/null 2>/tmp/js.err); then echo "OK   $f"; else echo "FAIL $f"; head -5 /tmp/js.err; fail=1; fi
  [ "$run" != "$f" ] && rm -f "$run"
done
rm -rf "$T"; exit $fail
