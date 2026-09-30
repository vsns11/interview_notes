# Microservices Patterns — Runnable Demos

A Gradle project with **one chapter folder per pattern**, matching the layout of the
`ocp-java21-study` project. Each chapter is its own Gradle **source set**, so default-package classes
never collide and no chapter can accidentally depend on another.

Notes for each pattern live alongside in [`../microservices-patterns/`](../microservices-patterns/).

---

## Running

```bash
./gradlew listDemos          # what exists and what each one shows
./gradlew runCh06            # run one chapter (main class already wired)
./gradlew compileAll         # compile every chapter
./gradlew runAll             # run all twelve in order
```

No arguments needed — each `runChNN` task knows its own main class.

Toolchain is pinned to **Java 21** via the Gradle toolchain, so it builds regardless of the JDK
running Gradle.

---

## Layout

```
demos/
├── build.gradle             # per-chapter source sets + run tasks
├── settings.gradle
├── gradlew / gradle/        # wrapper (Gradle 9.4.1)
└── chapters/
    ├── 02-strangler-fig/            StranglerFigDemo.java
    ├── 03-api-gateway/              ApiGatewayDemo.java
    ├── 04-bff/                      BffDemo.java
    ├── 05-service-discovery/        ServiceDiscoveryDemo.java
    ├── 06-circuit-breaker/          CircuitBreakerDemo.java
    ├── 07-bulkhead/                 BulkheadDemo.java
    ├── 08-retry/                    RetryDemo.java
    ├── 09-sidecar/                  SidecarDemo.java
    ├── 10-saga/                     SagaDemo.java
    ├── 11-event-driven/             EventDrivenDemo.java
    ├── 12-cqrs/                     CqrsDemo.java
    └── 13-config-externalization/   ConfigExternalizationDemo.java
```

Chapter numbers match the notes and the course chapters, so `runCh06` ↔
`06-circuit-breaker.md`.

---

## The demos

| Task | Pattern | The number to look at |
|---|---|---|
| `runCh02` | [Strangler Fig](../microservices-patterns/chapters/02-strangler-fig/README.md) | Same user always routed to the same system; rollback with no deploy |
| `runCh03` | [API Gateway](../microservices-patterns/chapters/03-api-gateway/README.md) | **279ms → 129ms** switching aggregation to parallel |
| `runCh04` | [BFF](../microservices-patterns/chapters/04-bff/README.md) | Mobile receives **7%** of the shared payload |
| `runCh05` | [Service Discovery](../microservices-patterns/chapters/05-service-discovery/README.md) | Registry dies; cached clients keep routing |
| `runCh06` | [Circuit Breaker](../microservices-patterns/chapters/06-circuit-breaker/README.md) | `extra real calls during OPEN: 0` |
| `runCh07` | [Bulkhead](../microservices-patterns/chapters/07-bulkhead/README.md) | Shipping **0/5** shared pool vs **5/5** bulkheaded |
| `runCh08` | [Retry](../microservices-patterns/chapters/08-retry/README.md) | Herd: **1000/1000** in one bucket vs **~208** with jitter |
| `runCh09` | [Sidecar](../microservices-patterns/chapters/09-sidecar/README.md) | Upstream called 3×, app asked once |
| `runCh10` | [Saga](../microservices-patterns/chapters/10-saga/README.md) | Ledger shows charge **and** refund — compensation ≠ rollback |
| `runCh11` | [Event-Driven](../microservices-patterns/chapters/11-event-driven/README.md) | Producer blocked **114ms → 0ms** |
| `runCh12` | [CQRS](../microservices-patterns/chapters/12-cqrs/README.md) | Write store updated, read model stale — the consistency window |
| `runCh13` | [Config Externalization](../microservices-patterns/chapters/13-config-externalization/README.md) | Flag flips every service at once, no deploy |

---

## What these are and aren't

**They are** minimal, in-process illustrations built so one `main` makes the mechanism visible. Each
prints the *contrast* — the failure mode first, then the pattern fixing it.

**They are not** production implementations. Real systems use Resilience4j, Kafka, Kubernetes, Istio
and Spring Cloud. Every pattern's notes carry a **Production** section with the actual library
configuration, and an **Implementation pitfalls** list.

Where a demo simplifies something that matters, the notes say so — `EventDrivenDemo`'s bus is
in-memory with no persistence, and `SagaDemo`'s saga log doesn't survive a crash.

## Suggested order

1. `runCh06`, `runCh07`, `runCh08` — the resilience trio; most concrete
2. `runCh03`, `runCh04` — request shaping at the edge
3. `runCh05`, `runCh09`, `runCh13` — infrastructure concerns
4. `runCh10`, `runCh11`, `runCh12` — data consistency; hardest and most valuable
5. `runCh02` — migration, once the rest makes sense

## Worth experimenting with

- **Ch06** — set the threshold to `1` and watch the breaker flap on a single blip
- **Ch07** — raise `POOL` to 50 and see starvation disappear; that's *why* sizing matters
- **Ch08** — set `useJitter=false` in section 5 and read the bucket count again
- **Ch10** — reorder the steps so payment runs last, and see which compensation becomes unnecessary
- **Ch12** — raise the projection lag to 2000ms to make the consistency window impossible to ignore
