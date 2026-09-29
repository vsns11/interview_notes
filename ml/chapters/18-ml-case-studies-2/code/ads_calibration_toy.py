"""Toy ad click prediction: good AUC is not enough, the auction needs CALIBRATED probabilities.

1) Train a click model on negatively down-sampled data (keep 10% of non-clicks) - a standard trick
   to cut training cost. Ordering (AUC) is fine, but every pCTR is inflated.
2) Fix it two ways: the analytic down-sampling correction, and post-hoc calibration
   (Platt scaling = logistic on the score; isotonic regression = monotone step function).
3) Show why it matters: in a bid x pCTR auction, an advertiser segment whose pCTR is inflated
   wins too often; and per-segment calibration exposes what the global average hides.
"""
import numpy as np
from sklearn.ensemble import HistGradientBoostingClassifier
from sklearn.isotonic import IsotonicRegression
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import log_loss, roc_auc_score

rng = np.random.default_rng(3)


def simulate(n):
    x = rng.normal(size=(n, 5))
    mobile = (rng.random(n) < 0.5).astype(float)
    logit = -4.0 + 0.9 * x[:, 0] + 0.6 * x[:, 1] - 0.5 * x[:, 2] + 0.4 * mobile
    y = (rng.random(n) < 1 / (1 + np.exp(-logit))).astype(int)
    return np.column_stack([x, mobile]), y


def ece(y, p, bins=10):
    """Expected calibration error: weighted gap between predicted and observed rate per bin."""
    edges = np.quantile(p, np.linspace(0, 1, bins + 1))
    idx = np.clip(np.searchsorted(edges, p, side="right") - 1, 0, bins - 1)
    return sum(abs(p[idx == b].mean() - y[idx == b].mean()) * (idx == b).mean() for b in range(bins))


if __name__ == "__main__":
    X, y = simulate(300_000)
    Xtr, ytr, Xcal, ycal, Xte, yte = X[:200_000], y[:200_000], X[200_000:250_000], y[200_000:250_000], X[250_000:], y[250_000:]
    print(f"true CTR in test traffic: {yte.mean():.4f}")

    # 1) negative down-sampling: keep all clicks, 10% of non-clicks
    w = 0.10
    keep = (ytr == 1) | (rng.random(len(ytr)) < w)
    model = HistGradientBoostingClassifier(max_iter=200, random_state=0).fit(Xtr[keep], ytr[keep])
    raw = model.predict_proba(Xte)[:, 1]

    # 2a) analytic correction for down-sampling: q = p / (p + (1 - p) / w)
    corrected = raw / (raw + (1 - raw) / w)
    # 2b) post-hoc calibration on a held-out, NOT down-sampled set
    raw_cal = model.predict_proba(Xcal)[:, 1]
    platt = LogisticRegression().fit(np.log(raw_cal / (1 - raw_cal)).reshape(-1, 1), ycal)
    p_platt = platt.predict_proba(np.log(raw / (1 - raw)).reshape(-1, 1))[:, 1]
    iso = IsotonicRegression(out_of_bounds="clip").fit(raw_cal, ycal)
    p_iso = iso.predict(raw)

    print(f"\n{'pCTR source':<22}{'AUC':>7}{'mean pCTR':>11}{'log loss':>10}{'ECE':>8}")
    for name, p in [("raw (down-sampled)", raw), ("analytic correction", corrected),
                    ("Platt scaling", p_platt), ("isotonic", p_iso)]:
        p = np.clip(p, 1e-6, 1 - 1e-6)
        print(f"{name:<22}{roc_auc_score(yte, p):>7.3f}{p.mean():>11.4f}{log_loss(yte, p):>10.4f}{ece(yte, p):>8.4f}")

    # 3) per-segment calibration: a model that is fine on average but off for one segment
    mobile = Xte[:, 5] == 1
    skewed = np.where(mobile, np.clip(p_iso * 1.3, 0, 1), p_iso * 0.59)   # pretend a buggy feature skews segments
    print(f"\nsegment check (skewed model)   overall pred/actual = {skewed.mean():.4f}/{yte.mean():.4f}")
    for seg, m in [("mobile", mobile), ("desktop", ~mobile)]:
        print(f"  {seg:<8} predicted {skewed[m].mean():.4f}  observed {yte[m].mean():.4f}  "
              f"ratio {skewed[m].mean() / yte[m].mean():.2f}")

    # 4) auction impact: two advertisers compete for every impression; rank by bid x pCTR.
    #    Each has its own true CTR per impression; A's pCTR is inflated 1.5x on mobile (miscalibrated segment).
    n = 20_000
    idx = rng.choice(len(Xte), n, replace=False)
    base, mob = p_iso[idx], mobile[idx]
    ctr_a = base * rng.lognormal(0, 0.4, n)                  # true CTRs of the two ads on this impression
    ctr_b = base * rng.lognormal(0, 0.4, n)
    bid_a, bid_b = 1.00, 1.00                                # $ per click
    for name, pa in [("calibrated pCTR", ctr_a), ("A inflated on mobile", ctr_a * np.where(mob, 1.5, 1.0))]:
        a_wins = bid_a * pa > bid_b * ctr_b
        value = np.where(a_wins, bid_a * ctr_a, bid_b * ctr_b).sum()   # expected clicks x bid actually delivered
        print(f"auction, {name:<21}: A wins {a_wins[mob].mean():.1%} of mobile / {a_wins[~mob].mean():.1%} of desktop, "
              f"expected value ${value:,.0f}")
