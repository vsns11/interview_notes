"""Toy harmful-content cascade: flag rate vs compute vs prevalence of violating VIEWS.

1M uploads, 0.5% violate policy. Views are heavy-tailed (a few items go viral).
  fast classifier : runs on every item, cheap (cost 1), noisy
  heavy model     : runs only on the flagged slice, 100x the cost, much sharper
Policy: remove if heavy score is high. We vary the fast stage's flag rate, then add
reach-aware re-scoring (anything that crosses 10K views gets the heavy model regardless).
Finally we ESTIMATE prevalence the way platforms do: sample views, label them, add a confidence interval.
"""
import numpy as np

rng = np.random.default_rng(5)
N, PREV, HEAVY_COST = 1_000_000, 0.005, 100


def world():
    bad = rng.random(N) < PREV
    views = np.floor(rng.pareto(1.1, N) * 50).astype(int) + 1        # heavy-tailed reach
    fast = rng.normal(0, 1, N) + 2.2 * bad                           # AUC around 0.94
    heavy = rng.normal(0, 1, N) + 4.5 * bad                          # AUC around 0.999
    return bad, views, fast, heavy


def run(bad, views, fast, heavy, flag_rate, viral_tier=None):
    flagged = fast >= np.quantile(fast, 1 - flag_rate)
    if viral_tier is not None:
        flagged |= views >= viral_tier                               # reach-aware re-scoring
    removed = flagged & (heavy > 2.5)
    compute = N * 1 + flagged.sum() * HEAVY_COST                     # fast on all + heavy on flagged
    recall = (removed & bad).sum() / bad.sum()
    viol_views = views[bad & ~removed].sum() / views[~removed].sum() # prevalence of violating views
    false_removals = (removed & ~bad).sum()
    return compute / (N * HEAVY_COST), recall, viol_views, false_removals, flagged.sum()


if __name__ == "__main__":
    bad, views, fast, heavy = world()
    print(f"violating uploads {bad.mean():.2%}, violating share of views before action "
          f"{views[bad].sum() / views.sum():.2%}; top 1% of items get {np.sort(views)[-N // 100:].sum() / views.sum():.0%} of views\n")
    print(f"{'policy':<34}{'heavy runs':>11}{'compute*':>10}{'recall':>8}{'viol. views':>13}{'false rem.':>11}")
    for fr in [0.005, 0.01, 0.02, 0.05]:
        c, r, v, fp, nf = run(bad, views, fast, heavy, fr)
        print(f"{'flag top ' + format(fr, '.1%'):<34}{nf:>11,}{c:>10.3f}{r:>8.1%}{v:>13.3%}{fp:>11,}")
    c, r, v, fp, nf = run(bad, views, fast, heavy, 0.01, viral_tier=10_000)
    print(f"{'flag top 1.0% + re-score >=10K views':<34}{nf:>11,}{c:>10.3f}{r:>8.1%}{v:>13.3%}{fp:>11,}")
    print("* compute relative to running the heavy model on every item")

    # prevalence estimate: sample VIEWS (not uploads) after the 1% + reach-aware policy, have humans label them
    flagged = (fast >= np.quantile(fast, 0.99)) | (views >= 10_000)
    live = ~(flagged & (heavy > 2.5))
    view_pool = np.repeat(np.flatnonzero(live), views[live])          # one entry per view
    print(f"\ntrue prevalence of violating views: {bad[view_pool].mean():.3%}")
    for n in [5_000, 50_000]:
        sample = rng.choice(view_pool, n, replace=False)
        p_hat = bad[sample].mean()
        se = np.sqrt(p_hat * (1 - p_hat) / n)
        print(f"  sample {n:>6,} labelled views -> estimate {p_hat:.3%}  "
              f"(95% CI {max(0, p_hat - 1.96 * se):.3%} to {p_hat + 1.96 * se:.3%})")
