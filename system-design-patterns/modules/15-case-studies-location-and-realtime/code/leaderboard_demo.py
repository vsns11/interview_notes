"""A Redis-sorted-set-style leaderboard in plain Python.

Redis keeps a sorted set as a hash map (member -> score) plus a skip list
ordered by (score, member), which gives O(log N) updates and rank lookups
(see redis.io docs "Sorted sets" and ZADD/ZINCRBY/ZREVRANK/ZREVRANGE).
Here a dict + a bisect-sorted Python list stands in for the skip list:
same API and semantics, simpler code (insert is O(N) here, O(log N) in Redis).

What it shows:
  1. ZINCRBY / ZREVRANK / ZREVRANGE for "my rank", "top 10", "players around me".
  2. Tie-breaking: equal points ranked by who got there first (composite score).
  3. Sharding by user: each shard keeps its own top-K; a scatter-gather merge of
     the per-shard top-K gives the exact global top-K.

Run:  .venv-books/bin/python leaderboard_demo.py
"""
import bisect
import heapq
import random
import zlib


class SortedSet:
    """Tiny subset of Redis ZSET semantics."""

    def __init__(self):
        self.score = {}      # member -> score          (the hash map)
        self.order = []      # sorted [(score, member)]  (stand-in for the skip list)

    def zadd(self, member, score):
        if member in self.score:
            self.order.pop(bisect.bisect_left(self.order, (self.score[member], member)))
        self.score[member] = score
        bisect.insort(self.order, (score, member))

    def zincrby(self, member, delta):
        self.zadd(member, self.score.get(member, 0) + delta)
        return self.score[member]

    def zrevrank(self, member):
        """0-based rank, highest score first (like Redis ZREVRANK)."""
        if member not in self.score:
            return None
        i = bisect.bisect_left(self.order, (self.score[member], member))
        return len(self.order) - 1 - i

    def zrevrange(self, start, stop):
        """Inclusive range by descending rank, like ZREVRANGE key start stop WITHSCORES."""
        n = len(self.order)
        stop = min(stop, n - 1)
        return [(self.order[n - 1 - r][1], self.order[n - 1 - r][0])
                for r in range(max(start, 0), stop + 1)]

    def zcard(self):
        return len(self.order)


# Tie-break: ranking must be by points DESC, then by "reached it first" ASC.
# Pack both into one score: points * 2^32 + (MAX_TS - timestamp). Redis scores are
# doubles, so keep points * 2^32 below 2^53 (exact integers) -- true for < 2M points.
MAX_TS = 2**32 - 1


def composite(points, ts):
    return points * 2**32 + (MAX_TS - ts)


def points_of(score):
    return score >> 32


if __name__ == "__main__":
    rnd = random.Random(15)
    board = SortedSet()                # key "leaderboard:2026-09" in Redis terms
    users = [f"u{i:05d}" for i in range(20_000)]
    points = {}
    reached = {}

    # 200k "match won" events; each win is worth 1-3 points
    for ts in range(1, 200_001):
        u = rnd.choice(users)
        points[u] = points.get(u, 0) + rnd.choice((1, 1, 2, 3))
        reached[u] = ts
        board.zadd(u, composite(points[u], ts))

    print(f"1) {board.zcard():,} players, 200,000 score events")
    top = board.zrevrange(0, 4)
    print("   top 5 (ZREVRANGE 0 4):")
    for rank, (m, s) in enumerate(top, 1):
        print(f"     #{rank}  {m}  {points_of(s)} pts")

    me = "u01234"
    r = board.zrevrank(me)
    print(f"\n2) {me}: {points[me]} pts, rank #{r + 1} (ZREVRANK = {r})")
    print("   players around me (ZREVRANGE r-2 r+2):")
    for i, (m, s) in enumerate(board.zrevrange(r - 2, r + 2), start=r - 1):
        mark = "  <- me" if m == me else ""
        print(f"     #{i}  {m}  {points_of(s)} pts{mark}")

    # correctness check of rank against a brute-force sort
    brute = sorted(users, key=lambda u: (-points.get(u, 0), reached.get(u, 0)))
    assert brute.index(me) == r
    assert [m for m, _ in board.zrevrange(0, 99)] == brute[:100]

    # tie-break: two players with equal points; earlier arrival must rank higher
    t = SortedSet()
    t.zadd("alice", composite(50, ts=1000))
    t.zadd("bob", composite(50, ts=900))      # bob reached 50 first
    assert t.zrevrange(0, 1)[0][0] == "bob"
    print("\n3) tie at 50 pts: bob (reached at t=900) ranks above alice (t=1000):",
          [m for m, _ in t.zrevrange(0, 1)])

    # sharding: 8 shards by hash(user); global top-10 = merge of per-shard top-10
    shards = [SortedSet() for _ in range(8)]
    for u in points:
        shards[zlib.crc32(u.encode()) % 8].zadd(u, composite(points[u], reached[u]))
    per_shard = [s.zrevrange(0, 9) for s in shards]           # 8 x 10 = 80 rows
    merged = heapq.nlargest(10, (row for rows in per_shard for row in rows),
                            key=lambda row: row[1])
    assert [m for m, _ in merged] == brute[:10]
    print(f"\n4) 8 shards: merged {sum(len(p) for p in per_shard)} rows "
          f"(top-10 from each) -> exact global top-10: {[m for m, _ in merged][:3]}...")
    print("   but a global rank for one user needs a count from EVERY shard:")
    cnt = sum(1 for s in shards for sc, _ in s.order
              if sc > composite(points[me], reached[me]))
    print(f"   {me}: sum of per-shard 'players above me' = {cnt} -> rank #{cnt + 1}")
    assert cnt == r
    print("\nall asserts passed")
