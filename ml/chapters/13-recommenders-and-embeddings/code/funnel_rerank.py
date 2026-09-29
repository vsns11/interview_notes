"""The recommendation funnel end to end:
candidate generation (item-kNN, top-100) -> ranking (logistic regression on richer
features incl. an ALS score) -> re-ranking (MMR for diversity). Metrics after each stage."""
import numpy as np
from sklearn.linear_model import LogisticRegression

from data import make_movielens_like
from implicit_eval import implicit_als, ndcg_at_k, recall_at_k

N_CAND, K = 100, 10


def mmr(cands, relevance, sim, lam=0.7, k=K):
    """Maximal Marginal Relevance: pick the item with the best
    lam * relevance - (1 - lam) * (max similarity to anything already picked)."""
    chosen, pool = [], list(range(len(cands)))
    while len(chosen) < k:
        best = max(pool, key=lambda j: lam * relevance[j]
                   - (1 - lam) * max((sim[cands[j], cands[c]] for c in chosen), default=0))
        chosen.append(best); pool.remove(best)
    return [cands[j] for j in chosen]


def main():
    d = make_movielens_like()
    nU, nI = d["n_users"], d["n_items"]
    X = np.zeros((nU, nI)); X[d["users"], d["items"]] = 1
    stars = np.zeros((nU, nI)); stars[d["users"], d["items"]] = d["ratings"]
    rng = np.random.default_rng(1)
    train, held = X.copy(), {}
    for u in range(nU):
        items = np.flatnonzero(X[u])
        if len(items) >= 10:
            h = rng.choice(items, 5, replace=False)
            train[u, h] = 0; held[u] = set(h.tolist())
    stars_tr = stars * train

    # ---- stage 1: candidate generation (cheap, recall-oriented): item-kNN ----
    Xn = train / (np.linalg.norm(train, axis=0, keepdims=True) + 1e-12)
    S_items = Xn.T @ Xn; np.fill_diagonal(S_items, 0)
    knn = np.where(train > 0, -np.inf, train @ S_items)
    cands = {u: np.argsort(-knn[u])[:N_CAND] for u in held}
    als = implicit_als(train)          # a heavier model: only scored on the 100 candidates

    # ---- stage 2: ranking features for each (user, candidate) ----
    G = d["item_genres"]; Gn = G / np.linalg.norm(G, axis=1, keepdims=True)
    profile = train @ G; profile /= np.maximum(profile.sum(1, keepdims=True), 1)
    pop = np.log1p(train.sum(0))
    avg_star = stars_tr.sum(0) / np.maximum(train.sum(0), 1)
    def feats(u):
        c = cands[u]
        return np.column_stack([knn[u, c], als[u, c], pop[c], G[c] @ profile[u], avg_star[c]])

    users = np.array(sorted(held)); rng.shuffle(users)
    fit_users, eval_users = users[: len(users) // 2], users[len(users) // 2:]   # split by user
    Xf = np.vstack([feats(u) for u in fit_users])
    yf = np.concatenate([[c in held[u] for c in cands[u]] for u in fit_users])
    ranker = LogisticRegression(max_iter=1000).fit((Xf - Xf.mean(0)) / Xf.std(0), yf)
    mu, sd = Xf.mean(0), Xf.std(0)

    S = Gn @ Gn.T
    def report(name, lists):
        rec = np.mean([recall_at_k(lists[u], held[u], K) for u in eval_users])
        nd = np.mean([ndcg_at_k(lists[u], held[u], K) for u in eval_users])
        div = np.mean([1 - S[np.ix_(l, l)][np.triu_indices(K, 1)].mean() for l in lists.values()])
        print(f"{name:34s} recall@10 {rec:.3f}  NDCG@10 {nd:.3f}  diversity {div:.3f}")

    cand_recall = np.mean([recall_at_k(list(cands[u]), held[u], N_CAND) for u in eval_users])
    print(f"stage 1 candidate recall@{N_CAND} = {cand_recall:.3f}  (ceiling for later stages)")
    stage1 = {u: list(cands[u][:K]) for u in eval_users}
    report("stage 1 only (kNN order)", stage1)

    probs = {u: ranker.predict_proba((feats(u) - mu) / sd)[:, 1] for u in eval_users}
    stage2 = {u: list(cands[u][np.argsort(-probs[u])][:K]) for u in eval_users}
    report("stage 2 ranker (logistic regr.)", stage2)

    stage3 = {u: mmr(list(cands[u][np.argsort(-probs[u])][:30]),
                     np.sort(probs[u])[::-1][:30] / probs[u].max(), S) for u in eval_users}
    report("stage 3 re-rank (MMR, lambda=0.7)", stage3)
    names = ["knn_score", "als_score", "log_popularity", "genre_match", "avg_star"]
    print("ranker weights:", {n: round(float(w), 2) for n, w in zip(names, ranker.coef_[0])})


if __name__ == "__main__":
    main()
