"""A complete small supervised-learning workflow: predict iris petal length from the other measurements.

Load -> inspect -> encode the categorical column -> split -> fit linear regression and a decision tree -> evaluate.
"""
import numpy as np
import pandas as pd
from sklearn.datasets import load_iris
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_squared_error
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeRegressor


def main() -> None:
    iris = load_iris(as_frame=True)                          # bundled with sklearn: no download
    df = iris.frame.rename(columns=lambda c: c.replace(" (cm)", "").replace(" ", "_"))
    df["species"] = iris.target_names[df.pop("target")]
    print(df.head(3).to_string(), "\n")
    print("missing values (total):", df.isna().sum().sum())

    X = pd.get_dummies(df.drop(columns="petal_length"), columns=["species"], dtype=int)   # one-hot species
    y = df["petal_length"]
    print("model inputs:", list(X.columns))
    X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.2, random_state=42)       # 80/20

    lin = LinearRegression().fit(X_tr, y_tr)
    print("\ncoefficients:", {c: round(float(w), 3) for c, w in zip(X.columns, lin.coef_)})
    print("intercept:", round(float(lin.intercept_), 3))

    print(f"\ny range: {y.min()} to {y.max()} cm, std {y.std():.2f}")
    print(f"{'model':<28}{'train MSE':>10}{'test MSE':>10}")
    baseline = np.full(len(y_te), y_tr.mean())
    print(f"{'baseline (predict mean)':<28}{'':>10}{mean_squared_error(y_te, baseline):>10.3f}")
    for name, m in [("linear regression", lin),
                    ("tree, no depth limit", DecisionTreeRegressor(random_state=0)),
                    ("tree, max_depth=3", DecisionTreeRegressor(max_depth=3, random_state=0))]:
        m.fit(X_tr, y_tr)
        print(f"{name:<28}{mean_squared_error(y_tr, m.predict(X_tr)):>10.3f}{mean_squared_error(y_te, m.predict(X_te)):>10.3f}")


if __name__ == "__main__":
    main()
