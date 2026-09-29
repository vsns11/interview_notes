"""Small PyTorch LSTMs on two toy sequence tasks (CPU, seconds).

A) Forecasting: read the last 30 values of a noisy sine wave, predict the next value (many-to-one regression).
B) Long memory: read T random symbols (1..8) and output the FIRST one. The information must survive
   T steps, so this separates a vanilla RNN from gated LSTM/GRU cells. Chance accuracy = 1/8."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import torch
from torch import nn

IMG = Path(__file__).resolve().parent.parent / "img"
torch.set_num_threads(1)


# ---------------- A) sine-wave forecasting ----------------
class Forecaster(nn.Module):
    def __init__(self, hidden=32):
        super().__init__()
        self.lstm = nn.LSTM(input_size=1, hidden_size=hidden, batch_first=True)  # (batch, time, feat)
        self.head = nn.Linear(hidden, 1)

    def forward(self, x):                       # x: (batch, 30, 1)
        out, (h_n, c_n) = self.lstm(x)          # out: every step's h; h_n/c_n: final hidden & cell state
        return self.head(out[:, -1])            # predict from the last hidden state


def sine_windows(n_points=1200, window=30, seed=0):
    g = torch.Generator().manual_seed(seed)
    t = torch.arange(n_points, dtype=torch.float32)
    series = torch.sin(2 * torch.pi * t / 50) + 0.5 * torch.sin(2 * torch.pi * t / 17) + 0.1 * torch.randn(n_points, generator=g)
    X = torch.stack([series[i:i + window] for i in range(n_points - window)]).unsqueeze(-1)
    y = series[window:].unsqueeze(-1)
    return series, X, y


def part_a():
    torch.manual_seed(0)
    series, X, y = sine_windows()
    split = 900                                  # chronological split: train on the past, test on the future
    Xtr, ytr, Xte, yte = X[:split], y[:split], X[split:], y[split:]
    model = Forecaster()
    opt = torch.optim.Adam(model.parameters(), lr=1e-2)
    for epoch in range(40):
        for idx in torch.randperm(len(Xtr)).split(64):
            loss = nn.functional.mse_loss(model(Xtr[idx]), ytr[idx])
            opt.zero_grad(); loss.backward(); opt.step()
    with torch.no_grad():
        pred = model(Xte)
    mse = nn.functional.mse_loss(pred, yte).item()
    naive = nn.functional.mse_loss(Xte[:, -1], yte).item()          # "tomorrow = today"
    print(f"A) LSTM test MSE {mse:.4f}  vs naive last-value MSE {naive:.4f}  (noise variance = 0.0100)")
    plt.figure(figsize=(9, 3.5))
    plt.plot(range(150), yte[:150, 0], label="actual", lw=1.5)
    plt.plot(range(150), pred[:150, 0], label="LSTM one-step forecast", lw=1.5, ls="--")
    plt.title("LSTM one-step-ahead forecast on held-out future of a noisy two-sine series")
    plt.xlabel("time step (test period)"); plt.legend(); plt.tight_layout()
    plt.savefig(IMG / "lstm_sine_forecast.png", dpi=120)


# ---------------- B) long-memory classification ----------------
N_SYM = 8


class SeqClassifier(nn.Module):
    def __init__(self, cell, hidden=32):
        super().__init__()
        self.emb = nn.Embedding(N_SYM + 1, 16)                     # symbol id -> 16-d vector
        self.rnn = {"RNN": nn.RNN, "LSTM": nn.LSTM, "GRU": nn.GRU}[cell](16, hidden, batch_first=True)
        if cell == "LSTM":                                         # forget-gate bias = 1: "remember by default"
            with torch.no_grad():                                  # PyTorch gate order: input, forget, cell, output
                self.rnn.bias_ih_l0[hidden:2 * hidden].fill_(1.0)
                self.rnn.bias_hh_l0[hidden:2 * hidden].fill_(0.0)
        self.head = nn.Linear(hidden, N_SYM)

    def forward(self, x):
        out, _ = self.rnn(self.emb(x))
        return self.head(out[:, -1])


def first_symbol_task(cell, T, steps=1500):
    gen = torch.Generator().manual_seed(0)
    torch.manual_seed(0)
    make = lambda n: (lambda x: (x, x[:, 0] - 1))(torch.randint(1, N_SYM + 1, (n, T), generator=gen))
    model = SeqClassifier(cell)
    opt = torch.optim.Adam(model.parameters(), lr=1e-2)
    Xte, yte = make(1000)
    for _ in range(steps):
        xb, yb = make(64)                                          # a fresh random batch every step
        loss = nn.functional.cross_entropy(model(xb), yb)
        opt.zero_grad(); loss.backward()
        nn.utils.clip_grad_norm_(model.parameters(), 1.0)          # standard for recurrent nets
        opt.step()
    with torch.no_grad():
        return (model(Xte).argmax(1) == yte).float().mean().item()


if __name__ == "__main__":
    part_a()
    print("B) recall the first of T symbols (test accuracy, chance 0.125)")
    print(f"{'T':>5s} {'RNN':>7s} {'LSTM':>7s} {'GRU':>7s}")
    for T in [5, 15]:
        print(f"{T:5d} " + " ".join(f"{first_symbol_task(c, T):7.3f}" for c in ["RNN", "LSTM", "GRU"]))
    sizes = {c: sum(p.numel() for p in SeqClassifier(c).rnn.parameters()) for c in ["RNN", "LSTM", "GRU"]}
    print("recurrent-layer parameters (hidden 32):", sizes)
    print("saved img/lstm_sine_forecast.png")
