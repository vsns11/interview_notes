"""Project: a pathfinding agent on a grid.

Step 1 set-up    : grid with start, goal and walls
Step 2 algorithm : BFS, uniform-cost (Dijkstra), greedy best-first or A*
Step 3 search    : expand cells, record what was visited and what is on the frontier
Step 4 visualise : ASCII for the small maze, matplotlib figures for the big one
Step 5 reflect   : compare path length (optimal?) and cells expanded (efficient?)"""
import heapq
import itertools
import random
from collections import deque
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

IMG = Path(__file__).resolve().parent.parent / "img"
MOVES = ((-1, 0), (1, 0), (0, -1), (0, 1))


# ---------- Step 1: the world ------------------------------------------------
def random_grid(n, wall_prob, seed, start, goal):
    rng = random.Random(seed)
    walls = {(r, c) for r in range(n) for c in range(n)
             if rng.random() < wall_prob and (r, c) not in (start, goal)}
    return {"n": n, "walls": walls, "start": start, "goal": goal}


def neighbours(world, cell):
    r, c = cell
    for dr, dc in MOVES:
        nr, nc = r + dr, c + dc
        if 0 <= nr < world["n"] and 0 <= nc < world["n"] and (nr, nc) not in world["walls"]:
            yield (nr, nc)


def manhattan(a, b):
    return abs(a[0] - b[0]) + abs(a[1] - b[1])


# ---------- Steps 2 + 3: one search routine, four strategies -------------------
def search(world, strategy, snapshot_at=()):
    start, goal = world["start"], world["goal"]
    tie = itertools.count()                        # FIFO tie-breaker for the priority queue
    if strategy == "BFS":
        frontier = deque([start])
        push, pop = frontier.append, frontier.popleft
    else:
        frontier = []
        def priority(g, cell):
            h = manhattan(cell, goal)
            return {"Uniform-cost": g, "Greedy": h, "A*": g + h}[strategy], h
        push = lambda cell: heapq.heappush(frontier, (*priority(g_cost[cell], cell), next(tie), cell))
        pop = lambda: heapq.heappop(frontier)[-1]
    g_cost, parent, closed, order, snaps = {start: 0}, {start: None}, set(), [], {}
    push(start)
    while frontier:
        cell = pop()
        if cell in closed:
            continue                                # already expanded via a cheaper entry
        closed.add(cell)
        order.append(cell)
        if len(order) in snapshot_at:
            cells = list(frontier) if strategy == "BFS" else [entry[-1] for entry in frontier]
            snaps[len(order)] = (list(order), cells)
        if cell == goal:
            path = []
            while cell is not None:
                path.append(cell)
                cell = parent[cell]
            return path[::-1], order, snaps
        for nxt in neighbours(world, cell):
            g2 = g_cost[cell] + 1
            if nxt not in g_cost or (strategy != "BFS" and g2 < g_cost[nxt]):
                g_cost[nxt], parent[nxt] = g2, cell
                push(nxt)
    return None, order, snaps


# ---------- Step 4: visualise ------------------------------------------------
def ascii_grid(world, path, visited):
    rows = []
    for r in range(world["n"]):
        row = []
        for c in range(world["n"]):
            cell = (r, c)
            row.append("S" if cell == world["start"] else "G" if cell == world["goal"]
                       else "#" if cell in world["walls"] else "*" if cell in path
                       else "o" if cell in visited else ".")
        rows.append(" ".join(row))
    return "\n".join(rows)


def draw(ax, world, visited, path, title, frontier=()):
    img = np.ones((world["n"], world["n"], 3))
    for r, c in world["walls"]:
        img[r, c] = (0.15, 0.15, 0.15)
    for r, c in visited:
        img[r, c] = (0.74, 0.84, 0.93)
    for r, c in frontier:
        img[r, c] = (0.99, 0.75, 0.45)
    ax.imshow(img, interpolation="nearest")
    if path:
        ys, xs = zip(*path)
        ax.plot(xs, ys, color="#d7301f", lw=2)
    for cell, colour in ((world["start"], "#1a9850"), (world["goal"], "#d73027")):
        ax.plot(cell[1], cell[0], "s", color=colour, ms=8)
    ax.set_title(title, fontsize=9)
    ax.set_xticks([])
    ax.set_yticks([])


def main() -> None:
    # Mini example: 5x5, start (1,1), goal (5,5) in 1-indexed terms = (0,0)..(4,4)
    small = random_grid(5, 0.25, seed=3, start=(0, 0), goal=(4, 4))
    print("Mini maze (S start, G goal, # wall, * path, o expanded but not on path)")
    for strategy in ("BFS", "A*"):
        path, order, _ = search(small, strategy)
        print(f"\n{strategy}: path length {len(path) - 1}, cells expanded {len(order)}")
        print(ascii_grid(small, set(path), set(order)))

    # Bigger world: 25x25 with ~30% walls
    big = random_grid(25, 0.30, seed=21, start=(0, 0), goal=(24, 24))
    fig, axes = plt.subplots(1, 4, figsize=(14, 4))
    print(f"\n25x25 grid, {len(big['walls'])} walls")
    print(f"{'strategy':<14}{'path length':>12}{'expanded':>10}")
    for ax, strategy in zip(axes, ("BFS", "Uniform-cost", "Greedy", "A*")):
        path, order, _ = search(big, strategy)
        print(f"{strategy:<14}{len(path) - 1:>12}{len(order):>10}")
        draw(ax, big, order, path, f"{strategy}: length {len(path) - 1}, expanded {len(order)}")
    fig.tight_layout()
    fig.savefig(IMG / "pathfinding_compare.png", dpi=130)

    # A* snapshots: visited (blue), frontier (orange), final path (red)
    stops = (15, 60)
    path, order, snaps = search(big, "A*", snapshot_at=stops)
    fig, axes = plt.subplots(1, 3, figsize=(11, 4))
    for ax, k in zip(axes, stops):
        vis, fr = snaps[k]
        draw(ax, big, vis, None, f"A* after {k} expansions", frontier=fr)
    draw(axes[2], big, order, path, f"A* finished: {len(order)} expansions")
    fig.tight_layout()
    fig.savefig(IMG / "astar_snapshots.png", dpi=130)
    print("saved pathfinding_compare.png, astar_snapshots.png")


if __name__ == "__main__":
    main()
