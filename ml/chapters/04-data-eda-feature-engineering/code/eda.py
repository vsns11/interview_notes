"""Exploratory data analysis on the messy churn table: summary stats, distributions, correlations, target balance."""
import numpy as np
import pandas as pd

from make_data import make_churn
from plotstyle import C, INK2, plt, save


def main() -> None:
    df = make_churn()
    pd.set_option("display.width", 120)

    print("shape:", df.shape)
    print("\n--- dtypes ---")
    print(df.dtypes.value_counts().to_string())
    print("\n--- missing values (share) ---")
    print(df.isna().mean().loc[lambda s: s > 0].round(3).to_string())
    print("\n--- numeric summary ---")
    print(df[["age", "income", "tenure_months", "support_tickets", "usage_hours"]]
          .describe().T[["mean", "50%", "std", "min", "max"]].round(1).to_string())
    print("\n--- categorical cardinality ---")
    print(df[["plan", "device", "city"]].nunique().to_string())
    print("\nchurn rate:", round(df["churn"].mean(), 3))
    print("exact duplicate rows:", df.duplicated().sum())
    print("\n--- churn rate by plan ---")
    print(df.groupby("plan")["churn"].agg(["mean", "size"]).round(3).to_string())

    # 1) Distributions: a skewed feature raw vs log
    clean_inc = df["income"].where(df["income"] < 1_000_000)
    fig, ax = plt.subplots(1, 3, figsize=(12, 3.2))
    ax[0].hist(clean_inc.dropna(), bins=50, color=C[0], edgecolor="white", linewidth=0.5)
    ax[0].set(title="income (raw): long right tail", xlabel="income")
    ax[1].hist(np.log1p(clean_inc.dropna()), bins=50, color=C[0], edgecolor="white", linewidth=0.5)
    ax[1].set(title="log(1 + income): roughly bell-shaped", xlabel="log income")
    ax[2].boxplot([df["age"].dropna()], orientation="horizontal", widths=0.5,
                  flierprops=dict(marker="o", markersize=6, markerfacecolor=C[1], markeredgecolor=C[1]))
    ax[2].set(title="age box plot: the 999s pop out", xlabel="age", yticks=[])
    save(fig, "eda_distributions.png")

    # 2) Correlation heatmap of numeric columns (diverging map, grey-ish midpoint at 0)
    num = df[["age", "income", "tenure_months", "support_tickets", "monthly_charges",
              "usage_hours", "churn"]].copy()
    num = num[(num["age"] < 120) & (num["income"] < 1_000_000)]
    corr = num.corr(method="spearman")
    print("\n--- Spearman correlation with churn ---")
    print(corr["churn"].drop("churn").sort_values().round(2).to_string())
    fig, ax = plt.subplots(figsize=(6, 5))
    im = ax.imshow(corr, cmap="RdBu_r", vmin=-1, vmax=1)
    ax.set_xticks(range(len(corr)), corr.columns, rotation=45, ha="right")
    ax.set_yticks(range(len(corr)), corr.columns)
    ax.grid(False)
    for i in range(len(corr)):
        for j in range(len(corr)):
            ax.text(j, i, f"{corr.iat[i, j]:.2f}", ha="center", va="center", fontsize=8,
                    color="white" if abs(corr.iat[i, j]) > 0.6 else "#0b0b0b")
    fig.colorbar(im, ax=ax, shrink=0.8, label="Spearman rho")
    ax.set_title("Correlation matrix (numeric features + target)")
    save(fig, "eda_correlation.png")

    # 3) Feature vs target: churn rate by tenure bucket and by tickets
    fig, ax = plt.subplots(1, 2, figsize=(10, 3.2))
    rate = df.groupby(pd.cut(df["tenure_months"], [0, 6, 12, 24, 48]), observed=True)["churn"].mean()
    ax[0].bar([str(i) for i in rate.index], rate.values, color=C[0], width=0.6)
    ax[0].set(title="churn rate by tenure (months)", ylabel="churn rate")
    rate = df.groupby(df["support_tickets"].clip(upper=5))["churn"].mean()
    ax[1].bar([str(i) if i < 5 else "5+" for i in rate.index], rate.values, color=C[0], width=0.6)
    ax[1].set(title="churn rate by support tickets", xlabel="tickets")
    for a in ax:
        a.tick_params(axis="x", colors=INK2)
    save(fig, "eda_target_relationships.png")


if __name__ == "__main__":
    main()
