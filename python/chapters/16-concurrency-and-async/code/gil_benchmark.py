"""CPU-bound vs I/O-bound work under sequential, threads and processes. Saves img/gil_benchmark.png."""
import sys
import time
from concurrent.futures import ProcessPoolExecutor, ThreadPoolExecutor
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

TASKS = 4


def cpu_task(n: int = 15_000_000) -> int:
    total = 0
    for i in range(n):                    # pure-Python loop: holds the GIL the whole time
        total += i * i
    return total


def io_task(_: int = 0) -> None:
    time.sleep(0.5)                       # sleeping releases the GIL, like waiting on a socket


def timed(fn) -> float:
    t0 = time.perf_counter()
    fn()
    return time.perf_counter() - t0


def sequential(task):
    return lambda: [task() for _ in range(TASKS)]


def threaded(task):
    def run():
        with ThreadPoolExecutor(max_workers=TASKS) as ex:
            list(ex.map(lambda _: task(), range(TASKS)))
    return run


def processes(task):
    def run():
        with ProcessPoolExecutor(max_workers=TASKS) as ex:
            list(ex.map(task, [15_000_000 if task is cpu_task else 0] * TASKS))
    return run


if __name__ == "__main__":
    gil = getattr(sys, "_is_gil_enabled", lambda: True)()
    print(f"Python {sys.version.split()[0]}, GIL enabled: {gil}")
    results = {}
    for label, task in [("CPU-bound", cpu_task), ("I/O-bound", io_task)]:
        row = {name: timed(make(task)) for name, make in
               [("sequential", sequential), ("threads", threaded), ("processes", processes)]}
        results[label] = row
        print(f"{label:9}: " + "  ".join(f"{k} {v:.2f}s" for k, v in row.items()))

    fig, axes = plt.subplots(1, 2, figsize=(9, 3.6))
    colors = ["#8c8c8c", "#3b7dd8", "#e07b39"]
    for ax, (label, row) in zip(axes, results.items()):
        ax.bar(list(row), list(row.values()), color=colors)
        ax.set_title(f"{label}: {TASKS} tasks")
        ax.set_ylabel("wall time (s)")
        for i, v in enumerate(row.values()):
            ax.text(i, v, f"{v:.2f}", ha="center", va="bottom")
    fig.suptitle("Threads help I/O-bound work, processes help CPU-bound work (GIL build)")
    fig.tight_layout()
    out = Path(__file__).resolve().parent.parent / "img" / "gil_benchmark.png"
    fig.savefig(out, dpi=120)
    print(f"saved {out.name}")
