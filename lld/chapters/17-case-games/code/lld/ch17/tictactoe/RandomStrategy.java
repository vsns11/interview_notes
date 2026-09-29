package lld.ch17.tictactoe;

import java.util.List;
import java.util.Random;

/** Picks any empty cell. Takes a seeded Random so games are repeatable. */
public final class RandomStrategy implements MoveStrategy {
    private final Random rng;

    public RandomStrategy(Random rng) { this.rng = rng; }

    @Override public Cell nextMove(Board board, int me, int playerCount) {
        List<Cell> empty = board.emptyCells();
        return empty.get(rng.nextInt(empty.size()));
    }
}
