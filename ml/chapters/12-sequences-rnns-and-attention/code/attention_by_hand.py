"""Scaled dot-product attention with small numbers, causal masking, multi-head shapes,
and sinusoidal positional encodings."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import torch
import torch.nn.functional as F

IMG = Path(__file__).resolve().parent.parent / "img"
np.set_printoptions(precision=3, suppress=True)


def softmax(z, axis=-1):
    z = z - z.max(axis=axis, keepdims=True)          # numerical stability
    e = np.exp(z)
    return e / e.sum(axis=axis, keepdims=True)


def attention(Q, K, V, mask=None):
    d_k = Q.shape[-1]
    scores = Q @ K.T / np.sqrt(d_k)                  # similarity of every query with every key
    if mask is not None:
        scores = np.where(mask, scores, -np.inf)     # hide disallowed positions
    weights = softmax(scores)                        # each row sums to 1
    return weights @ V, scores, weights              # weighted average of values


def positional_encoding(n_pos, d_model):
    pos = np.arange(n_pos)[:, None]
    i = np.arange(0, d_model, 2)[None, :]
    angle = pos / (10000 ** (i / d_model))
    pe = np.zeros((n_pos, d_model))
    pe[:, 0::2] = np.sin(angle)
    pe[:, 1::2] = np.cos(angle)
    return pe


if __name__ == "__main__":
    # 3 tokens ("the", "cat", "sat"), d_k = 2. In a transformer Q, K, V = X Wq, X Wk, X Wv.
    Q = np.array([[1.0, 0.0], [0.0, 1.0], [1.0, 1.0]])
    K = np.array([[1.0, 0.0], [0.0, 1.0], [1.0, 1.0]])
    V = np.array([[1.0, 2.0], [3.0, 4.0], [5.0, 6.0]])
    out, scores, w = attention(Q, K, V)
    print("scores = Q K^T / sqrt(2):\n", scores)
    print("attention weights (softmax per row):\n", w)
    print("output = weights @ V:\n", out)
    ref = F.scaled_dot_product_attention(*(torch.tensor(a)[None] for a in (Q, K, V)))[0].numpy()
    print("matches torch.nn.functional.scaled_dot_product_attention:", np.allclose(out, ref))

    causal = np.tril(np.ones((3, 3), dtype=bool))    # token i may only look at tokens <= i
    out_c, _, w_c = attention(Q, K, V, causal)
    print("causal (decoder) weights:\n", w_c)
    ref_c = F.scaled_dot_product_attention(*(torch.tensor(a)[None] for a in (Q, K, V)), is_causal=True)[0].numpy()
    print("causal output matches torch (is_causal=True):", np.allclose(out_c, ref_c))

    # why divide by sqrt(d_k): dot products of random vectors grow with dimension
    rng = np.random.default_rng(0)
    for d in [4, 64, 512]:
        q, k = rng.standard_normal((1000, d)), rng.standard_normal((1000, d))
        dots = (q * k).sum(1)
        print(f"d_k={d:3d}: std of q.k = {dots.std():6.2f}   std after / sqrt(d_k) = {(dots / np.sqrt(d)).std():.2f}")

    # multi-head attention: shapes only
    mha = torch.nn.MultiheadAttention(embed_dim=16, num_heads=4, batch_first=True)
    x = torch.randn(2, 5, 16)                        # batch 2, 5 tokens, model width 16
    y, attn = mha(x, x, x)                           # self-attention: Q, K, V all come from x
    print("MultiheadAttention: input", tuple(x.shape), "-> output", tuple(y.shape),
          "| averaged attention weights", tuple(attn.shape))

    pe = positional_encoding(50, 64)
    print("positional encoding rows 0 and 1 (first 4 dims):", pe[0, :4], pe[1, :4])
    fig, axes = plt.subplots(1, 2, figsize=(11, 3.8))
    im = axes[0].imshow(pe, aspect="auto", cmap="RdBu_r"); fig.colorbar(im, ax=axes[0])
    axes[0].set_xlabel("embedding dimension"); axes[0].set_ylabel("position"); axes[0].set_title("Sinusoidal positional encoding")
    im2 = axes[1].imshow(w, cmap="Blues", vmin=0, vmax=1)
    for i in range(3):
        for j in range(3):
            axes[1].text(j, i, f"{w[i, j]:.2f}", ha="center", va="center")
    labels = ["the", "cat", "sat"]
    axes[1].set_xticks(range(3), labels); axes[1].set_yticks(range(3), labels)
    axes[1].set_xlabel("key (attended to)"); axes[1].set_ylabel("query"); axes[1].set_title("Worked example: attention weights")
    fig.tight_layout(); fig.savefig(IMG / "attention_and_positions.png", dpi=120)
    print("saved img/attention_and_positions.png")
