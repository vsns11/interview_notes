"""Uninformed and informed search on the classic Romania road map (AIMA).

Problem: drive from Arad to Bucharest. Road lengths are in km; H gives the
straight-line distance from each city to Bucharest (the heuristic)."""
import heapq
from collections import deque

ROADS = [
    ("Arad", "Zerind", 75), ("Arad", "Sibiu", 140), ("Arad", "Timisoara", 118),
    ("Zerind", "Oradea", 71), ("Oradea", "Sibiu", 151), ("Timisoara", "Lugoj", 111),
    ("Lugoj", "Mehadia", 70), ("Mehadia", "Drobeta", 75), ("Drobeta", "Craiova", 120),
    ("Craiova", "Rimnicu Vilcea", 146), ("Craiova", "Pitesti", 138),
    ("Sibiu", "Fagaras", 99), ("Sibiu", "Rimnicu Vilcea", 80),
    ("Rimnicu Vilcea", "Pitesti", 97), ("Fagaras", "Bucharest", 211),
    ("Pitesti", "Bucharest", 101), ("Bucharest", "Giurgiu", 90),
    ("Bucharest", "Urziceni", 85), ("Urziceni", "Hirsova", 98), ("Hirsova", "Eforie", 86),
    ("Urziceni", "Vaslui", 142), ("Vaslui", "Iasi", 92), ("Iasi", "Neamt", 87),
]
H = {"Arad": 366, "Bucharest": 0, "Craiova": 160, "Drobeta": 242, "Eforie": 161,
     "Fagaras": 176, "Giurgiu": 77, "Hirsova": 151, "Iasi": 226, "Lugoj": 244,
     "Mehadia": 241, "Neamt": 234, "Oradea": 380, "Pitesti": 100,
     "Rimnicu Vilcea": 193, "Sibiu": 253, "Timisoara": 329, "Urziceni": 80,
     "Vaslui": 199, "Zerind": 374}

GRAPH: dict[str, dict[str, int]] = {}
for a, b, km in ROADS:
    GRAPH.setdefault(a, {})[b] = km
    GRAPH.setdefault(b, {})[a] = km
# Successors are tried in the order roads are listed above (e.g. Arad: Zerind, Sibiu,
# Timisoara). Search results depend on this order for BFS/DFS, so it is fixed.


def path_cost(path):
    return sum(GRAPH[a][b] for a, b in zip(path, path[1:]))


def bfs(start, goal):
    frontier, reached, expanded = deque([[start]]), {start}, 0
    while frontier:
        path = frontier.popleft()                     # FIFO queue: shallowest first
        expanded += 1
        for nxt in GRAPH[path[-1]]:
            if nxt == goal:                           # early goal test on generation
                return path + [nxt], expanded
            if nxt not in reached:
                reached.add(nxt)
                frontier.append(path + [nxt])
    return None, expanded


def dfs(start, goal):
    frontier, expanded = [[start]], 0
    while frontier:
        path = frontier.pop()                         # LIFO stack: deepest first
        expanded += 1
        if path[-1] == goal:
            return path, expanded
        for nxt in reversed(GRAPH[path[-1]]):         # reversed so the first-listed road is tried first
            if nxt not in path:                       # avoid cycles along this path
                frontier.append(path + [nxt])
    return None, expanded


def depth_limited(path, goal, limit, counter):
    counter[0] += 1
    if path[-1] == goal:
        return path
    if limit == 0:
        return None
    for nxt in GRAPH[path[-1]]:
        if nxt not in path:
            found = depth_limited(path + [nxt], goal, limit - 1, counter)
            if found:
                return found
    return None


def iddfs(start, goal, max_depth=20):
    counter = [0]
    for limit in range(max_depth + 1):                # depth 0, 1, 2, ...
        found = depth_limited([start], goal, limit, counter)
        if found:
            return found, counter[0]
    return None, counter[0]


def best_first(start, goal, f):
    """Generic best-first search: always expand the frontier node with lowest f."""
    frontier = [(f(0, start), 0, [start])]
    best_g = {start: 0}
    expanded = 0
    while frontier:
        _, g, path = heapq.heappop(frontier)
        node = path[-1]
        if g > best_g.get(node, float("inf")):
            continue                                  # stale queue entry
        expanded += 1
        if node == goal:                              # goal test on expansion (needed for optimality)
            return path, expanded
        for nxt, km in GRAPH[node].items():
            g2 = g + km
            if g2 < best_g.get(nxt, float("inf")):
                best_g[nxt] = g2
                heapq.heappush(frontier, (f(g2, nxt), g2, path + [nxt]))
    return None, expanded


def ucs(start, goal):
    return best_first(start, goal, lambda g, n: g)          # f = g


def greedy(start, goal):
    return best_first(start, goal, lambda g, n: H[n])       # f = h


def astar(start, goal):
    return best_first(start, goal, lambda g, n: g + H[n])   # f = g + h


def main() -> None:
    print(f"{'algorithm':<22}{'expanded':>9}{'km':>6}  path")
    for name, algo in [("BFS", bfs), ("DFS", dfs), ("Iterative deepening", iddfs),
                       ("Uniform-cost", ucs), ("Greedy best-first", greedy), ("A*", astar)]:
        path, expanded = algo("Arad", "Bucharest")
        print(f"{name:<22}{expanded:>9}{path_cost(path):>6}  {' -> '.join(path)}")


if __name__ == "__main__":
    main()
