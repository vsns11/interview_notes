"""Probability for ML: random variables, Bernoulli / binomial / normal distributions, expectation and
variance, conditional probability and Bayes' theorem. Saves img/distributions.png."""
from math import comb
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from scipy import stats

IMG = Path(__file__).resolve().parent.parent / "img"
rng = np.random.default_rng(0)


def distributions() -> None:
    p = 0.3
    flips = rng.random(100_000) < p                      # Bernoulli(0.3): 1 with prob 0.3
    print(f"Bernoulli(0.3): sample mean {flips.mean():.3f} (theory {p}), "
          f"variance {flips.var():.3f} (theory p(1-p) = {p*(1-p):.2f})")

    n = 10
    print(f"Binomial(10, 0.3): P(X=3) = C(10,3) 0.3^3 0.7^7 = {comb(10,3) * 0.3**3 * 0.7**7:.4f}"
          f" | scipy {stats.binom.pmf(3, n, p):.4f}")
    draws = rng.binomial(n, p, 100_000)
    print(f"  mean {draws.mean():.3f} (theory np = {n*p}), variance {draws.var():.3f} (theory np(1-p) = {n*p*(1-p):.1f})")

    mu, sigma = 170, 10                                  # heights in cm
    heights = rng.normal(mu, sigma, 100_000)
    within1 = np.mean(np.abs(heights - mu) < sigma)
    within2 = np.mean(np.abs(heights - mu) < 2 * sigma)
    print(f"Normal(170, 10): within 1 sd {within1:.3f}, within 2 sd {within2:.3f} (theory 0.683, 0.954)")
    print(f"  P(height > 190) = {1 - stats.norm.cdf(190, mu, sigma):.4f}  (z = (190-170)/10 = 2)")

    # Expectation of a fair die by definition and by simulation
    faces = np.arange(1, 7)
    ex = np.sum(faces * (1 / 6))
    var = np.sum((faces - ex) ** 2 * (1 / 6))
    rolls = rng.integers(1, 7, 100_000)
    print(f"die: E[X] = {ex:.3f}, Var[X] = {var:.3f} | simulated mean {rolls.mean():.3f}, var {rolls.var():.3f}")

    fig, axes = plt.subplots(1, 3, figsize=(12, 3.3))
    axes[0].bar([0, 1], [1 - p, p], color=["#999", "#1f77b4"]); axes[0].set_xticks([0, 1])
    axes[0].set_title("Bernoulli(p = 0.3)", fontsize=10)
    k = np.arange(0, 11)
    axes[1].bar(k, stats.binom.pmf(k, n, p), color="#1f77b4"); axes[1].set_title("Binomial(n = 10, p = 0.3)", fontsize=10)
    axes[1].set_xlabel("number of successes")
    axes[2].hist(heights, bins=80, density=True, color="#bbb")
    xs = np.linspace(130, 210, 200)
    axes[2].plot(xs, stats.norm.pdf(xs, mu, sigma), color="#d62728")
    axes[2].set_title("Normal(mean 170, sd 10)", fontsize=10); axes[2].set_xlabel("height (cm)")
    fig.tight_layout()
    fig.savefig(IMG / "distributions.png", dpi=120)
    plt.close(fig)


def conditional_and_bayes() -> None:
    # Conditional probability from a count table: 1000 emails
    spam_with_free, spam_total = 120, 200
    ham_with_free, ham_total = 40, 800
    p_free = (spam_with_free + ham_with_free) / 1000
    p_spam_given_free = spam_with_free / (spam_with_free + ham_with_free)
    print(f"P(free) = {p_free:.2f}, P(spam | free) = 120/160 = {p_spam_given_free:.2f}")

    # Bayes: disease with 1% prevalence, test with 90% sensitivity and 95% specificity.
    prior = 0.01
    sens = 0.90              # P(positive | disease)
    fpr = 0.05               # P(positive | healthy) = 1 - specificity
    evidence = sens * prior + fpr * (1 - prior)
    posterior = sens * prior / evidence
    print(f"P(positive) = 0.9*0.01 + 0.05*0.99 = {evidence:.4f}")
    print(f"P(disease | positive) = 0.009 / {evidence:.4f} = {posterior:.3f}")

    # Check by simulating 1,000,000 people
    n = 1_000_000
    sick = rng.random(n) < prior
    positive = np.where(sick, rng.random(n) < sens, rng.random(n) < fpr)
    print(f"simulation: P(disease | positive) = {sick[positive].mean():.3f}")
    second = sens * posterior / (sens * posterior + fpr * (1 - posterior))
    print(f"after a second independent positive test: {second:.3f}")


if __name__ == "__main__":
    distributions()
    print()
    conditional_and_bayes()
