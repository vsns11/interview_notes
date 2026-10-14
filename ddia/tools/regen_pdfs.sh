#!/usr/bin/env bash
# Regenerate every chapter's ChapterNN-Notes.pdf and the combined book PDF.
# Run from the ddia/ root:  bash tools/regen_pdfs.sh
set -euo pipefail
cd "$(dirname "$0")/.."
[ -d tools/node_modules ] || (cd tools && npm install --silent)
for d in chapters/*/; do
  [ -f "$d/README.md" ] || continue
  name=$(basename "$d"); num=${name%%-*}
  node tools/md2pdf.mjs "$d/Chapter${num}-Notes.pdf" "$d/README.md"
done
# The combined book is split into two volumes so each PDF stays under 600 pages.
COVER_TITLE="Designing Data-Intensive Applications — Vol 1 (Preface, Fundamentals, Ch 1–3)" node tools/md2pdf.mjs DDIA-Notes-Vol1.pdf chapters/0[0-3]*/README.md
COVER_TITLE="Designing Data-Intensive Applications — Vol 2 (Ch 4–14)" node tools/md2pdf.mjs DDIA-Notes-Vol2.pdf chapters/0[4-9]*/README.md chapters/1*/README.md
COVER_TITLE="Designing Data-Intensive Applications — Interview Edition" COVER_SUB="Fast revision: every topic, key facts, top questions, gotchas" node tools/md2pdf.mjs interview/DDIA-Interview-Edition.pdf interview/INTERVIEW.md
COVER_TITLE="Designing Data-Intensive Applications — Capstone Projects" node tools/md2pdf.mjs capstones/DDIA-Capstone-Projects.pdf capstones/CAPSTONES.md
