"""Autocomplete trie that caches the top-k completions at every node.

Run: .venv-books/bin/python trie_topk.py
Build is offline (from aggregated query counts); lookup is O(len(prefix)) + reading k items.
"""
import heapq


class Node:
    __slots__ = ("children", "top")

    def __init__(self):
        self.children = {}
        self.top = []          # list of (count, query), best first, length <= k


class AutocompleteTrie:
    def __init__(self, k=5):
        self.k, self.root, self.nodes = k, Node(), 1

    def build(self, counts):
        # 1) insert every query, remembering its path
        for q in counts:
            node = self.root
            for ch in q:
                if ch not in node.children:
                    node.children[ch] = Node()
                    self.nodes += 1
                node = node.children[ch]
        # 2) push each query's count into the top-k of every prefix node on its path
        for q, c in counts.items():
            node = self.root
            for ch in q:
                node = node.children[ch]
                node.top.append((c, q))
        # 3) keep only the k best per node (ties broken alphabetically)
        stack = [self.root]
        while stack:
            n = stack.pop()
            n.top = heapq.nsmallest(self.k, n.top, key=lambda t: (-t[0], t[1]))
            stack.extend(n.children.values())

    def suggest(self, prefix):
        node = self.root
        for ch in prefix.lower():
            node = node.children.get(ch)
            if node is None:
                return []
        return [q for _, q in node.top]


if __name__ == "__main__":
    # one "week" of aggregated query counts (made-up numbers)
    counts = {
        "tree": 9_000, "trie": 1_200, "trip advisor": 7_500, "train times": 6_800,
        "translate": 25_000, "transformer model": 3_100, "true crime": 2_400,
        "tram map": 900, "trivia": 1_900, "tricks for dogs": 400,
        "weather": 40_000, "weather tomorrow": 12_000, "web crawler": 800,
    }
    blocked = {"true crime"}                 # filtered offline before the build
    t = AutocompleteTrie(k=3)
    t.build({q: c for q, c in counts.items() if q not in blocked})
    for p in ("t", "tr", "tri", "tra", "we", "x"):
        print(f"{p!r:>6} -> {t.suggest(p)}")
    print("trie nodes:", t.nodes, "| top-k stored per node: 3")

    # next build picks up a trending query; the old trie keeps serving until the swap
    counts["trip advisor"] += 30_000
    t2 = AutocompleteTrie(k=3)
    t2.build({q: c for q, c in counts.items() if q not in blocked})
    print("after rebuild, 'tr' ->", t2.suggest("tr"))
