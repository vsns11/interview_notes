"""A small CNN on scikit-learn's 8x8 digits (1,797 images), trained on CPU.
Compared with an MLP of similar size; optional data augmentation (random 1-pixel shifts)."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import torch
from torch import nn
from torch.utils.data import DataLoader, TensorDataset
from sklearn.datasets import load_digits
from sklearn.metrics import confusion_matrix
from sklearn.model_selection import train_test_split

IMG = Path(__file__).resolve().parent.parent / "img"
torch.set_num_threads(1)


class SmallCNN(nn.Module):
    def __init__(self):
        super().__init__()
        self.features = nn.Sequential(
            nn.Conv2d(1, 16, kernel_size=3, padding=1),   # 1x8x8  -> 16x8x8
            nn.BatchNorm2d(16), nn.ReLU(),
            nn.MaxPool2d(2),                              # 16x8x8 -> 16x4x4
            nn.Conv2d(16, 32, kernel_size=3, padding=1),  # -> 32x4x4
            nn.BatchNorm2d(32), nn.ReLU(),
            nn.MaxPool2d(2),                              # -> 32x2x2
        )
        self.classifier = nn.Sequential(
            nn.Flatten(),                                 # -> 128
            nn.Dropout(0.3),
            nn.Linear(32 * 2 * 2, 10),                    # 10 class logits
        )

    def forward(self, x):
        return self.classifier(self.features(x))


def mlp():
    return nn.Sequential(nn.Flatten(), nn.Linear(64, 96), nn.ReLU(), nn.Dropout(0.3), nn.Linear(96, 10))


def random_shift(x, max_shift=1):
    """Augmentation: shift each image by up to 1 pixel in x and y (zero-filled)."""
    out = torch.zeros_like(x)
    for i in range(len(x)):
        dy, dx = np.random.randint(-max_shift, max_shift + 1, size=2)
        out[i] = torch.roll(x[i], shifts=(dy, dx), dims=(1, 2))
        if dy > 0: out[i, :, :dy] = 0
        if dy < 0: out[i, :, dy:] = 0
        if dx > 0: out[i, :, :, :dx] = 0
        if dx < 0: out[i, :, :, dx:] = 0
    return out


def fit(model, train_dl, Xte, yte, epochs=40, augment=False):
    opt = torch.optim.AdamW(model.parameters(), lr=3e-3, weight_decay=1e-3)
    sched = torch.optim.lr_scheduler.OneCycleLR(opt, max_lr=3e-3, total_steps=epochs * len(train_dl))
    hist = []
    for _ in range(epochs):
        model.train()
        for xb, yb in train_dl:
            if augment:
                xb = random_shift(xb)
            loss = nn.functional.cross_entropy(model(xb), yb)
            opt.zero_grad(); loss.backward(); opt.step(); sched.step()
        model.eval()
        with torch.no_grad():
            hist.append((model(Xte).argmax(1) == yte).float().mean().item())
    return hist


if __name__ == "__main__":
    torch.manual_seed(0); np.random.seed(0)
    X, y = load_digits(return_X_y=True)
    X = (X / 16.0).reshape(-1, 1, 8, 8).astype(np.float32)         # N x C x H x W
    Xtr, Xte, ytr, yte = train_test_split(X, y, test_size=0.3, stratify=y, random_state=0)
    Xtr, Xte = torch.tensor(Xtr), torch.tensor(Xte)
    ytr, yte = torch.tensor(ytr), torch.tensor(yte)
    train_dl = DataLoader(TensorDataset(Xtr, ytr), batch_size=64, shuffle=True)
    print(f"train {tuple(Xtr.shape)}  test {tuple(Xte.shape)}")

    results = {}
    for name, make, aug in [("MLP", mlp, False), ("CNN", SmallCNN, False), ("CNN + shift augmentation", SmallCNN, True)]:
        torch.manual_seed(0); np.random.seed(0)
        model = make()
        n_params = sum(p.numel() for p in model.parameters())
        hist = fit(model, train_dl, Xte, yte, augment=aug)
        results[name] = (model, hist)
        print(f"{name:26s} params={n_params:5d}  test accuracy={hist[-1]:.4f}")

    # robustness: shift every test image 1 pixel right - CNNs + augmentation cope better
    Xshift = torch.roll(Xte, shifts=1, dims=3); Xshift[:, :, :, 0] = 0
    for name, (model, _) in results.items():
        with torch.no_grad():
            acc = (model(Xshift).argmax(1) == yte).float().mean().item()
        print(f"{name:26s} accuracy on test images shifted 1px right = {acc:.4f}")

    cnn = results["CNN + shift augmentation"][0]
    with torch.no_grad():
        pred = cnn(Xte).argmax(1)
    cm = confusion_matrix(yte, pred)
    off = [(int(cm[i, j]), i, j) for i in range(10) for j in range(10) if i != j and cm[i, j] > 0]
    print("misclassified (count, true, predicted):", sorted(off, reverse=True)[:5])

    fig, axes = plt.subplots(1, 3, figsize=(14, 4))
    for name, (_, hist) in results.items():
        axes[0].plot(hist, label=name)
    axes[0].set_ylim(0.8, 1.0); axes[0].set_xlabel("epoch"); axes[0].set_ylabel("test accuracy"); axes[0].legend(); axes[0].set_title("Digits 8x8")
    w = cnn.features[0].weight.detach()[:, 0]                         # 16 first-layer 3x3 kernels
    axes[1].imshow(torch.cat([torch.cat(list(w[r * 4:(r + 1) * 4]), 1) for r in range(4)], 0), cmap="RdBu_r")
    axes[1].set_title("16 learned 3x3 first-layer kernels"); axes[1].axis("off")
    axes[2].imshow(cm, cmap="Blues"); axes[2].set_title("Confusion matrix (CNN + aug)")
    axes[2].set_xlabel("predicted"); axes[2].set_ylabel("true"); axes[2].set_xticks(range(10)); axes[2].set_yticks(range(10))
    fig.tight_layout(); fig.savefig(IMG / "digits_cnn.png", dpi=110)
    print("saved img/digits_cnn.png")
