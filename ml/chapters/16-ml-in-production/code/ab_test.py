"""A/B testing a new model: sample size, two-proportion z-test, confidence interval, and why peeking is bad.

Metric: click-through rate (CTR) per user-assignment. Assignment is sticky by hashing the user id.
"""
import hashlib
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from scipy.stats import norm

IMG = Path(__file__).resolve().parent.parent / "img"


def assign(user_id: str, experiment: str, treatment_share=0.5):
    """Deterministic, sticky bucketing: same user + same experiment -> same arm, every time."""
    h = int(hashlib.sha256(f"{experiment}:{user_id}".encode()).hexdigest(), 16)
    return "B" if (h % 10_000) / 10_000 < treatment_share else "A"


def sample_size_per_arm(p_base, rel_lift, alpha=0.05, power=0.80):
    """Two-sided two-proportion test, classic normal-approximation formula."""
    p1, p2 = p_base, p_base * (1 + rel_lift)
    pbar = (p1 + p2) / 2
    za, zb = norm.ppf(1 - alpha / 2), norm.ppf(power)
    num = (za * np.sqrt(2 * pbar * (1 - pbar)) + zb * np.sqrt(p1 * (1 - p1) + p2 * (1 - p2))) ** 2
    return int(np.ceil(num / (p2 - p1) ** 2))


def two_prop_ztest(x_a, n_a, x_b, n_b):
    pa, pb = x_a / n_a, x_b / n_b
    pooled = (x_a + x_b) / (n_a + n_b)
    se0 = np.sqrt(pooled * (1 - pooled) * (1 / n_a + 1 / n_b))       # SE under H0 (for the test)
    z = (pb - pa) / se0
    p = 2 * (1 - norm.cdf(abs(z)))
    se = np.sqrt(pa * (1 - pa) / n_a + pb * (1 - pb) / n_b)          # unpooled SE (for the CI)
    ci = (pb - pa - 1.96 * se, pb - pa + 1.96 * se)
    return pa, pb, z, p, ci


def peeking_simulation(n_experiments=2000, n_per_arm=20_000, looks=20, p=0.05, seed=0):
    """A/A tests (no real difference). Compare: test once at the end vs stop at the first 'significant' look."""
    rng = np.random.default_rng(seed)
    checkpoints = np.linspace(n_per_arm / looks, n_per_arm, looks).astype(int)
    fp_final = fp_peek = 0
    for _ in range(n_experiments):
        a = rng.random(n_per_arm) < p
        b = rng.random(n_per_arm) < p
        ca, cb = np.cumsum(a), np.cumsum(b)
        pvals = [two_prop_ztest(ca[n - 1], n, cb[n - 1], n)[3] for n in checkpoints]
        fp_final += pvals[-1] < 0.05
        fp_peek += min(pvals) < 0.05
    return fp_final / n_experiments, fp_peek / n_experiments


if __name__ == "__main__":
    print("sticky assignment:", [assign(f"user{i}", "ranker_v7") for i in range(8)],
          "| again:", [assign(f"user{i}", "ranker_v7") for i in range(8)] == [assign(f"user{i}", "ranker_v7") for i in range(8)])

    for lift in (0.01, 0.02, 0.05):
        n = sample_size_per_arm(0.05, lift)
        print(f"baseline CTR 5%, detect +{lift:.0%} relative lift (alpha .05, power .8): {n:,} users per arm")

    # one finished experiment (simulated): true CTR A=5.00%, B=5.15% (+3% relative)
    rng = np.random.default_rng(42)
    n = 400_000
    x_a = int((rng.random(n) < 0.0500).sum())
    x_b = int((rng.random(n) < 0.0515).sum())
    pa, pb, z, p, ci = two_prop_ztest(x_a, n, x_b, n)
    print(f"\nA: {x_a:,}/{n:,} = {pa:.4%}   B: {x_b:,}/{n:,} = {pb:.4%}")
    print(f"diff {pb - pa:+.4%} (relative {(pb - pa) / pa:+.1%}), z = {z:.2f}, p = {p:.4f}, "
          f"95% CI [{ci[0]:+.4%}, {ci[1]:+.4%}]")

    fp_final, fp_peek = peeking_simulation()
    print(f"\nA/A tests, alpha 0.05: false-positive rate testing once at the end = {fp_final:.1%}; "
          f"stopping at the first significant of 20 peeks = {fp_peek:.1%}")

    fig, ax = plt.subplots(figsize=(5, 3.2))
    ax.bar(["test once\n(fixed horizon)", "peek 20 times,\nstop when p<.05"], [fp_final * 100, fp_peek * 100],
           color=["#2b6cb0", "#c05621"])
    ax.axhline(5, color="k", ls="--", lw=1)
    ax.set_ylabel("false-positive rate (%)")
    ax.set_title("A/A tests: no real difference")
    fig.tight_layout(); fig.savefig(IMG / "peeking.png", dpi=130)
    print("saved img/peeking.png")
