"""Agent memory in four tiers, in plain Python + numpy.

1. Working memory  - the recent steps kept verbatim inside a token budget.
2. Compaction      - older steps folded into a STRUCTURED summary that keeps
                     decisions, constraints and failures (not narrative prose).
3. Scratchpad      - notes stored outside the context; the window keeps a pointer.
4. Long-term memory- facts embedded as vectors; retrieved by cosine similarity
                     at the start of a new task; user-visible and deletable.

Embeddings here are a hashed bag-of-words (deterministic, no model download).
A real system would call an embedding model and a vector database.

Run:  python code/agent_memory.py
"""
from __future__ import annotations

import hashlib
import re
from dataclasses import dataclass, field

import numpy as np

DIM = 256


STOP = {"a", "an", "the", "for", "to", "of", "on", "and", "is", "must", "have",
        "user", "users", "s", "next", "year", "over", "book", "find", "choose"}


def words(text: str) -> list[str]:
    """Lower-case, drop stop-words, crude plural stripping (venues -> venue)."""
    out = []
    for w in re.findall(r"[a-z]+", text.lower()):
        if w in STOP:
            continue
        out.append(w[:-1] if w.endswith("s") and len(w) > 3 else w)
    return out


def embed(text: str) -> np.ndarray:
    """Hashed bag-of-words: each word adds 1 to one of DIM buckets."""
    v = np.zeros(DIM)
    for w in words(text):
        v[int(hashlib.md5(w.encode()).hexdigest(), 16) % DIM] += 1
    n = np.linalg.norm(v)
    return v / n if n else v


def tokens(text: str) -> int:
    return max(1, len(text) // 4)


# ------------------------------------------------ tiers 1 + 2: the window
@dataclass
class Step:
    kind: str      # "decision" | "constraint" | "failure" | "tool_result"
    text: str


@dataclass
class WorkingMemory:
    budget_tokens: int = 40
    steps: list[Step] = field(default_factory=list)
    summary: dict[str, list[str]] = field(
        default_factory=lambda: {"decisions": [], "constraints": [], "failures": []})

    def add(self, step: Step) -> None:
        self.steps.append(step)
        while sum(tokens(s.text) for s in self.steps) > self.budget_tokens:
            self._compact(self.steps.pop(0))

    def _compact(self, old: Step) -> None:
        """Loss-aware compaction: raw tool output is dropped, but decisions,
        constraints and failures survive as one-line bullets."""
        bucket = {"decision": "decisions", "constraint": "constraints",
                  "failure": "failures"}.get(old.kind)
        if bucket:
            self.summary[bucket].append(old.text[:70])

    def render(self) -> str:
        lines = ["SUMMARY OF EARLIER WORK:"]
        for k, items in self.summary.items():
            lines += [f"{k}: " + ("; ".join(items) if items else "-")]
        lines += ["RECENT STEPS (verbatim):"] + [f"[{s.kind}] {s.text}" for s in self.steps]
        return "\n".join(lines)


# ------------------------------------------------------- tier 3: scratchpad
class Scratchpad:
    """Stands in for files the agent writes through a tool."""
    def __init__(self):
        self.files: dict[str, str] = {}

    def write(self, name: str, text: str) -> str:
        self.files[name] = text
        return f"(saved {tokens(text)} tokens to {name})"   # the pointer

    def read(self, name: str) -> str:
        return self.files[name]


# ------------------------------------------------- tier 4: long-term memory
class LongTermMemory:
    def __init__(self):
        self.items: list[tuple[str, str, np.ndarray]] = []  # (id, text, vector)

    def remember(self, text: str) -> str:
        mid = f"m{len(self.items) + 1}"
        self.items.append((mid, text, embed(text)))
        return mid

    def recall(self, query: str, k: int = 2, min_score: float = 0.2):
        q = embed(query)
        scored = sorted(((float(v @ q), mid, t) for mid, t, v in self.items), reverse=True)
        return [(round(s, 2), mid, t) for s, mid, t in scored[:k] if s >= min_score]

    def list_for_user(self) -> list[tuple[str, str]]:
        return [(mid, t) for mid, t, _ in self.items]

    def forget(self, mid: str) -> None:
        self.items = [x for x in self.items if x[0] != mid]


if __name__ == "__main__":
    print("--- tiers 1+2: a long task squeezed into a 40-token window ---")
    wm = WorkingMemory()
    pad = Scratchpad()
    log_dump = "row " * 120  # a big raw tool output
    wm.add(Step("constraint", "Budget for the offsite is 5000 dollars."))
    wm.add(Step("tool_result", "search_venues -> 14 venues found"))
    wm.add(Step("failure", "Venue Lakeside rejected: no wheelchair access."))
    wm.add(Step("tool_result", pad.write("venues_raw.txt", log_dump)))
    wm.add(Step("decision", "Shortlist: Hilltop and Riverside."))
    wm.add(Step("tool_result", "get_quote Hilltop -> 4200 dollars"))
    wm.add(Step("tool_result", "get_quote Riverside -> 4700 dollars"))
    print(wm.render())
    print("\nScratchpad holds:", {k: f"{tokens(v)} tokens" for k, v in pad.files.items()})

    print("\n--- tier 4: long-term memory across tasks ---")
    ltm = LongTermMemory()
    for fact in ["User prefers aisle seats on flights.",
                 "User is vegetarian; avoid steak restaurants.",
                 "Team offsite venues must have wheelchair access.",
                 "User's manager approves travel over 1000 dollars."]:
        ltm.remember(fact)
    for task in ["Book a flight to Denver for the user",
                 "Choose a restaurant for the team dinner",
                 "Find a venue for next year's offsite"]:
        print(f"Task: {task}\n   recalled: {ltm.recall(task)}")
    print("\nUser views memory:", ltm.list_for_user())
    ltm.forget("m2")
    print("After user deletes m2:", [m for m, _ in ltm.list_for_user()])
    print("Restaurant task now recalls:", ltm.recall("Choose a restaurant for the team dinner"))
