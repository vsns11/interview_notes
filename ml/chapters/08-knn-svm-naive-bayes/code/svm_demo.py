"""SVMs: maximum margin, support vectors, soft margin C, kernels (RBF / poly), gamma, SVR."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.calibration import CalibratedClassifierCV
from sklearn.datasets import make_blobs, make_circles, make_moons
from sklearn.model_selection import GridSearchCV, cross_val_score, train_test_split
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.svm import SVC, SVR

IMG = Path(__file__).resolve().parent.parent / "img"


def plot_svc(ax, model, X, y, title):
    xx, yy = np.meshgrid(np.linspace(X[:, 0].min() - .5, X[:, 0].max() + .5, 300),
                         np.linspace(X[:, 1].min() - .5, X[:, 1].max() + .5, 300))
    Z = model.decision_function(np.c_[xx.ravel(), yy.ravel()]).reshape(xx.shape)
    ax.contourf(xx, yy, Z > 0, alpha=.15, cmap="coolwarm")
    ax.contour(xx, yy, Z, levels=[-1, 0, 1], linestyles=["--", "-", "--"], colors="k", linewidths=1)
    ax.scatter(X[:, 0], X[:, 1], c=y, cmap="coolwarm", s=18, edgecolors="k", linewidths=.3)
    sv = model.support_vectors_ if hasattr(model, "support_vectors_") else model[-1].support_vectors_
    if not hasattr(model, "support_vectors_"):   # pipeline: map SVs back to original space
        sv = model[0].inverse_transform(sv)
    ax.scatter(sv[:, 0], sv[:, 1], s=80, facecolors="none", edgecolors="k", linewidths=1)
    ax.set_title(title, fontsize=9); ax.set_xticks([]); ax.set_yticks([])


def margin_and_C():
    X, y = make_blobs(n_samples=80, centers=2, cluster_std=1.3, random_state=6)
    fig, axes = plt.subplots(1, 2, figsize=(8, 3.5))
    print("linear SVM on 2 blobs")
    for ax, C in zip(axes, (100, 0.01)):
        m = SVC(kernel="linear", C=C).fit(X, y)
        w = m.coef_[0]
        print(f"  C={C:<6} support vectors={len(m.support_):3d}  margin width 2/||w|| = {2 / np.linalg.norm(w):.2f}"
              f"  train acc={m.score(X, y):.3f}")
        plot_svc(ax, m, X, y, f"C={C}: {len(m.support_)} support vectors (circled)")
    plt.tight_layout(); plt.savefig(IMG / "svm_margin_C.png", dpi=120); plt.close()


def kernels():
    X, y = make_moons(n_samples=200, noise=0.2, random_state=0)
    Xc, yc = make_circles(n_samples=200, noise=0.1, factor=0.4, random_state=0)
    print("\n5-fold CV accuracy      moons   circles")
    models = {
        "linear": SVC(kernel="linear"),
        "poly (degree 3)": SVC(kernel="poly", degree=3, coef0=1),
        "RBF": SVC(kernel="rbf"),
    }
    for name, m in models.items():
        p = make_pipeline(StandardScaler(), m)
        print(f"  {name:18s}  {cross_val_score(p, X, y, cv=5).mean():.3f}   "
              f"{cross_val_score(p, Xc, yc, cv=5).mean():.3f}")

    fig, axes = plt.subplots(1, 3, figsize=(11, 3.3))
    for ax, g in zip(axes, (0.1, 1, 50)):
        p = make_pipeline(StandardScaler(), SVC(kernel="rbf", gamma=g, C=1)).fit(X, y)
        plot_svc(ax, p, X, y, f"RBF gamma={g}: train acc {p.score(X, y):.2f}")
    plt.tight_layout(); plt.savefig(IMG / "svm_rbf_gamma.png", dpi=120); plt.close()

    grid = GridSearchCV(make_pipeline(StandardScaler(), SVC()),
                        {"svc__C": [0.1, 1, 10, 100], "svc__gamma": [0.01, 0.1, 1, 10]}, cv=5)
    grid.fit(X, y)
    print(f"\ngrid search best params {grid.best_params_}  CV acc {grid.best_score_:.3f}")


def kernel_trick_check():
    """Polynomial kernel (x.z)^2 equals a dot product in an explicit 3-D feature space."""
    x, z = np.array([1.0, 2.0]), np.array([3.0, 4.0])
    phi = lambda v: np.array([v[0] ** 2, np.sqrt(2) * v[0] * v[1], v[1] ** 2])
    print(f"\n(x.z)^2 = {(x @ z) ** 2:.1f}   phi(x).phi(z) = {phi(x) @ phi(z):.1f}")


def svr_demo():
    rng = np.random.RandomState(0)
    X = np.sort(5 * rng.rand(60, 1), axis=0)
    y = np.sin(X).ravel() + 0.1 * rng.randn(60)
    grid = np.linspace(0, 5, 300).reshape(-1, 1)
    plt.figure(figsize=(6, 3.3))
    for eps, c in ((0.1, "tab:blue"), (0.4, "tab:orange")):
        m = SVR(kernel="rbf", C=10, epsilon=eps).fit(X, y)
        pred = m.predict(grid)
        plt.plot(grid, pred, c=c, label=f"epsilon={eps}: {len(m.support_)} support vectors")
        plt.fill_between(grid.ravel(), pred - eps, pred + eps, color=c, alpha=.12)
        print(f"SVR epsilon={eps}: {len(m.support_)} of 60 points are support vectors")
    plt.scatter(X, y, s=12, c="k")
    plt.title("SVR: points inside the epsilon-tube cost nothing"); plt.legend(fontsize=8)
    plt.tight_layout(); plt.savefig(IMG / "svr_tube.png", dpi=120); plt.close()


def svm_probabilities():
    """SVC gives scores; wrap it in CalibratedClassifierCV to get probabilities
    (replaces SVC(probability=True), deprecated in scikit-learn 1.9)."""
    X, y = make_moons(n_samples=400, noise=0.3, random_state=0)
    X_tr, X_te, y_tr, y_te = train_test_split(X, y, random_state=0)
    svc = make_pipeline(StandardScaler(), SVC())
    cal = CalibratedClassifierCV(svc, method="sigmoid", ensemble=False).fit(X_tr, y_tr)
    print(f"\nscores  {np.round(svc.fit(X_tr, y_tr).decision_function(X_te[:3]), 2)}")
    print(f"P(y=1)  {np.round(cal.predict_proba(X_te[:3])[:, 1], 2)}")
    print(f"calibrated SVC test accuracy {cal.score(X_te, y_te):.3f}")


if __name__ == "__main__":
    margin_and_C()
    kernels()
    kernel_trick_check()
    svr_demo()
    svm_probabilities()
