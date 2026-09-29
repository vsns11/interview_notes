"""Data cleaning: duplicates, impossible values, outliers (IQR / z-score / clipping), missing-value strategies."""
import numpy as np
import pandas as pd
from sklearn.experimental import enable_iterative_imputer  # noqa: F401  (unlocks IterativeImputer)
from sklearn.impute import IterativeImputer, KNNImputer, SimpleImputer

from make_data import make_churn


def main() -> None:
    df = make_churn()

    # 1) Duplicates ---------------------------------------------------------
    before = len(df)
    df = df.drop_duplicates()
    print(f"duplicates removed: {before - len(df)}  -> rows now {len(df)}")
    print("duplicate customer_ids left:", df["customer_id"].duplicated().sum())

    # 2) Impossible values -> treat as missing (they are errors, not extremes)
    bad_age = (df["age"] > 110).sum()
    bad_inc = (df["income"] >= 9_999_999).sum()
    df.loc[df["age"] > 110, "age"] = np.nan
    df.loc[df["income"] >= 9_999_999, "income"] = np.nan
    print(f"impossible ages -> NaN: {bad_age}, sentinel incomes -> NaN: {bad_inc}")

    # 3) Outlier detection on a legitimately skewed column --------------------
    inc = df["income"].dropna()
    q1, q3 = inc.quantile([0.25, 0.75])
    iqr = q3 - q1
    lo, hi = q1 - 1.5 * iqr, q3 + 1.5 * iqr
    z = (inc - inc.mean()) / inc.std()
    print(f"\nIQR fences: [{lo:,.0f}, {hi:,.0f}]  -> {((inc < lo) | (inc > hi)).sum()} flagged")
    print(f"|z| > 3                     -> {(z.abs() > 3).sum()} flagged")
    log_inc = np.log(inc)
    zl = (log_inc - log_inc.mean()) / log_inc.std()
    print(f"|z| > 3 on log(income)      -> {(zl.abs() > 3).sum()} flagged  (skew made raw z-score over-flag)")
    p1, p99 = inc.quantile([0.01, 0.99])
    print(f"winsorize (clip) to 1st/99th pct: [{p1:,.0f}, {p99:,.0f}]")

    # 4) Missing values ------------------------------------------------------
    print("\nmissing per column:", df[["age", "income", "device"]].isna().sum().to_dict())
    # Is missingness informative? Compare churn rate when income is missing vs present.
    print("churn rate | income missing:", round(df.loc[df.income.isna(), "churn"].mean(), 3),
          " | present:", round(df.loc[df.income.notna(), "churn"].mean(), 3))

    # Evaluate imputers honestly: hide 300 KNOWN incomes, impute them, measure the error.
    rng = np.random.default_rng(1)
    num_cols = ["age", "income", "tenure_months", "monthly_charges", "usage_hours"]
    X = df[num_cols].to_numpy(dtype=float)
    known = np.flatnonzero(~np.isnan(X[:, 1]))
    hide = rng.choice(known, 300, replace=False)
    truth = X[hide, 1].copy()
    X_masked = X.copy()
    X_masked[hide, 1] = np.nan

    imputers = {
        "drop rows (listwise)": None,
        "mean": SimpleImputer(strategy="mean"),
        "median": SimpleImputer(strategy="median"),
        "KNN (k=5)": KNNImputer(n_neighbors=5),
        "iterative (MICE-like)": IterativeImputer(random_state=0),
    }
    print("\nimputer                 MAE on hidden incomes")
    for name, imp in imputers.items():
        if imp is None:
            kept = (~np.isnan(X_masked).any(axis=1)).mean()
            print(f"{name:<24}n/a  (would keep only {kept:.0%} of rows)")
            continue
        filled = imp.fit_transform(X_masked)[hide, 1]
        print(f"{name:<24}{np.abs(filled - truth).mean():,.0f}")

    # Categorical: most-frequent vs an explicit 'missing' category
    dev_mode = SimpleImputer(strategy="most_frequent").fit_transform(df[["device"]]).ravel()
    dev_flag = df["device"].fillna("missing")
    print("\ndevice after mode imputation:", pd.Series(dev_mode).value_counts().to_dict())
    print("device with 'missing' level :", dev_flag.value_counts().to_dict())

    # Missing-indicator: keep the signal that a value was absent
    si = SimpleImputer(strategy="median", add_indicator=True)
    out = si.fit_transform(df[["income"]])
    print("\nmedian + add_indicator output columns:", out.shape[1], "| first rows:\n", out[:3])


if __name__ == "__main__":
    main()
