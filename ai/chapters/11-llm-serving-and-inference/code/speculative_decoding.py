"""Speculative decoding with the exact accept/reject rule (Leviathan et al. 2023, Chen et al. 2023).

Toy setting: the "target" (big) model and the "draft" (small) model each give a fixed
next-token distribution over 6 tokens (p and q). The draft proposes k tokens; the target
checks them all in ONE forward pass. We verify two claims:
  1. the tokens that come out follow the target distribution p exactly;
  2. average tokens produced per target pass = (1 - a^(k+1)) / (1 - a),
     where a = sum_x min(p(x), q(x)) is the chance a draft token is accepted.
"""
import numpy as np

rng = np.random.default_rng(0)
VOCAB = ["the", "cat", "sat", "on", "mat", "."]
p = np.array([0.30, 0.25, 0.15, 0.12, 0.10, 0.08])    # target model
q = np.array([0.40, 0.15, 0.20, 0.05, 0.15, 0.05])    # cheaper, slightly wrong draft


def speculative_step(k):
    """One target-model pass. Returns the list of tokens emitted."""
    drafts = rng.choice(len(p), size=k, p=q)          # draft model runs k cheap steps
    out = []
    for x in drafts:                                  # target scores all k at once
        if rng.random() < min(1.0, p[x] / q[x]):      # accept with prob min(1, p/q)
            out.append(x)
        else:                                         # reject: resample from the residual
            resid = np.maximum(p - q, 0)
            out.append(rng.choice(len(p), p=resid / resid.sum()))
            return out
    out.append(rng.choice(len(p), p=p))               # all accepted: free bonus token
    return out


if __name__ == "__main__":
    alpha = np.minimum(p, q).sum()
    print(f"acceptance rate a = sum(min(p, q)) = {alpha:.2f}")
    for k in (1, 2, 4, 8):
        emitted, passes = [], 0
        while len(emitted) < 200_000:
            emitted += speculative_step(k)
            passes += 1
        freq = np.bincount(emitted, minlength=len(p)) / len(emitted)
        theory = (1 - alpha ** (k + 1)) / (1 - alpha)
        print(f"k={k}: tokens per target pass = {len(emitted) / passes:.2f} "
              f"(theory {theory:.2f}); max |freq - p| = {np.abs(freq - p).max():.4f}")
    print("target p :", {v: float(x) for v, x in zip(VOCAB, p)})
    print("emitted  :", {v: round(float(x), 3) for v, x in zip(VOCAB, freq)})
