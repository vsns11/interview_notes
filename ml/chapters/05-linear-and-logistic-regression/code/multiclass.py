"""Multiclass logistic regression: one-vs-rest vs softmax (multinomial), plus softmax regression from scratch."""
import numpy as np
from matplotlib.colors import ListedColormap
from sklearn.datasets import load_iris
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split
from sklearn.multiclass import OneVsRestClassifier
from sklearn.preprocessing import StandardScaler

from plotstyle import C, plt, save


def softmax(Z):
    Z = Z - Z.max(axis=1, keepdims=True)        # subtract row max: same result, no overflow
    E = np.exp(Z)
    return E / E.sum(axis=1, keepdims=True)


def fit_softmax_gd(X, y, k, lr=0.5, epochs=3000, l2=0.0):
    Xb = np.c_[np.ones(len(X)), X]
    W = np.zeros((Xb.shape[1], k))
    Y = np.eye(k)[y]                            # one-hot targets
    for _ in range(epochs):
        P = softmax(Xb @ W)
        grad = Xb.T @ (P - Y) / len(y)          # cross-entropy gradient: X^T (P - Y)
        grad[1:] += l2 * W[1:]
        W -= lr * grad
    return W


def main() -> None:
    z = np.array([[2.0, 1.0, 0.1]])
    print("softmax([2, 1, 0.1]) =", softmax(z).round(3), " sum =", softmax(z).sum().round(3))

    iris = load_iris()
    X = iris.data[:, 2:4]                       # petal length, petal width (2-D so we can plot)
    y = iris.target
    X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.3, stratify=y, random_state=0)
    sc = StandardScaler().fit(X_tr)
    X_tr, X_te = sc.transform(X_tr), sc.transform(X_te)

    softmax_sk = LogisticRegression(C=10).fit(X_tr, y_tr)                  # multinomial by default
    ovr = OneVsRestClassifier(LogisticRegression(C=10)).fit(X_tr, y_tr)     # 3 binary models
    W = fit_softmax_gd(X_tr, y_tr, k=3, l2=1 / (10 * len(y_tr)))           # same penalty as C=10
    P_scratch = softmax(np.c_[np.ones(len(X_te)), X_te] @ W)

    print(f"\ntest accuracy  softmax(sklearn) {accuracy_score(y_te, softmax_sk.predict(X_te)):.3f}"
          f" | OvR {accuracy_score(y_te, ovr.predict(X_te)):.3f}"
          f" | softmax(scratch) {accuracy_score(y_te, P_scratch.argmax(1)):.3f}")
    row = X_te[[0]]
    print("one flower, P(setosa, versicolor, virginica):")
    print("  softmax sklearn :", softmax_sk.predict_proba(row).round(3))
    print("  softmax scratch :", P_scratch[0].round(3))
    raw = np.array([est.predict_proba(row)[0, 1] for est in ovr.estimators_])
    print("  OvR raw scores  :", raw.round(3), f"(sum {raw.sum():.3f}) -> normalized", ovr.predict_proba(row).round(3))
    print("sklearn softmax coef_ shape:", softmax_sk.coef_.shape, "(one weight vector per class)")

    # Decision regions
    g0, g1 = np.meshgrid(np.linspace(-2.2, 2.2, 300), np.linspace(-2.2, 2.2, 300))
    G = np.c_[g0.ravel(), g1.ravel()]
    fig, axes = plt.subplots(1, 2, figsize=(11, 4))
    cmap = ListedColormap([C[0], C[1], C[2]])
    for ax, model, title in [(axes[0], softmax_sk, "softmax (multinomial)"), (axes[1], ovr, "one-vs-rest")]:
        ax.contourf(g0, g1, model.predict(G).reshape(g0.shape), levels=[-0.5, 0.5, 1.5, 2.5], cmap=cmap, alpha=0.18)
        for k, name in enumerate(iris.target_names):
            ax.scatter(*X_tr[y_tr == k].T, s=16, color=C[k], edgecolor="white", linewidth=0.4, label=name)
        ax.set(title=title, xlabel="petal length (std.)", ylabel="petal width (std.)")
        ax.grid(False)
        ax.legend(loc="upper left", markerscale=1.5, frameon=True)
    save(fig, "multiclass_regions.png")


if __name__ == "__main__":
    main()
