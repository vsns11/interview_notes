package lld.ch17.jigsaw;

import java.util.List;

/**
 * One puzzle being assembled. Players place and remove pieces by id; a solver strategy can finish the job.
 * Methods are synchronized so several players can build the same puzzle together (co-op mode).
 */
public final class JigsawGame {
    private final Puzzle puzzle;
    private final Board board;
    private int moves;
    private GameStatus status = GameStatus.IN_PROGRESS;

    public JigsawGame(Puzzle puzzle) {
        List<String> problems = PuzzleValidator.validate(puzzle);
        if (!problems.isEmpty()) throw new IllegalArgumentException("invalid puzzle: " + problems);
        this.puzzle = puzzle;
        this.board = new Board(puzzle.rows(), puzzle.cols());
    }

    public synchronized PlaceResult place(int pieceId, int rotation, int row, int col) {
        if (status == GameStatus.SOLVED) throw new IllegalStateException("puzzle already solved");
        PlaceResult result = board.place(puzzle.piece(pieceId), rotation, row, col);
        if (result == PlaceResult.OK) {
            moves++;
            if (board.isSolved()) status = GameStatus.SOLVED;
        }
        return result;
    }

    public synchronized boolean remove(int row, int col) {
        if (status == GameStatus.SOLVED) throw new IllegalStateException("puzzle already solved");
        return board.remove(row, col) != null;
    }

    /** Let a strategy finish from the current position. On failure, the player's own pieces are kept. */
    public synchronized SolveResult finishWith(SolverStrategy solver) {
        SolveResult result = solver.solve(puzzle, board);
        if (board.isSolved()) status = GameStatus.SOLVED;
        return result;
    }

    public synchronized GameStatus status() { return status; }
    public synchronized int moves() { return moves; }
    public synchronized String progress() { return board.filled() + "/" + board.size(); }
    public synchronized String render() { return board.render(); }
    public Puzzle puzzle() { return puzzle; }
}
