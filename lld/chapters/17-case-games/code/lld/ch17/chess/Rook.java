package lld.ch17.chess;

public final class Rook extends SlidingPiece {
    public Rook(Color color) { super(color); }
    @Override public PieceType type() { return PieceType.ROOK; }
    @Override protected int[][] directions() { return STRAIGHT; }
}
