"""High-resolution image synthesis: why generate in a compressed space.

1) Sequence-length and attention arithmetic for a 1024x1024 image in pixel space,
   in a latent space (downsampling factor f), and with a two-stage cascade.
2) A toy vector-quantised (VQ) autoencoder: cut images into 4x4 patches, learn a
   codebook with k-means, store each patch as one code index. We measure the
   compression ratio and reconstruction quality (PSNR) as the codebook grows,
   on a held-out image the codebook never saw.
Synthetic images; numpy only.
"""
from __future__ import annotations

import numpy as np

rng = np.random.default_rng(4)


def arithmetic() -> None:
    H = W = 1024
    print("1) How long is the sequence the generator must model? (attention cost ~ tokens^2)")
    rows = [("pixels, 1 token per pixel", H * W, 3),
            ("pixels, 16x16 patches", (H // 16) * (W // 16), 16 * 16 * 3),
            ("latent f=8 (128x128x4), 2x2 patches", (H // 8 // 2) * (W // 8 // 2), 2 * 2 * 4),
            ("latent f=32 (32x32x16), 1 token per position", (H // 32) * (W // 32), 16)]
    base = rows[2][1] ** 2
    print(f"   {'representation':46} {'tokens':>9}  {'values/token':>12}  attention pairs vs f=8")
    for name, n, v in rows:
        print(f"   {name:46} {n:>9,}  {v:>12,}  {n * n / base:>10,.4g}x")
    raw = H * W * 3
    lat = (H // 8) * (W // 8) * 4
    print(f"   values per image: pixels {raw:,} vs f=8 latent {lat:,} -> {raw / lat:.0f}x fewer numbers to denoise")
    b, s = 64 * 64, 256 * 256
    print(f"   cascade: base model at 64x64 ({b:,} px) then super-resolution 64->256->1024;"
          f" the base sees {H * W // b}x fewer pixels than one-shot 1024x1024 generation\n")


def make_image(n: int = 64) -> np.ndarray:
    """Smooth gradient + a few random discs + mild texture, values in [0, 1]."""
    y, x = np.mgrid[0:n, 0:n] / n
    img = 0.3 + 0.4 * (x * rng.uniform(-1, 1) + y * rng.uniform(-1, 1))
    for _ in range(4):
        cx, cy, r = rng.uniform(0, 1, 2).tolist() + [rng.uniform(0.08, 0.2)]
        img = np.where((x - cx) ** 2 + (y - cy) ** 2 < r * r, rng.uniform(0, 1), img)
    return np.clip(img + rng.normal(0, 0.02, img.shape), 0, 1)


def patches(img: np.ndarray, p: int = 4) -> np.ndarray:
    n = img.shape[0] // p
    return img.reshape(n, p, n, p).transpose(0, 2, 1, 3).reshape(n * n, p * p)


def unpatch(ps: np.ndarray, n: int, p: int = 4) -> np.ndarray:
    k = n // p
    return ps.reshape(k, k, p, p).transpose(0, 2, 1, 3).reshape(n, n)


def kmeans(x: np.ndarray, k: int, iters: int = 25) -> np.ndarray:
    c = x[rng.choice(len(x), k, replace=False)]
    for _ in range(iters):
        a = ((x[:, None, :] - c[None]) ** 2).sum(2).argmin(1)
        for j in range(k):
            m = x[a == j]
            if len(m):
                c[j] = m.mean(0)
    return c


def psnr(a: np.ndarray, b: np.ndarray) -> float:
    return 10 * np.log10(1.0 / np.mean((a - b) ** 2))


def vq_demo() -> None:
    train = np.concatenate([patches(make_image()) for _ in range(40)])
    test_img = make_image()
    tp = patches(test_img)
    print("2) Toy VQ autoencoder on 64x64 images (4x4 patches -> one code each; held-out image)")
    print("   codebook | bits/patch | compression vs 8-bit pixels | PSNR dB (higher is better)")
    for k in (4, 16, 64, 256):
        cb = kmeans(train.copy(), k)
        codes = ((tp[:, None, :] - cb[None]) ** 2).sum(2).argmin(1)
        rec = unpatch(cb[codes], 64)
        bits = np.log2(k)
        print(f"   {k:8} | {bits:10.0f} | {16 * 8 / bits:22.0f}x      | {psnr(test_img, rec):6.1f}")
    print("   (the generator then models 256 code indices per image instead of 4,096 pixels)")


if __name__ == "__main__":
    arithmetic()
    vq_demo()
