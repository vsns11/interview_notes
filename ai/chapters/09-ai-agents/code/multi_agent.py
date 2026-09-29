"""Two multi-agent patterns with contract-style handoffs.

A. Orchestrator-workers (supervisor): a lead agent splits a task, fans the
   pieces out to bounded workers IN PARALLEL, and integrates their structured
   reports. The overall budget is split top-down.
B. Handoff (routing): a triage agent transfers the whole conversation to one
   specialist, which then owns it.

Workers are deterministic stand-ins for LLM calls; the coordination code
(contracts, budgets, parallel fan-out, shared state store) is real.

Run:  python code/multi_agent.py
"""
from __future__ import annotations

import time
from concurrent.futures import ThreadPoolExecutor
from dataclasses import dataclass, field

# Fictional per-competitor facts a research worker can "find".
FACTS = {
    "Acme": {"price": 20, "free_tier": True, "source": "acme.example/pricing"},
    "Globex": {"price": 25, "free_tier": False, "source": "globex.example/plans"},
    "Initech": {"price": 18, "free_tier": True, "source": "initech.example/buy"},
}


@dataclass
class Handoff:
    """The contract: what the worker gets and what it must return."""
    task: str
    context: dict                 # only what this worker needs
    tools: list[str]              # scoped tool menu
    budget_tokens: int            # this worker's slice of the task budget
    report_schema: tuple = ("findings", "evidence", "open_questions", "tokens_used")


@dataclass
class TaskState:
    """External store owned by the orchestrator: survives worker crashes."""
    reports: dict = field(default_factory=dict)
    spent: int = 0


def research_worker(h: Handoff) -> dict:
    time.sleep(0.2)               # pretend network + model latency
    name = h.context["company"]
    f = FACTS.get(name)
    used = 900
    if used > h.budget_tokens:    # out of budget -> partial report, not silence
        return {"findings": {}, "evidence": [], "open_questions": ["budget exhausted"],
                "tokens_used": h.budget_tokens}
    return {"findings": {"price": f["price"], "free_tier": f["free_tier"]},
            "evidence": [f["source"]], "open_questions": [], "tokens_used": used}


def orchestrator(companies: list[str], total_budget: int) -> str:
    state = TaskState()
    integration_reserve = 1000                        # keep budget for the final step
    per_worker = (total_budget - integration_reserve) // len(companies)
    handoffs = [Handoff(task=f"Find list price and free tier for {c}",
                        context={"company": c}, tools=["web_search"],
                        budget_tokens=per_worker) for c in companies]
    t0 = time.perf_counter()
    with ThreadPoolExecutor(max_workers=len(handoffs)) as pool:   # fan-out
        for h, report in zip(handoffs, pool.map(research_worker, handoffs)):
            assert set(report) == set(h.report_schema), "worker broke the contract"
            state.reports[h.context["company"]] = report              # fan-in
            state.spent += report["tokens_used"]
    elapsed = time.perf_counter() - t0
    # Integration step: uses only structured findings + evidence pointers.
    rows = sorted((r["findings"]["price"], c, r["evidence"][0])
                  for c, r in state.reports.items() if r["findings"])
    cheapest = rows[0]
    state.spent += 600
    return (f"Cheapest: {cheapest[1]} at ${cheapest[0]}/seat (source: {cheapest[2]}). "
            f"Tokens spent {state.spent}/{total_budget}; wall time {elapsed:.2f}s "
            f"for {len(companies)} workers run in parallel.")


# ------------------------------------------------------ B. handoff pattern
def billing_agent(msg: str) -> str:
    return "Billing agent: I can see a duplicate charge; refund request drafted for approval."


def tech_agent(msg: str) -> str:
    return "Tech agent: please restart the router; I have opened ticket T-17."


def triage_agent(msg: str) -> tuple[str, callable]:
    """Stand-in for an LLM classifier choosing whom to hand off to."""
    m = msg.lower()
    if any(w in m for w in ("charge", "invoice", "refund")):
        return "billing", billing_agent
    if any(w in m for w in ("wifi", "router", "internet")):
        return "tech", tech_agent
    return "human", lambda _: "Escalated to a human with the full transcript."


if __name__ == "__main__":
    print("A. Orchestrator-workers")
    print("  ", orchestrator(["Acme", "Globex", "Initech"], total_budget=4000))
    tight = research_worker(Handoff("Find price for Acme", {"company": "Acme"},
                                    ["web_search"], budget_tokens=500))
    print("   A worker given only 500 tokens returns a partial report:", tight)
    print("\nB. Handoffs")
    for msg in ["I was charged twice this month", "My wifi keeps dropping",
                "Can I change my company name?"]:
        who, agent = triage_agent(msg)
        print(f"   '{msg}' -> handoff to {who}: {agent(msg)}")
