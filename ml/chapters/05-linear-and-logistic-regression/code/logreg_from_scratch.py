"""Logistic regression from scratch (sigmoid, log-loss, gradient descent) vs scikit-learn, with a decision-boundary plot."""
import numpy as np
from sklearn.datasets import make_blobs
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, log_loss
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler

from plotstyle import C, plt, save


def sigmoid(z):
    return 1 / (1 + np.exp(-z))


def log_loss_np(y, p, eps=1e-12):
    p = np.clip(p, eps, 1 - eps)
    return -np.mean(y * np.log(p) + (1 - y) * np.log(1 - p))


def fit_logreg_gd(X, y, lr=0.5, epochs=5000, l2=0.0):
    """Batch gradient descent on mean log-loss (+ optional L2 on weights, not the bias)."""
    Xb = np.c_[np.ones(len(X)), X]
    theta = np.zeros(Xb.shape[1])
    for _ in range(epochs):
        p = sigmoid(Xb @ theta)
        grad = Xb.T @ (p - y) / len(y)          # same form as linear regression: X^T (prediction - y)
        grad[1:] += l2 * theta[1:]
        theta -= lr * grad
    return theta


def main() -> None:
    # 1) Worked numbers: sigmoid and the log-loss of single predictions
    for z in [-2, 0, 2]:
        print(f"sigmoid({z:+d}) = {sigmoid(z):.3f}")
    for y, p in [(1, 0.9), (1, 0.5), (1, 0.1), (0, 0.1)]:
        print(f"y={y}, p={p}: loss = {-(y * np.log(p) + (1 - y) * np.log(1 - p)):.3f}")

    # 2) Two-feature data, standardized
    X, y = make_blobs(n_samples=400, centers=[[-1, -0.5], [1, 0.8]], cluster_std=1.1, random_state=0)
    X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.25, random_state=0, stratify=y)
    sc = StandardScaler().fit(X_tr)
    X_tr, X_te = sc.transform(X_tr), sc.transform(X_te)

    theta = fit_logreg_gd(X_tr, y_tr)
    sk = LogisticRegression(C=np.inf).fit(X_tr, y_tr)          # C=inf -> no regularization (sklearn >= 1.8)
    p_gd = sigmoid(np.c_[np.ones(len(X_te)), X_te] @ theta)
    p_sk = sk.predict_proba(X_te)[:, 1]
    print("\nfrom scratch [b, w1, w2]:", theta.round(3))
    print("sklearn      [b, w1, w2]:", np.r_[sk.intercept_, sk.coef_[0]].round(3))
    print(f"test log-loss scratch {log_loss_np(y_te, p_gd):.4f} | sklearn {log_loss(y_te, p_sk):.4f}")
    print(f"test accuracy scratch {accuracy_score(y_te, p_gd >= 0.5):.3f} | sklearn {accuracy_score(y_te, p_sk >= 0.5):.3f}")

    # Odds interpretation of a coefficient
    w1 = sk.coef_[0][0]
    print(f"w1 = {w1:.3f}: +1 std in x1 multiplies the odds of class 1 by e^w1 = {np.exp(w1):.2f}")

    # 3) Plot: sigmoid + decision boundary with probability contours
    fig, axes = plt.subplots(1, 2, figsize=(11, 4))
    z = np.linspace(-8, 8, 300)
    axes[0].plot(z, sigmoid(z), color=C[0])
    axes[0].axhline(0.5, color="#8a8983", linewidth=1, linestyle="--")
    axes[0].axvline(0, color="#8a8983", linewidth=1, linestyle="--")
    axes[0].set(title="sigmoid: sigma(z) = 1 / (1 + e^-z)", xlabel="z = w.x + b", ylabel="predicted P(y=1)")

    ax = axes[1]
    g0, g1 = np.meshgrid(np.linspace(-3, 3, 300), np.linspace(-3, 3, 300))
    P = sk.predict_proba(np.c_[g0.ravel(), g1.ravel()])[:, 1].reshape(g0.shape)
    cs = ax.contourf(g0, g1, P, levels=np.linspace(0, 1, 11), cmap="RdBu_r", alpha=0.18)
    ax.contour(g0, g1, P, levels=[0.5], colors="#0b0b0b", linewidths=2)
    ax.scatter(*X_tr[y_tr == 0].T, s=14, color=C[0], edgecolor="white", linewidth=0.4, label="class 0")
    ax.scatter(*X_tr[y_tr == 1].T, s=14, color=C[1], edgecolor="white", linewidth=0.4, label="class 1")
    fig.colorbar(cs, ax=ax, label="P(class 1)")
    ax.set(xlim=(-3, 3), ylim=(-3, 3), xlabel="x1 (standardized)", ylabel="x2 (standardized)",
           title="Decision boundary: the line where p = 0.5")
    ax.grid(False)
    ax.legend(loc="upper left", markerscale=2, frameon=True)
    save(fig, "logreg_sigmoid_boundary.png")


if __name__ == "__main__":
    main()
