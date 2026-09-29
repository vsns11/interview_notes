"""Residual (skip) connections: (1) gradient reaching the first layer of deep plain vs residual CNNs,
(2) training a deep plain CNN vs a residual CNN of the same depth on 8x8 digits."""
import torch
from torch import nn
from sklearn.datasets import load_digits

torch.set_num_threads(1)


class PlainBlock(nn.Module):
    def __init__(self, c):
        super().__init__()
        self.conv = nn.Conv2d(c, c, 3, padding=1, bias=False)
        self.bn = nn.BatchNorm2d(c)

    def forward(self, x):
        return torch.relu(self.bn(self.conv(x)))


class ResidualBlock(nn.Module):
    """y = ReLU(x + F(x)), F = conv-BN-ReLU-conv-BN (the ResNet 'basic block')."""
    def __init__(self, c):
        super().__init__()
        self.f = nn.Sequential(
            nn.Conv2d(c, c, 3, padding=1, bias=False), nn.BatchNorm2d(c), nn.ReLU(),
            nn.Conv2d(c, c, 3, padding=1, bias=False), nn.BatchNorm2d(c))

    def forward(self, x):
        return torch.relu(x + self.f(x))                 # the skip connection


def make_net(blocks, n_blocks, c=16):
    stem = nn.Conv2d(1, c, 3, padding=1)
    return nn.Sequential(stem, *[blocks(c) for _ in range(n_blocks)],
                         nn.AdaptiveAvgPool2d(1), nn.Flatten(), nn.Linear(c, 10))


def first_layer_grad(blocks, n_blocks):
    torch.manual_seed(0)
    net = make_net(blocks, n_blocks); stem = net[0]
    x = torch.randn(32, 1, 16, 16); y = torch.randint(0, 10, (32,))
    nn.functional.cross_entropy(net(x), y).backward()
    return stem.weight.grad.norm().item()


if __name__ == "__main__":
    print(f"{'conv layers':>11s} {'plain |grad|':>13s} {'residual |grad|':>16s}")
    for depth in [2, 10, 30, 60]:
        g_plain = first_layer_grad(PlainBlock, depth)
        g_res = first_layer_grad(ResidualBlock, depth // 2)     # 2 convs per residual block
        print(f"{depth:11d} {g_plain:13.2e} {g_res:16.2e}")

    X, y = load_digits(return_X_y=True)
    X = torch.tensor((X / 16.0).reshape(-1, 1, 8, 8), dtype=torch.float32); y = torch.tensor(y)
    print("\nTraining 30-conv-layer nets on digits (SGD+momentum lr 0.05, 5 epochs):")
    for name, blocks, n in [("plain", PlainBlock, 30), ("residual", ResidualBlock, 15)]:
        torch.manual_seed(0)
        net = make_net(blocks, n)
        opt = torch.optim.SGD(net.parameters(), lr=0.05, momentum=0.9)
        for epoch in range(5):
            net.train()
            for idx in torch.randperm(len(X)).split(64):
                loss = nn.functional.cross_entropy(net(X[idx]), y[idx])
                opt.zero_grad(); loss.backward(); opt.step()
        net.eval()
        with torch.no_grad():
            print(f"  {name:8s} training accuracy {(net(X).argmax(1) == y).float().mean():.3f}  "
                  f"training loss {nn.functional.cross_entropy(net(X), y):.3f}")
