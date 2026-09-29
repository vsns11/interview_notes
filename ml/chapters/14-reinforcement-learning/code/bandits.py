"""Multi-armed bandit: greedy vs epsilon-greedy vs UCB1 vs Thompson sampling.
10 'slot machines' (e.g. 10 ad banners) with unknown click probabilities."""
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

from gridworld import IMG

N_ARMS, STEPS, RUNS = 10, 3000, 200


def run(policy, true_p, rng, eps=0.1):
    n = np.zeros(N_ARMS)            # pulls per arm
    q = np.zeros(N_ARMS)            # estimated value (mean reward) per arm
    wins, losses = np.ones(N_ARMS), np.ones(N_ARMS)   # Beta(1,1) prior for Thompson
    rewards, optimal = np.zeros(STEPS), np.zeros(STEPS)
    best = np.argmax(true_p)
    for t in range(STEPS):
        if policy == "greedy":
            a = int(np.argmax(q)) if t >= N_ARMS else t          # try each arm once, then exploit
        elif policy == "eps-greedy":
            a = int(rng.integers(N_ARMS)) if rng.random() < eps else int(np.argmax(q))
        elif policy.startswith("UCB"):
            c = np.sqrt(2) if policy == "UCB1" else 0.3               # UCB1 = theory's c = sqrt(2)
            if t < N_ARMS:
                a = t
            else:                                                # optimism bonus shrinks as n grows
                a = int(np.argmax(q + c * np.sqrt(np.log(t + 1) / n)))
        elif policy == "Thompson":
            a = int(np.argmax(rng.beta(wins, losses)))            # sample a belief, act on it
        r = float(rng.random() < true_p[a])                       # click (1) or no click (0)
        n[a] += 1
        q[a] += (r - q[a]) / n[a]                                 # incremental mean
        wins[a] += r; losses[a] += 1 - r
        rewards[t], optimal[t] = r, a == best
    return rewards, optimal


def main():
    rng = np.random.default_rng(0)
    policies = ["greedy", "eps-greedy", "UCB1", "UCB c=0.3", "Thompson"]
    avg_r = {p: np.zeros(STEPS) for p in policies}
    avg_opt = {p: np.zeros(STEPS) for p in policies}
    regret = {p: 0.0 for p in policies}
    for _ in range(RUNS):
        true_p = rng.uniform(0.05, 0.35, N_ARMS)                 # conversion rates 5 % - 35 %
        for p in policies:
            r, o = run(p, true_p, rng)
            avg_r[p] += r / RUNS
            avg_opt[p] += o / RUNS
            regret[p] += (true_p.max() * STEPS - r.sum()) / RUNS
    print(f"{'policy':11s} avg reward  % optimal arm (last 500)  total regret")
    for p in policies:
        print(f"{p:11s} {avg_r[p].mean():.4f}      {avg_opt[p][-500:].mean():6.1%}               {regret[p]:6.1f}")

    fig, axes = plt.subplots(1, 2, figsize=(10, 4))
    for p in policies:
        axes[0].plot(np.cumsum(avg_r[p]) / np.arange(1, STEPS + 1), label=p)
        axes[1].plot(np.convolve(avg_opt[p], np.ones(50) / 50, mode="valid"), label=p)
    axes[0].set(xlabel="step", ylabel="average reward so far", title="Average reward (conversion rate)")
    axes[1].set(xlabel="step", ylabel="% picks = best arm", title="How often the best arm is chosen")
    axes[1].legend(); fig.tight_layout()
    fig.savefig(IMG / "bandits.png", dpi=130)
    print("saved img/bandits.png")


if __name__ == "__main__":
    main()
