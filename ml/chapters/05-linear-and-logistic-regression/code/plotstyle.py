"""Shared matplotlib style for this chapter's figures (thin marks, quiet grid, fixed colour order)."""
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402

IMG = Path(__file__).resolve().parent.parent / "img"
IMG.mkdir(exist_ok=True)

# Categorical colours, always used in this order (blue, orange, aqua, yellow, magenta, green, violet, red)
C = ["#2a78d6", "#eb6834", "#1baf7a", "#eda100", "#e87ba4", "#008300", "#4a3aa7", "#e34948"]
INK, INK2, GRID = "#0b0b0b", "#52514e", "#e4e3df"

plt.rcParams.update({
    "figure.dpi": 110, "savefig.dpi": 110, "savefig.bbox": "tight",
    "figure.facecolor": "white", "axes.facecolor": "white",
    "axes.edgecolor": INK2, "axes.labelcolor": INK, "axes.titlecolor": INK,
    "axes.spines.top": False, "axes.spines.right": False,
    "axes.grid": True, "grid.color": GRID, "grid.linewidth": 0.8,
    "axes.prop_cycle": matplotlib.cycler(color=C),
    "xtick.color": INK2, "ytick.color": INK2, "font.size": 10,
    "lines.linewidth": 2, "legend.frameon": False,
})


def save(fig, name: str) -> None:
    fig.savefig(IMG / name)
    plt.close(fig)
    print(f"saved img/{name}")
