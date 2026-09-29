# tools/

Run everything from the `ml/` book root. Needs Node 18+ and Google Chrome. `regen_pdfs.sh` installs `node_modules` on first run.

| Script | Purpose | Example |
|---|---|---|
| `md2pdf.mjs` | Render one or more `README.md` files → styled PDF (headless Chrome, Monaco font, Mermaid → SVG, images embedded). Given several inputs, it adds a cover (title from `COVER_TITLE`, default "Machine Learning Handbook") and a table of contents. Reports Mermaid errors and broken images. | `node tools/md2pdf.mjs chapters/06-evaluation-and-tuning/Chapter06-Notes.pdf chapters/06-evaluation-and-tuning/README.md` |
| `regen_pdfs.sh` | Regenerate every `chapters/NN-name/ChapterNN-Notes.pdf` and the combined `ML-Handbook.pdf`. | `bash tools/regen_pdfs.sh` |

To run all the book's Python examples, use the shared runner one level up: `bash ../tools_run_python.sh ml`.

## Notes
- `md2pdf.mjs` hardcodes the Chrome path (`/Applications/Google Chrome.app/...`). Update it if Chrome moves.
- Images are referenced as `![alt](img/x.png)` (plots made by the chapter's scripts, plus a few course diagrams) relative to each chapter README.
