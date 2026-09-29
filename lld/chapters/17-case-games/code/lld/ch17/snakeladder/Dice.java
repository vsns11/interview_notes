package lld.ch17.snakeladder;

/** Strategy for rolling. Real dice use a seeded Random; tests use a fixed sequence. */
public interface Dice {
    int roll();
    int maxRoll();
}
