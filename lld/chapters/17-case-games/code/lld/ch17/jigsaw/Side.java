package lld.ch17.jigsaw;

/**
 * One side of a piece: its type and a shape code. Two sides interlock when one is a TAB, the other a BLANK,
 * and both have the same shape code (the same cut profile). A FLAT side always has shape 0.
 */
public record Side(SideType type, int shape) {
    public static final Side FLAT = new Side(SideType.FLAT, 0);

    public Side {
        if (type == SideType.FLAT && shape != 0) throw new IllegalArgumentException("flat side has no shape");
        if (type != SideType.FLAT && shape <= 0) throw new IllegalArgumentException("tab/blank needs shape > 0");
    }

    public static Side tab(int shape) { return new Side(SideType.TAB, shape); }
    public static Side blank(int shape) { return new Side(SideType.BLANK, shape); }

    public boolean isFlat() { return type == SideType.FLAT; }

    /** The only side that fits against this one. */
    public Side partner() { return new Side(type.complement(), shape); }

    public boolean fits(Side other) {
        return !isFlat() && !other.isFlat() && other.type == type.complement() && other.shape == shape;
    }

    @Override public String toString() {
        return switch (type) { case FLAT -> "F"; case TAB -> "T" + shape; case BLANK -> "B" + shape; };
    }
}
