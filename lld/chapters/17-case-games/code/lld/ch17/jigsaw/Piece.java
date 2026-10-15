package lld.ch17.jigsaw;

import java.util.Arrays;

/**
 * An immutable piece: an id and four sides listed clockwise (TOP, RIGHT, BOTTOM, LEFT) as the piece
 * lies in the box (rotation 0). Rotation is not stored here: it is part of a placement on the board.
 */
public final class Piece {
    private final int id;
    private final Side[] sides;   // index = Direction.ordinal() at rotation 0

    public Piece(int id, Side top, Side right, Side bottom, Side left) {
        this.id = id;
        this.sides = new Side[]{top, right, bottom, left};
        if (flatCount() > 2) throw new IllegalArgumentException("piece " + id + " has more than two flat sides");
        if ((top.isFlat() && bottom.isFlat()) || (left.isFlat() && right.isFlat()))
            throw new IllegalArgumentException("piece " + id + ": two flat sides must be adjacent (a corner)");
    }

    public int id() { return id; }

    /** Side at rotation 0. */
    public Side side(int index) { return sides[index]; }

    /**
     * The side that faces direction d after turning the piece clockwise 'rotation' quarter turns.
     * Turning clockwise once moves what was on TOP to RIGHT, so the side now facing d came from d - rotation.
     */
    public Side side(Direction d, int rotation) {
        return sides[Math.floorMod(d.ordinal() - rotation, 4)];
    }

    public int flatCount() {
        int n = 0;
        for (Side s : sides) if (s.isFlat()) n++;
        return n;
    }

    public PieceKind kind() {
        return switch (flatCount()) { case 2 -> PieceKind.CORNER; case 1 -> PieceKind.BORDER; default -> PieceKind.INTERIOR; };
    }

    @Override public String toString() { return "#" + id + Arrays.toString(sides); }
}
