#!/usr/bin/env bash
# Builds and runs an executable JAR of the chapter-18 shop by hand (what Maven/Gradle do for you in 18.2).
#   bash package_jar.sh        (needs JDK 25+; writes only to a temporary folder)
set -euo pipefail
cd "$(dirname "$0")"
W=$(mktemp -d); trap 'rm -rf "$W"' EXIT
mkdir -p "$W/src/ch18" && cp -R ch18/shop "$W/src/ch18/" && cd "$W"      # work on a copy: src/ -> classes/ -> shop.jar
run() { echo "\$ $*"; "$@"; }
echo "\$ javac --release 25 -d classes \$(find src -name '*.java')"
javac --release 25 -d classes $(find src -name '*.java')
run jar --create --file shop.jar --main-class ch18.shop.ShopDemo -C classes .
echo "\$ jar --list --file shop.jar | head -5"; jar --list --file shop.jar | head -5
echo "\$ unzip -p shop.jar META-INF/MANIFEST.MF"; unzip -p shop.jar META-INF/MANIFEST.MF
echo "\$ java -jar shop.jar"; java -jar shop.jar
echo "\$ ls -l shop.jar"; ls -l shop.jar | awk '{print $5 " bytes"}'
