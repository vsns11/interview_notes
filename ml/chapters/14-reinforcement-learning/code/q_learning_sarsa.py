"""Tabular Q-learning vs SARSA.
Part 1: Cliff Walking (the classic example where the two behave differently).
Part 2: Q-learning on slippery FrozenLake 4x4, compared with the optimal policy from value iteration."""
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

from gridworld import CLIFF_4x12, FROZEN_LAKE_4x4, IMG, GridWorld, epsilon_greedy
from value_iteration import success_rate, value_iteration


def train(env, method, episodes, alpha, gamma, eps_schedule, seed=0):
    rng = np.random.default_rng(seed)
    Q = np.zeros((env.nS, env.nA))
    returns = []
    for ep in range(episodes):
        eps = eps_schedule(ep)
        s = env.reset()
        a = epsilon_greedy(Q, s, eps, rng)
        total, done, trunc = 0.0, False, False
        while not (done or trunc):
            s2, r, done, trunc = env.step(a)
            a2 = epsilon_greedy(Q, s2, eps, rng)
            if method == "Q-learning":           # off-policy: bootstrap from the BEST next action
                target = r + gamma * Q[s2].max() * (not done)
            else:                                # SARSA, on-policy: from the action we WILL take
                target = r + gamma * Q[s2, a2] * (not done)
            Q[s, a] += alpha * (target - Q[s, a])
            s, a, total = s2, a2, total + r
        returns.append(total)
    return Q, np.array(returns)


def main():
    # ---------- Part 1: cliff walking ----------
    fig, axes = plt.subplots(1, 2, figsize=(11, 4))
    for method in ("Q-learning", "SARSA"):
        curves = []
        for seed in range(20):
            env = GridWorld(CLIFF_4x12, step_reward=-1, goal_reward=-1, cliff_reward=-100,
                            max_steps=500, seed=seed)
            Q, ret = train(env, method, 500, alpha=0.5, gamma=1.0, eps_schedule=lambda ep: 0.1, seed=seed)
            curves.append(ret)
        curve = np.mean(curves, 0)
        axes[0].plot(np.convolve(curve, np.ones(10) / 10, mode="valid"), label=method)
        print(f"{method:10s} avg return per episode (last 100, while exploring eps=0.1): {curve[-100:].mean():7.1f}")
        print("greedy path learned:\n" + env.show_policy(Q.argmax(1)))
    axes[0].set(xlabel="episode", ylabel="sum of rewards (smoothed)", title="Cliff walking, eps=0.1",
                ylim=(-120, 0)); axes[0].legend()

    # ---------- Part 2: slippery FrozenLake ----------
    fl = GridWorld(FROZEN_LAKE_4x4, slip=2 / 3, max_steps=100, seed=1)
    _, pi_star, _ = value_iteration(fl, gamma=0.99)
    decay = lambda ep: max(0.05, 1.0 - ep / 6000)            # explore a lot early, less later
    Q, ret = train(fl, "Q-learning", 10000, alpha=0.05, gamma=0.99, eps_schedule=decay, seed=1)
    pi_q = Q.argmax(1)
    print(f"\nFrozenLake Q-learning policy:\n{fl.show_policy(pi_q)}")
    print(f"greedy success rate: Q-learning {success_rate(fl, pi_q):.1%} | "
          f"value-iteration optimum {success_rate(fl, pi_star):.1%}")
    axes[1].plot(np.convolve(ret, np.ones(200) / 200, mode="valid"))
    axes[1].set(xlabel="episode", ylabel="success rate (200-ep moving avg)",
                title="Q-learning on slippery FrozenLake (eps decays 1 -> 0.05)")
    fig.tight_layout()
    fig.savefig(IMG / "q_learning_curves.png", dpi=130)
    print("saved img/q_learning_curves.png")


if __name__ == "__main__":
    main()
