# Interview Notes

Personal study handbooks for software-engineering interviews, from beginner to intermediate level. Each book is written in Markdown with runnable code, diagrams and colour-coded headings. Each is also rendered to a dark-theme PDF with a small **Interview Edition** for fast revision.

> **PDFs** sit next to their sources: each book's combined Handbook is in its folder, the per-chapter PDFs are in each chapter folder, and the Interview Edition is in `interview/`. The same PDFs are also on the [Releases](../../releases) page.

## Books

| Book | Folder | Interview Edition | Capstones |
|---|---|---|---|
| System Design Patterns | [system-design-patterns/](system-design-patterns/) | [interview/](system-design-patterns/interview/) | [capstones/](system-design-patterns/capstones/) |
| Designing Data-Intensive Applications (study notes) | [ddia/](ddia/) | [interview/](ddia/interview/) | [capstones/](ddia/capstones/) |
| Microservices Patterns | [microservices-patterns/](microservices-patterns/) · runnable demos in [demos/](demos/) | [interview/](microservices-patterns/interview/) | [capstones/](microservices-patterns/capstones/) |
| Low-Level Design (Java) | [lld/](lld/) | [interview/](lld/interview/) | [capstones/](lld/capstones/) |
| Python | [python/](python/) | [interview/](python/interview/) | [capstones/](python/capstones/) |
| Machine Learning | [ml/](ml/) | [interview/](ml/interview/) | [capstones/](ml/capstones/) |
| AI & GenAI | [ai/](ai/) | [interview/](ai/interview/) | [capstones/](ai/capstones/) |
| SQL | [sql/](sql/) | [interview/](sql/interview/) | [capstones/](sql/capstones/) |
| Java | [java/](java/) | [interview/](java/interview/) | [capstones/](java/capstones/) |
| Spring | [spring/](spring/) | [interview/](spring/interview/) | [capstones/](spring/capstones/) |
| HTML & CSS | [html-css/](html-css/) | [interview/](html-css/interview/) | [capstones/](html-css/capstones/) |
| JavaScript | [javascript/](javascript/) | [interview/](javascript/interview/) | [capstones/](javascript/capstones/) |
| React | [react/](react/) | [interview/](react/interview/) | [capstones/](react/capstones/) |

Each book folder has a `README.md` (contents), `chapters/` (or modules), `interview/INTERVIEW.md`, `capstones/` (40 build-it-yourself project briefs, beginner to advanced, as `CAPSTONES.md` and `<Name>-Capstone-Projects.pdf`) and `tools/` (the PDF builder).

## Running the code

Shared runners live at the root:

| Runner | What it does | Setup |
|---|---|---|
| `bash tools_run_python.sh <book>` | Runs every Python example | `python3 -m venv .venv-books && .venv-books/bin/pip install -r requirements-books.txt` (Python 3.14) |
| `bash tools_run_java.sh java` | Compiles with `javac --release 25` and runs every `main` | JDK 25+. Put JUnit 6, Mockito, AssertJ and the PostgreSQL JDBC jars in `.java-books/lib/` |
| `bash tools_run_js.sh <book>` | Runs JS/JSX/TS examples with Node 24 (esbuild for JSX/TS) | `cd .web-books && npm ci` |
| `bash tools_run_sql.sh sql '<chapter-glob>'` | Fresh database per file on a private PostgreSQL | PostgreSQL 14. `bash tools_pg.sh start` (port 5544, localhost only) |
| `bash tools_run_spring.sh spring '<chapter-glob>'` | `mvn verify` for each Spring Boot 4.1 project | JDK 25, Maven |

## Rebuilding the PDFs

```bash
cd <book> && (cd tools && npm install) && bash tools/regen_pdfs.sh
```

This needs Google Chrome installed; `puppeteer-core` drives it (on Linux, point `CHROME_PATH` at a Chrome or Chromium binary). It writes the per-chapter PDFs, the combined Handbook, `interview/<Name>-Interview-Edition.pdf` and `capstones/<Name>-Capstone-Projects.pdf`.

## Note on sources

These are personal notes, written in my own words, from courses I own, textbooks, official docs and papers. Diagrams marked *(course diagram)* belong to their original course authors. This repository is private for that reason.
