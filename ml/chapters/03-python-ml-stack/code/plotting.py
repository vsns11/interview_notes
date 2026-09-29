"""matplotlib essentials for data exploration: line, scatter, histogram, bar, box plot, subplots,
and pandas' built-in plotting. All figures are saved to ../img/."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")                     # render to files, no window (scripts, servers, CI)
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from sklearn.datasets import load_iris

IMG = Path(__file__).resolve().parent.parent / "img"


def line_plot() -> None:
    epochs = np.arange(1, 31)
    train = 1.0 / epochs ** 0.5
    val = train + 0.02 * np.maximum(0, epochs - 12)   # validation loss turns up: overfitting
    fig, ax = plt.subplots(figsize=(6, 3.5))           # object-oriented API: fig + ax
    ax.plot(epochs, train, label="training loss")
    ax.plot(epochs, val, "--", label="validation loss")
    ax.axvline(12, color="grey", lw=0.8)
    ax.annotate("best epoch", xy=(12, val[11]), xytext=(17, 0.6), arrowprops={"arrowstyle": "->"})
    ax.set(xlabel="epoch", ylabel="loss", title="Learning curves")
    ax.legend()
    fig.tight_layout()
    fig.savefig(IMG / "line_learning_curves.png", dpi=120)
    plt.close(fig)


def iris_overview() -> None:
    iris = load_iris(as_frame=True)
    df = iris.frame.rename(columns=lambda c: c.replace(" (cm)", "").replace(" ", "_"))
    df["species"] = iris.target_names[iris.target]
    colours = {"setosa": "#1f77b4", "versicolor": "#ff7f0e", "virginica": "#2ca02c"}

    fig, axes = plt.subplots(2, 2, figsize=(10, 7))
    ax = axes[0, 0]
    for sp, g in df.groupby("species"):
        ax.scatter(g["petal_length"], g["petal_width"], s=15, label=sp, color=colours[sp])
    ax.set(xlabel="petal length (cm)", ylabel="petal width (cm)", title="Scatter: two features, coloured by class")
    ax.legend(fontsize=8)

    ax = axes[0, 1]
    for sp, g in df.groupby("species"):
        ax.hist(g["petal_length"], bins=15, alpha=0.6, label=sp, color=colours[sp])
    ax.set(xlabel="petal length (cm)", ylabel="count", title="Histogram: distribution per class")

    ax = axes[1, 0]
    means = df.groupby("species")["sepal_length"].mean()
    ax.bar(means.index, means.values, color=[colours[s] for s in means.index])
    ax.set(ylabel="mean sepal length (cm)", title="Bar: a summary per group")

    ax = axes[1, 1]
    ax.boxplot([g["sepal_width"].values for _, g in df.groupby("species")], tick_labels=list(colours))
    ax.set(ylabel="sepal width (cm)", title="Box plot: median, quartiles, outliers")
    fig.suptitle("Exploring the iris dataset with subplots")
    fig.tight_layout()
    fig.savefig(IMG / "iris_subplots.png", dpi=120)
    plt.close(fig)

    # Correlation heatmap (seaborn's heatmap, done with plain matplotlib)
    corr = df.drop(columns=["species", "target"]).corr()
    fig, ax = plt.subplots(figsize=(5, 4.2))
    im = ax.imshow(corr, cmap="RdBu_r", vmin=-1, vmax=1)
    ax.set_xticks(range(len(corr)), corr.columns, rotation=45, ha="right")
    ax.set_yticks(range(len(corr)), corr.columns)
    for i in range(len(corr)):
        for j in range(len(corr)):
            v = corr.iloc[i, j]
            ax.text(j, i, f"{v:.2f}", ha="center", va="center", fontsize=8, color="white" if abs(v) > 0.6 else "black")
    fig.colorbar(im, ax=ax)
    ax.set_title("Correlation heatmap")
    fig.tight_layout()
    fig.savefig(IMG / "iris_corr_heatmap.png", dpi=120)
    plt.close(fig)

    # pandas plotting is a thin wrapper over matplotlib
    ax = df["sepal_length"].hist(bins=20, figsize=(5, 3))
    ax.set(title="df['sepal_length'].hist()", xlabel="sepal length (cm)")
    ax.figure.tight_layout()
    ax.figure.savefig(IMG / "pandas_hist.png", dpi=120)
    plt.close(ax.figure)
    print(corr.round(2))


if __name__ == "__main__":
    line_plot()
    iris_overview()
    print("saved:", sorted(p.name for p in IMG.glob("*.png")))
