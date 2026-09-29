"""list vs array.array vs numpy.ndarray: memory and 'multiply everything by 2' speed."""
import array
import sys
import timeit

import numpy as np

N = 1_000_000


def main() -> None:
    lst = list(range(N))
    arr = array.array("q", range(N))          # 'q' = signed 8-byte int
    nd = np.arange(N, dtype=np.int64)

    list_bytes = sys.getsizeof(lst) + sum(sys.getsizeof(x) for x in lst)
    print(f"{'container':<14}{'memory':>10}{'x*2 time':>12}")
    t_list = min(timeit.repeat(lambda: [x * 2 for x in lst], number=5, repeat=3)) / 5
    t_arr = min(timeit.repeat(lambda: array.array("q", (x * 2 for x in arr)), number=5, repeat=3)) / 5
    t_np = min(timeit.repeat(lambda: nd * 2, number=5, repeat=3)) / 5
    print(f"{'list':<14}{list_bytes / 1e6:>8.1f}MB{t_list * 1000:>10.1f}ms")
    print(f"{'array.array':<14}{sys.getsizeof(arr) / 1e6:>8.1f}MB{t_arr * 1000:>10.1f}ms")
    print(f"{'numpy':<14}{nd.nbytes / 1e6:>8.1f}MB{t_np * 1000:>10.1f}ms")
    print("same result:", [x * 2 for x in lst[:5]], (nd * 2)[:5].tolist())


if __name__ == "__main__":
    main()
