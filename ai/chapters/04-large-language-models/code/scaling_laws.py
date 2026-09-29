"""Chinchilla scaling-law intuition (Hoffmann et al., 2022).

Uses the paper's fitted loss formula L(N, D) = E + A/N^alpha + B/D^beta and the
compute rule C ~= 6 * N * D to find the compute-optimal model size.
"""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

# Fitted constants reported in the Chinchilla paper (approach 3).
E, A, B, ALPHA, BETA = 1.69, 406.4, 410.7, 0.34, 0.28


def loss(N, D):
    """Predicted pre-training loss for N parameters trained on D tokens."""
    return E + A / N**ALPHA + B / D**BETA


def tokens_for(C, N):
    return C / (6 * N)                     # training FLOPs ~= 6 * params * tokens


if __name__ == "__main__":
    print("Roughly the same compute budget, two ways to spend it:")
    for name, N, D in [("Gopher-like  (280B params, 300B tokens)", 280e9, 300e9),
                       ("Chinchilla   ( 70B params, 1.4T tokens)", 70e9, 1.4e12)]:
        print(f"  {name}: compute={6 * N * D:.2e}  predicted loss={loss(N, D):.3f}")

    print("\nCompute-optimal size with the ~20 tokens/parameter rule (N = sqrt(C/120)):")
    for C in [1e21, 1e23, 5.76e23, 1e25]:
        N = np.sqrt(C / 120)
        print(f"  C={C:.2e} FLOPs -> N ~ {N / 1e9:7.1f}B params, D ~ {20 * N / 1e12:6.2f}T tokens")

    # IsoFLOP curve: fix compute, vary model size, find the loss minimum numerically.
    C = 5.76e23
    Ns = np.logspace(9.5, 12, 200)
    Ls = loss(Ns, tokens_for(C, Ns))
    best = Ns[np.argmin(Ls)]
    print(f"\nFormula's own optimum at C={C:.2e}: N ~ {best / 1e9:.0f}B, "
          f"D ~ {tokens_for(C, best) / 1e12:.2f}T tokens ({tokens_for(C, best) / best:.0f} tokens/param)")

    out = Path(__file__).resolve().parent.parent / "img" / "isoflop.png"
    fig, ax = plt.subplots(figsize=(6.5, 3.6))
    for c in [1e22, 1e23, 5.76e23, 1e24]:
        ax.plot(Ns / 1e9, loss(Ns, tokens_for(c, Ns)), label=f"C = {c:.2g} FLOPs")
    ax.set_xscale("log")
    ax.set_xlabel("model size (billions of parameters)")
    ax.set_ylabel("predicted loss")
    ax.set_ylim(1.8, 2.6)
    ax.set_title("IsoFLOP curves: for each budget there is a best model size")
    ax.legend(fontsize=8)
    fig.tight_layout()
    fig.savefig(out, dpi=130)
    print("saved", out.name)
