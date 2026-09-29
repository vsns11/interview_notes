package lld.ch17.game2048;

import java.util.ArrayList;
import java.util.List;

/** N x N grid of tile values (0 = empty). Knows how to slide in a direction and whether any move is left. */
public final class Board {
    private final int n;
    private final int[][] cells;

    public Board(int n) {
        if (n < 2) throw new IllegalArgumentException("board too small");
        this.n = n;
        this.cells = new int[n][n];
    }

    public static Board of(int[][] values) {
        Board b = new Board(values.length);
        for (int r = 0; r < b.n; r++) b.cells[r] = values[r].clone();
        return b;
    }

    public int size() { return n; }
    public int get(int r, int c) { return cells[r][c]; }
    void set(int r, int c, int v) { cells[r][c] = v; }

    public List<int[]> emptyCells() {
        List<int[]> out = new ArrayList<>();
        for (int r = 0; r < n; r++) for (int c = 0; c < n; c++) if (cells[r][c] == 0) out.add(new int[]{r, c});
        return out;
    }

    /**
     * Slides every line toward the given wall. Returns the points earned, or -1 if nothing moved
     * (a move that changes nothing is not a turn and must not spawn a tile).
     */
    int slide(Direction d) {
        boolean changed = false;
        int points = 0;
        for (int i = 0; i < n; i++) {
            int[] line = new int[n];
            for (int j = 0; j < n; j++) line[j] = read(d, i, j);       // j = 0 is the cell next to the wall
            LineMerger.Result res = LineMerger.merge(line);
            for (int j = 0; j < n; j++) {
                if (read(d, i, j) != res.line()[j]) changed = true;
                write(d, i, j, res.line()[j]);
            }
            points += res.points();
        }
        return changed ? points : -1;
    }

    /** Maps (line i, position j from the wall) to a real cell for each direction. */
    private int[] coords(Direction d, int i, int j) {
        return switch (d) {
            case LEFT -> new int[]{i, j};
            case RIGHT -> new int[]{i, n - 1 - j};
            case UP -> new int[]{j, i};
            case DOWN -> new int[]{n - 1 - j, i};
        };
    }

    private int read(Direction d, int i, int j) { int[] rc = coords(d, i, j); return cells[rc[0]][rc[1]]; }
    private void write(Direction d, int i, int j, int v) { int[] rc = coords(d, i, j); cells[rc[0]][rc[1]] = v; }

    /** A move exists if a cell is empty or two neighbours (right or below) are equal. */
    public boolean hasMove() {
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++) {
                if (cells[r][c] == 0) return true;
                if (c + 1 < n && cells[r][c] == cells[r][c + 1]) return true;
                if (r + 1 < n && cells[r][c] == cells[r + 1][c]) return true;
            }
        return false;
    }

    public int maxTile() {
        int m = 0;
        for (int[] row : cells) for (int v : row) m = Math.max(m, v);
        return m;
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < n; r++) {
            sb.append("   ");
            for (int c = 0; c < n; c++) sb.append(String.format("%5s", cells[r][c] == 0 ? "." : cells[r][c]));
            if (r < n - 1) sb.append('\n');
        }
        return sb.toString();
    }
}
