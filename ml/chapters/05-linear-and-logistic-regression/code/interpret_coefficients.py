"""Interpreting coefficients: units vs standardized, odds ratios in logistic regression, and a correlated-feature trap."""
import numpy as np
from sklearn.datasets import load_breast_cancer
from sklearn.linear_model import LinearRegression, LogisticRegression
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler


def main() -> None:
    rng = np.random.default_rng(0)
    # 1) House prices: size in m^2, age in years  -> price in $1000s
    n = 500
    size = rng.uniform(40, 200, n)
    age = rng.uniform(0, 50, n)
    price = 50 + 3.0 * size - 1.5 * age + rng.normal(0, 20, n)
    X = np.c_[size, age]
    raw = LinearRegression().fit(X, price)
    print("raw units      : price = %.1f + %.2f*size_m2 %+.2f*age_years" % (raw.intercept_, *raw.coef_))
    X_km = np.c_[size / 1e6, age]                     # size in km^2 instead of m^2
    print("size in km^2   : coef for size = %.0f  (same model, different units -> different number)"
          % LinearRegression().fit(X_km, price).coef_[0])
    std = make_pipeline(StandardScaler(), LinearRegression()).fit(X, price)
    print("standardized   : +1 std of size -> %+.1f, +1 std of age -> %+.1f  (comparable importance)"
          % tuple(std[-1].coef_))

    # 2) Logistic regression on breast-cancer data: coefficients are log-odds, e^coef is an odds ratio
    data = load_breast_cancer()
    clf = make_pipeline(StandardScaler(), LogisticRegression(C=0.1, max_iter=5000)).fit(data.data, data.target)
    coef = clf[-1].coef_[0]
    order = np.argsort(coef)
    print("\nbreast cancer (target 1 = benign), standardized features, C=0.1:")
    for i in list(order[:3]) + list(order[-2:]):
        print(f"  {data.feature_names[i]:<24} coef {coef[i]:+.2f}  odds ratio e^coef = {np.exp(coef[i]):.2f}")

    # 3) Correlated features: coefficients become unstable across resamples
    x1 = rng.normal(size=300)
    x2 = x1 + rng.normal(0, 0.05, 300)                # almost a copy of x1
    yy = 1.0 * x1 + 1.0 * x2 + rng.normal(0, 1, 300)
    print("\nbootstrap coefficients for two near-duplicate features (true: 1.0 and 1.0):")
    for b in range(3):
        idx = rng.integers(0, 300, 300)
        c = LinearRegression().fit(np.c_[x1, x2][idx], yy[idx]).coef_
        print(f"  resample {b}: w1 = {c[0]:+.2f}, w2 = {c[1]:+.2f}, sum = {c.sum():.2f}")


if __name__ == "__main__":
    main()
