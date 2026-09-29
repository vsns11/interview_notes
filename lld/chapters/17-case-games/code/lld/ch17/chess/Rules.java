package lld.ch17.chess;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateless rules engine: generates legal moves (including castling, en passant, promotion)
 * and applies a move to a board. "Legal" = pseudo-legal AND does not leave your own king attacked.
 */
public final class Rules {
    private Rules() {}

    private static final PieceType[] PROMOTIONS = {PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT};

    public static boolean isInCheck(Board board, Color side) {
        return board.isAttacked(board.kingOf(side), side.opposite());
    }

    /** Moves that follow the pieces' movement rules, before the king-safety filter. */
    public static List<Move> pseudoLegalMoves(Board board, Color side, CastlingRights rights, Position enPassant) {
        List<Move> moves = new ArrayList<>();
        int lastRank = side == Color.WHITE ? 7 : 0;
        for (int f = 0; f < 8; f++) {
            for (int r = 0; r < 8; r++) {
                Position from = new Position(f, r);
                Piece piece = board.get(from);
                if (piece == null || piece.color() != side) continue;
                for (Position to : piece.targets(board, from)) {
                    if (piece.type() == PieceType.PAWN && to.rank() == lastRank) {
                        for (PieceType promo : PROMOTIONS) moves.add(new Move(from, to, MoveType.PROMOTION, promo));
                    } else {
                        moves.add(Move.normal(from, to));
                    }
                }
                if (piece.type() == PieceType.PAWN && enPassant != null && piece.attacks(board, from).contains(enPassant)) {
                    moves.add(new Move(from, enPassant, MoveType.EN_PASSANT, null));
                }
            }
        }
        addCastling(board, side, rights, moves);
        return moves;
    }

    private static void addCastling(Board board, Color side, CastlingRights rights, List<Move> moves) {
        int r = side == Color.WHITE ? 0 : 7;
        Position kingSq = new Position(4, r);
        Piece king = board.get(kingSq);
        if (king == null || king.type() != PieceType.KING || king.color() != side) return;
        Color enemy = side.opposite();
        if (board.isAttacked(kingSq, enemy)) return;                       // cannot castle out of check
        if (rights.kingSide(side) && isOwnRook(board, new Position(7, r), side)
                && empty(board, r, 5, 6)
                && !board.isAttacked(new Position(5, r), enemy)             // cannot pass through check
                && !board.isAttacked(new Position(6, r), enemy)) {
            moves.add(new Move(kingSq, new Position(6, r), MoveType.CASTLE_KINGSIDE, null));
        }
        if (rights.queenSide(side) && isOwnRook(board, new Position(0, r), side)
                && empty(board, r, 1, 2, 3)
                && !board.isAttacked(new Position(3, r), enemy)
                && !board.isAttacked(new Position(2, r), enemy)) {
            moves.add(new Move(kingSq, new Position(2, r), MoveType.CASTLE_QUEENSIDE, null));
        }
    }

    private static boolean isOwnRook(Board b, Position p, Color side) {
        Piece piece = b.get(p);
        return piece != null && piece.type() == PieceType.ROOK && piece.color() == side;
    }

    private static boolean empty(Board b, int rank, int... files) {
        for (int f : files) if (b.get(new Position(f, rank)) != null) return false;
        return true;
    }

    /** Explains why a king move of two squares (a castling attempt) is not legal right now. */
    public static String whyCannotCastle(Board board, Color side, CastlingRights rights, Position kingTo) {
        int r = side == Color.WHITE ? 0 : 7;
        if (kingTo.rank() != r || (kingTo.file() != 6 && kingTo.file() != 2)) return "that is not a castling square";
        boolean kingSide = kingTo.file() == 6;
        if (!(kingSide ? rights.kingSide(side) : rights.queenSide(side))
                || !isOwnRook(board, new Position(kingSide ? 7 : 0, r), side))
            return "the king or that rook has already moved";
        if (!(kingSide ? empty(board, r, 5, 6) : empty(board, r, 1, 2, 3)))
            return "the squares between king and rook are not empty";
        Color enemy = side.opposite();
        if (board.isAttacked(new Position(4, r), enemy)) return "the king is in check";
        Position crossed = new Position(kingSide ? 5 : 3, r);
        if (board.isAttacked(crossed, enemy)) return "the king would pass through the attacked square " + crossed;
        if (board.isAttacked(kingTo, enemy)) return "the king would land on the attacked square " + kingTo;
        return "it is not legal in this position";
    }

    public static List<Move> legalMoves(Board board, Color side, CastlingRights rights, Position enPassant) {
        List<Move> legal = new ArrayList<>();
        for (Move m : pseudoLegalMoves(board, side, rights, enPassant)) {
            Board trial = board.copy();
            apply(trial, m);
            if (!isInCheck(trial, side)) legal.add(m);
        }
        return legal;
    }

    /** Mutates the board. Handles the extra piece movements of the special moves. */
    public static void apply(Board board, Move m) {
        Piece piece = board.get(m.from());
        board.set(m.from(), null);
        board.set(m.to(), piece);
        switch (m.type()) {
            case EN_PASSANT -> board.set(new Position(m.to().file(), m.from().rank()), null);   // the passed pawn
            case PROMOTION -> board.set(m.to(), Piece.create(m.promotion(), piece.color()));
            case CASTLE_KINGSIDE -> moveRook(board, m.from().rank(), 7, 5);
            case CASTLE_QUEENSIDE -> moveRook(board, m.from().rank(), 0, 3);
            case NORMAL -> { }
        }
    }

    private static void moveRook(Board board, int rank, int fromFile, int toFile) {
        Position from = new Position(fromFile, rank);
        board.set(new Position(toFile, rank), board.get(from));
        board.set(from, null);
    }
}
