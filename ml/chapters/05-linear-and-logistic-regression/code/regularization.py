"""Ridge (L2), Lasso (L1), ElasticNet: coefficient paths, sparsity, smoothing an overfit polynomial, choosing alpha by CV."""
import warnings

import numpy as np
from sklearn.datasets import load_diabetes
from sklearn.exceptions import ConvergenceWarning
from sklearn.linear_model import ElasticNetCV, Lasso, LassoCV, LinearRegression, Ridge, RidgeCV
from sklearn.metrics import mean_squared_error
from sklearn.model_selection import train_test_split
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import PolynomialFeatures, StandardScaler

from plotstyle import C, plt, save

warnings.filterwarnings("ignore", category=ConvergenceWarning)


def main() -> None:
    data = load_diabetes()
    X = StandardScaler().fit_transform(data.data)
    y = data.target
    names = data.feature_names

    # 1) Coefficient paths as regularization strength grows
    alphas = np.logspace(-2, 4, 60)
    ridge_path = np.array([Ridge(alpha=a).fit(X, y).coef_ for a in alphas])
    lasso_alphas = np.logspace(-2, 2, 60)
    lasso_path = np.array([Lasso(alpha=a, max_iter=50_000).fit(X, y).coef_ for a in lasso_alphas])
    fig, axes = plt.subplots(1, 2, figsize=(11, 3.8))
    highlight = {"s5": C[0], "bmi": C[1], "s1": C[2], "bp": C[6]}
    for ax, A, P, title in [(axes[0], alphas, ridge_path, "Ridge (L2): all shrink smoothly, none hit 0"),
                            (axes[1], lasso_alphas, lasso_path, "Lasso (L1): coefficients drop to exactly 0")]:
        for j, nm in enumerate(names):
            ax.plot(A, P[:, j], color=highlight.get(nm, "#c9c8c2"), linewidth=2 if nm in highlight else 1,
                    label=nm if nm in highlight else None)
        ax.set(xscale="log", xlabel="alpha (regularization strength)", ylabel="coefficient", title=title)
        ax.axhline(0, color="#0b0b0b", linewidth=0.8)
    axes[1].legend(loc="upper right", fontsize=8)
    save(fig, "regularization_paths.png")

    for a in [0.1, 1, 5, 20]:
        nz = (Lasso(alpha=a, max_iter=50_000).fit(X, y).coef_ != 0).sum()
        print(f"Lasso alpha={a:<4}: {nz:>2} of 10 coefficients non-zero")

    # 2) Correlated duplicate feature: Ridge splits, Lasso picks one
    X_dup = np.c_[X[:, [2]], X[:, [2]] + np.random.default_rng(0).normal(0, 0.01, (len(X), 1))]   # bmi twice
    print("\nbmi duplicated -> OLS:", LinearRegression().fit(X_dup, y).coef_.round(1),
          " Ridge(1):", Ridge(alpha=1).fit(X_dup, y).coef_.round(1),
          " Lasso(1):", Lasso(alpha=1).fit(X_dup, y).coef_.round(1))

    # 3) Choosing alpha with cross-validation, then comparing on a test set.
    #    10 features + all squares/pairwise products = 65 features for ~330 training rows: easy to overfit.
    X_tr, X_te, y_tr, y_te = train_test_split(data.data, y, test_size=0.25, random_state=1)
    poly = PolynomialFeatures(2, include_bias=False)
    models = {
        "OLS": make_pipeline(poly, StandardScaler(), LinearRegression()),
        "RidgeCV": make_pipeline(poly, StandardScaler(), RidgeCV(alphas=np.logspace(-3, 3, 50))),
        "LassoCV": make_pipeline(poly, StandardScaler(), LassoCV(cv=5, random_state=0)),
        "ElasticNetCV": make_pipeline(poly, StandardScaler(),
                                      ElasticNetCV(l1_ratio=[0.2, 0.5, 0.8], cv=5, random_state=0)),
    }
    print(f"\n{'model':<14}{'test MSE':>9}{'alpha':>9}{'l1_ratio':>9}{'non-zero':>9}")
    for name, m in models.items():
        m.fit(X_tr, y_tr)
        est = m[-1]
        alpha = getattr(est, "alpha_", float("nan"))
        l1r = getattr(est, "l1_ratio_", float("nan"))
        print(f"{name:<14}{mean_squared_error(y_te, m.predict(X_te)):>9.0f}{alpha:>9.3f}{l1r:>9}"
              f"{(est.coef_ != 0).sum():>9}")

    # 4) Ridge tames a degree-15 polynomial
    rng = np.random.default_rng(0)
    x = rng.uniform(-3, 3, 40)
    yy = 0.5 * x ** 2 + x + 2 + rng.normal(0, 1, 40)
    grid = np.linspace(-3, 3, 300)[:, None]
    fig, ax = plt.subplots(figsize=(7, 3.6))
    ax.scatter(x, yy, s=14, color="#8a8983", zorder=3, label="training data")
    for a, col in [(1e-6, C[1]), (1, C[0]), (100, C[2])]:
        m = make_pipeline(PolynomialFeatures(15, include_bias=False), StandardScaler(), Ridge(alpha=a)).fit(x[:, None], yy)
        ax.plot(grid, m.predict(grid), color=col, label=f"Ridge alpha={a:g}")
    ax.set(ylim=(-2, 12), xlabel="x", ylabel="y", title="Degree-15 polynomial: larger alpha = smoother curve")
    ax.legend(loc="upper left")
    save(fig, "ridge_polynomial.png")


if __name__ == "__main__":
    main()
