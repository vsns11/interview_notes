"""concurrent.futures: ThreadPoolExecutor / ProcessPoolExecutor, submit, map, as_completed, errors."""
import math
import time
from concurrent.futures import (ProcessPoolExecutor, ThreadPoolExecutor, as_completed, wait,
                                FIRST_COMPLETED)


def fake_download(url: str) -> tuple[str, int]:
    delay = {"a.com": 0.3, "b.com": 0.1, "c.com": 0.2}[url]
    time.sleep(delay)                     # pretend network wait
    if url == "c.com":
        raise ConnectionError(f"{url} timed out")
    return url, int(delay * 1000)


def is_prime(n: int) -> bool:
    if n < 2:
        return False
    for d in range(2, math.isqrt(n) + 1):
        if n % d == 0:
            return False
    return True


if __name__ == "__main__":
    urls = ["a.com", "b.com", "c.com"]

    # submit + as_completed: results arrive in COMPLETION order
    with ThreadPoolExecutor(max_workers=3) as ex:
        futures = {ex.submit(fake_download, u): u for u in urls}
        for fut in as_completed(futures):
            try:
                print(f"done {futures[fut]} -> {fut.result()}")
            except ConnectionError as e:
                print(f"failed {futures[fut]} -> {e!r}")

    # map: results in INPUT order; an exception is re-raised when you reach that item
    with ThreadPoolExecutor(max_workers=3) as ex:
        it = ex.map(fake_download, urls)
        try:
            for r in it:
                print(f"map result {r}")
        except ConnectionError as e:
            print(f"map raised {e!r}")

    # wait(FIRST_COMPLETED) + cancel the rest (only not-yet-started futures can be cancelled)
    with ThreadPoolExecutor(max_workers=1) as ex:
        fs = [ex.submit(time.sleep, 0.2) for _ in range(3)]
        done, pending = wait(fs, return_when=FIRST_COMPLETED)
        cancelled = sum(f.cancel() for f in pending)
        print(f"first completed: {len(done)} done, {cancelled} cancelled")

    # CPU-bound: the same API with processes
    nums = [112_272_535_095_293, 112_582_705_942_171, 115_280_095_190_773, 1_099_726_899_285_419]
    t0 = time.perf_counter()
    with ProcessPoolExecutor() as ex:
        flags = list(ex.map(is_prime, nums))
    print(f"primes: {flags} in {time.perf_counter() - t0:.2f}s with processes")
