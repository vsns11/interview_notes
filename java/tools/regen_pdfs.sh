#!/usr/bin/env bash
# Regenerate every chapter's chapters/NN-name/ChapterNN-Notes.pdf and the combined Java-Handbook-Vol1.pdf and Java-Handbook-Vol2.pdf.
# Run from anywhere:  bash tools/regen_pdfs.sh   (needs Node 18+ and Google Chrome)
set -euo pipefail
cd "$(dirname "$0")/.."
[ -d tools/node_modules ] || (cd tools && npm install --silent)
for d in chapters/*/; do
  d=${d%/}
  [ -f "$d/README.md" ] || continue
  name=$(basename "$d"); num=${name%%-*}
  node tools/md2pdf.mjs "$d/Chapter${num}-Notes.pdf" "$d/README.md"
done
COVER_TITLE="Java Handbook — Vol 1" COVER_SUB="the language — JVM, types, control flow, methods and arrays, OOP, core APIs, exceptions, generics, collections, lambdas, streams (Ch 1–12)" node tools/md2pdf.mjs Java-Handbook-Vol1.pdf chapters/0*/README.md chapters/1[0-2]*/README.md
COVER_TITLE="Java Handbook — Vol 2" COVER_SUB="I/O, concurrency, JVM internals, modern Java 17–25, testing and tooling, coding interviews, cheat sheet, question bank, multithreading interview problems (Ch 13–22)" node tools/md2pdf.mjs Java-Handbook-Vol2.pdf chapters/1[3-9]*/README.md chapters/2*/README.md
COVER_TITLE="Java Handbook — Interview Edition" COVER_SUB="Fast revision: every topic, key facts, top questions, gotchas" node tools/md2pdf.mjs interview/Java-Interview-Edition.pdf interview/INTERVIEW.md
COVER_TITLE="Java — Capstone Projects" node tools/md2pdf.mjs capstones/Java-Capstone-Projects.pdf capstones/CAPSTONES.md
