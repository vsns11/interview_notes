# tools/

Run everything from the `python/` book root. Needs Node 18+ and Google Chrome. `regen_pdfs.sh` installs `node_modules` on first run.

| Script | Purpose | Example |
|---|---|---|
| `md2pdf.mjs` | Render one or more `README.md` files → styled PDF (headless Chrome, Monaco font, Mermaid → SVG, images embedded). Given several inputs, it adds a cover (title from `COVER_TITLE`, default "Python Handbook") and a table of contents. Reports Mermaid errors and broken images. | `node tools/md2pdf.mjs chapters/05-lists-tuples-arrays/Chapter05-Notes.pdf chapters/05-lists-tuples-arrays/README.md` |
| `regen_pdfs.sh` | Regenerate every `chapters/NN-name/ChapterNN-Notes.pdf` and the combined `Python-Handbook.pdf`. | `bash tools/regen_pdfs.sh` |

To run all the book's Python examples, use the shared runner one level up: `bash ../tools_run_python.sh python`.

## Notes
- `md2pdf.mjs` hardcodes the Chrome path (`/Applications/Google Chrome.app/...`). Update it if Chrome moves.
- Images are referenced as `![alt](img/x.png)` relative to each chapter README.
