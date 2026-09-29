"""Monte Carlo vs TD(0) prediction on the classic 5-state random walk (Sutton & Barto, Ex. 6.2).
   [0] A B C D E [+1]   start at C, move left/right with 50 % each. True V = 1/6 ... 5/6."""
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

from gridworld import IMG

TRUE_V = np.arange(1, 6) / 6


def episode(rng):
    s, states, = 2, []
    while 0 <= s <= 4:
        states.append(s)
        s += 1 if rng.random() < 0.5 else -1
    return states, (1.0 if s == 5 else 0.0)        # reward only on the final step


def run(method, alpha, n_episodes, rng, gamma=1.0):
    V = np.full(5, 0.5)
    errs = []
    for _ in range(n_episodes):
        states, final_r = episode(rng)
        if method == "MC":        # wait until the end, then move each V(s) toward the actual return G
            G = final_r
            for s in states:
                V[s] += alpha * (G - V[s])
        else:                     # TD(0): after every step, move V(s) toward r + gamma * V(s')
            for t, s in enumerate(states):
                if t + 1 < len(states):
                    target = 0.0 + gamma * V[states[t + 1]]
                else:
                    target = final_r                   # next state is terminal (value 0)
                V[s] += alpha * (target - V[s])
        errs.append(np.sqrt(np.mean((V - TRUE_V) ** 2)))
    return np.array(errs), V


def main():
    rng = np.random.default_rng(0)
    fig, ax = plt.subplots(figsize=(7, 4))
    for method, alphas, style in (("MC", (0.01, 0.03), "--"), ("TD", (0.05, 0.1), "-")):
        for a in alphas:
            curves = np.mean([run(method, a, 100, rng)[0] for _ in range(100)], axis=0)
            ax.plot(curves, style, label=f"{method} alpha={a}")
            print(f"{method} alpha={a:<5} RMS error after 10 eps {curves[9]:.3f}, after 100 eps {curves[-1]:.3f}")
    _, V = run("TD", 0.05, 300, rng)
    print("TD estimate after 300 episodes:", np.round(V, 2), " true:", np.round(TRUE_V, 2))
    ax.set(xlabel="episodes", ylabel="RMS error of V (avg of 100 runs)", title="Random walk: MC vs TD(0)")
    ax.legend(); fig.tight_layout()
    fig.savefig(IMG / "mc_vs_td.png", dpi=130)
    print("saved img/mc_vs_td.png")


if __name__ == "__main__":
    main()
