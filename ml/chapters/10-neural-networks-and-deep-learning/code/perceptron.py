"""Perceptron learning rule on AND (linearly separable) and XOR (not separable)."""
import numpy as np


def train_perceptron(X, y, epochs=20, lr=1.0):
    w = np.zeros(X.shape[1])
    b = 0.0
    for epoch in range(epochs):
        errors = 0
        for xi, yi in zip(X, y):
            pred = 1 if xi @ w + b > 0 else 0      # step activation
            update = lr * (yi - pred)               # 0 if correct, +-1 if wrong
            w += update * xi
            b += update
            errors += int(update != 0)
        if errors == 0:
            return w, b, epoch + 1, True
    return w, b, epochs, False


if __name__ == "__main__":
    X = np.array([[0, 0], [0, 1], [1, 0], [1, 1]], dtype=float)
    for name, y in [("AND", np.array([0, 0, 0, 1])), ("XOR", np.array([0, 1, 1, 0]))]:
        w, b, ep, ok = train_perceptron(X, y)
        preds = (X @ w + b > 0).astype(int)
        print(f"{name}: converged={ok} after {ep} epochs, w={w}, b={b}, preds={preds}, target={y}")
