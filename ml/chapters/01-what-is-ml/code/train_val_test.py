"""Training / validation / test split, loss, and a feedback loop (monitoring + retraining on drift)."""
import numpy as np
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import log_loss
from sklearn.model_selection import train_test_split

SEED = 0


def make_data(n: int, shift: float, rng: np.random.Generator):
    """Two features; the decision boundary moves by `shift` to simulate drift over time."""
    X = rng.normal(0, 1, size=(n, 2))
    y = (X[:, 0] + X[:, 1] > shift).astype(int)
    return X, y


def main() -> None:
    rng = np.random.default_rng(SEED)
    X, y = make_data(3000, 0.0, rng)

    # 60 / 20 / 20 split: fit on train, choose settings on validation, report on test once.
    X_tmp, X_test, y_tmp, y_test = train_test_split(X, y, test_size=0.2, random_state=SEED)
    X_train, X_val, y_train, y_val = train_test_split(X_tmp, y_tmp, test_size=0.25, random_state=SEED)
    print("sizes train/val/test:", len(X_train), len(X_val), len(X_test))

    best_c, best_loss = None, np.inf
    for c in [0.001, 0.01, 1.0]:
        m = LogisticRegression(C=c).fit(X_train, y_train)
        loss = log_loss(y_val, m.predict_proba(X_val))
        print(f"C={c:<6} validation log-loss = {loss:.3f}")
        if loss < best_loss:
            best_c, best_loss = c, loss
    model = LogisticRegression(C=best_c).fit(X_tmp, y_tmp)  # refit on train+val with the chosen C
    print(f"chosen C={best_c}; test accuracy = {model.score(X_test, y_test):.3f}")

    # Deployment + monitoring: the world changes (boundary shifts), accuracy drops, we retrain.
    X_new, y_new = make_data(1000, 1.0, rng)
    print(f"after drift, live accuracy = {model.score(X_new, y_new):.3f}  -> alert, retrain")
    model = LogisticRegression(C=best_c).fit(X_new[:500], y_new[:500])
    print(f"after retraining on fresh labelled data, accuracy = {model.score(X_new[500:], y_new[500:]):.3f}")


if __name__ == "__main__":
    main()
