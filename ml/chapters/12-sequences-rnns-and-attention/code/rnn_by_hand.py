"""A vanilla RNN unrolled by hand (NumPy) + how gradients through time vanish or explode."""
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import torch

IMG = Path(__file__).resolve().parent.parent / "img"

if __name__ == "__main__":
    # --- 1. tiny RNN: 1 input, 2 hidden units, worked numbers ---
    Wxh = np.array([[0.5], [-0.3]])           # hidden x input
    Whh = np.array([[0.4, 0.1], [-0.2, 0.3]]) # hidden x hidden (shared across time!)
    bh = np.zeros(2)
    xs = [1.0, 0.5, -1.0]
    h = np.zeros(2)
    print("h_t = tanh(Wxh x_t + Whh h_{t-1} + b)")
    for t, x in enumerate(xs, 1):
        pre = Wxh[:, 0] * x + Whh @ h + bh
        h = np.tanh(pre)
        print(f"  t={t}: x={x:+.1f}  pre-activation={pre.round(4)}  h={h.round(4)}")

    # same thing with torch.nn.RNN to confirm
    rnn = torch.nn.RNN(input_size=1, hidden_size=2, batch_first=True)
    with torch.no_grad():
        rnn.weight_ih_l0.copy_(torch.tensor(Wxh)); rnn.weight_hh_l0.copy_(torch.tensor(Whh))
        rnn.bias_ih_l0.zero_(); rnn.bias_hh_l0.zero_()
        out, h_last = rnn(torch.tensor(xs, dtype=torch.float32).view(1, 3, 1))
    print("  torch.nn.RNN final hidden:", h_last.view(-1).numpy().round(4))

    # --- 2. gradient of h_T w.r.t. h_0 through T steps (BPTT) ---
    torch.manual_seed(0)
    H, T = 32, 60
    fig, ax = plt.subplots(figsize=(7, 4))
    for scale in [0.5, 1.0, 1.5, 3.0]:
        W = torch.randn(H, H) * scale / H ** 0.5          # spectral radius ~ scale
        norms = []
        for steps in range(1, T + 1):
            h0 = torch.randn(H) * 0.1
            h0.requires_grad_(True)
            h = h0
            for _ in range(steps):
                h = torch.tanh(W @ h)
            (g,) = torch.autograd.grad(h.sum(), h0)
            norms.append(g.norm().item())
        ax.semilogy(range(1, T + 1), norms, label=f"recurrent weight scale {scale}")
        print(f"scale {scale}: |d h_T / d h_0| at T=1: {norms[0]:.2e}  T=10: {norms[9]:.2e}  T=60: {norms[-1]:.2e}")
    ax.set_xlabel("time steps back (T)"); ax.set_ylabel("gradient norm (log)"); ax.legend(fontsize=8)
    ax.set_title("Gradients through time in a tanh RNN"); ax.grid(alpha=0.3)
    fig.tight_layout(); fig.savefig(IMG / "rnn_gradients.png", dpi=120)
    print("saved img/rnn_gradients.png")
