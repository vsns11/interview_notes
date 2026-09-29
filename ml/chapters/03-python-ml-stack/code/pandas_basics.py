"""pandas essentials: Series and DataFrame, loading a CSV, inspecting, cleaning missing values and types,
selection with loc/iloc, filtering, groupby, merge, and apply vs vectorised operations."""
import tempfile
import timeit
from pathlib import Path

import numpy as np
import pandas as pd

pd.set_option("display.width", 120)

CSV_TEXT = """emp_id,name,dept,salary,age,start_date
1,Asha,Eng,120000,34,2019-03-01
2,Ben,Eng,,29,2021-07-15
3,Chen,Sales,70000,41,2015-01-10
4,Dana,Sales,65000,unknown,2020-11-01
5,Eli,HR,60000,38,2018-05-20
6,Fay,Eng,135000,45,2012-09-03
7,Gus,HR,,27,2023-02-14
"""


def series_and_frames() -> None:
    print("--- Series and DataFrame ---")
    s = pd.Series([10, 20, 30], index=["a", "b", "c"], name="points")
    print(s, "\ns['b'] =", s["b"], "| s.mean() =", s.mean())
    df = pd.DataFrame({"city": ["Paris", "Lima"], "pop_m": [2.1, 10.0]})
    print(df)


def load_and_inspect() -> pd.DataFrame:
    print("\n--- load and inspect ---")
    path = Path(tempfile.gettempdir()) / "employees.csv"
    path.write_text(CSV_TEXT)
    df = pd.read_csv(path, parse_dates=["start_date"])
    print(df.head(3))
    print("\nshape:", df.shape)
    print(df.dtypes.to_string())
    print("\nmissing values per column:\n", df.isnull().sum().to_string())
    return df


def clean(df: pd.DataFrame) -> pd.DataFrame:
    print("\n--- clean ---")
    df = df.copy()
    df["age"] = pd.to_numeric(df["age"], errors="coerce")        # "unknown" -> NaN
    print("age dtype after to_numeric:", df["age"].dtype, "| missing ages:", df["age"].isna().sum())
    print("dropna() would keep", len(df.dropna()), "of", len(df), "rows")
    df["salary_missing"] = df["salary"].isna()                    # keep the fact it was missing
    df["salary"] = df["salary"].fillna(df.groupby("dept")["salary"].transform("median"))
    df["age"] = df["age"].fillna(df["age"].median())
    print(df[["name", "dept", "salary", "salary_missing", "age"]])
    print("\n", df.select_dtypes("number").describe().round(1).loc[["count", "mean", "std", "min", "max"]])
    return df


def select_and_filter(df: pd.DataFrame) -> None:
    print("\n--- select and filter ---")
    print("df.loc[0, 'name'] =", df.loc[0, "name"], "| df.iloc[0, 1] =", df.iloc[0, 1])
    print(df.loc[df["dept"] == "Eng", ["name", "salary"]])
    print(df.iloc[:2, :3])
    rich_young = df[(df["salary"] > 100_000) & (df["age"] < 40)]
    print("salary > 100k AND age < 40:", rich_young["name"].tolist())
    print("query():", df.query("dept == 'Sales' and salary >= 65000")["name"].tolist())
    print("isin():", df[df["dept"].isin(["HR", "Sales"])]["name"].tolist())
    df = df.assign(years=(pd.Timestamp("2026-01-01") - df["start_date"]).dt.days // 365)
    print(df.sort_values("years", ascending=False)[["name", "years"]].head(3))


def group_and_merge(df: pd.DataFrame) -> None:
    print("\n--- groupby and merge ---")
    summary = df.groupby("dept").agg(headcount=("emp_id", "count"),
                                     avg_salary=("salary", "mean"),
                                     max_age=("age", "max"))
    print(summary.round(0))
    print("\nvalue_counts:\n", df["dept"].value_counts().to_string())

    depts = pd.DataFrame({"dept": ["Eng", "Sales", "Legal"], "floor": [3, 1, 5]})
    inner = df.merge(depts, on="dept", how="inner")
    left = df.merge(depts, on="dept", how="left")
    print(f"\ninner join rows: {len(inner)} (HR has no match) | left join rows: {len(left)}"
          f" (HR kept with floor NaN: {left.loc[left.dept == 'HR', 'floor'].isna().all()})")
    print(left[["name", "dept", "floor"]].tail(3))
    print("\npivot_table:\n", df.pivot_table(index="dept", values="salary", aggfunc="median"))


def apply_vs_vectorised() -> None:
    print("\n--- apply vs vectorised ---")
    big = pd.DataFrame({"price": np.random.default_rng(0).uniform(1, 100, 200_000),
                        "qty": np.random.default_rng(1).integers(1, 10, 200_000)})
    t_apply = min(timeit.repeat(lambda: big.apply(lambda r: r["price"] * r["qty"], axis=1), number=1, repeat=2))
    t_vec = min(timeit.repeat(lambda: big["price"] * big["qty"], number=1, repeat=3))
    print(f"row-wise apply: {t_apply*1000:.0f} ms | vectorised: {t_vec*1000:.2f} ms "
          f"-> ~{t_apply / t_vec:.0f}x faster (varies by machine)")
    big["band"] = np.where(big["price"] > 50, "high", "low")          # vectorised if/else
    big["bucket"] = pd.cut(big["price"], bins=[0, 25, 75, 100], labels=["cheap", "mid", "premium"])
    print(big.head(3))


def chained_assignment_pitfall() -> None:
    print("\n--- a pandas 3 pitfall ---")
    df = pd.DataFrame({"A": [1.0, None, 3.0]})
    import warnings
    with warnings.catch_warnings():
        warnings.simplefilter("ignore")
        df["A"].fillna(0, inplace=True)        # chained + inplace: does NOT change df under copy-on-write
    print("after df['A'].fillna(0, inplace=True):", df["A"].tolist())
    df["A"] = df["A"].fillna(0)                # correct: assign the result back
    print("after df['A'] = df['A'].fillna(0):   ", df["A"].tolist())


if __name__ == "__main__":
    series_and_frames()
    raw = load_and_inspect()
    cleaned = clean(raw)
    select_and_filter(cleaned)
    group_and_merge(cleaned)
    apply_vs_vectorised()
    chained_assignment_pitfall()
