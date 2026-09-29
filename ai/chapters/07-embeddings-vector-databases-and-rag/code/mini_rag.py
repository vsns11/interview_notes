"""A complete (toy) RAG pipeline with permissions, grounding checks and evals.

ingest: chunk with breadcrumbs, keep metadata (source, date, ACL groups, status)
query : rewrite follow-ups -> route out-of-scope -> hybrid retrieval (BM25 +
        vectors, ACL pre-filter, RRF) -> rerank -> budgeted, labelled context
answer: a fake extractive generator that cites [S#] -> post-checks (citations
        resolve, each claim supported) -> answer or designed refusal
eval  : recall@k, faithfulness, correctness, refusal correctness, permission leaks
"""
import re

from chunking import structure_chunks
from retrieval_basics import BM25, WORD2CONCEPT, FakeEmbedder, VectorIndex, rrf, tokenize

EXTRA_STOP = set("many much get we our me you your which when who per too there should".split())

DOCS = [
    {"id": "hr-handbook", "updated": "2026-03-12", "acl": {"all"}, "status": "active",
     "md": "# HR Handbook\n## Leave\n### Parental leave\n"
           "Full-time employees receive 16 weeks of paid parental leave.\n"
           "Leave can start up to two weeks before the due date.\n"
           "## Benefits\n### Dental plan\n"
           "The dental plan covers employees, spouses and children.\n"
           "Orthodontics is covered at 50 percent.\n"
           "## Travel\n### Meals\nThe limit is $50 per day. Alcohol is not reimbursed.\n"},
    {"id": "travel-2024", "updated": "2024-01-05", "acl": {"all"}, "status": "superseded",
     "md": "# Travel Policy 2024\n## Meals\nThe meal limit is $40 per day.\n"},
    {"id": "it-kb", "updated": "2026-08-30", "acl": {"all"}, "status": "active",
     "md": "# IT Knowledge Base\n## Password reset\n"
           "To reset a forgotten password, open the sign-in page and choose Forgot password.\n"
           "## Account lockout\n"
           "After five failed sign-in attempts the account is locked for 15 minutes.\n"
           "## Error E4022\n"
           "Error E4022 means the payment card was declined by the issuing bank.\n"
           "## Error E4021\nError E4021 means the card has expired.\n"},
    {"id": "exec-comp", "updated": "2026-02-01", "acl": {"hr-admins"}, "status": "active",
     "md": "# Executive Compensation\n## Bonus pool\n"
           "The executive bonus pool for 2026 is 12 percent of base salary.\n"},
]
USERS = {"alice": {"all"}, "hana": {"all", "hr-admins"}}


# ------------------------------------------------------------------ ingestion
def ingest(docs):
    chunks = []
    for d in docs:
        for crumb, text in structure_chunks(d["md"], max_tokens=40):
            chunks.append({"doc": d["id"], "crumb": crumb, "text": text,
                           "updated": d["updated"], "acl": d["acl"], "status": d["status"],
                           "index_text": f"{crumb}. {text}"})   # breadcrumb is indexed too
    return chunks


CHUNKS = ingest(DOCS)
BM = BM25([c["index_text"] for c in CHUNKS])
VEC = VectorIndex([c["index_text"] for c in CHUNKS], FakeEmbedder())


# ------------------------------------------------------------------ helpers
def norm(t):
    return t[:-1] if len(t) > 3 and t.endswith("s") else t   # crude stemming


def content_tokens(text):
    return [norm(t) for t in tokenize(text) if t not in EXTRA_STOP]


def coverage(query, text):
    """Fake cross-encoder: share of query terms the passage covers, exactly or
    by concept. IDs (like e4022) only count on an exact match."""
    q, p = content_tokens(query), set(content_tokens(text))
    p_concepts = {WORD2CONCEPT.get(t) for t in p} - {None}
    if not q:
        return 0.0
    hit = 0
    for t in q:
        if t in p:
            hit += 1
        elif not re.fullmatch(r"e\d{4}", t) and WORD2CONCEPT.get(t) in p_concepts:
            hit += 1
    return hit / len(q)


# ------------------------------------------------------------------ query path
TOPICS = ["dental plan", "parental leave", "password", "meal limit", "bonus pool"]


def rewrite(query, history):
    """Resolve 'that/it' using the previous question (a small model in real life)."""
    if history and re.search(r"\b(that|it)\b", query, re.I):
        topic = next((t for t in TOPICS if t in history[-1].lower()), None)
        if topic:
            return re.sub(r"\b(that|it)\b", f"the {topic}", query, count=1, flags=re.I)
    return query


def retrieve(query, user, k=6, enforce_acl=True):
    groups = USERS[user]
    allowed = {i for i, c in enumerate(CHUNKS)          # ACL + version filter FIRST
               if (c["acl"] & groups or not enforce_acl) and c["status"] == "active"}
    fused = rrf([BM.search(query, k=k, allowed=allowed),
                 VEC.search(query, k=k, allowed=allowed)])
    return [i for i, _ in fused[:k]]


def rerank(query, ids, keep=3):
    scored = sorted(((coverage(query, CHUNKS[i]["index_text"]), i) for i in ids), reverse=True)
    return scored[:keep]


def assemble(scored, budget_words=80, min_score=0.5):
    ctx, used = [], 0
    for s, i in scored:
        c = CHUNKS[i]
        n = len(c["text"].split())
        if s < min_score or used + n > budget_words:
            continue
        ctx.append({"label": f"S{len(ctx) + 1}", "chunk": i, "score": s})
        used += n
    return ctx


def fake_generate(query, ctx, sloppy=False):
    """Extractive stand-in for the LLM: keep the best-matching sentence(s), cite them."""
    cands = []
    for item in ctx:
        for sent in re.split(r"(?<=\.)\s+", CHUNKS[item["chunk"]]["text"]):
            cands.append((coverage(query, sent), sent.strip(), item["label"]))
    best = max((c[0] for c in cands), default=0)
    claims = [(t, l) for sc, t, l in cands if sc >= 0.5 and sc >= best - 0.05][:2]
    if sloppy and claims:                                   # an unsupported addition
        claims.append(("Coverage also includes parents and grandparents.", claims[0][1]))
    return claims


def post_check(claims, ctx):
    by_label = {c["label"]: CHUNKS[c["chunk"]]["text"] for c in ctx}
    kept, dropped = [], []
    for text, label in claims:
        src = by_label.get(label)                            # 1) citation must resolve
        support = (len(set(content_tokens(text)) & set(content_tokens(src))) /
                   max(1, len(set(content_tokens(text))))) if src else 0
        (kept if support >= 0.8 else dropped).append((text, label))   # 2) claim supported
    return kept, dropped


def answer(query, user, history=(), sloppy=False, verbose=True, enforce_acl=True):
    q = rewrite(query, list(history))
    ctx, kept, dropped, text = [], [], [], None
    if not content_tokens(q) or max(coverage(q, c["index_text"]) for c in CHUNKS) == 0:
        text = "REFUSE: out of scope for this assistant (routed away before retrieval)."
    else:
        ctx = assemble(rerank(q, retrieve(q, user, enforce_acl=enforce_acl)))
        if ctx:
            kept, dropped = post_check(fake_generate(q, ctx, sloppy), ctx)
    if text:
        pass                                              # already routed away
    elif not ctx:
        text = ("REFUSE: I couldn't find this in the documents you can access. "
                "Try the HR portal or ask the help desk.")
    elif not kept:
        text = "REFUSE: the evidence did not support an answer."
    else:
        text = " ".join(f"{t} [{l}]" for t, l in kept)
    if verbose:
        print(f"  rewritten : {q}")
        for c in ctx:
            ch = CHUNKS[c["chunk"]]
            print(f"  context   : [{c['label']} | {ch['crumb']} | {ch['updated']}] "
                  f"rerank={c['score']:.2f}")
        for t, l in dropped:
            print(f"  DROPPED unsupported claim: {t!r} (cited {l})")
        print(f"  answer    : {text}")
    return {"q": q, "ctx": ctx, "text": text, "claims": kept, "dropped": dropped}


# ------------------------------------------------------------------ evaluation
GOLDEN = [  # (user, history, question, gold docs/sections, expected fact or REFUSE)
    ("alice", [], "How many weeks of parental leave do we get?", {"Parental leave"}, "16 weeks"),
    ("alice", [], "I can't log into my profile, what do I do?",
     {"Password reset", "Account lockout"}, "sign-in"),
    ("alice", [], "What does error E4022 mean?", {"Error E4022"}, "declined"),
    ("alice", [], "What is the meal limit per day?", {"Meals"}, "$50"),
    ("alice", ["What does the dental plan cover?"], "Does that cover my spouse too?",
     {"Dental plan"}, "spouses"),
    ("alice", [], "What is the office wifi password?", set(), "REFUSE"),
    ("alice", [], "What is the executive bonus pool?", set(), "REFUSE"),   # permission probe
    ("hana", [], "What is the executive bonus pool?", {"Bonus pool"}, "12 percent"),
]


def evaluate(sloppy=False, enforce_acl=True):
    rows = []
    for user, hist, q, gold, expect in GOLDEN:
        r = answer(q, user, hist, sloppy=sloppy, verbose=False, enforce_acl=enforce_acl)
        sections = {CHUNKS[c["chunk"]]["crumb"].split(" > ")[-1] for c in r["ctx"]}
        refused = r["text"].startswith("REFUSE")
        leak = any("hr-admins" in CHUNKS[c["chunk"]]["acl"] and "hr-admins" not in USERS[user]
                   for c in r["ctx"])
        n_claims = len(r["claims"]) + len(r["dropped"])
        rows.append({
            "recall": (len(gold & sections) > 0) if gold else None,
            "faithful": len(r["claims"]) / n_claims if n_claims else None,
            "correct": refused if expect == "REFUSE" else (expect in r["text"]),
            "should_refuse": expect == "REFUSE", "refused": refused, "leak": leak})
    mean = lambda xs: sum(xs) / len(xs)
    rec = [r["recall"] for r in rows if r["recall"] is not None]
    fai = [r["faithful"] for r in rows if r["faithful"] is not None]
    print(f"  recall@3 (answerable qs)     : {mean(rec):.2f}")
    print(f"  faithfulness before checks   : {mean(fai):.2f}  (share of generated claims supported)")
    print(f"  correct (fact or refusal)    : {mean([r['correct'] for r in rows]):.2f}")
    print(f"  refused when it should       : {sum(r['refused'] for r in rows if r['should_refuse'])}"
          f"/{sum(r['should_refuse'] for r in rows)}")
    print(f"  refused when it should NOT   : {sum(r['refused'] for r in rows if not r['should_refuse'])}")
    print(f"  permission leaks             : {sum(r['leak'] for r in rows)}")


if __name__ == "__main__":
    print(f"Ingested {len(DOCS)} documents -> {len(CHUNKS)} chunks\n")
    demos = [("alice", [], "What does error E4022 mean?"),
             ("alice", ["What does the dental plan cover?"], "Does that cover my spouse too?"),
             ("alice", [], "What is the meal limit per day?"),
             ("alice", [], "What is the executive bonus pool?"),
             ("hana", [], "What is the executive bonus pool?"),
             ("alice", [], "Write a poem about cats")]
    for user, hist, q in demos:
        print(f"[{user}] Q: {q}")
        answer(q, user, hist)
        print()
    print("[alice] Q: Does the dental plan cover my spouse? (SLOPPY generator)")
    answer("Does the dental plan cover my spouse?", "alice", sloppy=True)

    print("\nGolden-set evaluation, careful generator:")
    evaluate()
    print("Golden-set evaluation, sloppy generator (post-check removes bad claims):")
    evaluate(sloppy=True)
    print("Ablation - ACL filter switched OFF (never do this):")
    evaluate(enforce_acl=False)
