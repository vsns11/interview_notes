"""The scikit-learn API: estimators, fit / predict / transform, learned attributes, Pipeline,
ColumnTransformer for mixed data, cross-validation and grid search on a whole pipeline."""
import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.impute import SimpleImputer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import GridSearchCV, cross_val_score, train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler

SEED = 0


def estimator_basics() -> None:
    print("--- transformer: fit learns, transform applies ---")
    X_train = np.array([[1.0, 100.0], [2.0, 200.0], [3.0, 300.0]])
    X_new = np.array([[4.0, 400.0]])
    scaler = StandardScaler()
    scaler.fit(X_train)                          # learns mean_ and scale_ from TRAINING data
    print("mean_", scaler.mean_, "scale_", scaler.scale_.round(3))
    print("transform(train):\n", scaler.transform(X_train).round(3))
    print("transform(new):", scaler.transform(X_new).round(3), "(uses the training mean/std)")
    print("hyperparameters via get_params():", scaler.get_params())

    print("\n--- predictor: fit, predict, predict_proba, score ---")
    X = np.array([[0.0], [1.0], [2.0], [3.0], [4.0], [5.0]])
    y = np.array([0, 0, 0, 1, 1, 1])
    clf = LogisticRegression().fit(X, y)          # fit returns self, so calls can be chained
    print("predict([[1.0],[4.5]]) =", clf.predict([[1.0], [4.5]]))
    print("predict_proba =", clf.predict_proba([[1.0], [4.5]]).round(3))
    print("score (accuracy) =", clf.score(X, y), "| coef_", clf.coef_.round(3), "intercept_", clf.intercept_.round(3))


def make_customers(n: int = 600) -> pd.DataFrame:
    rng = np.random.default_rng(SEED)
    df = pd.DataFrame({
        "age": rng.integers(18, 70, n).astype(float),
        "monthly_spend": rng.gamma(2.0, 30.0, n).round(2),
        "plan": rng.choice(["basic", "pro", "family"], n, p=[0.5, 0.3, 0.2]),
        "country": rng.choice(["IN", "US", "DE", "BR"], n),
    })
    logit = 0.08 * (40 - df["age"]) + 0.03 * (df["monthly_spend"] - 60) - 1.5 * (df["plan"] == "pro")
    df["churned"] = (rng.random(n) < 1 / (1 + np.exp(-logit))).astype(int)
    df.loc[rng.random(n) < 0.08, "age"] = np.nan              # some missing values
    df.loc[rng.random(n) < 0.05, "plan"] = np.nan
    return df


def pipeline_and_column_transformer() -> None:
    print("\n--- Pipeline + ColumnTransformer on mixed data ---")
    df = make_customers()
    X, y = df.drop(columns="churned"), df["churned"]
    print(X.head(3)); print("missing:", X.isna().sum().to_dict(), "| churn rate:", round(y.mean(), 3))
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.25, stratify=y, random_state=SEED)

    numeric = ["age", "monthly_spend"]
    categorical = ["plan", "country"]
    preprocess = ColumnTransformer([
        ("num", Pipeline([("impute", SimpleImputer(strategy="median")),
                          ("scale", StandardScaler())]), numeric),
        ("cat", Pipeline([("impute", SimpleImputer(strategy="most_frequent")),
                          ("onehot", OneHotEncoder(handle_unknown="ignore"))]), categorical),
    ])
    model = Pipeline([("prep", preprocess), ("clf", LogisticRegression(max_iter=1000))])

    model.fit(X_train, y_train)                  # every step is fit on training data only
    print("output features:", list(model.named_steps["prep"].get_feature_names_out()))
    print(f"test accuracy: {model.score(X_test, y_test):.3f} | baseline (always 'no churn'): {1 - y_test.mean():.3f}")
    print("CV accuracy (5 folds):", cross_val_score(model, X_train, y_train, cv=5).mean().round(3))

    unseen = pd.DataFrame({"age": [30.0], "monthly_spend": [150.0], "plan": ["enterprise"], "country": ["JP"]})
    print("P(churn) for a customer with unseen categories:", model.predict_proba(unseen)[0, 1].round(3))

    grid = GridSearchCV(model, {
        "prep__num__impute__strategy": ["mean", "median"],
        "clf": [LogisticRegression(max_iter=1000), RandomForestClassifier(n_estimators=100, random_state=SEED)],
    }, cv=5)
    grid.fit(X_train, y_train)
    print("best:", {k: (type(v).__name__ if k == "clf" else v) for k, v in grid.best_params_.items()},
          "| CV", round(grid.best_score_, 3), "| test", round(grid.score(X_test, y_test), 3))


if __name__ == "__main__":
    estimator_basics()
    pipeline_and_column_transformer()
