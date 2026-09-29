"""A tiny two-tower retrieval model in PyTorch with in-batch negatives.
User tower: user-id embedding + average genre profile of the user's history.
Item tower: item-id embedding + genre multi-hot (content) -> works for brand-new items too."""
import numpy as np
import torch
import torch.nn as nn
import torch.nn.functional as F

from data import make_movielens_like

torch.manual_seed(0)
DIM = 32


class Tower(nn.Module):
    def __init__(self, n_ids, n_feats, id_dropout=0.0):
        super().__init__()
        self.id_emb = nn.Embedding(n_ids, 32)
        self.mlp = nn.Sequential(nn.Linear(32 + n_feats, 64), nn.ReLU(), nn.Linear(64, DIM))
        self.id_dropout = id_dropout   # randomly hide the id so the tower also learns from content

    def forward(self, ids, feats, use_id=True):
        e = self.id_emb(ids)
        if not use_id:
            e = torch.zeros_like(e)
        elif self.training and self.id_dropout:
            keep = (torch.rand(len(ids), 1) > self.id_dropout).float()
            e = e * keep
        return F.normalize(self.mlp(torch.cat([e, feats], 1)), dim=1)   # unit-length vectors


def main():
    d = make_movielens_like()
    nU, nI = d["n_users"], d["n_items"]
    G = torch.tensor(d["item_genres"], dtype=torch.float32)
    rng = np.random.default_rng(0)
    cold = rng.choice(nI, 40, replace=False)               # 40 items that never appear in training
    is_cold = np.zeros(nI, bool); is_cold[cold] = True

    users, items = d["users"], d["items"]
    test = rng.random(len(users)) < 0.2
    tr = ~test & ~is_cold[items]
    tu, ti = users[tr], items[tr]

    # user features: average genre vector of the user's training history
    prof = np.zeros((nU, G.shape[1]))
    np.add.at(prof, tu, d["item_genres"][ti])
    prof /= np.maximum(prof.sum(1, keepdims=True), 1)
    U_feat = torch.tensor(prof, dtype=torch.float32)

    item_count = np.bincount(ti, minlength=nI) + 1
    log_q = torch.tensor(np.log(item_count / item_count.sum()), dtype=torch.float32)

    user_tower, item_tower = Tower(nU, G.shape[1]), Tower(nI, G.shape[1], id_dropout=0.3)
    opt = torch.optim.Adam(list(user_tower.parameters()) + list(item_tower.parameters()), lr=3e-3)
    tu_t, ti_t = torch.tensor(tu), torch.tensor(ti)
    temperature = 0.1
    for epoch in range(15):
        perm = torch.randperm(len(tu_t))
        total = 0.0
        for b in range(0, len(perm), 512):
            idx = perm[b:b + 512]
            u, i = tu_t[idx], ti_t[idx]
            uv = user_tower(u, U_feat[u])
            iv = item_tower(i, G[i])
            logits = uv @ iv.T / temperature        # (B, B): every other item in the batch is a negative
            logits = logits - log_q[i][None, :]      # logQ correction: popular items appear as
            loss = F.cross_entropy(logits, torch.arange(len(idx)))  # negatives too often otherwise
            opt.zero_grad(); loss.backward(); opt.step()
            total += loss.item() * len(idx)
        if epoch in (0, 4, 14):
            print(f"epoch {epoch + 1:2d}  in-batch softmax loss {total / len(perm):.3f}")

    user_tower.eval(); item_tower.eval()
    with torch.no_grad():
        # OFFLINE: embed the whole catalogue once (cold items use content only)
        all_items = torch.arange(nI)
        warm_vecs = item_tower(all_items, G)
        content_vecs = item_tower(all_items, G, use_id=False)
        item_vecs = torch.where(torch.tensor(is_cold)[:, None], content_vecs, warm_vecs)
        # ONLINE: one user-tower pass per request
        user_vecs = user_tower(torch.arange(nU), U_feat)
        scores = (user_vecs @ item_vecs.T).numpy()

    seen = np.zeros((nU, nI), bool); seen[tu, ti] = True
    warm_scores = np.where(seen | is_cold[None, :], -np.inf, scores)
    pop = np.where(is_cold, -np.inf, item_count.astype(float))
    te = test & ~is_cold[items]
    held = {}
    for u, i in zip(users[te], items[te]):
        held.setdefault(u, set()).add(i)

    def recall(score_fn, k=50):
        return np.mean([len(set(np.argsort(-score_fn(u))[:k]) & rel) / len(rel) for u, rel in held.items()])

    print(f"\nwarm items  recall@50: two-tower {recall(lambda u: warm_scores[u]):.3f}"
          f" | popularity {recall(lambda u: np.where(seen[u], -np.inf, pop)):.3f}")

    # COLD START: users who interacted with a cold item -- can we rank it among the 40 cold ones?
    hits = []
    for u, i in zip(users[is_cold[items]], items[is_cold[items]]):
        ranked = cold[np.argsort(-scores[u, cold])]
        hits.append(i in ranked[:10])
    print(f"cold items  hit@10 among the 40 new items: {np.mean(hits):.3f} (random = {10 / 40:.3f})")


if __name__ == "__main__":
    main()
