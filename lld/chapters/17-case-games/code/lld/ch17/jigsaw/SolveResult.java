package lld.ch17.jigsaw;

/** What a solver reports: did it finish, how many placements it tried, and how many it had to undo. */
public record SolveResult(String strategy, boolean solved, long attempts, long backtracks) {
    @Override public String toString() {
        return String.format("%-18s solved=%-5s attempts=%,9d backtracks=%,9d", strategy, solved, attempts, backtracks);
    }
}
