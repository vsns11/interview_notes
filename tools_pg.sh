#!/usr/bin/env bash
# Private PostgreSQL for the SQL Handbook (never touches your own server).
#   bash tools_pg.sh start | stop | psql [args]      data dir: ~/.pg-books-system-design/ (outside iCloud), port 5544
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"; D="$HOME/.pg-books-system-design"; PORT=5544   # data kept OUTSIDE the iCloud-synced Documents folder
case "${1:-start}" in
  start) [ -d "$D" ] || initdb -D "$D" -U postgres --auth=trust -E UTF8 --locale=C >/dev/null
         pg_ctl -D "$D" -o "-p $PORT -k /tmp -c listen_addresses=127.0.0.1" -l "$D/log.txt" status >/dev/null 2>&1 || pg_ctl -D "$D" -o "-p $PORT -k /tmp -c listen_addresses=127.0.0.1" -l "$D/log.txt" start -w >/dev/null
         echo "postgres ready on port $PORT (socket /tmp)";;
  stop)  pg_ctl -D "$D" stop -m fast >/dev/null && echo stopped;;
  psql)  shift; exec psql -h /tmp -p $PORT -U postgres -X "$@";;
esac
