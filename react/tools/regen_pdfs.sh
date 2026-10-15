#!/usr/bin/env bash
# Regenerate every chapter's chapters/NN-name/ChapterNN-Notes.pdf and the combined React-Handbook-Vol1.pdf and React-Handbook-Vol2.pdf.
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
COVER_TITLE="React Handbook — Vol 1" COVER_SUB="core React — components, state, forms, effects, state management, hooks, styling, routing, performance (Ch 1–10)" node tools/md2pdf.mjs React-Handbook-Vol1.pdf chapters/0*/README.md chapters/10*/README.md
COVER_TITLE="React Handbook — Vol 2" COVER_SUB="TypeScript, testing, server rendering, patterns, interview and machine coding, cheat sheet, question bank (Ch 11–17)" node tools/md2pdf.mjs React-Handbook-Vol2.pdf chapters/1[1-9]*/README.md
COVER_TITLE="React Handbook — Interview Edition" COVER_SUB="Fast revision: every topic, key facts, top questions, gotchas" node tools/md2pdf.mjs interview/React-Interview-Edition.pdf interview/INTERVIEW.md
COVER_TITLE="React — Capstone Projects" node tools/md2pdf.mjs capstones/React-Capstone-Projects.pdf capstones/CAPSTONES.md
