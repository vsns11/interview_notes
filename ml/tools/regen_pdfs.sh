#!/usr/bin/env bash
# Regenerate every chapter's chapters/NN-name/ChapterNN-Notes.pdf and the combined ML-Handbook-Vol1.pdf and ML-Handbook-Vol2.pdf.
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
COVER_TITLE="Machine Learning Handbook — Vol 1" COVER_SUB="foundations and core algorithms — ML basics, maths, Python stack, data and features, regression, evaluation, trees and ensembles, kNN/SVM/Naive Bayes, unsupervised learning, neural networks (Ch 1–10)" node tools/md2pdf.mjs ML-Handbook-Vol1.pdf chapters/0*/README.md chapters/10*/README.md
COVER_TITLE="Machine Learning Handbook — Vol 2" COVER_SUB="deep learning, recommenders, reinforcement learning, ML system design, production, case studies, responsible ML, cheat sheet, question bank (Ch 11–21)" node tools/md2pdf.mjs ML-Handbook-Vol2.pdf chapters/1[1-9]*/README.md chapters/2*/README.md
COVER_TITLE="Machine Learning Handbook — Interview Edition" COVER_SUB="Fast revision: every topic, key facts, top questions, gotchas" node tools/md2pdf.mjs interview/ML-Interview-Edition.pdf interview/INTERVIEW.md
COVER_TITLE="Machine Learning — Capstone Projects" node tools/md2pdf.mjs capstones/ML-Capstone-Projects.pdf capstones/CAPSTONES.md
