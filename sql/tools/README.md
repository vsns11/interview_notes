# tools/

Run everything from the `sql/` book root. Needs Node 18+ and Google Chrome. `regen_pdfs.sh` installs `node_modules` on first run (marked, highlight.js, mermaid 10, puppeteer-core).

| Script | Purpose | Example |
|---|---|---|
| `md2pdf.mjs` | Render one or more `README.md` files → styled PDF (headless Chrome, Monaco font, colour-coded headings, highlighted code, Mermaid → SVG, images embedded). Given several inputs, it adds a cover (title from `COVER_TITLE`, default "SQL Handbook"; subtitle from `COVER_SUB`) and a table of contents. Reports Mermaid errors and broken images. | `node tools/md2pdf.mjs chapters/05-joins/Chapter05-Notes.pdf chapters/05-joins/README.md` |
| `regen_pdfs.sh` | Regenerate every `chapters/NN-name/ChapterNN-Notes.pdf` and the combined handbook as `SQL-Handbook-Vol1.pdf` and `SQL-Handbook-Vol2.pdf` (two volumes, each under 600 pages). | `bash tools/regen_pdfs.sh` |

Shared helpers one level up (in the notes root) are used by the book's code, not by these tools:
- `bash ../tools_pg.sh start | stop | psql [args]` runs the book's private PostgreSQL 14 (data in `~/.pg-books-system-design/` — outside the iCloud-synced Documents folder, port 5544, Unix socket in `/tmp`, UTF8). It never touches another PostgreSQL server.
- `bash ../tools_run_sql.sh sql ['NN-*']` runs every chapter's `code/*.sql` in a fresh database per file (the chapter's `00_schema.sql` first; `*.sqlite.sql` in SQLite) and prints OK / FAIL / SKIP. The optional glob limits it to some chapters.
- `../.venv-books/` (Python with psycopg 3 and matplotlib) and `../.java-books/lib/` (PostgreSQL JDBC driver, junixsocket) serve the Python and Java demos.

## Notes
- `md2pdf.mjs` hardcodes the Chrome path (`/Applications/Google Chrome.app/...`). Update it if Chrome moves.
- Images are referenced as `![alt](img/x.png)` relative to each chapter README.
