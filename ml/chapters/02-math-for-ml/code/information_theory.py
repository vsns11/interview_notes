"""Entropy, cross-entropy and KL divergence, and how cross-entropy becomes the log loss of a classifier."""
import numpy as np
from sklearn.metrics import log_loss


def entropy(p: np.ndarray) -> float:
    p = p[p > 0]
    return float(-np.sum(p * np.log2(p))) + 0.0   # + 0.0 turns -0.0 into 0.0


def cross_entropy(p: np.ndarray, q: np.ndarray) -> float:
    return float(-np.sum(p * np.log2(q)))


def kl(p: np.ndarray, q: np.ndarray) -> float:
    return float(np.sum(p * np.log2(p / q)))


def main() -> None:
    for name, p in [("fair coin", [0.5, 0.5]), ("biased coin 0.9/0.1", [0.9, 0.1]),
                    ("certain", [1.0, 0.0]), ("fair 8-sided die", [1 / 8] * 8)]:
        print(f"entropy of {name:20s} = {entropy(np.array(p)):.3f} bits")

    p = np.array([0.7, 0.2, 0.1])      # true distribution
    q = np.array([0.5, 0.3, 0.2])      # model's distribution
    H, CE, KL = entropy(p), cross_entropy(p, q), kl(p, q)
    print(f"\nH(p) = {H:.3f}, H(p, q) = {CE:.3f}, KL(p||q) = {KL:.3f}, H(p) + KL = {H + KL:.3f}")
    print(f"KL(q||p) = {kl(q, p):.3f}  (KL is not symmetric)")

    # Log loss = average cross-entropy between one-hot true labels and predicted probabilities (natural log)
    y_true = np.array([1, 0, 1, 1])
    p_pred = np.array([0.9, 0.2, 0.6, 0.99])
    manual = -np.mean(y_true * np.log(p_pred) + (1 - y_true) * np.log(1 - p_pred))
    print(f"\nlog loss by hand = {manual:.4f} | sklearn = {log_loss(y_true, p_pred):.4f}")
    for prob in (0.99, 0.6, 0.1, 0.01):
        print(f"  true class predicted with p={prob:<4}: loss = {-np.log(prob):.3f}")


if __name__ == "__main__":
    main()
