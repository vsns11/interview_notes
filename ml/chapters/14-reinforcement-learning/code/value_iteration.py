"""Dynamic programming when the model is known: value iteration and policy iteration."""
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

from gridworld import FROZEN_LAKE_4x4, IMG, TREASURE_5x5, GridWorld


def q_from_v(env, V, s, gamma):
    """One-step look-ahead: Q(s,a) = sum_s' P(s'|s,a) * [r + gamma * V(s') * (not done)]."""
    return np.array([sum(p * (r + gamma * V[s2] * (not done)) for p, s2, r, done in env.transitions(s, a))
                     for a in range(env.nA)])


def value_iteration(env, gamma, tol=1e-8):
    V, deltas = np.zeros(env.nS), []
    while True:
        delta = 0.0
        for s in range(env.nS):
            if env.is_terminal(s):
                continue
            best = q_from_v(env, V, s, gamma).max()      # Bellman OPTIMALITY backup
            delta = max(delta, abs(best - V[s]))
            V[s] = best
        deltas.append(delta)
        if delta < tol:
            break
    policy = np.array([np.argmax(q_from_v(env, V, s, gamma)) for s in range(env.nS)])
    return V, policy, deltas


def policy_evaluation(env, policy, gamma, tol=1e-8):
    V = np.zeros(env.nS)
    while True:
        delta = 0.0
        for s in range(env.nS):
            if env.is_terminal(s):
                continue
            v = q_from_v(env, V, s, gamma)[policy[s]]    # Bellman EXPECTATION backup
            delta, V[s] = max(delta, abs(v - V[s])), v
        if delta < tol:
            return V


def policy_iteration(env, gamma):
    policy = np.zeros(env.nS, dtype=int)                  # start: always go left
    for it in range(1, 100):
        V = policy_evaluation(env, policy, gamma)
        new = np.array([np.argmax(q_from_v(env, V, s, gamma)) for s in range(env.nS)])
        if np.array_equal(new, policy):
            return V, policy, it
        policy = new


def success_rate(env, policy, episodes=5000):
    wins = 0
    for _ in range(episodes):
        s, done, trunc = env.reset(), False, False
        while not (done or trunc):
            s, r, done, trunc = env.step(policy[s])
        wins += r > 0
    return wins / episodes


def main():
    tg = GridWorld(TREASURE_5x5, step_reward=-1, goal_reward=10, trap_reward=-5)
    V, pi, deltas_tg = value_iteration(tg, gamma=0.9)
    print("== Treasure grid (deterministic), gamma = 0.9 ==")
    print(f"value iteration converged in {len(deltas_tg)} sweeps")
    print("V*:\n" + tg.show_values(V))
    print("greedy policy:\n" + tg.show_policy(pi))

    fl = GridWorld(FROZEN_LAKE_4x4, slip=2 / 3, max_steps=100)
    V, pi, deltas_fl = value_iteration(fl, gamma=0.99)
    print("\n== FrozenLake 4x4, slippery (2/3 of moves slide sideways), gamma = 0.99 ==")
    print(f"value iteration converged in {len(deltas_fl)} sweeps")
    print("V*:\n" + fl.show_values(V))
    print("policy:\n" + fl.show_policy(pi))
    V2, pi2, its = policy_iteration(fl, gamma=0.99)
    print(f"policy iteration: {its} improvement rounds, same policy as VI: {np.array_equal(pi, pi2)}")
    print(f"simulated success rate of this policy: {success_rate(fl, pi):.1%}")
    rand = np.random.default_rng(0).integers(0, 4, fl.nS)
    print(f"a random fixed policy succeeds:         {success_rate(fl, rand):.1%}")

    fig, ax = plt.subplots(figsize=(6, 3.6))
    ax.semilogy(deltas_tg, label="treasure grid, gamma=0.9")
    ax.semilogy(deltas_fl, label="FrozenLake slippery, gamma=0.99")
    ax.set(xlabel="sweep", ylabel="largest change in V (log)", title="Value iteration converges")
    ax.legend(); fig.tight_layout()
    fig.savefig(IMG / "value_iteration_convergence.png", dpi=130)
    print("saved img/value_iteration_convergence.png")


if __name__ == "__main__":
    main()
