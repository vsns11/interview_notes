"""A design-review linter: describe an AI architecture as facts, apply rules,
and print findings in SEVERITY order (invariants -> loops -> reliability ->
economics -> quality), each with a consequence and a fix, then a sequenced
plan (blockers / week one / later). The rules encode the critique method; a
real review still needs a human to confirm each finding.
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Callable

TIERS = {1: "violated invariant", 2: "missing loop", 3: "reliability", 4: "economics", 5: "quality"}
WHEN = {1: "blocker (before rollout)", 2: "blocker (before rollout)", 3: "week one", 4: "later", 5: "later"}


@dataclass(frozen=True)
class Rule:
    tier: int
    check: Callable[[dict], bool]
    finding: str
    consequence: str
    fix: str


RULES = [
    Rule(1, lambda d: d.get("restricted_content") and not d.get("acl_filter_in_retrieval"),
         "No permission filter on retrieval",
         "restricted pages reach anyone who asks a nearby question",
         "store ACLs per chunk, filter in the index query, re-check current access"),
    Rule(1, lambda d: d.get("tool") == "free_sql" and not d.get("authz_outside_model"),
         "Model writes the SQL (and its WHERE clause) for HR data",
         "one injected prompt can read another employee's record",
         "replace with get_my_record(fields) bound to the caller's identity; row-level security"),
    Rule(1, lambda d: d.get("ingests_untrusted_text") and d.get("tool") and not d.get("injection_controls"),
         "No injection posture while the model holds a tool",
         "a poisoned page can steer the tool (indirect prompt injection)",
         "treat retrieved text as data, add input and action gates, least-privilege tools"),
    Rule(2, lambda d: not d.get("golden_set"),
         "No evaluation set ('works great in the demo')",
         "no way to know quality or catch regressions",
         "golden set with unanswerables and permission probes; groundedness gate on HR answers"),
    Rule(2, lambda d: not d.get("feedback_capture"),
         "No feedback capture or traces",
         "failures are silent by default",
         "thumbs, escalation path, per-request traces with prompt and doc IDs"),
    Rule(3, lambda d: d.get("reindex") == "nightly_full",
         "Nightly full re-index",
         "up to 24h stale policies; fragile all-at-once job; partial index during rebuild",
         "incremental, idempotent, delete-propagating sync with a freshness SLO; build-then-swap"),
    Rule(3, lambda d: not d.get("fallbacks"),
         "No fallbacks for vector DB, model API or tool timeouts",
         "every dependency blip is a user-facing error",
         "timeouts, bounded retries, degrade to search results or human handoff"),
    Rule(4, lambda d: d.get("model_tiers", 1) == 1,
         "Frontier model for every query, no routing or caching",
         "cost scales linearly with 30,000 employees",
         "route simple questions to a small model; permission-scoped caching"),
    Rule(4, lambda d: d.get("top_k", 0) >= 10 and not d.get("rerank"),
         "Top-10 chunks stuffed into context without reranking",
         "more tokens, lost-in-the-middle risk",
         "retrieve wider, rerank, keep a tight context budget"),
    Rule(5, lambda d: not d.get("hybrid"),
         "Vector-only retrieval",
         "may miss exact identifiers (form names, ticket numbers)",
         "measure, then add keyword (BM25) retrieval and fusion if it helps"),
]


def critique(name: str, design: dict) -> None:
    found = sorted((r for r in RULES if r.check(design)), key=lambda r: r.tier)
    print(f"== {name}: {len(found)} findings")
    print("   what the shape gets right:", design.get("strength", "-"))
    for i, r in enumerate(found, 1):
        print(f"   {i}. [T{r.tier} {TIERS[r.tier]}] {r.finding}\n"
              f"      consequence: {r.consequence}\n      fix: {r.fix}")
    print("   plan:")
    for when in dict.fromkeys(WHEN.values()):
        items = [r.finding for r in found if WHEN[r.tier] == when]
        if items:
            print(f"     {when}: " + "; ".join(items))


if __name__ == "__main__":
    support_copilot = {
        "strength": "RAG is the right pattern and a tool for personal HR questions is a good instinct",
        "restricted_content": True, "acl_filter_in_retrieval": False,
        "tool": "free_sql", "authz_outside_model": False,
        "ingests_untrusted_text": True, "injection_controls": False,
        "golden_set": False, "feedback_capture": False,
        "reindex": "nightly_full", "fallbacks": False,
        "model_tiers": 1, "top_k": 10, "rerank": False, "hybrid": False,
    }
    critique("Internal support copilot (30,000 employees)", support_copilot)
    fixed = dict(support_copilot, acl_filter_in_retrieval=True, authz_outside_model=True,
                 injection_controls=True, golden_set=True, feedback_capture=True)
    print()
    critique("Same design after the blockers are fixed", fixed)
