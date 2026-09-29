package lld.ch17.chess;

public final class Bishop extends SlidingPiece {
    public Bishop(Color color) { super(color); }
    @Override public PieceType type() { return PieceType.BISHOP; }
    @Override protected int[][] directions() { return DIAGONAL; }
}
