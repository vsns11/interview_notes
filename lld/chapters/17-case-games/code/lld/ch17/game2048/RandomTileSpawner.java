package lld.ch17.game2048;

import java.util.List;
import java.util.Random;

/** Classic rule: a 2 with probability 0.9, else a 4, in a uniformly random empty cell. Seeded for tests. */
public final class RandomTileSpawner implements TileSpawner {
    private final Random rng;
    private final double probabilityOfTwo;

    public RandomTileSpawner(Random rng, double probabilityOfTwo) {
        this.rng = rng;
        this.probabilityOfTwo = probabilityOfTwo;
    }

    @Override public void spawn(Board board) {
        int[] cell = randomEmptyCell(board);
        board.set(cell[0], cell[1], rng.nextDouble() < probabilityOfTwo ? 2 : 4);
    }

    /** The problem statement starts every game with two tiles numbered 2. */
    @Override public void spawnStart(Board board) {
        int[] cell = randomEmptyCell(board);
        board.set(cell[0], cell[1], 2);
    }

    private int[] randomEmptyCell(Board board) {
        List<int[]> empty = board.emptyCells();
        if (empty.isEmpty()) throw new IllegalStateException("no empty cell");
        return empty.get(rng.nextInt(empty.size()));
    }
}
