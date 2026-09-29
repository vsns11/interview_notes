"""A* on a 5x5 grid, printing every expansion so you can follow f = g + h by hand.

Coordinates are (row, col), 1-indexed like the drawing. Moves: up/down/left/right,
cost 1 each. Heuristic: Manhattan distance |r - gr| + |c - gc| (admissible here)."""
import heapq

GRID = [                # S start, G goal, # wall, . free
    "S....",
    "###.#",
    "....#",
    ".####",
    "....G",
]


def find(ch):
    for r, row in enumerate(GRID, start=1):
        for c, cell in enumerate(row, start=1):
            if cell == ch:
                return (r, c)


def neighbours(cell):
    r, c = cell
    for dr, dc in ((-1, 0), (1, 0), (0, -1), (0, 1)):       # up, down, left, right
        nr, nc = r + dr, c + dc
        if 1 <= nr <= len(GRID) and 1 <= nc <= len(GRID[0]) and GRID[nr - 1][nc - 1] != "#":
            yield (nr, nc)


def manhattan(a, b):
    return abs(a[0] - b[0]) + abs(a[1] - b[1])


def astar(start, goal, trace=True):
    frontier = [(manhattan(start, goal), manhattan(start, goal), 0, start)]  # (f, h, g, cell)
    parent, best_g, step = {start: None}, {start: 0}, 0
    while frontier:
        f, h, g, cell = heapq.heappop(frontier)     # ties on f broken by smaller h
        if g > best_g[cell]:
            continue
        step += 1
        if trace:
            print(f"step {step:>2}: expand {cell}  g={g} h={h} f={f}")
        if cell == goal:
            path = []
            while cell:
                path.append(cell)
                cell = parent[cell]
            return path[::-1], step
        for nxt in neighbours(cell):
            g2 = g + 1
            if g2 < best_g.get(nxt, float("inf")):
                best_g[nxt], parent[nxt] = g2, cell
                h2 = manhattan(nxt, goal)
                heapq.heappush(frontier, (g2 + h2, h2, g2, nxt))
    return None, step


def show(path):
    rows = [list(row) for row in GRID]
    for r, c in path[1:-1]:
        rows[r - 1][c - 1] = "*"
    print("\n".join(" ".join(row) for row in rows))


def main() -> None:
    start, goal = find("S"), find("G")
    path, expanded = astar(start, goal)
    print(f"\npath length {len(path) - 1} moves, {expanded} expansions")
    show(path)


if __name__ == "__main__":
    main()
