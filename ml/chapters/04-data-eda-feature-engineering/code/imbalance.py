"""Class imbalance: why accuracy lies, class weights, over/under-sampling, SMOTE (from scratch), recalibration."""
import os

os.environ.setdefault("OMP_NUM_THREADS", "2")  # keep the demo fast on busy machines

import numpy as np  # noqa: E402
from sklearn.datasets import make_classification
from sklearn.dummy import DummyClassifier
from sklearn.ensemble import HistGradientBoostingClassifier
from sklearn.metrics import (accuracy_score, average_precision_score, f1_score, precision_score,
                             recall_score, roc_auc_score)
from sklearn.model_selection import train_test_split
from sklearn.neighbors import NearestNeighbors


def Model(**kw):
    """Gradient-boosted trees: fast, non-linear, supports class_weight."""
    return HistGradientBoostingClassifier(max_iter=100, early_stopping=False, random_state=0, **kw)


def smote(X_min: np.ndarray, n_new: int, k: int = 5, seed: int = 0) -> np.ndarray:
    """Create n_new synthetic minority points on segments between a point and one of its k minority neighbours."""
    rng = np.random.default_rng(seed)
    nn = NearestNeighbors(n_neighbors=k + 1).fit(X_min)
    _, idx = nn.kneighbors(X_min)                 # idx[:, 0] is the point itself
    base = rng.integers(0, len(X_min), n_new)
    neigh = idx[base, rng.integers(1, k + 1, n_new)]
    gap = rng.random((n_new, 1))
    return X_min[base] + gap * (X_min[neigh] - X_min[base])


def report(name, model, X_te, y_te, threshold=0.5):
    p = model.predict_proba(X_te)[:, 1]
    yhat = (p >= threshold).astype(int)
    print(f"{name:<26}{accuracy_score(y_te, yhat):>6.3f}{precision_score(y_te, yhat, zero_division=0):>7.3f}"
          f"{recall_score(y_te, yhat):>7.3f}{f1_score(y_te, yhat):>7.3f}"
          f"{roc_auc_score(y_te, p):>8.3f}{average_precision_score(y_te, p):>8.3f}{p.mean():>9.4f}")
    return p


def main() -> None:
    X, y = make_classification(n_samples=20_000, n_features=10, n_informative=4, weights=[0.98],
                               flip_y=0.005, class_sep=0.8, random_state=0)
    X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.3, stratify=y, random_state=0)
    print(f"positive rate: train {y_tr.mean():.4f}, test {y_te.mean():.4f}  (about 1 in {1 / y_tr.mean():.0f})\n")
    print(f"{'method':<26}{'acc':>6}{'prec':>7}{'rec':>7}{'F1':>7}{'ROC-AUC':>8}{'PR-AUC':>8}{'mean p':>9}")

    report("majority-class baseline", DummyClassifier(strategy="most_frequent").fit(X_tr, y_tr), X_te, y_te)
    report("GBM, no fix", Model().fit(X_tr, y_tr), X_te, y_te)
    report("GBM class_weight=bal.", Model(class_weight="balanced").fit(X_tr, y_tr), X_te, y_te)

    rng = np.random.default_rng(0)
    pos, neg = np.flatnonzero(y_tr == 1), np.flatnonzero(y_tr == 0)
    # Random oversampling: repeat minority rows until balanced
    over = np.r_[neg, rng.choice(pos, len(neg), replace=True)]
    report("random oversampling", Model().fit(X_tr[over], y_tr[over]), X_te, y_te)
    # SMOTE: synthetic minority points
    X_syn = smote(X_tr[pos], len(neg) - len(pos))
    X_sm = np.r_[X_tr, X_syn]
    y_sm = np.r_[y_tr, np.ones(len(X_syn), dtype=int)]
    report("SMOTE (from scratch)", Model().fit(X_sm, y_sm), X_te, y_te)
    # Downsample negatives: keep all positives, 1 in 10 negatives
    w = 0.10
    keep = np.r_[pos, rng.choice(neg, int(len(neg) * w), replace=False)]
    m_down = Model().fit(X_tr[keep], y_tr[keep])
    p_s = report("downsample negatives 1:10", m_down, X_te, y_te)

    # Recalibrate: undo the prior shift caused by keeping only a fraction w of negatives
    p_corr = p_s * w / (p_s * w + (1 - p_s))
    print(f"\nafter downsampling, mean predicted p = {p_s.mean():.4f} vs true rate {y_te.mean():.4f}")
    print(f"after prior correction p' = p*w / (p*w + 1 - p): mean p' = {p_corr.mean():.4f}"
          f" (ranking unchanged: ROC-AUC {roc_auc_score(y_te, p_corr):.3f})")

    # Threshold moving: same plain model, lower threshold
    plain = Model().fit(X_tr, y_tr)
    for t in [0.5, 0.2, 0.1]:
        yhat = plain.predict_proba(X_te)[:, 1] >= t
        print(f"plain model, threshold {t:>4}: precision {precision_score(y_te, yhat, zero_division=0):.3f}"
              f"  recall {recall_score(y_te, yhat):.3f}")


if __name__ == "__main__":
    main()
