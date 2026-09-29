"""Approximate nearest neighbours: brute force vs an IVF index (inverted file), in numpy.
IVF = cluster the vectors with k-means; at query time only scan the closest `nprobe` clusters."""
import time

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

from data import IMG

rng = np.random.default_rng(0)
N, D, Q, K = 100_000, 32, 200, 10


def make_vectors():
    centres = rng.normal(0, 1, (300, D))                    # clumpy data, like real embeddings
    X = centres[rng.integers(0, 300, N)] + rng.normal(0, 1.5, (N, D))
    X /= np.linalg.norm(X, axis=1, keepdims=True)           # unit vectors: dot = cosine
    q = centres[rng.integers(0, 300, Q)] + rng.normal(0, 1.5, (Q, D))
    q /= np.linalg.norm(q, axis=1, keepdims=True)
    return X.astype(np.float32), q.astype(np.float32)


def kmeans(X, k, iters=10):
    C = X[rng.choice(len(X), k, replace=False)].copy()
    for _ in range(iters):
        assign = np.argmax(X @ C.T, axis=1)                  # nearest centroid (by cosine)
        for c in range(k):
            members = X[assign == c]
            if len(members):
                C[c] = members.mean(0)
        C /= np.linalg.norm(C, axis=1, keepdims=True)
    return C, np.argmax(X @ C.T, axis=1)


def main():
    X, queries = make_vectors()

    t = time.perf_counter()
    exact = np.argsort(-(queries @ X.T), axis=1)[:, :K]      # brute force: score every vector
    t_brute = (time.perf_counter() - t) / Q * 1e3
    print(f"brute force: scans 100% of {N:,} vectors, {t_brute:.2f} ms/query, recall@10 = 1.000")

    t = time.perf_counter()
    nlist = 256
    C, assign = kmeans(X[rng.choice(N, 20_000, replace=False)], nlist)
    assign = np.argmax(X @ C.T, axis=1)
    lists = [np.flatnonzero(assign == c) for c in range(nlist)]   # the "inverted file"
    print(f"IVF build (k-means, {nlist} lists): {time.perf_counter() - t:.1f} s")

    rows = []
    for nprobe in (1, 2, 4, 8, 16, 32, 64):
        t = time.perf_counter()
        rec, scanned = 0.0, 0
        for qi, q in enumerate(queries):
            probe = np.argsort(-(C @ q))[:nprobe]                 # closest clusters
            cand = np.concatenate([lists[c] for c in probe])
            top = cand[np.argsort(-(X[cand] @ q))[:K]]
            rec += len(set(top) & set(exact[qi])) / K
            scanned += len(cand)
        ms = (time.perf_counter() - t) / Q * 1e3
        rows.append((nprobe, rec / Q, scanned / Q / N, ms))
        print(f"IVF nprobe={nprobe:2d}: scans {scanned / Q / N:6.2%}, {ms:.2f} ms/query, recall@10 = {rec / Q:.3f}")

    fig, ax = plt.subplots(figsize=(6, 4))
    ax.plot([r[2] * 100 for r in rows], [r[1] for r in rows], "o-")
    for r in rows:
        ax.annotate(f"nprobe={r[0]}", (r[2] * 100, r[1]), textcoords="offset points", xytext=(5, -12), fontsize=8)
    ax.set_xscale("log"); ax.set_xlabel("% of vectors scanned (log)"); ax.set_ylabel("recall@10 vs exact")
    ax.set_title("IVF: speed/recall trade-off"); fig.tight_layout()
    fig.savefig(IMG / "ivf_recall_tradeoff.png", dpi=130)
    print("saved img/ivf_recall_tradeoff.png")


if __name__ == "__main__":
    main()
