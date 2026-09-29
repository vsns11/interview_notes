package lld.ch17.chess;

import java.util.List;

/** One square in any direction. Castling is a two-piece move, so Rules generates it, not King. */
public final class King extends Piece {
    private static final int[][] OFFSETS = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};

    public King(Color color) { super(color); }

    @Override public PieceType type() { return PieceType.KING; }

    @Override public List<Position> targets(Board board, Position from) {
        return StepPieces.step(this, board, from, OFFSETS);
    }
}
