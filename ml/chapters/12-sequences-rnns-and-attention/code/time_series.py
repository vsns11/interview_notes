"""Time-series forecasting intro: lag features, baselines, a linear autoregressive model (the 'AR' in ARIMA),
and gradient boosting with lag features - evaluated on a chronological hold-out."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from sklearn.ensemble import HistGradientBoostingRegressor
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_absolute_error

IMG = Path(__file__).resolve().parent.parent / "img"


def make_series(n=500, seed=0):
    rng = np.random.default_rng(seed)
    t = np.arange(n)
    y = 100 + 0.1 * t + 10 * np.sin(2 * np.pi * t / 7) + rng.normal(0, 2, n)   # trend + weekly season + noise
    return pd.Series(y, index=pd.date_range("2024-01-01", periods=n, freq="D"), name="sales")


def lag_features(s, lags=(1, 2, 3, 7, 14)):
    df = pd.DataFrame({"y": s})
    for L in lags:
        df[f"lag_{L}"] = s.shift(L)                       # value L days earlier (only past info!)
    df["rolling_mean_7"] = s.shift(1).rolling(7).mean()   # shift first so today's value doesn't leak
    df["dayofweek"] = s.index.dayofweek
    return df.dropna()


if __name__ == "__main__":
    s = make_series()
    df = lag_features(s)
    test_days = 60
    train, test = df.iloc[:-test_days], df.iloc[-test_days:]        # chronological split - never shuffle
    X_cols = [c for c in df.columns if c != "y"]
    print("feature table (first 3 rows):")
    print(df.head(3).round(1).to_string())

    preds = {
        "naive (yesterday)": test["lag_1"],
        "seasonal naive (same weekday last week)": test["lag_7"],
    }
    ar = LinearRegression().fit(train[X_cols], train["y"])           # autoregression on lags
    preds["linear AR on lag features"] = ar.predict(test[X_cols])
    gbm = HistGradientBoostingRegressor(random_state=0).fit(train[X_cols], train["y"])
    preds["gradient boosting on lag features"] = gbm.predict(test[X_cols])
    # trees can't extrapolate a trend -> model the day-to-day change, not the level
    d_train = train["y"] - train["lag_1"]
    gbm_d = HistGradientBoostingRegressor(random_state=0).fit(train[X_cols].sub(train["lag_1"], axis=0).assign(
        dayofweek=train["dayofweek"]), d_train)
    Xd_test = test[X_cols].sub(test["lag_1"], axis=0).assign(dayofweek=test["dayofweek"])
    preds["gradient boosting on differences"] = test["lag_1"] + gbm_d.predict(Xd_test)

    print(f"\none-step-ahead MAE on the last {test_days} days (noise std = 2.0):")
    for name, p in preds.items():
        print(f"  {name:42s} {mean_absolute_error(test['y'], p):6.2f}")
    print(f"train target range {train['y'].min():.0f}-{train['y'].max():.0f}, test range {test['y'].min():.0f}-{test['y'].max():.0f}")

    plt.figure(figsize=(10, 4))
    plt.plot(s.index[-150:], s.values[-150:], color="k", lw=1, label="actual")
    for name in ["seasonal naive (same weekday last week)", "linear AR on lag features", "gradient boosting on lag features"]:
        plt.plot(test.index, preds[name], lw=1.2, label=name)
    plt.axvline(test.index[0], color="gray", ls="--"); plt.legend(fontsize=8); plt.title("One-step-ahead forecasts on the test period")
    plt.tight_layout(); plt.savefig(IMG / "time_series_forecasts.png", dpi=120)
    print("saved img/time_series_forecasts.png")
