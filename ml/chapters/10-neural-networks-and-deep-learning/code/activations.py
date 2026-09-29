"""Plot common activation functions and their derivatives."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import torch
import torch.nn.functional as F

IMG = Path(__file__).resolve().parent.parent / "img"

ACTS = {
    "sigmoid": torch.sigmoid,
    "tanh": torch.tanh,
    "ReLU": F.relu,
    "Leaky ReLU (0.1)": lambda z: F.leaky_relu(z, 0.1),
    "ELU": F.elu,
    "GELU": F.gelu,
    "SiLU / Swish": F.silu,
}

if __name__ == "__main__":
    z = torch.linspace(-5, 5, 401, requires_grad=True)
    fig, axes = plt.subplots(1, 2, figsize=(11, 4))
    for name, f in ACTS.items():
        a = f(z)
        (g,) = torch.autograd.grad(a.sum(), z)          # derivative via autograd
        axes[0].plot(z.detach(), a.detach(), label=name)
        axes[1].plot(z.detach(), g, label=name)
    axes[0].set_title("Activation f(z)"); axes[1].set_title("Derivative f'(z)")
    for ax in axes:
        ax.axhline(0, color="gray", lw=0.5); ax.axvline(0, color="gray", lw=0.5)
        ax.set_xlabel("z"); ax.grid(alpha=0.3)
    axes[0].set_ylim(-1.5, 3); axes[1].legend(fontsize=8)
    fig.tight_layout(); fig.savefig(IMG / "activations.png", dpi=120)

    # A few numbers worth remembering
    pts = torch.tensor([-2.0, 0.0, 2.0], requires_grad=True)
    for name in ["sigmoid", "tanh", "ReLU"]:
        out = ACTS[name](pts)
        (g,) = torch.autograd.grad(out.sum(), pts)
        print(f"{name:8s} f(-2,0,2)={out.detach().numpy().round(3)}  f'={g.numpy().round(3)}")
    print("softmax([2,1,0.1]) =", torch.softmax(torch.tensor([2.0, 1.0, 0.1]), 0).numpy().round(3))
    print("saved img/activations.png")
