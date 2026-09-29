"""Regression metrics by hand and with sklearn: MAE, MSE, RMSE, R^2, percentile error, per-cohort error, baselines."""
import numpy as np
from sklearn.datasets import load_diabetes
from sklearn.dummy import DummyRegressor
from sklearn.linear_model import Ridge
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score, root_mean_squared_error
from sklearn.model_selection import train_test_split


def main() -> None:
    # 1) Worked example: delivery-time predictions in minutes
    y = np.array([30, 25, 40, 35, 50])
    yhat = np.array([28, 27, 35, 35, 60])
    err = yhat - y
    print("errors:", err)
    print(f"MAE  = mean(|e|)      = {np.abs(err).mean():.2f}   sklearn {mean_absolute_error(y, yhat):.2f}")
    print(f"MSE  = mean(e^2)      = {(err ** 2).mean():.2f}  sklearn {mean_squared_error(y, yhat):.2f}")
    print(f"RMSE = sqrt(MSE)      = {np.sqrt((err ** 2).mean()):.2f}   sklearn {root_mean_squared_error(y, yhat):.2f}")
    ss_res, ss_tot = (err ** 2).sum(), ((y - y.mean()) ** 2).sum()
    print(f"R^2  = 1 - {ss_res}/{ss_tot:.0f} = {1 - ss_res / ss_tot:.3f}  sklearn {r2_score(y, yhat):.3f}")

    # 2) One big miss: MAE grows linearly, RMSE grows much faster
    base = np.full(10, 10.0)                  # ten 10-minute errors
    one_big = base.copy(); one_big[0] = 40    # one of them becomes a 40-minute error
    for name, e in [("ten 10-min errors", base), ("one of them is 40 min", one_big)]:
        print(f"{name:<24} MAE {np.abs(e).mean():5.1f}   RMSE {np.sqrt((e ** 2).mean()):5.1f}")

    # 3) Real data + baselines + tail + cohorts
    X, y = load_diabetes(return_X_y=True)
    X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.3, random_state=0)
    model = Ridge(alpha=0.1).fit(X_tr, y_tr)
    pred = model.predict(X_te)
    print(f"\n{'model':<22}{'MAE':>7}{'RMSE':>7}{'R^2':>7}")
    for name, p in [("mean baseline", DummyRegressor(strategy="mean").fit(X_tr, y_tr).predict(X_te)),
                    ("median baseline", DummyRegressor(strategy="median").fit(X_tr, y_tr).predict(X_te)),
                    ("Ridge", pred)]:
        print(f"{name:<22}{mean_absolute_error(y_te, p):>7.1f}{root_mean_squared_error(y_te, p):>7.1f}{r2_score(y_te, p):>7.3f}")
    abs_err = np.abs(y_te - pred)
    print(f"p50 abs error {np.percentile(abs_err, 50):.1f} | p90 abs error {np.percentile(abs_err, 90):.1f}")
    sex = X_te[:, 1] > 0                      # 'sex' column (scaled) splits patients into two cohorts
    for label, mask in [("cohort A", ~sex), ("cohort B", sex)]:
        print(f"{label}: n={mask.sum():>3}  MAE {mean_absolute_error(y_te[mask], pred[mask]):.1f}")


if __name__ == "__main__":
    main()
