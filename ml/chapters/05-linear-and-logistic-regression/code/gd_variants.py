"""Gradient descent in practice: learning rate, batch vs mini-batch vs stochastic, and why feature scaling matters."""
import numpy as np

from plotstyle import C, INK2, plt, save


def mse_grad(Xb, y, theta):
    return (2 / len(y)) * Xb.T @ (Xb @ theta - y)


def run(Xb, y, lr, epochs, batch_size, seed=0, lr_decay=False):
    """Generic GD: batch_size=len(y) -> batch GD, 1 -> SGD, in between -> mini-batch."""
    rng = np.random.default_rng(seed)
    m = len(y)
    theta = np.array([-1.0, -1.0])                    # deliberately poor start
    path = [theta.copy()]
    step = 0
    for epoch in range(epochs):
        idx = rng.permutation(m)                      # reshuffle every epoch
        for start in range(0, m, batch_size):
            b = idx[start:start + batch_size]
            eta = lr / (1 + 0.05 * step) if lr_decay else lr   # simple learning-rate schedule
            theta = theta - eta * mse_grad(Xb[b], y[b], theta)
            step += 1
            path.append(theta.copy())
    return theta, np.array(path)


def main() -> None:
    rng = np.random.default_rng(42)
    x = 2 * rng.random(100)
    y = 4 + 3 * x + rng.normal(0, 1, 100)
    Xb = np.c_[np.ones(100), x]
    best = np.linalg.solve(Xb.T @ Xb, Xb.T @ y)

    # 1) Learning rate: too small, good, too big
    fig, ax = plt.subplots(figsize=(6.5, 3.2))
    for lr, col in [(0.01, C[0]), (0.1, C[2]), (0.6, C[1])]:
        th = np.zeros(2)
        losses = []
        for _ in range(60):
            losses.append(((Xb @ th - y) ** 2).mean())
            th -= lr * mse_grad(Xb, y, th)
        print(f"lr={lr:<5} MSE after 60 epochs: {losses[-1]:.3g}")
        ax.plot(losses, color=col, label=f"learning rate {lr}")
    ax.set(yscale="log", xlabel="epoch", ylabel="training MSE (log scale)",
           title="Too small crawls, too large diverges")
    ax.legend()
    save(fig, "gd_learning_rates.png")

    # 2) Batch vs mini-batch vs stochastic: paths in parameter space
    runs = {
        "batch (m=100)": run(Xb, y, lr=0.1, epochs=50, batch_size=100),
        "mini-batch (16)": run(Xb, y, lr=0.1, epochs=8, batch_size=16),
        "stochastic (1), decaying lr": run(Xb, y, lr=0.1, epochs=1, batch_size=1, lr_decay=True),
    }
    print("\nexact (normal eq.):", best.round(3))
    for name, (th, path) in runs.items():
        print(f"{name:<28} updates={len(path) - 1:>4}  final={th.round(3)}")

    b0, b1 = np.meshgrid(np.linspace(-1.5, 6, 200), np.linspace(-1.5, 5, 200))
    Z = ((b0[..., None] + b1[..., None] * x - y) ** 2).mean(-1)
    fig, ax = plt.subplots(figsize=(6.5, 4.5))
    ax.contour(b0, b1, Z, levels=np.geomspace(Z.min() + 0.05, Z.max(), 14), colors="#c9c8c2", linewidths=0.8)
    for (name, (_, path)), col in zip(runs.items(), [C[0], C[1], C[2]]):
        ax.plot(path[:, 0], path[:, 1], "-o", color=col, markersize=3, linewidth=1.2, label=name)
    ax.plot(*best, marker="*", markersize=14, color="#0b0b0b", linestyle="none", label="minimum")
    ax.set(xlabel="intercept b", ylabel="slope w", title="Paths toward the minimum of the MSE bowl")
    ax.legend(loc="lower right", fontsize=8)
    ax.grid(False)
    save(fig, "gd_batch_minibatch_sgd.png")

    # 3) Feature scaling: two features, one with ~6x the spread of the other
    n = 200
    f1, f2 = rng.normal(0, 1, n), rng.normal(0, 6, n)
    yy = 2 * f1 + 0.5 * f2 + rng.normal(0, 0.1, n)
    fig, axes = plt.subplots(1, 2, figsize=(10, 4))
    for ax, (label, X) in zip(axes, [("unscaled", np.c_[f1, f2]),
                                     ("standardized", np.c_[(f1 - f1.mean()) / f1.std(), (f2 - f2.mean()) / f2.std()])]):
        H = 2 / n * X.T @ X                          # Hessian of MSE (no intercept, data ~ centred)
        eig = np.linalg.eigvalsh(H)
        lr = 1 / eig.max()                            # a safe step: the divergence limit is 2 / max eigenvalue
        w_star = np.linalg.solve(X.T @ X, X.T @ yy)
        w = np.zeros(2)
        path = [w.copy()]
        for it in range(1, 200_001):
            w = w - lr * (2 / n) * X.T @ (X @ w - yy)
            if it <= 60:
                path.append(w.copy())
            if np.linalg.norm(w - w_star) < 1e-6 * np.linalg.norm(w_star):
                break
        print(f"{label:<13} condition number {eig.max() / eig.min():>9.0f}  lr={lr:.2e}  iterations to converge: {it}")
        path = np.array(path)
        # contours in a zoomed window around the optimum
        g0, g1 = np.meshgrid(np.linspace(-1, 3.5, 200), np.linspace(-1, 3.5, 200)) if label == "standardized" \
            else np.meshgrid(np.linspace(-0.5, 2.5, 200), np.linspace(-0.3, 1.0, 200))
        W = np.stack([g0, g1], -1)
        Zs = ((W @ X.T - yy) ** 2).mean(-1)
        ax.contour(g0, g1, Zs, levels=np.geomspace(Zs.min() + 1e-3, Zs.max(), 15), colors="#c9c8c2", linewidths=0.8)
        ax.plot(path[:, 0], path[:, 1], "-", color=C[0], linewidth=1.5, label="GD steps (first 60)")
        ax.plot(*w_star, marker="*", markersize=13, color="#0b0b0b", linestyle="none", label="minimum")
        ax.set(title=f"{label}: {it:,} iterations", xlabel="w1", ylabel="w2")
        ax.set_aspect("equal")
        ax.grid(False)
        ax.legend(loc="lower right", fontsize=8)
        ax.tick_params(colors=INK2)
    save(fig, "gd_scaling_contours.png")


if __name__ == "__main__":
    main()
