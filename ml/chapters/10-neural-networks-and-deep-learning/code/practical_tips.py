"""Practical deep-learning checks on sklearn digits (8x8 images flattened to 64 features):
1) overfit one small batch  2) LR range test ('LR finder')  3) loss curves for bad/good LRs
4) class imbalance: plain loss vs class-weighted loss."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import torch
from torch import nn
from sklearn.datasets import load_digits, make_classification
from sklearn.metrics import recall_score
from sklearn.model_selection import train_test_split

IMG = Path(__file__).resolve().parent.parent / "img"
torch.set_num_threads(1)


def digits():
    X, y = load_digits(return_X_y=True)
    X = X / 16.0                                     # pixel values 0..16 -> 0..1 (min-max scaling)
    Xtr, Xva, ytr, yva = train_test_split(X, y, test_size=0.25, stratify=y, random_state=0)
    f = lambda a: torch.tensor(a, dtype=torch.float32)
    return f(Xtr), torch.tensor(ytr), f(Xva), torch.tensor(yva)


def model():
    return nn.Sequential(nn.Linear(64, 128), nn.ReLU(), nn.Linear(128, 10))


def overfit_one_batch(Xtr, ytr):
    torch.manual_seed(0)
    m = model(); opt = torch.optim.Adam(m.parameters(), lr=1e-2)
    xb, yb = Xtr[:32], ytr[:32]
    for step in range(201):
        loss = nn.functional.cross_entropy(m(xb), yb)
        opt.zero_grad(); loss.backward(); opt.step()
        if step in (0, 50, 200):
            print(f"  step {step:3d}  loss on the single batch = {loss.item():.4f}")
    print(f"  expected start ~ ln(10) = {np.log(10):.3f} (uniform guess over 10 classes)")


def lr_finder(Xtr, ytr, lo=1e-5, hi=10.0, steps=150):
    torch.manual_seed(0)
    m = model(); opt = torch.optim.SGD(m.parameters(), lr=lo, momentum=0.9)
    mult = (hi / lo) ** (1 / (steps - 1))
    lrs, losses, smooth = [], [], None
    for i in range(steps):
        idx = torch.randint(0, len(Xtr), (64,))
        loss = nn.functional.cross_entropy(m(Xtr[idx]), ytr[idx])
        opt.zero_grad(); loss.backward(); opt.step()
        smooth = loss.item() if smooth is None else 0.9 * smooth + 0.1 * loss.item()  # EMA smoothing
        lrs.append(opt.param_groups[0]["lr"]); losses.append(smooth)
        if smooth > 4 * min(losses):                # stop once loss explodes
            break
        opt.param_groups[0]["lr"] *= mult           # exponential LR increase
    return np.array(lrs), np.array(losses)


def train_curve(Xtr, ytr, Xva, yva, lr, epochs=30):
    torch.manual_seed(0)
    m = model(); opt = torch.optim.SGD(m.parameters(), lr=lr, momentum=0.9)
    tr, va = [], []
    for _ in range(epochs):
        for idx in torch.randperm(len(Xtr)).split(64):
            loss = nn.functional.cross_entropy(m(Xtr[idx]), ytr[idx])
            opt.zero_grad(); loss.backward(); opt.step()
        with torch.no_grad():
            tr.append(nn.functional.cross_entropy(m(Xtr), ytr).item())
            va.append(nn.functional.cross_entropy(m(Xva), yva).item())
    return tr, va


def imbalance_demo():
    X, y = make_classification(n_samples=4000, n_features=10, n_informative=4, weights=[0.95, 0.05],
                               class_sep=0.8, random_state=0)
    Xtr, Xte, ytr, yte = train_test_split(X, y, test_size=0.5, stratify=y, random_state=0)
    f = lambda a: torch.tensor(a, dtype=torch.float32)
    counts = np.bincount(ytr)
    print(f"  class counts in train: {counts.tolist()}  (minority = {counts[1] / counts.sum():.1%})")
    for name, w in [("plain cross-entropy", None),
                    ("class-weighted (n/(k*n_c))", torch.tensor(len(ytr) / (2 * counts), dtype=torch.float32))]:
        torch.manual_seed(0)
        m = nn.Sequential(nn.Linear(10, 32), nn.ReLU(), nn.Linear(32, 2))
        opt = torch.optim.Adam(m.parameters(), lr=1e-2)
        for _ in range(200):
            loss = nn.functional.cross_entropy(m(f(Xtr)), torch.tensor(ytr), weight=w)
            opt.zero_grad(); loss.backward(); opt.step()
        pred = m(f(Xte)).argmax(1).numpy()
        print(f"  {name:28s} accuracy={np.mean(pred == yte):.3f}  minority recall={recall_score(yte, pred):.3f}")
    print(f"  (always predicting the majority class would score accuracy {np.mean(yte == 0):.3f})")


if __name__ == "__main__":
    Xtr, ytr, Xva, yva = digits()
    print("1) Overfit one batch of 32 examples")
    overfit_one_batch(Xtr, ytr)

    print("2) LR range test (SGD+momentum, lr grows 1e-5 -> 10)")
    lrs, losses = lr_finder(Xtr, ytr)
    best = lrs[np.argmin(losses)]
    print(f"  lowest smoothed loss at lr={best:.3g}; loss blew up by lr={lrs[-1]:.3g}; "
          f"rule of thumb: pick ~1/10 of the minimum -> {best / 10:.2g}")

    print("3) Training curves for three learning rates (SGD+momentum, 30 epochs)")
    curves = {}
    for lr in [1e-4, 5e-2, 3.0]:
        tr, va = train_curve(Xtr, ytr, Xva, yva, lr)
        curves[lr] = (tr, va)
        print(f"  lr={lr:<6g} final train loss {tr[-1]:.3f}  val loss {va[-1]:.3f}")

    print("4) Class imbalance (95% / 5%)")
    imbalance_demo()

    fig, axes = plt.subplots(1, 2, figsize=(11, 4))
    axes[0].semilogx(lrs, losses); axes[0].axvline(best / 10, color="k", ls="--", lw=1, label="chosen lr (min/10)")
    axes[0].set_xlabel("learning rate (log)"); axes[0].set_ylabel("smoothed training loss"); axes[0].set_title("LR range test"); axes[0].legend()
    for lr, (tr, va) in curves.items():
        l, = axes[1].plot(tr, label=f"lr={lr:g} train")
        axes[1].plot(va, ls="--", color=l.get_color(), label=f"lr={lr:g} val")
    axes[1].set_yscale("log"); axes[1].set_xlabel("epoch"); axes[1].set_title("Too small / good / too large LR"); axes[1].legend(fontsize=7)
    fig.tight_layout(); fig.savefig(IMG / "lr_finder_and_curves.png", dpi=120)
    print("saved img/lr_finder_and_curves.png")
