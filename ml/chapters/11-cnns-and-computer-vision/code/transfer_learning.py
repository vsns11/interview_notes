"""Transfer learning mechanics without downloading anything.

Source task : clean 8x8 digits (half of the dataset, ~900 images) -> pretrain a CNN.
Target task : the other half, but 'scanned by a different device' (Gaussian noise added),
              with only k labelled images per class.
Strategies  : from scratch  /  frozen backbone + new head (feature extraction)  /
              fine-tune the whole pretrained model with a small learning rate
Real projects do the same with ImageNet weights from torchvision (see README)."""
import numpy as np
import torch
from torch import nn
from sklearn.datasets import load_digits
from sklearn.model_selection import train_test_split

torch.set_num_threads(1)


def backbone():
    return nn.Sequential(
        nn.Conv2d(1, 16, 3, padding=1), nn.ReLU(), nn.MaxPool2d(2),
        nn.Conv2d(16, 32, 3, padding=1), nn.ReLU(), nn.MaxPool2d(2),
        nn.Flatten())                                    # -> 128 features


def train(model, params, X, y, epochs, lr):
    opt = torch.optim.Adam(params, lr=lr)
    for _ in range(epochs):
        model.train()
        for idx in torch.randperm(len(X)).split(32):
            loss = nn.functional.cross_entropy(model(X[idx]), y[idx])
            opt.zero_grad(); loss.backward(); opt.step()


def acc(model, X, y):
    model.eval()
    with torch.no_grad():
        return (model(X).argmax(1) == y).float().mean().item()


def make_model(strategy, pretrained):
    """pretrained = state_dict of the whole source model (backbone '0.*' + head '1.*')."""
    bb, head = backbone(), nn.Linear(128, 10)
    model = nn.Sequential(bb, head)
    if strategy == "frozen backbone":                    # pretrained backbone, NEW head
        bb.load_state_dict({k[2:]: v for k, v in pretrained.items() if k.startswith("0.")})
    elif strategy == "fine-tune all":                    # start from the whole source model
        model.load_state_dict(pretrained)
    if strategy == "frozen backbone":
        for p in bb.parameters():
            p.requires_grad = False                      # freeze: no gradients, no updates
        return model, head.parameters()
    if strategy == "fine-tune all":
        return model, [{"params": model.parameters(), "lr": 1e-4}]   # small lr: nudge, don't overwrite
    return model, model.parameters()


if __name__ == "__main__":
    X, y = load_digits(return_X_y=True)
    X = (X / 16.0).reshape(-1, 1, 8, 8).astype(np.float32)
    Xs, Xt, ys, yt = map(torch.tensor, train_test_split(X, y, test_size=0.5, stratify=y, random_state=0))
    Xt = Xt + 0.3 * torch.randn(Xt.shape, generator=torch.Generator().manual_seed(1))   # domain shift

    torch.manual_seed(0)
    pre = backbone()
    src_model = nn.Sequential(pre, nn.Linear(128, 10))
    train(src_model, src_model.parameters(), Xs, ys, epochs=15, lr=3e-3)
    print(f"source model: train acc on clean digits {acc(src_model, Xs, ys):.3f}, "
          f"acc on noisy target digits with NO adaptation {acc(src_model, Xt, yt):.3f}")
    state = {k: v.clone() for k, v in src_model.state_dict().items()}

    strategies = ["from scratch", "frozen backbone", "fine-tune all"]
    print(f"\n{'labels/class':>12s} " + " ".join(f"{s:>16s}" for s in strategies) + "   (mean test acc over 3 seeds)")
    for k in [2, 5, 10, 30]:
        scores = {s: [] for s in strategies}
        for seed in range(3):
            rng = np.random.default_rng(seed)
            idx = np.concatenate([rng.choice(np.where(yt.numpy() == c)[0], k, replace=False) for c in range(10)])
            test = np.ones(len(yt), bool); test[idx] = False
            for s in strategies:
                torch.manual_seed(seed)
                model, params = make_model(s, state)
                train(model, params, Xt[idx], yt[idx], epochs=60, lr=3e-3)
                scores[s].append(acc(model, Xt[test], yt[test]))
        print(f"{k:12d} " + " ".join(f"{np.mean(scores[s]):16.3f}" for s in strategies))
    frozen, _ = make_model("frozen backbone", state)
    print("\ntrainable params: from scratch / fine-tune =", sum(p.numel() for p in frozen.parameters()),
          "| frozen backbone =", sum(p.numel() for p in frozen.parameters() if p.requires_grad))
