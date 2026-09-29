"""15.8 Leader election: message counts of the Bully and Ring algorithms.
Nodes have ids 1..n; the old leader (id n) has crashed; the lowest id notices first
(the worst case for Bully)."""

def bully(n: int, starter: int = 1) -> tuple[int, int]:
    alive = list(range(1, n))              # node n is dead
    msgs, pending, seen = 0, [starter], set()
    while pending:
        node = pending.pop()
        if node in seen:
            continue
        seen.add(node)
        higher = [x for x in range(node + 1, n + 1)]   # ELECTION to every higher id
        msgs += len(higher)
        answering = [x for x in higher if x in alive]
        msgs += len(answering)                          # OK replies from live nodes
        pending.extend(answering)                       # each responder runs its own election
    leader = max(alive)
    msgs += len(alive) - 1                              # COORDINATOR broadcast
    return leader, msgs

def ring(n: int, starter: int = 1) -> tuple[int, int]:
    alive = list(range(1, n))              # node n is dead and skipped in the ring
    k = len(alive)
    msgs = k        # ELECTION message carrying the best id travels once around
    msgs += k       # COORDINATOR message travels once around
    return max(alive), msgs

print(f"{'nodes':>5} {'bully leader':>12} {'bully msgs':>10} {'ring leader':>11} {'ring msgs':>9}")
for n in (5, 10, 50):
    bl, bm = bully(n)
    rl, rm = ring(n)
    print(f"{n:>5} {bl:>12} {bm:>10} {rl:>11} {rm:>9}")
