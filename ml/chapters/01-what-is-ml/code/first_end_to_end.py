"""A first end-to-end ML project with scikit-learn (breast-cancer classification).

Steps: frame -> load -> split -> baseline -> pipeline -> cross-validate -> tune -> test once -> save -> predict.
"""
from pathlib import Path
import tempfile

import joblib
import numpy as np
from sklearn.datasets import load_breast_cancer
from sklearn.dummy import DummyClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix
from sklearn.model_selection import GridSearchCV, cross_val_score, train_test_split
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler

SEED = 42


def main() -> None:
    # 1. Data: 569 tumours, 30 numeric features each, label 0 = malignant, 1 = benign
    data = load_breast_cancer(as_frame=True)
    X, y = data.data, data.target
    print("shape:", X.shape, "| class counts:", np.bincount(y).tolist())

    # 2. Hold out a test set FIRST (stratify keeps the class ratio the same in both parts)
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, stratify=y, random_state=SEED)
    print("train:", X_train.shape[0], "test:", X_test.shape[0])

    # 3. Baseline: always predict the most common class
    base = DummyClassifier(strategy="most_frequent").fit(X_train, y_train)
    print(f"baseline accuracy (test): {base.score(X_test, y_test):.3f}")

    # 4. Model = scaling + logistic regression, bundled so scaling is learned on training folds only
    pipe = make_pipeline(StandardScaler(), LogisticRegression(max_iter=1000))
    cv = cross_val_score(pipe, X_train, y_train, cv=5)
    print(f"5-fold CV accuracy: {cv.mean():.3f} +/- {cv.std():.3f}")

    # 5. Tune one hyperparameter (C = inverse regularisation strength) with CV on the training set
    grid = GridSearchCV(pipe, {"logisticregression__C": [0.01, 0.1, 1, 10]}, cv=5)
    grid.fit(X_train, y_train)
    print("best C:", grid.best_params_["logisticregression__C"],
          f"| best CV accuracy: {grid.best_score_:.3f}")

    # 6. Evaluate ONCE on the untouched test set
    y_pred = grid.predict(X_test)
    print(f"test accuracy: {accuracy_score(y_test, y_pred):.3f}")
    print("confusion matrix [rows=true, cols=pred]:\n", confusion_matrix(y_test, y_pred))
    print(classification_report(y_test, y_pred, target_names=data.target_names, digits=3))

    # 7. Save the whole pipeline (scaler + model) and reload it, as a serving process would
    path = Path(tempfile.gettempdir()) / "cancer_model.joblib"
    joblib.dump(grid.best_estimator_, path)
    served = joblib.load(path)
    new_patient = X_test.iloc[[0]]
    print("prediction for a new sample:", data.target_names[served.predict(new_patient)[0]],
          "| P(benign) =", round(served.predict_proba(new_patient)[0, 1], 3))

    # 8. Parameters (learned) vs hyperparameters (chosen by us)
    lr = grid.best_estimator_[-1]
    print("learned parameters: coef_ shape", lr.coef_.shape, "intercept", lr.intercept_.round(3))


if __name__ == "__main__":
    main()
