"""Toy video recommender: compare candidate sources by recall@K, then check the union.

Synthetic world: users and videos live in a hidden 'taste space'. A user watches videos that
are close to their taste (plus some popular ones). We hold out each user's LAST watches and ask:
which candidate source would have retrieved them?

Sources:
  1) popularity          - same list for everyone (the baseline)
  2) co-occurrence       - "people who watched X also watched Y", seeded by the user's recent watches
  3) two-tower (MF)      - learned user and item vectors, trained with sampled negatives (numpy SGD)
  4) union of 1+2+3      - what a real multi-source retrieval stage does
"""
import numpy as np

rng = np.random.default_rng(7)
N_USERS, N_ITEMS, DIM, HIST, TEST = 2000, 3000, 8, 30, 5


def make_world():
    users = rng.normal(size=(N_USERS, DIM))
    items = rng.normal(size=(N_ITEMS, DIM))
    pop = rng.zipf(1.6, N_ITEMS).clip(max=200).astype(float)      # a few very popular videos
    logits = 1.5 * users @ items.T / np.sqrt(DIM) + 0.4 * np.log(pop)    # taste + popularity
    watches = []
    for u in range(N_USERS):
        p = np.exp(logits[u] - logits[u].max())
        p /= p.sum()
        watches.append(rng.choice(N_ITEMS, size=HIST + TEST, replace=False, p=p))
    return np.array(watches)                                        # order = time order


def recall_at_k(cands, held_out):
    return np.mean([len(set(c) & set(h)) / len(h) for c, h in zip(cands, held_out)])


def popularity(train, k):
    counts = np.bincount(train.ravel(), minlength=N_ITEMS)
    top = np.argsort(-counts)[:k]
    return [top] * len(train)


def co_occurrence(train, k, recent=5):
    co = np.zeros((N_ITEMS, N_ITEMS), dtype=np.float32)
    for row in train:                                  # count item pairs watched by the same user
        co[np.ix_(row, row)] += 1
    np.fill_diagonal(co, 0)
    co /= np.sqrt(co.sum(1, keepdims=True) + 1)        # damp very popular items a little
    out = []
    for row in train:
        s = co[row[-recent:]].sum(0)                   # seed with the user's most recent watches
        s[row] = -np.inf                               # never recommend what they already watched
        out.append(np.argsort(-s)[:k])
    return out


def two_tower(train, k, dim=16, epochs=15, lr=0.05, reg=1e-4, n_neg=5):
    """Matrix factorisation = the simplest 'two-tower': user tower and item tower are embedding tables."""
    U = 0.1 * rng.normal(size=(N_USERS, dim))
    V = 0.1 * rng.normal(size=(N_ITEMS, dim))
    pairs = np.array([(u, i) for u, row in enumerate(train) for i in row])
    for _ in range(epochs):
        rng.shuffle(pairs)
        for u, i in pairs:
            negs = rng.integers(0, N_ITEMS, n_neg)
            items = np.concatenate(([i], negs))
            labels = np.zeros(n_neg + 1)
            labels[0] = 1
            scores = V[items] @ U[u]
            g = 1 / (1 + np.exp(-scores)) - labels           # logistic-loss gradient
            gu = g @ V[items] + reg * U[u]
            V[items] -= lr * (np.outer(g, U[u]) + reg * V[items])
            U[u] -= lr * gu
    out = []
    for u, row in enumerate(train):
        s = V @ U[u]                                          # in production: ANN search, not a full scan
        s[row] = -np.inf
        out.append(np.argsort(-s)[:k])
    return out


if __name__ == "__main__":
    watches = make_world()
    train, test = watches[:, :HIST], watches[:, HIST:]
    K = 100
    pop = popularity(train, K)
    co = co_occurrence(train, K)
    tt = two_tower(train, K)
    union = [np.unique(np.concatenate(x)) for x in zip(pop, co, tt)]

    print(f"{N_USERS} users, {N_ITEMS} videos, {HIST} past watches each, last {TEST} held out\n")
    print(f"{'candidate source':<24}{'size':>6}{'recall@K':>10}")
    for name, c in [("popularity", pop), ("co-occurrence", co), ("two-tower (MF)", tt)]:
        print(f"{name:<24}{K:>6}{recall_at_k(c, test):>10.3f}")
    print(f"{'union of the three':<24}{np.mean([len(x) for x in union]):>6.0f}{recall_at_k(union, test):>10.3f}")

    # marginal value: share of held-out hits that ONLY this source found
    for name, c, others in [("popularity", pop, (co, tt)), ("co-occurrence", co, (pop, tt)),
                            ("two-tower (MF)", tt, (pop, co))]:
        only = np.mean([len((set(a) - set(b) - set(d)) & set(h)) / TEST
                        for a, b, d, h in zip(c, *others, test)])
        print(f"hits found ONLY by {name:<16}: {only:.3f} of held-out watches")
