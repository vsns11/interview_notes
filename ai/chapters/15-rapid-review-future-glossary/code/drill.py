"""Rapid-fire drill for the Chapter 15 question bank.

Reads the Q/A pairs from this chapter's README, shuffles them (seeded),
and shows how a Leitner spaced-repetition schedule would space the reviews:
a correct answer moves a card to a box reviewed less often; a miss sends it
back to box 1 (review again after about a day, not immediately).
"""
import random
import re
from pathlib import Path

README = Path(__file__).resolve().parent.parent / "README.md"
REVIEW_EVERY_DAYS = {1: 1, 2: 3, 3: 7, 4: 14}      # box -> days until next review


def load_bank(path: Path) -> list[tuple[str, str]]:
    text = path.read_text(encoding="utf-8")
    pairs = re.findall(r"^\*\*Q\d+\.\s*(.+?)\*\*\s*\nA\.\s*(.+)$", text, flags=re.M)
    return [(q.strip().replace("\\*", "*"), a.strip().replace("\\*", "*")) for q, a in pairs]


def leitner(results: list[bool], box: int = 1) -> list[tuple[int, int]]:
    """Given right/wrong results over successive reviews, return (box, wait) steps."""
    steps = []
    for ok in results:
        box = min(box + 1, 4) if ok else 1
        steps.append((box, REVIEW_EVERY_DAYS[box]))
    return steps


if __name__ == "__main__":
    bank = load_bank(README)
    print(f"Loaded {len(bank)} questions from the README.")
    rng = random.Random(15)
    session = rng.sample(bank, 5)
    for i, (q, a) in enumerate(session, 1):
        anchor = a.split(". ")[0]
        print(f"\n{i}. {q}\n   (30 s, out loud)  anchor: {anchor}.")
    print("\nLeitner schedule for one card answered right, right, wrong, right:")
    for n, (box, wait) in enumerate(leitner([True, True, False, True]), 1):
        print(f"  review {n}: box {box} -> next review in {wait} day(s)")
