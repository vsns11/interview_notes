package lld.ch17.tictactoe;

import java.util.ArrayList;
import java.util.List;

/**
 * N x N grid plus per-player line counters, so "did that move win?" is O(1) instead of O(N^2).
 * rows[p][r] = how many marks player p has in row r. Same idea for columns and the two diagonals.
 * A line is won when one player's count reaches N (other players' marks make that impossible).
 */
public final class Board {
    private final int n;
    private final int[][] grid;       // -1 empty, otherwise player index
    private final int[][] rows, cols;
    private final int[] diag, anti;
    private int filled;

    public Board(int n, int players) {
        if (n < 3) throw new IllegalArgumentException("board must be at least 3x3");
        this.n = n;
        grid = new int[n][n];
        for (int[] row : grid) java.util.Arrays.fill(row, -1);
        rows = new int[players][n];
        cols = new int[players][n];
        diag = new int[players];
        anti = new int[players];
    }

    public int size() { return n; }

    public boolean isEmpty(Cell c) { return inside(c) && grid[c.row()][c.col()] == -1; }

    private boolean inside(Cell c) { return c.row() >= 0 && c.row() < n && c.col() >= 0 && c.col() < n; }

    /** Places a mark and returns true if this move completed a line. O(1). */
    boolean place(Cell c, int player) {
        if (!inside(c)) throw new IllegalArgumentException("cell " + c + " is off the board");
        if (grid[c.row()][c.col()] != -1) throw new IllegalArgumentException("cell " + c + " is already taken");
        grid[c.row()][c.col()] = player;
        filled++;
        int r = c.row(), k = c.col();
        boolean win = ++rows[player][r] == n;
        win |= ++cols[player][k] == n;
        if (r == k) win |= ++diag[player] == n;
        if (r + k == n - 1) win |= ++anti[player] == n;
        return win;
    }

    /** Would placing player's mark on this empty cell win? Also O(1): a line needs exactly one more mark. */
    public boolean wouldWin(Cell c, int player) {
        int r = c.row(), k = c.col();
        return rows[player][r] == n - 1 || cols[player][k] == n - 1
                || (r == k && diag[player] == n - 1) || (r + k == n - 1 && anti[player] == n - 1);
    }

    public boolean isFull() { return filled == n * n; }

    public List<Cell> emptyCells() {
        List<Cell> out = new ArrayList<>();
        for (int r = 0; r < n; r++) for (int c = 0; c < n; c++) if (grid[r][c] == -1) out.add(new Cell(r, c));
        return out;
    }

    public String render(char[] marks) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < n; r++) {
            sb.append("    ");
            for (int c = 0; c < n; c++) sb.append(grid[r][c] == -1 ? '.' : marks[grid[r][c]]).append(c < n - 1 ? " " : "");
            if (r < n - 1) sb.append('\n');
        }
        return sb.toString();
    }
}
