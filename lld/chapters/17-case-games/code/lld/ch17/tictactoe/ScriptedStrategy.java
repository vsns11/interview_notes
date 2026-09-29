package lld.ch17.tictactoe;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/** Plays a fixed list of moves. Stands in for a human in tests. */
public final class ScriptedStrategy implements MoveStrategy {
    private final Deque<Cell> moves;

    public ScriptedStrategy(List<Cell> moves) { this.moves = new ArrayDeque<>(moves); }

    @Override public Cell nextMove(Board board, int me, int playerCount) { return moves.poll(); }
}
