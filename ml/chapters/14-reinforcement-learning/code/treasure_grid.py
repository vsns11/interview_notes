"""The 'treasure grid' paper game, in code: score hand-picked paths, compute discounted
returns, then let pure trial and error (random exploration) search for a better path."""
import numpy as np

from gridworld import TREASURE_5x5, GridWorld

L, D, R, U = 0, 1, 2, 3
NAMES = "LDRU"


def play(env, actions, gamma=0.9):
    s = env.reset()
    rewards = []
    for a in actions:
        s, r, done, _ = env.step(a)
        rewards.append(r)
        if done:
            break
    G = sum(gamma ** t * r for t, r in enumerate(rewards))
    return rewards, sum(rewards), G, done


def main():
    env = GridWorld(TREASURE_5x5, step_reward=-1, goal_reward=10, trap_reward=-5)
    print("\n".join(TREASURE_5x5), "\n(S start, G treasure +10, - trap -5, every other step -1)\n")
    paths = {
        "A: right along the top, then down": [R, R, R, R, D, D, D, D],
        "B: down the left edge, then right": [D, D, D, D, R, R, R, R],
        "C: zig-zag through the middle":     [D, R, D, R, D, R, D, R],
    }
    for name, acts in paths.items():
        rewards, total, G, done = play(env, acts)
        print(f"{name:36s} rewards={rewards}  total={total:+.0f}  "
              f"discounted G(gamma=0.9)={G:+.2f}  reached={done}")

    # trial and error: try 2,000 random walks (max 30 steps), keep the best
    rng = np.random.default_rng(0)
    best, reached = (-np.inf, None), 0
    for _ in range(2000):
        acts = rng.integers(0, 4, 30).tolist()
        rewards, total, G, done = play(env, acts)
        reached += done
        if done and total > best[0]:
            best = (total, "".join(NAMES[a] for a in acts[: len(rewards)]))
    print(f"\nrandom tries that reached the treasure within 30 moves: {reached} / 2000")
    print(f"best of 2,000 random tries: total={best[0]:+.0f}  moves={best[1]}")


if __name__ == "__main__":
    main()
