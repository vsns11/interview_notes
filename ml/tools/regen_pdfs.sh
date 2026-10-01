#!/usr/bin/env bash
# Regenerate every chapter's chapters/NN-name/ChapterNN-Notes.pdf and the combined ML-Handbook.pdf.
# Run from the ml/ book root:  bash tools/regen_pdfs.sh
set -euo pipefail
cd "$(dirname "$0")/.."
[ -d tools/node_modules ] || (cd tools && npm install --silent)
for d in chapters/*/; do
  d=${d%/}
  [ -f "$d/README.md" ] || continue
  name=$(basename "$d"); num=${name%%-*}
  node tools/md2pdf.mjs "$d/Chapter${num}-Notes.pdf" "$d/README.md"
done
COVER_TITLE="Machine Learning Handbook" node tools/md2pdf.mjs ML-Handbook.pdf chapters/*/README.md
COVER_TITLE="Machine Learning Handbook — Interview Edition" COVER_SUB="Fast revision: every topic, key facts, top questions, gotchas" node tools/md2pdf.mjs interview/ML-Interview-Edition.pdf interview/INTERVIEW.md
COVER_TITLE="Machine Learning — Capstone Projects" node tools/md2pdf.mjs capstones/ML-Capstone-Projects.pdf capstones/CAPSTONES.md
