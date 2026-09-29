"""Reward hacking in miniature: a designer adds a +3 'bonus' tile to encourage exploring.
The optimal agent for that reward never reaches the treasure -- it farms the bonus forever."""
from gridworld import GridWorld
from value_iteration import value_iteration

NAMES = "LDRU"
LAYOUT = ["S+-..",
          "..-..",
          "...-.",
          ".-...",
          "....G"]


def rollout(env, policy, steps=12):
    s, moves, total = env.reset(), "", 0.0
    for _ in range(steps):
        a = policy[s]
        s, r, done, _ = env.step(a)
        moves += NAMES[a]; total += r
        if done:
            return moves, total, True
    return moves, total, False


def main():
    for bonus in (3.0, 0.0):
        env = GridWorld(LAYOUT, step_reward=-1, goal_reward=10, trap_reward=-5, bonus_reward=bonus)
        V, pi, _ = value_iteration(env, gamma=0.9)
        moves, total, reached = rollout(env, pi)
        print(f"bonus tile worth {bonus:+.0f}: first 12 moves {moves:12s} reward {total:+.0f} "
              f"reached treasure: {reached}   V(start) = {V[env.start]:.2f}")


if __name__ == "__main__":
    main()
