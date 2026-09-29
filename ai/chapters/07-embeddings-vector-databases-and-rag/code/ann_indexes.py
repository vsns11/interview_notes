"""Exact search vs two classic approximate-nearest-neighbour (ANN) ideas:

- IVF (inverted file): k-means the vectors into `nlist` clusters; at query time
  scan only the `nprobe` closest clusters.
- PQ (product quantization): cut each vector into m sub-vectors and store only
  the id (1 byte) of the nearest of 256 centroids for each piece.

We measure recall@10 against exact search - the honest way to evaluate ANN.
"""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.cluster import KMeans

rng = np.random.default_rng(0)
N, D, Q, K = 20_000, 64, 200, 10

# Clustered synthetic "embeddings" (real embeddings are clustered by topic too)
centers = rng.normal(size=(50, D))
X = centers[rng.integers(0, 50, N)] + 0.6 * rng.normal(size=(N, D))
X /= np.linalg.norm(X, axis=1, keepdims=True)
queries = centers[rng.integers(0, 50, Q)] + 0.6 * rng.normal(size=(Q, D))
queries /= np.linalg.norm(queries, axis=1, keepdims=True)


def exact_topk(q, k=K):
    return np.argsort(-(X @ q))[:k]


def recall(approx, exact):
    return len(set(approx) & set(exact)) / len(exact)


truth = [exact_topk(q) for q in queries]

# ---------------------------------------------------------------- IVF
NLIST = 100
km = KMeans(n_clusters=NLIST, n_init=1, random_state=0).fit(X)
lists = [np.where(km.labels_ == c)[0] for c in range(NLIST)]


def ivf_search(q, nprobe, k=K):
    probe = np.argsort(-(km.cluster_centers_ @ q))[:nprobe]      # closest clusters
    cand = np.concatenate([lists[c] for c in probe])
    return cand[np.argsort(-(X[cand] @ q))[:k]], len(cand)


# ---------------------------------------------------------------- PQ
def train_pq(m_sub, ks=256):
    sub = D // m_sub
    books, codes = [], np.zeros((N, m_sub), dtype=np.uint8)
    for m in range(m_sub):
        part = X[:, m * sub:(m + 1) * sub]
        cb = KMeans(n_clusters=ks, n_init=1, random_state=0, max_iter=25).fit(part)
        books.append(cb.cluster_centers_)
        codes[:, m] = cb.labels_
    return books, codes


def pq_search(q, books, codes, k=K, rerank=0):
    m_sub = len(books)
    sub = D // m_sub
    # asymmetric distance: exact query pieces vs quantized database pieces
    table = np.stack([books[m] @ q[m * sub:(m + 1) * sub] for m in range(m_sub)])
    approx = table[np.arange(m_sub), codes].sum(axis=1)          # (N,) approx dot
    if rerank:                                                    # re-score a shortlist exactly
        short = np.argsort(-approx)[:rerank]
        return short[np.argsort(-(X[short] @ q))[:k]]
    return np.argsort(-approx)[:k]


if __name__ == "__main__":
    print(f"{N:,} vectors x {D} dims, {Q} queries, recall@{K} vs exact search")
    print("exact search scans 100% of vectors for every query\n")
    print("IVF (nlist=100)")
    print(" nprobe  scanned  recall@10")
    xs, ys = [], []
    for nprobe in [1, 2, 4, 8, 16, 32]:
        res = [ivf_search(q, nprobe) for q in queries]
        r = np.mean([recall(a, t_) for (a, _), t_ in zip(res, truth)])
        frac = np.mean([n for _, n in res]) / N
        xs.append(frac * 100)
        ys.append(r)
        print(f" {nprobe:>6}  {frac:7.1%}  {r:9.3f}")

    raw = X.astype(np.float32).nbytes
    print(f"\nPQ, 256 centroids per sub-vector (float32 vectors = {D * 4} bytes each, "
          f"{raw / 1e6:.1f} MB total)")
    print(" bytes/vec  smaller  recall@10  recall@10 with exact re-rank of top 100")
    for m_sub in [8, 16]:
        books, codes = train_pq(m_sub)
        r_pq = np.mean([recall(pq_search(q, books, codes), t_) for q, t_ in zip(queries, truth)])
        r_rr = np.mean([recall(pq_search(q, books, codes, rerank=100), t_)
                        for q, t_ in zip(queries, truth)])
        print(f" {m_sub:>9}  {D * 4 // m_sub:>6}x  {r_pq:9.3f}  {r_rr:9.3f}")

    gb = 10_000_000 * 1024 * 4 / 1e9
    print(f"\nCapacity check: 10M vectors x 1,024 float32 = {gb:.2f} GB of raw vectors "
          f"(before graph links, metadata, replicas)")

    plt.figure(figsize=(6, 3.6))
    plt.plot(xs, ys, marker="o")
    for x, y, p in zip(xs, ys, [1, 2, 4, 8, 16, 32]):
        plt.annotate(f"nprobe={p}", (x, y), textcoords="offset points", xytext=(6, -4), fontsize=8)
    plt.xscale("log")
    plt.xticks([1, 2, 5, 10, 20, 40], ["1", "2", "5", "10", "20", "40"])
    plt.minorticks_off()
    plt.xlim(0.9, 60)
    plt.ylim(0.65, 1.03)
    plt.xlabel("% of vectors scanned (log scale)")
    plt.ylabel("recall@10")
    plt.title("IVF: scanning less costs recall")
    plt.grid(alpha=0.3)
    plt.tight_layout()
    plt.savefig(Path(__file__).resolve().parent.parent / "img" / "ivf_recall.png", dpi=150)
    print("saved img/ivf_recall.png")
