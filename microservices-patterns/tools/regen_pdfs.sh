#!/usr/bin/env bash
# Regenerate every chapter's chapters/NN-name/ChapterNN-Notes.pdf, the combined Microservices-Patterns-Handbook.pdf
# and the Interview Edition. Run from anywhere:  bash tools/regen_pdfs.sh   (needs Node 18+ and Google Chrome)
set -euo pipefail
cd "$(dirname "$0")/.."
[ -d tools/node_modules ] || (cd tools && npm install --silent)
for d in chapters/*/; do
  d=${d%/}
  [ -f "$d/README.md" ] || continue
  name=$(basename "$d"); num=${name%%-*}
  node tools/md2pdf.mjs "$d/Chapter${num}-Notes.pdf" "$d/README.md"
done
COVER_TITLE="Microservices Patterns Handbook" node tools/md2pdf.mjs Microservices-Patterns-Handbook.pdf chapters/*/README.md
COVER_TITLE="Microservices Patterns — Interview Edition" COVER_SUB="Fast revision: every topic, key facts, top questions, gotchas" node tools/md2pdf.mjs interview/Microservices-Patterns-Interview-Edition.pdf interview/INTERVIEW.md
