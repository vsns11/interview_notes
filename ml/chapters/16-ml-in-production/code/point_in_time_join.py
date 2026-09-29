"""Label leakage vs a point-in-time-correct join.

Feature: "restaurant's average delivery time today".
  LEAKY  = computed over the WHOLE day (includes the order being predicted and later ones).
  PIT    = computed only from orders that finished BEFORE this order was placed (what serving sees).
The leaky model looks great offline and gets worse when it meets real serving features.
"""
import numpy as np
import pandas as pd
from sklearn.ensemble import HistGradientBoostingRegressor
from sklearn.metrics import mean_absolute_error

rng = np.random.default_rng(0)


def make_orders(n_days=60, n_rest=80, per_day=10):
    rows = []
    for day in range(n_days):
        for r in range(n_rest):
            day_effect = rng.normal(0, 6)                    # today this kitchen is fast or slow
            times = np.sort(rng.uniform(10, 22, per_day))    # order-placed hour
            dist = rng.gamma(2, 1.5, per_day)
            y = 25 + day_effect + 4 * dist + rng.normal(0, 4, per_day)
            for t, d, m in zip(times, dist, y):
                rows.append((day, r, day * 24 + t, d, m))
    df = pd.DataFrame(rows, columns=["day", "rest", "placed_h", "dist", "minutes"])
    df["done_h"] = df["placed_h"] + df["minutes"] / 60       # when the label becomes known
    return df.sort_values("placed_h").reset_index(drop=True)


def pit_feature(df):
    """As-of join: for each order, the mean of same-day same-restaurant orders DONE before it was placed."""
    done = (df[["rest", "day", "done_h", "minutes"]].sort_values("done_h").copy())
    done["cum_sum"] = done.groupby(["rest", "day"])["minutes"].cumsum()
    done["cum_n"] = done.groupby(["rest", "day"]).cumcount() + 1
    left = df[["rest", "day", "placed_h"]].reset_index().sort_values("placed_h")
    j = pd.merge_asof(left, done[["rest", "day", "done_h", "cum_sum", "cum_n"]],
                      left_on="placed_h", right_on="done_h", by=["rest", "day"],
                      direction="backward", allow_exact_matches=False)
    j = j.set_index("index").sort_index()
    return (j["cum_sum"] / j["cum_n"]).to_numpy()            # NaN when nothing finished yet today


if __name__ == "__main__":
    df = make_orders()
    df["avg_today_LEAKY"] = df.groupby(["rest", "day"])["minutes"].transform("mean")
    df["avg_today_PIT"] = pit_feature(df)
    print(df[["placed_h", "rest", "minutes", "avg_today_LEAKY", "avg_today_PIT"]].head(4).round(1))
    print(f"PIT feature missing (no finished orders yet today): {df['avg_today_PIT'].isna().mean():.1%}\n")

    train, test = df[df.day < 48], df[df.day >= 48]           # temporal split: last 12 days = test
    y_tr, y_te = train["minutes"], test["minutes"]

    def fit(col):
        m = HistGradientBoostingRegressor(random_state=0)     # handles NaN natively
        return m.fit(train[["dist", col]], y_tr)

    leaky = fit("avg_today_LEAKY")
    pit = fit("avg_today_PIT")
    base = HistGradientBoostingRegressor(random_state=0).fit(train[["dist"]], y_tr)

    offline_leaky = mean_absolute_error(y_te, leaky.predict(test[["dist", "avg_today_LEAKY"]]))
    # at serving time only the PIT value exists; feed it to the leaky model under the leaky name
    serving_leaky = mean_absolute_error(
        y_te, leaky.predict(test[["dist", "avg_today_PIT"]].set_axis(["dist", "avg_today_LEAKY"], axis=1)))
    offline_pit = mean_absolute_error(y_te, pit.predict(test[["dist", "avg_today_PIT"]]))

    print(f"{'setup':45s} MAE (min)")
    print(f"{'distance only (no daily feature)':45s} {mean_absolute_error(y_te, base.predict(test[['dist']])):.2f}")
    print(f"{'LEAKY model, offline test (looks amazing)':45s} {offline_leaky:.2f}")
    print(f"{'LEAKY model, fed real serving features':45s} {serving_leaky:.2f}")
    print(f"{'PIT model, offline = what serving will see':45s} {offline_pit:.2f}")
