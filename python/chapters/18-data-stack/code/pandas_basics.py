"""pandas: Series/DataFrame, IO, loc/iloc, filtering, groupby, merge/concat, pivot, missing data,
datetimes, apply vs vectorised, and performance tips."""
import io
import timeit

import numpy as np
import pandas as pd


def section(title: str) -> None:
    print(f"\n--- {title} ---")


CSV = """order_id,date,customer,city,product,qty,price
1,2026-01-03,ana,Pune,pen,10,1.5
2,2026-01-03,ben,Delhi,notebook,2,4.0
3,2026-01-10,ana,Pune,notebook,,4.0
4,2026-02-01,cara,Mumbai,pen,5,1.5
5,2026-02-14,ben,Delhi,bag,1,25.0
6,2026-02-20,dev,Pune,bag,2,
7,2026-03-05,cara,Mumbai,notebook,3,4.0
8,2026-03-05,ana,Pune,pen,20,1.5
"""

if __name__ == "__main__":
    pd.set_option("display.width", 120)
    print(f"pandas {pd.__version__}")

    section("Series")
    s = pd.Series([10, 20, 30], index=["a", "b", "c"], name="score")
    print(s)
    print(f"s['b']={s['b']}  s.iloc[0]={s.iloc[0]}  s[s > 15].tolist()={s[s > 15].tolist()}  s.mean()={s.mean()}")

    section("read_csv + inspect")
    df = pd.read_csv(io.StringIO(CSV), parse_dates=["date"])
    print(df.head(3))
    print(df.dtypes.to_string())
    print(f"shape={df.shape}")
    print(df.describe().loc[["count", "mean", "min", "max"]])

    section("loc / iloc / filtering")
    print(df.loc[0, "customer"], "|", df.iloc[0, 2], "|", df.loc[1:2, ["customer", "qty"]].to_dict("list"))
    pune_pens = df[(df["city"] == "Pune") & (df["product"] == "pen")]
    print(pune_pens[["order_id", "qty"]].to_dict("list"))
    print(df.query("qty >= 5 and price < 2")["order_id"].tolist())
    print(df[df["product"].isin(["bag", "notebook"])]["order_id"].tolist())

    section("missing data")
    print(df.isna().sum()[lambda x: x > 0].to_dict())
    df["qty"] = df["qty"].fillna(0).astype(int)
    df["price"] = df["price"].fillna(df.groupby("product")["price"].transform("median"))
    print(f"after fill: nulls={int(df.isna().sum().sum())}, order 6 (missing price, bag) filled with bag median={df.loc[5, 'price']}")

    section("new columns (vectorised)")
    df = df.assign(revenue=df["qty"] * df["price"], month=df["date"].dt.to_period("M"))
    df.loc[df["revenue"] >= 20, "tier"] = "big"          # set with .loc - never chained indexing
    df["tier"] = df["tier"].fillna("small")
    print(df[["order_id", "revenue", "month", "tier"]].head(4))

    section("groupby / agg")
    print(df.groupby("city")["revenue"].sum().sort_values(ascending=False))
    print(df.groupby("customer").agg(orders=("order_id", "count"),
                                     revenue=("revenue", "sum"),
                                     avg_qty=("qty", "mean")).round(2))
    df["share_of_city"] = df["revenue"] / df.groupby("city")["revenue"].transform("sum")
    print(df[["order_id", "city", "share_of_city"]].round(2).head(3))

    section("merge / concat")
    customers = pd.DataFrame({"customer": ["ana", "ben", "cara", "eve"],
                              "segment": ["retail", "business", "retail", "business"]})
    inner = df.merge(customers, on="customer", how="inner")
    left = df.merge(customers, on="customer", how="left")
    print(f"inner rows={len(inner)}, left rows={len(left)}, dev's segment={left.loc[left.customer == 'dev', 'segment'].iloc[0]}")
    outer = customers.merge(df[["customer"]].drop_duplicates(), on="customer", how="outer", indicator=True)
    print(outer)
    april = pd.DataFrame({"order_id": [9], "customer": ["eve"], "qty": [1]})
    print(f"concat rows: {len(pd.concat([df, april], ignore_index=True))}")

    section("pivot / melt")
    pivot = df.pivot_table(index="city", columns="product", values="revenue", aggfunc="sum", fill_value=0)
    print(pivot)
    long = pivot.reset_index().melt(id_vars="city", var_name="product", value_name="revenue")
    print(long.head(3))

    section("datetime")
    print(df["date"].dt.day_name().head(5).tolist(), df["date"].dt.month.unique().tolist())
    monthly = df.set_index("date")["revenue"].resample("ME").sum()
    print(monthly)

    section("apply vs vectorised")
    rng = np.random.default_rng(0)
    big = pd.DataFrame({"qty": rng.integers(1, 10, 100_000), "price": rng.random(100_000) * 10})
    t_apply = min(timeit.repeat(lambda: big.apply(lambda r: r["qty"] * r["price"], axis=1), number=1, repeat=3))
    t_vec = min(timeit.repeat(lambda: big["qty"] * big["price"], number=1, repeat=3))
    print(f"100k rows: apply(axis=1) {t_apply*1000:.0f} ms, vectorised {t_vec*1000:.2f} ms, x{t_apply/t_vec:.0f}")

    section("memory: str vs category")
    cities = pd.Series(rng.choice(["Pune", "Delhi", "Mumbai"], 100_000))
    print(f"dtype {cities.dtype}: {cities.memory_usage(deep=True)/1e6:.2f} MB, "
          f"category: {cities.astype('category').memory_usage(deep=True)/1e6:.2f} MB")

    section("copy-on-write (pandas 3)")
    sub = df[df["city"] == "Pune"]
    sub["qty"] = 0                                       # modifies only `sub`, never df
    print(f"df Pune qty still: {df.loc[df.city == 'Pune', 'qty'].tolist()}")
