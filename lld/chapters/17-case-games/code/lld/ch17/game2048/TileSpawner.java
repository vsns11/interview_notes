package lld.ch17.game2048;

/** Strategy for adding a new tile after each successful move. */
public interface TileSpawner {
    /** Adds one tile to a random empty cell. The board must have at least one empty cell. */
    void spawn(Board board);

    /** One of the two tiles placed when a new game starts. By default the same as spawn(). */
    default void spawnStart(Board board) { spawn(board); }
}
