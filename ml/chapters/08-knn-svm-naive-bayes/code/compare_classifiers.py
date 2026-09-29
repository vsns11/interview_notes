"""Same datasets, every classic supervised classifier, 5-fold CV accuracy."""
import os
import warnings

os.environ.setdefault("OMP_NUM_THREADS", "2")  # avoid thread oversubscription on busy machines

import numpy as np
from sklearn.datasets import load_breast_cancer, load_digits, load_wine, make_moons
from sklearn.ensemble import HistGradientBoostingClassifier, RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import cross_val_score
from sklearn.naive_bayes import GaussianNB
from sklearn.neighbors import KNeighborsClassifier
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.svm import SVC
from sklearn.tree import DecisionTreeClassifier

warnings.filterwarnings("ignore")

if __name__ == "__main__":
    Xm, ym = make_moons(n_samples=500, noise=0.3, random_state=0)
    datasets = {
        "breast_cancer": load_breast_cancer(return_X_y=True),
        "wine": load_wine(return_X_y=True),
        "digits": load_digits(return_X_y=True),
        "moons (nonlinear)": (Xm, ym),
    }
    models = {
        "LogisticRegression": make_pipeline(StandardScaler(), LogisticRegression(max_iter=2000)),
        "k-NN (k=5)": make_pipeline(StandardScaler(), KNeighborsClassifier()),
        "SVM (RBF)": make_pipeline(StandardScaler(), SVC()),
        "GaussianNB": GaussianNB(),
        "DecisionTree": DecisionTreeClassifier(random_state=0),
        "RandomForest": RandomForestClassifier(random_state=0),
        "HistGradBoosting": HistGradientBoostingClassifier(random_state=0),
    }
    print(f"{'model':20s}" + "".join(f"{d:>19s}" for d in datasets))
    for name, m in models.items():
        row = [cross_val_score(m, X, y, cv=5).mean() for X, y in datasets.values()]
        print(f"{name:20s}" + "".join(f"{s:19.3f}" for s in row))
