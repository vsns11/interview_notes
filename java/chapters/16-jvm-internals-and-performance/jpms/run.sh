#!/usr/bin/env bash
# Compiles and runs the two-module JPMS example (not handled by tools_run_java.sh, which compiles code/ as one unit).
# Usage: bash run.sh      (from this jpms/ folder; needs JDK 25+ on PATH)
set -euo pipefail
cd "$(dirname "$0")"
rm -rf mods runtime
echo '$ javac -d mods --module-source-path src $(find src -name "*.java")'
javac --release 25 -d mods --module-source-path src $(find src -name '*.java')
echo '$ java --module-path mods -m com.example.app/com.example.app.Main'
java --module-path mods -m com.example.app/com.example.app.Main
echo '$ java --module-path mods --describe-module com.example.greet'
java --module-path mods --describe-module com.example.greet
echo '$ jdeps --module-path mods -s mods/com.example.app'
jdeps --module-path mods -s mods/com.example.app
echo '$ jlink --module-path mods --add-modules com.example.app --output runtime --strip-debug --no-header-files --no-man-pages'
jlink --module-path mods --add-modules com.example.app --output runtime --strip-debug --no-header-files --no-man-pages
echo '$ runtime/bin/java --list-modules'
runtime/bin/java --list-modules
echo '$ du -sh runtime'
du -sh runtime | awk '{print $1 "  runtime/"}'
rm -rf mods runtime
