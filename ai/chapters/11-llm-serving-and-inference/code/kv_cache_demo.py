"""KV cache vs no cache on a toy decoder-only transformer (NumPy only).

The toy model has L layers of single-head causal self-attention + a small MLP.
We generate tokens greedily two ways:
  1. no cache  - every step re-runs the whole sequence through the model
  2. KV cache  - prefill the prompt once, then each decode step processes only
                 the newest token and appends its K and V to the cache
Both must produce exactly the same tokens; the cached one is much faster.
"""
import time
import numpy as np

VOCAB, D, LAYERS = 64, 128, 4
rng = np.random.default_rng(0)

# ---- random but fixed weights -------------------------------------------
emb = rng.normal(0, 0.5, (VOCAB, D))
W = [
    {
        "q": rng.normal(0, D**-0.5, (D, D)),
        "k": rng.normal(0, D**-0.5, (D, D)),
        "v": rng.normal(0, D**-0.5, (D, D)),
        "o": rng.normal(0, D**-0.5, (D, D)),
        "m1": rng.normal(0, D**-0.5, (D, 2 * D)),
        "m2": rng.normal(0, (2 * D) ** -0.5, (2 * D, D)),
    }
    for _ in range(LAYERS)
]
out_head = rng.normal(0, D**-0.5, (D, VOCAB))


def pos_enc(positions):
    """Sinusoidal positions so the model knows token order."""
    i = np.arange(D // 2)
    angles = positions[:, None] / (10000 ** (2 * i / D))
    return np.concatenate([np.sin(angles), np.cos(angles)], axis=1)


def rms_norm(x):
    return x / np.sqrt((x**2).mean(-1, keepdims=True) + 1e-6)


def softmax(x):
    x = x - x.max(-1, keepdims=True)
    e = np.exp(x)
    return e / e.sum(-1, keepdims=True)


def block(x, w, k_past=None, v_past=None):
    """One transformer block. x: (T_new, D). Returns output and full K, V."""
    h = rms_norm(x)
    q, k, v = h @ w["q"], h @ w["k"], h @ w["v"]
    if k_past is not None:                      # decode: reuse cached K, V
        k = np.concatenate([k_past, k])
        v = np.concatenate([v_past, v])
    t_new, t_all = q.shape[0], k.shape[0]
    scores = q @ k.T / np.sqrt(D)               # (T_new, T_all)
    # causal mask: new token i (absolute position t_all - t_new + i) sees <= itself
    mask = np.triu(np.ones((t_new, t_all)), k=t_all - t_new + 1).astype(bool)
    scores[mask] = -1e9
    x = x + softmax(scores) @ v @ w["o"]
    x = x + np.maximum(0, rms_norm(x) @ w["m1"]) @ w["m2"]
    return x, k, v


def generate_no_cache(prompt, n_new):
    seq = list(prompt)
    for _ in range(n_new):
        x = emb[seq] + pos_enc(np.arange(len(seq)))
        for w in W:
            x, _, _ = block(x, w)               # K, V thrown away every step
        seq.append(int(np.argmax(rms_norm(x[-1]) @ out_head)))
    return seq[len(prompt):]


def generate_with_cache(prompt, n_new):
    # --- prefill: whole prompt in one parallel pass, keep K and V ---
    x = emb[prompt] + pos_enc(np.arange(len(prompt)))
    cache = []
    for w in W:
        x, k, v = block(x, w)
        cache.append((k, v))
    out = [int(np.argmax(rms_norm(x[-1]) @ out_head))]
    # --- decode: one token per step, attend to cached K, V ---
    for step in range(n_new - 1):
        pos = len(prompt) + step
        x = emb[[out[-1]]] + pos_enc(np.array([pos]))
        for i, w in enumerate(W):
            x, k, v = block(x, w, *cache[i])
            cache[i] = (k, v)
        out.append(int(np.argmax(rms_norm(x[-1]) @ out_head)))
    kv_floats = sum(k.size + v.size for k, v in cache)
    return out, kv_floats


if __name__ == "__main__":
    prompt = list(rng.integers(0, VOCAB, 32))
    n_new = 256

    t0 = time.perf_counter()
    a = generate_no_cache(prompt, n_new)
    t_nc = time.perf_counter() - t0

    t0 = time.perf_counter()
    b, kv_floats = generate_with_cache(prompt, n_new)
    t_c = time.perf_counter() - t0

    print(f"prompt tokens={len(prompt)}  generated={n_new}  layers={LAYERS}  d_model={D}")
    print(f"identical output tokens: {a == b}")
    print(f"no cache : {t_nc:6.2f} s  ({n_new / t_nc:7.1f} tokens/s)")
    print(f"KV cache : {t_c:6.2f} s  ({n_new / t_c:7.1f} tokens/s)")
    print(f"speed-up : {t_nc / t_c:.1f}x")
    total = len(prompt) + n_new - 1
    print(f"KV cache holds {kv_floats:,} floats = 2 (K,V) x {LAYERS} layers x {total} tokens x {D}")
    print(f"  = {kv_floats * 2 / 1024:.0f} KiB at fp16")

    # tokens processed: no-cache re-reads the whole prefix every step
    work_nc = sum(len(prompt) + i for i in range(n_new))
    work_c = len(prompt) + (n_new - 1)
    print(f"token positions pushed through the model: no cache={work_nc:,}  cache={work_c:,}")

    # prefill is parallel: time per token in prefill vs decode
    x = emb[prompt] + pos_enc(np.arange(len(prompt)))
    t0 = time.perf_counter()
    for _ in range(50):
        y = x
        for w in W:
            y, _, _ = block(y, w)
    t_pf = (time.perf_counter() - t0) / 50
    print(f"prefill: {len(prompt)} tokens in {t_pf * 1e3:.2f} ms "
          f"-> {len(prompt) / t_pf:,.0f} tokens/s (one parallel pass)")
    print(f"decode : {n_new - 1} tokens one by one -> {(n_new - 1) / t_c:,.0f} tokens/s")
