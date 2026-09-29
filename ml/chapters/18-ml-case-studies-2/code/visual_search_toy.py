"""Toy visual search: a shared image-text embedding trained contrastively, then ANN (IVF) retrieval.

Synthetic 'photos' are noisy 64-d views of a hidden item vector (bad light, clutter = noise and a
random 'nuisance' direction). 'Descriptions' are noisy 24-d views of the same item.
1) Baseline: raw photo features, cosine similarity (no training).
2) Two small encoders (image, text) trained with an in-batch-negative contrastive loss (InfoNCE, as in CLIP)
   on pairs (photo A of a listing, photo B of the same listing) and (photo, description).
3) Serve with an IVF index (k-means buckets; search only the `nprobe` closest buckets) and compare to exact.
4) Sold listings are tombstoned: filtered before results are returned.
"""
import time

import numpy as np
import torch
from torch import nn

torch.manual_seed(0)
torch.set_num_threads(1)                                             # deterministic results run to run
rng = np.random.default_rng(0)
LAT, IMG, TXT, EMB = 16, 64, 24, 32
A_img, A_txt = rng.normal(size=(LAT, IMG)), rng.normal(size=(LAT, TXT))
NUIS = rng.normal(size=(4, IMG)) * 3.0                               # lighting / background directions


def photo(z, noise=4.0):
    return z @ A_img + rng.normal(size=(len(z), 4)) @ NUIS + noise * rng.normal(size=(len(z), IMG))


def describe(z):
    return z @ A_txt + 4.0 * rng.normal(size=(len(z), TXT))


def enc(dim_in):
    return nn.Sequential(nn.Linear(dim_in, 128), nn.ReLU(), nn.Linear(128, EMB))


def info_nce(a, b, temp=0.07):
    a, b = nn.functional.normalize(a, dim=1), nn.functional.normalize(b, dim=1)
    logits = a @ b.T / temp                                           # every other row = an in-batch negative
    target = torch.arange(len(a))
    return (nn.functional.cross_entropy(logits, target) + nn.functional.cross_entropy(logits.T, target)) / 2


def kmeans(x, k, iters=10):
    """Plain Lloyd's k-means in numpy (deterministic): the coarse quantiser of an IVF index."""
    cent = x[rng.choice(len(x), k, replace=False)]
    for _ in range(iters):
        labels = np.argmax(x @ cent.T, axis=1)                        # unit vectors: max dot = nearest
        for c in range(k):
            members = x[labels == c]
            if len(members):
                cent[c] = members.mean(0) / np.linalg.norm(members.mean(0))
    return cent, np.argmax(x @ cent.T, axis=1)


def recall_at_10(q, index):
    q = q / np.linalg.norm(q, axis=1, keepdims=True)
    index = index / np.linalg.norm(index, axis=1, keepdims=True)
    top = np.argsort(-(q @ index.T), axis=1)[:, :10]
    return np.mean([i in row for i, row in enumerate(top)])


if __name__ == "__main__":
    # --- training on 30k listings (two photos + a description each) ---
    z_tr = rng.normal(size=(30_000, LAT))
    p1, p2, d = map(lambda a: torch.tensor(a, dtype=torch.float32), (photo(z_tr), photo(z_tr), describe(z_tr)))
    f_img, f_txt = enc(IMG), enc(TXT)
    opt = torch.optim.Adam(list(f_img.parameters()) + list(f_txt.parameters()), lr=1e-3)
    for epoch in range(6):
        perm = torch.randperm(len(p1))
        for i in range(0, len(p1), 256):
            b = perm[i:i + 256]
            loss = info_nce(f_img(p1[b]), f_img(p2[b])) + info_nce(f_img(p1[b]), f_txt(d[b]))
            opt.zero_grad()
            loss.backward()
            opt.step()

    # --- evaluation on 5k NEW listings: listing photo in the index, a worse buyer photo as the query ---
    z = rng.normal(size=(5_000, LAT))
    listing, query, text = photo(z), photo(z, noise=6.0), describe(z)
    with torch.no_grad():
        E_list = f_img(torch.tensor(listing, dtype=torch.float32)).numpy()
        E_q = f_img(torch.tensor(query, dtype=torch.float32)).numpy()
        E_t = f_txt(torch.tensor(text, dtype=torch.float32)).numpy()
    print("recall@10 among 5,000 listings")
    print(f"  raw features, photo -> listing     : {recall_at_10(query, listing):.3f}")
    print(f"  trained encoder, photo -> listing  : {recall_at_10(E_q, E_list):.3f}")
    print(f"  trained encoder, text  -> listing  : {recall_at_10(E_t, E_list):.3f}   (same index, text query)")

    # --- IVF approximate search over a bigger index: 100k listings ---
    zb = rng.normal(size=(100_000, LAT))
    with torch.no_grad():
        idx_vec = nn.functional.normalize(f_img(torch.tensor(photo(zb), dtype=torch.float32)), dim=1).numpy()
        qv = nn.functional.normalize(f_img(torch.tensor(photo(zb[:500], 6.0), dtype=torch.float32)), dim=1).numpy()
    sold = rng.random(len(zb)) < 0.05                                 # tombstoned since the index build
    centers, labels = kmeans(idx_vec, 256)
    lists = [np.flatnonzero(labels == c) for c in range(256)]
    t0 = time.perf_counter()
    exact = np.argsort(-(qv @ idx_vec.T), axis=1)[:, :10]
    t_exact = (time.perf_counter() - t0) / len(qv) * 1e3
    print(f"\nIVF over {len(zb):,} vectors, 256 buckets (exact search: {t_exact:.2f} ms/query)")
    for nprobe in [1, 4, 16]:
        t0, agree, scanned = time.perf_counter(), 0, 0
        for qi, q in enumerate(qv):
            buckets = np.argsort(-(centers @ q))[:nprobe]
            cand = np.concatenate([lists[c] for c in buckets])
            scanned += len(cand)
            top = cand[np.argsort(-(idx_vec[cand] @ q))[:10]]
            agree += len(set(top) & set(exact[qi]))
        ms = (time.perf_counter() - t0) / len(qv) * 1e3
        print(f"  nprobe={nprobe:>2}: scans {scanned / len(qv) / len(zb):>5.1%} of index, "
              f"recall vs exact {agree / (10 * len(qv)):.3f}, {ms:.2f} ms/query")
    hits_sold = sold[exact].mean()
    print(f"\nwithout a live-status check, {hits_sold:.1%} of returned results are already sold -> filter tombstones")
