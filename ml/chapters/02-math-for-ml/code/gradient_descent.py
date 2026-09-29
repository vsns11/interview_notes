"""Gradient descent: by hand on f(w) = (w - 3)^2, the effect of the learning rate, and linear regression
from scratch. Saves img/gd_learning_rates.png and img/gd_linear_regression.png."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.linear_model import LinearRegression

IMG = Path(__file__).resolve().parent.parent / "img"


def one_dimension() -> None:
    f = lambda w: (w - 3) ** 2
    grad = lambda w: 2 * (w - 3)
    w, lr = 0.0, 0.1
    print("step      w     f(w)   gradient")
    for step in range(6):
        print(f"{step:>4} {w:7.4f} {f(w):8.4f} {grad(w):9.4f}")
        w = w - lr * grad(w)

    fig, axes = plt.subplots(1, 3, figsize=(12, 3.4))
    for ax, lr, title in zip(axes, [0.02, 0.3, 1.05], ["too small", "good", "too large: diverges"]):
        path = [0.0]
        for _ in range(12):
            path.append(path[-1] - lr * grad(path[-1]))
        path = np.array(path)
        xs = np.linspace(min(-1, path.min() - 0.5), max(7, path.max() + 0.5), 300)
        ax.plot(xs, f(xs), color="#999")
        ax.plot(path, f(path), "o-", color="#d62728", ms=4)
        ax.set_title(f"learning rate {lr}: {title}\nw after 12 steps = {path[-1]:.2f}", fontsize=10)
        ax.set_xlabel("w")
    axes[0].set_ylabel("f(w) = (w - 3)^2")
    fig.tight_layout()
    fig.savefig(IMG / "gd_learning_rates.png", dpi=120)
    plt.close(fig)


def linear_regression_gd() -> None:
    rng = np.random.default_rng(0)
    x = rng.uniform(0, 10, 100)
    y = 4 * x + 7 + rng.normal(0, 2, 100)        # true w = 4, b = 7

    w, b, lr = 0.0, 0.0, 0.01
    losses = []
    for epoch in range(2000):
        y_hat = w * x + b
        err = y_hat - y
        losses.append(np.mean(err ** 2))
        if epoch in (0, 1, 10, 100, 1999):
            print(f"epoch {epoch:>4}: w={w:.3f} b={b:.3f} MSE={losses[-1]:.3f}")
        dw = 2 * np.mean(err * x)                 # dMSE/dw
        db = 2 * np.mean(err)                     # dMSE/db
        w -= lr * dw
        b -= lr * db

    ref = LinearRegression().fit(x[:, None], y)
    print(f"scikit-learn closed form: w={ref.coef_[0]:.3f} b={ref.intercept_:.3f}")

    fig, axes = plt.subplots(1, 2, figsize=(10, 3.6))
    axes[0].plot(losses, color="#1f77b4")
    axes[0].set_yscale("log"); axes[0].set_xlabel("epoch"); axes[0].set_ylabel("MSE (log scale)")
    axes[0].set_title("loss falls as gradient descent runs", fontsize=10)
    axes[1].scatter(x, y, s=10, color="#444")
    grid = np.linspace(0, 10, 2)
    axes[1].plot(grid, w * grid + b, color="#d62728", lw=2, label=f"GD fit: y = {w:.2f}x + {b:.2f}")
    axes[1].legend(fontsize=9); axes[1].set_xlabel("x"); axes[1].set_ylabel("y")
    axes[1].set_title("fitted line", fontsize=10)
    fig.tight_layout()
    fig.savefig(IMG / "gd_linear_regression.png", dpi=120)
    plt.close(fig)


if __name__ == "__main__":
    one_dimension()
    print()
    linear_regression_gd()
