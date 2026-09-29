"""Train/validation/test splits, stratification, time-based and group splits, and three leakage demos."""
import numpy as np
from sklearn.feature_selection import SelectKBest, f_classif
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import roc_auc_score
from sklearn.model_selection import GroupKFold, TimeSeriesSplit, cross_val_score, train_test_split
from sklearn.pipeline import make_pipeline

from plotstyle import C, plt, save


def main() -> None:
    rng = np.random.default_rng(0)

    # 1) Train / validation / test = 60 / 20 / 20 with two calls, stratified on a 5%-positive label
    y = (rng.random(1000) < 0.05).astype(int)
    X = rng.normal(size=(1000, 3))
    X_tmp, X_test, y_tmp, y_test = train_test_split(X, y, test_size=0.2, stratify=y, random_state=0)
    X_train, X_val, y_train, y_val = train_test_split(X_tmp, y_tmp, test_size=0.25, stratify=y_tmp, random_state=0)
    print("sizes train/val/test:", len(y_train), len(y_val), len(y_test))
    print("positive rate  all/train/val/test: %.3f %.3f %.3f %.3f" % (y.mean(), y_train.mean(), y_val.mean(), y_test.mean()))
    shares = [train_test_split(X, y, test_size=0.2, random_state=s)[3].mean() for s in range(200)]
    print("WITHOUT stratify, test positive rate over 200 random splits: min %.3f  max %.3f" % (min(shares), max(shares)))

    # 2) Time-based split: every validation fold is strictly after its training data
    tss = TimeSeriesSplit(n_splits=4)
    fig, ax = plt.subplots(figsize=(7, 2.4))
    for k, (tr, va) in enumerate(tss.split(np.arange(20))):
        print(f"fold {k}: train t={tr.min()}..{tr.max()}  validate t={va.min()}..{va.max()}")
        ax.barh(k, len(tr), left=0, color=C[0], height=0.6, label="train" if k == 0 else None)
        ax.barh(k, len(va), left=va.min(), color=C[1], height=0.6, label="validate" if k == 0 else None)
    ax.set(yticks=range(4), yticklabels=[f"fold {k}" for k in range(4)], xlabel="time index",
           title="TimeSeriesSplit: expanding window, always validate on the future")
    ax.invert_yaxis()
    ax.legend(loc="lower right", ncol=2)
    save(fig, "time_series_split.png")

    # 3) Group split: the same customer never appears on both sides
    groups = np.repeat(np.arange(5), 2)          # 5 customers, 2 rows each
    for tr, va in list(GroupKFold(n_splits=5).split(np.zeros(10), groups=groups))[:2]:
        print("GroupKFold val customers:", sorted(int(g) for g in set(groups[va])), " train customers:", sorted(int(g) for g in set(groups[tr])))

    # ---------------- Leakage demos ----------------
    # (a) Feature selection BEFORE cross-validation on pure noise
    Xn = rng.normal(size=(100, 5000))
    yn = rng.integers(0, 2, 100)
    leaky_X = SelectKBest(f_classif, k=20).fit_transform(Xn, yn)      # peeked at ALL labels
    leaky = cross_val_score(LogisticRegression(max_iter=1000), leaky_X, yn, cv=5).mean()
    honest = cross_val_score(make_pipeline(SelectKBest(f_classif, k=20), LogisticRegression(max_iter=1000)),
                             Xn, yn, cv=5).mean()
    print(f"\n(a) pure-noise data: select-then-CV accuracy = {leaky:.2f} | select-inside-CV = {honest:.2f}")

    # (b) A label-adjacent feature: 'refund_issued' is only filled in AFTER the customer churned
    n = 3000
    x_real = rng.normal(size=(n, 2))
    y2 = (rng.random(n) < 1 / (1 + np.exp(-(x_real[:, 0] - 2)))).astype(int)
    refund = np.where(y2 == 1, rng.random(n) < 0.9, rng.random(n) < 0.02).astype(float)
    Xtr, Xte, ytr, yte, rtr, rte = train_test_split(x_real, y2, refund, test_size=0.3, random_state=0)
    m_leak = LogisticRegression().fit(np.c_[Xtr, rtr], ytr)
    m_ok = LogisticRegression().fit(Xtr, ytr)
    print("(b) with refund_issued: offline AUC %.3f | without: %.3f | with it at serving time (field is empty): %.3f"
          % (roc_auc_score(yte, m_leak.predict_proba(np.c_[Xte, rte])[:, 1]),
             roc_auc_score(yte, m_ok.predict_proba(Xte)[:, 1]),
             roc_auc_score(yte, m_leak.predict_proba(np.c_[Xte, np.zeros(len(yte))])[:, 1])))
    rec_off = (m_leak.predict(np.c_[Xte, rte])[yte == 1] == 1).mean()
    rec_live = (m_leak.predict(np.c_[Xte, np.zeros(len(yte))])[yte == 1] == 1).mean()
    print(f"    recall at threshold 0.5: offline {rec_off:.2f} -> in production {rec_live:.2f}")

    # (c) Random split vs time split when the world drifts
    t = np.arange(4000)
    xs = rng.normal(size=(4000, 1)) + (t[:, None] / 4000) * 2.0           # feature drifts upward
    w = np.where(t < 3000, 1.5, 0.2)                                       # relationship weakens late
    ys = (rng.random(4000) < 1 / (1 + np.exp(-(w * (xs[:, 0] - 1))))).astype(int)
    a, b = train_test_split(t, test_size=0.25, random_state=0)
    rand_auc = roc_auc_score(ys[b], LogisticRegression().fit(xs[a], ys[a]).predict_proba(xs[b])[:, 1])
    past, future = t < 3000, t >= 3000
    time_auc = roc_auc_score(ys[future], LogisticRegression().fit(xs[past], ys[past]).predict_proba(xs[future])[:, 1])
    print(f"(c) drifting data: random-split AUC {rand_auc:.3f} | train-on-past, test-on-future AUC {time_auc:.3f}")


if __name__ == "__main__":
    main()
