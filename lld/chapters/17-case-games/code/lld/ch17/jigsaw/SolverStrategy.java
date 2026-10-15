package lld.ch17.jigsaw;

/**
 * Strategy pattern: one interface, many ways to solve. A solver works on the given board, keeps any
 * pieces already placed, and fills the empty cells using the pieces not yet on the board.
 */
public interface SolverStrategy {
    String name();

    SolveResult solve(Puzzle puzzle, Board board);
}
