package lld.ch17.jigsaw;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** What comes in the box: the picture's size and a jumbled list of pieces. */
public final class Puzzle {
    private final int rows, cols;
    private final List<Piece> pieces;
    private final Map<Integer, Piece> byId = new LinkedHashMap<>();

    public Puzzle(int rows, int cols, List<Piece> pieces) {
        this.rows = rows;
        this.cols = cols;
        this.pieces = List.copyOf(pieces);
        for (Piece p : pieces) byId.put(p.id(), p);
    }

    public int rows() { return rows; }
    public int cols() { return cols; }
    public List<Piece> pieces() { return pieces; }

    public Piece piece(int id) {
        Piece p = byId.get(id);
        if (p == null) throw new IllegalArgumentException("no piece " + id);
        return p;
    }
}
