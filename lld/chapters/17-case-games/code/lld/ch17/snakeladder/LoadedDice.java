package lld.ch17.snakeladder;

/** Returns a scripted sequence of rolls (cycling). Used to test exact scenarios. */
public final class LoadedDice implements Dice {
    private final int[] rolls;
    private int i;

    public LoadedDice(int... rolls) { this.rolls = rolls.clone(); }

    @Override public int roll() { return rolls[i++ % rolls.length]; }

    @Override public int maxRoll() { return 6; }
}
