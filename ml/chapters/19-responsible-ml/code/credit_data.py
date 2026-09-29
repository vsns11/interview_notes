"""Synthetic credit-approval data with a built-in, known bias.

Group 0 ("A") is the majority, group 1 ("B") the minority. The TRUE chance of
repaying depends only on finances - never on group. Two biases are injected:
  * historical inequality: group B has lower incomes on average (a real-world
    effect the model sees through the features),
  * label bias: in the historical records, 20% of group-B people who actually
    repaid were recorded as "bad" (e.g. loans sold off / closed early and coded
    as defaults). The model only ever sees these recorded labels.
`zip_region` is a proxy: it is strongly correlated with group membership.
"""
import numpy as np
import pandas as pd

FEATURES = ["income_k", "debt_ratio", "years_employed", "prior_defaults", "zip_region"]


def make_credit_data(n=20_000, seed=0, label_bias=0.20):
    rng = np.random.default_rng(seed)
    group = (rng.random(n) < 0.30).astype(int)                     # 30% group B
    income = rng.lognormal(mean=np.where(group == 1, 3.75, 4.0), sigma=0.35)  # in $k
    debt = np.clip(rng.normal(0.35, 0.12, n), 0.0, 1.0)
    years = rng.poisson(6, n).astype(float)
    defaults = rng.poisson(0.4, n)
    zip_region = np.where(rng.random(n) < np.where(group == 1, 0.80, 0.15), 1, 0)

    # true repayment probability: finances only
    z = -1.0 + 0.045 * (income - 50) - 5.0 * (debt - 0.35) + 0.12 * (years - 6) - 0.9 * defaults + 1.2
    p_true = 1 / (1 + np.exp(-z))
    y_true = (rng.random(n) < p_true).astype(int)

    # recorded (biased) label
    flip = (group == 1) & (y_true == 1) & (rng.random(n) < label_bias)
    y_recorded = np.where(flip, 0, y_true)

    return pd.DataFrame({
        "income_k": income.round(1), "debt_ratio": debt.round(3),
        "years_employed": years, "prior_defaults": defaults,
        "zip_region": zip_region, "group": group,
        "y_true": y_true, "y": y_recorded,
    })


if __name__ == "__main__":
    df = make_credit_data()
    print(df.head().to_string())
    print(df.groupby("group")[["income_k", "zip_region", "y_true", "y"]].mean().round(3))
