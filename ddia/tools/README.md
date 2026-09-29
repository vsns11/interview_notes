# tools/

Run everything from the repo root. Needs Node 18+ and Google Chrome. `regen_pdfs.sh` installs `node_modules` on first run.

| Script | Purpose | Example |
|---|---|---|
| `md2pdf.mjs` | Render one or more `README.md` files → styled PDF (headless Chrome, Monaco font, Mermaid → SVG, course images embedded). Given several inputs, it adds a cover and a table of contents. Reports Mermaid errors and broken images. | `node tools/md2pdf.mjs modules/05-reliability/Module05-Notes.pdf modules/05-reliability/README.md` |
| `regen_pdfs.sh` | Regenerate all 12 module PDFs and the combined `System-Design-Patterns-Notes.pdf`. | `bash tools/regen_pdfs.sh` |

## Notes
- `md2pdf.mjs` hardcodes the Chrome path (`/Applications/Google Chrome.app/...`). Update it if Chrome moves.
- Course images are referenced as `![alt](images/x.png)` relative to each module README. They were downscaled to 1600px wide and quantised to 256 colours to keep the PDFs small.
