"""The same logistic-regression classifier in scikit-learn and in PyTorch, to show the difference
between a high-level ML library and a deep-learning framework."""
import numpy as np
import torch
from sklearn.datasets import load_breast_cancer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler


def main() -> None:
    X, y = load_breast_cancer(return_X_y=True)
    X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.2, stratify=y, random_state=0)
    scaler = StandardScaler().fit(X_tr)
    X_tr, X_te = scaler.transform(X_tr), scaler.transform(X_te)

    # scikit-learn: one line to train
    sk = LogisticRegression(max_iter=1000).fit(X_tr, y_tr)
    print(f"scikit-learn accuracy: {sk.score(X_te, y_te):.3f}")

    # PyTorch: you write the model, the loss, and the training loop yourself
    torch.manual_seed(0)
    Xt = torch.tensor(X_tr, dtype=torch.float32)
    yt = torch.tensor(y_tr, dtype=torch.float32).unsqueeze(1)
    model = torch.nn.Linear(X_tr.shape[1], 1)                  # w.x + b
    loss_fn = torch.nn.BCEWithLogitsLoss()                     # sigmoid + cross-entropy
    opt = torch.optim.SGD(model.parameters(), lr=0.1, weight_decay=1e-3)
    for epoch in range(300):
        opt.zero_grad()
        loss = loss_fn(model(Xt), yt)
        loss.backward()                                        # autograd computes the gradient
        opt.step()                                             # gradient-descent update
        if epoch in (0, 299):
            print(f"  epoch {epoch:>3} loss {loss.item():.4f}")
    with torch.no_grad():
        pred = (model(torch.tensor(X_te, dtype=torch.float32)) > 0).squeeze(1).numpy()
    print(f"PyTorch accuracy:      {np.mean(pred == y_te):.3f}")
    print("device available:", "cuda" if torch.cuda.is_available() else
          "mps" if torch.backends.mps.is_available() else "cpu")


if __name__ == "__main__":
    main()
