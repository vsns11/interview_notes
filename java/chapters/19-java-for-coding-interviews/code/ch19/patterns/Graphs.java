package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** BFS and DFS on trees, grids and adjacency lists; Dijkstra for weighted shortest paths. */
public class Graphs {
    // ---------- trees ----------
    record TreeNode(int val, TreeNode left, TreeNode right) {
        static TreeNode leaf(int v) { return new TreeNode(v, null, null); }
    }

    /** DFS (recursive): height of a tree. */
    static int depth(TreeNode n) {
        return n == null ? 0 : 1 + Math.max(depth(n.left()), depth(n.right()));
    }

    /** BFS: values level by level. Snapshot the queue size to know where a level ends. */
    static List<List<Integer>> levels(TreeNode root) {
        List<List<Integer>> out = new ArrayList<>();
        Deque<TreeNode> q = new ArrayDeque<>();
        if (root != null) q.offer(root);
        while (!q.isEmpty()) {
            List<Integer> level = new ArrayList<>();
            for (int size = q.size(); size > 0; size--) {
                TreeNode n = q.poll();
                level.add(n.val());
                if (n.left() != null) q.offer(n.left());
                if (n.right() != null) q.offer(n.right());
            }
            out.add(level);
        }
        return out;
    }

    // ---------- grids ----------
    static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    /** Number of islands with an explicit stack (iterative DFS) so big grids can't overflow the call stack. */
    static int islands(char[][] grid) {
        int rows = grid.length, cols = grid[0].length, count = 0;
        boolean[][] seen = new boolean[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != '1' || seen[r][c]) continue;
                count++;
                Deque<int[]> stack = new ArrayDeque<>();
                stack.push(new int[]{r, c});
                seen[r][c] = true;
                while (!stack.isEmpty()) {
                    int[] cell = stack.pop();
                    for (int[] d : DIRS) {
                        int nr = cell[0] + d[0], nc = cell[1] + d[1];
                        if (nr < 0 || nc < 0 || nr >= rows || nc >= cols) continue;
                        if (grid[nr][nc] != '1' || seen[nr][nc]) continue;
                        seen[nr][nc] = true;           // mark when PUSHED, not when popped
                        stack.push(new int[]{nr, nc});
                    }
                }
            }
        }
        return count;
    }

    /** Fewest steps from top-left to bottom-right through '.' cells (BFS = shortest in unweighted graphs). */
    static int shortestPath(String[] maze) {
        int rows = maze.length, cols = maze[0].length();
        int[][] dist = new int[rows][cols];
        for (int[] row : dist) Arrays.fill(row, -1);
        Deque<int[]> q = new ArrayDeque<>();
        q.offer(new int[]{0, 0});
        dist[0][0] = 0;
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            if (cur[0] == rows - 1 && cur[1] == cols - 1) return dist[cur[0]][cur[1]];
            for (int[] d : DIRS) {
                int nr = cur[0] + d[0], nc = cur[1] + d[1];
                if (nr < 0 || nc < 0 || nr >= rows || nc >= cols) continue;
                if (maze[nr].charAt(nc) == '#' || dist[nr][nc] != -1) continue;
                dist[nr][nc] = dist[cur[0]][cur[1]] + 1;
                q.offer(new int[]{nr, nc});
            }
        }
        return -1;
    }

    // ---------- adjacency lists ----------
    static List<List<Integer>> undirected(int n, int[][] edges) {
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) { g.get(e[0]).add(e[1]); g.get(e[1]).add(e[0]); }
        return g;
    }

    /** Recursive DFS: the connected component containing start, in visit order. */
    static void dfs(List<List<Integer>> g, int node, boolean[] seen, List<Integer> order) {
        seen[node] = true;
        order.add(node);
        for (int next : g.get(node)) if (!seen[next]) dfs(g, next, seen, order);
    }

    /** Dijkstra: shortest distances from src with non-negative weights. edges = {from, to, weight}. */
    static long[] dijkstra(int n, int[][] edges, int src) {
        List<List<int[]>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) g.get(e[0]).add(new int[]{e[1], e[2]});
        long[] dist = new long[n];
        Arrays.fill(dist, Long.MAX_VALUE);
        dist[src] = 0;
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(x -> x[0]));
        pq.offer(new long[]{0, src});
        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            int u = (int) top[1];
            if (top[0] > dist[u]) continue;             // stale entry: a shorter path was found already
            for (int[] e : g.get(u)) {
                long nd = dist[u] + e[1];
                if (nd < dist[e[0]]) { dist[e[0]] = nd; pq.offer(new long[]{nd, e[0]}); }
            }
        }
        return dist;
    }

    public static void main(String[] args) {
        TreeNode root = new TreeNode(3, TreeNode.leaf(9), new TreeNode(20, TreeNode.leaf(15), TreeNode.leaf(7)));
        eq(depth(root), 3, "tree depth");
        eq(levels(root), List.of(List.of(3), List.of(9, 20), List.of(15, 7)), "level order");

        char[][] grid = {
                "11000".toCharArray(),
                "11000".toCharArray(),
                "00100".toCharArray(),
                "00011".toCharArray()};
        eq(islands(grid), 3, "islands");
        eq(shortestPath(new String[]{"..#", "#..", "..."}), 4, "maze BFS");
        eq(shortestPath(new String[]{".#", "#."}), -1, "maze blocked");

        List<Integer> order = new ArrayList<>();
        dfs(undirected(6, new int[][]{{0, 1}, {1, 2}, {0, 3}, {4, 5}}), 0, new boolean[6], order);
        eq(order, List.of(0, 1, 2, 3), "graph DFS component");

        long[] dist = dijkstra(4, new int[][]{{0, 1, 4}, {0, 2, 1}, {2, 1, 2}, {1, 3, 1}}, 0);
        eq(Arrays.toString(dist), "[0, 3, 1, 4]", "dijkstra");
        done();
    }
}
