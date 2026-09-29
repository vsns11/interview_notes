"""Plot common learning-rate schedules using torch.optim.lr_scheduler."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import torch
from torch.optim import lr_scheduler as S

IMG = Path(__file__).resolve().parent.parent / "img"
STEPS = 100


def trace(make_sched, base_lr=0.1):
    p = torch.nn.Parameter(torch.zeros(1))
    opt = torch.optim.SGD([p], lr=base_lr)
    sched = make_sched(opt)
    lrs = []
    for _ in range(STEPS):
        lrs.append(opt.param_groups[0]["lr"])
        opt.step()          # (a real loop would compute loss + backward first)
        sched.step()        # advance the schedule AFTER optimizer.step()
    return lrs


if __name__ == "__main__":
    schedules = {
        "Constant": lambda o: S.ConstantLR(o, factor=1.0, total_iters=0),
        "StepLR (x0.5 every 25)": lambda o: S.StepLR(o, step_size=25, gamma=0.5),
        "ExponentialLR (0.97)": lambda o: S.ExponentialLR(o, gamma=0.97),
        "CosineAnnealingLR": lambda o: S.CosineAnnealingLR(o, T_max=STEPS),
        "Warmup 10 + cosine": lambda o: S.SequentialLR(
            o, [S.LinearLR(o, start_factor=0.01, total_iters=10), S.CosineAnnealingLR(o, T_max=STEPS - 10)],
            milestones=[10]),
        "OneCycleLR": lambda o: S.OneCycleLR(o, max_lr=0.1, total_steps=STEPS),
    }
    plt.figure(figsize=(8, 4))
    for name, mk in schedules.items():
        lrs = trace(mk)
        plt.plot(lrs, label=name)
        print(f"{name:24s} lr at steps 0/10/50/99: {lrs[0]:.4f} {lrs[10]:.4f} {lrs[50]:.4f} {lrs[99]:.5f}")
    plt.xlabel("step"); plt.ylabel("learning rate"); plt.legend(fontsize=8); plt.grid(alpha=0.3)
    plt.title("Learning-rate schedules (base lr 0.1)"); plt.tight_layout()
    plt.savefig(IMG / "lr_schedules.png", dpi=120)
    print("saved img/lr_schedules.png")
