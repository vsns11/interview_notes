"""Snowflake-style 64-bit IDs: 1 sign bit | 41 bits ms since epoch | 10 bits worker | 12 bits sequence.

Run: .venv-books/bin/python snowflake_ids.py
A fake clock is used so the output is the same on every run.
"""
EPOCH_MS = 1_704_067_200_000          # custom epoch: 2024-01-01T00:00:00Z
WORKER_BITS, SEQ_BITS = 10, 12
MAX_WORKER = (1 << WORKER_BITS) - 1   # 1023
MAX_SEQ = (1 << SEQ_BITS) - 1         # 4095


class FakeClock:
    def __init__(self, start_ms):
        self.now = start_ms

    def ms(self):
        return self.now


class IdGenerator:
    def __init__(self, worker_id, clock):
        assert 0 <= worker_id <= MAX_WORKER
        self.worker_id, self.clock = worker_id, clock
        self.last_ms, self.seq = -1, 0
        self.waits = 0                 # times we ran out of sequence numbers in one ms

    def next_id(self):
        now = self.clock.ms()
        if now < self.last_ms:         # clock went backwards (e.g. NTP step)
            raise RuntimeError(f"clock moved back {self.last_ms - now} ms; refusing to issue IDs")
        if now == self.last_ms:
            self.seq = (self.seq + 1) & MAX_SEQ
            if self.seq == 0:          # 4,096 IDs already used in this millisecond
                self.waits += 1
                self.clock.now += 1    # a real generator would spin until the next ms
                now = self.clock.ms()
        else:
            self.seq = 0
        self.last_ms = now
        return ((now - EPOCH_MS) << (WORKER_BITS + SEQ_BITS)) | (self.worker_id << SEQ_BITS) | self.seq


def decode(i):
    return {"ms_since_epoch": i >> 22, "worker": (i >> 12) & MAX_WORKER, "seq": i & MAX_SEQ}


if __name__ == "__main__":
    clock = FakeClock(EPOCH_MS + 86_400_000 * 270)   # day 270 after the epoch
    gens = [IdGenerator(w, clock) for w in (1, 2, 3)]

    # 3 workers each issue 5,000 IDs during the same burst
    ids = []
    for _ in range(5_000):
        for g in gens:
            ids.append(g.next_id())
    print("issued:", len(ids), "unique:", len(set(ids)))
    print("first ID:", ids[0], decode(ids[0]))
    print("last ID :", ids[-1], decode(ids[-1]))
    print("sequence exhaustion waits per worker:", [g.waits for g in gens])

    # IDs from later milliseconds always sort after earlier ones (k-sorted across workers)
    clock.now += 5
    later = gens[0].next_id()
    print("later ID sorts after every earlier ID:", all(later > x for x in ids))

    # capacity of the bit budget
    years = (1 << 41) / 1000 / 86_400 / 365.25
    print(f"41-bit timestamp lasts {years:.1f} years; per-worker max {MAX_SEQ + 1:,} IDs/ms "
          f"= {(MAX_SEQ + 1) * 1000:,}/s; workers: {MAX_WORKER + 1}")

    # clock moving backwards is refused, not silently duplicated
    clock.now -= 3
    try:
        gens[0].next_id()
    except RuntimeError as e:
        print("clock skew guard:", e)
