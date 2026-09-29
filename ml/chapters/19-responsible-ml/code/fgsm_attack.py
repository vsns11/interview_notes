"""Adversarial examples with FGSM (Fast Gradient Sign Method) on a small MLP
trained on sklearn's 8x8 digits - plus FGSM adversarial training as a defence."""
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import torch
import torch.nn as nn
from sklearn.datasets import load_digits
from sklearn.model_selection import train_test_split

torch.manual_seed(0)
EPS_GRID = [0.0, 0.05, 0.1, 0.2, 0.3]


def make_model():
    return nn.Sequential(nn.Linear(64, 128), nn.ReLU(), nn.Linear(128, 10))


def fgsm(model, x, y, eps):
    """x_adv = clip(x + eps * sign(d loss / d x), 0, 1): one step that increases the loss most
    under an L-infinity budget of eps per pixel."""
    x = x.clone().requires_grad_(True)
    loss = nn.functional.cross_entropy(model(x), y)
    grad, = torch.autograd.grad(loss, x)
    return (x + eps * grad.sign()).clamp(0, 1).detach()


def train(model, X, y, adversarial=False, epochs=60, eps=0.1):
    opt = torch.optim.Adam(model.parameters(), lr=1e-2)
    for _ in range(epochs):
        for idx in torch.randperm(len(X)).split(128):
            xb, yb = X[idx], y[idx]
            if adversarial:                                   # train on a clean + FGSM mix
                xb = torch.cat([xb, fgsm(model, xb, yb, eps)]); yb = torch.cat([yb, yb])
            opt.zero_grad()
            nn.functional.cross_entropy(model(xb), yb).backward()
            opt.step()
    return model


def acc(model, X, y):
    with torch.no_grad():
        return (model(X).argmax(1) == y).float().mean().item()


def main():
    d = load_digits()
    X = torch.tensor(d.data / 16.0, dtype=torch.float32)       # pixels in [0, 1]
    y = torch.tensor(d.target)
    Xtr, Xte, ytr, yte = train_test_split(X, y, test_size=0.3, random_state=0, stratify=y)

    plain = train(make_model(), Xtr, ytr)
    robust = train(make_model(), Xtr, ytr, adversarial=True)

    print("eps    plain-model acc   adversarially-trained acc")
    for eps in EPS_GRID:
        a1 = acc(plain, fgsm(plain, Xte, yte, eps), yte)
        a2 = acc(robust, fgsm(robust, Xte, yte, eps), yte)
        print(f"{eps:<5}  {a1:.3f}             {a2:.3f}")

    # picture: one digit, clean vs adversarial
    i = 0
    x0, y0 = Xte[i:i + 1], yte[i:i + 1]
    x_adv = fgsm(plain, x0, y0, 0.2)
    with torch.no_grad():
        p_clean = plain(x0).softmax(1)[0]; p_adv = plain(x_adv).softmax(1)[0]
    print(f"\nexample: true={y0.item()}  clean pred={p_clean.argmax().item()} ({p_clean.max():.2f})"
          f"  adversarial pred={p_adv.argmax().item()} ({p_adv.max():.2f})")
    fig, ax = plt.subplots(1, 3, figsize=(7.5, 2.8))
    for a, img, title in zip(ax, [x0, x_adv - x0, x_adv],
                             [f"clean: pred {p_clean.argmax().item()}", "perturbation (eps=0.2)",
                              f"adversarial: pred {p_adv.argmax().item()}"]):
        if "pert" in title:
            a.imshow(img.reshape(8, 8).numpy(), cmap="bwr", vmin=-0.2, vmax=0.2)
        else:
            a.imshow(img.reshape(8, 8).numpy(), cmap="gray", vmin=0, vmax=1)
        a.set_title(title); a.axis("off")
    fig.tight_layout(); fig.savefig("../img/fgsm_example.png", dpi=130)


if __name__ == "__main__":
    main()
