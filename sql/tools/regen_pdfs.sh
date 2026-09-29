#!/usr/bin/env bash
# Regenerate every chapter's chapters/NN-name/ChapterNN-Notes.pdf and the combined SQL-Handbook.pdf.
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
COVER_TITLE="SQL Handbook" node tools/md2pdf.mjs SQL-Handbook.pdf chapters/*/README.md
COVER_TITLE="SQL Handbook — Interview Edition" COVER_SUB="Fast revision: every topic, key facts, top questions, gotchas" node tools/md2pdf.mjs interview/SQL-Interview-Edition.pdf interview/INTERVIEW.md
