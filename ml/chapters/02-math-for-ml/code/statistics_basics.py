"""Statistics for ML: mean / median / std, standardisation, correlation vs causation, sampling and the
central limit theorem, a t-test with a p-value, and confidence intervals. Saves img/clt.png."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from scipy import stats

IMG = Path(__file__).resolve().parent.parent / "img"
rng = np.random.default_rng(0)


def describe() -> None:
    salaries = np.array([40, 42, 45, 47, 50, 52, 55, 300])   # $1000s, one CEO outlier
    print(f"mean {salaries.mean():.1f}, median {np.median(salaries):.1f}")
    print(f"population std (ddof=0) {salaries.std():.1f}, sample std (ddof=1) {salaries.std(ddof=1):.1f}")
    print(f"without the outlier: mean {salaries[:-1].mean():.1f}, median {np.median(salaries[:-1]):.1f}")

    x = np.array([2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0])
    z = (x - x.mean()) / x.std()
    print(f"x mean {x.mean()}, std {x.std()} -> standardised z = {z.round(2)}")
    print(f"z mean {z.mean():.1f}, z std {z.std():.1f}")


def correlation_vs_causation() -> None:
    # Temperature drives both ice-cream sales and drownings; they correlate but neither causes the other.
    temp = rng.uniform(10, 35, 365)
    ice_cream = 20 * temp + rng.normal(0, 60, 365)
    drownings = 0.3 * temp + rng.normal(0, 1.5, 365)
    r = np.corrcoef(ice_cream, drownings)[0, 1]
    print(f"corr(ice cream, drownings) = {r:.2f}")
    # Remove the confounder: correlate what is left after regressing each on temperature
    res_ice = ice_cream - np.polyval(np.polyfit(temp, ice_cream, 1), temp)
    res_drown = drownings - np.polyval(np.polyfit(temp, drownings, 1), temp)
    print(f"corr after controlling for temperature = {np.corrcoef(res_ice, res_drown)[0, 1]:.2f}")

    x = np.linspace(-3, 3, 200)
    print(f"corr(x, x^2) = {np.corrcoef(x, x**2)[0, 1]:.2f}  (perfect dependence, zero LINEAR correlation)")


def sampling_and_clt() -> None:
    population = rng.exponential(scale=50, size=1_000_000)    # skewed, e.g. session length in seconds
    print(f"population mean {population.mean():.2f}, std {population.std():.2f}")
    fig, axes = plt.subplots(1, 3, figsize=(12, 3.3))
    axes[0].hist(population[:20000], bins=60, color="#bbb"); axes[0].set_title("population (skewed)", fontsize=10)
    for ax, n in zip(axes[1:], (5, 50)):
        means = rng.choice(population, size=(5000, n)).mean(axis=1)
        print(f"n={n:>2}: mean of sample means {means.mean():.2f}, std of sample means {means.std():.2f} "
              f"(theory sigma/sqrt(n) = {population.std()/np.sqrt(n):.2f})")
        ax.hist(means, bins=60, color="#1f77b4")
        ax.set_title(f"means of samples of size {n}", fontsize=10)
    fig.tight_layout()
    fig.savefig(IMG / "clt.png", dpi=120)
    plt.close(fig)


def hypothesis_test_and_ci() -> None:
    # A/B test: did the new model reduce page load time? (milliseconds)
    old = rng.normal(200, 30, 60)
    new = rng.normal(188, 30, 60)
    t, p = stats.ttest_ind(new, old, equal_var=False)          # Welch's t-test
    print(f"old mean {old.mean():.1f}, new mean {new.mean():.1f}, diff {new.mean() - old.mean():.1f} ms")
    print(f"Welch t = {t:.2f}, p-value = {p:.4f} -> {'reject' if p < 0.05 else 'cannot reject'} H0 at 0.05")

    # 95% confidence interval for the mean of `new`
    m, se = new.mean(), new.std(ddof=1) / np.sqrt(len(new))
    tcrit = stats.t.ppf(0.975, df=len(new) - 1)
    print(f"95% CI for new mean: {m:.1f} +/- {tcrit:.3f}*{se:.2f} = [{m - tcrit*se:.1f}, {m + tcrit*se:.1f}]")

    # Bootstrap CI for a model's accuracy from 200 test predictions (170 correct)
    correct = np.array([1] * 170 + [0] * 30)
    boots = [rng.choice(correct, len(correct)).mean() for _ in range(5000)]
    lo, hi = np.percentile(boots, [2.5, 97.5])
    print(f"accuracy 0.850, bootstrap 95% CI [{lo:.3f}, {hi:.3f}]")

    # What a 95% CI means: repeat the experiment many times, ~95% of intervals contain the true mean
    covered = 0
    for _ in range(2000):
        s = rng.normal(100, 15, 30)
        half = stats.t.ppf(0.975, 29) * s.std(ddof=1) / np.sqrt(30)
        covered += abs(s.mean() - 100) <= half
    print(f"coverage of 2000 simulated 95% CIs: {covered / 2000:.3f}")


if __name__ == "__main__":
    describe(); print()
    correlation_vs_causation(); print()
    sampling_and_clt(); print()
    hypothesis_test_and_ci()
