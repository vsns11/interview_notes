"""Global and local explanations for a gradient-boosted credit model:
  global - permutation importance, impurity importance, partial dependence
  local  - exact Shapley values (brute force, 5 features) and a LIME-style local surrogate
"""
from itertools import combinations
from math import factorial

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.ensemble import HistGradientBoostingClassifier
from sklearn.inspection import PartialDependenceDisplay, permutation_importance
from sklearn.linear_model import Ridge
from sklearn.model_selection import train_test_split

from credit_data import FEATURES, make_credit_data


def exact_shapley(f, x, background):
    """phi_i = sum over subsets S without i of |S|!(M-|S|-1)!/M! * [v(S+i) - v(S)],
    where v(S) = mean model output when features in S are fixed to x and the rest
    come from background rows ("interventional" Shapley, what KernelSHAP approximates)."""
    M = len(x)

    def v(S):
        Z = background.copy()
        Z[:, list(S)] = x[list(S)]
        return f(Z).mean()

    phi = np.zeros(M)
    for i in range(M):
        others = [j for j in range(M) if j != i]
        for size in range(M):
            for S in combinations(others, size):
                w = factorial(size) * factorial(M - size - 1) / factorial(M)
                phi[i] += w * (v(S + (i,)) - v(S))
    return phi, v(())


def lime_like(f, x, X_ref, n=5000, width=0.75, seed=0):
    """Perturb around x, weight samples by closeness, fit a weighted linear model."""
    rng = np.random.default_rng(seed)
    sd = X_ref.std(axis=0)
    Z = x + rng.normal(0, 1, (n, len(x))) * sd                  # samples near x
    d = np.sqrt((((Z - x) / sd) ** 2).sum(axis=1))
    w = np.exp(-(d ** 2) / width ** 2 / len(x))                 # closer = heavier
    lin = Ridge(alpha=1e-3).fit((Z - x) / sd, f(Z), sample_weight=w)
    return lin.coef_                                            # effect of +1 sd of each feature


def main():
    df = make_credit_data()
    X, y = df[FEATURES].to_numpy(float), df.y.to_numpy()
    Xtr, Xte, ytr, yte = train_test_split(X, y, test_size=0.3, random_state=0)
    model = HistGradientBoostingClassifier(max_iter=200, learning_rate=0.1, random_state=0).fit(Xtr, ytr)
    print(f"test accuracy: {model.score(Xte, yte):.3f}")

    # --- global: permutation importance on held-out data
    pi = permutation_importance(model, Xte, yte, scoring="roc_auc", n_repeats=10, random_state=0)
    print("\nPermutation importance (drop in test ROC-AUC when a column is shuffled):")
    for i in pi.importances_mean.argsort()[::-1]:
        print(f"  {FEATURES[i]:15s} {pi.importances_mean[i]:.4f} +/- {pi.importances_std[i]:.4f}")

    # --- global: partial dependence plots
    fig, ax = plt.subplots(1, 3, figsize=(11, 3.4))
    PartialDependenceDisplay.from_estimator(model, Xte, [0, 1, 4], feature_names=FEATURES,
                                            ax=ax, kind="both", subsample=300, random_state=0)
    fig.suptitle("Partial dependence (dashed average) + individual ICE curves (thin)")
    fig.tight_layout(); fig.savefig("../img/partial_dependence.png", dpi=130)

    # --- local: explain one applicant
    f = lambda Z: model.predict_proba(Z)[:, 1]
    rng = np.random.default_rng(0)
    background = Xtr[rng.choice(len(Xtr), 200, replace=False)]
    x = Xte[3]
    phi, base = exact_shapley(f, x, background)
    print("\nApplicant:", {k: float(v) for k, v in zip(FEATURES, x.round(2))})
    print(f"model score f(x) = {f(x[None])[0]:.3f}   baseline E[f] = {base:.3f}")
    print("Shapley values (push towards approval if +):")
    for name, p in sorted(zip(FEATURES, phi), key=lambda t: -abs(t[1])):
        print(f"  {name:15s} {p:+.3f}")
    print(f"check: baseline + sum(phi) = {base + phi.sum():.3f}  (equals f(x))")

    coef = lime_like(f, x, Xtr)
    print("\nLIME-style local slopes (score change per +1 sd):")
    for name, c in sorted(zip(FEATURES, coef), key=lambda t: -abs(t[1])):
        print(f"  {name:15s} {c:+.3f}")


if __name__ == "__main__":
    main()
