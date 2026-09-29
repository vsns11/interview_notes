package lld.ch17.chess;

public final class Queen extends SlidingPiece {
    private static final int[][] ALL = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
    public Queen(Color color) { super(color); }
    @Override public PieceType type() { return PieceType.QUEEN; }
    @Override protected int[][] directions() { return ALL; }
}
