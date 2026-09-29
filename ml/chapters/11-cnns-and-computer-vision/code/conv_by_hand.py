"""Convolution and pooling by hand (NumPy), checked against PyTorch."""
import numpy as np
import torch
import torch.nn.functional as F

X = np.array([[1, 2, 0, 1, 3],
              [0, 1, 3, 2, 1],
              [2, 1, 0, 1, 0],
              [1, 0, 2, 3, 1],
              [0, 2, 1, 0, 2]], dtype=float)
K = np.array([[1, 0, -1],
              [1, 0, -1],
              [1, 0, -1]], dtype=float)       # vertical-edge detector


def conv2d(x, k, stride=1, padding=0):
    """'Convolution' as used in deep learning = cross-correlation (kernel not flipped)."""
    x = np.pad(x, padding)
    kh, kw = k.shape
    oh = (x.shape[0] - kh) // stride + 1
    ow = (x.shape[1] - kw) // stride + 1
    out = np.zeros((oh, ow))
    for i in range(oh):
        for j in range(ow):
            patch = x[i * stride:i * stride + kh, j * stride:j * stride + kw]
            out[i, j] = np.sum(patch * k)          # element-wise multiply, then sum
    return out


def maxpool2d(x, size=2, stride=2):
    oh = (x.shape[0] - size) // stride + 1
    ow = (x.shape[1] - size) // stride + 1
    return np.array([[x[i*stride:i*stride+size, j*stride:j*stride+size].max() for j in range(ow)] for i in range(oh)])


def out_size(n, k, s, p):
    return (n + 2 * p - k) // s + 1


if __name__ == "__main__":
    for (i, j) in [(0, 0), (1, 1)]:
        patch = X[i:i + 3, j:j + 3]
        print(f"patch at output ({i},{j}):\n{patch}\n  sum(patch * kernel) = {np.sum(patch * K)}")
    for s, p in [(1, 0), (2, 0), (1, 1)]:
        ours = conv2d(X, K, stride=s, padding=p)
        ref = F.conv2d(torch.tensor(X)[None, None], torch.tensor(K)[None, None], stride=s, padding=p)[0, 0].numpy()
        print(f"\nstride={s} padding={p} -> output {ours.shape} (formula {out_size(5, 3, s, p)}), matches torch: {np.allclose(ours, ref)}")
        print(ours)
    fm = conv2d(X, K, padding=1)
    print("\nmax-pool 2x2/2 of the padded-conv output (5x5 -> 2x2):\n", maxpool2d(fm))
    print("avg-pool via torch:\n", F.avg_pool2d(torch.tensor(fm)[None, None], 2)[0, 0].numpy())

    # parameter counts
    conv = torch.nn.Conv2d(3, 16, kernel_size=3)
    dense = torch.nn.Linear(32 * 32 * 3, 16)
    print(f"\nConv2d(3->16, 3x3) params: {sum(p.numel() for p in conv.parameters())}  "
          f"vs Linear(32*32*3 -> 16) params: {sum(p.numel() for p in dense.parameters())}")
