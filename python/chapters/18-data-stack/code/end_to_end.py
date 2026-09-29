"""Mini end-to-end analysis: generate raw data -> load -> clean -> transform -> analyse -> plot."""
import io

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

from plot_style import BLUE, IMG, PALETTE, apply_style


def make_raw_csv(n: int = 2_000, seed: int = 42) -> str:
    """Synthetic coffee-shop orders with realistic mess: missing values, bad rows, duplicates."""
    rng = np.random.default_rng(seed)
    dates = pd.to_datetime("2026-01-01") + pd.to_timedelta(rng.integers(0, 181, n), unit="D")
    store = rng.choice(["Downtown", "Campus", "Airport"], n, p=[0.5, 0.3, 0.2])
    item = rng.choice(["espresso", "latte", "tea", "pastry"], n, p=[0.25, 0.35, 0.2, 0.2])
    base = pd.Series(item).map({"espresso": 2.5, "latte": 4.0, "tea": 2.0, "pastry": 3.0}).to_numpy()
    price = base * np.where(store == "Airport", 1.4, 1.0)            # airport markup
    qty = rng.integers(1, 4, n).astype(float)
    qty[rng.random(n) < 0.03] = np.nan                                # 3 % missing quantities
    df = pd.DataFrame({"order_id": np.arange(1, n + 1), "date": dates.strftime("%Y-%m-%d"), "store": store, "item": item,
                       "qty": qty, "unit_price": price.round(2)})
    df.loc[5, "unit_price"] = -1.0                                    # an impossible value
    df = pd.concat([df, df.iloc[:10]], ignore_index=True)             # 10 duplicate rows
    return df.to_csv(index=False)


if __name__ == "__main__":
    apply_style()
    # 1. Load
    raw = pd.read_csv(io.StringIO(make_raw_csv()), parse_dates=["date"])
    print(f"raw: {raw.shape[0]} rows, missing qty={raw['qty'].isna().sum()}, "
          f"duplicates={raw.duplicated().sum()}, negative prices={(raw['unit_price'] < 0).sum()}")

    # 2. Clean
    df = (raw.drop_duplicates()
             .query("unit_price > 0")
             .assign(qty=lambda d: d["qty"].fillna(d["qty"].median()).astype(int),
                     store=lambda d: d["store"].astype("category"),
                     item=lambda d: d["item"].astype("category")))
    print(f"clean: {len(df)} rows, nulls={int(df.isna().sum().sum())}")

    # 3. Transform
    df["revenue"] = df["qty"] * df["unit_price"]
    df["month"] = df["date"].dt.to_period("M")
    df["weekday"] = df["date"].dt.day_name()

    # 4. Analyse
    by_store = df.groupby("store", observed=True).agg(orders=("qty", "size"),
                                                      revenue=("revenue", "sum"),
                                                      avg_ticket=("revenue", "mean")).round(2)
    print(by_store.sort_values("revenue", ascending=False))
    monthly = df.pivot_table(index="month", columns="store", values="revenue",
                             aggfunc="sum", observed=True).round(0)
    print(monthly)
    top_item = df.groupby(["store", "item"], observed=True)["revenue"].sum().groupby(level="store", observed=True).idxmax()
    print("best item per store:", {s: i for s, i in top_item.to_list()})
    growth = monthly.sum(axis=1).pct_change().mul(100).round(1)
    print("month-over-month total revenue change %:", {str(k): v for k, v in growth.dropna().items()})

    # 5. Plot: monthly revenue per store + revenue by item
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(10, 3.8))
    for color, store in zip(PALETTE, monthly.columns):
        ax1.plot(monthly.index.to_timestamp(), monthly[store], marker="o", label=store, color=color)
    ax1.set(title="Monthly revenue by store", ylabel="revenue ($)")
    ax1.legend(frameon=False)
    ax1.tick_params(axis="x", rotation=30)
    item_rev = df.groupby("item", observed=True)["revenue"].sum().sort_values()
    ax2.barh(item_rev.index.astype(str), item_rev.to_numpy(), color=BLUE)
    ax2.set(title="Total revenue by item (Jan-Jun 2026)", xlabel="revenue ($)")
    fig.tight_layout()
    fig.savefig(IMG / "coffee_analysis.png", dpi=120)
    plt.close(fig)
    print("saved coffee_analysis.png")
