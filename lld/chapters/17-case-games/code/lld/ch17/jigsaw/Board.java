package lld.ch17.jigsaw;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The rows x cols frame the picture is built on. It owns every placement rule, so the player,
 * the game and every solver ask the same question: check(piece, rotation, row, col).
 */
public final class Board {
    /** A piece lying on a cell, turned clockwise 'rotation' quarter turns. */
    public record Placement(Piece piece, int rotation) {
        public Side side(Direction d) { return piece.side(d, rotation); }
    }

    private final int rows, cols;
    private final Placement[][] cells;
    private final Set<Integer> placedIds = new HashSet<>();

    public Board(int rows, int cols) {
        if (rows < 2 || cols < 2) throw new IllegalArgumentException("board must be at least 2 x 2");
        this.rows = rows;
        this.cols = cols;
        this.cells = new Placement[rows][cols];
    }

    public int rows() { return rows; }
    public int cols() { return cols; }
    public int size() { return rows * cols; }
    public int filled() { return placedIds.size(); }
    public boolean isFull() { return filled() == size(); }
    public boolean isPlaced(int pieceId) { return placedIds.contains(pieceId); }
    public Placement at(int r, int c) { return cells[r][c]; }
    public boolean inside(int r, int c) { return r >= 0 && r < rows && c >= 0 && c < cols; }

    /** How many sides of this cell face outside the picture: 2 = corner cell, 1 = border cell, 0 = interior. */
    public PieceKind kindOfCell(int r, int c) {
        int out = 0;
        for (Direction d : Direction.values()) if (!inside(r + d.dRow, c + d.dCol)) out++;
        return out == 2 ? PieceKind.CORNER : out == 1 ? PieceKind.BORDER : PieceKind.INTERIOR;
    }

    /** Every rule for one placement, checked against the frame and against neighbours already placed. */
    public PlaceResult check(Piece piece, int rotation, int r, int c) {
        if (rotation < 0 || rotation > 3) throw new IllegalArgumentException("rotation must be 0..3");
        if (!inside(r, c)) return PlaceResult.OUT_OF_BOUNDS;
        if (cells[r][c] != null) return PlaceResult.CELL_OCCUPIED;
        if (placedIds.contains(piece.id())) return PlaceResult.ALREADY_PLACED;
        for (Direction d : Direction.values()) {
            Side mine = piece.side(d, rotation);
            int nr = r + d.dRow, nc = c + d.dCol;
            if (!inside(nr, nc)) {
                if (!mine.isFlat()) return PlaceResult.EDGE_MUST_BE_FLAT;
                continue;
            }
            if (mine.isFlat()) return PlaceResult.FLAT_FACES_INSIDE;
            Placement neighbour = cells[nr][nc];
            if (neighbour != null && !mine.fits(neighbour.side(d.opposite()))) return PlaceResult.SIDES_DO_NOT_FIT;
        }
        return PlaceResult.OK;
    }

    public PlaceResult place(Piece piece, int rotation, int r, int c) {
        PlaceResult result = check(piece, rotation, r, c);
        if (result == PlaceResult.OK) {
            cells[r][c] = new Placement(piece, rotation);
            placedIds.add(piece.id());
        }
        return result;
    }

    public Placement remove(int r, int c) {
        Placement p = cells[r][c];
        if (p != null) {
            cells[r][c] = null;
            placedIds.remove(p.piece().id());
        }
        return p;
    }

    /**
     * Independent check of a finished board: every cell filled, every outward side flat, every joint fits.
     * place() already enforces this, but a board loaded from a save file or built by other code may not.
     */
    public List<String> verify() {
        List<String> problems = new ArrayList<>();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                Placement p = cells[r][c];
                if (p == null) { problems.add("empty cell (" + r + "," + c + ")"); continue; }
                for (Direction d : Direction.values()) {
                    int nr = r + d.dRow, nc = c + d.dCol;
                    if (!inside(nr, nc)) {
                        if (!p.side(d).isFlat()) problems.add("(" + r + "," + c + ") " + d + " should be flat");
                    } else if ((d == Direction.RIGHT || d == Direction.BOTTOM) && cells[nr][nc] != null
                            && !p.side(d).fits(cells[nr][nc].side(d.opposite()))) {
                        problems.add("joint (" + r + "," + c + ")-(" + nr + "," + nc + ") does not fit");
                    }
                }
            }
        return problems;
    }

    public boolean isSolved() { return isFull() && verify().isEmpty(); }

    /** Each cell shows piece id and rotation, e.g. "7r2"; "." is empty. */
    public String render() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rows; r++) {
            sb.append("   ");
            for (int c = 0; c < cols; c++) {
                Placement p = cells[r][c];
                sb.append(String.format("%6s", p == null ? "." : p.piece().id() + "r" + p.rotation()));
            }
            if (r < rows - 1) sb.append('\n');
        }
        return sb.toString();
    }
}
