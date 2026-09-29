"""Decision trees: fitting, reading the rules, depth vs overfitting, pruning, regression trees."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.datasets import load_breast_cancer, load_iris
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeClassifier, DecisionTreeRegressor, export_text

IMG = Path(__file__).resolve().parent.parent / "img"
IMG.mkdir(exist_ok=True)


def read_the_rules():
    iris = load_iris()
    tree = DecisionTreeClassifier(max_depth=2, random_state=42).fit(iris.data, iris.target)
    print(export_text(tree, feature_names=list(iris.feature_names)))


def depth_curve():
    X, y = load_breast_cancer(return_X_y=True)
    X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.3, random_state=0, stratify=y)
    depths = range(1, 16)
    tr, te = [], []
    for d in depths:
        m = DecisionTreeClassifier(max_depth=d, random_state=0).fit(X_tr, y_tr)
        tr.append(m.score(X_tr, y_tr))
        te.append(m.score(X_te, y_te))
    print("depth  train  test")
    for d in (1, 2, 3, 4, 6, 10, 15):
        print(f"{d:5d}  {tr[d-1]:.3f}  {te[d-1]:.3f}")
    plt.figure(figsize=(6, 3.5))
    plt.plot(depths, tr, "o-", label="train accuracy")
    plt.plot(depths, te, "s-", label="test accuracy")
    plt.xlabel("max_depth"); plt.ylabel("accuracy"); plt.title("Deeper tree = lower bias, higher variance")
    plt.legend(); plt.grid(alpha=.3); plt.tight_layout()
    plt.savefig(IMG / "tree_depth_curve.png", dpi=120); plt.close()

    # Cost-complexity (post-)pruning: grow a full tree, then prune with ccp_alpha
    full = DecisionTreeClassifier(random_state=0).fit(X_tr, y_tr)
    path = full.cost_complexity_pruning_path(X_tr, y_tr)
    print(f"\nfull tree: {full.get_n_leaves()} leaves, test acc {full.score(X_te, y_te):.3f}")
    for a in (0.0, 0.005, 0.01, 0.02, 0.05):
        m = DecisionTreeClassifier(random_state=0, ccp_alpha=a).fit(X_tr, y_tr)
        print(f"ccp_alpha={a:<5}  leaves={m.get_n_leaves():3d}  test acc={m.score(X_te, y_te):.3f}")
    print(f"(pruning path has {len(path.ccp_alphas)} candidate alphas)")


def regression_tree():
    rng = np.random.RandomState(1)
    X = np.sort(5 * rng.rand(80, 1), axis=0)
    y = np.sin(X).ravel() + 0.2 * rng.randn(80)
    grid = np.linspace(0, 5, 500).reshape(-1, 1)
    plt.figure(figsize=(6, 3.5))
    plt.scatter(X, y, s=12, c="gray", label="data")
    for d, style in ((2, "-"), (5, "--")):
        m = DecisionTreeRegressor(max_depth=d, random_state=0).fit(X, y)
        plt.plot(grid, m.predict(grid), style, lw=2, label=f"max_depth={d}")
    plt.title("Regression tree = piecewise-constant prediction")
    plt.legend(); plt.tight_layout()
    plt.savefig(IMG / "regression_tree.png", dpi=120); plt.close()
    m = DecisionTreeRegressor(max_depth=2, random_state=0).fit(X, y)
    print("\nregression tree (depth 2) leaf values = mean of y in each leaf:")
    print(export_text(m, feature_names=["x"], decimals=2))


if __name__ == "__main__":
    read_the_rules()
    depth_curve()
    regression_tree()
