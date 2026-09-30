# tools/

Run everything from the `microservices-patterns/` book root. Needs Node 18+ and Google Chrome. `regen_pdfs.sh` installs `node_modules` on first run.

| Script | Purpose | Example |
|---|---|---|
| `md2pdf.mjs` | Render one or more `README.md` files → styled PDF (headless Chrome, Mermaid → SVG, images embedded). Given several inputs, it adds a cover (title from `COVER_TITLE`) and a table of contents. Reports Mermaid errors and broken images. | `node tools/md2pdf.mjs chapters/06-circuit-breaker/Chapter06-Notes.pdf chapters/06-circuit-breaker/README.md` |
| `regen_pdfs.sh` | Regenerate every `chapters/NN-name/ChapterNN-Notes.pdf`, the combined `Microservices-Patterns-Handbook.pdf` and `interview/Microservices-Patterns-Interview-Edition.pdf`. | `bash tools/regen_pdfs.sh` |

The runnable Java demos for each pattern are in [`../../demos/`](../../demos/) (Gradle; chapter numbers match).
