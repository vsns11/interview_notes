"""Static vs dynamic vs continuous batching - a discrete-time decode simulator.

Model of the GPU: one decode step emits one token for every sequence in the batch.
Decode is memory-bandwidth bound, so a step costs roughly the same whether it
serves 1 or 16 sequences:  step_ms = 25 + 0.5 * batch_size  (illustrative).
Requests arrive at random (Poisson) and want between 20 and 500 output tokens.
"""
import numpy as np

SLOTS = 16
STEP_BASE_MS, STEP_PER_SEQ_MS = 25.0, 0.5
N_REQ = 400
rng = np.random.default_rng(7)
gaps = rng.exponential(1.0, N_REQ)                            # unit-rate Poisson gaps
lengths = rng.integers(20, 501, N_REQ)                        # output tokens wanted
arrivals = None                                               # set per load level below


def step_ms(b):
    return STEP_BASE_MS + STEP_PER_SEQ_MS * b


def simulate(policy, max_wait_ms=200.0):
    t, nxt = 0.0, 0                    # clock, next request index not yet admitted
    queue, running = [], {}            # running: req_id -> tokens left
    first_tok, done = {}, {}
    busy_slot_steps = total_slot_steps = 0
    while len(done) < N_REQ:
        while nxt < N_REQ and arrivals[nxt] <= t:   # move arrivals into the queue
            queue.append(nxt)
            nxt += 1
        # ---------------- admission policy ----------------
        if policy == "continuous":                  # refill any free slot, every step
            while queue and len(running) < SLOTS:
                r = queue.pop(0)
                running[r] = lengths[r]
        elif not running:                           # static/dynamic: only between batches
            oldest_wait = t - arrivals[queue[0]] if queue else 0
            full = len(queue) >= SLOTS
            if policy == "static" and (full or (nxt == N_REQ and queue)):
                pass                                # wait for a FULL batch
            elif policy == "dynamic" and queue and (full or oldest_wait >= max_wait_ms):
                pass                                # full OR waited long enough
            else:
                t = arrivals[nxt] if nxt < N_REQ else t + 1   # idle: jump to next arrival
                if policy == "dynamic" and queue:
                    t = min(t, arrivals[queue[0]] + max_wait_ms)
                continue
            for _ in range(min(SLOTS, len(queue))):
                r = queue.pop(0)
                running[r] = lengths[r]
        if not running:
            t = arrivals[nxt] if nxt < N_REQ else t + 1
            continue
        # ---------------- one decode step ----------------
        # static/dynamic keep finished sequences' slots reserved until the batch ends
        b = len(running)
        t += step_ms(b if policy == "continuous" else max(b, 1))
        active = [r for r, left in running.items() if left > 0]
        busy_slot_steps += len(active)
        total_slot_steps += SLOTS
        for r in active:
            running[r] -= 1
            first_tok.setdefault(r, t)
            if running[r] == 0:
                done[r] = t
        if policy == "continuous":
            running = {r: left for r, left in running.items() if left > 0}
        elif all(left == 0 for left in running.values()):
            running = {}                            # whole batch finished together
    lat = np.array([done[r] - arrivals[r] for r in range(N_REQ)]) / 1000
    ttft = np.array([first_tok[r] - arrivals[r] for r in range(N_REQ)]) / 1000
    makespan = max(done.values()) / 1000
    return {
        "throughput tok/s": lengths.sum() / makespan,
        "slot utilisation": busy_slot_steps / total_slot_steps,
        "mean TTFT s": ttft.mean(),
        "p95 TTFT s": np.percentile(ttft, 95),
        "mean latency s": lat.mean(),
    }


if __name__ == "__main__":
    print(f"{N_REQ} requests, 20-500 output tokens each (mean {lengths.mean():.0f}), {SLOTS} slots per GPU")
    print(f"step time = {STEP_BASE_MS} ms + {STEP_PER_SEQ_MS} ms per sequence in the batch")
    for rate in (0.8, 1.6):
        arrivals = np.cumsum(gaps) * 1000 / rate               # ms
        print(f"\n--- load: {rate} req/s = {rate * lengths.mean():.0f} output tok/s demanded ---")
        rows = {p: simulate(p) for p in ("static", "dynamic", "continuous")}
        keys = list(next(iter(rows.values())).keys())
        print(f"{'metric':<18}" + "".join(f"{p:>12}" for p in rows))
        for k in keys:
            fmt = "{:>12.0%}" if "util" in k else ("{:>12.0f}" if "tok/s" in k else "{:>12.2f}")
            print(f"{k:<18}" + "".join(fmt.format(rows[p][k]) for p in rows))
    print(f"\nbatch of 1 would give {1000 / step_ms(1):.0f} tok/s; "
          f"a full batch of {SLOTS} gives {SLOTS * 1000 / step_ms(SLOTS):.0f} tok/s")
