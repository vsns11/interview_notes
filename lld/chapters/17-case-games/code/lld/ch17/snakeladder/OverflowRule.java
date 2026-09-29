package lld.ch17.snakeladder;

/** What happens when a roll would carry a token past the last square. */
@FunctionalInterface
public interface OverflowRule {
    /** Returns the new square (before snakes/ladders), given current square, roll and board size. */
    int target(int from, int roll, int size);

    /** Must land exactly on the last square, otherwise stay put. */
    static OverflowRule exactFinish() { return (from, roll, size) -> from + roll > size ? from : from + roll; }

    /** Bounce back by the extra amount (at 98 on a 100 board, a 5 goes to 100 then back to 97). */
    static OverflowRule bounceBack() { return (from, roll, size) -> from + roll > size ? size - (from + roll - size) : from + roll; }
}
