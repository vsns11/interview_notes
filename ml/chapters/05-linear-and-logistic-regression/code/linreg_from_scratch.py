"""Linear regression three ways: normal equation, batch gradient descent (numpy), and scikit-learn."""
import time

import numpy as np
from sklearn.datasets import load_diabetes
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_squared_error
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler


def add_bias(X: np.ndarray) -> np.ndarray:
    """Prepend a column of ones so the intercept b becomes weight w0."""
    return np.c_[np.ones(len(X)), X]


def normal_equation(X: np.ndarray, y: np.ndarray) -> np.ndarray:
    Xb = add_bias(X)
    # theta = (X^T X)^-1 X^T y ; solve() is more stable than an explicit inverse
    return np.linalg.solve(Xb.T @ Xb, Xb.T @ y)


def batch_gd(X, y, lr=0.1, epochs=1000):
    Xb = add_bias(X)
    m, d = Xb.shape
    theta = np.zeros(d)
    history = []
    for _ in range(epochs):
        err = Xb @ theta - y                   # prediction error for all m rows
        grad = (2 / m) * Xb.T @ err            # gradient of MSE
        theta -= lr * grad
        history.append((err ** 2).mean())
    return theta, history


def main() -> None:
    # 1) Tiny example with a known answer: y = 4 + 3x + noise
    rng = np.random.default_rng(42)
    x = 2 * rng.random((100, 1))
    y = 4 + 3 * x[:, 0] + rng.normal(0, 1, 100)
    print("normal equation  [b, w]:", normal_equation(x, y).round(3))
    theta, hist = batch_gd(x, y, lr=0.1, epochs=1000)
    print("batch GD         [b, w]:", theta.round(3), f"(MSE {hist[0]:.2f} -> {hist[-1]:.3f})")
    sk = LinearRegression().fit(x, y)
    print("sklearn          [b, w]:", np.r_[sk.intercept_, sk.coef_].round(3))

    # 2) Real data: diabetes progression, 10 features (already centred/scaled by sklearn), 442 patients
    X, y = load_diabetes(return_X_y=True)
    X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.2, random_state=0)
    sc = StandardScaler().fit(X_tr)
    X_tr_s, X_te_s = sc.transform(X_tr), sc.transform(X_te)

    t0 = time.perf_counter(); th_ne = normal_equation(X_tr_s, y_tr); t_ne = time.perf_counter() - t0
    t0 = time.perf_counter(); th_gd, h = batch_gd(X_tr_s, y_tr, lr=0.05, epochs=5000); t_gd = time.perf_counter() - t0
    skm = LinearRegression().fit(X_tr_s, y_tr)
    th_sk = np.r_[skm.intercept_, skm.coef_]

    def test_mse(th):
        return mean_squared_error(y_te, add_bias(X_te_s) @ th)

    print(f"\n{'method':<16}{'test MSE':>10}{'max |diff vs sklearn|':>24}{'time':>10}")
    print(f"{'normal eq.':<16}{test_mse(th_ne):>10.1f}{np.abs(th_ne - th_sk).max():>24.2e}{t_ne * 1e3:>8.2f}ms")
    print(f"{'batch GD':<16}{test_mse(th_gd):>10.1f}{np.abs(th_gd - th_sk).max():>24.2e}{t_gd * 1e3:>8.2f}ms")
    print(f"{'sklearn':<16}{test_mse(th_sk):>10.1f}{0:>24.2e}")
    print(f"baseline (predict train mean) test MSE: {mean_squared_error(y_te, np.full_like(y_te, y_tr.mean())):.1f}")
    print("GD after 5000 epochs, coef for s1, s2 (correlated pair):", th_gd[5:7].round(1),
          "exact:", th_sk[5:7].round(1))

    # 3) Interpreting coefficients (standardized features => effect of +1 std)
    names = load_diabetes().feature_names
    order = np.argsort(-np.abs(skm.coef_))
    print("\nlargest standardized effects (+1 std in feature -> change in disease score):")
    for i in order[:4]:
        print(f"  {names[i]:>4}: {skm.coef_[i]:+7.1f}")
    print("intercept = mean training target:", round(skm.intercept_, 1), "=", round(y_tr.mean(), 1))


if __name__ == "__main__":
    main()
