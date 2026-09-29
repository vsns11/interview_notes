"""k-means vs agglomerative vs DBSCAN vs GMM on awkward shapes, dendrogram, DBSCAN eps via k-distance, GMM + BIC."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from scipy.cluster.hierarchy import dendrogram, linkage
from sklearn.cluster import DBSCAN, AgglomerativeClustering, KMeans
from sklearn.datasets import make_blobs, make_moons
from sklearn.metrics import adjusted_rand_score
from sklearn.mixture import GaussianMixture
from sklearn.neighbors import NearestNeighbors
from sklearn.preprocessing import StandardScaler

IMG = Path(__file__).resolve().parent.parent / "img"


def datasets():
    Xm, ym = make_moons(n_samples=400, noise=0.06, random_state=0)
    Xa, ya = make_blobs(n_samples=400, centers=3, random_state=170)
    Xa = Xa @ np.array([[0.6, -0.64], [-0.41, 0.85]])            # stretch -> elongated clusters
    Xv, yv = make_blobs(n_samples=400, centers=3, cluster_std=[0.5, 2.5, 1.0], random_state=8)
    return {"moons": (Xm, ym, 2), "elongated": (Xa, ya, 3), "unequal spread": (Xv, yv, 3)}


def compare():
    data = datasets()
    algos = {
        "k-means": lambda k: KMeans(n_clusters=k, n_init=10, random_state=0),
        "agglomerative (single)": lambda k: AgglomerativeClustering(n_clusters=k, linkage="single"),
        "agglomerative (ward)": lambda k: AgglomerativeClustering(n_clusters=k, linkage="ward"),
        "DBSCAN": lambda k: DBSCAN(eps=0.3, min_samples=5),
        "GMM (full cov)": lambda k: GaussianMixture(n_components=k, covariance_type="full", random_state=0),
    }
    fig, axes = plt.subplots(len(data), len(algos), figsize=(14, 8))
    print(f"{'Adjusted Rand Index':24s}" + "".join(f"{n:>16s}" for n in data))
    scores = {a: [] for a in algos}
    for r, (dname, (X, y, k)) in enumerate(data.items()):
        Xs = StandardScaler().fit_transform(X)
        for c, (aname, make) in enumerate(algos.items()):
            labels = make(k).fit_predict(Xs)
            scores[aname].append(adjusted_rand_score(y, labels))
            ax = axes[r, c]
            ax.scatter(Xs[:, 0], Xs[:, 1], c=labels, s=6, cmap="tab10")
            ax.set_xticks([]); ax.set_yticks([])
            if r == 0:
                ax.set_title(aname, fontsize=9)
            if c == 0:
                ax.set_ylabel(dname, fontsize=9)
            ax.text(.02, .02, f"ARI {scores[aname][-1]:.2f}", transform=ax.transAxes, fontsize=8)
    for aname, s in scores.items():
        print(f"{aname:24s}" + "".join(f"{v:16.2f}" for v in s))
    plt.tight_layout(); plt.savefig(IMG / "clustering_comparison.png", dpi=110); plt.close()


def dendrogram_plot():
    X, _ = make_blobs(n_samples=30, centers=3, cluster_std=0.8, random_state=1)
    Z = linkage(X, method="ward")
    plt.figure(figsize=(7, 3.3))
    dendrogram(Z, color_threshold=8)
    plt.axhline(8, ls="--", c="gray")
    plt.title("Ward dendrogram: cut at height 8 -> 3 clusters"); plt.xlabel("sample index"); plt.ylabel("merge distance")
    plt.tight_layout(); plt.savefig(IMG / "dendrogram.png", dpi=120); plt.close()
    print(f"\nlast 3 merge heights: {np.round(Z[-3:, 2], 2)}  (big jump before the final merges = natural cut)")


def dbscan_eps():
    X, _ = make_moons(n_samples=400, noise=0.06, random_state=0)
    Xs = StandardScaler().fit_transform(X)
    k = 5
    d, _ = NearestNeighbors(n_neighbors=k).fit(Xs).kneighbors(Xs)   # includes the point itself
    kd = np.sort(d[:, -1])
    plt.figure(figsize=(6, 3.2))
    plt.plot(kd); plt.axhline(0.3, ls="--", c="gray")
    plt.title(f"k-distance plot (k={k}): pick eps near the knee"); plt.xlabel("points sorted"); plt.ylabel("distance to k-th neighbour")
    plt.tight_layout(); plt.savefig(IMG / "dbscan_kdistance.png", dpi=120); plt.close()
    print("\nDBSCAN on moons (min_samples=5)")
    for eps in (0.1, 0.2, 0.3, 0.5, 1.0):
        lab = DBSCAN(eps=eps, min_samples=5).fit_predict(Xs)
        print(f"  eps={eps:<4} clusters={len(set(lab) - {-1})}  noise points={int((lab == -1).sum())}")


def gmm_demo():
    X, _ = make_blobs(n_samples=500, centers=3, cluster_std=[1.0, 2.0, 1.5], random_state=2)
    print("\nGMM model selection by BIC (lower is better)")
    for k in range(1, 7):
        g = GaussianMixture(n_components=k, random_state=0).fit(X)
        print(f"  k={k}  BIC={g.bic(X):8.1f}")
    g = GaussianMixture(n_components=3, random_state=0).fit(X)
    p = g.predict_proba(X)
    unsure = (p.max(axis=1) < 0.9).sum()
    print(f"soft assignments: {unsure} of {len(X)} points have max membership < 0.9, e.g. {np.round(p[np.argmin(p.max(1))], 2)}")
    print(f"EM converged in {g.n_iter_} iterations; mixing weights {np.round(g.weights_, 2)}")


if __name__ == "__main__":
    compare()
    dendrogram_plot()
    dbscan_eps()
    gmm_demo()
