"""End-to-end: raw messy DataFrame -> ColumnTransformer -> model, tuned with CV, evaluated once on a test set."""
import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.dummy import DummyClassifier
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.impute import SimpleImputer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import average_precision_score, roc_auc_score
from sklearn.model_selection import GridSearchCV, KFold, StratifiedKFold, train_test_split
from sklearn.pipeline import Pipeline, make_pipeline
from sklearn.preprocessing import (FunctionTransformer, OneHotEncoder, OrdinalEncoder, StandardScaler,
                                   TargetEncoder)

from make_data import make_churn

REF_DATE = pd.Timestamp("2026-01-01")


def fix_impossible(X: pd.DataFrame) -> pd.DataFrame:
    """Row-wise rule (no learning): impossible values become missing."""
    X = X.copy()
    X["age"] = X["age"].where(X["age"] <= 110)
    X["income"] = X["income"].where(X["income"] < 9_999_999)
    X["income"] = np.log1p(X["income"])            # tame the long tail
    return X


def date_parts(X: pd.DataFrame) -> pd.DataFrame:
    d = pd.to_datetime(X["signup_date"])
    return pd.DataFrame({"signup_age_days": (REF_DATE - d).dt.days,
                         "signup_month_sin": np.sin(2 * np.pi * d.dt.month / 12),
                         "signup_month_cos": np.cos(2 * np.pi * d.dt.month / 12)})


def date_names(transformer, input_features):
    return np.array(["signup_age_days", "signup_month_sin", "signup_month_cos"])


def build_pipeline() -> Pipeline:
    numeric = ["age", "income", "tenure_months", "support_tickets", "monthly_charges", "usage_hours"]
    numeric_pipe = make_pipeline(
        FunctionTransformer(fix_impossible, feature_names_out="one-to-one"),
        SimpleImputer(strategy="median", add_indicator=True),
        StandardScaler())
    pre = ColumnTransformer([
        ("num", numeric_pipe, numeric),
        ("plan", OrdinalEncoder(categories=[["basic", "standard", "premium"]]), ["plan"]),
        ("device", make_pipeline(SimpleImputer(strategy="constant", fill_value="missing"),
                                 OneHotEncoder(handle_unknown="ignore")), ["device"]),
        ("city", TargetEncoder(target_type="binary", cv=KFold(5, shuffle=True, random_state=0)), ["city"]),
        ("date", make_pipeline(FunctionTransformer(date_parts, feature_names_out=date_names),
                               StandardScaler()), ["signup_date"]),
        ("text", TfidfVectorizer(min_df=2), "review"),          # a string, not a list: 1-D text column
    ])                                                            # remainder="drop" drops customer_id
    return Pipeline([("pre", pre), ("model", LogisticRegression(max_iter=2000, class_weight="balanced"))])


def main() -> None:
    df = make_churn().drop_duplicates().reset_index(drop=True)
    X, y = df.drop(columns="churn"), df["churn"]
    X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.2, stratify=y, random_state=42)

    pipe = build_pipeline()
    search = GridSearchCV(pipe, {"model__C": [0.01, 0.1, 1.0, 10.0, 100.0]},
                          cv=StratifiedKFold(5, shuffle=True, random_state=0), scoring="average_precision")
    search.fit(X_tr, y_tr)                       # every fold re-fits imputer/scaler/encoders on its own train part
    print("best C:", search.best_params_["model__C"], "| CV PR-AUC: %.3f" % search.best_score_)

    best = search.best_estimator_
    n_feat = best.named_steps["pre"].transform(X_tr.head(5)).shape[1]
    print("features after ColumnTransformer:", n_feat)
    names = best.named_steps["pre"].get_feature_names_out()
    coefs = pd.Series(best.named_steps["model"].coef_[0], index=names).sort_values()
    print("most negative coefs:\n", coefs.head(3).round(2).to_string())
    print("most positive coefs:\n", coefs.tail(3).round(2).to_string())
    print("scaled numeric coefs:", coefs[[n for n in names if n.startswith("num__")
                                           and "missing" not in n]].round(2).to_dict())

    p = best.predict_proba(X_te)[:, 1]
    base = DummyClassifier(strategy="prior").fit(X_tr, y_tr).predict_proba(X_te)[:, 1]
    print(f"\nTEST  model   : ROC-AUC {roc_auc_score(y_te, p):.3f} | PR-AUC {average_precision_score(y_te, p):.3f}")
    print(f"TEST  baseline: ROC-AUC {roc_auc_score(y_te, base):.3f} | PR-AUC {average_precision_score(y_te, base):.3f}"
          f"  (PR-AUC of a no-skill model = positive rate {y_te.mean():.3f})")

    # One object to ship: the same transformations run at serving time on a raw row
    new = X_te.head(1).copy()
    new["city"] = "city_never_seen"
    new["device"] = np.nan
    print("\nraw new row with unseen city + missing device -> p(churn) = %.3f" % best.predict_proba(new)[0, 1])


if __name__ == "__main__":
    main()
