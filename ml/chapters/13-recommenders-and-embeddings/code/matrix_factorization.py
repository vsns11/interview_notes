"""Tiny matrix factorisation in numpy: SGD (with biases) and ALS, on the synthetic ratings."""
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

from data import IMG, make_movielens_like, train_test_split_per_user


def rmse(pred, r):
    return float(np.sqrt(np.mean((pred - r) ** 2)))


def mf_sgd(train, test, n_users, n_items, k=16, lr=0.02, reg=0.05, epochs=40, seed=0):
    """r_hat(u,i) = mu + b_u + b_i + p_u . q_i, trained by stochastic gradient descent."""
    rng = np.random.default_rng(seed)
    (tu, ti, tr), (vu, vi, vr) = train, test
    mu = tr.mean()
    bu, bi = np.zeros(n_users), np.zeros(n_items)
    P = rng.normal(0, 0.1, (n_users, k))
    Q = rng.normal(0, 0.1, (n_items, k))
    hist = []
    for _ in range(epochs):
        for idx in rng.permutation(len(tr)):          # one rating at a time
            u, i, r = tu[idx], ti[idx], tr[idx]
            err = r - (mu + bu[u] + bi[i] + P[u] @ Q[i])
            bu[u] += lr * (err - reg * bu[u])
            bi[i] += lr * (err - reg * bi[i])
            pu = P[u].copy()
            P[u] += lr * (err * Q[i] - reg * P[u])
            Q[i] += lr * (err * pu - reg * Q[i])
        pred = lambda U, I: mu + bu[U] + bi[I] + np.sum(P[U] * Q[I], 1)
        hist.append((rmse(pred(tu, ti), tr), rmse(pred(vu, vi), vr)))
    return hist, (mu, bu, bi, P, Q)


def mf_als(train, test, n_users, n_items, k=16, reg=5.0, iters=10, seed=0):
    """Alternating least squares on mean-centred ratings: fix Q, solve every p_u exactly
    (a small ridge regression), then fix P and solve every q_i. Repeat."""
    rng = np.random.default_rng(seed)
    (tu, ti, tr), (vu, vi, vr) = train, test
    mu = tr.mean()
    P = rng.normal(0, 0.1, (n_users, k))
    Q = rng.normal(0, 0.1, (n_items, k))
    by_user = [np.flatnonzero(tu == u) for u in range(n_users)]
    by_item = [np.flatnonzero(ti == i) for i in range(n_items)]
    I_k = reg * np.eye(k)
    hist = []
    for _ in range(iters):
        for u, idx in enumerate(by_user):
            if len(idx):
                Qi = Q[ti[idx]]
                P[u] = np.linalg.solve(Qi.T @ Qi + I_k, Qi.T @ (tr[idx] - mu))
        for i, idx in enumerate(by_item):
            if len(idx):
                Pu = P[tu[idx]]
                Q[i] = np.linalg.solve(Pu.T @ Pu + I_k, Pu.T @ (tr[idx] - mu))
        pred = lambda U, I: mu + np.sum(P[U] * Q[I], 1)
        hist.append((rmse(pred(tu, ti), tr), rmse(pred(vu, vi), vr)))
    return hist


def main():
    d = make_movielens_like()
    train, test = train_test_split_per_user(d["users"], d["items"], d["ratings"])
    nU, nI = d["n_users"], d["n_items"]
    print(f"baseline (global mean) test RMSE: {rmse(train[2].mean(), test[2]):.3f}")

    hist_sgd, (mu, bu, bi, P, Q) = mf_sgd(train, test, nU, nI)
    for e in (0, 4, 9, 19, 39):
        print(f"SGD epoch {e + 1:2d}: train RMSE {hist_sgd[e][0]:.3f}  test RMSE {hist_sgd[e][1]:.3f}")
    hist_nr, _ = mf_sgd(train, test, nU, nI, k=64, reg=0.0, epochs=40)
    print(f"SGD k=64, no regularisation, epoch 40: train {hist_nr[-1][0]:.3f}  test {hist_nr[-1][1]:.3f}")

    hist_als = mf_als(train, test, nU, nI)
    for e in (0, 1, 4, 9):
        print(f"ALS iter  {e + 1:2d}: train RMSE {hist_als[e][0]:.3f}  test RMSE {hist_als[e][1]:.3f}")

    # recommend for user 0: highest predicted score among movies they have NOT rated
    u = 0
    seen = set(train[1][train[0] == u])
    scores = mu + bu[u] + bi + Q @ P[u]
    recs = [int(i) for i in np.argsort(-scores) if i not in seen][:5]
    print(f"\nTop-5 for user {u}:", recs, "predicted", np.round(scores[recs], 2))
    print("true (hidden) stars of those movies:", d["true_stars"][u, recs])

    fig, ax = plt.subplots(figsize=(7, 4))
    ep = np.arange(1, len(hist_sgd) + 1)
    ax.plot(ep, [h[0] for h in hist_sgd], label="SGD k=16 reg=0.05 train")
    ax.plot(ep, [h[1] for h in hist_sgd], label="SGD k=16 reg=0.05 test")
    ax.plot(ep, [h[0] for h in hist_nr], "--", label="SGD k=64 no reg train")
    ax.plot(ep, [h[1] for h in hist_nr], "--", label="SGD k=64 no reg test")
    ax.axhline(rmse(train[2].mean(), test[2]), color="grey", lw=1, label="global-mean baseline")
    ax.set_xlabel("epoch"); ax.set_ylabel("RMSE"); ax.set_title("Matrix factorisation learning curves")
    ax.legend(fontsize=8); fig.tight_layout()
    fig.savefig(IMG / "mf_learning_curves.png", dpi=130)
    print("saved img/mf_learning_curves.png")


if __name__ == "__main__":
    main()
