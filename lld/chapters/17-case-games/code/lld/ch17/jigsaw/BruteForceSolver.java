package lld.ch17.jigsaw;

/**
 * Baseline strategy: fill cells row by row; at each empty cell try every unused piece in all four
 * rotations; undo and try the next option when the rest cannot be completed (backtracking).
 */
public final class BruteForceSolver implements SolverStrategy {
    private final long maxAttempts;
    private long attempts, backtracks;
    private boolean gaveUp;

    public BruteForceSolver(long maxAttempts) { this.maxAttempts = maxAttempts; }

    @Override public String name() { return "BruteForce"; }

    @Override public SolveResult solve(Puzzle puzzle, Board board) {
        attempts = backtracks = 0;
        gaveUp = false;
        boolean ok = fill(puzzle, board, 0);
        return new SolveResult(name(), ok, attempts, backtracks);
    }

    private boolean fill(Puzzle puzzle, Board board, int cell) {
        if (cell == board.size()) return true;
        int r = cell / board.cols(), c = cell % board.cols();
        if (board.at(r, c) != null) return fill(puzzle, board, cell + 1);      // keep the player's pieces
        for (Piece piece : puzzle.pieces()) {
            if (board.isPlaced(piece.id())) continue;
            for (int rot = 0; rot < 4; rot++) {
                if (gaveUp || attempts >= maxAttempts) { gaveUp = true; return false; }   // budget spent
                attempts++;
                if (board.place(piece, rot, r, c) != PlaceResult.OK) continue;
                if (fill(puzzle, board, cell + 1)) return true;
                board.remove(r, c);
                if (!gaveUp) backtracks++;
            }
        }
        return false;
    }
}
