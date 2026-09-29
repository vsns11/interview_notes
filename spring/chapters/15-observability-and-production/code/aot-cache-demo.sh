#!/usr/bin/env bash
# Startup-time comparison: plain fat jar vs extracted jar vs extracted jar + JDK AOT cache (JEP 483/514/515).
# Needs JDK 25+. Results are machine-dependent. Usage: bash aot-cache-demo.sh [path-to-JDK-25-home]
set -euo pipefail
JAVA_HOME=${1:-${JAVA_HOME:-$(/usr/libexec/java_home -v 25 2>/dev/null || true)}}
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"
cd "$(dirname "$0")/production-runtime"
mvn -q -B -ntp -DskipTests package                    # builds target/ch15-production-runtime-1.0.0.jar
JAR=target/ch15-production-runtime-1.0.0.jar
rm -rf target/app && "$JAVA" -Djarmode=tools -jar "$JAR" extract --destination target/app >/dev/null
cd target/app                                         # app jar + lib/ folder

# Start the app until the context is refreshed, then exit; report wall-clock ms (median of 5)
time_ms() {
  local runs=() ; for i in 1 2 3 4 5; do
    local s=$(perl -MTime::HiRes=time -e 'print int(time*1000)')
    "$@" -Dspring.context.exit=onRefresh -Dspring.main.banner-mode=off -Dlogging.level.root=warn \
         -jar ch15-production-runtime-1.0.0.jar >/dev/null 2>&1 || true
    local e=$(perl -MTime::HiRes=time -e 'print int(time*1000)'); runs+=($((e - s)))
  done
  printf '%s\n' "${runs[@]}" | sort -n | sed -n 3p
}

echo "java: $("$JAVA" -version 2>&1 | head -1)"
FAT=$(cd ../.. && s=$(perl -MTime::HiRes=time -e 'print int(time*1000)'); for i in 1 2 3; do "$JAVA" -Dspring.context.exit=onRefresh -Dspring.main.banner-mode=off -Dlogging.level.root=warn -jar "$JAR" >/dev/null 2>&1 || true; done; e=$(perl -MTime::HiRes=time -e 'print int(time*1000)'); echo $(((e - s) / 3)))
echo "1. fat jar (java -jar)                : ${FAT} ms (mean of 3)"
echo "2. extracted jar                      : $(time_ms "$JAVA") ms"
# Training run: record which classes load/link and write the cache in one step (JDK 25, JEP 514)
"$JAVA" -XX:AOTCacheOutput=app.aot -Dspring.context.exit=onRefresh -Dspring.main.banner-mode=off \
        -Dlogging.level.root=warn -jar ch15-production-runtime-1.0.0.jar >/dev/null 2>&1 || true
echo "   AOT cache file                     : $(du -h app.aot | cut -f1)"
echo "3. extracted jar + -XX:AOTCache       : $(time_ms "$JAVA" -XX:AOTCache=app.aot) ms"
