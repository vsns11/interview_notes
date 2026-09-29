"""Implicit feedback + ranking metrics: precision@k, recall@k, MAP, NDCG, hit rate,
coverage, diversity, popularity bias. Compares Popularity vs item-kNN vs implicit ALS."""
import numpy as np

from data import make_movielens_like

K = 10


# ---------- metrics for ONE user: `recs` = ranked list, `relevant` = set of held-out items ----------
def precision_at_k(recs, relevant, k=K):
    return len(set(recs[:k]) & relevant) / k


def recall_at_k(recs, relevant, k=K):
    return len(set(recs[:k]) & relevant) / len(relevant)


def average_precision_at_k(recs, relevant, k=K):
    hits, total = 0, 0.0
    for rank, item in enumerate(recs[:k], start=1):
        if item in relevant:
            hits += 1
            total += hits / rank          # precision at each position where we hit
    return total / min(len(relevant), k)


def ndcg_at_k(recs, relevant, k=K):
    dcg = sum(1 / np.log2(rank + 1) for rank, it in enumerate(recs[:k], 1) if it in relevant)
    ideal = sum(1 / np.log2(rank + 1) for rank in range(1, min(len(relevant), k) + 1))
    return dcg / ideal


def hit_rate_at_k(recs, relevant, k=K):
    return float(bool(set(recs[:k]) & relevant))


def worked_example():
    recs = ["A", "B", "C", "D", "E"]
    relevant = {"A", "C", "F"}
    print("== Worked example: recs = A B C D E, relevant = {A, C, F}, k = 5 ==")
    print(f"precision@5 = {precision_at_k(recs, relevant, 5):.3f}")
    print(f"recall@5    = {recall_at_k(recs, relevant, 5):.3f}")
    print(f"AP@5        = {average_precision_at_k(recs, relevant, 5):.3f}")
    print(f"NDCG@5      = {ndcg_at_k(recs, relevant, 5):.3f}")
    print(f"hit@5       = {hit_rate_at_k(recs, relevant, 5):.0f}\n")


# ---------- models ----------
def implicit_als(X, k=16, alpha=10.0, reg=50.0, iters=10, seed=0):
    """Weighted ALS for implicit data (Hu, Koren & Volinsky 2008).
    preference p = 1 if interacted else 0; confidence c = 1 + alpha * X."""
    rng = np.random.default_rng(seed)
    nU, nI = X.shape
    P = rng.normal(0, 0.01, (nU, k))
    Q = rng.normal(0, 0.01, (nI, k))
    pref, C = (X > 0).astype(float), 1 + alpha * X
    I_k = reg * np.eye(k)
    for _ in range(iters):
        for u in range(nU):   # small data: dense per-user solve is fine
            Cu = C[u]
            P[u] = np.linalg.solve((Q.T * Cu) @ Q + I_k, (Q.T * Cu) @ pref[u])
        for i in range(nI):
            Ci = C[:, i]
            Q[i] = np.linalg.solve((P.T * Ci) @ P + I_k, (P.T * Ci) @ pref[:, i])
    return P @ Q.T


def main():
    worked_example()
    d = make_movielens_like()
    nU, nI = d["n_users"], d["n_items"]
    X = np.zeros((nU, nI))
    X[d["users"], d["items"]] = 1          # implicit: "watched" (we ignore the stars)

    # hold out 5 random watched items per user (leave-n-out)
    rng = np.random.default_rng(1)
    train, held = X.copy(), {}
    for u in range(nU):
        items = np.flatnonzero(X[u])
        if len(items) >= 10:
            h = rng.choice(items, 5, replace=False)
            train[u, h] = 0
            held[u] = set(h.tolist())

    # tune implicit-ALS on a VALIDATION split carved out of train (never on the test items)
    fit, val = train.copy(), {}
    for u in held:
        items = np.flatnonzero(train[u])
        v = rng.choice(items, 5, replace=False)
        fit[u, v] = 0
        val[u] = set(v.tolist())
    best = None
    for alpha in (5.0, 10.0, 20.0):
        for reg in (10.0, 50.0, 100.0):
            sc = np.where(fit > 0, -np.inf, implicit_als(fit, alpha=alpha, reg=reg))
            score = np.mean([ndcg_at_k(np.argsort(-sc[u])[:K].tolist(), rel) for u, rel in val.items()])
            if best is None or score > best[0]:
                best = (score, alpha, reg)
    print(f"ALS chosen on validation split: alpha={best[1]:.0f}, lambda={best[2]:.0f} "
          f"(validation NDCG@10 {best[0]:.3f})\n")

    popularity = train.sum(0)
    Xn = train / (np.linalg.norm(train, axis=0, keepdims=True) + 1e-12)
    S = Xn.T @ Xn
    np.fill_diagonal(S, 0)

    G = d["item_genres"]
    Gn = G / np.linalg.norm(G, axis=1, keepdims=True)
    models = {
        "Popularity": np.tile(popularity, (nU, 1)),
        "Item-kNN (cosine)": train @ S,
        "Implicit ALS": implicit_als(train, alpha=best[1], reg=best[2]),
    }
    print(f"{'model':18s} P@10   R@10   MAP@10 NDCG@10 HR@10  cover  divers  avgPop")
    for name, scores in models.items():
        scores = np.where(train > 0, -np.inf, scores)   # never re-recommend what was seen
        m, rec_items, divs, pops = [], set(), [], []
        for u, rel in held.items():
            recs = np.argsort(-scores[u])[:K].tolist()
            m.append([f(recs, rel) for f in (precision_at_k, recall_at_k,
                      average_precision_at_k, ndcg_at_k, hit_rate_at_k)])
            rec_items.update(recs)
            sim = Gn[recs] @ Gn[recs].T                        # genre similarity inside the list
            divs.append(1 - sim[np.triu_indices(K, 1)].mean())  # intra-list diversity
            pops.append(popularity[recs].mean())
        m = np.mean(m, 0)
        print(f"{name:18s} " + " ".join(f"{v:.3f} " for v in m)
              + f" {len(rec_items) / nI:.2f}   {np.mean(divs):.3f}   {np.mean(pops):.0f}")
    print(f"(average item popularity in the catalogue = {popularity.mean():.0f} interactions)")


if __name__ == "__main__":
    main()
