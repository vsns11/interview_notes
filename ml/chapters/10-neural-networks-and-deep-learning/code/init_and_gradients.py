"""Initialization and vanishing/exploding signals in a deep (20-layer) MLP."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import torch
from torch import nn

IMG = Path(__file__).resolve().parent.parent / "img"
DEPTH, WIDTH = 20, 256


def build(act, init):
    layers = []
    for _ in range(DEPTH):
        lin = nn.Linear(WIDTH, WIDTH)
        nn.init.zeros_(lin.bias)
        if init == "small N(0, 0.01)":
            nn.init.normal_(lin.weight, 0, 0.01)
        elif init == "large N(0, 1)":
            nn.init.normal_(lin.weight, 0, 1.0)
        elif init == "Xavier":
            nn.init.xavier_normal_(lin.weight)
        elif init == "He (Kaiming)":
            nn.init.kaiming_normal_(lin.weight, nonlinearity="relu")
        layers += [lin, act()]
    return nn.Sequential(*layers, nn.Linear(WIDTH, 1))


def activation_stds(model, x):
    stds = []
    for layer in model:
        x = layer(x)
        if not isinstance(layer, nn.Linear):
            stds.append(x.std().item())
    return stds


def first_layer_grad(model, x):
    model.zero_grad()
    model(x).pow(2).mean().backward()
    return model[0].weight.grad.norm().item()


if __name__ == "__main__":
    torch.manual_seed(0)
    x = torch.randn(512, WIDTH)
    fig, ax = plt.subplots(figsize=(8, 4))
    for act_name, act in [("tanh", nn.Tanh), ("ReLU", nn.ReLU)]:
        for init in ["small N(0, 0.01)", "large N(0, 1)", "Xavier", "He (Kaiming)"]:
            torch.manual_seed(0)
            m = build(act, init)
            with torch.no_grad():
                s = activation_stds(m, x)
            g = first_layer_grad(m, x)
            print(f"{act_name:5s} + {init:17s} activation std layer1={s[0]:.3g} layer20={s[-1]:.3g}   |grad W1|={g:.3g}")
            if (act_name, init) in [("tanh", "Xavier"), ("ReLU", "He (Kaiming)"), ("ReLU", "Xavier"), ("tanh", "small N(0, 0.01)")]:
                ax.plot(range(1, DEPTH + 1), s, marker="o", ms=3, label=f"{act_name} + {init}")
    ax.set_yscale("log"); ax.set_xlabel("layer"); ax.set_ylabel("std of activations")
    ax.set_title("Signal through a 20-layer MLP"); ax.legend(fontsize=8); ax.grid(alpha=0.3)
    fig.tight_layout(); fig.savefig(IMG / "init_activation_std.png", dpi=120)

    # sigmoid chain: the derivative is at most 0.25, so gradients shrink geometrically
    print("max sigmoid'(z) = 0.25 -> after 10 layers the factor is at most 0.25**10 =", 0.25 ** 10)
    print("saved img/init_activation_std.png")
