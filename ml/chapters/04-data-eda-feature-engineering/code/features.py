"""Feature creation: interactions/polynomials, binning, log transform, date parts, cyclical encoding, TF-IDF."""
import numpy as np
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LinearRegression, LogisticRegression
from sklearn.model_selection import cross_val_score
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import KBinsDiscretizer, PolynomialFeatures, StandardScaler

from make_data import make_churn


def main() -> None:
    rng = np.random.default_rng(0)

    # 1) Interaction: target depends on a PRODUCT of two features (price x quantity)
    n = 500
    price, qty = rng.uniform(1, 10, n), rng.uniform(1, 10, n)
    revenue = price * qty + rng.normal(0, 1, n)
    X = np.c_[price, qty]
    lin = cross_val_score(LinearRegression(), X, revenue, cv=5, scoring="r2").mean()
    inter = cross_val_score(make_pipeline(PolynomialFeatures(2, interaction_only=True, include_bias=False),
                                          LinearRegression()), X, revenue, cv=5, scoring="r2").mean()
    print(f"revenue ~ price, qty          R^2 = {lin:.3f}")
    print(f"revenue ~ price, qty, price*qty R^2 = {inter:.3f}")
    print("PolynomialFeatures(2) names:", PolynomialFeatures(2, include_bias=False)
          .fit(X).get_feature_names_out(["price", "qty"]))

    # 2) Binning: a U-shaped effect a linear model cannot draw with one slope
    age = rng.uniform(18, 80, 2000)
    p = 1 / (1 + np.exp(-(0.004 * (age - 50) ** 2 - 2)))    # risk high for young and old
    y = (rng.random(2000) < p).astype(int)
    raw = cross_val_score(LogisticRegression(), age[:, None], y, cv=5, scoring="roc_auc").mean()
    binned = cross_val_score(make_pipeline(KBinsDiscretizer(n_bins=6, encode="onehot", strategy="quantile",
                                                            quantile_method="averaged_inverted_cdf"),
                                           LogisticRegression()), age[:, None], y, cv=5, scoring="roc_auc").mean()
    print(f"\nU-shaped age effect: raw age AUC = {raw:.3f} | 6 quantile bins AUC = {binned:.3f}")

    # 3) Date features from a timestamp
    df = make_churn().drop_duplicates().head(4)
    ts = df["signup_date"]
    dates = pd.DataFrame({
        "signup_date": ts.dt.date, "year": ts.dt.year, "month": ts.dt.month,
        "dayofweek": ts.dt.dayofweek, "is_weekend": (ts.dt.dayofweek >= 5).astype(int),
        "days_since": (pd.Timestamp("2026-01-01") - ts).dt.days,
        "month_sin": np.sin(2 * np.pi * ts.dt.month / 12).round(3) + 0.0,
        "month_cos": np.cos(2 * np.pi * ts.dt.month / 12).round(3) + 0.0,
    })
    print("\n", dates.to_string(index=False))

    # 4) TF-IDF on short review texts
    docs = ["app is slow and expensive", "love the app, fast and easy",
            "slow slow slow, will cancel", "easy to use and helpful support"]
    vec = TfidfVectorizer()
    M = vec.fit_transform(docs)
    vocab = vec.get_feature_names_out()
    print("\nvocabulary:", list(vocab))
    row = M[2].toarray().ravel()
    print("doc 2 non-zero weights:", {str(vocab[i]): round(float(row[i]), 3) for i in np.flatnonzero(row)})
    print("idf('slow') = %.3f, idf('and') = %.3f" % (vec.idf_[vec.vocabulary_["slow"]],
                                                    vec.idf_[vec.vocabulary_["and"]]))

    # TF-IDF as a feature on the churn data
    full = make_churn().drop_duplicates()
    auc = cross_val_score(make_pipeline(TfidfVectorizer(), LogisticRegression(max_iter=1000)),
                          full["review"], full["churn"], cv=5, scoring="roc_auc").mean()
    print(f"churn ~ TF-IDF(review) alone: AUC = {auc:.3f}")


if __name__ == "__main__":
    main()
