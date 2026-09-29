"""Sinusoidal positional encoding (Vaswani et al., 2017) and a heatmap."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np


def sinusoidal_pe(num_positions: int, d_model: int) -> np.ndarray:
    pos = np.arange(num_positions)[:, None]            # (P, 1)
    i = np.arange(0, d_model, 2)[None, :]              # even dims 0,2,4,...
    angle = pos / np.power(10000, i / d_model)         # (P, d_model/2)
    pe = np.zeros((num_positions, d_model))
    pe[:, 0::2] = np.sin(angle)                        # even dims -> sin
    pe[:, 1::2] = np.cos(angle)                        # odd dims  -> cos
    return pe


if __name__ == "__main__":
    np.set_printoptions(precision=3, suppress=True)
    small = sinusoidal_pe(4, 4)
    print("PE for positions 0..3, d_model=4:")
    print(small)

    pe = sinusoidal_pe(64, 64)
    print("\nSimilarity (dot product) of position 10 with others:")
    for p in [10, 11, 12, 15, 20, 40]:
        print(f"  pos 10 . pos {p:2d} = {pe[10] @ pe[p]:6.2f}")

    out = Path(__file__).resolve().parent.parent / "img" / "positional_encoding.png"
    fig, ax = plt.subplots(figsize=(7, 3.6))
    im = ax.imshow(pe, aspect="auto", cmap="RdBu", vmin=-1, vmax=1)
    ax.set_xlabel("embedding dimension")
    ax.set_ylabel("position in sequence")
    ax.set_title("Sinusoidal positional encoding (64 positions x 64 dims)")
    fig.colorbar(im, ax=ax, fraction=0.03)
    fig.tight_layout()
    fig.savefig(out, dpi=130)
    print("\nsaved", out.name)
