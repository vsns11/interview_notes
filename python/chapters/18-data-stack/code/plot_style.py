"""Shared chart style for this chapter: a colour-blind-checked categorical palette and quiet axes."""
import matplotlib
matplotlib.use("Agg")                     # file output only, no window
import matplotlib.pyplot as plt
from pathlib import Path

BLUE, ORANGE, AQUA, YELLOW = "#2a78d6", "#eb6834", "#1baf7a", "#eda100"
PALETTE = [BLUE, ORANGE, AQUA, YELLOW]
IMG = Path(__file__).resolve().parent.parent / "img"


def apply_style() -> None:
    plt.rcParams.update({
        "figure.facecolor": "#fcfcfb", "axes.facecolor": "#fcfcfb",
        "axes.edgecolor": "#b5b4ae", "axes.labelcolor": "#52514e",
        "xtick.color": "#52514e", "ytick.color": "#52514e", "text.color": "#0b0b0b",
        "axes.spines.top": False, "axes.spines.right": False,
        "axes.grid": True, "axes.axisbelow": True, "grid.color": "#e6e5e0", "grid.linewidth": 0.8,
        "axes.prop_cycle": plt.cycler(color=PALETTE), "lines.linewidth": 2,
        "font.size": 10,
    })
