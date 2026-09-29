"""Main challenges of ML, demonstrated: under/overfitting, data leakage, non-representative data,
and noisy labels (garbage in, garbage out). Saves img/under_over_fit.png."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.feature_selection import SelectKBest, f_classif
from sklearn.linear_model import LinearRegression, LogisticRegression
from sklearn.metrics import mean_squared_error
from sklearn.model_selection import cross_val_score, train_test_split
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import PolynomialFeatures, StandardScaler
from sklearn.datasets import load_breast_cancer

IMG = Path(__file__).resolve().parent.parent / "img"
SEED = 0


def under_over_fit() -> None:
    rng = np.random.default_rng(SEED)
    X = np.sort(rng.uniform(0, 1, 20))[:, None]
    y = np.sin(2 * np.pi * X[:, 0]) + rng.normal(0, 0.2, 20)
    X_val = rng.uniform(0, 1, 200)[:, None]
    y_val = np.sin(2 * np.pi * X_val[:, 0]) + rng.normal(0, 0.2, 200)

    grid = np.linspace(0, 1, 300)[:, None]
    fig, axes = plt.subplots(1, 3, figsize=(12, 3.6), sharey=True)
    print("degree  train MSE  val MSE")
    for ax, deg, name in zip(axes, [1, 4, 15], ["underfit", "good fit", "overfit"]):
        model = make_pipeline(PolynomialFeatures(deg), LinearRegression()).fit(X, y)
        tr = mean_squared_error(y, model.predict(X))
        va = mean_squared_error(y_val, model.predict(X_val))
        print(f"{deg:>6}  {tr:9.3f}  {va:7.3f}")
        ax.scatter(X, y, s=15, color="#444", label="training data")
        ax.plot(grid, np.sin(2 * np.pi * grid), "--", color="#999", label="true pattern")
        ax.plot(grid, np.clip(model.predict(grid), -2, 2), color="#1f77b4", lw=2, label="model")
        ax.set_title(f"degree {deg}: {name}\ntrain MSE {tr:.3f} | val MSE {va:.3f}", fontsize=10)
        ax.set_ylim(-2, 2)
    axes[0].legend(fontsize=8, loc="lower left")
    fig.tight_layout()
    fig.savefig(IMG / "under_over_fit.png", dpi=120)
    plt.close(fig)


def leakage() -> None:
    # Pure noise: 100 rows, 5,000 random features, random labels. Honest accuracy should be ~50%.
    rng = np.random.default_rng(SEED)
    X = rng.standard_normal((100, 5000))
    y = rng.integers(0, 2, 100)

    # WRONG: pick the 20 "best" features using ALL rows, then cross-validate.
    X_leaky = SelectKBest(f_classif, k=20).fit_transform(X, y)
    wrong = cross_val_score(LogisticRegression(max_iter=1000), X_leaky, y, cv=5).mean()

    # RIGHT: feature selection lives inside the pipeline, so it is refit on each training fold.
    pipe = make_pipeline(SelectKBest(f_classif, k=20), LogisticRegression(max_iter=1000))
    right = cross_val_score(pipe, X, y, cv=5).mean()
    print(f"leaky CV accuracy on pure noise: {wrong:.2f}  |  honest CV accuracy: {right:.2f}")


def non_representative() -> None:
    # Train only on small houses, then predict big ones: the relationship bends outside what we saw.
    rng = np.random.default_rng(SEED)
    size = rng.uniform(50, 300, 500)                  # m^2
    price = 2000 * size - 3 * size**2 + rng.normal(0, 20000, 500)
    small = size < 120
    model = LinearRegression().fit(size[small, None], price[small])
    err_in = np.abs(model.predict(size[small, None]) - price[small]).mean()
    err_out = np.abs(model.predict(size[~small, None]) - price[~small]).mean()
    print(f"trained on houses < 120 m^2: mean error small houses {err_in:,.0f} | big houses {err_out:,.0f}")


def noisy_labels() -> None:
    X, y = load_breast_cancer(return_X_y=True)
    Xtr, Xte, ytr, yte = train_test_split(X, y, random_state=SEED, stratify=y)
    rng = np.random.default_rng(SEED)
    for flip in [0.0, 0.2, 0.4]:
        y_noisy = ytr.copy()
        idx = rng.random(len(ytr)) < flip
        y_noisy[idx] = 1 - y_noisy[idx]               # corrupt a fraction of labels
        model = make_pipeline(StandardScaler(), LogisticRegression(max_iter=5000)).fit(Xtr, y_noisy)
        print(f"{int(flip*100):>2}% wrong training labels -> test accuracy {model.score(Xte, yte):.3f}")


if __name__ == "__main__":
    under_over_fit()
    print()
    leakage()
    print()
    non_representative()
    print()
    noisy_labels()
