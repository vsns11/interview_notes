"""Toy fraud detection: accuracy trap, velocity features, and two cost-based thresholds.

Synthetic stream of card transactions over one week, ~0.2% fraud. Stolen cards are used in quick
bursts, so a VELOCITY feature (transactions on this card in the previous 10 minutes) is powerful.
We compare a model with and without it, then pick an approve / review / block policy by
expected dollar cost under a review-capacity limit.
"""
import numpy as np
from sklearn.ensemble import HistGradientBoostingClassifier
from sklearn.metrics import average_precision_score, roc_auc_score

rng = np.random.default_rng(11)
N_CARDS, WEEK = 60_000, 7 * 24 * 60           # time in minutes


def make_stream():
    rows = []
    for card in range(N_CARDS):
        for t in np.sort(rng.uniform(0, WEEK, rng.poisson(6))):     # normal usage: ~6 per week
            rows.append((card, t, rng.lognormal(3.5, 0.8), rng.random() < 0.1, 0))
    for card in rng.choice(N_CARDS, 150, replace=False):            # 150 stolen cards -> bursts
        start = rng.uniform(0, WEEK - 60)
        for t in start + np.sort(rng.uniform(0, 30, rng.integers(3, 9))):
            rows.append((card, t, rng.lognormal(4.5, 0.9), rng.random() < 0.6, 1))
    rows.sort(key=lambda r: r[1])                                   # time order
    return np.array(rows)


def velocity(card, t, window=10.0):
    """# of earlier transactions on the same card within `window` minutes (a streaming aggregate)."""
    out = np.zeros(len(card))
    last = {}
    for i, (c, ti) in enumerate(zip(card, t)):
        hist = [x for x in last.get(c, []) if ti - x <= window]
        out[i] = len(hist)
        last[c] = hist + [ti]
    return out


if __name__ == "__main__":
    s = make_stream()
    card, t, amount, foreign, y = s[:, 0], s[:, 1], s[:, 2], s[:, 3], s[:, 4].astype(int)
    vel = velocity(card, t)
    hour = (t // 60) % 24
    print(f"{len(y):,} transactions, fraud rate {y.mean():.3%}")
    print(f"'approve everything' accuracy = {1 - y.mean():.3%}  (catches 0 fraud)\n")

    split = t < 5 * 24 * 60                                         # train on days 1-5, test on 6-7
    base = np.column_stack([np.log(amount), foreign, hour])
    full = np.column_stack([base, vel])
    for name, X in [("amount+foreign+hour", base), ("+ velocity (10 min)", full)]:
        m = HistGradientBoostingClassifier(max_iter=200, random_state=0).fit(X[split], y[split])
        p = m.predict_proba(X[~split])[:, 1]
        print(f"{name:<22} ROC-AUC {roc_auc_score(y[~split], p):.3f}   PR-AUC {average_precision_score(y[~split], p):.3f}")

    # three-way policy on the test days (p = scores of the + velocity model): approve < lo <= review < hi <= block
    yt, amt = y[~split], amount[~split]
    COST_FALSE_DECLINE, COST_REVIEW, CAPACITY = 15.0, 3.0, 300      # $ lost goodwill+margin, $ per review, reviews/2 days

    def cost(lo, hi):
        block, review = p >= hi, (p >= lo) & (p < hi)
        missed = (yt == 1) & ~block & ~review                       # approved fraud: lose the amount
        c = amt[missed].sum() + COST_FALSE_DECLINE * ((yt == 0) & block).sum() + COST_REVIEW * review.sum()
        c += amt[(yt == 1) & review].sum() * 0.1                    # reviewers miss ~10% of what they see
        return c, block.sum(), review.sum(), ((yt == 1) & (block | review)).sum() / yt.sum()

    grid = [0.01, 0.02, 0.05, 0.1, 0.2, 0.3, 0.5, 0.7, 0.9]
    options = [(lo, hi, *cost(lo, hi)) for lo in grid for hi in grid if hi > lo]
    feasible = [o for o in options if o[4] <= CAPACITY]
    best = min(feasible, key=lambda o: o[2])
    print(f"\nno model (approve all): cost ${amt[yt == 1].sum():,.0f}")
    for label, (lo, hi) in [("single threshold 0.5", (0.5, 0.5 + 1e-9)), ("best two thresholds", best[:2])]:
        c, nb, nr, rec = cost(lo, hi)
        print(f"{label:<22} lo={lo:.2f} hi={hi:.2f}: cost ${c:,.0f}  blocked {nb}  reviewed {nr}  fraud caught {rec:.0%}")
