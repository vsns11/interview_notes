"""KV-cache intuition: same outputs, far less recomputation, real memory cost.

1) Generate with and without a KV cache for one attention layer -> identical.
2) Count the key/value projections each approach performs.
3) KV-cache memory for a few model shapes, and chat-history token growth.
"""
import numpy as np

rng = np.random.default_rng(0)
d = 8
W_q, W_k, W_v = (rng.normal(size=(d, d)) / np.sqrt(d) for _ in range(3))


def softmax(z):
    e = np.exp(z - z.max())
    return e / e.sum()


def attend(q, K, V):
    return softmax(K @ q / np.sqrt(d)) @ V


def generate_no_cache(x0, steps):
    xs, projections = [x0], 0
    for _ in range(steps):
        X = np.stack(xs)
        K, V = X @ W_k, X @ W_v          # recompute keys/values for ALL tokens
        projections += 2 * len(xs)
        xs.append(np.tanh(attend(xs[-1] @ W_q, K, V)))   # toy "next token" vector
    return np.stack(xs), projections


def generate_with_cache(x0, steps):
    xs, K_cache, V_cache, projections = [x0], [], [], 0
    for _ in range(steps):
        K_cache.append(xs[-1] @ W_k)      # only the NEW token's key/value
        V_cache.append(xs[-1] @ W_v)
        projections += 2
        xs.append(np.tanh(attend(xs[-1] @ W_q, np.stack(K_cache), np.stack(V_cache))))
    return np.stack(xs), projections


def kv_bytes_per_token(layers, kv_heads, head_dim, bytes_per_value=2):
    return 2 * layers * kv_heads * head_dim * bytes_per_value   # 2 = keys and values


if __name__ == "__main__":
    x0 = rng.normal(size=d)
    a, pa = generate_no_cache(x0, 50)
    b, pb = generate_with_cache(x0, 50)
    print("outputs identical:", np.allclose(a, b))
    print(f"K/V projections for 50 steps: no cache={pa}, with cache={pb}")

    print("\nKV-cache memory (16-bit values):")
    shapes = {
        "7B-class, full MHA (32 layers, 32 KV heads x 128)": (32, 32, 128),
        "8B-class, GQA      (32 layers,  8 KV heads x 128)": (32, 8, 128),
        "70B-class, GQA     (80 layers,  8 KV heads x 128)": (80, 8, 128),
    }
    for name, (L, H, hd) in shapes.items():
        per_tok = kv_bytes_per_token(L, H, hd)
        print(f"  {name}: {per_tok / 2**10:5.0f} KiB/token, "
              f"{per_tok * 8192 / 2**30:5.2f} GiB per 8K-token sequence")

    print("\nChat history re-sent every turn (200 tokens per turn):")
    per_turn, total = 200, 0
    for turn in range(1, 21):
        prompt = per_turn * turn          # all previous turns + the new one
        total += prompt
        if turn in (1, 5, 10, 20):
            print(f"  turn {turn:2d}: prompt={prompt:5d} tokens, cumulative input={total:6d}")
