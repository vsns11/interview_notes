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
# The whole book in three volumes (each under 600 pages): concepts first, then patterns, then interview method + case studies.
COVER_TITLE="System Design Patterns — Vol 1: Fundamentals and Algorithms" COVER_SUB="Every basic concept from zero (Module 0) and the algorithms behind real systems, step by step (Module 19)" node tools/md2pdf.mjs System-Design-Patterns-Vol1.pdf modules/00*/README.md modules/19*/README.md
COVER_TITLE="System Design Patterns — Vol 2: The Patterns" COVER_SUB="Modules 1-11: moving data, storing data, caching, reliability, scaling, consistency, API and edge, operations, batch and streaming, AI-era systems" node tools/md2pdf.mjs System-Design-Patterns-Vol2.pdf modules/0[1-9]*/README.md modules/1[01]*/README.md
COVER_TITLE="System Design Patterns — Vol 3: Interview Method and Case Studies" COVER_SUB="The five-questions method (Module 1), capstones and 40+ worked designs (Modules 12-18, 20, 21)" node tools/md2pdf.mjs System-Design-Patterns-Vol3.pdf modules/01*/README.md modules/1[2-8]*/README.md modules/2[01]*/README.md
COVER_TITLE="System Design Patterns — Interview Edition" COVER_SUB="Fast revision: every topic, key facts, top questions, gotchas" node tools/md2pdf.mjs interview/System-Design-Patterns-Interview-Edition.pdf interview/INTERVIEW.md
COVER_TITLE="System Design Patterns — Capstone Projects" node tools/md2pdf.mjs capstones/System-Design-Patterns-Capstone-Projects.pdf capstones/CAPSTONES.md
