"""Toy search ranking: why raw clicks mislead, and how inverse-propensity weighting (IPW) helps.

World: each query has 20 candidate products with 3 features.
  x0, x1 -> truly drive relevance (graded 0..3)
  x2     -> 'promoted' score that the OLD ranker leaned on, but it has NO effect on relevance
Clicks follow the examination model:  click = examined(position) AND attracted(relevance)
  P(examined at rank k) = 1 / k        (people rarely look far down the page)
We train a linear scorer three ways and measure NDCG@10 against the TRUE relevance:
  naive  - logistic loss on raw clicks
  IPW    - each click is up-weighted by 1 / P(examined at its position)  (unbiased in expectation)
  oracle - trained on the true relevance labels (upper bound; not available in real life)
"""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import torch

IMG = Path(__file__).resolve().parent.parent / "img"
rng = np.random.default_rng(1)
torch.manual_seed(1)
DOCS, POS = 20, np.arange(1, 21)
PROPENSITY = 1.0 / POS


def make_queries(n):
    x = rng.normal(size=(n, DOCS, 3))
    raw = 1.2 * x[..., 0] + 0.8 * x[..., 1] + 0.5 * rng.normal(size=(n, DOCS))
    rel = np.digitize(raw, [0.0, 1.2, 2.2])                     # graded 0,1,2,3
    return x, rel


def ndcg_at_k(scores, rel, k=10):
    order = np.argsort(-scores)[:k]
    gains = (2.0 ** rel[order] - 1) / np.log2(np.arange(2, k + 2))
    ideal = np.sort(rel)[::-1][:k]
    idcg = ((2.0 ** ideal - 1) / np.log2(np.arange(2, k + 2))).sum()
    return gains.sum() / idcg if idcg > 0 else 0.0


def log_clicks(x, rel):
    """Old ranker shows docs sorted mostly by the promoted score x2, then users click."""
    old_score = 1.5 * x[..., 2] + 0.5 * x[..., 0] + rng.normal(size=rel.shape)
    order = np.argsort(-old_score, axis=1)
    xs = np.take_along_axis(x, order[..., None], 1)
    rs = np.take_along_axis(rel, order, 1)
    attracted = rng.random(rs.shape) < np.array([0.05, 0.3, 0.6, 0.9])[rs]
    examined = rng.random(rs.shape) < PROPENSITY
    return xs, rs, (attracted & examined).astype(np.float32)


def fit_linear(x, target, weight_pos, weight_neg, epochs=300):
    """Weighted logistic loss: w_pos*log(s) + w_neg*log(1-s), full-batch Adam."""
    X = torch.tensor(x.reshape(-1, 3), dtype=torch.float32)
    wp = torch.tensor(weight_pos.reshape(-1), dtype=torch.float32)
    wn = torch.tensor(weight_neg.reshape(-1), dtype=torch.float32)
    w = torch.zeros(3, requires_grad=True)
    b = torch.zeros(1, requires_grad=True)
    opt = torch.optim.Adam([w, b], lr=0.05)
    for _ in range(epochs):
        z = X @ w + b
        loss = -(wp * torch.nn.functional.logsigmoid(z) + wn * torch.nn.functional.logsigmoid(-z)).mean()
        opt.zero_grad()
        loss.backward()
        opt.step()
    return w.detach().numpy()


if __name__ == "__main__":
    x, rel = make_queries(4000)
    xs, rs, clicks = log_clicks(x, rel)
    print("CTR by position (1..10):", np.round(clicks.mean(0)[:10], 3))
    print("mean true relevance by position (1..10):", np.round(rs.mean(0)[:10], 2))

    prop = np.broadcast_to(PROPENSITY, clicks.shape)
    ipw = clicks / np.clip(prop, 0.1, None)                  # clip: cap weights at 10x
    models = {
        "naive clicks": fit_linear(xs, clicks, clicks, 1 - clicks),
        "IPW clicks": fit_linear(xs, clicks, ipw, 1 - ipw),   # unbiased pointwise loss (weights may be < 0)
        "oracle (true rel)": fit_linear(xs, None, (rs >= 2).astype(float), (rs < 2).astype(float)),
    }

    xt, relt = make_queries(2000)
    print(f"\n{'model':<20}{'w_x0':>7}{'w_x1':>7}{'w_x2':>7}{'NDCG@10':>9}")
    for name, w in models.items():
        nd = np.mean([ndcg_at_k(xt[q] @ w, relt[q]) for q in range(len(xt))])
        print(f"{name:<20}{w[0]:>7.2f}{w[1]:>7.2f}{w[2]:>7.2f}{nd:>9.3f}")
    old = np.mean([ndcg_at_k(1.5 * xt[q, :, 2] + 0.5 * xt[q, :, 0], relt[q]) for q in range(len(xt))])
    print(f"{'old ranker':<20}{'':>21}{old:>9.3f}")

    IMG.mkdir(exist_ok=True)
    fig, ax = plt.subplots(figsize=(6.5, 3.4))
    ax.bar(POS, clicks.mean(0), color="#4C78A8", label="observed CTR")
    ax.plot(POS, rs.mean(0) / 10, "o-", color="#F58518", label="true relevance / 10")
    ax.set_xlabel("position on the results page")
    ax.set_ylabel("rate")
    ax.set_xticks(POS)
    ax.set_title("Clicks collapse with position; relevance barely changes")
    ax.legend()
    fig.tight_layout()
    fig.savefig(IMG / "search_ctr_by_position.png", dpi=150)
    print("saved img/search_ctr_by_position.png")
