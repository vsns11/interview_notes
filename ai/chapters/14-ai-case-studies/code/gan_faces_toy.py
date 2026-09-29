"""Face-generation GAN in miniature: train a tiny GAN on an 8-mode ring (each mode
stands for a "kind of face"), watch coverage and Frechet distance during training,
then apply the truncation trick used by BigGAN/StyleGAN at sampling time.

Metrics:
  modes covered  - how many of the 8 modes receive >= 1% of samples (diversity)
  high quality   - share of samples within 3 std of some mode centre (fidelity)
  FD             - Frechet distance between real and generated 2-D samples
                   (the FID formula without the Inception network)
CPU only, fixed seeds, ~10 s.
"""
from __future__ import annotations

import numpy as np
import torch
from scipy import linalg
from torch import nn

torch.manual_seed(0)
torch.set_num_threads(1)
rng = np.random.default_rng(0)

K, R, STD = 8, 2.0, 0.05
CENTRES = np.stack([[R * np.cos(2 * np.pi * k / K), R * np.sin(2 * np.pi * k / K)] for k in range(K)])


def real(n: int) -> np.ndarray:
    return CENTRES[rng.integers(0, K, n)] + rng.normal(0, STD, (n, 2))


def metrics(x: np.ndarray, ref: np.ndarray) -> tuple[int, float, float]:
    d = np.linalg.norm(x[:, None, :] - CENTRES[None], axis=2)
    nearest, dist = d.argmin(1), d.min(1)
    good = dist < 3 * STD
    counts = np.bincount(nearest[good], minlength=K)
    covered = int((counts >= 0.01 * len(x)).sum())
    mu1, mu2 = x.mean(0), ref.mean(0)
    s1, s2 = np.cov(x.T), np.cov(ref.T)
    fd = float(((mu1 - mu2) ** 2).sum() + np.trace(s1 + s2 - 2 * linalg.sqrtm(s1 @ s2).real))
    return covered, float(good.mean()), fd


def mlp(i: int, o: int) -> nn.Sequential:
    return nn.Sequential(nn.Linear(i, 128), nn.ReLU(), nn.Linear(128, 128), nn.ReLU(), nn.Linear(128, o))


def main() -> None:
    G, D = mlp(8, 2), mlp(2, 1)                       # generator: z(8) -> point; discriminator: point -> logit
    og = torch.optim.Adam(G.parameters(), 1e-3, betas=(0.5, 0.999))
    od = torch.optim.Adam(D.parameters(), 1e-3, betas=(0.5, 0.999))
    bce = nn.BCEWithLogitsLoss()
    ref = real(5000)
    print("step  | D loss | G loss | modes covered | high quality | FD (lower is better)")
    for step in range(1, 4001):
        x = torch.tensor(real(256), dtype=torch.float32)
        z = torch.randn(256, 8)
        fake = G(z)
        ld = bce(D(x), torch.ones(256, 1)) + bce(D(fake.detach()), torch.zeros(256, 1))
        od.zero_grad(); ld.backward(); od.step()
        lg = bce(D(fake), torch.ones(256, 1))        # non-saturating generator loss
        og.zero_grad(); lg.backward(); og.step()
        if step in (250, 1000, 2000, 4000):
            with torch.no_grad():
                s = G(torch.randn(5000, 8)).numpy()
            c, q, fd = metrics(s, ref)
            print(f"{step:5} | {ld.item():6.3f} | {lg.item():6.3f} | {c:>7}/8       | {q:11.1%} | {fd:.4f}")

    print("\nTruncation trick at sampling time: z ~ psi * N(0, I)  (no retraining)")
    print("  psi  | modes covered | high quality | FD")
    with torch.no_grad():
        for psi in (1.0, 0.7, 0.4, 0.1):
            s = G(psi * torch.randn(5000, 8)).numpy()
            c, q, fd = metrics(s, ref)
            print(f"  {psi:3.1f}  | {c:>7}/8       | {q:11.1%} | {fd:.4f}")


if __name__ == "__main__":
    main()
