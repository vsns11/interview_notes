"""A minimal DQN (Mnih et al. 2015 ideas) on the treasure grid: a neural net replaces the Q-table.
Key pieces: experience replay buffer + target network + epsilon-greedy exploration."""
import random
from collections import deque

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import torch
import torch.nn as nn

from gridworld import IMG, TREASURE_5x5, GridWorld


def one_hot(s, n):
    x = torch.zeros(n)
    x[s] = 1.0
    return x


def train_dqn(use_replay=True, use_target=True, episodes=300, seed=0):
    torch.manual_seed(seed); random.seed(seed)
    env = GridWorld(TREASURE_5x5, step_reward=-1, goal_reward=10, trap_reward=-5, max_steps=50, seed=seed)
    nS, nA, gamma = env.nS, env.nA, 0.9
    q_net = nn.Sequential(nn.Linear(nS, 64), nn.ReLU(), nn.Linear(64, nA))
    target_net = nn.Sequential(nn.Linear(nS, 64), nn.ReLU(), nn.Linear(64, nA))
    target_net.load_state_dict(q_net.state_dict())
    opt = torch.optim.Adam(q_net.parameters(), lr=1e-3)
    buffer = deque(maxlen=5000)             # experience replay memory
    steps, returns = 0, []
    for ep in range(episodes):
        eps = max(0.05, 1 - ep / 150)
        s, total, done, trunc = env.reset(), 0.0, False, False
        while not (done or trunc):
            if random.random() < eps:
                a = random.randrange(nA)
            else:
                with torch.no_grad():
                    a = int(q_net(one_hot(s, nS)).argmax())
            s2, r, done, trunc = env.step(a)
            buffer.append((s, a, r, s2, done))
            s, total, steps = s2, total + r, steps + 1

            batch = random.sample(buffer, min(64, len(buffer))) if use_replay else [buffer[-1]]
            S = torch.stack([one_hot(b[0], nS) for b in batch])
            A = torch.tensor([b[1] for b in batch])
            R = torch.tensor([b[2] for b in batch], dtype=torch.float32)
            S2 = torch.stack([one_hot(b[3], nS) for b in batch])
            D = torch.tensor([b[4] for b in batch], dtype=torch.float32)
            with torch.no_grad():                       # TD target from the (frozen) target network
                nxt = (target_net if use_target else q_net)(S2).max(1).values
                y = R + gamma * nxt * (1 - D)
            q = q_net(S).gather(1, A[:, None]).squeeze(1)
            loss = nn.functional.smooth_l1_loss(q, y)   # Huber loss, as in the DQN paper
            opt.zero_grad(); loss.backward(); opt.step()
            if use_target and steps % 200 == 0:
                target_net.load_state_dict(q_net.state_dict())   # periodic hard copy
        returns.append(total)
    return np.array(returns), q_net, env


def main():
    fig, ax = plt.subplots(figsize=(7, 4))
    for replay, target in ((True, True), (False, False)):
        runs = [train_dqn(replay, target, seed=s)[0] for s in range(3)]
        curve = np.mean(runs, 0)
        name = "DQN (replay + target net)" if replay else "naive online Q-net (no replay, no target)"
        ax.plot(np.convolve(curve, np.ones(10) / 10, mode="valid"), label=name)
        print(f"{name:42s} mean return, last 50 episodes: {curve[-50:].mean():+.2f}")
    ret, q_net, env = train_dqn(seed=0)
    with torch.no_grad():
        policy = [int(q_net(one_hot(s, env.nS)).argmax()) for s in range(env.nS)]
    print("greedy policy of the DQN (seed 0):\n" + env.show_policy(policy))
    ax.axhline(3, color="grey", lw=1, ls=":", label="best possible return (+3)")
    ax.set(xlabel="episode", ylabel="return (10-ep moving avg, 3 seeds)", title="DQN on the treasure grid")
    ax.legend(fontsize=8); fig.tight_layout()
    fig.savefig(IMG / "dqn_learning_curve.png", dpi=130)
    print("saved img/dqn_learning_curve.png")


if __name__ == "__main__":
    main()
