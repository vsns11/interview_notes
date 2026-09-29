package lld.ch17.chess;

import java.util.ArrayList;
import java.util.List;

/** Rook, bishop and queen slide until blocked. The "is the path clear" loop is written once, here. */
public abstract sealed class SlidingPiece extends Piece permits Rook, Bishop, Queen {
    protected static final int[][] STRAIGHT = {{1,0},{-1,0},{0,1},{0,-1}};
    protected static final int[][] DIAGONAL = {{1,1},{1,-1},{-1,1},{-1,-1}};

    protected SlidingPiece(Color color) { super(color); }

    protected abstract int[][] directions();

    @Override public final List<Position> targets(Board board, Position from) {
        List<Position> out = new ArrayList<>();
        for (int[] d : directions()) {
            int f = from.file() + d[0], r = from.rank() + d[1];
            while (Position.onBoard(f, r)) {
                Position p = new Position(f, r);
                Piece occupant = board.get(p);
                if (occupant == null) {
                    out.add(p);
                } else {
                    if (occupant.color() != color()) out.add(p);   // capture, then stop
                    break;
                }
                f += d[0];
                r += d[1];
            }
        }
        return out;
    }
}
