#!/usr/bin/env bash
# Regenerate every module's ModuleNN-Notes.pdf and the combined course PDF.
# Run from the repo root:  bash tools/regen_pdfs.sh
set -euo pipefail
cd "$(dirname "$0")/.."
[ -d tools/node_modules ] || (cd tools && npm install --silent)
for d in modules/*/; do
  name=$(basename "$d")        # e.g. 05-reliability
  num=${name%%-*}              # e.g. 05
  node tools/md2pdf.mjs "$d/Module${num}-Notes.pdf" "$d/README.md"
done
node tools/md2pdf.mjs System-Design-Patterns-Notes.pdf modules/*/README.md
COVER_TITLE="System Design Patterns — Interview Edition" COVER_SUB="Fast revision: every topic, key facts, top questions, gotchas" node tools/md2pdf.mjs interview/System-Design-Patterns-Interview-Edition.pdf interview/INTERVIEW.md
COVER_TITLE="System Design Patterns — Capstone Projects" node tools/md2pdf.mjs capstones/System-Design-Patterns-Capstone-Projects.pdf capstones/CAPSTONES.md
