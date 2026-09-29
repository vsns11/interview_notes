"""Toy privacy blurring for street imagery: the detector is judged on recall, per size, per object, per pixel.

No real images: we simulate what a face/plate detector outputs, so we can study the decisions around it.
  - Objects (faces, plates) have a pixel height; small, far-away ones are much harder to detect.
  - Each physical object is photographed in 3 overlapping frames (the car drives past it).
  - A detection is a box whose corners are off by a few % of the object's size (localisation error).
1) Threshold sweep: recall overall and for tiny objects vs false positives (wasted blur) per image.
2) Object-level recall: a person is exposed if ANY frame misses them -> track and propagate boxes.
3) Box padding: a detected but badly placed box still leaks pixels -> pad the blur region.
"""
import numpy as np

rng = np.random.default_rng(0)
sig = lambda z: 1 / (1 + np.exp(-z))
IMGS, VIEWS = 20000, 3                                          # frames, frames per physical object
W, H = 2048, 1024                                               # frame size in pixels

if __name__ == "__main__":
    # physical objects: ~2 per frame; each appears in 3 consecutive frames at a different distance
    n_obj = 2 * IMGS // VIEWS
    base_h = np.exp(rng.normal(np.log(30), 0.8, n_obj))            # median 30 px tall, long tail of tiny ones
    h = np.clip(base_h[:, None] * rng.uniform(0.6, 1.6, (n_obj, VIEWS)), 6, 400)   # size in each view
    # detector confidence for a true object rises with log(size); plus per-view nuisance (blur, occlusion, angle)
    score = sig(2.2 * (np.log(h) - np.log(14)) + rng.normal(0, 1.2, h.shape))
    # false positives: background patches (posters, wheels) with mostly low confidence
    fp_scores = sig(rng.normal(-3.0, 1.5, IMGS * 4))
    fp_area = np.exp(rng.normal(np.log(40), 0.6, IMGS * 4)) ** 2   # px^2 wasted per false blur

    tiny = h < 16
    print(f"{n_obj:,} physical objects x {VIEWS} views; {tiny.mean():.0%} of views are under 16 px tall\n")
    print(f"{'threshold':>9}{'recall/view':>13}{'recall <16px':>14}{'FP per frame':>14}{'% frame blurred by FP':>23}")
    for t in [0.7, 0.5, 0.3, 0.1, 0.05]:
        det = score >= t
        fp = fp_scores >= t
        print(f"{t:>9}{det.mean():>13.3f}{det[tiny].mean():>14.3f}{fp.sum() / IMGS:>14.2f}"
              f"{fp_area[fp].sum() / IMGS / (W * H):>23.3%}")

    # 2) object level: exposed if missed in ANY view. Tracking: a box found in one view is propagated to the
    #    neighbouring views (known camera motion + a tracker), succeeding 90% of the time.
    t = 0.1
    det = score >= t
    all_views = det.all(axis=1)
    tracked = det | (det.any(axis=1, keepdims=True) & (rng.random(det.shape) < 0.9))
    print(f"\nthreshold {t}: per-view recall {det.mean():.3f}")
    print(f"  objects blurred in ALL {VIEWS} views, per-frame detection only : {all_views.mean():.3f}")
    print(f"  ... with box propagation between neighbouring views       : {tracked.all(axis=1).mean():.3f}")
    print(f"  objects never detected in any view (tracking cannot help) : {(~det.any(axis=1)).mean():.3f}")

    # 3) localisation: predicted box corners are off by N(0, 8% of size); blur = predicted box padded by p%
    hs = h[det]
    true = np.stack([np.zeros_like(hs), np.zeros_like(hs), 0.75 * hs, hs], 1)         # x1, y1, x2, y2 (face ~3:4)
    pred = true + rng.normal(0, 0.08, true.shape) * hs[:, None]
    print("\nbox padding vs leaked pixels (detected objects only)")
    print(f"{'padding':>8}{'objects with >5% of pixels unblurred':>40}{'blur area vs true box':>24}")
    for pad in [0.0, 0.1, 0.2, 0.3]:
        w_, h_ = pred[:, 2] - pred[:, 0], pred[:, 3] - pred[:, 1]
        bx = pred + np.stack([-pad * w_, -pad * h_, pad * w_, pad * h_], 1)
        iw = np.clip(np.minimum(bx[:, 2], true[:, 2]) - np.maximum(bx[:, 0], true[:, 0]), 0, None)
        ih = np.clip(np.minimum(bx[:, 3], true[:, 3]) - np.maximum(bx[:, 1], true[:, 1]), 0, None)
        covered = iw * ih / (0.75 * hs * hs)
        area = (bx[:, 2] - bx[:, 0]) * (bx[:, 3] - bx[:, 1]) / (0.75 * hs * hs)
        print(f"{pad:>8.0%}{(covered < 0.95).mean():>40.1%}{np.median(area):>23.2f}x")
