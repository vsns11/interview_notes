"""Model routing/cascade + token-bucket rate limiting + per-request cost telemetry.

Everything is local: two fake models with different prices and skills,
a rule-based router, a cascade with a verifier, and a tokens-per-minute budget.
Prices are illustrative ($ per 1M tokens).
"""
import time
from collections import defaultdict
from dataclasses import dataclass


@dataclass
class FakeModel:
    name: str
    price_in: float
    price_out: float
    skill: int                      # handles tasks with difficulty <= skill

    def run(self, task):
        ok = task["difficulty"] <= self.skill
        text = "good answer" if ok else "vague answer"
        return text, task["tin"], task["tout"]

    def cost(self, tin, tout):
        return (tin * self.price_in + tout * self.price_out) / 1e6


SMALL = FakeModel("small", 0.3, 1.2, skill=2)
LARGE = FakeModel("large", 3.0, 12.0, skill=5)


def rule_router(task):
    """Decide up front, before any model runs (cheap heuristics)."""
    easy_kinds = {"summarize_thread", "status_draft", "classify"}
    return SMALL if task["kind"] in easy_kinds and task["tin"] < 4000 else LARGE


def verifier(answer):
    """Stand-in for a validation check (schema check, grader model, citation check)."""
    return answer == "good answer"


class TokenBucket:
    """Tokens-per-minute limiter: capacity refills continuously."""

    def __init__(self, tokens_per_min):
        self.capacity = tokens_per_min
        self.level = tokens_per_min
        self.rate = tokens_per_min / 60.0
        self.t = 0.0

    def try_take(self, n, now):
        self.level = min(self.capacity, self.level + (now - self.t) * self.rate)
        self.t = now
        if n <= self.level:
            self.level -= n
            return True
        return False


TASKS = [
    {"kind": "summarize_thread", "difficulty": 1, "tin": 2500, "tout": 300},
    {"kind": "status_draft", "difficulty": 2, "tin": 2000, "tout": 250},
    {"kind": "classify", "difficulty": 1, "tin": 800, "tout": 5},
    {"kind": "answer_question", "difficulty": 4, "tin": 3000, "tout": 400},
    {"kind": "summarize_thread", "difficulty": 3, "tin": 2600, "tout": 300},  # tricky one
    {"kind": "status_draft", "difficulty": 1, "tin": 1800, "tout": 200},
    {"kind": "answer_question", "difficulty": 2, "tin": 2200, "tout": 300},
    {"kind": "summarize_thread", "difficulty": 2, "tin": 6000, "tout": 350},
]

if __name__ == "__main__":
    all_large = sum(LARGE.cost(t["tin"], t["tout"]) for t in TASKS)

    # --- router: decide first ---
    cost_r, bad_r = 0.0, 0
    for t in TASKS:
        m = rule_router(t)
        ans, tin, tout = m.run(t)
        cost_r += m.cost(tin, tout)
        bad_r += not verifier(ans)

    # --- cascade: try small, escalate if the verifier fails ---
    cost_c, escalations, telemetry = 0.0, 0, defaultdict(float)
    for t in TASKS:
        ans, tin, tout = SMALL.run(t)
        c = SMALL.cost(tin, tout)
        telemetry[("small", t["kind"])] += c
        if not verifier(ans):
            escalations += 1
            ans, tin, tout = LARGE.run(t)
            c2 = LARGE.cost(tin, tout)
            telemetry[("large", t["kind"])] += c2
            c += c2
        cost_c += c

    print(f"all on large model : ${all_large:.5f} for {len(TASKS)} tasks, 0 bad answers")
    print(f"rule router        : ${cost_r:.5f} ({1 - cost_r / all_large:.0%} cheaper), "
          f"{bad_r} bad answer(s) slipped through")
    print(f"cascade + verifier : ${cost_c:.5f} ({1 - cost_c / all_large:.0%} cheaper), "
          f"escalation rate {escalations}/{len(TASKS)}, 0 bad answers")
    print("cost telemetry by (model, feature):")
    for (m, kind), c in sorted(telemetry.items()):
        print(f"  {m:<6}{kind:<18}${c:.5f}")

    # --- rate limiting: 20K tokens/minute budget, burst of requests ---
    bucket = TokenBucket(20_000)
    admitted, queued = 0, 0
    for i in range(12):                       # 12 requests of ~2,800 tokens in 3 seconds
        now = i * 0.25
        if bucket.try_take(2800, now):
            admitted += 1
        else:
            queued += 1                       # backpressure: queue, retry later, or degrade
    print(f"\ntoken bucket 20K tok/min, 12 x 2.8K-token requests in 3 s: "
          f"{admitted} admitted, {queued} queued/degraded")
