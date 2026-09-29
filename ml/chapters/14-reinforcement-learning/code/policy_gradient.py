"""Policy-gradient methods on the treasure grid, in numpy, with a tabular softmax policy
pi(a|s) = softmax(theta[s]):  REINFORCE, REINFORCE + baseline, and one-step actor-critic."""
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

from gridworld import IMG, TREASURE_5x5, GridWorld

GAMMA = 0.9


def softmax(x):
    z = np.exp(x - x.max())
    return z / z.sum()


def run(method, episodes=2000, lr=0.01, lr_v=0.1, seed=0):
    rng = np.random.default_rng(seed)
    env = GridWorld(TREASURE_5x5, step_reward=-1, goal_reward=10, trap_reward=-5, max_steps=50, seed=seed)
    theta = np.zeros((env.nS, env.nA))      # policy parameters (the "actor")
    V = np.zeros(env.nS)                    # state-value estimate (baseline / "critic")
    returns = []
    for _ in range(episodes):
        s, done, trunc, traj = env.reset(), False, False, []
        while not (done or trunc):
            p = softmax(theta[s])
            a = rng.choice(4, p=p)
            s2, r, done, trunc = env.step(a)
            traj.append((s, a, r))
            if method == "actor-critic":     # update every step using the TD error
                delta = r + GAMMA * V[s2] * (not done) - V[s]
                V[s] += lr_v * delta
                grad = -p; grad[a] += 1      # d log pi(a|s) / d theta[s]
                theta[s] += lr * delta * grad
            s = s2
        returns.append(sum(r for _, _, r in traj))
        if method.startswith("REINFORCE"):   # Monte Carlo: wait for the episode to end
            G = 0.0
            for t in reversed(range(len(traj))):
                s, a, r = traj[t]
                G = r + GAMMA * G
                adv = G
                if method == "REINFORCE+baseline":
                    V[s] += lr_v * (G - V[s])
                    adv = G - V[s]           # 'was this better than usual from here?'
                grad = -softmax(theta[s]); grad[a] += 1
                theta[s] += lr * adv * grad   # (textbook has an extra gamma**t; practice drops it)
    return np.array(returns)


def main():
    fig, ax = plt.subplots(figsize=(7, 4))
    for method in ("REINFORCE", "REINFORCE+baseline", "actor-critic"):
        curves = np.array([run(method, seed=s) for s in range(5)])
        m = curves.mean(0)
        ax.plot(np.convolve(m, np.ones(25) / 25, mode="valid"), label=method)
        print(f"{method:19s} mean return: episodes 1-100 {m[:100].mean():+6.2f} | last 100 {m[-100:].mean():+5.2f}"
              f" | std across seeds (last 100) {curves[:, -100:].mean(1).std():.2f}")
    ax.axhline(3, color="grey", ls=":", lw=1, label="best possible (+3)")
    ax.set(xlabel="episode", ylabel="return (25-ep moving avg, 5 seeds)", title="Policy gradients on the treasure grid",
           ylim=(-55, 5))
    ax.legend(); fig.tight_layout()
    fig.savefig(IMG / "policy_gradient_curves.png", dpi=130)
    print("saved img/policy_gradient_curves.png")


if __name__ == "__main__":
    main()
