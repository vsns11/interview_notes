package lld.ch17.chess;

import java.util.List;

/** L-shaped jump; the only piece that ignores pieces in between. */
public final class Knight extends Piece {
    private static final int[][] OFFSETS = {{1,2},{2,1},{2,-1},{1,-2},{-1,-2},{-2,-1},{-2,1},{-1,2}};

    public Knight(Color color) { super(color); }

    @Override public PieceType type() { return PieceType.KNIGHT; }

    @Override public List<Position> targets(Board board, Position from) {
        return StepPieces.step(this, board, from, OFFSETS);
    }
}
