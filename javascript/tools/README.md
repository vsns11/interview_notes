# tools/

Run everything from the `javascript/` book root. Needs Node 18+ and Google Chrome. `regen_pdfs.sh` installs `node_modules` on first run.

| Script | Purpose | Example |
|---|---|---|
| `md2pdf.mjs` | Render one or more `README.md` files → styled PDF (headless Chrome, Monaco font, colour-coded headings, highlighted code, Mermaid → SVG, images embedded). Given several inputs, it adds a cover (title from `COVER_TITLE`, default "JavaScript Handbook"; subtitle from `COVER_SUB`) and a table of contents. Reports Mermaid errors and broken images. | `node tools/md2pdf.mjs chapters/07-arrays/Chapter07-Notes.pdf chapters/07-arrays/README.md` |
| `regen_pdfs.sh` | Regenerate every `chapters/NN-name/ChapterNN-Notes.pdf` and the combined `JavaScript-Handbook-Vol1.pdf` and `JavaScript-Handbook-Vol2.pdf`. | `bash tools/regen_pdfs.sh` |

Shared helpers one level up (in the notes root) are used by the book's code, not by these tools:
- `bash ../tools_run_js.sh javascript` runs every example (OK / FAIL / SKIP per file).
- `../tools_dom.mjs` gives Node examples a jsdom `document` and `window`.
- `node ../tools_web_shot.mjs <page.html> <out.png> [width]` screenshots an `.html` demo (light scheme by default, `--dark` for dark).

## Notes
- `md2pdf.mjs` hardcodes the Chrome path (`/Applications/Google Chrome.app/...`). Update it if Chrome moves.
- Images are referenced as `![alt](img/x.png)` relative to each chapter README.
