package lld.ch17.jigsaw;

/** The four sides of a cell, in clockwise order. dRow/dCol point at the neighbouring cell. */
public enum Direction {
    TOP(-1, 0), RIGHT(0, 1), BOTTOM(1, 0), LEFT(0, -1);

    public final int dRow, dCol;

    Direction(int dRow, int dCol) { this.dRow = dRow; this.dCol = dCol; }

    public Direction opposite() { return values()[(ordinal() + 2) % 4]; }
}
