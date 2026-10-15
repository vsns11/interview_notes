#!/usr/bin/env bash
# Regenerate every chapter's chapters/NN-name/ChapterNN-Notes.pdf and the combined Python-Handbook-Vol1.pdf and Python-Handbook-Vol2.pdf.
# Run from the python/ book root:  bash tools/regen_pdfs.sh
set -euo pipefail
cd "$(dirname "$0")/.."
[ -d tools/node_modules ] || (cd tools && npm install --silent)
for d in chapters/*/; do
  d=${d%/}
  [ -f "$d/README.md" ] || continue
  name=$(basename "$d"); num=${name%%-*}
  node tools/md2pdf.mjs "$d/Chapter${num}-Notes.pdf" "$d/README.md"
done
COVER_TITLE="Python Handbook — Vol 1" COVER_SUB="the language — types, control flow, strings, collections, functions, modules, errors and files, OOP, iterators and generators (Ch 1–11)" node tools/md2pdf.mjs Python-Handbook-Vol1.pdf chapters/0*/README.md chapters/1[01]*/README.md
COVER_TITLE="Python Handbook — Vol 2" COVER_SUB="decorators, type hints, standard library, testing, concurrency, internals, data stack, coding interviews, idioms, cheat sheet, question bank (Ch 12–22)" node tools/md2pdf.mjs Python-Handbook-Vol2.pdf chapters/1[2-9]*/README.md chapters/2*/README.md
COVER_TITLE="Python Handbook — Interview Edition" COVER_SUB="Fast revision: every topic, key facts, top questions, gotchas" node tools/md2pdf.mjs interview/Python-Interview-Edition.pdf interview/INTERVIEW.md
COVER_TITLE="Python — Capstone Projects" node tools/md2pdf.mjs capstones/Python-Capstone-Projects.pdf capstones/CAPSTONES.md
