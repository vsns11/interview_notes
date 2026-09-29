# tools/

Run everything from the `java/` book root. Needs Node 18+ and Google Chrome. `regen_pdfs.sh` installs `node_modules` on first run.

| Script | Purpose | Example |
|---|---|---|
| `md2pdf.mjs` | Render one or more `README.md` files → styled PDF (headless Chrome, Monaco font, colour-coded headings, highlighted code, Mermaid → SVG, images embedded). Given several inputs, it adds a cover (title from `COVER_TITLE`, default "Java Handbook"; subtitle from `COVER_SUB`) and a table of contents. Reports Mermaid errors and broken images. | `node tools/md2pdf.mjs chapters/12-streams/Chapter12-Notes.pdf chapters/12-streams/README.md` |
| `regen_pdfs.sh` | Regenerate every `chapters/NN-name/ChapterNN-Notes.pdf` and the combined `Java-Handbook.pdf`. | `bash tools/regen_pdfs.sh` |

Shared helpers one level up (in the notes root) are used by the book's code, not by these tools:
- `bash ../tools_run_java.sh java` compiles each chapter's `code/` with `javac --release 25` and runs every class with a `main` (OK / FAIL / SKIP per file). Jars in `../.java-books/lib/` (JUnit, Mockito, AssertJ, PostgreSQL JDBC driver) are on its class path.
- `bash ../tools_pg.sh start` starts the private PostgreSQL that Chapter 18's `JdbcDemo` connects to.
- Chapter-specific scripts live with their chapter: `chapters/18-testing-build-and-tooling/code/run_tests.sh` (JUnit tests), `code/package_jar.sh` (executable JAR), and `chapters/16-jvm-internals-and-performance/jpms/run.sh` (modules).

## Notes
- `md2pdf.mjs` hardcodes the Chrome path (`/Applications/Google Chrome.app/...`). Update it if Chrome moves.
- Images are referenced as `![alt](img/x.png)` relative to each chapter README.
