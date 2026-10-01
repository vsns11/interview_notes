# System Design Patterns — Study Notes

Condensed study notes for the DesignGurus course **System Design Patterns: From Fundamentals to Real Systems** (Arslan Ahmad), extended into **18 modules**: the course's 12 modules and 93 lessons (60+ patterns), **5 added lessons** in modules 6, 8, 9 and 10 (multi-region and cells, authorization, containers and Kubernetes, CI/CD, inside Kafka), and **6 added case-study modules (13–18)** with 29 worked designs plus two primers (how money moves; the client framework). Every pattern lesson uses the same layout: problem → how it works → key numbers → trade-offs → when to use it → a 30-second interview one-liner; every case study goes requirements → estimates → API and data model → architecture → deep dives → failure walk → trade-offs → follow-ups. The course's own diagrams are embedded, and redrawn Mermaid diagrams cover the rest. Added lessons and modules are written from primary sources (papers, official docs, engineering blogs) cited at the end of each.

> Personal study notes. The embedded diagrams belong to DesignGurus; do not redistribute.

## What's in here

| Path | Purpose |
|---|---|
| `System-Design-Patterns-Notes.pdf` | **The whole book in one PDF**: cover, clickable contents, all 18 modules. |
| `interview/System-Design-Patterns-Interview-Edition.pdf` | **Interview Edition** (~63 pages): one compact card per pattern and per case study, the interview framework, back-of-envelope numbers, top questions, gotchas and a pattern-selection table for fast revision (source: `interview/INTERVIEW.md`). |
| `capstones/CAPSTONES.md` + `capstones/System-Design-Patterns-Capstone-Projects.pdf` | **Capstone Projects**: 40 build-it-yourself design briefs, beginner to advanced (URL shortener and webhook delivery up to a Dynamo-style KV store, Kafka-like log, multi-region ledger and AI copilot platform), each with requirements, architecture, data model, milestones, hard parts, testing and interview questions. |
| `modules/NN-name/README.md` | One module's notes (Markdown, renders on GitHub/VS Code). |
| `modules/NN-name/ModuleNN-Notes.pdf` | That module's notes as a standalone PDF. |
| `modules/NN-name/images/` | The course's original diagrams for that module (modules 01–12). |
| `modules/NN-name/code/` | Small runnable Python demos for the case-study modules (13–17), e.g. Snowflake IDs, geohash, ledger, order book. |
| `tools/` | The PDF build (`md2pdf.mjs`, `regen_pdfs.sh`); see `tools/README.md`. |

## Modules

| # | Module | Patterns |
|---|---|---|
| 01 | [Thinking in Patterns](modules/01-thinking-in-patterns/README.md) | Five Questions, back-of-envelope math |
| 02 | [Moving Data](modules/02-moving-data/README.md) | Request-response, message queue, pub/sub, EDA, webhooks, SSE, bidirectional streaming |
| 03 | [Storing Data](modules/03-storing-data/README.md) | Primary-replica, sharding, consistent hashing, WAL, event sourcing, CQRS |
| 04 | [Caching](modules/04-caching/README.md) | Cache-aside, read-through, write-through, write-behind, stampede prevention |
| 05 | [Reliability](modules/05-reliability/README.md) | Timeout, retry + backoff, idempotency, circuit breaker, bulkhead, DLQ, graceful degradation |
| 06 | [Scaling](modules/06-scaling/README.md) | Vertical, horizontal, load balancing, auto-scaling, connection pooling + added: multi-region & cell-based architecture (DR strategies, static stability, shuffle sharding, cloud building blocks) |
| 07 | [Consistency](modules/07-consistency/README.md) | 2PC, saga, quorum, vector clocks |
| 08 | [API and Edge](modules/08-api-and-edge/README.md) | Reverse proxy, CDN, API gateway, BFF, rate limiting, cursor pagination, versioning, sidecar, service mesh + added: authorization service (RBAC/ABAC/ReBAC, Zanzibar-style tuples) |
| 09 | [Operating in Production](modules/09-operating-in-production/README.md) | Health checks, distributed tracing, blue-green, canary, feature flags + added: containers & Kubernetes, CI/CD pipelines & deployment strategies |
| 10 | [Batch and Streaming](modules/10-batch-and-streaming/README.md) | MapReduce, stream processing, Lambda/Kappa, CDC, exactly-once, backpressure, partitioned consumption + added: inside Kafka (segments, ISR, acks, consumer groups, transactions) |
| 11 | [AI-Era Systems](modules/11-ai-era-systems/README.md) | Feature store, model serving, GPU auto-scaling, LLM gateway, semantic caching, vector DB sharding, RAG |
| 12 | [Capstones](modules/12-capstones/README.md) | News feed, ride-sharing, payments, AI copilot + **pattern cheat sheet** |
| 13 | [Case Studies: Building Blocks](modules/13-case-studies-building-blocks/README.md) | Unique ID generator (Snowflake layout), URL shortener (full), distributed key-value store (Dynamo-style), distributed message queue (Kafka-style), web crawler, search autocomplete + runnable demos |
| 14 | [Case Studies: Messaging and Media](modules/14-case-studies-messaging-and-media/README.md) | Chat system (per-conversation sequencer, inbox cursors, presence), email service (SMTP spool, SPF/DKIM/DMARC, per-mailbox search), video platform (chunked transcoding DAG, ABR, tiered CDN), file sync (content-defined chunking, namespace journal, conflicts), live streaming (LL-HLS latency budget, request collapsing, 1+1 redundancy) + chunking demo |
| 15 | [Case Studies: Location and Real-Time](modules/15-case-studies-location-and-realtime/README.md) | Proximity service (geohash/quadtree/S2/H3), nearby friends (pub/sub fan-out), maps & navigation (tiles, routing, ETA), real-time leaderboard (sorted sets) + runnable demos |
| 16 | [Case Studies: Infrastructure](modules/16-case-studies-infrastructure/README.md) | S3-like object storage, metrics monitoring & alerting, distributed job scheduler, distributed counter, collaborative editor (OT vs CRDT) |
| 17 | [Case Studies: Money and Reservations](modules/17-case-studies-money-and-reservations/README.md) | "How money moves" primer (cards, ACH, wires, real-time rails, settlement, reconciliation), hotel reservation (last-room concurrency, overbooking), digital wallet (double-entry ledger, cross-shard sagas, exactly-once), stock exchange (order book, sequencer, matching engine) + runnable demos |
| 18 | [Client and Mobile System Design](modules/18-client-and-mobile-system-design/README.md) | Client framework (layers, offline-first & sync, caching, pagination, push, background work, battery/data, releases & flags, observability) + client-side case studies: news feed, chat, stock trading, hotel reservation, file sync, video streaming |

## Rebuild the PDFs

```bash
bash tools/regen_pdfs.sh
```
