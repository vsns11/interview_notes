"""Linear algebra for ML with NumPy: vectors, matrices, dot products, matrix multiplication,
transpose, inverse, determinant, norms, eigenvalues and SVD. Saves img/svd_compression.png."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.datasets import load_digits

IMG = Path(__file__).resolve().parent.parent / "img"
np.set_printoptions(precision=3, suppress=True)


def vectors() -> None:
    print("--- vectors ---")
    house = np.array([120.0, 3.0, 2.0])          # size m^2, bedrooms, bathrooms: one data point
    w = np.array([2.0, 10.0, 5.0])               # weights of a linear model
    print("x =", house, "| w =", w)
    print("x + w =", house + w, "| 2 * x =", 2 * house)
    print("dot w.x =", w @ house, "(= 2*120 + 10*3 + 5*2)")
    a, b = np.array([1.0, 0.0]), np.array([1.0, 1.0])
    cos = a @ b / (np.linalg.norm(a) * np.linalg.norm(b))
    print(f"cosine similarity of [1,0] and [1,1] = {cos:.3f} (angle {np.degrees(np.arccos(cos)):.0f} deg)")
    v = np.array([3.0, -4.0])
    print("L2 norm of [3,-4] =", np.linalg.norm(v), "| L1 norm =", np.linalg.norm(v, 1),
          "| max norm =", np.linalg.norm(v, np.inf))
    print("unit vector =", v / np.linalg.norm(v))


def matrices() -> None:
    print("\n--- matrices ---")
    X = np.array([[120.0, 3, 2],               # 4 houses x 3 features: one row per data point
                  [80.0, 2, 1],
                  [200.0, 4, 3],
                  [95.0, 2, 2]])
    w = np.array([2.0, 10.0, 5.0])
    b = 50.0
    print("X shape:", X.shape, "| predictions X @ w + b =", X @ w + b)

    A = np.array([[1, 2], [3, 4]])
    B = np.array([[5, 6], [7, 8]])
    print("A @ B =\n", A @ B)
    print("B @ A =\n", B @ A, "\n(A @ B != B @ A: order matters)")
    print("A * B (element-wise) =\n", A * B)
    print("A.T =\n", A.T)
    print("det(A) =", round(np.linalg.det(A), 3))
    A_inv = np.linalg.inv(A)
    print("inv(A) =\n", A_inv, "\nA @ inv(A) =\n", A @ A_inv)

    # Solve A x = c. Prefer solve() over inv() @ c: faster and more accurate.
    c = np.array([5.0, 11.0])
    print("solve A x = [5, 11] ->", np.linalg.solve(A, c))

    # A layer of a neural network: 3 inputs -> 2 outputs
    W = np.array([[0.2, -0.5, 1.0],
                  [0.7, 0.1, -0.2]])
    x = np.array([1.0, 2.0, 3.0])
    print("layer output W @ x =", W @ x)


def eigen_and_svd() -> None:
    print("\n--- eigenvalues and SVD ---")
    A = np.array([[2.0, 1.0], [1.0, 2.0]])
    vals, vecs = np.linalg.eigh(A)               # eigh: for symmetric matrices, sorted ascending
    print("eigenvalues:", vals)
    print("eigenvectors (columns):\n", vecs)
    v = vecs[:, 1]
    print("A @ v =", A @ v, "= 3 * v =", 3 * v)

    # Covariance eigenvectors = principal directions of data (the heart of PCA)
    rng = np.random.default_rng(0)
    data = rng.multivariate_normal([0, 0], [[3, 2], [2, 2]], size=2000)
    cov = np.cov(data, rowvar=False)
    vals, vecs = np.linalg.eigh(cov)
    print("covariance eigenvalues:", vals, "-> share of variance on main axis:",
          round(vals[-1] / vals.sum(), 3))

    M = np.array([[3.0, 1.0, 1.0], [-1.0, 3.0, 1.0]])
    U, s, Vt = np.linalg.svd(M, full_matrices=False)
    print("singular values:", s)
    print("rebuild U @ diag(s) @ Vt =\n", U @ np.diag(s) @ Vt)


def svd_compression() -> None:
    digits = load_digits()
    img = digits.images[0]                       # 8x8 image of a "0"
    pixels = digits.data                          # 1797 x 64
    U, s, Vt = np.linalg.svd(pixels - pixels.mean(0), full_matrices=False)
    energy = np.cumsum(s**2) / np.sum(s**2)
    for k in (5, 10, 20):
        print(f"top {k:>2} of 64 singular values keep {energy[k-1]:.1%} of the variance")

    x = pixels[0] - pixels.mean(0)
    fig, axes = plt.subplots(1, 4, figsize=(8, 2.4))
    axes[0].imshow(img, cmap="gray_r"); axes[0].set_title("original (64 numbers)", fontsize=9)
    for ax, k in zip(axes[1:], (2, 5, 15)):
        approx = (x @ Vt[:k].T) @ Vt[:k] + pixels.mean(0)   # project onto top-k directions
        ax.imshow(approx.reshape(8, 8), cmap="gray_r"); ax.set_title(f"rank {k} ({k} numbers)", fontsize=9)
    for ax in axes:
        ax.axis("off")
    fig.tight_layout()
    fig.savefig(IMG / "svd_compression.png", dpi=120)
    plt.close(fig)


if __name__ == "__main__":
    vectors()
    matrices()
    eigen_and_svd()
    svd_compression()
