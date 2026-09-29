"""Cross-validation: why one split is noisy, k-fold vs stratified vs time-series vs group, multiple metrics, nested CV."""
import numpy as np
from sklearn.datasets import load_breast_cancer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import (GridSearchCV, KFold, StratifiedKFold, TimeSeriesSplit, cross_val_score,
                                     cross_validate, train_test_split)
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler


def main() -> None:
    X, y = load_breast_cancer(return_X_y=True)
    model = make_pipeline(StandardScaler(), LogisticRegression(max_iter=1000))

    # 1) A single train/test split is a noisy estimate
    single = []
    for seed in range(20):
        X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.2, random_state=seed)
        single.append(model.fit(X_tr, y_tr).score(X_te, y_te))
    print(f"20 different 80/20 splits: accuracy from {min(single):.3f} to {max(single):.3f}")

    # 2) 5-fold cross-validation: every row is used for validation exactly once
    scores = cross_val_score(model, X, y, cv=5)
    print("5-fold CV scores:", scores.round(3), f"mean {scores.mean():.3f} +/- {scores.std():.3f}")

    # 3) Plain KFold on sorted labels vs StratifiedKFold
    order = np.argsort(y)
    Xs, ys = X[order], y[order]
    for name, cv in [("KFold (no shuffle), sorted y", KFold(5)),
                     ("StratifiedKFold", StratifiedKFold(5, shuffle=True, random_state=0))]:
        rates = [ys[va].mean() for _, va in cv.split(Xs, ys)]
        s = cross_val_score(model, Xs, ys, cv=cv)
        print(f"{name:<30} positive rate per fold {np.round(rates, 2)}  accuracy {s.mean():.3f}")

    # 4) TimeSeriesSplit with a gap (e.g. to respect label delay)
    for tr, va in TimeSeriesSplit(n_splits=3, gap=2, test_size=3).split(np.arange(15)):
        print(f"time split: train {tr.min()}..{tr.max()}  (gap)  validate {va.min()}..{va.max()}")

    # 5) Several metrics at once, plus train scores to spot overfitting
    res = cross_validate(model, X, y, cv=StratifiedKFold(5, shuffle=True, random_state=0),
                         scoring=["accuracy", "roc_auc", "f1"], return_train_score=True)
    for m in ["accuracy", "roc_auc", "f1"]:
        print(f"{m:<9} train {res['train_' + m].mean():.3f} | validation {res['test_' + m].mean():.3f}")

    # 6) Nested CV: inner loop tunes C, outer loop estimates the performance of the whole tuning procedure
    inner = GridSearchCV(model, {"logisticregression__C": [0.01, 0.1, 1, 10, 100]},
                         cv=StratifiedKFold(5, shuffle=True, random_state=1))
    outer = cross_val_score(inner, X, y, cv=StratifiedKFold(5, shuffle=True, random_state=2))
    inner.fit(X, y)
    print(f"non-nested best CV score {inner.best_score_:.3f} (optimistic) | nested CV {outer.mean():.3f}")


if __name__ == "__main__":
    main()
