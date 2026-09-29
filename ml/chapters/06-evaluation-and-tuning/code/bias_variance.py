"""Bias-variance decomposition by simulation, learning curves, and validation curves."""
import numpy as np
from sklearn.datasets import load_digits
from sklearn.model_selection import StratifiedKFold, learning_curve, validation_curve
from sklearn.naive_bayes import GaussianNB
from sklearn.neighbors import KNeighborsRegressor
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.svm import SVC
from sklearn.tree import DecisionTreeClassifier

from plotstyle import C, plt, save


def true_f(x):
    return np.sin(2 * np.pi * x)


def main() -> None:
    rng = np.random.default_rng(0)
    sigma = 0.3                                   # noise std -> irreducible error sigma^2 = 0.09
    x_test = np.linspace(0.05, 0.95, 50)
    ks = [40, 25, 15, 10, 6, 4, 2, 1]             # k-NN regression: small k = flexible, large k = rigid
    rows = []
    for k in ks:
        preds = []
        for _ in range(300):                      # 300 independent training sets of 50 points
            x = rng.uniform(0, 1, 50)
            y = true_f(x) + rng.normal(0, sigma, 50)
            preds.append(KNeighborsRegressor(n_neighbors=k).fit(x[:, None], y).predict(x_test[:, None]))
        preds = np.array(preds)
        bias2 = ((preds.mean(0) - true_f(x_test)) ** 2).mean()   # how far the AVERAGE model is from the truth
        var = preds.var(0).mean()                                 # how much models differ across training sets
        rows.append((k, bias2, var, bias2 + var + sigma ** 2))
    print(f"{'k':>4}{'bias^2':>9}{'variance':>10}{'expected test MSE':>19}")
    for k, b, v, t in rows:
        print(f"{k:>4}{b:>9.3f}{v:>10.3f}{t:>19.3f}")
    r = np.array(rows)
    fig, ax = plt.subplots(figsize=(6.5, 3.6))
    ax.plot(r[:, 0], r[:, 1], "-o", color=C[0], label="bias^2")
    ax.plot(r[:, 0], r[:, 2], "-o", color=C[1], label="variance")
    ax.plot(r[:, 0], r[:, 3], "-o", color=C[2], label="bias^2 + variance + noise")
    ax.axhline(sigma ** 2, color="#8a8983", linestyle="--", linewidth=1, label="irreducible noise")
    ax.set(xscale="log", xlabel="k neighbours (log scale): left = simpler, right = more flexible", ylabel="error",
           title="k-NN regression: bias grows with k, variance shrinks")
    ax.invert_xaxis()
    ax.set_xticks(ks, [str(k) for k in ks])
    ax.legend(fontsize=8)
    save(fig, "bias_variance.png")

    # Learning curves on handwritten digits: high-bias vs high-variance models
    X, y = load_digits(return_X_y=True)
    cv = StratifiedKFold(5, shuffle=True, random_state=0)
    fig, axes = plt.subplots(1, 2, figsize=(11, 3.8), sharey=True)
    for ax, (name, model) in zip(axes, [("Gaussian naive Bayes (high bias)", GaussianNB()),
                                        ("unpruned decision tree (high variance)", DecisionTreeClassifier(random_state=0))]):
        sizes, tr, va = learning_curve(model, X, y, cv=cv, train_sizes=np.linspace(0.1, 1.0, 6))
        ax.plot(sizes, tr.mean(1), "-o", color=C[0], label="training score")
        ax.plot(sizes, va.mean(1), "-o", color=C[1], label="validation score")
        ax.fill_between(sizes, va.mean(1) - va.std(1), va.mean(1) + va.std(1), color=C[1], alpha=0.15)
        ax.set(title=name, xlabel="training examples", ylabel="accuracy", ylim=(0.5, 1.02))
        ax.legend(loc="lower right")
        print(f"{name:<40} n={sizes[-1]}: train {tr.mean(1)[-1]:.3f}  validation {va.mean(1)[-1]:.3f}")
    save(fig, "learning_curves.png")

    # Validation curve: SVM RBF kernel width gamma
    gammas = np.logspace(-6, -1, 11)
    tr, va = validation_curve(make_pipeline(StandardScaler(), SVC()), X, y, param_name="svc__gamma",
                              param_range=gammas, cv=cv)
    best = gammas[va.mean(1).argmax()]
    print(f"\nvalidation curve: best gamma = {best:.0e}, validation acc {va.mean(1).max():.3f}")
    for g, a, b in zip(gammas[::2], tr.mean(1)[::2], va.mean(1)[::2]):
        print(f"  gamma {g:.0e}: train {a:.3f}  validation {b:.3f}")
    fig, ax = plt.subplots(figsize=(6.5, 3.6))
    ax.plot(gammas, tr.mean(1), "-o", color=C[0], label="training score")
    ax.plot(gammas, va.mean(1), "-o", color=C[1], label="validation score")
    ax.axvline(best, color="#8a8983", linestyle="--", linewidth=1)
    ax.set(xscale="log", xlabel="gamma (larger = more complex boundary)", ylabel="accuracy",
           title="Validation curve: underfit (left), sweet spot, overfit (right)")
    ax.legend(loc="lower left")
    save(fig, "validation_curve.png")


if __name__ == "__main__":
    main()
