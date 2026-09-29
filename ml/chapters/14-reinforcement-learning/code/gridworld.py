"""Tiny grid environments in pure numpy/python (no gym needed).

Map characters:
  S start   . empty   G goal (terminal)   H hole (terminal)   - trap (penalty, keep going)
  C cliff (penalty and back to start)     + bonus (reward every time you step on it)
Actions: 0=left 1=down 2=right 3=up  (same numbering as Gymnasium's FrozenLake).
"""
from pathlib import Path

import numpy as np

IMG = Path(__file__).resolve().parent.parent / "img"
ARROWS = "<v>^"
MOVES = [(0, -1), (1, 0), (0, 1), (-1, 0)]

FROZEN_LAKE_4x4 = ["SFFF", "FHFH", "FFFH", "HFFG"]   # F = frozen (safe) tile
TREASURE_5x5 = ["S.-..",
                "..-..",
                "...-.",
                ".-...",
                "....G"]
CLIFF_4x12 = ["............",
              "............",
              "............",
              "SCCCCCCCCCCG"]


class GridWorld:
    def __init__(self, layout, slip=0.0, step_reward=0.0, goal_reward=1.0, hole_reward=0.0,
                 trap_reward=-5.0, bonus_reward=3.0, cliff_reward=-100.0, max_steps=100, seed=0):
        self.grid = [row.replace("F", ".") for row in layout]
        self.nrow, self.ncol = len(layout), len(layout[0])
        self.nS, self.nA = self.nrow * self.ncol, 4
        self.slip = slip
        self.r = dict(step=step_reward, goal=goal_reward, hole=hole_reward,
                      trap=trap_reward, bonus=bonus_reward, cliff=cliff_reward)
        self.start = next(self.to_s(r, c) for r in range(self.nrow)
                          for c in range(self.ncol) if self.grid[r][c] == "S")
        self.max_steps = max_steps
        self.rng = np.random.default_rng(seed)

    # --- helpers ---
    def to_s(self, r, c):
        return r * self.ncol + c

    def cell(self, s):
        return self.grid[s // self.ncol][s % self.ncol]

    def is_terminal(self, s):
        return self.cell(s) in "GH"

    def _move(self, s, a):
        r, c = divmod(s, self.ncol)
        dr, dc = MOVES[a]
        r, c = min(max(r + dr, 0), self.nrow - 1), min(max(c + dc, 0), self.ncol - 1)
        return self.to_s(r, c)

    def _outcome(self, s2):
        """reward, next state, done for landing on s2."""
        ch = self.cell(s2)
        if ch == "G":
            return self.r["goal"], s2, True
        if ch == "H":
            return self.r["hole"], s2, True
        if ch == "C":
            return self.r["cliff"], self.start, False
        if ch == "-":
            return self.r["trap"], s2, False       # trap replaces the normal step reward
        if ch == "+":
            return self.r["bonus"], s2, False
        return self.r["step"], s2, False

    # --- the model: list of (prob, next_state, reward, done). Used by dynamic programming ---
    def transitions(self, s, a):
        if self.is_terminal(s):
            return [(1.0, s, 0.0, True)]
        if self.slip == 0:
            dirs = [(1.0, a)]
        else:                                   # FrozenLake-style: intended or either side
            dirs = [(1 - self.slip, a), (self.slip / 2, (a - 1) % 4), (self.slip / 2, (a + 1) % 4)]
        out = []
        for p, d in dirs:
            r, s2, done = self._outcome(self._move(s, d))
            out.append((p, s2, r, done))
        return out

    # --- the simulator: what a model-free agent sees ---
    def reset(self):
        self.s, self.t = self.start, 0
        return self.s

    def step(self, a):
        trans = self.transitions(self.s, a)
        k = self.rng.choice(len(trans), p=[t[0] for t in trans])
        _, self.s, r, done = trans[k]
        self.t += 1
        truncated = self.t >= self.max_steps
        return self.s, r, done, truncated

    # --- pretty printing ---
    def show_policy(self, policy):
        rows = []
        for r in range(self.nrow):
            line = ""
            for c in range(self.ncol):
                s = self.to_s(r, c)
                line += self.cell(s) if self.cell(s) in "GHC" else ARROWS[policy[s]]
            rows.append(line)
        return "\n".join(rows)

    def show_values(self, V):
        return "\n".join(" ".join(f"{V[self.to_s(r, c)]:6.2f}" for c in range(self.ncol))
                         for r in range(self.nrow))


def epsilon_greedy(Q, s, eps, rng):
    if rng.random() < eps:
        return int(rng.integers(Q.shape[1]))
    best = np.flatnonzero(Q[s] == Q[s].max())       # break ties randomly
    return int(rng.choice(best))


if __name__ == "__main__":
    env = GridWorld(FROZEN_LAKE_4x4, slip=2 / 3)
    print("FrozenLake 4x4, slippery: from state 0 pressing RIGHT ->",
          [(round(p, 3), s2, r, d) for p, s2, r, d in env.transitions(0, 2)])
    s, total = env.reset(), 0.0
    for _ in range(20):
        s, r, done, trunc = env.step(int(env.rng.integers(4)))
        total += r
        if done or trunc:
            break
    print("one random episode ended in state", s, "reward", total)
