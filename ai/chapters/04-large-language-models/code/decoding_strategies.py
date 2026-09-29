"""Decoding strategies on a toy next-token distribution.

Greedy, temperature, top-k, top-p (nucleus) and beam search, with no model
needed: we hand-write the logits a model might produce after "I love".
"""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

vocab = ["chocolate", "music", "coding", "you", "pizza", "Mondays", "taxes"]
logits = np.array([2.0, 1.8, 1.5, 1.2, 1.0, -0.5, -1.5])   # model scores after "I love"


def softmax(z):
    e = np.exp(z - z.max())
    return e / e.sum()


def with_temperature(logits, T):
    return softmax(logits / T)


def top_k(probs, k):
    keep = np.argsort(probs)[::-1][:k]                # k most likely tokens
    out = np.zeros_like(probs)
    out[keep] = probs[keep]
    return out / out.sum()                            # renormalize


def top_p(probs, p):
    order = np.argsort(probs)[::-1]
    cum = np.cumsum(probs[order])
    cutoff = np.searchsorted(cum, p) + 1              # smallest set whose mass >= p
    keep = order[:cutoff]
    out = np.zeros_like(probs)
    out[keep] = probs[keep]
    return out / out.sum()


def show(name, probs):
    cells = "  ".join(f"{w[:6]:>6}:{p:4.2f}" for w, p in zip(vocab, probs))
    print(f"{name:<18}{cells}")


# --- beam search on a two-step toy language model -------------------------
# P(first word) and P(second word | first word). Greedy picks the best FIRST
# word; beam search keeps several partial sentences and scores whole sequences.
step1 = {"The": 0.5, "A": 0.4, "One": 0.1}
step2 = {
    "The": {"cat": 0.35, "dog": 0.35, "end": 0.30},
    "A":   {"cat": 0.9, "dog": 0.1},
    "One": {"day": 1.0},
}


def greedy_two_step():
    w1 = max(step1, key=step1.get)
    w2 = max(step2[w1], key=step2[w1].get)
    return f"{w1} {w2}", round(step1[w1] * step2[w1][w2], 3)


def beam_two_step(beam_width):
    beams = sorted(step1.items(), key=lambda kv: -kv[1])[:beam_width]
    candidates = [(f"{w1} {w2}", p1 * p2) for w1, p1 in beams for w2, p2 in step2[w1].items()]
    best = max(candidates, key=lambda c: c[1])
    return best[0], round(best[1], 3)


if __name__ == "__main__":
    probs = softmax(logits)
    show("softmax (T=1)", probs)
    print("greedy pick     ->", vocab[int(np.argmax(probs))])
    print()
    for T in [0.2, 0.7, 1.5]:
        show(f"temperature {T}", with_temperature(logits, T))
    print()
    show("top-k (k=3)", top_k(probs, 3))
    show("top-p (p=0.8)", top_p(probs, 0.8))
    show("T=0.7 + top-p .9", top_p(with_temperature(logits, 0.7), 0.9))

    rng = np.random.default_rng(42)
    print("\n10 samples at T=1.0:", [vocab[i] for i in rng.choice(len(vocab), 10, p=probs)])
    print("10 samples at T=0.2:", [vocab[i] for i in rng.choice(len(vocab), 10, p=with_temperature(logits, 0.2))])

    print("\nbeam search demo:")
    print("  greedy        ->", greedy_two_step())
    print("  beam width 2  ->", beam_two_step(2))

    out = Path(__file__).resolve().parent.parent / "img" / "temperature.png"
    fig, axes = plt.subplots(1, 3, figsize=(10, 3), sharey=True)
    for ax, T in zip(axes, [0.2, 1.0, 1.5]):
        ax.bar(range(len(vocab)), with_temperature(logits, T), color="#4C72B0")
        ax.set_xticks(range(len(vocab)), vocab, rotation=60, fontsize=8)
        ax.set_title(f"temperature = {T}")
    axes[0].set_ylabel("probability")
    fig.suptitle('Next-token distribution after "I love"')
    fig.tight_layout()
    fig.savefig(out, dpi=130)
    print("saved", out.name)
