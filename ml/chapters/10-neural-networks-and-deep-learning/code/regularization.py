"""Overfitting a big MLP on a small noisy dataset, and how regularizers help:
dropout, weight decay (AdamW), batch norm, input-noise augmentation, early stopping."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import torch
from torch import nn
from sklearn.datasets import make_moons

IMG = Path(__file__).resolve().parent.parent / "img"
EPOCHS = 400
torch.set_num_threads(1)   # tiny models: threading overhead > benefit


def data():
    Xtr, ytr = make_moons(n_samples=80, noise=0.35, random_state=1)
    Xva, yva = make_moons(n_samples=2000, noise=0.35, random_state=2)
    f = lambda a: torch.tensor(a, dtype=torch.float32)
    return f(Xtr), f(ytr), f(Xva), f(yva)


def mlp(dropout=0.0, batchnorm=False, width=256):
    layers, d = [], 2
    for _ in range(3):
        layers.append(nn.Linear(d, width))
        if batchnorm:
            layers.append(nn.BatchNorm1d(width))
        layers.append(nn.ReLU())
        if dropout:
            layers.append(nn.Dropout(dropout))
        d = width
    layers.append(nn.Linear(d, 1))
    return nn.Sequential(*layers)


def run(name, dropout=0.0, wd=0.0, batchnorm=False, noise=0.0, patience=None):
    torch.manual_seed(0)
    Xtr, ytr, Xva, yva = data()
    model = mlp(dropout, batchnorm)
    opt = torch.optim.AdamW(model.parameters(), lr=1e-3, weight_decay=wd)
    lossf = nn.BCEWithLogitsLoss()
    tr_hist, va_hist = [], []
    best, best_state, bad = float("inf"), None, 0
    for epoch in range(EPOCHS):
        model.train()                                  # dropout ON, BN uses batch stats
        for idx in torch.randperm(len(Xtr)).split(16):  # mini-batches of 16
            xb = Xtr[idx] + noise * torch.randn_like(Xtr[idx])   # augmentation: jitter inputs
            loss = lossf(model(xb).squeeze(1), ytr[idx])
            opt.zero_grad(); loss.backward(); opt.step()
        model.eval()                                   # dropout OFF, BN uses running stats
        with torch.no_grad():
            tr_hist.append(lossf(model(Xtr).squeeze(1), ytr).item())
            va = lossf(model(Xva).squeeze(1), yva).item(); va_hist.append(va)
        if patience is not None:
            if va < best - 1e-4:
                best, bad = va, 0
                best_state = {k: v.clone() for k, v in model.state_dict().items()}
            else:
                bad += 1
                if bad >= patience:
                    model.load_state_dict(best_state)   # restore best weights
                    break
    model.eval()
    with torch.no_grad():
        acc = lambda X, y: ((model(X).squeeze(1) > 0).float() == y).float().mean().item()
        va_final = lossf(model(Xva).squeeze(1), yva).item()
        print(f"{name:28s} epochs={len(tr_hist):3d}  train acc={acc(Xtr, ytr):.3f}  "
              f"val acc={acc(Xva, yva):.3f}  val loss={va_final:.3f}")
    return tr_hist, va_hist


if __name__ == "__main__":
    runs = {
        "No regularization": {},
        "Dropout p=0.5": dict(dropout=0.5),
        "Weight decay 1.0 (AdamW)": dict(wd=1.0),
        "BatchNorm": dict(batchnorm=True),
        "Input-noise augmentation": dict(noise=0.15),
        "Early stopping (patience 30)": dict(patience=30),
    }
    hists = {n: run(n, **kw) for n, kw in runs.items()}
    fig, axes = plt.subplots(1, 2, figsize=(11, 4))
    tr, va = hists["No regularization"]
    axes[0].plot(tr, label="train"); axes[0].plot(va, label="validation")
    es = len(hists["Early stopping (patience 30)"][0])
    axes[0].axvline(es - 30, color="k", ls="--", lw=1, label="early-stopping best epoch")
    axes[0].set_title("No regularization: classic overfitting"); axes[0].set_xlabel("epoch"); axes[0].set_ylabel("BCE loss")
    axes[0].set_ylim(0, 1.5); axes[0].legend(fontsize=8)
    for n, (tr, va) in hists.items():
        axes[1].plot(va, label=n)
    axes[1].set_title("Validation loss by technique"); axes[1].set_xlabel("epoch"); axes[1].set_ylim(0, 1.5); axes[1].legend(fontsize=7)
    fig.tight_layout(); fig.savefig(IMG / "regularization_curves.png", dpi=120)
    print("saved img/regularization_curves.png")
