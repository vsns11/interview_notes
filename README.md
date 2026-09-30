# Interview Notes

Personal study handbooks for software-engineering interviews, from beginner to intermediate level. Each book is written in Markdown with runnable code, diagrams and colour-coded headings. Each is also rendered to a dark-theme PDF with a small **Interview Edition** for fast revision.

> **PDFs** sit next to their sources: each book's combined Handbook is in its folder, the per-chapter PDFs are in each chapter folder, and the Interview Edition is in `interview/`. The same PDFs are also on the [Releases](../../releases) page.

## Books

| Book | Folder | Interview Edition |
|---|---|---|
| System Design Patterns | [system-design-patterns/](system-design-patterns/) | [interview/](system-design-patterns/interview/) |
| Designing Data-Intensive Applications (study notes) | [ddia/](ddia/) | [interview/](ddia/interview/) |
| Microservices Patterns | [microservices-patterns/](microservices-patterns/) · runnable demos in [demos/](demos/) | [interview/](microservices-patterns/interview/) |
| Low-Level Design (Java) | [lld/](lld/) | [interview/](lld/interview/) |
| Python | [python/](python/) | [interview/](python/interview/) |
| Machine Learning | [ml/](ml/) | [interview/](ml/interview/) |
| AI & GenAI | [ai/](ai/) | [interview/](ai/interview/) |
| SQL | [sql/](sql/) | [interview/](sql/interview/) |
| Java | [java/](java/) | [interview/](java/interview/) |
| Spring | [spring/](spring/) | [interview/](spring/interview/) |
| HTML & CSS | [html-css/](html-css/) | [interview/](html-css/interview/) |
| JavaScript | [javascript/](javascript/) | [interview/](javascript/interview/) |
| React | [react/](react/) | [interview/](react/interview/) |

Each book folder has a `README.md` (contents), `chapters/` (or modules), `interview/INTERVIEW.md` and `tools/` (the PDF builder).

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

This needs Google Chrome installed; `puppeteer-core` drives it. It writes the per-chapter PDFs, the combined Handbook and `interview/<Name>-Interview-Edition.pdf`.

## Note on sources

These are personal notes, written in my own words, from courses I own, textbooks, official docs and papers. Diagrams marked *(course diagram)* belong to their original course authors. This repository is private for that reason.
