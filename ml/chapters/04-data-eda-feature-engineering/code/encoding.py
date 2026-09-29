"""Encoding categoricals: one-hot, ordinal, hashing, and target encoding (naive = leaky vs cross-fitted)."""
import numpy as np
import pandas as pd
from sklearn.feature_extraction import FeatureHasher
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import roc_auc_score
from sklearn.model_selection import KFold, train_test_split
from sklearn.preprocessing import OneHotEncoder, OrdinalEncoder, TargetEncoder

from make_data import make_churn


def main() -> None:
    df = make_churn().drop_duplicates().reset_index(drop=True)

    # One-hot: one 0/1 column per category; unknown categories at predict time -> all zeros
    ohe = OneHotEncoder(handle_unknown="ignore", sparse_output=False)
    ohe.fit(df[["device"]].fillna("missing"))
    print("one-hot columns:", list(ohe.get_feature_names_out()))
    print("encode ['ios'], ['tablet'(unseen)]:\n", ohe.transform(pd.DataFrame({"device": ["ios", "tablet"]})))

    # Ordinal: categories with a real order -> 0,1,2 in THAT order (not alphabetical)
    ord_enc = OrdinalEncoder(categories=[["basic", "standard", "premium"]])
    print("\nordinal plan:", ord_enc.fit_transform(pd.DataFrame({"plan": ["premium", "basic", "standard"]})).ravel())

    # Hashing: fixed number of columns no matter how many distinct values appear
    fh = FeatureHasher(n_features=16, input_type="string")
    hashed = fh.transform([[c] for c in df["city"].head(3)]).toarray()
    print("\nhashed city (16 buckets), row 0 non-zeros at:", np.flatnonzero(hashed[0]), hashed[0][hashed[0] != 0])

    # ---------- Target encoding and leakage -------------------------------------
    # A pure-noise ID-like column with 1000 levels (about 2-3 rows per level) + real features.
    rng = np.random.default_rng(0)
    df["noise_id"] = rng.integers(0, 1000, len(df)).astype(str)
    y = df["churn"].to_numpy()
    tr, te = train_test_split(np.arange(len(df)), test_size=0.3, random_state=0, stratify=y)

    def fit_eval(train_feat, test_feat, label):
        m = LogisticRegression(max_iter=1000).fit(train_feat, y[tr])
        auc_tr = roc_auc_score(y[tr], m.predict_proba(train_feat)[:, 1])
        auc_te = roc_auc_score(y[te], m.predict_proba(test_feat)[:, 1])
        print(f"{label:<38} train AUC {auc_tr:.3f} | test AUC {auc_te:.3f}")

    print("\nFeature = target-encoded 'noise_id' (contains NO real signal):")
    # Naive: category mean of y computed on the same rows the model trains on
    means = pd.Series(y[tr]).groupby(df["noise_id"].iloc[tr].to_numpy()).mean()
    prior = y[tr].mean()
    naive_tr = df["noise_id"].iloc[tr].map(means).to_numpy()[:, None]
    naive_te = df["noise_id"].iloc[te].map(means).fillna(prior).to_numpy()[:, None]
    fit_eval(naive_tr, naive_te, "naive target mean (leaky)")

    # Proper: sklearn TargetEncoder uses cross-fitting inside fit_transform + shrinkage
    te_enc = TargetEncoder(target_type="binary", cv=KFold(5, shuffle=True, random_state=0))
    cf_tr = te_enc.fit_transform(df[["noise_id"]].iloc[tr], y[tr])
    cf_te = te_enc.transform(df[["noise_id"]].iloc[te])
    fit_eval(cf_tr, cf_te, "TargetEncoder (cross-fitted, smoothed)")

    print("\nFeature = target-encoded 'city' (60 levels, real signal):")
    te_city = TargetEncoder(target_type="binary", cv=KFold(5, shuffle=True, random_state=0))
    fit_eval(te_city.fit_transform(df[["city"]].iloc[tr], y[tr]),
             te_city.transform(df[["city"]].iloc[te]), "TargetEncoder(city)")
    oh = OneHotEncoder(handle_unknown="ignore").fit(df[["city"]].iloc[tr])
    fit_eval(oh.transform(df[["city"]].iloc[tr]), oh.transform(df[["city"]].iloc[te]), "OneHot(city)")


if __name__ == "__main__":
    main()
