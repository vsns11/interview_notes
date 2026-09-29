package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Disjoint-set union with path compression and union by size: near-O(1) per operation. */
public class UnionFind {
    private final int[] parent, size;
    private int components;

    UnionFind(int n) {
        parent = new int[n];
        size = new int[n];
        components = n;
        for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
    }

    int find(int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];                 // path halving: point to grandparent
            x = parent[x];
        }
        return x;
    }

    /** Returns false if a and b were already connected (the new edge would close a cycle). */
    boolean union(int a, int b) {
        int ra = find(a), rb = find(b);
        if (ra == rb) return false;
        if (size[ra] < size[rb]) { int t = ra; ra = rb; rb = t; }
        parent[rb] = ra;                                   // attach the smaller tree under the larger
        size[ra] += size[rb];
        components--;
        return true;
    }

    int components() { return components; }

    /** The edge that creates a cycle in an undirected graph (nodes 1..n). */
    static int[] redundantEdge(int[][] edges) {
        UnionFind uf = new UnionFind(edges.length + 1);
        for (int[] e : edges) if (!uf.union(e[0], e[1])) return e;
        return new int[0];
    }

    /** Kruskal's minimum spanning tree weight: sort edges by weight, keep those that join two components. */
    static int kruskal(int n, int[][] edges) {
        int[][] sorted = edges.clone();
        Arrays.sort(sorted, Comparator.comparingInt(e -> e[2]));
        UnionFind uf = new UnionFind(n);
        int total = 0;
        for (int[] e : sorted) if (uf.union(e[0], e[1])) total += e[2];
        return total;
    }

    public static void main(String[] args) {
        UnionFind uf = new UnionFind(5);
        uf.union(0, 1); uf.union(3, 4);
        eq(uf.components(), 3, "components");
        isTrue(uf.find(0) == uf.find(1), "0 and 1 connected");
        isTrue(uf.find(1) != uf.find(3), "1 and 3 separate");
        eq(redundantEdge(new int[][]{{1, 2}, {1, 3}, {2, 3}}), new int[]{2, 3}, "redundant edge");
        eq(kruskal(4, new int[][]{{0, 1, 10}, {0, 2, 6}, {0, 3, 5}, {1, 3, 15}, {2, 3, 4}}), 19, "kruskal MST");
        done();
    }
}
