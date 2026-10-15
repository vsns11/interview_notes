# Module 5 — Surviving Failure (Reliability)

## Module Introduction
*Original lesson: [Module introduction on DesignGurus ↗](https://www.designgurus.io/course-play/system-design-patterns/doc/6a50724c914a537a0045f5fe)*


**What this module is about.** An arrow from service A to service B can break four ways in production: B never replies (a *hang*), replies with an error, does only part of the job, or replies too late to be useful. Seven well-known patterns let you handle these outcomes deliberately instead of discovering them by accident.

**Why "rare" failures are not rare.** A checkout processing 100,000 orders a day where 1 call in 200 fails sees **500 failures every day**. Reliable designs plan for that measured number — and it's why interviewers keep asking, of every arrow you draw, "and what if B doesn't answer?"

**Pattern picker** — name the failure, then choose the tool:

| What the failure looks like | Pattern |
|---|---|
| Silence; waiting callers keep stacking up | Timeout |
| Breaks right now, then works again a few seconds later | Retry with Backoff |
| You can't tell whether your payment actually went through | Idempotency |
| Stays broken for minutes, not just moments | Circuit Breaker |
| No errors anywhere, yet one workload is swallowing all the capacity | Bulkhead |
| A single bad message holds up every message behind it | Dead Letter Queue |
| All the mechanisms did their job, yet the user still landed on an error page | Graceful Degradation |

**Two kinds of pattern.** Six are **mechanisms** deciding *when to stop* — waiting, retrying, calling, redelivering. Graceful Degradation is a **policy** deciding *what the user gets* while a part stays broken. Mechanisms alone just produce errors faster; a policy alone has nothing enforcing it.

**Why the order matters.** Each lesson solves what the previous one causes or reveals — timeouts expose failures, retries risk double writes, idempotency makes them safe, breakers handle sustained outages of a **dependency** (a service or system yours calls), bulkheads handle success-shaped floods, DLQs catch messages that never succeed, and degradation decides what users see (the full chain is in the [capstone debrief](#capstone-hardening-a-payment-flow)). Short on time? Timeout, Retry, Idempotency and Circuit Breaker are the "critical four"; they build on each other, so study them together.

**Four terms used everywhere in this module**
- **Transient vs. sustained failure.** *Transient* clears on its own within seconds (e.g. a lost network **packet**, one small chunk of data in transit); *sustained* persists because the service is truly down or swamped. Retries fit transient, breakers sustained; the wrong tool piles load onto an outage.
- **Fail open vs. fail closed.** When a check can't run, *failing open* lets the operation proceed; *failing closed* blocks it. Nice-to-haves may fail open; security and money always fail closed — a down login service must never let everyone in.
- **At-least-once delivery.** Queues and retry loops never lose work but may deliver an item more than once; receivers must handle repeats safely.
- **Little's law.** *Work in progress = arrival rate × time per item.* 300 requests/s × 2 s = 600 in flight, so 600 threads (a thread is the unit of execution handling one request) if each holds one. Three lessons use it to predict exactly when a server runs out ([Module 0](../00-system-design-fundamentals/README.md#littles-law)).

**Before you start** — for a system you know ("I don't know" is a fine start; the capstone repeats these):
1. If something your service calls goes silent — no error, nothing — how long does it wait, and who chose that number?
2. If your service retries a failed payment or write, what prevents a double charge?
3. When your most important page's *least* important dependency fails, what exactly does the user see?

## Timeout
*Original lesson: [Timeout on DesignGurus ↗](https://www.designgurus.io/course-play/system-design-patterns/doc/6a4fe0a43592acf1ba4501f2)*


**What it is.** A deadline on every network call: if no reply arrives by then, the caller stops waiting, marks the call failed, and releases what it held. *Analogy:* after ten unanswered rings you hang up and call another restaurant instead of holding the phone all evening.

**The problem: waiting is not free.** Many clients have no deadline until you set one: Python's widely used `requests` library waits forever by default, and a raw TCP connection (the basic connection most protocols run on; see [Module 0](../00-system-design-fundamentals/README.md#tcp-udp-and-quic)) to a silently dead machine can stay open for minutes before the OS gives up.

A waiting call holds a thread, a connection, often a lock (a marker keeping other work off the same data). At 300 requests/s with a dependency hanging 30 s per call, keeping up needs 300 × 30 = **9,000 threads**; a normal service has a few hundred, so it freezes in about **one second**. *Waiting*, not errors, killed it.

Timeouts come first for a second reason: a hung call is invisible (no error, no metric, no signal), while a timed-out call is an *event* that retries can retry and breakers can count. Without timeouts, the later patterns have nothing to see.

The figure: a timer (e.g. 300 ms) starts with the request and, on expiry, the client deliberately fails the call; a normal call returns in time. Nothing hangs forever, resources are released, and "slow" becomes a countable failure — the foundation for retries, breakers and SLOs (service-level objectives: the **latency** — response time — and **availability** — share of time working — targets a team commits to).

![Timeout Pattern](images/timeout-1.png)
*Figure — Timeout Pattern (course diagram)*

**How it works — three rules**

1. **Measure; don't copy-paste.** The industry's most common timeout is whatever was copied — usually 30 seconds — which protects nothing, since no user is still waiting at second 29. Measure the dependency while *healthy*, typically its **p99** (the latency 99 of 100 calls beat). Healthy p99 of 120 ms → timeout ~**2–3×, roughly 300–400 ms**: room for slow-but-normal calls, fast cutoff for real hangs. **One value per dependency** — an 8 ms database and a 900 ms ML model can't share a number.
2. **Connection timeout much tighter than request timeout.** "Can I reach you at all?" reveals an unreachable host within milliseconds; "can you answer?" involves real, variable work.
3. **Pass the leftover time down the chain (deadline propagation).** An API promising 2 s that makes three sequential calls, each with a fresh 2 s timeout, has a worst case of 3 × 2 = **6 seconds**. Each hop should inherit only what's *left*:
   - The **API gateway** (front-door server receiving every outside request) starts with 2,000 ms.
   - Auth (checks who the user is) spends 100 ms → 1,900 ms remain.
   - The database spends 400 ms → 1,500 ms remain.
   - Search's timeout = min(its own limit, 1,500 ms).

   gRPC (a widely used remote-call framework) carries the caller's deadline with the request automatically; plain HTTP mostly doesn't, so deep services keep working on requests that already failed upstream.

**Trade-offs**
- *You gain:* bounded damage (a failing dependency holds threads only for the timeout), failures as signals for retries, breakers, alerts and dashboards, and a predictable worst-case response time — the basis of any latency promise.
- *You pay:*
  - **False alarms.** The timer sometimes fires on a call about to succeed; for a *write*, that leaves real uncertainty — did it happen? Hence the Idempotency lesson.
  - **Ongoing tuning.** Too long protects nothing; too short creates errors on ordinary slow days. Recheck when a dependency's latency changes.
  - **The work doesn't stop.** A timeout ends *your waiting*, not *the server's working*; the database may keep crunching an abandoned query, burning capacity under heavy load on answers nobody reads. gRPC propagates *cancellation*; plain HTTP mostly doesn't.

**When not to use it.** Never go without timeouts; every misuse is a wrong *value*:
- **Genuinely long jobs** (reports, **transcoding** — converting video formats or sizes — large LLM jobs, an LLM being a large language model like the one behind a chat assistant) shouldn't get a 10-minute live-call timeout. Make them async: accept, reply immediately, deliver the result via a message queue, **polling** (client asks "done yet?" periodically) or a webhook (the server calls you back when finished).
- **One global number** is right for no dependency.
- **Library defaults** range from 30 seconds to infinity; both are wrong. Check every client.

**In the interview.** Rarely asked directly, yet every arrow silently asks "how long do you wait if this hangs?" — answer before asked. When it comes up: "what happens if this service just… doesn't respond?"

> **30-second answer:** Every network call gets a deadline at 2–3× the dependency's measured healthy p99, with connection timeouts much tighter than request timeouts. Timeouts turn silent hangs into countable failures for my retries and breakers to react to. Along a call chain I pass the remaining budget down, as gRPC does, so inner calls never outlive the request that started them.

- *Follow-up: "How do you pick the number?"* — Measure: healthy p99 × a small factor, per dependency, revisited when its latency shifts. An unreasoned "30 seconds" fails both ways: useless in incidents, wasteful in recovery.
- *Follow-up: "The timeout fired, but the write actually went through — now what?"* — Unavoidable uncertainty; make the write safe to repeat — the whole topic of Idempotency.
- *Mistake that fails candidates:* a multi-service design with no word on hanging calls, or "30 seconds" with no reasoning — the mark of someone who has only seen systems on good days.

**Self-check:**
- Q1: Why should connection timeouts be tighter than request timeouts, and where should the request timeout come from? → Unreachable hosts reveal themselves in milliseconds, so extra waiting gains nothing. Request timeout = measured healthy p99 × 2–3, per dependency.
- Q2: What does a timeout *not* stop, and why does that matter most during overload? → Server-side work: abandoned queries keep consuming a struggling server's capacity, deepening overload. Propagated cancellation (as in gRPC) fixes it where available.
- Q3: An API promises 2 s, makes three sequential calls each with a 2 s timeout, and users see 6-second errors. Why, and how do you fix it? → Each hop got a fresh budget: 3 × 2 = 6 s. Use deadline budgeting — timeout = min(own limit, time remaining) — so the request always fails by 2 s.

**Related patterns:** [Retry](#retry-with-exponential-backoff) (reacts to the failures timeouts create); [Circuit Breaker](#circuit-breaker) (can't notice hangs without them); [Idempotency](#idempotency) (settles "did my write go through?"); [Bulkhead](#bulkhead) (limits threads stuck waiting within the timeout window).

**TL;DR**
- **Problem:** calls without deadlines can hang forever, silently holding threads and connections until one slow dependency consumes the whole caller.
- **Mechanism:** a deadline on every call at 2–3× healthy p99, a tighter connection timeout, and the remaining budget passed down the chain.
- **Costs:** occasional false failures (write uncertainty), ongoing tuning, abandoned server work unless cancellation propagates.
- **Skip it when:** never — move long work to async and replace copied values with measured ones.

## Retry with Exponential Backoff
*Original lesson: [Retry with Exponential Backoff on DesignGurus ↗](https://www.designgurus.io/course-play/system-design-patterns/doc/6a4ff5ac3c9b22d583068469)*


**What it is.** When a call fails for a self-correcting reason, try again — waiting longer after each failure, randomizing each wait so thousands of clients don't retry together, capping attempts, and retrying only errors that could turn out differently. *Analogy:* on a busy line you don't redial every second forever; you wait a little, then longer, then give up — and if the whole town redialed in sync, the line would stay jammed.

**The problem.** A checkout calls a healthy payment provider, both sides correctly coded, yet ~1 call in 200 fails: networks drop packets, reset connections, occasionally route to a bad server. With 100,000 orders/day, **500 customers a day** get an error on a request that would likely have worked 100 ms later. Such brief, self-clearing failures are **transient**; treating each as final throws away work a second try would finish.

**Why the obvious fix fails.** "Retry immediately, up to 3 times" goes wrong at scale:
- **Wrong moment.** An overloaded provider is still overloaded a microsecond later. At 20,000 requests/s, a one-second failure with three immediate attempts can land **60,000 requests/s** on the provider when it has least room — a self-inflicted **retry storm**.
- **Everyone at once.** Requests that failed together retry together on a fixed schedule, producing synchronized spikes — a **thundering herd** that can knock a recovering service back down.
- A third problem (next lesson): if the payment *succeeded* but the response was lost, a retry can charge twice.

**How it works.** The figure follows one payment call: attempt 1 times out; wait random 0–100 ms; attempt 2 gets a 500 (HTTP "internal server error"); wait random 0–200 ms; attempt 3 succeeds, unnoticed by the customer. Wait windows double (0–100, 0–200, 0–400, 0–800 ms…, capped at 5 s).

![Retry with Exponential Backoff](images/retry-with-exponential-backoff-1.png)
*Figure — Retry with Exponential Backoff (course diagram)*

The formula ("full jitter"):
```
window = base × 2^attempt          (e.g. base 100 ms → 100, 200, 400 ms…)
wait   = random(0, min(cap, window))   (cap e.g. 5 s)
```
Each part has one job:
1. **Base delay (~100 ms):** lets the most frequent transient problems (lost packets, resets) clear.
2. **Doubling ("exponential backoff"):** each extra failure suggests a bigger problem, so back off harder — the difference between a patient client and one that worsens the outage.
3. **Randomness (jitter) — not optional:** scatters the herd across the window. Backoff *without* jitter just reschedules the same synchronized wave to 100, 200, 400 ms.
4. **Caps and budgets:** a maximum wait (never minutes), a maximum attempt count (3–5 for anything a user waits on), and ideally a service-wide **retry budget** ("retries may add at most 10% extra traffic"); once spent, stop — the failures clearly aren't transient.
5. **Retry only errors that can change.** Retry: timeouts, connection failures, 500 (server error), 503 (service unavailable), 429 (too many requests — obey its `Retry-After` header). Never: 400 (bad request), 401 (bad credentials), 404 (not found); they fail identically every time.

**Walkthrough with numbers**
- *One request:* a 200 ms network blip; attempt 1 fails, wait ~60 ms; attempt 2 fails, wait ~150 ms; attempt 3 succeeds. Checkout takes ~**400 ms longer** but completes — times 500 customers a day.
- *The fleet:* the provider struggles for one second at 20,000 requests/s. Immediate retries fire ~20,000 × 3 attempts into the outage, then the backlog retries at the instant it ends: ~**3× normal load** at the weakest moment. With backoff and jitter, retry rounds spread over 100, 200, 400 ms and the provider sees **a small bump a few percent above normal**. Same per-request rule, completely different system behavior: spikes become smooth load.

**Trade-offs**
- *You gain:* transient failures vanish from screens, dashboards and support queues; recovering services get smooth traffic; fewer 2 AM manual re-runs of **batch jobs** (scheduled programs processing a large pile of work).
- *You pay:*
  - **Slower worst cases:** each retry adds its wait (≈400 ms above); your slowest requests now contain your retry policy.
  - **Extra load in real outages:** retries against a dead dependency are waste; backoff softens it, but only a circuit breaker or retry budget stops it.
  - **Multiplication across layers:** app 3× × gateway 3× × service 3× = **27 requests** per tap at the bottom. Retry in one deliberately chosen layer (two at most), usually nearest the failure.
  - **Hidden chronic problems:** silent retries make a 5%-failing dependency look healthy while adding latency and load; record retry counts — a rising rate is an early warning.
  - **A hard prerequisite:** retrying a *write* is safe only if doing it twice is harmless (idempotency, next lesson). Until then, don't retry it.

**When not to use it**
- **Writes not safe to repeat:** add an idempotency key first.
- **Errors that can't change** (validation, permission, not-found): classify errors before looping.
- **When the user has waited enough:** a fast failure plus a Retry button often beats four silent attempts over 3 seconds. User-facing paths get small budgets (1–2 retries); background jobs generous ones.
- **Sustained failures:** the circuit breaker's job.
- *Common misuse:* "retry 3 times" at every layer, even on 400s, no jitter, no metrics — painfully obvious at the next partial outage.

**Retry now vs. queue for later.** Retry is the optimistic tool, the breaker the pessimistic one, and real systems stack them. A message queue is the less obvious alternative; both answer "this call failed — now what?"

| Situation | Reach for |
|---|---|
| The caller needs the answer during this request (payment authorization, a user is waiting) | Synchronous retry with backoff |
| The caller needs the work to happen, but not the answer right now (send a receipt, sync the warehouse) | Put it on a queue; the consumer retries over minutes or hours |
| The retry has to survive a process crash or a long outage | Queue, with a dead letter queue as the final safety net |
| The entire retry window fits inside one request's timeout | Synchronous retry |

Ask aloud: *does anyone need this result right now?* If not, the sturdiest retry is a durable one: queued attempts can span hours, survive restarts, and be kept safely when exhausted.

**Real-world examples**
- **AWS SDKs** use exponential backoff with jitter by default; AWS's "Exponential Backoff and Jitter" post, born of real thundering herds on DynamoDB (Amazon's managed key-value database), is the classic explanation.
- **gRPC and Envoy** (a popular network proxy) support retry budgets capping retries at a percentage of live traffic.
- **Stripe** pairs retries with idempotency keys on every money-moving endpoint — what makes retrying a charge safe.
- **Kafka producers** (programs writing to Kafka, a widely used message log) retry sends with backoff; enabling idempotence adds the next pattern in the client.
- **For AI engineers:** LLM APIs shed load with 429 and "overloaded" errors, and the official Anthropic and OpenAI **SDKs** (ready-made client libraries) include backoff-with-jitter by default. In production: obey `Retry-After` exactly, keep a per-provider retry budget, and when one is exhausted, switch models at your **LLM gateway** (a proxy your apps call instead of providers directly) rather than retrying into a sustained outage.

**In the interview.** "What happens if this call fails?" is the most common follow-up; answer "retry" and the trap springs: "Could your retries make an outage worse?" Raise retries unprompted, with idempotency in the same breath.

> **30-second answer:** I retry with exponential backoff and jitter — about 100, 200, 400 ms, each randomized so clients don't retry in sync and flatten a recovering service — with capped attempts, and only for errors that can change, like timeouts and 500s. Writes are made idempotent first so a retry can't apply twice. AWS SDKs do exactly this by default. When failure is sustained rather than transient, I stop retrying and let a circuit breaker take over.

- *Follow-up: "Why jitter? What breaks without it?"* — Clients that failed together retry together; backoff alone reschedules the herd to 100, 200, 400 ms. Randomized waits spread them, turning spikes into a smooth plateau.
- *Follow-up: "How do you stop retries from making an outage worse?"* — Weakest to strongest: backoff spaces attempts; a retry budget caps them at a traffic fraction; a circuit breaker drops them to zero once failure is clearly sustained. Structurally: retry at one layer.
- *Mistake that fails candidates:* "I'd just retry 3 times" — no backoff, jitter, idempotency or error classification: four follow-ups you'll miss.

**Self-check:**
- Q1: Write the full-jitter formula and say what the cap protects you from. → `wait = random(0, min(cap, base × 2^attempt))`. The cap bounds worst-case waits so a persistently failing call can't back off into minutes, blowing response budgets and holding resources, and keeps timing predictable for capacity planning.
- Q2: App retries 3×, gateway 3×, backend 3× on DB calls; the DB has a 10-second brownout (slow but not fully down). What does it experience and where should retries live? → Up to 27 attempts per tap — self-inflicted overload, every layer's queue full of duplicate work. Retry in one layer, usually nearest the failure (the backend's DB call, with a budget); outer layers fail fast and show a Retry button.
- Q3: A dependency recovers from a 30-second outage, collapses 100 ms later, recovers, collapses again, in perfect rhythm; your config is 3 attempts, fixed 100 ms delay, no jitter. → Your clients are a scheduled thundering herd: the whole backlog lands in one wave at each recovery. Fix with doubling delays, jitter and a circuit breaker so sustained failure stops generating retry pressure. The other team's service is fine.

**Related patterns:** [Idempotency](#idempotency) (required before retrying writes); [Timeout](#timeout) (defines "failed"); [Circuit Breaker](#circuit-breaker) (takes over for sustained failure); [Dead Letter Queue](#dead-letter-queue) (where exhausted queue retries land); Rate Limiting (the other side — good retriers obey 429 and `Retry-After`, good limiters send them).

**TL;DR**
- **Problem:** network calls fail briefly all the time, and naive instant retries turn that into synchronized spikes against an already struggling dependency.
- **Mechanism:** longer waits after each failure (100, 200, 400 ms…), randomized, capped attempts and delays, only changeable errors, within a service-wide retry budget.
- **Costs:** slower worst cases, extra load in real outages, 27× multiplication across careless layers, hidden chronic failures, mandatory idempotency for writes.
- **Skip it when:** the write isn't safe to repeat, the error can't change (400s), the user's patience is spent, or the failure is sustained.

## Idempotency
*Original lesson: [Idempotency on DesignGurus ↗](https://www.designgurus.io/course-play/system-design-patterns/doc/6a4ff91c702b283eecb7c2e9)*


**What it is.** An operation is **idempotent** if running it many times has exactly the effect of running it once. For operations that aren't (like "charge this card"), the client attaches a unique **idempotency key**; the server remembers each handled key with its result and answers repeats with the saved result instead of executing again. *Analogy:* pressing an elevator call button five times doesn't bring five elevators; the key gives a card charge that behavior.

**The problem.** A customer clicks Pay on a **$499** course bundle; the provider charges the card but the success response is lost; ~100 ms later the retry sends an identical request, which the provider treats as new and charges again. The customer sees **$998**.

Root cause: a timeout means "no response came back," *not* "the operation failed" — the request may never have arrived, or finished with only the reply lost. Repeating reads is harmless; repeating an uncertain *write* can apply it twice. Duplicate writes come from retry policies, double-clicks, and at-least-once message queues.

The numbers: ~500 uncertain timeouts a day; if a third had already charged → **~165 double charges per day**, each costing a refund, support time and a ~**$15 chargeback fee** (the customer's bank reverses the charge, marking your record with the processor): ~**$4,000 a day**, plus customers watching you bill them twice.

**Why the obvious fixes fail**
- **"Check before you write"** ("is there already a recent charge for this order?"). Between check and write, two concurrent retries can both find nothing and both charge — a *race condition* (outcome depends on uncontrollable timing), made far more frequent by retry bursts than tests suggest.
- **"Don't retry writes."** Brings back 500 failed checkouts a day.
- **"Detect duplicates by similarity"** (equal charges, same user, within 60 s). Blocks someone legitimately buying two $499 gifts, and misses a real duplicate at 61 seconds.
- Common flaw: they lack information *only the client has*. To the server, a retry and a genuinely new identical order look the same; only the client knows both came from one click, so the client must send that identity.

**How it works.** Replaying the $998 incident with idempotency: checkout sends "charge $499, Idempotency-Key: ord_7f3a"; the provider atomically claims the key ("first time seen"), charges, stores the result under the key; the response is lost; the retry carries the *same* key, the claim finds it taken, and the stored result returns — 200 OK, no second charge.

![Idempotency](images/idempotency-1.png)
*Figure — Idempotency (course diagram)*

1. **The client creates the key before the first attempt**, one per *logical* operation (`ord_7f3a` for this order's payment), sent with every retry. Create it at the Pay click and store it with the pending order, so even after an app crash and reload the retry carries the same key. A client inventing a new key per retry only *looks* idempotent.
2. **Claiming is atomic** (one indivisible step, no gap): insert the key under a uniqueness rule — insert if missing, fail if present. The database guarantees one winner, which truly closes the check-then-write race: of two simultaneous requests, exactly one executes; the other gets the winner's result.
3. **Save the result, not just the key.** A retrying caller needs the original answer (charge ID, status), not an error; a duplicate arriving mid-execution gets "in progress — wait" (or HTTP 409 Conflict), not a second run.
4. **Keys expire.** Stripe keeps them **24 hours**; keep them longer than your longest realistic retry window. This expiry timer is a **TTL (time to live)**.

**The cheaper option first: naturally idempotent operations.** "Set status to shipped" can run ten times; "increment login count" can't. Prefer *set* semantics ("balance = 90") over *increment* ("subtract 10"), **conditional writes** ("update only if version is still 6"), and **upserts** (insert if missing, else update). These never need a key store.

**Walkthrough with numbers** (100,000 orders/day, 1 in 200 payment calls times out, retries on)
- *Without idempotency:* ~500 uncertain outcomes/day → ~165 double charges → ~$4,000/day plus reputational damage.
- *With keys:* the retry's `ord_7f3a` is recognized in ~**5 ms** and the saved response returned. Double charges: **zero** — by design, not "rare."
- *Cost:* one key lookup per write (~**1 ms**) and 100,000 keys/day × ~1 KB saved responses ≈ **100 MB** at a 24-hour TTL — the cheapest protection in the course against your scariest failure.

**Trade-offs**
- *You gain:* safe retries, redeliveries and double-clicks for writes, by design; **the effect of exactly-once on top of at-least-once delivery** (the only "exactly once" that exists — below); calmer incidents, since "did the retry double anything?" stops being a question.
- *You pay:*
  - **A key store on your busiest write path:** uniqueness guarantee, result storage, expiry — modest but real infrastructure.
  - **Client discipline:** keys made before attempt one and surviving crashes — an agreement with every client team that needs testing.
  - **The in-progress case:** a small *state machine* (states plus transition rules): claimed → in progress → completed.
  - **The boundary problem** (trickiest): the key protects only the claim-and-execute step. Side effects outside it — confirmation email, warehouse webhook, third-party call — can still happen twice and need their own keys (e.g. key the email on the order ID).

**When not to use it**
- **Already naturally idempotent operations** — reads, deletes, full-value sets; key machinery is pure overhead.
- **When you can redesign instead:** the best key is the one you never need — "set balance to 90" or a conditional write over "subtract 10."
- **Streams where duplicates are acceptable** — high-volume analytics deduplicated later, or where a 0.5% duplicate rate changes no decision.
- **As business-level duplicate detection.** A key means "the same operation, retried," not "the user probably didn't mean to do this twice." Two deliberate identical orders are two operations with two keys; blocking them is a product decision.
- *Common misuse:* key stores on every endpoint while the operations stay unsafe increments and appends — more complexity, no more clarity.

**Idempotency vs. "exactly-once delivery."** Interviewers raise this deliberately: exactly-once *delivery* is impossible over an unreliable network because any acknowledgment ("got it") can be lost, forcing the sender to resend and risk a duplicate (**at-least-once**) or not and risk loss (**at-most-once**). No third option exists. Systems *can* achieve exactly-once **processing**: at-least-once delivery plus an idempotent handler.

| You need | Build |
|---|---|
| No lost work | At-least-once delivery (retries, queues with acknowledgments) |
| No doubled effects | An idempotent consumer (keys, or natural idempotency) |
| "Exactly once" | Both of the rows above — there's no third ingredient |

Kafka's "exactly-once semantics" is exactly this: producer IDs plus sequence numbers make the broker's (Kafka server's) writes idempotent, combined with transactional consumers. "I'd use exactly-once delivery" names the problem; "at-least-once delivery plus an idempotent consumer" solves it.

**Real-world examples**
- **Stripe:** an `Idempotency-Key` header on every money-moving endpoint, saved responses replayed for 24 hours; its docs made the term industry vocabulary.
- **Kafka's idempotent producer:** a producer ID and sequence number per batch let the broker discard duplicates — idempotency keys by another name.
- **AWS SQS FIFO** dedupes by message ID within a **5-minute** window; **DynamoDB conditional writes** give the atomic claim with no extra infrastructure.
- **Webhook consumers:** providers openly deliver at-least-once, so every handler dedupes on event ID.
- **For AI engineers:** agent frameworks may retry a timed-out `send_email` or `create_ticket` tool call, or the agent loop may request it again; every tool with external effects needs an idempotency key derived from the tool-call ID. Usage-recording writes for billing need the same, or retries silently overcharge.

**In the interview.** Rarely "define idempotency"; it arrives as a follow-up once you say "retry" or draw a queue: "what if the payment succeeded but the response was lost?" or "your consumer crashes before acknowledging — what happens on redelivery?" Answer it in the same sentence as retries.

> **30-second answer:** A timeout is uncertain — the write may have succeeded with only the response lost — so a blind retry can apply it twice. The client creates one unique key per logical operation and sends it with every retry; the server atomically claims it, executes once, saves the result, and replays that result for duplicates. That's Stripe's Idempotency-Key model: an exactly-once effect on at-least-once delivery, the only exactly-once there is.

- *Follow-up: "Two requests with the same key arrive at the same moment — walk me through it."* — The claim is an atomic insert-if-absent: one wins and runs; the other sees it claimed, waits or gets "in progress," then receives the saved result. An answer involving check-then-write has just revealed your race.
- *Follow-up: "Why can't the server generate the key?"* — Only the client knows two physical requests are one logical operation; a server-made key differs per request, defeating the purpose. The client makes it before attempt one and keeps it across retries.
- *Mistake that fails candidates:* "check if it exists, then write" as the defense, or claiming exactly-once queue delivery handles it.

**Self-check:**
- Q1: Define idempotent; why must the client generate the key, and when? → N executions = one execution's effect. Only the client knows two requests are one intent; the key must exist before the first attempt and survive crashes (stored with the pending order).
- Q2: With standard idempotency keys on checkout, what duplicates can still happen? → Side effects outside the claim-and-execute boundary (emails, webhooks, third-party calls); intentional business duplicates (two keys); buggy clients making new keys per retry; duplicates arriving after the key's TTL.
- Q3: After enabling retries, customers get duplicate welcome emails, yet orders have no duplicate rows and the key store correctly replays results. Diagnose. → The email is sent outside the idempotent boundary: attempt one charged, emailed, lost its response; the retry correctly replayed the result, but the email code ran again. Move the email inside the boundary or, better, key it on the order ID (or send via a deduplicating queue). Each side effect needs its own idempotency plan.

**Related patterns:** [Retry](#retry-with-exponential-backoff) (why this lesson exists); [Message Queue](../02-moving-data/README.md#message-queue) (at-least-once makes every consumer a retry target); [Webhooks](../02-moving-data/README.md#webhooks) (dedupe on event ID); Saga (every step and undo step must be idempotent, since the coordinator retries them); Exactly-Once Semantics (how Kafka assembles exactly-once processing from these parts).

**TL;DR**
- **Problem:** timeouts are uncertain, so retries, redeliveries and double-clicks can apply writes twice — double charges, duplicate orders.
- **Mechanism:** the client makes one key per logical operation and reuses it; the server atomically claims it, executes once, saves and replays the result. Prefer naturally idempotent (set-style) operations.
- **Costs:** a key store on the busy write path, client key discipline, an in-progress state, side effects outside the boundary needing their own keys.
- **Skip it when:** the operation is already safe to repeat, duplicates are harmless downstream, or the "duplicate" is a business-rule question.

## Circuit Breaker
*Original lesson: [Circuit Breaker on DesignGurus ↗](https://www.designgurus.io/course-play/system-design-patterns/doc/6a4fcf72a310a275a13ff3ed)*


**What it is.** A wrapper around every call to a dependency that tallies recent failures; past a threshold it "trips" and for a while rejects every call immediately without trying the dependency; after a cooldown it lets a few test calls through to check recovery. *Analogy:* your home's breaker panel cuts power before an overloaded wire catches fire — no in-the-moment decision needed.

**The problem.** Black Friday, 2:15 PM. Checkout calls a recommendations service for an *optional* "customers also bought" widget. The recommendations database starts failing: 20 ms queries now take 30 seconds or never return. Checkout goes down.

Why: checkout has **200 threads** and **300 requests/second**, each now waiting up to 30 s; Little's law wants 300 × 30 = **9,000 threads**, so all 200 are stuck on the widget within about a second. Checkout already had short timeouts and retries, but even a 2 s timeout needs 300 × 2 = **600 threads** — three times the pool — and retries triple calls into the dying database (a *retry storm*).

The deeper issue: timeouts and retries both assume *a later call might work*. During a sustained outage that assumption does harm; the overloaded dependency needs **zero** new calls, and nothing so far delivers that.

**Why the obvious fixes fail**
- **"Tighten the timeout."** 2 s → 500 ms reduces checkout's thread use, but every request still hits the failing database, spending its remaining capacity on 300 hopeless queries/s. Protects the caller, not the dependency's recovery.
- **"Retry harder."** Retries are for transient failures; this is sustained, so they add load.
- **"Have someone flip a switch."** A flag like `recommendations_enabled: false` lets **on-call** (the engineer currently responding to alerts) stop calls — the right policy at human speed: outage at 2:15:00, alert at 2:19, flag at 2:26 — **11 minutes** of broken checkout versus about a second automatically.

**How it works.** The breaker sits between Service A (caller) and Service B (dependency). Closed (and successful half-open tests): requests pass normally. Open: the request is never sent; the caller gets a fast failure or fallback (e.g. a cached response) in ~1 ms.

![Circuit Breaker: request flow](images/circuit-breaker-1.png)
*Figure — Circuit Breaker: request flow (course diagram)*

It's a three-state **state machine**. Example transitions: Closed → Open when >50% of the last 20 calls fail; Open → Half-Open after a 30-second cooldown; Half-Open → Closed if all 5 test calls succeed, back to Open if any fails. Open never jumps straight to Closed — recovery must be *proved* by test calls.

![Circuit Breaker: state transitions](images/circuit-breaker-2.svg)
*Figure — Circuit Breaker: state transitions (course diagram)*

1. **Closed (normal).** Calls pass; failures (errors *and timeouts*) are counted over a recent window, e.g. the last 20 calls. Without timeouts, hung calls never count — hence Timeout first.
2. **Open (tripped).** Past the threshold, every call is rejected in ~1 ms without touching the dependency: your threads stop draining, and the failing service gets what it most needs — a break from traffic.
3. **Half-open (testing).** After the cooldown, a few trial calls go through: success closes the breaker; failure reopens it for another cooldown. Recovery is detected automatically without dumping full traffic on a just-recovered service.

To remember: *a breaker swaps slow, costly failures for fast, cheap ones, and gives the dependency room to recover.*

**Walkthrough with numbers** (window 20 calls, trip at 50%, 2 s timeout, 30 s cooldown, 5 half-open test calls)
- **2:15:00** — the recommendations DB starts failing; calls time out at 2 s.
- **2:15:01** — ~10 of the last 20 calls failed; the breaker trips. Damage: ten calls held threads 2 s each.
- **2:15:01 onward** — all 300 req/s get an instant "breaker open" in ~1 ms: 300 × 0.001 s = **0.3 threads** busy on average, versus 9,000 unprotected. The page renders without the widget; checkout is normal.
- **2:15:31** — cooldown ends; 5 test calls fail; the breaker reopens. Cost of checking: five calls.
- **2:19:31** — a few cycles later tests succeed; it closes and recommendations return automatically.
- Result: ~4 minutes of dependency failure, zero change in checkout availability — an optional feature traded for a few minutes to protect everything else.

**Trade-offs**
- *You gain:* failure isolation (one bad dependency can't swallow threads, connections or memory); fast failure (a millisecond, not a full timeout); automatic recovery without full-traffic bursts or 2 AM flag-flipping; and real relief for the dependency — the fastest help for an overloaded service is to stop calling it.
- *You pay:*
  - **False trips:** a short blip — a 5-second *garbage-collection pause* (the runtime freezing to reclaim memory) or a *database failover* (switching to a standby copy) — can trip it and reject good traffic for a whole cooldown. Threshold tuning is ongoing.
  - **More settings:** window, threshold, cooldown, test-call count. Bad values give a breaker that *flaps* (repeatedly opens and closes) or never trips.
  - **Per-server decisions:** state usually lives per *instance* (running copy); 50 instances each spend ~10 failed calls discovering the same outage. Shared state (e.g. in Redis) fixes it but adds a failure point; most teams accept the per-instance cost.
  - **It only decides *whether* to call,** not what the user sees; an open breaker without a fallback is just a faster error page (Graceful Degradation).

**When not to use it**
- **Local in-process calls:** breakers guard remote things that can hang or fail long; wrapping a function call is cargo-culting.
- **Calls where failing fast is worse than waiting**, like **capturing** a payment (actually taking the money) or sending a legal notice: wait, retry carefully, or queue.
- **Very low-traffic paths:** at 4 calls an hour, "half of the last 20" spans days; it never trips or trips on noise. A timeout suffices.
- **As a stand-in for capacity:** a DB breaker opening daily at peak means insufficient database capacity; don't hide it.
- *Common misuse:* breakers everywhere with defaults, no fallbacks, no dashboards — healthy traffic rejected and nobody knows why. Every breaker needs an **owner, a fallback, and an alert** when it opens.

**Circuit breaker vs. retry** — opposite diagnoses; interviewers love making you choose:

| Situation | Reach for |
|---|---|
| Transient failures: a dropped packet, one bad server, a brief failover | Retry with backoff |
| Sustained failures: the dependency is down or overloaded | Circuit breaker |
| Another try would probably succeed | Retry |
| Another try only piles load onto a struggling service | Circuit breaker |

In production, combine them with the **breaker outside the retries**, counting each request's *final* outcome after retries: retries absorb blips, and the breaker cuts their extra load once failure persists. (A breaker without retries rejects blips that would have recovered quickly.) The [Bulkhead](#bulkhead) answers a different question: the breaker decides *whether to call* (avoidance); the bulkhead limits *how much capacity* a dependency may occupy (containment). They stack.

**Real-world examples**
- **Netflix** built **Hystrix**, which made the pattern famous, after slow dependencies kept *cascading* (failure spreading service to service) into full streaming outages; its successor **Resilience4j** is now standard on the **JVM** (Java Virtual Machine, the runtime for Java and similar languages).
- **Envoy and Istio** (a *service mesh*: proxies beside every service) do it as "outlier detection," ejecting persistently failing servers from the pool with no application code.
- **Polly** (.NET, Microsoft's platform) and **PyBreaker** (Python) implement the same three states; **AWS SDKs** include adaptive versions for *throttling* (a service capping accepted calls).
- **For AI engineers:** model providers routinely rate-limit and slow under load; a breaker per provider lets an LLM gateway switch models in milliseconds instead of hanging on a failing endpoint.

**In the interview.** Rarely by name; instead "what happens to your service if this dependency goes down?", "how do you prevent cascading failures?", "what if this search service hangs?" The signal sought: **a slow dependency is more dangerous than a dead one.**

> **30-second answer:** A circuit breaker wraps the call and tracks failures over a rolling window; past a threshold it opens and rejects calls instantly instead of letting them pile up on timeouts, protecting my thread pool and relieving the struggling service. After a cooldown it sends a few test calls and closes once they succeed. Netflix built Hystrix for exactly this after slow dependencies cascaded into full outages.

- *Follow-up: "How do you pick the threshold and cooldown?"* — Measure, as with timeouts: a window with enough traffic to mean something (20+ calls), a threshold well above the normal error rate, a cooldown near the dependency's typical recovery time; then tune from production data and alert on every open.
- *Follow-up: "The breaker is open — what does the user see?"* — The breaker is half the pattern; the other half is a fallback: cached data, a default, or hiding the optional widget (Graceful Degradation).
- *Mistake that fails candidates:* proposing retries for a failing dependency with nothing that ever *stops* retrying.

**Self-check:**
- Q1: Name the three states and what causes each transition. → Closed (failures counted over a rolling window) → Open when the failure rate passes the threshold; Open (all calls rejected instantly) → Half-open when the cooldown ends; Half-open (a few test calls) → Closed on success, Open on failure.
- Q2: Your team adds a breaker to primary-database calls with library defaults. What new failure modes did you introduce? → False trips (an ordinary 10 s failover rejecting good queries for a whole cooldown, turning a blip into minutes of partial outage); flapping on quiet endpoints; and with no fallback, instant errors on primary data — likely a case where waiting beats failing fast. It needs tuned thresholds, a fallback (cached reads or read-only mode) and an alert.
- Q3: Worst-case latency spikes to exactly 10 s (your timeout) whenever optional search degrades; soon after the whole API errors. Explain and fix. → Every request holds a thread for the full 10 s; requests/s × 10 s exceeds the pool, so healthy endpoints starve — a cascading failure by resource exhaustion. Fix with the whole module: a timeout just above search's healthy p99, a breaker so sustained failure costs 1 ms, a bulkhead capping search's threads, and rendering the page without search results.

**Related patterns:** [Timeout](#timeout) (the breaker's evidence); [Retry](#retry-with-exponential-backoff) (handles transient failures inside; the breaker prevents storms); [Graceful Degradation](#graceful-degradation) (runs while open); [Bulkhead](#bulkhead) (limits occupancy while evidence accumulates); Health Check Endpoint (a URL a **load balancer** — the server spreading requests across service copies — polls for health; the same idea at machine level, removing failing servers).

**TL;DR**
- **Problem:** a slow or failing dependency eats your threads through timeout-length waits and drags your healthy service into its outage; retries worsen it, and nothing says "stop calling."
- **Mechanism:** a three-state machine — count failures (closed), reject instantly past a threshold (open), probe with a few calls after a cooldown (half-open).
- **Costs:** threshold tuning, false trips on blips, per-instance state, and you still owe users a fallback.
- **Skip it when:** the call is local, traffic is too thin for a signal, or failing fast costs more than waiting.

## Bulkhead
*Original lesson: [Bulkhead on DesignGurus ↗](https://www.designgurus.io/course-play/system-design-patterns/doc/6a50027e65a61eeda0fdf45d)*


**What it is.** Divide a shared resource (threads, connections, machines) into walled pools — one per feature, dependency or **tenant** (a customer organization sharing the system) — so one full pool can't starve the rest. *Analogy:* a hull's watertight compartments (bulkheads) mean a hole floods one compartment, not the ship. Total capacity is unchanged; no workload can grab all of it.

**The problem.** One API **deployment** (a running installation, often many identical copies) serves checkout, search and order history, sharing **200 threads** and **100 database connections** (a *pool* is a fixed set of workers or connections requests borrow briefly). Marketing emails **2 million** people a link to order history, whose traffic leaps from **50 to 1,500 requests/second**, and it runs the service's most expensive query, ~**500 ms**.

Little's law: 1,500 × 0.5 = **750 threads** wanted, 200 available; order history takes all of them within ~a fifth of a second, and checkout, with zero threads, stops selling.

Yet *nothing fails*: every query succeeds, the database is up, error rate **0.0%**, dashboards green, circuit breaker closed (no failures to count). The problem is **occupancy** — how much shared capacity one workload holds — not outcomes. Same shape: one big tenant starving 500 others, a batch job holding every connection, one expensive endpoint eating the service.

**Why the obvious fixes fail**
- **"Add more threads."** 1,000 threads moves the limit without controlling who gets them; the next surge wants 1,200, and meanwhile 1,000 threads fight over 100 DB connections. Growing everything for one temporary workload is costly and still not isolation.
- **"The circuit breaker will handle it."** Calls complete before timeout, so the breaker sees 0% failures. You need something that watches occupancy.
- **"Rate-limit the users."** Useful at the edge for individual clients, but thousands of legitimate users each send one request and none exceeds a per-user limit. You need a cap on the *feature's share* of the pool.

**How it works.** The figure contrasts one shared 200-thread pool (order history needs 1,500 × 500 ms = 750 threads, takes all 200, others get 0, error rate 0.0%) with walled pools — checkout 80, search 60, order history 40, reserve 20 — each sized by rate × duration and triggering on *fullness*, not errors.

![Bulkhead](images/bulkhead-1.svg)
*Figure — Bulkhead (course diagram)*

Three levels of wall, each costlier:
1. **Counters (semaphores):** a *semaphore* allows at most N concurrent things ("at most 40 order-history requests at once"); the 41st is rejected immediately. Cheapest; Resilience4j's default.
2. **Separate pools:** each feature or dependency gets its own thread or connection pool; more overhead, but even a fully stuck compartment can't touch others' threads. Hystrix's signature.
3. **Physical separation:** different processes, **containers** (lightweight isolated packages running an app with its own CPU and memory slice) or machines per workload or customer tier. Heaviest, and the only level also isolating CPU, memory and crashes.

At every level a full compartment **rejects new requests quickly** (or queues briefly) and never borrows from another. A rejection means the wall works; what the rejected user gets is Graceful Degradation's job.

**Sizing:** run the incident's arithmetic backwards — *needed = rate × duration* plus *headroom* (burst room). Checkout: 300 req/s × 0.25 s = 75 → **80**.

**Walkthrough with numbers** (checkout 80, search 60, order history 40, reserve 20)
- **The flood:** order history's 40 threads at 500 ms serve 40 / 0.5 = **80 req/s**; the other **1,420 req/s** get "busy, try again shortly" in ~1 ms.
- **Checkout meanwhile** needs 75 of its own 80; latency and **conversion** (share of visitors completing a purchase) are unchanged.
- **The database is protected too:** at most 40 concurrent order-history queries instead of 200 — an upstream cap is a downstream ceiling.
- **Total damage:** one feature degrades honestly under load it wasn't sized for, versus the whole product down in a fifth of a second with green dashboards.
- The promise shifts from "survives if *total* demand fits" to "**each feature survives if its own demand fits**" — several keepable promises instead of one fragile one.

**Trade-offs**
- *You gain:* protection when *nothing is failing* (the module's only occupancy-based tool); a *blast radius* (how far one failure spreads) chosen in advance; honest per-feature capacity numbers (pool fullness shows who needs scaling); a ceiling protecting downstream systems.
- *You pay:*
  - **Idle capacity you can't lend:** overnight checkout's 80 threads may idle while order history rejects next door; every compartment needs its own spare room.
  - **Sizing goes stale:** as traffic mix drifts, an outdated split rejects in one full pool while capacity idles nearby — a self-inflicted outage invisible in totals. Re-split on a schedule.
  - **More to watch:** N pools, N fullness metrics, N rejection alerts, N configs. Alert on **per-compartment fullness**; error-rate alerts can't see this failure.
  - **One more concept** for every on-call engineer.

**When not to use it**
- **Single-purpose services**, already isolated at the service boundary.
- **Tiny pools:** 20 threads split four ways rejects on ordinary variation; below a few dozen threads, isolation may cost more than it protects.
- **Uniform, elastic workloads:** similar cost and priority, with servers added in seconds (*elastic*), may use capacity better in a shared **autoscaled** pool (growing and shrinking automatically). Bulkheads pay off when workloads differ in cost, importance or trust.
- **As a stand-in for capacity:** every compartment near full daily means you need more total capacity.
- *Common misuse:* a compartment per endpoint and client, each too small for normal variation — a hundred undersized pools give rejections, not resilience.

**Bulkhead vs. circuit breaker**

| | Circuit Breaker | Bulkhead |
|---|---|---|
| Watches | Failures (errors, timeouts) | Occupancy (who is holding how much) |
| Triggers when | The dependency is failing | Something is hogging the capacity |
| Action | Stop calling completely; check again later | Limit the share; reject the overflow |
| Sees "everything succeeds, slowly, everywhere"? | No — there's nothing to count | Yes — fullness shows up |
| Sees "dependency is hard down"? | Yes — drops calls to zero quickly | Partly — limits the stuck threads |

Breaker when the dependency is failing; bulkhead when sharing is the danger. Stack them: the bulkhead limits threads stuck in a bad dependency while the breaker gathers evidence, and once it trips the compartment drains — why Hystrix shipped both. Versus rate limiting: rate limiting controls *outside clients by identity* at the front door; bulkheads control *internal sharing by compartment*.

**Real-world examples**
- **Netflix Hystrix** made per-dependency thread pools famous — a hung dependency could trap only its own small pool; **Resilience4j** continues it, defaulting to lighter counters.
- **Kubernetes** builds bulkheads into the platform: CPU/memory limits per *pod* (a unit of deployed containers), separate node pools for batch vs. interactive work, per-team quotas.
- **Envoy** caps connections per upstream inside the mesh — bulkheads without application code.
- **Database connection pools split by service** (via PgBouncer, a PostgreSQL connection-pooling proxy, or per-deployment limits) keep one busy service from taking every connection.
- **For AI engineers:** multi-tenant GPU serving is today's biggest *noisy-neighbor* problem (one tenant's load hurting everyone on shared hardware) — one customer's batch embedding job can starve every interactive chat. Separate interactive and batch GPU pools (or limited batch quota slots), give each pricing tier a compartment so free-tier load can't push out paying customers, and cap per-tenant concurrency at the LLM gateway. Same rate × duration math, in GPU slots.

**In the interview.** "One endpoint suddenly gets 100× traffic — what happens to the others?", "noisy neighbor in a multi-tenant system?", "what does one slow feature do to a shared thread pool?"

> **30-second answer:** I split thread, connection or node pools by feature or tenant, each sized by rate × duration plus headroom, and a full compartment rejects only its own overflow. That protects critical work even when the competing requests all *succeed* — invisible to a circuit breaker, which counts failures. Hystrix's per-dependency thread pools are the best-known implementation.

- *Follow-up: "How do you size the compartments?"* — Rate × duration per compartment plus burst headroom; then treat "one pool keeps rejecting while total capacity idles" as the signal to re-split. Admitting the split drifts shows you've operated one.
- *Follow-up: "Bulkhead or circuit breaker here?"* — Diagnose aloud: errors or timeouts from the dependency → breaker; legitimate successful load taking over shared capacity → bulkhead. Production stacks both.
- *Mistake that fails candidates:* prescribing a breaker for hogging, or treating green error dashboards as proof critical features have capacity.

**Self-check:**
- Q1: What does a bulkhead partition, what triggers it, and how does that differ from a breaker? → It walls shared capacity (threads, connections, memory, machines, GPU slots) and reacts to *fullness*; a breaker reacts to *failure* evidence. So bulkheads catch success-shaped hogging that breakers can't see.
- Q2: Overnight, checkout's pool is 90% idle while order history rejects; a teammate wants to merge pools. What's the real choice? → Efficiency vs. worst-case protection: merging recovers idle capacity but restores the original failure mode. Right only if workloads are equal in importance and shape, capacity autoscales fast, or the pool is too small to split. With revenue-critical checkout involved, idle threads are the price of protection.
- Q3: A multi-tenant **SaaS** (software-as-a-service: one hosted product shared by many paying customers) with 500 customers; one ships a buggy integration opening 2,000 concurrent requests; everyone slows down; error rate near zero. Diagnose and fix at two layers. → Success-shaped hogging starving 499 tenants. Front: per-tenant rate limits and concurrency caps answering excess with 429. Inside: bulkhead shared pools by tenant tier (or per tenant for the largest), sized by rate × duration. Alert on per-compartment fullness, not just errors.

**Related patterns:** [Circuit Breaker](#circuit-breaker) (stack them); [Timeout](#timeout) (bounds how long each slot is held — without it compartments fill and stay full); Rate Limiting (limits clients by identity at the boundary); [Graceful Degradation](#graceful-degradation) (what a rejected request becomes).

**TL;DR**
- **Problem:** shared pools let any feature, tenant or dependency legally take everything, starving the rest at zero error rate.
- **Mechanism:** walled pools per consumer (counters, separate pools, physical separation), sized by rate × duration; a full compartment rejects its own overflow quickly.
- **Costs:** idle capacity that can't be lent, stale sizing, more pools to monitor.
- **Skip it when:** the service does one thing, the pool is too small to split, or workloads are uniform and elastically autoscaled.

## Dead Letter Queue
*Original lesson: [Dead Letter Queue on DesignGurus ↗](https://www.designgurus.io/course-play/system-design-patterns/doc/6a506aaa914a537a0045982f)*


**What it is.** A **dead letter queue (DLQ)** is a separate queue for messages that keep failing: after a set number of failed attempts, the queue system pulls the message from the main line, with notes on how it failed. The main line keeps flowing, and the message is kept safe for inspection, fixing and replay. *Analogy:* the postal "dead letter office" holds undeliverable mail instead of discarding it or jamming the sorting line.

**Terms first.** A *message queue* stores a *producer*'s messages until a *consumer* processes them; on finishing, the consumer sends an *acknowledgment (ack)* and the queue deletes the message. The *broker* is the queue system (SQS, RabbitMQ, Kafka). Under at-least-once delivery, unacked messages are delivered again. (Full lesson: [Message Queue](../02-moving-data/README.md#message-queue).)

**The problem.** An order-fulfillment consumer reserves inventory and notifies the warehouse at **200 messages/second**, flawlessly for months. At **2:47 AM** a partner publishes an order with a **null** shipping address; the consumer throws an **exception** (the error signal raised when code can't continue), doesn't ack, and the broker redelivers it forever. This isn't transient: malformed input fails *deterministically* — attempt 4,000 fails like attempt 1 — a **poison message**. With ordered processing, everything behind it waits: **720,000 stuck orders per hour** from one null field.

A teammate's emergency fix — catch, ack, move on — restores flow but makes things worse: the next malformed message is simply deleted with no replayable record, and the following weekend **40 paid orders vanish** — never shipped, no payload to investigate. A poison message forces a choice between blocking ordered processing forever and silently deleting business data. The DLQ is the third option.

**Why the obvious fixes fail**
- **"Retry harder."** Backoff helps failures that might clear; an unparseable message fails every time, and each retry steals capacity from healthy messages.
- **"Catch, log, and skip."** Better than silent deletion, but logs may be sampled, rotated, truncated or barred from storing full payloads, and can't be replayed through the pipeline after a fix. The order is still lost; you just recorded losing it.
- **"Validate everything at the producer."** Worth doing, but can't stop consumer bugs, valid-but-unusual edge cases or downstream rejections. Some messages will always fail in processing; the design must say where they go.

**How it works.** The producer writes to the main queue; the consumer processes. Success → ack. Failure with attempts < 5 → back to the main queue for redelivery. 5th failure → DLQ. DLQ depth > 0 → alert; an engineer inspects payload and error; after a fix, the message is redriven to the main queue.

![Dead Letter Queue](images/dead-letter-queue-1.png)
*Figure — Dead Letter Queue (course diagram)*

Four pieces (only the first is really code):
1. **A broker-enforced attempt limit.** The broker counts deliveries and at the limit (SQS's `maxReceiveCount`, usually **3–5**) moves the message itself. It must be the broker: the failure may be the consumer crashing on this very message, and code that can't survive a payload can't be trusted to route it.
2. **Failure notes travel with it:** source queue, exception, attempt count, first and last failure times. Without context a DLQ entry is a puzzle; with it, a bug report.
3. **An alarm on the DLQ.** An unwatched DLQ only *postpones* deletion: retention is limited (**14 days on SQS**), after which messages are auto-deleted. Alert the moment an important DLQ is non-empty.
4. **A way back (redrive).** After the fix ships, messages are *redriven* to the main queue for real processing — impossible with logs. A redrive is a redelivery, so consumers must be idempotent.

The DLQ separates two jobs the emergency fix conflated: keeping healthy messages moving in milliseconds, and giving a person hours to understand strange input.

**Walkthrough with numbers** (5 attempts with backoff, then dead-letter)
- **2:47:00** — the null-address event fails attempt 1; attempts 2–5 fail over ~30 seconds.
- **2:47:30** — the broker moves it to the DLQ with exception and history; the consumer moves on.
- **Backlog math:** 30 s × 200 msg/s ≈ **6,000 messages** queue behind it and clear in well under a minute — versus 720,000 per hour without end.
- **2:47:31** — the DLQ alarm fires; on-call reads the **payload** (message contents) and **stack trace** (function calls active at the error), finds the partner's null field; the fix (edge validation plus a consumer patch) ships in the morning.
- **9:15 AM** — one-click redrive; the order ships hours late instead of never.
- **Total:** one order delayed, zero lost, a 30-second slowdown instead of an all-night outage, for one extra queue and one alarm.

**Trade-offs**
- *You gain:* poison no longer blocks processing and stays available; a structured, searchable record (payload, error, history) logs can't give; replay — fix, redrive, and the work really happens; and DLQ depth, among a pipeline's most honest health signals.
- *You pay:*
  - **One more queue to own:** unwatched, it turns visible loss into delayed silent deletion.
  - **Broken ordering for the parked message:** message 47 runs after 48 through 6,000 — fine for independent events (orders, emails), not where order carries meaning.
  - **A judgment the counter can't make:** an attempt count can't distinguish bad input from a healthy message caught in a ten-minute downstream outage; a bad threshold can dump thousands of valid messages in the DLQ (self-check Q2).
  - **Redrive tooling and a data-retention question:** payloads with personal data (PII) now live in a second place.

**When not to use it**
- **Strictly ordered streams** — per-customer sequences where event 48 must never precede 47 (account ledgers, state machines). Skipping corrupts data; instead **pause that stream** (stop its **partition**, the ordered queue slice holding that key's messages), alert loudly, and **fix forward** (fix the code and resume without skipping). Other streams continue. A DLQ here is a data-corruption feature.
- **Throwaway data** — high-volume telemetry where one lost reading changes nothing: drop and count it.
- **Before retries are tuned:** dead-lettering on the first failure turns every blip into a ticket; the DLQ is for *after* retries run out.
- **As a business-logic route:** "payment declined" is an expected outcome with its own flow. Keep the DLQ for the unexpected so its alarm always means something.

**DLQ vs. retry-queue tiers.** In-place redelivery with backoff suits short gaps, but holding a consumer slot through an hour-long downstream outage is wasteful. Some systems (especially around Kafka) use **retry tiers**: a failed message goes to `retry-5m` (whose consumer waits five minutes, then retries), then `retry-1h`, and only then the DLQ.

| Situation | Reach for |
|---|---|
| Failures clear within seconds (transient, brief contention) | In-place redelivery with backoff, then DLQ |
| Failures need minutes to hours (downstream outages, rate limits) | Retry tiers, with the DLQ as the last stop |
| Deterministic failure (malformed payload, code bug) | Straight to the DLQ — waiting won't help |
| Ordering per key must be kept | Neither one can skip safely — pause the stream |

These are steps in one sequence — quick redelivery → spaced tiers → DLQ. Most systems need the first and last; add the middle only if outage-length failures are common.

**Real-world examples**
- **AWS SQS** (Amazon's managed queue) mainstreamed it: per-queue redrive policy, broker-driven moves, a redrive button in the console (AWS's web control panel).
- **RabbitMQ** generalizes it as *dead letter exchanges*; with per-queue delays, that's also how it builds retry tiers.
- **Kafka** has no built-in DLQ; its ecosystem (Kafka Connect, Spring Kafka) provides dead-letter topics, and the strict-ordering warning applies fully.
- **Google Cloud Pub/Sub** includes dead letter topics with built-in delivery-attempt tracking.
- **For AI engineers:** LLM pipelines constantly produce poison — invalid **JSON** (common structured text format), embedding-breaking documents, moderation rejects — each failing deterministically. In RAG ingestion (loading documents for retrieval-augmented generation), one corrupt PDF in a 100,000-document crawl goes to a DLQ for human review while 99,999 get indexed; the DLQ doubles as the review queue.

**In the interview.** Draw a queue and expect "what happens when a message can't be processed?" or "a malformed message is crashing your consumer — walk me through the next ten minutes." Saying *poison message* instantly marks you as someone who has run a queue.

> **30-second answer:** Under at-least-once delivery a malformed message is redelivered forever and blocks everything behind it, while dropping it is silent data loss. So after a few failed attempts the broker moves it, with error details, to a dead letter queue: the main line flows, nothing is lost, an alarm fires as soon as the DLQ is non-empty, and after the fix we redrive. SQS's redrive policy is the standard example. Rule to remember: a DLQ nobody watches is data loss on a timer.

- *Follow-up: "What's your process for what lands in the DLQ?"* — Alarm at depth ≥ 1 for critical flows; inspect payload and exception; fix; redrive. Any steady inflow is a defect, never normal. Mention retention — "parked" isn't "safe forever."
- *Follow-up: "Your events are ordered per customer — does dead-lettering still work?"* — No; skipping breaks the sequence. Pause the affected stream and fix forward; dead-letter only independent events.
- *Mistake that fails candidates:* "retry until it succeeds" (deterministic failures never do), or an elaborate DLQ with no one watching it.

**Self-check:**
- Q1: What moves a message to the DLQ, why must the broker do it, and what should travel with it? → The delivery-attempt count reaching its limit (say 5). The consumer may be what's crashing, so it can't route. Carry source queue, exception, attempt count, first/last failure times.
- Q2: A downstream dependency is down for ten minutes; the queue is set to 5 attempts over 30 s, then DLQ. What happens and how do you fix it? → Every message arriving in the outage exhausts its attempts in 30 s and is dead-lettered: thousands of healthy messages flood the DLQ, hiding real poison and forcing a bulk redrive. Fix: stretch the retry window beyond realistic outages (retry tiers) and put a circuit breaker on the consumer's downstream call so consumption pauses during hard outages.
- Q3: A payments DLQ set up eight months ago: 12,000 messages passed through, 300 remain, no incident was ever raised. What happened? → Nobody watched it: 14-day retention silently deleted ~11,700 payment events, found by an auditor instead of an alarm. Missing: an alarm on DLQ depth/inflow, a named owner with an inspect-fix-redrive *runbook* (written steps), and retention long enough for the response process.

**Related patterns:** [Message Queue](../02-moving-data/README.md#message-queue) (at-least-once delivery makes endless redelivery, hence DLQs, necessary); [Retry](#retry-with-exponential-backoff) (handles passing failures; the attempt limit is where they meet); [Idempotency](#idempotency) (redrive is redelivery); [Circuit Breaker](#circuit-breaker) (on the consumer's downstream calls, keeps outages from looking like thousands of poison messages).

**TL;DR**
- **Problem:** under at-least-once delivery, a poison message forces a choice: redeliver forever and block everything, or drop it and silently lose data.
- **Mechanism:** after N failed attempts the broker moves it, with failure details, to a separate queue; an alarm fires when that queue is non-empty; messages are redriven after the fix.
- **Costs:** another queue to own, broken ordering for parked messages, an attempt counter that can't tell poison from an outage, and the trap that an unwatched DLQ is delayed loss.
- **Skip it when:** ordering is strict (pause instead), data is throwaway (drop and count), or the "failure" is a normal business outcome.

## Graceful Degradation
*Original lesson: [Graceful Degradation on DesignGurus ↗](https://www.designgurus.io/course-play/system-design-patterns/doc/6a506c110e21df19e8bf2756)*


**What it is.** When part of a system fails, return a *lesser* answer instead of an error: optional features disappear or show older data, while the few critical operations that must never be wrong refuse honestly. You get there by ranking features by criticality and giving each a pre-planned sequence of fallbacks, so failures cost *quality* rather than *availability*. *Analogy:* when the grill breaks, a good kitchen pulls grilled dishes and keeps serving the rest — but never serves undercooked chicken to fill an order.

**The problem.** A homepage makes **12 service calls** (catalog, cart badge, recommendations, reviews, pricing, promotions, recently viewed…), each guarded by a timeout, controlled retries and a circuit breaker. Recommendations fails; its breaker opens within a second and rejects instantly, as designed. The homepage still errors: the caller has no handling for an "open breaker" result, so the exception kills the whole page **render** (assembling the final page for the browser). The breaker worked; the application had no alternative answer.

The math: each dependency is **99.9%** available, but the page renders only if all twelve succeed: 0.999¹² ≈ **98.8%**, about **104 hours of homepage downtime a year**, mostly from optional widgets ([availability in chains](../01-thinking-in-patterns/README.md#back-of-envelope-math)). Rule: *every dependency without a fallback becomes a required one*, and page availability is all of theirs multiplied. Earlier patterns decide when to stop calling; none decides what the user gets afterward.

**Why the obvious fixes fail**
- **"Make every dependency five-nines"** (99.999% uptime). Twelve services at 99.99% still multiply down, and one bad deploy wrecks the estimate; better targets don't change "all twelve must succeed."
- **"Wrap every call in try/catch and return null"** (*try/catch* intercepts an exception; *null* means "no value"). Treats optional reviews and the essential cart identically, tells the user nothing, and can hide critical failures — say, checkout silently skipping inventory reservation and leaving inconsistent data. It dodges the pattern's central decision: which features are required?
- **"Cache everything."** A cache can be one fallback step, but it serves reads, not writes, needs explicit *staleness limits* (how old an answer may be), and a miss during an outage still needs another response.
- What's needed: engineering **and product** rank each feature and choose its fallback *before* the incident.

**How it works — two parts, and the first isn't code**
1. **Criticality tiers, decided in a meeting with product.** *Critical* — the page is meaningless without it (catalog, checkout, login). *Important* — clearly worse without it (pricing detail, search filters). *Optional* — nobody would file a ticket (recommendations, recently viewed). That meeting *is* the pattern; code enforces it.
2. **A fallback sequence per feature** — an ordered ladder of progressively lesser answers (figure): (1) fresh, full quality → (2) a cached copy seconds to minutes old → (3) a static default ("popular this week," the list price) → (4) hide the feature → (5) critical features only: refuse clearly and loudly ("nothing was charged, nothing was faked"). Optional features may fall to "hidden," important ones stop at a default, critical ones (checkout, login, payment capture) skip straight to refusing. How far each may drop is agreed with product *beforehand*, not decided by an exception handler mid-incident.

![Graceful Degradation](images/graceful-degradation-1.svg)
*Figure — Graceful Degradation (course diagram)*

Examples: *recommendations* → personalized list, else precomputed "popular this week," else hide the row. *Pricing* → live price, else a cached price within a business-approved staleness limit, else fail — a wrong price isn't cosmetic.

**Whole-system versions of the same idea**
- **Read-only mode:** browsing works; writes are on hold.
- **Queue-writes-for-later:** accept the action, queue it durably, confirm by email once done.
- **Priority shedding:** under overload, drop optional traffic first so critical traffic survives.

**Two operating rules that separate real implementations from decorative ones**
- **Alarm inside, degrade smoothly outside.** Degraded mode must page on-call even when users see an acceptable page, or the root cause never gets fixed and the "temporary" fallback becomes permanent.
- **Fallbacks rot unless exercised.** An else-branch that never runs is broken when needed. Netflix's **Chaos Monkey** (deliberately breaking things in production) makes fallbacks run for real; **game days** (scheduled practice outages) are the gentler version.

**Walkthrough with numbers** (catalog critical, 99.95% available alone; the other 11 fragments each get a ladder ending in "hide")
- **The recommendations outage again:** the breaker opens and the fallback serves the hourly "popular this week" list in ~**2 ms**; the page looks complete; a `degraded_mode` metric alerts on-call; conversion drops a measured **~1%** instead of to zero.
- **Availability rebuilt:** the page hard-fails only if catalog fails: 99.95% ≈ **4.4 hours/year**, down from **104**. No dependency got more reliable; 11 just lost the power to take the page down — twelve multiplied numbers became one.
- **The write side:** during a 20-minute payments-database incident, checkout switches to queue-writes mode: orders accepted, given idempotency keys, queued durably (surviving a crash), confirmed by email minutes later. Revenue flows through what used to be 20 minutes of zero sales.

**Trade-offs**
- *You gain:* system availability independent of optional dependencies; gentler incidents (gradual quality loss buys responders time); an honest written map of what matters, sharpening later capacity and spending decisions.
- *You pay:*
  - **Fallback code is real code:** written, reviewed, and above all exercised — or a second bug behind the first.
  - **Staleness needs a named owner:** cached prices and yesterday's inventory each need a business owner approving how wrong they may be.
  - **The masking hazard:** a fallback can hide failure so well nobody investigates — "popular this week" for a whole quarter — unless degraded mode alerts internally.
  - **Many states to test:** N features × M fallback steps, requiring deliberate *fault injection* (causing controlled failures and checking each response).

**When not to use it — things that must fail closed** (an honest, immediate "no," never a lesser answer)
- **Correctness-critical writes:** payment capture, inventory commits, dosage calculations, trades. A wrong or half-done answer is worse than refusal. Queuing *without executing* can be fine; pretending it ran never is.
- **Authentication and authorization:** an auth outage never justifies "let them through" or "trust the old answer" — degraded security is called a breach. Auth only degrades toward refusing.
- **Compliance flows:** audit trails, consent checks, regulatory reports.
- **When stale costs more than down:** cached prices in a fast market can lose more than an honest maintenance page.
- *Common misuse:* degrading with no metric, alert, owner or exit plan, so the fallback quietly becomes permanent product behavior.

**Degradation vs. failover.** *Failover* switches to a standby copy of the failing dependency; both answer "the dependency is failing — now what?", in opposite ways:

| | Failover | Graceful Degradation |
|---|---|---|
| The move | Swap out the dependency (replica, standby, another region) | Swap out the answer (cached, default, hidden) |
| Quality afterward | Full | Lower, on purpose |
| Cost paid in advance | Extra capacity sitting idle | Fallback code and product decisions |
| Fails when | The whole class is down (a bug in every replica, the provider itself) | No sensible lesser answer exists |

They're steps in one sequence: fail over first (users never notice), degrade when failover runs out (users barely notice). A primary-replica database promoting a follower is failover ([Primary-Replica](../03-storing-data/README.md#primary-replica)); this lesson plans for when promotion isn't enough.

**Real-world examples**
- **Netflix:** failed personalization shows popular non-personalized titles instead of an error; Chaos Monkey triggers those fallbacks in production to verify them.
- **Amazon** degrades recommendations, reviews, even search, rather than let anything touch checkout; "add to cart must always work" has shaped its architecture for two decades.
- **Cloudflare Always Online** serves cached pages when **origin servers** (the site's own servers behind the CDN) are down — degradation as a product.
- **Facebook** has gone read-only during several major incidents.
- **For AI engineers:** an LLM gateway can fall back from the main model to a smaller or older one, then to a **semantic cache** (matching cached answers by meaning, not exact text), before "try again shortly." A RAG system with retrieval down can answer from the base model with a visible "sources unavailable" label — the label matters, since silently dropping sources degrades *correctness*, possibly a fail-closed matter. Under GPU pressure, shed quality first (smaller **token** limits — tokens being the word-pieces models read and write — and trimmed **context**, the text sent with each request) before dropping requests.

**In the interview.** Directly: "Service X is down — what does the user see?", usually after you mention a breaker or bulkhead. Most candidates stop at "fail fast" without asking *into what?* The seniority signal: **"Which parts of your design are allowed to fail?"** With tiers you have an answer; without them you discover live that you made everything critical.

> **30-second answer:** With product, I rank features by criticality and give each dependency a fallback sequence — fresh, else cached, else a static default, else hide the feature — so only truly critical things fail, and loudly. The system trades quality for availability: recommendations fall back to popular items while checkout stays untouchable, as Netflix does. Degraded mode alarms internally while looking smooth outside, and game days exercise the fallbacks, because an else-branch that never runs is already broken. Auth and payment capture fail closed: a lesser answer there is a breach or a double charge.

- *Follow-up: "Who decides what's critical, and how?"* — Product and engineering together, before the incident: revenue-path and correctness-bound features are critical; enhancements get ladders. The meeting is the pattern, not the try/catch.
- *Follow-up: "How do you know your fallbacks still work?"* — Scheduled fault injection — game days, chaos tooling, or continuously routing a small share of real traffic down fallback paths — plus a metric and alert per degraded state, so degradation is tracked and has an exit.
- *Mistake that fails candidates:* a design where every dependency is secretly critical, or the reverse — degrading something that must fail closed ("if auth is slow we can skip the check" has ended interviews).

**Self-check:**
- Q1: Name the fallback steps in order; who assigns criticality, and why not engineering alone? → Fresh → cached/stale → static default → hide → (critical only) honest, loud failure. Product and engineering together, because criticality is a business judgment about revenue, trust and legal weight; engineering alone tends to rank by build difficulty or make everything critical.
- Q2: Degradation works so well that a recommendations outage causes zero complaints and no visible revenue dip. Why is that dangerous and what keeps it safe? → It hides the failure, so the root cause can linger for a quarter. Keep it safe with a degraded-mode metric that alerts internally, plus an owner and exit plan for every degraded state.
- Q3: During an auth brownout a teammate proposes caching each user's last auth answer for an hour and letting known session cookies through if auth stays down — "like recommendations." Evaluate. → The halves fall on opposite sides of the fail-closed line. Briefly caching auth answers (including the "no"s) under a security-owned limit is defensible; letting requests through while auth is down is failing open on a security boundary — a breach. The real mistake is treating auth like recommendations: for some features the worst case is "missing," for others "wrong." Auth only degrades toward refusing.

**Related patterns:** [Circuit Breaker](#circuit-breaker) (decides when to stop calling; the fallback runs while it's open); [Bulkhead](#bulkhead) (rejected overflow lands on a fallback step); [Cache-Aside](../04-caching/README.md#cache-aside) (the stale-copy step, promoted from performance trick to availability mechanism); [Message Queue](../02-moving-data/README.md#message-queue) (drives queue-writes-for-later).

**TL;DR**
- **Problem:** without fallbacks every optional widget quietly becomes load-bearing, and availability becomes a dozen teams' numbers multiplied.
- **Mechanism:** rank features with product, pre-plan a fallback sequence per feature (fresh, cached, default, hidden, fail), alarm internally while degrading externally.
- **Costs:** fallbacks that rot unless exercised, staleness someone must own, a masking hazard, many states to test.
- **Skip it when:** the operation must fail closed — payment capture, auth, compliance, or anywhere a lesser answer is worse than none.

## Capstone: Hardening a Payment Flow
*Original lesson: [Capstone: Hardening a Payment Flow on DesignGurus ↗](https://www.designgurus.io/course-play/system-design-patterns/doc/6a501948a12dfb1da789399d)*


**What this capstone is.** Seven patterns, one payment system, seven incidents in the order teams usually hit them, each fix causing or uncovering the next — reliability patterns interact as a system, not a checklist. At each stage, decide the pattern and its key numbers before reading on; that's what interviews test. Each stage summarizes its lesson; follow the link for the full mechanics.

**The brief.** You've just inherited checkout at a mid-size commerce company, a week before Black Friday. The user clicks Pay; the Checkout API (**200-thread pool**) synchronously calls, in order and while the user waits, the Payment Provider (external, healthy **p99 = 400 ms**), the Orders DB (**100-connection pool**), the Email Service and the Warehouse API. Traffic: **100,000 orders/day**, peaking at **300 checkout requests/second** in promotions. Promise: answer within **2 seconds, 99.9% of the time**. Reliability tooling: none — every call uses its HTTP client's defaults.

![Payment flow: the brief](images/capstone-hardening-a-payment-flow-1.png)
*Figure — Payment flow: the brief (course diagram)*

### Stage 1 — The hang → Timeout
- **Incident.** Monday 11:40 AM: a bad deploy on one provider server returns *nothing*, connections just open. Within **90 seconds** all 200 checkout threads are tied up, health checks fail, and the load balancer pulls your **instances** (running copies of your service) out of rotation. Provider error rate: **0.4%**. Your outage: **total**.
- **Pause and decide:** how did a 0.4% problem become a 100% outage? Which pattern, what number?
- **Why it happened.** The client default waited forever; at 300 req/s, hung calls hold threads indefinitely, draining a 200-thread pool in under a second of hung traffic.
- **Decision ([Timeout](#timeout)).** Measured deadlines: provider request timeout **1,200 ms** (3× its 400 ms healthy p99) with a **200 ms** connection timeout; database **250 ms**; email and warehouse **500 ms** each — cutting real hangs fast while leaving room for slow-but-normal calls.
- **Budget check — the first design problem.** 1,200 + 250 + 500 + 500 = **2,450 ms**, over the 2 s promise. Honest conclusion: email and warehouse don't belong in the synchronous path (Stage 6); for now cut them to **300 ms** each so everything fits.
- **What it creates.** Hangs become visible failures: ~1 in 200 payment calls now fails transiently where 100 ms later it would have worked — an invisible problem swapped for a visible one, a good trade.

### Stage 2 — Transient failures → Retry with backoff
- **Incident.** ~**500 checkouts/day** fail from dropped packets, slow provider servers or brief resets. Tickets: "I clicked Pay, got an error, tried again and it worked."
- **Pause and decide:** failures are transient and random — which pattern, and which three configuration properties?
- **Decision ([Retry](#retry-with-exponential-backoff)).** Retry payment calls **up to 3 times** with waits doubling from ~**100 ms** plus **jitter**; only changeable errors (timeouts, connection failures, 5xx — server errors in the 500s — and 429 obeying `Retry-After`). A declined card is an *answer*, never retried. Retries live in **exactly one layer** — the Checkout API — so the app and gateway can't multiply them (avoiding 3 × 3 × 3).
- **What it creates.** A timeout is uncertain. Three days after launch, finance forwards a chargeback: a customer charged **$499 twice**.

### Stage 3 — The double charge → Idempotency
- **Incident.** About a third of payment timeouts occur *after* the provider took the money; each retry of those is a double charge — dozens a day, each costing a refund, support time and a **$15** chargeback fee. Retries work exactly as designed; that *is* the problem.
- **Pause and decide:** the retry can't know whether attempt one landed. Who can, and what must travel with the request?
- **Decision ([Idempotency](#idempotency)).** The Checkout API creates **one idempotency key when the user clicks Pay** — one per logical payment, stored with the pending order, so even a crashed-and-reloaded client retries with the same key — sent on every attempt. The provider atomically claims it, charges once, saves and replays the result, so a retry after a lost response gets the original confirmation. Your own writes too: the **order insert is keyed**, so redelivered work can't create duplicate rows. Client-side because only the client knows two requests are one intent.
- **What it creates.** Nothing bad, for once — idempotency is the rare fix adding no new failure mode. But one case remains that retries can't handle.

### Stage 4 — The sustained outage → Circuit Breaker
- **Incident.** Black Friday 2:15 PM: the provider enters a sustained *brownout* (up, but too slow to be useful). Every call times out at 1,200 ms and retries faithfully triple traffic against it; each checkout thread spends ~**4 seconds** failing through a full retry sequence. Your API isn't down, isn't working, and has become part of the provider's problem.
- **Pause and decide:** retries assume transience; this is sustained. Which pattern, its three states, and what exactly should it count?
- **Decision ([Circuit Breaker](#circuit-breaker)).** A breaker around the provider call: rolling window of **20 calls**, **50%** threshold, **30 s** cooldown, **5 half-open test calls**, counting **only final outcomes after retries** (breaker outside, retries inside) so neither skews the other's signal.
- **Result.** ~**90 seconds** in, the breaker opens: provider calls are rejected in **1 ms instead of 4 s**, payment-thread usage nears zero, and the provider gets no new traffic. About **10 minutes** later successful test calls close it automatically.
- **What it creates.** Two unavoidable questions: (1) while open, users got an *instant* error — still an error (→ Stage 7); (2) the **postmortem** (written incident review) notes everything slowed during the brownout, even provider-free order-status lookups, because all features share thread and connection pools (→ Stage 5).

### Stage 5 — The success-shaped flood → Bulkhead
- **Incident.** Cyber Monday: finance's end-of-day reconciliation batch hits the refunds endpoint in the same service at **800 req/s, ~600 ms each**, all succeeding. Error rate **0.0%**, breakers correctly closed, checkout starving: 800 × 0.6 = **480 threads** wanted from **200** shared. Every dashboard green while sales drop.
- **Pause and decide:** nothing fails, so Stages 1–4 are blind. Which pattern, sized by what arithmetic?
- **Decision ([Bulkhead](#bulkhead)).** Split pools by feature and priority: **Checkout 100** (300 req/s × 250 ms = 75 needed, plus headroom); **Refunds 40**, capping reconciliation at ~**65 req/s** so the batch takes **3 hours instead of 1** — a trade *agreed with finance*, not discovered by them; **Order-status 40**; **reserve 20**. Same split for DB connections. Reconciliation's overflow waits; checkout notices nothing. New alert: **per-compartment fullness**, since error alerts can't see success-shaped floods.
- **What it creates.** With heavy work capped, slow synchronous work can't be ignored: email (300 ms) and warehouse (300 ms) still sit inside the user's 2 s and can fail a checkout *after* the card was charged.

### Stage 6 — The poison message → Dead Letter Queue
- **Change.** Post-payment work goes async: after the charge succeeds and the order commits, the API publishes **`order.paid`**; consumers send the receipt, notify the warehouse, award loyalty points. The synchronous path shrinks to **charge + order insert** — fast, and a failing email can no longer kill a paid order.
- **Incident.** Three weeks later a partner order has a **null shipping address**; the warehouse consumer throws and the queue redelivers it forever. Every order behind it waits, while checkout keeps selling, unaware fulfillment has stopped company-wide.
- **Pause and decide:** the failure is deterministic — redelivery can't fix it, dropping it loses a paid order. Which pattern, and which *non-code* part makes it real?
- **Decision ([Dead Letter Queue](#dead-letter-queue)).** Every consumer queue gets a redrive policy: **5 attempts with backoff**, then the broker moves the message to a DLQ with exception and attempt history; the line unblocks in ~**30 seconds** instead of never. The non-code part: **a DLQ alarm routed to the owning team, with a runbook** (inspect, fix, redrive) — otherwise it's data loss on a **14-day** retention timer. Consumers are idempotent via Stage 3's keys (the receipt keyed on order ID), so redrives can't double-send or double-ship.
- **What it creates.** A changed business rule: an order can be paid but unreported to the warehouse for minutes, which product and engineering must explicitly accept — and the final stage turns such decisions into a system.

### Stage 7 — The question nobody answered → Graceful Degradation
- **Incident.** The provider is down **20 minutes** and every mechanism works perfectly — timeouts expire, retries stop, the breaker opens, bulkheads hold, queues drain — yet every Pay click returns an instant error page, losing **$40,000** of purchase intent. The review asks what users *should* have seen; nobody had decided.
- **Pause and decide:** no mechanism is missing. What is, which parts of checkout may degrade, and which must fail closed?
- **Decision ([Graceful Degradation](#graceful-degradation), made with product).** Payment capture is **fail-closed** — never fake a charge or assume it worked — but failing closed needn't be rude or total:
  - **The rest of the site stays up:** cart, browsing and order history never touch the provider; bulkheads and fallbacks keep them running.
  - **The cart is protected:** Pay becomes "Payments are briefly delayed; your cart is saved, and we'll email you when we're back," with cart state kept server-side and one-click notification sign-up.
  - **Where the business accepts the risk, capture is postponed:** trusted repeat customers under a spending threshold get **authorize-later** (*authorizing* asks the bank to approve and hold the amount now; capture comes later). The order is accepted, queued durably with its idempotency key, captured when the breaker closes, shown as "payment processing," with an email either way. Finance approved that risk budget *before* the incident — that meeting was the pattern.
  - **Alarm internally, degrade smoothly externally:** a degraded-mode metric pages on-call immediately; the state has an owner and an exit plan.
- **Result.** In the next 20-minute outage, ~**70% of would-be buyers convert within the hour**, via saved carts and postponed capture.

**The final architecture.** Every stage's decision in one picture: bulkheaded pools, a fully wrapped provider call with a degraded-mode path, a protected Orders DB, and an `order.paid` queue whose consumers each have a DLQ with a depth alarm.

![Payment flow: final architecture](images/capstone-hardening-a-payment-flow-2.png)
*Figure — Payment flow: final architecture (course diagram)*

**Debrief: the chain, named**

| Stage | Symptom | Pattern | The number or decision that mattered |
|---|---|---|---|
| 1 | Hangs eat up every thread | Timeout | 3× healthy p99, budgeted along the chain |
| 2 | Transient failures kill real checkouts | Retry + backoff | Jitter; only retryable errors; one layer |
| 3 | Retries charge twice | Idempotency | Key made on the client, before attempt one |
| 4 | A sustained outage wastes threads on hopeless calls | Circuit breaker | Counts outcomes after retries; 30 s cooldown |
| 5 | Green dashboards, starving checkout | Bulkhead | Rate × duration per compartment; fullness alerts |
| 6 | A poison message halts fulfillment | Dead letter queue | The alarm and runbook, with an owner |
| 7 | Flawless machinery, error page anyway | Graceful degradation | Fail-closed capture; tiers signed off by product |

The Pattern column, top to bottom, is your answer to "how do you make a payment system reliable?" — and each row exists because the one above shipped: timeouts created visible failures → retries created duplicates → idempotency made retries safe → sustained failure beat retries, hence the breaker → the breaker couldn't see a success-shaped flood, hence the bulkhead → the bulkhead pushed slow work onto queues → queues brought poison, hence the DLQ → and with every mechanism working, one human question remained: *what should the user see?* Don't stop at "what happens when it fails?" Ask **"what happens instead?"**

**Interview one-liner:** Timeouts make hangs visible, jittered retries soak up blips, idempotency keys make those retries safe, a breaker stops storms caused by sustained failure, bulkheads contain floods that look like success, async work gets a DLQ that someone watches, and a degradation policy agreed with product decides what users see — with payment capture failing closed.

**Extend it yourself**
- **Multi-provider:** add a second payment provider. When should the breaker trigger a provider switch, and what does idempotency mean across two providers with no shared key store?
- **The webhook side:** the provider confirms captures by webhook, at-least-once and out of order. Harden that inbound path with this module's patterns.
- **For AI engineers:** swap the provider for an LLM API inside an agent-powered checkout assistant. Which stage changes most? Hint: Stage 7's fallback sequence gains a semantic cache, and Stage 4's breaker can switch models at the LLM gateway.
