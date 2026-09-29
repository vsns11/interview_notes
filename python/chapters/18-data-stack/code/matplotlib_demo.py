"""matplotlib: Figure/Axes, line, bar, scatter, histogram, subplots, labels, saving to img/."""
import numpy as np
import matplotlib.pyplot as plt

from plot_style import AQUA, BLUE, IMG, ORANGE, apply_style

if __name__ == "__main__":
    apply_style()
    rng = np.random.default_rng(7)

    # 1. One Figure with one Axes - the object-oriented API
    x = np.linspace(0, 2 * np.pi, 100)
    fig, ax = plt.subplots(figsize=(6, 3.2))          # Figure = canvas, Axes = one plot
    ax.plot(x, np.sin(x), label="sin(x)")
    ax.plot(x, np.cos(x), label="cos(x)", linestyle="--")
    ax.set(title="Line plot", xlabel="x (radians)", ylabel="value")
    ax.axhline(0, color="#8a8984", linewidth=0.8)
    ax.legend(frameon=False)
    fig.tight_layout()
    fig.savefig(IMG / "line_plot.png", dpi=120)
    plt.close(fig)                                    # free memory in scripts/loops

    # 2. A 2x2 grid of the four workhorse charts
    fig, axes = plt.subplots(2, 2, figsize=(9, 6))
    months = ["Jan", "Feb", "Mar", "Apr"]
    sales = [120, 135, 128, 160]
    axes[0, 0].plot(months, sales, marker="o", color=BLUE)
    axes[0, 0].set(title="Line: trend over time", ylabel="units")

    axes[0, 1].bar(["pen", "notebook", "bag"], [95, 20, 75], color=BLUE)
    axes[0, 1].set(title="Bar: compare categories", ylabel="revenue")

    h = rng.normal(170, 8, 200)
    w = 0.9 * h - 90 + rng.normal(0, 5, 200)
    axes[1, 0].scatter(h, w, s=14, alpha=0.6, color=AQUA)
    axes[1, 0].set(title="Scatter: relationship", xlabel="height (cm)", ylabel="weight (kg)")

    axes[1, 1].hist(rng.normal(50, 10, 1_000), bins=30, color=ORANGE, edgecolor="#fcfcfb")
    axes[1, 1].set(title="Histogram: distribution", xlabel="score", ylabel="count")
    fig.suptitle("Four basic chart types")
    fig.tight_layout()
    fig.savefig(IMG / "four_charts.png", dpi=120)
    plt.close(fig)

    print(f"corr(height, weight) = {np.corrcoef(h, w)[0, 1]:.2f}")
    print("saved line_plot.png, four_charts.png")
