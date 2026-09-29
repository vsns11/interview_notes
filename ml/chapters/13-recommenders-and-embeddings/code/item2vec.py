"""item2vec: word2vec skip-gram with negative sampling, trained on "sessions" of item ids.
Items watched in the same sessions end up with nearby vectors -- no genre labels used."""
import numpy as np

from data import GENRES, make_movielens_like


def make_sessions(d, seed=0):
    rng = np.random.default_rng(seed)
    sessions = []
    for u in range(d["n_users"]):
        items = d["items"][d["users"] == u]
        rng.shuffle(items)
        sessions += [items[i:i + 8] for i in range(0, len(items), 8)]  # sessions of ~8 items
    return sessions


def train_sgns(sessions, n_items, dim=16, window=3, neg=5, lr=0.05, epochs=10, seed=0):
    rng = np.random.default_rng(seed)
    W_in = rng.normal(0, 0.1, (n_items, dim))    # the embeddings we keep
    W_out = rng.normal(0, 0.1, (n_items, dim))   # "context" vectors
    # (centre, context) positive pairs from a sliding window
    pairs = np.array([(s[i], s[j]) for s in sessions for i in range(len(s))
                      for j in range(max(0, i - window), min(len(s), i + window + 1)) if i != j])
    freq = np.bincount(np.concatenate(sessions), minlength=n_items) ** 0.75
    noise = freq / freq.sum()                    # negatives ~ unigram^0.75, as in word2vec
    sig = lambda x: 1 / (1 + np.exp(-x))
    for ep in range(epochs):
        rng.shuffle(pairs)
        loss = 0.0
        for b in range(0, len(pairs), 256):
            c, o = pairs[b:b + 256, 0], pairs[b:b + 256, 1]
            negs = rng.choice(n_items, (len(c), neg), p=noise)
            vc = W_in[c]                                          # (B, d)
            pos = sig(np.sum(vc * W_out[o], 1))                   # want -> 1
            ngs = sig(np.einsum("bd,bkd->bk", vc, W_out[negs]))   # want -> 0
            loss += -np.sum(np.log(pos + 1e-9)) - np.sum(np.log(1 - ngs + 1e-9))
            g_pos = (pos - 1)[:, None]                            # dL/dscore for positives
            g_neg = ngs[:, :, None]                               # dL/dscore for negatives
            grad_c = g_pos * W_out[o] + np.sum(g_neg * W_out[negs], 1)
            np.add.at(W_out, o, -lr * g_pos * vc)
            np.add.at(W_out, negs, -lr * g_neg * vc[:, None, :])
            np.add.at(W_in, c, -lr * grad_c)
        if ep in (0, epochs - 1):
            print(f"epoch {ep + 1:2d}  mean loss per pair {loss / len(pairs):.3f}")
    return W_in


def main():
    d = make_movielens_like()
    sessions = make_sessions(d)
    E = train_sgns(sessions, d["n_items"])
    En = E / np.linalg.norm(E, axis=1, keepdims=True)
    S = En @ En.T
    np.fill_diagonal(S, -1)
    main_genre = d["item_genres"].argmax(1)
    top5 = np.argsort(-S, axis=1)[:, :5]
    same = (main_genre[top5] == main_genre[:, None]).mean()
    chance = np.mean([(main_genre == g).mean() for g in main_genre])
    print(f"top-5 neighbours sharing the main genre: {same:.0%} (random would give ~{chance:.0%})")
    i = int(np.bincount(d["items"]).argmax())
    print(f"item {i} ({GENRES[main_genre[i]]}) neighbours:",
          [f"{j}:{GENRES[main_genre[j]]}" for j in top5[i]])


if __name__ == "__main__":
    main()
