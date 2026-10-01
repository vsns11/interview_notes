# Designing Data-Intensive Applications — Study Notes

Beginner-friendly notes for Martin Kleppmann's *Designing Data-Intensive Applications*. The notes cover every chapter, section and sub-section in the book's order, and every topic listed in the book's Index for that chapter. The explanations are in plain English, and the facts (numbers, systems, examples) are kept as the book states them.

> Personal study notes written from the book. Figure numbers ("Figure 5-2, redrawn") refer to the book, so you can cross-check against your copy. Not for redistribution.

## What's in here

| Path | Purpose |
|---|---|
| `DDIA-Notes.pdf` | **The whole book in one PDF**: cover, clickable contents, preface through glossary. |
| `interview/DDIA-Interview-Edition.pdf` | **Interview Edition** (~18k words, 38 pages): every chapter's must-know terms, comparison tables, top questions and gotchas for fast revision. Source: `interview/INTERVIEW.md`. |
| `chapters/NN-name/README.md` | One chapter's notes (Markdown). |
| `chapters/NN-name/ChapterNN-Notes.pdf` | That chapter as a standalone PDF. |
| `capstones/CAPSTONES.md` + `capstones/DDIA-Capstone-Projects.pdf` | **Capstone Projects**: 40 build-it-yourself design briefs, beginner to advanced — Bitcask, LSM-tree, B-tree with WAL, Raft, Dynamo-style quorums, SSI, CDC to Kafka, event-time stream processing, CRDTs and more. |
| `tools/` | PDF build (`md2pdf.mjs`, `regen_pdfs.sh`); same dark theme as the course notes. |

## Each chapter contains
Overview (with analogy) → Key terms → every book section and sub-section explained step by step → redrawn figures → own code/query examples → Chapter summary → Interview angle (30-second answers + follow-ups) → Self-check (Q → A).

## Chapters

| # | Chapter | Part |
|---|---|---|
| 00 | [Preface: how to read this book](chapters/00-preface/README.md) | |
| 01 | [Reliable, Scalable, and Maintainable Applications](chapters/01-reliable-scalable-maintainable/README.md) | I · Foundations of Data Systems |
| 02 | [Data Models and Query Languages](chapters/02-data-models-and-query-languages/README.md) | I |
| 03 | [Storage and Retrieval](chapters/03-storage-and-retrieval/README.md) | I |
| 04 | [Encoding and Evolution](chapters/04-encoding-and-evolution/README.md) | I |
| 05 | [Replication](chapters/05-replication/README.md) | II · Distributed Data |
| 06 | [Partitioning](chapters/06-partitioning/README.md) | II |
| 07 | [Transactions](chapters/07-transactions/README.md) | II |
| 08 | [The Trouble with Distributed Systems](chapters/08-trouble-with-distributed-systems/README.md) | II |
| 09 | [Consistency and Consensus](chapters/09-consistency-and-consensus/README.md) | II |
| 10 | [Batch Processing](chapters/10-batch-processing/README.md) | III · Derived Data |
| 11 | [Stream Processing](chapters/11-stream-processing/README.md) | III |
| 12 | [The Future of Data Systems](chapters/12-future-of-data-systems/README.md) | III |
| 13 | [Glossary](chapters/13-glossary/README.md) | |

## Rebuild the PDFs
```bash
bash tools/regen_pdfs.sh
```
