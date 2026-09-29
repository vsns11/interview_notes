"""PyTorch essentials: tensors, autograd, nn.Module, Dataset/DataLoader, the training loop,
evaluation, and saving/loading weights. Task: 3-class classification of sklearn's wine dataset."""
import io

import torch
from torch import nn
from torch.utils.data import DataLoader, TensorDataset
from sklearn.datasets import load_wine
from sklearn.model_selection import train_test_split

torch.manual_seed(0)


def tensor_tour():
    a = torch.tensor([[1.0, 2.0], [3.0, 4.0]])
    print("tensor:", a.tolist(), "shape", tuple(a.shape), "dtype", a.dtype, "device", a.device)
    print("a @ a =", (a @ a).tolist(), "| a * a =", (a * a).tolist(), "| a.sum(dim=0) =", a.sum(0).tolist())
    print("broadcast a + [10, 20] =", (a + torch.tensor([10.0, 20.0])).tolist())
    # autograd: y = x^2 + 3x at x=2  ->  dy/dx = 2x + 3 = 7
    x = torch.tensor(2.0, requires_grad=True)
    y = x ** 2 + 3 * x
    y.backward()
    print("autograd dy/dx at x=2:", x.grad.item())


class WineNet(nn.Module):
    def __init__(self, n_in, n_hidden, n_classes):
        super().__init__()                           # registers sub-modules and parameters
        self.net = nn.Sequential(
            nn.Linear(n_in, n_hidden), nn.ReLU(),
            nn.Linear(n_hidden, n_classes),          # raw scores (logits), no softmax here
        )

    def forward(self, x):
        return self.net(x)


def evaluate(model, loader, loss_fn):
    model.eval()
    total, correct, loss_sum = 0, 0, 0.0
    with torch.no_grad():                            # no graph building during evaluation
        for xb, yb in loader:
            logits = model(xb)
            loss_sum += loss_fn(logits, yb).item() * len(yb)
            correct += (logits.argmax(1) == yb).sum().item()
            total += len(yb)
    return loss_sum / total, correct / total


if __name__ == "__main__":
    tensor_tour()

    X, y = load_wine(return_X_y=True)
    Xtr, Xte, ytr, yte = train_test_split(X, y, test_size=0.3, stratify=y, random_state=0)
    mean, std = Xtr.mean(0), Xtr.std(0)             # standardize with TRAIN statistics only
    Xtr, Xte = (Xtr - mean) / std, (Xte - mean) / std
    to_t = lambda a, dt: torch.tensor(a, dtype=dt)
    train_ds = TensorDataset(to_t(Xtr, torch.float32), to_t(ytr, torch.long))
    test_ds = TensorDataset(to_t(Xte, torch.float32), to_t(yte, torch.long))
    train_dl = DataLoader(train_ds, batch_size=32, shuffle=True)
    test_dl = DataLoader(test_ds, batch_size=256)

    model = WineNet(13, 32, 3)
    print(model)
    print("trainable parameters:", sum(p.numel() for p in model.parameters() if p.requires_grad))
    loss_fn = nn.CrossEntropyLoss()                  # = log-softmax + negative log-likelihood
    opt = torch.optim.Adam(model.parameters(), lr=1e-2)

    for epoch in range(1, 31):
        model.train()
        for xb, yb in train_dl:
            logits = model(xb)                       # 1. forward
            loss = loss_fn(logits, yb)               # 2. loss
            opt.zero_grad()                          # 3. clear old gradients
            loss.backward()                          # 4. backprop
            opt.step()                               # 5. update weights
        if epoch in (1, 5, 10, 30):
            tr = evaluate(model, train_dl, loss_fn); te = evaluate(model, test_dl, loss_fn)
            print(f"epoch {epoch:2d}  train loss {tr[0]:.3f} acc {tr[1]:.3f} | test loss {te[0]:.3f} acc {te[1]:.3f}")

    buf = io.BytesIO()                               # in real code: torch.save(..., "wine.pt")
    torch.save(model.state_dict(), buf); buf.seek(0)
    clone = WineNet(13, 32, 3); clone.load_state_dict(torch.load(buf))
    print("reloaded model test acc:", evaluate(clone, test_dl, loss_fn)[1])
    probs = torch.softmax(clone(test_ds[:3][0]), dim=1)
    print("class probabilities for 3 test wines:\n", probs.detach().numpy().round(3), "true:", test_ds[:3][1].tolist())
