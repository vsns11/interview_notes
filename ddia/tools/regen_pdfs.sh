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
node tools/md2pdf.mjs DDIA-Notes.pdf chapters/*/README.md
COVER_TITLE="Designing Data-Intensive Applications — Interview Edition" COVER_SUB="Fast revision: every topic, key facts, top questions, gotchas" node tools/md2pdf.mjs interview/DDIA-Interview-Edition.pdf interview/INTERVIEW.md
