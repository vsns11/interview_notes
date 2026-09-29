"""19.8 Graphs: adjacency lists, BFS/DFS, grids, Dijkstra, topological sort."""
import heapq
import sys
from collections import defaultdict, deque


def build_graph(edges: list[tuple[str, str]], directed: bool = False) -> dict[str, list[str]]:
    g: defaultdict[str, list[str]] = defaultdict(list)
    for u, v in edges:
        g[u].append(v)
        if not directed:
            g[v].append(u)
    return g


def bfs_shortest_path(g: dict[str, list[str]], start: str, goal: str) -> list[str] | None:
    parent = {start: None}          # doubles as the visited set
    q = deque([start])
    while q:
        node = q.popleft()
        if node == goal:
            path = []
            while node is not None:
                path.append(node)
                node = parent[node]
            return path[::-1]
        for nxt in g.get(node, []):
            if nxt not in parent:   # mark when ENQUEUED, not when popped
                parent[nxt] = node
                q.append(nxt)
    return None


def dfs_recursive(g, node, seen=None) -> list[str]:
    if seen is None:                # never use a mutable default (see 19.13)
        seen = set()
    seen.add(node)
    order = [node]
    for nxt in g.get(node, []):
        if nxt not in seen:
            order += dfs_recursive(g, nxt, seen)
    return order


def dfs_iterative(g, start) -> list[str]:
    seen, stack, order = set(), [start], []
    while stack:
        node = stack.pop()
        if node in seen:
            continue
        seen.add(node)
        order.append(node)
        for nxt in reversed(g.get(node, [])):   # reversed -> same order as recursive
            if nxt not in seen:
                stack.append(nxt)
    return order


def num_islands(grid: list[str]) -> int:
    rows, cols = len(grid), len(grid[0])
    seen = set()

    def flood(r, c):
        stack = [(r, c)]
        seen.add((r, c))
        while stack:
            r, c = stack.pop()
            for dr, dc in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nr, nc = r + dr, c + dc
                if 0 <= nr < rows and 0 <= nc < cols and grid[nr][nc] == "1" and (nr, nc) not in seen:
                    seen.add((nr, nc))
                    stack.append((nr, nc))

    count = 0
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] == "1" and (r, c) not in seen:
                flood(r, c)
                count += 1
    return count


def dijkstra(g: dict[str, list[tuple[str, int]]], src: str) -> dict[str, int]:
    dist = {src: 0}
    heap = [(0, src)]
    while heap:
        d, u = heapq.heappop(heap)
        if d > dist.get(u, float("inf")):
            continue                    # stale entry: a shorter path was found already
        for v, w in g.get(u, []):
            nd = d + w
            if nd < dist.get(v, float("inf")):
                dist[v] = nd
                heapq.heappush(heap, (nd, v))
    return dist


def topo_sort_kahn(n: int, prereqs: list[tuple[int, int]]) -> list[int] | None:
    """prereqs (a, b) means b must come before a. Returns None if there is a cycle."""
    g = defaultdict(list)
    indeg = [0] * n
    for a, b in prereqs:
        g[b].append(a)
        indeg[a] += 1
    q = deque(i for i in range(n) if indeg[i] == 0)
    order = []
    while q:
        u = q.popleft()
        order.append(u)
        for v in g[u]:
            indeg[v] -= 1
            if indeg[v] == 0:
                q.append(v)
    return order if len(order) == n else None


def topo_sort_dfs(n: int, prereqs: list[tuple[int, int]]) -> list[int] | None:
    g = defaultdict(list)
    for a, b in prereqs:
        g[b].append(a)
    WHITE, GREY, BLACK = 0, 1, 2          # unvisited / on current path / done
    color = [WHITE] * n
    out = []

    def visit(u) -> bool:
        color[u] = GREY
        for v in g[u]:
            if color[v] == GREY:
                return False              # back edge -> cycle
            if color[v] == WHITE and not visit(v):
                return False
        color[u] = BLACK
        out.append(u)                     # post-order
        return True

    for u in range(n):
        if color[u] == WHITE and not visit(u):
            return None
    return out[::-1]


def deep_chain_depth(n: int) -> int:
    def depth(k):
        return 0 if k == 0 else 1 + depth(k - 1)
    return depth(n)


if __name__ == "__main__":
    g = build_graph([("A", "B"), ("A", "C"), ("B", "D"), ("C", "D"), ("D", "E")])
    print("adjacency:", dict(g))
    assert bfs_shortest_path(g, "A", "E") == ["A", "B", "D", "E"]
    assert bfs_shortest_path(g, "A", "Z") is None
    print("dfs recursive:", dfs_recursive(g, "A"), "| iterative:", dfs_iterative(g, "A"))
    assert dfs_recursive(g, "A") == dfs_iterative(g, "A")

    assert num_islands(["11000", "11000", "00100", "00011"]) == 3

    wg = {"A": [("B", 4), ("C", 1)], "C": [("B", 2), ("D", 5)], "B": [("D", 1)]}
    dist = dijkstra(wg, "A")
    print("dijkstra from A:", dist)
    assert dist == {"A": 0, "C": 1, "B": 3, "D": 4}

    order = topo_sort_kahn(4, [(1, 0), (2, 0), (3, 1), (3, 2)])
    print("course order (Kahn):", order, "| DFS:", topo_sort_dfs(4, [(1, 0), (2, 0), (3, 1), (3, 2)]))
    assert order == [0, 1, 2, 3]
    assert topo_sort_kahn(2, [(0, 1), (1, 0)]) is None
    assert topo_sort_dfs(2, [(0, 1), (1, 0)]) is None

    print("recursion limit:", sys.getrecursionlimit())
    try:
        deep_chain_depth(5_000)
    except RecursionError as e:
        print("depth 5000 ->", type(e).__name__)
    sys.setrecursionlimit(10_000)
    print("after setrecursionlimit(10_000): depth", deep_chain_depth(5_000))
    print("all asserts passed")
