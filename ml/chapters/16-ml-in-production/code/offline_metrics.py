"""Offline metrics by problem family: classification under imbalance, ranking, regression.

Everything is computed by hand where it is short, and checked against scikit-learn.
"""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (average_precision_score, ndcg_score, precision_recall_curve,
                             roc_auc_score, roc_curve)

IMG = Path(__file__).resolve().parent.parent / "img"


# ---------- ranking metrics ----------
def recall_at_k(ranked_ids, relevant, k):
    return len(set(ranked_ids[:k]) & relevant) / len(relevant)


def dcg(grades):
    grades = np.asarray(grades, dtype=float)
    return np.sum(grades / np.log2(np.arange(2, len(grades) + 2)))   # position i discounted by log2(i+1)


def ndcg_at_k(grades_in_shown_order, k):
    ideal = sorted(grades_in_shown_order, reverse=True)
    return dcg(grades_in_shown_order[:k]) / dcg(ideal[:k])


def mrr(list_of_ranked_relevance):
    """Each item: list of 0/1 in shown order. Reciprocal rank of first 1, or 0 if none."""
    rr = [1 / (r.index(1) + 1) if 1 in r else 0.0 for r in list_of_ranked_relevance]
    return float(np.mean(rr))


def classification_demo():
    # synthetic fraud data: 0.2% positives; each of 8 features is shifted a little for fraud
    rng = np.random.default_rng(1)
    n = 200_000
    y = (rng.random(n) < 0.002).astype(int)
    X = rng.normal(0, 1, (n, 8)) + y[:, None] * rng.uniform(0.4, 1.0, 8)
    Xtr, ytr, Xte, yte = X[:120_000], y[:120_000], X[120_000:], y[120_000:]
    clf = LogisticRegression(max_iter=1000).fit(Xtr, ytr)
    s = clf.predict_proba(Xte)[:, 1]
    print(f"test rows {len(yte):,}, positives {yte.sum()} (base rate {yte.mean():.2%})")
    print(f"ROC-AUC {roc_auc_score(yte, s):.3f}   PR-AUC (average precision) {average_precision_score(yte, s):.3f}"
          f"   PR-AUC of a random model = base rate = {yte.mean():.3f}")
    print(f"{'threshold':>9s} {'flagged':>8s} {'TP':>4s} {'FP':>6s} {'precision':>9s} {'recall':>7s} {'FPR':>7s}")
    for t in (0.005, 0.02, 0.05, 0.1):
        f = s >= t
        tp, fp = int((f & (yte == 1)).sum()), int((f & (yte == 0)).sum())
        prec = tp / max(f.sum(), 1)
        rec = tp / yte.sum()
        fpr = fp / (yte == 0).sum()
        print(f"{t:9.3f} {f.sum():8d} {tp:4d} {fp:6d} {prec:9.2f} {rec:7.2f} {fpr:7.4f}")

    # two thresholds -> approve / review / block
    lo, hi = 0.01, 0.05
    band = np.where(s >= hi, "block", np.where(s >= lo, "review", "approve"))
    for b in ("approve", "review", "block"):
        m = band == b
        print(f"  {b:8s}: {m.sum():6d} txns, fraud rate in band {yte[m].mean():.2%}")

    fig, ax = plt.subplots(1, 2, figsize=(9, 3.6))
    fpr, tpr, _ = roc_curve(yte, s)
    ax[0].plot(fpr, tpr); ax[0].plot([0, 1], [0, 1], "k--", lw=1)
    ax[0].set_title(f"ROC  (AUC {roc_auc_score(yte, s):.3f})"); ax[0].set_xlabel("false positive rate"); ax[0].set_ylabel("recall")
    p, r, _ = precision_recall_curve(yte, s)
    ax[1].plot(r, p); ax[1].axhline(yte.mean(), color="k", ls="--", lw=1)
    ax[1].set_title(f"Precision-Recall  (AP {average_precision_score(yte, s):.3f})"); ax[1].set_xlabel("recall"); ax[1].set_ylabel("precision")
    fig.tight_layout(); fig.savefig(IMG / "roc_vs_pr.png", dpi=130)


def ranking_demo():
    shown = ["a", "b", "c", "d", "e", "f"]
    grades = [3, 0, 2, 0, 1, 0]                 # graded relevance of what we showed, in shown order
    relevant = {"a", "c", "e", "z"}             # z was relevant but never retrieved
    print(f"recall@3 = {recall_at_k(shown, relevant, 3):.2f}   recall@6 = {recall_at_k(shown, relevant, 6):.2f}")
    print(f"NDCG@6 (by hand) = {ndcg_at_k(grades, 6):.4f}   "
          f"sklearn ndcg_score = {ndcg_score([grades], [[6, 5, 4, 3, 2, 1]], k=6):.4f}")
    print(f"MRR over 3 queries = {mrr([[0, 1, 0], [1, 0, 0], [0, 0, 0]]):.4f}   (1/2 + 1 + 0) / 3")


def regression_demo():
    rng = np.random.default_rng(0)
    city = rng.choice(["mature"] * 40 + ["new"] * 3, 20_000)
    y = rng.gamma(9, 4, 20_000)
    err = np.where(city == "mature", rng.normal(0, 4, 20_000), rng.normal(6, 12, 20_000))
    pred = y + err
    a = np.abs(err)
    print(f"overall  MAE {a.mean():5.2f}  RMSE {np.sqrt((err ** 2).mean()):5.2f}  p90|err| {np.percentile(a, 90):5.2f}")
    for c in ("mature", "new"):
        m = city == c
        print(f"{c:7s}  MAE {a[m].mean():5.2f}  RMSE {np.sqrt((err[m] ** 2).mean()):5.2f}"
              f"  p90|err| {np.percentile(a[m], 90):5.2f}  share {m.mean():.0%}")
    naive = np.full_like(y, y.mean())
    print(f"naive baseline (predict the mean) MAE {np.abs(y - naive).mean():5.2f}")
    print(f"a 40-min miss adds {40 ** 2 / 10 ** 2:.0f}x a 10-min miss to squared error, {40 / 10:.0f}x to absolute error")


if __name__ == "__main__":
    print("== classification (rare positives) ==")
    classification_demo()
    print("\n== ranking ==")
    ranking_demo()
    print("\n== regression (ETA minutes) ==")
    regression_demo()
    print("\nsaved img/roc_vs_pr.png")
