"""Batch vs online learning, and instance-based vs model-based learning."""
import time

import numpy as np
from sklearn.datasets import make_classification
from sklearn.linear_model import LinearRegression, SGDClassifier
from sklearn.neighbors import KNeighborsRegressor

SEED = 0


def batch_vs_online() -> None:
    X, y = make_classification(n_samples=20_000, n_features=20, random_state=SEED)
    X_test, y_test = X[-4000:], y[-4000:]

    # Batch: train once on everything available.
    batch = SGDClassifier(average=True, random_state=SEED).fit(X[:16_000], y[:16_000])
    print(f"batch model (16,000 rows at once): test acc = {batch.score(X_test, y_test):.3f}")

    # Online: data arrives in mini-batches; the model updates with partial_fit and never stores old data.
    online = SGDClassifier(average=True, random_state=SEED)
    for i, start in enumerate(range(0, 16_000, 2000)):
        chunk = slice(start, start + 2000)
        online.partial_fit(X[chunk], y[chunk], classes=[0, 1])  # classes needed on the first call
        if i in (0, 3, 7):
            print(f"online after {start + 2000:>6,} rows: test acc = {online.score(X_test, y_test):.3f}")


def instance_vs_model() -> None:
    rng = np.random.default_rng(SEED)
    X = rng.uniform(0, 10, size=(5000, 1))
    y = 3 * X[:, 0] + 2 + rng.normal(0, 1, 5000)

    lin = LinearRegression().fit(X, y)            # model-based: compress data into 2 numbers
    knn = KNeighborsRegressor(n_neighbors=5).fit(X, y)  # instance-based: keep all 5000 rows

    x_new = np.array([[4.0], [15.0]])             # 15 is outside the training range 0..10
    t0 = time.perf_counter(); lin.predict(np.repeat(x_new, 5000, axis=0)); t_lin = time.perf_counter() - t0
    t0 = time.perf_counter(); knn.predict(np.repeat(x_new, 5000, axis=0)); t_knn = time.perf_counter() - t0
    print(f"linear model: w={lin.coef_[0]:.2f}, b={lin.intercept_:.2f} -> predicts {lin.predict(x_new).round(1)}")
    print(f"k-NN (k=5): stores {knn.n_samples_fit_} rows -> predicts {knn.predict(x_new).round(1)}")
    print(f"prediction time for 10,000 queries: linear {t_lin*1000:.2f} ms vs k-NN {t_knn*1000:.1f} ms "
          f"(exact times vary by machine)")


if __name__ == "__main__":
    batch_vs_online()
    print()
    instance_vs_model()
