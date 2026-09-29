"""Measuring bias: (A) a resume screener trained on biased history, (B) a word-association
probe on a tiny 'language model' built from a skewed corpus.

(A) Two groups have the SAME skill distribution, but past hiring favoured group 0.
    We train on that history and measure selection rates, the disparate-impact ratio
    (the US "four-fifths" rule of thumb), equal-opportunity gap and counterfactual flips -
    and show that deleting the group column does not remove the bias when a proxy remains.
(B) Counts pronoun continuations after occupation words in a small skewed corpus -
    the same kind of association a real LLM can absorb from web text.
"""
import numpy as np
from collections import Counter
from sklearn.linear_model import LogisticRegression

rng = np.random.default_rng(0)
N = 4000
group = rng.integers(0, 2, N)                          # protected attribute (0 or 1)
skill = rng.normal(0, 1, N)                            # identical distribution in both groups
proxy = (rng.random(N) < np.where(group == 0, 0.9, 0.1)).astype(float)  # e.g. a club/hobby
qualified = (skill > 0.3).astype(int)                  # ground truth we care about
# biased history: group 0 got a boost from past decision-makers
hired = ((skill + 0.8 * (group == 0) + rng.normal(0, 0.5, N)) > 0.7).astype(int)


def report(name, cols):
    X = np.column_stack(cols)
    tr, te = slice(0, 3000), slice(3000, N)
    model = LogisticRegression().fit(X[tr], hired[tr])
    pred = model.predict(X[te])
    g, q = group[te], qualified[te]
    sel = [pred[g == k].mean() for k in (0, 1)]
    tpr = [pred[(g == k) & (q == 1)].mean() for k in (0, 1)]
    di = min(sel) / max(sel)
    print(f"{name:<28} select g0={sel[0]:.2f} g1={sel[1]:.2f}  DI={di:.2f}"
          f"{' (fails 4/5 rule)' if di < 0.8 else ''}  TPR gap={tpr[0] - tpr[1]:+.2f}")
    return model, X


if __name__ == "__main__":
    print("(A) resume screener trained on biased hiring history")
    m1, X1 = report("with group column", [skill, group])
    m2, X2 = report("group removed, proxy kept", [skill, proxy])
    m3, X3 = report("group and proxy removed", [skill])

    # counterfactual test: flip the protected attribute, keep everything else
    Xf = X1.copy()
    Xf[:, 1] = 1 - Xf[:, 1]
    flips = (m1.predict(X1) != m1.predict(Xf)).mean()
    print(f"counterfactual flip test (model 1): {flips:.1%} of decisions change when only group flips")

    # mitigation by reweighing (Kamiran & Calders): weight = P(group)P(label)/P(group,label)
    X = np.column_stack([skill, proxy])
    w = np.ones(N)
    for k in (0, 1):
        for lab in (0, 1):
            cell = (group == k) & (hired == lab)
            w[cell] = (np.mean(group == k) * np.mean(hired == lab)) / np.mean(cell)
    m4 = LogisticRegression().fit(X[:3000], hired[:3000], sample_weight=w[:3000])
    pred = m4.predict(X[3000:])
    g = group[3000:]
    sel = [pred[g == k].mean() for k in (0, 1)]
    print(f"{'proxy kept + reweighing':<28} select g0={sel[0]:.2f} g1={sel[1]:.2f}  "
          f"DI={min(sel) / max(sel):.2f}")

    print("\n(B) word associations in a skewed corpus")
    corpus = (["the nurse said she"] * 9 + ["the nurse said he"] * 1 +
              ["the engineer said he"] * 8 + ["the engineer said she"] * 2 +
              ["the teacher said she"] * 6 + ["the teacher said he"] * 4)
    counts = {}
    for s in corpus:
        w = s.split()
        counts.setdefault(w[1], Counter())[w[3]] += 1
    for occ, c in counts.items():
        tot = sum(c.values())
        print(f"P(she | 'the {occ} said') = {c['she'] / tot:.2f}   P(he) = {c['he'] / tot:.2f}")
