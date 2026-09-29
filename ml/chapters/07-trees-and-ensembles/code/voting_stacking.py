"""Voting and stacking ensembles of different model families."""
from sklearn.datasets import load_breast_cancer
from sklearn.ensemble import (HistGradientBoostingClassifier, RandomForestClassifier,
                              StackingClassifier, VotingClassifier)
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import cross_val_score
from sklearn.neighbors import KNeighborsClassifier
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler

if __name__ == "__main__":
    X, y = load_breast_cancer(return_X_y=True)
    base = [
        ("lr", make_pipeline(StandardScaler(), LogisticRegression(max_iter=1000))),
        ("knn", make_pipeline(StandardScaler(), KNeighborsClassifier(n_neighbors=7))),
        ("rf", RandomForestClassifier(n_estimators=200, random_state=0, n_jobs=-1)),
        ("hgb", HistGradientBoostingClassifier(random_state=0)),
    ]
    models = dict(base)
    models["hard voting"] = VotingClassifier(base, voting="hard")
    models["soft voting"] = VotingClassifier(base, voting="soft")
    models["stacking (meta = LR)"] = StackingClassifier(
        base, final_estimator=LogisticRegression(max_iter=1000), cv=5)
    print("10-fold CV accuracy")
    for name, m in models.items():
        s = cross_val_score(m, X, y, cv=10, n_jobs=-1)
        print(f"  {name:22s} {s.mean():.3f} +/- {s.std():.3f}")
