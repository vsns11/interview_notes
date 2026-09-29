package lld.ch17.game2048;

public enum Direction {
    LEFT, RIGHT, UP, DOWN;

    /** Command-line input of the common machine-coding statement: 0 = left, 1 = right, 2 = up, 3 = down. */
    public static Direction fromKey(int key) {
        return switch (key) {
            case 0 -> LEFT;
            case 1 -> RIGHT;
            case 2 -> UP;
            case 3 -> DOWN;
            default -> throw new IllegalArgumentException("unknown move " + key + " (use 0-3)");
        };
    }
}
