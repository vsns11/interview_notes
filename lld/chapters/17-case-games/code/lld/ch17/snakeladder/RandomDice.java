package lld.ch17.snakeladder;

import java.util.Random;

/** count dice with the given number of faces. Pass new Random(seed) for repeatable games. */
public final class RandomDice implements Dice {
    private final int count, faces;
    private final Random rng;

    public RandomDice(int count, int faces, Random rng) {
        this.count = count;
        this.faces = faces;
        this.rng = rng;
    }

    @Override public int roll() {
        int sum = 0;
        for (int i = 0; i < count; i++) sum += 1 + rng.nextInt(faces);
        return sum;
    }

    @Override public int maxRoll() { return count * faces; }
}
