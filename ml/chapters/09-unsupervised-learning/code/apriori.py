"""Apriori from scratch on a tiny basket dataset: frequent itemsets, then rules with support/confidence/lift."""
from itertools import combinations

BASKETS = [
    {"bread", "milk"},
    {"bread", "diapers", "beer", "eggs"},
    {"milk", "diapers", "beer", "cola"},
    {"bread", "milk", "diapers", "beer"},
    {"bread", "milk", "diapers", "cola"},
]


def support(itemset):
    return sum(itemset <= b for b in BASKETS) / len(BASKETS)


def apriori(min_support):
    items = sorted({i for b in BASKETS for i in b})
    level = [frozenset([i]) for i in items if support({i}) >= min_support]
    frequent, k = {}, 1
    while level:
        print(f"L{k}: " + ", ".join("{" + ",".join(sorted(s)) + f"}}={support(s):.1f}" for s in level))
        frequent.update({s: support(s) for s in level})
        k += 1
        # join step: unions of size k; prune step: every (k-1)-subset must itself be frequent
        cands = {a | b for a in level for b in level if len(a | b) == k}
        cands = {c for c in cands if all(frozenset(s) in frequent for s in combinations(c, k - 1))}
        level = sorted((c for c in cands if support(c) >= min_support), key=sorted)  # sorted = deterministic output
    return frequent


def rules(frequent, min_conf):
    out = []
    for itemset, sup in frequent.items():
        if len(itemset) < 2:
            continue
        for r in range(1, len(itemset)):
            for lhs in map(frozenset, combinations(itemset, r)):
                rhs = itemset - lhs
                conf = sup / frequent[lhs]
                lift = conf / frequent[rhs]
                if conf >= min_conf:
                    out.append((sorted(lhs), sorted(rhs), sup, conf, lift))
    return sorted(out, key=lambda r: (-r[4], -r[3], r[0], r[1]))


if __name__ == "__main__":
    freq = apriori(min_support=0.6)
    print("\nrule                         support  confidence  lift")
    for lhs, rhs, s, c, l in rules(freq, min_conf=0.7):
        print(f"{','.join(lhs):>16s} -> {','.join(rhs):9s}  {s:6.1f}  {c:9.2f}  {l:5.2f}")
