"""Input-drift monitoring: PSI and the two-sample KS test, plus null-rate checks.

PSI = sum over bins of (cur% - ref%) * ln(cur% / ref%), bins = quantiles of the reference.
Common rule of thumb (credit-scoring convention, not a law): < 0.1 stable, 0.1-0.25 watch, > 0.25 investigate.
"""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from scipy.stats import ks_2samp

IMG = Path(__file__).resolve().parent.parent / "img"
rng = np.random.default_rng(0)


def psi(reference, current, bins=10, eps=1e-6):
    ref, cur = reference[~np.isnan(reference)], current[~np.isnan(current)]
    edges = np.quantile(ref, np.linspace(0, 1, bins + 1))
    edges[0], edges[-1] = -np.inf, np.inf                  # catch values outside the training range
    r = np.histogram(ref, edges)[0] / len(ref)
    c = np.histogram(cur, edges)[0] / len(cur)
    r, c = np.clip(r, eps, None), np.clip(c, eps, None)
    return float(np.sum((c - r) * np.log(c / r)))


def check(name, reference, current):
    nulls = np.isnan(current).mean()
    ks = ks_2samp(reference[~np.isnan(reference)], current[~np.isnan(current)])
    p = psi(reference, current)
    verdict = "stable" if p < 0.1 else ("watch" if p < 0.25 else "INVESTIGATE")
    print(f"{name:34s} PSI {p:6.3f}  KS D {ks.statistic:5.3f}  p {ks.pvalue:8.1e}  nulls {nulls:5.1%}  -> {verdict}")


if __name__ == "__main__":
    # reference = training-time distribution of "distance_km"
    ref = rng.gamma(2.0, 1.5, 50_000)
    scenarios = {
        "same distribution (new sample)": rng.gamma(2.0, 1.5, 20_000),
        "tiny shift (+3%) -- big n": rng.gamma(2.0, 1.5, 200_000) * 1.03,
        "new suburb city (longer trips)": rng.gamma(2.6, 1.7, 20_000),
        "upstream bug: miles not km": rng.gamma(2.0, 1.5, 20_000) * 0.621,
    }
    broken = rng.gamma(2.0, 1.5, 20_000)
    broken[rng.random(20_000) < 0.3] = np.nan                  # a producer started sending nulls
    scenarios["30% nulls (broken producer)"] = broken

    for name, cur in scenarios.items():
        check(name, ref, cur)

    fig, ax = plt.subplots(figsize=(6.5, 3.6))
    bins = np.linspace(0, 16, 60)
    ax.hist(ref, bins, density=True, alpha=.5, label="reference (training)")
    ax.hist(scenarios["new suburb city (longer trips)"], bins, density=True, histtype="step", lw=2, label="new suburb city")
    ax.hist(scenarios["upstream bug: miles not km"], bins, density=True, histtype="step", lw=2, label="miles-not-km bug")
    ax.set_xlabel("distance feature"); ax.set_ylabel("density"); ax.legend()
    fig.tight_layout(); fig.savefig(IMG / "drift_hist.png", dpi=130)
    print("saved img/drift_hist.png")
