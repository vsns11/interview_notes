"""Toy feed ranker: one shared-body model with three heads (like, comment, hide) + a value formula.

Synthetic posts have 6 features. Some posts are 'bait': they attract likes but also many hides.
We train a small multi-task network (PyTorch) on logged impressions, then rank each user's
candidate posts two ways and compare what lands in the top 10:
  A) by P(like) alone
  B) by value formulas   score = w1*P(like) + w2*P(comment) - w3*P(hide), two weight settings
"""
import numpy as np
import torch
from sklearn.metrics import roc_auc_score
from torch import nn

torch.manual_seed(0)
rng = np.random.default_rng(0)
N_FEAT = 6


def simulate(n):
    """Return features and the TRUE probabilities of like / comment / hide for each impression."""
    x = rng.normal(size=(n, N_FEAT)).astype(np.float32)
    affinity, closeness, quality, bait = x[:, 0], x[:, 1], x[:, 2], x[:, 3]
    sig = lambda z: 1 / (1 + np.exp(-z))
    p_like = sig(-1.5 + 0.8 * affinity + 0.5 * quality + 1.2 * bait)
    p_comment = sig(-3.0 + 1.0 * closeness + 0.6 * quality)
    p_hide = sig(-4.0 - 0.5 * affinity + 1.8 * bait)
    return x, np.stack([p_like, p_comment, p_hide], 1)


class MultiTask(nn.Module):
    def __init__(self):
        super().__init__()
        self.body = nn.Sequential(nn.Linear(N_FEAT, 32), nn.ReLU(), nn.Linear(32, 16), nn.ReLU())
        self.heads = nn.ModuleList([nn.Linear(16, 1) for _ in range(3)])   # like, comment, hide

    def forward(self, x):
        h = self.body(x)                                   # shared representation, computed once
        return torch.cat([head(h) for head in self.heads], 1)   # 3 logits


if __name__ == "__main__":
    # 1) logged impressions: sample actual events from the true probabilities
    x, p_true = simulate(60_000)
    y = (rng.random(p_true.shape) < p_true).astype(np.float32)
    print("event rates in logs  like={:.3f} comment={:.3f} hide={:.3f}".format(*y.mean(0)))

    xtr, ytr, xte, yte = x[:50_000], y[:50_000], x[50_000:], y[50_000:]
    model = MultiTask()
    opt = torch.optim.Adam(model.parameters(), lr=3e-3)
    loss_fn = nn.BCEWithLogitsLoss()                        # one loss per head, summed
    X, Y = torch.tensor(xtr), torch.tensor(ytr)
    for epoch in range(8):
        perm = torch.randperm(len(X))
        for i in range(0, len(X), 512):
            idx = perm[i:i + 512]
            logits = model(X[idx])
            loss = sum(loss_fn(logits[:, k], Y[idx, k]) for k in range(3))
            opt.zero_grad()
            loss.backward()
            opt.step()

    with torch.no_grad():
        p_hat = torch.sigmoid(model(torch.tensor(xte))).numpy()
    for k, name in enumerate(["like", "comment", "hide"]):
        print(f"head {name:<8} AUC={roc_auc_score(yte[:, k], p_hat[:, k]):.3f}  "
              f"mean pred={p_hat[:, k].mean():.3f} vs actual={yte[:, k].mean():.3f}")

    # 2) ranking: 500 users x 200 candidate posts, keep top 10, measure TRUE expected outcomes
    formulas = {"P(like) only": np.array([1.0, 0.0, 0.0]),
                "gentle 1/2/-3": np.array([1.0, 2.0, -3.0]),
                "strong 1/4/-10": np.array([1.0, 4.0, -10.0])}
    res = {name: np.zeros(3) for name in formulas}
    for _ in range(500):
        xc, pc = simulate(200)
        with torch.no_grad():
            ph = torch.sigmoid(model(torch.tensor(xc))).numpy()
        for name, w in formulas.items():
            top = np.argsort(-(ph @ w))[:10]
            res[name] += pc[top].sum(0) / 500
    print("\nexpected events per user in the top 10 (true probabilities):")
    print(f"{'ranking':<16}{'likes':>7}{'comments':>10}{'hides':>7}")
    for name, (l, c, h) in res.items():
        print(f"{name:<16}{l:>7.2f}{c:>10.2f}{h:>7.2f}")
