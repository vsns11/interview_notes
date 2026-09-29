"""Local search on 8-queens: hill climbing, random restarts, simulated annealing,
and a genetic algorithm.

State: a tuple of 8 numbers; state[col] = row of the queen in that column.
Cost:  number of attacking pairs (0 = solved). Neighbours: move one queen within its column."""
import math
import random
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

N = 8


def conflicts(s):
    return sum(1 for i in range(N) for j in range(i + 1, N)
               if s[i] == s[j] or abs(s[i] - s[j]) == j - i)


def random_state(rng):
    return tuple(rng.randrange(N) for _ in range(N))


def neighbours(s):
    for col in range(N):
        for row in range(N):
            if row != s[col]:
                yield s[:col] + (row,) + s[col + 1:]


def hill_climb(s, rng, history=None):
    """Steepest descent on conflicts: take the best neighbour until none is better."""
    while True:
        if history is not None:
            history.append(conflicts(s))
        best = min(conflicts(n) for n in neighbours(s))
        if best >= conflicts(s):
            return s                                   # local minimum (maybe the goal)
        s = rng.choice([n for n in neighbours(s) if conflicts(n) == best])


def random_restart(rng, max_restarts=100):
    for restart in range(max_restarts):
        s = hill_climb(random_state(rng), rng)
        if conflicts(s) == 0:
            return s, restart + 1
    return None, max_restarts


def simulated_annealing(s, rng, t0=2.0, cooling=0.995, steps=5000, history=None):
    t = t0
    for _ in range(steps):
        c = conflicts(s)
        if history is not None:
            history.append(c)
        if c == 0:
            return s
        nxt = s[:(col := rng.randrange(N))] + (rng.randrange(N),) + s[col + 1:]
        delta = conflicts(nxt) - c                     # > 0 means worse
        if delta <= 0 or rng.random() < math.exp(-delta / t):
            s = nxt                                    # sometimes accept a worse move
        t = max(t * cooling, 1e-3)
    return s


def genetic_algorithm(rng, pop_size=100, generations=500, mutation=0.2):
    pop = [random_state(rng) for _ in range(pop_size)]
    max_pairs = N * (N - 1) // 2                       # 28 pairs
    for gen in range(generations):
        fitness = [max_pairs - conflicts(s) for s in pop]       # higher is better
        best = max(range(pop_size), key=fitness.__getitem__)
        if fitness[best] == max_pairs:
            return pop[best], gen
        new_pop = [pop[best]]                                   # elitism: keep the best
        while len(new_pop) < pop_size:
            x, y = rng.choices(pop, weights=fitness, k=2)       # fitness-proportional selection
            cut = rng.randrange(1, N)
            child = x[:cut] + y[cut:]                           # one-point crossover
            if rng.random() < mutation:
                col = rng.randrange(N)
                child = child[:col] + (rng.randrange(N),) + child[col + 1:]
            new_pop.append(child)
        pop = new_pop
    return None, generations


def main() -> None:
    rng = random.Random(42)
    trials = 300
    hc = [conflicts(hill_climb(random_state(rng), rng)) == 0 for _ in range(trials)]
    print(f"hill climbing        : solved {sum(hc)}/{trials} = {sum(hc)/trials:.0%} of random starts")

    restarts = [random_restart(rng)[1] for _ in range(50)]
    print(f"random-restart HC    : solved 50/50, mean restarts needed {sum(restarts)/50:.1f}")

    sa = [conflicts(simulated_annealing(random_state(rng), rng)) == 0 for _ in range(100)]
    print(f"simulated annealing  : solved {sum(sa)}/100")

    ga_runs = [genetic_algorithm(random.Random(seed)) for seed in range(10)]
    solved = [g for s, g in ga_runs if s is not None]
    print(f"genetic algorithm    : solved {len(solved)}/10, mean generations {sum(solved)/max(1, len(solved)):.0f}")

    solution, _ = random_restart(random.Random(1))
    print("\none solution (row of the queen in each column):", solution)
    for row in range(N):
        print(" ".join("Q" if solution[col] == row else "." for col in range(N)))

    # Plot: one hill-climbing run that gets stuck vs one annealing run
    for seed in range(100):
        h_hist = []
        if conflicts(hill_climb(random_state(random.Random(seed)), random.Random(seed), h_hist)) > 0:
            break
    s_hist = []
    simulated_annealing(random_state(random.Random(seed)), random.Random(seed), history=s_hist)
    fig, ax = plt.subplots(figsize=(8, 3.5))
    ax.plot(s_hist, lw=0.8, label="simulated annealing")
    ax.plot(h_hist, "o-", color="#d7301f", label="hill climbing (stuck)")
    ax.set_xlabel("iteration")
    ax.set_ylabel("attacking pairs")
    ax.set_xscale("symlog")
    ax.legend()
    ax.set_title("8-queens: greedy descent stalls, annealing escapes local minima")
    fig.tight_layout()
    fig.savefig(Path(__file__).resolve().parent.parent / "img" / "local_search.png", dpi=130)
    print(f"\nplot: hill climbing stopped at {h_hist[-1]} conflicts after {len(h_hist) - 1} moves; "
          f"annealing reached {s_hist[-1]} after {len(s_hist) - 1} steps")


if __name__ == "__main__":
    main()
