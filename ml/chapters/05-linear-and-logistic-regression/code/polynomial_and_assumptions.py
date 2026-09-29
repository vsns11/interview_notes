"""Polynomial regression (under/over-fitting by degree) and residual diagnostics for linear-regression assumptions."""
import numpy as np
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_squared_error
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import PolynomialFeatures, StandardScaler

from plotstyle import C, plt, save


def main() -> None:
    rng = np.random.default_rng(0)
    # Quadratic ground truth: y = 0.5 x^2 + x + 2 + noise
    x = rng.uniform(-3, 3, 40)
    y = 0.5 * x ** 2 + x + 2 + rng.normal(0, 1, 40)
    x_te = rng.uniform(-3, 3, 400)
    y_te = 0.5 * x_te ** 2 + x_te + 2 + rng.normal(0, 1, 400)

    grid = np.linspace(-3, 3, 300)
    fig, ax = plt.subplots(figsize=(7, 3.8))
    ax.scatter(x, y, s=14, color="#8a8983", label="training data", zorder=3)
    print(f"{'degree':>6}{'train MSE':>11}{'test MSE':>10}")
    for deg, col in [(1, C[0]), (2, C[2]), (15, C[1])]:
        model = make_pipeline(PolynomialFeatures(deg, include_bias=False), StandardScaler(), LinearRegression())
        model.fit(x[:, None], y)
        tr = mean_squared_error(y, model.predict(x[:, None]))
        te = mean_squared_error(y_te, model.predict(x_te[:, None]))
        print(f"{deg:>6}{tr:>11.2f}{te:>10.2f}")
        ax.plot(grid, model.predict(grid[:, None]), color=col, label=f"degree {deg}")
    lr2 = make_pipeline(PolynomialFeatures(2, include_bias=False), LinearRegression()).fit(x[:, None], y)
    print("degree-2 learned: y = %.2f x^2 + %.2f x + %.2f   (truth 0.5, 1, 2)"
          % (lr2[-1].coef_[1], lr2[-1].coef_[0], lr2[-1].intercept_))
    ax.set(ylim=(-2, 12), xlabel="x", ylabel="y", title="Degree 1 underfits, 2 fits, 15 overfits")
    ax.legend(loc="upper left")
    save(fig, "polynomial_degrees.png")

    # Residual plots: fitted value vs residual. Good model = structureless band around 0.
    lin = LinearRegression().fit(x[:, None], y)
    quad = lr2
    # heteroscedastic data: noise grows with x
    xh = rng.uniform(0, 10, 200)
    yh = 3 * xh + rng.normal(0, 0.3 + 0.5 * xh)
    het = LinearRegression().fit(xh[:, None], yh)

    fig, axes = plt.subplots(1, 3, figsize=(12, 3.2), sharey=False)
    for ax, (title, fitted, resid) in zip(axes, [
        ("straight line on curved data:\nU-shape = missed non-linearity", lin.predict(x[:, None]), y - lin.predict(x[:, None])),
        ("quadratic model:\nrandom band = assumptions OK", quad.predict(x[:, None]), y - quad.predict(x[:, None])),
        ("noise grows with x:\nfunnel = heteroscedasticity", het.predict(xh[:, None]), yh - het.predict(xh[:, None])),
    ]):
        ax.scatter(fitted, resid, s=10, color=C[0])
        ax.axhline(0, color="#0b0b0b", linewidth=1)
        ax.set(title=title, xlabel="fitted value", ylabel="residual")
    save(fig, "residual_plots.png")


if __name__ == "__main__":
    main()
