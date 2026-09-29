# tools/

Run everything from the `html-css/` book root. Needs Node 18+ and Google Chrome. `regen_pdfs.sh` installs `node_modules` on first run.

| Script | Purpose | Example |
|---|---|---|
| `md2pdf.mjs` | Render one or more `README.md` files → styled PDF (headless Chrome, Monaco font, colour-coded headings, highlighted code, Mermaid → SVG, images embedded). Given several inputs, it adds a cover (title from `COVER_TITLE`, default "HTML & CSS Handbook"; subtitle from `COVER_SUB`) and a table of contents. Reports Mermaid errors and broken images. | `node tools/md2pdf.mjs chapters/08-flexbox/Chapter08-Notes.pdf chapters/08-flexbox/README.md` |
| `regen_pdfs.sh` | Regenerate every `chapters/NN-name/ChapterNN-Notes.pdf` and the combined `HTML-CSS-Handbook.pdf`. | `bash tools/regen_pdfs.sh` |

Shared helpers one level up (in the notes root) are used by the book's code, not by these tools:
- `bash ../tools_run_js.sh html-css` runs every example (OK / FAIL / SKIP per file).
- `node ../tools_web_shot.mjs <page.html> <out.png> [width] [height] [--dark] [--wait=ms]` screenshots an `.html` page (light scheme by default).
- `../tools_dom.mjs` gives Node examples a jsdom `document` and `window`.
- Each of Chapters 7–13 has its own `code/measure.mjs` (puppeteer-core + Chrome) used by that chapter's measuring scripts.

## Notes
- `md2pdf.mjs` hardcodes the Chrome path (`/Applications/Google Chrome.app/...`). Update it if Chrome moves.
- Images are referenced as `![alt](img/x.png)` relative to each chapter README.
