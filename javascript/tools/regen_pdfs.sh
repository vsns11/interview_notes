#!/usr/bin/env bash
# Regenerate every chapter's chapters/NN-name/ChapterNN-Notes.pdf and the combined JavaScript-Handbook-Vol1.pdf and JavaScript-Handbook-Vol2.pdf.
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
COVER_TITLE="JavaScript Handbook — Vol 1" COVER_SUB="the language — setup, types and operators, control flow, functions and closures, objects, strings and regex, arrays, collections and generators, this/prototypes/classes, modules and tooling (Ch 1–10)" node tools/md2pdf.mjs JavaScript-Handbook-Vol1.pdf chapters/0*/README.md chapters/10*/README.md
COVER_TITLE="JavaScript Handbook — Vol 2" COVER_SUB="errors, async, DOM, browser APIs and security, Node.js, testing and TypeScript, interview prep, cheat sheet, question bank (Ch 11–19)" node tools/md2pdf.mjs JavaScript-Handbook-Vol2.pdf chapters/1[1-9]*/README.md
COVER_TITLE="JavaScript Handbook — Interview Edition" COVER_SUB="Fast revision: every topic, key facts, top questions, gotchas" node tools/md2pdf.mjs interview/JavaScript-Interview-Edition.pdf interview/INTERVIEW.md
COVER_TITLE="JavaScript — Capstone Projects" node tools/md2pdf.mjs capstones/JavaScript-Capstone-Projects.pdf capstones/CAPSTONES.md
