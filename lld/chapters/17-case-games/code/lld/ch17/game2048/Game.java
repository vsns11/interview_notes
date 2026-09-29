package lld.ch17.game2048;

/**
 * One game: board + spawner + score + status. Reaching the target tile sets WON once;
 * the player may keep going ("continue" mode). No move left sets LOST.
 */
public final class Game {
    private final Board board;
    private final TileSpawner spawner;
    private final int target;
    private int score;
    private int moves;
    private boolean reachedTarget;
    private GameStatus status = GameStatus.PLAYING;

    /** New game: an empty board with two starting tiles (two 2s with the standard spawner). */
    public Game(int size, int target, TileSpawner spawner) {
        this(new Board(size), target, spawner);
        spawner.spawnStart(board);
        spawner.spawnStart(board);
        updateStatus();
    }

    /** Game from a preset board (used by tests). */
    public Game(Board preset, int target, TileSpawner spawner) {
        this.board = preset;
        this.target = target;
        this.spawner = spawner;
        updateStatus();
    }

    /** Returns true if the move changed the board (and so counted as a turn). */
    public boolean move(Direction d) {
        if (status == GameStatus.LOST) throw new IllegalStateException("game over");
        int points = board.slide(d);
        if (points < 0) return false;          // nothing moved: no spawn, no turn
        score += points;
        moves++;
        spawner.spawn(board);                  // at least one cell is empty, because something moved or merged
        updateStatus();
        return true;
    }

    private void updateStatus() {
        if (!reachedTarget && board.maxTile() >= target) { reachedTarget = true; status = GameStatus.WON; }
        else if (status == GameStatus.WON) status = GameStatus.PLAYING;   // "keep going" after the win screen
        if (!board.hasMove()) status = GameStatus.LOST;
    }

    public GameStatus status() { return status; }
    public boolean reachedTarget() { return reachedTarget; }
    public int score() { return score; }
    public int moves() { return moves; }
    public Board board() { return board; }
}
