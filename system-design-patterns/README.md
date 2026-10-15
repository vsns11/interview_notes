# System Design Patterns — Study Notes

Condensed study notes for the DesignGurus course **System Design Patterns: From Fundamentals to Real Systems** (Arslan Ahmad), extended into **27 modules (00–26)**: a new **Module 00 — System Design Fundamentals** (every basic concept and term defined from zero: latency, throughput, load balancers and all their types, DNS, CDNs, caching, databases, replication, sharding, queues, consistency, CAP, security and more, with a 300-term glossary), the course's 12 modules and 93 lessons (60+ patterns), **5 added lessons** in modules 6, 8, 9 and 10 (multi-region and cells, authorization, containers and Kubernetes, CI/CD, inside Kafka), and **6 added case-study modules (13–18)**, an **Algorithms Deep Dive (Module 19)** with 30+ algorithms explained step by step, **two more case-study modules (20–21)** with 10 designs (search, logging, Quora, Instagram, Twitter, ad-click aggregation, LeetCode, deployment system, ChatGPT-style service, cache cluster) with 29 worked designs plus two primers (how money moves; the client framework), and **four modules added to cover *Grokking the Modern System Design Interview*** (23: CDN, rate limiter and pub-sub as full cases; 24: AI code assistant, LLM support bot and ML data platform; 25: TikTok, food delivery, e-commerce and Prime Day, online education, Spotify Wrapped, Twilio-style platform; 26: real outage postmortems). Every pattern lesson uses the same layout: problem → how it works → key numbers → trade-offs → when to use it → a 30-second interview one-liner; every case study goes requirements → estimates → API and data model → architecture → deep dives → failure walk → trade-offs → follow-ups. The course's own diagrams are embedded, and redrawn Mermaid diagrams cover the rest. Added lessons and modules are written from primary sources (papers, official docs, engineering blogs) cited at the end of each.

> Personal study notes. The embedded diagrams belong to DesignGurus; do not redistribute.

## What's in here

| Path | Purpose |
|---|---|
| `System-Design-Patterns-Vol1.pdf` | **Vol 1 — Fundamentals and Algorithms** (Modules 0 and 19): every basic concept from zero, then the algorithms step by step. |
| `System-Design-Patterns-Vol2.pdf` | **Vol 2 — The Patterns** (Modules 1–11: the five-questions method, then every pattern). |
| `System-Design-Patterns-Vol3.pdf` | **Vol 3 — Case Studies I** (Modules 12–18 and 20). |
| `System-Design-Patterns-Vol4.pdf` | **Vol 4 — Case Studies II and Interview Practice** (Modules 21 and 23–25, Module 26's outage postmortems, then the Module 22 interview question bank with prep plans). Each volume is under 350 pages, with a cover and clickable contents. |
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
| 22 | [Interview Question Bank](modules/22-interview-question-bank/README.md) | 219 most-asked questions: 111 "Design X" prompts (Q213–Q219 added at the end of Part B) and 108 concept questions with short answers, a 45-minute answer plan, and Part D on preparing: RESHADED mapping, do's and don'ts, traps, 4/8/12-week plans, mock-interview rubric, AI-era interviews, API-design rounds, level expectations |
| 23 | [Case Studies: Edge and Messaging Building Blocks](modules/23-case-studies-building-blocks-ii/README.md) | CDN (request routing, in-PoP hashing, tiers, sequenced purge log, edge compute), rate limiter as a shared service (GCRA, atomic scripts, token leasing, multi-region budgets), pub-sub service (store once, ack ranges, push windows, ordering keys) |
| 24 | [Case Studies: AI Systems](modules/24-case-studies-ai-systems/README.md) | How AI changed design constants; AI code assistant (250 ms loop, FIM, prefix cache, speculative decoding, Merkle-synced index), LLM customer support bot (state machine, handoff, PII, guardrails, eval loop), data infrastructure for ML (lakehouse, labeling, dataset versions, GPU loaders, drift) |
| 25 | [Case Studies: Commerce, Media and Platforms](modules/25-case-studies-commerce-media-platforms/README.md) | TikTok-style feed, food delivery, e-commerce with Prime Day readiness, online education, Spotify Wrapped, Twilio-style communications API |
| 26 | [When Systems Fail: Postmortems as Patterns](modules/26-postmortems-when-systems-fail/README.md) | Postmortem anatomy and blameless culture; Facebook 2021, AWS Kinesis 2020, AWS us-east-1 2021 and 2025, GitLab 2017, Cloudflare 2019, CrowdStrike 2024; lessons across outages (in Vol 2) |

## Course coverage map: Grokking the Modern System Design Interview

Where each lesson of Educative's *Grokking the Modern System Design Interview* (course outline as of October 2026; titles only) is explained in this book. Lessons inside one chapter that map to the same place share a row; quizzes are omitted.

**Introduction; System Design Interviews**

| Course lesson | Where in this book |
|---|---|
| Introduction · Why every developer should learn system design · Course structure | n/a (course meta, interactive or marketing) |
| Getting ready for the system design interview | [22 · Part C](modules/22-interview-question-bank/README.md#part-c--how-to-answer-a-system-design-question-in-45-minutes); [00 · How to approach a system design interview](modules/00-system-design-fundamentals/README.md#how-to-approach-a-system-design-interview) |
| The do's and don'ts | [22 · Do's and don'ts per phase](modules/22-interview-question-bank/README.md#dos-and-donts-per-phase) |
| Interview trap: why engineers fail and succeed | [22 · Why engineers fail: trap patterns](modules/22-interview-question-bank/README.md#why-engineers-fail-trap-patterns) |
| How long does it take to prepare? | [22 · 4-, 8- and 12-week prep plans](modules/22-interview-question-bank/README.md#4--8--and-12-week-prep-plans) |
| System design mock interviews | [22 · Mock-interview protocol and self-scoring rubric](modules/22-interview-question-bank/README.md#mock-interview-protocol-and-self-scoring-rubric) |
| Let AI evaluate your preparation | n/a (course meta, interactive or marketing) |

**Preliminary concepts; Non-functional characteristics; Back-of-the-envelope**

| Course lesson | Where in this book |
|---|---|
| Abstractions in system design | [00 · What system design is](modules/00-system-design-fundamentals/README.md#what-system-design-is); [01 · Thinking in Patterns](modules/01-thinking-in-patterns/README.md) |
| Network abstractions: RPC | [00 · RPC internals: stubs, marshalling, binding and call semantics](modules/00-system-design-fundamentals/README.md#rpc-internals-stubs-marshalling-binding-and-call-semantics) |
| Spectrum of consistency models | [00 · Consistency models](modules/00-system-design-fundamentals/README.md#consistency-models); [00 · CAP theorem and PACELC](modules/00-system-design-fundamentals/README.md#cap-theorem-and-pacelc) |
| Spectrum of failure models | [00 · Faults, failures and fault tolerance (failure models)](modules/00-system-design-fundamentals/README.md#faults-failures-and-fault-tolerance) |
| Availability | [00 · Availability and the nines](modules/00-system-design-fundamentals/README.md#availability-and-the-nines) |
| Reliability | [00 · MTBF, MTTR and measuring failure](modules/00-system-design-fundamentals/README.md#mtbf-mttr-and-measuring-failure); [00 · Reliability, availability and durability](modules/00-system-design-fundamentals/README.md#reliability-availability-and-durability) |
| Scalability | [00 · Scalability](modules/00-system-design-fundamentals/README.md#scalability); [06 · Scaling](modules/06-scaling/README.md) |
| Maintainability | [00 · NFR levers (maintainability)](modules/00-system-design-fundamentals/README.md#non-functional-requirements-and-the-levers-that-meet-them) |
| Fault tolerance | [00 · Redundancy, single points of failure and failover](modules/00-system-design-fundamentals/README.md#redundancy-single-points-of-failure-and-failover); [05 · Reliability](modules/05-reliability/README.md) |
| Non-functional requirements for interviews | [00 · Non-functional requirements and the levers that meet them](modules/00-system-design-fundamentals/README.md#non-functional-requirements-and-the-levers-that-meet-them) |
| Let AI evaluate NFRs | n/a (course meta, interactive or marketing) |
| Put back-of-the-envelope numbers in perspective | [00 · Latency numbers every engineer should know](modules/00-system-design-fundamentals/README.md#latency-numbers-every-engineer-should-know); [00 · Units, powers of two and data sizes](modules/00-system-design-fundamentals/README.md#units-powers-of-two-and-data-sizes) |
| Examples of resource estimation | [00 · Back-of-the-envelope estimation](modules/00-system-design-fundamentals/README.md#back-of-the-envelope-estimation); [00 · Worked example: how many servers do we need?](modules/00-system-design-fundamentals/README.md#worked-example-how-many-servers-do-we-need); [01 · Back-of-Envelope Math](modules/01-thinking-in-patterns/README.md#back-of-envelope-math) |

**Building blocks**

| Course lesson | Where in this book |
|---|---|
| Introduction to building blocks | [00 · The building blocks of a web application](modules/00-system-design-fundamentals/README.md#the-building-blocks-of-a-web-application) |
| Domain Name System (2) | [00 · DNS — the internet's phone book](modules/00-system-design-fundamentals/README.md#dns--the-internets-phone-book) |
| Load balancers (3) | [00 · Load balancers](modules/00-system-design-fundamentals/README.md#load-balancers); [06 · Load Balancing](modules/06-scaling/README.md#load-balancing) |
| Databases (5) | [00 · Replication](modules/00-system-design-fundamentals/README.md#replication); [00 · Partitioning and sharding](modules/00-system-design-fundamentals/README.md#partitioning-and-sharding); [00 · Choosing a database](modules/00-system-design-fundamentals/README.md#choosing-a-database) |
| Key-value store (5) | [13 · Case 3 — Design a Distributed Key-Value Store](modules/13-case-studies-building-blocks/README.md#case-3--design-a-distributed-key-value-store) |
| Content delivery network (7) | [23 · Case 1 — Design a CDN](modules/23-case-studies-building-blocks-ii/README.md#case-1--design-a-cdn) |
| Sequencer; unique IDs with causality | [13 · Case 1 — Design a Unique ID Generator](modules/13-case-studies-building-blocks/README.md#case-1--design-a-unique-id-generator); [19 · Hybrid logical clocks](modules/19-algorithms-deep-dive/README.md#hybrid-logical-clocks) |
| Distributed monitoring; monitor server-side errors (6) | [16 · Design 2 — Metrics Monitoring & Alerting](modules/16-case-studies-infrastructure/README.md#design-2--metrics-monitoring--alerting) |
| Monitor client-side errors (2) | [16 · Design 2 (client-side errors subsection)](modules/16-case-studies-infrastructure/README.md#design-2--metrics-monitoring--alerting); [20 · Case 2 — Distributed Logging and Error Monitoring](modules/20-case-studies-content-search-and-platforms/README.md#case-2--distributed-logging-and-error-monitoring) |
| Distributed cache (5); Memcached versus Redis | [21 · Case 4 — Distributed Cache Cluster (Redis Cluster / Memcached-like)](modules/21-case-studies-developer-and-ai-platforms/README.md#case-4--distributed-cache-cluster-redis-cluster--memcached-like) |
| Distributed messaging queue (7) | [13 · Case 4 — Design a Distributed Message Queue](modules/13-case-studies-building-blocks/README.md#case-4--design-a-distributed-message-queue); [10 · Inside Kafka](modules/10-batch-and-streaming/README.md#inside-kafka) |
| Pub-sub (3) | [23 · Case 3 — Design a Pub-Sub Service](modules/23-case-studies-building-blocks-ii/README.md#case-3--design-a-pub-sub-service) |
| Rate limiter (5) | [23 · Case 2 — Design a Rate Limiter](modules/23-case-studies-building-blocks-ii/README.md#case-2--design-a-rate-limiter) |
| Blob store (6) | [16 · Design 1 — S3-like Object Storage](modules/16-case-studies-infrastructure/README.md#design-1--s3-like-object-storage) |
| Distributed search (6) | [20 · Case 1 — Distributed Search (Twitter/Elasticsearch-style)](modules/20-case-studies-content-search-and-platforms/README.md#case-1--distributed-search-twitterelasticsearch-style) |
| Distributed logging (3) | [20 · Case 2 — Distributed Logging and Error Monitoring](modules/20-case-studies-content-search-and-platforms/README.md#case-2--distributed-logging-and-error-monitoring) |
| Distributed task scheduler (5) | [16 · Design 3 — Distributed Job Scheduler](modules/16-case-studies-infrastructure/README.md#design-3--distributed-job-scheduler) |
| Sharded counters (4) | [16 · Design 4 — Distributed Counter (likes and views at scale)](modules/16-case-studies-infrastructure/README.md#design-4--distributed-counter-likes-and-views-at-scale) |
| AI evaluation: building blocks in an e-commerce platform | [25 · Case 3 — E-Commerce Platform and Peak-Day (Prime Day) Readiness](modules/25-case-studies-commerce-media-platforms/README.md#case-3--e-commerce-platform-and-peak-day-prime-day-readiness) |
| AI evaluation: building blocks in an online education system | [25 · Case 4 — Online Education Platform (Coursera / Udemy style)](modules/25-case-studies-commerce-media-platforms/README.md#case-4--online-education-platform-coursera--udemy-style) |
| Wrapping up the building blocks | [13 · Pattern Review (building blocks)](modules/13-case-studies-building-blocks/README.md#pattern-review-building-blocks) |
| The RESHADED approach | [22 · Our method vs the RESHADED-style checklist](modules/22-interview-question-bank/README.md#our-method-vs-the-reshaded-style-checklist) |

**Design problems**

| Course lesson | Where in this book |
|---|---|
| Design YouTube (5) | [14 · Case 3 — Video Platform (YouTube-like)](modules/14-case-studies-messaging-and-media/README.md#case-3--video-platform-youtube-like) |
| The reality is more complicated (YouTube) | [14 · What the real YouTube does differently](modules/14-case-studies-messaging-and-media/README.md#what-the-real-youtube-does-differently) |
| Design Quora (5) | [20 · Case 3 — Q&A Platform (Quora-like)](modules/20-case-studies-content-search-and-platforms/README.md#case-3--qa-platform-quora-like) |
| Design Google Maps (6) | [15 · Design 3 — Maps & Navigation](modules/15-case-studies-location-and-realtime/README.md#design-3--maps--navigation) |
| Design a proximity service / Yelp (5) | [15 · Design 1 — Proximity Service (Nearby Places)](modules/15-case-studies-location-and-realtime/README.md#design-1--proximity-service-nearby-places) |
| Design Uber (6) | [12 · Design 2 — Ride-Sharing Dispatch](modules/12-capstones/README.md#design-2--ride-sharing-dispatch); [15 · Design 2 — Nearby Friends (Real-Time Location Sharing)](modules/15-case-studies-location-and-realtime/README.md#design-2--nearby-friends-real-time-location-sharing) |
| Payment service and fraud detection in Uber | [12 · Design 2 (payments and fraud detection)](modules/12-capstones/README.md#design-2--ride-sharing-dispatch) |
| Design Twitter (5) | [20 · Case 5 — Microblogging (Twitter/X-like)](modules/20-case-studies-content-search-and-platforms/README.md#case-5--microblogging-twitterx-like) |
| Client-side load balancer for Twitter | [20 · Client-side load balancing at Twitter (Finagle, aperture)](modules/20-case-studies-content-search-and-platforms/README.md#client-side-load-balancing-at-twitter-finagle-aperture) |
| Design newsfeed system | [12 · Design 1 — The News Feed](modules/12-capstones/README.md#design-1--the-news-feed) |
| Design Instagram (5) | [20 · Case 4 — Photo Sharing (Instagram-like)](modules/20-case-studies-content-search-and-platforms/README.md#case-4--photo-sharing-instagram-like) |
| Design TinyURL | [13 · Case 2 — Design a URL Shortener](modules/13-case-studies-building-blocks/README.md#case-2--design-a-url-shortener) |
| Design a web crawler | [13 · Case 5 — Design a Web Crawler](modules/13-case-studies-building-blocks/README.md#case-5--design-a-web-crawler) |
| Design WhatsApp (6) | [14 · Case 1 — Chat System](modules/14-case-studies-messaging-and-media/README.md#case-1--chat-system) |
| Design typeahead suggestion (7) | [13 · Case 6 — Design Search Autocomplete](modules/13-case-studies-building-blocks/README.md#case-6--design-search-autocomplete) |
| Design Google Docs (5) | [16 · Design 5 — Collaborative Document Editor (Google Docs-style)](modules/16-case-studies-infrastructure/README.md#design-5--collaborative-document-editor-google-docs-style) |
| Design a deployment system (2) | [21 · Case 2 — Global Deployment System](modules/21-case-studies-developer-and-ai-platforms/README.md#case-2--global-deployment-system) |
| Design a payment system (2) | [12 · Design 3 — The Payment Platform](modules/12-capstones/README.md#design-3--the-payment-platform); [17 · How Money Moves (primer)](modules/17-case-studies-money-and-reservations/README.md#how-money-moves-primer) |
| Design a LeetCode system (2) | [21 · Case 1 — Online Judge (LeetCode-like)](modules/21-case-studies-developer-and-ai-platforms/README.md#case-1--online-judge-leetcode-like) |

**AI era**

| Course lesson | Where in this book |
|---|---|
| How AI reshaped system design and interviews; the AI design problems | [24 · How AI reshaped system design](modules/24-case-studies-ai-systems/README.md#how-ai-reshaped-system-design) |
| How roles and tools reshaped the interview | [22 · How AI changed system design interviews and roles](modules/22-interview-question-bank/README.md#how-ai-changed-system-design-interviews-and-roles) |
| Design a ChatGPT system (2) | [21 · Case 3 — Conversational AI Service (ChatGPT-like)](modules/21-case-studies-developer-and-ai-platforms/README.md#case-3--conversational-ai-service-chatgpt-like) |
| Design a data infrastructure system for AI/ML (3) | [24 · Case 3 — Data Infrastructure for AI/ML](modules/24-case-studies-ai-systems/README.md#case-3--data-infrastructure-for-aiml) |
| LLM-powered customer support bot (2) | [24 · Case 2 — LLM Customer Support Bot](modules/24-case-studies-ai-systems/README.md#case-2--llm-customer-support-bot) |
| AI-powered code assistant (2) | [24 · Case 1 — AI-Powered Code Assistant](modules/24-case-studies-ai-systems/README.md#case-1--ai-powered-code-assistant) |

**Lessons from system failures; Concluding remarks**

| Course lesson | Where in this book |
|---|---|
| Lessons from system failures | [26 · How to read a postmortem](modules/26-postmortems-when-systems-fail/README.md#how-to-read-a-postmortem); [26 · Patterns across all the outages](modules/26-postmortems-when-systems-fail/README.md#patterns-across-all-the-outages) |
| Facebook, WhatsApp, Instagram, Oculus outage | [26 · 2021 — Facebook BGP and DNS outage](modules/26-postmortems-when-systems-fail/README.md#2021--facebook-bgp-and-dns-outage) |
| AWS Kinesis outage | [26 · 2020 — AWS Kinesis outage (us-east-1)](modules/26-postmortems-when-systems-fail/README.md#2020--aws-kinesis-outage-us-east-1) |
| AWS widespread outage | [26 · 2021 — AWS us-east-1 internal network congestion](modules/26-postmortems-when-systems-fail/README.md#2021--aws-us-east-1-internal-network-congestion); [26 · 2025 — AWS us-east-1 DynamoDB DNS outage](modules/26-postmortems-when-systems-fail/README.md#2025--aws-us-east-1-dynamodb-dns-outage) |
| Conclusions; Resources | n/a (course meta, interactive or marketing) |

**Free lessons; Case studies; Mock interviews**

| Course lesson | Where in this book |
|---|---|
| All-in-one master template | [22 · Part C](modules/22-interview-question-bank/README.md#part-c--how-to-answer-a-system-design-question-in-45-minutes); [00 · Decision cheat sheet](modules/00-system-design-fundamentals/README.md#decision-cheat-sheet) |
| System design primer | [00 · System Design Fundamentals](modules/00-system-design-fundamentals/README.md) |
| Distributed systems considerations and trade-offs | [00 · CAP theorem and PACELC](modules/00-system-design-fundamentals/README.md#cap-theorem-and-pacelc); [07 · Consistency](modules/07-consistency/README.md) |
| Key concepts to prepare | [22 · Part B](modules/22-interview-question-bank/README.md#part-b--concept-questions) |
| Scalable systems 101 | [06 · Scaling](modules/06-scaling/README.md) |
| Best practices for low latency | [00 · Low-latency checklist](modules/00-system-design-fundamentals/README.md#low-latency-checklist) |
| Distributed system design patterns | Vol2: [02 · Moving Data](modules/02-moving-data/README.md) to [11 · AI-Era Systems](modules/11-ai-era-systems/README.md) |
| API design interview vs system design interview | [22 · API-design interview vs system design interview](modules/22-interview-question-bank/README.md#api-design-interview-vs-system-design-interview) |
| Does Google ask system design for L4? | [22 · Level expectations: mid, senior, staff](modules/22-interview-question-bank/README.md#level-expectations-mid-senior-staff) |
| System analysis vs design · TPM fundamentals · NeetCode Pro review · OOD guide · Interview PDF | n/a (course meta, interactive or marketing) |
| How Spotify Wrapped scales | [25 · Case 5 — Spotify Wrapped: a Yearly Batch Personalisation Pipeline](modules/25-case-studies-commerce-media-platforms/README.md#case-5--spotify-wrapped-a-yearly-batch-personalisation-pipeline) |
| How Amazon scales for Prime Day | [25 · Case 3, deep dive 5 (peak-day readiness)](modules/25-case-studies-commerce-media-platforms/README.md#case-3--e-commerce-platform-and-peak-day-prime-day-readiness) |
| How Twilio builds for scale | [25 · Case 6 — A Twilio-Style Communications API Platform](modules/25-case-studies-commerce-media-platforms/README.md#case-6--a-twilio-style-communications-api-platform) |
| Dropbox system design | [14 · Case 4 — File Sync and Storage (Google Drive / Dropbox-like)](modules/14-case-studies-messaging-and-media/README.md#case-4--file-sync-and-storage-google-drive--dropbox-like) |
| Ticketmaster system design | [17 · Design an Event Ticketing and Flash-Sale System (Ticketmaster-like)](modules/17-case-studies-money-and-reservations/README.md#design-an-event-ticketing-and-flash-sale-system-ticketmaster-like) |
| Mock interviews: distributed cache · pub-sub · blob store | [21 · Case 4 — Distributed Cache Cluster (Redis Cluster / Memcached-like)](modules/21-case-studies-developer-and-ai-platforms/README.md#case-4--distributed-cache-cluster-redis-cluster--memcached-like) · [23 · Case 3 — Design a Pub-Sub Service](modules/23-case-studies-building-blocks-ii/README.md#case-3--design-a-pub-sub-service) · [16 · Design 1 — S3-like Object Storage](modules/16-case-studies-infrastructure/README.md#design-1--s3-like-object-storage) |
| Mock interviews: TikTok · Uber Eats | [25 · Case 1 — TikTok-Style Short-Video Feed](modules/25-case-studies-commerce-media-platforms/README.md#case-1--tiktok-style-short-video-feed) · [25 · Case 2 — Food Delivery (Uber Eats / DoorDash style)](modules/25-case-studies-commerce-media-platforms/README.md#case-2--food-delivery-uber-eats--doordash-style) |
| Mock interviews: newsfeed · Facebook Messenger · ChatGPT | [12 · Design 1 — The News Feed](modules/12-capstones/README.md#design-1--the-news-feed) · [14 · Case 1 — Chat System](modules/14-case-studies-messaging-and-media/README.md#case-1--chat-system) · [21 · Case 3 — Conversational AI Service (ChatGPT-like)](modules/21-case-studies-developer-and-ai-platforms/README.md#case-3--conversational-ai-service-chatgpt-like) |

## Rebuild the PDFs

```bash
bash tools/regen_pdfs.sh
```
