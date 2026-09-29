"""Measure group fairness of a credit model: selection rate, TPR, FPR, PPV,
calibration - and show that dropping the sensitive column does not help."""
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import roc_auc_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler

from credit_data import FEATURES, make_credit_data


def group_report(y, y_hat, group):
    """One row per group with the confusion-matrix rates fairness is built on."""
    rows = []
    for g in np.unique(group):
        m = group == g
        yt, yp = y[m], y_hat[m]
        tp = np.sum((yt == 1) & (yp == 1)); fp = np.sum((yt == 0) & (yp == 1))
        fn = np.sum((yt == 1) & (yp == 0)); tn = np.sum((yt == 0) & (yp == 0))
        rows.append({
            "group": "AB"[g], "n": m.sum(), "base_rate": yt.mean(),
            "selection_rate": yp.mean(),                 # P(approve | group)
            "TPR": tp / (tp + fn),                       # P(approve | repays, group)
            "FPR": fp / (fp + tn),                       # P(approve | defaults, group)
            "PPV": tp / (tp + fp),                       # P(repays | approved, group)
            "accuracy": (tp + tn) / m.sum(),
        })
    return pd.DataFrame(rows).set_index("group")


def fairness_summary(r):
    a, b = r.loc["A"], r.loc["B"]
    return {
        "demographic_parity_diff": a.selection_rate - b.selection_rate,
        "disparate_impact_ratio": b.selection_rate / a.selection_rate,   # 4/5 rule: >= 0.8
        "equal_opportunity_diff": a.TPR - b.TPR,
        "equalized_odds_diff": max(abs(a.TPR - b.TPR), abs(a.FPR - b.FPR)),
        "predictive_parity_diff": a.PPV - b.PPV,
    }


def calibration_by_group(y, score, group, bins=5):
    edges = np.linspace(0, 1, bins + 1)
    out = {}
    for g in np.unique(group):
        m = group == g
        idx = np.clip(np.digitize(score[m], edges) - 1, 0, bins - 1)
        out["AB"[g]] = [(score[m][idx == k].mean(), y[m][idx == k].mean())
                        for k in range(bins) if np.sum(idx == k) > 30]
    return out


def main():
    df = make_credit_data()
    train, test = train_test_split(df, test_size=0.3, random_state=0, stratify=df.group)

    # 1) "fairness through unawareness": the model never sees `group`
    model = make_pipeline(StandardScaler(), LogisticRegression())
    model.fit(train[FEATURES], train.y)
    score = model.predict_proba(test[FEATURES])[:, 1]
    y_hat = (score >= 0.5).astype(int)
    g, y = test.group.to_numpy(), test.y.to_numpy()

    pd.set_option("display.precision", 3)
    rep = group_report(y, y_hat, g)
    print("Group report vs RECORDED labels (what an auditor can see):")
    print(rep.to_string())
    for k, v in fairness_summary(rep).items():
        print(f"  {k:24s} {v:+.3f}")

    rep_true = group_report(test.y_true.to_numpy(), y_hat, g)
    print("\nSame predictions vs the hidden TRUE labels:")
    print(rep_true[["base_rate", "selection_rate", "TPR", "FPR"]].to_string())

    # 2) the proxy problem: can the features reveal the group?
    proxy = LogisticRegression().fit(train[FEATURES], train.group)
    print("\nAUC for predicting group from the 'group-blind' features:",
          round(roc_auc_score(test.group, proxy.predict_proba(test[FEATURES])[:, 1]), 3))

    # 3) calibration within groups
    cal = calibration_by_group(y, score, g)
    for grp, pts in cal.items():
        print(f"calibration {grp}:", " ".join(f"({p:.2f}->{o:.2f})" for p, o in pts))

    # plots
    fig, ax = plt.subplots(1, 2, figsize=(10, 3.8))
    rep[["selection_rate", "TPR", "FPR", "PPV"]].T.plot.bar(ax=ax[0], rot=0, color=["#4C72B0", "#DD8452"])
    ax[0].set_title("Group rates at threshold 0.5"); ax[0].set_ylim(0, 1)
    for grp, pts in cal.items():
        p, o = zip(*pts)
        ax[1].plot(p, o, "o-", label=f"group {grp}")
    ax[1].plot([0, 1], [0, 1], "k--", lw=1, label="perfect")
    ax[1].set_xlabel("mean predicted score"); ax[1].set_ylabel("observed repay rate (recorded)")
    ax[1].set_title("Calibration by group"); ax[1].legend()
    fig.tight_layout(); fig.savefig("../img/group_rates_calibration.png", dpi=130)


if __name__ == "__main__":
    main()
