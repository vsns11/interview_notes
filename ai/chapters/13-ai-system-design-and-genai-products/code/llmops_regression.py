"""LLMOps in miniature: versioned prompts, a golden set, a regression gate,
deterministic canary bucketing and an online A/B significance test.

The "model" is a deterministic fake so the example runs offline. What matters
is the workflow: no prompt or model change reaches users without passing the
same gate.
"""
from __future__ import annotations

import hashlib
import math
import re
from dataclasses import dataclass

import numpy as np

KB = {
    "K1": "Refunds reach the original card within 5 business days.",
    "K2": "Orders can be cancelled for free until they ship.",
    "K3": "Premium members get phone support from 8am to 8pm.",
    "K4": "Passwords are reset from the login page using the Forgot link.",
}


@dataclass(frozen=True)
class PromptVersion:
    """A prompt is config + text. Here, behaviour flags stand in for wording."""
    version: str
    text: str
    cite_sources: bool
    min_overlap_to_answer: int     # how much evidence before answering vs refusing
    verbosity: int                 # extra filler sentences (cost!)
    normalise: bool = False        # e.g. a query-rewrite step: match word stems


def terms(text: str, normalise: bool) -> set[str]:
    ws = re.findall(r"[a-z]+", text.lower())
    if normalise:
        ws = [re.sub(r"(ling|ing|led|ed|s)$", "", w) if len(w) > 4 else w for w in ws]
    return set(ws)


def fake_llm(prompt: PromptVersion, question: str) -> str:
    q = terms(question, prompt.normalise)
    best_id, best_overlap = None, 0
    for doc_id, text in KB.items():
        ov = len(q & terms(text, prompt.normalise))
        if ov > best_overlap:
            best_id, best_overlap = doc_id, ov
    if best_id is None or best_overlap < prompt.min_overlap_to_answer:
        return "I can't find that in our help centre, so I'll connect you to an agent."
    out = KB[best_id] + (f" [{best_id}]" if prompt.cite_sources else "")
    return out + " Happy to help with anything else!" * prompt.verbosity


@dataclass(frozen=True)
class Case:
    question: str
    must_contain: str | None       # None -> the correct behaviour is to refuse
    slice: str


GOLDEN = [
    Case("How many days until refunds reach my card?", "5 business days", "billing"),
    Case("When do refunds arrive on the original card?", "5 business days", "billing"),
    Case("Can orders be cancelled before they ship?", "until they ship", "orders"),
    Case("Is cancelling free?", "until they ship", "orders"),
    Case("What hours is phone support for premium members?", "8am to 8pm", "support"),
    Case("How do I reset my password?", "Forgot link", "account"),
    Case("Where is the login page to reset passwords?", "Forgot link", "account"),
    Case("Do you ship to Mars?", None, "unanswerable"),
    Case("What is the CEO's salary?", None, "unanswerable"),
    Case("Can I pay with seashells?", None, "unanswerable"),
]


def evaluate(prompt: PromptVersion) -> dict:
    per_slice: dict[str, list[int]] = {}
    correct = cited = refuse_ok = n_answerable = n_unans = 0
    out_tokens = 0
    for c in GOLDEN:
        ans = fake_llm(prompt, c.question)
        out_tokens += round(len(ans.split()) / 0.75)
        refused = ans.startswith("I can't find")
        if c.must_contain is None:
            n_unans += 1
            ok = refused
            refuse_ok += ok
        else:
            n_answerable += 1
            ok = (not refused) and c.must_contain in ans
            correct += ok
            cited += bool(re.search(r"\[K\d\]", ans))
        per_slice.setdefault(c.slice, []).append(int(ok))
    return {
        "answer_accuracy": correct / n_answerable,
        "citation_rate": cited / n_answerable,
        "refusal_accuracy": refuse_ok / n_unans,
        "avg_output_tokens": out_tokens / len(GOLDEN),
        "slices": {k: sum(v) / len(v) for k, v in per_slice.items()},
    }


def gate(base: dict, cand: dict, tol: float = 0.0, max_token_growth: float = 0.2) -> list[str]:
    """Return a list of blocking regressions (empty list = ship to canary)."""
    fails = []
    for m in ("answer_accuracy", "citation_rate", "refusal_accuracy"):
        if cand[m] < base[m] - tol:
            fails.append(f"{m}: {base[m]:.2f} -> {cand[m]:.2f}")
    for s, v in base["slices"].items():
        if cand["slices"][s] < v - tol:
            fails.append(f"slice '{s}': {v:.2f} -> {cand['slices'][s]:.2f}")
    if cand["avg_output_tokens"] > base["avg_output_tokens"] * (1 + max_token_growth):
        fails.append(f"avg_output_tokens +{cand['avg_output_tokens'] / base['avg_output_tokens'] - 1:.0%}")
    return fails


def canary_bucket(user_id: str, salt: str, percent: float) -> bool:
    """Stable assignment: the same user always lands in the same arm."""
    h = int(hashlib.sha256(f"{salt}:{user_id}".encode()).hexdigest(), 16) % 10_000
    return h < percent * 100


def two_proportion_z(s1: int, n1: int, s2: int, n2: int) -> tuple[float, float]:
    p1, p2 = s1 / n1, s2 / n2
    p = (s1 + s2) / (n1 + n2)
    se = math.sqrt(p * (1 - p) * (1 / n1 + 1 / n2))
    z = (p2 - p1) / se
    p_two_sided = math.erfc(abs(z) / math.sqrt(2))
    return z, p_two_sided


if __name__ == "__main__":
    v1 = PromptVersion("v1", "Answer from sources and cite them.", True, 2, 0)
    v2 = PromptVersion("v2", "Be friendly and concise.", False, 1, 1)       # a 'harmless' tweak
    v3 = PromptVersion("v3", "Answer from sources, cite, refuse if unsure.", True, 2, 0, normalise=True)

    base = evaluate(v1)
    print("baseline v1:", {k: round(v, 2) for k, v in base.items() if k != "slices"})
    for cand in (v2, v3):
        res = evaluate(cand)
        fails = gate(base, res)
        print(f"\ncandidate {cand.version}:", {k: (round(v, 2) if isinstance(v, float) else v)
                                            for k, v in res.items() if k != "slices"})
        print("  gate:", "PASS -> canary" if not fails else "BLOCK")
        for f in fails:
            print("   -", f)

    users = [f"user{i}" for i in range(20_000)]
    in_canary = sum(canary_bucket(u, "support_answer:v3", 5.0) for u in users)
    print(f"\ncanary: {in_canary} of {len(users)} users ({in_canary / len(users):.1%}) get v3; "
          f"user42 in canary? {canary_bucket('user42', 'support_answer:v3', 5.0)} (stable on every call)")

    rng = np.random.default_rng(7)
    n = 8_000                                             # conversations per arm
    ctrl = rng.binomial(1, 0.62, n).sum()                 # thumbs-up rate, control
    treat = rng.binomial(1, 0.645, n).sum()               # treatment is slightly better
    z, p = two_proportion_z(ctrl, n, treat, n)
    print(f"A/B: control {ctrl / n:.3f} vs treatment {treat / n:.3f} thumbs-up; z={z:.2f}, p={p:.4f} "
          f"-> {'significant' if p < 0.05 else 'not significant'} at 5%")
