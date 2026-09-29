"""Anomaly detection: z-score / robust z-score, Isolation Forest, One-Class SVM, Local Outlier Factor."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.ensemble import IsolationForest
from sklearn.metrics import average_precision_score, roc_auc_score
from sklearn.neighbors import LocalOutlierFactor
from sklearn.svm import OneClassSVM

IMG = Path(__file__).resolve().parent.parent / "img"


def zscore_demo():
    amounts = np.array([42, 38, 55, 47, 51, 39, 44, 60, 48, 45, 950, 41], dtype=float)  # card payments ($)
    z = (amounts - amounts.mean()) / amounts.std()
    med = np.median(amounts)
    mad = np.median(np.abs(amounts - med))
    robust = 0.6745 * (amounts - med) / mad            # modified z-score (Iglewicz & Hoaglin)
    print("amount   z-score   robust z")
    for a, zz, r in zip(amounts, z, robust):
        flag = "  <- anomaly" if abs(r) > 3.5 else ""
        print(f"{a:6.0f}   {zz:7.2f}   {r:8.2f}{flag}")
    print(f"mean={amounts.mean():.1f} (dragged up by the outlier), median={med:.1f}")


def make_data(rng):
    inliers = np.vstack([rng.normal([0, 0], 0.5, (300, 2)),       # dense cluster
                         rng.normal([4, 4], 1.2, (300, 2))])       # sparse cluster
    outliers = rng.uniform(-3, 8, (30, 2))
    X = np.vstack([inliers, outliers])
    y = np.r_[np.zeros(600), np.ones(30)]                          # 1 = anomaly (used only to evaluate)
    return X, y


def compare_detectors():
    rng = np.random.default_rng(0)
    X, y = make_data(rng)
    detectors = {
        "Isolation Forest": IsolationForest(n_estimators=200, contamination=30 / 630, random_state=0),
        "One-Class SVM": OneClassSVM(kernel="rbf", gamma=0.3, nu=0.05),
        "LOF (k=20)": LocalOutlierFactor(n_neighbors=20, contamination=30 / 630),
    }
    fig, axes = plt.subplots(1, 3, figsize=(12, 3.8))
    print("\ndetector            ROC-AUC   avg precision   flagged   true anomalies caught")
    for ax, (name, det) in zip(axes, detectors.items()):
        pred = det.fit_predict(X)                                   # +1 inlier, -1 outlier
        score = -det.negative_outlier_factor_ if name.startswith("LOF") else -det.score_samples(X)
        flagged = pred == -1
        print(f"{name:18s}  {roc_auc_score(y, score):7.3f}   {average_precision_score(y, score):13.3f}"
              f"   {flagged.sum():7d}   {int((flagged & (y == 1)).sum()):2d} / 30")
        ax.scatter(X[:, 0], X[:, 1], c=np.where(flagged, "red", "lightgray"), s=10, edgecolors="none")
        ax.scatter(X[y == 1, 0], X[y == 1, 1], facecolors="none", edgecolors="k", s=40, linewidths=.7)
        ax.set_title(f"{name} (red = flagged, ring = true anomaly)", fontsize=8)
        ax.set_xticks([]); ax.set_yticks([])
    plt.tight_layout(); plt.savefig(IMG / "anomaly_detectors.png", dpi=120); plt.close()


def isolation_path_length():
    """Anomalies are isolated in fewer random splits."""
    rng = np.random.default_rng(1)
    X = np.vstack([rng.normal(0, 1, (500, 2)), [[6, 6]]])
    iso = IsolationForest(n_estimators=200, random_state=0).fit(X)
    s = iso.score_samples(X)          # = -(anomaly score); anomaly score near 1 = anomaly, ~0.5 = normal
    print(f"\nanomaly score of (6,6): {-s[-1]:.2f}   typical point (median): {np.median(-s[:-1]):.2f}")


if __name__ == "__main__":
    zscore_demo()
    compare_detectors()
    isolation_path_length()
