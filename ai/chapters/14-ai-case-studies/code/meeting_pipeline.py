"""AI meeting assistant, in miniature.

1. Word error rate (WER) for the live ASR lane.
2. Async post-call workflow: segment -> map-reduce summary -> action items
   with owner validation and source timestamps; low-confidence ASR is flagged.
3. Archive Q&A that is bounded by the asker's access (ACL) and cites
   meeting moments.
4. Tombstone-and-propagate deletion across every derived artefact.
5. The "comb-shaped" load at the top of the hour and the queue that absorbs it.
The summariser is a deterministic extractive stand-in for an LLM.
"""
from __future__ import annotations

import re
from collections import deque
from dataclasses import dataclass, field

import numpy as np


# ------------------------------------------------------------------ 1. WER
def wer(reference: str, hypothesis: str) -> float:
    """(substitutions + deletions + insertions) / words in reference, via edit distance."""
    r, h = reference.lower().split(), hypothesis.lower().split()
    d = [[0] * (len(h) + 1) for _ in range(len(r) + 1)]
    for i in range(len(r) + 1):
        d[i][0] = i
    for j in range(len(h) + 1):
        d[0][j] = j
    for i in range(1, len(r) + 1):
        for j in range(1, len(h) + 1):
            cost = 0 if r[i - 1] == h[j - 1] else 1
            d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost)
    return d[len(r)][len(h)] / len(r)


# ------------------------------------------------------------- 2. pipeline
@dataclass
class Utterance:
    ts: str          # mm:ss into the meeting
    speaker: str
    text: str
    conf: float      # ASR confidence 0..1


@dataclass
class Meeting:
    mid: str
    title: str
    attendees: set[str]
    utterances: list[Utterance]
    extra_viewers: set[str] = field(default_factory=set)   # explicitly shared with
    deleted: bool = False


M1 = Meeting("m1", "Launch sync", {"priya", "tom", "lee"}, [
    Utterance("00:10", "priya", "Topic one is the launch date for the mobile app.", 0.97),
    Utterance("01:05", "tom", "Engineering commits to shipping the beta to marketing by Friday.", 0.95),
    Utterance("02:30", "lee", "Marketing needs final screenshots two days before the campaign.", 0.93),
    Utterance("04:12", "priya", "Next topic is pricing for the premium tier.", 0.96),
    Utterance("05:40", "lee", "We agreed premium stays at twelve dollars for launch.", 0.62),
    Utterance("06:15", "tom", "Tom will update the pricing page by Wednesday.", 0.94),
    Utterance("07:02", "priya", "Sam will draft the press release by Monday.", 0.91),
])
M2 = Meeting("m2", "Legal review", {"ana", "priya"}, [
    Utterance("00:20", "ana", "Topic one is the acquisition term sheet.", 0.96),
    Utterance("03:10", "ana", "Engineering commits to a security audit before the deal closes.", 0.94),
])
ARCHIVE = [M1, M2]


def segments(m: Meeting) -> list[list[Utterance]]:
    """Split on explicit topic changes (a real system uses an LLM or topic model)."""
    segs, cur = [], []
    for u in m.utterances:
        if re.search(r"\b(next topic|topic one)\b", u.text.lower()) and cur:
            segs.append(cur)
            cur = []
        cur.append(u)
    return segs + [cur]


def map_summarise(seg: list[Utterance]) -> str:
    """MAP: summarise one segment (extractive stand-in: keep decisions and commitments)."""
    key = re.compile(r"\b(agreed|commits?|decided|needs?)\b", re.I)
    keep = [u for u in seg if key.search(u.text)] or seg[:1]
    return " ".join(u.text + (" [low-confidence audio - verify]" if u.conf < 0.8 else "")
                    + f" ({u.ts})" for u in keep)


def reduce_summarise(parts: list[str]) -> str:
    """REDUCE: combine segment summaries into the final summary."""
    return "\n".join(f"  - {p}" for p in parts)


ACTION = re.compile(r"(?P<owner>[A-Z][a-z]+) will (?P<task>.+?) by (?P<due>\w+day)")


def action_items(m: Meeting) -> list[dict]:
    items = []
    for u in m.utterances:
        for a in ACTION.finditer(u.text):
            owner = a["owner"].lower()
            items.append({
                "owner": owner if owner in m.attendees else f"UNCONFIRMED ({owner} not an attendee)",
                "task": a["task"], "due": a["due"], "source": f"{m.mid}@{u.ts}: \"{u.text}\"",
            })
    return items


# --------------------------------------------------------- 3. archive Q&A
def can_read(user: str, m: Meeting) -> bool:
    return not m.deleted and (user in m.attendees or user in m.extra_viewers)


def ask(user: str, question: str) -> None:
    q = set(re.findall(r"[a-z]+", question.lower()))
    hits = []
    for m in ARCHIVE:
        if not can_read(user, m):                       # ACL applied BEFORE retrieval scoring
            continue
        for u in m.utterances:
            score = len(q & set(re.findall(r"[a-z]+", u.text.lower())))
            if score >= 2:
                hits.append((score, m, u))
    print(f"[{user}] {question}")
    for _, m, u in sorted(hits, key=lambda h: -h[0])[:3]:
        print(f"    - {u.text}  <{m.title}, {m.mid}@{u.ts}>")
    print("    (answer covers only meetings you can access)")


# ------------------------------------------------------ 4. deletion
def tombstone(m: Meeting, stores: dict[str, dict]) -> None:
    m.deleted = True                                   # 1) mark dead everywhere reads check
    for name, store in stores.items():                 # 2) propagate to every derived copy
        removed = store.pop(m.mid, None)
        print(f"    purge {name:<13} {'removed' if removed is not None else 'nothing to remove'}")


# -------------------------------------------------------- 5. comb load
def comb_load(meetings_per_hour: int, workers: int, job_minutes: float, seed: int = 3) -> None:
    rng = np.random.default_rng(seed)
    # 70% of meetings end within 3 minutes of the top of the hour, the rest spread out
    ends = np.concatenate([rng.uniform(0, 3, int(meetings_per_hour * 0.7)),
                           rng.uniform(0, 60, meetings_per_hour - int(meetings_per_hour * 0.7))])
    arrivals = np.bincount(ends.astype(int), minlength=60)
    queue, free_at, waits = deque(), [0.0] * workers, []
    for minute in range(120):
        for _ in range(arrivals[minute] if minute < 60 else 0):
            queue.append(minute)
        for w in range(workers):
            while free_at[w] <= minute and queue:
                start = max(free_at[w], queue[0])
                waits.append(start - queue.popleft())
                free_at[w] = start + job_minutes
    if workers == 150:
        print(f"    arrivals in minutes 0-2 after the hour: {arrivals[:3].sum()} of {meetings_per_hour}; "
              f"busiest minute: {arrivals.max()}")
    print(f"    {workers} workers x {job_minutes} min/job: p50 wait {np.percentile(waits, 50):.1f} min, "
          f"max wait {max(waits):.1f} min, all done within 10-min SLO: {max(waits) + job_minutes <= 10}")


if __name__ == "__main__":
    print("1) WER")
    ref = "engineering commits to shipping the beta by friday"
    for hyp in ["engineering commits to shipping the beta by friday",
                "engineering commit to shipping beta by friday",
                "uh engineering um commits to uh shipping the the beta by friday okay"]:
        print(f"    WER={wer(ref, hyp):.2f}  <- '{hyp}'")
    print(f"    WER can exceed 1: {wer('yes', 'well yes I mean maybe'):.2f}")

    print("\n2) Post-call workflow for", M1.title)
    parts = [map_summarise(s) for s in segments(M1)]
    print(reduce_summarise(parts))
    for it in action_items(M1):
        print(f"    ACTION owner={it['owner']}, task='{it['task']}', due={it['due']}\n           source {it['source']}")

    print("\n3) Archive Q&A with access control")
    ask("tom", "what did engineering commit to marketing")
    ask("priya", "what did engineering commit to marketing")

    print("\n4) Delete meeting m1 (approved, no legal hold)")
    stores = {"transcript": {"m1": 1, "m2": 1}, "vector index": {"m1": 1, "m2": 1},
              "summary cache": {"m1": 1}, "action items": {"m1": 1}, "email digests": {}}
    tombstone(M1, stores)
    ask("priya", "what did engineering commit to marketing")

    print("\n5) Comb-shaped load (5,000 meetings/hour in one region)")
    print(f"    average need: 5,000 x 1.5 min / 60 min = {5000 * 1.5 / 60:.0f} busy workers")
    comb_load(5_000, workers=150, job_minutes=1.5)
    comb_load(5_000, workers=700, job_minutes=1.5)
