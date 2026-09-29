#!/usr/bin/env bash
# Compiles the chapter-18 code and its JUnit tests, then runs them with the JUnit Console Launcher.
#   bash run_tests.sh          all tests except the deliberately failing ones (tag demo-failure)
#   bash run_tests.sh fail     only the deliberately failing demo tests
# Needs JDK 25+ and the jars in ROOT/.java-books/lib/ (JUnit 6 console launcher, Mockito, AssertJ).
set -uo pipefail
cd "$(dirname "$0")"
LIB="../../../../.java-books/lib"
CP="$LIB/*"
OUT=$(mktemp -d)
javac --release 25 -cp "$CP" -d "$OUT" $(find ch18/shop test -name '*.java') || exit 1
if [ "${1:-}" = fail ]; then TAGS=(--include-tag demo-failure); else TAGS=(--exclude-tag demo-failure); fi
# Everything on ONE class path, launcher started by main class: Mockito is loaded once, as a -javaagent
# (the agent must be the same jar the tests use, so `java -jar console-standalone.jar --class-path ...` won't do here).
java -Xshare:off -javaagent:"$(ls "$LIB"/mockito-core-*.jar)" \
     -Djava.util.logging.SimpleFormatter.format='%4$s: %5$s%n' \
     -cp "$OUT:$CP" org.junit.platform.console.ConsoleLauncher execute \
     --scan-class-path "$OUT" "${TAGS[@]}" \
     --disable-banner --disable-ansi-colors --details=tree
status=$?
rm -rf "$OUT"; exit $status
