"""Boosting: gradient boosting from scratch on residuals, learning rate, AdaBoost, HistGradientBoosting."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.datasets import load_breast_cancer, make_regression
from sklearn.ensemble import (AdaBoostClassifier, GradientBoostingRegressor,
                              HistGradientBoostingClassifier, RandomForestClassifier)
from sklearn.metrics import mean_squared_error
from sklearn.model_selection import cross_val_score, train_test_split
from sklearn.tree import DecisionTreeRegressor

IMG = Path(__file__).resolve().parent.parent / "img"


class TinyGradientBoosting:
    """Gradient boosting for squared error: each tree fits the current residuals."""

    def __init__(self, n_estimators=100, learning_rate=0.1, max_depth=2):
        self.n, self.lr, self.depth = n_estimators, learning_rate, max_depth

    def fit(self, X, y):
        self.f0 = y.mean()                       # step 0: constant prediction
        pred = np.full(len(y), self.f0)
        self.trees = []
        for _ in range(self.n):
            residual = y - pred                  # = negative gradient of 1/2 (y - pred)^2
            t = DecisionTreeRegressor(max_depth=self.depth, random_state=0).fit(X, residual)
            pred += self.lr * t.predict(X)       # take a small step toward the residuals
            self.trees.append(t)
        return self

    def predict(self, X):
        return self.f0 + self.lr * sum(t.predict(X) for t in self.trees)


def worked_example():
    # 4 houses: size (100 m2) -> price (k$)
    X = np.array([[1.0], [1.5], [2.0], [3.0]])
    y = np.array([200.0, 260.0, 300.0, 420.0])
    lr = 0.5
    pred = np.full(4, y.mean())
    print(f"round 0: predict mean = {y.mean():.1f} for every house")
    for r in range(1, 4):
        res = y - pred
        stump = DecisionTreeRegressor(max_depth=1).fit(X, res)
        pred = pred + lr * stump.predict(X)
        print(f"round {r}: residuals {np.round(res, 1)} -> new preds {np.round(pred, 1)}  "
              f"MSE={mean_squared_error(y, pred):.1f}")


def scratch_vs_sklearn():
    X, y = make_regression(n_samples=600, n_features=8, noise=10, random_state=0)
    X_tr, X_te, y_tr, y_te = train_test_split(X, y, random_state=0)
    mine = TinyGradientBoosting(n_estimators=200, learning_rate=0.1, max_depth=3).fit(X_tr, y_tr)
    sk = GradientBoostingRegressor(n_estimators=200, learning_rate=0.1, max_depth=3,
                                   random_state=0).fit(X_tr, y_tr)
    print(f"\nfrom-scratch GB test RMSE = {mean_squared_error(y_te, mine.predict(X_te)) ** .5:.2f}")
    print(f"sklearn GB      test RMSE = {mean_squared_error(y_te, sk.predict(X_te)) ** .5:.2f}")

    # learning-rate curve using staged_predict
    plt.figure(figsize=(6, 3.5))
    for lr in (1.0, 0.3, 0.1, 0.03):
        m = GradientBoostingRegressor(n_estimators=400, learning_rate=lr, max_depth=3,
                                      random_state=0).fit(X_tr, y_tr)
        errs = [mean_squared_error(y_te, p) ** .5 for p in m.staged_predict(X_te)]
        plt.plot(range(1, 401), errs, label=f"learning_rate={lr} (best {min(errs):.1f} @ {np.argmin(errs)+1})")
    plt.yscale("log"); plt.xlabel("number of trees"); plt.ylabel("test RMSE (log)")
    plt.title("Smaller learning rate: needs more trees, often ends lower")
    plt.legend(fontsize=8); plt.grid(alpha=.3); plt.tight_layout()
    plt.savefig(IMG / "learning_rate_curve.png", dpi=120); plt.close()


def classifier_comparison():
    X, y = load_breast_cancer(return_X_y=True)
    models = {
        "AdaBoost (stumps)": AdaBoostClassifier(n_estimators=200, random_state=0),
        "RandomForest": RandomForestClassifier(n_estimators=200, random_state=0, n_jobs=-1),
        "HistGradientBoosting": HistGradientBoostingClassifier(random_state=0),
    }
    print("\n5-fold CV accuracy")
    for name, m in models.items():
        s = cross_val_score(m, X, y, cv=5)
        print(f"  {name:22s} {s.mean():.3f} +/- {s.std():.3f}")


if __name__ == "__main__":
    worked_example()
    scratch_vs_sklearn()
    classifier_comparison()
