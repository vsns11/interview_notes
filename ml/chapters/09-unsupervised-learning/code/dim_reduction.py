"""PCA by hand (covariance -> eigenvectors), explained variance on digits, PCA vs t-SNE, trustworthiness."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.datasets import load_digits
from sklearn.decomposition import PCA
from sklearn.manifold import TSNE, trustworthiness
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler

IMG = Path(__file__).resolve().parent.parent / "img"


def pca_by_hand():
    # 5 students: hours studied, exam score (strongly correlated)
    X = np.array([[2, 50], [4, 60], [6, 70], [8, 85], [10, 95]], dtype=float)
    Xc = X - X.mean(axis=0)                           # 1. centre
    Xs = Xc / X.std(axis=0, ddof=1)                   # 2. scale (units differ)
    C = np.cov(Xs, rowvar=False)                      # 3. covariance matrix
    vals, vecs = np.linalg.eigh(C)                    # 4. eigen-decomposition
    order = np.argsort(vals)[::-1]
    vals, vecs = vals[order], vecs[:, order]
    vecs *= np.sign(vecs[0])                          # an eigenvector's sign is arbitrary: make it readable
    print("covariance (standardised):\n", np.round(C, 3))
    print("eigenvalues:", np.round(vals, 3), " explained variance ratio:", np.round(vals / vals.sum(), 3))
    print("PC1 direction:", np.round(vecs[:, 0], 3))
    z = Xs @ vecs[:, 0]                               # 5. project onto PC1
    print("students on PC1:", np.round(z, 2))
    sk = PCA().fit(Xs)
    print("sklearn explained_variance_ratio_:", np.round(sk.explained_variance_ratio_, 3))


def digits_variance():
    X, y = load_digits(return_X_y=True)
    Xs = StandardScaler().fit_transform(X)
    pca = PCA().fit(Xs)
    cum = np.cumsum(pca.explained_variance_ratio_)
    for t in (0.5, 0.8, 0.9, 0.95, 0.99):
        print(f"components for {int(t * 100)}% variance: {np.searchsorted(cum, t) + 1:2d} of 64")
    p95 = PCA(n_components=0.95).fit(Xs)
    recon = p95.inverse_transform(p95.transform(Xs))
    print(f"PCA(n_components=0.95) keeps {p95.n_components_} components, reconstruction MSE = {((Xs - recon) ** 2).mean():.3f}")
    plt.figure(figsize=(6, 3.2))
    plt.plot(range(1, 65), cum, "o-", ms=3)
    plt.axhline(0.95, ls="--", c="gray"); plt.axvline(p95.n_components_, ls="--", c="gray")
    plt.xlabel("number of components"); plt.ylabel("cumulative explained variance")
    plt.title("Digits (64 pixels): scree / cumulative variance"); plt.grid(alpha=.3)
    plt.tight_layout(); plt.savefig(IMG / "pca_explained_variance.png", dpi=120); plt.close()


def pca_vs_tsne():
    X, y = load_digits(return_X_y=True)
    Xs = StandardScaler().fit_transform(X)
    Z_pca = PCA(n_components=2, random_state=0).fit_transform(Xs)
    Z_tsne = TSNE(n_components=2, perplexity=30, init="pca", random_state=0).fit_transform(
        PCA(n_components=30, random_state=0).fit_transform(Xs))   # common trick: PCA to ~30-50 dims first
    for name, Z in (("PCA", Z_pca), ("t-SNE", Z_tsne)):
        print(f"trustworthiness (k=5) {name:5s}: {trustworthiness(Xs, Z, n_neighbors=5):.3f}")
    fig, ax = plt.subplots(1, 2, figsize=(10, 4.3))
    for a, Z, t in ((ax[0], Z_pca, "PCA (linear)"), (ax[1], Z_tsne, "t-SNE (perplexity 30)")):
        sc = a.scatter(Z[:, 0], Z[:, 1], c=y, cmap="tab10", s=5)
        a.set_title(t); a.set_xticks([]); a.set_yticks([])
    fig.colorbar(sc, ax=ax, ticks=range(10), label="digit")
    plt.savefig(IMG / "pca_vs_tsne.png", dpi=120, bbox_inches="tight"); plt.close()


if __name__ == "__main__":
    pca_by_hand()
    print()
    digits_variance()
    print()
    pca_vs_tsne()
