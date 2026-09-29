"""Profiling: timeit for micro-benchmarks, cProfile + pstats to find hot spots, tracemalloc for memory."""
import cProfile
import io
import pstats
import timeit
import tracemalloc


# ---- a deliberately slow "report" with a hidden hot spot ----
def load_records(n: int) -> list[dict]:
    return [{"id": i, "country": ["IN", "US", "DE", "BR"][i % 4], "amount": i % 97}
            for i in range(n)]


def is_vip(customer_id: int, vip_ids: list[int]) -> bool:
    return customer_id in vip_ids             # O(len(vip_ids)) per call!


def build_report(n: int = 20_000) -> dict:
    records = load_records(n)
    vip_ids = list(range(0, n, 7))            # a list, not a set
    totals: dict[str, int] = {}
    for r in records:
        if is_vip(r["id"], vip_ids):
            totals[r["country"]] = totals.get(r["country"], 0) + r["amount"]
    return totals


def build_report_fast(n: int = 20_000) -> dict:
    records = load_records(n)
    vip_ids = set(range(0, n, 7))             # O(1) membership
    totals: dict[str, int] = {}
    for r in records:
        if r["id"] in vip_ids:
            totals[r["country"]] = totals.get(r["country"], 0) + r["amount"]
    return totals


if __name__ == "__main__":
    # 1. timeit: repeat a small statement many times, take the best run
    t = min(timeit.repeat("sum(range(1000))", number=10_000, repeat=5))
    print(f"timeit sum(range(1000)): {t / 10_000 * 1e6:.2f} µs per call")

    # 2. cProfile: which function eats the time?
    prof = cProfile.Profile()
    prof.enable()
    build_report()
    prof.disable()
    buf = io.StringIO()
    pstats.Stats(prof, stream=buf).strip_dirs().sort_stats("cumulative").print_stats(4)
    lines = [ln for ln in buf.getvalue().splitlines() if ln.strip()]
    print("\n".join([lines[0]] + lines[-5:]))   # summary + table

    # 3. Fix and re-measure
    slow = min(timeit.repeat(build_report, number=1, repeat=3))
    fast = min(timeit.repeat(build_report_fast, number=1, repeat=3))
    print(f"list lookup: {slow*1000:.0f} ms  set lookup: {fast*1000:.1f} ms  "
          f"speed-up x{slow / fast:.0f}  same result: {build_report() == build_report_fast()}")

    # 4. tracemalloc: where is memory allocated?
    tracemalloc.start()
    data = load_records(50_000)
    snapshot = tracemalloc.take_snapshot()
    current, peak = tracemalloc.get_traced_memory()
    tracemalloc.stop()
    top = snapshot.statistics("lineno")[0]
    print(f"records: {len(data):,}, current {current/1e6:.1f} MB, peak {peak/1e6:.1f} MB")
    print(f"top allocation site: line {top.traceback[0].lineno}, {top.size/1e6:.1f} MB")
