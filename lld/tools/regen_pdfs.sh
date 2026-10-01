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
node tools/md2pdf.mjs LLD-Handbook.pdf chapters/*/README.md
# Study Edition (condensed): per-chapter + combined
for d in chapters/*/; do
  [ -f "$d/STUDY.md" ] || continue
  name=$(basename "$d"); num=${name%%-*}
  COVER_SUB="Study Edition — every topic, less padding. Full Edition = LLD-Handbook.pdf" node tools/md2pdf.mjs "$d/Chapter${num}-Study.pdf" "$d/STUDY.md"
done
COVER_TITLE="Low-Level Design Handbook — Study Edition" COVER_SUB="OOP · UML · SOLID · Clean Code · Refactoring · Patterns · Concurrency · 45 case studies in Java — every topic, readable length" node tools/md2pdf.mjs LLD-Handbook-Study.pdf chapters/*/STUDY.md
# Interview Edition (fast revision, single file)
COVER_TITLE="Low-Level Design Handbook — Interview Edition" COVER_SUB="Fast revision: every topic, key facts, top questions, gotchas" node tools/md2pdf.mjs interview/LLD-Interview-Edition.pdf interview/INTERVIEW.md
COVER_TITLE="Low-Level Design (Java) — Capstone Projects" node tools/md2pdf.mjs capstones/LLD-Capstone-Projects.pdf capstones/CAPSTONES.md
