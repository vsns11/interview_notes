"""numpy: arrays, dtypes, indexing, views, broadcasting, vectorisation, aggregation, linear algebra, random."""
import timeit

import numpy as np


def section(title: str) -> None:
    print(f"\n--- {title} ---")


if __name__ == "__main__":
    np.set_printoptions(precision=3, suppress=True)

    section("create")
    a = np.array([1, 2, 3])
    print(a, a.dtype, a.shape, a.ndim)
    print(np.zeros((2, 3)), np.ones(3, dtype=np.int32), np.arange(0, 10, 3), np.linspace(0, 1, 5), sep="\n")
    m = np.arange(12).reshape(3, 4)                     # 3 rows x 4 cols
    print(m)
    print(f"m.shape={m.shape} m.size={m.size} m.dtype={m.dtype} m.nbytes={m.nbytes} itemsize={m.itemsize}")
    print(f"mixed ints and floats -> {np.array([1, 2.5]).dtype}; astype -> {np.array([1.7, -1.7]).astype(int)}")

    section("index and slice")
    print(f"m[1, 2]={m[1, 2]}  m[0]={m[0]}  m[:, 1]={m[:, 1]}  m[-1, ::2]={m[-1, ::2]}")
    view = m[:2, :2]                                    # basic slicing returns a VIEW
    view[0, 0] = 100
    print(f"after view[0,0]=100, m[0,0]={m[0, 0]}  shares memory: {np.shares_memory(view, m)}")
    m[0, 0] = 0
    mask = m % 2 == 0                                   # boolean mask
    print(f"even values: {m[mask]}")
    print(f"fancy rows [2, 0]:\n{m[[2, 0]]}")           # fancy indexing returns a COPY
    print(f"np.where(m > 5, m, -1):\n{np.where(m > 5, m, -1)}")

    section("vectorised maths and aggregation")
    x = np.array([1.0, 2.0, 3.0, 4.0])
    print(f"x*2={x * 2}  x**2={x ** 2}  np.sqrt(x)={np.sqrt(x)}  x>2={x > 2}")
    print(f"m.sum()={m.sum()}  m.sum(axis=0)={m.sum(axis=0)}  m.sum(axis=1)={m.sum(axis=1)}")
    print(f"m.mean()={m.mean():.2f}  m.max(axis=1)={m.max(axis=1)}  m.argmax()={m.argmax()}  "
          f"cumsum={np.arange(1, 5).cumsum()}")

    section("broadcasting")
    col = np.array([[10], [20], [30]])                  # shape (3, 1)
    row = np.array([1, 2, 3, 4])                        # shape (4,)
    print(f"(3,1) + (4,) -> {(col + row).shape}\n{col + row}")
    data = np.array([[1.0, 200.0], [2.0, 300.0], [3.0, 400.0]])
    z = (data - data.mean(axis=0)) / data.std(axis=0)   # standardise each column
    print(f"column-standardised:\n{z}")
    try:
        np.ones((3, 2)) + np.ones(3)
    except ValueError as e:
        print(f"(3,2) + (3,) -> ValueError: {e}")

    section("vectorisation speed")
    big = list(range(1_000_000))
    arr = np.arange(1_000_000)
    t_py = min(timeit.repeat(lambda: [v * 2 + 1 for v in big], number=5, repeat=3)) / 5
    t_np = min(timeit.repeat(lambda: arr * 2 + 1, number=5, repeat=3)) / 5
    print(f"1M elements: list comprehension {t_py*1000:.1f} ms, numpy {t_np*1000:.2f} ms, x{t_py/t_np:.0f}")

    section("linear algebra")
    A = np.array([[3.0, 1.0], [1.0, 2.0]])
    b = np.array([9.0, 8.0])
    print(f"A @ A =\n{A @ A}")
    sol = np.linalg.solve(A, b)                         # solves A x = b
    print(f"solve A x = b -> x = {sol}, check A @ x = {A @ sol}")
    print(f"det = {np.linalg.det(A):.3f}, inv =\n{np.linalg.inv(A)}")
    vals, vecs = np.linalg.eigh(A)                      # eigh: for symmetric matrices
    print(f"eigenvalues = {vals}, norm of b = {np.linalg.norm(b):.3f}, A.T =\n{A.T}")

    section("random")
    rng = np.random.default_rng(seed=42)                # modern Generator API
    print(f"integers: {rng.integers(0, 10, size=5)}")
    print(f"normal:   {rng.normal(loc=0, scale=1, size=3)}")
    print(f"choice:   {rng.choice(['a', 'b', 'c'], size=4)}")
    arr5 = np.arange(5); rng.shuffle(arr5)
    print(f"shuffle:  {arr5}")
    rng2 = np.random.default_rng(seed=42)
    print(f"same seed, same numbers: {rng2.integers(0, 10, size=5)}")
