"""A small, deliberately messy synthetic 'subscription churn' table used by every script in this chapter.

It has the problems real tables have: skewed numbers, missing values, typos/outliers,
exact duplicate rows, a high-cardinality category, dates and free text.
"""
import numpy as np
import pandas as pd

CITIES = [f"city_{i:02d}" for i in range(60)]  # high-cardinality categorical
PLANS = ["basic", "standard", "premium"]        # ordered categorical
DEVICES = ["android", "ios", "web"]             # unordered categorical
POS_WORDS = ["great", "love", "fast", "helpful", "easy"]
NEG_WORDS = ["slow", "expensive", "bug", "cancel", "crash"]


def make_churn(n: int = 3000, seed: int = 0) -> pd.DataFrame:
    rng = np.random.default_rng(seed)
    age = rng.normal(40, 12, n).clip(18, 85).round()
    income = np.exp(rng.normal(10.8, 0.6, n)).round(-2)          # log-normal: long right tail
    plan = rng.choice(PLANS, n, p=[0.5, 0.35, 0.15])
    device = rng.choice(DEVICES, n, p=[0.45, 0.35, 0.20])
    city = rng.choice(CITIES, n, p=np.r_[np.full(10, 0.05), np.full(50, 0.01)])
    signup = pd.Timestamp("2024-01-01") + pd.to_timedelta(rng.integers(0, 730, n), unit="D")
    tenure_months = rng.integers(1, 48, n)
    tickets = rng.poisson(1.2, n)
    monthly = np.select([plan == "basic", plan == "standard"], [10, 20], 35) + rng.normal(0, 2, n)
    usage_hours = rng.gamma(2.0, 6.0, n)

    # True churn logic (unknown to the model): short tenure, many tickets, low usage, basic plan
    city_effect = {c: rng.normal(0, 0.6) for c in CITIES}
    logit = (-1.2 - 0.05 * tenure_months + 0.45 * tickets - 0.06 * usage_hours
             + 0.5 * (plan == "basic") + np.array([city_effect[c] for c in city]))
    churn = (rng.random(n) < 1 / (1 + np.exp(-logit))).astype(int)

    # Review text leans negative for churners
    words = []
    for y in churn:
        pool = NEG_WORDS if (rng.random() < (0.7 if y else 0.25)) else POS_WORDS
        words.append(" ".join(rng.choice(pool, 3)))

    df = pd.DataFrame({
        "customer_id": np.arange(n), "age": age, "income": income, "plan": plan,
        "device": device, "city": city, "signup_date": signup, "tenure_months": tenure_months,
        "support_tickets": tickets, "monthly_charges": monthly.round(2),
        "usage_hours": usage_hours.round(1), "review": words, "churn": churn,
    })

    # --- make it messy ---
    df.loc[rng.choice(n, 240, replace=False), "income"] = np.nan   # 8% missing income
    df.loc[rng.choice(n, 90, replace=False), "age"] = np.nan       # 3% missing age
    df.loc[rng.choice(n, 60, replace=False), "device"] = np.nan    # 2% missing device
    df.loc[rng.choice(n, 4, replace=False), "age"] = 999           # data-entry typos
    df.loc[rng.choice(n, 3, replace=False), "income"] = 9_999_999  # sentinel / typo
    dups = df.sample(25, random_state=seed)                        # exact duplicate rows
    df = pd.concat([df, dups], ignore_index=True)
    return df


if __name__ == "__main__":
    d = make_churn()
    print(d.shape)
    print(d.head(3).T)
