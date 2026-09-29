"""One-hot vs dense embeddings, cosine similarity, and the lookup trick."""
import numpy as np

words = ["king", "queen", "man", "woman", "apple"]


def cosine(a: np.ndarray, b: np.ndarray) -> float:
    return float(a @ b / (np.linalg.norm(a) * np.linalg.norm(b)))


def table(vectors: np.ndarray, title: str) -> None:
    print(title)
    print("        " + "".join(f"{w:>8}" for w in words))
    for i, w in enumerate(words):
        row = "".join(f"{cosine(vectors[i], vectors[j]):8.2f}" for j in range(len(words)))
        print(f"{w:>8}{row}")
    print()


if __name__ == "__main__":
    # 1) One-hot: each word is a unit vector. Every pair is orthogonal.
    one_hot = np.eye(len(words))
    table(one_hot, "One-hot cosine similarity (no notion of meaning):")

    # 2) Hand-made dense vectors. Dimensions: [royalty, is_person, maleness, is_food]
    dense = np.array([
        [0.9, 1.0, 0.7, 0.0],   # king
        [0.9, 1.0, -0.7, 0.0],  # queen
        [0.1, 1.0, 0.7, 0.0],   # man
        [0.1, 1.0, -0.7, 0.0],  # woman
        [0.0, 0.0, 0.0, 1.0],   # apple
    ])
    table(dense, "Dense cosine similarity (meaning lives in the geometry):")

    # 3) Vector arithmetic: king - man + woman lands next to queen.
    target = dense[0] - dense[2] + dense[3]
    scores = {w: cosine(target, dense[i]) for i, w in enumerate(words)}
    best = max((w for w in words if w not in {"king", "man", "woman"}), key=scores.get)
    print("king - man + woman =", np.round(target, 2), "-> closest:", best)

    # 4) An embedding layer is a lookup table: one_hot @ E == E[row]
    rng = np.random.default_rng(0)
    E = rng.normal(size=(len(words), 4)).round(2)   # vocab_size x d_model
    idx = words.index("queen")
    print("\nE[queen]            =", E[idx])
    print("one_hot(queen) @ E  =", one_hot[idx] @ E)
