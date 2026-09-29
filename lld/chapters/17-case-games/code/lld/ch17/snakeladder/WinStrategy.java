package lld.ch17.snakeladder;

/** When is the whole game over? */
@FunctionalInterface
public interface WinStrategy {
    boolean isGameOver(int finishedPlayers, int totalPlayers);

    /** Classic: the first player to reach the end wins and the game stops. */
    static WinStrategy firstToFinish() { return (finished, total) -> finished >= 1; }

    /** Play on to rank everybody; stop when only one player is left on the board. */
    static WinStrategy rankAll() { return (finished, total) -> finished >= total - 1; }
}
