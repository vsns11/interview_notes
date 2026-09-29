"""Text-to-image product at scale: the envelope math, then a small fleet
simulation with priority queues (paid before free), autoscaling with warm-up
lag, a warm pool, idempotent retries and a dead-letter queue.

All prices and timings are illustrative assumptions.
"""
from __future__ import annotations

import heapq
import math
from collections import deque
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

IMG = Path(__file__).resolve().parent.parent / "img"
IMG.mkdir(exist_ok=True)


def envelope() -> None:
    per_day, gpu_s, price = 2_000_000, 10, 4.0
    rate = per_day / 86_400
    busy = rate * gpu_s
    print("1) Envelope math (illustrative)")
    print(f"   {per_day:,} images/day = {rate:.1f}/s x {gpu_s} GPU-s = {busy:.0f} busy GPUs on average")
    print(f"   fully utilised: {busy:.0f} x ${price}/h x 24 = ${busy * price * 24:,.0f}/day; "
          f"a 500-GPU fleet 24h = ${500 * 2 * 24:,.0f}-${500 * price * 24:,.0f}/day at $2-$4/h")
    free = 0.8 * per_day / 86_400 * gpu_s
    print(f"   free tier (80%): {free:.0f} busy GPUs; halving its steps saves ~{free / 2:.0f} GPUs "
          f"= ${free / 2 * price * 24:,.0f}/day (if GPU time really halves)")
    print(f"   a model with 3x GPU-seconds fleet-wide: {busy * 3:.0f} busy GPUs = "
          f"${busy * 3 * price * 24:,.0f}/day before headroom")
    print(f"   storage: 2M images = {per_day // 4:,} requests x 4; most grid images are never picked")


def simulate(warm_pool: int, seconds: int = 3 * 3600, seed: int = 11):
    """1-second time steps. Arrivals ~ Poisson with a spike in hour 2."""
    rng = np.random.default_rng(seed)
    gpu_s = 10
    base = 23.0                                       # images/s average
    paid_q: deque = deque()
    free_q: deque = deque()
    ready = 280 + warm_pool                           # GPUs able to take work now
    pending: list[tuple[int, int]] = []               # (ready_at, count) - booting GPUs
    busy: list[int] = []                              # heap of finish times
    waits = {"paid": [], "free": []}
    trace = []                                        # (t, paid p90 so far, free queue len)
    gpu_seconds_provisioned = gpu_seconds_busy = 0
    crashes: dict[str, int] = {}
    dead_letter, published = [], set()
    job_id = 0
    seen_last_minute = 0
    for t in range(seconds):
        spike = 2.2 if 3600 <= t < 5400 else 1.0      # a 30-minute viral spike
        n_new = rng.poisson(base * spike)
        seen_last_minute += n_new
        for _ in range(n_new):
            job_id += 1
            (paid_q if rng.random() < 0.2 else free_q).append((t, f"job{job_id}"))
        while pending and pending[0][0] <= t:          # booted GPUs join the fleet
            ready += heapq.heappop(pending)[1]
        while busy and busy[0] <= t:
            heapq.heappop(busy)
        free_gpus = ready - len(busy)
        while free_gpus > 0 and (paid_q or free_q):
            tier, q = ("paid", paid_q) if paid_q else ("free", free_q)
            t0, jid = q.popleft()
            if jid == "job500":                        # a poison prompt that crashes workers
                crashes[jid] = crashes.get(jid, 0) + 1
                if crashes[jid] >= 2:
                    dead_letter.append(jid)            # park it, page a human
                else:
                    q.appendleft((t0, jid))            # retry once, same idempotency key
                continue
            if jid not in published:                   # idempotent publish
                published.add(jid)
                waits[tier].append(t - t0)
            heapq.heappush(busy, t + gpu_s)
            free_gpus -= 1
        if t % 60 == 59:                               # autoscaler sees only the PAST minute
            arrivals_rate = seen_last_minute / 60
            seen_last_minute = 0
            backlog = len(paid_q) + len(free_q)
            want = math.ceil(arrivals_rate * gpu_s / 0.8 + backlog * gpu_s / 120) + warm_pool
            booting = sum(c for _, c in pending)
            if want > ready + booting:
                heapq.heappush(pending, (t + 180, want - ready - booting))   # 3-min warm-up
            elif want < ready - 20:
                ready -= min(ready - len(busy), ready - want)                  # scale in idle GPUs
            trace.append((t / 60, np.percentile(waits["paid"][-500:] or [0], 90), len(free_q)))
        gpu_seconds_provisioned += ready
        gpu_seconds_busy += len(busy)
    util = gpu_seconds_busy / gpu_seconds_provisioned
    cost = gpu_seconds_provisioned / 3600 * 4.0
    return waits, util, cost, trace, dead_letter, len(published)


if __name__ == "__main__":
    envelope()
    print("\n2) Three simulated hours (spike x2.2 in hour 2), paid = 20% of jobs, 3-min GPU warm-up")
    results = {}
    for pool in (0, 60):
        waits, util, cost, trace, dlq, n = simulate(pool)
        results[pool] = trace
        print(f"   warm pool {pool:>2}: paid p99/max wait {np.percentile(waits['paid'], 99):4.0f}/"
              f"{max(waits['paid']):3.0f}s | free p99/max {np.percentile(waits['free'], 99):4.0f}/"
              f"{max(waits['free']):3.0f}s | util {util:.0%} | GPU ${cost:,.0f} | "
              f"published {n:,} | dead-letter {dlq}")
    plt.figure(figsize=(6.4, 3.6))
    for pool, tr in results.items():
        plt.plot([x[0] for x in tr], [x[2] for x in tr], label=f"free-tier queue, warm pool {pool}")
    plt.axvspan(60, 90, color="grey", alpha=0.15, label="demand spike")
    plt.xlim(50, 80)
    plt.xlabel("minute (zoomed on the spike)")
    plt.ylabel("jobs waiting (free tier)")
    plt.title("Paid jobs jump the queue; free jobs absorb the spike")
    plt.legend()
    plt.grid(alpha=0.3)
    plt.tight_layout()
    plt.savefig(IMG / "t2i_queue.png", dpi=130)
    print("   plot: img/t2i_queue.png")
