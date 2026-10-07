# Spring — Capstone Projects

## How to use this book

A **capstone** is a project big enough to force you to combine several chapters at once: the container, configuration, the web layer, data access, security, tests and operations. Reading a chapter teaches you a tool. Building a capstone teaches you when to reach for it, how it breaks, and how to explain your choice in an interview. This book holds **40 design briefs**, ordered from a weekend REST API to a multi-tenant SaaS with its own identity provider.

Every brief targets the same stack as the handbook: **Spring Boot 4.1.1** (which brings **Spring Framework 7**, Spring Security 7 and the Spring Data 2026.0 train), **Java 25**, and **Jakarta EE 11** (Servlet 6.1, JPA 3.2, Bean Validation 3.1). Projects that need libraries outside the Boot BOM (Spring Cloud 2025.1 "Oakwood", Spring AI 2.0, Spring Modulith 2.1, Resilience4j, Bucket4j, ShedLock) say so. Where a brief relies on something new in Boot 4 or Framework 7 — API versioning, HTTP service client groups, `@Retryable`/`@ConcurrencyLimit`, `RestTestClient`, Authorization Server inside Spring Security — it is labelled, and the handbook chapter that proves it is linked.

**How to pick a project.**
- New to Spring: start with projects 1–5 in order. Each adds one idea (REST, persistence, outbound HTTP, JPA, configuration).
- Comfortable with Boot 3: jump to the projects tagged with a Boot 4 / Framework 7 feature (3, 21, 33, 34) and to the testing suite (20) to learn the new APIs on something real.
- Preparing for a back-end interview: pick one project from each band and finish all milestones — interviewers ask about depth on one system much more than about breadth.
- Preparing for a senior or architect interview: do 26–40, especially the trade-off sections, and practise the "In the interview" answers aloud.

**How to use a brief.**
1. Read *What it is*, *The problem it solves* and *Requirements*. Write the non-functional numbers on a sticky note — they decide most design choices.
2. Sketch the architecture yourself before reading the diagram, then compare.
3. Build the **milestones in order**. Each has a "done when" check: do not move on until it is true and covered by a test.
4. Read *Deep dive* when you reach the hard milestone, not before — it will make more sense once you have hit the problem.
5. Only then attempt the **stretch goals**.
6. Finish by answering *In the interview* without looking, and by writing a one-page README for your project (what, why, how to run, trade-offs).

**Using these projects in interviews.** Lead with the 30-second answer: what you built, the key design decision, the hardest problem and one concrete number. Know every term under *Key terms to know* well enough to define it in one sentence and point to where it appears in your build. Practise the Q&As out loud until the answers sound like your own words, not memorised text. Quote your own numbers — latency, data size, test counts, startup time — from the build, not from this book. Finally, expect the two follow-ups every interviewer asks: "what would you change at 10x?" and "what breaks first?" Each project's Q&As include both.

**Colour code (in the PDF).** Section headings are coloured by role, the same way as in the handbook: green for **What it is**, coral for **The problem it solves**, violet for **Real-world examples**, amber for **Trade-offs**, pink for **In the interview**, teal for **Self-check** and a yellow box for **TL;DR**. Requirements, architecture and deep-dive headings use the blue "how it works" colour.

**Level legend.**

| Level | You should already know | Typical size |
|---|---|---|
| Beginner | Java basics, one Spring Boot "hello" app (Ch 1) | 1–2 weekends, one service, H2 or PostgreSQL |
| Intermediate | Ch 1–8 and some of 9–14 | 1–3 weeks, one service plus a broker, cache or IdP |
| Advanced | most of the handbook | 2–4 weeks, several services or hard consistency, security or performance goals |

**A note on what you get.** The briefs describe **what** to build and **why** each piece is there. They deliberately contain almost no code: you write it. When a brief names a class or property, look it up in the linked chapter, read the Javadoc, and write a test that proves it behaves the way the brief claims. That is the habit the handbook teaches, and it is the habit interviewers look for.

## Project map

| # | Project | Level | Time | Prerequisites (chapters) |
|---|---|---|---|---|
| 1 | Notes REST API | Beginner | 1 weekend | Ch 1, 5, 12 |
| 2 | URL Shortener | Beginner | 1 weekend | Ch 5, 7 |
| 3 | Weather Client with RestClient and HTTP Interfaces | Beginner | 1 weekend | Ch 3, 6 |
| 4 | Todo App with Spring Data JPA | Beginner | 1–2 weekends | Ch 5, 8 |
| 5 | Config-driven Greeting Service | Beginner | 1 weekend | Ch 2, 3 |
| 6 | Scheduled Daily Sales Report | Beginner | 1 weekend | Ch 7, 14 |
| 7 | Bookstore Catalog API with Problem Details and OpenAPI | Beginner | 1–2 weekends | Ch 5, 8 |
| 8 | Expense Tracker with JdbcClient and Flyway | Beginner | 1–2 weekends | Ch 7, 8 |
| 9 | Audit-log Spring Boot Starter | Beginner | 1–2 weekends | Ch 2, 3, 4 |
| 10 | Uptime Monitor | Beginner | 1–2 weekends | Ch 6, 14, 15 |
| 11 | Blog Platform with Spring Security and JWT | Intermediate | 2 weeks | Ch 8, 10, 11 |
| 12 | Photo Journal with OAuth2 Login and a Resource Server | Intermediate | 2 weeks | Ch 10, 11 |
| 13 | E-commerce Cart and Checkout | Intermediate | 2–3 weeks | Ch 7, 8 |
| 14 | Product Catalog Cache with Redis | Intermediate | 1–2 weeks | Ch 9, 15 |
| 15 | File Upload and Media Service | Intermediate | 2 weeks | Ch 5, 6, 14 |
| 16 | Event-driven Notifications with Kafka | Intermediate | 2–3 weeks | Ch 14, 12 |
| 17 | Image Job Queue with RabbitMQ | Intermediate | 2 weeks | Ch 14 |
| 18 | Bulk CSV Import with Spring Batch | Intermediate | 2 weeks | Ch 7, 14 |
| 19 | Live Dashboard with WebFlux and SSE | Intermediate | 2 weeks | Ch 13 |
| 20 | Testcontainers Integration Test Suite | Intermediate | 1–2 weeks | Ch 12, 9, 14 |
| 21 | Versioned Public API (Framework 7) | Intermediate | 1–2 weeks | Ch 5, 6 |
| 22 | Rate-limited API with Bucket4j and Redis | Intermediate | 1–2 weeks | Ch 9, 11 |
| 23 | Concert Seat Reservation | Intermediate | 2 weeks | Ch 7, 8 |
| 24 | Webhook Delivery Service | Intermediate | 2–3 weeks | Ch 4, 6, 14 |
| 25 | Feature-flag Service | Intermediate | 2 weeks | Ch 3, 9, 13 |
| 26 | Multi-tenant SaaS Project Tracker | Advanced | 3–4 weeks | Ch 7, 8, 11 |
| 27 | Microservices Shop with Gateway, Config and Discovery | Advanced | 4 weeks | Ch 6, 16 |
| 28 | Order Saga with a Transactional Outbox | Advanced | 3–4 weeks | Ch 7, 14, 16 |
| 29 | Observability with Micrometer and OpenTelemetry | Advanced | 2–3 weeks | Ch 15, 16 |
| 30 | Spring AI RAG Documentation Assistant | Advanced | 3 weeks | Ch 13, 17 |
| 31 | Modular Monolith with Spring Modulith | Advanced | 3 weeks | Ch 2, 14, 16 |
| 32 | GraalVM Native Image Function | Advanced | 2 weeks | Ch 3, 15 |
| 33 | Resilient Travel Search Aggregator (Framework 7 resilience) | Advanced | 2–3 weeks | Ch 4, 6, 16 |
| 34 | Identity Provider with Spring Authorization Server | Advanced | 3–4 weeks | Ch 10, 11 |
| 35 | Event-sourced Wallet Ledger with CQRS | Advanced | 4 weeks | Ch 7, 9, 14 |
| 36 | AI Support Agent with Tools and MCP | Advanced | 3 weeks | Ch 11, 17 |
| 37 | Zero-downtime Deployments on Kubernetes | Advanced | 2–3 weeks | Ch 8, 15 |
| 38 | Distributed Job Scheduler | Advanced | 3 weeks | Ch 7, 14 |
| 39 | Payments API with Idempotency Keys | Advanced | 3 weeks | Ch 5, 7, 14 |
| 40 | Backend-for-Frontend with Token Relay | Advanced | 3 weeks | Ch 6, 11, 16 |

## 1. Notes REST API
**Level:** Beginner · **Time:** 1 weekend · **Prerequisites:** Ch 1, Ch 5, Ch 12

### What it is
A small HTTP service that lets a client create, read, update, delete and search short text notes. It is the "hello world" of back-end work, but done properly: correct status codes, input validation, consistent error bodies, pagination, and tests that prove every behaviour. A **REST API** (Representational State Transfer) is an API where each URL names a *resource* (here `/api/notes/42`) and the HTTP method says what to do with it (`GET` reads, `POST` creates, `PUT` replaces, `PATCH` changes part, `DELETE` removes).

### The problem it solves
Almost every Spring job starts with "add an endpoint". Doing that well needs a surprising number of decisions: what status code to return when a note is missing, what a validation error looks like, how a client pages through 10,000 notes, and how to test all of it quickly. This project builds the reflexes once, on a domain so simple that the only hard part is the craft.

### Real-world examples
- The official [Spring tutorial "Building REST services with Spring"](https://spring.io/guides/tutorials/rest) walks through the same controller-service-repository shape and status codes you will use here.
- The [Spring "Building a RESTful Web Service" guide](https://spring.io/guides/gs/rest-service) is the smallest possible version; compare it with your finished project to see what "production-shaped" adds.

### Requirements
**Functional**
- `POST /api/notes` creates a note (`title` 1–100 characters, `body` up to 10,000 characters, optional `tags`) and returns **201 Created** with a `Location` header.
- `GET /api/notes/{id}` returns one note or **404**. `GET /api/notes?q=&tag=&page=&size=` lists notes, newest first, filtered by text and tag.
- `PUT /api/notes/{id}` replaces, `DELETE /api/notes/{id}` returns **204 No Content**.
- Every error uses **Problem Details** (RFC 9457): a JSON body with `type`, `title`, `status`, `detail`.

**Non-functional**
- p95 latency under 50 ms for a list of 20 notes on a laptop with in-memory storage.
- Page size capped at 100, so no request can ask for the whole table.
- Full test suite (unit + web slice) runs in under 10 seconds.

### The architecture
```mermaid
flowchart TD
    C["HTTP client - curl or browser"] --> DS["DispatcherServlet"]
    DS --> NC["NoteController"]
    NC --> NS["NoteService"]
    NS --> NR["NoteRepository - in-memory map"]
    NC -.->|"exceptions"| EH["@RestControllerAdvice - ProblemDetail"]
```
- **DispatcherServlet** — Spring MVC's front controller: one servlet that receives every request and routes it to the right handler method.
- **NoteController** — a `@RestController` that maps URLs to methods, binds JSON to Java records and returns `ResponseEntity` with the right status.
- **NoteService** — the business rules (trim titles, stamp timestamps, enforce limits). Keeping them out of the controller makes them unit-testable without HTTP.
- **NoteRepository** — an interface with one in-memory implementation (`ConcurrentHashMap`). Project 4 swaps it for Spring Data JPA without touching the controller.
- **Exception handler** — a `@RestControllerAdvice` that turns `NoteNotFoundException` into a 404 `ProblemDetail`.

### Data model
| Type | Fields | Notes |
|---|---|---|
| `Note` (record) | `id: long`, `title`, `body`, `tags: Set<String>`, `createdAt`, `updatedAt: Instant` | immutable; updates create a new record |
| `CreateNoteRequest` | `title`, `body`, `tags` | carries Bean Validation annotations (`@NotBlank`, `@Size`) |
| `Page<Note>`-like response | `content`, `page`, `size`, `totalElements` | your own record, so the JSON shape is stable |

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Web | `spring-boot-starter-webmvc` (Boot 4 name for the old `-web`) | Servlet stack, Jackson 3 JSON included |
| Validation | `spring-boot-starter-validation` | Hibernate Validator 9 implements Bean Validation 3.1 |
| Tests | `spring-boot-starter-webmvc-test` | `@WebMvcTest`, `MockMvcTester`, `RestTestClient` |
| Ids | `AtomicLong` | simplest correct generator for one process |

### Milestones
1. **Skeleton.** Generate the project on start.spring.io with Boot 4.1.1 and Java 25; add a `GET /api/notes` returning an empty list. *Done when* `mvn verify` passes and `curl` prints `[]`.
2. **CRUD.** Add create/read/update/delete with correct statuses. *Done when* a `@WebMvcTest` asserts 201 + `Location`, 200, 204 and 404.
3. **Validation and errors.** Add `@Valid` and a `@RestControllerAdvice`. *Done when* an empty title returns 400 with a Problem Details body naming the field.
4. **Search and pagination.** Add `q`, `tag`, `page`, `size`. *Done when* a test with 25 notes shows page 2 of size 10 returns notes 11–20 in newest-first order.
5. **Full-stack test.** One `@SpringBootTest(webEnvironment = RANDOM_PORT)` test using `RestTestClient` (new in Framework 7) exercises the whole flow. *Done when* it passes in CI.

### Deep dive: the hard parts
- **201 and the `Location` header.** A create should tell the client where the new resource lives. Build it with `ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")` so it works behind any context path. Return `ResponseEntity.created(uri).body(note)`.
- **PUT vs PATCH.** `PUT` replaces the whole resource, so a missing `tags` field means "no tags". `PATCH` changes only the fields sent. Mixing them up is a classic bug: clients that send partial `PUT`s silently wipe data. Pick one meaning per endpoint and test it.
- **Thread safety of the in-memory store.** Two simultaneous `PUT`s can interleave. `ConcurrentHashMap.compute` makes "read, change, write" atomic for one key. Plain `get` then `put` does not.
- **Stable error shape.** Spring Boot can produce `ProblemDetail` for its own exceptions (`spring.mvc.problemdetails.enabled=true`). Your own exceptions need a handler. Add a `type` URI per error kind so clients can switch on it rather than parse text.

### Testing strategy
- **Unit tests** for `NoteService` with a fake repository — no Spring context, milliseconds each.
- **Web slice** (`@WebMvcTest(NoteController.class)` + `@MockitoBean NoteService`) for status codes, JSON shape and validation messages. `@MockitoBean` replaced the removed `@MockBean` in Boot 4.
- **One full-stack test** with `RestTestClient` on a random port to prove the pieces are wired together.

### Trade-offs, scaling and failure modes
- In-memory storage loses everything on restart and cannot be shared by two instances; it is fine here because the repository interface hides it.
- Offset pagination (`page=500`) gets slower as the offset grows in a real database; keyset ("give me notes older than this id") pagination scales better. Note it for project 4.
- Text search with `contains` is O(n). Beyond a few thousand notes you want a database index or a search engine.

### Stretch goals
- Add `ETag` headers and support `If-None-Match` (304 Not Modified) on `GET /api/notes/{id}`.
- Add `PATCH` with JSON Merge Patch (RFC 7396).
- Publish an OpenAPI document with springdoc-openapi (its Boot 4 line) and browse it in Swagger UI.

### Skills practised
- [Ch 1 — The Spring ecosystem and your first app](../chapters/01-spring-ecosystem-and-first-app/README.md): project layout, starters, running.
- [Ch 5 — Spring MVC and REST APIs](../chapters/05-spring-mvc-and-rest/README.md): mappings, `ResponseEntity`, validation, Problem Details.
- [Ch 12 — Testing Spring applications](../chapters/12-testing-spring-apps/README.md): `@WebMvcTest`, `MockMvcTester`, `RestTestClient`, `@MockitoBean`.

### In the interview
**30-second answer:** I built a notes CRUD API on Spring MVC with a controller, a service and a repository behind an interface, so storage could later move to JPA without touching the web layer. The key design decision was to treat the HTTP contract as the product: 201 with a `Location` header on create, 204 on delete, 404 for a missing note, and every error as an RFC 9457 Problem Details body. The trickiest part was keeping the in-memory store correct under concurrent updates, which I solved with `ConcurrentHashMap.compute`. Page size is capped at 100 and the whole test suite runs in under 10 seconds.

**Key terms to know:**
- **REST resource** — a thing named by a URL (`/api/notes/42`) that HTTP methods act on; every endpoint in this project is a note resource or the collection of notes.
- **DispatcherServlet** — Spring MVC's front controller that receives every request and routes it to a handler method; it routes all calls to `NoteController`.
- **`ResponseEntity`** — a return type that lets a controller set status, headers and body together; used for 201 + `Location` and 204.
- **Bean Validation (`@Valid`)** — annotation-driven input checks (`@NotBlank`, `@Size`) run before the controller method; enforces title and body limits.
- **Problem Details (RFC 9457)** — a standard JSON error body (`type`, `title`, `status`, `detail`); produced by the `@RestControllerAdvice`.
- **`@RestControllerAdvice`** — a bean that handles exceptions for all controllers in one place; turns `NoteNotFoundException` into a 404.
- **Idempotent method** — calling it twice has the same effect as once; `PUT` and `DELETE` are idempotent, `POST` is not.
- **Pagination** — returning a list in pages (`page`, `size`) so no request can fetch everything; capped at 100 here.
- **Test slice (`@WebMvcTest`)** — a test that starts only the MVC layer; used with `@MockitoBean` to test status codes in milliseconds.

**Q:** Why return 201 instead of 200 on create?

**A:** 201 Created tells the client a new resource now exists, and the `Location` header says where it lives. Clients can follow that URL without parsing the body. I build the URI with `ServletUriComponentsBuilder.fromCurrentRequest()` so it stays correct behind a context path or proxy.

**Q:** Where does validation happen and what does the client see?

**A:** `@Valid` on the `@RequestBody` parameter makes Spring run Bean Validation before my method runs. A failure throws `MethodArgumentNotValidException`, which my advice turns into a 400 Problem Details body listing each failing field and its message. The service never sees invalid input.

**Q:** What is the difference between PUT and PATCH, and which did you implement?

**A:** `PUT` replaces the whole resource, so a field the client leaves out is cleared. `PATCH` changes only the fields sent. I implemented `PUT` with full-replacement semantics and documented that omitting `tags` means "no tags", because mixing the two meanings is how clients silently wipe data.

**Q:** Why separate controller and service in such a small app?

**A:** The controller deals with HTTP (status codes, binding, headers) and the service deals with rules (trimming, timestamps, limits). That lets me unit-test the rules with no web layer and swap storage in project 4 without touching the controller. It costs one extra class.

**Q:** Two clients update the same note at the same moment. What happens?

**A:** With a plain `get` then `put`, one update can overwrite the other without either noticing. I use `ConcurrentHashMap.compute`, which runs read-modify-write atomically for one key. It protects the map, not the user's intent — for that you need optimistic locking with a version, which comes with the database in project 4.

**Q:** How would this change at 10x or 100x traffic? What breaks first?

**A:** The in-memory map breaks first: it is lost on restart and cannot be shared, so a second instance would show different data. I would move to a database behind the same repository interface and run several stateless instances behind a load balancer. After that, offset pagination and `contains` text search become slow, so I would add keyset pagination and a database text index.

**Q:** How do you test a controller without starting the whole app?

**A:** `@WebMvcTest(NoteController.class)` starts only MVC infrastructure plus that controller. I replace the service with `@MockitoBean` (Boot 4 removed the old `@MockBean`) and assert status, headers and JSON with `MockMvcTester`. One `@SpringBootTest` with `RestTestClient` on a random port then proves everything is wired together.

**Q:** Show how you return 201 with a `Location` header.

**A:** Build the URI from the current request and use `ResponseEntity.created`:
```java
@PostMapping
ResponseEntity<Note> create(@Valid @RequestBody CreateNoteRequest req) {
    Note n = service.create(req);
    URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}").buildAndExpand(n.id()).toUri();
    return ResponseEntity.created(uri).body(n);
}
```

**Common mistakes in answers:**
- Saying "the controller validates input with if-statements" — the point is declarative Bean Validation with `@Valid` and one central error handler.
- Returning 200 with `{"error": ...}` for failures — errors must use real 4xx/5xx statuses with a Problem Details body.
- Claiming `ConcurrentHashMap` alone makes every update safe — separate `get` and `put` calls still race; only atomic methods such as `compute` are safe.

### TL;DR
A tiny CRUD API built the right way: correct status codes, validated input, Problem Details errors, pagination and three layers of tests — the template every later project reuses.

## 2. URL Shortener
**Level:** Beginner · **Time:** 1 weekend · **Prerequisites:** Ch 5, Ch 7

### What it is
A service that turns a long URL into a short code (`https://sho.rt/aZ3k9`) and **redirects** anyone who visits the short link to the original. It also counts clicks. A **redirect** is an HTTP response with status 301, 302, 307 or 308 and a `Location` header; the browser follows it automatically.

### The problem it solves
Long links break in emails, are hard to type and leak tracking parameters. Shorteners make links short, editable and measurable. As a learning project it is ideal: two endpoints, a real database table, a uniqueness problem, and a read-heavy workload that makes you think about indexes and caching.

### Real-world examples
- [Kutt](https://github.com/thedevs-network/kutt) is an open-source shortener with custom domains, link statistics and a REST API — a good reference for the feature list.
- [YOURLS](https://github.com/YOURLS/YOURLS) is a long-lived self-hosted shortener; its plugin system shows which features users actually ask for (custom keywords, expiry, stats).

### Requirements
**Functional**
- `POST /api/links {url, customCode?, expiresAt?}` → 201 with the short code and short URL.
- `GET /{code}` → redirect to the target, or 404 if unknown, or 410 Gone if expired.
- `GET /api/links/{code}/stats` → total clicks and clicks per day for the last 30 days.
- Reject non-HTTP(S) targets and URLs longer than 2,048 characters.

**Non-functional**
- Redirect p95 under 20 ms with a warm cache; reads outnumber writes about 100 to 1.
- Codes of 7 characters from a 62-symbol alphabet (`[0-9a-zA-Z]`) give 62^7, about 3.5 trillion codes.
- A click must never block the redirect: counting is best-effort.

### The architecture
```mermaid
flowchart LR
    B["Browser"] -->|"GET /aZ3k9"| RC["RedirectController"]
    A["API client"] -->|"POST /api/links"| LC["LinkController"]
    LC --> LS["LinkService"]
    RC --> LS
    LS --> CG["CodeGenerator"]
    LS --> DB[("PostgreSQL - links, click_counts")]
    RC -.->|"async"| CL["ClickRecorder"]
    CL --> DB
```
- **RedirectController** — handles `GET /{code}` and returns `302 Found` (or `301`, see deep dive) with `Location`.
- **LinkController** — the JSON API for creating links and reading stats.
- **LinkService** — validates URLs, generates or checks custom codes, enforces expiry.
- **CodeGenerator** — produces candidate codes (random base62 or an encoded sequence).
- **ClickRecorder** — increments counters off the request path (an `@Async` method or a batched in-memory counter flushed every few seconds).
- **PostgreSQL** — the system of record, accessed with `JdbcClient`.

### Data model
```mermaid
erDiagram
    LINK ||--o{ CLICK_COUNT : "has daily"
    LINK {
        bigint id PK
        text code UK
        text target_url
        timestamptz created_at
        timestamptz expires_at
    }
    CLICK_COUNT {
        bigint link_id FK
        date day
        bigint clicks
    }
```
`CLICK_COUNT` has a composite primary key (`link_id`, `day`) so each day's counter is one row updated with `INSERT ... ON CONFLICT DO UPDATE SET clicks = clicks + ?` — an **upsert**, meaning insert-or-update in one atomic statement.

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Web | `spring-boot-starter-webmvc` | redirects are plain MVC responses |
| Data | `spring-boot-starter-jdbc` + `JdbcClient` | two tables, hand-written SQL is clearer than an ORM |
| Database | PostgreSQL (H2 in tests is acceptable here) | unique index on `code`, `ON CONFLICT` upserts |
| Async | `@EnableAsync` + virtual threads | click counting off the request thread |

### Milestones
1. **Create and redirect.** Random 7-character codes stored in a table with a unique index. *Done when* a test creates a link and `GET /{code}` returns 302 with the right `Location`.
2. **Collisions and custom codes.** Retry generation on a unique-key violation (`DuplicateKeyException`); return 409 Conflict if a custom code is taken. *Done when* a test forces a collision with a fake generator and still succeeds.
3. **Validation and expiry.** Reject `javascript:` URLs, enforce length, return 410 for expired links. *Done when* each case has a test.
4. **Click stats.** Asynchronous counter with daily rows. *Done when* 1,000 parallel redirects in a test produce exactly 1,000 counted clicks after the flush.
5. **Cache hot links.** Add `@Cacheable("links")` with Caffeine for code → target lookups. *Done when* a test shows the second redirect does not hit the database.

### Deep dive: the hard parts
- **Generating unique codes.** Random codes are simple and unguessable, but can collide; the unique index is the real guarantee, and the code retries on `DuplicateKeyException` (Spring's translated exception from [Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md)). The alternative — base62-encoding a database sequence — never collides but makes codes guessable and sequential, which leaks how many links you have.
- **301 vs 302.** A 301 (Moved Permanently) is cached by browsers forever, so later clicks never reach you: fast, but you lose counts and cannot change the target. A 302 (Found) reaches you every time. Most shorteners that sell analytics use 302 or 307.
- **Counting without slowing redirects.** Writing a row per click doubles database writes. Buffer counts in a `ConcurrentHashMap<String, LongAdder>` and flush every 5 seconds with a `@Scheduled` method using one batched upsert. You trade at most 5 seconds of counts on a crash for far fewer writes.
- **Open-redirect abuse.** A shortener is literally an open redirect. Validate schemes, optionally check targets against a blocklist, and never let a code point back at your own admin pages.

### Testing strategy
- Unit tests for `CodeGenerator` (alphabet, length) and URL validation (table-driven with JUnit `@ParameterizedTest`).
- `@JdbcTest` for the repository SQL, including the upsert.
- `@SpringBootTest` with `RestTestClient` configured **not** to follow redirects, asserting status and `Location`.
- A concurrency test with virtual threads for the click counter.

### Trade-offs, scaling and failure modes
- One PostgreSQL instance easily serves thousands of redirects per second with an index on `code`; a cache in front removes most reads. Beyond that, read replicas or a key-value store.
- Losing buffered clicks on a crash is the price of cheap counting — acceptable for analytics, not for billing.
- A cache entry for an edited or deleted link must be evicted (`@CacheEvict`), or users are redirected to the old target until the entry expires.

### Stretch goals
- Per-user API keys and a "my links" page.
- QR-code generation for each link.
- Bot filtering: do not count requests whose `User-Agent` matches known crawlers.

### Skills practised
- [Ch 5 — Spring MVC and REST APIs](../chapters/05-spring-mvc-and-rest/README.md): redirects, validation, status codes.
- [Ch 7 — Data access with JDBC and transactions](../chapters/07-data-access-jdbc-and-transactions/README.md): `JdbcClient`, exception translation, upserts.
- [Ch 9 — Spring Data beyond JPA, and caching](../chapters/09-spring-data-beyond-jpa-and-caching/README.md): `@Cacheable`, eviction.
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): `@Async`, `@Scheduled`.

### In the interview
**30-second answer:** I built a URL shortener with two hot paths: `POST /api/links` creates a 7-character base62 code and `GET /{code}` redirects with 302. Uniqueness is guaranteed by a unique index on the code column, and the service retries on `DuplicateKeyException` rather than checking first, because check-then-insert races. The hardest part was counting clicks without slowing redirects, so I buffer counts in memory with `LongAdder` and flush them every 5 seconds with one batched upsert. 62^7 gives about 3.5 trillion codes, and 1,000 parallel redirects in a test produce exactly 1,000 counted clicks.

**Key terms to know:**
- **Redirect (301/302/307/308)** — a response with a `Location` header the browser follows; `GET /{code}` returns 302.
- **Base62 code** — a short id from the alphabet `[0-9a-zA-Z]`; 7 characters give about 3.5 trillion combinations.
- **Unique index** — a database constraint that rejects a second row with the same value; the real guarantee that codes never collide.
- **Exception translation (`DuplicateKeyException`)** — Spring converts vendor SQL errors into a common exception hierarchy; the generator retries on it.
- **Upsert (`INSERT ... ON CONFLICT DO UPDATE`)** — insert-or-update in one atomic statement; used for the daily click counter.
- **Open redirect** — a link that sends users anywhere, abusable for phishing; the service validates schemes and blocks its own admin pages.
- **Look-aside cache (`@Cacheable`)** — check cache, fall back to the database, store the result; hot code-to-URL lookups use Caffeine.
- **Best-effort async counting** — side work done off the request path that may lose a little data on a crash; clicks are buffered and flushed.

**Q:** How do you guarantee short codes are unique?

**A:** The unique index on `code` is the guarantee: the application generates a random code, inserts it, and retries with a new code if the insert throws `DuplicateKeyException`. Checking "does it exist?" first is not enough because two requests can both see "no" and both insert. For custom codes I do not retry; I return 409 Conflict.

**Q:** Random codes or an encoded database sequence — why?

**A:** A base62-encoded sequence never collides, but codes are predictable and reveal how many links exist, and anyone can walk them. Random codes are unguessable and need a retry on the rare collision. At 3.5 trillion possible codes and millions of links, collisions are extremely rare, so random wins.

**Q:** 301 or 302?

**A:** 302 (or 307) when I need analytics or may change or expire the target, because browsers cache a 301 permanently and later clicks never reach my server. A 301 is faster for the user and cheaper for me, but I lose counts and control. Shorteners that sell analytics use 302.

**Q:** How do you count clicks without slowing the redirect?

**A:** The redirect only increments a `LongAdder` in a `ConcurrentHashMap<String, LongAdder>`, which is a few nanoseconds. A `@Scheduled` method swaps out the counts every 5 seconds and writes them with one batched `ON CONFLICT DO UPDATE SET clicks = clicks + ?`. The trade-off is that a crash can lose up to 5 seconds of counts, which is fine for analytics but not for billing.

**Q:** How would you scale redirects to 50,000 per second? What breaks first?

**A:** The database read per redirect breaks first. I would cache code-to-URL in process (Caffeine) and in a shared cache like Redis, run many stateless instances behind a load balancer, and add read replicas. Click counting would move to an append-only log such as Kafka and be aggregated by a consumer, so the write path never touches the main table.

**Q:** A link is edited or deleted but users still land on the old target. Why?

**A:** The cache still holds the old code-to-URL entry. Every update or delete must evict it with `@CacheEvict` (or `@CachePut` the new value), and entries should have a TTL as a safety net. With several instances and in-process caches, eviction must be broadcast or the cache must be shared.

**Q:** How did you prove the collision retry works?

**A:** I injected a fake `CodeGenerator` that returns an existing code first and a fresh one second, then asserted the create still succeeded and the generator was called twice. Because the guarantee lives in the database, this is a `@SpringBootTest` or `@JdbcTest` against a real table with the unique index, not a mock.

**Q:** Write the upsert for the daily counter.

**A:** With a composite primary key on `(link_id, day)`:
```sql
INSERT INTO click_count (link_id, day, clicks)
VALUES (:linkId, :day, :n)
ON CONFLICT (link_id, day)
DO UPDATE SET clicks = click_count.clicks + EXCLUDED.clicks;
```
`EXCLUDED` refers to the row that failed to insert, so the increment is atomic.

**Common mistakes in answers:**
- "I check if the code exists, then insert" — this races; only a unique constraint plus retry is correct.
- Recommending 301 "because it is permanent" without noting that browsers cache it and analytics stop working.
- Writing one row per click synchronously — this doubles writes and puts the database on the redirect path.

### TL;DR
Two endpoints, one unique index and a buffered counter teach collision handling, redirect semantics, read-heavy caching and asynchronous side effects.

## 3. Weather Client with RestClient and HTTP Interfaces
**Level:** Beginner · **Time:** 1 weekend · **Prerequisites:** Ch 3, Ch 6

### What it is
A service that answers `GET /api/weather?city=Lisbon` by calling a public weather API, converting the answer into a clean domain object, and caching it briefly. It is built twice: first with **`RestClient`** (Spring's fluent, blocking HTTP client) and then with an **HTTP service interface** — a plain Java interface annotated with `@GetExchange` that Spring turns into a working client. Boot 4 can create those clients for you from properties through `@ImportHttpServices` "groups" (new in Framework 7 / Boot 4).

### The problem it solves
Calling other services is half of back-end work, and it is where most production incidents start: no timeout, an unexpected 500, a slow dependency that ties up every thread. This project teaches the client side properly — base URLs in configuration, timeouts, error mapping, retries for transient failures, and tests that do not touch the real internet.

### Real-world examples
- [Open-Meteo](https://open-meteo.com/en/docs) is a free weather API without API keys for non-commercial use, ideal for this project (geocoding plus forecast endpoints).
- The [Spring Framework REST clients reference](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html) documents `RestClient`, HTTP service interfaces and the client groups you will configure.

### Requirements
**Functional**
- `GET /api/weather?city=` resolves the city to coordinates (geocoding call) and returns current temperature, wind and a short description.
- Unknown city → 404 Problem Details. Upstream failure → 503 with a `Retry-After` header.
- Results cached per city for 10 minutes.

**Non-functional**
- Connect timeout 1 s, read timeout 2 s: a slow upstream must not hold a thread longer.
- At most 2 retries on connection errors or 503, with back-off; never retry 4xx.
- p95 under 30 ms for cached cities.

### The architecture
```mermaid
flowchart TD
    U["Client"] --> WC["WeatherController"]
    WC --> WS["WeatherService - @Cacheable"]
    WS --> GA["GeocodingApi - @HttpExchange"]
    WS --> FA["ForecastApi - @HttpExchange"]
    GA -->|"group geo"| OM1["Open-Meteo geocoding"]
    FA -->|"group forecast"| OM2["Open-Meteo forecast"]
```
- **WeatherController** — validates `city` and maps exceptions to HTTP errors.
- **WeatherService** — orchestrates two calls and converts upstream DTOs to a `Weather` record; its result is cached.
- **GeocodingApi / ForecastApi** — HTTP service interfaces. Each belongs to a **group** (`geo`, `forecast`) whose base URL, timeouts and default headers come from `spring.http.serviceclient.<group>.*` properties.
- **Upstream** — the public API. In tests it is replaced by `MockRestServiceServer` or a local stub server.

### Data model
| Type | Purpose |
|---|---|
| `GeoResult` record | upstream geocoding DTO: name, latitude, longitude, country |
| `ForecastResponse` record | upstream forecast DTO, only the fields you use (Jackson 3 ignores unknown properties by default — prove it with a test) |
| `Weather` record | your domain object: city, temperature in Celsius, wind km/h, description, fetchedAt |
| `WeatherProperties` | `@ConfigurationProperties("weather")` record: cache TTL, default units |

### Tech stack
| Component | Choice | Why |
|---|---|---|
| HTTP client | `spring-boot-starter-restclient` | `RestClient.Builder` and HTTP service client groups (Boot 4) |
| Declarative client | `@HttpExchange` + `@ImportHttpServices` | no extra library, unlike OpenFeign |
| Retries | Framework 7 `@Retryable` + `@EnableResilientMethods` | built in, no Spring Retry dependency |
| Cache | `spring-boot-starter-cache` + Caffeine | in-process TTL cache |
| Tests | `spring-boot-starter-restclient-test` | `@RestClientTest`, `MockRestServiceServer` |

### Milestones
1. **RestClient version.** A hand-written `GeocodingClient` using `RestClient.builder().baseUrl(...)`. *Done when* a `@RestClientTest` with `MockRestServiceServer` asserts the request URL and the mapped `GeoResult`.
2. **Timeouts and errors.** Configure timeouts; map 404 to an empty result and 5xx to `UpstreamUnavailableException` with `onStatus`. *Done when* tests for 404, 500 and a slow response (timeout) pass.
3. **HTTP interfaces.** Replace the class with `GeocodingApi` and `ForecastApi` interfaces registered through `@ImportHttpServices(group = ...)`. *Done when* the same tests pass and the bean class prints as a JDK proxy.
4. **Retries and cache.** Add `@Retryable(includes = ..., maxRetries = 2, delay = 200, multiplier = 2, jitter = 50)` on the service call and `@Cacheable`. *Done when* a test shows two failures then success, and a second call within 10 minutes makes no HTTP request.
5. **Configuration per environment.** Move base URLs and timeouts to `application.yaml` with a `test` profile. *Done when* nothing URL-shaped remains in Java code.

### Deep dive: the hard parts
- **Timeouts are not optional.** Without a read timeout, a stalled upstream keeps your request thread waiting indefinitely; with enough of them the whole app stops answering. Set both connect and read timeouts per group, and make your own endpoint's total budget (e.g. 5 s) larger than the sum of retries.
- **Retry only what is safe and transient.** `GET` is idempotent, so retrying it is safe. Retry connection resets and 503, never 400/404. Remember `maxRetries = 2` means up to 3 attempts. Because `@Retryable` is proxy-based ([Ch 4](../chapters/04-aop-and-proxies/README.md)), calling the retried method from the same class (`this.fetch()`) silently skips the retry.
- **DTOs at the boundary.** Never return upstream DTOs from your controller. If Open-Meteo renames a field, only the mapping code changes; your clients see the same `Weather` shape.
- **Testing without the internet.** `MockRestServiceServer` intercepts requests made by a `RestClient` built from the auto-configured builder, so tests are fast and deterministic. A test that hits the real API belongs in a separate, optional "contract" job.

### Testing strategy
- `@RestClientTest` + `MockRestServiceServer` for each client method, including error statuses.
- A `@SpringBootTest` with a stub server on a random port to prove the property-driven group configuration really reaches the HTTP layer (headers, base URL).
- A cache test asserting the number of upstream calls.

### Trade-offs, scaling and failure modes
- Hand-written `RestClient` classes allow custom logic per call; HTTP interfaces remove boilerplate but push custom behaviour into wrappers or `default` methods.
- An in-process cache is per instance: three instances can each call upstream once per city. A shared cache (Redis, project 14) fixes that at the cost of a network hop.
- If upstream is down for long, serving the last known value with a "stale" flag is often better than a 503 — a product decision, not a technical one.

### Stretch goals
- Call geocoding and forecast for several cities in parallel with virtual threads and `StructuredTaskScope` (a Java 25 preview API — enable preview or use an executor).
- Add a client-side API version header per group (`apiversion.insert.header`) against a stub that requires it.
- Expose `http.client.requests` metrics through Actuator and graph upstream latency.

### Skills practised
- [Ch 3 — Configuration and auto-configuration](../chapters/03-configuration-and-auto-configuration/README.md): `@ConfigurationProperties`, profiles.
- [Ch 6 — REST clients and HTTP interfaces](../chapters/06-rest-clients-and-http-interfaces/README.md): `RestClient`, timeouts, error handling, `@ImportHttpServices`, testing clients.
- [Ch 4 — AOP and proxies](../chapters/04-aop-and-proxies/README.md): `@Retryable` and why self-invocation bypasses it.
- [Ch 9 — Spring Data beyond JPA, and caching](../chapters/09-spring-data-beyond-jpa-and-caching/README.md): `@Cacheable` with a TTL.

### In the interview
**30-second answer:** I built a weather service that calls Open-Meteo's geocoding and forecast APIs and returns my own `Weather` record, first with a hand-written `RestClient` and then with HTTP service interfaces registered through Boot 4's `@ImportHttpServices` groups. The key decision was to treat every remote call as unreliable: 1 s connect and 2 s read timeouts, at most 2 retries only for connection errors and 503, and a 10-minute cache. The hardest part was a retry that silently did nothing, which turned out to be self-invocation bypassing the proxy. Cached cities answer in under 30 ms and every test runs offline against `MockRestServiceServer`.

**Key terms to know:**
- **`RestClient`** — Spring's fluent, blocking HTTP client (Framework 6.1+); used for the first version of the geocoding client.
- **HTTP service interface (`@HttpExchange`, `@GetExchange`)** — a Java interface that Spring turns into a working client proxy; `GeocodingApi` and `ForecastApi`.
- **HTTP service client group (`@ImportHttpServices`)** — Boot 4 / Framework 7 feature that registers interface clients in named groups configured from `spring.http.serviceclient.<group>.*`.
- **Connect vs read timeout** — time to open the TCP connection vs time to wait for response data; both set per group.
- **Transient failure / idempotency** — a failure likely to disappear on retry, on a call safe to repeat; only `GET`s on 503 or connection resets are retried.
- **`@Retryable` + `@EnableResilientMethods`** — Framework 7's built-in, proxy-based retry with back-off and jitter; replaces Spring Retry here.
- **Self-invocation** — a bean calling its own method via `this`, which bypasses the proxy and therefore skips retry and caching.
- **DTO at the boundary** — upstream response records are mapped to the domain `Weather` record so upstream changes do not leak to clients.
- **`MockRestServiceServer`** — a test tool that intercepts `RestClient` requests and returns canned responses; used in `@RestClientTest`.

**Q:** `RestClient`, `WebClient` or `RestTemplate` for new code?

**A:** `RestClient` for blocking code, which with virtual threads scales well; `WebClient` only in a reactive application. `RestTemplate` is on its way out: the Spring team announced its deprecation with Framework 7.0, it becomes formally `@Deprecated` in 7.1 (Boot 4.1 still ships 7.0.x), and removal is planned for 8.0.

**Q:** What does `@ImportHttpServices` give you over hand-written clients?

**A:** It creates proxies for HTTP service interfaces and puts them in named groups. Boot configures each group's underlying client (base URL, timeouts, default headers, API version) from `spring.http.serviceclient.<group>.*`, so no URL lives in Java code. The trade-off is that custom per-call logic has to go into `default` methods or a wrapper service.

**Q:** Why are timeouts not optional?

**A:** Without a read timeout, a stalled upstream keeps a request thread waiting indefinitely, and enough stuck threads stop the whole app from answering. I set connect 1 s and read 2 s per group and keep my own endpoint's budget larger than the worst case of all retries. Even with virtual threads, waiting calls still hold sockets and memory.

**Q:** Which failures do you retry, and why not all of them?

**A:** Only transient ones on idempotent calls: connection resets and 503, on `GET`. A 400 or 404 will fail the same way every time, so retrying just adds load and latency. `maxRetries = 2` means up to 3 attempts in total, with exponential back-off and jitter so many clients do not retry in lockstep.

**Q:** Why did your retry not fire?

**A:** Usually one of two reasons: `@EnableResilientMethods` is missing, so no proxy is created, or the retried method was called from inside the same bean (`this.fetch()`), bypassing the proxy. I fixed it by putting the retried call on the service and calling it from the controller, and a test now asserts two failures followed by success.

**Q:** What happens if Open-Meteo is down for an hour?

**A:** After the retries are used up, the client throws `UpstreamUnavailableException`, which my advice maps to 503 with a `Retry-After` header. Cached cities keep working until their 10-minute entries expire. A better product choice is often to serve the last known value marked as stale, which needs a separate stale store rather than a TTL cache.

**Q:** You run three instances. What changes at 10x traffic?

**A:** The in-process Caffeine cache is per instance, so each instance calls upstream once per city, and at 10x I may hit Open-Meteo's rate limits. A shared Redis cache (project 14) removes the duplicates at the cost of a network hop. I would also add a concurrency limit on outbound calls so a traffic spike cannot fan out into a flood upstream.

**Q:** How do you test the client without the internet?

**A:** `@RestClientTest` with `MockRestServiceServer` asserts the request URL and query parameters and returns canned JSON, 404s, 500s or delays. A separate `@SpringBootTest` against a local stub server proves the group properties really reach the HTTP layer. A test against the real API runs only in an optional contract job.

**Q:** Write the HTTP interface for the geocoding call.

**A:**
```java
@HttpExchange("/v1")
public interface GeocodingApi {
    @GetExchange("/search")
    GeoResponse search(@RequestParam String name,
                       @RequestParam int count);
}
```
It is registered with `@ImportHttpServices(group = "geo", types = GeocodingApi.class)`.

**Common mistakes in answers:**
- "Retries make the client reliable" — retries without timeouts, limits and an idempotency check multiply load on a failing service.
- Returning upstream DTOs directly from the controller — any upstream rename then breaks your own clients.
- Saying `maxRetries = 2` means two attempts — it means one initial attempt plus two retries.

### TL;DR
Wrap a public API behind your own domain type with timeouts, narrow retries, a short cache and offline tests — first with `RestClient`, then with Boot 4's HTTP service client groups.

## 4. Todo App with Spring Data JPA
**Level:** Beginner · **Time:** 1–2 weekends · **Prerequisites:** Ch 5, Ch 8

### What it is
A multi-list todo service: users own lists, lists contain items with due dates, priorities and a done flag. It stores data in PostgreSQL through **Spring Data JPA** — **JPA** (Jakarta Persistence) is the standard Java API for mapping objects to tables, **Hibernate** is the implementation, and **Spring Data JPA** generates repository classes from interfaces.

### The problem it solves
Most business apps are "lists of things with relationships". JPA saves a lot of SQL, but it hides costs: the **N+1 problem** (one query for lists, then one more per list for its items), lazy-loading exceptions, and accidental updates through dirty checking. This project makes you meet each of those on purpose and fix them.

### Real-world examples
- [TodoMVC](https://todomvc.com/) defines the classic todo feature set used to compare front-end frameworks; your API can back any of its implementations.
- The [Spring Data JPA reference](https://docs.spring.io/spring-data/jpa/reference/) covers the derived queries, projections, auditing and pagination this project uses.

### Requirements
**Functional**
- CRUD for lists and items; move an item between lists; mark done/undone.
- `GET /api/lists` returns each list with its item counts (total and done).
- `GET /api/items?due=before:2026-12-01&priority=HIGH&page=` filters items.
- Each entity records `createdAt`, `updatedAt` and `createdBy`.

**Non-functional**
- Loading 50 lists with counts uses a **constant** number of SQL statements (no N+1), verified by a test.
- Schema managed by Flyway migrations, never by `ddl-auto=update`.
- Concurrent edits of the same item must not silently overwrite each other.

### The architecture
```mermaid
flowchart TD
    C["Client"] --> CT["ListController and ItemController"]
    CT --> SV["TodoService - @Transactional"]
    SV --> R1["TodoListRepository"]
    SV --> R2["TodoItemRepository"]
    R1 --> H["Hibernate 7 - persistence context"]
    R2 --> H
    H --> DB[("PostgreSQL")]
    FW["Flyway migrations"] --> DB
```
- **Controllers** — map JSON to commands; they never touch entities directly, only DTOs (records).
- **TodoService** — transaction boundary: one `@Transactional` method per use case.
- **Repositories** — `JpaRepository` interfaces with derived queries and a few `@Query` JPQL queries.
- **Persistence context** — Hibernate's per-transaction cache of loaded entities; it tracks changes and flushes them at commit (dirty checking).
- **Flyway** — versioned SQL scripts (`V1__init.sql`) applied at startup.

### Data model
```mermaid
erDiagram
    APP_USER ||--o{ TODO_LIST : "owns"
    TODO_LIST ||--o{ TODO_ITEM : "contains"
    APP_USER {
        bigint id PK
        text username UK
    }
    TODO_LIST {
        bigint id PK
        bigint owner_id FK
        text name
        bigint version
        timestamptz created_at
    }
    TODO_ITEM {
        bigint id PK
        bigint list_id FK
        text title
        text priority
        date due_date
        boolean done
        bigint version
    }
```
The `version` columns support **optimistic locking**: every update checks that the version is unchanged and increments it.

### Tech stack
| Component | Choice | Why |
|---|---|---|
| ORM | `spring-boot-starter-data-jpa` (Hibernate 7.4) | JPA 3.2, Spring Data repositories |
| Migrations | `spring-boot-starter-flyway` | Boot 4 needs the starter, `flyway-core` alone is no longer auto-configured |
| Database | PostgreSQL; H2 only for quick slice tests | real constraints and types |
| Auditing | `@EnableJpaAuditing` + `@CreatedDate` etc. | timestamps without boilerplate |

### Milestones
1. **Entities and migrations.** Map `TodoList` and `TodoItem` with `@ManyToOne(fetch = LAZY)`. *Done when* `@DataJpaTest` saves and reloads a list with items.
2. **API.** Controllers with DTOs and pagination via `Pageable`. *Done when* web tests cover create, list, filter and 404.
3. **Find and fix N+1.** Turn on SQL statement counting (Hibernate statistics or a datasource proxy), write a test that loads 50 lists, watch it issue 51 queries, then fix it with a projection query (`select new ...ListSummary(l.id, l.name, count(i), ...) group by`). *Done when* the test asserts 1–2 statements.
4. **Auditing and optimistic locking.** *Done when* two concurrent updates with the same version produce one success and one 409 Conflict.
5. **Turn off open-in-view.** Set `spring.jpa.open-in-view=false`. *Done when* every endpoint still works, proving no lazy loading leaks into the web layer.

### Deep dive: the hard parts
- **N+1 queries.** Lazy collections load on first access, one query per parent. Fixes, from simplest: a DTO projection that computes what the endpoint needs in one query; `JOIN FETCH` or `@EntityGraph` for a specific use case; batch fetching (`hibernate.default_batch_fetch_size`) as a safety net. Never switch everything to `EAGER` — that just moves the problem.
- **Open Session in View.** Boot enables it by default (with a warning), keeping the persistence context open during JSON rendering so lazy loading "just works" — at the cost of database queries during serialization and connections held longer. Disabling it forces you to load what you need inside the transaction.
- **Dirty checking surprises.** Inside a transaction, changing a managed entity's field is enough to issue an `UPDATE` at commit, even without `save()`. Mark read use cases `@Transactional(readOnly = true)` so Hibernate skips dirty checking and so the intent is explicit.
- **Optimistic locking.** With `@Version`, a stale update throws `ObjectOptimisticLockingFailureException`. Map it to 409 and include the current version so the client can reload and retry.

### Testing strategy
- `@DataJpaTest` for repositories and custom queries (it rolls back after each test).
- A statement-count assertion test for the N+1 fix — it guards against regressions when someone adds a field.
- `@WebMvcTest` for controllers; one `@SpringBootTest` against PostgreSQL (Testcontainers if you have Docker, see project 20).

### Trade-offs, scaling and failure modes
- JPA shines for rich object graphs with modest data volumes; for reports and bulk updates, write SQL (`@Query(nativeQuery = true)` or `JdbcClient`).
- `ddl-auto=update` is tempting but cannot rename columns or migrate data safely; Flyway scripts are reviewable and repeatable.
- Very large lists need keyset pagination and an index on `(list_id, due_date)`.

### Stretch goals
- Soft delete with a `deleted_at` column and a Hibernate `@SoftDelete` annotation.
- Full-text search on titles using PostgreSQL `tsvector` with a native query.
- Expose the repositories with Spring Data REST and compare the result with your hand-written API.

### Skills practised
- [Ch 8 — Spring Data JPA and Hibernate](../chapters/08-spring-data-jpa-and-hibernate/README.md): entities, relationships, N+1, projections, auditing, locking, open-in-view, Flyway.
- [Ch 5 — Spring MVC and REST APIs](../chapters/05-spring-mvc-and-rest/README.md): pagination, DTOs.
- [Ch 7 — Data access with JDBC and transactions](../chapters/07-data-access-jdbc-and-transactions/README.md): transaction boundaries, `readOnly`.

### In the interview
**30-second answer:** I built a multi-list todo API on Spring Data JPA and Hibernate with PostgreSQL and Flyway migrations. The main design decision was that controllers only see DTOs and every use case is one `@Transactional` service method, so I control exactly what is loaded. The hardest problem was the N+1 query: loading 50 lists with item counts issued 51 SQL statements, which I caught with a statement-counting test and fixed with one `GROUP BY` projection query. I also turned off open-in-view and added `@Version` optimistic locking, so a stale update returns 409 instead of overwriting.

**Key terms to know:**
- **JPA / Hibernate / Spring Data JPA** — the standard ORM API, its implementation, and the library that generates repositories from interfaces; the whole persistence layer.
- **Persistence context** — Hibernate's per-transaction cache of loaded entities that tracks changes; it is what makes dirty checking work.
- **Dirty checking** — Hibernate compares managed entities to their snapshot at flush and writes an `UPDATE` automatically, even without `save()`.
- **Lazy vs eager loading** — loading a relation on first access vs immediately; all `@ManyToOne` relations here are `LAZY`.
- **N+1 problem** — one query for parents plus one per parent for children; fixed with a projection query.
- **DTO projection / `JOIN FETCH` / `@EntityGraph`** — ways to load exactly what a screen needs in one query.
- **Open Session in View (OSIV)** — keeps the persistence context open during rendering so lazy loading "works"; disabled with `spring.jpa.open-in-view=false`.
- **Optimistic locking (`@Version`)** — each update checks and increments a version column; prevents lost updates on items.
- **Flyway migration** — a versioned SQL script applied once in order; the only way the schema changes here.

**Q:** What is the N+1 problem and how did you fix it?

**A:** Loading N lists and then touching each list's lazy `items` collection issues one query for the lists plus one per list — 51 queries for 50 lists. I proved it with a test that counts SQL statements, then replaced it with one JPQL projection: `select new ListSummary(l.id, l.name, count(i), ...) from TodoList l left join l.items i group by l.id, l.name`. For screens that really need entities I use `JOIN FETCH` or `@EntityGraph`, and `hibernate.default_batch_fetch_size` as a safety net.

**Q:** Why not just make the relationship `EAGER`?

**A:** `EAGER` loads the collection on every query of the parent, even where it is not needed, and with JPQL queries Hibernate often still loads it with extra selects, so the N+1 just moves. The fetch strategy should be chosen per use case, which is what projections and entity graphs do.

**Q:** What is open-in-view and why did you disable it?

**A:** Boot enables it by default and logs a warning: the persistence context stays open until the response is written, so lazy loading works in the controller and during JSON serialization. The cost is database queries during rendering and a connection held for the whole request. With it off, a lazy access outside the transaction throws `LazyInitializationException`, which forces each service method to load exactly what the response needs.

**Q:** Why did an entity get updated when you never called `save()`?

**A:** Dirty checking: inside a transaction, a managed entity whose field changes is written at flush. That is by design, but it surprises people who modify an entity "just to format it". I mark read use cases `@Transactional(readOnly = true)`, which tells Hibernate to skip dirty checking, and never hand entities to the web layer.

**Q:** How do you prevent lost updates when two users edit the same item?

**A:** A `@Version` column. Hibernate adds `where version = ?` to the `UPDATE` and increments it; if another transaction got there first, zero rows match and it throws `ObjectOptimisticLockingFailureException`, which I map to 409 Conflict. The client reloads and retries. It costs nothing when there is no conflict, unlike a pessimistic lock.

**Q:** What happens at 100x data — a list with 100,000 items?

**A:** Offset pagination gets slower because the database still walks all skipped rows, so I would switch to keyset pagination (`where due_date > :lastDue order by due_date`) with an index on `(list_id, due_date)`. Loading the whole `items` collection into memory becomes impossible, so the collection mapping should be used only for small lists or dropped in favour of repository queries. Bulk changes move to SQL updates.

**Q:** Why Flyway instead of `ddl-auto=update`?

**A:** `ddl-auto=update` only adds things: it cannot rename a column, migrate data or run safely against production, and nobody reviews what it did. Flyway scripts are versioned, reviewed, applied once in order and recorded with a checksum. In Boot 4 you need `spring-boot-starter-flyway`; `flyway-core` alone is no longer auto-configured.

**Q:** How do you keep the N+1 fix from regressing?

**A:** The statement-count test stays in the suite: it enables Hibernate statistics (or a datasource proxy), calls the endpoint for 50 lists and asserts at most 2 statements. If someone adds a lazy field to the DTO, the count jumps and the test fails in CI, long before production.

**Common mistakes in answers:**
- "Fix N+1 by making everything EAGER" — that loads too much everywhere and often does not reduce query count.
- "`save()` is needed to persist changes" — inside a transaction, managed entities are flushed automatically.
- Treating `LazyInitializationException` as a bug to hide with OSIV — it is a signal that the service did not load what the caller needs.

### TL;DR
A todo API that deliberately triggers and fixes JPA's classic traps — N+1, open-in-view, dirty checking and lost updates — with Flyway-managed schema and tests that count queries.

## 5. Config-driven Greeting Service
**Level:** Beginner · **Time:** 1 weekend · **Prerequisites:** Ch 2, Ch 3

### What it is
A deliberately simple service — it greets users in their language, in a seasonal style — whose *behaviour* is driven entirely by configuration: languages, templates, feature switches and per-environment settings. The point is not the greeting; it is learning exactly where Spring Boot reads configuration from, in what order, how it binds it to typed objects, and how beans are switched on or off by conditions.

### The problem it solves
"Works on my machine" bugs are very often configuration bugs: a property set in the wrong file, overridden by an environment variable, misspelled so it is silently ignored, or a bean that exists in one profile and not another. Mastering configuration means you can predict and test which value wins.

### Real-world examples
- [The Twelve-Factor App, factor III "Config"](https://12factor.net/config) argues that config that varies between deploys belongs in the environment, not in code — the principle Boot's externalized configuration implements.
- The [Spring Boot externalized configuration reference](https://docs.spring.io/spring-boot/reference/features/external-config.html) lists the property-source order you will prove with tests.

### Requirements
**Functional**
- `GET /api/greet?name=Ana&lang=pt` → `{"message":"Olá, Ana!"}` using templates from configuration.
- Unknown language falls back to a configured default.
- A `seasonal` feature switch adds a seasonal suffix; it is off unless enabled.
- `GET /actuator/configprops` (secured or local only) shows the bound properties.

**Non-functional**
- Invalid configuration (empty template, unknown default language) must **fail at startup**, never at the first request.
- Startup in under 2 seconds on a laptop.
- Every configuration precedence claim in your README is backed by a test.

### The architecture
```mermaid
flowchart TD
    PS["Property sources - defaults, application.yaml, profile files, env vars, command line"] --> ENV["Environment"]
    ENV --> GP["GreetingProperties - @ConfigurationProperties record"]
    GP --> GS["GreetingService"]
    COND["@ConditionalOnBooleanProperty seasonal.enabled"] --> SD["SeasonalDecorator bean"]
    SD --> GS
    GS --> GC["GreetingController"]
```
- **Property sources** — places Boot reads settings from, layered so later ones override earlier ones (command-line arguments beat environment variables, which beat `application.yaml`).
- **Environment** — Spring's merged view of all property sources and active profiles.
- **GreetingProperties** — a Java record annotated `@ConfigurationProperties("greeting")` and `@Validated`; Boot binds and validates it at startup.
- **SeasonalDecorator** — a bean created only when a property is true. `@ConditionalOnBooleanProperty` is a Boot 3.5+ convenience; `@ConditionalOnProperty` works too.
- **GreetingService** — composes the message from templates and the optional decorator.

### Data model
| Type | Fields |
|---|---|
| `GreetingProperties` (record) | `defaultLanguage: String`, `templates: Map<String, String>`, `seasonal: Seasonal` |
| `Seasonal` (nested record) | `enabled: boolean`, `suffixes: Map<Month, String>` |
| `Greeting` (response) | `message`, `language` |

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Binding | `@ConfigurationProperties` records + `@Validated` | type-safe, immutable, validated at startup |
| Profiles | `application-dev.yaml`, `application-prod.yaml` | per-environment overrides |
| Conditions | `@ConditionalOnBooleanProperty`, `@Profile` | beans present only when needed |
| Inspection | Actuator `configprops`, `env` | see what was bound and from where |

### Milestones
1. **Typed properties.** Bind templates into a record. *Done when* a test using `ApplicationContextRunner` with `withPropertyValues(...)` shows the map bound correctly.
2. **Fail fast.** Add `@NotEmpty`/`@NotBlank` constraints and a custom check that the default language exists in `templates`. *Done when* a context-runner test asserts startup **fails** with a clear message.
3. **Precedence proved.** Set the same property in `application.yaml`, a profile file, an environment variable (`GREETING_DEFAULTLANGUAGE`) and a command-line argument. *Done when* tests show which wins in each combination.
4. **Conditional bean.** The seasonal decorator exists only when `greeting.seasonal.enabled=true`. *Done when* two context-runner tests assert `hasSingleBean` and `doesNotHaveBean`.
5. **Clock injection.** Inject a `java.time.Clock` bean so the seasonal logic is testable. *Done when* a test with a fixed December clock gets the December suffix.

### Deep dive: the hard parts
- **Relaxed binding.** `greeting.default-language`, `greeting.defaultLanguage` and the environment variable `GREETING_DEFAULTLANGUAGE` all bind to the same field. Environment variables cannot contain dots or dashes, so Boot maps upper-case and underscores. Map keys with special characters need bracket notation (`greeting.templates[pt-BR]`).
- **`@Value` vs `@ConfigurationProperties`.** `@Value("${greeting.x}")` reads one string at a time, is not validated as a group and is easy to misspell. `@ConfigurationProperties` gives one typed object, IDE metadata and validation. Use `@Value` only for one-off values.
- **Profiles are additive.** Activating `dev` loads `application.yaml` then `application-dev.yaml` on top. Profile groups (`spring.profiles.group.prod=prod-db,prod-mq`) let one name activate several.
- **Conditions are evaluated once.** Beans are created at startup; flipping a property later does not create or remove the decorator. Dynamic switches need a feature-flag service (project 25).

### Testing strategy
- `ApplicationContextRunner` for configuration and conditions: it starts a tiny context per test in milliseconds and lets you assert startup failures.
- `@SpringBootTest(properties = ...)` and `@ActiveProfiles` for integration tests.
- A plain unit test for `GreetingService` with a fixed `Clock`.

### Trade-offs, scaling and failure modes
- Fail-fast validation turns runtime surprises into deploy-time failures — which is the point, but your deployment must surface startup logs clearly.
- Secrets do not belong in `application.yaml` committed to git: use environment variables, a mounted secret file (`spring.config.import=configtree:`) or a vault.
- Too many profiles become a combinatorial maze; prefer few profiles and environment-specific values from the platform.

### Stretch goals
- Package the decorator as a tiny starter with its own auto-configuration (a preview of project 9).
- Import extra settings with `spring.config.import=optional:file:./local.yaml`.
- Register one bean per configured language programmatically with Framework 7's `BeanRegistrar`.

### Skills practised
- [Ch 2 — IoC and dependency injection](../chapters/02-ioc-and-dependency-injection/README.md): beans, conditions, profiles, `BeanRegistrar`.
- [Ch 3 — Configuration and auto-configuration](../chapters/03-configuration-and-auto-configuration/README.md): property sources and precedence, relaxed binding, `@ConfigurationProperties`, profiles, config import.
- [Ch 12 — Testing Spring applications](../chapters/12-testing-spring-apps/README.md): profiles and properties in tests.

### In the interview
**30-second answer:** I built a greeting service whose behaviour — languages, templates, a seasonal switch — comes entirely from configuration, to learn exactly how Spring Boot resolves and binds properties. The key decision was one validated `@ConfigurationProperties` record instead of scattered `@Value` fields, so bad configuration fails at startup rather than on the first request. The hardest part was proving precedence, so I wrote tests showing that a command-line argument beats an environment variable, which beats a profile file, which beats `application.yaml`. Every condition and failure mode is tested with `ApplicationContextRunner`, which starts a tiny context in milliseconds.

**Key terms to know:**
- **IoC container / bean** — Spring creates and wires objects (beans) for you instead of your code calling `new`; `GreetingService` and the decorator are beans.
- **Dependency injection** — a bean receives its collaborators through its constructor; `GreetingService` gets the properties, an optional decorator and a `Clock`.
- **Property source and precedence** — the ordered places Boot reads settings from; later sources override earlier ones.
- **`Environment`** — Spring's merged view of all property sources and active profiles.
- **`@ConfigurationProperties` + `@Validated`** — binds a prefix to a typed record and validates it at startup; `GreetingProperties`.
- **Relaxed binding** — `default-language`, `defaultLanguage` and `GREETING_DEFAULTLANGUAGE` all bind to the same field.
- **Profile** — a named set of overrides (`application-dev.yaml`) activated per environment; profiles are additive.
- **Conditional bean** — a bean created only if a condition holds (`@ConditionalOnBooleanProperty`, `@Profile`); the seasonal decorator.
- **`ApplicationContextRunner`** — a test utility that starts a minimal context and asserts beans or startup failures.

**Q:** An environment variable and `application.yaml` set the same property — which wins?

**A:** The environment variable, because OS environment variables come later in Boot's precedence order than packaged config files. Command-line arguments beat both, and test properties beat everything in tests. I have a test for each combination so the README claims are proven, not remembered.

**Q:** Why prefer `@ConfigurationProperties` over `@Value`?

**A:** `@ConfigurationProperties` gives one typed, immutable object for a whole prefix, validated as a group at startup and documented through generated IDE metadata. `@Value("${greeting.x}")` reads one value at a time, is not validated together and a typo in the key just fails or uses a default somewhere. I use `@Value` only for one-off values.

**Q:** What is the difference between constructor and field injection, and which did you use?

**A:** Constructor injection passes dependencies as constructor parameters, so fields can be `final`, the object is never half-built, and a unit test can call `new GreetingService(props, decorator, clock)` without Spring. Field injection with `@Autowired` on a field hides dependencies and needs reflection to test. I use constructor injection everywhere; a single constructor needs no `@Autowired`.

**Q:** How do you make a bean optional, and how do you inject something that may not exist?

**A:** The decorator has `@ConditionalOnBooleanProperty("greeting.seasonal.enabled")`, so it only exists when the flag is true. `GreetingService` takes an `ObjectProvider<SeasonalDecorator>` (or `Optional<...>`), so it works either way. Two `ApplicationContextRunner` tests assert `hasSingleBean` and `doesNotHaveBean`.

**Q:** What happens if the configuration is invalid?

**A:** Validation runs when the properties bean is bound, during startup, so the application refuses to start and Boot prints a "failed to bind properties" report naming the field. That is the point: a broken deploy fails immediately and visibly instead of returning errors to the first user. The deployment must surface startup logs so the failure is easy to see.

**Q:** Someone flips `greeting.seasonal.enabled` at runtime and nothing changes. Why?

**A:** Conditions are evaluated once, while the context is being built; changing a property later does not create or destroy beans. For switches that must change while running, you need code that reads the value on each call — a feature-flag service like project 25 — not a conditional bean.

**Q:** How would this scale to 10 environments and 30 settings? What breaks first?

**A:** The number of profile files breaks first: combinations become a maze and nobody knows which value applies. I would keep few profiles, let the platform inject environment-specific values as environment variables or mounted files (`spring.config.import=configtree:`), and keep secrets in a vault. The `/actuator/env` and `configprops` endpoints, secured, show which source supplied each value.

**Q:** How did you make the seasonal logic testable?

**A:** I injected a `java.time.Clock` bean instead of calling `LocalDate.now()`. Tests pass `Clock.fixed(...)` set to a December date and assert the December suffix. Without that, the test would pass or fail depending on the month it runs in.

**Q:** Which environment variable sets `greeting.templates[pt-BR]`, and what about simple keys?

**A:** Simple keys map by upper-casing and replacing dots with underscores and removing dashes: `greeting.seasonal.enabled` is `GREETING_SEASONAL_ENABLED`. Map keys with special characters such as `pt-BR` cannot be expressed reliably in an environment variable name, so I set them in YAML with bracket notation, or use `SPRING_APPLICATION_JSON` if they must come from the environment.

**Common mistakes in answers:**
- "`application.yaml` always wins because it is the main file" — it is near the bottom of the precedence order.
- "Profiles replace the base config" — a profile file is layered on top of `application.yaml`, not instead of it.
- "Changing a property at runtime toggles a conditional bean" — conditions are evaluated once at startup.

### Self-check
1. Which environment variable sets `greeting.seasonal.enabled`? → `GREETING_SEASONAL_ENABLED`.
2. When is a `@ConditionalOnProperty` evaluated? → Once, while the context is being built.

### TL;DR
A trivial greeting with non-trivial configuration: typed and validated properties, proved precedence, profiles and conditional beans, all tested with `ApplicationContextRunner`.

## 6. Scheduled Daily Sales Report
**Level:** Beginner · **Time:** 1 weekend · **Prerequisites:** Ch 7, Ch 14

### What it is
A background job that runs every morning at 06:00 in the business's time zone, aggregates yesterday's orders from a database (revenue, order count, top products), writes a CSV file and emails a summary. It uses `@Scheduled` — Spring's annotation for running a method on a timer or a **cron expression** (a compact schedule format such as "minute hour day month weekday").

### The problem it solves
Every company has reports nobody wants to click a button for. Scheduled jobs look trivial until they run twice on two servers, skip a day after a daylight-saving change, fail halfway and leave half a file, or silently stop. This project builds a job that is **idempotent** (running it twice for the same day produces the same result once), observable and safe on several instances.

### Real-world examples
- The [Spring "Scheduling Tasks" guide](https://spring.io/guides/gs/scheduling-tasks) shows the basic `@Scheduled` setup you will extend.
- [ShedLock](https://github.com/lukas-krecan/ShedLock) is a widely used library that makes a scheduled task run on at most one node at a time by holding a lock row in a shared database.

### Requirements
**Functional**
- Every day at 06:00 Europe/London, build the report for the previous calendar day.
- Store each run in a `report_run` table (date, status, row count, file path, error).
- `POST /admin/reports/{date}/rerun` regenerates one day on demand.
- Email the summary to a configurable list; attach the CSV.

**Non-functional**
- The job for one date runs **at most once successfully**, even with 3 app instances.
- Report for 100,000 orders completes in under 30 seconds and uses constant memory (streamed rows).
- A failed run is visible within one minute (log + metric + run row with status `FAILED`).

### The architecture
```mermaid
flowchart TD
    SCH["@Scheduled cron - zone Europe/London"] --> LK["ShedLock - lock row"]
    LK --> RJ["ReportJob"]
    API["Admin rerun endpoint"] --> RJ
    RJ --> RR["ReportRepository - JdbcClient"]
    RR --> DB[("PostgreSQL - orders, report_run")]
    RJ --> CSV["CsvWriter - temp file then rename"]
    RJ --> MAIL["JavaMailSender"]
```
- **Scheduler** — `@EnableScheduling` plus `@Scheduled(cron = "0 0 6 * * *", zone = "Europe/London")`. Spring cron has six fields; the first is seconds.
- **ShedLock** — before running, inserts or updates a lock row with an expiry; other instances see the lock and skip.
- **ReportJob** — computes the date range, checks `report_run` for an existing success, aggregates, writes, mails and records the outcome.
- **ReportRepository** — SQL aggregation with `JdbcClient`; streaming rows for the CSV.
- **CsvWriter** — writes to a temporary file and renames it atomically, so readers never see half a file.

### Data model
```mermaid
erDiagram
    ORDERS ||--o{ ORDER_LINE : "has"
    ORDERS {
        bigint id PK
        timestamptz placed_at
        numeric total
    }
    ORDER_LINE {
        bigint order_id FK
        text sku
        int quantity
        numeric amount
    }
    REPORT_RUN {
        date report_date PK
        text status
        int order_count
        text file_path
        text error
        timestamptz finished_at
    }
```

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Scheduling | `@EnableScheduling`, `@Scheduled` | built in; virtual threads with `spring.threads.virtual.enabled=true` |
| Single-run lock | ShedLock with its JDBC provider | one table, works with any instance count |
| Data | `spring-boot-starter-jdbc`, `JdbcClient` | aggregation SQL is clearer by hand |
| Mail | `spring-boot-starter-mail` | `JavaMailSender`; a local SMTP catcher (e.g. GreenMail in tests) |
| Time | an injected `Clock` | testable "yesterday" |

### Milestones
1. **Aggregation query.** Revenue, count and top 5 SKUs for a date range. *Done when* a `@JdbcTest` with seeded orders asserts exact totals (use `BigDecimal`, never `double`).
2. **The job.** `ReportJob.run(LocalDate)` writes CSV and a `report_run` row. *Done when* running it twice for the same date leaves one success row and one file.
3. **Schedule.** Cron with an explicit zone; inject `Clock`. *Done when* a test with a fixed clock at 2026-03-29 06:00 London (a DST change day) computes the right "yesterday" boundaries in UTC.
4. **Single instance.** Add ShedLock. *Done when* two application contexts started in one test both trigger the job and only one run happens.
5. **Mail and failure handling.** *Done when* a GreenMail test receives the email, and a forced SQL failure records `FAILED` with the error message.

### Deep dive: the hard parts
- **Time zones and "yesterday".** Orders are stored as UTC instants; the business day is local. Compute `start = yesterday.atStartOfDay(zone).toInstant()` and `end = today.atStartOfDay(zone).toInstant()` and query `placed_at >= start AND placed_at < end`. A local day can be 23 or 25 hours long on DST changes; half-open ranges handle that.
- **Idempotency.** The `report_run` primary key on `report_date` is the guard: insert a `RUNNING` row first; if it already exists with `SUCCESS`, skip. A rerun explicitly overwrites.
- **Multiple instances.** `@Scheduled` fires in every JVM. ShedLock's `lockAtMostFor` must be longer than the job's worst runtime (or the lock expires mid-run and a second node starts) and `lockAtLeastFor` stops a fast job from running again on a node whose clock is slightly behind.
- **Overlapping runs.** One `@Scheduled` cron method never overlaps with itself (the next trigger is computed after the run finishes), but a manual rerun from the admin endpoint can overlap with the scheduled run. The `RUNNING` row (or the same ShedLock name) prevents that.

### Testing strategy
- Repository tests against PostgreSQL or H2 in PostgreSQL mode for the aggregation.
- Job tests calling `run(date)` directly, never waiting for the cron. Disable scheduling in tests behind a property (`@ConditionalOnBooleanProperty("app.jobs.enabled")`).
- A cron-expression test with `CronExpression.parse(...).next(...)` to prove the next fire times.

### Trade-offs, scaling and failure modes
- ShedLock is simple but relies on the database; if the database is down, the job cannot run anyway.
- For very large data, aggregate in SQL and stream rows; loading 100,000 entities into memory is the classic outage.
- For many jobs with dependencies and retries, a real scheduler (project 38, or Spring Batch in project 18) beats a growing pile of `@Scheduled` methods.

### Stretch goals
- Expose a metric `report.duration` and an alert if no `SUCCESS` row exists by 07:00.
- Generate a PDF or HTML report with a template engine.
- Backfill: `POST /admin/reports/backfill?from=&to=` with one run per day.

### Skills practised
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): `@Scheduled`, cron, ShedLock, testing jobs.
- [Ch 7 — Data access with JDBC and transactions](../chapters/07-data-access-jdbc-and-transactions/README.md): `JdbcClient`, aggregation, `@JdbcTest`.
- [Ch 15 — Observability and production](../chapters/15-observability-and-production/README.md): metrics for background jobs.

### In the interview
**30-second answer:** I built a daily sales report job that runs at 06:00 Europe/London, aggregates yesterday's orders with SQL, writes a CSV atomically and emails a summary. The key decision was making the job idempotent per date with a `report_run` table keyed by `report_date`, and adding ShedLock so only one of several instances runs it. The hardest problem was "yesterday" on daylight-saving days, which I solved by computing a half-open UTC range from local midnights. A 100,000-order report finishes in under 30 seconds with streamed rows and constant memory.

**Key terms to know:**
- **`@Scheduled` / `@EnableScheduling`** — Spring's annotation for running a method on a timer or cron; the 06:00 trigger.
- **Spring cron expression** — six fields, the first is seconds: `0 0 6 * * *`; always used with an explicit `zone`.
- **`fixedRate` vs `fixedDelay`** — start-to-start interval vs end-to-start interval; cron is calendar-based instead.
- **Idempotency** — running the same job for the same date twice has the effect of running it once; guaranteed by the `report_run` primary key.
- **Distributed lock (ShedLock)** — a lock row in a shared database so only one instance runs the job.
- **`lockAtMostFor` / `lockAtLeastFor`** — the lock's safety expiry if a node dies, and the minimum hold time to absorb clock skew.
- **Half-open range** — `start <= t < end`, which handles 23- and 25-hour days correctly.
- **Atomic file write** — write to a temp file then rename, so readers never see half a CSV.
- **Injected `Clock`** — lets tests fix "now", including DST change days.

**Q:** Your job runs on three pods. What happens?

**A:** `@Scheduled` fires in every JVM, so without coordination it runs three times. I use ShedLock with a JDBC lock table so one instance wins and the others skip, and I also make the job idempotent per date, so even a double run is harmless. The lock is an optimisation; idempotency is the real safety net.

**Q:** `fixedRate` vs `fixedDelay` vs cron?

**A:** `fixedRate` measures start to start, `fixedDelay` measures from the end of one run to the start of the next, and cron fires at calendar times. For a business report "every day at 06:00 London", cron with an explicit `zone` is the right choice; without a zone it uses the server's default time zone, which in containers is usually UTC.

**Q:** How did you compute "yesterday" correctly?

**A:** Orders are stored as UTC instants but the business day is local. I compute `start = yesterday.atStartOfDay(zone).toInstant()` and `end = today.atStartOfDay(zone).toInstant()` and query `placed_at >= start AND placed_at < end`. On a DST change the local day is 23 or 25 hours long, and the half-open range covers exactly that day with no gaps or overlaps.

**Q:** How do you choose `lockAtMostFor`?

**A:** It must be longer than the job's worst realistic runtime, otherwise the lock expires mid-run and another node starts a second copy. It must not be so long that a crashed node blocks the job for a day. I measured the 100,000-order run at under 30 seconds and set `lockAtMostFor` to 10 minutes, plus a short `lockAtLeastFor` so a fast run does not repeat on a node whose clock is slightly behind.

**Q:** What happens if the job fails halfway, after the CSV but before the email?

**A:** The run row stays `FAILED` with the error message, a metric is incremented and an error is logged, so it is visible within a minute. The CSV was written to a temp file and renamed only when complete, so no half file exists. The admin rerun endpoint regenerates the day, overwriting the previous attempt, because the job is idempotent per date.

**Q:** Why use `BigDecimal` for revenue?

**A:** `double` is binary floating point and cannot represent most decimal fractions exactly, so totals drift by fractions of a cent. `BigDecimal` with SQL `numeric` is exact. Tests assert exact totals, which is only possible with exact types.

**Q:** What changes at 100x data — 10 million orders a day?

**A:** Loading rows into memory is the first thing to break, so aggregation stays in SQL and CSV rows are streamed (`JdbcClient` query to a `Stream`, closed with try-with-resources). The aggregation query needs an index on `placed_at`, or a pre-aggregated daily table filled incrementally. For many dependent jobs with retries and partitioning, I would move to Spring Batch (project 18).

**Q:** How do you test a scheduled job without waiting until 06:00?

**A:** I call `ReportJob.run(date)` directly with a fixed `Clock`, including a DST change day. I test the cron separately with `CronExpression.parse("0 0 6 * * *").next(...)` to prove the next fire times. Scheduling is switched off in other tests behind a property so jobs do not fire during the suite.

**Q:** Show the scheduled method with a zone and a lock.

**A:**
```java
@Scheduled(cron = "0 0 6 * * *", zone = "Europe/London")
@SchedulerLock(name = "dailyReport",
    lockAtMostFor = "PT10M", lockAtLeastFor = "PT30S")
public void daily() {
    reportJob.run(LocalDate.now(clock).minusDays(1));
}
```
`clock` here must be zoned to Europe/London, or "yesterday" is computed in the wrong zone.

**Common mistakes in answers:**
- "Only one instance runs `@Scheduled`" — every instance runs it unless something coordinates.
- Using `between start and end` with `23:59:59` — this misses the last second and breaks on DST; use a half-open range.
- Treating the lock as enough — locks can expire or fail; the job itself must be idempotent.

### TL;DR
A cron job made production-safe: explicit time zones, half-open date ranges, an idempotent run table, a distributed lock, streamed output and visible failures.

## 7. Bookstore Catalog API with Problem Details and OpenAPI
**Level:** Beginner · **Time:** 1–2 weekends · **Prerequisites:** Ch 5, Ch 8

### What it is
A public-facing catalog API for books, authors and categories, designed as if strangers will integrate with it: a documented contract (OpenAPI), predictable error bodies (Problem Details), validation with clear messages, content negotiation (JSON and CSV), filtering, sorting and conditional requests. **OpenAPI** is a standard machine-readable description of an HTTP API; tools turn it into documentation pages and client code.

### The problem it solves
Internal APIs can get away with surprises; public ones cannot. Integrators need to know every field, every error and every limit up front, and they need errors they can handle in code. This project is about **API craft**: designing the contract first, then making the implementation obey it.

### Real-world examples
- [RFC 9457 "Problem Details for HTTP APIs"](https://www.rfc-editor.org/rfc/rfc9457) defines the error format Spring's `ProblemDetail` implements.
- [Zalando's RESTful API Guidelines](https://opensource.zalando.com/restful-api-guidelines/) are a public, detailed rule book (naming, pagination, errors, compatibility) you can measure your design against.

### Requirements
**Functional**
- `GET /api/books?author=&category=&q=&sort=title,asc&page=` with filtering and sorting on whitelisted fields.
- `GET /api/books/{isbn}`; `POST`/`PUT`/`DELETE` for admins (simple API-key header for now).
- `Accept: text/csv` on the list endpoint returns CSV.
- Validation: ISBN-13 checksum, price ≥ 0 with two decimals, at least one author.
- An OpenAPI document at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`.

**Non-functional**
- Every 4xx/5xx response is `application/problem+json` with a stable `type` URI.
- `GET /api/books/{isbn}` supports `ETag`/`If-None-Match` so unchanged books cost a 304 with no body.
- List p95 under 100 ms for 50,000 books with the right indexes.

### The architecture
```mermaid
flowchart TD
    C["Integrator"] --> F["ApiKeyFilter"]
    F --> BC["BookController"]
    BC --> BS["BookService"]
    BS --> BR["BookRepository - Spring Data JPA"]
    BR --> DB[("PostgreSQL")]
    BC --> CSV["CsvHttpMessageConverter"]
    BC -.-> EH["ProblemDetailsAdvice"]
    DOC["springdoc - OpenAPI and Swagger UI"] -.-> BC
```
- **ApiKeyFilter** — a servlet filter that checks an `X-API-Key` header on write methods (real auth comes in project 11).
- **BookController** — thin; uses `@Valid`, `Pageable` and `ResponseEntity` with `ETag`.
- **CsvHttpMessageConverter** — a custom `HttpMessageConverter` that writes lists of `BookDto` as CSV when the client asks for `text/csv`.
- **ProblemDetailsAdvice** — extends `ResponseEntityExceptionHandler` so Spring's own exceptions and yours share one format.
- **springdoc-openapi** — generates the OpenAPI document from annotations and types (use its Boot 4 line).

### Data model
```mermaid
erDiagram
    BOOK }o--o{ AUTHOR : "written by"
    BOOK }o--|| CATEGORY : "in"
    BOOK {
        text isbn PK
        text title
        numeric price
        int published_year
        bigint category_id FK
        bigint version
    }
    AUTHOR {
        bigint id PK
        text name
    }
    CATEGORY {
        bigint id PK
        text name UK
    }
```
The many-to-many between books and authors becomes a join table `book_author(isbn, author_id)` in the migration.

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Web | `spring-boot-starter-webmvc` | message converters, content negotiation |
| Validation | `spring-boot-starter-validation` + a custom `@Isbn` constraint | checksum logic in one reusable annotation |
| Persistence | Spring Data JPA + Flyway | filtering with `Specification` or Querydsl-free JPQL |
| Docs | springdoc-openapi (Boot 4 compatible line) | generated contract and UI |
| Errors | `ProblemDetail`, `ErrorResponseException` | RFC 9457 built in |

### Milestones
1. **Contract first.** Write the OpenAPI YAML by hand for three endpoints, review it, then implement. *Done when* the generated `/v3/api-docs` matches your hand-written contract for those endpoints (diff in a test).
2. **Validation.** A custom `@Isbn` constraint and messages from `ValidationMessages.properties`. *Done when* invalid ISBN, negative price and missing authors each produce a Problem Details body with a per-field `errors` array.
3. **Filtering and sorting.** Whitelist sort fields (reject `sort=password`). *Done when* tests show 400 for unknown sort fields and correct ordering for known ones.
4. **CSV and ETags.** *Done when* `Accept: text/csv` returns CSV and a second `GET` with `If-None-Match` returns 304.
5. **Error catalogue.** A table in your README listing every `type` URI and its meaning. *Done when* a test iterates all endpoints' error paths and asserts the content type is `application/problem+json`.

### Deep dive: the hard parts
- **Extending Problem Details.** `ProblemDetail.setProperty("errors", list)` adds custom members; clients must ignore unknown members, so adding fields is backwards compatible. Keep `title` stable per `type` and put specifics in `detail`.
- **Content negotiation.** Spring chooses a converter from the `Accept` header and the return type. Registering your CSV converter in a `WebMvcConfigurer` (Framework 7 adds a builder-style `configureMessageConverters`) makes `text/csv` just another representation of the same resource.
- **ETags.** A **strong ETag** is a hash or version that changes whenever the representation changes. Using the entity `version` column is cheap and exact. Spring's `ShallowEtagHeaderFilter` hashes the body instead — easy, but the server still does all the work.
- **Sort injection.** Passing `sort` straight into a query lets clients sort on non-indexed or private columns. Map public names to properties explicitly.

### Testing strategy
- Constraint validator unit tests (valid and invalid ISBNs, including checksum edge cases).
- `@WebMvcTest` for negotiation, ETags and errors.
- An OpenAPI snapshot test: fetch `/v3/api-docs` in a `@SpringBootTest` and compare with a committed file, so contract changes are always deliberate and reviewed.

### Trade-offs, scaling and failure modes
- Contract-first costs time up front but prevents accidental breaking changes; code-first is faster but your docs drift unless you snapshot them.
- Offset pagination on 50,000 rows is fine; at millions, switch to keyset pagination.
- CSV exports of the whole catalog should be streamed or produced asynchronously, not built in memory.

### Stretch goals
- Generate a Java client from your OpenAPI file and use it in a test.
- Add HATEOAS links (`_links.next`) with Spring HATEOAS.
- Add API versioning (preview of project 21) for a breaking change to the price field.

### Skills practised
- [Ch 5 — Spring MVC and REST APIs](../chapters/05-spring-mvc-and-rest/README.md): validation, Problem Details, content negotiation, springdoc.
- [Ch 8 — Spring Data JPA and Hibernate](../chapters/08-spring-data-jpa-and-hibernate/README.md): many-to-many, pagination and sorting, versions.
- [Ch 12 — Testing Spring applications](../chapters/12-testing-spring-apps/README.md): web slices, snapshot tests.

### In the interview
**30-second answer:** I built a public book catalog API designed for strangers to integrate with: an OpenAPI contract written first, RFC 9457 Problem Details for every error, a custom `@Isbn` validator, CSV through content negotiation, and ETags for conditional GETs. The key decision was contract-first, with a snapshot test that fails whenever the generated `/v3/api-docs` changes, so no breaking change ships by accident. The trickiest part was making Spring's own exceptions and mine produce the same error shape, which I did by extending `ResponseEntityExceptionHandler`. An unchanged book costs a 304 with no body, and lists stay under 100 ms for 50,000 books.

**Key terms to know:**
- **OpenAPI** — a machine-readable description of an HTTP API; springdoc generates it at `/v3/api-docs` and Swagger UI renders it.
- **Contract-first** — design and review the API description before writing code; milestone 1.
- **Problem Details (`ProblemDetail`)** — the RFC 9457 error body with a stable `type` URI; every 4xx/5xx here.
- **`ResponseEntityExceptionHandler`** — a Spring base class that already handles MVC's exceptions; extended so all errors share one format.
- **Custom constraint (`@Isbn`)** — a Bean Validation annotation plus a `ConstraintValidator` that checks the ISBN-13 checksum.
- **Content negotiation / `HttpMessageConverter`** — Spring picks a converter by the `Accept` header and return type; the CSV converter.
- **ETag / `If-None-Match` / 304** — a version tag for a representation; an unchanged resource returns 304 Not Modified.
- **Whitelisted sorting** — mapping public sort names to columns so clients cannot sort on arbitrary fields.
- **Many-to-many with join table** — books and authors linked through `book_author`.

**Q:** What is Problem Details and why use it?

**A:** RFC 9457 defines a standard JSON error body with `type`, `title`, `status`, `detail`, `instance` and optional extension members, served as `application/problem+json`. Clients can branch on the stable `type` URI instead of parsing messages. Spring supports it natively with `ProblemDetail` and `ErrorResponseException`, and I add an `errors` array for field-level validation failures.

**Q:** How does Spring pick JSON vs CSV?

**A:** Content negotiation: Spring reads the `Accept` header, looks at the handler's return type, and asks each registered `HttpMessageConverter` whether it can write that type as that media type. My `CsvHttpMessageConverter` claims `text/csv` for lists of `BookDto`. If nothing matches, the client gets 406 Not Acceptable.

**Q:** Contract-first or code-first — why?

**A:** For a public API I chose contract-first: designing the YAML first forces decisions about names, errors and pagination before code exists, and reviewers can comment on the contract. It costs time up front. Code-first is faster but the docs drift unless you snapshot the generated spec, which I also do as a safety net.

**Q:** How do you avoid breaking API clients?

**A:** Only additive changes in a version: new optional fields and new endpoints, never removing or renaming fields or tightening validation. Clients must ignore unknown fields. The OpenAPI snapshot test makes every contract change visible in code review, and the rare real breaking change goes into a new API version (project 21).

**Q:** How did you implement ETags, and why not `ShallowEtagHeaderFilter`?

**A:** I use the entity's `version` column as a strong ETag and return 304 if `If-None-Match` matches, using `WebRequest.checkNotModified(etag)`, before doing the expensive work. `ShallowEtagHeaderFilter` hashes the finished response body, which saves bandwidth but the server still loads and serializes everything. The version-based ETag saves both.

**Q:** What is sort injection and how did you prevent it?

**A:** Passing a client's `sort` parameter straight into a query lets them sort by private or unindexed columns, causing data leaks or slow queries. I map public names (`title`, `price`, `year`) to entity properties explicitly and return 400 Problem Details for anything else.

**Q:** What happens at 10x — 500,000 books and full CSV exports?

**A:** Offset pagination deep into the list slows down, so I would switch to keyset pagination. Building a full-catalog CSV in memory is the first real outage risk: I would stream it with `StreamingResponseBody` or generate it asynchronously and return a download link. Filtering needs indexes on author, category and a text index for `q`.

**Q:** How do you test that every error is `application/problem+json`?

**A:** A parameterized `@WebMvcTest` iterates the error paths (bad ISBN, unknown sort field, missing book, wrong method, unsupported media type) and asserts the content type and the `type` URI. Validator unit tests cover checksum edge cases separately, so failures point at the exact rule.

**Q:** Show a minimal custom exception mapped to Problem Details.

**A:**
```java
@ExceptionHandler(BookNotFoundException.class)
ProblemDetail notFound(BookNotFoundException e) {
    var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
        "No book with ISBN " + e.isbn());
    pd.setType(URI.create("https://api.example.com/errors/book-not-found"));
    pd.setTitle("Book not found");
    return pd;
}
```

**Common mistakes in answers:**
- Putting the specific detail in `title` — `title` should be stable per `type`; specifics go in `detail`.
- Saying an ETag "caches the response on the server" — it lets the client skip downloading an unchanged representation.
- Calling a renamed field a "minor change" — removing or renaming fields breaks clients; only additions are compatible.

### TL;DR
A catalog API designed for strangers: contract-first OpenAPI, RFC 9457 errors, custom validation, CSV via content negotiation, ETags and whitelisted sorting.

## 8. Expense Tracker with JdbcClient and Flyway
**Level:** Beginner · **Time:** 1–2 weekends · **Prerequisites:** Ch 7, Ch 8

### What it is
A personal-finance API: accounts, categories, expenses and monthly budgets, with a "where did my money go" summary per month. It uses plain SQL through **`JdbcClient`** — the fluent JDBC API added in Spring Framework 6.1 — and **Flyway** for schema migrations, so you see exactly which SQL runs. Money is stored as exact decimals.

### The problem it solves
Many teams use JPA for everything and never learn what the database can do on its own: constraints, aggregates, window functions, upserts. Money bugs — rounding with `double`, partial updates — are expensive and embarrassing. This project teaches SQL-first data access, transactions you control, and schema evolution that never needs manual fixes.

### Real-world examples
- [Firefly III](https://github.com/firefly-iii/firefly-iii) is a mature open-source personal finance manager; its feature set (budgets, categories, rules, reports) is a good map of where this project can grow.
- The [Flyway documentation](https://documentation.red-gate.com/flyway) explains versioned and repeatable migrations, checksums and baselining.

### Requirements
**Functional**
- CRUD for accounts, categories, expenses (amount, currency, date, note, category).
- Transfer between accounts creates two linked entries atomically.
- `GET /api/summary?month=2026-09` → totals per category, budget vs actual, top 5 expenses.
- CSV import of a bank statement (date, description, amount), skipping duplicates.

**Non-functional**
- Amounts stored as `numeric(19,4)` and handled as `BigDecimal`; no floating point anywhere.
- Every schema change is a Flyway migration; the app refuses to start if a migration was edited after being applied (checksum mismatch).
- Monthly summary under 50 ms for 100,000 expenses with an index on `(account_id, spent_on)`.

### The architecture
```mermaid
flowchart TD
    C["Client"] --> EC["ExpenseController and SummaryController"]
    EC --> ES["ExpenseService - @Transactional"]
    ES --> ER["ExpenseRepository - JdbcClient"]
    ES --> SR["SummaryRepository - aggregate SQL"]
    ER --> DB[("PostgreSQL")]
    SR --> DB
    FW["Flyway - V1, V2, V3 scripts"] --> DB
    IMP["CsvImporter"] --> ES
```
- **Controllers** — JSON in and out; validation of amounts and dates.
- **ExpenseService** — transaction boundary for transfers and imports.
- **Repositories** — `JdbcClient` with named parameters and `RowMapper`s or record mapping (`.query(Expense.class)`).
- **SummaryRepository** — one SQL statement per summary, using `GROUP BY` and `FILTER` clauses.
- **CsvImporter** — parses statements and computes a fingerprint per line for de-duplication.

### Data model
```mermaid
erDiagram
    ACCOUNT ||--o{ EXPENSE : "records"
    CATEGORY ||--o{ EXPENSE : "classifies"
    CATEGORY ||--o{ BUDGET : "limited by"
    ACCOUNT {
        bigint id PK
        text name
        text currency
    }
    CATEGORY {
        bigint id PK
        text name UK
    }
    EXPENSE {
        bigint id PK
        bigint account_id FK
        bigint category_id FK
        numeric amount
        date spent_on
        text note
        text fingerprint UK
        uuid transfer_id
    }
    BUDGET {
        bigint category_id FK
        text month
        numeric limit_amount
    }
```

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Data access | `spring-boot-starter-jdbc`, `JdbcClient` | explicit SQL, named parameters, record mapping |
| Migrations | `spring-boot-starter-flyway` | required in Boot 4 for auto-configuration |
| Database | PostgreSQL | `numeric`, `CHECK` constraints, `ON CONFLICT` |
| Money | `BigDecimal` with `RoundingMode.HALF_EVEN` | exact decimal arithmetic |

### Milestones
1. **Schema V1.** Tables with `CHECK (amount <> 0)` and foreign keys. *Done when* Flyway applies it on startup and a test inserting a bad row gets `DataIntegrityViolationException`.
2. **Repositories.** CRUD with `JdbcClient`. *Done when* `@JdbcTest` tests pass against PostgreSQL.
3. **Transfers.** Two rows sharing a `transfer_id` inside one `@Transactional` method. *Done when* a test that throws after the first insert leaves no rows.
4. **Summary SQL.** *Done when* a seeded month returns exact per-category totals and budget differences.
5. **Import with de-duplication.** A unique `fingerprint` (hash of account, date, amount, description) and `ON CONFLICT DO NOTHING`. *Done when* importing the same file twice adds rows once.
6. **Schema V2.** Add `currency` to expenses with a backfill. *Done when* the migration runs on a database containing V1 data.

### Deep dive: the hard parts
- **Money.** `0.1 + 0.2` is not `0.3` in `double`. Use `BigDecimal` from JSON to SQL, compare with `compareTo` (not `equals`, which also compares scale: `2.0` vs `2.00`), and round only at defined points.
- **Transactions are proxy-based.** `@Transactional` works only on calls that go through the Spring proxy, on public methods, and by default rolls back only for unchecked exceptions. A checked `IOException` during import commits the half-done work unless you set `rollbackFor`.
- **Migrations that never change.** Flyway stores a checksum per applied script. Editing V1 after it ran makes startup fail; the fix is always a new V-script. For data backfills on big tables, add the column nullable, backfill in batches, then add the constraint — the expand/contract approach of project 37.
- **Aggregates in SQL.** `SUM(amount) FILTER (WHERE amount < 0)` and `GROUP BY category_id` do in one round trip what would otherwise be a loop over thousands of objects.

### Testing strategy
- `@JdbcTest` with PostgreSQL (Testcontainers or the local server) — H2 does not support all PostgreSQL syntax.
- A Flyway migration test: start from an empty database, migrate to latest, assert the schema; then a test that migrates V1 data to V2.
- Transaction rollback tests that throw at chosen points.

### Trade-offs, scaling and failure modes
- SQL-first gives control and performance but more code than JPA; for this domain (few relationships, many aggregates) it is a good fit.
- Fingerprint de-duplication can merge two genuinely identical purchases on the same day; adding the bank's own transaction id when available fixes it.
- Multi-currency totals need exchange rates on a date — out of scope, but your schema should not make it impossible.

### Stretch goals
- Recurring expenses generated by a scheduled job.
- A running balance per account using a window function (`SUM(...) OVER (ORDER BY spent_on)`).
- Liquibase version of the migrations, to compare the two tools.

### Skills practised
- [Ch 7 — Data access with JDBC and transactions](../chapters/07-data-access-jdbc-and-transactions/README.md): `JdbcClient`, exception translation, `@Transactional`, rollback rules.
- [Ch 8 — Spring Data JPA and Hibernate](../chapters/08-spring-data-jpa-and-hibernate/README.md): Flyway basics.
- [Ch 9 — Spring Data beyond JPA, and caching](../chapters/09-spring-data-beyond-jpa-and-caching/README.md): Flyway vs Liquibase.

### In the interview
**30-second answer:** I built a personal-finance API with plain SQL through `JdbcClient`, Flyway migrations and `BigDecimal` money end to end. The key design choice was letting the database do what it is good at: `CHECK` constraints, foreign keys, a unique fingerprint for import de-duplication and `GROUP BY` aggregates, so a monthly summary for 100,000 expenses takes one query under 50 ms. The trickiest bug was a transfer that half-committed when a checked exception escaped `@Transactional`, which taught me the default rollback rules. Importing the same bank statement twice adds rows only once.

**Key terms to know:**
- **`JdbcClient`** — Spring's fluent JDBC API (Framework 6.1+) with named parameters and record mapping; all data access here.
- **Exception translation** — vendor SQL errors become Spring's `DataAccessException` hierarchy, e.g. `DataIntegrityViolationException` for a `CHECK` failure.
- **`@Transactional`** — declarative transaction boundary on a proxy; the transfer writes two rows atomically.
- **Rollback rules** — by default only `RuntimeException` and `Error` roll back; checked exceptions commit unless configured.
- **Flyway versioned migration and checksum** — scripts `V1__…` applied once; editing an applied script fails validation at startup.
- **`BigDecimal` / `numeric(19,4)`** — exact decimal types for money; compared with `compareTo`.
- **Fingerprint + `ON CONFLICT DO NOTHING`** — a hash of each statement line with a unique index makes imports idempotent.
- **Expand/contract migration** — add nullable, backfill, then constrain; used for the V2 currency column.

**Q:** Why not `double` for money?

**A:** Binary floating point cannot represent most decimal fractions exactly: `0.1 + 0.2` is `0.30000000000000004`. Over thousands of rows, sums drift. `BigDecimal` and SQL `numeric` are exact, and I round only at defined points with `HALF_EVEN`. I also compare with `compareTo`, because `equals` treats `2.0` and `2.00` as different.

**Q:** Does `@Transactional` roll back on a checked exception?

**A:** Not by default — only on unchecked exceptions (`RuntimeException`) and `Error`. A checked `IOException` during import would commit the half-done work. I set `rollbackFor = Exception.class` on that method; since Framework 6.2 you can also change the default for the whole app with `@EnableTransactionManagement(rollbackOn = RollbackOn.ALL_EXCEPTIONS)`.

**Q:** Why `JdbcClient` instead of JPA here?

**A:** This domain has few relationships and many aggregates, so hand-written SQL is clearer and faster, and I see exactly what runs. JPA would add entity mapping, dirty checking and N+1 risks without much benefit. The cost is more code for plain CRUD and manual mapping, which `JdbcClient`'s record mapping keeps small.

**Q:** Someone edited an applied Flyway script. What happens?

**A:** On startup Flyway compares the stored checksum with the file and fails validation, so the app does not start. The fix is to revert the edit and write a new migration with the change. `flyway repair` only realigns checksums and should be a deliberate, reviewed action, never a habit.

**Q:** How does the transfer stay atomic, and how did you prove it?

**A:** Both rows are inserted inside one `@Transactional` service method called through the Spring proxy, sharing a `transfer_id`. A test injects a failure after the first insert and asserts the table has no rows for that transfer afterwards. If the method were called from the same class, the proxy would be bypassed and the first row would commit.

**Q:** How does the import avoid duplicates, and when does it go wrong?

**A:** Each line gets a fingerprint — a hash of account, date, amount and description — stored with a unique index, and inserts use `ON CONFLICT (fingerprint) DO NOTHING`. Re-importing a file adds nothing. It wrongly merges two genuinely identical purchases on the same day, which the bank's own transaction id fixes when available.

**Q:** How would you add a NOT NULL column to a 50-million-row table without downtime?

**A:** Expand/contract: add the column nullable (instant in PostgreSQL), deploy code that writes it, backfill old rows in batches of a few thousand, then add the `NOT NULL` constraint (validated separately). One huge `UPDATE` would lock rows and bloat the table for minutes. Each step is its own Flyway migration.

**Q:** What changes at 100x data?

**A:** The monthly summary needs the composite index on `(account_id, spent_on)` to stay fast, and possibly a pre-aggregated monthly table. CSV import should be batched (`batchUpdate`) instead of one insert per line. H2 in tests would hide PostgreSQL-specific behaviour, so tests run on real PostgreSQL.

**Q:** Write the monthly per-category total with `JdbcClient`.

**A:**
```java
record CategoryTotal(long categoryId, BigDecimal total) {}

List<CategoryTotal> totals = jdbc.sql("""
    SELECT category_id, SUM(amount) AS total FROM expense
    WHERE account_id = :acc AND spent_on >= :from AND spent_on < :to
    GROUP BY category_id""")
  .param("acc", accountId).param("from", from).param("to", from.plusMonths(1))
  .query(CategoryTotal.class).list();
```

**Common mistakes in answers:**
- "`@Transactional` rolls back on any exception" — by default only unchecked exceptions and errors.
- Comparing `BigDecimal` with `equals` — scale differences make equal amounts unequal; use `compareTo`.
- "Just fix the old migration file" — applied migrations are immutable; write a new one.

### TL;DR
SQL-first data access with `JdbcClient`, exact money handling, transactional transfers, idempotent imports and Flyway migrations you never edit.

## 9. Audit-log Spring Boot Starter
**Level:** Beginner · **Time:** 1–2 weekends · **Prerequisites:** Ch 2, Ch 3, Ch 4

### What it is
A reusable library, packaged as a **Spring Boot starter**, that any application can add to its dependencies to get audit logging: annotate a service method with `@Audited(action = "ORDER_CANCELLED")` and the library records who did what, when, with which arguments and whether it succeeded. A **starter** is a dependency that brings libraries plus **auto-configuration** — classes that create beans automatically when certain conditions hold.

### The problem it solves
Auto-configuration feels like magic until you write one. Building a starter teaches you how Boot decides which beans to create, how to let users override your defaults, how proxies and aspects intercept calls, and why a missing `@EnableXxx` or a self-invocation silently disables a feature. It is also genuinely useful: audit trails are required in finance, health and admin tools.

### Real-world examples
- The Spring Boot reference chapter ["Creating Your Own Auto-configuration"](https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html) describes the module layout, `@AutoConfiguration` and condition annotations used here.
- [JaVers](https://javers.org/) is an open-source Java audit and object-diff library with Spring Boot starters — a real example of this pattern taken much further.

### Requirements
**Functional**
- `@Audited(action, resource = "#orderId")` on any public bean method; the resource is a SpEL expression over the arguments.
- Records: timestamp, principal (from Spring Security if present, else "anonymous"), action, resource, outcome (`SUCCESS`/`FAILURE`), duration, error class.
- Sinks: logging (default), JDBC table (if a `DataSource` exists and a property is set), or a user-provided `AuditSink` bean that replaces both.
- Properties: `audit.enabled`, `audit.sink=log|jdbc`, `audit.mask-arguments=password,cardNumber`.

**Non-functional**
- Overhead below 1 ms per audited call for the log sink.
- The library must not fail the business call if writing the audit record fails (configurable).
- Zero configuration needed for the default behaviour; everything overridable.

### The architecture
```mermaid
flowchart TD
    APP["Application bean method with @Audited"] --> PX["CGLIB proxy"]
    PX --> AS["AuditAspect - @Around"]
    AS --> AE["AuditEvent builder - SpEL, principal, timing"]
    AE --> SK{"AuditSink"}
    SK --> LS["LoggingAuditSink"]
    SK --> JS["JdbcAuditSink"]
    SK --> US["User-defined sink"]
    AC["AuditAutoConfiguration - conditions"] -.->|"creates"| AS
    AC -.->|"creates if missing"| LS
    AC -.->|"creates if DataSource and property"| JS
```
- **CGLIB proxy** — a generated subclass wrapping the bean, so calls pass through the aspect. Only calls from *outside* the bean are intercepted.
- **AuditAspect** — `@Around` advice: start timer, call `proceed()`, build the event, send to the sink, rethrow any exception unchanged.
- **AuditAutoConfiguration** — an `@AutoConfiguration` class listed in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, guarded by `@ConditionalOnBooleanProperty("audit.enabled", matchIfMissing = true)`.
- **Sinks** — `@ConditionalOnMissingBean(AuditSink.class)` lets the application replace the default by declaring its own.

### Data model
| Type | Purpose |
|---|---|
| `@Audited` annotation | `action`, `resource` (SpEL), `includeArgs` |
| `AuditEvent` record | `Instant at`, `principal`, `action`, `resource`, `outcome`, `Duration took`, `errorType`, `Map<String,Object> args` |
| `AuditSink` interface | `void write(AuditEvent event)` |
| `AuditProperties` | `@ConfigurationProperties("audit")` record |

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Modules | `audit-spring-boot-autoconfigure` + `audit-spring-boot-starter` | Boot's recommended split: logic vs dependency bundle |
| AOP | `spring-boot-starter-aspectj` (Boot 4 name, replaces `-aop`) | `@Aspect` support |
| Expressions | SpEL `StandardEvaluationContext` with method parameters | dynamic resource ids |
| Metadata | `spring-boot-configuration-processor` | IDE completion for `audit.*` properties |
| Tests | `ApplicationContextRunner` | assert beans per condition |

### Milestones
1. **Annotation and aspect inside one app.** *Done when* calling an `@Audited` method logs one line, and calling it from the same class logs nothing — write that test on purpose.
2. **Extract the auto-configuration module.** *Done when* a separate sample app gets auditing just by adding the starter dependency.
3. **Conditions.** Disabled by property; JDBC sink only with a `DataSource` and `audit.sink=jdbc`; user sink wins. *Done when* four `ApplicationContextRunner` tests cover the combinations.
4. **SpEL and masking.** *Done when* `resource = "#order.id"` resolves and arguments named `password` are logged as `***`.
5. **Failure isolation.** *Done when* a sink that throws does not change the business method's result, and a metric `audit.sink.failures` counts it.

### Deep dive: the hard parts
- **Ordering of auto-configurations.** Your JDBC sink needs the `DataSource` that Boot's own auto-configuration creates. Use `@AutoConfiguration(after = DataSourceAutoConfiguration.class)` and `@ConditionalOnBean(DataSource.class)`; otherwise the condition is evaluated before the data source exists and silently fails. The `--debug` condition report shows why a bean was or was not created.
- **Proxy limits.** Self-invocation, `private` and `final` methods are not intercepted. Document this in your starter README; the test from milestone 1 proves it.
- **Aspect order with transactions.** If both `@Transactional` and `@Audited` apply, which runs outside? With the audit aspect outside the transaction, a commit failure is reported as a failure; inside, you might record `SUCCESS` for a rolled-back change. Set `@Order` deliberately and test it.
- **Parameter names for SpEL.** `#order` works only if parameter names are available: compile with `-parameters` (Boot's parent POM does this).

### Testing strategy
- `ApplicationContextRunner` with `AutoConfigurations.of(AuditAutoConfiguration.class, ...)` for every condition.
- An aspect test with a small configuration and an in-memory sink capturing events.
- A sample application module whose `@SpringBootTest` proves the starter works end to end.

### Trade-offs, scaling and failure modes
- Synchronous sinks add latency to every audited call; an asynchronous queue is faster but can lose events on a crash — for compliance, write the audit row in the same transaction as the change.
- Logging arguments can leak personal data; masking by name is a minimum, explicit allow-lists are safer.
- AOP-based auditing misses changes made outside annotated methods (batch SQL, other services); database-level auditing (triggers, CDC) catches everything at the cost of business context.

### Stretch goals
- Publish events with `ApplicationEventPublisher` so applications can listen with `@TransactionalEventListener`.
- An Actuator endpoint `/actuator/audit` showing the last 100 events.
- Register sinks programmatically with Framework 7's `BeanRegistrar`, one per configured destination.

### Skills practised
- [Ch 2 — IoC and dependency injection](../chapters/02-ioc-and-dependency-injection/README.md): conditions, post-processors, `BeanRegistrar`.
- [Ch 3 — Configuration and auto-configuration](../chapters/03-configuration-and-auto-configuration/README.md): writing a custom starter, conditions, ordering, Boot 4 modularization.
- [Ch 4 — AOP and proxies](../chapters/04-aop-and-proxies/README.md): `@Around`, pointcuts on annotations, proxy limitations, aspect order.

### In the interview
**30-second answer:** I built a Spring Boot starter that gives any application audit logging: annotate a method with `@Audited` and an `@Around` aspect records who did what, the outcome and the duration. The key design was a conditional auto-configuration — logging sink by default, a JDBC sink only when a `DataSource` exists and a property asks for it, and any user-defined `AuditSink` replacing both through `@ConditionalOnMissingBean`. The hardest part was proxy behaviour: I wrote a test that proves self-invocation is not audited, and I ordered the aspect relative to `@Transactional` on purpose. Overhead is under 1 ms per call and a failing sink never breaks the business call.

**Key terms to know:**
- **Starter vs auto-configure module** — the starter is a dependency bundle; the autoconfigure module holds the `@AutoConfiguration` classes and conditions.
- **`AutoConfiguration.imports`** — the file under `META-INF/spring/` that lists auto-configuration classes for Boot to load.
- **Condition annotations** — `@ConditionalOnMissingBean`, `@ConditionalOnBean`, `@ConditionalOnClass`, `@ConditionalOnBooleanProperty`; decide which sink exists.
- **Auto-configuration ordering** — `@AutoConfiguration(after = DataSourceAutoConfiguration.class)` so `@ConditionalOnBean(DataSource.class)` sees the data source.
- **AOP proxy (JDK vs CGLIB)** — a wrapper object Spring puts in front of a bean so advice runs; Boot defaults to CGLIB subclass proxies.
- **`@Around` advice / `proceed()`** — code that runs before and after the target method and decides whether to call it.
- **Self-invocation** — a call via `this` skips the proxy, so the aspect never runs.
- **Advice order (`@Order`)** — which aspect wraps which; matters when combined with `@Transactional`.
- **SpEL** — Spring Expression Language, used for `resource = "#order.id"`.

**Q:** How does Boot know to load your auto-configuration?

**A:** At startup Boot reads `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` from every jar on the classpath and treats each listed class as an auto-configuration candidate. It then evaluates each class's conditions, after user configuration is processed, so user beans are known. `spring.factories` is no longer used for auto-configuration since Boot 3.

**Q:** How can an app override your default bean?

**A:** The default sink is declared with `@ConditionalOnMissingBean(AuditSink.class)`. Auto-configurations are processed after the application's own configuration, so if the user has declared an `AuditSink`, mine backs off. An `ApplicationContextRunner` test with a user configuration asserts that only their bean exists.

**Q:** Why did auditing not happen for one call?

**A:** Most likely the call never went through the proxy: it was a self-invocation (`this.cancel()`), or the method was `private` or `final`, which a CGLIB subclass cannot override. Milestone 1 includes a test that calls an audited method from the same class and asserts nothing was recorded, and the README documents the limitation.

**Q:** What is the difference between JDK dynamic proxies and CGLIB proxies?

**A:** A JDK proxy implements the bean's interfaces, so callers must use the interface type. CGLIB generates a subclass at runtime, so it works without interfaces but cannot intercept `final` methods or `final` classes. Spring Boot defaults to CGLIB (`spring.aop.proxy-target-class=true`).

**Q:** Your JDBC sink is never created even though the app has a database. Why?

**A:** Probably the condition ran before Boot's own `DataSourceAutoConfiguration`, so `@ConditionalOnBean(DataSource.class)` saw nothing. Fix it with `@AutoConfiguration(after = DataSourceAutoConfiguration.class)`. Running the app with `--debug` prints the condition evaluation report, which says exactly which condition did or did not match.

**Q:** How do `@Audited` and `@Transactional` interact on the same method?

**A:** Both are proxies' advice, and order decides which is outside. If the audit aspect is outside the transaction, a commit failure is recorded as `FAILURE`; if inside, it might record `SUCCESS` for a change that was rolled back. I give the audit aspect an explicit `@Order` so it wraps the transaction, and a test forces a commit failure to prove it.

**Q:** The audit sink is down. What happens to the business call?

**A:** By default the aspect catches sink exceptions, increments an `audit.sink.failures` metric and lets the business result through unchanged. For compliance use cases a property makes it strict, failing the call instead, because a change without an audit row may be worse than no change. Either way, the business method's own exception is always rethrown unchanged.

**Q:** What changes at 10x audit volume?

**A:** A synchronous JDBC insert per call adds latency and database load everywhere. An asynchronous queue with batched writes is much faster but can lose events on a crash. For compliance, the audit row should be written in the same transaction as the change; for analytics, ship events to a log pipeline instead.

**Q:** Write the aspect's core.

**A:**
```java
@Around("@annotation(audited)")
Object audit(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
    long t0 = System.nanoTime();
    try { Object r = pjp.proceed(); record(pjp, audited, SUCCESS, t0, null); return r; }
    catch (Throwable e) { record(pjp, audited, FAILURE, t0, e); throw e; }
}
```
`record` swallows its own failures so it never replaces the business result.

**Common mistakes in answers:**
- "Auto-configuration is loaded from `spring.factories`" — Boot 3 and 4 use the `AutoConfiguration.imports` file.
- "Making the method `private` keeps it audited" — proxies only intercept calls they can see; private, final and self-invoked calls are skipped.
- Forgetting ordering — `@ConditionalOnBean` depends on registration order, so auto-configurations must declare `after`.

### TL;DR
Build the "magic" yourself: an annotation, an `@Around` aspect and a conditional auto-configuration packaged as a starter, with tests for every condition and every proxy limitation.

## 10. Uptime Monitor
**Level:** Beginner · **Time:** 1–2 weekends · **Prerequisites:** Ch 6, Ch 14, Ch 15

### What it is
A small self-hosted monitoring service: you register URLs (often other apps' `/actuator/health` endpoints), it checks each one on an interval, records response time and status, detects when a site goes down or comes back, and sends a notification. It also exposes its **own** health and metrics through **Spring Boot Actuator**, the module that adds production endpoints such as `/actuator/health` and `/actuator/prometheus`.

### The problem it solves
You cannot fix what you do not know is broken. This project teaches concurrent outbound HTTP with strict timeouts, scheduled work, state transitions (UP → DOWN → UP) without alert spam, and how a service reports its own health to Kubernetes or a load balancer.

### Real-world examples
- [Uptime Kuma](https://github.com/louislam/uptime-kuma) is a popular self-hosted monitoring tool; its monitor types and notification integrations show what users expect.
- The [Spring Boot Actuator endpoints reference](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html) documents the health endpoint format your monitor will consume and expose.

### Requirements
**Functional**
- CRUD for monitors: URL, interval (30 s–10 min), expected status, optional keyword in the body, timeout.
- Each check stores status, latency and error; keep 7 days of history.
- A monitor goes DOWN after 3 consecutive failures and UP after 2 consecutive successes; each transition sends one notification (webhook or email).
- `GET /api/monitors/{id}/uptime?days=7` returns availability percentage and p95 latency.

**Non-functional**
- 500 monitors at 60-second intervals (about 8 checks per second) on one small instance.
- A hanging target never delays other checks: per-check timeout of at most 10 s.
- The monitor's own `/actuator/health/readiness` reports DOWN if its database is unreachable.

### The architecture
```mermaid
flowchart TD
    T["@Scheduled tick every 5 s"] --> DUE["DueMonitorFinder"]
    DUE --> EX["Virtual-thread executor"]
    EX --> CK["Checker - RestClient with timeouts"]
    CK --> TGT["Target sites"]
    CK --> RS["ResultStore"]
    RS --> DB[("PostgreSQL - monitors, checks")]
    RS --> SM["StateMachine - UP DOWN with thresholds"]
    SM --> NT["Notifier - webhook or mail"]
    ACT["Actuator - health, metrics"] -.-> DB
```
- **Tick** — a frequent `@Scheduled(fixedDelay = 5000)` that asks "which monitors are due?" instead of one scheduled task per monitor.
- **Executor** — `Executors.newVirtualThreadPerTaskExecutor()`; each check is a blocking HTTP call on its own cheap virtual thread.
- **Checker** — `RestClient` with connect/read timeouts, measures latency, validates status and keyword.
- **StateMachine** — counts consecutive results and changes status only when thresholds are crossed.
- **Notifier** — sends one message per transition, with retries.
- **Actuator** — exposes health groups and Micrometer metrics (`monitor.checks`, `monitor.latency`).

### Data model
```mermaid
erDiagram
    MONITOR ||--o{ CHECK_RESULT : "produces"
    MONITOR {
        bigint id PK
        text url
        int interval_seconds
        int expected_status
        text keyword
        text state
        int consecutive_failures
        int consecutive_successes
        timestamptz next_check_at
    }
    CHECK_RESULT {
        bigint id PK
        bigint monitor_id FK
        timestamptz checked_at
        boolean ok
        int status_code
        int latency_ms
        text error
    }
```

### Tech stack
| Component | Choice | Why |
|---|---|---|
| HTTP | `RestClient` with JDK `HttpClient` | timeouts, virtual-thread friendly |
| Concurrency | virtual threads | thousands of waiting checks are cheap |
| Scheduling | `@Scheduled` tick + `next_check_at` column | one query finds due work |
| Ops | `spring-boot-starter-actuator`, Micrometer, Prometheus registry | self-monitoring |
| Data | `JdbcClient`, Flyway | simple tables, retention delete job |

### Milestones
1. **Single check.** *Done when* tests against a local stub server cover 200, 500, wrong keyword and a timeout.
2. **Scheduling loop.** Due-monitor query with `next_check_at <= now()`. *Done when* 50 monitors with a fake clock are each checked exactly once per interval.
3. **State machine.** *Done when* a unit test feeding fail, fail, fail, ok, ok produces exactly one DOWN and one UP notification.
4. **Uptime report.** SQL for availability and p95 (`percentile_cont(0.95)`). *Done when* seeded results produce the expected numbers.
5. **Self-observability.** Readiness group including the database, metrics per monitor. *Done when* stopping the database makes `/actuator/health/readiness` return 503 while `/actuator/health/liveness` stays 200.

### Deep dive: the hard parts
- **Timeouts and bounded concurrency.** Virtual threads make waiting cheap, but 500 simultaneous slow targets still hold 500 sockets. Cap in-flight checks (a `Semaphore`, or Framework 7 `@ConcurrencyLimit` on the check method) and enforce a total deadline per check.
- **Flapping.** A site that fails every other check would send endless alerts without thresholds. Separate thresholds for DOWN and UP (**hysteresis**) keep notifications meaningful.
- **Liveness vs readiness.** Liveness answers "should Kubernetes restart me?" and should almost never depend on external systems; readiness answers "should I receive traffic?" and may include the database. Putting the database in liveness turns a database blip into a restart storm.
- **Data retention.** At 8 checks per second you write about 700,000 rows a day. A nightly delete of rows older than 7 days (in batches) or a partitioned table keeps the database small.

### Testing strategy
- Stub HTTP servers on random ports (a tiny `HttpServer` from the JDK works) for realistic timeouts.
- A fixed `Clock` and calling the tick method directly for scheduling logic.
- Actuator health tests with `@SpringBootTest` and a stopped or mocked dependency.

### Trade-offs, scaling and failure modes
- One instance is a single point of failure for monitoring itself; two instances need a lock or partitioning of monitors (project 38).
- Checking from one network location cannot tell "site down" from "my network down"; real services check from several regions and require agreement.
- Notification channels fail too; retry with back-off and record undelivered alerts.

### Stretch goals
- A public status page rendered server-side.
- TLS certificate expiry checks with a warning 14 days ahead.
- Multi-region agents that report to a central service.

### Skills practised
- [Ch 6 — REST clients and HTTP interfaces](../chapters/06-rest-clients-and-http-interfaces/README.md): `RestClient`, timeouts, error handling.
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): `@Scheduled`, executors, virtual threads.
- [Ch 15 — Observability and production](../chapters/15-observability-and-production/README.md): Actuator, health groups, probes, Micrometer.
- [Ch 4 — AOP and proxies](../chapters/04-aop-and-proxies/README.md): `@ConcurrencyLimit`.

### In the interview
**30-second answer:** I built an uptime monitor that checks 500 URLs every minute, records latency and status, and sends one notification per UP/DOWN transition. The key design decision was a single `@Scheduled` tick every 5 seconds that queries `next_check_at <= now()`, with each check running as a blocking `RestClient` call on a virtual thread, capped by a concurrency limit and strict timeouts. The hardest part was alert spam from flapping sites, which I solved with hysteresis: 3 failures to go DOWN, 2 successes to come UP. The monitor's own readiness probe goes DOWN when its database is unreachable while liveness stays UP.

**Key terms to know:**
- **Virtual threads** — lightweight JDK threads (Java 21+) that make blocking I/O cheap; each check runs on one.
- **Bounded concurrency (`Semaphore`, `@ConcurrencyLimit`)** — caps how many checks run at once, because virtual threads have no pool limit.
- **Connect/read timeout and deadline** — maximum waits per check, so one hanging target cannot hold resources.
- **Due-work query** — one SQL query for monitors whose `next_check_at` has passed, instead of one timer per monitor.
- **Hysteresis / flapping** — different thresholds for going DOWN and coming UP so a flickering site does not spam alerts.
- **Spring Boot Actuator** — production endpoints such as `/actuator/health` and `/actuator/prometheus`.
- **Liveness vs readiness probe** — "should I be restarted?" vs "should I receive traffic?"; health groups in Actuator.
- **Micrometer** — Spring's metrics facade; `monitor.checks` and `monitor.latency` timers.
- **Data retention** — deleting or partitioning old check rows; about 700,000 rows a day here.

**Q:** What goes into liveness vs readiness?

**A:** Liveness should only reflect whether the app itself can make progress — if it fails, Kubernetes restarts the pod. Readiness says whether it can serve traffic now and may include the database. Putting the database in liveness turns a database blip into restarts of every pod at once, a restart storm.

**Q:** How do you check 500 URLs concurrently without a huge thread pool?

**A:** Each check is a blocking `RestClient` call on its own virtual thread from `Executors.newVirtualThreadPerTaskExecutor()`; while it waits on the network the carrier thread is free. Virtual threads remove the thread cost but not the socket and memory cost, so I cap in-flight checks with a semaphore or `@ConcurrencyLimit` and give each check a 10-second deadline.

**Q:** Why one tick plus a due-query instead of one scheduled task per monitor?

**A:** Monitors are added, edited and deleted at runtime, and one timer per monitor would have to be created and cancelled dynamically and would be lost on restart. The tick plus `next_check_at` column keeps the schedule in the database, survives restarts, and makes "what is due?" one indexed query.

**Q:** How do you avoid alert spam?

**A:** The state machine counts consecutive results and changes state only when a threshold is crossed: 3 failures to go DOWN, 2 successes to go UP. Notifications are sent only on a state transition, never per failed check. A site failing every other check never crosses the threshold, which is exactly the hysteresis I want.

**Q:** What happens if you run two instances of the monitor?

**A:** Both ticks see the same due monitors and every site is checked twice, with duplicate alerts. I would claim work atomically — `UPDATE ... SET next_check_at = ... WHERE id IN (SELECT ... FOR UPDATE SKIP LOCKED)` — or partition monitors across instances. Project 38 builds exactly that.

**Q:** Your own network goes down. What do you report?

**A:** Every target fails at once and, naively, every monitor goes DOWN. A single checking location cannot tell "site down" from "my network down". Real services check from several regions and require agreement; a cheaper guard is a canary check against a known-good target that suppresses alerts when it fails too.

**Q:** What changes at 10x — 5,000 monitors?

**A:** About 80 checks per second and 7 million rows a day, so the history table becomes the bottleneck. I would partition `check_result` by day so retention is a cheap partition drop, batch the inserts, and keep pre-aggregated hourly uptime numbers for the report. The concurrency cap and timeouts stay the main defence against slow targets.

**Q:** How do you test timeouts and thresholds?

**A:** For timeouts I start a local JDK `HttpServer` on a random port that sleeps longer than the timeout, and assert the check fails within the deadline. The state machine is a plain unit test feeding fail, fail, fail, ok, ok and asserting exactly one DOWN and one UP notification. Scheduling uses a fixed `Clock` and calls the tick method directly.

**Q:** Show how to cap concurrency on the check method in Framework 7.

**A:**
```java
@ConcurrencyLimit(50)
public CheckResult check(Monitor m) {
    long t0 = System.nanoTime();
    var resp = restClient.get().uri(m.url()).retrieve().toBodilessEntity();
    return CheckResult.ok(resp.getStatusCode(), Duration.ofNanos(System.nanoTime() - t0));
}
```
It needs `@EnableResilientMethods` and a call through the proxy; extra callers wait until a slot frees.

**Common mistakes in answers:**
- "Virtual threads mean no limits are needed" — they remove thread cost, not socket, memory or target-side load.
- Putting the database in the liveness probe — this causes restart storms on a database blip.
- Alerting on every failed check — without thresholds, a flapping site floods the notification channel.

### TL;DR
A monitoring service that checks many URLs concurrently with virtual threads and strict timeouts, turns noisy results into clean UP/DOWN transitions, and exposes its own health correctly.

## 11. Blog Platform with Spring Security and JWT
**Level:** Intermediate · **Time:** 2 weeks · **Prerequisites:** Ch 8, Ch 10, Ch 11

### What it is
A multi-author blog API (articles, comments, tags, follows, favourites) secured with **JSON Web Tokens**. A **JWT** is a compact, signed string holding claims such as "user 42, role AUTHOR, expires 10:15"; anyone with the public key can verify it was issued by you and not altered. Users log in once with a password, receive a short-lived **access token** and a longer-lived **refresh token**, and send `Authorization: Bearer <token>` on every request. The API acts as an **OAuth2 resource server**: Spring Security validates the token on each call without a server-side session.

### The problem it solves
Stateless token authentication is the default for APIs consumed by single-page apps and mobile clients, and it is the most common Spring Security interview topic. It is also easy to get dangerously wrong: weak signing keys, tokens that never expire, no way to log out, ownership checks missing on update endpoints. This project builds the whole chain and tests each security rule.

### Real-world examples
- [RealWorld](https://github.com/gothinkster/realworld) specifies a Medium-style blog API (articles, comments, tags, profiles, token auth) implemented in dozens of back-end frameworks — use its spec as your functional contract and compare implementations.
- [RFC 7519](https://www.rfc-editor.org/rfc/rfc7519) defines JWT; the [Spring Security JWT resource server reference](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html) describes the validation pipeline you will configure.

### Requirements
**Functional**
- Register, log in (`POST /api/auth/login` → access + refresh tokens), refresh, log out (revoke refresh token).
- Articles: anyone can read published articles; AUTHORs create and edit **their own**; EDITORs can edit or unpublish any; ADMINs manage users.
- Comments by any logged-in user; delete by comment author or article author.
- Feed of articles from followed authors, paginated.

**Non-functional**
- Access tokens expire in 15 minutes, refresh tokens in 14 days and rotate on each use.
- Tokens signed with RS256 (an RSA key pair); the private key never leaves the auth component; a JWKS endpoint publishes the public key.
- Passwords hashed with a slow adaptive function (bcrypt or Argon2) via `DelegatingPasswordEncoder`.
- Login endpoint limited to 5 attempts per minute per account and per IP.

### The architecture
```mermaid
flowchart LR
    SPA["SPA or mobile app"] -->|"POST /auth/login"| AUTH["AuthController"]
    AUTH --> AM["AuthenticationManager - DaoAuthenticationProvider"]
    AM --> US["UserDetailsService - users table"]
    AUTH --> TI["TokenIssuer - NimbusJwtEncoder RS256"]
    AUTH --> RT[("refresh_token table - hashed")]
    SPA -->|"Bearer access token"| FC["SecurityFilterChain - BearerTokenAuthenticationFilter"]
    FC --> JD["JwtDecoder - signature, exp, iss, aud"]
    FC --> API["Article and Comment controllers"]
    API --> MS["@PreAuthorize ownership checks"]
    API --> DB[("PostgreSQL")]
```
- **AuthController** — login, refresh and logout; the only place passwords are checked.
- **AuthenticationManager / DaoAuthenticationProvider** — loads the user and compares the password hash.
- **TokenIssuer** — builds claims (`sub`, `roles`, `iat`, `exp`, `iss`, `aud`) and signs with `NimbusJwtEncoder`.
- **SecurityFilterChain** — configured with the Security 7 lambda DSL; `oauth2ResourceServer(o -> o.jwt(...))` adds the bearer-token filter.
- **JwtDecoder** — verifies the signature and standard claims; a `JwtAuthenticationConverter` maps `roles` to `GrantedAuthority`s.
- **Method security** — `@PreAuthorize("@articleSecurity.canEdit(#id, authentication)")` for ownership rules.

### Data model
```mermaid
erDiagram
    APP_USER ||--o{ ARTICLE : "writes"
    APP_USER ||--o{ COMMENT : "writes"
    ARTICLE ||--o{ COMMENT : "has"
    APP_USER ||--o{ REFRESH_TOKEN : "holds"
    APP_USER ||--o{ FOLLOW : "follows"
    APP_USER {
        bigint id PK
        text username UK
        text email UK
        text password_hash
        text role
    }
    ARTICLE {
        bigint id PK
        text slug UK
        bigint author_id FK
        text title
        text body
        text status
        timestamptz published_at
    }
    COMMENT {
        bigint id PK
        bigint article_id FK
        bigint author_id FK
        text body
    }
    REFRESH_TOKEN {
        uuid id PK
        bigint user_id FK
        text token_hash UK
        uuid family_id
        timestamptz expires_at
        timestamptz revoked_at
    }
    FOLLOW {
        bigint follower_id FK
        bigint followee_id FK
    }
```

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Security | `spring-boot-starter-security` + `-security-oauth2-resource-server` | filter chain, JWT validation |
| Token minting | `NimbusJwtEncoder.withKeyPair(...)` (Security 7 builder) | standard JOSE library, no hand-rolled crypto |
| Passwords | `PasswordEncoderFactories.createDelegatingPasswordEncoder()` | `{bcrypt}` prefix allows future upgrades |
| Data | Spring Data JPA, Flyway | relationships and pagination |
| Tests | `spring-boot-starter-security-test` | `jwt()` request post-processor, `@WithMockUser` |

### Milestones
1. **Users and passwords.** Registration with hashing. *Done when* the stored hash starts with `{bcrypt}` and login with a wrong password returns 401.
2. **Issue and validate tokens.** *Done when* a test logs in, calls a protected endpoint with the token (200), without it (401), and with an expired or tampered token (401 with a `WWW-Authenticate` header).
3. **Roles and ownership.** *Done when* tests prove an AUTHOR cannot edit someone else's article (403), an EDITOR can, and anonymous users can read published articles only.
4. **Refresh rotation and logout.** *Done when* reusing an already-used refresh token revokes the whole token family and returns 401.
5. **Hardening.** CORS for the SPA origin only, login rate limiting, security headers. *Done when* a preflight from an unknown origin is rejected and the 6th login in a minute returns 429.

### Deep dive: the hard parts
- **Logout with stateless tokens.** A JWT stays valid until it expires; the server cannot "delete" it. Keep access tokens short (15 minutes), make refresh tokens server-side records you can revoke, and accept that a stolen access token works for at most its lifetime. A denylist of token ids (`jti`) checked on each request is possible but reintroduces state.
- **Refresh-token rotation and reuse detection.** Each refresh returns a new refresh token and marks the old one used. If a used token is presented again, someone copied it: revoke every token in that family. Store only a hash of the token, like a password.
- **401 vs 403.** 401 means "who are you?" (missing or invalid credentials) and includes `WWW-Authenticate: Bearer`; 403 means "I know who you are, and you may not". Tests should assert both precisely.
- **Ownership is data-dependent.** URL rules (`requestMatchers("/api/articles/**").hasRole("AUTHOR")`) cannot know who owns article 7. Put that check in method security with a bean that queries ownership, or in the service with a repository query `where id = ? and author_id = ?`.

### Testing strategy
- `@WebMvcTest` with `spring-security-test`: `.with(jwt().authorities(...))` builds a token without real signing, so authorization rules are tested fast.
- Full `@SpringBootTest` for the real login → token → call flow with the actual encoder and decoder.
- A test matrix (role × endpoint × owner/non-owner) generated with `@ParameterizedTest`, so no rule is forgotten.

### Trade-offs, scaling and failure modes
- Self-issued JWTs are fine for one product; once several apps need single sign-on, MFA or social login, move to a real authorization server (project 34) and keep this API as a pure resource server.
- Stateless validation scales horizontally with no session store; the cost is revocation delay.
- Key rotation: publish two public keys in the JWKS during a rotation window, sign with the new one, retire the old after the longest token lifetime.

### Stretch goals
- Store tokens for the browser in an HttpOnly cookie via a BFF (project 40) instead of JavaScript memory.
- Add one-time-token (magic link) login, supported natively by Spring Security.
- Full-text search on articles with PostgreSQL `tsvector`.

### Skills practised
- [Ch 10 — Spring Security fundamentals](../chapters/10-spring-security-1-fundamentals/README.md): filter chain, lambda DSL, password storage, 401 vs 403, method security, CORS, testing.
- [Ch 11 — Tokens and OAuth2](../chapters/11-spring-security-2-tokens-and-oauth2/README.md): JWT, resource server, `NimbusJwtEncoder`, claims to authorities, refresh and revocation, rate limiting.
- [Ch 8 — Spring Data JPA and Hibernate](../chapters/08-spring-data-jpa-and-hibernate/README.md): relationships and feeds.

### In the interview
**30-second answer:** I built a RealWorld-style blog API where users log in with a password, get a 15-minute RS256 access token and a rotating 14-day refresh token, and every request is validated by Spring Security as an OAuth2 resource server. The key decision was splitting authorization into URL rules for roles and method security for ownership, because "may this author edit article 7?" depends on data. The hardest part was logout with stateless tokens, which I solved with server-side, hashed refresh tokens and family-wide revocation when a used token is replayed. A parameterized test matrix proves every 401 and 403 across roles and owners.

**Key terms to know:**
- **Security filter chain** — the ordered servlet filters Spring Security puts in front of the app; configured as a `SecurityFilterChain` bean.
- **Authentication vs authorization** — proving who you are vs deciding what you may do.
- **JWT (claims, signature, `exp`)** — a signed token carrying claims; validated by `JwtDecoder` on each request.
- **RS256 / JWKS** — signing with an RSA private key, verifying with the public key published as a JSON Web Key Set.
- **Resource server (`BearerTokenAuthenticationFilter`)** — reads `Authorization: Bearer`, validates the token and sets the security context.
- **Access vs refresh token** — short-lived token sent on every call vs long-lived token used only to get new access tokens.
- **Refresh-token rotation and reuse detection** — each refresh issues a new token; replay of an old one revokes the family.
- **Method security (`@PreAuthorize`)** — authorization checks on service methods, used for ownership.
- **`DelegatingPasswordEncoder`** — stores `{bcrypt}`-prefixed hashes so the algorithm can be upgraded.
- **401 vs 403** — not authenticated vs authenticated but not allowed.

**Q:** Walk through what happens on a request with a bearer token.

**A:** The request passes the filter chain; `BearerTokenAuthenticationFilter` extracts the token and hands it to the `JwtAuthenticationProvider`. The `JwtDecoder` checks the signature against the public key and validates `exp`, `iss` and `aud`; the `JwtAuthenticationConverter` maps the `roles` claim to authorities. The resulting `Authentication` is stored in the `SecurityContext`, URL rules run in the `AuthorizationFilter`, and `@PreAuthorize` runs on the service method.

**Q:** How do you log a user out when you use JWTs?

**A:** A JWT stays valid until it expires, so I keep access tokens short (15 minutes), revoke the server-side refresh token on logout, and accept that a stolen access token works for at most its remaining lifetime. If immediate revocation is required, I would add a `jti` denylist checked on each request or use opaque tokens with introspection — both reintroduce server-side state.

**Q:** Why RS256 instead of HS256?

**A:** With an asymmetric key only the issuer holds the private key, and any service can verify tokens with the public key from the JWKS. With HS256 every verifier holds the shared secret that can also mint tokens, so one leaked service compromises all. RS256 also makes key rotation easy: publish two keys during the rotation window.

**Q:** Where do you check that a user owns the article?

**A:** In method security or the service layer, because ownership depends on data; URL rules only see roles and paths. I use `@PreAuthorize("@articleSecurity.canEdit(#id, authentication)")`, where the bean runs `exists where id = ? and author_id = ?`. EDITORs bypass ownership by role.

**Q:** What is the difference between 401 and 403?

**A:** 401 means the request has missing or invalid credentials and comes with `WWW-Authenticate: Bearer`, telling the client to authenticate. 403 means the server knows who you are and you are not allowed. An AUTHOR editing someone else's article gets 403; an expired token gets 401.

**Q:** How does refresh-token reuse detection work?

**A:** Each refresh token belongs to a family; using it marks it used and issues a new one in the same family. If a used token is presented again, someone has a copy, so I revoke every token in that family and the user must log in again. Tokens are stored only as hashes, like passwords.

**Q:** What happens at 10x users and several instances?

**A:** Access-token validation is stateless, so instances scale horizontally with no session store; the JWKS is cached. The refresh table and login endpoint become the hot spots, so they need indexes and rate limits (5 attempts per minute per account and IP). Once several apps need single sign-on or MFA, I would move token issuing to an authorization server (project 34).

**Q:** How do you test the security rules quickly?

**A:** In `@WebMvcTest` with `spring-security-test`, `.with(jwt().authorities(...))` builds an authenticated request without real signing, so each rule is a millisecond test. A `@ParameterizedTest` matrix covers role × endpoint × owner/non-owner. One `@SpringBootTest` runs the real login → token → call flow with the actual encoder and decoder.

**Q:** Show the filter-chain configuration.

**A:**
```java
@Bean SecurityFilterChain api(HttpSecurity http) throws Exception {
    return http
      .authorizeHttpRequests(a -> a
         .requestMatchers(HttpMethod.GET, "/api/articles/**").permitAll()
         .requestMatchers("/api/auth/**").permitAll()
         .anyRequest().authenticated())
      .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
      .sessionManagement(s -> s.sessionCreationPolicy(STATELESS)).build();
}
```

**Common mistakes in answers:**
- "JWTs are encrypted" — a signed JWT is only base64url-encoded; anyone can read its claims, so never put secrets in it.
- "Logout deletes the JWT on the server" — there is nothing to delete; you revoke refresh tokens and wait for expiry or add a denylist.
- Using `hasRole` URL rules for ownership — roles cannot express "only the author of this article".

### TL;DR
A RealWorld-style blog API with password login, RS256 access tokens, rotating refresh tokens, role and ownership rules, and a test matrix proving every 401 and 403.

## 12. Photo Journal with OAuth2 Login and a Resource Server
**Level:** Intermediate · **Time:** 2 weeks · **Prerequisites:** Ch 10, Ch 11

### What it is
Two cooperating applications. **Journal Web** is a server-rendered web app where users "Sign in with Google" (or GitHub, via an identity provider) — this is **OAuth2 login**, where an external **identity provider (IdP)** authenticates the user and your app receives an **ID token** describing them. **Photo API** is a separate REST service that stores photo entries and accepts only **access tokens** issued by the same IdP — it is an **OAuth2 resource server**. Journal Web calls Photo API on the user's behalf, attaching the user's access token.

### The problem it solves
Real systems rarely own passwords any more; they delegate login to Google, Microsoft, Okta or Keycloak. You need to understand the moving parts — authorization code flow, PKCE, ID tokens vs access tokens, scopes, audiences, token relay — to configure Spring Security correctly and debug the inevitable redirect loops. This project separates the two roles (client and resource server) so each is clear.

### Real-world examples
- The Spring tutorial ["Spring Boot and OAuth2"](https://spring.io/guides/tutorials/spring-boot-oauth2) builds social login with GitHub and Google, the same client side as Journal Web.
- [Keycloak](https://www.keycloak.org/docs/latest/server_admin/) is an open-source IdP that can broker logins from GitHub or Google and issue JWT access tokens for your own APIs — the piece GitHub's plain OAuth apps cannot provide.

### Requirements
**Functional**
- Journal Web: login with the IdP, show "my journal", upload a caption and photo, log out (both locally and at the IdP — RP-initiated logout).
- Photo API: `GET/POST/DELETE /api/entries`, accessible only with a valid access token having scope `journal.read` or `journal.write`; users see only their own entries (`sub` claim).
- A nightly job in Photo API's ecosystem uses the **client credentials** flow (machine-to-machine, no user) to call an admin endpoint.

**Non-functional**
- Authorization code flow **with PKCE** (Proof Key for Code Exchange: a one-time secret that stops stolen codes being redeemed) — Security 7 uses it by default.
- Photo API validates `iss`, `aud`, `exp` and signature using the IdP's JWKS, cached and refreshed on key rotation.
- No tokens ever reach browser JavaScript: they stay in Journal Web's server-side session.

### The architecture
```mermaid
sequenceDiagram
    participant B as Browser
    participant W as Journal Web - OAuth2 client
    participant I as IdP - Keycloak
    participant A as Photo API - resource server
    B->>W: GET /journal
    W->>B: 302 to IdP authorize with PKCE challenge
    B->>I: login and consent
    I->>B: 302 back with code
    B->>W: GET /login/oauth2/code/idp?code=...
    W->>I: exchange code plus verifier for tokens
    I->>W: ID token, access token, refresh token
    W->>A: GET /api/entries with Bearer access token
    A->>A: verify JWT via JWKS, check scope and aud
    A->>W: 200 entries for sub
    W->>B: HTML page
```
- **Journal Web** — `oauth2Login()` handles the redirect dance; the `OAuth2AuthorizedClientManager` stores tokens per user session and refreshes them.
- **IdP** — authenticates the user, issues tokens, publishes `/.well-known/openid-configuration` and a JWKS.
- **Photo API** — `oauth2ResourceServer(o -> o.jwt(...))` with `spring.security.oauth2.resourceserver.jwt.issuer-uri`; scopes become `SCOPE_journal.read` authorities.
- **Token relay** — a `RestClient` (or HTTP service interface) in Journal Web with `OAuth2ClientHttpRequestInterceptor` attaches the current user's access token.

### Data model
```mermaid
erDiagram
    ENTRY ||--o| PHOTO : "has"
    ENTRY {
        uuid id PK
        text owner_sub
        text caption
        date taken_on
        timestamptz created_at
    }
    PHOTO {
        uuid entry_id FK
        text storage_key
        text content_type
        bigint size_bytes
    }
```
`owner_sub` is the IdP's stable subject identifier. Never key data by email — emails change and can be reassigned.

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Web client | `spring-boot-starter-security-oauth2-client` + Thymeleaf | OIDC login, authorized-client storage |
| API | `spring-boot-starter-security-oauth2-resource-server` | JWT validation from `issuer-uri` |
| IdP | Keycloak in a container (or Spring Authorization Server, project 34) | realms, clients, scopes, identity brokering |
| Calling the API | `RestClient` + `OAuth2ClientHttpRequestInterceptor`, or `@ClientRegistrationId` on an HTTP interface (type-level since Security 7) | automatic token attach and refresh |

### Milestones
1. **IdP setup.** A realm with a confidential client for Journal Web and an API audience. *Done when* you can fetch the discovery document and see your scopes.
2. **Login.** *Done when* visiting `/journal` redirects to the IdP and, after login, shows the user's name from the ID token's claims.
3. **Resource server.** *Done when* Photo API returns 401 without a token, 403 with a token lacking `journal.write` on `POST`, and 200 otherwise — tested with the `jwt()` post-processor.
4. **Token relay.** *Done when* Journal Web lists entries fetched from Photo API, and an expired access token is refreshed transparently.
5. **Client credentials and logout.** *Done when* the nightly job authenticates as itself, and logout ends both the local session and the IdP session.

### Deep dive: the hard parts
- **ID token vs access token.** The ID token is for the *client* — it says who logged in. The access token is for the *API* — it says what the bearer may do. Never send ID tokens to APIs, and never let the client parse access tokens for identity; their format is the IdP's business.
- **Audience validation.** Without checking `aud`, a token issued for some other API in the same IdP would be accepted by yours. Add a `JwtValidator` for the expected audience (or the Boot property for audiences) and test it with a token for a different audience.
- **Mapping claims to authorities.** Scopes map automatically to `SCOPE_*`. Keycloak puts roles in nested claims (`realm_access.roles`); write a `Converter<Jwt, Collection<GrantedAuthority>>`, or in Boot 4.1 use `spring.security.oauth2.resourceserver.jwt.authorities-claim-expressions`.
- **Redirect URI and proxies.** Behind a reverse proxy, Spring must know the external scheme and host to build `redirect_uri`; set `server.forward-headers-strategy=framework` or the IdP rejects the callback.

### Testing strategy
- Photo API: slice tests with `jwt().jwt(j -> j.claim("scope", ...))`; one test with a real `JwtDecoder` against a token signed by a test key and a mock JWKS.
- Journal Web: `oauth2Login()` request post-processor for controller tests; `MockRestServiceServer` to assert the bearer header is relayed.
- An end-to-end test with Keycloak in Testcontainers, if Docker is available.

### Trade-offs, scaling and failure modes
- Server-side sessions in Journal Web keep tokens out of the browser but make the web tier stateful; use Spring Session with Redis for several instances.
- If the IdP is down, nobody can log in, but existing access tokens still validate (the JWKS is cached) — a useful property to state explicitly.
- Token relay passes the user's full permissions downstream; for deeper call chains, **token exchange** (RFC 8693) issues narrower tokens per hop.

### Stretch goals
- Add GitHub as a second login option brokered through Keycloak, and link accounts by verified email.
- Multi-issuer resource server: accept tokens from two realms (Ch 11 multi-tenancy).
- Replace Thymeleaf with a SPA and turn Journal Web into a BFF (project 40).

### Skills practised
- [Ch 11 — Tokens and OAuth2](../chapters/11-spring-security-2-tokens-and-oauth2/README.md): OAuth 2.0/OIDC, resource server, claims to authorities, OAuth2 login, client credentials, token relay.
- [Ch 10 — Spring Security fundamentals](../chapters/10-spring-security-1-fundamentals/README.md): filter chains, sessions, logout, testing.
- [Ch 6 — REST clients and HTTP interfaces](../chapters/06-rest-clients-and-http-interfaces/README.md): interceptors and HTTP service groups.

### In the interview
**30-second answer:** I built two applications: Journal Web, which signs users in through Keycloak with OAuth2 login and PKCE, and Photo API, a resource server that accepts only access tokens with the right issuer, audience and scopes. The key decision was keeping tokens out of the browser — Journal Web stores them in its server-side session and relays the user's access token to the API with `OAuth2ClientHttpRequestInterceptor`. The hardest part was audience validation and Keycloak's nested role claims, which I handled with a custom validator and authority converter. Entries are keyed by the IdP's stable `sub`, and a nightly job uses client credentials with no user at all.

**Key terms to know:**
- **OAuth 2.0 vs OpenID Connect** — OAuth delegates access to APIs; OIDC adds login and an ID token on top.
- **Authorization code flow with PKCE** — the browser gets a one-time code, the server exchanges it plus a verifier for tokens.
- **ID token vs access token** — "who logged in" for the client vs "what the bearer may do" for the API.
- **Scopes and `SCOPE_` authorities** — permissions in the token (`journal.write`) mapped to Spring authorities.
- **Audience (`aud`)** — which API a token is meant for; Photo API rejects tokens for others.
- **Issuer discovery and JWKS** — `/.well-known/openid-configuration` tells the API where to fetch signing keys.
- **Token relay** — the client attaches the current user's access token when calling the API.
- **Client credentials flow** — machine-to-machine tokens with no user; the nightly job.
- **RP-initiated logout** — ending the session at the IdP too, not only locally.

**Q:** What is PKCE and why use it even for confidential clients?

**A:** The client creates a random verifier, sends its SHA-256 hash with the authorization request, and sends the verifier itself when exchanging the code. A stolen authorization code is useless without the verifier. OAuth 2.1 recommends PKCE for every client, and Spring Security 7 enables it by default for confidential clients too.

**Q:** ID token or access token for your API?

**A:** Access token. The ID token is the client's proof of who logged in and its audience is the client itself. The API validates access tokens for signature, issuer, audience, expiry and scopes. Sending ID tokens to APIs is a classic mistake that lets a token minted for one app be replayed at another.

**Q:** How does a resource server get the IdP's keys?

**A:** With `issuer-uri` set, Spring reads the issuer's discovery document, finds the `jwks_uri`, downloads the public keys and caches them. When a token arrives with an unknown key id (`kid`), it refetches the set, so key rotation at the IdP works without redeploying.

**Q:** Why validate `aud` when the signature is already valid?

**A:** The signature only proves the IdP issued the token. Without an audience check, a token issued for some other API in the same realm is accepted by mine. I add an audience validator (or the Boot `audiences` property) and test that a token for a different audience gets 401.

**Q:** How do Keycloak roles become Spring authorities?

**A:** Scopes map automatically to `SCOPE_*`, but Keycloak puts roles in a nested `realm_access.roles` claim. I wrote a `Converter<Jwt, Collection<GrantedAuthority>>` that reads that claim and adds `ROLE_` authorities, and plugged it into a `JwtAuthenticationConverter`.

**Q:** The IdP goes down. What still works?

**A:** Nobody can log in or refresh tokens, but existing access tokens keep validating at Photo API, because the JWKS is cached and validation is local. Journal Web users with a live session continue until their access token expires and the refresh fails.

**Q:** Login loops back to the IdP behind a reverse proxy. Why?

**A:** Spring builds `redirect_uri` from the request it sees, so behind a proxy it may use `http` and an internal host that the IdP does not recognise. Setting `server.forward-headers-strategy=framework` (or `native`) makes Spring use `X-Forwarded-*` headers, so the callback URL matches what the IdP has registered.

**Q:** What changes at 10x and with several instances of Journal Web?

**A:** Server-side sessions make the web tier stateful, so I would store sessions in Redis with Spring Session or use sticky sessions. Photo API is stateless and scales freely. For deeper call chains, relaying the user's full token to every service is too broad; token exchange (RFC 8693) issues narrower tokens per hop.

**Q:** How do you test each side?

**A:** For Photo API, slice tests with `jwt().jwt(j -> j.claim("scope", "journal.write"))` cover 401/403/200, plus one test with a real `JwtDecoder` and a test-signed token for audience checks. For Journal Web, the `oauth2Login()` post-processor fakes a logged-in user and `MockRestServiceServer` asserts the bearer header is relayed. Keycloak in Testcontainers runs the full flow end to end.

**Common mistakes in answers:**
- "PKCE is only for mobile apps" — it is recommended for every client and is on by default in Security 7.
- Keying data by email — emails change and can be reassigned; use the stable `sub`.
- "The resource server calls the IdP on every request" — with JWTs it validates locally using cached keys.

### TL;DR
Split client and API: a web app that signs users in through an IdP with PKCE and relays their access token to a resource server that checks signature, issuer, audience and scopes.

## 13. E-commerce Cart and Checkout
**Level:** Intermediate · **Time:** 2–3 weeks · **Prerequisites:** Ch 7, Ch 8

### What it is
The money path of an online shop: a shopping cart, inventory, a checkout that reserves stock, prices the order, charges a (fake) payment provider and creates the order — without ever overselling stock or charging twice. The core lesson is **transactions**: a group of database operations that succeed or fail together, and how Spring's `@Transactional` controls them.

### The problem it solves
Checkout looks like "insert an order", but it combines contended shared data (the last item in stock), external side effects that cannot be rolled back (a card charge), and users who double-click. Getting it wrong means oversold inventory, orphaned charges or angry customers. This project forces you to choose isolation levels, lock strategies and transaction boundaries deliberately.

### Real-world examples
- Shopify's engineering post ["10 Tips for Building Resilient Payment Systems"](https://shopify.engineering/building-resilient-payment-systems) covers low timeouts, idempotency keys and reconciliation for checkout.
- The PostgreSQL manual on [transaction isolation](https://www.postgresql.org/docs/current/transaction-iso.html) and [explicit locking](https://www.postgresql.org/docs/current/explicit-locking.html) explains exactly what your `@Transactional(isolation = ...)` and `SELECT ... FOR UPDATE` do.

### Requirements
**Functional**
- Cart: add/remove items, change quantity; carts expire after 7 days of inactivity.
- Checkout: validate cart, reserve stock, compute totals (price snapshot, tax, discount code), call payment, confirm order; on payment failure, release stock.
- Order history and order status (`PENDING_PAYMENT`, `PAID`, `CANCELLED`).
- Admin: adjust stock, change prices (existing carts show the new price; existing orders keep the old).

**Non-functional**
- **Zero overselling** under 200 concurrent checkouts for the last 10 units, proved by a test.
- A repeated checkout request with the same `Idempotency-Key` returns the same order, never a second charge.
- Checkout p95 under 300 ms excluding the payment provider; payment calls time out after 3 s.

### The architecture
```mermaid
flowchart TD
    C["Client"] --> CC["CartController"]
    C -->|"POST /checkout with Idempotency-Key"| CO["CheckoutController"]
    CO --> CS["CheckoutService - orchestrates, no long transaction"]
    CS --> T1["Tx 1 - reserve stock, create PENDING order"]
    CS --> PAY["PaymentClient - outside any transaction"]
    CS --> T2["Tx 2 - mark PAID or release stock"]
    T1 --> DB[("PostgreSQL - products, stock, carts, orders")]
    T2 --> DB
    SW["@Scheduled sweeper - expire stale PENDING orders"] --> DB
```
- **CartController** — simple CRUD on cart lines, keyed by user or anonymous cart id.
- **CheckoutService** — not itself transactional: it calls two short transactional methods on another bean around the payment call.
- **Tx 1** — atomically decrements stock for each line with a conditional update and inserts the order with a price snapshot.
- **PaymentClient** — `RestClient` to a fake provider with timeouts and an idempotency key derived from the order id.
- **Tx 2** — marks the order `PAID`, or restores stock and marks `CANCELLED`.
- **Sweeper** — finds orders stuck in `PENDING_PAYMENT` (crash between steps) and reconciles them with the provider.

### Data model
```mermaid
erDiagram
    PRODUCT ||--|| STOCK : "has"
    CART ||--o{ CART_LINE : "contains"
    ORDERS ||--o{ ORDER_LINE : "contains"
    PRODUCT {
        bigint id PK
        text sku UK
        numeric price
    }
    STOCK {
        bigint product_id PK
        int available
        bigint version
    }
    CART {
        uuid id PK
        bigint user_id
        timestamptz updated_at
    }
    CART_LINE {
        uuid cart_id FK
        bigint product_id FK
        int quantity
    }
    ORDERS {
        bigint id PK
        text idempotency_key UK
        text status
        numeric total
        text payment_ref
    }
    ORDER_LINE {
        bigint order_id FK
        bigint product_id FK
        int quantity
        numeric unit_price
    }
```

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Transactions | `@Transactional`, `TransactionTemplate` | declarative for services, programmatic for fine-grained steps |
| Persistence | Spring Data JPA for carts and orders, `JdbcClient` for the stock update | the hot path is one SQL statement |
| Database | PostgreSQL | row locks, `READ COMMITTED` default |
| Payment stub | WireMock or a small stub controller | simulate timeouts and declines |
| Tests | Testcontainers PostgreSQL | real locking behaviour, not H2's |

### Milestones
1. **Cart.** *Done when* cart CRUD and expiry work with tests.
2. **Naive checkout, broken on purpose.** Read stock, check, write in Java. *Done when* a 200-thread test oversells — keep this test.
3. **Fix with a conditional update.** `UPDATE stock SET available = available - :q WHERE product_id = :id AND available >= :q`; zero rows updated means out of stock. *Done when* the same test sells exactly 10.
4. **Split transactions around payment.** *Done when* a payment timeout leaves stock released and the order `CANCELLED`, and the database connection is not held during the 3 s wait (assert with a pool of size 2 and parallel checkouts).
5. **Idempotency.** Unique `idempotency_key`; a repeat returns the stored result. *Done when* two identical concurrent requests produce one order and one payment call.
6. **Reconciliation sweeper.** *Done when* a simulated crash after payment but before Tx 2 is repaired by the sweeper asking the provider for the payment status.

### Deep dive: the hard parts
- **Lost updates and overselling.** Under `READ COMMITTED`, two transactions can both read `available = 1` and both write 0. Options: the conditional update (atomic in one statement — simplest), a pessimistic lock (`SELECT ... FOR UPDATE`, `@Lock(PESSIMISTIC_WRITE)`), optimistic locking with `@Version` and retries, or `SERIALIZABLE` isolation with retry on serialization failures. Measure them under contention.
- **Never hold a transaction across a remote call.** A transaction open during a 3 s payment call holds a connection and row locks for 3 s; with a pool of 10, eleven slow checkouts stop the shop. Short transaction, remote call, short transaction — and design for the crash between them.
- **Self-invocation.** If `CheckoutService.checkout()` calls `this.reserve()` annotated `@Transactional`, no transaction starts — the call bypasses the proxy. Put the transactional steps on a separate bean or use `TransactionTemplate`.
- **Price snapshots.** Order lines store `unit_price` at purchase time; recomputing from `product.price` later rewrites history. Carts, in contrast, show live prices and must warn when a price changed since the item was added.

### Testing strategy
- Concurrency tests with virtual threads and a `CountDownLatch` start gate, asserting final stock and order counts.
- Failure-injection tests: payment timeout, decline, exception after payment.
- Repository tests on real PostgreSQL via Testcontainers (`@ServiceConnection`), because lock semantics differ in H2.

### Trade-offs, scaling and failure modes
- Conditional updates scale well for single-row stock; a flash sale on one SKU still serializes on that row — shard stock into buckets or pre-allocate tokens if that matters.
- Reserving at checkout (not at add-to-cart) avoids locking stock in abandoned carts, at the cost of "sold out" surprises at checkout.
- Two separate transactions mean temporary states (`PENDING_PAYMENT`) are visible; the sweeper and idempotency make them safe. A saga (project 28) generalizes this across services.

### Stretch goals
- Discount codes with a usage limit, enforced with the same conditional-update trick.
- Emit an `OrderPaid` event with `@TransactionalEventListener(phase = AFTER_COMMIT)` to send email only after commit.
- Compare throughput of the four locking strategies and chart it.

### Skills practised
- [Ch 7 — Data access with JDBC and transactions](../chapters/07-data-access-jdbc-and-transactions/README.md): `@Transactional`, propagation, isolation, pitfalls, `TransactionTemplate`, `@TransactionalEventListener`.
- [Ch 8 — Spring Data JPA and Hibernate](../chapters/08-spring-data-jpa-and-hibernate/README.md): optimistic and pessimistic locking.
- [Ch 12 — Testing Spring applications](../chapters/12-testing-spring-apps/README.md): Testcontainers with `@ServiceConnection`.

### In the interview
**30-second answer:** I built a cart and checkout that never oversells and never charges twice. The key design decision was splitting checkout into two short transactions around the payment call — reserve stock and create a `PENDING_PAYMENT` order, call the provider outside any transaction, then mark `PAID` or release stock — so no connection or row lock is held during a 3-second remote call. The hardest problem was overselling under contention, which I first reproduced with a 200-thread test and then fixed with a single conditional `UPDATE ... WHERE available >= :q`. The same test now sells exactly 10 of 10 units, and a repeated `Idempotency-Key` returns the original order.

**Key terms to know:**
- **Transaction (ACID)** — a group of operations that commit or roll back together; Tx 1 and Tx 2 here.
- **`@Transactional` proxy** — Spring starts and ends the transaction in a proxy around the method; only calls through the proxy are transactional.
- **Propagation (`REQUIRED`, `REQUIRES_NEW`)** — whether a method joins the caller's transaction or starts its own.
- **Isolation level** — how much concurrent transactions see of each other; PostgreSQL defaults to `READ COMMITTED`.
- **Lost update** — two transactions read the same value and both write, one overwriting the other; the overselling bug.
- **Conditional (atomic) update** — `UPDATE ... SET available = available - :q WHERE available >= :q`; the overselling fix.
- **Pessimistic vs optimistic locking** — lock the row while working (`FOR UPDATE`) vs detect conflicts at write time (`@Version`).
- **Idempotency key** — a client-chosen unique key that makes a retried request return the first result.
- **Price snapshot** — `unit_price` copied into the order line at purchase time.
- **Reconciliation** — a sweeper that repairs orders stuck between steps by asking the payment provider.

**Q:** How do you prevent overselling?

**A:** One atomic conditional update per line: `UPDATE stock SET available = available - :q WHERE product_id = :id AND available >= :q`, and I check the affected row count — zero means out of stock. The database applies it atomically under a row lock, so no two transactions can both take the last unit. Alternatives are `SELECT ... FOR UPDATE`, `@Version` with retry, or `SERIALIZABLE` with retry; the conditional update is the simplest and fastest.

**Q:** Why not wrap the whole checkout, including payment, in one transaction?

**A:** A transaction open during a 3-second payment call holds a database connection and row locks for 3 seconds; with a pool of 10, eleven slow checkouts stop the shop. The card charge cannot be rolled back by the database anyway. So I use short transactions around the call, plus idempotency and a reconciliation sweeper for the crash in between.

**Q:** Your `@Transactional` method did not roll back. Why?

**A:** The usual causes: self-invocation (`this.reserve()` bypasses the proxy), a non-public method, a checked exception (only unchecked ones roll back by default), or the exception was caught and swallowed inside the method. I put the transactional steps on a separate bean, and for fine-grained steps I use `TransactionTemplate`, which has no proxy to bypass.

**Q:** Explain `REQUIRED` vs `REQUIRES_NEW` and when you would use the second.

**A:** `REQUIRED` (the default) joins an existing transaction or starts one; if an inner method fails, the whole outer transaction is marked rollback-only. `REQUIRES_NEW` suspends the outer transaction and runs in its own, committing independently. I would use it for something that must persist even if the outer work rolls back, such as an audit row — but it uses a second connection, which can deadlock a small pool.

**Q:** Would `SERIALIZABLE` isolation fix overselling?

**A:** Yes — PostgreSQL would detect the conflicting read-then-write and abort one transaction with a serialization failure (SQLSTATE 40001), which the app must retry. It works for any logic, not just a single counter, but it costs aborts and retries under contention. For a stock counter the conditional update is cheaper and needs no retry loop.

**Q:** The app crashes after the payment succeeded but before Tx 2. What happens?

**A:** The order is stuck in `PENDING_PAYMENT` with stock reserved. The scheduled sweeper finds orders older than a few minutes in that state, asks the provider for the payment status using the order-derived idempotency key, and then marks `PAID` or releases stock. Because the payment call used the same key, a retry never creates a second charge.

**Q:** Two identical checkout requests arrive at the same time. What happens?

**A:** Both try to insert an order with the same `idempotency_key`; the unique constraint lets only one succeed. The loser catches the duplicate-key error, waits for or reads the stored result and returns the same order. Only one payment call happens, which a concurrent test asserts.

**Q:** What breaks first in a flash sale at 100x?

**A:** All buyers of one hot SKU serialize on one stock row, so throughput is limited by how fast that row can be updated. Fixes are to split stock into several bucket rows and decrement any bucket with stock, or to pre-allocate purchase tokens in Redis and settle in the database afterwards. The connection pool is the second limit, which is why no transaction spans the payment call.

**Q:** How did you prove there is no overselling?

**A:** A Testcontainers PostgreSQL test with 10 units and 200 virtual threads waiting on a `CountDownLatch`, all released at once to call checkout. Afterwards it asserts exactly 10 successful orders and `available = 0`. The naive read-check-write version of the same test oversells, which is why I kept it as proof. H2 is not used because its locking differs from PostgreSQL's.

**Q:** Write the stock decrement with `JdbcClient`.

**A:**
```java
int updated = jdbc.sql("""
    UPDATE stock SET available = available - :q
    WHERE product_id = :id AND available >= :q""")
  .param("q", qty).param("id", productId).update();
if (updated == 0) throw new OutOfStockException(productId);
```

**Common mistakes in answers:**
- "Use `synchronized` in Java" — it only works inside one JVM and does nothing across several instances.
- "Wrap everything in one big `@Transactional`" — it holds locks during remote calls and still cannot undo a card charge.
- "Check stock, then update" — read-then-write races under `READ COMMITTED`; the check must be in the same atomic statement.

### TL;DR
Checkout done safely: atomic stock decrements, short transactions around an external payment, idempotency keys, price snapshots and a reconciliation job for the crash in between.

## 14. Product Catalog Cache with Redis
**Level:** Intermediate · **Time:** 1–2 weeks · **Prerequisites:** Ch 9, Ch 15

### What it is
A read-heavy product catalog service whose responses are cached in **Redis**, an in-memory key-value store shared by all instances, using Spring's **cache abstraction** (`@Cacheable`, `@CachePut`, `@CacheEvict`). You will add a small in-process cache in front (two levels), measure hit rates, and deal with the classic caching failures: stale data, stampedes and cache outages.

### The problem it solves
Catalog pages are read thousands of times for each update. Hitting the database every time wastes money and adds latency. But caching is where correctness quietly dies: users see old prices, an expired hot key sends a thousand requests to the database at once, or the cache going down takes the whole site with it. This project teaches you to cache deliberately and to measure the result.

### Real-world examples
- ["Scaling Memcache at Facebook"](https://www.usenix.org/conference/nsdi13/technical-sessions/presentation/nishtala) (NSDI 2013) describes look-aside caching at huge scale, including leases to tame stale sets and thundering herds.
- The [Spring Framework cache abstraction reference](https://docs.spring.io/spring-framework/reference/integration/cache.html) and the [Spring Boot caching reference](https://docs.spring.io/spring-boot/reference/io/caching.html) cover the annotations and the Redis cache manager configuration.

### Requirements
**Functional**
- `GET /api/products/{id}` and `GET /api/categories/{id}/products?page=` served from cache when possible.
- Admin updates (price, description, stock status) visible to readers within 5 seconds.
- `POST /admin/cache/evict?name=` for operators.

**Non-functional**
- Cache hit rate above 95% under a realistic read/write mix (100:1), measured from metrics.
- p95 under 10 ms on hits, under 60 ms on misses.
- If Redis is unavailable, the service keeps serving from the database (slower) instead of failing.
- At most one database load per hot key per instance at expiry time.

### The architecture
```mermaid
flowchart TD
    C["Client"] --> PC["ProductController"]
    PC --> PS["ProductService - @Cacheable"]
    PS --> L1["L1 Caffeine - per instance, 5 s TTL"]
    L1 -->|"miss"| L2["L2 Redis - shared, 10 min TTL"]
    L2 -->|"miss"| DB[("PostgreSQL")]
    ADM["Admin update"] --> PS
    PS -->|"after commit evict"| L2
    PS -->|"publish invalidation"| PUB["Redis pub-sub channel"]
    PUB --> L1
```
- **ProductService** — the cached methods; the cache key is the product id (or category id + page).
- **L1 Caffeine** — in-JVM cache with a very short TTL: absorbs hot keys with zero network cost; staleness bounded by 5 s.
- **L2 Redis** — shared across instances; values stored as JSON with a type-safe serializer and a key prefix per cache.
- **Invalidation** — after the database transaction commits, evict the Redis key and publish a message so every instance drops its L1 entry.
- **Fallback** — a `CacheErrorHandler` that logs and treats Redis errors as misses.

### Data model
| Key pattern | Value | TTL |
|---|---|---|
| `catalog::product::{id}` | `ProductView` JSON (id, name, price, currency, inStock) | 10 min plus random jitter |
| `catalog::category::{id}::page::{n}` | list of product ids only | 2 min |
| channel `catalog-invalidation` | product id | — |

Caching category pages as **lists of ids** (then fetching each product from cache) means a price change invalidates one product entry, not every page containing it.

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Abstraction | `spring-boot-starter-cache`, `@EnableCaching` | provider-independent annotations |
| L2 | `spring-boot-starter-data-redis` (Lettuce), `RedisCacheManager` | TTL per cache, key prefixes |
| L1 | Caffeine | fast, size and time bounds, `sync` loading |
| Metrics | Micrometer cache metrics via Actuator | hit/miss/eviction counts |
| Tests | Testcontainers Redis | real serialization and TTL |

### Milestones
1. **Baseline.** No cache; a load test (e.g. a JUnit loop or a tool such as k6) records p95 and database queries per second. *Done when* you have numbers to beat.
2. **Redis cache.** `@Cacheable("product")` with a `RedisCacheManager`, JSON values, 10-minute TTL. *Done when* the second read issues no SQL and the key is visible in Redis.
3. **Correct invalidation.** Evict after commit (`@TransactionalEventListener` or a transaction-aware cache manager). *Done when* a test proves a rolled-back update does not evict and a committed one does.
4. **Two levels and pub/sub.** *Done when* two application instances in one test both serve the new price within 5 s of an update.
5. **Resilience and stampede.** `CacheErrorHandler` fallback, `sync = true`, TTL jitter. *Done when* stopping Redis still returns 200s, and 100 concurrent misses for one key cause one SQL query per instance.

### Deep dive: the hard parts
- **Evict after commit, not before.** If you evict and then the transaction rolls back, fine; but if you evict, another request reloads the **old** value before your commit, and caches it for 10 minutes. Evicting after commit narrows that window; a short L1 TTL bounds the rest.
- **Cache stampede.** When a hot key expires, many concurrent requests miss together and all hit the database. `@Cacheable(sync = true)` lets one thread per JVM compute while others wait; random TTL jitter stops many keys expiring together; across instances, a short lock in Redis or "refresh ahead" helps.
- **Serialization.** Java serialization in Redis is fragile (class changes break reads) and unsafe; use JSON with explicit types, version your cached DTOs, and never cache JPA entities (lazy proxies and session state do not belong in a cache).
- **Caching null and errors.** Caching "not found" protects the database from repeated lookups of missing ids, but must expire quickly; never cache exceptions.

### Testing strategy
- Cache behaviour tests that count repository calls with a spy (`@MockitoSpyBean`).
- Testcontainers Redis for serialization, TTL and pub/sub.
- A failure test that stops the Redis container mid-test.

### Trade-offs, scaling and failure modes
- Every cache trades freshness for speed; write down the staleness each cache allows and make sure product owners agree (prices: 5 s; descriptions: 10 min).
- Redis is now a dependency: monitor memory, set `maxmemory` and an eviction policy, and plan for failover.
- Caching full pages is simplest but invalidation becomes "flush everything"; caching small entities and composing is more work and much more precise.

### Stretch goals
- Write-through for stock status with `@CachePut`.
- Redis client-side caching (server-assisted invalidation) and compare with your pub/sub approach.
- Add `Cache-Control` and `ETag` headers so browsers and CDNs cache too.

### Skills practised
- [Ch 9 — Spring Data beyond JPA, and caching](../chapters/09-spring-data-beyond-jpa-and-caching/README.md): Spring Data Redis, the caching abstraction, `sync`, pitfalls.
- [Ch 7 — Data access with JDBC and transactions](../chapters/07-data-access-jdbc-and-transactions/README.md): `@TransactionalEventListener` for after-commit actions.
- [Ch 15 — Observability and production](../chapters/15-observability-and-production/README.md): cache metrics.
- [Ch 12 — Testing Spring applications](../chapters/12-testing-spring-apps/README.md): Testcontainers.

### In the interview
**30-second answer:** I put a two-level cache in front of a product catalog: a 5-second Caffeine cache in each JVM and a shared Redis cache with a 10-minute TTL, all through Spring's `@Cacheable` abstraction. The key design decision was cache-aside with eviction after the database commit plus a Redis pub/sub message so every instance drops its local entry, which bounds staleness to about 5 seconds. The hardest problems were stampedes on hot keys and Redis outages, solved with `sync = true`, TTL jitter and a `CacheErrorHandler` that treats Redis errors as misses. Measured hit rate was above 95% with p95 under 10 ms on hits.

**Key terms to know:**
- **Cache abstraction (`@Cacheable`, `@CachePut`, `@CacheEvict`)** — provider-independent annotations applied through a proxy; on `ProductService`.
- **Cache-aside (look-aside)** — read cache, on miss load from the database and store; the read path here.
- **TTL and jitter** — time-to-live per entry, randomised so many keys do not expire together.
- **L1/L2 cache** — local in-JVM Caffeine in front of shared Redis.
- **Invalidation after commit** — evict only once the transaction has committed, via `@TransactionalEventListener`.
- **Cache stampede (thundering herd)** — many concurrent misses on one key all hitting the database.
- **`sync = true`** — one thread per JVM loads a missing key while others wait.
- **`CacheErrorHandler`** — decides what happens when the cache itself fails; here, treat as a miss.
- **Hit rate** — fraction of reads served from cache, measured via Micrometer cache metrics.

**Q:** How do you keep a cache consistent with the database?

**A:** Cache-aside with eviction after commit, short TTLs as a safety net, and pub/sub to clear per-instance L1 caches. Perfect consistency is not the goal; bounded staleness that the product owner has agreed to is (5 seconds for prices). I wrote the allowed staleness per cache into the README.

**Q:** Why evict after commit rather than before or inside the transaction?

**A:** If I evict before commit, another request can reload the old value from the database before my commit lands and cache it for 10 minutes. If the transaction rolls back after an early eviction, that is harmless, but caching the stale value is not. Evicting after commit narrows the race to a tiny window, and the short L1 TTL bounds whatever is left.

**Q:** What is a cache stampede and how do you prevent it?

**A:** When a hot key expires, many concurrent requests miss together and all query the database. `@Cacheable(sync = true)` makes one thread per JVM compute while the others wait, random TTL jitter stops many keys expiring at once, and across instances a short Redis lock or refresh-ahead helps. A test with 100 concurrent misses asserts one SQL query per instance.

**Q:** What happens when Redis goes down?

**A:** My `CacheErrorHandler` logs the error and treats it as a miss, so reads go to the database (slower) and writes skip the cache. I also cap database load, for example with a connection pool limit and the L1 cache, so the fallback does not cause a second outage. A test stops the Redis container mid-test and still gets 200s.

**Q:** Why do you never cache JPA entities, and how do you serialize values?

**A:** Entities carry lazy-loading proxies and persistence-context state that make no sense outside a session and may trigger queries or fail on deserialization. I cache small immutable DTOs (`ProductView`) as JSON with explicit types and a version in the key prefix. Java serialization is fragile when classes change and is a known security risk.

**Q:** `@Cacheable` seems to do nothing. Why?

**A:** Common causes: `@EnableCaching` is missing, the method is called from inside the same bean (self-invocation bypasses the proxy), the method is not public, or the key differs on every call because an argument without a stable `equals`/`hashCode` is part of it. I verify with a spy repository (`@MockitoSpyBean`) that counts calls.

**Q:** What changes at 10x read traffic and many instances?

**A:** Redis network round trips and a few very hot keys become the bottleneck, so the L1 cache matters more and hot keys may need replication or client-side caching. Pub/sub invalidation fans out to more instances, and a missed message means up to 5 s of staleness, which the TTL bounds. Redis needs `maxmemory`, an eviction policy and failover planning.

**Q:** Why cache category pages as lists of ids?

**A:** If a page stores full product data, a single price change requires evicting every page that contains that product, which in practice means flushing all pages. Storing only ids means a price change evicts one product entry, and pages stay valid. It costs an extra cache lookup per product, which is cheap.

**Q:** Show a cached read and an after-commit eviction.

**A:**
```java
@Cacheable(cacheNames = "product", key = "#id", sync = true)
public ProductView find(long id) { return repo.findView(id).orElseThrow(); }

@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
void on(ProductChanged e) { cacheManager.getCache("product").evict(e.id()); publishInvalidation(e.id()); }
```

**Common mistakes in answers:**
- "Update the cache and the database in the same method and they stay consistent" — without after-commit ordering, races leave stale data for the whole TTL.
- "If Redis is down the site is down" — a cache should degrade to the database, not be a hard dependency.
- Caching exceptions or never-expiring "not found" entries — negative results need a short TTL, errors should not be cached.

### TL;DR
Two-level caching with Caffeine and Redis, evicted after commit, protected against stampedes and outages, and justified by measured hit rates and latencies.

## 15. File Upload and Media Service
**Level:** Intermediate · **Time:** 2 weeks · **Prerequisites:** Ch 5, Ch 6, Ch 14

### What it is
A service that accepts user files (images, PDFs), stores them in **object storage** (an S3-compatible bucket such as AWS S3 or MinIO — storage for blobs addressed by key rather than files in folders), validates and scans them, generates thumbnails asynchronously, and serves them back through short-lived signed URLs. It supports both small uploads through the API and large uploads directly from the browser to storage using **presigned URLs** — URLs that grant temporary permission for one specific upload.

### The problem it solves
File handling is a security and resource minefield: huge uploads that exhaust memory or disk, files that lie about their type, malware, path traversal in file names, and slow processing that blocks request threads. This project teaches streaming, validation, asynchronous processing and the "do not proxy big files through your app" pattern.

### Real-world examples
- AWS documents [uploading objects with presigned URLs](https://docs.aws.amazon.com/AmazonS3/latest/userguide/PresignedUrlUploadObject.html), the direct-to-storage pattern used here.
- [MinIO](https://github.com/minio/minio) is an S3-compatible object store you can run locally and in tests.
- The [OWASP File Upload Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/File_Upload_Cheat_Sheet.html) lists the validation rules (extension allow-lists, content checks, size limits, storage outside the web root).

### Requirements
**Functional**
- `POST /api/files` (multipart) for files up to 10 MB; returns an id and status `PENDING_SCAN`.
- `POST /api/files/presign` returns a presigned PUT URL for files up to 2 GB; the client uploads directly, then calls `POST /api/files/{id}/complete`.
- After upload: content-type detection, malware scan (stub or ClamAV), thumbnail generation for images; status moves to `READY` or `REJECTED`.
- `GET /api/files/{id}` returns metadata and a 5-minute presigned download URL; only the owner can access.

**Non-functional**
- Memory per upload stays constant (streamed), regardless of file size.
- Thumbnail generation does not run on request threads; p95 of `POST /api/files` under 500 ms for 10 MB on a LAN.
- Allowed types: JPEG, PNG, WebP, PDF — decided by content, not by the file extension.

### The architecture
```mermaid
flowchart TD
    B["Browser"] -->|"small file multipart"| API["FileController"]
    B -->|"presign request"| API
    B -->|"PUT large file direct"| S3[("Object storage - MinIO or S3")]
    API --> FS["FileService"]
    FS --> S3
    FS --> DB[("PostgreSQL - file metadata")]
    FS -->|"FileUploaded event"| Q["Async pipeline - @Async or a queue"]
    Q --> DET["Type detection - Tika"]
    Q --> SCAN["Malware scan"]
    Q --> TH["Thumbnailer"]
    TH --> S3
    Q --> DB
```
- **FileController** — multipart endpoint with Boot's limits (`spring.servlet.multipart.max-file-size`), presign and complete endpoints.
- **FileService** — generates a random storage key (never the user's file name), streams to storage, records metadata.
- **Object storage** — holds originals under `originals/` and thumbnails under `thumbs/`; the bucket is private.
- **Async pipeline** — triggered after commit; detects type from magic bytes, scans, thumbnails, then updates status.
- **Presigned URLs** — generated by the AWS SDK's presigner with short expiry and fixed content length/type conditions.

### Data model
```mermaid
erDiagram
    STORED_FILE ||--o{ FILE_VARIANT : "has"
    STORED_FILE {
        uuid id PK
        text owner_sub
        text original_name
        text storage_key UK
        text declared_type
        text detected_type
        bigint size_bytes
        text sha256
        text status
        timestamptz created_at
    }
    FILE_VARIANT {
        uuid file_id FK
        text kind
        text storage_key
        int width
        int height
    }
```

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Upload | Spring MVC multipart, `MultipartFile.getInputStream()` | streaming, Boot-managed limits |
| Storage | AWS SDK for Java v2 S3 client + presigner, MinIO locally | standard S3 API |
| Detection | Apache Tika | content-based MIME detection |
| Async | `@Async` on virtual threads (or RabbitMQ, project 17) | keep request threads free |
| Images | `javax.imageio` or a thumbnail library | resize without external tools |

### Milestones
1. **Multipart to storage.** *Done when* a 10 MB upload streams to MinIO and a test asserts the stored SHA-256 equals the original's.
2. **Limits and validation.** *Done when* an 11 MB upload returns 413 Problem Details, and a `.png` that is really an executable is `REJECTED`.
3. **Async pipeline.** *Done when* status goes `PENDING_SCAN → READY` within 2 s in a test using Awaitility, and the upload endpoint returns before thumbnails exist.
4. **Presigned upload and download.** *Done when* a test uploads 50 MB directly to MinIO with a presigned URL, completes it, and downloads with a presigned GET that expires.
5. **Ownership and cleanup.** *Done when* another user gets 404 (not 403, to avoid leaking existence) and a nightly job deletes uploads never completed after 24 h.

### Deep dive: the hard parts
- **Streaming vs buffering.** By default the servlet container writes multipart parts above a threshold to a temporary disk file; reading `getBytes()` loads the whole file into the heap. Use `getInputStream()` and pass a known content length to the S3 client; for unknown lengths use multipart upload to storage.
- **Never trust the client.** The file name can contain `../`, the declared content type can lie, and an image can be a decompression bomb (tiny file, gigantic pixel dimensions). Generate your own keys, detect type from bytes, and check image dimensions before decoding fully.
- **Direct uploads and completion.** With presigned URLs your app never sees the bytes, so it must verify after the fact: on `complete`, `HEAD` the object to check size and type, then run the same pipeline. Bucket notifications can replace the explicit `complete` call.
- **After-commit processing.** Start async processing only after the metadata row is committed, or a fast worker may look up a row that does not exist yet. `@TransactionalEventListener(phase = AFTER_COMMIT)` with `@Async` is the Spring idiom.

### Testing strategy
- MinIO in Testcontainers for real S3 calls, including presigned URLs.
- Unit tests for name sanitizing and type detection with sample files committed to `src/test/resources`.
- A test with a crafted oversized image to prove the decompression-bomb guard.

### Trade-offs, scaling and failure modes
- Proxying uploads through the app is simple and allows inline validation but costs bandwidth and threads; presigned direct uploads scale much better but validate later.
- `@Async` work is lost on a crash; a durable queue (project 17) or a status sweeper that re-enqueues `PENDING_SCAN` files makes processing reliable.
- Serve downloads through a CDN with signed URLs when traffic grows; your app then only signs URLs.

### Stretch goals
- Resumable uploads (chunked, with S3 multipart upload).
- Image variants on demand with caching.
- Per-user storage quotas enforced atomically.

### Skills practised
- [Ch 5 — Spring MVC and REST APIs](../chapters/05-spring-mvc-and-rest/README.md): multipart upload, limits, Problem Details.
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): `@Async`, `@TransactionalEventListener`, scheduled cleanup.
- [Ch 6 — REST clients and HTTP interfaces](../chapters/06-rest-clients-and-http-interfaces/README.md): calling external services (scanner) with timeouts.
- [Ch 12 — Testing Spring applications](../chapters/12-testing-spring-apps/README.md): Testcontainers.

### In the interview
**30-second answer:** I built a media service that accepts uploads, stores them in private S3-compatible storage under random keys, detects their real type from content, scans them and creates thumbnails asynchronously. The key design decision was two upload paths: small files up to 10 MB stream through the API, while large files up to 2 GB go straight from the browser to storage with a presigned URL, so big bytes never touch my threads. The hardest part was never trusting the client — file names, declared types and decompression-bomb images — which I handled with generated keys, Tika magic-byte detection and dimension checks. Memory per upload is constant regardless of size.

**Key terms to know:**
- **Object storage (S3/MinIO)** — blob storage addressed by key in a bucket; the private store for originals and thumbnails.
- **Presigned URL** — a time-limited URL granting one specific upload or download without credentials.
- **Multipart upload (HTTP)** — the `multipart/form-data` request type for files; Boot limits it with `spring.servlet.multipart.*`.
- **Streaming vs buffering** — passing bytes through with an `InputStream` vs loading the whole file into memory.
- **Magic bytes / content sniffing** — identifying a file type from its first bytes (Tika), not its name.
- **Decompression bomb** — a tiny file that expands to huge dimensions when decoded.
- **Path traversal** — a file name like `../../etc/passwd` escaping the intended folder; avoided with generated keys.
- **`@Async` + `@TransactionalEventListener(AFTER_COMMIT)`** — run processing in the background only after the metadata row commits.
- **Status state machine** — `PENDING_SCAN → READY | REJECTED`.

**Q:** How do you handle a 2 GB upload?

**A:** The app issues a presigned PUT URL with a short expiry and fixed content length and type, and the browser uploads straight to object storage. When the client calls `complete`, the app does a `HEAD` on the object to check size and type, then runs the same scan pipeline. Big bytes never pass through application threads or memory.

**Q:** How do you know a file is really a PNG?

**A:** I inspect its magic bytes with Apache Tika and ignore both the extension and the declared `Content-Type`, which the client controls. For images I also read the header dimensions before decoding fully, rejecting anything above a pixel limit to stop decompression bombs.

**Q:** Why start processing after commit?

**A:** If the async worker starts before the metadata row is committed, it may look up a row that does not exist yet, or one that is then rolled back. `@TransactionalEventListener(phase = AFTER_COMMIT)` with `@Async` starts the work only once the row is durable.

**Q:** Why does reading `getBytes()` on a `MultipartFile` hurt?

**A:** It loads the whole file into the heap, so ten concurrent 10 MB uploads cost 100 MB, and larger limits cause out-of-memory errors. The servlet container already spools large parts to a temp file; I read them with `getInputStream()` and pass the known size to the S3 client, so memory stays constant.

**Q:** Why return 404 instead of 403 when another user asks for my file?

**A:** A 403 confirms the file exists, which leaks information (for example, that an id is valid). Returning 404 for "not yours" makes guessed ids indistinguishable from missing ones. Ownership is checked in the query itself (`where id = ? and owner_sub = ?`).

**Q:** The app crashes while thumbnails are being generated. What happens?

**A:** `@Async` work lives only in memory, so it is lost and the file stays `PENDING_SCAN`. A scheduled sweeper re-enqueues files stuck in that state for more than a few minutes; for stronger guarantees I would use a durable queue (project 17). Thumbnail writes use deterministic keys, so re-running is idempotent.

**Q:** Presigned direct upload vs proxying through the app — what is the trade-off?

**A:** Proxying is simple and lets me validate inline before storing, but costs bandwidth, threads and memory for every byte. Presigned uploads scale much better and offload transfer to the storage service, but validation happens after the fact, so files must stay unusable until the pipeline marks them `READY`.

**Q:** What changes at 100x traffic?

**A:** Downloads move behind a CDN with signed URLs so the app only signs URLs. Processing moves to a durable queue with autoscaled workers. Abandoned presigned uploads are cleaned by a bucket lifecycle rule as well as the nightly job, and per-user quotas need atomic enforcement.

**Q:** How do you test this without AWS?

**A:** MinIO in Testcontainers gives real S3 calls, including presigned URLs, which I exercise by uploading 50 MB with a plain HTTP client. Name sanitizing and type detection are unit-tested with sample files in `src/test/resources`, including a renamed executable and a crafted oversized image. Awaitility waits for the async status change.

**Common mistakes in answers:**
- Trusting the extension or `Content-Type` — both are attacker-controlled; check content.
- Storing files under the user's original name — invites path traversal and collisions; generate keys.
- "`@Async` makes processing reliable" — in-memory async work is lost on a crash without a sweeper or queue.

### TL;DR
Stream small files, presign big ones, store under random keys in private object storage, verify content not names, and process asynchronously after commit.

## 16. Event-driven Notifications with Kafka
**Level:** Intermediate · **Time:** 2–3 weeks · **Prerequisites:** Ch 14, Ch 12

### What it is
A notification service that listens to business events ("order shipped", "password changed", "comment on your post") published by other services to **Apache Kafka**, decides who should be told and how (email, SMS, push, in-app), renders a message from a template, and delivers it — exactly once from the user's point of view. **Kafka** is a distributed, durable log: producers append records to **topics**, which are split into **partitions**; **consumer groups** read partitions in order and track their position with **offsets**.

### The problem it solves
Sending notifications inside the business transaction ("ship order, then call the email API") couples every service to every channel and makes slow email providers slow down checkout. Moving notifications behind an event stream decouples them, but introduces the hard parts of messaging: duplicates, ordering, poison messages, retries and back-pressure. This project is a safe place to learn them.

### Real-world examples
- The [Apache Kafka documentation](https://kafka.apache.org/documentation/) explains topics, partitions, consumer groups and delivery semantics — read the "Message Delivery Semantics" section before milestone 3.
- [Novu](https://github.com/novuhq/novu) is an open-source notification infrastructure (multi-channel, templates, preferences, digests); its concepts map closely to this design.
- The [Spring for Apache Kafka reference](https://docs.spring.io/spring-kafka/reference/) covers `@KafkaListener`, error handlers, dead-letter publishing and `@RetryableTopic`.

### Requirements
**Functional**
- Consume `order-events`, `account-events` and `social-events` topics (JSON with an `eventId`, `type`, `userId`, payload).
- User preferences per event type and channel (e.g. no SMS for social events, quiet hours 22:00–07:00).
- Templates per type, channel and locale.
- In-app notifications stored and readable via `GET /api/notifications` with unread counts.
- Digest: social events in quiet hours are batched into one morning email.

**Non-functional**
- At-least-once consumption with **idempotent** handling: a redelivered event never produces a second notification.
- Per-user ordering preserved (events for one user are processed in order).
- A failing provider does not block other users: retries with back-off on separate retry topics, then a dead-letter topic.
- 2,000 events per second with 6 partitions and 3 instances; end-to-end p95 under 2 s for in-app notifications.

### The architecture
```mermaid
flowchart TD
    P1["Order service"] --> K1["Kafka topic order-events - key userId"]
    P2["Account service"] --> K2["Kafka topic account-events"]
    K1 --> L["@KafkaListener - group notifications"]
    K2 --> L
    L --> D["Dedup - processed_event table"]
    D --> R["Router - preferences and quiet hours"]
    R --> T["TemplateRenderer"]
    T --> CH["Channel senders - email, SMS, push, in-app"]
    L -.->|"failure"| RT["Retry topics - 1 s, 10 s, 60 s"]
    RT -.->|"exhausted"| DLT["Dead-letter topic"]
    CH --> DB[("PostgreSQL - notifications, preferences")]
```
- **Producers** — key each record by `userId` so all of a user's events land in one partition, which preserves their order.
- **Listener** — a `@KafkaListener` in consumer group `notifications`, concurrency 2 per instance; manual or batch offset commits after processing.
- **Dedup** — insert `eventId` into `processed_event` in the same database transaction as the notification rows; a duplicate key means "already done".
- **Router** — reads preferences and decides channels; quiet-hours events go to a digest table.
- **Channel senders** — call providers via `RestClient`; failures throw a retryable exception.
- **Retry topics and DLT** — `@RetryableTopic` sends failed records to delayed retry topics so the main partition keeps flowing, then to a **dead-letter topic** for humans.

### Data model
```mermaid
erDiagram
    APP_USER ||--o{ PREFERENCE : "sets"
    APP_USER ||--o{ NOTIFICATION : "receives"
    PREFERENCE {
        bigint user_id FK
        text event_type
        text channel
        boolean enabled
    }
    NOTIFICATION {
        uuid id PK
        bigint user_id FK
        text event_id
        text channel
        text status
        text body
        boolean read
        timestamptz created_at
    }
    PROCESSED_EVENT {
        text event_id PK
        timestamptz processed_at
    }
```

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Messaging | `spring-boot-starter-kafka` (+ `spring-boot-starter-jackson`) | `KafkaTemplate`, `@KafkaListener`, JSON (de)serializers |
| Errors | `DefaultErrorHandler` + `DeadLetterPublishingRecoverer`, or `@RetryableTopic` | blocking vs non-blocking retries |
| Storage | PostgreSQL via Spring Data JPA or `JdbcClient` | dedup, preferences, in-app inbox |
| Tests | `spring-boot-starter-kafka-test` (`@EmbeddedKafka`) or Testcontainers Kafka | real broker semantics in tests |
| Observability | Micrometer Kafka metrics, consumer lag | lag is the key health signal |

### Milestones
1. **Consume and store in-app.** *Done when* an `@EmbeddedKafka` test publishes an event and Awaitility sees one notification row.
2. **Idempotency.** *Done when* publishing the same `eventId` three times yields one notification.
3. **Preferences, templates and quiet hours.** *Done when* table-driven tests cover each rule with a fixed `Clock`.
4. **Retries and DLT.** *Done when* a provider stub failing twice succeeds on the third attempt via retry topics, a permanently failing one lands in the DLT with the exception headers, and other users' events are not delayed.
5. **Poison messages.** *Done when* an invalid JSON record goes straight to the DLT (deserialization errors wrapped with `ErrorHandlingDeserializer`) instead of blocking the partition forever.
6. **Scale-out.** *Done when* three instances share six partitions and the per-user order is preserved in a test that checks sequence numbers.

### Deep dive: the hard parts
- **At-least-once means duplicates.** Kafka redelivers anything whose offset was not committed — after a crash, a rebalance or a timeout. The consumer must be idempotent. The dedup row and the side effect must commit together; for external side effects (an email), pass the `eventId` as the provider's idempotency key or accept rare duplicates.
- **Blocking vs non-blocking retries.** `DefaultErrorHandler` retries in place, which preserves order but stalls the partition. `@RetryableTopic` forwards the record to a delayed retry topic and moves on — no stall, but per-user ordering is broken for that record. Notifications usually prefer non-blocking; a ledger would not.
- **Partition count and keys.** Ordering is per partition, so choose the key that defines "must be ordered" (`userId`). More partitions allow more parallel consumers but cannot be reduced later, and changing the count reshuffles keys.
- **Rebalances.** When an instance joins or leaves, partitions move; in-flight records may be processed twice. Keep processing per record short and commit regularly; set `max.poll.interval.ms` above your worst-case processing time.

### Testing strategy
- `@EmbeddedKafka` for listener wiring, retries and DLT (fast, no Docker), as in [Ch 14](../chapters/14-messaging-async-and-scheduling/README.md).
- Testcontainers Kafka for one end-to-end test with real serialization and multiple consumers.
- Pure unit tests for routing and templates.

### Trade-offs, scaling and failure modes
- Kafka retains events, so a new channel can replay history; RabbitMQ (project 17) is simpler for pure work queues.
- Consumer lag is the leading indicator of trouble — alert on lag growth, not only on errors.
- If the database is down, consuming must pause (container `pause()` or let the error handler back off) rather than dead-letter thousands of good events.

### Stretch goals
- Produce events from a service using the transactional outbox (project 28).
- Per-channel rate limiting (an SMS provider allows 50 messages per second).
- Server-Sent Events push of new in-app notifications (project 19).

### Skills practised
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): Kafka setup, producing with keys, consumer groups, `@EmbeddedKafka`, dead-letter topics, `@RetryableTopic`, idempotence.
- [Ch 12 — Testing Spring applications](../chapters/12-testing-spring-apps/README.md): Testcontainers, Awaitility.
- [Ch 15 — Observability and production](../chapters/15-observability-and-production/README.md): consumer metrics.

### In the interview
**30-second answer:** I built a notification service that consumes business events from Kafka, routes them by user preferences and quiet hours, renders templates and delivers email, SMS, push and in-app messages. The key design decision was accepting at-least-once delivery and making the consumer idempotent: each `eventId` is inserted into a `processed_event` table in the same transaction as the notification rows. The hardest part was retries without stalling partitions, solved with `@RetryableTopic` delayed retry topics and a dead-letter topic, plus `ErrorHandlingDeserializer` for poison messages. Records are keyed by `userId`, so per-user order holds with 6 partitions across 3 instances at 2,000 events per second.

**Key terms to know:**
- **Topic / partition / offset** — a named log split into ordered partitions; the offset is a consumer's position in a partition.
- **Consumer group** — consumers sharing a group id split partitions between them; group `notifications`.
- **Record key and ordering** — records with the same key go to the same partition and stay in order; key = `userId`.
- **At-least-once delivery** — every record is processed, but possibly more than once after crashes or rebalances.
- **Idempotent consumer** — processing a duplicate has no extra effect; the `processed_event` table.
- **Blocking vs non-blocking retry** — `DefaultErrorHandler` retries in place; `@RetryableTopic` moves the record to delayed retry topics.
- **Dead-letter topic (DLT)** — where records go after retries are exhausted, with error headers.
- **Poison message** — a record that can never be processed (e.g. invalid JSON); `ErrorHandlingDeserializer` routes it to the DLT.
- **Rebalance** — partitions moving between consumers when instances join or leave.
- **Consumer lag** — how far a consumer is behind the end of the log; the key health metric.

**Q:** How do you avoid sending a notification twice?

**A:** Kafka is at-least-once, so the consumer inserts the `eventId` into `processed_event` in the same database transaction as its notification rows; a duplicate-key error means "already done" and the record is acknowledged. For external providers I pass the `eventId` as their idempotency key, or accept rare duplicates for channels that cannot dedupe.

**Q:** How do you keep events for one user in order?

**A:** Producers key records by `userId`, so all of one user's events land in the same partition, and Kafka assigns each partition to exactly one consumer in the group at a time. Ordering is only per partition, never across a topic.

**Q:** What is a dead-letter topic for?

**A:** Records that keep failing are moved there with headers describing the exception and origin, so the main topic keeps flowing and someone can inspect, fix and replay them. Without it, one bad record would block its partition forever or be silently dropped.

**Q:** Blocking or non-blocking retries — which and why?

**A:** `DefaultErrorHandler` retries in place, which keeps strict order but stalls the whole partition while it waits. `@RetryableTopic` forwards the record to a delayed retry topic and moves on, so other users are not delayed, but that one record can now be processed after later events for the same user. For notifications that is acceptable; for a ledger it would not be.

**Q:** What happens during a rebalance?

**A:** When an instance joins or leaves, partitions move, and records whose offsets were not committed are redelivered to the new owner — so some are processed twice, which idempotency handles. Long processing can also trigger rebalances if it exceeds `max.poll.interval.ms`, so I keep per-record work short and set that value above the worst case.

**Q:** The database is down. What should the consumer do?

**A:** Pause, not dead-letter: every event would fail, and dumping thousands of good events into the DLT creates a mess. I let the error handler back off with retries on database exceptions, or pause the listener container until the database recovers. Consumer lag grows, and that lag alert tells operators what is happening.

**Q:** How would you scale from 2,000 to 20,000 events per second?

**A:** Parallelism is capped by the partition count, so I would raise partitions (planned up front, since adding them remaps keys and they cannot be reduced) and run more consumer instances. The dedup table and notification inserts become the bottleneck, so I would batch inserts and partition or TTL the `processed_event` table. Slow providers need per-channel rate limits.

**Q:** How did you test retries and poison messages?

**A:** With `@EmbeddedKafka` I publish to the topic and use a provider stub that fails twice then succeeds, asserting with Awaitility that one notification appears via the retry topics. A permanently failing stub must land in the DLT with exception headers, and an invalid JSON record must go straight to the DLT without blocking the next valid record. One Testcontainers Kafka test checks three consumers and per-user sequence numbers.

**Q:** Show a listener with non-blocking retries.

**A:**
```java
@RetryableTopic(attempts = "4", backOff = @BackOff(delay = 1000, multiplier = 10),
                dltTopicSuffix = "-dlt")
@KafkaListener(topics = "order-events", groupId = "notifications")
void on(OrderEvent e) { notifier.handle(e); }

@DltHandler void dead(OrderEvent e) { log.warn("dead-lettered {}", e.eventId()); }
```
In Spring Kafka 4 (Boot 4), `@BackOff` replaces Spring Retry's old `@Backoff`.

**Common mistakes in answers:**
- "Kafka gives exactly-once, so duplicates cannot happen" — exactly-once applies to Kafka-to-Kafka transactions, not to emails or database writes outside Kafka.
- "Kafka keeps a topic in order" — order is only guaranteed within a partition.
- Retrying in place forever on a poison message — it blocks the partition; route it to a DLT.

### TL;DR
A Kafka consumer that is idempotent, ordered per user, retries without stalling, dead-letters poison messages and is measured by consumer lag.

## 17. Image Job Queue with RabbitMQ
**Level:** Intermediate · **Time:** 2 weeks · **Prerequisites:** Ch 14

### What it is
A background job system where a web API enqueues work ("resize this image to 4 sizes", "extract text from this PDF") and a pool of worker processes consumes jobs from **RabbitMQ**, a message broker built around **exchanges** (where producers send messages) and **queues** (where consumers read them), connected by **bindings** with routing keys. Workers acknowledge each job when done, failures are retried with delays, and jobs that keep failing end up in a **dead-letter queue**.

### The problem it solves
Slow work in a request thread makes users wait and wastes server capacity. A **work queue** lets you return immediately, smooth out bursts, add workers when the queue grows, and survive worker crashes without losing jobs. RabbitMQ is the classic tool for this "task distribution" shape, and this project teaches its reliability knobs: acknowledgements, prefetch, publisher confirms, dead-lettering and delayed retries.

### Real-world examples
- RabbitMQ's tutorial ["Work Queues"](https://www.rabbitmq.com/tutorials/tutorial-two-java) introduces competing consumers, manual acknowledgements and fair dispatch with prefetch.
- The RabbitMQ docs on [dead letter exchanges](https://www.rabbitmq.com/docs/dlx) and [consumer acknowledgements and publisher confirms](https://www.rabbitmq.com/docs/confirms) describe the reliability features used here.

### Requirements
**Functional**
- `POST /api/jobs {type, sourceKey, options}` → 202 Accepted with a job id; `GET /api/jobs/{id}` shows status and result.
- Job types: `THUMBNAILS`, `PDF_TEXT`; each type has its own queue and worker settings.
- Priority lane: jobs from paying users go to a higher-priority queue.
- Failed jobs retried 3 times with delays of 10 s, 1 min, 10 min, then dead-lettered; an admin endpoint can re-queue dead letters.

**Non-functional**
- No job is lost if a worker crashes mid-job or the broker restarts (durable queues, persistent messages, manual acks, publisher confirms).
- Each worker processes at most N jobs at once (prefetch), so a slow job does not starve others.
- Scale from 1 to 10 workers without configuration changes; 50 jobs per second total.

### The architecture
```mermaid
flowchart TD
    API["Job API"] -->|"publish with confirm"| EX["Exchange jobs - direct"]
    EX -->|"thumbnails"| Q1["Queue jobs.thumbnails"]
    EX -->|"pdf-text"| Q2["Queue jobs.pdf-text"]
    Q1 --> W1["Thumbnail workers - prefetch 4"]
    Q2 --> W2["PDF workers - prefetch 1"]
    W1 -.->|"reject"| RX["Retry exchange - TTL queues 10s 1m 10m"]
    RX -.->|"expired"| EX
    W1 -.->|"after 3 retries"| DLQ["Dead-letter queue"]
    W1 --> DB[("PostgreSQL - jobs table")]
    API --> DB
```
- **Job API** — stores a job row (`QUEUED`), then publishes; publisher confirms tell it the broker has safely stored the message.
- **Exchange `jobs`** — a direct exchange routing by job type.
- **Work queues** — durable, one per type; consumers compete for messages.
- **Workers** — `@RabbitListener` with manual ack after the job row is updated; prefetch limits unacknowledged messages per consumer.
- **Retry queues** — queues with a message TTL whose dead-letter exchange points back to `jobs`, implementing delayed retries without a plugin.
- **DLQ** — final parking place with the failure reason in headers.

### Data model
```mermaid
erDiagram
    JOB ||--o{ JOB_ATTEMPT : "tried"
    JOB {
        uuid id PK
        text type
        text status
        text source_key
        jsonb options
        jsonb result
        int priority
        timestamptz created_at
    }
    JOB_ATTEMPT {
        uuid job_id FK
        int attempt
        text worker
        text outcome
        text error
        timestamptz finished_at
    }
```

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Messaging | `spring-boot-starter-amqp` (Spring AMQP) | `RabbitTemplate`, `@RabbitListener`, declarables |
| Reliability | `spring.rabbitmq.publisher-confirm-type=correlated`, manual ack mode | no silent loss on either side |
| Broker | RabbitMQ in a container | quorum queues for durability |
| Workers | a separate Spring Boot app (same code base, `worker` profile) | scale independently of the API |
| Tests | Testcontainers RabbitMQ | real routing, TTL and DLX |

### Milestones
1. **Topology as code.** Declare exchanges, queues and bindings as beans. *Done when* the app creates them on a fresh broker and a test inspects them.
2. **Enqueue and process.** *Done when* a posted job reaches `DONE` and the result is stored.
3. **Crash safety.** *Done when* killing a worker mid-job (a test that throws before ack, or stops the container) causes redelivery and the job still completes once.
4. **Delayed retries and DLQ.** *Done when* a job failing every time shows exactly 4 attempts with the expected delays (use short TTLs in tests) and lands in the DLQ.
5. **Fairness and priority.** *Done when* with prefetch 1, a slow PDF job does not block other PDF jobs on other workers, and priority jobs overtake a backlog.

### Deep dive: the hard parts
- **Acknowledgements.** With auto-ack, a message is gone as soon as it is delivered — a crash loses it. Manual ack after the work (and after the database update) gives at-least-once; that means a job can run twice after a crash, so workers must be idempotent (e.g. check job status first, write outputs to deterministic keys).
- **Publisher confirms and the "save then publish" gap.** If the API saves the job row and crashes before publishing, the job is stuck in `QUEUED`. Either publish from an outbox (project 28) or run a sweeper that re-publishes old `QUEUED` jobs. Confirms cover the other half: knowing the broker accepted the message.
- **Prefetch.** It caps unacknowledged messages per consumer. Too high and one worker hoards jobs while others idle; too low and throughput suffers on fast jobs. Start at 1 for slow jobs, higher for quick ones, and measure.
- **Delayed retry without blocking.** Requeueing immediately (`requeue=true`) creates a hot loop on a failing message. TTL-based retry queues, or Spring AMQP's retry interceptor with a `RepublishMessageRecoverer`, spread retries out.

### Testing strategy
- Testcontainers RabbitMQ with `@ServiceConnection` for the topology, retries and DLQ.
- Worker logic unit-tested without a broker.
- A chaos-style test that stops the worker's listener container mid-processing.

### Trade-offs, scaling and failure modes
- RabbitMQ is excellent for task queues with per-message acks and routing; Kafka is better for replayable event streams and very high throughput.
- Quorum queues (Raft-replicated) are the durable choice; classic mirrored queues were removed in RabbitMQ 4.0, so replication now means quorum queues or streams, at some throughput cost.
- Long-running jobs (minutes) can exceed the broker's consumer acknowledgement timeout; split them or extend the timeout deliberately.

### Stretch goals
- Autoscale workers on queue depth (Kubernetes KEDA or a simple script).
- Job cancellation via a status check before each expensive step.
- Progress reporting to the client via SSE.

### Skills practised
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): messaging concepts, RabbitMQ with Spring AMQP, idempotence, the outbox.
- [Ch 12 — Testing Spring applications](../chapters/12-testing-spring-apps/README.md): Testcontainers and `@ServiceConnection`.
- [Ch 3 — Configuration and auto-configuration](../chapters/03-configuration-and-auto-configuration/README.md): profiles for API vs worker.

### In the interview
**30-second answer:** I built a job queue where an API stores a job row and publishes it to a RabbitMQ direct exchange, and separate worker apps consume per-type queues, process the job and acknowledge it. The key design decision was guaranteeing no lost jobs: durable quorum queues, persistent messages, publisher confirms, manual acks after the job row is updated, and a sweeper for the save-then-publish gap. The hardest part was delayed retries without a plugin, done with TTL retry queues whose dead-letter exchange routes back to the main exchange, then a final dead-letter queue after 3 retries. Prefetch 1 for slow PDF jobs keeps one slow job from starving others.

**Key terms to know:**
- **Exchange / queue / binding / routing key** — producers publish to exchanges, which route to queues via bindings that match routing keys.
- **Direct exchange** — routes by exact routing key; `jobs` routes `thumbnails` and `pdf-text`.
- **Competing consumers** — several workers read one queue, each message goes to one of them.
- **Manual acknowledgement** — the worker tells the broker "done" after the work; unacked messages are redelivered.
- **Publisher confirms** — the broker tells the producer a message is safely stored.
- **Prefetch** — the maximum number of unacknowledged messages a consumer holds.
- **Dead-letter exchange (DLX) / DLQ** — where rejected or expired messages go; used for both delayed retries and final parking.
- **Quorum queue** — a Raft-replicated durable queue type; the durable choice in RabbitMQ 4.
- **Idempotent worker** — running a job twice has the same result; required because delivery is at-least-once.

**Q:** How do you make sure a job is not lost?

**A:** On the producer side, durable queues, persistent messages and publisher confirms tell me the broker has stored the message; on the consumer side, manual ack only after the job's result and status are committed. The remaining gap — the API saved the row but crashed before publishing — is closed by a sweeper that re-publishes old `QUEUED` jobs, or by a transactional outbox.

**Q:** What does prefetch do?

**A:** It caps how many unacknowledged messages the broker pushes to one consumer. With prefetch 1, a worker gets a new job only after acking the last, so a slow PDF job does not hold others hostage while other workers idle. For fast thumbnail jobs a higher prefetch (4) cuts round-trip overhead.

**Q:** RabbitMQ or Kafka for this?

**A:** RabbitMQ: per-message acks, routing by type, priorities and delayed retries via TTL and dead-lettering fit a task queue, and I do not need replay or long event history. Kafka would be better for a replayable event stream at very high throughput, but it has no per-message ack or native delay.

**Q:** Why not just `requeue = true` on failure?

**A:** The message goes straight back to the head of the queue and is redelivered immediately, creating a hot loop that burns CPU and floods logs while blocking progress. TTL-based retry queues spread attempts out (10 s, 1 min, 10 min), and after the last attempt the message is dead-lettered with the reason in headers.

**Q:** A worker crashes halfway through a job. What happens?

**A:** The message was never acked, so when the channel closes the broker redelivers it to another worker. The job may now run twice, so workers check the job status first and write outputs to deterministic keys, making a re-run harmless. A test throws before ack and asserts the job completes exactly once.

**Q:** Auto-ack vs manual ack?

**A:** With auto-ack the broker considers the message done as soon as it is delivered, so a crash mid-job loses it — at-most-once. Manual ack after the work gives at-least-once, at the cost of possible duplicates. For jobs that matter, at-least-once plus idempotency is the standard choice.

**Q:** How do you scale from 1 to 10 workers, and what breaks first?

**A:** Workers are competing consumers on the same queues, so adding instances needs no configuration change; I would autoscale on queue depth. The first limits are the shared database the workers update and the object storage they write to. Very long jobs can also exceed RabbitMQ's consumer acknowledgement timeout (30 minutes by default) and get the channel closed.

**Q:** How do you test retries and dead-lettering?

**A:** With Testcontainers RabbitMQ and `@ServiceConnection`, I use short TTLs in the test profile, a worker that always fails, and assert exactly 4 attempts in `job_attempt` with increasing gaps, then the message in the DLQ. Topology is declared as beans and a test inspects it on a fresh broker. Worker logic is unit-tested without a broker.

**Q:** Show a manual-ack listener.

**A:**
```java
@RabbitListener(queues = "jobs.thumbnails", ackMode = "MANUAL")
void handle(JobMessage m, Channel ch, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
    try { worker.run(m.jobId()); ch.basicAck(tag, false); }
    catch (Exception e) { ch.basicReject(tag, false); } // false = do not requeue; goes to DLX
}
```

**Common mistakes in answers:**
- "Manual ack gives exactly-once" — it gives at-least-once; workers must be idempotent.
- Forgetting the producer side — without confirms, a message can be lost before it ever reaches a queue.
- Setting prefetch very high "for speed" — one worker hoards messages while others idle.

### TL;DR
A durable work queue with confirmed publishing, manual acks, prefetch-based fairness, TTL-driven delayed retries and a dead-letter queue — and idempotent workers because delivery is at-least-once.

## 18. Bulk CSV Import with Spring Batch
**Level:** Intermediate · **Time:** 2 weeks · **Prerequisites:** Ch 7, Ch 14

### What it is
A nightly import of a partner's customer file — up to 5 million CSV rows — into your database: validate each row, transform it, upsert it, report bad rows, and be able to **restart** a failed run from where it stopped. It uses **Spring Batch**, a framework for jobs made of **steps**; a **chunk-oriented step** reads items one by one, processes them, and writes them in chunks (say 1,000 at a time), each chunk in its own transaction.

### The problem it solves
"Loop over the file and insert" works for 1,000 rows. At millions it runs out of memory, takes hours, dies at row 3,200,000 and has to start over, and one bad row aborts everything. Batch processing needs chunking, skip and retry policies, restartability and reporting — exactly what Spring Batch provides, if you understand its model.

### Real-world examples
- The [Spring Batch reference](https://docs.spring.io/spring-batch/reference/) documents jobs, steps, chunk-oriented processing, skip and retry, and the job repository used for restarts.
- The Spring guide ["Creating a Batch Service"](https://spring.io/guides/gs/batch-processing) is the minimal CSV-to-database job; this project is that guide grown up.

### Requirements
**Functional**
- Job `customerImport` with parameter `file` and `runDate`: step 1 validates the header and counts rows; step 2 imports; step 3 writes a report of rejected rows and emails a summary.
- Rows with invalid email, unknown country code or missing id are **skipped** and reported, up to a limit of 1% of rows; above that the job fails.
- Upsert by `external_id`; unchanged rows do not update `updated_at`.
- Admin endpoints to start, stop, restart and inspect executions.

**Non-functional**
- 5 million rows in under 20 minutes on a laptop-class machine with PostgreSQL.
- Memory use independent of file size.
- A failure at any point can be restarted and resumes from the last committed chunk without duplicating rows.

### The architecture
```mermaid
flowchart TD
    TR["Trigger - schedule or REST"] --> JO["JobOperator"]
    JO --> J["Job customerImport"]
    J --> S1["Step validateHeader - tasklet"]
    S1 --> S2["Step importRows - chunk 1000"]
    S2 --> S3["Step report - tasklet"]
    S2 --> RD["FlatFileItemReader - streaming CSV"]
    S2 --> PR["Processor - validate and map"]
    S2 --> WR["JdbcBatchItemWriter - upsert"]
    WR --> DB[("PostgreSQL - customers")]
    J -.-> JR[("JobRepository - batch metadata tables")]
```
- **JobOperator** — starts, stops and restarts jobs (in Spring Batch 6 it also does what `JobLauncher` did).
- **Tasklet steps** — single-shot steps for validation and reporting.
- **Chunk step** — reader → processor → writer, committing every 1,000 items and saving the reader's position in the execution context.
- **Reader** — streams the CSV line by line and maps it to a `CustomerRow` record.
- **Processor** — Bean Validation plus lookup of reference data; returns `null` to filter, throws a skippable exception to reject.
- **Writer** — batched `INSERT ... ON CONFLICT (external_id) DO UPDATE ... WHERE` the data differs.
- **JobRepository** — stores job and step executions and checkpoints. Boot 4's plain batch starter runs **without a database** by default; restartability needs the JDBC-backed repository (`spring-boot-starter-batch-jdbc`).

### Data model
```mermaid
erDiagram
    CUSTOMER {
        bigint id PK
        text external_id UK
        text email
        text country
        text name
        timestamptz updated_at
    }
    IMPORT_REJECT {
        bigint id PK
        bigint job_execution_id
        int line_number
        text reason
        text raw_line
    }
```
Spring Batch adds its own metadata tables (`BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, `BATCH_STEP_EXECUTION`, execution contexts) through its schema scripts.

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Batch | `spring-boot-starter-batch-jdbc` (Spring Batch 6) | persistent job repository for restarts |
| I/O | `FlatFileItemReader`, `JdbcBatchItemWriter` | streaming read, JDBC batch writes |
| Validation | Bean Validation in the processor | reuse annotations |
| Retries | Spring Batch 6 uses Framework 7's core retry support | transient database errors |
| Tests | `spring-batch-test` (`JobOperatorTestUtils` or the launcher utilities of your version) | run jobs and steps in tests |

### Milestones
1. **Happy path.** *Done when* a 10,000-row file imports in a `@SpringBatchTest` and counts match.
2. **Skips and reporting.** *Done when* a file with 50 bad rows completes with 50 rejects reported, and a file with 5% bad rows fails the job.
3. **Upsert semantics.** *Done when* importing the same file twice changes nothing and the second run's write count of changed rows is 0.
4. **Restartability.** *Done when* a test that fails the writer at chunk 7 and then restarts the same job instance ends with exactly the full row count and no duplicates.
5. **Performance.** Tune chunk size, JDBC batch size and indexes. *Done when* a 5-million-row run meets the 20-minute budget, recorded in your README with the machine used.

### Deep dive: the hard parts
- **Chunk transactions and restarts.** Each chunk commits its writes and the reader's checkpoint in the same transaction. After a crash, a restart of the same job instance (same identifying parameters) resumes after the last committed chunk. Using a new parameter value on each run creates a new instance — and restarts from zero.
- **Skip vs retry vs fail.** Retry transient errors (deadlock, connection reset) a few times; skip data errors (bad email) and record them; fail on anything systemic (missing reference table). When a write fails inside a chunk, Spring Batch rolls back and re-processes items one at a time to isolate the bad one — slow, so validate in the processor where possible.
- **Idempotent writes.** Upserts make the writer safe to replay, which matters because a chunk can be written, then the commit fail, then re-run.
- **Throughput.** Most time goes to database round trips. JDBC batching (`reWriteBatchedInserts=true` for the PostgreSQL driver), a sensible chunk size (500–5,000), and avoiding per-row lookups (cache reference data in memory) usually matter far more than multi-threading.

### Testing strategy
- Step-level tests for reader mapping and processor rules with small fixture files.
- Job-level tests for skip limits and restart behaviour, using a writer that fails on demand.
- A generated large file (not committed) for a manual performance run.

### Trade-offs, scaling and failure modes
- Spring Batch is heavyweight for small imports; for a 10,000-row admin upload, a simple service with `JdbcClient` batch updates may be enough.
- Partitioning (split the file into ranges processed in parallel) increases throughput but makes ordering and reporting harder.
- Running the job inside the web app competes with user traffic; a separate batch deployment or scheduled container isolates it.

### Stretch goals
- Partition the import across 4 threads by line ranges and compare timings.
- Read from object storage (S3) instead of a local file.
- Add a JFR or Micrometer dashboard of items read, written and skipped per second.

### Skills practised
- [Ch 7 — Data access with JDBC and transactions](../chapters/07-data-access-jdbc-and-transactions/README.md): transactions, batch updates, upserts.
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): scheduling a job, background work.
- [Ch 17 — Spring AI and what's new](../chapters/17-spring-ai-and-whats-new/README.md): the Boot 4 change to Spring Batch's default (no database unless you use the JDBC starter).
- [Ch 5 — Spring MVC and REST APIs](../chapters/05-spring-mvc-and-rest/README.md): admin endpoints.

### In the interview
**30-second answer:** I built a nightly Spring Batch job that imports up to 5 million partner CSV rows: a tasklet validates the header, a chunk step reads, validates and upserts 1,000 rows per transaction, and a final step reports rejected rows. The key design decision was restartability: with the JDBC job repository, each chunk commits its writes and the reader's checkpoint together, so a failed run resumes from the last committed chunk. The hardest part was separating skip, retry and fail — bad data is skipped up to 1%, transient database errors are retried, systemic errors fail the job — and making the writer an idempotent upsert. A 5-million-row run finished inside the 20-minute budget with constant memory.

**Key terms to know:**
- **Job / step / job instance / job execution** — a job is made of steps; an instance is identified by its identifying parameters; each run attempt is an execution.
- **Chunk-oriented step** — reader → processor → writer, committing every N items in one transaction.
- **Tasklet step** — a single-shot step; used for header validation and reporting.
- **`JobRepository`** — stores executions and checkpoints; Boot 4 needs `spring-boot-starter-batch-jdbc` to persist it.
- **`JobOperator`** — starts, stops and restarts jobs; in Spring Batch 6 it also covers the old `JobLauncher` role.
- **Execution context / checkpoint** — saved reader position that a restart resumes from.
- **Skip / retry policy** — skip bad items with a limit; retry transient exceptions a few times.
- **Idempotent upsert writer** — `INSERT ... ON CONFLICT ... DO UPDATE ... WHERE` data differs; safe to replay.
- **JDBC batching** — sending many statements in one round trip; the main performance lever.

**Q:** How does Spring Batch restart a failed job?

**A:** The JDBC job repository stores each step execution and the reader's position in the execution context, committed in the same transaction as each chunk's writes. Restarting the same job instance — same identifying parameters — resumes the import step after the last committed chunk, and steps that already completed are skipped. A test fails the writer at chunk 7, restarts, and asserts the exact row count with no duplicates.

**Q:** What changed for Spring Batch in Boot 4?

**A:** The basic batch starter now runs with a resourceless, in-memory job repository, so no metadata tables are needed — but nothing survives a restart. To persist metadata for restarts and monitoring you add `spring-boot-starter-batch-jdbc`. Spring Batch 6 also moved its retry support onto Framework 7's core retry and folded `JobLauncher`'s role into `JobOperator`.

**Q:** Skip or retry?

**A:** Retry transient infrastructure errors such as deadlocks or connection resets a few times, because the same item will probably succeed next time. Skip bad data (invalid email, unknown country) up to a limit and record it in the reject table. Fail on anything systemic, such as a missing reference table, because skipping would quietly reject every row.

**Q:** Why did my restart start from zero?

**A:** A new value for an identifying job parameter (for example a timestamp added on every launch) creates a new job instance instead of a new execution of the old one, and a new instance starts fresh. Only `file` and `runDate` are identifying here; anything added just to force a rerun must be non-identifying.

**Q:** Why must the writer be idempotent if chunks are transactional?

**A:** On a normal restart it does not have to be, because a chunk's writes and its checkpoint commit together, so committed rows are never replayed. Idempotency covers everything else: an ambiguous commit where the database committed but the driver reported an error and the chunk is retried, an operator re-running the same file as a new job instance, or the partner resending yesterday's file. An upsert keyed by `external_id` that updates only when data differs makes all of these harmless and keeps `updated_at` stable.

**Q:** What happens when one row in a chunk fails at write time?

**A:** Spring Batch rolls back the chunk and, with a skip policy, re-processes the items one at a time ("scanning") to find the bad item, skipping it and committing the rest. This is slow, so I validate in the processor where possible, which lets the processor skip the item cheaply without rolling back the chunk.

**Q:** How do you make 5 million rows fast?

**A:** Most time goes to database round trips, so I use JDBC batch writes with `reWriteBatchedInserts=true` for the PostgreSQL driver, a chunk size around 1,000–5,000, and reference data (country codes) cached in memory instead of a lookup per row. Multi-threading or partitioning comes last, because it complicates ordering, restart and reporting.

**Q:** What changes at 10x — 50 million rows?

**A:** A single-threaded step may not meet the budget, so I would partition the file by line ranges and run partitions in parallel, each with its own checkpoint. The job should run in its own deployment so it does not compete with web traffic, and the target table's indexes and vacuum settings matter more.

**Q:** How do you test a batch job?

**A:** Reader mapping and processor rules get small fixture files and plain unit tests. `@SpringBatchTest` with the test utilities runs whole jobs and single steps; job-level tests cover skip limits (50 bad rows pass, 5% fail the job) and restart, using a writer that fails on demand. A generated large file is used only for a manual performance run.

**Q:** Show the chunk step definition.

**A:**
```java
@Bean Step importRows(JobRepository repo, PlatformTransactionManager tx) {
    return new StepBuilder("importRows", repo)
        .<CustomerRow, Customer>chunk(1000).transactionManager(tx)
        .reader(reader).processor(processor).writer(upsertWriter)
        .faultTolerant().skip(InvalidRowException.class).skipLimit(50_000)
        .build();
}
```
Spring Batch 6 deprecates the old `chunk(int, PlatformTransactionManager)` form in favour of this builder.

**Common mistakes in answers:**
- "Restart re-reads the whole file and skips existing rows" — it resumes from the saved checkpoint; idempotent writes are a second safety net.
- "Make each run unique with a timestamp parameter" — that defeats restart by creating a new instance.
- Reaching for multi-threading first — batching and avoiding per-row lookups usually matter far more.

### TL;DR
A restartable, chunked import with skip limits, upserts and a reject report — and the Boot 4 detail that restartability needs the JDBC job repository.

## 19. Live Dashboard with WebFlux and SSE
**Level:** Intermediate · **Time:** 2 weeks · **Prerequisites:** Ch 13

### What it is
A real-time operations dashboard: a browser page that shows live order counts, revenue per minute and recent alerts, updating every second without polling. The server is built with **Spring WebFlux**, Spring's **reactive** web stack — non-blocking code built on **Project Reactor** types `Mono` (0 or 1 value) and `Flux` (0 to many values over time) — and pushes updates with **Server-Sent Events (SSE)**, a simple standard where the server keeps one HTTP response open and writes text events into it.

### The problem it solves
Streaming many long-lived connections is where reactive programming shines: 10,000 open dashboards would mean 10,000 blocked platform threads in a classic thread-per-request server. You will learn to model data as streams, share one upstream stream among many subscribers, handle slow clients (back-pressure), and test time-based code without waiting.

### Real-world examples
- MDN's guide to [Server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events) explains the browser `EventSource` API, reconnection and `Last-Event-ID`; the formal definition is in the [WHATWG HTML standard](https://html.spec.whatwg.org/multipage/server-sent-events.html).
- The [Spring WebFlux reference](https://docs.spring.io/spring-framework/reference/web/webflux.html) covers annotated and functional endpoints and streaming responses.

### Requirements
**Functional**
- Ingest order events (from Kafka, or a simulator generating 200 events per second).
- `GET /stream/metrics` (SSE) emits a snapshot every second: orders per minute, revenue per minute, top 5 products.
- `GET /stream/alerts` emits alerts (revenue drop over 50% versus the previous 5-minute average).
- On reconnect, the client receives missed alerts since `Last-Event-ID` (keep the last 15 minutes).
- `GET /api/history?from=&to=` returns per-minute aggregates from a database via **R2DBC** (reactive SQL).

**Non-functional**
- 5,000 concurrent SSE clients on one instance with under 512 MB heap.
- One slow client never slows others or grows memory without bound.
- Heartbeat comment every 15 s so proxies do not close idle connections.

### The architecture
```mermaid
flowchart TD
    SRC["Order events - Kafka or simulator"] --> AGG["Aggregator - windowed Flux"]
    AGG --> HOT["Hot shared stream - Sinks.many multicast"]
    HOT --> E1["SSE client 1"]
    HOT --> E2["SSE client 2"]
    HOT --> EN["SSE client N"]
    AGG --> AL["AlertDetector"]
    AL --> RB["Replay buffer - last 15 min"]
    RB --> E1
    AGG --> R2["R2DBC writer - per-minute rows"]
    R2 --> DB[("PostgreSQL")]
```
- **Aggregator** — groups events into one-second and one-minute windows (`window`/`buffer` operators or explicit counters ticked by `Flux.interval`).
- **Hot shared stream** — one upstream computation shared by all clients; a **hot** publisher emits whether or not anyone listens, unlike a **cold** one that restarts per subscriber.
- **Per-client pipeline** — `onBackpressureLatest()` (keep only the newest snapshot for a slow client) merged with a heartbeat `Flux`.
- **AlertDetector** — compares windows and emits alerts with incrementing ids.
- **Replay buffer** — a bounded in-memory buffer used for `Last-Event-ID` catch-up.
- **R2DBC writer** — stores per-minute aggregates without blocking event-loop threads.

### Data model
| Type | Fields |
|---|---|
| `MetricsSnapshot` record | `Instant at`, `long ordersPerMinute`, `BigDecimal revenuePerMinute`, `List<ProductCount> top` |
| `Alert` record | `long id`, `Instant at`, `String kind`, `String message` |
| `minute_aggregate` table | `minute timestamptz PK`, `orders int`, `revenue numeric` |

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Web | `spring-boot-starter-webflux` (Reactor Netty) | non-blocking connections |
| Streams | Reactor 3.8 (`Flux`, `Sinks`) | operators for windows, sharing, back-pressure |
| SQL | `spring-boot-starter-data-r2dbc` + PostgreSQL R2DBC driver | non-blocking database access |
| Input | a simulator `Flux`, or a Spring Kafka listener feeding a `Sinks.Many` | keep the aggregation pipeline reactive |
| Tests | `spring-boot-starter-webflux-test` (`WebTestClient`), `StepVerifier` with virtual time | deterministic time-based tests |

### Milestones
1. **Simulator and aggregator.** *Done when* a `StepVerifier.withVirtualTime` test proves per-second snapshots for a scripted event sequence, running in milliseconds.
2. **SSE endpoint.** `Flux<ServerSentEvent<MetricsSnapshot>>` with `text/event-stream`. *Done when* `WebTestClient` reads 3 events, and the browser page updates live.
3. **Sharing and back-pressure.** *Done when* 1,000 subscribers in a test cause one aggregation computation, and a deliberately slow subscriber receives only the latest snapshots without memory growth.
4. **Alerts with replay.** *Done when* reconnecting with `Last-Event-ID: 41` replays alerts 42 onwards.
5. **History with R2DBC.** *Done when* per-minute rows are written and queried reactively, and BlockHound (or a test) shows no blocking call on event-loop threads.

### Deep dive: the hard parts
- **Hot vs cold.** Returning `simulator.events().map(...)` from the controller creates a new simulation per client — a cold stream. Share it with a `Sinks.many().multicast()` sink or `.share()`/`.publish().autoConnect()`, and understand what late subscribers miss.
- **Back-pressure over HTTP.** Inside the JVM, Reactive Streams demand is exact. Over the network it becomes TCP flow control: a slow client fills its socket buffer and writes stall. Without a strategy, buffers grow per client. `onBackpressureLatest()` drops intermediate snapshots — correct for a dashboard, wrong for alerts, which use a bounded buffer and then disconnect the client.
- **Never block the event loop.** WebFlux runs on a few Netty threads. One JDBC call or `Thread.sleep` on them freezes thousands of connections. Use R2DBC or wrap unavoidable blocking calls with `subscribeOn(Schedulers.boundedElastic())`.
- **Cancellation.** When a browser tab closes, the subscription is cancelled; `doFinally` should release per-client resources. Test it with `StepVerifier`'s `thenCancel()`.

### Testing strategy
- `StepVerifier` with virtual time for every time-based operator.
- `WebTestClient` against the running server for SSE framing (`id`, `event`, `data`, comments).
- A load script opening thousands of connections to check memory and thread counts.

### Trade-offs, scaling and failure modes
- SSE is one-way and runs over plain HTTP (simple, proxy-friendly, auto-reconnect); WebSockets are bidirectional but need more infrastructure. Dashboards rarely need client-to-server messages.
- State lives in one instance's memory; with several instances, each must consume all events (separate Kafka consumer groups) or read from a shared store.
- Spring MVC with virtual threads can also stream SSE (`SseEmitter`), and for modest connection counts it is simpler; [Ch 13](../chapters/13-reactive-and-webflux/README.md) measures the trade-off.

### Stretch goals
- Per-user filters (only my region) without duplicating the aggregation.
- Expose the same stream as NDJSON for programmatic clients.
- An MVC + virtual-threads version with `SseEmitter`, and a comparison of memory and code complexity.

### Skills practised
- [Ch 13 — Reactive programming and Spring WebFlux](../chapters/13-reactive-and-webflux/README.md): `Mono`/`Flux`, operators, `StepVerifier`, back-pressure, schedulers, hot vs cold, SSE, R2DBC, virtual threads vs WebFlux.
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): consuming the event source.
- [Ch 12 — Testing Spring applications](../chapters/12-testing-spring-apps/README.md): full-stack tests.

### In the interview
**30-second answer:** I built a live operations dashboard on Spring WebFlux that pushes metrics snapshots and alerts to thousands of browsers over Server-Sent Events. The key design decision was one hot, shared aggregation stream (a `Sinks.many().multicast()`) that every client subscribes to, with a per-client back-pressure strategy: keep only the latest snapshot for slow clients, a bounded buffer and then disconnect for alerts. The hardest part was not blocking the event loop, so history goes through R2DBC and BlockHound checks for blocking calls. One instance served 5,000 SSE clients under 512 MB of heap, and time-based logic is tested with virtual time in milliseconds.

**Key terms to know:**
- **Reactive / non-blocking** — threads never wait on I/O; work resumes via callbacks; WebFlux runs on a few Netty event-loop threads.
- **`Mono` / `Flux`** — Reactor types for 0–1 and 0–N values over time.
- **Back-pressure** — a subscriber signals how much it can take; over HTTP it becomes TCP flow control.
- **Hot vs cold publisher** — hot emits regardless of subscribers and is shared; cold restarts per subscriber.
- **`Sinks.Many` / `.share()`** — ways to multicast one upstream to many subscribers.
- **Server-Sent Events (SSE)** — one long HTTP response carrying `text/event-stream` events; browser `EventSource` reconnects with `Last-Event-ID`.
- **Event loop blocking** — a JDBC call or sleep on a Netty thread freezes all its connections.
- **R2DBC** — reactive, non-blocking SQL access.
- **`StepVerifier` with virtual time** — test utility that advances a fake clock.
- **WebFlux vs MVC + virtual threads** — reactive streams vs blocking code on cheap threads.

**Q:** Why WebFlux here rather than MVC?

**A:** Thousands of long-lived streaming connections with per-client back-pressure and stream operators (windows, sharing, merging heartbeats) are WebFlux's sweet spot. For ordinary request/response APIs I would choose MVC with virtual threads, which gives similar scalability for blocking code with a simpler programming model. For a few hundred dashboards, `SseEmitter` on MVC would also be fine.

**Q:** What happens with a slow SSE client?

**A:** Its socket buffer fills, TCP flow control stalls writes, and Reactor stops requesting more for that subscriber. Without a strategy, a buffer for that client would grow without bound. I use `onBackpressureLatest()` for snapshots, so it just gets the newest one, and a bounded buffer for alerts that disconnects the client on overflow; it then reconnects and catches up via `Last-Event-ID`.

**Q:** What is the difference between hot and cold streams, and which bug did it cause?

**A:** A cold publisher starts its work for each subscriber; a hot one emits once and shares. Returning `simulator.events().map(...)` directly created a separate simulation per client — 1,000 clients meant 1,000 aggregations. Sharing through a multicast sink gives one computation, with the trade-off that late subscribers miss earlier events, which the replay buffer covers for alerts.

**Q:** What happens if someone puts a JDBC call in a WebFlux handler?

**A:** It blocks one of the few Netty event-loop threads, and every connection on that thread freezes until it returns — under load the whole server stalls. I use R2DBC, and any unavoidable blocking call is wrapped with `subscribeOn(Schedulers.boundedElastic())`. BlockHound in tests throws when a blocking call happens on a non-blocking thread.

**Q:** How does reconnect with `Last-Event-ID` work?

**A:** Each alert has an incrementing id written as the SSE `id` field. When the connection drops, the browser's `EventSource` reconnects automatically and sends `Last-Event-ID: 41`; the server replays alerts 42 onwards from a bounded 15-minute buffer and then continues live. Snapshots need no replay because the next one arrives within a second.

**Q:** How do you test a once-per-second stream without waiting?

**A:** `StepVerifier.withVirtualTime(() -> pipeline)` replaces Reactor's time source, so `thenAwait(Duration.ofMinutes(1))` advances a minute instantly and I assert exactly 60 snapshots. The publisher must be created inside the supplier for virtual time to apply. `WebTestClient` checks the SSE framing against the running server, and `thenCancel()` checks cleanup on disconnect.

**Q:** What changes with several instances?

**A:** State lives in one instance's memory, so each instance would show only the events it consumed. Either every instance consumes all events (each in its own Kafka consumer group) and computes the same aggregates, or aggregation moves to one place and instances read from a shared store such as Redis. A load balancer must also allow long-lived connections and idle timeouts above the 15-second heartbeat.

**Q:** SSE or WebSockets?

**A:** SSE is one-way server-to-client over plain HTTP, works through proxies, and reconnects automatically with `Last-Event-ID`. WebSockets are bidirectional but need upgrade support and their own reconnection logic. A dashboard does not send data back, so SSE is the simpler fit.

**Q:** Write the SSE endpoint with a heartbeat.

**A:**
```java
@GetMapping(path = "/stream/metrics", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
Flux<ServerSentEvent<MetricsSnapshot>> metrics() {
    var data = hub.snapshots().onBackpressureLatest()
        .map(s -> ServerSentEvent.builder(s).event("metrics").build());
    var beat = Flux.interval(Duration.ofSeconds(15))
        .map(i -> ServerSentEvent.<MetricsSnapshot>builder().comment("hb").build());
    return Flux.merge(data, beat);
}
```

**Common mistakes in answers:**
- "WebFlux is always faster" — it improves concurrency for I/O-bound streaming, not raw speed; for typical CRUD, MVC with virtual threads is simpler and comparable.
- "Back-pressure is automatic over HTTP" — the server still has to choose what to drop or buffer per client.
- Calling blocking libraries "inside `Mono.fromCallable`" without a scheduler — it still runs on the event loop.

### TL;DR
A shared, hot reactive stream pushed to thousands of SSE clients, with per-client back-pressure, replay on reconnect, R2DBC history and virtual-time tests.

## 20. Testcontainers Integration Test Suite
**Level:** Intermediate · **Time:** 1–2 weeks · **Prerequisites:** Ch 12, Ch 9, Ch 14

### What it is
Not a new application, but a **test suite** for one you already built (projects 13, 14 or 16 are ideal): fast unit tests, focused **slice** tests, and integration tests that run against **real** PostgreSQL, Redis and Kafka started in throw-away Docker containers by **Testcontainers**, wired into Spring with **`@ServiceConnection`**. The goal is a suite that catches real bugs, runs in a few minutes in CI, and never flakes.

### The problem it solves
Teams often have either fast tests that miss database and broker bugs (H2, mocks everywhere) or slow, flaky end-to-end tests nobody trusts. Testcontainers gives production-like dependencies on every laptop and CI agent; Spring's test context caching keeps it fast — if you design for it. This project teaches test architecture, not just test syntax.

### Real-world examples
- The [Testcontainers project site](https://testcontainers.com/) documents the modules (PostgreSQL, Kafka, Redis, RabbitMQ and many more) and the lifecycle options you will use.
- The Spring Boot reference on [Testcontainers support](https://docs.spring.io/spring-boot/reference/testing/testcontainers.html) covers `@ServiceConnection`, containers as beans and running the app locally with containers.

### Requirements
**Functional**
- A test pyramid for the chosen app: about 70% unit tests, 20% slice tests (`@WebMvcTest`, `@DataJpaTest`, `@JsonTest`, `@RestClientTest`), 10% full integration tests.
- Integration tests use PostgreSQL, Redis and Kafka containers pinned to the production versions.
- A `TestApplication` main in `src/test/java` runs the app locally with the same containers (`mvn spring-boot:test-run`).
- Contract snapshot tests for public JSON (Problem Details shapes, event payloads).

**Non-functional**
- Whole suite under 4 minutes in CI; each container type starts at most once per run.
- Zero flaky tests over 20 consecutive CI runs (track it).
- At most 3 distinct Spring application contexts in the integration layer (measured from logs).

### The architecture
```mermaid
flowchart TD
    U["Unit tests - no Spring, milliseconds"] --> S["Slice tests - partial contexts"]
    S --> I["Integration tests - @SpringBootTest"]
    I --> CC["Shared ContainersConfig - @ServiceConnection beans"]
    CC --> PG["PostgreSQL container"]
    CC --> RD["Redis container"]
    CC --> KF["Kafka container"]
    I --> CTX["Spring context cache - reused across classes"]
    DEV["TestApplication main for local dev"] --> CC
```
- **Unit layer** — plain JUnit 6 and AssertJ on services and domain logic, with fakes.
- **Slice layer** — Boot test slices that load only the beans relevant to one layer.
- **Integration layer** — a small number of `@SpringBootTest` classes sharing one configuration so the context (and its containers) is cached.
- **ContainersConfig** — a `@TestConfiguration` declaring containers as `@Bean @ServiceConnection` methods; containers become part of the cached context.
- **TestApplication** — `SpringApplication.from(App::main).with(ContainersConfig.class)` for local runs.

### Data model
| Item | Purpose |
|---|---|
| `ContainersConfig` | containers as beans, images pinned (e.g. `postgres:17-alpine`) |
| `AbstractIntegrationTest` | `@SpringBootTest`, `@Import(ContainersConfig.class)`, common helpers |
| Test data builders | `anOrder().withLines(3).build()` — readable, independent fixtures |
| Snapshot files | expected JSON committed under `src/test/resources/snapshots` |

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Containers | Testcontainers 2.x (`testcontainers-postgresql`, `-kafka`, `-junit-jupiter` artifacts) | managed by the Boot 4.1 BOM |
| Spring glue | `spring-boot-testcontainers`, `@ServiceConnection` | connection details without properties |
| Async assertions | Awaitility | wait for Kafka consumers without `sleep` |
| Test starters | Boot 4 per-technology test starters | slices for each module |
| Clients | `RestTestClient` (Framework 7), `MockMvcTester` | HTTP assertions |

### Milestones
1. **Audit the current suite.** Count tests per layer, time them, and count distinct contexts (`logging.level.org.springframework.test.context.cache=DEBUG`). *Done when* you have a baseline table in your README.
2. **Containers as beans.** Replace H2 and embedded brokers in integration tests. *Done when* PostgreSQL-specific SQL (e.g. `ON CONFLICT`, `jsonb`) is covered and passes.
3. **Context discipline.** Remove per-class `@MockitoBean`s and property tweaks that fragment the cache; move them to slices. *Done when* the integration layer uses at most 3 contexts.
4. **Async and messaging tests.** *Done when* Kafka round-trip tests use Awaitility with explicit timeouts and pass 20 times in a row.
5. **Local dev and CI.** *Done when* `mvn spring-boot:test-run` starts the app with containers, and CI caches images and reports per-layer timings.

### Deep dive: the hard parts
- **The context cache is the performance lever.** Spring caches application contexts by their configuration key: classes, profiles, properties, `@MockitoBean`s and more. Every variation creates a new context — and with containers as beans, possibly new containers. Standardize on one base class; keep mocks in slice tests.
- **Data isolation without rollbacks.** `@Transactional` on a `@SpringBootTest` test does not roll back work done by other threads (HTTP server on a random port, Kafka consumers). Clean tables explicitly (`TRUNCATE ... CASCADE` in `@BeforeEach`) or use unique data per test (random ids), which also allows parallel runs.
- **Flakiness sources.** `Thread.sleep`, wall-clock time, test order dependence, shared mutable data and unpinned `latest` images. Replace sleeps with Awaitility, inject `Clock`, randomize test order once to find hidden dependencies, pin images.
- **`@ServiceConnection` vs `@DynamicPropertySource`.** Use `@ServiceConnection` for types Boot knows (JDBC, Redis, Kafka, RabbitMQ, MongoDB, ...); fall back to `@DynamicPropertySource` for custom containers such as WireMock or your own images.

### Testing strategy
This project *is* the testing strategy; additionally, write a short ADR (architecture decision record) explaining what is tested at which layer and why, so new contributors put tests in the right place.

### Trade-offs, scaling and failure modes
- Containers need Docker on every machine; offer a profile that skips container tests locally if Docker is absent, but never in CI.
- Reusing containers across runs (`withReuse(true)`) speeds local work but risks leftover data; keep it off in CI.
- Very large suites may need to split into parallel CI jobs by package; context caching is per JVM.

### Stretch goals
- Add Toxiproxy (via Testcontainers) to inject network latency and test your timeouts.
- Mutation testing (e.g. PIT) to measure whether tests actually detect bugs.
- Spring Cloud Contract or consumer-driven contracts between two of your projects.

### Skills practised
- [Ch 12 — Testing Spring applications](../chapters/12-testing-spring-apps/README.md): the pyramid, slices, `RestTestClient`, `@MockitoBean`, context caching, `@DynamicPropertySource`, Testcontainers and `@ServiceConnection`.
- [Ch 9 — Spring Data beyond JPA, and caching](../chapters/09-spring-data-beyond-jpa-and-caching/README.md): testing Redis with Testcontainers.
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): testing Kafka consumers.

### In the interview
**30-second answer:** I built a test suite for my checkout and cache projects: about 70% unit tests, 20% slice tests and 10% integration tests against real PostgreSQL, Redis and Kafka in Testcontainers, wired with `@ServiceConnection`. The key design decision was treating Spring's test context cache as the performance lever — one shared containers configuration and base class, mocks only in slices — which cut the integration layer to at most 3 contexts. The hardest part was flakiness, which I removed by replacing sleeps with Awaitility, injecting a `Clock`, pinning image versions and cleaning data explicitly. The full suite runs in under 4 minutes in CI with zero flakes over 20 consecutive runs.

**Key terms to know:**
- **Test pyramid** — many fast unit tests, fewer slice tests, few full integration tests.
- **Test slice** — `@WebMvcTest`, `@DataJpaTest`, `@JsonTest`, `@RestClientTest` load only one layer's beans.
- **Testcontainers** — starts real dependencies in throw-away Docker containers from tests.
- **`@ServiceConnection`** — Boot derives connection properties from a container bean; no URLs in test config.
- **`@DynamicPropertySource`** — registers properties at runtime for containers Boot does not know.
- **Context caching** — Spring reuses an application context across test classes with the same configuration key.
- **`@MockitoBean`** — Boot 4's way to replace a bean with a Mockito mock (replaced `@MockBean`); each distinct set creates a new context.
- **Awaitility** — polls for an asynchronous outcome with a timeout instead of sleeping.
- **Flaky test** — passes or fails without code changes, usually from timing, ordering or shared data.

**Q:** Why not H2 for repository tests?

**A:** H2 is not PostgreSQL: SQL dialect, `ON CONFLICT`, `jsonb`, locking behaviour and some migrations differ, so tests can pass on H2 and fail in production. Testcontainers gives the real engine at the production version for a few seconds of startup, shared across the whole run.

**Q:** Your integration tests are slow. Where do you look first?

**A:** The number of distinct Spring contexts. Each new combination of `@MockitoBean`s, properties, profiles or configuration classes builds a new context and, with containers as beans, may start new containers. I turn on `logging.level.org.springframework.test.context.cache=DEBUG`, count contexts, and move per-class mocks into slices.

**Q:** How do you test an asynchronous Kafka consumer reliably?

**A:** Publish the event, then poll the observable outcome (a row in the database) with Awaitility and an explicit timeout — never a fixed `Thread.sleep`. Sleep is either too short (flaky) or too long (slow); polling returns as soon as the condition holds.

**Q:** Does `@Transactional` on a `@SpringBootTest` roll back everything?

**A:** Only work done on the test's own thread in that transaction. Requests to a server on a random port and Kafka listeners run on other threads with their own transactions, so their data is committed and leaks into later tests. I clean tables explicitly in `@BeforeEach` (`TRUNCATE ... CASCADE`) or give each test unique data.

**Q:** `@ServiceConnection` or `@DynamicPropertySource`?

**A:** `@ServiceConnection` for container types Boot knows — JDBC databases, Redis, Kafka, RabbitMQ, MongoDB — because it creates the connection details bean directly. `@DynamicPropertySource` for anything custom, such as WireMock or my own image, where I must map the container's host and port to properties myself.

**Q:** What are the main causes of flaky tests and how did you fix them?

**A:** Sleeps, wall-clock time, test-order dependence, shared mutable data and unpinned `latest` images. I replaced sleeps with Awaitility, injected a `Clock`, ran tests in random order once to find hidden dependencies, used unique data per test, and pinned every image tag. Then I ran CI 20 times in a row and tracked failures.

**Q:** What happens if Docker is not available?

**A:** Container tests cannot start. Locally, a profile can skip them with a clear message so developers can still run unit and slice tests; in CI they must never be skipped, because that would silently remove the most realistic tests. Reusable containers speed local runs but stay off in CI because of leftover data.

**Q:** How does this scale to a suite 10x larger?

**A:** Context caching is per JVM, so very large suites are split into parallel CI jobs by package or module, each sharing its contexts. Unique data per test allows parallel execution within a JVM. Image pulls are cached in CI, and per-layer timing reports show where the time goes.

**Q:** Show a containers configuration.

**A:**
```java
@TestConfiguration(proxyBeanMethods = false)
class ContainersConfig {
    @Bean @ServiceConnection
    PostgreSQLContainer postgres() { return new PostgreSQLContainer("postgres:17-alpine"); }

    @Bean @ServiceConnection(name = "redis")
    GenericContainer<?> redis() { return new GenericContainer<>("redis:8-alpine").withExposedPorts(6379); }
}
```
Testcontainers 2 dropped the generic type parameter on modules such as `PostgreSQLContainer`.

**Common mistakes in answers:**
- "Use `@SpringBootTest` everywhere for realism" — it is slow; most behaviour belongs in unit and slice tests.
- "Add `@MockitoBean` wherever convenient" — each distinct mock set creates a new context and slows the whole suite.
- "Sleep two seconds for the consumer" — fixed sleeps cause both flakiness and slowness.

### TL;DR
A deliberate test pyramid with real dependencies in containers, a disciplined context cache, no sleeps, pinned images and a local dev mode — fast, trustworthy tests that find real bugs.

## 21. Versioned Public API (Framework 7)
**Level:** Intermediate · **Time:** 1–2 weeks · **Prerequisites:** Ch 5, Ch 6

### What it is
A public "Shipments" API that has to make a **breaking change** — renaming fields, splitting an address into structured parts, changing a status enum — while existing clients keep working. It uses **API versioning built into Spring Framework 7**: the version is read from each request (a header here), parsed, validated and used as one more mapping condition (`@GetMapping(version = "2.0")`), with standard deprecation headers telling old clients when their version will be switched off. A matching client uses Framework 7's client-side versioning in an HTTP service interface.

### The problem it solves
Before Framework 7, every team invented its own scheme: `/v1/` URL prefixes copied into every controller, custom request conditions, or one controller full of `if (version == 2)`. Versioning is now first-class in Spring MVC, WebFlux, `RestClient`, `WebClient`, HTTP interfaces and the test clients. This project teaches the feature and the bigger skill around it: evolving an API without breaking people.

### Real-world examples
- Stripe's post ["APIs as infrastructure: future-proofing Stripe with versioning"](https://stripe.com/blog/api-versioning) describes date-named versions, pinning each account to a version, and "version change modules" that transform responses backwards.
- GitHub's REST API uses a date-based [`X-GitHub-Api-Version` header](https://docs.github.com/en/rest/about-the-rest-api/api-versions) — the header strategy this project uses.
- The [Spring Framework API versioning reference](https://docs.spring.io/spring-framework/reference/web/webmvc-versioning.html) describes resolvers, parsing, validation and deprecation handling.

### Requirements
**Functional**
- `GET /api/shipments/{id}`, `GET /api/shipments`, `POST /api/shipments` in versions `1.0`, `1.1` (adds an optional `eta` field) and `2.0` (structured address, renamed `state` → `status`, new enum values).
- Version from header `X-API-Version`; requests without it get the default `1.0`; unsupported versions get 400 Problem Details.
- Version `1.0` is **deprecated**: responses carry `Deprecation`, `Sunset` and a `Link` to the migration guide.
- Per-version OpenAPI documents.
- A Java client library (HTTP service interface) that pins version `2.0`.

**Non-functional**
- One domain model and one service layer; version differences live only at the edges (DTOs and mappers).
- Adding version `2.1` should touch only the endpoints that change, thanks to baseline versions.
- Usage metrics per version and per client, so you know when `1.0` can be removed.

### The architecture
```mermaid
flowchart TD
    C1["Old client - no header"] --> R["ApiVersionResolver - header X-API-Version"]
    C2["New client - 2.0"] --> R
    R --> P["Parser and validation - semantic versions"]
    P --> M["Handler mapping - version is a condition"]
    M --> V1["ShipmentV1Controller - 1.0 and 1.1+"]
    M --> V2["ShipmentV2Controller - 2.0"]
    V1 --> MAP["DTO mappers per version"]
    V2 --> MAP
    MAP --> S["ShipmentService - one domain model"]
    M -.-> DH["Deprecation handler - Deprecation, Sunset, Link"]
    M -.-> MET["Metrics - requests by version"]
```
- **Resolver** — `spring.mvc.apiversion.use.header=X-API-Version` (Boot 4 properties) or `configureApiVersioning` in a `WebMvcConfigurer`.
- **Parser** — the default semantic parser treats `1.1` as `1.1.0`, so versions compare naturally.
- **Version-aware mappings** — `version = "1.0"` matches exactly; `version = "1.1+"` is a **baseline**: 1.1 and every later version until a more specific mapping exists.
- **DTO mappers** — convert one domain `Shipment` to `ShipmentV1` or `ShipmentV2` records, and incoming DTOs back.
- **Deprecation handler** — a `StandardApiVersionDeprecationHandler` bean configured with dates and a link; it emits RFC 9745 `Deprecation` and RFC 8594 `Sunset` headers.
- **Metrics** — a filter or observation tag recording the resolved version.

### Data model
| Type | Version | Shape |
|---|---|---|
| `Shipment` (domain) | — | id, sender, recipient `Address`, status enum, eta |
| `ShipmentV1` | 1.0 | `id`, `to` (one address string), `state` |
| `ShipmentV1_1` | 1.1+ | V1 fields plus optional `eta` |
| `ShipmentV2` | 2.0 | `id`, `recipient {line1, city, postcode, country}`, `status`, `eta` |

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Server versioning | Framework 7 API versioning, `spring.mvc.apiversion.*` | first-class, no custom conditions |
| Client | `@HttpExchange(version = "2.0")` + `spring.http.serviceclient.<group>.apiversion.insert.header` | the client sends its version automatically |
| Docs | springdoc-openapi groups per version | one document per contract |
| Tests | `MockMvcTester` and `RestTestClient`, sending `X-API-Version` explicitly | Framework 7 adds versioning support to its test clients too |

### Milestones
1. **Version 1.0 only.** *Done when* the API works with and without the header, and `X-API-Version: 9.0` returns 400.
2. **Additive 1.1 as a baseline.** *Done when* `1.1` and a listed `1.2` both return `eta`, and `1.0` does not.
3. **Breaking 2.0.** New DTOs and mappers, same service. *Done when* a table-driven test calls each endpoint with each version and checks the JSON shape.
4. **Deprecation.** *Done when* every `1.0` response carries `Deprecation`, `Sunset` and `Link`, and `2.0` responses do not.
5. **Client library and metrics.** *Done when* a client test proves the interface sends `X-API-Version: 2.0`, and a metric shows request counts per version.

### Deep dive: the hard parts
- **Where to branch.** Versioning controllers (or methods) and sharing a single domain and service keeps business logic single-sourced. Versioning the whole application (copying packages per version) doubles maintenance. Stripe's approach — transform the latest response backwards through a chain of version changes — scales to many small versions; separate DTOs per version are simpler for a few large ones.
- **Baselines and supported versions.** With `1.1+`, a request for `1.2` is served by the `1.1+` method — but only if `1.2` is a known supported version (detected from mappings or listed in `spring.mvc.apiversion.supported`). Unknown versions are rejected, which protects you from typos like `2.O`.
- **Header vs path.** A path segment (`/v2/...`) is visible and cache-friendly; a header keeps URLs stable but needs `Vary: X-API-Version` so shared caches do not mix versions. Decide once and document it.
- **Retiring a version.** Announce with headers and docs, watch per-client metrics, contact the remaining callers, then return 400 (or a dedicated error) after the sunset date. The metric is what makes removal safe.

### Testing strategy
- A version × endpoint matrix test with expected status and JSON snapshot per cell.
- Tests for missing, malformed and unsupported versions.
- Client tests with `MockRestServiceServer` asserting the header.

### Trade-offs, scaling and failure modes
- Every live version is code to maintain and test; aim for few versions and prefer additive changes (new optional fields) that need no version at all.
- Default-version behaviour is a contract too: changing the default silently breaks clients that never sent a header. Consider `spring.mvc.apiversion.required=true` for new APIs.
- Gateways and CDNs must forward the version header and include it in cache keys.

### Stretch goals
- Implement Stripe-style backward transformers for a few fields and compare with separate DTOs.
- Version Kafka event schemas the same way (a `version` field and upcasters).
- A Spring Cloud Gateway route that uses a version predicate to send `2.x` traffic to a new service.

### Skills practised
- [Ch 5 — Spring MVC and REST APIs](../chapters/05-spring-mvc-and-rest/README.md): Framework 7 API versioning, Problem Details, springdoc.
- [Ch 6 — REST clients and HTTP interfaces](../chapters/06-rest-clients-and-http-interfaces/README.md): client-side versioning in HTTP service groups.
- [Ch 17 — Spring AI and what's new](../chapters/17-spring-ai-and-whats-new/README.md): Framework 6 → 7 changes.
- [Ch 15 — Observability and production](../chapters/15-observability-and-production/README.md): usage metrics.

### In the interview
**30-second answer:** I built a Shipments API that ships a breaking 2.0 while 1.0 and 1.1 clients keep working, using Spring Framework 7's built-in API versioning with an `X-API-Version` header. The key design decision was one domain model and one service, with versions living only in DTOs, mappers and versioned mappings; `1.1+` is a baseline so additive versions touch only changed endpoints. The hardest part was retiring 1.0 safely, which I handle with `Deprecation`, `Sunset` and `Link` headers plus per-version usage metrics. A version × endpoint matrix test checks the JSON shape for every cell.

**Key terms to know:**
- **Breaking vs additive change** — renaming or removing fields breaks clients; adding optional fields does not.
- **`ApiVersionStrategy` / resolver** — Framework 7 component that reads the version from a header, query parameter, path segment or media type.
- **Semantic version parser** — treats `1.1` as `1.1.0` so versions compare numerically.
- **`version` mapping attribute** — `@GetMapping(version = "2.0")` makes the version a mapping condition.
- **Baseline version (`1.1+`)** — matches 1.1 and every later supported version until a more specific mapping exists.
- **Supported versions** — versions detected from mappings or configured; unknown ones get 400.
- **Deprecation / Sunset headers** — RFC 9745 and RFC 8594 headers emitted by `StandardApiVersionDeprecationHandler`.
- **Tolerant reader** — clients ignore unknown fields, so additions are safe.
- **`Vary` header** — tells caches that responses differ by a request header.

**Q:** How does API versioning work in Spring Framework 7?

**A:** An `ApiVersionStrategy` resolves the version from the request (header, query parameter, path segment or media type parameter), parses it and validates it against the supported versions. Mapping annotations take a `version` attribute — exact like `2.0` or a baseline like `1.1+` — so the version is one more condition when choosing a handler. Deprecated versions get `Deprecation`/`Sunset`/`Link` headers through a deprecation handler.

**Q:** How do you avoid breaking clients in the first place?

**A:** Make additive changes (new optional fields, new endpoints), rely on tolerant readers that ignore unknown fields, and keep OpenAPI contract snapshot tests so every change is reviewed. A new version is only for genuinely breaking changes such as the rename `state` → `status` and the structured address.

**Q:** Header or path versioning — why header here?

**A:** A header keeps URLs stable and matches GitHub's style, but caches must know responses vary by it, so I send `Vary: X-API-Version` and the gateway must forward the header. A path segment (`/v2/...`) is visible, easy to test in a browser and cache-friendly. Both are supported by Framework 7; the important part is choosing one and documenting it.

**Q:** What happens to a request with no version, or version `9.0`?

**A:** With a configured default, a request without the header is treated as `1.0`; without a default, Framework 7 requires a version and missing ones get 400. An unsupported version like `9.0` or a typo like `2.O` fails validation with a 400, which I render as Problem Details. Changing the default later is itself a breaking change, so new APIs should consider requiring the header.

**Q:** Where do version differences live in the code?

**A:** Only at the edges: separate DTO records (`ShipmentV1`, `ShipmentV2`) and mappers, called from version-specific controller methods. The service and domain exist once, so a business rule is fixed in one place. Copying whole packages per version would double maintenance and tests.

**Q:** When can you delete version 1?

**A:** After announcing a sunset date in headers and docs and when per-version, per-client metrics show no meaningful traffic left — and after contacting remaining callers. After the sunset date it returns a clear error. The metric is what makes deletion safe instead of a guess.

**Q:** What changes as versions multiply?

**A:** Each live version is code to maintain and a column in the test matrix, so cost grows linearly. For many small changes, Stripe-style backward transformers (render the latest shape, then transform it back through version changes) scale better than separate DTOs. The real answer is to keep the number of live versions small.

**Q:** How do you test versioning?

**A:** A table-driven test calls each endpoint with each version and compares the JSON to a snapshot per cell, plus tests for missing, malformed and unsupported versions. `MockMvcTester` and `RestTestClient` send `X-API-Version` explicitly. On the client side, `MockRestServiceServer` asserts the HTTP interface sends `X-API-Version: 2.0`.

**Q:** Show the configuration and a versioned mapping.

**A:**
```properties
spring.mvc.apiversion.use.header=X-API-Version
spring.mvc.apiversion.default=1.0
```
```java
@GetMapping(path = "/api/shipments/{id}", version = "1.1+")
ShipmentV1_1 getV11(@PathVariable UUID id) { return mapper.toV11(service.get(id)); }

@GetMapping(path = "/api/shipments/{id}", version = "2.0")
ShipmentV2 getV2(@PathVariable UUID id) { return mapper.toV2(service.get(id)); }
```

**Common mistakes in answers:**
- "Add a new version for every change" — additive changes need no version; each version is ongoing cost.
- Branching with `if (version == 2)` inside services — versions belong at the edge, not in business logic.
- Forgetting caches — header versioning without `Vary` can serve one version's response to another version's client.

### TL;DR
One domain, several contracts: Framework 7's built-in versioning routes requests by header, baselines minimize code, deprecation headers announce retirement, and metrics tell you when it is safe.

## 22. Rate-limited API with Bucket4j and Redis
**Level:** Intermediate · **Time:** 1–2 weeks · **Prerequisites:** Ch 9, Ch 11

### What it is
A public API protected by **rate limits** per API key and plan (Free: 60 requests per minute, Pro: 1,000), enforced consistently across several instances by storing counters in Redis. It uses **Bucket4j**, a Java implementation of the **token bucket** algorithm: each client has a bucket that refills at a steady rate; each request takes a token; an empty bucket means **429 Too Many Requests**.

### The problem it solves
Without limits, one buggy script or attacker can exhaust your capacity and take the service down for everyone, or run up your cloud bill. Spring Security does not include rate limiting, and `@ConcurrencyLimit` caps concurrent calls, not requests per second. You need to choose an algorithm, a key (user, API key, IP), where to enforce it (gateway or app), and what to do when the limit store itself fails.

### Real-world examples
- Stripe's ["Scaling your API with rate limiters"](https://stripe.com/blog/rate-limiters) describes four limiters (request rate, concurrent requests, and two load shedders) built on token buckets in Redis.
- [Bucket4j](https://github.com/bucket4j/bucket4j) documents local and distributed (Redis, JCache, databases) token buckets for Java.
- GitHub documents its [REST API rate limits](https://docs.github.com/en/rest/using-the-rest-api/rate-limits-for-the-rest-api) and the headers clients use to back off.

### Requirements
**Functional**
- API keys with plans stored in PostgreSQL; `X-API-Key` header authenticates.
- Per-key limits by plan, plus a stricter per-key limit on expensive endpoints (`/search`: 10 per minute).
- Anonymous endpoints limited per client IP.
- Responses include remaining quota headers; 429 responses include `Retry-After` and Problem Details.
- Admin can change a key's plan; the new limit applies within one minute.

**Non-functional**
- Limit check adds under 2 ms p95 (one Redis round trip).
- Limits hold across 4 instances: a Free key gets at most 60 requests per minute in total, not 60 per instance.
- If Redis is unavailable: fail **open** for paid plans with a local fallback limiter, fail **closed** for anonymous traffic — a documented, tested choice.

### The architecture
```mermaid
flowchart TD
    C["Client with X-API-Key"] --> LB["Load balancer"]
    LB --> I1["Instance 1"]
    LB --> I2["Instance 2"]
    I1 --> F["RateLimitFilter - before controllers"]
    F --> KR["ApiKeyResolver - cached plan lookup"]
    F --> B4["Bucket4j proxy manager"]
    B4 --> RD[("Redis - one bucket per key and rule")]
    F -->|"allowed"| CT["Controllers"]
    F -->|"rejected"| E429["429 with Retry-After"]
    KR --> DB[("PostgreSQL - api_keys, plans")]
```
- **RateLimitFilter** — a `OncePerRequestFilter` registered after authentication; builds the bucket key (`plan-rule:apiKey` or `ip:addr`) and tries to consume a token.
- **ApiKeyResolver** — maps keys to plans, cached for 60 s so plan changes propagate within a minute.
- **Bucket4j proxy manager** — keeps bucket state in Redis and updates it atomically (compare-and-swap or Lua scripts depending on the integration).
- **Fallback limiter** — a local in-memory Bucket4j bucket per key, sized at limit ÷ instance count, used when Redis fails.

### Data model
```mermaid
erDiagram
    PLAN ||--o{ API_KEY : "assigned"
    PLAN {
        text code PK
        int requests_per_minute
        int search_per_minute
        int burst
    }
    API_KEY {
        text key_hash PK
        text owner
        text plan_code FK
        boolean active
        timestamptz created_at
    }
```
Redis keys: `rl:{rule}:{subject}` holding Bucket4j's serialized bucket state, with an expiry slightly longer than a full refill.

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Algorithm | Bucket4j token bucket | bursts allowed up to capacity, smooth refill |
| Shared state | Redis via Bucket4j's Lettuce integration | atomic updates across instances |
| Filter | Servlet filter in the Security filter chain order | runs before expensive work |
| Keys | API keys stored as SHA-256 hashes | a leaked database does not leak keys |
| Tests | Testcontainers Redis, a fixed time source | deterministic refill tests |

### Milestones
1. **Local limiter.** One in-memory bucket per key. *Done when* the 61st request in a minute gets 429 with `Retry-After`.
2. **Plans and rules.** *Done when* tests show Free and Pro limits, and the stricter `/search` rule applies in addition to the global one.
3. **Distributed.** Move buckets to Redis. *Done when* two application contexts in one test share the limit (60 total, not 120).
4. **Headers.** Remaining-quota and reset headers (follow GitHub's `X-RateLimit-*` style or the IETF RateLimit header draft). *Done when* clients can compute when to retry.
5. **Redis failure policy.** *Done when* stopping Redis makes paid keys use the local fallback and anonymous requests get 503, both covered by tests.

### Deep dive: the hard parts
- **Algorithms.** A **fixed window** counter ("60 per calendar minute") lets a client send 120 requests across a window boundary. A **sliding window** fixes that at more cost. A **token bucket** allows controlled bursts (capacity) and a steady average (refill rate) and is cheap to store — one timestamp and one count.
- **Atomicity across instances.** "Read tokens, decide, write tokens" from two instances at once loses updates. Bucket4j's distributed integrations make the update atomic in Redis; never implement it as separate `GET` and `SET` calls.
- **Choosing the key.** Per-IP limits break for users behind one corporate NAT and are easy to evade with many IPs; per-API-key limits need authentication first; per-user limits need login. Combine: IP limits on login and anonymous endpoints, key limits on the API.
- **Where to enforce.** A gateway limit (Spring Cloud Gateway `RequestRateLimiter`, a CDN or WAF) protects every service and drops abuse early; an in-app limit knows business rules (plan, endpoint cost). Production systems usually do both.

### Testing strategy
- Unit tests with Bucket4j's custom time meter to advance time without sleeping.
- Testcontainers Redis for the distributed behaviour and the failure policy.
- A small load test showing the aggregate rate across instances stays within the limit.

### Trade-offs, scaling and failure modes
- Every request now depends on Redis; a Redis replica or cluster, short timeouts and the fallback limiter keep that dependency from becoming an outage.
- Fail-open preserves availability but removes protection during the failure; fail-closed protects capacity but blocks paying customers. State the choice per audience.
- A hot key (one very busy client) concentrates load on one Redis shard; local pre-limiting reduces round trips.

### Stretch goals
- A concurrent-requests limiter (Stripe's second type) for expensive endpoints, and compare it with `@ConcurrencyLimit`.
- Cost-based limits: `/search` costs 5 tokens, `/get` 1.
- A dashboard of the top limited keys from metrics.

### Skills practised
- [Ch 11 — Tokens and OAuth2](../chapters/11-spring-security-2-tokens-and-oauth2/README.md): rate limiting and brute-force protection, filter placement.
- [Ch 9 — Spring Data beyond JPA, and caching](../chapters/09-spring-data-beyond-jpa-and-caching/README.md): Redis.
- [Ch 5 — Spring MVC and REST APIs](../chapters/05-spring-mvc-and-rest/README.md): filters vs interceptors, Problem Details.
- [Ch 4 — AOP and proxies](../chapters/04-aop-and-proxies/README.md): why `@ConcurrencyLimit` is not a rate limiter.

### In the interview
**30-second answer:** I built a public API with per-key rate limits by plan — Free 60 requests per minute, Pro 1,000 — enforced in a servlet filter with Bucket4j token buckets stored in Redis so the limit holds across 4 instances. The key design decision was the token bucket algorithm, which allows short bursts up to capacity while enforcing the average rate, and atomic bucket updates in Redis instead of read-then-write. The hardest part was the failure policy: if Redis is down, paid keys fall back to a local limiter (fail open) while anonymous traffic gets 503 (fail closed), both tested. The limit check adds under 2 ms p95.

**Key terms to know:**
- **Rate limit** — maximum requests per time unit for a subject; exceeding it returns 429 Too Many Requests.
- **Token bucket** — a bucket with capacity that refills at a steady rate; each request takes a token.
- **Fixed vs sliding window** — counting per calendar window (allows double bursts at edges) vs over a moving window (more precise, more cost).
- **Bucket4j proxy manager** — keeps bucket state in Redis with atomic updates.
- **Rate-limit key** — what is limited: API key, user or IP address.
- **`Retry-After`** — header telling the client when to try again.
- **Fail open vs fail closed** — allow traffic vs reject traffic when the limiter's store is unavailable.
- **Filter vs interceptor** — servlet filters run before Spring MVC and Security can be ordered around them; the limiter is a `OncePerRequestFilter`.
- **`@ConcurrencyLimit` vs rate limit** — caps simultaneous calls, not requests per second.

**Q:** Token bucket or fixed window?

**A:** Token bucket: it allows short bursts up to the capacity while enforcing the refill rate as the average, and it is cheap to store (a token count and a timestamp). A fixed window lets a client send 60 requests at 12:00:59 and 60 more at 12:01:00 — double the limit in two seconds. A sliding window fixes that but costs more storage or computation.

**Q:** How do you enforce one limit across many instances?

**A:** Shared state in Redis with atomic updates — Bucket4j's Redis integration uses compare-and-swap or Lua scripts so "check and consume" is one atomic operation. A separate `GET` then `SET` from the app would let two instances both see a free token and both consume it, exceeding the limit. A test with two application contexts proves 60 total, not 120.

**Q:** Redis is down. Do you let requests through?

**A:** It depends on the audience. Paid traffic falls back to a local in-memory bucket per instance sized at limit ÷ instance count (fail open), because blocking paying customers is worse than briefly weaker limits. Anonymous traffic is rejected with 503 (fail closed), because that is where abuse comes from. Both paths are covered by tests that stop the Redis container.

**Q:** Why not use `@ConcurrencyLimit` for this?

**A:** `@ConcurrencyLimit` caps how many calls to a method run at the same time; it says nothing about how many requests arrive per minute. A client making fast sequential calls never exceeds a concurrency limit but can still flood the API. They solve different problems and I could use both: rate limits per key and a concurrency cap on expensive endpoints.

**Q:** Which key do you limit on?

**A:** Per API key for the authenticated API, because that is the billing and plan unit. Per IP only for anonymous endpoints and login, knowing that many users behind one corporate NAT share an IP and attackers can rotate IPs. The filter runs after authentication so it knows the key and plan.

**Q:** Gateway or in-app limiting?

**A:** A gateway or CDN limit drops abuse early and protects every service; an in-app limit knows business rules such as plan and endpoint cost. Production systems usually do both: a coarse per-IP limit at the edge and precise per-key limits in the app.

**Q:** What breaks first at 10x traffic?

**A:** Redis round trips on every request, and hot keys — one very busy client concentrates load on one Redis shard. Local pre-limiting (a small local bucket that only consults Redis occasionally) cuts round trips, and a Redis cluster with replicas removes the single point of failure. The plan lookup is cached for 60 s, so it does not hit PostgreSQL per request.

**Q:** How do you test refill without sleeping?

**A:** Bucket4j accepts a custom time meter, so unit tests advance a fake clock by 30 seconds and assert that exactly 30 tokens came back. Distributed behaviour and the failure policy run against Testcontainers Redis, and a small load test across two instances shows the aggregate rate stays within the limit.

**Q:** Show the core of the filter.

**A:**
```java
var probe = buckets.builder().build(key, () -> configFor(plan)).tryConsumeAndReturnRemaining(1);
if (probe.isConsumed()) {
    res.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
    chain.doFilter(req, res);
} else {
    long secs = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1;
    res.setHeader("Retry-After", String.valueOf(secs));
    writeProblem(res, 429, "Rate limit exceeded");
}
```

**Common mistakes in answers:**
- Using one in-memory counter per instance and calling it "the limit" — with 4 instances the client gets 4x the limit.
- "Rate limiting is part of Spring Security" — it is not built in; you add a filter or a gateway limiter.
- Ignoring the store's failure mode — the limiter itself must have a tested fail-open or fail-closed policy.

### TL;DR
Token buckets per key and rule, stored atomically in Redis, enforced in a filter with clear 429 responses — and a deliberate, tested policy for when Redis fails.

## 23. Concert Seat Reservation
**Level:** Intermediate · **Time:** 2 weeks · **Prerequisites:** Ch 7, Ch 8

### What it is
A ticketing back end for numbered seats: users browse a venue map, **hold** specific seats for 10 minutes while they pay, and the hold either becomes a booking or expires and frees the seats. "Best available" picks the best free seats automatically. The core is **concurrency control**: thousands of people trying to grab the same front-row seats in the first minute of a sale.

### The problem it solves
Project 13 had a counter (stock); here each seat is a distinct row that exactly one person may get. Double-booking is unacceptable, but locking the whole venue is too slow. You will compare pessimistic locks, optimistic locks, database constraints and `SKIP LOCKED`, and design expiring holds that clean themselves up.

### Real-world examples
- The PostgreSQL [`SELECT` reference](https://www.postgresql.org/docs/current/sql-select.html) documents the locking clause (`FOR UPDATE`, `NOWAIT`, `SKIP LOCKED`) used for "best available" seat picking.
- The PostgreSQL chapter on [explicit locking](https://www.postgresql.org/docs/current/explicit-locking.html) explains row-level lock modes and deadlocks.

### Requirements
**Functional**
- Venue with sections, rows and seats; events at a venue; prices by section.
- `POST /api/events/{id}/holds {seatIds}` holds exactly those seats or none (all-or-nothing).
- `POST /api/events/{id}/holds/best {section, count}` holds the best `count` adjacent seats available.
- `POST /api/holds/{id}/confirm` turns a hold into a booking (after payment); holds expire after 10 minutes.
- Seat map endpoint returns each seat's status, cached for 1 second.

**Non-functional**
- No seat is ever held or booked by two users — proved with 500 concurrent requests for the same 10 seats.
- 200 hold requests per second for one event with p95 under 150 ms.
- Expired holds become available again within 30 seconds.

### The architecture
```mermaid
flowchart TD
    U["Users"] --> HC["HoldController"]
    HC --> HS["HoldService - @Transactional short"]
    HS --> SR["SeatRepository - locking queries"]
    SR --> DB[("PostgreSQL - event_seat rows")]
    HS --> BK["BookingService"]
    EXP["Expiry job - every 15 s"] --> DB
    MAP["SeatMapController - 1 s cache"] --> DB
```
- **HoldService** — one short transaction per hold: lock the requested seat rows, check they are free, mark them held with an expiry and a hold id.
- **SeatRepository** — queries with `@Lock(PESSIMISTIC_WRITE)` or native `FOR UPDATE SKIP LOCKED`.
- **BookingService** — confirms a still-valid hold into a booking.
- **Expiry job** — releases holds whose `expires_at` has passed (a status update, in batches).
- **SeatMapController** — read-only, slightly stale view for the UI.

### Data model
```mermaid
erDiagram
    EVENT ||--o{ EVENT_SEAT : "offers"
    HOLD ||--o{ EVENT_SEAT : "reserves"
    HOLD ||--o| BOOKING : "becomes"
    EVENT {
        bigint id PK
        text name
        timestamptz starts_at
    }
    EVENT_SEAT {
        bigint event_id FK
        text seat_code
        text section
        int row_no
        int seat_no
        int rank
        text status
        uuid hold_id FK
        bigint version
    }
    HOLD {
        uuid id PK
        bigint user_id
        timestamptz expires_at
        text status
    }
    BOOKING {
        uuid id PK
        uuid hold_id FK
        numeric total
        timestamptz confirmed_at
    }
```
`EVENT_SEAT` has a composite key (`event_id`, `seat_code`); `rank` orders seats by quality for "best available".

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Persistence | Spring Data JPA for holds and bookings, native SQL for seat picking | `SKIP LOCKED` is easiest in SQL |
| Locking | pessimistic row locks, optimistic `@Version` as a comparison | measure both |
| Scheduling | `@Scheduled` + ShedLock | one instance releases expired holds |
| Tests | Testcontainers PostgreSQL, virtual threads | real lock semantics |

### Milestones
1. **Specific seats, pessimistic.** Lock rows in a consistent order (`ORDER BY seat_code FOR UPDATE`). *Done when* 500 concurrent requests for the same 10 seats produce exactly one winner and no deadlocks.
2. **Optimistic variant.** `@Version` on seats; conflicts become 409. *Done when* the same test passes, and you have measured latency and conflict rate for both approaches.
3. **Best available with `SKIP LOCKED`.** *Done when* 100 concurrent "best 2 in section A" requests receive 100 distinct pairs without waiting on each other's locks.
4. **Holds expiry.** *Done when* a hold not confirmed within 10 minutes (fixed clock) is released by the job, and confirming an expired hold returns 410.
5. **Seat map.** *Done when* the map reflects holds within 1 second and adds no load proportional to viewers (cached).

### Deep dive: the hard parts
- **Deadlocks.** Two transactions locking seats A then B and B then A wait for each other forever; PostgreSQL detects it and aborts one. Always lock in a deterministic order (sort the seat codes), and retry the victim once.
- **`SKIP LOCKED` for "any N".** When the user does not care which seats, `SELECT ... WHERE status = 'FREE' ORDER BY rank LIMIT 2 FOR UPDATE SKIP LOCKED` hands each concurrent transaction different rows instead of making them queue. It is the same trick job queues use (project 38). Adjacency makes it harder: lock candidate rows, verify adjacency, and retry with the next candidates.
- **Expiry without a race.** A hold expiring at the same instant it is confirmed must have one outcome. Confirm with `UPDATE hold SET status='CONFIRMED' WHERE id=? AND status='HELD' AND expires_at > now()`, and release with the mirror condition; exactly one update wins.
- **Defence in depth.** Even with correct application logic, a partial unique index on bookings (one active booking per event seat) makes double-booking impossible at the database level.

### Testing strategy
- Concurrency tests with a start latch and virtual threads, asserting invariants (each seat held at most once) rather than exact winners.
- Clock-controlled tests for expiry boundaries.
- Measured comparison of locking strategies, recorded in the README.

### Trade-offs, scaling and failure modes
- Pessimistic locks are simple and fair under heavy contention but hold connections while waiting; optimistic locks are cheap with little contention but waste work when everyone wants the same seats.
- A hot on-sale moment can overwhelm the database; a virtual waiting room (admit N users per second) protects it — a product decision as much as a technical one.
- The 1-second-stale seat map means users sometimes click a seat that was just taken; the hold endpoint is the source of truth and must say so clearly.

### Stretch goals
- A waiting-room queue with tokens issued at a controlled rate.
- Push seat-map changes to browsers with SSE (project 19).
- Partition seat rows by event for very large venues.

### Skills practised
- [Ch 7 — Data access with JDBC and transactions](../chapters/07-data-access-jdbc-and-transactions/README.md): isolation, transaction pitfalls, short transactions.
- [Ch 8 — Spring Data JPA and Hibernate](../chapters/08-spring-data-jpa-and-hibernate/README.md): optimistic and pessimistic locking, native queries.
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): scheduled expiry with ShedLock.

### In the interview
**30-second answer:** I built a ticketing back end where users hold specific numbered seats for 10 minutes and then confirm or lose them. The key design decision was one short transaction per hold that locks the requested seat rows in a fixed order with `FOR UPDATE`, checks they are free and marks them held, with `FOR UPDATE SKIP LOCKED` for "best available" so concurrent buyers get different seats without queueing. The hardest problem was making expiry and confirmation race-free, which I solved with mirrored conditional updates so exactly one wins. 500 concurrent requests for the same 10 seats produce exactly one winner and no deadlocks, and a partial unique index makes double-booking impossible even if the code is wrong.

**Key terms to know:**
- **Pessimistic lock (`SELECT ... FOR UPDATE`, `@Lock(PESSIMISTIC_WRITE)`)** — lock rows while deciding; used for specific-seat holds.
- **Optimistic lock (`@Version`)** — detect conflicting writes at update time; the comparison variant returning 409.
- **Deadlock** — two transactions waiting on each other's locks; PostgreSQL aborts one.
- **Consistent lock ordering** — always lock rows in the same order (sorted seat codes) to prevent deadlocks.
- **`SKIP LOCKED`** — skip rows another transaction has locked instead of waiting; best-available picking.
- **Hold with expiry** — a temporary reservation with `expires_at`, released by a scheduled job.
- **Conditional state transition** — `UPDATE ... WHERE status='HELD' AND expires_at > now()`; exactly one of confirm/expire wins.
- **Partial unique index** — a unique constraint only over rows matching a condition (active bookings); defence in depth.
- **Contention** — many transactions wanting the same rows at once, as in the first minute of a sale.

**Q:** How do you stop two people booking the same seat?

**A:** Inside a short transaction I lock the requested seat rows, check they are all `FREE`, and mark them `HELD` with a hold id and expiry; a concurrent request waits on the lock and then sees them held. The hold is all-or-nothing, so if one seat is taken the whole transaction rolls back. A partial unique index on active bookings per event seat is the final safety net.

**Q:** What does `SKIP LOCKED` do?

**A:** It makes `SELECT ... FOR UPDATE` skip rows already locked by another transaction instead of waiting for them. For "give me the best 2 free seats", 100 concurrent requests each lock different rows immediately rather than queuing behind the first. It is only correct when the caller does not care which rows it gets — never for specific seats.

**Q:** How do you avoid deadlocks?

**A:** Lock rows in a consistent order: I sort the requested seat codes and use `ORDER BY seat_code FOR UPDATE`, so two transactions never hold locks the other needs in opposite order. Transactions stay short, and the rare deadlock victim (SQLSTATE 40P01) is retried once.

**Q:** Pessimistic or optimistic locking for this — why?

**A:** I measured both. Under heavy contention for the same seats, optimistic locking wastes work — most transactions read, compute and then fail with a version conflict — and returns many 409s. Pessimistic locks make them wait briefly and give clearer, fairer outcomes, at the cost of holding a connection while waiting. With low contention (a quiet event), optimistic is cheaper.

**Q:** A hold expires at the exact moment the user confirms. What happens?

**A:** Confirm runs `UPDATE hold SET status='CONFIRMED' WHERE id=? AND status='HELD' AND expires_at > now()` and the expiry job runs the mirror condition. Both are atomic on the same row, so exactly one changes it; the other sees zero rows updated. A confirm that loses returns 410 Gone.

**Q:** What happens if the app crashes while seats are held?

**A:** The hold rows are committed with an `expires_at`, so nothing is lost or stuck: the expiry job (every 15 s, with ShedLock so one instance runs it) releases them within 30 seconds of expiry. Payment for a hold that expired meanwhile must be refunded or re-held, which is why confirm checks expiry atomically.

**Q:** What breaks first when a big concert goes on sale — 100x load?

**A:** The database: thousands of transactions contending for the same few hundred front-row rows, holding connections while they wait. A virtual waiting room that admits a controlled number of users per second protects it, and the seat map is served from a 1-second cache so viewers add no load. Partitioning seat rows by event helps very large venues.

**Q:** How did you prove there is no double-booking?

**A:** A Testcontainers PostgreSQL test with a start latch releases 500 virtual threads requesting the same 10 seats. I assert invariants rather than exact winners: each seat is held by at most one hold, and exactly one request succeeded. A second test runs 100 best-available requests and asserts 100 distinct pairs.

**Q:** Write the best-available query.

**A:**
```sql
SELECT seat_code FROM event_seat
WHERE event_id = :event AND section = :section AND status = 'FREE'
ORDER BY rank
LIMIT :n
FOR UPDATE SKIP LOCKED;
```
Adjacency is checked after locking; if the rows are not adjacent, the transaction tries the next candidates.

**Common mistakes in answers:**
- "Check if the seat is free, then update" — without a lock or atomic condition, two transactions both see it free.
- Using `SKIP LOCKED` for specific seats — it would silently skip the seat the user asked for.
- Treating deadlocks as impossible — they happen without consistent ordering and must be retried.

### TL;DR
Seat-level concurrency: ordered pessimistic locks, optimistic versions, `SKIP LOCKED` for best-available, race-free hold expiry, and a database constraint as the last line of defence.

## 24. Webhook Delivery Service
**Level:** Intermediate · **Time:** 2–3 weeks · **Prerequisites:** Ch 4, Ch 6, Ch 14

### What it is
A service that lets your platform's customers register **webhook** endpoints — URLs on *their* servers — and reliably delivers signed HTTP POST notifications to them when events happen ("invoice.paid"). It retries for up to three days with exponential back-off, signs every payload so receivers can verify it came from you, disables endpoints that keep failing, and shows customers a delivery log.

### The problem it solves
Calling other people's servers is the opposite of calling your own: they are slow, down for maintenance, misconfigured, or malicious (pointing your server at internal addresses). Webhooks need at-least-once delivery with idempotency on the receiver side, isolation so one broken customer cannot delay others, and security on both ends. It is a compact but realistic distributed-systems problem.

### Real-world examples
- [Stripe's webhook documentation](https://docs.stripe.com/webhooks) describes signed payloads, retries with back-off over several days, and the receiver's duty to handle duplicates and return 2xx quickly.
- [Standard Webhooks](https://github.com/standard-webhooks/standard-webhooks) is an open specification and set of libraries for consistent webhook headers and signature verification.
- The [OWASP SSRF Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Server_Side_Request_Forgery_Prevention_Cheat_Sheet.html) explains why user-supplied URLs must not reach internal networks.

### Requirements
**Functional**
- Customers register endpoints (HTTPS URL, event types, a generated signing secret); can rotate the secret.
- Internal services publish events; the service creates one delivery per matching endpoint.
- Each delivery: POST JSON with headers for message id, timestamp and HMAC signature; success = any 2xx within 10 s.
- Retries at roughly 1 min, 5 min, 30 min, 2 h, 8 h, 24 h, 72 h; then `FAILED`. Endpoints failing continuously for 3 days are disabled and the customer is emailed.
- Delivery log with request, response status and latency; manual "resend" button.

**Non-functional**
- At-least-once delivery; the message id stays the same across retries so receivers can de-duplicate.
- One slow customer endpoint cannot reduce delivery throughput for others.
- 500 deliveries per second sustained.
- No delivery to private, loopback or link-local addresses, even through DNS tricks.

### The architecture
```mermaid
flowchart TD
    SVC["Internal services"] -->|"event"| IN["Event intake - Kafka or outbox"]
    IN --> FAN["Fan-out - match endpoints"]
    FAN --> DQ[("deliveries table - next_attempt_at")]
    DQ --> SCH["Dispatcher - polls due deliveries with SKIP LOCKED"]
    SCH --> POOL["Per-endpoint concurrency limit"]
    POOL --> SND["Sender - RestClient, 10 s timeout, HMAC"]
    SND --> GUARD["SSRF guard - resolve and check IP"]
    GUARD --> EXT["Customer endpoints"]
    SND --> DQ
    DQ --> UI["Delivery log API"]
```
- **Intake** — receives events reliably (Kafka consumer or an outbox poller).
- **Fan-out** — one `delivery` row per (event, endpoint), in one transaction.
- **Dispatcher** — selects due deliveries with `FOR UPDATE SKIP LOCKED` so many instances can work in parallel without double-sending.
- **Per-endpoint limit** — at most N concurrent requests per endpoint, so one slow customer only slows itself (Framework 7's `@ConcurrencyLimit` limits per bean method, not per endpoint, so use a keyed semaphore here).
- **Sender** — signs, sends, records the outcome, computes the next attempt time with jitter.
- **SSRF guard** — resolves the host and refuses non-public addresses at connect time.

### Data model
```mermaid
erDiagram
    ENDPOINT ||--o{ DELIVERY : "receives"
    EVENT ||--o{ DELIVERY : "fans out to"
    DELIVERY ||--o{ ATTEMPT : "tried as"
    ENDPOINT {
        uuid id PK
        text customer_id
        text url
        text secret_ciphertext
        text status
        timestamptz failing_since
    }
    EVENT {
        uuid id PK
        text type
        jsonb payload
        timestamptz created_at
    }
    DELIVERY {
        uuid id PK
        uuid endpoint_id FK
        uuid event_id FK
        text status
        int attempt_count
        timestamptz next_attempt_at
    }
    ATTEMPT {
        uuid delivery_id FK
        int attempt_no
        int http_status
        int latency_ms
        text error
    }
```

### Tech stack
| Component | Choice | Why |
|---|---|---|
| HTTP | `RestClient` on the JDK `HttpClient`, strict timeouts | virtual-thread friendly, no redirects followed |
| Queue | PostgreSQL table + `SKIP LOCKED` | durable, inspectable, scheduled retries are just a timestamp |
| Signing | HMAC-SHA256 over `id.timestamp.body` | receivers verify integrity and freshness |
| Secrets | encrypted at rest (envelope encryption with a key from a vault) | a database leak does not leak secrets |
| Concurrency | virtual threads + keyed semaphores | many slow endpoints, bounded per endpoint |

### Milestones
1. **Register and sign.** *Done when* a receiver test using the same algorithm verifies the signature and rejects a body changed by one byte or a timestamp older than 5 minutes.
2. **Durable fan-out and dispatch.** *Done when* an event with 3 matching endpoints creates 3 deliveries and all succeed against stub servers.
3. **Retries with back-off and jitter.** *Done when* a stub that fails 3 times then succeeds gets 4 attempts at the expected (clock-controlled) times, with the same message id.
4. **Isolation.** *Done when* one endpoint that sleeps 10 s does not reduce delivery rate to 20 healthy endpoints in a load test.
5. **SSRF guard and disabling.** *Done when* URLs resolving to `127.0.0.1`, `10.x`, `169.254.169.254` or IPv6 loopback are refused, and an endpoint failing for 3 (simulated) days is disabled with a notification.

### Deep dive: the hard parts
- **At-least-once is the only honest promise.** If the customer's server processes the request but the response is lost, you will retry. The stable message id lets receivers de-duplicate; say so in your docs.
- **Back-off with jitter.** Retrying all failed deliveries at the same instant after an outage creates a thundering herd against recovering customers. Exponential delays plus random jitter spread them out. Framework 7's `@Retryable` has `multiplier` and `jitter`, but in-process retries vanish on restart, so here retries are rows with `next_attempt_at`.
- **SSRF through DNS.** Checking the URL's host string is not enough: `evil.example` can resolve to `10.0.0.5`, or resolve differently at check time and connect time (DNS rebinding). Resolve once, check the IP, and connect to that exact IP (or use an HTTP client hook that filters addresses at connect time); disable redirects.
- **Fairness.** A global thread pool lets one slow endpoint occupy every worker. Bound concurrency per endpoint and give each dispatcher pass a quota per endpoint.

### Testing strategy
- Stub receivers on random ports with programmable behaviour (fail N times, sleep, return 410).
- Clock-controlled tests for the retry schedule.
- Security tests for the SSRF guard, including a hostname that resolves to a private IP via a test DNS resolver.

### Trade-offs, scaling and failure modes
- A database-backed queue is simple and auditable to a few thousand deliveries per second; beyond that, partitioned Kafka topics per endpoint shard scale further at the cost of complex delayed retries.
- Ordering is not guaranteed across retries; document it, and include an event timestamp so receivers can ignore stale updates.
- Payload retention costs storage; keep bodies for a fixed period, then only metadata.

### Stretch goals
- Thin payloads ("invoice 123 changed, fetch it") to reduce sensitive data in transit.
- Secret rotation with two valid signatures for an overlap period.
- A customer-facing CLI that forwards webhooks to `localhost` for testing.

### Skills practised
- [Ch 6 — REST clients and HTTP interfaces](../chapters/06-rest-clients-and-http-interfaces/README.md): `RestClient`, timeouts, error handling, interceptors.
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): durable work, scheduling, idempotence, the outbox.
- [Ch 4 — AOP and proxies](../chapters/04-aop-and-proxies/README.md): `@Retryable` and `@ConcurrencyLimit` — and why durable retries need storage.
- [Ch 11 — Tokens and OAuth2](../chapters/11-spring-security-2-tokens-and-oauth2/README.md): storing keys and secrets.

### In the interview
**30-second answer:** I built a webhook delivery service that sends signed POSTs to customers' URLs, retries with jittered exponential back-off for up to 72 hours, and disables endpoints that keep failing. The key design decision was a durable PostgreSQL delivery table with `next_attempt_at`, polled with `FOR UPDATE SKIP LOCKED`, so retries survive restarts and many dispatcher instances can work in parallel. The hardest parts were isolation — a keyed semaphore per endpoint so one slow customer cannot starve others — and SSRF, which I block by resolving the host, checking the IP and connecting to that exact address. Every message keeps the same id across retries so receivers can de-duplicate.

**Key terms to know:**
- **Webhook** — an HTTP callback your platform POSTs to a customer's URL when an event happens.
- **At-least-once delivery** — a message may arrive more than once; a stable message id lets receivers de-duplicate.
- **HMAC signature** — a keyed hash over id, timestamp and body with a per-endpoint secret; proves origin and integrity.
- **Replay protection** — rejecting signatures with old timestamps.
- **Exponential back-off with jitter** — growing, randomised delays between retries to avoid thundering herds.
- **Durable retry schedule** — retries stored as rows with `next_attempt_at`, not in-memory.
- **`SKIP LOCKED` dispatcher** — many workers claim different due rows without blocking each other.
- **Per-endpoint concurrency limit (keyed semaphore)** — bounds in-flight requests per customer.
- **SSRF / DNS rebinding** — tricking the server into calling internal addresses, including by changing DNS answers between check and connect.

**Q:** How do receivers know a webhook is genuine?

**A:** Each request carries a message id, a timestamp and an HMAC-SHA256 signature over `id.timestamp.body` with the endpoint's secret. The receiver recomputes it, compares in constant time, and rejects timestamps older than a few minutes to stop replays. Secret rotation keeps two valid secrets for an overlap period.

**Q:** Why not just use `@Retryable` for retries?

**A:** `@Retryable` retries inside the running process: the schedule is lost on restart and a thread or task is tied up while waiting. Webhook retries span three days, so each retry is a row with `next_attempt_at` in durable storage, and the dispatcher picks it up whenever it is due. In-process retries are fine for sub-second transient errors, not this.

**Q:** What is SSRF and how do you prevent it here?

**A:** Server-side request forgery: a customer registers a URL that points my server at internal addresses, such as `169.254.169.254` cloud metadata or `10.x` services. Checking the hostname string is not enough because DNS can resolve to a private IP or change between check and connect. I resolve once, reject private, loopback and link-local ranges (IPv4 and IPv6), connect to that exact IP, and do not follow redirects.

**Q:** How does one slow customer not slow everyone else?

**A:** A global pool would let one endpoint that always times out at 10 s occupy every worker. I cap concurrent requests per endpoint with a keyed semaphore and give each dispatcher pass a per-endpoint quota, so a slow endpoint only delays its own deliveries. Framework 7's `@ConcurrencyLimit` limits per method, not per key, so it does not fit here.

**Q:** The customer processed the webhook but the response was lost. What happens?

**A:** From my side the attempt failed, so I retry with the same message id. The receiver sees a duplicate and must ignore it by id — this is documented as part of the contract. At-least-once with receiver-side de-duplication is the only honest promise over an unreliable network.

**Q:** Why add jitter to back-off?

**A:** After an outage at a large customer, thousands of deliveries failed at the same moment; with pure exponential delays they would all retry at the same instant again, hammering the customer as it recovers. Random jitter spreads the retries out over time.

**Q:** What changes at 10x — 5,000 deliveries per second?

**A:** The single delivery table and its index on `next_attempt_at` become the bottleneck, along with storing every request and response body. I would partition the table, keep bodies for a fixed period only, and eventually move to Kafka topics sharded by endpoint, accepting that delayed retries become harder to implement. Senders are virtual threads, so thread count is not the limit; sockets and per-endpoint fairness are.

**Q:** How do you test the SSRF guard and the retry schedule?

**A:** For SSRF I use a test DNS resolver that maps a public-looking hostname to `127.0.0.1` and assert the request is refused, plus direct cases for `10.0.0.1`, `169.254.169.254` and `::1`. For retries, a stub receiver fails three times then succeeds, and a fixed `Clock` lets me assert four attempts at the expected times with the same message id.

**Q:** Show the signature computation.

**A:**
```java
Mac mac = Mac.getInstance("HmacSHA256");
mac.init(new SecretKeySpec(secret, "HmacSHA256"));
byte[] sig = mac.doFinal((id + "." + ts + "." + body).getBytes(UTF_8));
String header = "v1," + Base64.getEncoder().encodeToString(sig);
// receiver: MessageDigest.isEqual(expected, received) for constant-time comparison
```

**Common mistakes in answers:**
- "Validate the hostname is not localhost" — DNS can still resolve to internal addresses; check the resolved IP at connect time.
- Promising exactly-once delivery — over HTTP to a third party only at-least-once is achievable.
- Comparing signatures with `equals` — use a constant-time comparison to avoid timing attacks.

### TL;DR
Durable, signed, at-least-once webhooks with jittered back-off over days, per-endpoint isolation, `SKIP LOCKED` dispatch and an SSRF guard.

## 25. Feature-flag Service
**Level:** Intermediate · **Time:** 2 weeks · **Prerequisites:** Ch 3, Ch 9, Ch 13

### What it is
A small feature-flag platform: an admin API to define flags and targeting rules ("new checkout for 10% of users in Portugal, and always for staff"), a server that streams flag changes, and a **client library** (a Spring Boot starter) that applications use to evaluate flags locally in microseconds: `flags.isEnabled("new-checkout", context)`. A **feature flag** (or toggle) is a runtime switch that changes behaviour without a deploy.

### The problem it solves
Configuration properties (project 5) are read at startup; changing them needs a restart. Teams want to ship code dark, roll out gradually, target segments and kill a broken feature in seconds. Building the service teaches deterministic percentage rollouts, local evaluation with streamed updates, safe defaults when the flag server is down, and the discipline of removing old flags.

### Real-world examples
- Martin Fowler's site hosts Pete Hodgson's article ["Feature Toggles (aka Feature Flags)"](https://martinfowler.com/articles/feature-toggles.html), which classifies release, experiment, ops and permission toggles and their lifetimes.
- [OpenFeature](https://openfeature.dev/) is a vendor-neutral standard API for flag evaluation, with a Java SDK; your client could implement its provider interface.
- [Unleash](https://github.com/Unleash/unleash) is an open-source flag server whose SDKs evaluate locally after fetching rules — the architecture used here.

### Requirements
**Functional**
- Admin API: create flags (boolean or string variants), rules (attribute matches, percentage rollout, allow-lists), environments (dev, prod).
- Evaluation context: user id, country, plan, app version.
- Client starter: local evaluation, `@ConfigurationProperties` for server URL and app key, fallback defaults per flag.
- Updates reach clients within 2 seconds via SSE, with polling as a fallback.
- Audit log of every flag change (who, when, before, after).

**Non-functional**
- Evaluation under 50 microseconds on the client (no network call per evaluation).
- The same user always gets the same variant for a given flag and rollout percentage (deterministic bucketing).
- If the flag server is unreachable at startup, clients start with last-known or default values; they never block application startup longer than 2 s.

### The architecture
```mermaid
flowchart TD
    ADM["Admin UI or API"] --> FS["Flag server - MVC admin API"]
    FS --> DB[("PostgreSQL - flags, rules, audit")]
    FS --> ST["Stream endpoint - SSE via WebFlux"]
    ST --> SDK1["App A - flags starter"]
    ST --> SDK2["App B - flags starter"]
    SDK1 --> EV["Local evaluator - rules in memory"]
    SDK1 --> SNAP["Snapshot file - last known rules"]
    SDK1 -.->|"fallback poll with ETag"| FS
```
- **Flag server** — CRUD with validation and audit; each change bumps a global revision number.
- **Stream endpoint** — SSE stream of revisions and changed flags; clients reconnect with `Last-Event-ID`.
- **Client starter** — auto-configures a `FeatureFlags` bean, opens the stream in the background, keeps rules in an immutable map swapped atomically on update.
- **Local evaluator** — evaluates rules in order; percentage rollouts hash `flagKey + userId` into 0–99.
- **Snapshot** — rules written to a local file on every update, read at startup if the server is down.

### Data model
```mermaid
erDiagram
    FLAG ||--o{ RULE : "has"
    FLAG ||--o{ FLAG_AUDIT : "changed by"
    FLAG {
        text flag_key PK
        text environment PK
        text type
        text default_variant
        boolean archived
        bigint revision
    }
    RULE {
        bigint id PK
        text flag_key FK
        int priority
        jsonb conditions
        int rollout_percent
        text variant
    }
    FLAG_AUDIT {
        bigint id PK
        text flag_key FK
        text actor
        jsonb before_state
        jsonb after_state
        timestamptz at
    }
```

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Admin API | Spring MVC, Spring Data JPA, `jsonb` conditions | simple CRUD with flexible rules |
| Streaming | WebFlux SSE endpoint (or MVC `SseEmitter`) | push updates to many clients |
| Client | custom starter with auto-configuration; `RestClient` for polling | drop-in for any Boot app |
| Hashing | MurmurHash3 or similar over `flagKey:userId` | uniform, deterministic buckets |
| Security | API keys per application, admin behind OAuth2 | who may read vs change flags |

### Milestones
1. **Evaluator library.** Pure Java, no Spring. *Done when* property-style tests show deterministic buckets and that a 10% rollout over 100,000 random ids lands between 9.5% and 10.5%.
2. **Admin API and audit.** *Done when* every change produces an audit row with before and after states.
3. **Streaming updates.** *Done when* a client in a test sees a flag flip within 2 seconds of the admin change.
4. **Resilience.** *Done when* a client starts within 2 s with the server down, uses the snapshot, and catches up when the server returns.
5. **Starter.** *Done when* a sample app adds the dependency and two properties, injects `FeatureFlags`, and `ApplicationContextRunner` tests cover the auto-configuration conditions.

### Deep dive: the hard parts
- **Deterministic rollouts.** Using `Random` gives users different experiences on each request. Hashing `flagKey + userId` gives a stable bucket per user per flag; including the flag key stops the same 10% of users from being the guinea pigs for every flag. Increasing 10% → 20% keeps the original 10% enabled.
- **Consistency of updates.** Clients apply updates as a whole revision, replacing an immutable map atomically, so a request never evaluates half-old, half-new rules. Revisions also detect missed events: a gap triggers a full refetch.
- **Failure modes are the product.** The safest default for a release flag is "off"; for an ops kill switch it may be "on". Make the default explicit per flag in code and test startup with no server and no snapshot.
- **Flag debt.** Each flag adds a second branch to test and reason about. Add an expiry date per release flag, report stale flags, and make removing a flag part of the definition of done.

### Testing strategy
- Pure unit and property-style tests for the evaluator (no Spring).
- `ApplicationContextRunner` for the starter's conditions and properties.
- An integration test with the server and a client in one JVM, toggling flags and asserting the client reacts.

### Trade-offs, scaling and failure modes
- Local evaluation scales to any request rate but means rules (and their attribute values) are shipped to every client; do not put secrets in rules.
- SSE gives fast propagation; polling with `ETag` is simpler and plays nicer with some proxies — support both.
- A hosted product (LaunchDarkly, Unleash, a cloud provider) is usually the right choice in a company; building one is for learning and for understanding what you buy.

### Stretch goals
- Implement an OpenFeature provider so apps use the standard API.
- Experiment metrics: record exposures and compare conversion per variant.
- Approval workflow: production changes need a second person.

### Skills practised
- [Ch 3 — Configuration and auto-configuration](../chapters/03-configuration-and-auto-configuration/README.md): building a starter, conditions, properties.
- [Ch 13 — Reactive programming and Spring WebFlux](../chapters/13-reactive-and-webflux/README.md): SSE streaming.
- [Ch 9 — Spring Data beyond JPA, and caching](../chapters/09-spring-data-beyond-jpa-and-caching/README.md): in-memory caches and invalidation.
- [Ch 6 — REST clients and HTTP interfaces](../chapters/06-rest-clients-and-http-interfaces/README.md): polling client with conditional requests.

### In the interview
**30-second answer:** I built a feature-flag platform: an admin API with audited rules, a server that streams changes over SSE, and a Spring Boot starter that evaluates flags locally in microseconds. The key design decision was local evaluation with deterministic bucketing — hashing `flagKey:userId` into 0–99 — so the same user always gets the same variant and no request makes a network call. The hardest part was failure behaviour: clients load a last-known snapshot file at startup, never block startup longer than 2 seconds, and fall back to explicit per-flag defaults. Updates reach clients within 2 seconds and a 10% rollout over 100,000 ids lands between 9.5% and 10.5%.

**Key terms to know:**
- **Feature flag (toggle)** — a runtime switch that changes behaviour without a deploy; release, experiment, ops and permission types.
- **Targeting rule** — conditions on context attributes (country, plan) that choose a variant.
- **Percentage rollout / deterministic bucketing** — a stable hash of flag key and user id decides whether a user is in the rollout.
- **Local evaluation** — rules cached in the client, evaluated in-process with no network call.
- **SSE streaming with revisions** — the server pushes changes; revision numbers detect missed events.
- **Atomic snapshot swap** — replacing an immutable rules map in one reference update so evaluation never sees half an update.
- **Safe default** — the value used when nothing else is known; usually "off" for release flags.
- **Starter / auto-configuration** — the client library that creates the `FeatureFlags` bean from properties.
- **Flag debt** — old flags that are never removed and multiply code paths.

**Q:** How do you roll out to 10% of users consistently?

**A:** Hash `flagKey + ":" + userId` (MurmurHash3) into 0–99 and enable users whose bucket is below 10. The same user always lands in the same bucket, so they do not flip between variants. Raising to 20% keeps the original 10% enabled, and including the flag key means different flags pick different 10% groups.

**Q:** Why evaluate flags locally?

**A:** A network call per evaluation adds latency to every request and makes the application depend on the flag server being up. Local evaluation takes microseconds and keeps working through server outages, with updates pushed in the background. The cost is that rules — and any attribute values in them — are shipped to every client, so no secrets belong in rules.

**Q:** Feature flags vs configuration properties?

**A:** Properties are read at startup, are per environment and need a restart to change; conditional beans are evaluated once. Flags change at runtime within seconds and can target individual users or segments. Many teams use both: properties for infrastructure settings, flags for behaviour they want to switch live.

**Q:** The flag server is down when an app starts. What happens?

**A:** The starter tries the server for at most 2 seconds, then loads the snapshot file written on the last update, and if there is none it uses the defaults declared in code per flag. It keeps reconnecting in the background and catches up when the server returns. A test starts a client with no server and no snapshot and asserts the defaults.

**Q:** How do you avoid evaluating half-old, half-new rules?

**A:** Each update is a complete revision. The client builds a new immutable map and swaps it in with one atomic reference assignment, so any evaluation sees either the old set or the new set. If a revision number is skipped (a missed SSE event), the client refetches everything.

**Q:** Why use `Random` for rollouts — or why not?

**A:** Not: `Random` gives a user a different answer on each request, so a page could render with the new checkout and submit to the old one. Deterministic hashing gives a stable answer per user per flag, which is also required for experiments to measure anything.

**Q:** What changes at 10x — 1,000 applications connected?

**A:** Evaluation load is zero on the server because it happens in clients; the server's cost is 1,000 long-lived SSE connections and fan-out of changes, which WebFlux handles well. Polling fallback uses `ETag` so unchanged rule sets cost a 304. The audit table and admin API stay small.

**Q:** How do you test the evaluator and the starter?

**A:** The evaluator is pure Java with no Spring: property-style tests generate 100,000 random ids and check the rollout percentage, and unit tests check rule order and determinism. The starter's conditions and properties are tested with `ApplicationContextRunner`, and one integration test runs server and client in one JVM, flips a flag and asserts the client sees it within 2 seconds.

**Q:** Write the bucketing function.

**A:**
```java
static int bucket(String flagKey, String userId) {
    byte[] key = (flagKey + ":" + userId).getBytes(StandardCharsets.UTF_8);
    int h = Hashing.murmur3_32_fixed().hashBytes(key).asInt();
    return Math.floorMod(h, 100);   // 0..99, never negative
}
boolean inRollout = bucket(flag, user) < percent;
```

**Common mistakes in answers:**
- Using `hashCode() % 100` — it can be negative and `String.hashCode` is poorly distributed for this use; use a good hash and `floorMod`.
- Calling the flag server on every request — it adds latency and a hard dependency.
- Never removing flags — each one doubles a code path; release flags need an expiry.

### TL;DR
A flag server with audited rules, SSE-pushed updates and a starter that evaluates locally with deterministic hashing, safe defaults and a snapshot for when the server is down.

## 26. Multi-tenant SaaS Project Tracker
**Level:** Advanced · **Time:** 3–4 weeks · **Prerequisites:** Ch 7, Ch 8, Ch 11

### What it is
A project-and-issue tracker sold to many companies (**tenants**) from one deployment. Every request belongs to exactly one tenant, and no tenant may ever see another's data — not through a missed `WHERE` clause, a shared cache key, a background job or a log line. The design uses a **shared database, shared schema** model with a `tenant_id` column on every table, enforced twice: in the application (Hibernate's tenant discriminator) and in the database (PostgreSQL **row-level security**, RLS — policies that filter rows per session automatically).

### The problem it solves
Multi-tenancy is how SaaS keeps costs low, and cross-tenant data leaks are among the worst incidents a SaaS company can have. Isolation must be designed into every layer: authentication (which tenant is this?), data access, caching, async work, search, rate limits and operations (per-tenant export, deletion, noisy neighbours). This project makes "tenant" a first-class concept everywhere.

### Real-world examples
- AWS's whitepaper ["SaaS Tenant Isolation Strategies"](https://docs.aws.amazon.com/whitepapers/latest/saas-tenant-isolation-strategies/saas-tenant-isolation-strategies.html) compares silo, pool and bridge models and the controls that enforce isolation in each.
- The PostgreSQL manual on [row security policies](https://www.postgresql.org/docs/current/ddl-rowsecurity.html) explains RLS, which this project uses as a database-level safety net.

### Requirements
**Functional**
- Sign-up creates a tenant, its admin user and default workflow; tenant admins invite users and assign roles.
- Projects, issues, comments, labels, attachments — all tenant-scoped.
- Tenant resolution from the access token (`tenant` claim) for the API, and from the subdomain (`acme.tracker.test`) for the web UI; the two must agree.
- Per-tenant settings (workflow states, custom fields as `jsonb`), plan limits (users, projects, storage).
- Tenant data export (ZIP of JSON) and full deletion on request.

**Non-functional**
- **Zero cross-tenant reads**, enforced by tests that try every endpoint with another tenant's ids.
- One large tenant cannot degrade others: per-tenant rate limits and query timeouts.
- 1,000 tenants, the largest with 2 million issues; issue list p95 under 150 ms.
- Option to move one premium tenant to a dedicated database without code changes.

### The architecture
```mermaid
flowchart TD
    REQ["Request - JWT with tenant claim"] --> SEC["Security filter chain - JwtIssuerAuthenticationManagerResolver"]
    SEC --> TF["TenantFilter - resolve and verify tenant"]
    TF --> TC["TenantContext - ScopedValue"]
    TC --> SVC["Services"]
    SVC --> HIB["Hibernate - @TenantId discriminator"]
    HIB --> TXH["Tx start hook - SET LOCAL app.tenant_id"]
    TXH --> PG[("PostgreSQL - RLS policies on every table")]
    TC --> CACHE["Cache keys prefixed by tenant"]
    TC --> ASYNC["TaskDecorator and message headers carry tenant"]
    RT["DataSource router - premium tenants"] -.-> PG2[("Dedicated database")]
```
- **Security** — for tenants that bring their own IdP, `JwtIssuerAuthenticationManagerResolver` picks keys per trusted issuer; otherwise one IdP issues tokens with a `tenant` claim.
- **TenantFilter** — extracts the tenant from the token, checks it matches the subdomain and that the user is a member, then binds it for the request.
- **TenantContext** — a Java 25 `ScopedValue` (final since JDK 25) or a `ThreadLocal` cleared in `finally`; one place to read the current tenant.
- **Hibernate `@TenantId`** — a field on each entity that Hibernate fills on insert and adds to every query, using a `CurrentTenantIdentifierResolver` bean.
- **RLS** — policies such as `USING (tenant_id = current_setting('app.tenant_id')::uuid)`; the app sets the variable with `SET LOCAL` at transaction start, so even a hand-written native query is filtered.
- **DataSource router** — an `AbstractRoutingDataSource` sending premium tenants to their own database.

### Data model
```mermaid
erDiagram
    TENANT ||--o{ MEMBERSHIP : "has"
    APP_USER ||--o{ MEMBERSHIP : "joins"
    TENANT ||--o{ PROJECT : "owns"
    PROJECT ||--o{ ISSUE : "contains"
    TENANT {
        uuid id PK
        text slug UK
        text plan
        text issuer
        text db_route
    }
    APP_USER {
        uuid id PK
        text subject UK
        text email
    }
    MEMBERSHIP {
        uuid tenant_id FK
        uuid user_id FK
        text role
    }
    PROJECT {
        uuid id PK
        uuid tenant_id FK
        text key
        text name
    }
    ISSUE {
        uuid id PK
        uuid tenant_id FK
        uuid project_id FK
        int number
        text title
        text status
        jsonb custom_fields
    }
```
Every unique constraint and index starts with `tenant_id` (e.g. unique `(tenant_id, project_id, number)`), so tenants' data is clustered and per-tenant queries use indexes.

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Security | resource server with multi-issuer support | tenants may bring their own IdP |
| ORM | Hibernate 7 `@TenantId` discriminator | automatic tenant filter on entities |
| Database | PostgreSQL RLS, a non-owner application role | defence in depth for native SQL |
| Context | `ScopedValue` (Java 25) or `ThreadLocal` with cleanup | request-scoped tenant id |
| Async | `TaskDecorator`, Kafka headers, `@Scheduled` loops per tenant | tenant never "falls off" a thread |

### Milestones
1. **Tenant resolution.** *Done when* tests show a token for tenant A on `b.tracker.test` returns 403, and an unknown issuer returns 401.
2. **Discriminator.** `@TenantId` on all entities. *Done when* a repository test with tenant A set cannot find tenant B's issue by id.
3. **RLS safety net.** Policies on every table, app connects as a role without `BYPASSRLS`. *Done when* a deliberately unfiltered native query returns only the current tenant's rows, and a query with no tenant set returns nothing.
4. **Isolation test suite.** For every endpoint, create data as tenant A, call as tenant B with A's ids. *Done when* all return 404 and the test fails if a new endpoint is added without being covered (generate the list from the handler mappings).
5. **Caches, async, jobs.** *Done when* cache keys include the tenant, `@Async` and Kafka consumers restore it, and a nightly job iterates tenants explicitly.
6. **Noisy neighbour and routing.** Per-tenant rate limits and statement timeouts; one tenant moved to a dedicated database. *Done when* a load test on tenant A leaves tenant B's p95 within 20% of baseline.

### Deep dive: the hard parts
- **Three isolation models.** *Database per tenant* (silo): strongest isolation and easy per-tenant restore, but 1,000 databases to migrate and connect to. *Schema per tenant*: medium isolation, migrations multiply. *Shared schema* (pool): cheapest and simplest to operate, but isolation depends on every query. Many SaaS products pool small tenants and silo large or regulated ones — which is why the routing hook exists.
- **Connection pools and RLS.** `SET app.tenant_id` on a pooled connection leaks into the next borrower. Use `SET LOCAL` inside the transaction (reset at commit) or reset on connection return; test it by alternating tenants on a pool of size 1.
- **Context propagation.** The tenant lives in request-scoped state; new threads (`@Async`, `CompletableFuture`, virtual threads you create) do not inherit a `ThreadLocal`. `ScopedValue` bindings are inherited by structured child tasks, but not by arbitrary executors; a `TaskDecorator` captures and restores it. Messages carry it in a header, verified on receipt.
- **Unique ids and enumeration.** UUIDs prevent guessing other tenants' ids, but isolation must not rely on secrecy — the tests in milestone 4 assume the attacker knows the ids.

### Testing strategy
- The cross-tenant isolation suite (milestone 4) as a permanent CI gate.
- Repository tests against real PostgreSQL with RLS enabled (Testcontainers).
- Concurrency test alternating tenants on a single pooled connection to catch session-variable leaks.

### Trade-offs, scaling and failure modes
- Shared schema scales to many small tenants cheaply; the largest tenants dominate index sizes — partition big tables by `tenant_id` hash or move whales to their own database.
- RLS adds a small planning cost and confuses people who forget it exists; document it and make the app's role non-owner so policies always apply.
- Per-tenant schema migrations (silo) need orchestration: migrate one canary tenant first.

### Stretch goals
- Per-tenant encryption keys for attachments (envelope encryption).
- Tenant-aware observability: `tenant` as a low-cardinality tag only for top tenants, others grouped.
- Self-service SSO: tenant admins register their own OIDC issuer.

### Skills practised
- [Ch 11 — Tokens and OAuth2](../chapters/11-spring-security-2-tokens-and-oauth2/README.md): multi-tenancy with several issuers, claims to authorities.
- [Ch 8 — Spring Data JPA and Hibernate](../chapters/08-spring-data-jpa-and-hibernate/README.md): entities, indexes, Flyway.
- [Ch 7 — Data access with JDBC and transactions](../chapters/07-data-access-jdbc-and-transactions/README.md): `DataSource`, transaction hooks, routing.
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): `TaskDecorator`, propagating context.

### In the interview
**30-second answer:** I built a multi-tenant project tracker where 1,000 companies share one deployment and one schema, with a `tenant_id` on every table. The key design decision was defence in depth: the tenant comes from the access token and must match the subdomain, Hibernate's `@TenantId` filters every entity query, and PostgreSQL row-level security filters even hand-written native SQL using a `SET LOCAL` session variable. The hardest part was keeping the tenant attached across threads, pooled connections, caches and messages. An automated suite calls every endpoint as tenant B with tenant A's real ids and expects 404, and premium tenants can be routed to a dedicated database without code changes.

**Key terms to know:**
- **Tenant / multi-tenancy** — one deployment serving many customers whose data must stay isolated.
- **Silo / bridge / pool models** — database per tenant, schema per tenant, shared schema with `tenant_id`.
- **Hibernate `@TenantId`** — a discriminator field Hibernate sets on insert and adds to every query, from a `CurrentTenantIdentifierResolver`.
- **Row-level security (RLS)** — PostgreSQL policies that filter rows per session; the database safety net.
- **`SET LOCAL`** — a session variable that resets at transaction end, so pooled connections do not leak a tenant.
- **`ScopedValue` vs `ThreadLocal`** — Java 25's immutable, scoped context vs mutable per-thread state that must be cleared.
- **Context propagation (`TaskDecorator`)** — copying the tenant onto `@Async` threads and message headers.
- **Multi-issuer resource server** — `JwtIssuerAuthenticationManagerResolver` trusts several IdPs.
- **`AbstractRoutingDataSource`** — chooses a database per tenant at runtime.
- **Noisy neighbour** — one big tenant degrading others; per-tenant rate limits and statement timeouts.

**Q:** Database per tenant or shared schema?

**A:** Shared schema for most tenants, because one database is cheap and simple to migrate and operate, with isolation enforced by Hibernate's tenant filter and PostgreSQL RLS. Large or regulated tenants go to a dedicated database through a routing data source, which gives them stronger isolation and easy per-tenant restore. Database per tenant for all 1,000 would mean 1,000 migrations and connection pools.

**Q:** How do you prove tenants are isolated?

**A:** An automated suite creates data as tenant A and calls every endpoint as tenant B with A's real ids, expecting 404. The endpoint list is generated from the handler mappings, so a new endpoint without coverage fails the build. RLS backs it up: even an unfiltered native query returns only the current tenant's rows.

**Q:** What breaks when you add `@Async`?

**A:** The tenant lives in request-scoped state on the request thread; a new thread does not inherit a `ThreadLocal`, so the async task would run with no tenant (RLS then returns nothing) or, worse, a stale one. I capture it with a `TaskDecorator` and restore it on the worker thread, and messages carry it in a header that consumers verify.

**Q:** Why `SET LOCAL` and not `SET` for the RLS variable?

**A:** Connections are pooled. A plain `SET app.tenant_id` stays on the connection after it returns to the pool, so the next request — maybe another tenant's — inherits it. `SET LOCAL` lasts only until the transaction ends. I test it by alternating tenants on a pool of size 1.

**Q:** Why does RLS not apply in some of your queries?

**A:** By default table owners bypass RLS, as do roles with `BYPASSRLS` or superuser. The application connects as a non-owner role without `BYPASSRLS`, and migrations run as a separate owner role. Otherwise the policies exist but silently do nothing.

**Q:** A token for tenant A arrives on `b.tracker.test`. What happens?

**A:** The `TenantFilter` compares the token's `tenant` claim with the subdomain and checks membership; a mismatch returns 403 before any service code runs. A token from an unknown issuer fails authentication with 401 in the multi-issuer resolver. The tenant is never taken from a request parameter the client controls.

**Q:** What changes as the largest tenant grows to 20 million issues?

**A:** It dominates shared index sizes and can slow everyone, so I would move it to its own database through the routing hook or partition big tables by `tenant_id`. Every index already starts with `tenant_id`, so per-tenant queries stay index-driven. Per-tenant rate limits and statement timeouts stop its load spikes from hurting others.

**Q:** How do caches and scheduled jobs stay tenant-safe?

**A:** Every cache key is prefixed with the tenant id, so two tenants asking for "project 7" never share an entry. Nightly jobs iterate tenants explicitly and bind each one in turn, instead of running with no tenant. Logs carry the tenant as a field so incidents can be scoped.

**Q:** Write the RLS policy.

**A:**
```sql
ALTER TABLE issue ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON issue
  USING (tenant_id = current_setting('app.tenant_id', true)::uuid)
  WITH CHECK (tenant_id = current_setting('app.tenant_id', true)::uuid);
-- per transaction: SET LOCAL app.tenant_id = '…';
```
With `true` as the second argument, a missing setting yields `NULL`, so no rows match.

**Common mistakes in answers:**
- "We add `WHERE tenant_id = ?` everywhere" — one forgotten query leaks data; isolation needs enforcement below the application code too.
- Taking the tenant from a header or query parameter — it must come from the authenticated token.
- Forgetting async work, caches and pooled connections — that is where most real leaks happen.

### TL;DR
Tenant as a first-class concept: resolved from the token, bound per request, enforced by Hibernate and by PostgreSQL row-level security, carried across threads and messages, and proved by an isolation test suite.

## 27. Microservices Shop with Gateway, Config and Discovery
**Level:** Advanced · **Time:** 4 weeks · **Prerequisites:** Ch 6, Ch 16

### What it is
A small online shop split into services — catalog, cart, orders, payments — behind **Spring Cloud Gateway**, with centralized configuration from **Spring Cloud Config Server**, service registration and lookup through **service discovery** (Eureka, or Kubernetes' own), client-side load balancing, circuit breakers and distributed tracing. **Spring Cloud** is a family of projects for these distributed-system concerns, released as a "train" whose version must match your Boot version: for Boot 4 that is 2025.1 "Oakwood" (2025.1.2 or later for Boot 4.1).

### The problem it solves
Microservices trade code complexity for operational complexity: how do services find each other, where does configuration live, what is the single front door, how does one slow service avoid taking down the rest, and how do you debug a request crossing five processes? This project builds a minimal but complete platform so you can speak from experience — including when **not** to split.

### Real-world examples
- [Spring PetClinic Microservices](https://github.com/spring-petclinic/spring-petclinic-microservices) is a reference application using Config Server, Eureka, Spring Cloud Gateway, Resilience4j and tracing — compare your design with it.
- The [Spring Cloud Gateway reference](https://docs.spring.io/spring-cloud-gateway/reference/) covers routes, predicates, filters and the MVC and WebFlux server variants.
- [Netflix Eureka](https://github.com/Netflix/eureka) is the registry Spring Cloud Netflix wraps; its README explains the client-side discovery model.

### Requirements
**Functional**
- Gateway routes: `/api/catalog/**`, `/api/cart/**`, `/api/orders/**` to services by logical name (`lb://catalog`).
- Orders call catalog (prices) and payments synchronously via HTTP service interfaces resolved through discovery.
- Config Server serves per-service, per-profile configuration from a Git repository; secrets come from environment variables or a vault, not Git.
- Gateway applies authentication (JWT validation), CORS, a request-size limit and per-client rate limits.

**Non-functional**
- Any single instance of catalog can die without failed user requests (2 instances, retries on idempotent `GET`s only).
- If payments is down, order placement fails fast (circuit open) within 50 ms instead of timing out after seconds.
- A trace id follows each request through gateway and services and appears in every log line.
- Each service is independently deployable and owns its database.

### The architecture
```mermaid
flowchart TD
    U["Client"] --> GW["Spring Cloud Gateway - server webmvc"]
    GW --> CAT["catalog x2"]
    GW --> CART["cart"]
    GW --> ORD["orders"]
    ORD -->|"HTTP interface lb"| CAT
    ORD -->|"HTTP interface lb, circuit breaker"| PAY["payments"]
    CFG["Config Server - Git backend"] -.-> GW
    CFG -.-> CAT
    CFG -.-> ORD
    REG["Eureka registry"] -.-> GW
    REG -.-> ORD
    CAT --> DB1[("catalog db")]
    ORD --> DB2[("orders db")]
    PAY --> DB3[("payments db")]
    GW -.->|"OTLP"| OTEL["Tracing backend"]
```
- **Gateway** — routes by path to `lb://` service names; filters for auth, rate limiting, `StripPrefix`, retries for `GET`, circuit breakers.
- **Config Server** — serves `application.yaml`, `orders.yaml`, `orders-prod.yaml` from Git; clients import it with `spring.config.import=configserver:`.
- **Eureka** — services register themselves and send heartbeats; clients fetch the registry and pick an instance with Spring Cloud LoadBalancer.
- **Service-to-service calls** — `@HttpExchange` interfaces on a load-balanced `RestClient.Builder`.
- **Resilience4j** — circuit breaker, time limiter and bulkhead around payments calls.
- **Per-service databases** — no shared tables; data crossing boundaries goes through APIs or events.

### Data model
| Service | Owns | Exposes |
|---|---|---|
| catalog | products, prices | `GET /products/{id}`, price lookup |
| cart | carts per user | cart CRUD |
| orders | orders, order lines (with price snapshots) | place order, history |
| payments | payment intents | authorize, capture, status |

Orders stores copies of the price and product name it used — data it needs to keep even if catalog changes later.

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Train | Spring Cloud 2025.1.x (2025.1.2+ for Boot 4.1) | Boot 4 compatibility |
| Gateway | `spring-cloud-starter-gateway-server-webmvc` | servlet stack, virtual threads, same model as the services |
| Config | Spring Cloud Config Server with Git | versioned, reviewable config |
| Discovery | Eureka locally; Kubernetes service discovery in a cluster | portable design |
| Clients | HTTP service interfaces + LoadBalancer | Spring's recommended declarative client |
| Resilience | `resilience4j-spring-boot4` | circuit breaker, bulkhead, time limiter |
| Tracing | `spring-boot-starter-opentelemetry` | trace context propagation |

### Milestones
1. **Two services and a gateway** with static URLs. *Done when* `curl` through the gateway reaches both and a gateway test stubs the backends.
2. **Config Server.** *Done when* changing a property in Git and calling a refresh endpoint (or restarting) changes behaviour, and the app fails fast if Config Server is unreachable in `prod`.
3. **Discovery and load balancing.** *Done when* stopping one catalog instance causes no failed `GET`s in a 1-minute load test.
4. **Circuit breaker.** *Done when* with payments down, `POST /orders` returns 503 in under 50 ms after the breaker opens, and recovers via half-open probes.
5. **Security and tracing.** *Done when* the gateway validates JWTs and relays them, and one trace shows gateway → orders → catalog and payments spans.

### Deep dive: the hard parts
- **Timeouts, retries and breakers fit together.** A retry policy at the gateway, in the HTTP client and in the service multiplies calls (3 × 3 × 3 = 27 downstream calls). Retry in one layer only, only idempotent operations, with a total time budget smaller than the caller's timeout. The breaker sits inside the retry so open-circuit failures are not retried.
- **Discovery is eventually consistent.** A dead instance stays in clients' cached registry until heartbeats expire and caches refresh; the first requests still hit it. Client retries on connection failure to another instance cover that window — another reason retries must be idempotent-only.
- **Config refresh is not free.** `@RefreshScope` beans are re-created on refresh, which can surprise (connection pools, caches). Many teams prefer immutable config and rolling restarts; know both.
- **Data ownership.** The moment two services share a table, you have a distributed monolith. Sagas (project 28) and events replace cross-service transactions.

### Testing strategy
- Each service tested in isolation with its dependencies stubbed (`MockRestServiceServer`, WireMock).
- Contract tests (Spring Cloud Contract or consumer-driven) between orders and catalog/payments.
- A Docker Compose or Testcontainers environment for a small end-to-end smoke test, kept to a handful of scenarios.

### Trade-offs, scaling and failure modes
- Four services multiply build, deploy, monitoring and on-call work. For a small team, a modular monolith (project 31) delivers most of the structure with a fraction of the cost; write that trade-off in your README.
- Eureka adds a component to run; on Kubernetes, platform DNS discovery is usually enough.
- The gateway is a single point of failure — run at least two instances, keep it thin.

### Stretch goals
- Replace Eureka with Kubernetes discovery and Config Server with ConfigMaps; compare.
- Canary routing with the gateway `Weight` predicate.
- Consumer-driven contract tests running in each provider's build.

### Skills practised
- [Ch 16 — Spring Cloud and microservices](../chapters/16-spring-cloud-and-microservices/README.md): release trains, Config Server, discovery, LoadBalancer, HTTP interface clients vs OpenFeign, Gateway, Resilience4j, tracing across services, contract testing.
- [Ch 6 — REST clients and HTTP interfaces](../chapters/06-rest-clients-and-http-interfaces/README.md): HTTP service interfaces and timeouts.
- [Ch 11 — Tokens and OAuth2](../chapters/11-spring-security-2-tokens-and-oauth2/README.md): JWT validation and token relay at the gateway.
- [Ch 15 — Observability and production](../chapters/15-observability-and-production/README.md): tracing.

### In the interview
**30-second answer:** I split a small shop into catalog, cart, orders and payments services behind Spring Cloud Gateway, with Config Server for configuration, Eureka for discovery, load-balanced HTTP service interfaces between services, Resilience4j circuit breakers and OpenTelemetry tracing. The key design decision was that each service owns its database and resilience is layered deliberately: timeouts everywhere, retries only for idempotent calls in one layer, and a breaker so a dead payments service fails fast in under 50 ms. The hardest part was retry amplification across layers, which I removed by retrying only at the client. One trace now shows gateway → orders → catalog and payments, and losing a catalog instance causes no failed requests.

**Key terms to know:**
- **API gateway** — the single entry point that routes, authenticates and rate-limits; Spring Cloud Gateway (server WebMVC variant here).
- **Service discovery** — services register and clients look up instances by name (`lb://catalog`); Eureka or Kubernetes DNS.
- **Client-side load balancing** — the caller picks an instance from the registry; Spring Cloud LoadBalancer.
- **Config Server** — serves per-service, per-profile configuration from Git; clients use `spring.config.import=configserver:`.
- **Circuit breaker** — stops calling a failing dependency after a threshold, fails fast, then probes in half-open state.
- **Timeout budget / retry amplification** — total time allowed for a call chain; retries in several layers multiply calls.
- **Bulkhead** — isolating resources per dependency so one cannot exhaust all threads.
- **Database per service** — no shared tables; data crosses boundaries via APIs or events.
- **Distributed tracing (`traceparent`)** — one trace id follows a request through all services.
- **Release train** — matching Spring Cloud version for a Boot version: 2025.1 "Oakwood" for Boot 4.

**Q:** Which Spring Cloud version do you use with Boot 4.1?

**A:** The 2025.1 "Oakwood" train, 2025.1.2 or later, imported as a BOM. The train is chosen to match the Boot version; mixing trains and Boot versions is a common cause of startup failures and `NoSuchMethodError`s.

**Q:** How do retries and circuit breakers interact?

**A:** Retry only idempotent calls, in one layer, within a total deadline smaller than the caller's timeout — retrying at the gateway, the client and the service would turn one call into 27. The breaker sits inside the retry, so when it is open the call fails immediately and is not retried. The breaker also stops retries from hammering a dependency that is already failing.

**Q:** Explain the circuit breaker states.

**A:** Closed: calls pass and failures are counted over a sliding window. Open: after the failure rate crosses a threshold, calls fail immediately without touching the dependency, for a wait period. Half-open: a few trial calls go through; if they succeed it closes, otherwise it opens again. That is how `POST /orders` returns 503 in under 50 ms while payments is down.

**Q:** When would you not use microservices?

**A:** With a small team, unclear domain boundaries, or no need for independent scaling or deployment. Microservices multiply build, deploy, monitoring and on-call work and turn method calls into network calls. I would start with a modular monolith (project 31) and extract services along boundaries that have proven stable.

**Q:** A catalog instance dies. What happens to requests?

**A:** Discovery is eventually consistent: until heartbeats expire and client caches refresh, some requests still go to the dead instance and fail with connection errors. The load-balanced client retries an idempotent `GET` on another instance, so users see no failure. Non-idempotent calls are not retried, which is why `POST /orders` relies on the breaker and idempotency keys instead.

**Q:** Config Server goes down. What happens?

**A:** Running services keep the configuration they loaded at startup. New instances fail to start in `prod`, because I made config import fail-fast there — starting with wrong defaults would be worse. Running Config Server with several instances, or using Kubernetes ConfigMaps, removes that single point of failure.

**Q:** What is `@RefreshScope` and why might you avoid it?

**A:** Beans in refresh scope are re-created when configuration is refreshed, so new property values apply without a restart. Re-creation can surprise you — connection pools rebuilt, caches dropped, references held elsewhere still pointing at the old bean. Many teams prefer immutable configuration and a rolling restart.

**Q:** What changes at 10x traffic? What breaks first?

**A:** The gateway is the first shared choke point, so it runs several instances and stays thin. Synchronous chains (orders → catalog → payments) multiply latency and failure probability, so price lookups move to local copies updated by events. Eureka is often replaced by Kubernetes service discovery to remove a component.

**Q:** How do you test services that call each other?

**A:** Each service is tested in isolation with its dependencies stubbed (`MockRestServiceServer`, WireMock), and consumer-driven contract tests make sure orders' expectations of catalog and payments match what they really provide. A small Docker Compose or Testcontainers environment runs a handful of end-to-end smoke scenarios, not hundreds.

**Common mistakes in answers:**
- Sharing a database between services "to keep it simple" — that creates a distributed monolith.
- Retrying in every layer — retries multiply and turn a slowdown into an outage.
- "Microservices are always more scalable" — they add operational cost; scaling needs rarely justify them for small teams.

### TL;DR
A minimal but complete Spring Cloud platform — gateway, config, discovery, load-balanced HTTP interfaces, circuit breakers and tracing — built to understand both the benefits and the operational price of microservices.

## 28. Order Saga with a Transactional Outbox
**Level:** Advanced · **Time:** 3–4 weeks · **Prerequisites:** Ch 7, Ch 14, Ch 16

### What it is
An order workflow spanning three services — orders, payments, inventory — that must end consistently (order confirmed, card charged, stock reserved; or everything undone) **without a distributed transaction**. It uses a **saga**: a sequence of local transactions, each publishing an event or command that triggers the next, with **compensating actions** (refund, release stock) when a later step fails. Every message is published through a **transactional outbox**: the event is written to an `outbox` table in the same database transaction as the business change, then relayed to the broker.

### The problem it solves
In project 27 each service owns its database, so "charge the card and reserve stock atomically" is impossible with one transaction. Naive solutions lose messages ("saved the order, crashed before publishing") or publish messages for changes that rolled back. Outbox + idempotent consumers + saga state is the standard answer, and it is a favourite senior interview topic.

### Real-world examples
- Chris Richardson's pattern pages for the [Saga](https://microservices.io/patterns/data/saga.html) and the [Transactional outbox](https://microservices.io/patterns/data/transactional-outbox.html) define the vocabulary (choreography vs orchestration, message relay, polling publisher).
- Debezium's [Outbox Event Router](https://debezium.io/documentation/reference/stable/transformations/outbox-event-router.html) documents the change-data-capture way to relay outbox rows to Kafka.

### Requirements
**Functional**
- `POST /orders` creates an order in `PENDING`; the saga reserves stock, authorizes payment, then confirms the order.
- Failures: out of stock → order `REJECTED`; payment declined → release stock, order `REJECTED`; timeout in any step → compensate and `CANCELLED`.
- Order status endpoint shows the saga's progress and history.
- Operators can see stuck sagas and retry or compensate them manually.

**Non-functional**
- No lost messages and no phantom messages (events for rolled-back changes) — proved by crash tests.
- Every consumer is idempotent: duplicates change nothing.
- A saga completes p95 within 2 s under normal conditions; stuck sagas are detected within 1 minute.
- Eventual consistency only: the client sees `PENDING` until the saga ends.

### The architecture
```mermaid
sequenceDiagram
    participant C as Client
    participant O as Orders - orchestrator
    participant K as Kafka
    participant I as Inventory
    participant P as Payments
    C->>O: POST /orders
    O->>O: tx - insert order PENDING, saga row, outbox ReserveStock
    O->>K: relay publishes ReserveStock
    K->>I: ReserveStock
    I->>I: tx - reserve, inbox row, outbox StockReserved
    I->>K: StockReserved
    K->>O: StockReserved
    O->>O: tx - saga step 2, outbox AuthorizePayment
    O->>K: AuthorizePayment
    K->>P: AuthorizePayment
    P->>K: PaymentDeclined
    K->>O: PaymentDeclined
    O->>O: tx - saga compensating, outbox ReleaseStock
    O->>K: ReleaseStock
    K->>I: ReleaseStock
    I->>K: StockReleased
    K->>O: StockReleased
    O->>O: tx - order REJECTED
```
- **Orders (orchestrator)** — owns the saga state machine and decides the next command; **orchestration** keeps the workflow in one place, unlike **choreography**, where each service reacts to others' events.
- **Outbox relay** — a poller reading unsent outbox rows (`FOR UPDATE SKIP LOCKED`) and publishing to Kafka, marking them sent after the broker acknowledges; or Debezium tailing the database log.
- **Inbox** — each consumer records processed message ids in the same transaction as its effect.
- **Timeout checker** — a scheduled job finding sagas stuck in a step past their deadline and triggering compensation.

### Data model
```mermaid
erDiagram
    ORDERS ||--|| SAGA : "driven by"
    SAGA ||--o{ SAGA_STEP_LOG : "records"
    ORDERS {
        uuid id PK
        text status
        numeric total
        timestamptz created_at
    }
    SAGA {
        uuid id PK
        uuid order_id FK
        text state
        int version
        timestamptz deadline
    }
    SAGA_STEP_LOG {
        bigint id PK
        uuid saga_id FK
        text step
        text outcome
        timestamptz at
    }
    OUTBOX {
        uuid id PK
        text aggregate_id
        text type
        jsonb payload
        timestamptz created_at
        timestamptz sent_at
    }
    INBOX {
        uuid message_id PK
        text consumer
        timestamptz processed_at
    }
```
Each service has its own `OUTBOX` and `INBOX` tables in its own database.

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Broker | Kafka via `spring-boot-starter-kafka` | durable, ordered per key (saga id) |
| Outbox relay | `@Scheduled` poller with `SKIP LOCKED` (or Debezium CDC) | simple first, CDC for scale |
| State machine | an explicit enum + transition table in code | readable, testable |
| Transactions | `@Transactional` local transactions only | no XA/2PC |
| Alternative | Spring Modulith event publication registry (project 31) | the same idea inside one app |

### Milestones
1. **Outbox in one service.** *Done when* a test that throws after the business insert leaves no outbox row, and a test that kills the relay after publishing but before marking sent results in a duplicate that consumers ignore.
2. **Inbox and idempotent consumers.** *Done when* delivering each message three times produces exactly one effect in every service.
3. **Happy-path saga.** *Done when* an order goes `PENDING → CONFIRMED` through all steps in an integration test with three services (or three modules) and Kafka.
4. **Compensations.** *Done when* declined payment and out-of-stock paths end `REJECTED` with stock correctly restored.
5. **Timeouts and operations.** *Done when* a silent inventory service triggers compensation after the deadline, and an admin endpoint lists and retries stuck sagas.

### Deep dive: the hard parts
- **Why the outbox works.** The dual-write problem — database and broker cannot commit atomically — is turned into one local transaction plus an at-least-once relay. The cost is duplicates and latency (poll interval), both handled by idempotent consumers and a short poll.
- **Compensations are business actions, not rollbacks.** A refund is a new transaction visible in reports; a released reservation might already have been resold. Design each step with its compensation, and make compensations idempotent and retryable forever.
- **Ordering and concurrency.** Key messages by saga id so one saga's messages stay ordered. Optimistic locking (`version`) on the saga row stops two messages for the same saga (a late timeout and a late reply) from both advancing it.
- **Semantic locks.** While a saga is in flight, other operations may see intermediate states (order `PENDING`, stock reserved but unpaid). Mark such states explicitly and make other features respect them — the saga equivalent of isolation.

### Testing strategy
- State-machine unit tests: every (state, event) pair → expected next state and command, including unexpected events.
- Crash-injection tests around each transaction and the relay.
- An end-to-end test with Kafka (Testcontainers) for one happy and two failure paths.

### Trade-offs, scaling and failure modes
- Orchestration centralizes logic (easy to understand, one more component); choreography avoids a coordinator but spreads the workflow across services — hard to see and change.
- Polling outbox relays are simple but add load and latency; CDC is near-real-time but adds Debezium and Kafka Connect to operate.
- Sagas give eventual, not immediate, consistency; product and support teams must understand intermediate states.

### Stretch goals
- Replace polling with Debezium and compare end-to-end latency.
- Implement the same flow with choreography and compare readability.
- Add a "compensation failed" dead-letter path with alerting and a manual-resolution UI.

### Skills practised
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): `@TransactionalEventListener`, Kafka, idempotence, the transactional outbox.
- [Ch 7 — Data access with JDBC and transactions](../chapters/07-data-access-jdbc-and-transactions/README.md): local transactions, `SKIP LOCKED`.
- [Ch 16 — Spring Cloud and microservices](../chapters/16-spring-cloud-and-microservices/README.md): messaging between services.
- [Ch 8 — Spring Data JPA and Hibernate](../chapters/08-spring-data-jpa-and-hibernate/README.md): optimistic locking on saga state.

### In the interview
**30-second answer:** I built an order workflow across orders, inventory and payments that ends consistent — confirmed, or fully compensated — without any distributed transaction. The orders service orchestrates a saga: each step is a local transaction that writes its state change and the next command into an outbox table atomically, a relay publishes outbox rows to Kafka, and every consumer de-duplicates with an inbox table. The hardest part was failure handling: compensations such as releasing stock are business actions that must be idempotent, and a timeout checker compensates sagas stuck past their deadline. Crash-injection tests prove there are no lost and no phantom messages.

**Key terms to know:**
- **Saga** — a sequence of local transactions where each step triggers the next and failures trigger compensating actions.
- **Orchestration vs choreography** — one coordinator tells services what to do vs services reacting to each other's events.
- **Compensating action** — a new business transaction that semantically undoes an earlier step (refund, release stock).
- **Dual-write problem** — a database commit and a broker publish cannot be made atomic together.
- **Transactional outbox** — write the message to an `outbox` table in the same transaction as the change; relay it later.
- **Message relay (polling or CDC)** — publishes outbox rows; a `SKIP LOCKED` poller or Debezium tailing the log.
- **Inbox / idempotent consumer** — records processed message ids with the effect so duplicates change nothing.
- **Two-phase commit (2PC/XA)** — a coordinator-driven atomic commit across resources; deliberately not used.
- **Semantic lock** — an explicit intermediate state (`PENDING`) that other features must respect.
- **Saga timeout** — a deadline after which a stuck saga is compensated.

**Q:** Why not two-phase commit?

**A:** Brokers and databases across services rarely support XA well, 2PC couples availability (every participant must be up to commit), and it holds locks across the network while the coordinator decides. If the coordinator dies at the wrong moment, participants stay blocked. Sagas trade isolation for availability, with explicit compensations instead of rollback.

**Q:** How does the outbox prevent lost messages?

**A:** The event row commits in the same local transaction as the business change, so either both exist or neither does. A relay then publishes unsent rows at least once and marks them sent after the broker acknowledges. If the relay crashes after publishing but before marking, the message is sent again, which the consumer's inbox ignores.

**Q:** What is a phantom message and how is it avoided?

**A:** A message published for a change that then rolled back — for example publishing to Kafka inside a transaction that later fails. With the outbox, nothing is published until the transaction has committed, because the relay only sees committed rows. A crash test throws after the business insert and asserts no outbox row and no message.

**Q:** Orchestration or choreography?

**A:** Orchestration here: three steps with compensations are much easier to read, change and monitor when one state machine owns the workflow, at the cost of one more component. Choreography avoids a coordinator and suits simple reactions, but the workflow is spread across services and hard to see. I would switch to choreography only for loosely coupled, fire-and-forget reactions.

**Q:** Payment is declined after stock was reserved. What happens?

**A:** Payments emits `PaymentDeclined`; the orchestrator moves the saga to compensating and writes a `ReleaseStock` command to its outbox in the same transaction. Inventory releases the reservation idempotently and replies `StockReleased`, and the order ends `REJECTED`. Every step is in the saga step log, visible through the status endpoint.

**Q:** Two messages for the same saga arrive at once — a late reply and a timeout. What stops both advancing it?

**A:** Messages are keyed by saga id so they share a partition and arrive in order to one consumer, and the saga row has a `version` column for optimistic locking. If two handlers still race, one update fails with a version conflict, reloads, sees the new state and treats the stale message as already handled.

**Q:** What if a compensation itself fails?

**A:** Compensations must be retryable forever and idempotent, because there is no "undo the undo". They retry with back-off; if they keep failing, the saga moves to a `COMPENSATION_FAILED` state, an alert fires, and an operator resolves it through the admin endpoint. This path is designed, not left to chance.

**Q:** What changes at 10x order volume?

**A:** The polling relay adds database load and latency proportional to the poll interval, so I would move to Debezium change-data-capture, which tails the write-ahead log in near real time. Kafka partitions by saga id scale consumers horizontally, and the outbox and inbox tables need cleanup jobs or partitioning so they do not grow forever.

**Q:** How did you test it?

**A:** The state machine is unit-tested for every (state, event) pair, including unexpected events. Crash-injection tests throw around each transaction and kill the relay between publish and mark-sent. An end-to-end test with Kafka in Testcontainers runs the happy path, out-of-stock and payment-declined, asserting final states and that stock is restored.

**Common mistakes in answers:**
- "Publish to Kafka inside the `@Transactional` method" — the publish is not part of the database transaction; it can be lost or become a phantom.
- "Compensation is a rollback" — it is a new, visible business transaction, and the world may have changed meanwhile.
- Forgetting idempotency — outbox relays and Kafka are at-least-once, so every consumer must de-duplicate.

### TL;DR
A saga that keeps three services consistent through local transactions, outbox-relayed commands, idempotent inboxes, compensations and timeouts — no distributed transaction anywhere.

## 29. Observability with Micrometer and OpenTelemetry
**Level:** Advanced · **Time:** 2–3 weeks · **Prerequisites:** Ch 15, Ch 16

### What it is
An observability layer for an existing multi-service system (project 27 or 28): **metrics** (numbers over time — request rates, latencies, queue lag), **traces** (the path of one request across services as a tree of timed **spans**) and **structured logs** (JSON log lines carrying trace ids), all correlated, exported with **OpenTelemetry**'s protocol (OTLP) to a collector, and turned into dashboards, **SLOs** (service-level objectives, e.g. "99.5% of checkouts succeed in under 500 ms over 28 days") and alerts. In Spring, **Micrometer** is the instrumentation facade, and its **Observation API** produces both metrics and spans from one instrumentation point.

### The problem it solves
"It's slow" and "some orders fail" are impossible to debug across services with plain logs. Good observability answers *what* is broken (metrics and SLO alerts), *where* (traces) and *why* (logs and exemplars) in minutes. It is also easy to do badly: unbounded metric tags that explode storage, 100% trace sampling that costs a fortune, alerts on symptoms nobody cares about.

### Real-world examples
- The [OpenTelemetry documentation](https://opentelemetry.io/docs/) defines signals, OTLP, the collector and semantic conventions for span and metric names.
- Google's paper ["Dapper, a Large-Scale Distributed Systems Tracing Infrastructure"](https://research.google/pubs/dapper-a-large-scale-distributed-systems-tracing-infrastructure/) introduced the trace/span model and the case for sampling that modern tracing follows.
- [Micrometer](https://micrometer.io/) documents meters, the Observation API and tracing bridges used by Spring Boot.

### Requirements
**Functional**
- Every HTTP server and client call, database query, Kafka produce/consume and `@Async` task is traced, with context propagated across services (W3C `traceparent` header).
- Business metrics: orders placed, payment failures by reason, saga duration, cart abandonment.
- Logs in JSON with `traceId`, `spanId`, service, version and tenant (when present).
- Dashboards: RED (rate, errors, duration) per service, Kafka consumer lag, JVM and connection pools.
- Two SLOs with burn-rate alerts: checkout availability and checkout latency.

**Non-functional**
- Instrumentation overhead under 5% CPU at production load.
- Tail-based sampling at the collector: keep 100% of error traces and slow traces, about 10% of the rest.
- No metric with unbounded tag cardinality (no user ids, order ids or raw URLs as tags).

### The architecture
```mermaid
flowchart LR
    S1["Service A - Micrometer, Observation API"] -->|"OTLP traces and metrics"| COL["OpenTelemetry Collector"]
    S2["Service B"] -->|"OTLP"| COL
    S1 -->|"JSON logs to stdout"| LA["Log agent"]
    S2 -->|"JSON logs"| LA
    COL -->|"tail sampling"| TR["Trace store - Tempo or Jaeger"]
    COL --> MET["Metrics store - Prometheus compatible"]
    LA --> LOGS["Log store - Loki or Elasticsearch"]
    MET --> GRA["Grafana - dashboards, SLOs, alerts"]
    TR --> GRA
    LOGS --> GRA
```
- **Services** — `spring-boot-starter-opentelemetry` adds Micrometer Tracing with the OpenTelemetry bridge and OTLP exporters; Actuator adds metrics endpoints.
- **Observation API** — `ObservationRegistry` and `@Observed` (or `Observation.createNotStarted(...).observe(...)`) for business operations; one observation yields a timer and a span.
- **Collector** — receives OTLP, applies tail sampling and attribute processing, and fans out to backends, so apps never hard-code a vendor.
- **Logs** — Boot's structured logging (`logging.structured.format.console=ecs` or `logstash`), with trace ids from the MDC.
- **Grafana** — correlates: from an SLO alert to a latency panel, to **exemplars** (sample trace ids attached to metric buckets), to the trace, to its logs.

### Data model
| Signal | Example | Tags (bounded) |
|---|---|---|
| Timer `http.server.requests` | latency histogram | method, uri template, status, outcome |
| Counter `orders.placed` | business rate | channel, payment method |
| Timer `saga.duration` | end-to-end saga time | outcome |
| Gauge `kafka.consumer.lag` | records behind | topic, group |
| Span `checkout.place` | one per checkout | `order.items.count` as a span attribute, not a metric tag |

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Instrumentation | Micrometer Observation + Micrometer Tracing (OTel bridge) | Spring's built-in instrumentation uses it |
| Export | `spring-boot-starter-opentelemetry`, `management.opentelemetry.*` properties (Boot 4.1) | OTLP for traces and metrics |
| Logs | Boot structured logging | JSON without extra libraries |
| Pipeline | OpenTelemetry Collector | sampling, routing, vendor neutrality |
| Backends | Prometheus-compatible store, Tempo/Jaeger, Loki, Grafana | open-source stack; swap for a vendor freely |

### Milestones
1. **Traces across services.** *Done when* a request through the gateway shows one trace with spans from every service, database and Kafka hop, and logs carry the same trace id.
2. **Business observations.** *Done when* `@Observed` (or programmatic observations) on checkout and saga steps produce both timers and spans with low-cardinality tags.
3. **Context propagation everywhere.** *Done when* `@Async` tasks (`spring.task.execution.propagate-context=true`), Reactor pipelines and Kafka consumers continue the parent trace — verified by a test with an in-memory span exporter.
4. **Sampling and cost.** Head sampling in apps at 100% to the collector, tail sampling in the collector. *Done when* every error trace is kept and total stored traces fall to roughly 10–15% under a load test.
5. **SLOs and alerts.** *Done when* dashboards show SLO compliance and a multi-window burn-rate alert fires during an injected 5% error rate, but not for a 30-second blip.

### Deep dive: the hard parts
- **Cardinality.** Each unique tag combination is a separate time series. Tagging `http.server.requests` with the raw URI (`/orders/123`) creates one series per order and can take down a metrics backend. Spring uses the URI **template** (`/orders/{id}`); put high-cardinality data on spans or logs instead.
- **Context propagation.** Trace context lives in thread-local state and request headers. It is lost on thread hops you create yourself, in message consumers that do not extract headers, and in hand-built HTTP clients. Always build clients from Boot's auto-configured builders so instrumentation is applied.
- **Sampling.** Head sampling (decided at the first service) is cheap but blind — it drops the rare error trace. Tail sampling decides after the whole trace is seen, keeping errors and slow traces, at the cost of buffering in the collector. Combine them.
- **SLO alerting.** Alerting on "error rate > 1% for 5 minutes" pages on noise and misses slow burns. Burn-rate alerts compare how fast you consume the error budget over short and long windows together, catching real incidents quickly and ignoring blips.

### Testing strategy
- Tests with an in-memory span exporter and `TestObservationRegistry` asserting span names, parents and tags.
- A cardinality test: hit an endpoint with 1,000 different ids and assert the number of meter instances stays constant.
- A local Docker Compose stack for manual verification of dashboards.

### Trade-offs, scaling and failure modes
- Observability data volume can rival business data; sampling, retention policies and aggregation are cost controls, not afterthoughts.
- A collector outage must never block the application: exporters batch and drop on overflow. Verify it by stopping the collector under load.
- Auto-instrumentation via a Java agent covers more libraries with no code; Micrometer-native instrumentation is lighter and integrated with Spring. Choose one primary approach to avoid duplicate spans.

### Stretch goals
- Exemplars linking latency histogram buckets to traces.
- Continuous profiling (JFR-based) correlated with traces.
- A synthetic-check job that exercises checkout every minute and feeds the availability SLO.

### Skills practised
- [Ch 15 — Observability and production](../chapters/15-observability-and-production/README.md): Actuator, Micrometer metrics, the Observation API, tracing, structured logging.
- [Ch 16 — Spring Cloud and microservices](../chapters/16-spring-cloud-and-microservices/README.md): tracing across services.
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): context in `@Async` and Kafka.
- [Ch 13 — Reactive programming and Spring WebFlux](../chapters/13-reactive-and-webflux/README.md): Reactor context propagation.

### In the interview
**30-second answer:** I added observability to my microservices shop: metrics, traces and structured JSON logs, correlated by trace id and exported over OTLP to an OpenTelemetry Collector, then into Grafana dashboards, SLOs and burn-rate alerts. The key design decision was instrumenting once with Micrometer's Observation API, which produces both a timer and a span, and keeping every metric tag low-cardinality while ids go into spans and logs. The hardest parts were context propagation across `@Async`, Reactor and Kafka hops, and cost, which I controlled with tail sampling in the collector: 100% of error and slow traces, about 10% of the rest. Instrumentation overhead stayed under 5% CPU.

**Key terms to know:**
- **Metrics / traces / logs** — numbers over time, the path of one request as timed spans, and detailed event records.
- **Span and trace context (`traceparent`)** — a timed operation in a trace; context propagated in the W3C header.
- **Micrometer Observation API** — one instrumentation point that produces metrics and spans via handlers; `@Observed`.
- **OpenTelemetry / OTLP / Collector** — vendor-neutral standard, its protocol, and the agent that receives, samples and routes data.
- **Cardinality** — the number of unique tag combinations; each is a separate time series.
- **Head vs tail sampling** — decide at the first service vs after seeing the whole trace.
- **RED metrics** — rate, errors, duration per service.
- **SLO / error budget / burn rate** — a reliability target, the failures it allows, and how fast you are consuming them.
- **Exemplar** — a sample trace id attached to a metric bucket, linking a latency spike to a real trace.

**Q:** Metrics, traces, logs — when do you use which?

**A:** Metrics are cheap aggregates for detecting problems and alerting: "checkout errors are up". Traces locate the slow or failing hop in one request across services. Logs give detailed context for one event. They are correlated by trace id, so I can go from an alert to an exemplar trace to its logs in a few clicks.

**Q:** What is the Observation API?

**A:** Micrometer's single instrumentation point: code wraps an operation in an `Observation`, and registered handlers turn it into a timer metric, a span, or log entries. Spring's own HTTP, data and messaging instrumentation uses it, and I use `@Observed` or `Observation.createNotStarted(...).observe(...)` for business operations such as checkout, so code is instrumented once.

**Q:** How do you avoid a metrics cardinality explosion?

**A:** Only bounded tag values: URI templates (`/orders/{id}`, not `/orders/123`), status classes, enums, a small set of channels. Ids, user names and raw URLs go into span attributes or logs. A test hits an endpoint with 1,000 different ids and asserts the number of meters stays constant.

**Q:** Head or tail sampling?

**A:** Head sampling decides at the first service, which is cheap but blind — at 10% it throws away 90% of the rare error traces. Tail sampling in the collector waits for the whole trace and keeps all errors and slow traces plus a fraction of the rest, at the cost of buffering. I send 100% from apps to the collector and tail-sample there; head sampling only if collector cost demands it.

**Q:** Why did a trace break into two at an `@Async` call?

**A:** Trace context lives in thread-local state, and a new thread does not inherit it unless something propagates it. Boot's auto-configured task executor can propagate context (via a context-propagating task decorator), but executors I create myself do not. The same happens with hand-built HTTP clients and Kafka consumers that do not extract headers, so I always use Boot's auto-configured builders.

**Q:** What is a burn-rate alert and why is it better than "error rate > 1%"?

**A:** It measures how fast the error budget of an SLO is being consumed, checked over a short and a long window together. A fixed threshold pages on 30-second blips and misses slow, steady burns. A multi-window burn-rate alert fires quickly for real incidents and ignores noise, which I verified by injecting a 5% error rate and a short blip.

**Q:** The collector goes down. What happens to the services?

**A:** Nothing visible to users: exporters batch asynchronously with bounded queues and drop data when full instead of blocking request threads. We lose telemetry for that period, which is acceptable. I verified it by stopping the collector under load and checking latency was unchanged.

**Q:** What changes at 10x traffic?

**A:** Telemetry volume and cost grow linearly, so sampling ratios, retention periods and metric aggregation become cost controls. The collector becomes a tier of several instances; tail sampling then needs all spans of a trace routed to the same collector instance, typically with a load-balancing exporter keyed by trace id.

**Q:** How do you test instrumentation?

**A:** With `TestObservationRegistry` and an in-memory span exporter, tests assert span names, parent-child relationships and tags, including across `@Async` and Kafka hops. The cardinality test guards the metrics side. A local Docker Compose stack is used for manual checks of dashboards.

**Q:** Show a business observation.

**A:**
```java
Observation.createNotStarted("checkout.place", registry)
    .lowCardinalityKeyValue("payment.method", order.paymentMethod().name())
    .highCardinalityKeyValue("order.id", order.id().toString())   // span only
    .observe(() -> checkout.place(order));
```
Low-cardinality values become metric tags and span attributes; high-cardinality values go only on the span.

**Common mistakes in answers:**
- Tagging metrics with user or order ids — this explodes the number of time series and the bill.
- "We log everything, so we have observability" — without metrics and traces you cannot detect or locate problems quickly.
- Sampling 1% at the head and expecting to find errors — rare error traces are thrown away before anyone sees them.

### TL;DR
One instrumentation API, three correlated signals exported over OTLP, bounded cardinality, tail sampling and burn-rate SLO alerts — observability that finds problems without bankrupting you.

## 30. Spring AI RAG Documentation Assistant
**Level:** Advanced · **Time:** 3 weeks · **Prerequisites:** Ch 17, Ch 13

### What it is
An internal assistant that answers employees' questions from the company's own documentation (Markdown handbooks, PDFs, runbooks) with citations, using **retrieval-augmented generation (RAG)**: split documents into chunks, turn each chunk into an **embedding** (a vector of numbers capturing its meaning), store the vectors, and at question time retrieve the most similar chunks and give them to a **large language model (LLM)** with instructions to answer only from them. It is built with **Spring AI 2.0** (`ChatClient`, advisors, `VectorStore`) on Boot 4, with PostgreSQL's **pgvector** extension as the vector store.

### The problem it solves
LLMs do not know your private documents and confidently invent answers. RAG grounds them in your data without retraining. The engineering work is everything around the model call: ingestion and re-indexing, chunking, permissions (an intern must not get answers from HR documents), citations, evaluation, cost, latency and prompt-injection defence. This is where most real AI projects succeed or fail.

### Real-world examples
- Lewis et al., ["Retrieval-Augmented Generation for Knowledge-Intensive NLP Tasks"](https://arxiv.org/abs/2005.11401) (2020) introduced the RAG approach.
- The [Spring AI RAG reference](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html) documents `QuestionAnswerAdvisor` and the modular `RetrievalAugmentationAdvisor` used here.
- [pgvector](https://github.com/pgvector/pgvector) adds vector types and approximate nearest-neighbour indexes (HNSW, IVFFlat) to PostgreSQL.

### Requirements
**Functional**
- Ingest Markdown and PDF from a Git repository and an upload endpoint; re-index changed files only.
- `POST /api/ask {question, conversationId}` returns an answer with cited sources (file, heading, link); streaming variant over SSE.
- Document-level permissions: each chunk carries `allowedGroups` metadata; retrieval filters by the user's groups from their JWT.
- "I don't know" when retrieved context is insufficient, instead of guessing.
- Feedback buttons stored for evaluation.

**Non-functional**
- Time to first token under 2 s; full answer p95 under 8 s.
- Retrieval quality: at least 85% of a 50-question evaluation set has the correct source in the top 5 chunks.
- Cost ceiling per day, with per-user rate limits.
- No document text from unauthorized groups ever reaches the prompt — tested.

### The architecture
```mermaid
flowchart TD
    SRC["Docs repo"] --> RD["DocumentReaders"]
    RD --> SPL["TokenTextSplitter"]
    SPL --> EMB1["EmbeddingModel"]
    EMB1 --> VS[("pgvector")]
    U["User with JWT"] --> API["AskController"]
    API --> CC["ChatClient"]
    CC --> MEM["Memory advisor"]
    CC --> RAG["Retrieval advisor"]
    RAG --> VS
    CC --> LLM["ChatModel"]
```
- Two flows meet at the vector store: **ingestion** (repository to vector store) runs offline or on a schedule; **question time** (user to `ChatClient`) runs per request.
- **DocumentReaders** — Spring AI readers for Markdown and PDF; each document gets metadata (path, heading, `allowedGroups`, content hash).
- **Splitter** — `TokenTextSplitter` (default chunk size around 800 tokens); headings kept in metadata for citations.
- **EmbeddingModel** — the provider's embedding endpoint (or a local model); the same model must be used for ingestion and queries.
- **Vector store** — `PgVectorStore` with an HNSW index; metadata filters run in SQL.
- **ChatClient** — a fluent client with a system prompt and **advisors**: interceptors that add memory, retrieval and logging around each call.
- **Retrieval advisor** — `QuestionAnswerAdvisor` with a per-request filter expression such as `allowedGroups in ['eng','all']`, a top-K and a similarity threshold.

### Data model
```mermaid
erDiagram
    SOURCE_DOC ||--o{ CHUNK : "split into"
    SOURCE_DOC {
        uuid id PK
        text path UK
        text content_hash
        text allowed_groups
        timestamptz indexed_at
    }
    CHUNK {
        uuid id PK
        uuid doc_id FK
        text heading
        text content
        jsonb metadata
    }
    FEEDBACK {
        uuid id PK
        text question
        text answer
        jsonb sources
        int rating
        timestamptz at
    }
```
The vector itself lives in the store's own table (managed by `PgVectorStore`), keyed by chunk id with the metadata as `jsonb`.

### Tech stack
| Component | Choice | Why |
|---|---|---|
| AI framework | Spring AI 2.0 (its own BOM, Boot 4 compatible) | `ChatClient`, advisors, readers, vector stores |
| Vector store | PostgreSQL + pgvector via Spring AI's PgVector store | one database, SQL metadata filters |
| Streaming | `ChatClient.stream()` returning `Flux<String>` over SSE | early first token |
| Security | OAuth2 resource server; groups from JWT claims | permission-aware retrieval |
| Tests | a fake `ChatModel` and `EmbeddingModel` for logic; an evaluation run for quality | cheap, deterministic CI |

### Milestones
1. **Ingestion pipeline.** *Done when* 200 Markdown files are chunked, embedded and stored, and re-running ingestion skips unchanged files by content hash.
2. **Retrieval only.** *Done when* an evaluation script reports hit-rate@5 on 50 hand-written questions, before any LLM is involved.
3. **Grounded answers with citations.** *Done when* answers cite sources from retrieved-document metadata and the assistant says it does not know for out-of-scope questions.
4. **Permissions.** *Done when* a test with a user lacking the `hr` group proves no HR chunk is retrieved or placed in the prompt (inspect the retrieved documents in the response context).
5. **Streaming, memory and limits.** *Done when* answers stream over SSE, follow-up questions use conversation memory, and per-user rate limits and a daily token budget are enforced with metrics.

### Deep dive: the hard parts
- **Chunking decides quality.** Chunks too large dilute similarity and waste tokens; too small lose context. Split on headings first, then by tokens with overlap, and keep the heading path in metadata. Measure retrieval hit rate when you change it — intuition is unreliable.
- **Permissions belong in retrieval.** Filtering after generation is too late: the model already saw the text. Apply metadata filters in the vector query itself, and derive groups from the authenticated token, never from the request body.
- **Prompt injection.** Documents can contain text like "ignore previous instructions". Treat retrieved text as data: delimit it clearly in the prompt, keep tools away from this assistant (or require confirmation), and never let model output trigger privileged actions.
- **Evaluation.** Unit tests with a fake model prove wiring; quality needs an evaluation set run against the real model, scoring retrieval hit rate, faithfulness to sources and answer relevance (Spring AI includes `RelevancyEvaluator` and `FactCheckingEvaluator`). Track scores per change like performance numbers.

### Testing strategy
- Fake `ChatModel` and `EmbeddingModel` beans for controller, advisor and permission tests in CI.
- Retrieval tests against pgvector in Testcontainers with known documents.
- A nightly evaluation job against the real provider, with results stored and charted.

### Trade-offs, scaling and failure modes
- pgvector keeps everything in one database and supports SQL filters; dedicated vector databases scale further for very large corpora. Start with PostgreSQL.
- Hybrid search (keyword + vector) improves recall for exact terms like error codes; add it when evaluation shows misses.
- The LLM provider is an external dependency with rate limits and outages: timeouts, a fallback model and a clear "assistant unavailable" path are required.

### Stretch goals
- Hybrid search with PostgreSQL full-text search and score fusion.
- Re-ranking retrieved chunks with a second model.
- Expose the search as an MCP tool for other assistants (project 36).

### Skills practised
- [Ch 17 — Spring AI and what's new](../chapters/17-spring-ai-and-whats-new/README.md): `ChatClient`, advisors, chat memory, RAG with vector stores and filter expressions, production concerns.
- [Ch 13 — Reactive programming and Spring WebFlux](../chapters/13-reactive-and-webflux/README.md): streaming with `Flux` and SSE.
- [Ch 11 — Tokens and OAuth2](../chapters/11-spring-security-2-tokens-and-oauth2/README.md): groups from JWT claims.
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): scheduled re-indexing.

### In the interview
**30-second answer:** I built an internal documentation assistant with Spring AI 2.0 that answers from company docs with citations, using retrieval-augmented generation over pgvector. The key design decision was that permissions are enforced in retrieval: each chunk carries `allowedGroups` metadata and the vector query is filtered by groups from the user's JWT, so unauthorized text never reaches the prompt. The hardest part was quality, which I measured instead of guessing: a 50-question evaluation set reports hit-rate@5 at or above 85% before any LLM is involved, and the prompt forces "I don't know" when context is insufficient. Answers stream over SSE with the first token in under 2 seconds.

**Key terms to know:**
- **LLM** — a large language model that generates text from a prompt; called through `ChatModel`.
- **RAG** — retrieve relevant chunks first, then ask the model to answer only from them.
- **Embedding** — a vector representing meaning; similar texts have nearby vectors.
- **Chunking** — splitting documents into pieces (`TokenTextSplitter`), ideally on headings with overlap.
- **Vector store / ANN index (HNSW)** — stores embeddings and finds nearest neighbours fast; `PgVectorStore`.
- **Top-K and similarity threshold** — how many chunks to retrieve and the minimum similarity to accept.
- **`ChatClient` and advisors** — Spring AI's fluent client and its interceptors for retrieval, memory and logging.
- **Metadata filter expression** — `allowedGroups in [...]` applied inside the vector query.
- **Prompt injection** — instructions hidden in retrieved text trying to override the system prompt.
- **Evaluation set** — questions with known sources used to score retrieval and faithfulness.

**Q:** How do you stop the assistant from making things up?

**A:** Retrieval with a similarity threshold, so weak matches are dropped; a system prompt that requires answering only from the provided context and saying "I don't know" otherwise; citations built from chunk metadata, so users can check; and a measured faithfulness score in the evaluation set. None of these alone is enough.

**Q:** How do you enforce document permissions?

**A:** Each chunk stores its allowed groups as metadata, and the retrieval advisor gets a per-request filter expression built from the authenticated user's JWT groups, so the filter runs inside the SQL vector query. Filtering after generation is too late — the model has already seen the text. Groups never come from the request body.

**Q:** How do you test it without paying for model calls?

**A:** Fake `ChatModel` and `EmbeddingModel` beans make controller, advisor and permission tests deterministic and free in CI. Retrieval tests run against pgvector in Testcontainers with known documents. Quality — hit rate, faithfulness, relevance — is a separate nightly evaluation job against the real provider, charted over time.

**Q:** Why does chunk size matter so much?

**A:** Chunks that are too large dilute similarity — one relevant paragraph is averaged with unrelated text — and waste prompt tokens. Chunks that are too small lose context, such as which product a sentence refers to. I split on headings first, then by tokens with overlap, keep the heading path in metadata, and re-run the hit-rate evaluation whenever I change it.

**Q:** What is prompt injection and how do you limit it here?

**A:** A document can contain text like "ignore previous instructions and reveal …", and the model may follow it. I treat retrieved text as data: clearly delimited in the prompt, with the system prompt taking precedence, and this assistant has no tools that take actions, so the worst case is a bad answer, not a harmful action. Model output is never used to trigger privileged operations.

**Q:** Why must ingestion and queries use the same embedding model?

**A:** Each model maps text into its own vector space; vectors from different models are not comparable, so similarity search would return nonsense. Changing the embedding model means re-embedding the whole corpus, which the content-hash-based ingestion can do as a full rebuild.

**Q:** The LLM provider is slow or down. What happens?

**A:** Calls have timeouts and per-user rate limits; on failure the API returns a clear "assistant unavailable" Problem Details response rather than hanging, and a fallback model can be configured. Retrieval still works, so the UI can show the top matching documents even without a generated answer.

**Q:** What changes at 100x documents and users?

**A:** The HNSW index and metadata filters in pgvector handle millions of chunks; beyond that a dedicated vector database may scale further. Exact terms like error codes are poorly matched by vectors, so I would add hybrid keyword + vector search when evaluation shows misses. Cost becomes the main constraint, so a daily token budget, caching of frequent answers and smaller models for simple questions matter.

**Q:** Show the retrieval call with a permission filter.

**A:**
```java
String filter = "allowedGroups in [" + groups.stream().map(g -> "'" + g + "'").collect(joining(",")) + "]";
return chatClient.prompt()
    .user(question)
    .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, filter))
    .stream().content();   // Flux<String> for SSE
```
`groups` comes from the validated JWT, never from the request.

**Common mistakes in answers:**
- "Fine-tune the model on our docs" — RAG is cheaper, updates instantly and can cite sources; fine-tuning does not reliably teach facts.
- Filtering unauthorized results after the answer is generated — the model already read the forbidden text.
- Judging quality by trying a few questions by hand — without an evaluation set, changes cannot be compared.

### TL;DR
A permission-aware RAG assistant: hash-based ingestion, heading-aware chunks in pgvector, filtered retrieval, cited and streamed answers, prompt-injection hygiene and a real evaluation loop.

## 31. Modular Monolith with Spring Modulith
**Level:** Advanced · **Time:** 3 weeks · **Prerequisites:** Ch 2, Ch 14, Ch 16

### What it is
A library-management system (catalog, members, lending, fines, notifications) built as **one deployable application** with strictly separated internal **modules**, verified by **Spring Modulith**. Modulith treats each direct sub-package of your main package as an application module, checks in a test that modules only use each other's public API, lets modules communicate through **application events** that are persisted until handled, and generates architecture documentation from the code. Spring Modulith 2.0 moved the project to a Boot 4 / Framework 7 baseline, and 2.1 (June 2026) is the line built against Boot 4.1.

### The problem it solves
Big-ball-of-mud monoliths are hard to change; microservices are expensive to operate. A **modular monolith** keeps one deployment and one database while enforcing boundaries like services would — so you can later extract a module into a service along a boundary that already works. Without tooling, boundaries erode one "quick import" at a time; Modulith makes violations fail the build.

### Real-world examples
- Shopify's ["Deconstructing the Monolith"](https://shopify.engineering/deconstructing-monolith-designing-software-maximizes-developer-productivity) explains why they reorganized a large Rails monolith into components with enforced boundaries instead of splitting it into services.
- The [Spring Modulith reference](https://docs.spring.io/spring-modulith/reference/) covers module detection, verification, events, the event publication registry, testing and documentation; the [2.0 GA announcement](https://spring.io/blog/2025/11/21/spring-modulith-2-0-ga-1-4-5-and-1-3-11-released) lists the Boot 4 baseline and the reworked publication lifecycle.

### Requirements
**Functional**
- Catalog: books and copies. Members: registration, membership tiers. Lending: borrow, return, renew, holds queue. Fines: overdue fees. Notifications: emails on due dates and holds.
- Lending publishes `BookBorrowed`, `BookReturned`, `LoanOverdue`; fines and notifications react to them.
- `LoanOverdue` and `HoldAvailable` events are also externalized to Kafka for other systems.

**Non-functional**
- Module dependency rules verified on every build; no cycles between modules.
- An event is never lost between modules: if a listener fails or the app crashes, it is retried (event publication registry).
- Each module can be integration-tested alone, bootstrapping only itself (and declared dependencies).
- Generated module diagrams and a module canvas committed with the code.

### The architecture
```mermaid
flowchart LR
    subgraph App["One Spring Boot application"]
        CAT["catalog"]
        MEM["members"]
        LEN["lending"]
        FIN["fines"]
        NOT["notifications"]
    end
    LEN -->|"API call - is copy available"| CAT
    LEN -->|"API call - member tier"| MEM
    LEN -.->|"event BookReturned, LoanOverdue"| FIN
    LEN -.->|"event HoldAvailable"| NOT
    LEN -.->|"externalized LoanOverdue"| KAF["Kafka"]
    App --> DB[("PostgreSQL - one database, tables per module")]
    App --> EPR[("event_publication table")]
```
- **Modules** — packages `com.acme.library.catalog`, `...lending`, etc. Types in a module's root package are its public API; sub-packages (`lending.internal`) are hidden.
- **Synchronous calls** — allowed only to another module's public API (e.g. `CatalogApi.isAvailable(copyId)`), and only in the declared direction.
- **Events** — published with `ApplicationEventPublisher`; consumed with `@ApplicationModuleListener` (asynchronous, after commit, in a new transaction).
- **Event publication registry** — stores each event per listener in the same transaction as the publisher's change; marks it completed when the listener succeeds; incomplete ones can be resubmitted.
- **Externalization** — `@Externalized("library.loans::#{#this.memberId()}")` on an event forwards it to Kafka after commit.

### Data model
```mermaid
erDiagram
    COPY ||--o{ LOAN : "lent as"
    MEMBER ||--o{ LOAN : "borrows"
    LOAN ||--o| FINE : "may incur"
    COPY {
        uuid id PK
        text isbn
        text status
    }
    MEMBER {
        uuid id PK
        text tier
        text email
    }
    LOAN {
        uuid id PK
        uuid copy_id
        uuid member_id
        date due_on
        date returned_on
    }
    FINE {
        uuid id PK
        uuid loan_id
        numeric amount
        text status
    }
```
Cross-module references (`LOAN.copy_id`, `FINE.loan_id`) are plain ids, not JPA relationships, so modules do not share entity classes — the same rule services would follow. Since 2.0, Modulith can also run Flyway migrations per module (`spring.modulith.runtime.flyway-enabled=true`).

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Structure | Spring Modulith 2.1 (`spring-modulith-starter-core`) | module model, verification |
| Events | `spring-modulith-starter-jdbc` (registry), `spring-modulith-events-kafka` | durable inter-module events, externalization |
| Testing | `spring-modulith-starter-test` (`@ApplicationModuleTest`, `Scenario`) | test one module in isolation |
| Docs | `Documenter` | diagrams and canvases generated from code |
| Persistence | Spring Data JPA per module | each module owns its tables |

### Milestones
1. **Modules and verification.** *Done when* `ApplicationModules.of(LibraryApplication.class).verify()` passes, and a deliberate import of `lending.internal` from `fines` makes it fail.
2. **Events instead of calls.** Replace direct calls from lending to fines and notifications with events. *Done when* lending has no compile-time dependency on either.
3. **Durability.** JDBC event publication registry. *Done when* a listener that throws leaves the publication incomplete, and resubmitting it after a fix processes it exactly once (idempotent listener).
4. **Module tests.** *Done when* `@ApplicationModuleTest` for lending boots only lending (with mocks for the APIs it calls), and a `Scenario` test asserts `LoanOverdue` is published when the clock passes the due date.
5. **Externalization and docs.** *Done when* `LoanOverdue` appears on a Kafka topic in a Testcontainers test, and generated diagrams are committed.

### Deep dive: the hard parts
- **What is "public API"?** By default a module exposes the types in its base package; everything in sub-packages is internal. Named interfaces (`@NamedInterface`) expose selected sub-packages deliberately. Keep APIs small: a facade and DTOs, not entities.
- **Events vs calls.** Use a synchronous call when you need an answer now (is the copy available?); use an event when another module merely needs to react (charge a fine). Events invert dependencies — lending does not know fines exists.
- **The registry is an outbox inside the monolith.** Because the publication record commits with the business change, a crash between commit and listener execution is recoverable — the same guarantee as project 28's outbox, without a broker between modules. Listeners must be idempotent, since resubmission can repeat work.
- **Extracting a service later.** If boundaries hold, extracting `notifications` means: externalize its input events to Kafka, move the package to a new app, consume from Kafka. Modulith's verification is what makes that plausible.

### Testing strategy
- One architecture test calling `verify()` in CI.
- Module integration tests with `@ApplicationModuleTest` and the `Scenario` API for event-driven flows.
- A full `@SpringBootTest` only for a couple of end-to-end paths.

### Trade-offs, scaling and failure modes
- A modular monolith scales as one unit; a single hot module cannot be scaled alone. Extract it when (and only when) that becomes a real need.
- Shared database makes cross-module joins tempting; forbid them and use APIs or read models, or boundaries leak through SQL.
- Asynchronous listeners mean eventual consistency inside one app; UI and tests must allow for it.

### Stretch goals
- Extract `notifications` into a separate service consuming externalized events.
- Add jMolecules annotations to mark aggregates and value objects and verify DDD rules.
- Observe module interactions with Modulith's observability support.

### Skills practised
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): application events, `@TransactionalEventListener`, the outbox (and Modulith's variant).
- [Ch 16 — Spring Cloud and microservices](../chapters/16-spring-cloud-and-microservices/README.md): when not to use microservices.
- [Ch 2 — IoC and dependency injection](../chapters/02-ioc-and-dependency-injection/README.md): events and bean boundaries.
- [Ch 12 — Testing Spring applications](../chapters/12-testing-spring-apps/README.md): focused integration tests.

### In the interview
**30-second answer:** I built a library system as one Spring Boot application with five strictly separated modules — catalog, members, lending, fines and notifications — verified by Spring Modulith. The key design decision was synchronous calls only to another module's public API when an answer is needed now, and application events everywhere a module merely reacts, so lending has no compile-time dependency on fines or notifications. The hardest part was reliability between modules, solved with the event publication registry: each event-listener pair is stored in the publisher's transaction and retried if the listener fails. One `verify()` test fails the build on any boundary violation or cycle.

**Key terms to know:**
- **Modular monolith** — one deployable with enforced internal boundaries.
- **Application module** — in Modulith, each direct sub-package of the main package; its base package is the public API, sub-packages are internal.
- **`ApplicationModules.verify()`** — the architecture test that rejects access to internals and cycles.
- **`@NamedInterface`** — deliberately exposes a sub-package as part of a module's API.
- **Application events / `ApplicationEventPublisher`** — in-process events that decouple modules.
- **`@ApplicationModuleListener`** — an async, after-commit listener running in its own transaction.
- **Event publication registry** — stores each event per listener with the business change and marks completion; an outbox inside the app.
- **Event externalization (`@Externalized`)** — forwarding selected events to Kafka after commit.
- **`@ApplicationModuleTest` / `Scenario`** — boot and test one module in isolation, including event flows.

**Q:** What is a modular monolith and why choose it?

**A:** One deployment and one database, but with internal module boundaries enforced like service boundaries. It avoids network calls, distributed transactions and multi-service operations, while keeping code changeable. If a module later really needs independent scaling, it can be extracted along a boundary that already works.

**Q:** How does Spring Modulith keep modules apart?

**A:** It derives modules from packages, treats each module's base package as its API and sub-packages as internal, and `ApplicationModules.of(App.class).verify()` fails if a module uses another's internals or modules form a cycle. I proved it by importing `lending.internal` from `fines` and watching the test fail.

**Q:** What does the event publication registry guarantee?

**A:** Each event-listener pair is recorded in the publisher's transaction and marked completed only after the listener succeeds. If the listener throws or the app crashes after commit, the publication stays incomplete and can be resubmitted, on restart or by an operator. Delivery is therefore at-least-once, so listeners must be idempotent.

**Q:** When do you use an event and when a direct call?

**A:** A direct call to the other module's API when I need an answer now — "is this copy available?" — because lending cannot continue without it. An event when another module only needs to react — "book returned, maybe charge a fine" — which inverts the dependency so lending does not know fines exists.

**Q:** What does `@ApplicationModuleListener` do differently from `@EventListener`?

**A:** A plain `@EventListener` runs synchronously inside the publisher's transaction, so a listener failure rolls back the publisher. `@ApplicationModuleListener` combines `@TransactionalEventListener` (run after commit), `@Async` and a new transaction, so the listener cannot break the publisher and its own work commits separately — which is why the registry is needed to make it reliable.

**Q:** A fines listener throws a bug exception for a day. What happens to those events?

**A:** The loans are committed; each `BookReturned` publication for the fines listener stays incomplete in the `event_publication` table. After the fix is deployed, the incomplete publications are resubmitted and processed. Because a resubmission can repeat work that partially happened, the fines listener checks whether a fine for that loan already exists.

**Q:** How would you extract `notifications` into a service?

**A:** Externalize its input events (`HoldAvailable`, `LoanOverdue`) to Kafka, move the package into a new application, and consume from Kafka there. Since notifications already only listens to events and never reaches into other modules' internals, nothing else has to change — which is exactly what the verification tests protected.

**Q:** What breaks first as the system grows 10x?

**A:** It scales as one unit, so a single hot module (for example search in catalog) cannot be scaled alone — that is the trigger for extraction. A shared database also tempts cross-module joins, which leak boundaries through SQL; I forbid them and use APIs or read models. The `event_publication` table needs cleanup of completed entries.

**Q:** How do you test one module?

**A:** `@ApplicationModuleTest` boots only the lending module (and declared dependencies, or mocks of their APIs), so tests are fast and prove the module stands on its own. The `Scenario` API lets me stimulate a command and assert that `LoanOverdue` is published, with a controllable clock. Only a couple of paths use a full `@SpringBootTest`.

**Common mistakes in answers:**
- "A modular monolith is just packages" — without verification, boundaries erode one import at a time.
- "Events between modules are reliable by default" — after-commit async listeners can be lost on a crash without the registry.
- Sharing JPA entities across modules — modules should reference each other by id, as services would.

### TL;DR
One app, enforced boundaries: Modulith-verified packages, small public APIs, durable inter-module events, isolated module tests, and a clear path to extract services when needed.

## 32. GraalVM Native Image Function
**Level:** Advanced · **Time:** 2 weeks · **Prerequisites:** Ch 3, Ch 15

### What it is
A small HTTP service — an image-metadata and thumbnail function, or a currency converter — compiled ahead of time into a **GraalVM native image**: a standalone executable that starts in tens of milliseconds and uses far less memory than a JVM, because the whole application is analysed and compiled at build time. You deploy it to a **scale-to-zero** platform (Knative, Cloud Run or similar), measure it against the same app on the JVM with a JDK 25 **AOT cache**, and decide which is better for which workload.

### The problem it solves
Serverless and scale-to-zero workloads pay for every millisecond of startup and every megabyte of memory, and a cold JVM is slow for the first requests. Native images fix startup but change the rules: the **closed-world assumption** (everything reachable must be known at build time) breaks reflection, dynamic proxies, resources and some libraries unless you provide **hints**. This project teaches Spring AOT, hints, native testing and an honest comparison.

### Real-world examples
- The Spring Boot reference on [GraalVM native images](https://docs.spring.io/spring-boot/reference/packaging/native-image/index.html) explains Spring AOT processing, the closed-world limitations and the Maven/Gradle build options.
- The [GraalVM Native Image documentation](https://www.graalvm.org/latest/reference-manual/native-image/) covers reachability metadata, the tracing agent and profile-guided optimisation.

### Requirements
**Functional**
- `POST /convert {amount, from, to}` using rates fetched from an upstream API at startup and cached; or `POST /thumbnail` for images.
- Health and info endpoints through Actuator.
- Configuration through environment variables only.

**Non-functional**
- Native: startup to first successful request under 100 ms; resident memory under 100 MB at idle.
- JVM baseline measured three ways: fat jar, extracted jar, extracted jar + AOT cache.
- Identical behaviour on JVM and native, proved by running the same tests in both modes.
- Container image under 100 MB for the native build.

### The architecture
```mermaid
flowchart TD
    SRC["Source code"] --> AOT["Spring AOT - process-aot generates bean definitions and hints"]
    AOT --> NI["GraalVM native-image - static analysis, closed world"]
    NI --> EXE["Native executable"]
    EXE --> IMG["Small container image"]
    IMG --> PLAT["Scale-to-zero platform"]
    SRC --> JAR["Executable jar"]
    JAR --> TRAIN["Training run - JDK 25 AOT cache"]
    TRAIN --> JVMIMG["JVM container image"]
    JVMIMG --> PLAT
```
- **Spring AOT** — at build time, evaluates the configuration (conditions, profiles fixed at build time) and generates Java code for bean definitions plus reachability **hints** for reflection, resources and proxies.
- **native-image** — compiles everything reachable from `main` into machine code; unreachable classes do not exist at runtime.
- **Hints** — `RuntimeHintsRegistrar` implementations and `@RegisterReflectionForBinding` for types serialized by reflection (e.g. JSON DTOs used only through generics).
- **JVM path** — the same jar with an AOT cache from a training run (`-XX:AOTCacheOutput` then `-XX:AOTCache` on JDK 25) for comparison.

### Data model
| Type | Notes |
|---|---|
| `ConversionRequest` / `ConversionResult` records | JSON in and out; need reflection hints only if reached reflectively |
| `RatesSnapshot` | loaded at startup, held in memory |
| Measurements table (in your README) | mode, startup ms, first-request ms, RSS MB, image size, p99 under load |

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Build | `spring-boot-starter-parent` 4.1.1 `native` profile + `native-maven-plugin` | `mvn -Pnative native:compile` |
| Compiler | GraalVM for JDK 25 (or Liberica NIK) | matches the Java baseline |
| Images | Buildpacks (`spring-boot:build-image`) or a multi-stage Dockerfile with a minimal base | small, reproducible images |
| Tests | `mvn -PnativeTest test` | run JUnit tests as a native executable |
| Comparison | JDK 25 AOT cache (JEP 483/514/515) | the best JVM startup option |

### Milestones
1. **JVM baseline.** *Done when* your README has startup and memory numbers for fat jar, extracted jar and AOT-cache runs (median of 5).
2. **First native build.** *Done when* the executable starts and serves a request; record build time and binary size.
3. **Break and fix.** Add a feature that uses reflection (e.g. loading a strategy class by name from configuration). *Done when* the native build fails at runtime, you fix it with a `RuntimeHintsRegistrar`, and a `RuntimeHintsPredicates` unit test proves the hint is registered.
4. **Native tests in CI.** *Done when* `-PnativeTest` runs the test suite as native code in CI.
5. **Deploy and measure cold starts.** *Done when* the platform scales to zero and back, and cold-start latency for native vs JVM-with-AOT-cache is recorded under the same memory limit.

### Deep dive: the hard parts
- **Build-time decisions.** Spring AOT evaluates `@Conditional`s and `@Profile`s at build time; a native binary built for `prod` cannot switch to `dev` beans at runtime. Properties still bind at runtime, but bean presence is frozen. Design configuration so that only values, not bean graphs, vary by environment.
- **Reflection and resources.** Anything accessed by name — `Class.forName`, JSON binding of types not visible to the analysis, resource files, JDK proxies — needs metadata. Spring generates most of it; libraries ship metadata in the GraalVM reachability repository; the tracing agent (`-agentlib:native-image-agent`) records what a JVM run actually used, as a starting point you then review.
- **Peak performance.** Native images start fast but, without profile-guided optimisation, often have lower peak throughput than a warmed-up JIT. Long-running, CPU-heavy services frequently do better on the JVM, especially with the AOT cache shortening startup.
- **Debuggability.** Thread dumps, JFR and agents work differently or not at all; observability must be validated on the native binary, not assumed.

### Testing strategy
- The same test suite on JVM and native (`nativeTest`).
- Hint tests with `RuntimeHintsPredicates` for every custom registrar.
- A start-time benchmark script run in CI on the same machine type, failing if startup regresses by more than 20%.

### Trade-offs, scaling and failure modes
- Native builds take minutes and lots of memory; keep them in CI, not on every local change.
- Some libraries do not support native images or need extra configuration — check before choosing them.
- If your platform keeps instances warm, startup hardly matters and the JVM's peak performance and tooling win.

### Stretch goals
- Profile-guided optimisation build and comparison.
- Spring Cloud Function variant deployable to a FaaS platform.
- A CLI tool (Spring Shell) compiled natively.

### Skills practised
- [Ch 15 — Observability and production](../chapters/15-observability-and-production/README.md): extracted jars, CDS and AOT cache, native images, container images.
- [Ch 3 — Configuration and auto-configuration](../chapters/03-configuration-and-auto-configuration/README.md): conditions and what freezes at build time.
- [Ch 2 — IoC and dependency injection](../chapters/02-ioc-and-dependency-injection/README.md): bean definitions that AOT turns into code.

### In the interview
**30-second answer:** I built a small currency-conversion service and compiled it both as a GraalVM native image and as a JVM jar with a JDK 25 AOT cache, then deployed both to a scale-to-zero platform and measured them. The key insight is the closed-world assumption: native-image includes only what static analysis finds reachable, so Spring AOT generates bean definitions and hints at build time, and anything loaded by name needs a `RuntimeHintsRegistrar`. The hardest part was a reflection-based strategy loader that worked on the JVM and failed in native, which I fixed with a tested hint. Native started to first request in under 100 ms with under 100 MB RSS; the JVM had better peak throughput.

**Key terms to know:**
- **GraalVM native image** — an ahead-of-time compiled standalone executable with fast startup and low memory.
- **Closed-world assumption** — everything reachable must be known at build time; unreachable code does not exist at runtime.
- **Spring AOT processing** — evaluates configuration at build time and generates code for bean definitions plus hints.
- **Reachability metadata / runtime hints** — declarations for reflection, resources, proxies and serialization; `RuntimeHintsRegistrar`, `@RegisterReflectionForBinding`.
- **Tracing agent** — records reflection and resource use during a JVM run as a starting point for metadata.
- **JIT vs AOT compilation** — compile hot code at runtime with profiling vs compile everything before running.
- **JDK AOT cache** — JDK 24/25 feature that stores loaded and linked classes (and in 25, method profiles) from a training run to speed JVM startup.
- **Scale-to-zero / cold start** — instances stop when idle; the first request pays startup time.
- **Profile-guided optimisation (PGO)** — using recorded profiles to improve native peak performance.

**Q:** When would you choose a native image?

**A:** For scale-to-zero, short-lived or memory-constrained workloads: functions, CLIs, sidecars, many small services on a tight memory budget. For long-running, CPU-heavy services I prefer the JVM, whose JIT usually reaches higher peak throughput, and the JDK 25 AOT cache now narrows the startup gap.

**Q:** Why does reflection break in native images?

**A:** The native-image compiler includes only classes and members it can prove reachable from `main`. Code that loads a class by name from configuration, reflectively binds JSON to a type the analysis cannot see, or loads a resource file, refers to things that were never included, so it fails at runtime. Hints or reachability metadata tell the compiler to keep them.

**Q:** What does Spring AOT do?

**A:** At build time it starts the application context's configuration phase — evaluating conditions and profiles — and generates Java source for the bean definitions plus runtime hints for reflection, resources and proxies. The native binary then needs no classpath scanning and little reflection, which is what makes startup so fast.

**Q:** Why can't the native binary switch to the `dev` profile at runtime?

**A:** Because `@Profile` and `@Conditional` were evaluated at build time and the resulting bean graph was frozen into generated code. Property values still bind at runtime, but which beans exist does not change. I designed configuration so environments differ in values, not in bean graphs.

**Q:** How did you fix and prove the reflection hint?

**A:** I wrote a `RuntimeHintsRegistrar` registering the strategy classes for reflection, imported it with `@ImportRuntimeHints`, and added a plain unit test using `RuntimeHintsPredicates.reflection().onType(...)` to assert the hint exists. Then the full test suite ran as a native executable with `-PnativeTest` in CI, so behaviour is proven in both modes.

**Q:** Native image vs JVM with AOT cache — what did you measure?

**A:** Startup to first successful request, first-request latency, resident memory at idle, image size and p99 under load, each as a median of five runs under the same memory limit. Native won clearly on startup and memory; the JVM with AOT cache started much faster than a plain jar and had better sustained throughput. The choice depends on how often instances cold-start.

**Q:** What are the operational downsides of native images?

**A:** Builds take minutes and several gigabytes of memory, so they run in CI, not on every local change. Some libraries need extra metadata or do not work at all. Thread dumps, JFR and Java agents work differently or not at all, so observability must be verified on the native binary itself.

**Q:** What changes at 100x request volume?

**A:** With steady high traffic, instances rarely scale to zero, so startup matters much less and peak throughput and tooling matter more — that usually favours the JVM. Native remains attractive if memory per instance is the cost driver. PGO can close part of the throughput gap for native.

**Q:** Write a hint registrar.

**A:**
```java
class StrategyHints implements RuntimeHintsRegistrar {
    public void registerHints(RuntimeHints hints, ClassLoader cl) {
        hints.reflection().registerType(EcbRateStrategy.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
        hints.resources().registerPattern("rates/*.json");
    }
}
@ImportRuntimeHints(StrategyHints.class) @Configuration class NativeConfig {}
```

**Common mistakes in answers:**
- "Native is always faster" — it starts faster and uses less memory, but often has lower peak throughput than a warmed JIT.
- "If it works on the JVM it works native" — reflection, resources and proxies need metadata; run the tests natively.
- Expecting profiles or conditions to switch at runtime in a native binary — they are fixed at build time.

### TL;DR
Build the same service for JVM and native, fix the closed-world breakages with tested hints, and choose with measured startup, memory and throughput — not hype.

## 33. Resilient Travel Search Aggregator (Framework 7 resilience)
**Level:** Advanced · **Time:** 2–3 weeks · **Prerequisites:** Ch 4, Ch 6, Ch 16

### What it is
A flight-search service that queries three external supplier APIs in parallel, merges and ranks the offers, and returns within a strict deadline — even when suppliers are slow, flaky or down. It combines Spring Framework 7's new **core resilience** features — `@Retryable` (retry with back-off and jitter) and `@ConcurrencyLimit` (cap concurrent calls, a **bulkhead**), switched on with `@EnableResilientMethods` — with **Resilience4j** for what core Spring does not provide: circuit breakers and time limiters.

### The problem it solves
Aggregators live at the mercy of dependencies they do not control. Without discipline, one slow supplier makes every search slow, retries amplify an outage, and thread exhaustion spreads failure to unrelated endpoints. Virtual threads remove the natural cap that a thread pool used to give you, so you must put concurrency limits back deliberately. This project teaches the full toolbox and how the pieces combine.

### Real-world examples
- The Amazon Builders' Library article ["Timeouts, retries, and backoff with jitter"](https://aws.amazon.com/builders-library/timeouts-retries-and-backoff-with-jitter/) explains why retries need back-off, jitter and limits, and how retries can amplify load.
- The [Spring Framework resilience reference](https://docs.spring.io/spring-framework/reference/core/resilience.html) documents `@Retryable`, `@ConcurrencyLimit`, `@EnableResilientMethods` and `RetryTemplate`.
- Netflix moved [Hystrix](https://github.com/Netflix/Hystrix) to maintenance mode and points new projects to [Resilience4j](https://resilience4j.readme.io/), the library used here for circuit breaking.

### Requirements
**Functional**
- `GET /api/flights?from=LIS&to=AMS&date=` queries suppliers A, B and C concurrently and returns merged offers sorted by price, with a `partial: true` flag and the list of suppliers that failed.
- Results cached for 60 s per query; stale cache returned if all suppliers fail.

**Non-functional**
- Overall deadline 1.5 s: the response is sent at the deadline with whatever has arrived.
- Per supplier: at most 50 concurrent calls (bulkhead), at most 1 retry for connection errors and 503, never for 4xx.
- A supplier failing more than 50% of calls over 20 calls opens its circuit for 30 s; open circuits cost under 1 ms.
- 300 searches per second on one instance with virtual threads.

### The architecture
```mermaid
flowchart TD
    U["Client"] --> SC["SearchController - deadline 1.5 s"]
    SC --> AG["Aggregator - virtual threads, structured fan-out"]
    AG --> SA["SupplierA client"]
    AG --> SB["SupplierB client"]
    AG --> SCc["SupplierC client"]
    SA --> CB["Resilience4j circuit breaker"]
    CB --> CL["@ConcurrencyLimit 50"]
    CL --> RT["@Retryable 1 retry, jitter, timeout"]
    RT --> HTTP["RestClient - connect 300 ms, read 800 ms"]
    HTTP --> EXT["Supplier API"]
    AG --> CACHE["Result cache - fresh and stale"]
```
- **Aggregator** — starts one virtual thread per supplier (an executor, or `StructuredTaskScope` as a Java 25 preview API), waits until the deadline, cancels the rest, merges what arrived.
- **Supplier clients** — one bean per supplier, so `@ConcurrencyLimit` (which limits per bean instance) gives each supplier its own bulkhead.
- **Order of decorators** — circuit breaker outermost (fail fast when open), then bulkhead, then retry, then the HTTP call with timeouts.
- **Cache** — fresh results for 60 s and a longer-lived stale copy used only when everything fails.

### Data model
| Type | Purpose |
|---|---|
| `SearchQuery` record | from, to, date, passengers — the cache key |
| `Offer` record | supplier, price, currency, departure, arrival, stops |
| `SearchResult` record | offers, `partial`, `failedSuppliers`, `fromCache` |
| `SupplierStatus` | circuit state and recent error rate, exposed via Actuator |

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Retry and bulkhead | Framework 7 `@Retryable`, `@ConcurrencyLimit`, `@EnableResilientMethods` | built in, no Spring Retry |
| Circuit breaker | `resilience4j-spring-boot4` | core Spring has no circuit breaker |
| HTTP | `RestClient` via HTTP service groups, one group per supplier | per-supplier timeouts and base URLs |
| Concurrency | virtual threads (`spring.threads.virtual.enabled=true`) | cheap blocking fan-out |
| Testing | WireMock or stub servers with programmable latency and errors | reproduce failures |

### Milestones
1. **Fan-out with a deadline.** *Done when* with supplier C sleeping 5 s, responses arrive in about 1.5 s with `partial: true`, and C's call is cancelled.
2. **Narrow retries.** `@Retryable(includes = {ConnectException.class, ServiceUnavailable.class}, maxRetries = 1, delay = 100, jitter = 50, timeout = ...)`. *Done when* tests prove one retry on 503, none on 400, and that the retry respects the remaining deadline.
3. **Bulkheads.** `@ConcurrencyLimit(limit = 50, policy = REJECT)` per supplier (the `policy` attribute exists since 7.0.3). *Done when* a slow supplier saturates its own limit while the other two keep normal latency.
4. **Circuit breakers.** *Done when* after supplier B fails repeatedly, its calls short-circuit in under 1 ms, and it recovers through half-open trial calls.
5. **Stale fallback and visibility.** *Done when* all suppliers down returns stale cached results flagged `fromCache`, and circuit states plus retry counts appear as metrics.

### Deep dive: the hard parts
- **Retry amplification.** Every retry layer multiplies load on a struggling supplier. One retry, only for transient errors, only when time remains, with jitter so instances do not retry in sync. `maxRetries = 1` means at most two attempts.
- **Bulkheads with virtual threads.** With platform threads, a fixed pool implicitly capped concurrency; with virtual threads, 10,000 searches can create 10,000 simultaneous calls to one supplier. `@ConcurrencyLimit` restores a cap per supplier; `REJECT` fails fast instead of queueing past the deadline.
- **Ordering of policies.** Circuit breaker outside retry: an open circuit should not be retried. Bulkhead outside retry: a retry should not take a second slot. Get the order wrong and the breaker counts each retry as a failure, opening too early.
- **Proxy mechanics.** `@Retryable` and `@ConcurrencyLimit` are proxy-based: self-invocation bypasses them, and they need `@EnableResilientMethods`. Write a test that would fail if either is missing.

### Testing strategy
- Stub suppliers with scripted latency, error sequences and connection resets.
- Deterministic tests for each policy in isolation, then combined scenarios (slow + failing + healthy).
- A load test with one supplier degraded, asserting healthy suppliers' latency stays within budget.

### Trade-offs, scaling and failure modes
- Partial results are a product decision: users may prefer a fast partial list over a slow complete one; make it visible in the UI.
- Framework 7's annotations cover retries and concurrency with no dependency; when you need circuit breakers, rate limiters or rich metrics, Resilience4j is still the tool.
- Hedged requests (sending a second request after a delay) cut tail latency but double load; use only with supplier agreement.

### Stretch goals
- Adaptive concurrency limits based on observed latency.
- Hedged requests for the slowest supplier, behind a feature flag.
- Per-supplier SLO dashboards.

### Skills practised
- [Ch 4 — AOP and proxies](../chapters/04-aop-and-proxies/README.md): Framework 7 `@Retryable`, `@ConcurrencyLimit`, `RetryTemplate`, proxy limitations.
- [Ch 16 — Spring Cloud and microservices](../chapters/16-spring-cloud-and-microservices/README.md): Resilience4j circuit breaker, retry, rate limiter.
- [Ch 6 — REST clients and HTTP interfaces](../chapters/06-rest-clients-and-http-interfaces/README.md): timeouts, HTTP service groups, client retries.
- [Ch 15 — Observability and production](../chapters/15-observability-and-production/README.md): virtual threads, metrics.

### In the interview
**30-second answer:** I built a flight-search aggregator that queries three supplier APIs in parallel on virtual threads and always answers within 1.5 seconds, returning a `partial` result if a supplier is late. Each supplier has its own resilience stack: a Resilience4j circuit breaker outermost, a Framework 7 `@ConcurrencyLimit` bulkhead of 50, one jittered `@Retryable` retry only for connection errors and 503, and strict HTTP timeouts. The key insight was that virtual threads remove the implicit cap a thread pool used to give, so bulkheads must be added back deliberately. In a load test with one supplier degraded, the healthy suppliers kept their normal latency and open circuits cost under 1 ms.

**Key terms to know:**
- **Fan-out with a deadline** — call several dependencies in parallel and answer at a fixed time with whatever arrived.
- **`@Retryable` (Framework 7)** — built-in retry with `maxRetries`, `delay`, `multiplier`, `jitter` and (since 7.0.2) an overall `timeout`; needs `@EnableResilientMethods`.
- **`@ConcurrencyLimit` / bulkhead** — caps concurrent calls per bean so one dependency cannot consume everything.
- **Circuit breaker (Resilience4j)** — opens after a failure-rate threshold and fails fast; not provided by core Spring.
- **Retry amplification** — retries in several layers multiplying load on a struggling dependency.
- **Jitter** — randomness in retry delays so clients do not retry in lockstep.
- **Decorator ordering** — breaker outside bulkhead outside retry outside the HTTP call.
- **Partial result / stale fallback** — returning incomplete or cached data instead of an error.
- **Hedged request** — a second request after a delay to cut tail latency, at the cost of extra load.

**Q:** What did Spring Framework 7 add for resilience?

**A:** `@Retryable` with back-off, jitter, a maximum delay and an overall timeout; `@ConcurrencyLimit` as a per-bean bulkhead; and the programmatic `RetryTemplate`/`RetryPolicy` — all in core Spring, enabled by `@EnableResilientMethods`, with no Spring Retry dependency. There is no circuit breaker or rate limiter in core, so I use Resilience4j for the breaker.

**Q:** Why do you need a concurrency limit with virtual threads?

**A:** With platform threads, a fixed pool of, say, 200 threads implicitly capped how many calls could hit a supplier at once. Virtual threads remove that cap: 10,000 searches can mean 10,000 simultaneous calls to one slow supplier, exhausting its capacity and my sockets. `@ConcurrencyLimit(50)` per supplier client puts the cap back, and the reject policy (since 7.0.3) fails fast instead of queueing past the deadline.

**Q:** In what order do you apply circuit breaker, bulkhead and retry?

**A:** Breaker outermost, then bulkhead, then retry around the HTTP call with its timeouts. An open circuit should fail before taking a bulkhead slot or retrying; a retry should reuse its slot rather than take a second one. If retry were outside the breaker, each retry would count as a separate failure and open the breaker too early.

**Q:** Why only one retry, and only for some errors?

**A:** Every retry adds load to a supplier that may already be struggling, and with three layers of retries one search could become dozens of calls. I retry once, only for connection errors and 503 (transient), never for 4xx, only if time remains before the deadline, and with jitter. `maxRetries = 1` means at most two attempts.

**Q:** Why one bean per supplier?

**A:** `@ConcurrencyLimit` limits per bean instance. If all three suppliers went through one shared client bean, one slow supplier would fill the shared limit and block the healthy ones. Separate beans give each supplier its own bulkhead, which the milestone test proves.

**Q:** What happens when all three suppliers are down?

**A:** All three circuits open, so calls fail in under a millisecond instead of waiting for timeouts. The aggregator then returns the stale cached result for that query, flagged `fromCache: true`, and only if there is none does it return an error. Circuit states and retry counts are exported as metrics, so operators see which supplier is failing.

**Q:** Your `@Retryable` never retries. Why?

**A:** Either `@EnableResilientMethods` is missing, so no proxy is created, or the method is called from another method in the same class, bypassing the proxy. It also does nothing if the thrown exception is not in `includes`. I wrote a test that fails if either annotation is not active, because silent no-ops are the usual failure.

**Q:** What changes at 10x — 3,000 searches per second?

**A:** The per-supplier bulkheads will reject more requests, which is correct, but means more partial results; the supplier's own rate limits become the real ceiling. I would raise the cache hit rate (fresh plus stale), consider adaptive concurrency limits based on observed latency, and scale instances horizontally, remembering that each instance has its own bulkhead and breaker state.

**Q:** How do you test resilience behaviour?

**A:** Stub supplier servers with scripted latency, error sequences and connection resets. Each policy is tested in isolation — one retry on 503 and none on 400, the breaker opening after the threshold and recovering through half-open calls, the bulkhead rejecting the 51st call — then combined scenarios with one slow, one failing and one healthy supplier.

**Q:** Show a supplier client method.

**A:**
```java
@ConcurrencyLimit(50)
@Retryable(includes = {ConnectException.class, ServiceUnavailableException.class},
           maxRetries = 1, delay = 100, jitter = 50, timeout = 1200)
public List<Offer> search(SearchQuery q) {
    return supplierA.search(q.from(), q.to(), q.date());
}
```
The Resilience4j breaker wraps this bean from outside.

**Common mistakes in answers:**
- "Spring 7 has a circuit breaker now" — core Spring added retry and concurrency limits only; circuit breakers still come from Resilience4j.
- "Virtual threads make bulkheads unnecessary" — they remove the natural cap, making explicit limits more important.
- Putting retries outside the circuit breaker — open circuits get retried and failures are over-counted.

### TL;DR
Deadline-bounded parallel fan-out where each supplier has its own bulkhead, one jittered retry and a circuit breaker — Framework 7 annotations for the first two, Resilience4j for the third.

## 34. Identity Provider with Spring Authorization Server
**Level:** Advanced · **Time:** 3–4 weeks · **Prerequisites:** Ch 10, Ch 11

### What it is
Your own **OAuth 2.1 / OpenID Connect identity provider** — the service that logs users in and issues tokens to every other app — built with **Spring Authorization Server**, which since **Spring Security 7.0 ships as part of Spring Security** (module `spring-security-oauth2-authorization-server`, Boot starter `spring-boot-starter-security-oauth2-authorization-server`). It supports the authorization code flow with PKCE for web and mobile apps, client credentials for services, refresh-token rotation, consent, multi-factor login, key rotation and discovery — and becomes the IdP for projects 11, 12 and 40.

### The problem it solves
Most companies should buy or run an existing IdP; but understanding one from the inside is the best way to master OAuth, and some products genuinely need an embedded, customizable authorization server. Building it exposes everything off-the-shelf products hide: client registration, token formats and lifetimes, signing-key rotation, consent storage, logout, and the security consequences of every default.

### Real-world examples
- The Spring blog post ["Spring Authorization Server moving to Spring Security 7.0"](https://spring.io/blog/2025/09/11/spring-authorization-server-moving-to-spring-security-7-0) explains the move and that Maven coordinates stay the same.
- The [Spring Security Authorization Server reference](https://docs.spring.io/spring-security/reference/servlet/oauth2/authorization-server/index.html) documents the endpoints, `RegisteredClientRepository`, token customization and configuration.
- The [OAuth 2.1 draft](https://datatracker.ietf.org/doc/draft-ietf-oauth-v2-1/) consolidates current best practice (PKCE required, implicit and password grants removed).

### Requirements
**Functional**
- Endpoints: authorization, token, JWKS, OIDC discovery and UserInfo, introspection, revocation, end-session (logout).
- Clients stored in PostgreSQL: a confidential web app, a public SPA or mobile client (PKCE, no secret), and service clients (client credentials).
- User login with password plus a second factor (one-time token by email, or WebAuthn passkeys) for admin users; consent screen for third-party clients.
- Custom claims: `tenant`, `roles` in access tokens; profile claims in ID tokens.
- Admin API to register clients and rotate their secrets.

**Non-functional**
- Access tokens 10 minutes, refresh tokens rotate on every use and expire after 30 days idle.
- Signing keys rotate every 30 days with two keys published during overlap; no token validation failures during rotation.
- Token endpoint p95 under 100 ms; two instances share all state (no sticky sessions for the token endpoint).
- Login brute-force protection and audit logs of every token issuance.

### The architecture
```mermaid
flowchart LR
    APP["Client apps - web, SPA, mobile, services"] -->|"authorize with PKCE"| AS["Authorization Server"]
    AS --> LOGIN["Login - password plus second factor"]
    AS --> CONS["Consent service"]
    AS --> TOK["Token generator - JWT access, opaque refresh"]
    TOK --> KEYS["JWK source - rotating keys"]
    AS --> RCR[("registered clients - JDBC")]
    AS --> AUTHZ[("authorizations and consents - JDBC")]
    LOGIN --> USERS[("users and credentials")]
    RS["Resource servers"] -->|"JWKS and discovery"| AS
```
- **Authorization Server filter chain** — a dedicated `SecurityFilterChain` for protocol endpoints, plus a second chain for the login pages and admin API.
- **RegisteredClientRepository** — the JDBC implementation stores clients, their grant types, redirect URIs, scopes and token settings.
- **OAuth2AuthorizationService / consent service** — JDBC-backed storage for issued authorizations (codes, refresh tokens) and user consents, shared by all instances.
- **Token generator** — JWT access tokens customized with an `OAuth2TokenCustomizer<JwtEncodingContext>`; refresh tokens opaque and stored.
- **JWK source** — loads active and next keys from a key store; publishes both in the JWKS.
- **Login** — Spring Security form login with an additional factor; Security 7's multi-factor support adds factor authorities you can require.

### Data model
```mermaid
erDiagram
    APP_USER ||--o{ CREDENTIAL : "has"
    APP_USER ||--o{ AUTHORIZATION_CONSENT : "grants"
    REGISTERED_CLIENT ||--o{ AUTHORIZATION_CONSENT : "receives"
    REGISTERED_CLIENT ||--o{ OAUTH2_AUTHORIZATION : "issued"
    APP_USER {
        uuid id PK
        text username UK
        text tenant
        boolean mfa_required
    }
    CREDENTIAL {
        uuid id PK
        uuid user_id FK
        text type
        text secret_hash
    }
    REGISTERED_CLIENT {
        text id PK
        text client_id UK
        text client_secret
        text grant_types
        text redirect_uris
        text scopes
    }
    OAUTH2_AUTHORIZATION {
        text id PK
        text registered_client_id FK
        text principal_name
        text refresh_token_value
        timestamptz refresh_token_expires_at
    }
    AUTHORIZATION_CONSENT {
        text registered_client_id FK
        text principal_name
        text authorities
    }
```
The authorization tables follow the schema shipped with Spring Authorization Server (simplified here); use its scripts in your Flyway migrations.

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Server | `spring-boot-starter-security-oauth2-authorization-server` (Security 7.1 in Boot 4.1.1) | standard OAuth 2.1 and OIDC endpoints |
| Storage | JDBC repositories on PostgreSQL | stateless instances, durable authorizations |
| Keys | RSA or EC keys from a key store or KMS, rotated by a scheduled job | key rotation without downtime |
| MFA | Spring Security 7 multi-factor support with one-time tokens or WebAuthn | second factor for admins |
| Tests | `spring-security-test`, an OAuth2 client test harness | full flows in tests |

### Milestones
1. **Minimal server.** In-memory client, form login. *Done when* the discovery document lists the endpoints and a test client completes authorization code + PKCE and gets tokens.
2. **JDBC storage and two instances.** *Done when* a code issued by instance 1 is redeemed at instance 2, and a restart loses no refresh tokens.
3. **Custom claims and consent.** *Done when* access tokens carry `tenant` and `roles`, and third-party clients show a consent page remembered per user.
4. **Refresh rotation, revocation and logout.** *Done when* reusing a rotated refresh token fails, revocation stops refresh immediately, and RP-initiated logout ends the session.
5. **MFA and key rotation.** *Done when* admin login requires a second factor, and a resource server (project 11) validates tokens across a key rotation with no failures.

### Deep dive: the hard parts
- **Two filter chains, ordered.** Protocol endpoints need their own chain with OAuth-specific error handling; login pages need a normal form-login chain. Wrong ordering sends token requests to the login page and returns HTML to machines. Use `@Order` and test both.
- **Token format choices.** JWT access tokens let resource servers validate offline (fast, scalable) but cannot be revoked before expiry; opaque tokens need introspection on every call but revoke instantly. Short-lived JWTs plus stored, rotating refresh tokens are the common compromise.
- **Key rotation.** Publish the new key first, wait for resource servers' JWKS caches to refresh, switch signing to the new key, keep the old one published for the longest access-token lifetime, then remove it. Resource servers refetch the JWKS when they see an unknown `kid`, which makes this smooth.
- **Client types.** Public clients (SPAs, mobile apps) cannot keep secrets, so they rely on PKCE and exact redirect-URI matching; never issue them long-lived refresh tokens without rotation. Confidential clients authenticate with a secret or, better, a private-key JWT.

### Testing strategy
- End-to-end flow tests driving the endpoints with an HTTP client (authorize → login → code → token → refresh → revoke).
- Negative tests: wrong redirect URI, missing PKCE, reused code, expired refresh token, unknown client.
- Rotation test with a resource server in the same test, validating tokens signed before and after the switch.

### Trade-offs, scaling and failure modes
- Running an IdP means owning security incidents, MFA recovery, account lockout policy and compliance; a managed IdP or Keycloak often costs less overall.
- The IdP is a tier-zero dependency: if it is down, nobody logs in. Run multiple instances, keep JWKS cacheable, and keep access tokens valid offline.
- Customization power is the reason to choose Spring Authorization Server — and the main way to introduce vulnerabilities. Review every customization against the OAuth security best current practice.

### Stretch goals
- Dynamic client registration for partner onboarding.
- Token exchange (RFC 8693) for service-to-service delegation.
- Federation: "Sign in with Google" upstream, while your server stays the token issuer.

### Skills practised
- [Ch 11 — Tokens and OAuth2](../chapters/11-spring-security-2-tokens-and-oauth2/README.md): OAuth 2.0/OIDC, JWT minting, refresh and revocation, Spring Authorization Server, storing keys and secrets.
- [Ch 10 — Spring Security fundamentals](../chapters/10-spring-security-1-fundamentals/README.md): filter chains, authentication, password storage, sessions.
- [Ch 17 — Spring AI and what's new](../chapters/17-spring-ai-and-whats-new/README.md): Security 6 → 7 changes (Authorization Server inside Security, MFA, PKCE defaults).

### In the interview
**30-second answer:** I built an OAuth 2.1 and OpenID Connect identity provider with Spring Authorization Server, which since Spring Security 7.0 ships as part of Spring Security. It supports authorization code with PKCE for web and mobile clients, client credentials for services, rotating refresh tokens, consent, MFA for admins and zero-downtime signing-key rotation. The key design decision was storing clients, authorizations and consents in PostgreSQL through the JDBC repositories, so two instances share all state and a code issued by one can be redeemed at the other. The hardest part was key rotation: publish the new key first, switch signing after JWKS caches refresh, and keep the old key until its tokens expire — a resource server validated tokens across a rotation with zero failures.

**Key terms to know:**
- **Authorization server / IdP** — the service that authenticates users and issues tokens to clients.
- **OAuth 2.1** — consolidated best practice: PKCE required, implicit and password grants removed.
- **OIDC** — identity layer adding ID tokens, UserInfo and discovery.
- **Public vs confidential client** — cannot keep a secret (SPA, mobile) vs can (server app).
- **`RegisteredClientRepository`** — stores client ids, secrets, grant types, redirect URIs, scopes and token settings.
- **`OAuth2AuthorizationService`** — stores issued codes and tokens so any instance can redeem them.
- **JWK set and `kid`** — published public keys; tokens name the key that signed them.
- **Token customizer** — `OAuth2TokenCustomizer<JwtEncodingContext>` adds claims such as `tenant` and `roles`.
- **JWT vs opaque access token** — self-contained and validated offline vs a reference checked by introspection.
- **Multi-factor authentication** — Security 7 support for requiring additional factors.

**Q:** Where does Spring Authorization Server live now?

**A:** Since Spring Security 7.0 it is part of Spring Security: the module is `spring-security-oauth2-authorization-server`, versioned with Security, and Boot 4 has the `spring-boot-starter-security-oauth2-authorization-server` starter. Before that it was a separate project with its own version line.

**Q:** JWT or opaque access tokens?

**A:** Short-lived JWTs let resource servers validate offline with cached public keys, which is fast and scales, but they cannot be revoked before they expire. Opaque tokens require an introspection call on every request but revoke instantly. I use 10-minute JWT access tokens plus stored, rotating refresh tokens, and would switch a specific high-risk API to opaque tokens if instant revocation mattered more than latency.

**Q:** How do you rotate signing keys without breaking clients?

**A:** Publish the new key in the JWKS alongside the old one, wait for resource servers' JWKS caches to refresh, switch signing to the new key, keep the old key published for at least the longest access-token lifetime, then remove it. Resource servers refetch the JWKS when they see an unknown `kid`, so the switch is smooth.

**Q:** Why two security filter chains?

**A:** The protocol endpoints (`/oauth2/token`, `/oauth2/authorize`, JWKS) need OAuth-specific authentication and error responses in JSON; the login pages and admin API need normal form login and HTML. Each gets its own `SecurityFilterChain` with an `@Order`. With the wrong order, a machine calling the token endpoint gets redirected to an HTML login page.

**Q:** How do public clients stay safe without a secret?

**A:** They rely on PKCE, so a stolen authorization code is useless without the verifier, and on exact redirect-URI matching, so codes cannot be sent to an attacker's URL. Refresh tokens issued to them must rotate on every use, so a stolen one is detected when both parties use it.

**Q:** What happens if a refresh token is reused after rotation?

**A:** The authorization server rejects it, because only the newest refresh token in the authorization is valid. Reuse indicates theft, so the safe response is to revoke the whole authorization and force the user to sign in again. A test covers it.

**Q:** The IdP goes down. What still works?

**A:** Nobody can log in or refresh, so it is a tier-zero dependency. Already issued JWT access tokens keep validating at resource servers because they use cached public keys, which buys time. I run two or more instances with shared JDBC state and keep JWKS responses cacheable.

**Q:** What changes at 10x — many more clients and token requests?

**A:** The token endpoint is the hot path; it stays fast because instances are stateless apart from the shared database, so I scale instances horizontally and index the authorization table on token values. Expired authorizations need a cleanup job, and signing keys should live in a KMS rather than a file. Login brute-force protection and audit logging grow with traffic.

**Q:** How do you test the server?

**A:** End-to-end flow tests drive the real endpoints with an HTTP client: authorize, log in, receive code, exchange with the verifier, refresh, revoke. Negative tests cover wrong redirect URIs, missing PKCE, reused codes, expired refresh tokens and unknown clients. A rotation test runs a resource server in the same test and validates tokens signed before and after the switch.

**Q:** Show how to add custom claims.

**A:**
```java
@Bean OAuth2TokenCustomizer<JwtEncodingContext> claims(UserDirectory users) {
    return ctx -> {
        if (OAuth2TokenType.ACCESS_TOKEN.equals(ctx.getTokenType())) {
            var u = users.find(ctx.getPrincipal().getName());
            ctx.getClaims().claim("tenant", u.tenant()).claim("roles", u.roles());
        }
    };
}
```

**Common mistakes in answers:**
- "Use the password grant for our own app" — OAuth 2.1 removes it; use authorization code with PKCE.
- "JWTs can be revoked at the IdP" — a JWT stays valid until it expires unless resource servers check a denylist.
- Removing the old signing key immediately after switching — tokens signed with it fail until they expire.

### TL;DR
An OAuth 2.1/OIDC provider on Spring Security 7's built-in Authorization Server: JDBC-backed clients and authorizations, PKCE, rotating refresh tokens, custom claims, MFA and zero-downtime key rotation.

## 35. Event-sourced Wallet Ledger with CQRS
**Level:** Advanced · **Time:** 4 weeks · **Prerequisites:** Ch 7, Ch 9, Ch 14

### What it is
A digital-wallet ledger where the source of truth is not the current balance but an **append-only log of events** — `WalletOpened`, `FundsDeposited`, `FundsTransferred`, `TransferReversed`. Current state is derived by replaying events (**event sourcing**). Queries are served from separate, purpose-built **read models** (balances, statements, daily totals) updated from the event stream — **CQRS**, Command Query Responsibility Segregation: the write side validates commands and appends events; the read side answers queries.

### The problem it solves
Financial systems must explain every balance: who moved what, when and why, forever. Updating a `balance` column loses history; audit tables bolted on later drift from reality. Event sourcing makes the audit log the database. It also brings hard problems — concurrent appends, rebuilding projections, schema evolution of old events, double-entry correctness — which make it a rich advanced project.

### Real-world examples
- Martin Fowler's article ["Event Sourcing"](https://martinfowler.com/eaaDev/EventSourcing.html) defines the pattern, replay, and temporal queries.
- [TigerBeetle](https://github.com/tigerbeetle/tigerbeetle) is an open-source database designed specifically for double-entry financial accounting; its documentation describes the debit/credit account-and-transfer model that this project copies in miniature.

### Requirements
**Functional**
- Commands: open wallet, deposit, withdraw, transfer between wallets, reverse a transfer (by an operator).
- Every transfer is **double-entry**: one debit and one credit of the same amount; the sum over all accounts is always zero (including a system "external" account for deposits and withdrawals).
- Queries: current balance, paginated statement, balance as of any past timestamp, daily totals per currency.
- Overdraft is impossible: a withdrawal or transfer that would make a balance negative is rejected.

**Non-functional**
- Concurrent commands on the same wallet never both succeed when only one is valid (optimistic concurrency on the stream version).
- Read models lag the write side by under 1 s at p99; balance reads under 10 ms.
- Any read model can be rebuilt from scratch from the event log; rebuilding 10 million events takes under 15 minutes.
- Events are immutable; corrections are new events (reversals), never updates.

### The architecture
```mermaid
flowchart TD
    CMD["Command API"] --> H["Command handlers"]
    H --> LOAD["Load wallet - snapshot plus events"]
    LOAD --> ES[("Event store - append only")]
    H -->|"append with expected version"| ES
    ES --> PUB["Event relay - outbox or polling by global position"]
    PUB --> P1["Balance projection"]
    PUB --> P2["Statement projection"]
    PUB --> P3["Daily totals projection"]
    P1 --> RM[("Read models")]
    P2 --> RM
    P3 --> RM
    QRY["Query API"] --> RM
```
- **Command handlers** — load the wallet **aggregate** (the consistency boundary) by replaying its events (from the latest snapshot), check business rules, produce new events.
- **Event store** — a PostgreSQL table with a unique `(stream_id, version)` constraint; appending with the expected version is the concurrency check.
- **Event relay** — reads new events in global order (a sequence column) and feeds projections; or the outbox + Kafka pattern from project 28 for other services.
- **Projections** — idempotent handlers that update read-model tables and record the last processed position.
- **Query API** — reads only from read models; historical balance queries replay a stream up to a timestamp.

### Data model
```mermaid
erDiagram
    EVENT_STORE {
        bigint global_position PK
        uuid stream_id
        int version
        text event_type
        int schema_version
        jsonb payload
        jsonb metadata
        timestamptz recorded_at
    }
    SNAPSHOT {
        uuid stream_id PK
        int version
        jsonb state
    }
    BALANCE_VIEW {
        uuid wallet_id PK
        text currency
        numeric balance
        int last_version
    }
    STATEMENT_LINE {
        bigint global_position PK
        uuid wallet_id
        numeric amount
        numeric running_balance
        text description
    }
    PROJECTION_CHECKPOINT {
        text projection PK
        bigint last_position
    }
```
`EVENT_STORE` has a unique constraint on (`stream_id`, `version`); `metadata` holds the command id (for idempotency), user and trace id.

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Event store | PostgreSQL table via `JdbcClient` | transactions, unique constraints, simple to inspect |
| Serialization | Jackson 3 with explicit event classes and a `schema_version` | controlled evolution |
| Projections | scheduled pollers by global position, or Kafka consumers | at-least-once, idempotent |
| Money | `BigDecimal`, `numeric` | exact arithmetic |
| Alternative | an event-sourcing framework (e.g. Axon) | compare after building by hand |

### Milestones
1. **Aggregate and store.** *Done when* unit tests in given-when-then form ("given these events, when this command, then these events or this error") cover all wallet rules.
2. **Optimistic concurrency.** *Done when* 100 concurrent withdrawals against a wallet that can afford 10 result in exactly 10 successes, the rest retried or rejected, and no negative balance.
3. **Double-entry transfers.** A transfer touches two streams. *Done when* both legs are appended atomically in one database transaction and a property-based test confirms the global sum stays zero.
4. **Projections and rebuild.** *Done when* balances and statements lag under 1 s, and dropping and rebuilding a projection yields identical tables.
5. **Evolution and snapshots.** *Done when* a v1 event is upcast to v2 on read, and snapshots every 500 events keep wallet load time flat for long streams.

### Deep dive: the hard parts
- **Concurrency through versions.** Two commands load version 41 and both try to append version 42; the unique constraint lets one win and the other gets a conflict, reloads and re-validates. No locks, and the rule check always runs against the latest state.
- **Transfers across aggregates.** Strictly, one transaction should touch one aggregate. With both wallets in one PostgreSQL database, appending both legs in one local transaction is pragmatic and correct; across databases or services you would need a saga (project 28) with a pending state and compensation.
- **Projections are eventually consistent.** After a command succeeds, the balance view may be stale for a moment. Return the new version from the command so clients can wait until the projection has caught up (read-your-writes), or compute the balance from the aggregate for that response.
- **Schema evolution.** Old events never change. New code reads them through **upcasters** that transform v1 payloads into the current shape. Never delete fields you need for replay; add new event types instead of overloading old ones.

### Testing strategy
- Given-when-then aggregate tests with no Spring or database.
- Concurrency and invariant tests on real PostgreSQL (Testcontainers).
- Projection rebuild test comparing a rebuilt read model with the live one.

### Trade-offs, scaling and failure modes
- Event sourcing adds real complexity: upcasting, projections, eventual consistency. It pays off where history and audit are core requirements (ledgers, workflows), not for simple CRUD.
- Event-store growth is unbounded; partition by time and archive, but keep replay possible.
- GDPR-style deletion conflicts with immutability: keep personal data outside events, or encrypt it per user and delete the key ("crypto-shredding").

### Stretch goals
- Temporal queries: balance as of any instant, efficiently, with snapshots per day.
- Publish integration events to Kafka for other services, distinct from internal domain events.
- Reconciliation job comparing projections with a full replay nightly.

### Skills practised
- [Ch 7 — Data access with JDBC and transactions](../chapters/07-data-access-jdbc-and-transactions/README.md): `JdbcClient`, transactions, unique-constraint concurrency.
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): events, idempotent consumers, the outbox.
- [Ch 9 — Spring Data beyond JPA, and caching](../chapters/09-spring-data-beyond-jpa-and-caching/README.md): aggregates and choosing a data technology.
- [Ch 5 — Spring MVC and REST APIs](../chapters/05-spring-mvc-and-rest/README.md): separate command and query APIs.

### In the interview
**30-second answer:** I built a double-entry wallet ledger where the source of truth is an append-only event store, and balances, statements and daily totals are separate read models updated from the event stream — event sourcing with CQRS. The key design decision was optimistic concurrency on the stream version: appending with a unique `(stream_id, version)` constraint means two commands on the same wallet cannot both succeed against stale state, with no locks. The hardest parts were transfers across two wallets, appended atomically in one local transaction, and evolving old events, handled with upcasters. 100 concurrent withdrawals against a wallet that can afford 10 produce exactly 10 successes, and the global sum of all accounts is always zero.

**Key terms to know:**
- **Event sourcing** — storing state as an append-only sequence of events and deriving current state by replay.
- **CQRS** — separate models for writing (commands) and reading (queries).
- **Aggregate / stream** — the consistency boundary (one wallet) and its ordered events.
- **Optimistic concurrency (expected version)** — append only if the stream is still at the version you read.
- **Projection / read model** — a table built from events for fast queries; rebuildable from the log.
- **Snapshot** — saved aggregate state at a version, so loading does not replay every event.
- **Upcaster** — code that converts an old event version to the current shape when reading.
- **Double-entry bookkeeping** — every movement is a debit and a credit of the same amount; totals sum to zero.
- **Eventual consistency / read-your-writes** — read models lag slightly; clients can wait for a version.
- **Crypto-shredding** — deleting a per-user encryption key to make personal data in immutable events unreadable.

**Q:** What is event sourcing and when is it worth it?

**A:** Instead of updating a balance column, every change is stored as an immutable event, and current state is computed by replaying them. It is worth it when full history, audit and "as of" queries are core requirements, as in ledgers. For simple CRUD it adds projections, upcasting and eventual consistency for no benefit.

**Q:** How do you handle concurrent writes?

**A:** Optimistic concurrency on the stream version. Both commands load version 41 and try to append version 42; the unique constraint on `(stream_id, version)` lets one insert win and the other gets a conflict, reloads the stream and re-validates its rule against the new balance. No locks are held, and the rule always runs against the latest state.

**Q:** How do you change an event's shape?

**A:** Stored events are never rewritten. Each event has a `schema_version`, and upcasters transform old versions into the current shape when they are read. Fields needed for replay are never deleted, and a genuinely new meaning gets a new event type rather than overloading an old one.

**Q:** Why CQRS — why not query the event store directly?

**A:** Replaying a stream for every balance read is slow and a statement over many wallets would be very expensive. Projections build exactly the tables queries need — current balance, statement lines with running balances, daily totals — so reads take under 10 ms. The price is that read models are eventually consistent.

**Q:** A transfer touches two wallets. How is it atomic?

**A:** Both wallets' streams live in the same PostgreSQL database, so I append the debit event to one stream and the credit event to the other in one local transaction, each with its expected version. If either append conflicts, both roll back and the command retries. Across services or databases this would need a saga with a pending state and compensation.

**Q:** A user withdraws and immediately sees the old balance. Why, and what do you do?

**A:** The balance projection lags the write side slightly. The command returns the new stream version; the client can either wait until the projection's `last_version` reaches it (read-your-writes) or show the balance computed from the aggregate in the command response. The lag target is under 1 s at p99.

**Q:** How do you rebuild a broken projection?

**A:** Drop its table, reset its checkpoint to position zero, and replay all events in global order; projection handlers are idempotent and record the last processed position. A test rebuilds a projection and compares it row by row with the live one. Rebuilding 10 million events takes under 15 minutes.

**Q:** What changes at 100x events?

**A:** Long streams make loading slow, so snapshots every 500 events keep load time flat. The event store grows forever, so I partition by time and archive old partitions while keeping replay possible. Projections can be fed through Kafka (via an outbox) and scaled independently from the write side.

**Q:** How do you test it?

**A:** Aggregate rules are given-when-then unit tests with no Spring or database: given these events, when this command, then these events or this error. Concurrency tests run on real PostgreSQL, and a property-based test generates random transfers and asserts the global sum stays zero and no balance goes negative.

**Q:** Write the append with the concurrency check.

**A:**
```sql
INSERT INTO event_store (stream_id, version, event_type, schema_version, payload, metadata)
VALUES (:stream, :expectedVersion + 1, :type, :schemaVersion, :payload::jsonb, :meta::jsonb);
-- unique (stream_id, version): a concurrent append of the same version fails
-- with a duplicate-key error, which the handler turns into reload-and-retry
```

**Common mistakes in answers:**
- "We update events to fix mistakes" — events are immutable; corrections are new events such as reversals.
- "Event sourcing means using Kafka as the database" — the event store needs per-stream optimistic concurrency, which a plain topic does not provide.
- Ignoring eventual consistency — users and tests must handle read models that lag.

### TL;DR
A double-entry wallet ledger where events are the truth: versioned appends for concurrency, rebuildable projections for queries, upcasters for evolution and an invariant that every transfer sums to zero.

## 36. AI Support Agent with Tools and MCP
**Level:** Advanced · **Time:** 3 weeks · **Prerequisites:** Ch 17, Ch 11

### What it is
A customer-support **agent**: an LLM-powered assistant that does not just answer from documents (project 30) but **acts** through tools — looks up orders, checks shipment status, opens a return, proposes a refund — while a human approves anything with money or irreversible effects. Tools are plain Spring methods annotated `@Tool` and invoked through Spring AI's **tool calling**; some tools come from external **MCP servers**. **MCP** (Model Context Protocol) is an open protocol for exposing tools, resources and prompts to AI applications; your agent is an MCP client, and you also publish your order tools as an MCP server so other assistants can use them.

### The problem it solves
Letting a model trigger real actions is where AI meets security engineering. Who is the user? Which tools may this user's request reach? What stops a prompt injection in an email from issuing a refund? How do you bound cost and loops, and how do you audit what happened? This project builds an agent with the guardrails that make it shippable.

### Real-world examples
- The [Model Context Protocol site](https://modelcontextprotocol.io/) hosts the specification, transports (STDIO and Streamable HTTP) and the server/client concepts.
- The [Spring AI MCP overview](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html) documents the MCP client and server starters and how MCP tools become Spring AI tool callbacks.

### Requirements
**Functional**
- Chat endpoint for authenticated customers (JWT) and a separate console for support staff.
- Tools: `findOrders`, `orderStatus`, `trackShipment` (read-only); `createReturn` (customer-confirmed); `proposeRefund` (creates a pending refund that a staff member approves).
- External MCP tools: a knowledge-base search server and a shipping-carrier server.
- Your own MCP server exposing read-only order tools to internal assistants, with OAuth2-protected access.
- Full audit trail: messages, tool calls with arguments and results, approvals.

**Non-functional**
- Tools only ever act for the authenticated user: the user id comes from the security context or `ToolContext`, never from model-chosen arguments.
- At most 6 tool calls per user turn; a turn times out after 30 s.
- Per-user daily token budget; cost and latency per turn recorded as metrics.
- Zero refunds executed without a human approval — tested.

### The architecture
```mermaid
flowchart TD
    U["Customer - JWT"] --> CH["ChatController"]
    CH --> CC["ChatClient - system prompt, memory, guard advisors"]
    CC --> LLM["ChatModel"]
    CC --> TL["Local @Tool beans - orders, returns, refunds"]
    CC --> MC["MCP client - tool callbacks"]
    MC -->|"Streamable HTTP"| KB["Knowledge-base MCP server"]
    MC -->|"Streamable HTTP"| SHIP["Carrier MCP server"]
    TL --> SVC["Order and refund services - authorization checks"]
    SVC --> DB[("PostgreSQL - orders, pending refunds, audit")]
    STAFF["Staff console"] -->|"approve or reject"| SVC
    MS["Your MCP server - read-only order tools"] --> SVC
```
- **ChatClient** — configured with a system prompt describing role and limits, chat memory per conversation, and advisors for logging, redaction and budget checks.
- **Local tools** — `@Tool`-annotated methods with precise descriptions and typed parameters; each calls a service that re-checks authorization.
- **MCP client** — `spring-ai-starter-mcp-client` connects to configured servers and exposes their tools as callbacks, filtered to an allow-list.
- **Approval flow** — `proposeRefund` writes a `PENDING` refund and returns "submitted for review"; only staff can approve, outside the model loop.
- **Your MCP server** — `spring-ai-starter-mcp-server-webmvc` with `@McpTool` methods, Streamable HTTP transport, protected as an OAuth2 resource server.

### Data model
```mermaid
erDiagram
    CONVERSATION ||--o{ MESSAGE : "contains"
    MESSAGE ||--o{ TOOL_CALL : "triggers"
    TOOL_CALL ||--o| PENDING_REFUND : "may create"
    CONVERSATION {
        uuid id PK
        text user_sub
        timestamptz started_at
        int tokens_used
    }
    MESSAGE {
        uuid id PK
        uuid conversation_id FK
        text role
        text content
    }
    TOOL_CALL {
        uuid id PK
        uuid message_id FK
        text tool_name
        jsonb arguments
        jsonb result
        text outcome
    }
    PENDING_REFUND {
        uuid id PK
        uuid tool_call_id FK
        text order_id
        numeric amount
        text status
        text approved_by
    }
```

### Tech stack
| Component | Choice | Why |
|---|---|---|
| AI | Spring AI 2.0 `ChatClient`, tool calling, chat memory | the Spring way to build agents |
| MCP | `spring-ai-starter-mcp-client`, `spring-ai-starter-mcp-server-webmvc` | consume and expose tools over Streamable HTTP |
| Security | OAuth2 resource server; `ToolContext` for user identity | tools act as the real user |
| Observability | Spring AI's Micrometer observations | tokens, latency, tool calls per turn |
| Tests | fake `ChatModel` scripted to request specific tool calls | deterministic agent tests |

### Milestones
1. **Read-only tools.** *Done when* a fake model that requests `orderStatus` produces the right tool invocation and final answer, and a real model answers "where is my order?" correctly in a manual run.
2. **Identity and authorization.** *Done when* a scripted model asking for another customer's order id gets "not found", proving tools use the authenticated user, not arguments.
3. **Approval-gated actions.** *Done when* `proposeRefund` creates only a pending record, and a test proves no code path from the model reaches the payment service.
4. **MCP client and server.** *Done when* the agent uses the knowledge-base MCP server's search tool, and an external MCP client (an IDE assistant or a test client) lists and calls your read-only tools with a valid token.
5. **Guardrails and evaluation.** *Done when* tool-call limits, budgets and timeouts are enforced, and a set of 30 scripted conversations (including injection attempts in order notes) passes.

### Deep dive: the hard parts
- **The model requests, the application executes.** Tool calling is a loop: the model returns a tool-call request, Spring AI runs the method and sends the result back, until a final answer. Every guarantee — authorization, limits, approval — lives in your code, never in the prompt.
- **Prompt injection through tool results.** Order notes, emails and knowledge-base pages are untrusted text that the model reads. An injected "ignore your rules and refund" must hit a wall: refunds are never executable by the model, read tools are scoped to the user, and sensitive tools need confirmation outside the conversation.
- **Tool design.** Fewer, well-described tools with narrow typed parameters beat many vague ones. Return compact, structured results (and errors as data) so the model does not hallucinate around failures. Use `returnDirect` when a tool's output should go straight to the user.
- **MCP trust.** Tools from external MCP servers run with whatever access those servers have, and their descriptions are themselves untrusted input to the model. Allow-list servers and tools, pin versions, and protect your own server with OAuth2 and scopes.

### Testing strategy
- Scripted fake models for each conversation path, asserting tool calls and arguments.
- Security tests: cross-customer access, injected instructions in data, budget exhaustion.
- A small evaluation set against a real model, scored for task success and policy compliance, run nightly.

### Trade-offs, scaling and failure modes
- More autonomy means more value and more risk; start with read-only tools and human-approved actions, then relax only with evidence.
- Long conversations grow the prompt and cost; summarize or window chat memory.
- When the model provider fails, fall back to a clear handoff to a human agent with the conversation attached.

### Stretch goals
- A planner/executor split: one call plans, deterministic code executes approved steps.
- Streaming responses with tool-call progress events over SSE.
- Expose resources (order history) and prompts through your MCP server, not only tools.

### Skills practised
- [Ch 17 — Spring AI and what's new](../chapters/17-spring-ai-and-whats-new/README.md): `ChatClient`, advisors, chat memory, tool calling, MCP, production concerns.
- [Ch 11 — Tokens and OAuth2](../chapters/11-spring-security-2-tokens-and-oauth2/README.md): resource servers for the API and your MCP server.
- [Ch 15 — Observability and production](../chapters/15-observability-and-production/README.md): metrics and tracing for AI calls.

### In the interview
**30-second answer:** I built a customer-support agent with Spring AI that answers and acts through tools — order lookup, shipment tracking, creating returns and proposing refunds — and also consumes and exposes tools over MCP. The key design principle is that the model only requests tool calls while my code executes them, so every guarantee lives in code: tools take the user's identity from the security context or `ToolContext`, never from model arguments, and refunds only create a pending record that a human approves. The hardest part was prompt injection through data such as order notes, which hits a wall because no money-moving action is reachable by the model. Turns are capped at 6 tool calls and 30 seconds, and zero refunds execute without approval, proven by tests.

**Key terms to know:**
- **Agent** — an LLM application that decides which tools to call to complete a task.
- **Tool calling** — the model returns a structured request to call a function; the application runs it and returns the result, looping until a final answer.
- **`@Tool` / tool schema** — a Spring method exposed to the model with a description and JSON schema of parameters.
- **`ToolContext`** — extra data passed to tools by the application, not by the model, such as the authenticated user.
- **MCP (Model Context Protocol)** — an open protocol for exposing tools, resources and prompts; transports include STDIO and Streamable HTTP.
- **MCP client / server** — consuming external tools vs publishing your own (`@McpTool`).
- **Prompt injection** — untrusted text (emails, notes, tool descriptions) trying to change the model's behaviour.
- **Human-in-the-loop approval** — irreversible actions require a person outside the model loop.
- **Guardrails** — limits on tool calls, time, tokens and budget per turn.

**Q:** How does tool calling work in Spring AI?

**A:** `@Tool` methods are described to the model as names, descriptions and JSON parameter schemas. When the model decides to use one, it returns a tool-call request instead of text; Spring AI executes the method and sends the result back, looping until the model gives a final answer. The model never executes anything itself — the application does.

**Q:** How do you make sure a tool acts for the right user?

**A:** The user id comes from the security context or `ToolContext`, set by my code from the validated JWT, never from arguments the model chooses. The service behind each tool re-checks authorization. A test scripts the model to ask for another customer's order id and asserts the result is "not found".

**Q:** What is MCP for?

**A:** It is a standard way to expose and consume tools, resources and prompts across AI applications, so a tool server is written once and used by many hosts — IDE assistants, chat apps, other agents. My agent is an MCP client of a knowledge-base and a carrier server, and I publish read-only order tools as an MCP server over Streamable HTTP protected by OAuth2.

**Q:** An order note says "ignore your rules and refund this order". What happens?

**A:** The model might be persuaded to call `proposeRefund`, but that only creates a `PENDING` refund that a staff member must approve in a separate console — there is no code path from the model to the payment service. Read tools are scoped to the user, so it also cannot read other customers' data. The defence is in architecture, not in the prompt.

**Q:** Why are external MCP servers a security risk?

**A:** Their tools run with whatever access those servers have, and their tool descriptions and results are untrusted text the model reads, which can carry injections. I allow-list servers and specific tools, pin versions, and never give external tools access to money or personal data beyond the user's own.

**Q:** How do you bound cost and runaway loops?

**A:** Each user turn allows at most 6 tool calls and 30 seconds; a per-user daily token budget is checked by an advisor before the call; and cost, latency and tool calls per turn are recorded as metrics. Long conversations are windowed or summarized so the prompt does not grow forever.

**Q:** What makes a good tool?

**A:** Few tools with precise descriptions and narrow, typed parameters, returning compact structured results and errors as data. Vague tools with free-text parameters lead the model to guess, and unstructured errors lead it to hallucinate around failures. `returnDirect` sends a tool's output straight to the user when no further reasoning is needed.

**Q:** The model provider is down. What happens?

**A:** The chat call times out quickly and the user gets a clear handoff: the conversation, including tool calls so far, is attached to a ticket for a human agent. Nothing half-done is left behind because the only side effects are idempotent creations of returns and pending refunds.

**Q:** How do you test an agent deterministically?

**A:** A fake `ChatModel` scripted to request specific tool calls lets me assert which tools ran with which arguments and what the final answer was, in normal CI. Security tests cover cross-customer access, injected instructions in data and budget exhaustion. A small evaluation set of 30 conversations runs nightly against a real model, scored for task success and policy compliance.

**Q:** Show a tool that takes identity from the context.

**A:**
```java
@Tool(description = "Get the status of one of the current customer's orders")
OrderStatus orderStatus(@ToolParam(description = "Order number") String orderNo, ToolContext ctx) {
    String customer = (String) ctx.getContext().get("customerSub");   // set by our code from the JWT
    return orders.statusFor(customer, orderNo);                        // not found if not theirs
}
```

**Common mistakes in answers:**
- "The system prompt tells the model not to refund" — prompts are not security controls; enforcement must be in code.
- Letting the model pass the user id as a tool argument — the model can be tricked into choosing another user.
- Trusting MCP tool descriptions — they are untrusted input and can carry prompt injection.

### TL;DR
An agent whose power lives in well-guarded Spring methods: identity from the token, read tools scoped to the user, money actions gated by humans, MCP for shared tools, and tests that try to break it.

## 37. Zero-downtime Deployments on Kubernetes
**Level:** Advanced · **Time:** 2–3 weeks · **Prerequisites:** Ch 8, Ch 15

### What it is
A deployment pipeline and application design that ship a Spring Boot service to **Kubernetes** dozens of times a day — including **database schema changes** and **API/event changes** — with no failed user requests. It combines Kubernetes **rolling updates** and **probes** (health checks Kubernetes uses to decide whether to send traffic or restart a container), Spring Boot's **graceful shutdown** and availability states, fast startup, and the **expand/contract** (parallel change) technique for schema and contract evolution.

### The problem it solves
"Deploy at night and hope" does not scale. Requests fail during rollouts when old pods are killed mid-request, new pods receive traffic before they are ready, or the new code and the old code disagree about the database schema for a few minutes. Each cause has a precise fix; this project proves each one with a load test running during deployments.

### Real-world examples
- Kubernetes documents how to [configure liveness, readiness and startup probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/) and what each controls.
- The Spring Boot reference on [graceful shutdown](https://docs.spring.io/spring-boot/reference/web/graceful-shutdown.html) describes how the web server drains in-flight requests.
- Martin Fowler's ["Parallel Change"](https://martinfowler.com/bliki/ParallelChange.html) describes the expand, migrate, contract steps used for breaking changes.

### Requirements
**Functional**
- A sample "accounts" service with a REST API, PostgreSQL and Kafka events.
- Pipeline: build, test, image (buildpacks), deploy to a cluster (kind or minikube locally) with a rolling update.
- Three planned changes, each deployed with zero errors: rename a column, split a field into two, change an event payload.

**Non-functional**
- A load test at 200 requests per second running throughout each deployment shows **0** failed requests.
- New pods become ready within 10 s; old pods drain within 30 s.
- Every release can be rolled back to the previous version at any point without data loss.

### The architecture
```mermaid
sequenceDiagram
    participant K as Kubernetes
    participant N as New pod v2
    participant O as Old pod v1
    participant LB as Service endpoints
    K->>N: start container
    N->>N: startup - migrations already applied, AOT cache
    K->>N: readiness probe
    N->>K: ready
    K->>LB: add v2 endpoint
    K->>O: remove from endpoints, then SIGTERM after preStop
    O->>O: readiness REFUSING_TRAFFIC, drain in-flight requests
    O->>K: exit 0 within grace period
```
- **Probes** — `/actuator/health/liveness` and `/actuator/health/readiness`; Boot exposes these probe groups by default in Boot 4. A startup probe protects slow starts.
- **preStop hook** — a short sleep (a few seconds) so endpoint removal propagates to load balancers before the app stops accepting connections.
- **Graceful shutdown** — default since Boot 3.4; `spring.lifecycle.timeout-per-shutdown-phase` must fit inside Kubernetes' `terminationGracePeriodSeconds`.
- **Migrations** — Flyway runs as a separate job (or init step) before the rollout, applying only backward-compatible "expand" changes.
- **Fast startup** — extracted jar plus JDK AOT cache from a training run in the image build.

### Data model
| Step | Release | Schema state | Code behaviour |
|---|---|---|---|
| Expand | v2 | add `email_address` (nullable), keep `email` | write both, read `email` |
| Migrate | v3 | backfill `email_address` in batches | write both, read `email_address` |
| Contract | v4 | drop `email` after v3 is fully rolled out | use only `email_address` |

At every step, the previous release still works against the current schema — that is what makes rollback safe.

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Health | Actuator probes, availability state, health groups | correct liveness vs readiness |
| Shutdown | graceful shutdown + preStop + aligned timeouts | drain without errors |
| Migrations | `spring-boot-starter-flyway` run as a pre-deploy job | schema before code, once |
| Images | `spring-boot:build-image` with AOT cache or CDS enabled | fast, reproducible startup |
| Cluster | kind or minikube; a PodDisruptionBudget | realistic local rehearsal |
| Load | k6 or Gatling | prove zero errors |

### Milestones
1. **Baseline failure.** Deploy with default settings under load. *Done when* you have recorded the error count and identified each cause.
2. **Probes and draining.** *Done when* readiness, liveness, startup probes, preStop and graceful shutdown bring errors during a code-only rollout to zero.
3. **Startup.** *Done when* image builds include an AOT cache and pod ready time is under 10 s, measured.
4. **Expand/contract column rename.** *Done when* the three-release rename runs under load with zero errors, and rolling back any single step works.
5. **Event contract change.** *Done when* consumers accept both old and new payload versions before producers switch, and no message fails during the transition.

### Deep dive: the hard parts
- **The shutdown race.** Kubernetes removes a pod from endpoints and sends `SIGTERM` at about the same time; load balancers may keep sending requests for a moment. A preStop sleep, then graceful shutdown that finishes in-flight requests, closes the gap. Total time must stay below the termination grace period, or the pod is killed mid-drain.
- **Liveness must be boring.** If liveness includes the database, a database blip restarts every pod at once and turns a small incident into an outage. Liveness reflects only the app's internal state; readiness may include critical dependencies.
- **Schema and code compatibility.** During a rollout old and new pods run side by side against one database. Every migration must work with both versions: add before use, stop using before drop, never rename in one step. Long backfills run in batches outside the deploy.
- **Contracts beyond the database.** APIs and events have the same problem: consumers must tolerate new fields first (tolerant reader), producers switch second, old fields go last. Versioning (project 21) handles the rare true break.

### Testing strategy
- Load tests during each deployment in a local cluster, with error counts as the pass criterion.
- Migration tests: run release N's tests against the schema of release N+1 (backward compatibility).
- A chaos step: kill a pod abruptly under load and verify retries and readiness keep errors within budget.

### Trade-offs, scaling and failure modes
- Expand/contract triples the number of releases for a breaking schema change — the price of zero downtime, cheap with automated deploys.
- Blue-green or canary deployments give faster rollback and traffic control at the cost of duplicate capacity or extra tooling.
- Running migrations inside application startup is simple but risky with many replicas; a single pre-deploy job is safer.

### Stretch goals
- Canary releases with automatic rollback on error-rate SLO breach.
- Horizontal Pod Autoscaler on request rate, with readiness-aware scale-up.
- Coordinated restarts with CRaC or native images, compared with the AOT cache.

### Skills practised
- [Ch 15 — Observability and production](../chapters/15-observability-and-production/README.md): health groups and Kubernetes probes, graceful shutdown, AOT cache, container images, production checklist.
- [Ch 8 — Spring Data JPA and Hibernate](../chapters/08-spring-data-jpa-and-hibernate/README.md): Flyway migrations.
- [Ch 9 — Spring Data beyond JPA, and caching](../chapters/09-spring-data-beyond-jpa-and-caching/README.md): Flyway beyond the basics.
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): evolving event payloads.

### In the interview
**30-second answer:** I made a Spring Boot accounts service deployable to Kubernetes many times a day with zero failed requests, proved by a 200-requests-per-second load test running during every rollout. The fixes form a chain: readiness probes so new pods get traffic only when ready, a preStop sleep plus graceful shutdown so old pods drain in-flight requests, shutdown timeouts that fit inside the termination grace period, and an AOT cache for pods ready in under 10 seconds. The hardest part was schema and contract changes, which I did with expand/contract — a column rename took three releases so old and new pods always worked against the same schema. Every step could be rolled back without data loss.

**Key terms to know:**
- **Rolling update** — Kubernetes replaces pods gradually, so old and new versions run side by side.
- **Liveness / readiness / startup probe** — restart me / send me traffic / I am still starting; Actuator's `/actuator/health/liveness` and `/readiness`.
- **Availability state** — Boot's `LivenessState` and `ReadinessState`, which drive the probe endpoints.
- **Graceful shutdown** — on SIGTERM the web server stops accepting new requests and finishes in-flight ones; default since Boot 3.4.
- **preStop hook** — a short sleep before SIGTERM so endpoint removal reaches load balancers first.
- **`terminationGracePeriodSeconds`** — how long Kubernetes waits before killing the pod; must exceed preStop plus drain time.
- **Expand/contract (parallel change)** — add new, migrate, remove old across separate releases.
- **Backward-compatible migration** — a schema change that release N and N+1 can both run against.
- **AOT cache / CDS** — JDK features that speed JVM startup using a training run.
- **PodDisruptionBudget** — limits how many pods can be down at once during voluntary disruptions.

**Q:** How do you deploy without failing requests?

**A:** Readiness probes so new pods receive traffic only when ready; a preStop delay so load balancers stop sending to the old pod before it stops accepting connections; graceful shutdown so in-flight requests finish; `spring.lifecycle.timeout-per-shutdown-phase` plus the preStop sleep kept below `terminationGracePeriodSeconds`; and schema and API changes that are backward compatible. Each piece fixed a specific error source I saw in the baseline run.

**Q:** How do you rename a column with zero downtime?

**A:** Expand: add the new column and write both, still reading the old one. Migrate: backfill in batches and switch reads to the new column. Contract: drop the old column only after the previous release is fully gone. That is three deployments, and at every point the previous release still works, so rollback is safe.

**Q:** What should a liveness probe check?

**A:** Only whether the application itself is healthy — for example not deadlocked. External dependencies belong in readiness, if anywhere. If liveness checked the database, a database blip would make Kubernetes restart every pod at once, turning a short incident into an outage.

**Q:** Why is a preStop sleep needed when Spring already shuts down gracefully?

**A:** Kubernetes removes the pod from service endpoints and sends SIGTERM at about the same time, and it takes a moment for load balancers and kube-proxy to notice. Without a delay, graceful shutdown stops accepting new connections while some requests are still being routed to the pod, and they fail. A few seconds of preStop sleep closes that race.

**Q:** What happens if the grace period is shorter than your drain time?

**A:** Kubernetes sends SIGKILL when `terminationGracePeriodSeconds` runs out, killing the pod mid-request, and those requests fail. The budget must cover preStop sleep plus `timeout-per-shutdown-phase` with a margin; I set 5 s preStop, 20 s drain, 30 s grace period.

**Q:** Why run Flyway as a separate job instead of at application startup?

**A:** With many replicas starting at once, each tries to migrate; Flyway's lock serializes them, but startup becomes slow and a failing migration crash-loops every pod. A single pre-deploy job applies only "expand" changes once, before any new pod starts, and the rollout begins only if it succeeds.

**Q:** How do you change an event payload without breaking consumers?

**A:** The same expand/contract order: first deploy consumers that accept both old and new shapes (tolerant readers), then switch producers to the new shape, and only later remove support for the old one. Messages already in the topic in the old format must still be readable, so the old path stays until they have aged out.

**Q:** What changes at 10x scale — 50 replicas?

**A:** Rollout settings matter more: `maxSurge` and `maxUnavailable` control how fast and how much spare capacity is needed, and a PodDisruptionBudget stops node maintenance from taking too many pods at once. Database migrations and backfills on bigger tables must be batched and run outside deployments. Canary releases with automatic rollback on SLO breach become worthwhile.

**Q:** How did you prove zero downtime?

**A:** A k6 load test at 200 requests per second runs throughout each deployment in a local kind cluster, and the pass criterion is zero failed requests. I first recorded the baseline failures and their causes, then fixed them one by one. Migration tests run release N's test suite against release N+1's schema to prove backward compatibility.

**Q:** Show the Kubernetes side of the shutdown settings.

**A:**
```yaml
spec:
  terminationGracePeriodSeconds: 30
  containers:
    - name: accounts
      lifecycle:
        preStop: { exec: { command: ["sh", "-c", "sleep 5"] } }
      readinessProbe: { httpGet: { path: /actuator/health/readiness, port: 8080 } }
      livenessProbe:  { httpGet: { path: /actuator/health/liveness,  port: 8080 } }
```
with `spring.lifecycle.timeout-per-shutdown-phase=20s` on the Spring side.

**Common mistakes in answers:**
- Putting the database in the liveness probe — this causes restart storms.
- Renaming a column in one migration — old pods still use the old name during the rollout.
- "Graceful shutdown alone is enough" — without a preStop delay, requests still arrive after the server stopped accepting them.

### TL;DR
Zero-downtime is a chain: correct probes, preStop and graceful drain, fast startup, and expand/contract changes to schema, APIs and events — each link proved by a load test during deployment.

## 38. Distributed Job Scheduler
**Level:** Advanced · **Time:** 3 weeks · **Prerequisites:** Ch 7, Ch 14

### What it is
A cluster-wide scheduler for an organization's background work: teams register jobs (cron schedules or one-off delayed tasks), and a fleet of identical worker instances executes them with guarantees — each run happens once (or is safely retried), crashed runs are detected and resumed, priorities and per-team quotas are respected, and everything is visible in an API. It uses PostgreSQL as a durable queue with **leases**: a worker claims a run for a limited time and must renew the claim while working.

### The problem it solves
`@Scheduled` runs in every instance (project 6 needed ShedLock for one job). With hundreds of jobs, retries, dependencies and crashes, you need a real scheduler. Building one teaches the core distributed-systems ideas in miniature: leases and heartbeats, at-least-once execution, **fencing tokens** (a number that lets storage reject writes from a worker whose lease has expired), clock skew, and fair scheduling.

### Real-world examples
- [db-scheduler](https://github.com/kagkarlsson/db-scheduler) is a cluster-friendly Java scheduler that needs a single database table and guarantees a task is executed by one instance at a time — a close model for this design.
- The [Quartz Scheduler documentation](https://www.quartz-scheduler.org/documentation/) describes JDBC job stores and clustering, the classic heavyweight alternative.
- [ShedLock](https://github.com/lukas-krecan/ShedLock) shows the simpler "lock a scheduled method" approach and its limits.

### Requirements
**Functional**
- Job definitions: name, owner team, cron or one-off time, handler type, payload, max attempts, timeout, priority.
- Runs: created from definitions by a planner; executed by workers; retried with back-off on failure; dead-lettered after max attempts.
- API: create/pause/resume jobs, trigger now, list runs with status and logs, cancel a running job.
- Handlers are Spring beans implementing `JobHandler`, discovered by name.

**Non-functional**
- 5 worker instances, 50,000 runs per day, claim latency under 1 s for due runs.
- A run whose worker dies is resumed by another worker within 2 × lease duration (lease 30 s).
- No two workers execute the same run concurrently, even with clock skew of a few seconds between machines.
- One team's flood of jobs cannot starve others (per-team concurrency quotas).

### The architecture
```mermaid
flowchart LR
    API["Job API"] --> DEF[("job_definition")]
    PL["Planner - one leader, creates due runs"] --> DEF
    PL --> RUN[("job_run - status, lease_until, fencing_token")]
    W1["Worker 1"] -->|"claim with SKIP LOCKED"| RUN
    W2["Worker 2"] -->|"claim"| RUN
    W1 -->|"heartbeat renews lease"| RUN
    W1 --> H["JobHandler beans"]
    H -->|"writes with fencing token"| TGT[("Target systems")]
    REAP["Reaper - expired leases back to READY"] --> RUN
```
- **Planner** — computes next fire times from cron expressions and inserts `job_run` rows; only one planner is active (leader election via a lease row or ShedLock), and inserts are idempotent with a unique `(definition_id, scheduled_for)` key.
- **Workers** — poll for `READY` runs whose time has come, ordered by priority, claiming a batch with `FOR UPDATE SKIP LOCKED`; set `RUNNING`, owner, `lease_until = now() + 30 s` and increment the **fencing token**.
- **Heartbeat** — renews the lease every 10 s while the handler runs; if renewal fails (lost lease), the worker stops the handler.
- **Reaper** — resets runs whose lease expired back to `READY` (or `FAILED` after max attempts).
- **Handlers** — business code; external writes include the fencing token so stale workers' writes are rejected.

### Data model
```mermaid
erDiagram
    JOB_DEFINITION ||--o{ JOB_RUN : "spawns"
    JOB_RUN ||--o{ RUN_LOG : "writes"
    JOB_DEFINITION {
        uuid id PK
        text name UK
        text team
        text cron
        text handler
        jsonb payload
        int max_attempts
        int priority
        boolean paused
    }
    JOB_RUN {
        uuid id PK
        uuid definition_id FK
        timestamptz scheduled_for
        text status
        int attempt
        text owner
        timestamptz lease_until
        bigint fencing_token
    }
    RUN_LOG {
        bigint id PK
        uuid run_id FK
        timestamptz at
        text message
    }
```
Index: `(status, scheduled_for, priority)` partial on `status = 'READY'`, so claiming stays fast as history grows.

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Queue and state | PostgreSQL, `JdbcClient` | transactions, `SKIP LOCKED`, inspectable |
| Cron | Spring's `CronExpression` | same semantics as `@Scheduled` |
| Execution | virtual threads, per-team semaphores | many waiting jobs, fair quotas |
| Leader election | lease row (or ShedLock) for the planner | one planner at a time |
| Time | database `now()` for leases | avoids trusting worker clocks |

### Milestones
1. **Claim and run.** *Done when* 5 workers in one test process 10,000 one-off runs with no run executed twice concurrently (assert via a table of start/end events).
2. **Cron planning.** *Done when* a planner with a fixed clock creates exactly one run per fire time, idempotently across restarts.
3. **Leases and crash recovery.** *Done when* a worker killed mid-run (stop its executor) has its run picked up by another worker after the lease expires.
4. **Fencing.** *Done when* a paused "zombie" worker that resumes after losing its lease has its write rejected by a target table that checks the fencing token.
5. **Quotas, priorities and API.** *Done when* a flood of 5,000 low-priority runs from team A does not delay team B's high-priority runs beyond 1 s, and runs are visible and cancellable via the API.

### Deep dive: the hard parts
- **Leases, not locks.** A database row lock dies with the connection, but a long job should not hold a transaction open for minutes. A lease is data: "owned by worker 3 until 12:00:30". Renew it by heartbeat; when it expires, someone else may take over.
- **The zombie worker.** A worker paused by a long GC or network partition can wake after its lease expired and another worker took over; both now believe they own the run. Fencing tokens solve it: each claim increments the token, and target writes include it, so storage rejects the stale holder. Where targets cannot check tokens, handlers must be idempotent.
- **Whose clock?** Comparing `lease_until` with each worker's local clock invites skew bugs. Use the database's `now()` for every lease decision, so there is one clock.
- **Fairness.** Pure priority ordering starves low priorities; pure FIFO lets one team flood the queue. Per-team concurrency quotas plus priority within a team, and aging (priority grows with wait time), balance it.

### Testing strategy
- Multi-worker tests in one JVM with separate executors and a shared Testcontainers PostgreSQL.
- Fault injection: kill workers, pause heartbeats, delay database calls.
- Invariant checks after every test: no overlapping executions per run, every due run eventually completes or fails.

### Trade-offs, scaling and failure modes
- A database queue handles tens of thousands of runs a day easily; at millions per hour, polling load and index churn suggest a broker or a dedicated system (Temporal-style workflow engines).
- At-least-once execution is inherent; exactly-once effects require idempotent handlers or fencing.
- The database is a single point of failure for scheduling; that is usually acceptable because the jobs depend on it anyway.

### Stretch goals
- Job dependencies (DAGs): run B after A succeeds.
- Cron in team time zones with daylight-saving handling.
- A UI with run timelines and log streaming over SSE.

### Skills practised
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): `@Scheduled` limits, ShedLock, executors, virtual threads, idempotence.
- [Ch 7 — Data access with JDBC and transactions](../chapters/07-data-access-jdbc-and-transactions/README.md): `JdbcClient`, isolation, short transactions.
- [Ch 4 — AOP and proxies](../chapters/04-aop-and-proxies/README.md): retry policies, and why durable retries live in data.

### In the interview
**30-second answer:** I built a cluster-wide job scheduler on PostgreSQL: a single planner turns cron definitions into run rows idempotently, and five identical workers claim due runs with `FOR UPDATE SKIP LOCKED`, holding a 30-second lease renewed by heartbeat. The key design decision was leases plus fencing tokens rather than locks: if a worker dies, its lease expires and another worker resumes the run, and if a paused "zombie" worker wakes up, its writes carry an old fencing token and are rejected. All lease decisions use the database clock to avoid skew. 5 workers processed 10,000 runs with no run executed twice concurrently, and per-team quotas stop one team's flood from starving others.

**Key terms to know:**
- **Lease** — time-limited ownership stored as data (`owner`, `lease_until`), renewed by heartbeats.
- **Heartbeat** — periodic lease renewal while work is in progress.
- **Fencing token** — a number incremented on each claim; target writes carry it so stale owners are rejected.
- **Zombie worker** — a worker that lost its lease (GC pause, partition) but still thinks it owns the run.
- **`SKIP LOCKED` claiming** — workers take different due rows without blocking each other.
- **Reaper** — resets runs with expired leases to `READY` or `FAILED`.
- **Leader election** — exactly one planner active, via a lease row or ShedLock.
- **At-least-once execution** — a run may execute more than once; handlers must be idempotent.
- **Clock skew** — machines' clocks disagree; solved by using the database's `now()`.
- **Fair scheduling / quotas / aging** — per-team limits and priority that grows with waiting time.

**Q:** How do you make sure a job runs on only one node?

**A:** Workers claim due runs in one transaction with `SELECT ... FOR UPDATE SKIP LOCKED`, set them `RUNNING` with an owner and `lease_until = now() + 30 s`, and commit; other workers skip those rows. The lease is renewed every 10 seconds while the handler runs. If the worker dies, the lease expires and the reaper makes the run available again.

**Q:** What is a fencing token?

**A:** A number incremented on every claim of a run and passed to the handler. Writes to target systems include it, and the target rejects any write with a token lower than the highest it has seen. So a stale worker that lost its lease cannot overwrite the new owner's work, even if it wakes up and keeps going.

**Q:** Why use the database clock?

**A:** Worker clocks drift by seconds; if each worker compared `lease_until` with its own clock, two workers could disagree on whether a lease has expired and both run the job. Using the database's `now()` for every lease decision gives one authoritative clock.

**Q:** Why leases and not a database row lock held during the job?

**A:** A row lock lives as long as a transaction and connection, and jobs can run for minutes; holding transactions that long exhausts the pool and blocks vacuum. A lease is just data, so the claiming transaction is short, and expiry handles crashed workers without needing the connection to drop.

**Q:** A worker is paused by a 40-second GC while its lease is 30 seconds. What happens?

**A:** Its lease expires, the reaper returns the run to `READY`, and another worker claims it with a higher fencing token. When the paused worker resumes, its next heartbeat fails (lease lost), so it stops the handler; any write it attempts in between carries the old token and is rejected. Where a target cannot check tokens, the handler must be idempotent.

**Q:** How does the planner avoid creating duplicate runs?

**A:** Only one planner is active at a time via a leader lease, and run inserts are idempotent with a unique `(definition_id, scheduled_for)` constraint plus `ON CONFLICT DO NOTHING`. Even if two planners briefly overlap after a failover, or the planner restarts mid-cycle, each fire time produces exactly one run.

**Q:** How do you stop one team from starving others?

**A:** Pure priority ordering starves low priorities and pure FIFO lets one team flood the queue. Each team has a concurrency quota enforced by a per-team semaphore in the worker, ordering is by priority within the team, and aging raises priority with waiting time. A test floods 5,000 low-priority runs from team A and checks team B's high-priority runs start within 1 second.

**Q:** What breaks first at 100x — millions of runs per hour?

**A:** Polling load and index churn on `job_run`: constant claims and updates bloat the table and indexes. A partial index on `READY` rows and partitioning history help for a while; beyond that, a message broker or a workflow engine such as Temporal is the right tool. The database is also a single point of failure, usually acceptable because the jobs depend on it anyway.

**Q:** How did you test crash recovery and fencing?

**A:** Multiple workers run in one JVM with separate executors against Testcontainers PostgreSQL. Fault injection stops a worker's executor mid-run and asserts another worker resumes it after the lease expires; another test pauses a worker's heartbeats, lets a second worker take over, then resumes the first and asserts its write is rejected by a target table checking the token. After every test, invariants are checked: no overlapping executions per run.

**Q:** Write the claim query.

**A:**
```sql
UPDATE job_run SET status = 'RUNNING', owner = :worker, attempt = attempt + 1,
       lease_until = now() + interval '30 seconds', fencing_token = fencing_token + 1
WHERE id IN (SELECT id FROM job_run
             WHERE status = 'READY' AND scheduled_for <= now()
             ORDER BY priority DESC, scheduled_for
             LIMIT :batch FOR UPDATE SKIP LOCKED)
RETURNING id, fencing_token;
```

**Common mistakes in answers:**
- "`@Scheduled` plus a lock is a distributed scheduler" — it cannot handle crashes mid-run, retries, quotas or many jobs.
- "Leases guarantee only one executor" — a zombie can still act after expiry; fencing or idempotency is required.
- Using each worker's local clock for lease expiry — clock skew causes double execution.

### TL;DR
A cluster scheduler on PostgreSQL: idempotent planning, `SKIP LOCKED` claims, heartbeat-renewed leases, fencing tokens against zombies, one authoritative clock and fair per-team quotas.

## 39. Payments API with Idempotency Keys
**Level:** Advanced · **Time:** 3 weeks · **Prerequisites:** Ch 5, Ch 7, Ch 14

### What it is
A payments API for merchants — create a payment intent, authorize, capture, refund — that sits in front of an external payment service provider (PSP). Its defining feature is **idempotency**: every mutating request carries an `Idempotency-Key` header, and retrying a request with the same key returns the original result instead of charging again. It also keeps a double-entry ledger of money movements, processes the PSP's asynchronous webhooks, and reconciles daily with the PSP's settlement report.

### The problem it solves
Networks fail in the worst place: after the server charged the card but before the client received the response. The client cannot know whether to retry. Idempotency keys make retries safe, which in turn lets clients retry aggressively. Payments also teach state machines, external asynchronous confirmations and reconciliation — the patterns every money-moving system needs.

### Real-world examples
- Stripe's post ["Designing robust and predictable APIs with idempotency"](https://stripe.com/blog/idempotency) explains idempotency keys and client retries with exponential back-off and jitter.
- The IETF draft ["The Idempotency-Key HTTP Header Field"](https://datatracker.ietf.org/doc/draft-ietf-httpapi-idempotency-key-header/) standardizes the header and the expected server behaviour.
- Shopify's ["10 Tips for Building Resilient Payment Systems"](https://shopify.engineering/building-resilient-payment-systems) adds timeouts, circuit breakers and reconciliation from a large payments operation.

### Requirements
**Functional**
- `POST /payments` (create intent), `POST /payments/{id}/authorize`, `/capture`, `/refunds` — all require `Idempotency-Key`.
- Same key + same request → same stored response (status and body). Same key + different body → 422. Same key while the first is still in progress → 409.
- Payment states: `CREATED → AUTHORIZED → CAPTURED → (PARTIALLY_)REFUNDED`, plus `FAILED`, `CANCELLED`.
- PSP webhooks update states; duplicates and out-of-order webhooks are handled.
- Ledger entries for every capture and refund; daily reconciliation report of mismatches.

**Non-functional**
- No double charge under client retries, concurrent duplicates or server crashes — proved by tests.
- Idempotency records kept for 24 hours.
- PSP calls time out after 5 s; unknown outcomes are resolved by status queries, never by blind retries.
- p95 under 400 ms excluding PSP time.

### The architecture
```mermaid
flowchart TD
    M["Merchant client"] -->|"Idempotency-Key"| IF["IdempotencyFilter"]
    IF --> IK[("idempotency_key table - status, request hash, response")]
    IF --> PC["PaymentController"]
    PC --> PS["PaymentService - state machine"]
    PS --> PSP["PSP client - timeouts, circuit breaker"]
    PS --> DB[("payments, ledger_entry")]
    WH["Webhook endpoint - signature check"] --> PS
    REC["Daily reconciliation job"] --> DB
    REC --> PSPR["PSP settlement report"]
    PS -->|"outbox"| EV["PaymentCaptured events"]
```
- **IdempotencyFilter** — inserts `(merchant, key)` with status `IN_PROGRESS` and a hash of the request; on conflict, returns the stored response, 409 (still in progress) or 422 (different request).
- **PaymentService** — validates transitions explicitly; each transition is one local transaction.
- **PSP client** — passes your own idempotency key to the PSP, so even your retries are safe at the provider.
- **Webhook endpoint** — verifies signatures, de-duplicates by event id, applies only valid forward transitions.
- **Ledger** — double-entry rows (merchant balance, fees, PSP clearing) written in the same transaction as the state change.
- **Reconciliation** — compares the day's captures and refunds with the PSP settlement file and flags differences.

### Data model
```mermaid
erDiagram
    PAYMENT ||--o{ LEDGER_ENTRY : "posts"
    PAYMENT ||--o{ REFUND : "has"
    IDEMPOTENCY_KEY {
        text merchant_id PK
        text idem_key PK
        text request_hash
        text status
        int response_status
        jsonb response_body
        timestamptz expires_at
    }
    PAYMENT {
        uuid id PK
        text merchant_id
        numeric amount
        text currency
        text status
        text psp_reference UK
        bigint version
    }
    REFUND {
        uuid id PK
        uuid payment_id FK
        numeric amount
        text status
    }
    LEDGER_ENTRY {
        bigint id PK
        uuid payment_id FK
        text account
        numeric debit
        numeric credit
        timestamptz posted_at
    }
```

### Tech stack
| Component | Choice | Why |
|---|---|---|
| Web | Spring MVC + a servlet filter for idempotency | applies uniformly to all mutating endpoints |
| Data | PostgreSQL, `JdbcClient` for idempotency rows, JPA for payments | unique keys and row-level atomicity |
| PSP client | `RestClient` + Resilience4j breaker; Framework 7 `@Retryable` only for safe status queries | never blind-retry charges |
| Events | transactional outbox to Kafka | reliable downstream notifications |
| Tests | stub PSP with scripted timeouts and webhooks | reproduce ambiguous failures |

### Milestones
1. **Idempotency layer.** *Done when* tests cover same-key replay, different-body 422, concurrent in-flight 409 (two threads, one key), and expiry.
2. **State machine.** *Done when* a transition table test rejects every invalid transition (e.g. capture before authorize, refund above captured amount).
3. **PSP integration with unknown outcomes.** *Done when* a PSP timeout on capture leaves the payment `CAPTURE_PENDING`, and a status query resolves it without a second capture.
4. **Webhooks.** *Done when* duplicated and out-of-order webhooks lead to the correct final state, and unsigned ones are rejected.
5. **Ledger and reconciliation.** *Done when* ledger entries always balance (property test) and a seeded mismatch with a fake settlement file appears in the report.

### Deep dive: the hard parts
- **Concurrent duplicates.** Two requests with the same key can arrive together. The unique `(merchant, key)` insert decides: the winner processes; the loser sees `IN_PROGRESS` and gets 409 with `Retry-After`. Checking "does the key exist?" before inserting would let both through.
- **Crash during processing.** If the server dies after the PSP charged but before storing the response, the key stays `IN_PROGRESS`. Recovery must query the PSP (using the same PSP idempotency key) rather than charge again; a sweeper resolves stale `IN_PROGRESS` keys.
- **Request fingerprinting.** Hash a canonical form of the body and relevant headers; a client reusing a key for a different amount is a bug that must fail loudly (422), not silently return the old result.
- **Webhooks vs API responses.** The PSP may report success via webhook before your API call returns, or never return at all. Treat webhooks and API responses as two inputs to the same state machine, accept only forward transitions, and make both idempotent.

### Testing strategy
- Concurrency tests on the idempotency layer with a start latch.
- Failure-injection tests at each step (before PSP call, after PSP call, before response stored).
- Property-based tests for ledger balance and state-machine invariants.

### Trade-offs, scaling and failure modes
- Storing full responses for 24 hours costs space; it is far cheaper than double charges. Partition or expire the table.
- Idempotency at the API layer does not remove the need for idempotency at the PSP and in event consumers — every hop needs it.
- Strict state machines reject odd real-world sequences (a refund webhook for a capture you missed); route those to manual review instead of crashing.

### Stretch goals
- Multi-PSP routing with failover when one provider's circuit is open.
- Strong customer authentication (3-D Secure) flow with redirects.
- Merchant-facing webhooks using project 24's delivery service.

### Skills practised
- [Ch 7 — Data access with JDBC and transactions](../chapters/07-data-access-jdbc-and-transactions/README.md): unique constraints, atomic inserts, transactions.
- [Ch 5 — Spring MVC and REST APIs](../chapters/05-spring-mvc-and-rest/README.md): filters, status codes (409, 422), Problem Details.
- [Ch 14 — Messaging, async and scheduling](../chapters/14-messaging-async-and-scheduling/README.md): outbox, idempotent consumers, scheduled reconciliation.
- [Ch 16 — Spring Cloud and microservices](../chapters/16-spring-cloud-and-microservices/README.md): circuit breakers for the PSP.

### In the interview
**30-second answer:** I built a payments API in front of an external provider where every mutating request needs an `Idempotency-Key`, so a client can retry safely and never be charged twice. The key design decision was an atomic insert of `(merchant, key)` with a request hash and `IN_PROGRESS` status before any work: a retry gets the stored response, a different body with the same key gets 422, and a concurrent duplicate gets 409. The hardest problem was ambiguous failures — a PSP timeout after the card may have been charged — which I resolve by querying the provider with my own idempotency reference, never by blindly retrying. Ledger entries always balance, and a daily reconciliation against the settlement file catches anything missed.

**Key terms to know:**
- **Idempotency key** — a client-chosen unique key that makes a repeated request return the first result.
- **Request fingerprint** — a hash of the canonical request body used to detect key reuse with different content.
- **`IN_PROGRESS` record** — marks a key as being processed so concurrent duplicates get 409.
- **Payment state machine** — explicit allowed transitions (`CREATED → AUTHORIZED → CAPTURED → REFUNDED`).
- **Authorize vs capture** — reserving funds on the card vs actually taking them.
- **Unknown outcome** — a timeout where the remote side may or may not have acted; resolved by status query.
- **PSP webhook** — the provider's asynchronous notification of state changes; verified, de-duplicated, applied forward-only.
- **Double-entry ledger** — every money movement recorded as balanced debits and credits.
- **Reconciliation** — comparing your records with the provider's settlement report.
- **Transactional outbox** — reliable publication of `PaymentCaptured` events.

**Q:** How do idempotency keys work?

**A:** Before doing any work, the filter inserts `(merchant, key)` with the request hash and status `IN_PROGRESS`. The unique constraint decides who wins: the winner processes and stores the final status and body; a later retry with the same key and hash gets the stored response; the same key with a different hash gets 422; a duplicate arriving while the first is in progress gets 409 with `Retry-After`. Records expire after 24 hours.

**Q:** The PSP call timed out. Do you retry the charge?

**A:** No — the outcome is unknown: the PSP may have captured the money and only the response was lost. The payment moves to `CAPTURE_PENDING`, and I query the PSP for the status using the idempotency reference I sent with the original call, then proceed based on the answer. Because I always pass my own idempotency key to the PSP, even a retry at the provider would be safe.

**Q:** Why reconcile if you already process webhooks?

**A:** Webhooks can be missed, delayed, duplicated or wrong, and my own state can have bugs. Reconciliation against the provider's settlement report is an independent check that money records match reality; mismatches go to a report for review. It catches the problems no single request path can see.

**Q:** Why not check "does this key exist?" before inserting?

**A:** Two concurrent requests with the same key would both see "no" and both charge. Only the atomic insert with a unique constraint decides the winner in one step. A concurrency test with a start latch sends two identical requests and asserts one payment and one PSP call.

**Q:** The server crashes after the PSP charged but before storing the response. What happens?

**A:** The idempotency record stays `IN_PROGRESS` and the payment is not marked captured. A sweeper finds stale `IN_PROGRESS` keys, queries the PSP using the same reference, updates the payment and stores the response. The client's retry then gets that stored response — no second charge.

**Q:** A webhook says "refunded" before your API even saw the capture. What do you do?

**A:** Webhooks and API responses are two inputs to the same state machine, and it accepts only valid forward transitions. Out-of-order or duplicate webhooks are de-duplicated by event id and applied when they make sense; an impossible sequence, like a refund for a capture we never recorded, goes to manual review instead of crashing or being ignored.

**Q:** Why return 422 when the same key is reused with a different body?

**A:** It is a client bug — for example reusing a key for a different amount — and silently returning the old result would make the client believe the new payment succeeded. Failing loudly with 422 Problem Details forces the bug to be fixed. The hash covers a canonical form of the body so formatting differences do not count.

**Q:** What changes at 10x volume?

**A:** The idempotency table grows with every request, so it is partitioned by day and old partitions dropped after 24 hours. PSP rate limits and outages matter more, so a circuit breaker fails fast and, as a stretch, a second PSP takes over. Events go through the outbox so downstream consumers scale independently.

**Q:** How do you prove "no double charge"?

**A:** Concurrency tests with a start latch on the idempotency layer, failure-injection tests at each step (before the PSP call, after it, before storing the response) using a stub PSP with scripted timeouts, and property-based tests that the ledger always balances and the state machine never makes an invalid transition. Each test counts PSP capture calls.

**Q:** Write the idempotency insert.

**A:**
```sql
INSERT INTO idempotency_key (merchant_id, idem_key, request_hash, status, expires_at)
VALUES (:merchant, :key, :hash, 'IN_PROGRESS', now() + interval '24 hours')
ON CONFLICT (merchant_id, idem_key) DO NOTHING;
-- 1 row inserted: we own the key and process the request
-- 0 rows: read the existing row -> stored response, 409 or 422
```

**Common mistakes in answers:**
- "Retry the charge on timeout" — an unknown outcome must be resolved by a status query, not a second charge.
- Keying idempotency only by the key, not by merchant — two merchants could collide.
- "Idempotency at the API is enough" — the PSP call and every event consumer need it too.

### TL;DR
Safe money movement: atomic idempotency records with request fingerprints, explicit state machines fed by both API responses and webhooks, status queries instead of blind retries, a balanced ledger and daily reconciliation.

## 40. Backend-for-Frontend with Token Relay
**Level:** Advanced · **Time:** 3 weeks · **Prerequisites:** Ch 6, Ch 11, Ch 16

### What it is
A **Backend-for-Frontend (BFF)** for a single-page app (React 19 or similar): a server component, built on Spring Cloud Gateway, that performs the OAuth2 login on behalf of the browser, keeps tokens **server-side** in a session, gives the browser only a secure, HttpOnly session cookie, and forwards API calls to downstream resource servers with the user's access token attached (**token relay**). It also shapes and aggregates responses for the UI.

### The problem it solves
SPAs that store access and refresh tokens in JavaScript are exposed to theft through cross-site scripting (XSS); the current IETF guidance for browser-based apps favours a BFF for sensitive applications. A BFF also lets the front end call one origin (no CORS), lets the back end tailor responses per UI, and centralizes token refresh. The cost: cookies bring back CSRF, sessions bring back state, and the BFF must be operated.

### Real-world examples
- The IETF draft ["OAuth 2.0 for Browser-Based Applications"](https://datatracker.ietf.org/doc/draft-ietf-oauth-browser-based-apps/) compares architectures for browser apps and describes the BFF pattern and its security properties.
- Sam Newman's article ["Backends For Frontends"](https://samnewman.io/patterns/architectural/bff/) introduced BFF as a per-UI back end that shapes APIs for one client.
- The [Spring Cloud Gateway reference](https://docs.spring.io/spring-cloud-gateway/reference/) documents the `TokenRelay` filter used with OAuth2 login.

### Requirements
**Functional**
- `GET /` serves the SPA's static files (or a CDN does, same site); `/bff/login` starts OAuth2 login with the IdP (project 34 or Keycloak); `/bff/user` returns the current user's profile for the UI; `/bff/logout` ends local and IdP sessions.
- `/api/orders/**` and `/api/profile/**` are relayed to two resource servers with the access token; tokens refresh transparently.
- `/bff/dashboard` aggregates three downstream calls into one UI-shaped response.

**Non-functional**
- No token (access, refresh or ID) is ever readable by browser JavaScript.
- CSRF protection on every state-changing request from the SPA.
- Session cookie: `Secure`, `HttpOnly`, `SameSite=Lax` (or `Strict`), host-only, with a `__Host-` prefix.
- Sessions shared across BFF instances (Redis); added latency per relayed call under 10 ms p95.

### The architecture
```mermaid
sequenceDiagram
    participant B as Browser SPA
    participant F as BFF - Gateway with oauth2Login
    participant I as IdP
    participant R as Orders resource server
    B->>F: GET /bff/login
    F->>B: 302 to IdP with PKCE
    B->>I: login
    I->>B: 302 back with code
    B->>F: callback with code
    F->>I: exchange code for tokens
    F->>F: store tokens in session - Redis
    F->>B: Set-Cookie session HttpOnly, CSRF cookie
    B->>F: GET /api/orders with cookie and CSRF header
    F->>R: GET /orders with Bearer access token
    R->>F: 200 JSON
    F->>B: 200 JSON
```
- **BFF** — Spring Cloud Gateway server (WebMVC flavour) with `oauth2Login()`; routes `/api/**` with the `TokenRelay` filter, which looks up the user's authorized client and attaches (and refreshes) the access token.
- **Session store** — Spring Session with Redis, so any BFF instance can serve any user.
- **CSRF** — Security 7's `csrf(c -> c.spa())` configuration: a readable CSRF cookie the SPA echoes in a header, with the token handling SPAs need.
- **Resource servers** — unchanged JWT validators (projects 11 and 12); they never see cookies.
- **Aggregation routes** — a small MVC controller inside the BFF calling downstream HTTP service interfaces in parallel with the user's token.

### Data model
| Item | Where | Notes |
|---|---|---|
| Session (`SPRING_SESSION` in Redis) | BFF | contains the `OAuth2AuthorizedClient` (access + refresh tokens) |
| Session cookie `__Host-SESSION` | browser | opaque id only |
| CSRF cookie `XSRF-TOKEN` | browser | readable by JS by design, echoed as `X-XSRF-TOKEN` |
| Routes | BFF config | `/api/orders/** → lb://orders`, `/api/profile/** → lb://profile` with `TokenRelay` |

### Tech stack
| Component | Choice | Why |
|---|---|---|
| BFF | `spring-cloud-starter-gateway-server-webmvc` + `spring-boot-starter-security-oauth2-client` | routing, `TokenRelay`, OAuth2 login |
| Sessions | Spring Session Data Redis (`spring.session.data.redis.*` in Boot 4) | stateless BFF instances |
| CSRF | Spring Security 7 `csrf.spa()` | cookie-based SPA protection |
| Front end | React 19 SPA (see the React book), same-site origin | no CORS, cookies flow naturally |
| Downstream | JWT resource servers | unchanged |

### Milestones
1. **Login and session.** *Done when* the SPA shows the user's name from `/bff/user` after login, and browser dev tools show no tokens anywhere (storage, cookies, responses).
2. **Token relay.** *Done when* `/api/orders` reaches the orders service with a bearer token, and an expired access token is refreshed transparently (shorten lifetimes in the IdP to test).
3. **CSRF.** *Done when* a `POST` without the CSRF header gets 403 and the SPA's normal requests succeed.
4. **Scale-out.** *Done when* two BFF instances behind a load balancer serve the same user alternately without re-login.
5. **Logout and aggregation.** *Done when* logout clears the session and the IdP session, and `/bff/dashboard` combines three calls within one round trip.

### Deep dive: the hard parts
- **Why tokens off the browser helps.** XSS in a token-holding SPA lets the attacker exfiltrate a refresh token and use it from anywhere for days. With a BFF, XSS can still make requests **through** the user's browser while the page is open, but cannot steal long-lived credentials. That is a real reduction, not a cure — Content Security Policy and output encoding still matter.
- **Cookies bring CSRF.** Because the browser now attaches credentials automatically, another site could trigger requests. `SameSite` cookies block most cross-site sends; a CSRF token checked on state-changing requests closes the rest. Keep both.
- **Refresh at the edge.** The BFF must refresh tokens before relaying, handle refresh failure by returning 401 to the SPA (which then restarts login), and avoid refreshing the same token concurrently from two instances — let the IdP's rotation and the session's atomic update decide, and tolerate one benign retry.
- **Audience and least privilege.** Relaying one token to many services means each must accept the same audience. For sensitive services, use token exchange to obtain a narrower token per downstream API.

### Testing strategy
- Security tests with `oauth2Login()` post-processors for routes and CSRF rules.
- `MockRestServiceServer` or stub resource servers asserting the relayed `Authorization` header.
- An end-to-end browser test (Playwright or similar) with a real IdP container for login, refresh and logout.

### Trade-offs, scaling and failure modes
- A BFF adds a hop and a stateful component; Redis availability now gates logins and API calls.
- One BFF per front end keeps each simple but multiplies deployments; a shared gateway for many UIs drifts back toward a generic API gateway.
- For public, low-risk apps, a pure SPA with PKCE and in-memory tokens may be acceptable; document why you chose the BFF.

### Stretch goals
- Token exchange per downstream service with narrower audiences.
- Step-up authentication: require MFA before `/api/payments/**`.
- Response caching for aggregated dashboards with per-user keys.

### Skills practised
- [Ch 11 — Tokens and OAuth2](../chapters/11-spring-security-2-tokens-and-oauth2/README.md): OAuth2 login, token relay, refresh and logout, the BFF pattern.
- [Ch 16 — Spring Cloud and microservices](../chapters/16-spring-cloud-and-microservices/README.md): Spring Cloud Gateway routes and filters.
- [Ch 10 — Spring Security fundamentals](../chapters/10-spring-security-1-fundamentals/README.md): CSRF, sessions, security headers.
- [Ch 6 — REST clients and HTTP interfaces](../chapters/06-rest-clients-and-http-interfaces/README.md): aggregation with HTTP service interfaces.

### In the interview
**30-second answer:** I built a Backend-for-Frontend for a React SPA on Spring Cloud Gateway: it performs the OAuth2 login with PKCE, keeps all tokens in a Redis-backed server-side session, gives the browser only an HttpOnly `__Host-` session cookie, and relays the user's access token to downstream resource servers with the `TokenRelay` filter. The key reason is security: XSS can no longer steal long-lived tokens because JavaScript never sees them. The hardest part was the risks cookies bring back — CSRF, handled with `SameSite` plus Security 7's `csrf.spa()` token — and transparent token refresh across several BFF instances. No token is ever readable in the browser, and relayed calls add under 10 ms p95.

**Key terms to know:**
- **Backend-for-Frontend (BFF)** — a server built for one UI that handles auth and shapes APIs for it.
- **Token relay** — attaching the logged-in user's access token to downstream calls; the Gateway `TokenRelay` filter.
- **`OAuth2AuthorizedClient`** — Spring's record of a user's access and refresh tokens for a client registration.
- **HttpOnly / Secure / SameSite cookies** — not readable by JavaScript / HTTPS only / not sent on most cross-site requests.
- **`__Host-` prefix** — forces a cookie to be Secure, host-only and path `/`.
- **XSS** — injected script running in your page.
- **CSRF** — another site making the browser send a request with your cookies; blocked with SameSite and CSRF tokens.
- **Spring Session with Redis** — shared session storage so any BFF instance can serve any user.
- **Same-origin API** — the SPA calls its own origin, so no CORS is needed.
- **Token exchange (RFC 8693)** — obtaining narrower tokens per downstream service.

**Q:** Why use a BFF instead of storing tokens in the SPA?

**A:** Tokens stay on the server, so an XSS bug cannot exfiltrate a refresh token and use it from elsewhere for days; the browser holds only an HttpOnly session cookie. It also gives the SPA one origin (no CORS), centralizes token refresh, and lets the back end aggregate and shape responses for the UI. Current IETF guidance for browser apps favours this for sensitive applications.

**Q:** What new risk does the BFF introduce?

**A:** Cookie-based authentication brings CSRF back, because the browser attaches the cookie automatically to requests other sites trigger. I keep `SameSite=Lax` (or `Strict`) cookies, which block most cross-site sends, and a CSRF token on every state-changing request, which Security 7's `csrf(c -> c.spa())` sets up for SPAs. It also adds a stateful hop that must be operated.

**Q:** How does token relay work in Spring Cloud Gateway?

**A:** The `TokenRelay` filter looks up the logged-in user's `OAuth2AuthorizedClient` from the session, refreshes the access token with the refresh token if it is expired, and adds it as `Authorization: Bearer` to the downstream request. Resource servers validate it as usual and never see the cookie.

**Q:** Does a BFF make XSS harmless?

**A:** No. Script injected into the page can still make requests through the user's browser while the page is open, and the BFF will happily relay them. What it prevents is theft of long-lived credentials that would let the attacker act from anywhere later. Content Security Policy, output encoding and dependency hygiene still matter.

**Q:** Why are tokens refreshed at the BFF, and what goes wrong with several instances?

**A:** The BFF owns the tokens, so it must refresh before relaying; if refresh fails it returns 401 and the SPA restarts login. With several instances, two requests for the same user might refresh the same refresh token at once; with rotation, one of them gets an invalid token. Atomic session updates in Redis and tolerating one benign retry handle it.

**Q:** Redis goes down. What happens?

**A:** Sessions are unavailable, so no instance can find users' tokens: logins and relayed API calls fail. Redis availability now gates the whole UI, so it needs replication and failover, and the BFF should fail fast with a clear error rather than hang. This is the main operational cost of the pattern.

**Q:** Why the `__Host-` cookie prefix?

**A:** Browsers accept a `__Host-` cookie only if it is `Secure`, has no `Domain` attribute (host-only) and has `Path=/`. That stops a compromised or malicious subdomain from setting or overwriting the session cookie for the main host.

**Q:** What changes at 10x users?

**A:** BFF instances scale horizontally because sessions are in Redis; Redis memory grows with active sessions and their tokens, so session timeouts matter. Each relayed call adds a hop, so the BFF must stay thin and use connection pooling. If many UIs share one BFF it drifts into a generic API gateway — one BFF per front end keeps each simple.

**Q:** How do you test the BFF?

**A:** Security tests use the `oauth2Login()` request post-processor to fake a logged-in user, checking routes and that a `POST` without the CSRF header gets 403. Stub resource servers assert the relayed `Authorization` header. An end-to-end browser test with Playwright and a real IdP container covers login, refresh and logout, and inspects that no tokens appear in storage, cookies or responses.

**Q:** Show the SPA CSRF configuration.

**A:**
```java
@Bean SecurityFilterChain bff(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(a -> a.requestMatchers("/", "/assets/**").permitAll().anyRequest().authenticated())
        .oauth2Login(Customizer.withDefaults())
        .csrf(c -> c.spa())   // readable XSRF-TOKEN cookie, echoed as X-XSRF-TOKEN
        .build();
}
```

**Common mistakes in answers:**
- "HttpOnly cookies remove the need for CSRF protection" — HttpOnly stops reading, not sending; CSRF defences are still required.
- "The BFF passes the session cookie to downstream APIs" — downstream services receive bearer tokens, never browser cookies.
- "A BFF fixes XSS" — it limits token theft but injected script can still act while the page is open.

### TL;DR
A gateway-based BFF that logs users in, keeps tokens in a Redis-backed session, relays and refreshes them for downstream APIs, and protects its cookies with `SameSite`, HttpOnly and CSRF tokens.

## Appendix: Concepts you need

This appendix lists, in alphabetical order, every technical term, acronym, library and tool that the 40 briefs above rely on, each explained in plain English in a sentence or two. After each definition you'll find the projects that use the term and, where this handbook teaches it, a link to the chapter. System-design ideas such as latency, caching, replication, queues and idempotency are explained in more depth in [System Design Fundamentals](../../system-design-patterns/modules/00-system-design-fundamentals/README.md).

### A

- **`AbstractRoutingDataSource`** — A `DataSource` that picks a real target (e.g. per tenant or replica) for each connection request. Used in project 26. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **Acceptance criteria / "done when"** — The concrete, testable condition that tells you a milestone is finished. Used in all 40 projects.
- **Access token** — Short-lived credential a client sends (usually as a bearer token) to call an API on a user's or its own behalf. Used in projects 11, 12, 26, 34, 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **ACID** — The guarantees of a database transaction: Atomic (all or nothing), Consistent, Isolated from others, Durable once committed. Used in project 13. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **Actuator (Spring Boot)** — Boot's production endpoints under `/actuator` for health, metrics, configuration, loggers and more. Used in projects 3, 5, 9, 10, 14, 29, 32, 33, 37. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Advice order (`@Order`)** — When several aspects or filters apply, `@Order` decides nesting: the lowest value is the outermost layer. Used in projects 9, 34. ([Ch 4](../chapters/04-aop-and-proxies/README.md))
- **Agent (AI)** — An LLM application that loops: the model decides which tool to call, the app runs it, and the result goes back to the model until the task is done. Used in project 36. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **Aggregate / event stream** — A cluster of objects changed together through one root; in event sourcing, each aggregate has its own ordered stream of events. Used in projects 16, 17, 28, 35. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))
- **Aging (scheduling)** — Raising the priority of work the longer it waits, so low-priority jobs are never starved. Used in project 38.
- **Allow-list** — A list of explicitly permitted values (sort fields, hosts, issuers); anything not on it is rejected. Used in projects 7, 9, 15, 25, 36. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **AMQP** — Advanced Message Queuing Protocol, spoken by RabbitMQ; Spring AMQP is Spring's library for it. Used in project 17. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **ANN index (HNSW)** — Approximate-nearest-neighbour index that finds similar vectors quickly by trading a little accuracy for speed; HNSW is a common graph-based one. Used in project 30. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **Apache Tika** — Library that detects a file's real type (and extracts text) from its content. Used in project 15.
- **API** — Application Programming Interface: the operations a program offers to others; here usually HTTP endpoints. Used in projects 1, 2, 3, 4, 6, 7, 8, 11, 12, 15, 16, 17, 18, 19, 21, 22, 24, 25, 26, 27, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **API gateway** — A single entry point in front of services that routes requests and handles edge concerns such as authentication, rate limits and CORS. Used in projects 21, 22, 27, 40. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **API key** — A secret string identifying a calling application; store only its hash, like a password. Used in projects 2, 3, 22, 25. ([Ch 10](../chapters/10-spring-security-1-fundamentals/README.md))
- **API versioning (Framework 7)** — Framework 7 support for reading a version from a header, path or query parameter and routing to handlers mapped with `version = "1.1+"`. Used in projects 3, 7, 21, 37. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **`ApiVersionStrategy`** — Framework 7 component that resolves, parses and validates the API version of a request. Used in project 21. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Application events / `ApplicationEventPublisher`** — In-process publish/subscribe: a bean publishes an object and `@EventListener` methods receive it, synchronously by default. Used in projects 9, 31. ([Ch 2](../chapters/02-ioc-and-dependency-injection/README.md))
- **Application module (Spring Modulith)** — A top-level package of a modular monolith with its own public API; other modules may use only that API. Used in projects 9, 31.
- **`ApplicationContextRunner`** — Test helper that starts a tiny Spring context with chosen configuration and properties, ideal for testing auto-configuration conditions. Used in projects 5, 9, 25. ([Ch 3](../chapters/03-configuration-and-auto-configuration/README.md))
- **`@ApplicationModuleListener`** — Spring Modulith listener that runs asynchronously in its own transaction after the publisher commits. Used in project 31.
- **`ApplicationModules.verify()`** — Spring Modulith check that fails a test when one module reaches into another module's internals or modules depend on each other in a cycle. Used in project 31.
- **`@ApplicationModuleTest` / `Scenario`** — Spring Modulith test that boots only one module; `Scenario` publishes an event and waits for the expected outcome. Used in project 31.
- **`@Around` advice / `proceed()`** — The AOP advice type that wraps a method call; it decides whether and when the real method runs by calling `proceed()`. Used in project 9. ([Ch 4](../chapters/04-aop-and-proxies/README.md))
- **`@Async`** — Runs a bean method on another thread from an executor and returns immediately; needs `@EnableAsync` and works only through the proxy. Used in projects 2, 15, 26, 29, 31. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **At-least-once delivery / execution** — Every message or job is processed, but a crash can cause a repeat, so the processing must be idempotent. Used in projects 16, 17, 24, 28, 31, 35, 38. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Atomic** — All or nothing: either the whole operation happens or none of it does, and nobody sees a half-done state. Used in projects 1, 2, 6, 8, 10, 13, 15, 22, 23, 25, 28, 35, 39, 40. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **Atomic file write** — Write to a temporary file and rename it into place, so readers never see a half-written file. Used in projects 6, 15.
- **Audience (`aud`)** — JWT claim naming the API a token is meant for; an API should reject tokens issued for another audience. Used in projects 11, 12, 22, 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Audit log** — An append-only record of who did what and when, kept for security and compliance. Used in projects 4, 9, 13, 20, 24, 25, 34, 35, 36.
- **Authentication vs authorization** — Authentication proves who the caller is; authorization decides what that known caller may do. Used in projects 11, 12, 22, 26, 27, 30, 34, 36, 39, 40. ([Ch 10](../chapters/10-spring-security-1-fundamentals/README.md))
- **Authorization code flow with PKCE** — OAuth2 login flow: the browser receives a one-time code and the client swaps it for tokens; PKCE ties the code to the client that started the flow. Used in projects 12, 34, 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Authorization server / IdP** — The service that logs users in and issues tokens (Keycloak, Okta, Spring Authorization Server). Used in projects 11, 12, 26, 34, 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Authorize vs capture (payments)** — Authorizing reserves money on the card; capturing actually takes it, often later when the goods ship. Used in projects 26, 27, 28, 39.
- **Auto-configuration** — Boot's conditional configuration classes that create sensible beans for whatever is on the classpath, unless you define your own. Used in projects 3, 4, 5, 8, 9, 17, 25, 29, 32. ([Ch 3](../chapters/03-configuration-and-auto-configuration/README.md))
- **`AutoConfiguration.imports`** — The file inside a jar (`META-INF/spring/...AutoConfiguration.imports`) listing its auto-configuration classes. Used in project 9. ([Ch 3](../chapters/03-configuration-and-auto-configuration/README.md))
- **Availability state** — Boot's internal liveness and readiness flags, which the health probes report and the app can change at runtime. Used in project 37. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Awaitility** — Test library that waits until an asynchronous condition becomes true, instead of a fixed `sleep`. Used in projects 15, 16, 20. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **AWS SDK / S3** — Amazon's Java library and its object-storage service; MinIO offers the same S3 API locally. Used in projects 15, 18.
- **Axon** — Java framework for event sourcing and CQRS. Used in project 35.

### B

- **Back-pressure** — A consumer limiting how fast a producer sends data, so a fast source cannot overwhelm a slow sink. Used in projects 16, 19. ([Ch 13](../chapters/13-reactive-and-webflux/README.md))
- **Backend-for-Frontend (BFF)** — A server dedicated to one front end that logs the user in, keeps tokens server-side and gives the browser only a session cookie. Used in projects 11, 12, 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Backward-compatible change** — A change old clients or old code can live with, such as adding an optional field or a new nullable column. Used in projects 7, 37. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Base62 code** — A short id written with the 62 characters 0-9, a-z, A-Z, as used by URL shorteners. Used in project 2.
- **Baseline version (`1.1+`)** — A version mapping that also matches all later versions, so a handler need not be copied for every release. Used in project 21. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Bean** — An object created, wired and managed by the Spring container. Used in projects 1, 3, 5, 9, 11, 13, 14, 17, 20, 21, 24, 25, 26, 27, 30, 31, 32, 33, 36, 38. ([Ch 2](../chapters/02-ioc-and-dependency-injection/README.md))
- **Bean Validation (`@Valid`)** — Jakarta standard for declaring input rules with annotations such as `@NotBlank` and `@Size`; Spring runs them before your method. Used in projects 1, 7, 18. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Best-effort** — Done when possible but not guaranteed; losing an occasional update is accepted. Used in project 2.
- **`BigDecimal` / `numeric`** — Exact decimal number types in Java and SQL; required for money because floating-point types round. Used in projects 6, 8, 19, 35.
- **Blocking vs non-blocking retry** — Blocking retries hold the partition while waiting; non-blocking retries move the message to a retry topic so others keep flowing. Used in project 16. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **BOM (bill of materials)** — A Maven file that pins compatible versions of many libraries; Boot's BOM manages most versions for you. Used in projects 20, 27, 30. ([Ch 1](../chapters/01-spring-ecosystem-and-first-app/README.md))
- **Breaking vs additive change** — A breaking change forces clients to change (removed field, new required input); an additive one does not. Used in projects 5, 7, 21, 37. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Broker (message broker)** — A server that receives messages from producers and delivers them to consumers (Kafka, RabbitMQ). Used in projects 12, 16, 17, 20, 28, 31, 38. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Bucket4j** — Java rate-limiting library implementing the token-bucket algorithm, with Redis support for shared limits. Used in project 22. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Buildpacks** — Tools that turn an application into a container image without a Dockerfile (`mvn spring-boot:build-image`). Used in projects 32, 37. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Bulkhead** — Limiting concurrent calls to one dependency so it cannot use up all threads or connections. Used in projects 27, 33. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **Burn rate / error budget** — Error budget = how many failures an SLO allows; burn rate = how fast you are using that budget up. Used in project 29. ([Ch 15](../chapters/15-observability-and-production/README.md))

### C

- **Cache abstraction (`@Cacheable`, `@CachePut`, `@CacheEvict`)** — Spring annotations that read from, update or remove entries in a cache around a method call; the storage behind them is pluggable. Used in projects 2, 3, 14. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))
- **Cache stampede (thundering herd)** — Many requests miss the same expired key at once and all hit the database together. Used in projects 14, 24. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))
- **Cache-aside (look-aside)** — Check the cache first; on a miss load from the database, store the result and return it. Used in projects 2, 14. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))
- **`CacheErrorHandler`** — Spring hook that decides what happens when the cache itself fails, e.g. log and fall back to the database. Used in project 14. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))
- **Caffeine** — Fast in-process Java cache with size and time limits. Used in projects 2, 3, 14. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))
- **Cardinality (metrics)** — Number of distinct tag-value combinations of a metric; high cardinality (user ids, raw URLs) explodes storage. Used in projects 26, 29. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **CDN** — Content Delivery Network: servers near users that cache and serve static files and media. Used in projects 14, 15, 21, 22, 40.
- **Chat memory** — Earlier messages of a conversation that Spring AI stores and re-sends so the model has context. Used in projects 30, 36. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **`ChatClient` and advisors** — Spring AI's fluent API for calling a chat model; advisors are interceptors that add memory, retrieved documents or logging. Used in projects 30, 36. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **Checkpoint / execution context** — Saved progress (the last line or event position processed) so a restarted job or projection continues where it stopped; Spring Batch keeps it in the step's execution context. Used in projects 18, 35.
- **Checksum** — A short value computed from content to detect changes or corruption; Flyway stores one per migration. Used in projects 4, 7, 8. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **Chunk-oriented step** — Spring Batch step that reads, processes and writes items in fixed-size chunks, each chunk in its own transaction. Used in project 18.
- **Chunking (RAG)** — Splitting documents into small passages before embedding them, so retrieval returns focused text. Used in projects 18, 30. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **CI (Continuous Integration)** — Building and testing every change automatically on a shared server. Used in projects 1, 4, 20, 26, 30, 31, 32, 36. ([Ch 12](../chapters/12-testing-spring-apps/README.md))
- **Circuit breaker** — Watches failures to a dependency and, past a threshold, stops calling it for a while (open), then lets a few trial calls through (half-open). Used in projects 27, 33, 39. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **CLI** — Command-line interface: a program you run from a terminal. Used in projects 24, 32.
- **Client credentials flow** — OAuth2 grant where a service gets a token for itself, with no user involved. Used in projects 12, 34. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Client-side load balancing** — The caller picks one instance from the registry for each request instead of going through a central load balancer. Used in project 27. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **`@ClientRegistrationId`** — Marks an HTTP interface (or method) so Spring Security attaches that OAuth2 client's access token to each call. Used in project 12. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **`Clock` (injected)** — Passing a `java.time.Clock` bean instead of calling `now()` directly, so tests can fix or move time. Used in projects 5, 6, 10, 16, 20, 24.
- **Clock skew** — Different machines' clocks disagree slightly; token and lease checks allow a small tolerance for it. Used in projects 6, 38. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Closed-world assumption** — GraalVM native images must know at build time every class, proxy and resource the program will ever use. Used in project 32. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Cold start** — The delay of starting a new instance from nothing, which hurts most in scale-to-zero setups. Used in project 32. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Collector (OpenTelemetry)** — A separate process that receives telemetry, samples, filters and routes it to one or more back ends. Used in project 29. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Compensating action** — A step that semantically undoes an earlier one (refund a payment, release stock) because a distributed transaction cannot roll back. Used in projects 28, 35. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **Competing consumers** — Several workers read from the same queue so each message is handled by exactly one of them. Used in project 17. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Concurrency** — Several things in progress at the same time; needs care when they touch shared data. Used in projects 1, 2, 3, 4, 10, 13, 14, 15, 16, 19, 22, 23, 24, 26, 28, 33, 35, 38, 39, 40. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **`@ConcurrencyLimit`** — Framework 7 annotation that caps how many calls of a method may run at the same time (a bulkhead); needs `@EnableResilientMethods`. Used in projects 10, 22, 24, 33. ([Ch 4](../chapters/04-aop-and-proxies/README.md))
- **Condition annotations (`@ConditionalOn...`)** — Annotations that register a bean only if a class, property or other bean is (or isn't) present. Used in projects 5, 6, 9, 25. ([Ch 3](../chapters/03-configuration-and-auto-configuration/README.md))
- **Conditional (atomic) update** — An `UPDATE ... WHERE` that changes a row only if it is still in the expected state, checked atomically by the database. Used in projects 13, 23, 38. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **Config Server** — Spring Cloud service that serves configuration from Git (or other stores) to many services. Used in project 27. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **`@ConfigurationProperties`** — Binds a group of properties with a common prefix into a typed object (ideally a record) that can be validated at startup. Used in projects 3, 5, 9, 25. ([Ch 3](../chapters/03-configuration-and-auto-configuration/README.md))
- **Connect vs read timeout** — How long to wait to establish a connection vs how long to wait for response data once connected. Used in projects 3, 10, 19. ([Ch 6](../chapters/06-rest-clients-and-http-interfaces/README.md))
- **Connection pool** — A set of open database or HTTP connections reused across requests, because opening one is slow. Used in projects 13, 14, 26, 27, 29, 40. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **Consistent lock ordering** — Always locking rows in the same order (e.g. by id) so two transactions cannot deadlock each other. Used in project 23. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **Consumer group** — Kafka consumers sharing a group id; each partition goes to one member, so they split the work. Used in projects 16, 19. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Consumer lag** — How far a consumer is behind the newest messages; the key health signal of a Kafka consumer. Used in projects 16, 29, 35. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Container (Docker)** — An isolated process packaged with everything it needs; Testcontainers starts real databases and brokers this way. Used in projects 4, 8, 12, 13, 14, 15, 16, 17, 20, 22, 23, 26, 27, 28, 29, 30, 31, 32, 35, 37, 38. ([Ch 12](../chapters/12-testing-spring-apps/README.md))
- **Content negotiation / `HttpMessageConverter`** — Picking the response format from the `Accept` header; converters turn Java objects into JSON, XML or CSV and back. Used in project 7. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Content sniffing / magic bytes** — Detecting a file's real type from its first bytes instead of trusting the file name or the client's `Content-Type`. Used in project 15.
- **Contention** — Many transactions or threads competing for the same lock or row, so they queue and slow down. Used in projects 13, 23. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **Context caching (tests)** — Spring's test framework reuses a started application context for test classes with the same configuration. Used in project 20. ([Ch 12](../chapters/12-testing-spring-apps/README.md))
- **Context propagation** — Copying per-thread values (tenant, user, trace id) onto the threads or messages that continue the work. Used in projects 26, 27, 29. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Contract-first** — Writing the API description (OpenAPI) before the code and treating it as the agreement with clients. Used in project 7. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Cookie flags (`HttpOnly`, `Secure`, `SameSite`)** — Cookie settings: not readable by JavaScript / sent only over HTTPS / not sent on cross-site requests. Used in projects 11, 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **CORS** — Browser rule that lets a page from one origin read responses from another origin only if the server allows it. Used in projects 11, 27, 40. ([Ch 10](../chapters/10-spring-security-1-fundamentals/README.md))
- **CQRS** — Command Query Responsibility Segregation: one model handles writes, separate read models serve queries. Used in project 35. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))
- **Cron expression** — Calendar schedule; Spring's form has six fields starting with seconds (`0 0 2 * * *` = 02:00 daily). Used in projects 6, 38. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **`CronExpression`** — Spring class that parses a cron string and computes the next fire time, the same way `@Scheduled` does. Used in projects 6, 38. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **CRUD** — Create, Read, Update, Delete: the four basic data operations. Used in projects 1, 4, 8, 10, 13, 19, 25, 27, 35. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **Crypto-shredding** — Deleting personal data in an immutable store by destroying the per-user encryption key, which makes the data unreadable. Used in project 35.
- **CSRF** — Cross-Site Request Forgery: another site makes the user's browser send a request with their cookies; blocked with a secret token. Used in project 40. ([Ch 10](../chapters/10-spring-security-1-fundamentals/README.md))
- **CSV** — Comma-separated values: a plain-text table format, one record per line. Used in projects 6, 7, 8, 18.
- **Custom constraint annotation** — Your own Bean Validation annotation (e.g. `@Isbn`) backed by a `ConstraintValidator` class. Used in project 7. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))

### D

- **Data model notation (PK, FK, UK)** — Primary key, foreign key and unique key markers in the entity diagrams. Used in projects 2, 4, 6, 7, 8, 10, 11, 12, 13, 15, 16, 17, 18, 19, 22, 23, 24, 25, 26, 28, 30, 31, 34, 35, 36, 38, 39. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **Data retention** — How long data is kept before a job deletes or archives it. Used in projects 10, 24, 29. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Database per service** — Each service (or module) owns its tables, and others reach that data only through its API or events. Used in projects 27, 28, 31. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **Dead-letter topic / queue / exchange (DLT, DLQ, DLX)** — Where messages that keep failing are parked for inspection instead of blocking or being lost. Used in projects 16, 17, 28, 38. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Deadline / timeout budget** — The total time a request may take; every downstream call must fit in what is left of it. Used in projects 10, 27, 28, 33. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **Deadlock** — Two transactions each wait for a lock the other holds; the database aborts one of them. Used in projects 13, 18, 23, 37. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **Debezium / CDC** — Change Data Capture reads the database's change log and publishes each change; Debezium is the common tool. Used in projects 9, 28. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Decompression bomb** — A tiny file that expands into gigabytes when unpacked, used to exhaust memory or disk. Used in project 15.
- **Decorator ordering** — The nesting order of retry, circuit breaker, bulkhead and timeout around a call, which changes how they interact. Used in project 33. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **`DefaultErrorHandler` / `DeadLetterPublishingRecoverer`** — spring-kafka's listener error handler (retries with back-off) and the recoverer that sends failures to the DLT. Used in project 16. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **`DelegatingPasswordEncoder`** — Spring Security encoder that stores `{algorithm}hash`, so stored passwords can move to a stronger algorithm over time. Used in project 11. ([Ch 10](../chapters/10-spring-security-1-fundamentals/README.md))
- **Dependency injection** — An object receives what it needs (usually through its constructor) instead of creating it itself. Used in projects 2, 5, 6, 9, 20, 29, 31, 32, 36, 40. ([Ch 2](../chapters/02-ioc-and-dependency-injection/README.md))
- **Deprecation / `Sunset` headers** — HTTP headers telling clients an API version is deprecated and the date it will be switched off. Used in project 21. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Deterministic bucketing** — Hashing a stable id (user id + flag key) into a number 0-99 so the same user always gets the same rollout decision. Used in project 25.
- **Dirty checking** — Hibernate compares managed entities with their loaded snapshot at flush and writes `UPDATE`s for changes. Used in projects 4, 8. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **`DispatcherServlet`** — Spring MVC's front controller that receives every request and routes it to a handler method. Used in project 1. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Distributed lock** — A lock shared by all instances (in a database or Redis) so only one runs a job at a time. Used in projects 6, 23, 38. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Distributed tracing** — Following one request across services by passing a trace id; each step is recorded as a span. Used in projects 27, 29, 32, 36. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **DNS rebinding** — Attack where a host name first resolves to a public IP and later to an internal one, to sneak past SSRF checks. Used in project 24.
- **Dockerfile (multi-stage)** — Recipe for building a container image; multi-stage builds compile in one image and copy only the result into a small one. Used in project 32. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Documenter (Spring Modulith)** — Generates module diagrams and documentation from the code structure. Used in project 31.
- **Double-entry bookkeeping / ledger** — Every money movement is written as equal debit and credit entries, so the books always balance. Used in projects 35, 39.
- **DSL** — Domain-specific language: an API that reads like configuration, such as Spring Security's lambda DSL. Used in project 11. ([Ch 10](../chapters/10-spring-security-1-fundamentals/README.md))
- **DTO / DTO projection** — Data Transfer Object: a simple record shaped for the API; a DTO projection loads only the columns it needs. Used in projects 3, 4, 14, 21, 31, 32. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **Dual-write problem** — Writing to the database and publishing to a broker cannot be made atomic; one may succeed and the other fail. Used in project 28. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Due-work query** — A query that selects only rows whose `next_run_at` is in the past, so a scheduler finds work cheaply. Used in project 10.
- **Durable** — Survives a crash or restart because it is written to disk (and often replicated). Used in projects 15, 16, 17, 24, 28, 31, 34, 38. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **`@DynamicPropertySource`** — Test method that registers properties known only at runtime, such as a container's random port. Used in project 20. ([Ch 12](../chapters/12-testing-spring-apps/README.md))

### E

- **`@EmbeddedKafka`** — Starts an in-memory Kafka broker inside the test JVM. Used in project 16. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Embedding** — A list of numbers representing the meaning of text, so similar texts have nearby vectors. Used in project 30. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **`@EnableJpaAuditing` / `@CreatedDate`** — Switches on Spring Data auditing, which fills created/modified timestamp (and user) fields automatically. Used in project 4. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **`@EnableResilientMethods`** — Switch that activates Framework 7's `@Retryable` and `@ConcurrencyLimit`; without it they silently do nothing. Used in projects 3, 10, 33. ([Ch 4](../chapters/04-aop-and-proxies/README.md))
- **Envelope encryption** — Data is encrypted with a data key, and that key is itself encrypted with a master key held in a vault or KMS. Used in projects 24, 26. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Environment (Spring)** — Spring's view of all property sources and active profiles. Used in project 5. ([Ch 3](../chapters/03-configuration-and-auto-configuration/README.md))
- **`ErrorResponseException`** — Spring exception that already carries a `ProblemDetail`, so it maps straight to an RFC 9457 response. Used in project 7. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **ETag / `If-None-Match` / 304** — A version fingerprint of a response; a client sends it back and gets `304 Not Modified` if nothing changed. Used in projects 1, 7, 14, 25. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Eureka** — Netflix service registry supported by Spring Cloud. Used in project 27. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **Evaluation set (AI)** — Fixed questions with known good answers used to measure whether changes improve or break an AI feature. Used in projects 30, 36. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **Event loop** — The few non-blocking threads of WebFlux/Netty; blocking one stalls every connection it serves. Used in project 19. ([Ch 13](../chapters/13-reactive-and-webflux/README.md))
- **Event publication registry (Spring Modulith)** — A table where Spring Modulith records each event until every listener has completed, so events survive crashes. Used in projects 28, 31.
- **Event sourcing** — Storing every change as an immutable event and rebuilding current state by replaying them. Used in projects 19, 35.
- **Eventual consistency / read-your-writes** — Copies agree after a short delay; read-your-writes means a user at least sees their own changes immediately. Used in projects 27, 28, 31, 35. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Exception translation** — Spring converts database-specific `SQLException`s into its own `DataAccessException` types such as `DuplicateKeyException`. Used in projects 2, 8. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **Exchange / queue / binding / routing key (RabbitMQ)** — Producers send to an exchange, which routes each message to queues whose binding matches its routing key. Used in project 17. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Exemplar** — A sample trace id attached to a metric data point, letting you jump from a latency spike to one example trace. Used in project 29. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Expand/contract migration** — Change a schema in safe steps: add the new form, migrate data and code, then remove the old form in a later release. Used in projects 8, 37. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **Exponential back-off with jitter** — Wait longer after each failure (1 s, 2 s, 4 s...) plus a random extra so clients don't retry all at once. Used in projects 3, 10, 14, 16, 24, 28, 33, 38, 39. ([Ch 6](../chapters/06-rest-clients-and-http-interfaces/README.md))
- **`@Externalized` (event externalization)** — Spring Modulith marker that also publishes an internal event to a broker such as Kafka. Used in project 31.

### F

- **Fail open vs fail closed** — When a safety component (rate limiter, auth check) breaks: let requests through anyway, or reject them. Used in project 22.
- **Fan-out** — Sending one request to many downstream services in parallel and combining the answers. Used in projects 3, 14, 24, 25, 29, 33. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **Feature flag** — A runtime switch that turns behaviour on or off (for some users) without redeploying. Used in projects 5, 25, 33.
- **Fencing token** — An increasing number handed out with each lock or lease; storage rejects writes carrying an older number, stopping zombie workers. Used in project 38.
- **FIFO** — First in, first out: items are processed in arrival order. Used in project 38.
- **Filter vs interceptor** — A servlet filter wraps every request before Spring MVC; an interceptor runs inside MVC and knows the chosen handler. Used in projects 7, 11, 12, 17, 22, 24, 30, 39. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Fingerprint (request / file)** — A hash of the content that identifies it, used to detect duplicates. Used in projects 8, 39.
- **Fixed vs sliding window** — Rate-limit counting per fixed clock interval (allows bursts at the edges) vs over a moving interval (smoother). Used in projects 22, 27. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **`fixedRate` vs `fixedDelay`** — Run every N ms from start to start, or wait N ms after the previous run finished. Used in projects 6, 10. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Flag debt** — Old feature flags left in the code after rollout; they add branches nobody tests and should be removed. Used in project 25.
- **Flaky test** — A test that sometimes passes and sometimes fails without code changes, usually because of timing or shared state. Used in projects 20, 33. ([Ch 12](../chapters/12-testing-spring-apps/README.md))
- **Flapping / hysteresis** — Flapping is a status switching back and forth rapidly; hysteresis requires several results in a row before changing state. Used in project 10.
- **`FlatFileItemReader` / `JdbcBatchItemWriter`** — Spring Batch reader that streams lines from a file and writer that inserts items with JDBC batches. Used in project 18.
- **Flyway** — Schema migration tool that applies numbered SQL scripts (`V1__init.sql`) in order and records them with checksums. Used in projects 4, 7, 8, 10, 11, 26, 31, 34, 37. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **Foreign key** — A column that must match the primary key of a row in another table. Used in project 8. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))

### G

- **GA (general availability)** — The first production-ready release of a version. Used in projects 3, 31. ([Ch 1](../chapters/01-spring-ecosystem-and-first-app/README.md))
- **Garbage collection (GC)** — The JVM freeing memory of unused objects automatically; pauses can add latency. Used in projects 5, 38. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **GraalVM native image** — An application compiled ahead of time into a standalone executable that starts in milliseconds and uses less memory. Used in projects 32, 37. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Graceful shutdown** — On a stop signal, stop taking new requests, finish in-flight ones, then exit. Used in project 37. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Grafana / Loki / Tempo / Jaeger** — Open-source observability back ends: dashboards / log storage / trace storage and viewing. Used in projects 29, 35, 38. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **GreenMail / SMTP** — SMTP is the protocol for sending e-mail; GreenMail is a fake mail server for tests. Used in project 6. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Guardrails (AI)** — Checks around an AI model's input and output (allowed tools, limits, content filters) that keep it within safe bounds. Used in project 36. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))

### H

- **H2** — Small in-memory Java SQL database, fine for quick tests but different from PostgreSQL in locking and SQL features. Used in projects 2, 4, 6, 8, 13, 20. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **Half-open range** — A time range that includes its start but excludes its end, so adjacent ranges never overlap or leave gaps. Used in project 6.
- **HATEOAS** — REST style where responses include links to the next possible actions. Used in project 7. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Head vs tail sampling** — Deciding whether to keep a trace at its start (cheap, random) or after it finishes (can keep all slow or failed traces). Used in project 29. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Heartbeat** — A periodic "I'm alive" signal; missing heartbeats mark a worker or lease as dead. Used in projects 19, 27, 38.
- **Hedged request** — Sending a second copy of a slow request to another replica and taking whichever answers first. Used in project 33. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **Hibernate** — The JPA implementation Boot uses: maps entities to tables and generates SQL. Used in projects 1, 4, 7, 8, 11, 13, 23, 26, 28, 37. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **Hit rate (cache)** — Share of lookups answered from the cache; the main measure of whether a cache helps. Used in projects 14, 30, 33. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))
- **HMAC signature** — A keyed hash of a message; only holders of the shared secret can produce or verify it, so it proves integrity and origin. Used in project 24. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Hold with expiry** — A temporary reservation (e.g. of a seat) that is released automatically if not confirmed in time. Used in project 23.
- **`__Host-` cookie prefix** — A cookie name prefix that browsers accept only for a `Secure`, host-only cookie with `Path=/`, so a subdomain cannot overwrite it. Used in project 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Hot vs cold publisher** — A cold publisher starts again for each subscriber; a hot one emits regardless, and late subscribers miss earlier items. Used in project 19. ([Ch 13](../chapters/13-reactive-and-webflux/README.md))
- **HTTP** — The web's request/response protocol: a method and path go in, a status code, headers and body come back. Used in projects 1, 2, 3, 7, 10, 12, 15, 19, 20, 21, 24, 25, 27, 29, 32, 33, 34, 36, 39, 40. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **HTTP 401 vs 403** — 401 = not authenticated (missing or invalid credentials); 403 = authenticated but not allowed. Used in projects 11, 12, 26, 30, 40. ([Ch 10](../chapters/10-spring-security-1-fundamentals/README.md))
- **HTTP methods (GET, POST, PUT, PATCH, DELETE)** — Read, create, replace, partly update and delete a resource. Used in projects 1, 7, 15. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **HTTP service client group (`@ImportHttpServices`)** — Framework 7 / Boot 4 way to register many HTTP interfaces at once and configure each group (base URL, timeouts) by properties. Used in projects 3, 21. ([Ch 6](../chapters/06-rest-clients-and-http-interfaces/README.md))
- **HTTP service interface (`@HttpExchange`)** — A Java interface whose annotated methods Spring implements as HTTP calls. Used in projects 3, 10, 12, 15, 21, 24, 25, 27, 33, 40. ([Ch 6](../chapters/06-rest-clients-and-http-interfaces/README.md))
- **HTTP status codes** — Three-digit result of an HTTP call: 2xx success, 3xx redirect, 4xx client error, 5xx server error. Used in projects 1, 2, 3, 4, 11, 15, 22, 23, 26, 39. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Human-in-the-loop approval** — A person must confirm a risky action (refund, delete) before an AI agent's tool call is executed. Used in project 36. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))

### I

- **ID token vs access token** — The ID token tells the client who logged in (OIDC); the access token is what APIs accept. Used in projects 12, 34. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **IDE** — Integrated Development Environment such as IntelliJ IDEA or VS Code. Used in projects 5, 9, 36. ([Ch 1](../chapters/01-spring-ecosystem-and-first-app/README.md))
- **Idempotency** — Doing an operation twice has the same effect as doing it once; makes retries safe. Used in projects 1, 3, 6, 8, 13, 15, 16, 17, 18, 24, 27, 28, 31, 35, 36, 38, 39. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Idempotency key** — A unique id the client sends with a request; the server stores it and returns the original result for repeats. Used in projects 13, 16, 27, 39. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Idempotent consumer / inbox** — A consumer that records processed message ids so a redelivered message has no second effect. Used in projects 16, 28, 35, 39. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **IETF / RFC** — The Internet Engineering Task Force publishes internet standards as numbered RFC documents. Used in projects 1, 7, 11, 12, 21, 22, 34, 39, 40. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Index (database)** — A sorted lookup structure that lets the database find rows without scanning the whole table. Used in projects 1, 2, 4, 6, 7, 8, 11, 18, 23, 24, 26, 30, 32, 34, 38. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **`IN_PROGRESS` record** — Marker row stored before work starts, so a concurrent duplicate request sees the work is already under way. Used in project 39.
- **Integration test** — A test of several real parts together, often the whole app plus a real database. Used in projects 5, 20, 25, 28, 31. ([Ch 12](../chapters/12-testing-spring-apps/README.md))
- **Invalidation after commit** — Removing cached entries only after the database transaction commits, so no reader caches the old value again. Used in projects 14, 25. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))
- **IoC container** — The Spring object that creates beans and injects their dependencies (Inversion of Control). Used in projects 5, 6, 9, 20, 22, 31, 32. ([Ch 2](../chapters/02-ioc-and-dependency-injection/README.md))
- **ISBN** — International Standard Book Number; its last digit is a checksum, which makes it a good custom-validation example. Used in project 7.
- **Isolation level** — How much concurrent transactions see of each other's changes (READ COMMITTED, REPEATABLE READ, SERIALIZABLE). Used in project 13. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **Issuer discovery and JWKS** — A resource server reads the issuer's metadata to find its public-key set (JWKS) for verifying token signatures. Used in projects 11, 12, 34. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))

### J

- **Jackson 3** — The JSON library in Boot 4 (packages `tools.jackson.*`). Used in projects 1, 3, 35. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Jakarta EE** — The enterprise Java standards (Servlet, JPA, Bean Validation) Spring builds on; formerly Java EE. Used in project 4. ([Ch 1](../chapters/01-spring-ecosystem-and-first-app/README.md))
- **JDBC / JDBC batching** — Java's low-level SQL API; batching sends many inserts or updates in one round trip. Used in projects 2, 4, 6, 8, 9, 13, 14, 18, 19, 20, 23, 26, 28, 31, 34, 35, 38, 39. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **`JdbcClient`** — Spring's fluent API for plain SQL with named parameters and automatic mapping to records. Used in projects 2, 4, 6, 8, 10, 13, 16, 18, 35, 38, 39. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **JDK AOT cache / CDS** — JDK files of pre-loaded and linked classes from a training run that make a normal JVM start much faster. Used in projects 32, 37. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **JDK `HttpClient`** — The HTTP client built into Java 11+, usable underneath `RestClient`. Used in projects 10, 24. ([Ch 6](../chapters/06-rest-clients-and-http-interfaces/README.md))
- **JFR (Java Flight Recorder)** — Low-overhead profiler built into the JVM. Used in projects 18, 29, 32. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **JIT vs AOT compilation** — JIT compiles hot code to machine code while the app runs; AOT does the work at build time. Used in projects 32, 37. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Job / step / job instance / job execution (Spring Batch)** — A job is made of steps; a job instance is one logical run (e.g. one file), and each attempt to run it is a job execution stored in the `JobRepository`. Used in project 18.
- **`JobRepository` / `JobOperator`** — Spring Batch's store of job and step executions, and the API to start, stop and restart jobs. Used in project 18.
- **JPA / Spring Data JPA** — JPA is the Java standard for mapping objects to tables; Spring Data JPA generates repositories on top of it. Used in projects 1, 2, 3, 4, 7, 8, 11, 13, 14, 16, 20, 22, 23, 25, 26, 28, 31, 35, 37, 39. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **JPQL** — JPA's query language over entities and fields rather than tables and columns. Used in projects 4, 7. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **JSON** — JavaScript Object Notation: the text format most REST APIs exchange. Used in projects 1, 2, 3, 4, 7, 8, 11, 14, 16, 20, 21, 24, 26, 29, 32, 34, 36, 40. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **JWK set and `kid`** — The published list of an issuer's public keys; the `kid` header in a token says which key signed it. Used in projects 11, 12, 34. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **JWT (claims, signature, `exp`)** — Signed JSON token of `header.payload.signature`; the claims (user, scopes, expiry `exp`) are readable by anyone but cannot be changed. Used in projects 11, 12, 26, 27, 30, 34, 36, 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **JWT vs opaque token** — A JWT is checked locally from its signature; an opaque token is a random reference the API must look up (introspect). Used in projects 11, 34, 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))

### K

- **Kafka** — Distributed, replicated log for event streaming; records are kept and can be re-read. Used in projects 2, 16, 17, 19, 20, 21, 24, 26, 28, 29, 31, 35, 37, 39. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **`@KafkaListener` / `KafkaTemplate`** — Spring for Apache Kafka's consumer annotation and producer API. Used in project 16. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Key / token rotation** — Replacing signing or encryption keys regularly while still accepting the previous one for a while. Used in projects 11, 12, 22, 24, 34, 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Keycloak** — Open-source identity provider and OAuth2/OIDC authorization server. Used in projects 12, 34, 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **kind / minikube** — Tools that run a small Kubernetes cluster on a laptop. Used in project 37.
- **Kubernetes** — Platform that runs, restarts, scales and connects containers across machines. Used in projects 6, 10, 17, 27, 37. ([Ch 15](../chapters/15-observability-and-production/README.md))

### L

- **L1 / L2 cache** — Two cache levels: a fast local in-process cache (L1, e.g. Caffeine) in front of a shared cache (L2, e.g. Redis). Used in project 14. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))
- **Latency / p95 / p99** — How long a request takes; p99 is the time 99% of requests beat, which exposes the slow tail. Used in projects 1, 2, 3, 7, 9, 10, 13, 14, 15, 16, 20, 22, 23, 24, 25, 26, 27, 28, 29, 30, 32, 33, 34, 35, 36, 38, 39, 40. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Lazy vs eager loading** — Load an association only when first accessed, or always together with its owner. Used in projects 4, 14. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **Leader election** — Instances agree that exactly one of them (the leader) does a given job. Used in project 38. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Lease** — A lock with an expiry time that its holder must renew; if the holder dies the lease simply runs out. Used in projects 14, 38.
- **Liberica NIK** — A GraalVM-based native-image kit distribution. Used in project 32. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Liveness / readiness / startup probe** — Kubernetes checks: should I restart it? / may it take traffic? / is it still starting? Used in projects 10, 37. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **LLM** — Large language model: the text-generating AI behind chat APIs. Used in projects 30, 36. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **Load testing (k6, Gatling)** — Firing many concurrent requests to measure throughput, latency and errors under load. Used in projects 14, 22, 24, 26, 27, 29, 33, 37. ([Ch 12](../chapters/12-testing-spring-apps/README.md))
- **Local evaluation (feature flags)** — The client library keeps a copy of the rules and decides flags in-process, with no network call per check. Used in project 25.
- **`lockAtMostFor` / `lockAtLeastFor`** — ShedLock settings: release the lock after this long even if the holder died / keep it at least this long to stop a second run. Used in project 6. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Lost update** — Two read-modify-write cycles run concurrently and one silently overwrites the other. Used in projects 4, 13. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))

### M

- **Manual acknowledgement** — The consumer tells the broker a message is done only after processing succeeds, so a crash leads to redelivery. Used in project 17. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Many-to-many with join table** — Two entities linked by a third table holding pairs of ids (book_author). Used in project 7. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **MCP (Model Context Protocol) client / server** — Standard protocol for exposing tools and data to AI applications across processes; a server offers tools, a client uses them. Used in projects 30, 36. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **Metadata filter expression** — A condition on stored document metadata (department, date) applied during vector search. Used in project 30. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **Method security (`@PreAuthorize`)** — Authorization rules on service methods, checked by a proxy before the method runs. Used in project 11. ([Ch 10](../chapters/10-spring-security-1-fundamentals/README.md))
- **Metrics / traces / logs** — The three kinds of observability data: numbers over time / the path of one request / individual event records. Used in projects 3, 6, 10, 14, 16, 19, 21, 22, 25, 29, 30, 33, 36. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Micrometer** — Spring's vendor-neutral metrics (and observation) library; exports to Prometheus, OTLP and others. Used in projects 10, 14, 16, 18, 29, 36. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Micrometer Observation API** — Instrument an operation once and get a timer metric, a trace span and log correlation from it. Used in project 29. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Microservices** — An architecture of small, independently deployed services, each owning one business capability and its data. Used in projects 27, 28, 29, 31, 33, 39, 40. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **MinIO / object storage** — Storage for whole files addressed by key over HTTP (S3 API); MinIO is an S3-compatible server you can run locally. Used in projects 15, 17, 18.
- **`MockitoBean`** — Replaces a bean in the test context with a Mockito mock (successor of `@MockBean`). Used in projects 1, 20. ([Ch 12](../chapters/12-testing-spring-apps/README.md))
- **`MockMvcTester`** — Framework 6.2 test API that calls Spring MVC in memory and checks results with AssertJ. Used in projects 1, 20, 21. ([Ch 12](../chapters/12-testing-spring-apps/README.md))
- **`MockRestServiceServer`** — Fake server for `RestClient` tests that checks the requests sent and returns canned responses. Used in projects 3, 12, 21, 27, 40. ([Ch 6](../chapters/06-rest-clients-and-http-interfaces/README.md))
- **Modular monolith** — One deployable application split into modules with strict boundaries; Spring Modulith verifies them. Used in projects 27, 28, 31.
- **Mono / Flux** — Reactor's publishers of 0..1 and 0..N items; nothing happens until something subscribes. Used in projects 19, 30. ([Ch 13](../chapters/13-reactive-and-webflux/README.md))
- **Multi-factor authentication (MFA)** — Login that needs two proofs, e.g. a password plus a one-time code or a passkey. Used in projects 11, 34, 40. ([Ch 10](../chapters/10-spring-security-1-fundamentals/README.md))
- **Multi-issuer resource server** — An API that accepts tokens from several identity providers, choosing the right keys per issuer. Used in projects 12, 26. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Multi-tenancy / tenant** — One system serving many customer organisations (tenants) whose data must stay isolated. Used in projects 12, 21, 24, 26, 29, 33, 34, 37. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Multipart upload** — HTTP request format (`multipart/form-data`) that carries files and form fields together. Used in project 15. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))

### N

- **N+1 problem** — One query loads N rows, then each row triggers another query for its association: N+1 round trips. Used in projects 4, 8, 37. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **`@NamedInterface`** — Spring Modulith annotation that exposes a sub-package of a module as part of its public API. Used in project 31.
- **`NimbusJwtEncoder`** — Spring Security's JWT signer, built on the Nimbus JOSE library. Used in project 11. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Noisy neighbour** — One tenant using so many resources that others sharing the system slow down. Used in project 26.
- **Non-functional requirements** — How well the system must work (latency, throughput, availability, security) rather than what it does. Used in all 40 projects.

### O

- **OAuth 2.0 / OAuth 2.1** — Standard for delegated authorization with access tokens; 2.1 folds in best practices such as mandatory PKCE. Used in projects 11, 12, 22, 24, 25, 26, 27, 30, 34, 36, 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **`OAuth2AuthorizationService`** — Spring Authorization Server store of issued authorizations and tokens. Used in project 34. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **`OAuth2AuthorizedClient`** — Spring Security object holding the access and refresh tokens a client obtained for a user. Used in projects 12, 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **`OAuth2ClientHttpRequestInterceptor`** — `RestClient` interceptor that obtains (and refreshes) an OAuth2 access token and adds it to each request. Used in project 12. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Observability** — Being able to understand a running system from its outputs: metrics, traces and logs. Used in projects 6, 10, 14, 16, 20, 21, 26, 27, 29, 31, 32, 33, 36, 37. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **OIDC (OpenID Connect)** — Identity layer on top of OAuth2 that adds the ID token and login, answering "who is the user?". Used in projects 12, 26, 34. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Open redirect** — A link on your site that forwards to any URL given as input, which attackers abuse for phishing. Used in project 2.
- **Open Session in View (OSIV)** — Keeps the persistence context open for the whole web request; hides N+1 queries, so turn it off for APIs. Used in project 4. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **OpenAPI** — Standard, machine-readable description of a REST API; springdoc generates it from the code. Used in projects 1, 7, 21. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **OpenTelemetry / OTLP** — Vendor-neutral standard for traces, metrics and logs; OTLP is its export protocol. Used in projects 27, 29. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Optimistic concurrency (expected version)** — An append succeeds only if the stream is still at the version the writer read; otherwise it retries. Used in project 35. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))
- **Optimistic locking (`@Version`)** — Each update checks a version column; if someone changed the row since it was read, the update fails instead of overwriting. Used in projects 1, 4, 13, 23, 28, 35. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **Orchestration vs choreography** — A saga coordinated by one orchestrator telling services what to do, vs services reacting to each other's events. Used in projects 3, 13, 26, 28. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **ORM** — Object-Relational Mapping: working with objects that a library (Hibernate) maps to tables. Used in projects 2, 4, 26. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **Outbox (transactional)** — Write the event into an outbox table in the same transaction as the data; a relay publishes it afterwards. Used in projects 16, 17, 24, 28, 31, 35, 39. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **OWASP** — Open Worldwide Application Security Project, known for its Top 10 list of web risks. Used in projects 15, 24. ([Ch 10](../chapters/10-spring-security-1-fundamentals/README.md))

### P

- **Pagination** — Returning a list in pages (`page`, `size`) or after a cursor, so no request loads everything. Used in projects 1, 2, 4, 7, 10, 11, 12, 14, 19, 25, 34, 35, 40. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **Partial result / stale fallback** — When some sources fail, return what you have, or a recently cached answer, and say so. Used in projects 14, 33. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **Partial unique index** — A unique index that applies only to rows matching a condition (e.g. only active holds). Used in project 23. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **Path traversal** — Attack using `../` in a file name to read or write files outside the intended directory. Used in project 15.
- **Percentage rollout** — Turning a feature on for a growing share of users, chosen deterministically. Used in project 25.
- **Persistence context** — Hibernate's first-level cache and unit of work: one managed object per row, changes tracked until flush. Used in project 4. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **Pessimistic locking (`SELECT ... FOR UPDATE`)** — Locking rows when reading them so others must wait; `@Lock(PESSIMISTIC_WRITE)` in Spring Data JPA. Used in projects 4, 10, 13, 23, 24, 28, 38. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **pgvector** — PostgreSQL extension that stores vectors and runs similarity search. Used in project 30. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **Playwright** — Browser-automation tool for end-to-end tests that drive a real browser. Used in project 40.
- **PodDisruptionBudget** — Kubernetes rule that keeps a minimum number of pods running during voluntary disruptions such as node upgrades. Used in project 37. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Poison message** — A message that fails every time (bad format, bug); it must be moved aside so it does not block the queue. Used in project 16. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **PostgreSQL** — Open-source relational database used by most projects here. Used in projects 2, 4, 6, 7, 8, 10, 11, 13, 14, 15, 16, 17, 18, 19, 20, 22, 23, 24, 25, 26, 30, 31, 34, 35, 36, 37, 38, 39. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **Prefetch** — How many unacknowledged messages a RabbitMQ consumer may hold at once; limits per-worker load. Used in project 17. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Presigned URL** — A time-limited URL signed by the server that lets a client upload or download one object directly from storage. Used in project 15.
- **preStop hook** — Kubernetes command run before a container is stopped; a short sleep lets traffic drain first. Used in project 37. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Price snapshot** — Copying the price into the order line at checkout so later catalogue changes do not alter past orders. Used in projects 13, 27.
- **Problem Details (RFC 9457) / `ProblemDetail`** — Standard JSON error body with `type`, `title`, `status` and `detail`; Spring's `ProblemDetail` produces it. Used in projects 1, 3, 7, 15, 20, 21, 22, 30, 39. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Profile (Spring)** — A named set of beans and property files activated per environment (`dev`, `prod`). Used in projects 3, 5, 11, 17, 20, 27, 32, 34, 40. ([Ch 3](../chapters/03-configuration-and-auto-configuration/README.md))
- **Profile-guided optimisation (PGO)** — Compiling a native image with data from a training run so hot paths are optimised. Used in project 32. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Projection / read model** — A query-optimised view of data, built from events or a subset of columns. Used in projects 4, 31, 35. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))
- **Prometheus** — Monitoring system that scrapes metrics from `/actuator/prometheus` and stores them as time series. Used in projects 10, 29. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Prompt / system prompt** — The text sent to an AI model; the system prompt holds fixed instructions that come before the user's message. Used in projects 30, 36. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **Prompt injection** — Instructions hidden in user input or retrieved documents that try to make the model ignore its rules. Used in projects 30, 36. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **Propagation (transactions)** — What a transactional method does when a transaction already exists: join it (`REQUIRED`) or start its own (`REQUIRES_NEW`). Used in projects 13, 26. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **Property source and precedence** — Each place a setting can come from (file, env var, argument); precedence decides which one wins. Used in projects 5, 30. ([Ch 3](../chapters/03-configuration-and-auto-configuration/README.md))
- **Proxy (AOP, JDK vs CGLIB)** — Stand-in object Spring puts in front of a bean to add transactions, caching or security; JDK proxies implement interfaces, CGLIB proxies subclass. Used in projects 3, 8, 9, 10, 12, 13, 14, 19, 22, 24, 25, 32, 33, 38. ([Ch 4](../chapters/04-aop-and-proxies/README.md))
- **PSP (payment service provider)** — The external company (Stripe, Adyen...) that actually moves card money and calls you back with webhooks. Used in project 39.
- **Public vs confidential client** — A public client (SPA, mobile app) cannot keep a secret; a confidential client (server) can. Used in projects 12, 34. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Publisher confirms** — RabbitMQ acknowledgements to the producer that a message was safely stored. Used in project 17. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))

### Q

- **Quorum queue** — Replicated RabbitMQ queue type that survives the loss of a node. Used in project 17. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Quotas / fair scheduling** — Limits per team or tenant so one heavy user cannot take all workers. Used in projects 15, 17, 22, 24, 38.

### R

- **R2DBC** — Reactive, non-blocking API for SQL databases. Used in project 19. ([Ch 13](../chapters/13-reactive-and-webflux/README.md))
- **`@RabbitListener` / `RabbitTemplate`** — Spring AMQP's consumer annotation and producer API for RabbitMQ. Used in project 17. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **RabbitMQ** — Message broker with exchanges, queues and per-message acknowledgements. Used in projects 15, 16, 17, 20. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **RAG (retrieval-augmented generation)** — Find relevant documents first and put them in the prompt so the model answers from your data. Used in project 30. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **Rate limit / rate-limit key** — A cap on requests per client per time window; the key says who is counted (API key, user, IP). Used in projects 3, 11, 16, 22, 26, 27, 30, 33, 39. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Reachability metadata / runtime hints** — Build-time declarations of reflection, proxies and resources a native image must include. Used in project 32. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **React SPA** — A single-page application built with React that runs in the browser and calls APIs. Used in projects 11, 12, 19, 25, 29, 30, 34, 40.
- **Reactive / non-blocking** — Code that never holds a thread while waiting; results are pushed through a pipeline when ready. Used in projects 3, 16, 19, 25, 29, 30. ([Ch 13](../chapters/13-reactive-and-webflux/README.md))
- **Reactor Netty** — Non-blocking network server and client under WebFlux. Used in project 19. ([Ch 13](../chapters/13-reactive-and-webflux/README.md))
- **Reaper** — Background job that finds work whose lease expired (a crashed worker) and makes it available again. Used in project 38.
- **Rebalance (Kafka)** — Reassigning partitions among the consumers of a group when one joins or leaves. Used in project 16. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Reconciliation** — Comparing your records with another system's (stock, payments) and fixing differences. Used in projects 13, 35, 39.
- **Record key and ordering (Kafka)** — Records with the same key go to the same partition, which is the only place Kafka keeps order. Used in projects 16, 28, 29, 39. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Redirect (301/302/307/308)** — HTTP responses telling the client to go to another URL; 301/308 permanent, 302/307 temporary. Used in projects 2, 12, 24, 34, 39. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Redis** — In-memory key/value store used for shared caches, sessions, rate limits and locks. Used in projects 2, 3, 12, 13, 14, 19, 20, 22, 40. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))
- **`RedisCacheManager` / Lettuce** — Spring's Redis-backed `CacheManager`; Lettuce is the Redis client library Boot uses. Used in projects 14, 22. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))
- **Refresh token / rotation / reuse detection** — Long-lived token used only to get new access tokens; rotation issues a new one each time and a reused old one signals theft. Used in projects 11, 12, 34, 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **`RegisteredClientRepository`** — Spring Authorization Server store of the client applications allowed to request tokens. Used in project 34. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Relaxed binding** — Boot accepts several spellings of a property (`shop.page-size`, `SHOP_PAGESIZE`). Used in project 5. ([Ch 3](../chapters/03-configuration-and-auto-configuration/README.md))
- **Release train** — A set of project versions released and tested together, such as Spring Cloud 2025.1. Used in project 27. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **Replay protection** — Rejecting a captured message sent again, using a timestamp window and remembered ids. Used in project 24. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Resilience4j** — Library for circuit breakers, retries, rate limiters, bulkheads and time limiters. Used in projects 27, 33, 39. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **Resource server** — An API that accepts access tokens and checks them on every request. Used in projects 11, 12, 26, 30, 34, 36, 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **`ResponseEntity`** — Controller return type that sets status code, headers and body together. Used in projects 1, 7. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **`ResponseEntityExceptionHandler`** — Base class for a `@ControllerAdvice` that turns Spring MVC's own exceptions into Problem Details. Used in project 7. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **REST / REST resource** — API style built on resources named by URLs and acted on with standard HTTP methods and status codes. Used in projects 1, 2, 3, 4, 7, 10, 12, 15, 18, 21, 22, 24, 25, 27, 33, 35, 37, 39, 40. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **`RestClient`** — Spring's fluent, blocking HTTP client (Framework 6.1+). Used in projects 3, 10, 12, 13, 16, 21, 24, 25, 27, 33, 39. ([Ch 6](../chapters/06-rest-clients-and-http-interfaces/README.md))
- **`@RestControllerAdvice`** — A bean with `@ExceptionHandler` methods that turns exceptions from all controllers into HTTP responses. Used in project 1. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **`RestTestClient`** — Framework 7 fluent test client that calls MockMvc or a running server. Used in projects 1, 2, 20, 21. ([Ch 12](../chapters/12-testing-spring-apps/README.md))
- **Retry amplification** — Retries at several layers multiply: 3 retries × 3 layers can turn one request into 27 calls on a struggling service. Used in projects 27, 33. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **`Retry-After`** — Response header telling the client how many seconds to wait before trying again (with 429 or 503). Used in projects 3, 22, 39. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **`@Retryable` (Framework 7)** — Framework 7 annotation that calls a method again after an exception, with a delay, multiplier and jitter between attempts. Used in projects 3, 16, 24, 33, 39. ([Ch 4](../chapters/04-aop-and-proxies/README.md))
- **`@RetryableTopic`** — spring-kafka annotation for non-blocking retries through delayed retry topics, ending in a dead-letter topic. Used in project 16. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Rollback rules** — Spring rolls back on unchecked exceptions and commits on checked ones unless told otherwise. Used in projects 4, 8, 13, 14, 18, 20, 23, 31, 35. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **Rolling update** — Replacing instances a few at a time so the service stays up during a deployment. Used in project 37. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Row-level security (RLS)** — PostgreSQL feature that filters rows per session (e.g. by tenant id) inside the database itself. Used in project 26.
- **RP-initiated logout** — The client app (relying party) asks the identity provider to end the user's login session too. Used in projects 12, 34. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **RS256 / JWKS** — RS256 signs tokens with an RSA private key; anyone can verify with the public key published in the JWKS. Used in project 11. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))

### S

- **Safe default** — The value used when configuration or a remote service is missing; it must be the harmless choice. Used in project 25.
- **Saga** — A business process spread over services as a series of local transactions, with compensations when a step fails. Used in projects 13, 27, 28, 29, 35. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **Saga timeout** — A deadline after which a waiting saga step is treated as failed and compensated. Used in project 28. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **Scale-to-zero** — Running no instances when there is no traffic and starting one on the first request. Used in project 32. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **`@Scheduled` / `@EnableScheduling`** — Runs a bean method on a timer (`fixedRate`, `fixedDelay` or a cron expression) once scheduling is switched on. Used in projects 2, 6, 10, 13, 23, 26, 28, 38. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **`ScopedValue` vs `ThreadLocal`** — Ways to make a value available to code on the current thread; a `ScopedValue` (Java 25) is immutable and ends with its scope. Used in project 26. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Scopes and `SCOPE_` authorities** — OAuth2 permissions in a token (`notes.read`); Spring maps them to authorities named `SCOPE_notes.read`. Used in projects 12, 34, 36. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Security filter chain** — The ordered servlet filters through which Spring Security authenticates and authorizes every request. Used in projects 11, 12, 22, 26, 34, 40. ([Ch 10](../chapters/10-spring-security-1-fundamentals/README.md))
- **Self-invocation** — A bean calling its own method through `this`, which skips the proxy and its transaction, cache or retry. Used in projects 3, 9, 13, 14, 33. ([Ch 4](../chapters/04-aop-and-proxies/README.md))
- **Semantic lock** — A status flag (e.g. `PENDING`) marking data as in use by an unfinished saga. Used in project 28. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **Semantic versioning / version parser** — Versions written MAJOR.MINOR.PATCH, where a major bump signals breaking changes. Used in project 21. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Semaphore** — A counter of permits limiting how many threads may do something at once. Used in projects 10, 24, 38. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Server-Sent Events (SSE)** — One-way stream of events from server to browser over a long-lived HTTP response. Used in projects 16, 17, 19, 23, 25, 30, 36, 38. ([Ch 13](../chapters/13-reactive-and-webflux/README.md))
- **Service discovery** — How services find the current addresses of each other (Eureka, Consul, Kubernetes DNS). Used in projects 12, 27, 34. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **`@ServiceConnection`** — Boot reads connection details straight from a Testcontainers container, no properties needed. Used in projects 13, 17, 20. ([Ch 12](../chapters/12-testing-spring-apps/README.md))
- **Session (HTTP) / Spring Session** — Server-side state for one browser, found by a cookie; Spring Session stores it in Redis so all instances share it. Used in projects 4, 11, 12, 14, 26, 34, 40. ([Ch 10](../chapters/10-spring-security-1-fundamentals/README.md))
- **`SET LOCAL`** — PostgreSQL command that sets a session variable (e.g. the tenant id) only until the current transaction ends. Used in project 26.
- **SHA-256** — Cryptographic hash function; fine for API keys and fingerprints, too fast on its own for passwords. Used in projects 12, 15, 22, 24. ([Ch 10](../chapters/10-spring-security-1-fundamentals/README.md))
- **Silo / bridge / pool models** — Multi-tenant isolation levels: separate database per tenant / separate schema / shared tables with a tenant column. Used in project 26.
- **Sinks / `.share()`** — Reactor tools to push values into a stream from ordinary code and to share one stream among many subscribers. Used in project 19. ([Ch 13](../chapters/13-reactive-and-webflux/README.md))
- **Skip / retry policy (Spring Batch)** — Rules for which item errors are retried and which bad items are skipped and logged. Used in project 18.
- **`SKIP LOCKED`** — SQL option that skips rows locked by others, so many workers can claim different rows without waiting. Used in projects 10, 23, 24, 28, 38. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **SLO** — Service Level Objective: a target such as "99.9% of requests under 300 ms over 30 days". Used in projects 29, 33, 37. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Snapshot** — A saved copy of state at one moment (a price at checkout, an aggregate after N events, an API contract) used as a reference or to avoid recomputing. Used in projects 4, 7, 13, 19, 20, 21, 25, 27, 32, 35. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))
- **Span and trace context (`traceparent`)** — A span is one timed step of a request; the trace context (W3C `traceparent` header) carries the trace id between services. Used in projects 13, 24, 27, 29. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **`Specification` (Spring Data JPA)** — Composable query criteria objects for building dynamic `WHERE` clauses. Used in project 7. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **SpEL** — Spring Expression Language: small expressions such as `#id` or `#result == null` inside annotations. Used in project 9. ([Ch 3](../chapters/03-configuration-and-auto-configuration/README.md))
- **Spring AI** — Spring project for calling AI models, embeddings, vector stores, tools and MCP. Used in projects 18, 21, 30, 34, 36. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **Spring AOT processing** — Build-time step that turns configuration into generated code and hints, needed for native images. Used in project 32. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Spring Authorization Server** — Spring's own OAuth2/OIDC authorization server, now part of Spring Security. Used in projects 12, 34. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Spring Batch** — Framework for large, restartable batch jobs that read, process and write data in chunks. Used in projects 6, 18.
- **Spring Boot** — Spring with starters, managed versions, auto-configuration, an embedded server and Actuator. Used in projects 1, 5, 9, 10, 12, 14, 17, 20, 25, 29, 31, 32, 37. ([Ch 1](../chapters/01-spring-ecosystem-and-first-app/README.md))
- **Spring Cloud Gateway** — Spring's API gateway: routes with predicates and filters, in a WebMVC or WebFlux flavour. Used in projects 21, 22, 27, 40. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **Spring Modulith** — Spring project that verifies module boundaries in a monolith and offers durable application events. Used in projects 28, 31.
- **SQL** — Structured Query Language for relational databases. Used in projects 2, 4, 6, 8, 9, 10, 13, 14, 19, 20, 23, 26, 30, 31. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **SQLSTATE** — Standard five-character database error code (e.g. `23505` = unique violation). Used in projects 13, 23. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **`SseEmitter`** — Spring MVC's way to send Server-Sent Events without WebFlux. Used in projects 19, 25. ([Ch 13](../chapters/13-reactive-and-webflux/README.md))
- **SSRF** — Server-Side Request Forgery: tricking your server into calling an internal address chosen by the attacker. Used in project 24.
- **Starter (Spring Boot)** — A code-free dependency that brings everything for one capability plus its auto-configuration. Used in projects 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 16, 17, 18, 19, 20, 25, 27, 28, 29, 31, 32, 34, 36, 37, 40. ([Ch 1](../chapters/01-spring-ecosystem-and-first-app/README.md))
- **Starter vs auto-configure module** — A reusable library is split into a module with the auto-configuration code and a starter that just pulls in dependencies. Used in project 9. ([Ch 3](../chapters/03-configuration-and-auto-configuration/README.md))
- **State machine (status, payment)** — The allowed statuses of a record and the transitions between them (e.g. PENDING → AUTHORIZED → CAPTURED), enforced in code and the database. Used in projects 10, 15, 28, 39.
- **Stateless** — Keeps no per-client data between requests, so any instance can serve any request. Used in projects 1, 2, 11, 12, 34, 40. ([Ch 10](../chapters/10-spring-security-1-fundamentals/README.md))
- **`StepVerifier` / virtual time** — Reactor test tool that subscribes and checks emitted items; virtual time fast-forwards delays. Used in project 19. ([Ch 13](../chapters/13-reactive-and-webflux/README.md))
- **Streamable HTTP / STDIO (MCP transports)** — Ways an MCP client talks to a server: over HTTP, or through a child process's standard input/output. Used in project 36. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **Streaming vs buffering** — Processing data piece by piece as it arrives, vs loading all of it into memory first. Used in projects 15, 29. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Structured logging** — Writing each log event as JSON with fields, so tools can search and correlate it. Used in project 29. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Supported versions** — The list of API versions the server accepts; others are rejected with 400. Used in project 21. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **`sync = true` (`@Cacheable`)** — Lets only one thread per key and instance compute a missing cache value while others wait. Used in project 14. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))

### T

- **Targeting rule** — Feature-flag condition choosing who gets a variant (country, plan, user list). Used in project 25.
- **Tasklet step** — Spring Batch step that runs one piece of code once (e.g. delete a temp file), not item by item. Used in project 18.
- **`@TenantId` (Hibernate)** — Hibernate annotation for a tenant column that it fills and filters automatically on every query. Used in project 26.
- **`terminationGracePeriodSeconds`** — How long Kubernetes waits after SIGTERM before killing a container; must exceed preStop plus shutdown time. Used in project 37. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Test pyramid** — Many fast unit tests, fewer slice tests, few full end-to-end tests. Used in project 20. ([Ch 12](../chapters/12-testing-spring-apps/README.md))
- **Test slice (`@WebMvcTest`, `@DataJpaTest`)** — A test that starts only one layer of the app, which is much faster than a full context. Used in projects 1, 4, 7, 11, 12, 20. ([Ch 12](../chapters/12-testing-spring-apps/README.md))
- **Testcontainers** — Library that starts real databases and brokers in Docker for the duration of a test. Used in projects 4, 8, 12, 13, 14, 15, 16, 17, 20, 22, 23, 26, 27, 28, 30, 31, 35, 38. ([Ch 12](../chapters/12-testing-spring-apps/README.md))
- **Thread pool / executor** — A fixed set of worker threads that take tasks from a queue. Used in projects 3, 10, 24, 26, 29, 33, 36, 38. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Throughput** — How much work is completed per unit of time (requests or messages per second). Used in projects 2, 10, 13, 14, 16, 17, 18, 19, 22, 23, 24, 32, 33, 37. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Thymeleaf** — Server-side HTML template engine supported by Spring Boot. Used in project 12.
- **Time limiter** — Resilience4j pattern that gives up on a call after a set time. Used in projects 27, 33. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))
- **Token bucket** — Rate-limit algorithm: a bucket refills with tokens at a steady rate and each request takes one, which allows short bursts. Used in project 22. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Token customizer** — Hook in Spring Authorization Server that adds or changes claims in issued tokens. Used in project 34. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Token exchange (RFC 8693)** — OAuth2 grant that swaps one token for another, e.g. a narrower token for a downstream service. Used in projects 12, 34, 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Token relay** — Forwarding the logged-in user's access token from a gateway or BFF to downstream APIs. Used in projects 12, 27, 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))
- **Tolerant reader** — A client that ignores unknown fields and copes with missing optional ones, so additive API changes don't break it. Used in projects 21, 37. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Tool calling / `@Tool` / tool schema** — The model asks the app to run a described function; Spring AI builds the JSON schema from `@Tool` methods and runs the call. Used in project 36. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **`ToolContext`** — Spring AI object carrying extra data (such as the current user) to a tool without exposing it to the model. Used in project 36. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **Top-K and similarity threshold** — How many nearest chunks to retrieve and the minimum similarity a chunk needs to be used. Used in project 30. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **Topic / partition / offset (Kafka)** — A topic is a named stream split into ordered partitions; an offset is a record's position in its partition. Used in projects 16, 24, 26, 28, 35, 39. ([Ch 14](../chapters/14-messaging-async-and-scheduling/README.md))
- **Tracing agent (GraalVM)** — A JVM agent that records reflection and resource use during a run to generate native-image metadata. Used in project 32. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **Transaction (database)** — A group of operations that succeed or fail together. Used in projects 2, 4, 6, 8, 9, 13, 14, 15, 16, 17, 18, 20, 23, 24, 26, 27, 28, 31, 35, 38, 39. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **`@Transactional`** — Declares that a method runs inside a database transaction; a proxy begins it before the call and commits or rolls back afterwards. Used in projects 4, 8, 9, 13, 20, 23, 28. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **`@TransactionalEventListener`** — Event listener that runs at a transaction phase, by default only after the publishing transaction has committed. Used in projects 9, 13, 14, 15, 28, 31. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **`TransactionTemplate`** — Programmatic transaction around a block of code, for boundaries smaller than a method. Used in project 13. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **Transient failure** — A short-lived error (timeout, 503) that may succeed if tried again. Used in projects 3, 18, 24, 33. ([Ch 6](../chapters/06-rest-clients-and-http-interfaces/README.md))
- **TTL and jitter (cache)** — Time to live of a cache entry; adding random jitter stops many keys expiring at the same moment. Used in projects 2, 3, 14, 16, 17. ([Ch 9](../chapters/09-spring-data-beyond-jpa-and-caching/README.md))
- **Two-phase commit (2PC/XA)** — Protocol coordinating one commit across several resources; avoided in microservices because it blocks and couples them. Used in project 28. ([Ch 16](../chapters/16-spring-cloud-and-microservices/README.md))

### U

- **Unique index / constraint** — Database rule that rejects a second row with the same value; the safest guard against duplicates. Used in projects 2, 8, 13, 23, 26, 35, 39. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))
- **Unknown outcome** — A call timed out and you can't tell whether it succeeded; never blindly retry money movements in this state. Used in projects 18, 39.
- **Upcaster** — Code that converts an old stored event format into the current one while reading. Used in projects 21, 35.
- **Upsert (`INSERT ... ON CONFLICT`)** — Insert a row, or update/ignore it if a row with the same key already exists, in one atomic statement. Used in projects 2, 8, 18, 20, 38, 39. ([Ch 7](../chapters/07-data-access-jdbc-and-transactions/README.md))
- **URI / URL** — Address of a resource; a URL also says how to reach it (`https://...`). Used in projects 1, 2, 3, 7, 10, 11, 12, 15, 20, 21, 24, 25, 27, 29, 33, 34. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **UTC** — Coordinated Universal Time; store timestamps in UTC and convert only for display. Used in project 6.
- **UUID** — 128-bit unique identifier that can be generated anywhere without coordination. Used in projects 21, 26. ([Ch 8](../chapters/08-spring-data-jpa-and-hibernate/README.md))

### V

- **`Vary` header** — Response header listing request headers (like `X-API-Version`) that caches must include in their key. Used in project 21. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Vector store** — Database of embeddings that finds the chunks most similar to a query. Used in project 30. ([Ch 17](../chapters/17-spring-ai-and-whats-new/README.md))
- **`version` mapping attribute** — Framework 7 attribute on `@GetMapping` and friends that ties a handler to an API version. Used in projects 3, 21. ([Ch 5](../chapters/05-spring-mvc-and-rest/README.md))
- **Virtual threads** — Cheap JVM-managed threads (Java 21+) that free their OS thread while waiting, so blocking code scales. Used in projects 2, 3, 6, 10, 13, 15, 19, 23, 24, 26, 27, 33, 38. ([Ch 15](../chapters/15-observability-and-production/README.md))

### W

- **W3C** — The World Wide Web Consortium; its Trace Context standard defines the `traceparent` header. Used in project 29. ([Ch 15](../chapters/15-observability-and-production/README.md))
- **WebFlux vs MVC + virtual threads** — Spring's reactive web stack vs the classic servlet stack running each request on a virtual thread. Used in projects 19, 21, 25, 27, 29, 30. ([Ch 13](../chapters/13-reactive-and-webflux/README.md))
- **Webhook** — An HTTP callback: your server POSTs an event to a URL the customer registered. Used in projects 10, 24, 39.
- **`WebTestClient`** — Fluent test client for WebFlux endpoints. Used in project 19. ([Ch 13](../chapters/13-reactive-and-webflux/README.md))
- **WireMock** — Stub HTTP server for tests, able to simulate slow or failing services. Used in projects 13, 20, 27, 33. ([Ch 6](../chapters/06-rest-clients-and-http-interfaces/README.md))
- **`@WithMockUser` / `jwt()`** — spring-security-test helpers that run a test as a fake user or with a fake JWT. Used in projects 11, 12. ([Ch 10](../chapters/10-spring-security-1-fundamentals/README.md))

### X

- **XSS** — Cross-site scripting: attacker JavaScript running in your page, able to read anything scripts can. Used in project 40. ([Ch 11](../chapters/11-spring-security-2-tokens-and-oauth2/README.md))

### Y

- **YAML** — Indentation-based configuration format (`application.yaml`). Used in projects 3, 5, 7, 27. ([Ch 3](../chapters/03-configuration-and-auto-configuration/README.md))

### Z

- **Zombie worker** — A worker that was presumed dead (its lease expired) but is still running and may write stale results. Used in project 38.
