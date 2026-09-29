"""Monte Carlo Tree Search (UCT) for tic-tac-toe - no evaluation function needed,
only random play-outs. Compare its choices with exact minimax."""
import math
import random

from tictactoe_minimax import best_move, moves, play, show, terminal_utility, to_move


class Node:
    def __init__(self, board, parent=None, move=None):
        self.board, self.parent, self.move = board, parent, move
        self.children = []
        self.untried = [] if terminal_utility(board) is not None else moves(board)  # game over: no moves
        self.visits, self.wins = 0, 0.0          # wins from the view of the player who moved INTO this node

    def ucb1(self, c=1.4):
        return self.wins / self.visits + c * math.sqrt(math.log(self.parent.visits) / self.visits)


def rollout(board, rng):
    while terminal_utility(board) is None:
        board = play(board, rng.choice(moves(board)))
    return terminal_utility(board)               # +1 X win, -1 O win, 0 draw


def mcts(board, iterations, rng):
    root = Node(board)
    for _ in range(iterations):
        node = root
        # 1. Selection: descend by UCB1 while the node is fully expanded
        while not node.untried and node.children:
            node = max(node.children, key=Node.ucb1)
        # 2. Expansion: add one unexplored child
        if node.untried:
            m = node.untried.pop(rng.randrange(len(node.untried)))
            node.children.append(Node(play(node.board, m), node, m))
            node = node.children[-1]
        # 3. Simulation: random play-out to the end
        result = rollout(node.board, rng)
        # 4. Back-propagation: update statistics up to the root
        while node is not None:
            node.visits += 1
            if node.parent is not None:
                mover = to_move(node.parent.board)
                node.wins += 1.0 if result == (1 if mover == "X" else -1) else 0.5 if result == 0 else 0.0
            node = node.parent
    return max(root.children, key=lambda n: n.visits)   # most-visited child = most robust choice


def main() -> None:
    rng = random.Random(0)
    positions = {
        "X can win now": "XX OO    ",
        "O must block": "XX  O    ",
        "empty board": " " * 9,
    }
    for name, board in positions.items():
        choice = mcts(board, 2000, rng)
        share = choice.visits / 2000
        print(f"{name:<14} MCTS picks cell {choice.move} ({share:.0%} of visits, "
              f"win rate {choice.wins / choice.visits:.2f}); minimax picks {best_move(board)}")

    # MCTS (as O, 1000 iterations per move) against a random X player
    tally = {"X wins": 0, "O wins": 0, "draw": 0}
    for g in range(20):
        board, game_rng = " " * 9, random.Random(100 + g)
        while terminal_utility(board) is None:
            m = game_rng.choice(moves(board)) if to_move(board) == "X" else mcts(board, 1000, rng).move
            board = play(board, m)
        tally[{1: "X wins", -1: "O wins", 0: "draw"}[terminal_utility(board)]] += 1
    print("\n20 games, random X vs MCTS O:", tally)
    print("\nlast game:\n" + show(board))


if __name__ == "__main__":
    main()
