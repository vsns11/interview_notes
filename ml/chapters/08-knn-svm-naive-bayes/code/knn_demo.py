"""k-NN: distance metrics by hand, the effect of scaling, choosing k, curse of dimensionality."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.datasets import load_wine
from sklearn.model_selection import cross_val_score
from sklearn.neighbors import KNeighborsClassifier
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler

IMG = Path(__file__).resolve().parent.parent / "img"
IMG.mkdir(exist_ok=True)


def distances():
    a, b = np.array([1.0, 2.0, 3.0]), np.array([4.0, 6.0, 3.0])
    print("a =", a, " b =", b)
    print(f"Euclidean (L2) = {np.sqrt(((a - b) ** 2).sum()):.3f}")
    print(f"Manhattan (L1) = {np.abs(a - b).sum():.3f}")
    print(f"Chebyshev (Linf) = {np.abs(a - b).max():.3f}")
    cos = a @ b / (np.linalg.norm(a) * np.linalg.norm(b))
    print(f"cosine similarity = {cos:.3f} -> cosine distance = {1 - cos:.3f}")


def scaling_matters():
    X, y = load_wine(return_X_y=True)
    print(f"\nwine feature ranges: min std {X.std(0).min():.2f}, max std {X.std(0).max():.1f} (proline)")
    raw = cross_val_score(KNeighborsClassifier(5), X, y, cv=5).mean()
    scaled = cross_val_score(make_pipeline(StandardScaler(), KNeighborsClassifier(5)), X, y, cv=5).mean()
    print(f"k-NN (k=5) CV accuracy  raw features: {raw:.3f}   standardized: {scaled:.3f}")


def choose_k():
    X, y = load_wine(return_X_y=True)
    ks = list(range(1, 41, 2))
    scores = [cross_val_score(make_pipeline(StandardScaler(), KNeighborsClassifier(k)), X, y, cv=5).mean()
              for k in ks]
    best = ks[int(np.argmax(scores))]
    print(f"\nbest k by 5-fold CV = {best} (accuracy {max(scores):.3f});  k=1: {scores[0]:.3f}, k=39: {scores[-1]:.3f}")
    plt.figure(figsize=(6, 3.2))
    plt.plot(ks, scores, "o-")
    plt.axvline(best, ls="--", c="gray")
    plt.xlabel("k (neighbours)"); plt.ylabel("5-fold CV accuracy"); plt.title("Choosing k on the wine data")
    plt.grid(alpha=.3); plt.tight_layout(); plt.savefig(IMG / "knn_choose_k.png", dpi=120); plt.close()


def curse_of_dimensionality():
    rng = np.random.default_rng(0)
    dims = [1, 2, 5, 10, 50, 100, 500, 1000]
    ratios = []
    print("\ndim  (farthest - nearest) / nearest distance, 500 uniform points")
    for d in dims:
        X = rng.random((500, d))
        q = rng.random(d)
        dist = np.linalg.norm(X - q, axis=1)
        r = (dist.max() - dist.min()) / dist.min()
        ratios.append(r)
        print(f"{d:5d}  {r:8.3f}")
    plt.figure(figsize=(6, 3.2))
    plt.loglog(dims, ratios, "o-")
    plt.xlabel("number of dimensions"); plt.ylabel("(max - min) / min distance")
    plt.title("Distances concentrate as dimensions grow"); plt.grid(alpha=.3, which="both")
    plt.tight_layout(); plt.savefig(IMG / "curse_of_dimensionality.png", dpi=120); plt.close()


if __name__ == "__main__":
    distances()
    scaling_matters()
    choose_k()
    curse_of_dimensionality()
