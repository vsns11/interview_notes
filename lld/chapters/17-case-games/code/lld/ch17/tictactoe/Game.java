package lld.ch17.tictactoe;

import java.util.List;
import java.util.function.Consumer;

/** Turn management and game state. Supports 2+ players on an N x N board. */
public final class Game {
    private final Board board;
    private final List<Player> players;
    private final char[] marks;
    private final Consumer<String> log;
    private int current;
    private GameState state = GameState.IN_PROGRESS;
    private Player winner;

    public Game(int n, List<Player> players, Consumer<String> log) {
        if (players.size() < 2) throw new IllegalArgumentException("need at least 2 players");
        this.players = List.copyOf(players);
        this.board = new Board(n, players.size());
        this.marks = new char[players.size()];
        for (int i = 0; i < marks.length; i++) marks[i] = players.get(i).mark();
        this.log = log;
    }

    /** Plays one turn for the current player. An invalid cell is rejected and the same player goes again. */
    public void playTurn() {
        if (state != GameState.IN_PROGRESS) throw new IllegalStateException("game is over");
        Player p = players.get(current);
        Cell cell = p.strategy().nextMove(board, current, players.size());
        boolean won;
        try {
            won = board.place(cell, current);
        } catch (IllegalArgumentException e) {
            log.accept("  " + p.name() + " tried " + cell + ": rejected (" + e.getMessage() + "), try again");
            return;
        }
        log.accept("  " + p.name() + " (" + p.mark() + ") -> " + cell);
        if (won) { state = GameState.WON; winner = p; }
        else if (board.isFull()) state = GameState.DRAW;
        else current = (current + 1) % players.size();
    }

    public GameState play() {
        while (state == GameState.IN_PROGRESS) playTurn();
        log.accept(board.render(marks));
        log.accept("  Result: " + (state == GameState.WON ? winner.name() + " wins" : "draw"));
        return state;
    }
}
