"""Adversarial search on tic-tac-toe: plain minimax vs alpha-beta pruning.

Board: a string of 9 cells, index 0..8 left-to-right, top-to-bottom; ' ' = empty.
X is MAX (wants +1), O is MIN (wants -1). Utility: +1 X wins, -1 O wins, 0 draw."""
from functools import lru_cache

LINES = [(0, 1, 2), (3, 4, 5), (6, 7, 8), (0, 3, 6), (1, 4, 7), (2, 5, 8), (0, 4, 8), (2, 4, 6)]
nodes = 0


def winner(b):
    for i, j, k in LINES:
        if b[i] != " " and b[i] == b[j] == b[k]:
            return b[i]
    return None


def terminal_utility(b):
    w = winner(b)
    if w:
        return 1 if w == "X" else -1
    if " " not in b:
        return 0
    return None                                   # game not over


def to_move(b):
    return "X" if b.count("X") == b.count("O") else "O"


def moves(b):
    return [i for i, c in enumerate(b) if c == " "]


def play(b, i):
    return b[:i] + to_move(b) + b[i + 1:]


def minimax(b):
    global nodes
    nodes += 1
    u = terminal_utility(b)
    if u is not None:
        return u
    values = [minimax(play(b, m)) for m in moves(b)]
    return max(values) if to_move(b) == "X" else min(values)


def alphabeta(b, alpha=-2, beta=2):
    """alpha = best value MAX can already guarantee, beta = best value MIN can guarantee."""
    global nodes
    nodes += 1
    u = terminal_utility(b)
    if u is not None:
        return u
    if to_move(b) == "X":
        v = -2
        for m in moves(b):
            v = max(v, alphabeta(play(b, m), alpha, beta))
            alpha = max(alpha, v)
            if alpha >= beta:
                break                             # MIN above will never allow this: prune
        return v
    v = 2
    for m in moves(b):
        v = min(v, alphabeta(play(b, m), alpha, beta))
        beta = min(beta, v)
        if alpha >= beta:
            break                                 # MAX above will never allow this: prune
    return v


@lru_cache(maxsize=None)
def cached_value(b):                              # memoised minimax (transposition table)
    u = terminal_utility(b)
    if u is not None:
        return u
    values = [cached_value(play(b, m)) for m in moves(b)]
    return max(values) if to_move(b) == "X" else min(values)


def best_move(b):
    pick = max if to_move(b) == "X" else min
    return pick(moves(b), key=lambda m: cached_value(play(b, m)))


def show(b):
    return "\n".join(" ".join(c if c != " " else "." for c in b[r * 3:r * 3 + 3]) for r in range(3))


def main() -> None:
    global nodes
    empty = " " * 9
    nodes = 0
    v = minimax(empty)
    print(f"minimax    : value of empty board = {v:+d}, nodes visited = {nodes:,}")
    nodes = 0
    v = alphabeta(empty)
    print(f"alpha-beta : value of empty board = {v:+d}, nodes visited = {nodes:,}")
    cached_value(empty)
    print(f"memoised   : distinct positions evaluated = {cached_value.cache_info().currsize:,}")

    puzzle = "XX OO    "                             # X to move: can win at cell 2
    print("\nposition (X to move):\n" + show(puzzle))
    print("best move for X:", best_move(puzzle))

    print("\nperfect play from an empty board:")
    b = empty
    while terminal_utility(b) is None:
        b = play(b, best_move(b))
    print(show(b))
    print("result:", {1: "X wins", -1: "O wins", 0: "draw"}[terminal_utility(b)])


if __name__ == "__main__":
    main()
