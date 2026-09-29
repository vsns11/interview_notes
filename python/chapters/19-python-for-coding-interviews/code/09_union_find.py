"""19.9 Union-Find (disjoint set union) with path compression + union by size."""


class DSU:
    def __init__(self, n: int):
        self.parent = list(range(n))   # each node starts as its own root
        self.size = [1] * n
        self.components = n

    def find(self, x: int) -> int:
        root = x
        while self.parent[root] != root:
            root = self.parent[root]
        while self.parent[x] != root:          # path compression (iterative)
            self.parent[x], x = root, self.parent[x]
        return root

    def union(self, a: int, b: int) -> bool:
        ra, rb = self.find(a), self.find(b)
        if ra == rb:
            return False                        # already connected
        if self.size[ra] < self.size[rb]:
            ra, rb = rb, ra                     # attach smaller under larger
        self.parent[rb] = ra
        self.size[ra] += self.size[rb]
        self.components -= 1
        return True


def count_components(n: int, edges: list[tuple[int, int]]) -> int:
    dsu = DSU(n)
    for a, b in edges:
        dsu.union(a, b)
    return dsu.components


def redundant_connection(edges: list[tuple[int, int]]) -> tuple[int, int] | None:
    """Nodes are 1..n. Return the edge that closes a cycle."""
    dsu = DSU(len(edges) + 1)
    for a, b in edges:
        if not dsu.union(a, b):
            return a, b
    return None


def kruskal_mst(n: int, edges: list[tuple[int, int, int]]) -> int:
    """edges (weight, u, v). Total weight of a minimum spanning tree."""
    dsu, total = DSU(n), 0
    for w, u, v in sorted(edges):
        if dsu.union(u, v):
            total += w
    return total


if __name__ == "__main__":
    d = DSU(6)
    d.union(0, 1)
    d.union(1, 2)
    d.union(3, 4)
    print("parent:", d.parent, "components:", d.components)
    assert d.find(2) == d.find(0) and d.find(3) != d.find(0)
    assert count_components(5, [(0, 1), (1, 2), (3, 4)]) == 2
    assert redundant_connection([(1, 2), (1, 3), (2, 3)]) == (2, 3)
    assert kruskal_mst(4, [(1, 0, 1), (4, 0, 2), (3, 1, 2), (2, 2, 3), (5, 1, 3)]) == 6
    print("all asserts passed")
