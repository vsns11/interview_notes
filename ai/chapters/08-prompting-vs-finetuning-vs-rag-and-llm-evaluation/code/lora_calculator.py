"""LoRA by the numbers, plus a tiny LoRA layer trained in PyTorch.

Part 1: parameter counts and rough GPU memory for full fine-tuning, LoRA and
        QLoRA on a 7B Llama-style transformer.
Part 2: a frozen linear layer + trainable low-rank update (W + (alpha/r)·B·A),
        trained to fit a hidden rank-4 change; shows rank matters and that the
        update merges back into W with no inference overhead.
"""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import torch
from torch import nn

# ---------------------------------------------------------------- Part 1
CFG = dict(d=4096, layers=32, ffn=11008, vocab=32000)   # Llama-2-7B shapes


def base_params(c):
    attn = 4 * c["d"] * c["d"]                     # q, k, v, o projections
    mlp = 3 * c["d"] * c["ffn"]                    # gate, up, down projections
    emb = 2 * c["vocab"] * c["d"]                  # input embedding + output head
    return c["layers"] * (attn + mlp) + emb


def lora_params(c, r, targets):
    shapes = {"q": (c["d"], c["d"]), "k": (c["d"], c["d"]), "v": (c["d"], c["d"]),
              "o": (c["d"], c["d"]), "gate": (c["d"], c["ffn"]), "up": (c["d"], c["ffn"]),
              "down": (c["ffn"], c["d"])}
    # each adapted matrix W (d_in x d_out) gets A (r x d_in) and B (d_out x r)
    return c["layers"] * sum(r * (shapes[t][0] + shapes[t][1]) for t in targets)


def gb(x):
    return x / 1e9


# ---------------------------------------------------------------- Part 2
class LoRALinear(nn.Module):
    def __init__(self, base: nn.Linear, r: int, alpha: float):
        super().__init__()
        self.base = base
        for p in self.base.parameters():
            p.requires_grad = False                                  # frozen W0
        self.A = nn.Parameter(torch.randn(r, base.in_features) * 0.01)  # small random
        self.B = nn.Parameter(torch.zeros(base.out_features, r))        # zero -> starts as W0
        self.scale = alpha / r

    def forward(self, x):
        return self.base(x) + self.scale * (x @ self.A.T @ self.B.T)

    def merged(self):
        return self.base.weight + self.scale * self.B @ self.A      # W0 + (alpha/r)·B·A


def train(r, steps=400, seed=0):
    torch.manual_seed(seed)
    d = 64
    base = nn.Linear(d, d)
    true_delta = (torch.randn(d, 4) @ torch.randn(4, d)) * 0.1    # hidden rank-4 change
    X = torch.randn(512, d)
    with torch.no_grad():
        Y = base(X) + X @ true_delta.T
    layer = LoRALinear(base, r=r, alpha=2 * r)
    opt = torch.optim.Adam([layer.A, layer.B], lr=1e-2)
    losses = []
    for _ in range(steps):
        loss = ((layer(X) - Y) ** 2).mean()
        opt.zero_grad()
        loss.backward()
        opt.step()
        losses.append(loss.item())
    return layer, X, losses


if __name__ == "__main__":
    P = base_params(CFG)
    print(f"Base model parameters: {P / 1e9:.2f} B\n")
    print(f"{'setup':<34}{'trainable':>12}{'% of base':>11}")
    for r, targets, label in [(8, ["q", "v"], "LoRA r=8 on q,v (LoRA paper style)"),
                              (16, ["q", "k", "v", "o"], "LoRA r=16 on attention"),
                              (16, ["q", "k", "v", "o", "gate", "up", "down"], "LoRA r=16 on all linear"),
                              (64, ["q", "k", "v", "o", "gate", "up", "down"], "LoRA r=64 on all linear")]:
        n = lora_params(CFG, r, targets)
        print(f"{label:<34}{n / 1e6:>10.1f} M{100 * n / P:>10.2f}%")

    n16 = lora_params(CFG, 16, ["q", "k", "v", "o", "gate", "up", "down"])
    print("\nRough GPU memory for weights + gradients + Adam state (no activations):")
    full = P * 16          # bf16 weights 2 + grads 2 + fp32 master 4 + Adam m 4 + v 4
    lora = P * 2 + n16 * 16
    qlora = P * 0.5 + n16 * 16           # 4-bit base weights (plus small quantization constants)
    print(f"  full fine-tune (16 bytes/param)      {gb(full):6.1f} GB")
    print(f"  LoRA, bf16 frozen base + adapters    {gb(lora):6.1f} GB")
    print(f"  QLoRA, 4-bit frozen base + adapters  {gb(qlora):6.1f} GB")
    print(f"  one saved adapter (bf16)             {n16 * 2 / 1e6:6.1f} MB  "
          f"vs full checkpoint {gb(P * 2):.1f} GB")

    print("\nTiny LoRA layer (64x64 frozen, hidden rank-4 change):")
    curves = {}
    for r in [1, 2, 4, 8]:
        layer, X, losses = train(r)
        curves[r] = losses
        n_train = layer.A.numel() + layer.B.numel()
        print(f"  r={r}: trainable {n_train:>4} of {n_train + 64 * 64 + 64} total params, "
              f"final MSE {losses[-1]:.2e}")
    layer, X, _ = train(4)
    with torch.no_grad():
        y_adapter = layer(X)
        y_merged = X @ layer.merged().T + layer.base.bias
    print(f"  merged W' = W0 + (alpha/r)BA gives same outputs: "
          f"max diff {torch.max(torch.abs(y_adapter - y_merged)).item():.1e}")

    plt.figure(figsize=(6, 3.6))
    for r, ls in curves.items():
        plt.plot(ls, label=f"r={r}")
    plt.yscale("log")
    plt.xlabel("training step")
    plt.ylabel("MSE (log scale)")
    plt.title("LoRA rank vs a hidden rank-4 change")
    plt.legend()
    plt.grid(alpha=0.3)
    plt.tight_layout()
    out = Path(__file__).resolve().parent.parent / "img" / "lora_rank_loss.png"
    plt.savefig(out, dpi=150)
    print("saved img/lora_rank_loss.png")
