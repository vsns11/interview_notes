"""Calibration: reliability diagrams, Brier score, and fixing miscalibration with Platt (sigmoid) or isotonic scaling."""
import numpy as np
from sklearn.calibration import CalibratedClassifierCV, calibration_curve
from sklearn.datasets import make_classification
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import brier_score_loss, log_loss, roc_auc_score
from sklearn.model_selection import train_test_split
from sklearn.naive_bayes import GaussianNB

from plotstyle import C, plt, save


def main() -> None:
    X, y = make_classification(n_samples=20_000, n_features=20, n_informative=5, n_redundant=10,
                               random_state=0)
    X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.3, random_state=0, stratify=y)

    models = {
        "logistic regression": LogisticRegression(max_iter=1000),
        "Gaussian naive Bayes": GaussianNB(),
        "random forest": RandomForestClassifier(n_estimators=100, random_state=0),
        "naive Bayes + isotonic": CalibratedClassifierCV(GaussianNB(), method="isotonic", cv=5),
        "naive Bayes + Platt (sigmoid)": CalibratedClassifierCV(GaussianNB(), method="sigmoid", cv=5),
    }
    fig, ax = plt.subplots(figsize=(6.5, 5))
    ax.plot([0, 1], [0, 1], "--", color="#8a8983", linewidth=1, label="perfectly calibrated")
    print(f"{'model':<31}{'ROC-AUC':>8}{'Brier':>8}{'log-loss':>9}")
    for (name, m), col in zip(models.items(), C):
        m.fit(X_tr, y_tr)
        p = m.predict_proba(X_te)[:, 1]
        print(f"{name:<31}{roc_auc_score(y_te, p):>8.3f}{brier_score_loss(y_te, p):>8.3f}{log_loss(y_te, p):>9.3f}")
        if name in ("logistic regression", "Gaussian naive Bayes", "random forest", "naive Bayes + isotonic"):
            frac_pos, mean_pred = calibration_curve(y_te, p, n_bins=10, strategy="quantile")
            ax.plot(mean_pred, frac_pos, "-o", color=col, markersize=5, label=name)
    ax.set(xlabel="mean predicted probability (per bin)", ylabel="observed fraction of positives",
           title="Reliability diagram (10 equal-count bins)")
    ax.legend(loc="upper left", fontsize=8)
    save(fig, "calibration_reliability.png")

    # Brier score by hand
    yt = np.array([1, 0, 1, 1])
    pp = np.array([0.9, 0.2, 0.6, 0.3])
    print(f"\nBrier by hand: mean((p - y)^2) = {np.mean((pp - yt) ** 2):.3f}  sklearn {brier_score_loss(yt, pp):.3f}")


if __name__ == "__main__":
    main()
