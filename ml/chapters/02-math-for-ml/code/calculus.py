"""Derivatives, partial derivatives, gradients and the chain rule, checked numerically."""
import numpy as np


def numerical_derivative(f, x: float, h: float = 1e-5) -> float:
    """Central difference: slope of the line through f(x-h) and f(x+h)."""
    return (f(x + h) - f(x - h)) / (2 * h)


def numerical_gradient(f, p: np.ndarray, h: float = 1e-5) -> np.ndarray:
    grad = np.zeros_like(p)
    for i in range(len(p)):
        step = np.zeros_like(p); step[i] = h
        grad[i] = (f(p + step) - f(p - step)) / (2 * h)
    return grad


def main() -> None:
    # 1. Derivative of f(x) = x^2 is 2x. At x = 3 the slope is 6.
    f = lambda x: x**2
    print(f"d/dx x^2 at x=3: numeric {numerical_derivative(f, 3.0):.4f}, exact {2*3}")

    # 2. Partial derivatives of f(x, y) = x^2 * y + 3y at (2, 5):
    #    df/dx = 2xy = 20,   df/dy = x^2 + 3 = 7
    g = lambda p: p[0]**2 * p[1] + 3 * p[1]
    print("gradient of x^2*y + 3y at (2,5): numeric", numerical_gradient(g, np.array([2.0, 5.0])).round(4),
          "exact [20. 7.]")

    # 3. Chain rule: h(x) = (3x + 1)^2. Outer u^2 -> 2u, inner 3x+1 -> 3. h'(x) = 2(3x+1)*3.
    h = lambda x: (3 * x + 1) ** 2
    print(f"d/dx (3x+1)^2 at x=2: numeric {numerical_derivative(h, 2.0):.4f}, exact {2*(3*2+1)*3}")

    # 4. Chain rule in ML: loss of a one-weight model. y_hat = sigmoid(w*x), L = (y_hat - y)^2
    sigmoid = lambda z: 1 / (1 + np.exp(-z))
    x, y, w = 2.0, 1.0, 0.5
    z = w * x; y_hat = sigmoid(z)
    dL_dyhat = 2 * (y_hat - y)
    dyhat_dz = y_hat * (1 - y_hat)
    dz_dw = x
    analytic = dL_dyhat * dyhat_dz * dz_dw
    loss = lambda w_: (sigmoid(w_ * x) - y) ** 2
    print(f"z={z}, y_hat={y_hat:.4f}, dL/dyhat={dL_dyhat:.4f}, dyhat/dz={dyhat_dz:.4f}, dz/dw={dz_dw}")
    print(f"dL/dw via chain rule {analytic:.5f} | numeric {numerical_derivative(loss, w):.5f}")

    # 5. Gradient of MSE for linear regression, vectorised: grad = (2/n) X^T (Xw - y)
    X = np.array([[1.0, 1.0], [1.0, 2.0], [1.0, 3.0]])   # first column = 1 for the bias
    yv = np.array([2.0, 4.0, 6.0])
    wv = np.array([0.0, 1.0])
    mse = lambda p: np.mean((X @ p - yv) ** 2)
    grad = 2 / len(yv) * X.T @ (X @ wv - yv)
    print("MSE gradient: formula", grad, "| numeric", numerical_gradient(mse, wv).round(4))


if __name__ == "__main__":
    main()
