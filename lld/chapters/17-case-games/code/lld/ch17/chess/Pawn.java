package lld.ch17.chess;

import java.util.ArrayList;
import java.util.List;

/** Moves straight forward but captures diagonally. En passant and promotion are added by Rules. */
public final class Pawn extends Piece {
    public Pawn(Color color) { super(color); }

    @Override public PieceType type() { return PieceType.PAWN; }

    private int dir() { return color() == Color.WHITE ? 1 : -1; }

    @Override public List<Position> targets(Board board, Position from) {
        List<Position> out = new ArrayList<>();
        int f = from.file(), r = from.rank() + dir();
        if (Position.onBoard(f, r) && board.get(new Position(f, r)) == null) {
            out.add(new Position(f, r));
            int startRank = color() == Color.WHITE ? 1 : 6;
            int r2 = r + dir();
            if (from.rank() == startRank && board.get(new Position(f, r2)) == null) out.add(new Position(f, r2));
        }
        for (Position p : attacks(board, from)) {
            Piece victim = board.get(p);
            if (victim != null && victim.color() != color()) out.add(p);
        }
        return out;
    }

    /** A pawn attacks both forward diagonals whether or not anything stands there. */
    @Override public List<Position> attacks(Board board, Position from) {
        List<Position> out = new ArrayList<>(2);
        int r = from.rank() + dir();
        for (int df : new int[]{-1, 1}) {
            int f = from.file() + df;
            if (Position.onBoard(f, r)) out.add(new Position(f, r));
        }
        return out;
    }
}
