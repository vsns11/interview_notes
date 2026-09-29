"""A 2-layer neural network from scratch in NumPy (forward, backprop, gradient descent).
Trained on XOR and on the two-moons dataset."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.datasets import make_moons
from sklearn.model_selection import train_test_split

IMG = Path(__file__).resolve().parent.parent / "img"


class TwoLayerNet:
    """x -> tanh(x W1 + b1) -> sigmoid(h W2 + b2). Rows of X are examples."""

    def __init__(self, n_in, n_hidden, seed=0):
        rng = np.random.default_rng(seed)
        self.W1 = rng.normal(0, np.sqrt(1 / n_in), (n_in, n_hidden))    # LeCun-style scale: variance 1/fan_in
        self.b1 = np.zeros(n_hidden)
        self.W2 = rng.normal(0, np.sqrt(1 / n_hidden), (n_hidden, 1))
        self.b2 = np.zeros(1)

    def forward(self, X):
        self.X = X
        self.H = np.tanh(X @ self.W1 + self.b1)
        self.P = 1 / (1 + np.exp(-(self.H @ self.W2 + self.b2)))
        return self.P

    def backward(self, y):
        n = len(y)
        dZ2 = (self.P - y[:, None]) / n                  # BCE + sigmoid, averaged over batch
        dW2 = self.H.T @ dZ2;  db2 = dZ2.sum(0)
        dH = dZ2 @ self.W2.T
        dZ1 = dH * (1 - self.H ** 2)                      # tanh'(z) = 1 - tanh(z)^2
        dW1 = self.X.T @ dZ1;  db1 = dZ1.sum(0)
        return dW1, db1, dW2, db2

    def step(self, grads, lr):
        for p, g in zip((self.W1, self.b1, self.W2, self.b2), grads):
            p -= lr * g                                   # in-place gradient descent

    @staticmethod
    def loss(P, y):
        P = np.clip(P[:, 0], 1e-9, 1 - 1e-9)
        return -np.mean(y * np.log(P) + (1 - y) * np.log(1 - P))


def train(net, X, y, lr, epochs):
    hist = []
    for _ in range(epochs):
        P = net.forward(X)
        hist.append(net.loss(P, y))
        net.step(net.backward(y), lr)
    return hist


if __name__ == "__main__":
    # --- XOR: impossible for a single perceptron, easy with one hidden layer ---
    X = np.array([[0, 0], [0, 1], [1, 0], [1, 1]], dtype=float)
    y = np.array([0, 1, 1, 0], dtype=float)
    net = TwoLayerNet(2, 4, seed=1)
    hist = train(net, X, y, lr=1.0, epochs=2000)
    print(f"XOR  loss {hist[0]:.3f} -> {hist[-1]:.4f}, probs = {net.forward(X)[:, 0].round(3)}")

    # --- Two moons ---
    Xm, ym = make_moons(n_samples=600, noise=0.2, random_state=0)
    Xtr, Xte, ytr, yte = train_test_split(Xm, ym, test_size=0.3, random_state=0)
    net = TwoLayerNet(2, 16, seed=0)
    hist = train(net, Xtr, ytr.astype(float), lr=0.5, epochs=3000)
    acc = lambda X_, y_: np.mean((net.forward(X_)[:, 0] > 0.5) == y_)
    print(f"Moons loss {hist[0]:.3f} -> {hist[-1]:.4f}, train acc {acc(Xtr, ytr):.3f}, test acc {acc(Xte, yte):.3f}")

    # --- gradient check on a few weights (numerical vs analytical) ---
    net.forward(Xtr[:20]); g = net.backward(ytr[:20].astype(float))[0]
    eps = 1e-6; num = []
    for (i, j) in [(0, 0), (1, 3), (0, 7)]:
        net.W1[i, j] += eps; lp = net.loss(net.forward(Xtr[:20]), ytr[:20])
        net.W1[i, j] -= 2 * eps; lm = net.loss(net.forward(Xtr[:20]), ytr[:20])
        net.W1[i, j] += eps; num.append((lp - lm) / (2 * eps))
    ana = [g[0, 0], g[1, 3], g[0, 7]]
    print("gradient check  numerical:", np.round(num, 6), " analytical:", np.round(ana, 6))

    # --- plot ---
    fig, axes = plt.subplots(1, 2, figsize=(10, 4))
    axes[0].plot(hist); axes[0].set_title("Training loss (moons)"); axes[0].set_xlabel("epoch"); axes[0].set_ylabel("BCE")
    xx, yy = np.meshgrid(np.linspace(-2, 3, 300), np.linspace(-1.5, 2, 300))
    zz = net.forward(np.c_[xx.ravel(), yy.ravel()])[:, 0].reshape(xx.shape)
    axes[1].contourf(xx, yy, zz, levels=20, cmap="RdBu", alpha=0.6)
    axes[1].scatter(Xm[:, 0], Xm[:, 1], c=ym, cmap="RdBu", edgecolors="k", s=12)
    axes[1].set_title("Decision boundary of NumPy 2-16-1 net")
    fig.tight_layout(); fig.savefig(IMG / "numpy_mlp_moons.png", dpi=120)
    print("saved img/numpy_mlp_moons.png")
