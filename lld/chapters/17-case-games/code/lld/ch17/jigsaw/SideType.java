package lld.ch17.jigsaw;

/** The three kinds of side a jigsaw piece can have. */
public enum SideType {
    FLAT,   // a straight edge: belongs on the outer frame of the picture
    TAB,    // a knob that sticks out
    BLANK;  // a hole that a tab slots into

    /** The type a neighbour's side must have to interlock with this one. FLAT interlocks with nothing. */
    public SideType complement() {
        return switch (this) {
            case TAB -> BLANK;
            case BLANK -> TAB;
            case FLAT -> throw new IllegalStateException("a flat side has no partner");
        };
    }
}
