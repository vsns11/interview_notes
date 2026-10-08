#!/usr/bin/env bash
# Regenerate every chapter's chapters/NN-name/ChapterNN-Notes.pdf and the combined handbook as SQL-Handbook-Vol1.pdf + SQL-Handbook-Vol2.pdf.
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
# The combined book is split into two volumes so each PDF stays under 600 pages.
COVER_TITLE="SQL Handbook — Vol 1 (Ch 1–11)" node tools/md2pdf.mjs SQL-Handbook-Vol1.pdf chapters/0*/README.md chapters/1[01]*/README.md
COVER_TITLE="SQL Handbook — Vol 2 (Ch 12–20)" node tools/md2pdf.mjs SQL-Handbook-Vol2.pdf chapters/1[2-9]*/README.md chapters/2*/README.md
COVER_TITLE="SQL Handbook — Interview Edition" COVER_SUB="Fast revision: every topic, key facts, top questions, gotchas" node tools/md2pdf.mjs interview/SQL-Interview-Edition.pdf interview/INTERVIEW.md
COVER_TITLE="SQL — Capstone Projects" node tools/md2pdf.mjs capstones/SQL-Capstone-Projects.pdf capstones/CAPSTONES.md
