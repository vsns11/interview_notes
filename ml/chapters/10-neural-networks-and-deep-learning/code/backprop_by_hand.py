"""One forward + backward pass through a 2-2-1 network, by hand, then checked with PyTorch autograd.

Network: x (2) -> h = ReLU(W1 x + b1) (2) -> y_hat = sigmoid(w2 . h + b2) (1)
Loss: binary cross-entropy L = -[y log y_hat + (1-y) log(1-y_hat)]
"""
import numpy as np
import torch

x = np.array([1.0, 2.0]);  y = 1.0
W1 = np.array([[0.5, -0.2],     # row j = weights into hidden unit j
               [0.3,  0.4]])
b1 = np.array([0.1, -0.5])
w2 = np.array([0.7, -0.6]);  b2 = 0.2
lr = 0.1


def sigmoid(z):
    return 1 / (1 + np.exp(-z))


if __name__ == "__main__":
    # ---------- forward ----------
    z1 = W1 @ x + b1                 # pre-activations of hidden layer
    h = np.maximum(0, z1)            # ReLU
    z2 = w2 @ h + b2                 # output logit
    y_hat = sigmoid(z2)
    L = -(y * np.log(y_hat) + (1 - y) * np.log(1 - y_hat))
    print("FORWARD")
    print(f"  z1 = W1 x + b1 = {z1.round(4)}")
    print(f"  h  = ReLU(z1)  = {h.round(4)}")
    print(f"  z2 = w2.h + b2 = {z2:.4f}")
    print(f"  y_hat = sigmoid(z2) = {y_hat:.4f}")
    print(f"  L = -log(y_hat) = {L:.4f}")

    # ---------- backward (chain rule, output -> input) ----------
    dz2 = y_hat - y                  # dL/dz2 for sigmoid + BCE
    dw2 = dz2 * h                    # dz2/dw2 = h
    db2 = dz2
    dh = dz2 * w2                    # dz2/dh = w2
    dz1 = dh * (z1 > 0)              # ReLU'(z1) = 1 if z1>0 else 0
    dW1 = np.outer(dz1, x)           # dz1_j/dW1_jk = x_k
    db1 = dz1
    print("BACKWARD")
    print(f"  dL/dz2 = y_hat - y = {dz2:.4f}")
    print(f"  dL/dw2 = dz2 * h   = {dw2.round(4)}   dL/db2 = {db2:.4f}")
    print(f"  dL/dh  = dz2 * w2  = {dh.round(4)}")
    print(f"  dL/dz1 = dh * ReLU'(z1) = {dz1.round(4)}")
    print(f"  dL/dW1 = outer(dz1, x) =\n{dW1.round(4)}")
    print(f"  dL/db1 = {db1.round(4)}")

    # ---------- check with autograd ----------
    t = lambda a: torch.tensor(a, dtype=torch.float64, requires_grad=True)
    tW1, tb1, tw2, tb2 = t(W1), t(b1), t(w2), t(b2)
    tx = torch.tensor(x)
    out = torch.sigmoid(tw2 @ torch.relu(tW1 @ tx + tb1) + tb2)
    tL = torch.nn.functional.binary_cross_entropy(out, torch.tensor(y, dtype=torch.float64))
    tL.backward()
    ok = all(np.allclose(a, b.grad.numpy()) for a, b in [(dW1, tW1), (db1, tb1), (dw2, tw2), (db2, tb2)])
    print("autograd agrees with hand-derived gradients:", ok)

    # ---------- one gradient-descent step ----------
    W1n, b1n, w2n, b2n = W1 - lr * dW1, b1 - lr * db1, w2 - lr * dw2, b2 - lr * db2
    y_hat_new = sigmoid(w2n @ np.maximum(0, W1n @ x + b1n) + b2n)
    print(f"after one SGD step (lr={lr}): y_hat {y_hat:.4f} -> {y_hat_new:.4f}, "
          f"loss {L:.4f} -> {-np.log(y_hat_new):.4f}")
