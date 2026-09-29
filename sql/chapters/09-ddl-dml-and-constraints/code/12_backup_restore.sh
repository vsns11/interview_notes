#!/usr/bin/env bash
# 9.12  Backup and restore with pg_dump / pg_restore against the book's PRIVATE server
# (port 5544, socket /tmp). Everything happens in a temporary directory that is removed at the end.
# Run from the book root:  bash chapters/09-ddl-dml-and-constraints/code/12_backup_restore.sh
set -euo pipefail
export PGOPTIONS="-c client_min_messages=warning"   # hide "does not exist, skipping" notices
CONN="-h /tmp -p 5544 -U postgres"
PSQL="psql $CONN -X -q -v ON_ERROR_STOP=1"
HERE="$(cd "$(dirname "$0")" && pwd)"
WORK="$(mktemp -d)"                                   # temp dir for the backup files
cleanup() {
  $PSQL -d postgres -c "DROP DATABASE IF EXISTS shop" -c "DROP DATABASE IF EXISTS shop_copy" \
        -c "DROP DATABASE IF EXISTS shop_plain" 2>/dev/null
  rm -rf "$WORK"
}
trap cleanup EXIT
count() { $PSQL -d "$1" -At -c "SELECT (SELECT count(*) FROM customers)||' customers, '||(SELECT count(*) FROM orders)||' orders, '||(SELECT count(*) FROM order_items)||' order_items'"; }

echo "== 1. a database to protect"
$PSQL -d postgres -c "DROP DATABASE IF EXISTS shop" -c "CREATE DATABASE shop"
$PSQL -d shop -f "$HERE/00_schema.sql" >/dev/null
echo "shop: $(count shop)"

echo "== 2. logical backups in three formats"
pg_dump $CONN -d shop -F p -f "$WORK/shop.sql"          # plain SQL script
pg_dump $CONN -d shop -F c -f "$WORK/shop.dump"         # custom (compressed, for pg_restore)
pg_dump $CONN -d shop -F d -j 2 -f "$WORK/shop_dir"     # directory format, 2 parallel jobs
ls -1 "$WORK"
echo "-- first CREATE TABLE in the plain dump:"
grep -m1 -A5 '^CREATE TABLE' "$WORK/shop.sql"
echo "-- plain dump contains $(grep -c '^COPY ' "$WORK/shop.sql") COPY blocks (one per table with data)"

echo "== 3. what is inside the custom-format archive (pg_restore -l = table of contents)"
pg_restore -l "$WORK/shop.dump" | grep -E 'TABLE (DATA )?public (customers|orders) ' | sed 's/^[0-9]*; [0-9]* [0-9]* //'

echo "== 4. disaster: someone drops a table"
$PSQL -d shop -c "DROP TABLE order_items"
$PSQL -d shop -c "SELECT count(*) FROM order_items" 2>&1 | head -1 || true

echo "== 5. restore just that table (definition + data) from the archive"
pg_restore $CONN -d shop -t order_items "$WORK/shop.dump"
echo "shop: $(count shop)"
echo "-- note: -t restores the table and its rows, not its PK/FK constraints:"
$PSQL -d shop -At -c "SELECT count(*) || ' constraints on order_items' FROM pg_constraint WHERE conrelid = 'order_items'::regclass AND contype IN ('p','f')"

echo "== 6. full restore into a NEW database, 2 parallel jobs"
$PSQL -d postgres -c "CREATE DATABASE shop_copy"
pg_restore $CONN -d shop_copy -j 2 "$WORK/shop.dump"
echo "shop_copy: $(count shop_copy)"
$PSQL -d shop_copy -At -c "SELECT count(*) || ' constraints on order_items' FROM pg_constraint WHERE conrelid = 'order_items'::regclass AND contype IN ('p','f')"

echo "== 7. a plain dump is restored with psql"
$PSQL -d postgres -c "CREATE DATABASE shop_plain"
$PSQL -d shop_plain -f "$WORK/shop.sql" >/dev/null
echo "shop_plain: $(count shop_plain)"

echo "== 8. schema-only and data-only dumps of one table"
n_schema=$(pg_dump $CONN -d shop -t products --schema-only | grep -c 'CREATE TABLE' || true)
n_data=$(pg_dump $CONN -d shop -t products --data-only | grep -c 'CREATE TABLE' || true)
echo "CREATE TABLE statements in schema-only dump: $n_schema, in data-only dump: $n_data"
echo "rows (COPY data lines) in data-only dump: $(pg_dump $CONN -d shop -t products --data-only | sed -n '/^COPY/,/^\\\./p' | grep -vc '^COPY\|^\\\.' || true)"

echo "== done: the temporary databases and the backup directory are removed on exit"
