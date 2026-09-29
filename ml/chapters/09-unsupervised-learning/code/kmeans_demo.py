"""k-means from scratch (step by step), k-means++ vs random init, elbow and silhouette."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.cluster import KMeans
from sklearn.datasets import make_blobs
from sklearn.metrics import silhouette_score

IMG = Path(__file__).resolve().parent.parent / "img"
IMG.mkdir(exist_ok=True)


def kmeans_scratch(X, k, rng, max_iter=100):
    centroids = X[rng.choice(len(X), k, replace=False)]          # 1. random init
    history = [centroids.copy()]
    for it in range(max_iter):
        d = np.linalg.norm(X[:, None, :] - centroids[None, :, :], axis=2)
        labels = d.argmin(axis=1)                                 # 2. assign to nearest centroid
        new = np.array([X[labels == j].mean(axis=0) for j in range(k)])  # 3. move centroids to means
        history.append(new.copy())
        if np.allclose(new, centroids):                           # 4. stop when nothing moves
            break
        centroids = new
    inertia = ((X - centroids[labels]) ** 2).sum()
    return labels, centroids, inertia, it + 1, history


def plot_steps(X, history, labels_final):
    fig, axes = plt.subplots(1, 3, figsize=(10, 3.2))
    for ax, step in zip(axes, (0, 1, len(history) - 1)):
        c = history[step]
        lab = np.linalg.norm(X[:, None] - c[None], axis=2).argmin(1)
        ax.scatter(X[:, 0], X[:, 1], c=lab, s=8, cmap="viridis", alpha=.6)
        ax.scatter(c[:, 0], c[:, 1], c="red", marker="X", s=150, edgecolors="k")
        ax.set_title(f"iteration {step}" + (" (converged)" if step == len(history) - 1 else ""), fontsize=9)
        ax.set_xticks([]); ax.set_yticks([])
    plt.tight_layout(); plt.savefig(IMG / "kmeans_steps.png", dpi=120); plt.close()


def elbow_and_silhouette(X):
    ks = range(2, 11)
    inertias, sils = [], []
    for k in ks:
        km = KMeans(n_clusters=k, n_init=10, random_state=0).fit(X)
        inertias.append(km.inertia_)
        sils.append(silhouette_score(X, km.labels_))
    print("\n k   inertia   silhouette")
    for k, i, s in zip(ks, inertias, sils):
        print(f"{k:2d}  {i:8.0f}   {s:.3f}")
    fig, ax = plt.subplots(1, 2, figsize=(9, 3.2))
    ax[0].plot(list(ks), inertias, "o-"); ax[0].set_title("Elbow: inertia vs k"); ax[0].set_xlabel("k")
    ax[1].plot(list(ks), sils, "s-", c="tab:orange"); ax[1].set_title("Silhouette vs k (higher is better)")
    ax[1].set_xlabel("k")
    for a in ax:
        a.grid(alpha=.3)
    plt.tight_layout(); plt.savefig(IMG / "elbow_silhouette.png", dpi=120); plt.close()


def init_comparison(X):
    print("\ninit          inertia over 20 single-run seeds, 8 true clusters (min / mean / max)")
    for init in ("random", "k-means++"):
        vals = [KMeans(n_clusters=8, init=init, n_init=1, random_state=s).fit(X).inertia_ for s in range(20)]
        print(f"{init:12s}  {min(vals):8.0f} / {np.mean(vals):8.0f} / {max(vals):8.0f}")


if __name__ == "__main__":
    X, y = make_blobs(n_samples=600, centers=4, cluster_std=1.0, random_state=42)
    labels, cents, inertia, iters, hist = kmeans_scratch(X, 4, np.random.default_rng(3))
    print(f"from scratch: converged in {iters} iterations, inertia = {inertia:.0f}")
    sk = KMeans(n_clusters=4, n_init=10, random_state=0).fit(X)
    print(f"sklearn     : inertia = {sk.inertia_:.0f}")
    plot_steps(X, hist, labels)
    elbow_and_silhouette(X)
    Xh, _ = make_blobs(n_samples=1500, centers=8, cluster_std=0.5, random_state=4, center_box=(-20, 20))
    init_comparison(Xh)
