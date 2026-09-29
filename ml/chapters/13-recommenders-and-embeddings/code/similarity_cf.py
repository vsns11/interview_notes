"""User-item matrix, cosine & Jaccard similarity, content-based vs collaborative filtering."""
import numpy as np

from data import GENRES, make_movielens_like, train_test_split_per_user


def cosine(a, b):
    return a @ b / (np.linalg.norm(a) * np.linalg.norm(b) + 1e-12)


def jaccard(a, b):
    a, b = set(a), set(b)
    return len(a & b) / len(a | b) if a | b else 0.0


def tiny_examples():
    print("== Tiny user-item matrix (rows = users, cols = movies, 0 = not rated) ==")
    R = np.array([[5, 4, 0, 1],   # Ana
                  [4, 5, 1, 0],   # Ben
                  [1, 0, 5, 4]])  # Cy
    print(R)
    print(f"cosine(Ana, Ben) = {cosine(R[0], R[1]):.3f}")
    print(f"cosine(Ana, Cy)  = {cosine(R[0], R[2]):.3f}")
    # mean-centred cosine (= Pearson on co-rated items) removes 'generous rater' bias
    liked_ana, liked_ben, liked_cy = {0, 1}, {0, 1}, {2, 3}
    print(f"jaccard(Ana, Ben) on liked sets = {jaccard(liked_ana, liked_ben):.2f}")
    print(f"jaccard(Ana, Cy)  on liked sets = {jaccard(liked_ana, liked_cy):.2f}")
    print()


def predict_item_knn(R, mask, u, i, S, k=20):
    """Item-based CF: weighted average of u's ratings on the k items most similar to i."""
    rated = np.flatnonzero(mask[u])
    if len(rated) == 0:
        return np.nan
    sims = S[i, rated]
    top = np.argsort(-sims)[:k]
    w, r = sims[top], R[u, rated[top]]
    w = np.clip(w, 0, None)
    return (w @ r) / w.sum() if w.sum() > 0 else np.nan


def main():
    tiny_examples()
    d = make_movielens_like()
    (tu, ti, tr), (vu, vi, vr) = train_test_split_per_user(d["users"], d["items"], d["ratings"])
    nU, nI = d["n_users"], d["n_items"]
    R = np.zeros((nU, nI))
    R[tu, ti] = tr
    mask = R > 0

    # --- collaborative filtering: item-item cosine on mean-centred ratings ---
    user_mean = R.sum(1) / np.maximum(mask.sum(1), 1)
    Rc = np.where(mask, R - user_mean[:, None], 0.0)          # adjusted cosine
    norms = np.linalg.norm(Rc, axis=0) + 1e-12
    S_cf = (Rc.T @ Rc) / np.outer(norms, norms)
    np.fill_diagonal(S_cf, 0)

    # --- content-based: item-item cosine on genre vectors ---
    G = d["item_genres"]
    Gn = G / np.linalg.norm(G, axis=1, keepdims=True)
    S_cb = Gn @ Gn.T
    np.fill_diagonal(S_cb, 0)

    def rmse(S):
        preds = []
        for u, i, r in zip(vu, vi, vr):
            p = predict_item_knn(Rc, mask, u, i, S)
            preds.append(user_mean[u] + (0 if np.isnan(p) else p))
        return np.sqrt(np.mean((np.array(preds) - vr) ** 2))

    global_mean = tr.mean()
    print("== Rating prediction on held-out ratings (RMSE, lower is better) ==")
    print(f"predict global mean      : {np.sqrt(np.mean((global_mean - vr) ** 2)):.3f}")
    print(f"predict user's mean      : {np.sqrt(np.mean((user_mean[vu] - vr) ** 2)):.3f}")
    print(f"content-based item-kNN   : {rmse(S_cb):.4f}")
    print(f"collaborative item-kNN   : {rmse(S_cf):.4f}")
    print(f"hybrid (average of both) : {rmse(0.5 * S_cf + 0.5 * S_cb):.4f}")

    i = int(np.argmax(mask.sum(0)))  # most-rated movie
    g = lambda j: "+".join(GENRES[k] for k in np.flatnonzero(G[j]))
    print(f"\nMovie {i} ({g(i)}) - 5 nearest neighbours")
    print("  collaborative:", [f"{j}:{g(j)}" for j in np.argsort(-S_cf[i])[:5]])
    print("  content-based:", [f"{j}:{g(j)}" for j in np.argsort(-S_cb[i])[:5]])


if __name__ == "__main__":
    main()
