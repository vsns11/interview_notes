package lld.ch17.jigsaw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The way people solve a jigsaw: sort pieces by kind, start in a corner, walk the frame, then fill the
 * inside row by row. Instead of trying every piece, it looks up only the pieces that have the side
 * the cell needs, using an index from Side to (piece, side position). Still backtracks on look-alikes.
 */
public final class EdgeFirstSolver implements SolverStrategy {
    private record Hit(Piece piece, int sideIndex) {}

    private final long maxAttempts;
    private long attempts, backtracks;
    private boolean gaveUp;
    private Map<Side, List<Hit>> index;

    public EdgeFirstSolver(long maxAttempts) { this.maxAttempts = maxAttempts; }

    @Override public String name() { return "EdgeFirst+Index"; }

    @Override public SolveResult solve(Puzzle puzzle, Board board) {
        attempts = backtracks = 0;
        gaveUp = false;
        index = new HashMap<>();
        for (Piece p : puzzle.pieces())
            for (int i = 0; i < 4; i++)
                if (!p.side(i).isFlat()) index.computeIfAbsent(p.side(i), k -> new ArrayList<>()).add(new Hit(p, i));
        boolean ok = fill(puzzle, board, order(board), 0);
        return new SolveResult(name(), ok, attempts, backtracks);
    }

    /** Frame first, clockwise from the top-left corner; then the inside, row by row. */
    static List<int[]> order(Board b) {
        List<int[]> cells = new ArrayList<>();
        int R = b.rows() - 1, C = b.cols() - 1;
        for (int c = 0; c <= C; c++) cells.add(new int[]{0, c});
        for (int r = 1; r <= R; r++) cells.add(new int[]{r, C});
        for (int c = C - 1; c >= 0; c--) cells.add(new int[]{R, c});
        for (int r = R - 1; r >= 1; r--) cells.add(new int[]{r, 0});
        for (int r = 1; r < R; r++) for (int c = 1; c < C; c++) cells.add(new int[]{r, c});
        return cells;
    }

    private boolean fill(Puzzle puzzle, Board board, List<int[]> order, int k) {
        if (k == order.size()) return true;
        int r = order.get(k)[0], c = order.get(k)[1];
        if (board.at(r, c) != null) return fill(puzzle, board, order, k + 1);
        for (Board.Placement option : candidates(puzzle, board, r, c)) {
            if (gaveUp || attempts >= maxAttempts) { gaveUp = true; return false; }   // budget spent
            attempts++;
            if (board.place(option.piece(), option.rotation(), r, c) != PlaceResult.OK) continue;
            if (fill(puzzle, board, order, k + 1)) return true;
            board.remove(r, c);
            if (!gaveUp) backtracks++;
        }
        return false;
    }

    /** Pieces that could go at (r, c): looked up by the side a placed neighbour needs, else every piece of the right kind. */
    private List<Board.Placement> candidates(Puzzle puzzle, Board board, int r, int c) {
        PieceKind wanted = board.kindOfCell(r, c);
        List<Board.Placement> out = new ArrayList<>();
        for (Direction d : Direction.values()) {
            int nr = r + d.dRow, nc = c + d.dCol;
            if (!board.inside(nr, nc) || board.at(nr, nc) == null) continue;
            Side needed = board.at(nr, nc).side(d.opposite()).partner();   // what must face d from this cell
            for (Hit h : index.getOrDefault(needed, List.of()))
                if (!board.isPlaced(h.piece().id()) && h.piece().kind() == wanted)
                    out.add(new Board.Placement(h.piece(), Math.floorMod(d.ordinal() - h.sideIndex(), 4)));
            return out;                                                    // one neighbour narrows enough
        }
        for (Piece p : puzzle.pieces())                                    // no neighbour yet: the first corner
            if (!board.isPlaced(p.id()) && p.kind() == wanted)
                for (int rot = 0; rot < 4; rot++) out.add(new Board.Placement(p, rot));
        return out;
    }
}
