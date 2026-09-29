"""Gini impurity, entropy and information gain for one candidate split, by hand.

Toy data: 10 loan applicants, label = defaulted (1) or repaid (0).
Candidate split: "income < 40k?".
"""
import math
from collections import Counter


def gini(labels):
    n = len(labels)
    return 1 - sum((c / n) ** 2 for c in Counter(labels).values())


def entropy(labels):
    n = len(labels)
    return -sum((c / n) * math.log2(c / n) for c in Counter(labels).values())


def weighted(children, fn):
    n = sum(len(c) for c in children)
    return sum(len(c) / n * fn(c) for c in children)


if __name__ == "__main__":
    # income (k$), defaulted?
    data = [(25, 1), (30, 1), (35, 1), (38, 0), (45, 0),
            (50, 0), (60, 0), (70, 0), (80, 1), (90, 0)]
    parent = [y for _, y in data]
    left = [y for x, y in data if x < 40]    # income < 40k
    right = [y for x, y in data if x >= 40]

    print(f"parent {parent}  -> gini={gini(parent):.3f}  entropy={entropy(parent):.3f}")
    print(f"left   {left}         -> gini={gini(left):.3f}  entropy={entropy(left):.3f}")
    print(f"right  {right}     -> gini={gini(right):.3f}  entropy={entropy(right):.3f}")
    wg = weighted([left, right], gini)
    we = weighted([left, right], entropy)
    print(f"weighted child gini    = {wg:.3f}  -> gini decrease   = {gini(parent) - wg:.3f}")
    print(f"weighted child entropy = {we:.3f}  -> information gain = {entropy(parent) - we:.3f}")

    # Try every threshold and pick the best (what a tree does for one feature)
    xs = sorted({x for x, _ in data})
    print("\nthreshold  gini_decrease  info_gain")
    for a, b in zip(xs, xs[1:]):
        t = (a + b) / 2
        l = [y for x, y in data if x <= t]
        r = [y for x, y in data if x > t]
        print(f"{t:9.1f}  {gini(parent) - weighted([l, r], gini):13.3f}  "
              f"{entropy(parent) - weighted([l, r], entropy):9.3f}")
