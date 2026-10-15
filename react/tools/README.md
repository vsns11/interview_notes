# tools/

Run everything from the `react/` book root. Needs Node 18+ and Google Chrome. `regen_pdfs.sh` installs `node_modules` on first run.

| Script | Purpose | Example |
|---|---|---|
| `md2pdf.mjs` | Render one or more `README.md` files → styled PDF (headless Chrome, Monaco font, colour-coded headings, highlighted code, Mermaid → SVG, images embedded). Given several inputs, it adds a cover (title from `COVER_TITLE`, default "React Handbook"; subtitle from `COVER_SUB`) and a table of contents. Reports Mermaid errors and broken images. | `node tools/md2pdf.mjs chapters/05-effects-and-refs/Chapter05-Notes.pdf chapters/05-effects-and-refs/README.md` |
| `regen_pdfs.sh` | Regenerate every `chapters/NN-name/ChapterNN-Notes.pdf` and the combined `React-Handbook-Vol1.pdf` and `React-Handbook-Vol2.pdf`. | `bash tools/regen_pdfs.sh` |

Shared helpers one level up (in the notes root) are used by the book's code, not by these tools:
- `bash ../tools_run_js.sh react` runs every example (OK / FAIL / SKIP per file); `.jsx`/`.tsx` files are bundled with esbuild first.
- `../tools_dom.mjs` gives Node examples a jsdom `document` and `window`, so components can be rendered with Testing Library.
- `node ../tools_web_shot.mjs <page.html> <out.png> [width] [height]` screenshots a UI page after its browser bundle is built with `../.web-books/node_modules/.bin/esbuild <entry>.jsx --bundle --format=iife --jsx=automatic --outfile=<name>.bundle.js` (light scheme by default, `--dark` for dark).
- `../.web-books/node_modules/.bin/tsc --noEmit -p tsconfig.check.json`, run in `chapters/11-typescript-with-react/code/`, type-checks the Chapter 11 examples.

## Notes
- `md2pdf.mjs` hardcodes the Chrome path (`/Applications/Google Chrome.app/...`). Update it if Chrome moves.
- Images are referenced as `![alt](img/x.png)` relative to each chapter README.
