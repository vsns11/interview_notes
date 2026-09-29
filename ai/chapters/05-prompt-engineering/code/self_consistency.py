"""Self-consistency: sample several reasoning paths, take the majority answer.

Simulated "model": each sample is correct with probability p, otherwise it
picks one of several wrong answers. We compare 1 sample vs majority votes.
"""
from collections import Counter
from math import comb

import numpy as np

rng = np.random.default_rng(7)
WRONG = ["41", "43", "44", "38"]   # plausible wrong answers, spread out


def sample_answer(p_correct: float) -> str:
    return "42" if rng.random() < p_correct else str(rng.choice(WRONG))


def majority_vote(n: int, p: float) -> str:
    votes = Counter(sample_answer(p) for _ in range(n))
    return votes.most_common(1)[0][0]   # ties broken by first seen


def binomial_majority(n: int, p: float) -> float:
    """P(correct answer gets a strict majority) -- a lower bound on vote accuracy
    here, because wrong answers split their votes."""
    return sum(comb(n, k) * p**k * (1 - p)**(n - k) for k in range(n // 2 + 1, n + 1))


if __name__ == "__main__":
    trials = 2000
    for p in [0.4, 0.6]:
        print(f"single-sample accuracy p = {p}")
        for n in [1, 3, 5, 9, 15]:
            acc = np.mean([majority_vote(n, p) == "42" for _ in range(trials)])
            print(f"  votes={n:2d}: simulated accuracy={acc:.3f}  "
                  f"(strict-majority bound={binomial_majority(n, p):.3f})  cost={n}x")
