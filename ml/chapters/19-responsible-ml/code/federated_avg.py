"""Federated averaging (FedAvg) from scratch: three 'hospitals' train a shared
logistic-regression model without ever sending their raw rows to the server."""
import numpy as np
from sklearn.datasets import make_classification

rng = np.random.default_rng(0)


def sigmoid(z):
    return 1 / (1 + np.exp(-z))


def local_train(w, X, y, epochs=5, lr=0.1):
    """A client's job: start from the global weights, do a few epochs of gradient descent."""
    for _ in range(epochs):
        grad = X.T @ (sigmoid(X @ w) - y) / len(y)
        w = w - lr * grad
    return w


def accuracy(w, X, y):
    return np.mean((sigmoid(X @ w) >= 0.5) == y)


def main():
    X, y = make_classification(n_samples=6000, n_features=8, n_informative=5, random_state=0)
    X = np.c_[X, np.ones(len(X))]                              # bias column
    X_test, y_test = X[5000:], y[5000:]
    X, y = X[:5000], y[:5000]
    # non-IID split: clients see different slices of feature 0 and different sizes
    order = np.argsort(X[:, 0])
    parts = np.split(order, [2500, 4000])                      # sizes 2500 / 1500 / 1000
    clients = [(X[p], y[p]) for p in parts]

    w_global = np.zeros(X.shape[1])
    for rnd in range(1, 31):
        updates, sizes = [], []
        for Xc, yc in clients:                                 # runs ON each client
            updates.append(local_train(w_global.copy(), Xc, yc))
            sizes.append(len(yc))
        w_global = np.average(updates, axis=0, weights=sizes)  # server: weighted average of weights
        if rnd in (1, 5, 10, 30):
            print(f"round {rnd:2d}: global test accuracy = {accuracy(w_global, X_test, y_test):.3f}")

    w_central = local_train(np.zeros(X.shape[1]), X, y, epochs=150)
    print(f"\ncentralised (all data pooled, 150 epochs): {accuracy(w_central, X_test, y_test):.3f}")
    for i, (Xc, yc) in enumerate(clients):
        w_solo = local_train(np.zeros(X.shape[1]), Xc, yc, epochs=150)
        print(f"client {i} alone (n={len(yc)}):            {accuracy(w_solo, X_test, y_test):.3f}")


if __name__ == "__main__":
    main()
