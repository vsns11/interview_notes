"""Bagging and Random Forests: variance reduction, OOB score, and feature-importance pitfalls."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.datasets import load_breast_cancer
from sklearn.ensemble import BaggingClassifier, RandomForestClassifier
from sklearn.inspection import permutation_importance
from sklearn.model_selection import cross_val_score, train_test_split
from sklearn.tree import DecisionTreeClassifier

IMG = Path(__file__).resolve().parent.parent / "img"


def compare_models():
    X, y = load_breast_cancer(return_X_y=True)
    models = {
        "single tree": DecisionTreeClassifier(random_state=0),
        "bagged trees (100)": BaggingClassifier(DecisionTreeClassifier(), n_estimators=100, random_state=0),
        "random forest (100)": RandomForestClassifier(n_estimators=100, random_state=0),
    }
    print("5-fold CV accuracy (mean +/- std)")
    for name, m in models.items():
        s = cross_val_score(m, X, y, cv=5)
        print(f"  {name:20s} {s.mean():.3f} +/- {s.std():.3f}")


def oob_demo():
    X, y = load_breast_cancer(return_X_y=True)
    X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.3, random_state=0, stratify=y)
    rf = RandomForestClassifier(n_estimators=300, oob_score=True, random_state=0, n_jobs=-1)
    rf.fit(X_tr, y_tr)
    print(f"\nOOB accuracy  = {rf.oob_score_:.3f}  (free validation estimate, no hold-out used)")
    print(f"test accuracy = {rf.score(X_te, y_te):.3f}")
    # fraction of rows NOT in one bootstrap sample ~ (1 - 1/n)^n -> 1/e
    n = len(X_tr)
    print(f"expected out-of-bag fraction per tree = {(1 - 1/n) ** n:.3f}  (1/e = {np.exp(-1):.3f})")


def importance_pitfall():
    """Impurity importance favours high-cardinality features, even pure noise."""
    rng = np.random.RandomState(0)
    X, y = load_breast_cancer(return_X_y=True)
    X = X[:, :5]  # keep 5 real features for a readable table
    names = ["mean radius", "mean texture", "mean perimeter", "mean area", "mean smoothness"]
    noise_cont = rng.randn(len(X), 1)                    # continuous random noise (many unique values)
    noise_bin = rng.randint(0, 2, size=(len(X), 1))      # binary random noise (2 values)
    X = np.hstack([X, noise_cont, noise_bin])
    names += ["RANDOM continuous", "RANDOM binary"]
    X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.3, random_state=0, stratify=y)
    rf = RandomForestClassifier(n_estimators=300, random_state=0, n_jobs=-1).fit(X_tr, y_tr)
    perm = permutation_importance(rf, X_te, y_te, n_repeats=20, random_state=0, n_jobs=-1)
    print("\nfeature              impurity(MDI)  permutation(test)")
    for i in np.argsort(-rf.feature_importances_):
        print(f"  {names[i]:18s} {rf.feature_importances_[i]:12.3f}  "
              f"{perm.importances_mean[i]:8.3f} +/- {perm.importances_std[i]:.3f}")

    order = np.argsort(rf.feature_importances_)
    fig, ax = plt.subplots(1, 2, figsize=(9, 3.5))
    ax[0].barh(np.array(names)[order], rf.feature_importances_[order], color="tab:blue")
    ax[0].set_title("Impurity-based (MDI), train data")
    ax[1].barh(np.array(names)[order], perm.importances_mean[order],
               xerr=perm.importances_std[order], color="tab:orange")
    ax[1].set_title("Permutation importance, test data")
    plt.tight_layout(); plt.savefig(IMG / "importance_pitfall.png", dpi=120); plt.close()


if __name__ == "__main__":
    compare_models()
    oob_demo()
    importance_pitfall()
