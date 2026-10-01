# Low-Level Design Handbook (Java)

One place to study LLD / OOD for interviews and real work — no switching between platforms. It combines, topic by topic:
- **DesignGurus courses** you own: *Grokking the Object Oriented Design Interview*, *Grokking Design Patterns for Engineers and Managers*, *Grokking SOLID Design Principles* (with their diagrams),
- **the classic books**: *Head First Design Patterns*, *Clean Code*, *Refactoring* (2nd ed.) and GoF,
- **the GitHub problem sets**: awesome-low-level-design, LLD-Handbook-for-Coding-Interviews, kumaransg/LLD,
- **the web** to fill gaps, plus real-world examples (JDK, Spring, products you use).

Every chapter is written for a first-time learner, every Java example is **Java 21, compiled and run** (real output shown), and every chapter was independently reviewed for accuracy.

> Personal study notes. Course diagrams belong to DesignGurus. Not for redistribution.

## What's in here
| Path | Purpose |
|---|---|
| `LLD-Handbook-Study.pdf` | **Start here — Study Edition (~670 pages).** Every topic, heading and case study, with less padding. |
| `interview/LLD-Interview-Edition.pdf` | **Interview Edition (~47 pages)** — fast night-before revision: framework, OOP/UML/SOLID, every pattern as one table row, concurrency, all 45 case studies as compact cards. Source: `interview/INTERVIEW.md`. |
| `LLD-Handbook.pdf` | Full Edition (~1,700 pages): longer explanations and complete code listings, for reference. |
| `chapters/NN-name/STUDY.md` + `ChapterNN-Study.pdf` | One chapter, Study Edition. |
| `chapters/NN-name/README.md` + `ChapterNN-Notes.pdf` | One chapter, Full Edition. |
| `chapters/NN-name/code/` | Runnable Java for that chapter (package per topic/case, each with a `Main`). |
| `assets/` | Course diagrams. |
| `capstones/CAPSTONES.md` + `capstones/LLD-Capstone-Projects.pdf` | **Capstone Projects**: 40 build-it-yourself design briefs, beginner to advanced (parking lot to order-matching engine, KV store, DI container), each with class/state diagrams, milestones, deep dives and interview Q&A, in Java 25. |
| `tools/` | `regen_pdfs.sh` (all PDFs), `compile_all.sh` (compile + run every Main), `md2pdf.mjs`. |

## Chapters
| # | Chapter |
|---|---|
| **Part I — Foundations** | |
| 01 | [The LLD interview & how to approach it](chapters/01-lld-interview-approach/README.md) |
| 02 | [OOP fundamentals](chapters/02-oop-fundamentals/README.md) |
| 03 | [Class relationships & UML](chapters/03-relationships-and-uml/README.md) |
| 04 | [SOLID principles](chapters/04-solid-principles/README.md) |
| 05 | [More design principles (DRY, KISS, YAGNI, Demeter, GRASP, Domain-Driven Design…)](chapters/05-design-principles/README.md) |
| 06 | [Clean Code](chapters/06-clean-code/README.md) |
| 07 | [Refactoring (all smells + full catalog)](chapters/07-refactoring/README.md) |
| **Part II — Design Patterns** | |
| 08 | [Introduction & creational patterns](chapters/08-patterns-creational/README.md) |
| 09 | [Structural patterns](chapters/09-patterns-structural/README.md) |
| 10 | [Behavioral patterns (1)](chapters/10-patterns-behavioral-1/README.md) |
| 11 | [Behavioral patterns (2) & beyond GoF](chapters/11-patterns-behavioral-2/README.md) |
| **Part III — Concurrency** | |
| 12 | [Concurrency & multithreading for LLD](chapters/12-concurrency-concepts/README.md) |
| 13 | [Classic concurrency problems](chapters/13-concurrency-problems/README.md) |
| **Part IV — Case Studies** | |
| 14 | [Everyday objects: parking lot, vending/coffee machine, ATM, elevator, traffic signal, shipping locker](chapters/14-case-everyday-objects/README.md) |
| 15 | [Booking 1: library, BookMyShow, concerts, hotel](chapters/15-case-booking-1/README.md) |
| 16 | [Booking 2: car rental, restaurant, airline, course registration](chapters/16-case-booking-2/README.md) |
| 17 | [Games: blackjack, chess, tic-tac-toe, snake & ladder, 2048](chapters/17-case-games/README.md) |
| 18 | [Platforms 1: Stack Overflow, Cricinfo, LinkedIn, Facebook](chapters/18-case-platforms-1/README.md) |
| 19 | [Platforms 2: Amazon, stock brokerage/exchange, auction, Spotify, Swiggy/FoodKart, grocery store POS](chapters/19-case-platforms-2/README.md) |
| 20 | [Components: Splitwise, notifications, cache/LRU, Uber, logger, pub-sub, rate limiter](chapters/20-case-components/README.md) |
| 21 | [Machine-coding round problems (task manager, wallet, calendar, Coding Blox, PropertyHunt, Ledger Co, inventory, Unix `find`)](chapters/21-case-machine-coding/README.md) |
| **Part V — Revision** | |
| 22 | [Cheat sheets & glossary](chapters/22-cheatsheets-glossary/README.md) |

## Rebuild
```bash
bash tools/compile_all.sh    # compile + run every example
bash tools/regen_pdfs.sh     # rebuild all PDFs
```
