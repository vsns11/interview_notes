"""A tiny class-conditional DDPM on 2-D data, with classifier-free guidance.

Data: "two moons", class 0 = upper moon, class 1 = lower moon.
Model: an MLP that predicts the noise eps from (x_t, t, class).
Training: pick t at random, noise x_0 with the closed form, regress on eps
(MSE). 20% of the time the class is replaced by a NULL token so the same
network also learns the unconditional model (needed for guidance).
Sampling: start from N(0, I) and run the reverse DDPM update T times.
Guidance: eps = eps_uncond + s * (eps_cond - eps_uncond).

Runs in about 15-25 s on a laptop CPU.
Run:  python code/toy_diffusion_2d.py
"""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import torch
import torch.nn as nn
from sklearn.datasets import make_moons

IMG = Path(__file__).resolve().parent.parent / "img"
torch.manual_seed(0)
torch.set_num_threads(1)          # tiny model: threads cost more than they save
np.random.seed(0)

T = 100
betas = torch.linspace(1e-3, 0.1, T)          # short schedule for a toy problem
alphas = 1 - betas
abar = torch.cumprod(alphas, 0)
NULL = 2                                       # the "no class" token


def time_embedding(t: torch.Tensor, dim: int = 16) -> torch.Tensor:
    """Sinusoidal embedding of the timestep (same idea as transformer positions)."""
    freqs = torch.exp(torch.arange(dim // 2) * (-np.log(1000.0) / (dim // 2)))
    ang = t.float()[:, None] * freqs[None]
    return torch.cat([ang.sin(), ang.cos()], dim=1)


class EpsNet(nn.Module):
    def __init__(self, hidden: int = 256):
        super().__init__()
        self.cls = nn.Embedding(3, 16)          # classes 0, 1 and NULL
        self.net = nn.Sequential(
            nn.Linear(2 + 16 + 16, hidden), nn.SiLU(),
            nn.Linear(hidden, hidden), nn.SiLU(),
            nn.Linear(hidden, hidden), nn.SiLU(),
            nn.Linear(hidden, 2))

    def forward(self, x, t, y):
        return self.net(torch.cat([x, time_embedding(t), self.cls(y)], dim=1))


def train(model, x0, y0, steps=6000, batch=512, p_uncond=0.2):
    opt = torch.optim.Adam(model.parameters(), lr=2e-3)
    sched = torch.optim.lr_scheduler.CosineAnnealingLR(opt, steps)
    for step in range(steps):
        idx = torch.randint(0, len(x0), (batch,))
        x, y = x0[idx], y0[idx].clone()
        y[torch.rand(batch) < p_uncond] = NULL          # label dropout for CFG
        t = torch.randint(0, T, (batch,))
        eps = torch.randn_like(x)
        a = abar[t][:, None]
        xt = a.sqrt() * x + (1 - a).sqrt() * eps        # closed-form forward jump
        loss = ((model(xt, t, y) - eps) ** 2).mean()    # "simple" DDPM loss
        opt.zero_grad(); loss.backward(); opt.step(); sched.step()
        if step % 1000 == 0 or step == steps - 1:
            print(f"  step {step:4d}  loss {loss.item():.4f}")


@torch.no_grad()
def sample(model, n, cls=None, guidance=1.0, keep=()):
    """Reverse process. cls=None -> unconditional. guidance = s."""
    x = torch.randn(n, 2)
    snaps = {}
    for t in reversed(range(T)):
        tt = torch.full((n,), t)
        e_u = model(x, tt, torch.full((n,), NULL))
        if cls is None:
            eps = e_u
        else:
            e_c = model(x, tt, torch.full((n,), cls))
            eps = e_u + guidance * (e_c - e_u)          # classifier-free guidance
        mean = (x - betas[t] / (1 - abar[t]).sqrt() * eps) / alphas[t].sqrt()
        x = mean + (betas[t].sqrt() * torch.randn_like(x) if t > 0 else 0)
        if t in keep:
            snaps[t] = x.clone()
    return x, snaps


def on_class(points, data, labels, cls):
    """Share of samples whose nearest real point belongs to class `cls`."""
    d = torch.cdist(points, data)
    return (labels[d.argmin(1)] == cls).float().mean().item()


def off_manifold(points, data, radius=0.25):
    """Share of samples farther than `radius` from every real point."""
    return (torch.cdist(points, data).min(1).values > radius).float().mean().item()


if __name__ == "__main__":
    X, Y = make_moons(4000, noise=0.05, random_state=0)
    X = (X - X.mean(0)) / X.std(0)
    x0 = torch.tensor(X, dtype=torch.float32)
    y0 = torch.tensor(Y)
    model = EpsNet()
    print("Training the noise-prediction network:")
    train(model, x0, y0)

    uncond, snaps = sample(model, 2000, keep=(99, 60, 30, 10, 0))
    print(f"\nUnconditional: {on_class(uncond, x0, y0, 0):.0%} near moon 0, "
          f"off-manifold {off_manifold(uncond, x0):.1%}")
    results = {}
    for s in (0.0, 1.0, 3.0):
        torch.manual_seed(1)
        pts, _ = sample(model, 2000, cls=0, guidance=s)
        results[s] = pts
        print(f"Class 0, guidance s={s}: {on_class(pts, x0, y0, 0):.1%} on class 0, "
              f"off-manifold {off_manifold(pts, x0):.1%}, spread (std) {pts.std(0).mean():.3f}")

    fig, axes = plt.subplots(1, 5, figsize=(16, 3.2))
    for ax, t in zip(axes, (99, 60, 30, 10, 0)):
        p = snaps[t]
        ax.scatter(p[:, 0], p[:, 1], s=2, alpha=0.5)
        ax.set_title(f"reverse step t = {t}", fontsize=9)
        ax.set_xlim(-3, 3); ax.set_ylim(-3, 3); ax.set_xticks([]); ax.set_yticks([])
    fig.suptitle("Reverse process: noise -> data (unconditional samples)")
    fig.tight_layout()
    IMG.mkdir(exist_ok=True)
    fig.savefig(IMG / "reverse_denoising.png", dpi=120)

    fig, axes = plt.subplots(1, 4, figsize=(13, 3.3))
    axes[0].scatter(X[:, 0], X[:, 1], c=Y, s=2, cmap="coolwarm")
    axes[0].set_title("training data (2 classes)")
    for ax, s in zip(axes[1:], (0.0, 1.0, 3.0)):
        p = results[s]
        ax.scatter(X[:, 0], X[:, 1], s=1, c="lightgrey")
        ax.scatter(p[:, 0], p[:, 1], s=2, c="tab:blue")
        ax.set_title(f"asked for class 0, s = {s}", fontsize=9)
    for ax in axes:
        ax.set_xlim(-3, 3); ax.set_ylim(-3, 3); ax.set_xticks([]); ax.set_yticks([])
    fig.suptitle("Classifier-free guidance: s=0 ignores the label, larger s follows it harder")
    fig.tight_layout()
    fig.savefig(IMG / "cfg_guidance.png", dpi=120)
    print("saved img/reverse_denoising.png and img/cfg_guidance.png")
