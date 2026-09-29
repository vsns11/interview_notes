"""Bias mitigation on the synthetic credit data:
  pre-processing  - reweighing (Kamiran & Calders)
  in-processing   - logistic regression with a fairness penalty (torch)
  post-processing - group-specific thresholds (equal opportunity / demographic parity)
"""
import numpy as np
import pandas as pd
import torch
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler

from credit_data import FEATURES, make_credit_data


def evaluate(name, y, y_true, y_hat, g):
    def rate(mask_extra, arr=y_hat):
        return [arr[(g == k) & mask_extra].mean() for k in (0, 1)]
    sel = rate(np.ones_like(y, bool))
    tpr = rate(y == 1)
    tpr_true = rate(y_true == 1)
    return {"method": name, "acc": (y_hat == y).mean(), "acc_true": (y_hat == y_true).mean(),
            "sel_A": sel[0], "sel_B": sel[1], "DP_diff": sel[0] - sel[1],
            "EO_diff": tpr[0] - tpr[1], "EO_diff_true": tpr_true[0] - tpr_true[1]}


def reweighing_weights(y, g):
    """w(g, y) = P(g) * P(y) / P(g, y): makes label and group independent in the weighted data."""
    w = np.empty(len(y))
    for gv in (0, 1):
        for yv in (0, 1):
            m = (g == gv) & (y == yv)
            w[m] = (np.mean(g == gv) * np.mean(y == yv)) / np.mean(m)
    return w


def fair_logreg(X, y, g, lam, epochs=300, seed=0):
    """Logistic regression + lam * (mean score of A - mean score of B)^2  (a demographic-parity penalty)."""
    torch.manual_seed(seed)
    X_t = torch.tensor(X, dtype=torch.float32); y_t = torch.tensor(y, dtype=torch.float32)
    gA = torch.tensor(g == 0); gB = torch.tensor(g == 1)
    lin = torch.nn.Linear(X.shape[1], 1)
    opt = torch.optim.Adam(lin.parameters(), lr=0.05)
    for _ in range(epochs):
        opt.zero_grad()
        logits = lin(X_t).squeeze(1)
        p = torch.sigmoid(logits)
        gap = p[gA].mean() - p[gB].mean()
        loss = torch.nn.functional.binary_cross_entropy_with_logits(logits, y_t) + lam * gap ** 2
        loss.backward(); opt.step()
    return lambda Z: torch.sigmoid(lin(torch.tensor(Z, dtype=torch.float32))).squeeze(1).detach().numpy()


def threshold_for_rate(score, target, positives_only_mask=None):
    """Smallest threshold whose approval rate (optionally among a subset) reaches `target`."""
    s = score if positives_only_mask is None else score[positives_only_mask]
    return np.quantile(s, 1 - target)


def main():
    df = make_credit_data()
    train, rest = train_test_split(df, test_size=0.4, random_state=0, stratify=df.group)
    val, test = train_test_split(rest, test_size=0.5, random_state=0, stratify=rest.group)
    sc = StandardScaler().fit(train[FEATURES])
    Xtr, Xva, Xte = (sc.transform(d[FEATURES]) for d in (train, val, test))
    ytr, yva, yte = train.y.to_numpy(), val.y.to_numpy(), test.y.to_numpy()
    gtr, gva, gte = train.group.to_numpy(), val.group.to_numpy(), test.group.to_numpy()
    yte_true = test.y_true.to_numpy()
    res = []

    # 0) baseline
    base = LogisticRegression().fit(Xtr, ytr)
    s_te = base.predict_proba(Xte)[:, 1]
    res.append(evaluate("baseline (t=0.5)", yte, yte_true, (s_te >= 0.5).astype(int), gte))

    # 1) pre-processing: reweighing
    w = reweighing_weights(ytr, gtr)
    print("reweighing weights  (A,y=0) (A,y=1) (B,y=0) (B,y=1):",
          [round(float(w[(gtr == a) & (ytr == b)][0]), 3) for a in (0, 1) for b in (0, 1)])
    rw = LogisticRegression().fit(Xtr, ytr, sample_weight=w)
    res.append(evaluate("pre: reweighing", yte, yte_true, (rw.predict_proba(Xte)[:, 1] >= 0.5).astype(int), gte))

    # 2) in-processing: fairness-penalised logistic regression
    fair = fair_logreg(Xtr, ytr, gtr, lam=5.0)
    res.append(evaluate("in: DP penalty (lam=5)", yte, yte_true, (fair(Xte) >= 0.5).astype(int), gte))

    # 3) post-processing: group thresholds fitted on the VALIDATION set
    s_va = base.predict_proba(Xva)[:, 1]
    tA = 0.5
    tpr_A = np.mean(s_va[(gva == 0) & (yva == 1)] >= tA)
    tB = threshold_for_rate(s_va[gva == 1], tpr_A, yva[gva == 1] == 1)     # match TPR of group A
    print(f"equal-opportunity thresholds: A={tA:.3f}  B={tB:.3f}")
    y_eo = np.where(gte == 0, s_te >= tA, s_te >= tB).astype(int)
    res.append(evaluate("post: equal opportunity", yte, yte_true, y_eo, gte))

    sel_A = np.mean(s_va[gva == 0] >= tA)
    tB_dp = threshold_for_rate(s_va[gva == 1], sel_A)                      # match selection rate
    print(f"demographic-parity thresholds: A={tA:.3f}  B={tB_dp:.3f}")
    y_dp = np.where(gte == 0, s_te >= tA, s_te >= tB_dp).astype(int)
    res.append(evaluate("post: demographic parity", yte, yte_true, y_dp, gte))

    pd.set_option("display.precision", 3); pd.set_option("display.width", 120)
    print(pd.DataFrame(res).set_index("method").to_string())


if __name__ == "__main__":
    main()
