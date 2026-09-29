"""Quantization error demo: fp16 -> int8 / int4, per-tensor vs per-channel vs per-group.

Symmetric "absmax" quantization:
    scale = max(|w|) / q_max          (q_max = 127 for int8, 7 for int4)
    q     = round(w / scale)           (stored integers)
    w_hat = q * scale                  (what the GPU computes with)
We measure (a) weight reconstruction error and (b) error of the layer output y = x @ W,
and show how a few outlier weights ruin coarse (per-tensor) scales.
"""
import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from pathlib import Path

rng = np.random.default_rng(42)
D_IN, D_OUT = 1024, 1024
W = rng.normal(0, 0.02, (D_IN, D_OUT)).astype(np.float32)
# real LLM weights have rare large outliers; plant a few
idx = rng.choice(W.size, 20, replace=False)
W.flat[idx] = rng.choice([-1, 1], 20) * rng.uniform(0.3, 0.5, 20)
X = rng.normal(0, 1, (64, D_IN)).astype(np.float32)   # a batch of activations
Y = X @ W


def quantize(w, bits, group=None, axis_channel=False):
    """Return dequantized weights. group=None & axis_channel=False -> per-tensor."""
    q_max = 2 ** (bits - 1) - 1
    if axis_channel:                                   # one scale per output column
        scale = np.abs(w).max(axis=0, keepdims=True) / q_max
        return np.clip(np.round(w / scale), -q_max, q_max) * scale
    if group:                                          # one scale per `group` rows of each column
        wg = w.reshape(-1, group, w.shape[1])          # (n_groups, group, D_OUT)
        scale = np.abs(wg).max(axis=1, keepdims=True) / q_max
        return (np.clip(np.round(wg / scale), -q_max, q_max) * scale).reshape(w.shape)
    scale = np.abs(w).max() / q_max
    return np.clip(np.round(w / scale), -q_max, q_max) * scale


def rel_err(a, b):
    return np.linalg.norm(a - b) / np.linalg.norm(b)


def bytes_per_param(bits, group=None):
    extra = 0 if group is None else 16 / group         # one fp16 scale per group
    return (bits + extra) / 8


if __name__ == "__main__":
    configs = [
        ("int8 per-tensor", 8, None, False),
        ("int8 per-channel", 8, None, True),
        ("int4 per-tensor", 4, None, False),
        ("int4 per-channel", 4, None, True),
        ("int4 group=128", 4, 128, False),
        ("int4 group=32", 4, 32, False),
    ]
    print(f"{'scheme':<18}{'bytes/param':>12}{'weight err':>12}{'output err':>12}")
    print(f"{'fp16 (baseline)':<18}{2.0:>12.3f}{0:>12.4f}{0:>12.4f}")
    results = {}
    for name, bits, group, per_ch in configs:
        Wq = quantize(W, bits, group, per_ch)
        results[name] = Wq
        bpp = bytes_per_param(bits, group)
        print(f"{name:<18}{bpp:>12.3f}{rel_err(Wq, W):>12.4f}{rel_err(X @ Wq, Y):>12.4f}")

    # how many weights collapse to zero with a per-tensor int4 scale?
    q_max = 7
    scale = np.abs(W).max() / q_max
    zeroed = np.mean(np.round(W / scale) == 0)
    print(f"\nper-tensor int4 scale = {scale:.4f}; typical |w| = {np.median(np.abs(W)):.4f}")
    print(f"weights rounded to 0 by per-tensor int4: {zeroed:.1%}")

    # memory for a 70B-parameter model under each scheme
    print("\n70B model, raw weight memory:")
    for name, bits, group in [("fp16", 16, None), ("int8", 8, None), ("int4 g128", 4, 128)]:
        gb = 70e9 * bytes_per_param(bits, group) / 1e9
        print(f"  {name:<10} {gb:6.1f} GB")

    # plot: error of each weight for int4 per-tensor vs group=32
    out = Path(__file__).resolve().parent.parent / "img"
    out.mkdir(exist_ok=True)
    fig, ax = plt.subplots(figsize=(7, 3.6))
    for name, color in [("int4 per-tensor", "#d62728"), ("int4 group=32", "#2ca02c")]:
        err = (results[name] - W).ravel()
        ax.hist(err, bins=120, range=(-0.04, 0.04), alpha=0.6, label=name, color=color)
    ax.set_xlabel("quantization error per weight (w_hat - w)")
    ax.set_ylabel("count")
    ax.set_title("int4: one scale for the whole matrix vs one scale per 32 weights")
    ax.legend()
    fig.tight_layout()
    fig.savefig(out / "quantization_error.png", dpi=130)
    print("saved img/quantization_error.png")
