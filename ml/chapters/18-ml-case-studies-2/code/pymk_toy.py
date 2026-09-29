"""Toy People You May Know: friends-of-friends retrieval, link-prediction scores, two-sided ranking.

Graph: a clustered, heavy-tailed social network (networkx powerlaw_cluster_graph).
1) Hide 10% of edges as "future connections". Retrieve friends-of-friends (FoF) candidates and rank them by
   random order, common neighbours (CN) and Adamic-Adar (AA). Measure recall@10 of the hidden edges.
2) Two-sided objective: simulate P(invite sent | shown) and P(accept | sent). Ranking by P(send) alone
   pushes suggestions toward very popular members, who rarely accept; P(send) x P(accept) fixes that.
"""
import math
import random

import networkx as nx
import numpy as np

random.seed(0)
rng = np.random.default_rng(0)
sig = lambda z: 1 / (1 + math.exp(-z))


def fof_candidates(G, u):
    """Two-hop neighbours with their mutual-connection lists (bounded in production: top neighbours only)."""
    mutual = {}
    for f in G[u]:
        for c in G[f]:
            if c != u and not G.has_edge(u, c):
                mutual.setdefault(c, []).append(f)
    return mutual


if __name__ == "__main__":
    G = nx.powerlaw_cluster_graph(4000, 4, 0.5, seed=1)
    edges = list(G.edges())
    random.shuffle(edges)
    hidden = edges[: len(edges) // 10]
    Gtr = G.copy()
    Gtr.remove_edges_from(hidden)
    future = {}
    for a, b in hidden:
        future.setdefault(a, set()).add(b)
        future.setdefault(b, set()).add(a)
    deg = dict(Gtr.degree())
    print(f"{G.number_of_nodes()} members, {len(edges)} connections, {len(hidden)} hidden as future; "
          f"max degree {max(deg.values())}, median {int(np.median(list(deg.values())))}")

    users = [u for u in future if deg[u] > 0][:1500]
    hits = {"random FoF": 0, "common neighbours": 0, "Adamic-Adar": 0}
    total = covered = n_cands = 0
    for u in users:
        mutual = fof_candidates(Gtr, u)
        n_cands += len(mutual)
        total += len(future[u])
        covered += len(future[u] & set(mutual))
        cands = list(mutual)
        scores = {
            "random FoF": {c: random.random() for c in cands},
            "common neighbours": {c: len(m) for c, m in mutual.items()},
            "Adamic-Adar": {c: sum(1 / math.log(deg[f]) for f in m if deg[f] > 1) for c, m in mutual.items()},
        }
        for name, s in scores.items():
            top = sorted(cands, key=s.get, reverse=True)[:10]
            hits[name] += len(future[u] & set(top))
    print(f"avg FoF candidates per member {n_cands / len(users):.0f}; "
          f"FoF retrieval covers {covered / total:.1%} of future connections\n")
    for name, h in hits.items():
        print(f"{name:<20} recall@10 = {h / total:.3f}")

    # 2) two-sided ranking. 'True' behaviour (unknown to the system; a trained model would estimate it):
    #    viewers like inviting well-connected people; well-connected people accept fewer invites.
    top1 = set(sorted(deg, key=deg.get, reverse=True)[: len(deg) // 100])
    res = {"P(send) only": [0, 0, 0], "P(send) x P(accept)": [0, 0, 0]}
    for u in users:
        mutual = fof_candidates(Gtr, u)
        feats = {c: (math.log1p(len(m)), math.log1p(deg[c])) for c, m in mutual.items()}
        p_send = {c: sig(-3 + 0.9 * cn + 0.5 * d) for c, (cn, d) in feats.items()}
        p_acc = {c: sig(1.5 + 0.8 * cn - 0.9 * d) for c, (cn, d) in feats.items()}
        for name, key in [("P(send) only", p_send.get), ("P(send) x P(accept)", lambda c: p_send[c] * p_acc[c])]:
            top = sorted(mutual, key=key, reverse=True)[:10]
            res[name][0] += sum(p_send[c] for c in top)
            res[name][1] += sum(p_send[c] * p_acc[c] for c in top)
            res[name][2] += sum(c in top1 for c in top)
    print(f"\n{'ranking (top 10 per member)':<24}{'invites sent':>13}{'accepted':>10}{'accept rate':>12}{'to top-1% members':>19}")
    for name, (s, a, t) in res.items():
        print(f"{name:<24}{s / len(users):>13.2f}{a / len(users):>10.2f}{a / s:>12.1%}{t / (10 * len(users)):>19.1%}")
