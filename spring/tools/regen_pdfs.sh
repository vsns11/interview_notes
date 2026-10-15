#!/usr/bin/env bash
# Regenerate every chapter's chapters/NN-name/ChapterNN-Notes.pdf and the combined Spring-Handbook-Vol1.pdf and Spring-Handbook-Vol2.pdf.
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
COVER_TITLE="Spring Handbook — Vol 1" COVER_SUB="Spring core, configuration, AOP, MVC and REST, REST clients, data access and transactions, Spring Data JPA, caching (Ch 1–9)" node tools/md2pdf.mjs Spring-Handbook-Vol1.pdf chapters/0*/README.md
COVER_TITLE="Spring Handbook — Vol 2" COVER_SUB="security, testing, reactive, messaging, observability, Spring Cloud, Spring AI and what's new, interview questions, cheat sheet, question bank (Ch 10–20)" node tools/md2pdf.mjs Spring-Handbook-Vol2.pdf chapters/1*/README.md chapters/2*/README.md
COVER_TITLE="Spring Handbook — Interview Edition" COVER_SUB="Fast revision: every topic, key facts, top questions, gotchas" node tools/md2pdf.mjs interview/Spring-Interview-Edition.pdf interview/INTERVIEW.md
COVER_TITLE="Spring — Capstone Projects" node tools/md2pdf.mjs capstones/Spring-Capstone-Projects.pdf capstones/CAPSTONES.md
