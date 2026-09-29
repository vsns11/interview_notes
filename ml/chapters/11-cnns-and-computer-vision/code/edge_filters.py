"""Apply hand-made kernels (edge detectors, blur) to a digit image and visualise feature maps."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import torch
import torch.nn.functional as F
from sklearn.datasets import load_digits

IMG = Path(__file__).resolve().parent.parent / "img"

KERNELS = {
    "vertical edges (Sobel x)": [[-1, 0, 1], [-2, 0, 2], [-1, 0, 1]],
    "horizontal edges (Sobel y)": [[-1, -2, -1], [0, 0, 0], [1, 2, 1]],
    "blur (box)": [[1 / 9] * 3] * 3,
    "sharpen": [[0, -1, 0], [-1, 5, -1], [0, -1, 0]],
}

if __name__ == "__main__":
    digits = load_digits()
    img = torch.tensor(digits.images[0], dtype=torch.float32)      # an 8x8 "0"
    img = F.interpolate(img[None, None], scale_factor=4, mode="bilinear")  # 32x32 for a nicer picture
    fig, axes = plt.subplots(1, 5, figsize=(14, 3))
    axes[0].imshow(img[0, 0], cmap="gray"); axes[0].set_title(f"input (digit {digits.target[0]})")
    for ax, (name, k) in zip(axes[1:], KERNELS.items()):
        out = F.conv2d(img, torch.tensor(k, dtype=torch.float32)[None, None], padding=1)[0, 0]
        ax.imshow(out, cmap="RdBu_r" if "edges" in name else "gray"); ax.set_title(name, fontsize=9)
        print(f"{name:28s} output shape {tuple(out.shape)}  min {out.min():7.2f}  max {out.max():7.2f}")
    for ax in axes:
        ax.axis("off")
    fig.tight_layout(); fig.savefig(IMG / "edge_filters.png", dpi=110)
    print("saved img/edge_filters.png")
