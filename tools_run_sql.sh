#!/usr/bin/env bash
# Run every chapters/*/code/*.sql of the SQL book in a FRESH database per chapter on the private server.
# Each chapter's code/00_schema.sql (if present) runs first. Files with "-- tools_run: skip" are skipped.
# A file named *.sqlite.sql runs in SQLite instead (in-memory).
# Optional 2nd arg: chapter glob, e.g.  bash tools_run_sql.sh sql '0[4-6]-*'   (only those chapters' DBs are reset)
set -uo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"; cd "$ROOT/${1:-sql}" || exit 1
bash "$ROOT/tools_pg.sh" start >/dev/null
P="psql -h /tmp -p 5544 -U postgres -X -q -v ON_ERROR_STOP=1"
fail=0
for d in chapters/${2:-*}/code; do
  [ -d "$d" ] || continue
  db="ch_$(basename "$(dirname "$d")" | cut -d- -f1)"
  for f in $(ls "$d"/*.sql 2>/dev/null | grep -v '00_schema.sql' | sort); do
    grep -q 'tools_run: skip' "$f" && { echo "SKIP $f"; continue; }
    if [[ "$f" == *.sqlite.sql ]]; then
      if sqlite3 :memory: < "$f" >/dev/null 2>/tmp/sql.err; then echo "OK   $f"; else echo "FAIL $f"; head -5 /tmp/sql.err; fail=1; fi; continue
    fi
    $P -d postgres -c "DROP DATABASE IF EXISTS $db" -c "CREATE DATABASE $db ENCODING 'UTF8' TEMPLATE template0" >/dev/null
    if { [ ! -f "$d/00_schema.sql" ] || $P -d $db -f "$d/00_schema.sql" >/dev/null 2>/tmp/sql.err; } && $P -d $db -f "$f" >/dev/null 2>>/tmp/sql.err; then echo "OK   $f"; else echo "FAIL $f"; head -5 /tmp/sql.err; fail=1; fi
  done
done
exit $fail
