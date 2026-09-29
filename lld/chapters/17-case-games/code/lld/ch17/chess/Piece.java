package lld.ch17.chess;

import java.util.List;

/**
 * Every piece answers "which squares can I reach from here on this board?" in its own way
 * (polymorphism). Pieces are immutable: moving a piece moves the reference, so a board copy is cheap
 * and undo can simply restore an old board. Rules that need history (castling rights, en passant)
 * live in Rules/Game, not here.
 */
public abstract sealed class Piece permits King, Knight, Pawn, SlidingPiece {
    private final Color color;

    protected Piece(Color color) { this.color = color; }

    public Color color() { return color; }

    public abstract PieceType type();

    /** Squares this piece may move to by its ordinary rules (captures included, own-king safety NOT checked). */
    public abstract List<Position> targets(Board board, Position from);

    /** Squares this piece attacks. Same as targets for every piece except the pawn. */
    public List<Position> attacks(Board board, Position from) { return targets(board, from); }

    /** Empty square or enemy piece. */
    protected boolean canLandOn(Board board, Position p) {
        Piece other = board.get(p);
        return other == null || other.color != color;
    }

    public char symbol() {
        char c = type().letter();
        return color == Color.WHITE ? c : Character.toLowerCase(c);
    }

    /** Simple factory used for set-up and promotion. */
    public static Piece create(PieceType type, Color color) {
        return switch (type) {
            case KING -> new King(color);
            case QUEEN -> new Queen(color);
            case ROOK -> new Rook(color);
            case BISHOP -> new Bishop(color);
            case KNIGHT -> new Knight(color);
            case PAWN -> new Pawn(color);
        };
    }
}
