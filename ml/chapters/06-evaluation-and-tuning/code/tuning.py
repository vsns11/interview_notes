"""Hyperparameter tuning: grid search, random search, successive halving, a tiny Bayesian optimisation, early stopping."""
import os

os.environ.setdefault("OMP_NUM_THREADS", "2")  # keep boosting fast on busy machines

import warnings  # noqa: E402

import numpy as np  # noqa: E402
from scipy.stats import loguniform, randint  # noqa: E402
from sklearn.datasets import load_breast_cancer, load_digits, make_classification  # noqa: E402
from sklearn.ensemble import HistGradientBoostingClassifier  # noqa: E402
from sklearn.experimental import enable_halving_search_cv  # noqa: E402,F401
from sklearn.gaussian_process import GaussianProcessRegressor  # noqa: E402
from sklearn.gaussian_process.kernels import Matern, WhiteKernel  # noqa: E402
from sklearn.model_selection import (GridSearchCV, HalvingRandomSearchCV, RandomizedSearchCV,  # noqa: E402
                                     StratifiedKFold, cross_val_score, train_test_split)
from sklearn.pipeline import make_pipeline  # noqa: E402
from sklearn.preprocessing import StandardScaler  # noqa: E402
from sklearn.svm import SVC  # noqa: E402
from sklearn.tree import DecisionTreeClassifier  # noqa: E402
from scipy.stats import norm  # noqa: E402

from plotstyle import C, plt, save  # noqa: E402

warnings.filterwarnings("ignore", category=UserWarning)


def main() -> None:
    X, y = load_breast_cancer(return_X_y=True)
    X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.25, stratify=y, random_state=0)

    # 1) Grid search: every combination (3 x 3 = 9 candidates x 3 folds = 27 fits)
    grid = GridSearchCV(DecisionTreeClassifier(random_state=0),
                        {"max_depth": [3, 5, 7], "min_samples_split": [2, 5, 10]}, cv=3, scoring="f1")
    grid.fit(X_tr, y_tr)
    print("grid   best params:", grid.best_params_, f"CV F1 {grid.best_score_:.3f}",
          f"| test F1 {grid.score(X_te, y_te):.3f}")

    # 2) Random search: sample 10 combinations from distributions
    rnd = RandomizedSearchCV(DecisionTreeClassifier(random_state=0),
                             {"max_depth": randint(3, 10), "min_samples_split": randint(2, 20),
                              "min_samples_leaf": randint(1, 10)},
                             n_iter=10, cv=3, scoring="f1", random_state=0)
    rnd.fit(X_tr, y_tr)
    print("random best params:", rnd.best_params_, f"CV F1 {rnd.best_score_:.3f}",
          f"| test F1 {rnd.score(X_te, y_te):.3f}")

    # 3) Same budget, continuous hyperparameters: SVM on digits, 9 evaluations each
    Xd, yd = load_digits(return_X_y=True)
    cv = StratifiedKFold(3, shuffle=True, random_state=0)
    svm = make_pipeline(StandardScaler(), SVC())
    g = GridSearchCV(svm, {"svc__C": [0.1, 1, 10], "svc__gamma": [1e-4, 1e-3, 1e-2]}, cv=cv).fit(Xd, yd)
    r = RandomizedSearchCV(svm, {"svc__C": loguniform(1e-2, 1e3), "svc__gamma": loguniform(1e-5, 1e-1)},
                           n_iter=9, cv=cv, random_state=1).fit(Xd, yd)
    print(f"\nSVM, 9 evaluations each: grid best {g.best_score_:.4f} {g.best_params_}")
    print(f"                         random best {r.best_score_:.4f} "
          f"{ {k: float(f'{v:.3g}') for k, v in r.best_params_.items()} }")

    # 4) Successive halving: many candidates on little data, survivors get more
    h = HalvingRandomSearchCV(svm, {"svc__C": loguniform(1e-2, 1e3), "svc__gamma": loguniform(1e-5, 1e-1)},
                              n_candidates=32, factor=2, cv=cv, random_state=0).fit(Xd, yd)
    print(f"halving: {h.n_candidates_} candidates per round, samples per round {h.n_resources_}"
          f" -> best {h.best_score_:.4f}")

    # 5) A tiny Bayesian optimisation of log10(gamma) with a Gaussian-process surrogate + expected improvement
    def objective(log_gamma):
        return cross_val_score(make_pipeline(StandardScaler(), SVC(C=10, gamma=10 ** log_gamma)), Xd, yd, cv=cv).mean()

    rng = np.random.default_rng(0)
    grid_pts = np.linspace(-5, -1, 200)[:, None]
    xs = list(rng.uniform(-5, -1, 3))                   # 3 random starting points
    ys = [objective(v) for v in xs]
    for _ in range(6):                                  # 6 guided evaluations
        gp = GaussianProcessRegressor(Matern(nu=2.5) + WhiteKernel(1e-4), normalize_y=True, random_state=0)
        gp.fit(np.array(xs)[:, None], ys)
        mu, sd = gp.predict(grid_pts, return_std=True)
        imp = mu - max(ys)
        z = imp / np.maximum(sd, 1e-9)
        ei = imp * norm.cdf(z) + sd * norm.pdf(z)        # expected improvement
        nxt = float(grid_pts[ei.argmax(), 0])
        xs.append(nxt)
        ys.append(objective(nxt))
    print("\nBayesian optimisation of gamma (9 evaluations):")
    for i, (a, b) in enumerate(zip(xs, ys)):
        print(f"  eval {i + 1}: gamma = 1e{a:+.2f}  CV accuracy {b:.4f}" + ("  (random start)" if i < 3 else ""))

    # 6) Early stopping for gradient boosting: watch validation loss per iteration
    Xc, yc = make_classification(n_samples=4000, n_features=20, n_informative=6, flip_y=0.15, random_state=0)
    full = HistGradientBoostingClassifier(max_iter=400, learning_rate=0.2, early_stopping=True,
                                          n_iter_no_change=400, validation_fraction=0.2, random_state=0).fit(Xc, yc)
    stop = HistGradientBoostingClassifier(max_iter=400, learning_rate=0.2, early_stopping=True,
                                          n_iter_no_change=10, validation_fraction=0.2, random_state=0).fit(Xc, yc)
    tr_loss, va_loss = -full.train_score_, -full.validation_score_     # scores are negative log-loss
    print(f"\nfull run: best validation loss at iteration {va_loss.argmin()} of {len(va_loss) - 1}")
    print(f"early stopping (patience 10) stopped after {stop.n_iter_} iterations")
    fig, ax = plt.subplots(figsize=(6.5, 3.6))
    ax.plot(tr_loss, color=C[0], label="training loss")
    ax.plot(va_loss, color=C[1], label="validation loss")
    ax.axvline(stop.n_iter_, color="#8a8983", linestyle="--", linewidth=1, label="early stop")
    ax.set(xlabel="boosting iteration", ylabel="log-loss", title="Stop when validation loss stops improving")
    ax.legend()
    save(fig, "early_stopping.png")


if __name__ == "__main__":
    main()
