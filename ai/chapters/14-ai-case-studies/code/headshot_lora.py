"""Personalised headshots: per-user low-rank fine-tuning with prior preservation.

A frozen linear "generator" W0 maps a prompt embedding to an output. A user gives
five noisy "selfies" of one identity, bound to a rare token v ("a photo of [v] person").
We fit an update to W0 several ways and measure:
  identity error - how far "[v] person" lands from the true identity (lower is better)
  prior drift    - how much *other* person prompts moved (lower is better; high means
                   every generated person starts to look like the user)
  trainable parameters, which set per-user storage.
Prior preservation (Ruiz et al., 2023 - DreamBooth) adds a loss that keeps the
model's outputs for generic class prompts where the frozen model had them.
LoRA (Hu et al., 2022) trains W0 + B @ A with rank r instead of all of W.
Then the per-user cost arithmetic. Linear toy, numpy only, fixed seed.
"""
from __future__ import annotations

import numpy as np

rng = np.random.default_rng(5)
D_IN, D_OUT = 32, 64

W0 = rng.normal(0, 1 / np.sqrt(D_IN), (D_OUT, D_IN))
c = rng.normal(0, 1, D_IN); c /= np.linalg.norm(c)            # "a photo of a person"
v = rng.normal(0, 1, D_IN); v *= 0.6 / np.linalg.norm(v)      # rare identifier token [v]
x_id = c + v
delta = rng.normal(0, 1, D_OUT); delta *= 1.5 / np.linalg.norm(delta)
y_true = W0 @ x_id + delta                                     # what this user's face "is"

X = np.stack([x_id + rng.normal(0, 0.05, D_IN) for _ in range(5)], 1)   # 5 augmented selfies
Y = np.stack([y_true + rng.normal(0, 0.08, D_OUT) for _ in range(5)], 1)


def class_prompts(n: int) -> np.ndarray:
    """Other person prompts ("a person smiling", "a person in a hat" ...): near c."""
    return np.stack([0.8 * c + rng.normal(0, 0.25, D_IN) for _ in range(n)], 1)


P_train, P_test = class_prompts(40), class_prompts(200)


def train(rank: int | None, lam: float, steps: int = 1500, lr: float = 0.05):
    if rank is None:                                           # full fine-tuning of W
        dW = np.zeros_like(W0)
        params = dW.size
    else:
        B = np.zeros((D_OUT, rank))
        A = rng.normal(0, 1 / np.sqrt(D_IN), (rank, D_IN))
        params = B.size + A.size
    for _ in range(steps):
        M = dW if rank is None else B @ A
        E = (W0 + M) @ X - Y
        g = 2 * E @ X.T / X.shape[1]
        if lam:
            Ep = M @ P_train                                   # frozen model's own outputs are the target
            g += lam * 2 * Ep @ P_train.T / P_train.shape[1]
        if rank is None:
            dW -= lr * g
        else:
            gB, gA = g @ A.T, B.T @ g
            B -= lr * gB
            A -= lr * gA
    M = dW if rank is None else B @ A
    id_err = np.linalg.norm((W0 + M) @ x_id - y_true) / np.linalg.norm(delta)
    drift = np.mean(np.linalg.norm(M @ P_test, axis=0)) / np.linalg.norm(delta)
    return id_err, drift, params


def main() -> None:
    print("identity error and prior drift are relative to the size of the identity offset (1.0 = the whole offset)")
    print("method                         | trainable params | identity error | prior drift")
    for name, rank, lam in (("no fine-tuning", 0, 0.0),
                            ("full fine-tune, no prior loss", None, 0.0),
                            ("LoRA r=4, no prior loss", 4, 0.0),
                            ("LoRA r=4 + prior loss (lam=1)", 4, 1.0),
                            ("LoRA r=4 + prior loss (lam=5)", 4, 5.0),
                            ("LoRA r=4 + prior loss (lam=25)", 4, 25.0),
                            ("LoRA r=1 + prior loss (lam=5)", 1, 5.0)):
        if rank == 0:
            print(f"{name:30} | {0:>16,} | {1.0:14.2f} | {0.0:11.2f}")
            continue
        e, d, p = train(rank, lam)
        print(f"{name:30} | {p:>16,} | {e:14.2f} | {d:11.2f}")

    print("\nPer-user cost (illustrative assumptions)")
    users_day, gpu_min, price_h = 20_000, 12, 3.0
    per_user = gpu_min / 60 * price_h
    print(f"   training: {gpu_min} GPU-min x ${price_h}/GPU-h = ${per_user:.2f} per user; "
          f"{users_day:,} users/day -> ${users_day * per_user:,.0f}/day, "
          f"{users_day * gpu_min / 60 / 24:.0f} GPUs busy around the clock")
    n_mats, d, r = 100, 1280, 16
    lora_params = n_mats * 2 * d * r
    print(f"   adapter: {n_mats} projection matrices of {d}x{d}, rank {r} -> {lora_params:,} params "
          f"= {lora_params * 2 / 1e6:.1f} MB in fp16 (vs {n_mats * d * d * 2 / 1e6:.0f} MB for those matrices in full)")
    print(f"   inference: 40 portraits x 6 GPU-s = 240 GPU-s = ${240 / 3600 * price_h:.2f} per user")


if __name__ == "__main__":
    main()
