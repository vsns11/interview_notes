# tools/

Run everything from the `spring/` book root. Needs Node 18+ and Google Chrome. `regen_pdfs.sh` installs `node_modules` on first run.

| Script | Purpose | Example |
|---|---|---|
| `md2pdf.mjs` | Render one or more `README.md` files → styled PDF (headless Chrome, Monaco font, colour-coded headings, highlighted code, Mermaid → SVG, images embedded). Given several inputs, it adds a cover (title from `COVER_TITLE`, default "Spring Handbook"; subtitle from `COVER_SUB`) and a table of contents. Reports Mermaid errors and broken images. | `node tools/md2pdf.mjs chapters/07-data-access-jdbc-and-transactions/Chapter07-Notes.pdf chapters/07-data-access-jdbc-and-transactions/README.md` |
| `regen_pdfs.sh` | Regenerate every `chapters/NN-name/ChapterNN-Notes.pdf` and the combined `Spring-Handbook.pdf`. | `bash tools/regen_pdfs.sh` |

Shared helpers one level up (in the notes root) are used by the book's code, not by these tools:
- `bash ../tools_run_spring.sh spring ['NN-*']` runs `mvn -q -B -ntp verify` in every Maven project under `chapters/*/code/` (optionally only the chapters matching the glob) and prints OK / FAIL / SKIP per project. Projects containing a `.tools_run_skip` file (they need Docker or a broker) are skipped.
- `bash ../tools_pg.sh start` starts the private PostgreSQL on `127.0.0.1:5544` that Chapter 7's `tx-demos` uses.

## Notes
- `md2pdf.mjs` hardcodes the Chrome path (`/Applications/Google Chrome.app/...`). Update it if Chrome moves.
- Images (none yet) would be referenced as `![alt](img/x.png)` relative to each chapter README.
- Delete `chapters/*/code/*/target/` after a run; only sources are kept.
