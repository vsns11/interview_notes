"""Scaled dot-product self-attention step by step, with tiny numbers.

3 tokens, d_model = 4, one head with d_k = d_v = 2, then a causal mask,
then 2-head attention. Saves an attention heatmap to img/.
"""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

np.set_printoptions(precision=3, suppress=True)
tokens = ["the", "cat", "sat"]


def softmax(z: np.ndarray, axis: int = -1) -> np.ndarray:
    z = z - z.max(axis=axis, keepdims=True)   # subtract max for numerical stability
    e = np.exp(z)
    return e / e.sum(axis=axis, keepdims=True)


def attention(Q, K, V, mask=None):
    d_k = Q.shape[-1]
    scores = Q @ K.T / np.sqrt(d_k)            # (T, T): how well each query matches each key
    if mask is not None:
        scores = np.where(mask, scores, -np.inf)
    weights = softmax(scores, axis=-1)         # each row sums to 1
    return weights @ V, weights


if __name__ == "__main__":
    # Step 0: token embeddings (already include position info), shape (T=3, d_model=4)
    X = np.array([[1.0, 0.0, 1.0, 0.0],    # the
                  [0.0, 1.0, 0.0, 1.0],    # cat
                  [1.0, 1.0, 1.0, 1.0]])   # sat

    # Step 1: learned projections (here: small hand-picked integers), shape (4, 2)
    W_Q = np.array([[1, 0], [0, 1], [1, 0], [0, 1]], dtype=float)
    W_K = np.array([[0, 1], [1, 0], [0, 1], [1, 0]], dtype=float)
    W_V = np.array([[1, 0], [0, 1], [1, 0], [0, 0]], dtype=float)
    Q, K, V = X @ W_Q, X @ W_K, X @ W_V
    print("Q =\n", Q, "\nK =\n", K, "\nV =\n", V)

    # Step 2: raw scores Q K^T
    raw = Q @ K.T
    print("\nraw scores Q.K^T =\n", raw)

    # Step 3: scale by sqrt(d_k) = sqrt(2)
    print("\nscaled scores =\n", raw / np.sqrt(2))

    # Step 4 + 5: softmax row-wise, then weighted sum of V
    out, w = attention(Q, K, V)
    print("\nattention weights (rows sum to 1) =\n", w)
    print("\noutput = weights @ V =\n", out)

    # Causal mask: token i may only look at tokens 0..i (used in GPT-style decoders)
    causal = np.tril(np.ones((3, 3), dtype=bool))
    out_c, w_c = attention(Q, K, V, mask=causal)
    print("\ncausal mask =\n", causal.astype(int))
    print("causal attention weights =\n", w_c)

    # Multi-head: split a 4-dim projection into 2 heads of size 2, attend, concatenate, mix
    rng = np.random.default_rng(0)
    d_model, h = 4, 2
    d_head = d_model // h
    Wq, Wk, Wv, Wo = (rng.normal(scale=0.5, size=(d_model, d_model)) for _ in range(4))
    Qa, Ka, Va = X @ Wq, X @ Wk, X @ Wv
    heads = []
    for i in range(h):
        sl = slice(i * d_head, (i + 1) * d_head)
        o, wi = attention(Qa[:, sl], Ka[:, sl], Va[:, sl], mask=causal)
        heads.append(o)
        print(f"\nhead {i} weights =\n", wi)
    mha = np.concatenate(heads, axis=-1) @ Wo
    print("\nmulti-head output shape:", mha.shape)

    out_path = Path(__file__).resolve().parent.parent / "img" / "attention_weights.png"
    fig, axes = plt.subplots(1, 2, figsize=(7, 3.2))
    for ax, m, title in [(axes[0], w, "full (encoder)"), (axes[1], w_c, "causal (decoder)")]:
        ax.imshow(m, cmap="Blues", vmin=0, vmax=1)
        ax.set_xticks(range(3), tokens)
        ax.set_yticks(range(3), tokens)
        ax.set_xlabel("key (attended to)")
        ax.set_title(title)
        for i in range(3):
            for j in range(3):
                ax.text(j, i, f"{m[i, j]:.2f}", ha="center", va="center",
                        color="white" if m[i, j] > 0.5 else "black")
    axes[0].set_ylabel("query (token doing the looking)")
    fig.tight_layout()
    fig.savefig(out_path, dpi=130)
    print("saved", out_path.name)
