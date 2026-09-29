"""The fairness impossibility result in numbers.

Scores are PERFECTLY calibrated in both groups by construction (y ~ Bernoulli(score)),
but the groups have different base rates. One shared threshold then gives
unequal error rates - you cannot have calibration AND equal FPR/FNR at once."""
import numpy as np

rng = np.random.default_rng(1)


def rates(score, y, t):
    pred = score >= t
    fpr = np.mean(pred[y == 0]); fnr = np.mean(~pred[y == 1])
    ppv = np.mean(y[pred]); sel = np.mean(pred)
    return fpr, fnr, ppv, sel


def main():
    n = 200_000
    groups = {"A": rng.beta(6, 4, n), "B": rng.beta(4, 6, n)}   # mean score 0.6 vs 0.4
    data = {g: (s, (rng.random(n) < s).astype(int)) for g, s in groups.items()}

    print("group  base_rate  calib(0.7-0.8 bin)  FPR    FNR    PPV   selection")
    for g, (s, y) in data.items():
        b = (s >= 0.7) & (s < 0.8)
        fpr, fnr, ppv, sel = rates(s, y, 0.5)
        print(f"  {g}      {y.mean():.3f}      {s[b].mean():.3f}->{y[b].mean():.3f}     "
              f"{fpr:.3f}  {fnr:.3f}  {ppv:.3f}  {sel:.3f}")

    # Force equal FPR (=0.20) with group-specific thresholds: calibration is untouched,
    # but now PPV and FNR differ instead.
    print("\nGroup thresholds chosen so FPR = 0.20 in both groups:")
    for g, (s, y) in data.items():
        t = np.quantile(s[y == 0], 0.80)
        fpr, fnr, ppv, sel = rates(s, y, t)
        print(f"  {g}: threshold={t:.3f}  FPR={fpr:.3f}  FNR={fnr:.3f}  PPV={ppv:.3f}")


if __name__ == "__main__":
    main()
