"""Chapter 20: check the concrete formulas and numbers quoted on the cheat sheets.

Every assert below corresponds to a value printed in README.md. If a library update
changes a default or a formula is mistyped on a sheet, this script fails and names the
sheet to fix. Runs in a few seconds on CPU:

    python code/cheatsheet_checks.py
"""
import math

import numpy as np
import pandas as pd
import torch
from scipy.stats import norm
from sklearn.cluster import KMeans
from sklearn.ensemble import HistGradientBoostingClassifier, RandomForestClassifier, RandomForestRegressor
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (average_precision_score, f1_score, mean_absolute_error,
                             mean_squared_error, ndcg_score, precision_score, r2_score,
                             recall_score, roc_auc_score)
from sklearn.neighbors import KNeighborsClassifier
from sklearn.svm import SVC


def close(a, b, tol=1e-3):
    return abs(a - b) <= tol


def sigmoid(z):
    return 1 / (1 + math.exp(-z))


def softmax(z):
    e = np.exp(np.asarray(z, dtype=float) - np.max(z))
    return e / e.sum()


def sheet_20_2_math() -> None:
    a, b = np.array([1, 2, 3]), np.array([4, 5, 6])
    assert a @ b == 32
    assert close(np.linalg.norm(a), 3.742)
    assert close(a @ b / (np.linalg.norm(a) * np.linalg.norm(b)), 0.975)
    A = np.array([[1, 2], [3, 4]])
    assert close(np.linalg.det(A), -2.0)
    assert np.allclose(np.linalg.inv(A), [[-2, 1], [1.5, -0.5]])
    assert np.allclose(np.linalg.solve(A, [5, 11]), [1, 2])
    lam = np.linalg.eigvalsh(np.array([[2, 1], [1, 2]]))
    assert np.allclose(lam, [1, 3])
    # sigmoid and its derivative
    assert sigmoid(0) == 0.5 and close(sigmoid(0) * (1 - sigmoid(0)), 0.25)
    # gradient descent on (w-3)^2, eta=0.1: each step keeps 80% of the gap
    w = 0.0
    for _ in range(2):
        w -= 0.1 * 2 * (w - 3)
    assert close(w, 1.08)
    # Bayes: 1% prevalence, 90% sensitivity, 5% false-positive rate
    post = 0.9 * 0.01 / (0.9 * 0.01 + 0.05 * 0.99)
    assert close(post, 0.154)
    assert close(0.9 * post / (0.9 * post + 0.05 * (1 - post)), 0.766)
    # binomial and normal
    assert close(math.comb(10, 3) * 0.3**3 * 0.7**7, 0.2668, 1e-4)
    assert close(1 - norm.cdf(2), 0.0228, 1e-4)
    # standard error and CLT
    assert 20 / math.sqrt(100) == 2
    # entropy / cross-entropy / KL (bits)
    p, q = np.array([0.7, 0.2, 0.1]), np.array([0.5, 0.3, 0.2])
    H = -(p * np.log2(p)).sum()
    CE = -(p * np.log2(q)).sum()
    assert close(H, 1.157) and close(CE, 1.280) and close(CE - H, 0.123)
    assert close(-(0.9 * math.log2(0.9) + 0.1 * math.log2(0.1)), 0.469)
    # log loss (natural log)
    y, pr = np.array([1, 0, 1, 1]), np.array([0.9, 0.2, 0.6, 0.99])
    ll = -np.mean(y * np.log(pr) + (1 - y) * np.log(1 - pr))
    assert close(ll, 0.2123, 1e-4)
    assert close(-math.log(0.5), 0.693)


def sheet_20_3_api() -> None:
    M = np.arange(12).reshape(3, 4)
    assert M.sum(axis=0).shape == (4,) and M.sum(axis=1).shape == (3,)
    assert (np.zeros((3, 1)) + np.zeros((1, 4))).shape == (3, 4)
    try:
        np.zeros((3, 2)) + np.zeros(3)
        raise AssertionError("(3,2)+(3,) should not broadcast")
    except ValueError:
        pass
    v = M[:2, 1:3]
    assert np.shares_memory(v, M)                      # basic slice = view
    assert not np.shares_memory(M[M > 5], M)           # boolean mask = copy
    df = pd.DataFrame({"x": range(5)})
    assert len(df.loc[0:2]) == 3 and len(df.iloc[0:2]) == 2
    # scikit-learn defaults quoted on the sheets
    assert LogisticRegression().C == 1.0 and LogisticRegression().solver == "lbfgs"
    assert RandomForestClassifier().max_features == "sqrt"
    assert RandomForestRegressor().max_features == 1.0
    hgb = HistGradientBoostingClassifier()
    assert (hgb.learning_rate, hgb.max_leaf_nodes, hgb.min_samples_leaf) == (0.1, 31, 20)
    assert (SVC().kernel, SVC().C, SVC().gamma) == ("rbf", 1.0, "scale")
    assert (KNeighborsClassifier().n_neighbors, KNeighborsClassifier().p) == (5, 2)
    assert KMeans().init == "k-means++"


def sheet_20_4_data_prep() -> None:
    # robust scaling example: [10, 11, 11, 12, 12, 13, 100] -> median 12, IQR 1.5
    x = np.array([10, 11, 11, 12, 12, 13, 100], dtype=float)
    q1, med, q3 = np.percentile(x, [25, 50, 75])
    assert (q1, med, q3) == (11.0, 12.0, 12.5)
    assert close((100 - med) / (q3 - q1), 58.667)
    # cyclical encoding: hour 6 -> (1, 0)
    assert np.allclose([math.sin(2 * math.pi * 6 / 24), math.cos(2 * math.pi * 6 / 24)], [1, 0])
    # TF-IDF idf (sklearn smooth_idf): n=4 docs
    idf = lambda df_t, n=4: math.log((1 + n) / (1 + df_t)) + 1
    assert close(idf(2), 1.511) and close(idf(1), 1.916)
    # Cohen's kappa: p_o = 0.8, p_e = 0.5
    assert close((0.8 - 0.5) / (1 - 0.5), 0.6)
    # prior correction after keeping a fraction w of negatives
    recal = lambda p, w: p * w / (p * w + (1 - p))
    assert close(recal(0.5, 0.1), 0.091) and close(recal(0.5, 0.05), 0.048)
    # polynomial features: d=10, degree 2 -> C(12,2) - 1 = 65
    assert math.comb(12, 2) - 1 == 65


def sheet_20_5_algorithms() -> None:
    gini = lambda ps: 1 - sum(p * p for p in ps)
    ent = lambda ps: -sum(p * math.log2(p) for p in ps if p > 0)
    assert close(gini([0.4, 0.6]), 0.48) and close(ent([0.4, 0.6]), 0.971)
    ig = ent([0.4, 0.6]) - (0.4 * ent([0.75, 0.25]) + 0.6 * ent([1 / 6, 5 / 6]))
    gd = gini([0.4, 0.6]) - (0.4 * gini([0.75, 0.25]) + 0.6 * gini([1 / 6, 5 / 6]))
    assert close(ig, 0.256) and close(gd, 0.163)
    assert close(gini([0.8, 0.2]), 0.32)
    # out-of-bag share -> 1/e
    assert close((1 - 1 / 10_000) ** 10_000, 1 / math.e, 1e-4) and close(1 / math.e, 0.368)
    # AdaBoost vote for eps = 0.2
    assert close(0.5 * math.log(0.8 / 0.2), 0.693)
    # SVM margin 2/||w||, polynomial kernel trick (x.z)^2 = phi(x).phi(z)
    x, z = np.array([1, 2]), np.array([3, 4])
    phi = lambda v: np.array([v[0] ** 2, math.sqrt(2) * v[0] * v[1], v[1] ** 2])
    assert close((x @ z) ** 2, phi(x) @ phi(z)) and (x @ z) ** 2 == 121
    # naive Bayes toy spam filter: ratio 9:3 -> 0.75
    assert close((3 * 3 * 1) / (3 * 3 * 1 + 1 * 1 * 3), 0.75)
    # k-means silhouette example: a = 1.5, b = 8
    assert close((8 - 1.5) / 8, 0.8125)
    # PCA on two standardised features with correlation r: eigenvalues 1 +/- r
    r = 0.8
    assert np.allclose(np.linalg.eigvalsh(np.array([[1, r], [r, 1]])), [1 - r, 1 + r])
    # association rules: confidence and lift
    assert close(0.2 / 0.4, 0.5) and close((0.2 / 0.4) / 0.5, 1.0)
    # curse of dimensionality: edge to capture 10% of a unit cube in 10-D
    assert close(0.1 ** (1 / 10), 0.794)


def sheet_20_6_losses() -> None:
    y, yh = np.array([300, 250, 400.0]), np.array([310, 240, 380.0])
    assert close(mean_squared_error(y, yh), 200) and close(mean_absolute_error(y, yh), 13.333)
    # Huber: quadratic inside delta, linear outside
    huber = lambda e, d=1.0: 0.5 * e * e if abs(e) <= d else d * (abs(e) - 0.5 * d)
    assert huber(0.5) == 0.125 and huber(3.0) == 2.5
    # cross-entropy: uniform guess over K classes = ln K
    logits = torch.zeros(1, 10)
    ce = torch.nn.functional.cross_entropy(logits, torch.tensor([3]))
    assert close(ce.item(), math.log(10)) and close(math.log(10), 2.303)
    assert close(-math.log(0.7), 0.357)
    # hinge
    assert max(0, 1 - 1 * 0.3) == 0.7 and max(0, 1 - 1 * 2.0) == 0


def sheet_20_7_metrics() -> None:
    y = np.array([30, 25, 40, 35, 50.0])
    yh = np.array([28, 27, 35, 35, 60.0])
    assert close(mean_absolute_error(y, yh), 3.8)
    assert close(math.sqrt(mean_squared_error(y, yh)), 5.158)
    assert close(r2_score(y, yh), 0.641)
    # fraud confusion matrix: TP 60, FP 20, FN 40, TN 880
    tp, fp, fn, tn = 60, 20, 40, 880
    P, R = tp / (tp + fp), tp / (tp + fn)
    assert (P, R) == (0.75, 0.6) and close(2 * P * R / (P + R), 0.667)
    assert close((tp + tn) / 1000, 0.94) and close(fp / (fp + tn), 0.0222)
    yt, yp = [0, 1, 0, 1], [0, 1, 1, 1]
    assert close(precision_score(yt, yp), 0.667) and recall_score(yt, yp) == 1
    assert close(f1_score(yt, yp), 0.8)
    # ROC-AUC by pairs = 7/9
    assert close(roc_auc_score([1, 1, 1, 0, 0, 0], [0.9, 0.7, 0.4, 0.8, 0.3, 0.2]), 7 / 9)
    # Brier and the cost-based threshold
    assert close(np.mean((np.array([0.9, 0.2, 0.6, 0.3]) - np.array([1, 0, 1, 1])) ** 2), 0.175)
    assert close(5 / (5 + 100), 0.048)
    # ranking: graded relevance [3,0,2,1,0,0], one relevant grade-2 doc not retrieved
    rel = [3, 0, 2, 1, 0, 0]
    dcg = sum(r / math.log2(i + 2) for i, r in enumerate(rel[:5]))
    idcg = sum(r / math.log2(i + 2) for i, r in enumerate([3, 2, 2, 1]))
    assert close(dcg, 4.431) and close(dcg / idcg, 0.778)
    assert close(ndcg_score([rel], [[6, 5, 4, 3, 2, 1]], k=5), 0.930)   # sklearn ignores the missed doc
    assert close((1 + 2 / 3 + 3 / 4) / 4, 0.604)                      # AP with 4 relevant docs
    # chapter-13 example: recs A..E, relevant {A, C, F}
    assert close((1 / 1 + 2 / 3) / 3, 0.556) and close(1.5 / (1 + 1 / math.log2(3) + 0.5), 0.704)
    # exponential-gain NDCG@3 for [3, 0, 2]
    g = lambda rels: sum((2**r - 1) / math.log2(i + 2) for i, r in enumerate(rels))
    assert close(g([3, 0, 2]) / g([3, 2, 0]), 0.956)
    # PR-AUC of a random scorer ~ base rate
    rng = np.random.default_rng(0)
    yr = (rng.random(200_000) < 0.02).astype(int)
    assert close(average_precision_score(yr, rng.random(yr.size)), 0.02, 0.003)


def sheet_20_8_validation() -> None:
    # grid 4 x 5 with 5-fold CV = 100 fits; bias-variance of averaging B models
    assert 4 * 5 * 5 == 100
    rho, s2, B = 0.3, 1.0, 100
    assert close(rho * s2 + (1 - rho) * s2 / B, 0.307)
    # A/B sample size per arm, two proportions (alpha .05 two-sided, power .8)
    def n_per_arm(p1, rel_lift, alpha=0.05, power=0.8):
        p2 = p1 * (1 + rel_lift)
        pbar = (p1 + p2) / 2
        za, zb = norm.ppf(1 - alpha / 2), norm.ppf(power)
        num = (za * math.sqrt(2 * pbar * (1 - pbar)) + zb * math.sqrt(p1 * (1 - p1) + p2 * (1 - p2))) ** 2
        return num / (p2 - p1) ** 2
    n1 = n_per_arm(0.05, 0.01)
    assert 2.9e6 < n1 < 3.1e6
    assert 3.8 < n1 / n_per_arm(0.05, 0.02) < 4.1                      # half the lift ~ 4x the users


def sheet_20_9_deep_learning() -> None:
    assert close(sigmoid(2), 0.881) and close(sigmoid(-2), 0.119)
    assert np.allclose(softmax([2, 1, 0.1]), [0.659, 0.242, 0.099], atol=1e-3)
    assert np.allclose(softmax([7, 6, 5.1]), softmax([2, 1, 0.1]))    # shift-invariant
    assert 256 * 128 + 128 == 32_896                                  # dense layer params
    assert (2 * 3 + 3) + (3 * 1 + 1) == 13
    assert close(2 / 256, 0.0078) and close(math.sqrt(2 / 256), 0.088)  # He init
    assert close(0.25**10, 9.5e-7, 1e-8)                              # sigmoid gradient over 10 layers
    # Adam / AdamW defaults quoted on the sheet
    p = [torch.zeros(1, requires_grad=True)]
    adam, adamw = torch.optim.Adam(p).defaults, torch.optim.AdamW(p).defaults
    assert (adam["lr"], adam["betas"], adam["eps"]) == (1e-3, (0.9, 0.999), 1e-8)
    assert adamw["weight_decay"] == 0.01
    # inverted dropout scales survivors by 1/(1-p)
    torch.manual_seed(0)
    out = torch.nn.Dropout(0.3).train()(torch.ones(10_000))
    assert close(out.max().item(), 1 / 0.7)
    assert close(1 / (1 - 0.9), 10)                                   # momentum horizon


def sheet_20_10_cnn_rnn_attention() -> None:
    out = lambda n, k, s, p: (n + 2 * p - k) // s + 1
    assert out(5, 3, 1, 0) == 3 and out(5, 3, 2, 0) == 2 and out(5, 3, 1, 1) == 5
    assert out(224, 7, 2, 3) == 112 and out(32, 5, 1, 0) == 28
    conv = torch.nn.Conv2d(64, 128, 3)
    assert sum(t.numel() for t in conv.parameters()) == 73_856
    assert conv(torch.zeros(2, 64, 32, 32)).shape == (2, 128, 30, 30)
    assert 3 * 3 * 3 * 16 + 16 == 448
    assert (224 // 16) ** 2 == 196                                    # ViT patches
    # receptive field of stacked 3x3 convs (stride 1): 3, 5, 7
    assert [1 + L * (3 - 1) for L in (1, 2, 3)] == [3, 5, 7]
    # IoU example
    inter = (150 - 70) * (150 - 60)
    assert close(inter / (10_000 + 10_000 - inter), 0.5625)
    # LSTM cell arithmetic
    c = 0.9 * 2.0 + 0.2 * 0.5
    assert close(c, 1.9) and close(0.7 * math.tanh(c), 0.669)
    # parameter blocks: RNN 1x, GRU 3x, LSTM 4x (PyTorch)
    n = lambda m: sum(t.numel() for t in m.parameters())
    rnn, gru, lstm = torch.nn.RNN(16, 32), torch.nn.GRU(16, 32), torch.nn.LSTM(16, 32)
    assert n(gru) == 3 * n(rnn) and n(lstm) == 4 * n(rnn)
    # scaled dot-product attention worked example
    Q = K = torch.tensor([[1.0, 0], [0, 1], [1, 1]])
    V = torch.tensor([[1.0, 2], [3, 4], [5, 6]])
    w = torch.softmax(Q @ K.T / math.sqrt(2), dim=-1)
    assert torch.allclose(w[0], torch.tensor([0.401, 0.198, 0.401]), atol=1e-3)
    assert torch.allclose((w @ V)[0], torch.tensor([3.0, 4.0]), atol=1e-3)
    ref = torch.nn.functional.scaled_dot_product_attention(Q[None], K[None], V[None])[0]
    assert torch.allclose(ref, w @ V, atol=1e-5)


def sheet_20_11_recommenders() -> None:
    ana, ben, cy = np.array([5, 4, 0, 1.0]), np.array([4, 5, 1, 0.0]), np.array([1, 0, 5, 4.0])
    cos = lambda a, b: a @ b / (np.linalg.norm(a) * np.linalg.norm(b))
    assert close(cos(ana, ben), 0.952) and close(cos(ana, cy), 0.214)
    u = np.array([5, 4, 3.0]) - 4
    v = np.array([3, 2, 1.0]) - 2
    assert close(cos(u, v), 1.0)                                      # adjusted cosine
    a, b = np.array([0.6, 0.8]), np.array([1.0, 0.0])
    assert close(np.sum((a - b) ** 2), 2 - 2 * cos(a, b))             # unit vectors
    assert close(50e6 * 128 * 4 / 1e9, 25.6)                          # raw vector memory (GB)
    assert close(math.log(512), 6.238)                                # in-batch softmax at B=512


def sheet_20_12_rl() -> None:
    g = 0.9
    G_B = -sum(g**k for k in range(7)) + 10 * g**7
    assert close(G_B, -0.43, 0.005)
    assert close(g**10, 0.349) and close(1 / (1 - g), 10)
    V = [10.0]
    for _ in range(3):
        V.append(-1 + g * V[-1])
    assert np.allclose(V, [10, 8, 6.2, 4.58])
    assert close(3 / (1 - g), 30)                                     # re-collectable +3 bonus
    # one Q-learning update: Q=0, r=-1, max Q(s')=5, alpha=0.5, gamma=0.9
    assert close(0 + 0.5 * (-1 + 0.9 * 5 - 0), 1.75)


def sheet_20_13_system_design() -> None:
    assert round(10e6 / 86_400) == 116 and round(3 * 10e6 / 86_400) == 347
    assert 10e6 * 0.01 == 100_000
    assert 500_000 * 0.08 == 40_000 and 40_000 * 10 * 0.01 == 4_000
    assert round(100e6 * 5 / 86_400) == 5_787
    assert round(500e6 * 5 * 3 / 86_400) == 86_806
    assert 25e6 * 500 == 12.5e9 and 5e6 * 500 == 2.5e9
    assert close(100e6 * 512 * 4 / 1e9, 204.8)
    assert 200e6 * 0.01 == 2e6 and round(2e6 / 86_400) == 23
    # expected support cost per ticket at 50% AI resolution, $0.10 AI + $4 human
    assert close(0.10 + 0.5 * 4.0, 2.10)


def sheet_20_14_production() -> None:
    def psi(ref, cur, bins=10):
        edges = np.quantile(ref, np.linspace(0, 1, bins + 1))
        edges[0], edges[-1] = -np.inf, np.inf
        r = np.histogram(ref, edges)[0] / len(ref)
        c = np.histogram(cur, edges)[0] / len(cur)
        r, c = np.clip(r, 1e-6, None), np.clip(c, 1e-6, None)
        return float(np.sum((c - r) * np.log(c / r)))
    rng = np.random.default_rng(1)
    ref = rng.normal(0, 1, 50_000)
    assert psi(ref, rng.normal(0, 1, 50_000)) < 0.01                 # stable
    assert psi(ref, rng.normal(1, 1, 50_000)) > 0.25                  # investigate


def sheet_20_15_responsible() -> None:
    assert close(0.35 / 0.50, 0.70)                                   # disparate impact < 0.8
    assert close(0.3 * 0.5 / 0.1, 1.5)                                # reweighing weight
    fpr = 0.601 / 0.399 * 0.333 / 0.667 * 0.829                       # Chouldechova identity
    assert close(fpr, 0.623)
    assert 1 / 0.5 == 2 and 10 * 0.2 == 2.0                           # Laplace scale, composition
    assert close(0.4 + 0.10 - 0.05 + 0.20, 0.65)                      # SHAP efficiency
    assert close(math.e, 2.718)


if __name__ == "__main__":
    checks = sorted((f for name, f in globals().items() if name.startswith("sheet_")),
                    key=lambda f: int(f.__name__.split("_")[2]))
    for f in checks:
        f()
        print(f"OK  {f.__name__}")
    print(f"all {len(checks)} cheat-sheet checks passed")
