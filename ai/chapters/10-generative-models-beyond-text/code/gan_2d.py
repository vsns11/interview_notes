"""A tiny GAN on a ring of 8 Gaussians, measuring mode coverage.

Generator G: noise z (2-D) -> fake point.  Discriminator D: point -> P(real).
Each step: (1) update D to tell real from fake (binary cross-entropy),
(2) update G to make D say "real" on fakes (the non-saturating loss -log D(G(z))).

The 8 blobs are the "modes". A healthy GAN covers all 8; MODE COLLAPSE is when
G keeps producing only a few of them because that already fools D.
We train twice: a balanced setup, and one where G gets 5 updates per D update
with a high learning rate (an easy way to provoke collapse on this toy).

Run:  python code/gan_2d.py   (~15 s on CPU)
"""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import torch
import torch.nn as nn

IMG = Path(__file__).resolve().parent.parent / "img"
torch.set_num_threads(1)
CENTERS = torch.tensor([[np.cos(a), np.sin(a)] for a in np.arange(8) * np.pi / 4],
                       dtype=torch.float32) * 2.0
SIGMA = 0.05


def real_batch(n):
    idx = torch.randint(0, 8, (n,))
    return CENTERS[idx] + SIGMA * torch.randn(n, 2)


def mlp(d_in, d_out):
    return nn.Sequential(nn.Linear(d_in, 128), nn.LeakyReLU(0.2),
                         nn.Linear(128, 128), nn.LeakyReLU(0.2),
                         nn.Linear(128, d_out))


def coverage(fake):
    """Modes with >= 2% of samples within 3 sigma, and share of 'good' samples."""
    d = torch.cdist(fake, CENTERS)
    near = d.min(1).values < 3 * SIGMA
    counts = torch.bincount(d.argmin(1)[near], minlength=8)
    return int((counts >= 0.02 * len(fake)).sum()), near.float().mean().item()


def train(seed, steps, g_steps, lr_g, snaps):
    torch.manual_seed(seed)
    G, D = mlp(2, 2), mlp(2, 1)
    opt_g = torch.optim.Adam(G.parameters(), lr=lr_g, betas=(0.5, 0.999))
    opt_d = torch.optim.Adam(D.parameters(), lr=1e-3, betas=(0.5, 0.999))
    bce = nn.BCEWithLogitsLoss()
    out = {}
    for step in range(1, steps + 1):
        real, fake = real_batch(256), G(torch.randn(256, 2)).detach()
        d_loss = bce(D(real), torch.ones(256, 1)) + bce(D(fake), torch.zeros(256, 1))
        opt_d.zero_grad(); d_loss.backward(); opt_d.step()
        for _ in range(g_steps):
            g_loss = bce(D(G(torch.randn(256, 2))), torch.ones(256, 1))   # non-saturating
            opt_g.zero_grad(); g_loss.backward(); opt_g.step()
        if step in snaps:
            with torch.no_grad():
                out[step] = G(torch.randn(2000, 2))
    return out


if __name__ == "__main__":
    snaps = (200, 1000, 3000)
    runs = {"balanced (1 G step, lr_G=1e-3)": train(0, 3000, 1, 1e-3, snaps),
            "unbalanced (5 G steps, lr_G=5e-3)": train(0, 3000, 5, 5e-3, snaps)}
    fig, axes = plt.subplots(2, 3, figsize=(11, 7.4))
    ref = real_batch(2000)
    for row, (name, out) in zip(axes, runs.items()):
        print(name)
        for ax, step in zip(row, snaps):
            modes, good = coverage(out[step])
            print(f"   step {step:4d}: modes covered {modes}/8, samples on a mode {good:.0%}")
            ax.scatter(ref[:, 0], ref[:, 1], s=1, c="lightgrey")
            ax.scatter(out[step][:, 0], out[step][:, 1], s=2, alpha=0.5)
            ax.set_title(f"{name}\nstep {step}: {modes}/8 modes", fontsize=8)
            ax.set_xlim(-3, 3); ax.set_ylim(-3, 3); ax.set_xticks([]); ax.set_yticks([])
    fig.suptitle("GAN on 8 Gaussians (grey = real data, blue = generator samples)")
    fig.tight_layout()
    IMG.mkdir(exist_ok=True)
    fig.savefig(IMG / "gan_modes.png", dpi=110)
    print("saved img/gan_modes.png")
