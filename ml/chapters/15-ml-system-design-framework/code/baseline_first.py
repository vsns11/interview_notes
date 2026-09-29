"""Baseline first: naive baseline -> heuristic baseline -> GBDT on a synthetic delivery-time (ETA) task.

Also turns the ML metric (MAE) into the business metric (promise-keeping rate) by
adding a display buffer, which is a product setting, not part of the model.
"""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.ensemble import HistGradientBoostingRegressor
from sklearn.linear_model import LinearRegression

IMG = Path(__file__).resolve().parent.parent / "img"
rng = np.random.default_rng(0)


def make_orders(n):
    """Synthetic orders. Delivery minutes depend on prep time, distance, kitchen queue and rain."""
    restaurant = rng.integers(0, 200, n)
    prep_base = 8 + (restaurant % 20)                      # each restaurant has its own prep speed
    distance_km = rng.gamma(2.0, 1.5, n)
    queue_now = rng.poisson(3, n)                          # orders already in the kitchen (streaming feature)
    hour = rng.integers(10, 23, n)
    rain = rng.random(n) < 0.15
    dinner = (hour >= 18) & (hour <= 20)
    minutes = (prep_base + 2.0 * queue_now * (1 + dinner)  # busy kitchens slow down more at dinner
               + 4.0 * distance_km * (1 + 0.5 * rain)      # rain slows the courier
               + rng.normal(0, 3, n))
    X = np.column_stack([restaurant, distance_km, queue_now, hour, rain])
    return X, minutes


def report(name, y, pred):
    err = np.abs(y - pred)
    return name, err.mean(), np.percentile(err, 90)


if __name__ == "__main__":
    # temporal-style split: first 40k orders = "past" (train), last 10k = "future" (test)
    X, y = make_orders(50_000)
    Xtr, ytr, Xte, yte = X[:40_000], y[:40_000], X[40_000:], y[40_000:]
    rows = []

    # 1) naive baseline: everyone gets the global average
    rows.append(report("global mean", yte, np.full(len(yte), ytr.mean())))

    # 2) heuristic baseline: per-restaurant average + a distance term (what you could ship in a week)
    rest_avg = {r: ytr[Xtr[:, 0] == r].mean() for r in np.unique(Xtr[:, 0])}
    resid = ytr - np.array([rest_avg[r] for r in Xtr[:, 0]])
    dist_fit = LinearRegression().fit(Xtr[:, [1]], resid)
    heur = np.array([rest_avg.get(r, ytr.mean()) for r in Xte[:, 0]]) + dist_fit.predict(Xte[:, [1]])
    rows.append(report("restaurant avg + distance", yte, heur))

    # 3) learned model: gradient-boosted trees on all features
    gbdt = HistGradientBoostingRegressor(random_state=0, categorical_features=[0]).fit(Xtr, ytr)
    pred = gbdt.predict(Xte)
    rows.append(report("GBDT (all features)", yte, pred))

    print(f"{'model':28s} {'MAE':>6s} {'p90 abs err':>12s}")
    for name, mae, p90 in rows:
        print(f"{name:28s} {mae:6.2f} {p90:12.2f}")

    # business metric: % of orders that arrive within the promised time (prediction + buffer)
    print("\nbuffer (min) -> promise kept % / avg promised minutes   [GBDT]")
    buffers = np.arange(0, 11)
    kept = [(yte <= pred + b).mean() * 100 for b in buffers]
    for b in (0, 3, 5, 8):
        print(f"  +{b:2d} min -> {kept[b]:5.1f}%  / {np.mean(pred + b):5.1f}")

    # plot: MAE per model, and the buffer trade-off curve
    fig, ax = plt.subplots(1, 2, figsize=(10, 3.6))
    ax[0].barh([r[0] for r in rows], [r[1] for r in rows], color=["#999", "#6a9fd8", "#2b6cb0"])
    ax[0].set_xlabel("test MAE (minutes, lower is better)")
    ax[0].invert_yaxis()
    ax[1].plot(buffers, kept, marker="o")
    ax[1].set_xlabel("buffer added to the displayed ETA (min)")
    ax[1].set_ylabel("orders delivered on time (%)")
    ax[1].grid(alpha=.3)
    fig.tight_layout()
    fig.savefig(IMG / "baseline_and_buffer.png", dpi=130)
    print("saved img/baseline_and_buffer.png")
