package lld.ch17.chess;

/** Which castles are still allowed. Lost forever once the king or that rook moves (or the rook is captured). */
public record CastlingRights(boolean whiteKingSide, boolean whiteQueenSide, boolean blackKingSide, boolean blackQueenSide) {
    public static CastlingRights all() { return new CastlingRights(true, true, true, true); }

    public boolean kingSide(Color c) { return c == Color.WHITE ? whiteKingSide : blackKingSide; }
    public boolean queenSide(Color c) { return c == Color.WHITE ? whiteQueenSide : blackQueenSide; }

    /** Returns the rights after a move from -> to by a piece of the given type. */
    public CastlingRights after(PieceType moved, Color mover, Position from, Position to) {
        boolean wk = whiteKingSide, wq = whiteQueenSide, bk = blackKingSide, bq = blackQueenSide;
        if (moved == PieceType.KING) {
            if (mover == Color.WHITE) { wk = false; wq = false; } else { bk = false; bq = false; }
        }
        for (Position p : new Position[]{from, to}) {          // a rook leaving or being captured on its corner
            if (p.equals(new Position(0, 0))) wq = false;
            if (p.equals(new Position(7, 0))) wk = false;
            if (p.equals(new Position(0, 7))) bq = false;
            if (p.equals(new Position(7, 7))) bk = false;
        }
        return new CastlingRights(wk, wq, bk, bq);
    }
}
