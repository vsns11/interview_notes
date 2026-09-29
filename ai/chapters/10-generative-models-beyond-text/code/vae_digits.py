"""A variational autoencoder (VAE) with a 2-D latent space on sklearn digits.

Encoder: image (8x8 = 64 pixels) -> mean mu and log-variance of q(z|x).
Reparameterisation: z = mu + sigma * eps, eps ~ N(0, I)  (keeps it differentiable).
Decoder: z -> 64 pixel probabilities.
Loss (negative ELBO) = reconstruction (binary cross-entropy)
                     + KL( q(z|x) || N(0, I) ) = -1/2 * sum(1 + logvar - mu^2 - exp(logvar)).

Because the prior is N(0, I), we can GENERATE new digits by sampling z ~ N(0, I)
and decoding. With a 2-D latent we can also draw the whole latent space.

Run:  python code/vae_digits.py   (~10 s on CPU)
"""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import torch
import torch.nn as nn
import torch.nn.functional as F
from sklearn.datasets import load_digits

IMG = Path(__file__).resolve().parent.parent / "img"
torch.manual_seed(0)
torch.set_num_threads(1)


class VAE(nn.Module):
    def __init__(self, d_in=64, hidden=128, d_z=2):
        super().__init__()
        self.enc = nn.Sequential(nn.Linear(d_in, hidden), nn.ReLU(),
                                 nn.Linear(hidden, hidden), nn.ReLU())
        self.mu = nn.Linear(hidden, d_z)
        self.logvar = nn.Linear(hidden, d_z)
        self.dec = nn.Sequential(nn.Linear(d_z, hidden), nn.ReLU(),
                                 nn.Linear(hidden, hidden), nn.ReLU(),
                                 nn.Linear(hidden, d_in))      # logits

    def encode(self, x):
        h = self.enc(x)
        return self.mu(h), self.logvar(h)

    def forward(self, x):
        mu, logvar = self.encode(x)
        z = mu + torch.exp(0.5 * logvar) * torch.randn_like(mu)   # reparameterisation
        return self.dec(z), mu, logvar


def loss_fn(logits, x, mu, logvar):
    recon = F.binary_cross_entropy_with_logits(logits, x, reduction="sum") / len(x)
    kl = (-0.5 * (1 + logvar - mu ** 2 - logvar.exp()).sum(1)).mean()
    return recon + kl, recon, kl


if __name__ == "__main__":
    digits = load_digits()
    X = torch.tensor(digits.data / 16.0, dtype=torch.float32)   # pixels in [0, 1]
    y = digits.target
    model = VAE()
    opt = torch.optim.Adam(model.parameters(), lr=1e-3)
    for epoch in range(1, 151):
        perm = torch.randperm(len(X))
        for i in range(0, len(X), 128):
            xb = X[perm[i:i + 128]]
            logits, mu, logvar = model(xb)
            loss, rec, kl = loss_fn(logits, xb, mu, logvar)
            opt.zero_grad(); loss.backward(); opt.step()
        if epoch in (1, 10, 50, 150):
            with torch.no_grad():
                logits, mu, logvar = model(X)
                loss, rec, kl = loss_fn(logits, X, mu, logvar)
            print(f"epoch {epoch:3d}: loss {loss:.2f} = reconstruction {rec:.2f} + KL {kl:.2f}")

    with torch.no_grad():
        mu, _ = model.encode(X)
        mu = mu.numpy()
        # 1) latent space coloured by the true digit (labels were never used in training)
        fig, axes = plt.subplots(1, 2, figsize=(12, 5.5))
        sc = axes[0].scatter(mu[:, 0], mu[:, 1], c=y, cmap="tab10", s=6)
        fig.colorbar(sc, ax=axes[0], ticks=range(10))
        axes[0].set_title("Encoder means mu(x), coloured by digit")
        axes[0].set_xlabel("z1"); axes[0].set_ylabel("z2")
        # 2) decode a grid of z values -> the learned "manifold" of digits
        n, lim, cell = 10, 2.0, 10            # 8x8 digit + 2-pixel white gap
        grid = np.linspace(-lim, lim, n)
        canvas = np.zeros((cell * n, cell * n))
        for r, z2 in enumerate(grid[::-1]):
            for c, z1 in enumerate(grid):
                img = torch.sigmoid(model.dec(torch.tensor([[z1, z2]], dtype=torch.float32)))
                canvas[r * cell + 1:r * cell + 9, c * cell + 1:c * cell + 9] = img.view(8, 8).numpy()
        axes[1].imshow(canvas, cmap="gray_r", extent=[-lim, lim, -lim, lim],
                       interpolation="nearest")
        axes[1].set_title("Decoder output for a grid of z values")
        axes[1].set_xlabel("z1"); axes[1].set_ylabel("z2")
        fig.tight_layout()
        IMG.mkdir(exist_ok=True)
        fig.savefig(IMG / "vae_latent_space.png", dpi=110)

        # 3) brand-new digits: z ~ N(0, I)
        z = torch.randn(10, 2)
        new = torch.sigmoid(model.dec(z)).view(10, 8, 8).numpy()
        fig, axes = plt.subplots(1, 10, figsize=(10, 1.4))
        for ax, im in zip(axes, new):
            ax.imshow(im, cmap="gray_r"); ax.axis("off")
        fig.suptitle("Samples: decode z ~ N(0, I)")
        fig.savefig(IMG / "vae_samples.png", dpi=110, bbox_inches="tight")
    print(f"latent means: mean {mu.mean(0).round(2)}, std {mu.std(0).round(2)} (prior is 0, 1)")
    print("saved img/vae_latent_space.png and img/vae_samples.png")
