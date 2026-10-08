# System Design Patterns — Study Notes

Condensed study notes for the DesignGurus course **System Design Patterns: From Fundamentals to Real Systems** (Arslan Ahmad), extended into **22 modules**: a new **Module 00 — System Design Fundamentals** (every basic concept and term defined from zero: latency, throughput, load balancers and all their types, DNS, CDNs, caching, databases, replication, sharding, queues, consistency, CAP, security and more, with a 300-term glossary), the course's 12 modules and 93 lessons (60+ patterns), **5 added lessons** in modules 6, 8, 9 and 10 (multi-region and cells, authorization, containers and Kubernetes, CI/CD, inside Kafka), and **6 added case-study modules (13–18)**, an **Algorithms Deep Dive (Module 19)** with 30+ algorithms explained step by step, **two more case-study modules (20–21)** with 10 designs (search, logging, Quora, Instagram, Twitter, ad-click aggregation, LeetCode, deployment system, ChatGPT-style service, cache cluster) with 29 worked designs plus two primers (how money moves; the client framework). Every pattern lesson uses the same layout: problem → how it works → key numbers → trade-offs → when to use it → a 30-second interview one-liner; every case study goes requirements → estimates → API and data model → architecture → deep dives → failure walk → trade-offs → follow-ups. The course's own diagrams are embedded, and redrawn Mermaid diagrams cover the rest. Added lessons and modules are written from primary sources (papers, official docs, engineering blogs) cited at the end of each.

> Personal study notes. The embedded diagrams belong to DesignGurus; do not redistribute.

## What's in here

| Path | Purpose |
|---|---|
| `System-Design-Patterns-Vol1.pdf` | **Vol 1 — Fundamentals and Algorithms** (Modules 0 and 19): every basic concept from zero, then the algorithms step by step. |
| `System-Design-Patterns-Vol2.pdf` | **Vol 2 — The Patterns** (Modules 1–11). |
| `System-Design-Patterns-Vol3.pdf` | **Vol 3 — Interview Method and Case Studies** (Module 1's five-questions method, then Modules 12–18, 20 and 21). Each volume is under 600 pages, with a cover and clickable contents. |
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
| 00 | [System Design Fundamentals](modules/00-system-design-fundamentals/README.md) | **Start here if you are new.** Clients/servers, latency, throughput, percentiles, estimation, availability and SLAs, scaling, networking (DNS, TCP/UDP, HTTP, TLS, REST/gRPC, WebSockets), proxies and API gateways, load balancers (L4/L7, every algorithm), CDNs, databases, ACID, indexes, replication, sharding, consistent hashing, caching, queues and streams, consistency models, CAP/PACELC, consensus, microservices, resilience, rate limiting, Kubernetes, observability, disaster recovery, security, plus a 300-term A–Z glossary |
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
| 19 | [Algorithms Deep Dive](modules/19-algorithms-deep-dive/README.md) | Paxos/Multi-Paxos vs Raft/ZAB, 2PC vs 3PC, leases, phi-accrual and SWIM, chain replication/CRAQ, hybrid logical clocks, TrueTime, version vectors; jump/rendezvous/Maglev hashing and bounded loads; HyperLogLog, count-min, heavy hitters, reservoir sampling, t-digest, trending; LSM compaction, B+-trees, MVCC, 2PL/SSI, skip lists, CLOCK/ARC/TinyLFU, timing wheels, Chandy-Lamport; inverted index, BM25, SimHash/MinHash/LSH, HNSW/IVF/PQ, Dijkstra/A*/contraction hierarchies, feed ranking |
| 20 | [Case Studies: Content, Search and Platforms](modules/20-case-studies-content-search-and-platforms/README.md) | Distributed search, distributed logging and error monitoring, Quora, Instagram, Twitter/X (ranked timeline, trending), ad-click aggregation |
| 21 | [Case Studies: Developer and AI Platforms](modules/21-case-studies-developer-and-ai-platforms/README.md) | Online judge (LeetCode), global deployment system, ChatGPT-style AI service, distributed cache cluster |

## Rebuild the PDFs

```bash
bash tools/regen_pdfs.sh
```
