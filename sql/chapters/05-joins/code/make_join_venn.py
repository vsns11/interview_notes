"""Draw the join 'Venn' cheat sheet used in Chapter 5 (img/join_venn.png).
Run: ../../../../.venv-books/bin/python make_join_venn.py   (from this code/ folder)
Left circle = rows of the LEFT table, right circle = rows of the RIGHT table,
overlap = pairs that satisfy the ON condition. Shaded = rows the query returns."""
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import Circle
import numpy as np

PANELS = [
    ("INNER JOIN", "ON a.k = b.k", (False, True, False)),
    ("LEFT JOIN", "ON a.k = b.k", (True, True, False)),
    ("RIGHT JOIN", "ON a.k = b.k", (False, True, True)),
    ("FULL JOIN", "ON a.k = b.k", (True, True, True)),
    ("LEFT anti-join", "LEFT JOIN ... WHERE b.k IS NULL", (True, False, False)),
    ("FULL minus INNER", "FULL JOIN ... WHERE a.k IS NULL\nOR b.k IS NULL", (True, False, True)),
]
FILL, EDGE = "#4C78A8", "#333333"

def panel(ax, title, sub, regions):
    xs, ys = np.meshgrid(np.linspace(-2, 2, 500), np.linspace(-1.3, 1.3, 330))
    in_a = (xs + 0.55) ** 2 + ys ** 2 <= 1
    in_b = (xs - 0.55) ** 2 + ys ** 2 <= 1
    mask = np.zeros_like(xs, dtype=bool)
    left_only, both, right_only = regions
    if left_only: mask |= in_a & ~in_b
    if both: mask |= in_a & in_b
    if right_only: mask |= in_b & ~in_a
    ax.imshow(np.where(mask, 1.0, np.nan), extent=(-2, 2, -1.3, 1.3), origin="lower",
              cmap=matplotlib.colors.ListedColormap([FILL]), alpha=0.75)
    for cx in (-0.55, 0.55):
        ax.add_patch(Circle((cx, 0), 1, fill=False, lw=1.6, ec=EDGE))
    ax.text(-1.05, 1.08, "A (left)", ha="center", fontsize=9)
    ax.text(1.05, 1.08, "B (right)", ha="center", fontsize=9)
    ax.set_title(title, fontsize=12, fontweight="bold", pad=14)
    ax.text(0, -1.28, sub, ha="center", va="top", fontsize=8.5, family="monospace")
    ax.set_xlim(-2, 2); ax.set_ylim(-1.75, 1.3); ax.set_aspect("equal"); ax.axis("off")

fig, axes = plt.subplots(2, 3, figsize=(10.5, 6.2))
for ax, (t, s, r) in zip(axes.flat, PANELS):
    panel(ax, t, s, r)
fig.tight_layout()
fig.savefig("../img/join_venn.png", dpi=110, facecolor="white")
print("wrote ../img/join_venn.png")
