package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Topological sort: Kahn's algorithm (BFS on in-degrees) and DFS with three colours. */
public class TopoSort {
    /** Order to take n courses; prereq {a, b} means b must come before a. Empty list if there is a cycle. */
    static List<Integer> kahn(int n, int[][] prereqs) {
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        int[] indegree = new int[n];
        for (int[] p : prereqs) { g.get(p[1]).add(p[0]); indegree[p[0]]++; }

        Deque<Integer> ready = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (indegree[i] == 0) ready.offer(i);
        List<Integer> order = new ArrayList<>();
        while (!ready.isEmpty()) {
            int u = ready.poll();
            order.add(u);
            for (int v : g.get(u)) if (--indegree[v] == 0) ready.offer(v);
        }
        return order.size() == n ? order : List.of();   // leftovers are stuck in a cycle
    }

    /** DFS version: WHITE = unvisited, GRAY = on the current path, BLACK = finished. GRAY -> GRAY = cycle. */
    static List<Integer> dfsOrder(int n, int[][] prereqs) {
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] p : prereqs) g.get(p[1]).add(p[0]);
        int[] colour = new int[n];                        // 0 white, 1 gray, 2 black
        Deque<Integer> finished = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (colour[i] == 0 && !visit(g, i, colour, finished)) return List.of();
        return new ArrayList<>(finished);                 // reverse post-order
    }

    private static boolean visit(List<List<Integer>> g, int u, int[] colour, Deque<Integer> finished) {
        colour[u] = 1;
        for (int v : g.get(u)) {
            if (colour[v] == 1) return false;             // back edge -> cycle
            if (colour[v] == 0 && !visit(g, v, colour, finished)) return false;
        }
        colour[u] = 2;
        finished.push(u);                                 // push = prepend, giving reverse post-order
        return true;
    }

    public static void main(String[] args) {
        int[][] prereqs = {{1, 0}, {2, 0}, {3, 1}, {3, 2}};
        eq(kahn(4, prereqs), List.of(0, 1, 2, 3), "kahn order");
        eq(kahn(2, new int[][]{{0, 1}, {1, 0}}), List.of(), "kahn cycle");
        eq(dfsOrder(4, prereqs), List.of(0, 2, 1, 3), "dfs order");
        eq(dfsOrder(3, new int[][]{{1, 0}, {2, 1}, {0, 2}}), List.of(), "dfs cycle");
        done();
    }
}
