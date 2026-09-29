"""Class imbalance (0.2% fraud): accuracy trap, negative downsampling, and probability recalibration.

After keeping only a fraction w of negatives, the model's odds are inflated by 1/w.
Correct them back:   p = w*q / (w*q + 1 - q)      (q = score from the downsampled model)
Then check calibration (reliability) and fit Platt / isotonic on held-out data.
"""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.calibration import CalibratedClassifierCV, calibration_curve
from sklearn.datasets import make_classification
from sklearn.frozen import FrozenEstimator
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, brier_score_loss, log_loss, average_precision_score

IMG = Path(__file__).resolve().parent.parent / "img"


def ece(y, p, bins=10):
    """Expected calibration error: weighted gap between mean score and observed rate, per score bin."""
    edges = np.quantile(p, np.linspace(0, 1, bins + 1))
    idx = np.clip(np.searchsorted(edges, p, side="right") - 1, 0, bins - 1)
    return sum((idx == b).mean() * abs(p[idx == b].mean() - y[idx == b].mean())
               for b in range(bins) if (idx == b).any())


if __name__ == "__main__":
    X, y = make_classification(n_samples=600_000, n_features=12, n_informative=6,
                               weights=[0.998], flip_y=0, class_sep=1.2, random_state=0)
    Xtr, ytr = X[:300_000], y[:300_000]
    Xcal, ycal = X[300_000:400_000], y[300_000:400_000]      # held out, real base rate
    Xte, yte = X[400_000:], y[400_000:]
    print(f"base rate: {y.mean():.3%}")
    print(f"'always legit' accuracy: {accuracy_score(yte, np.zeros_like(yte)):.2%}  (catches 0 fraud)\n")

    # keep every positive, keep w = 10% of negatives
    w = 0.10
    rng = np.random.default_rng(0)
    keep = (ytr == 1) | (rng.random(len(ytr)) < w)
    print(f"downsampled train: {keep.sum():,} rows (was {len(ytr):,}), positive share {ytr[keep].mean():.2%}")

    model = LogisticRegression(max_iter=1000).fit(Xtr[keep], ytr[keep])
    q = model.predict_proba(Xte)[:, 1]
    p_corrected = w * q / (w * q + 1 - q)

    cal_platt = CalibratedClassifierCV(FrozenEstimator(model), method="sigmoid").fit(Xcal, ycal)
    cal_iso = CalibratedClassifierCV(FrozenEstimator(model), method="isotonic").fit(Xcal, ycal)
    p_platt = cal_platt.predict_proba(Xte)[:, 1]
    p_iso = cal_iso.predict_proba(Xte)[:, 1]

    print(f"\n{'scores':26s} {'mean':>8s} {'PR-AUC':>7s} {'Brier':>9s} {'logloss':>8s} {'ECE':>8s}")
    print(f"{'(true rate)':26s} {yte.mean():8.4f}")
    for name, p in [("raw (downsampled)", q), ("odds-corrected (w=0.1)", p_corrected),
                    ("Platt on held-out", p_platt), ("isotonic on held-out", p_iso)]:
        print(f"{name:26s} {p.mean():8.4f} {average_precision_score(yte, p):7.3f} "
              f"{brier_score_loss(yte, p):9.6f} {log_loss(yte, p):8.4f} {ece(yte, p):8.5f}")
    print("-> ranking (PR-AUC) is identical for raw and corrected: calibration is a separate property")

    # reliability diagram
    fig, ax = plt.subplots(figsize=(5, 4.2))
    for name, p in [("raw (downsampled)", q), ("odds-corrected", p_corrected), ("isotonic", p_iso)]:
        frac, mean_pred = calibration_curve(yte, p, n_bins=10, strategy="quantile")
        ax.plot(mean_pred, frac, marker="o", label=name)
    lim = max(q.max(), 0.05)
    ax.plot([0, lim], [0, lim], "k--", lw=1, label="perfect")
    ax.set_xscale("log"); ax.set_yscale("log")
    ax.set_xlabel("mean predicted probability (bin)")
    ax.set_ylabel("observed fraud rate (bin)")
    ax.legend(fontsize=8)
    fig.tight_layout()
    fig.savefig(IMG / "reliability.png", dpi=130)
    print("saved img/reliability.png")
