"""Three more attacks on ML models, each in a few lines:
  1) data poisoning by label flipping (random vs targeted)
  2) model extraction: copy a black-box model through its prediction API
  3) membership inference: was this record in the training set?
"""
import numpy as np
from sklearn.datasets import make_classification
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import recall_score, roc_auc_score
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeClassifier

rng = np.random.default_rng(0)


def poisoning():
    X, y = make_classification(n_samples=4000, n_features=10, n_informative=6, random_state=1)
    Xtr, Xte, ytr, yte = train_test_split(X, y, test_size=0.3, random_state=0)
    print("1) label-flip poisoning (logistic regression, test accuracy / recall of class 1)")
    for frac in (0.0, 0.1, 0.2, 0.3):
        # random: flip a fraction of ALL labels;  targeted: relabel a fraction of class-1 rows as 0
        y_rand = ytr.copy(); idx = rng.choice(len(ytr), int(frac * len(ytr)), replace=False)
        y_rand[idx] = 1 - y_rand[idx]
        y_tgt = ytr.copy(); ones = np.flatnonzero(ytr == 1)
        y_tgt[rng.choice(ones, int(frac * len(ones)), replace=False)] = 0
        out = []
        for yy in (y_rand, y_tgt):
            m = LogisticRegression(max_iter=1000).fit(Xtr, yy)
            out.append(f"{m.score(Xte, yte):.3f}/{recall_score(yte, m.predict(Xte)):.3f}")
        print(f"   poisoned {int(frac*100):2d}%:  random flips {out[0]}   targeted 1->0 {out[1]}")


def extraction():
    X, y = make_classification(n_samples=15000, n_features=6, n_informative=4, random_state=2)
    X_pool = X[10000:]                         # unlabelled look-alike data the attacker owns
    Xtr, Xte, ytr, yte = train_test_split(X[:10000], y[:10000], test_size=0.3, random_state=0)
    victim = RandomForestClassifier(n_estimators=200, random_state=0).fit(Xtr, ytr)
    print(f"\n2) model extraction - victim test accuracy {victim.score(Xte, yte):.3f}")
    lo, hi = Xtr.min(0), Xtr.max(0)
    for n_queries in (100, 1000, 5000):
        row = []
        for kind in ("random", "look-alike"):
            Q = rng.uniform(lo, hi, (n_queries, X.shape[1])) if kind == "random" else X_pool[:n_queries]
            answers = victim.predict(Q)         # the only thing the API returns
            stolen = DecisionTreeClassifier(max_depth=10, random_state=0).fit(Q, answers)
            row.append(np.mean(stolen.predict(Xte) == victim.predict(Xte)))
        print(f"   {n_queries:5d} queries: agreement with victim - random queries {row[0]:.3f},"
              f" look-alike queries {row[1]:.3f}")


def membership_inference():
    X, y = make_classification(n_samples=2000, n_features=20, n_informative=5, flip_y=0.1, random_state=3)
    X_in, X_out, y_in, y_out = train_test_split(X, y, test_size=0.5, random_state=0)
    print("\n3) membership inference (attack = 'high confidence on the true label means member')")
    for name, model in [("overfit forest (no depth limit)", RandomForestClassifier(200, random_state=0)),
                        ("regularised forest (depth 4)", RandomForestClassifier(200, max_depth=4, random_state=0))]:
        model.fit(X_in, y_in)
        conf_in = model.predict_proba(X_in)[np.arange(len(y_in)), y_in]      # members
        conf_out = model.predict_proba(X_out)[np.arange(len(y_out)), y_out]  # non-members
        auc = roc_auc_score(np.r_[np.ones(len(conf_in)), np.zeros(len(conf_out))], np.r_[conf_in, conf_out])
        print(f"   {name:32s} train acc {model.score(X_in, y_in):.3f}  test acc {model.score(X_out, y_out):.3f}"
              f"  attack AUC {auc:.3f}")


if __name__ == "__main__":
    poisoning()
    extraction()
    membership_inference()
