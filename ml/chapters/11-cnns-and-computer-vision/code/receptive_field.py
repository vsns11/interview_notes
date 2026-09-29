"""Receptive field and output size of a stack of conv/pool layers."""

def walk(layers, n_in):
    rf, jump, n = 1, 1, n_in      # jump = distance in input pixels between adjacent outputs
    print(f"{'layer':22s} {'out size':>8s} {'RF':>4s} {'jump':>5s}")
    for name, k, s, p in layers:
        n = (n + 2 * p - k) // s + 1
        rf = rf + (k - 1) * jump
        jump *= s
        print(f"{name:22s} {n:8d} {rf:4d} {jump:5d}")


if __name__ == "__main__":
    print("Three 3x3 convs (stride 1) vs one 7x7 conv, on a 32x32 input")
    walk([("conv3x3", 3, 1, 1)] * 3, 32)
    walk([("conv7x7", 7, 1, 3)], 32)
    c = 64
    print(f"weights (C={c} channels in and out, no bias): three 3x3 = {3 * 9 * c * c:,}  vs one 7x7 = {49 * c * c:,}")
    print("\nVGG-style block pattern: conv, conv, pool, ... on 224x224")
    walk([("conv3x3", 3, 1, 1), ("conv3x3", 3, 1, 1), ("maxpool2", 2, 2, 0),
          ("conv3x3", 3, 1, 1), ("conv3x3", 3, 1, 1), ("maxpool2", 2, 2, 0),
          ("conv3x3", 3, 1, 1), ("conv3x3", 3, 1, 1), ("maxpool2", 2, 2, 0)], 224)
