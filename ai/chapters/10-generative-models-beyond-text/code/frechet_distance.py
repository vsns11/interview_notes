"""The Frechet distance behind FID, on toy feature vectors.

FID = ||mu_r - mu_g||^2 + Tr(S_r + S_g - 2 (S_r S_g)^(1/2))
where mu, S are the mean and covariance of FEATURES of real (r) and generated
(g) images. Real FID uses 2048-d Inception-v3 features of tens of thousands of
images; here the "features" are simply 2-D points from the 8-Gaussians ring so
you can see what each kind of failure does to the score. Lower is better.

Run:  python code/frechet_distance.py
"""
import numpy as np
from scipy import linalg

rng = np.random.default_rng(0)
CENTERS = np.array([[np.cos(a), np.sin(a)] for a in np.arange(8) * np.pi / 4]) * 2


def ring(n, modes=range(8), sigma=0.05):
    idx = rng.choice(list(modes), n)
    return CENTERS[idx] + sigma * rng.standard_normal((n, 2))


def frechet(a: np.ndarray, b: np.ndarray) -> float:
    mu1, mu2 = a.mean(0), b.mean(0)
    s1, s2 = np.cov(a, rowvar=False), np.cov(b, rowvar=False)
    covmean = linalg.sqrtm(s1 @ s2)
    if np.iscomplexobj(covmean):          # tiny imaginary parts from numerics
        covmean = covmean.real
    return float(((mu1 - mu2) ** 2).sum() + np.trace(s1 + s2 - 2 * covmean))


if __name__ == "__main__":
    real = ring(5000)
    cases = {
        "another real sample (ideal model)": ring(5000),
        "blurry: right modes, extra noise": ring(5000, sigma=0.4),
        "mode collapse: only 2 of 8 modes": ring(5000, modes=[0, 4]),
        "mode collapse: only 1 of 8 modes": ring(5000, modes=[2]),
        "shifted: everything moved +0.5": ring(5000) + 0.5,
    }
    for name, fake in cases.items():
        print(f"{name:36s} FD = {frechet(real, fake):6.3f}")
    small = frechet(real[:50], ring(50))
    print(f"\nideal model but only 50 samples      FD = {small:6.3f}  (small n biases FID upward)")
