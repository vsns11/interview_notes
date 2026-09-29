package lld.ch17.chess;

/** 8x8 grid of (immutable) pieces. Knows geometry and attacks; knows nothing about turns or history. */
public final class Board {
    private final Piece[][] squares = new Piece[8][8]; // [file][rank]

    public static Board standard() {
        Board b = new Board();
        PieceType[] back = {PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK};
        for (int f = 0; f < 8; f++) {
            b.squares[f][0] = Piece.create(back[f], Color.WHITE);
            b.squares[f][1] = new Pawn(Color.WHITE);
            b.squares[f][6] = new Pawn(Color.BLACK);
            b.squares[f][7] = Piece.create(back[f], Color.BLACK);
        }
        return b;
    }

    public Piece get(Position p) { return squares[p.file()][p.rank()]; }

    void set(Position p, Piece piece) { squares[p.file()][p.rank()] = piece; }

    /** Pieces are immutable, so copying the references is a full, independent copy of the position. */
    public Board copy() {
        Board b = new Board();
        for (int f = 0; f < 8; f++) b.squares[f] = squares[f].clone();
        return b;
    }

    public Position kingOf(Color color) {
        for (int f = 0; f < 8; f++)
            for (int r = 0; r < 8; r++) {
                Piece p = squares[f][r];
                if (p != null && p.type() == PieceType.KING && p.color() == color) return new Position(f, r);
            }
        throw new IllegalStateException("no " + color + " king on board");
    }

    public boolean isAttacked(Position target, Color by) {
        for (int f = 0; f < 8; f++)
            for (int r = 0; r < 8; r++) {
                Piece p = squares[f][r];
                if (p != null && p.color() == by && p.attacks(this, new Position(f, r)).contains(target)) return true;
            }
        return false;
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        for (int r = 7; r >= 0; r--) {
            sb.append("    ").append(r + 1).append(' ');
            for (int f = 0; f < 8; f++) {
                Piece p = squares[f][r];
                sb.append(' ').append(p == null ? '.' : p.symbol());
            }
            sb.append('\n');
        }
        return sb.append("       a b c d e f g h").toString();
    }
}
