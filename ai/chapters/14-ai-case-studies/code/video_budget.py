"""Text-to-video: token arithmetic, temporal consistency, and fleet cost.

1) How many latent tokens a short clip becomes, and what full spacetime attention
   costs compared with factorised (spatial-then-temporal) attention.
2) Why frames cannot be generated independently: a moving disc rendered frame by
   frame with independent noise flickers; noise that is shared/correlated across
   frames (what joint denoising with temporal layers achieves) does not.
   Flicker = mean absolute change between consecutive frames after undoing the
   known motion (0 = perfectly stable).
3) GPU fleet arithmetic for an asynchronous clip-generation service.
All numbers are illustrative assumptions; numpy only.
"""
from __future__ import annotations

import numpy as np

rng = np.random.default_rng(8)


def tokens() -> None:
    secs, fps, H, W = 5, 24, 720, 1280
    fs, ft, p = 8, 4, 2                               # spatial /8, temporal /4 compression, 2x2 patches
    frames = secs * fps
    lat_t = frames // ft
    per_frame = (H // fs // p) * (W // fs // p)
    n = lat_t * per_frame
    img = (1024 // 8 // 2) ** 2
    print("1) A 5 s, 24 fps, 1280x720 clip in a video latent space")
    print(f"   {frames} frames -> {lat_t} latent frames x {per_frame:,} tokens/frame = {n:,} tokens"
          f" ({n / img:.0f}x a 1024x1024 image's {img:,})")
    full = n * n
    fact = lat_t * per_frame ** 2 + per_frame * lat_t ** 2
    print(f"   full spacetime attention pairs: {full:,.3e}")
    print(f"   factorised (spatial within frame + temporal per position): {fact:,.3e}  ({full / fact:.0f}x fewer)")
    print(f"   doubling length to 10 s: full x{4:.0f}, factorised x{(2 * lat_t * per_frame ** 2 + per_frame * (2 * lat_t) ** 2) / fact:.1f}\n")


def render(t: int, n: int = 48) -> np.ndarray:
    y, x = np.mgrid[0:n, 0:n]
    cx = 10 + t                                       # disc moves 1 px per frame
    return ((x - cx) ** 2 + (y - 24) ** 2 < 64).astype(float)


def flicker(frames: list[np.ndarray]) -> float:
    diffs = [np.abs(np.roll(a, 1, axis=1) - b)[:, 2:-2].mean() for a, b in zip(frames, frames[1:])]
    return float(np.mean(diffs))


def consistency() -> None:
    T, sigma = 16, 0.25
    clean = [render(t) for t in range(T)]
    indep = [f + sigma * rng.normal(size=f.shape) for f in clean]
    base = sigma * rng.normal(size=clean[0].shape)
    shared = [f + np.roll(base, t, axis=1) for t, f in enumerate(clean)]   # noise moves with the content
    rho = 0.9
    corr, e = [], sigma * rng.normal(size=clean[0].shape)
    for t, f in enumerate(clean):
        if t:
            e = rho * np.roll(e, 1, axis=1) + np.sqrt(1 - rho ** 2) * sigma * rng.normal(size=f.shape)
        corr.append(f + e)
    print("2) Temporal consistency (residual detail is the same size in every case)")
    print(f"   clean frames                         flicker {flicker(clean):.3f}")
    print(f"   independent per-frame generation     flicker {flicker(indep):.3f}")
    print(f"   correlated across frames (rho=0.9)   flicker {flicker(corr):.3f}")
    print(f"   shared, motion-aligned               flicker {flicker(shared):.3f}\n")


def fleet() -> None:
    clips_day, gpu_s_per_clip, price_h = 200_000, 300, 3.0
    busy = clips_day / 86_400 * gpu_s_per_clip
    print("3) Fleet arithmetic (assumed 300 GPU-s per 5 s clip, $3/GPU-h)")
    print(f"   {clips_day:,} clips/day = {clips_day / 86_400:.2f} clips/s x {gpu_s_per_clip} GPU-s"
          f" = {busy:.0f} busy GPUs; ${busy * price_h * 24:,.0f}/day; ${gpu_s_per_clip / 3600 * price_h:.2f} per clip")
    print(f"   a 4-frame low-res preview at ~5% of the cost = ${0.05 * gpu_s_per_clip / 3600 * price_h:.3f};"
          f" if previews cut full renders by 30%, saving ${0.3 * busy * price_h * 24 - 0.05 * busy * price_h * 24:,.0f}/day")


if __name__ == "__main__":
    tokens()
    consistency()
    fleet()
