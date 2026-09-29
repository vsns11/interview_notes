"""Compare SGD, SGD+momentum, RMSProp and Adam (torch.optim):
(a) on a 2-D 'ravine' loss surface, (b) training an MLP on two moons."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import torch
from torch import nn
from sklearn.datasets import make_moons

IMG = Path(__file__).resolve().parent.parent / "img"


def ravine(p):                        # elongated bowl: steep in y, shallow in x
    return 0.05 * p[0] ** 2 + 2.0 * p[1] ** 2


RAVINE_OPTS = {   # (class, kwargs) - each lr picked near the largest value that behaves well
    "SGD": (torch.optim.SGD, dict(lr=0.45)),
    "SGD+momentum(0.9)": (torch.optim.SGD, dict(lr=0.05, momentum=0.9)),
    "RMSProp": (torch.optim.RMSprop, dict(lr=0.3)),
    "Adam": (torch.optim.Adam, dict(lr=0.5)),
}


if __name__ == "__main__":
    torch.manual_seed(0)
    # ---------- (a) trajectories ----------
    fig, axes = plt.subplots(1, 2, figsize=(12, 4.5))
    xs, ys = np.meshgrid(np.linspace(-10.5, 1.5, 200), np.linspace(-2.5, 2.5, 200))
    axes[0].contour(xs, ys, 0.05 * xs ** 2 + 2 * ys ** 2, levels=25, cmap="Greys", linewidths=0.6)
    print("(a) ravine f = 0.05 x^2 + 2 y^2, start (-10, 2), 60 steps")
    for name, (cls, kw) in RAVINE_OPTS.items():
        p = torch.tensor([-10.0, 2.0], requires_grad=True)
        opt = cls([p], **kw)
        path = [p.detach().clone()]
        for _ in range(60):
            opt.zero_grad(); ravine(p).backward(); opt.step()
            path.append(p.detach().clone())
        path = torch.stack(path).numpy()
        axes[0].plot(path[:, 0], path[:, 1], "o-", ms=2, label=name)
        print(f"  {name:18s} final point {path[-1].round(3)}  loss {ravine(torch.tensor(path[-1])):.2e}")
    axes[0].set_title("Optimizer paths on a ravine"); axes[0].legend(fontsize=8)

    # ---------- (b) MLP on moons ----------
    X, y = make_moons(n_samples=1000, noise=0.2, random_state=0)
    X = torch.tensor(X, dtype=torch.float32); y = torch.tensor(y, dtype=torch.float32)
    print("(b) MLP 2-32-32-1 on moons, 200 full-batch epochs")
    for name, kw in [("SGD", dict(lr=0.1)), ("SGD+momentum(0.9)", dict(lr=0.1, momentum=0.9)),
                     ("RMSProp", dict(lr=0.01)), ("Adam", dict(lr=0.01))]:
        torch.manual_seed(0)
        model = nn.Sequential(nn.Linear(2, 32), nn.ReLU(), nn.Linear(32, 32), nn.ReLU(), nn.Linear(32, 1))
        cls = torch.optim.SGD if name.startswith("SGD") else getattr(torch.optim, "RMSprop" if name == "RMSProp" else name)
        opt = cls(model.parameters(), **kw)
        losses = []
        for _ in range(200):
            loss = nn.functional.binary_cross_entropy_with_logits(model(X).squeeze(1), y)
            opt.zero_grad(); loss.backward(); opt.step(); losses.append(loss.item())
        axes[1].plot(losses, label=name)
        print(f"  {name:18s} loss after 20 epochs {losses[19]:.3f}, after 200 {losses[-1]:.3f}")
    axes[1].set_title("Training loss, MLP on moons"); axes[1].set_xlabel("epoch"); axes[1].set_yscale("log"); axes[1].legend(fontsize=8)
    fig.tight_layout(); fig.savefig(IMG / "optimizers.png", dpi=120)
    print("saved img/optimizers.png")
