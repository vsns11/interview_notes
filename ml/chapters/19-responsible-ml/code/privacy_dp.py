"""Privacy demos:
  1) k-anonymity: measure k, generalise quasi-identifiers, spot a homogeneity problem
  2) a differencing attack on exact counts
  3) differential privacy with the Laplace mechanism (count and clipped mean)
"""
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

rng = np.random.default_rng(42)


def k_anonymity(df, quasi):
    return int(df.groupby(quasi).size().min())


def laplace_count(true_count, eps, rng):
    return true_count + rng.laplace(0.0, 1.0 / eps)            # sensitivity of a count = 1


def laplace_mean(x, lo, hi, eps, rng):
    x = np.clip(x, lo, hi)                                      # clipping bounds the sensitivity
    sens = (hi - lo) / len(x)                                   # one person moves the mean at most this much
    return x.mean() + rng.laplace(0.0, sens / eps)


def main():
    # ---------- 1) k-anonymity
    df = pd.DataFrame({
        "zip":  ["02138", "02139", "02141", "02142", "02138", "02139", "02141", "02142"],
        "age":  [29, 22, 27, 43, 52, 47, 30, 36],
        "sex":  ["F", "F", "F", "M", "M", "M", "F", "M"],
        "diagnosis": ["flu", "flu", "flu", "flu", "cancer", "heart", "flu", "heart"],
    })
    quasi = ["zip", "age", "sex"]
    print("raw table: k =", k_anonymity(df, quasi))
    gen = df.assign(zip=df.zip.str[:3] + "**",
                    age=pd.cut(df.age, [0, 35, 120], labels=["<=35", ">35"]).astype(str))
    print("generalised (zip prefix + age band): k =", k_anonymity(gen, quasi))
    print(gen.groupby(quasi).diagnosis.agg(lambda s: sorted(set(s))).to_string())

    # ---------- 2) differencing attack on exact answers
    patients = pd.DataFrame({"name": [f"p{i}" for i in range(999)] + ["Bob"],
                             "hiv": np.r_[rng.random(999) < 0.05, True]})
    q_all = int(patients.hiv.sum())
    q_without_bob = int(patients[patients.name != "Bob"].hiv.sum())
    print(f"\nexact: count(all)={q_all}  count(all except Bob)={q_without_bob}"
          f"  -> Bob's status leaked: {q_all - q_without_bob}")
    eps = 0.5
    a, b = laplace_count(q_all, eps, rng), laplace_count(q_without_bob, eps, rng)
    print(f"DP (eps={eps} each): {a:.1f} vs {b:.1f} -> difference {a - b:+.1f} (noise sd ~ {np.sqrt(2)/eps:.1f} per query)")

    # ---------- 3) accuracy vs privacy
    incomes = rng.lognormal(4.0, 0.5, 10_000)                     # in $k
    print(f"\ntrue count(hiv) = {q_all},  true mean income = {incomes.mean():.2f}k")
    print("eps     noisy count (3 draws)        mean |err| count   noisy mean income")
    eps_grid = [0.01, 0.1, 1.0, 10.0]
    errs = []
    for e in eps_grid:
        draws = [laplace_count(q_all, e, rng) for _ in range(3)]
        err = np.mean([abs(laplace_count(q_all, e, rng) - q_all) for _ in range(2000)])
        errs.append(err)
        m = laplace_mean(incomes, 0, 300, e, rng)
        print(f"{e:<6}  {', '.join(f'{d:7.1f}' for d in draws)}      {err:8.2f}          {m:.2f}k")

    fig, ax = plt.subplots(figsize=(5, 3.4))
    ax.loglog(eps_grid, errs, "o-", label="measured mean |error|")
    ax.loglog(eps_grid, [1 / e for e in eps_grid], "k--", label="theory: 1/eps")
    ax.set_xlabel("epsilon (privacy budget, smaller = more private)"); ax.set_ylabel("mean |error| of a count")
    ax.set_title("Laplace mechanism: privacy vs accuracy"); ax.legend()
    fig.tight_layout(); fig.savefig("../img/dp_error_vs_epsilon.png", dpi=130)


if __name__ == "__main__":
    main()
