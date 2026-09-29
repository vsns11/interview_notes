"""One tiny demo per learning type: supervised (classification + regression), unsupervised,
semi-supervised, self-supervised and reinforcement learning."""
import numpy as np
from sklearn.cluster import KMeans
from sklearn.datasets import load_diabetes, load_digits, load_iris, make_blobs
from sklearn.linear_model import LinearRegression, LogisticRegression
from sklearn.metrics import adjusted_rand_score, mean_absolute_error
from sklearn.model_selection import train_test_split
from sklearn.semi_supervised import SelfTrainingClassifier

SEED = 0


def supervised() -> None:
    X, y = load_iris(return_X_y=True)                  # labels = species (a category)
    Xtr, Xte, ytr, yte = train_test_split(X, y, random_state=SEED, stratify=y)
    clf = LogisticRegression(max_iter=1000).fit(Xtr, ytr)
    print(f"[classification] iris accuracy = {clf.score(Xte, yte):.3f}")

    X, y = load_diabetes(return_X_y=True)              # label = disease progression (a number)
    Xtr, Xte, ytr, yte = train_test_split(X, y, random_state=SEED)
    reg = LinearRegression().fit(Xtr, ytr)
    print(f"[regression]     diabetes MAE = {mean_absolute_error(yte, reg.predict(Xte)):.1f}"
          f" (target std = {y.std():.1f})")


def unsupervised() -> None:
    X, true_groups = make_blobs(n_samples=300, centers=3, random_state=SEED)
    labels = KMeans(n_clusters=3, n_init=10, random_state=SEED).fit_predict(X)  # never sees true_groups
    print(f"[unsupervised]   k-means agreement with hidden groups (ARI) = "
          f"{adjusted_rand_score(true_groups, labels):.3f}")


def semi_supervised() -> None:
    X, y = load_digits(return_X_y=True)
    Xtr, Xte, ytr, yte = train_test_split(X, y, random_state=SEED, stratify=y)
    rng = np.random.default_rng(SEED)
    y_partial = ytr.copy()
    hidden = rng.random(len(ytr)) > 0.05               # hide 95% of the labels
    y_partial[hidden] = -1                             # -1 means "unlabelled" in sklearn
    few = LogisticRegression(max_iter=2000).fit(Xtr[~hidden], ytr[~hidden])
    self_train = SelfTrainingClassifier(LogisticRegression(max_iter=2000), threshold=0.9).fit(Xtr, y_partial)
    print(f"[semi-supervised] {(~hidden).sum()} labels only: {few.score(Xte, yte):.3f}"
          f" | + self-training on unlabelled rows: {self_train.score(Xte, yte):.3f}")


def self_supervised() -> None:
    # The label is made from the data itself: predict the next value of a series from the previous 3.
    t = np.arange(400)
    series = np.sin(t / 10) + 0.05 * np.random.default_rng(SEED).standard_normal(400)
    X = np.stack([series[i:i + 3] for i in range(len(series) - 3)])
    y = series[3:]                                     # "labels" = the next value, no human needed
    model = LinearRegression().fit(X[:300], y[:300])
    print(f"[self-supervised] next-value MAE = {mean_absolute_error(y[300:], model.predict(X[300:])):.3f}")


def reinforcement() -> None:
    # 3-armed bandit: learn by trial and reward which arm pays best (epsilon-greedy).
    rng = np.random.default_rng(SEED)
    true_payout = np.array([0.2, 0.5, 0.8])
    estimates, counts, total = np.zeros(3), np.zeros(3), 0.0
    for step in range(2000):
        arm = rng.integers(3) if rng.random() < 0.1 else int(np.argmax(estimates))  # explore vs exploit
        reward = float(rng.random() < true_payout[arm])
        counts[arm] += 1
        estimates[arm] += (reward - estimates[arm]) / counts[arm]                 # running average
        total += reward
    print(f"[reinforcement]  estimated payouts {estimates.round(2).tolist()}, pulls {counts.astype(int).tolist()},"
          f" avg reward {total / 2000:.3f}")


if __name__ == "__main__":
    supervised()
    unsupervised()
    semi_supervised()
    self_supervised()
    reinforcement()
