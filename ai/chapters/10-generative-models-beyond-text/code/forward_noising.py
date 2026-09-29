"""The forward (noising) process of a diffusion model, in numpy.

q(x_t | x_0) = N( sqrt(abar_t) * x_0 , (1 - abar_t) * I )
with alpha_t = 1 - beta_t and abar_t = alpha_1 * alpha_2 * ... * alpha_t.

We (1) noise 2-D "two moons" data at several timesteps with the closed form,
(2) check that the closed form matches applying 1 small step at a time, and
(3) compare the linear (DDPM) and cosine (Improved DDPM) schedules.

Run:  python code/forward_noising.py
"""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from sklearn.datasets import make_moons

IMG = Path(__file__).resolve().parent.parent / "img"
rng = np.random.default_rng(0)

T = 1000
betas = np.linspace(1e-4, 0.02, T)            # DDPM (Ho et al. 2020) linear schedule
alphas = 1.0 - betas
abar = np.cumprod(alphas)                      # abar[t-1] is abar_t


def cosine_abar(T: int, s: float = 0.008) -> np.ndarray:
    """Nichol & Dhariwal (2021) cosine schedule."""
    t = np.arange(T + 1) / T
    f = np.cos((t + s) / (1 + s) * np.pi / 2) ** 2
    return (f / f[0])[1:]


def q_sample(x0: np.ndarray, t: int) -> np.ndarray:
    """Jump straight to step t (1-based) in one shot."""
    eps = rng.standard_normal(x0.shape)
    return np.sqrt(abar[t - 1]) * x0 + np.sqrt(1 - abar[t - 1]) * eps


if __name__ == "__main__":
    x0, _ = make_moons(2000, noise=0.05, random_state=0)
    x0 = (x0 - x0.mean(0)) / x0.std(0)         # standardise: mean 0, std 1

    print(" t    abar_t   signal coef sqrt(abar)   noise std sqrt(1-abar)")
    for t in (1, 50, 150, 300, 600, 1000):
        print(f"{t:4d}  {abar[t-1]:.4f}   {np.sqrt(abar[t-1]):.4f}"
              f"                   {np.sqrt(1 - abar[t-1]):.4f}")

    # closed form vs. stepping one beta at a time up to t = 300
    x = x0.copy()
    for t in range(1, 301):
        x = np.sqrt(alphas[t - 1]) * x + np.sqrt(betas[t - 1]) * rng.standard_normal(x.shape)
    jump = q_sample(x0, 300)
    print(f"\nstep-by-step to t=300: mean {x.mean():+.3f}, std {x.std():.3f}")
    print(f"closed-form jump t=300: mean {jump.mean():+.3f}, std {jump.std():.3f}")

    ts = (0, 50, 150, 300, 600, 1000)
    fig, axes = plt.subplots(1, len(ts), figsize=(15, 2.8))
    for ax, t in zip(axes, ts):
        pts = x0 if t == 0 else q_sample(x0, t)
        ax.scatter(pts[:, 0], pts[:, 1], s=2, alpha=0.5)
        ax.set_title(f"t = {t}" + ("" if t == 0 else f"\nabar={abar[t-1]:.3f}"), fontsize=9)
        ax.set_xlim(-4, 4); ax.set_ylim(-4, 4); ax.set_xticks([]); ax.set_yticks([])
    fig.suptitle("Forward process: data -> pure Gaussian noise (linear schedule, T=1000)")
    fig.tight_layout()
    IMG.mkdir(exist_ok=True)
    fig.savefig(IMG / "forward_noising.png", dpi=120)

    fig, ax = plt.subplots(figsize=(6, 3.5))
    ax.plot(abar, label="linear (DDPM)")
    ax.plot(cosine_abar(T), label="cosine (Improved DDPM)")
    ax.set_xlabel("timestep t"); ax.set_ylabel("abar_t  (share of signal variance)")
    ax.set_title("How fast each schedule destroys the signal")
    ax.legend(); ax.grid(alpha=0.3)
    fig.tight_layout()
    fig.savefig(IMG / "noise_schedules.png", dpi=120)
    half_lin = int(np.argmax(abar < 0.5)) + 1
    half_cos = int(np.argmax(cosine_abar(T) < 0.5)) + 1
    print(f"\nabar drops below 0.5 at t={half_lin} (linear) vs t={half_cos} (cosine)")
    print("saved img/forward_noising.png and img/noise_schedules.png")
