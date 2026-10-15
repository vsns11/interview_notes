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
# Full Edition in five volumes (each under 350 pages): concepts first, then interview approach + case studies.
COVER_TITLE="LLD Handbook — Vol 1: OOP, UML, SOLID, Design Principles, Clean Code, Refactoring" node tools/md2pdf.mjs LLD-Handbook-Vol1.pdf chapters/0[2-7]*/README.md
COVER_TITLE="LLD Handbook — Vol 2: Design Patterns and Concurrency" node tools/md2pdf.mjs LLD-Handbook-Vol2.pdf chapters/0[89]*/README.md chapters/1[0-3]*/README.md
COVER_TITLE="LLD Handbook — Vol 3: Interview Approach and Case Studies I" node tools/md2pdf.mjs LLD-Handbook-Vol3.pdf chapters/01*/README.md chapters/1[4-6]*/README.md
COVER_TITLE="LLD Handbook — Vol 4: Case Studies II" node tools/md2pdf.mjs LLD-Handbook-Vol4.pdf chapters/1[7-9]*/README.md chapters/20*/README.md
COVER_TITLE="LLD Handbook — Vol 5: Machine Coding, Revision and Practice" node tools/md2pdf.mjs LLD-Handbook-Vol5.pdf chapters/2[1-9]*/README.md
# Study Edition (condensed): per-chapter + combined
for d in chapters/*/; do
  [ -f "$d/STUDY.md" ] || continue
  name=$(basename "$d"); num=${name%%-*}
  COVER_SUB="Study Edition — every topic, less padding. Full Edition = LLD-Handbook Vol 1-5" node tools/md2pdf.mjs "$d/Chapter${num}-Study.pdf" "$d/STUDY.md"
done
# Study Edition in three volumes (each under 350 pages).
COVER_TITLE="LLD Handbook Study Edition — Vol 1: Concepts (OOP to Concurrency)" COVER_SUB="OOP · UML · SOLID · Clean Code · Refactoring · Patterns · Concurrency · 45 case studies in Java — every topic, readable length" node tools/md2pdf.mjs LLD-Handbook-Study-Vol1.pdf chapters/0[2-9]*/STUDY.md chapters/1[0-3]*/STUDY.md
COVER_TITLE="LLD Handbook Study Edition — Vol 2: Interview Approach and Case Studies I" COVER_SUB="OOP · UML · SOLID · Clean Code · Refactoring · Patterns · Concurrency · 45 case studies in Java — every topic, readable length" node tools/md2pdf.mjs LLD-Handbook-Study-Vol2.pdf chapters/01*/STUDY.md chapters/1[4-8]*/STUDY.md
COVER_TITLE="LLD Handbook Study Edition — Vol 3: Case Studies II, Machine Coding, Revision and Practice" COVER_SUB="OOP · UML · SOLID · Clean Code · Refactoring · Patterns · Concurrency · 45 case studies in Java — every topic, readable length" node tools/md2pdf.mjs LLD-Handbook-Study-Vol3.pdf chapters/19*/STUDY.md chapters/2*/STUDY.md
# Interview Edition (fast revision, single file)
COVER_TITLE="Low-Level Design Handbook — Interview Edition" COVER_SUB="Fast revision: every topic, key facts, top questions, gotchas" node tools/md2pdf.mjs interview/LLD-Interview-Edition.pdf interview/INTERVIEW.md
COVER_TITLE="Low-Level Design (Java) — Capstone Projects" node tools/md2pdf.mjs capstones/LLD-Capstone-Projects.pdf capstones/CAPSTONES.md
