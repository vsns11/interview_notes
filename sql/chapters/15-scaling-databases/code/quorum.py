"""15.5 Leaderless replication: quorum reads and writes with N replicas.
A write reaches only W replicas (the rest miss it, e.g. they were slow or down);
a read asks R random replicas and keeps the value with the highest version.
If R + W > N every read set overlaps every write set, so no read is stale."""
import itertools
import random

random.seed(7)
N = 3

def trial(w: int, r: int) -> bool:
    replicas = [{"v": 1, "val": "old"} for _ in range(N)]
    for i in random.sample(range(N), w):          # the write lands on W replicas only
        replicas[i] = {"v": 2, "val": "new"}
    answers = [replicas[i] for i in random.sample(range(N), r)]
    return max(answers, key=lambda a: a["v"])["val"] == "old"   # stale read?

print(f"N = {N} replicas, 10,000 trials per setting")
print(f"{'W':>2} {'R':>2}  R+W>N  stale reads")
for w, r in itertools.product([1, 2, 3], [1, 2, 3]):
    stale = sum(trial(w, r) for _ in range(10_000))
    print(f"{w:>2} {r:>2}  {str(r + w > N):5}  {stale / 10_000:7.1%}")

# Fault tolerance of the common choice N=3, W=2, R=2:
print("\nN=3, W=2, R=2: writes and reads still succeed with 1 replica down "
      "(2 of 3 answer); with 2 down they fail -> the system chooses consistency.")
