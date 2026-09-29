"""Scaling: StandardScaler vs MinMaxScaler vs RobustScaler, and which models actually care."""
import numpy as np
from sklearn.datasets import load_wine
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import cross_val_score
from sklearn.neighbors import KNeighborsClassifier
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import MinMaxScaler, RobustScaler, StandardScaler
from sklearn.svm import SVC
from sklearn.tree import DecisionTreeClassifier

from plotstyle import C, plt, save


def main() -> None:
    # 1) What each scaler does to a column with one outlier
    x = np.array([[10.0], [12], [11], [13], [12], [11], [100]])
    print("raw       :", x.ravel())
    for s in [StandardScaler(), MinMaxScaler(), RobustScaler()]:
        print(f"{type(s).__name__:<15}:", s.fit_transform(x).ravel().round(2))

    # 2) Which models care? Wine: 13 features on very different scales (proline ~ 1000, hue ~ 1)
    X, y = load_wine(return_X_y=True)
    print("\nfeature ranges: min std = %.3f, max std = %.1f" % (X.std(0).min(), X.std(0).max()))
    models = {
        "k-NN (k=5)": KNeighborsClassifier(),
        "SVM (RBF)": SVC(),
        "LogReg (lbfgs)": LogisticRegression(max_iter=5000),
        "Decision tree": DecisionTreeClassifier(random_state=0),
    }
    print(f"\n{'model':<16}{'no scaling':>12}{'standard':>10}{'min-max':>9}{'robust':>8}")
    rows = {}
    for name, m in models.items():
        accs = [cross_val_score(m, X, y, cv=5).mean()]
        for sc in [StandardScaler(), MinMaxScaler(), RobustScaler()]:
            accs.append(cross_val_score(make_pipeline(sc, m), X, y, cv=5).mean())
        rows[name] = accs
        print(f"{name:<16}" + "".join(f"{a:>10.3f}" for a in accs))

    fig, ax = plt.subplots(figsize=(7, 3.2))
    names = list(rows)
    w = 0.38
    xs = np.arange(len(names))
    ax.bar(xs - w / 2 - 0.01, [rows[n][0] for n in names], w, color=C[1], label="no scaling")
    ax.bar(xs + w / 2 + 0.01, [rows[n][1] for n in names], w, color=C[0], label="StandardScaler")
    ax.set_xticks(xs, names)
    ax.set_ylim(0.5, 1.02)
    ax.set_ylabel("5-fold CV accuracy")
    ax.set_title("Wine data: distance/gradient models need scaling, trees do not")
    ax.legend(loc="lower right")
    save(fig, "scaling_effect.png")


if __name__ == "__main__":
    main()
