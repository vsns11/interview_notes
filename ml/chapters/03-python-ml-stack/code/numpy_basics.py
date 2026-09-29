"""NumPy essentials for ML: arrays, dtypes, shapes, indexing, axis operations, broadcasting,
vectorisation vs loops, and random numbers."""
import timeit

import numpy as np


def arrays_and_dtypes() -> None:
    print("--- arrays and dtypes ---")
    a = np.array([1, 2, 3])
    b = np.array([1.0, 2.0, 3.0])
    print(a.dtype, b.dtype, np.array([1, 2.5]).dtype, "(mixing int and float upcasts to float)")
    M = np.arange(12).reshape(3, 4)
    print("M =\n", M)
    print("ndim", M.ndim, "| shape", M.shape, "| size", M.size, "| bytes", M.nbytes)
    print("float64 vs float32 memory for 1M values:",
          np.zeros(1_000_000).nbytes // 1_000_000, "MB vs", np.zeros(1_000_000, dtype=np.float32).nbytes // 1_000_000, "MB")
    print("zeros", np.zeros(3), "| ones", np.ones(3), "| linspace", np.linspace(0, 1, 5), "| eye\n", np.eye(2))
    print("int8 overflow wraps around:", np.array([127], dtype=np.int8) + np.int8(1))
    print("0.1 + 0.2 == 0.3?", 0.1 + 0.2 == 0.3, "| np.isclose:", np.isclose(0.1 + 0.2, 0.3))
    print("reshape(-1) flattens:", M.reshape(-1)[:6], "... | M.T shape", M.T.shape)
    col = np.array([1, 2, 3])
    print("1-D shape", col.shape, "-> column vector", col.reshape(-1, 1).shape, "or", col[:, None].shape)


def indexing() -> None:
    print("\n--- indexing ---")
    M = np.arange(12).reshape(3, 4)
    print("M[1, 2] =", M[1, 2], "| row M[0] =", M[0], "| column M[:, 1] =", M[:, 1])
    print("M[:2, 1:3] =\n", M[:2, 1:3])
    print("boolean mask M[M % 2 == 0] =", M[M % 2 == 0])
    print("fancy index rows [2, 0] =\n", M[[2, 0]])
    print("np.where(M > 5, 1, 0) =\n", np.where(M > 5, 1, 0))

    view = M[0]              # slicing returns a VIEW: same memory
    view[0] = 99
    print("after changing the slice, M[0, 0] =", M[0, 0], "(views share memory)")
    safe = M[1].copy()       # .copy() gives independent data
    safe[0] = -1
    print("after changing a copy, M[1, 0] =", M[1, 0])


def axis_ops() -> None:
    print("\n--- axis operations ---")
    scores = np.array([[80, 90, 70],       # rows = students, columns = exams
                       [60, 75, 85]])
    print("sum all:", scores.sum(), "| axis=0 (down rows, per exam):", scores.sum(axis=0),
          "| axis=1 (across columns, per student):", scores.mean(axis=1))
    print("max per exam:", scores.max(axis=0), "| argmax per student:", scores.argmax(axis=1))
    print("keepdims shape:", scores.mean(axis=1, keepdims=True).shape)
    print("concatenate rows:", np.concatenate([scores, scores]).shape,
          "| hstack:", np.hstack([scores, scores]).shape, "| stack:", np.stack([scores, scores]).shape)


def broadcasting() -> None:
    print("\n--- broadcasting ---")
    X = np.array([[1.0, 200.0],
                  [2.0, 300.0],
                  [3.0, 400.0]])            # shape (3, 2)
    mu = X.mean(axis=0)                      # shape (2,)
    sd = X.std(axis=0)
    print("mean", mu, "std", sd.round(3))
    print("standardised (X - mu) / sd =\n", ((X - mu) / sd).round(3))
    print("(3,1) + (1,4) ->\n", np.arange(3).reshape(3, 1) + np.arange(4).reshape(1, 4))
    try:
        X + np.array([1.0, 2.0, 3.0])        # (3,2) + (3,) : trailing dims 2 vs 3 clash
    except ValueError as e:
        print("shape error:", e)
    # Pairwise squared distances between 4 points and 2 centres, no loops: (4,1,2) - (1,2,2) -> (4,2,2)
    pts = np.array([[0, 0], [1, 1], [5, 5], [6, 5]], dtype=float)
    centres = np.array([[0, 0], [5, 5]], dtype=float)
    d2 = ((pts[:, None, :] - centres[None, :, :]) ** 2).sum(axis=2)
    print("squared distances (points x centres):\n", d2, "\nnearest centre:", d2.argmin(axis=1))


def vectorisation() -> None:
    print("\n--- vectorisation vs Python loops ---")
    x = np.random.default_rng(0).random(1_000_000)

    def loop():
        total = 0.0
        for v in x:
            total += v * v
        return total

    def vec():
        return float(x @ x)

    t_loop = min(timeit.repeat(loop, number=1, repeat=3))
    t_vec = min(timeit.repeat(vec, number=1, repeat=3))
    print(f"sum of squares of 1M floats: loop {t_loop*1000:.0f} ms, vectorised {t_vec*1000:.2f} ms "
          f"-> ~{t_loop / t_vec:.0f}x faster (varies by machine)")
    print("same answer:", np.isclose(loop(), vec()))


def random_numbers() -> None:
    print("\n--- random numbers ---")
    rng = np.random.default_rng(42)          # modern Generator API, seeded for reproducibility
    print("uniform", rng.random(3).round(3))
    print("normal", rng.normal(0, 1, 3).round(3))
    print("integers", rng.integers(0, 10, 5))
    print("choice", rng.choice(["a", "b", "c"], size=4))
    idx = rng.permutation(10)
    print("shuffled indices for a train/test split:", idx, "-> train", idx[:8], "test", idx[8:])
    print("same seed, same numbers:", np.random.default_rng(42).random(3).round(3))


if __name__ == "__main__":
    arrays_and_dtypes()
    indexing()
    axis_ops()
    broadcasting()
    vectorisation()
    random_numbers()
