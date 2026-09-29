#!/usr/bin/env bash
# Build + test every Maven project under chapters/*/code/*/ of the Spring book (mvn -q -B verify).
#   bash tools_run_spring.sh [book=spring] [chapter-glob='*']
# Projects containing a file named .tools_run_skip are skipped (e.g. need Docker). Uses ~/.m2 as the cache.
set -uo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"; cd "$ROOT/${1:-spring}" || exit 1
fail=0
for pom in $(find chapters/${2:-*}/code -name pom.xml -not -path '*/target/*' -maxdepth 3 | sort); do
  d=$(dirname "$pom")
  [ -f "$d/.tools_run_skip" ] && { echo "SKIP $d"; continue; }
  if (cd "$d" && perl -e 'alarm 600; exec @ARGV' mvn -q -B -ntp verify >/tmp/spring_run.log 2>&1); then echo "OK   $d"; else echo "FAIL $d"; tail -15 /tmp/spring_run.log; fail=1; fi
done
exit $fail
