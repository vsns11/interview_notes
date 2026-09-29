package lld.ch17.chess;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Controls one game: whose turn it is, validates and plays moves, detects check/checkmate/stalemate,
 * keeps the move log, and supports undo.
 *
 * Undo = Command + Memento: each history entry stores the move (the command) and a snapshot of the
 * state before it (the memento). Undo restores the snapshot, which is always correct even for
 * castling, en passant and promotion.
 */
public final class Game {
    /** Memento: everything needed to restore the position exactly. */
    private record Snapshot(Board board, Color turn, CastlingRights rights, Position enPassant,
                            GameStatus status, Color winner) {}

    private record HistoryEntry(Player player, Move move, String notation, Snapshot before) {}

    private final Player white, black;
    private Board board = Board.standard();
    private Color turn = Color.WHITE;                      // white always moves first
    private CastlingRights rights = CastlingRights.all();
    private Position enPassant;                            // square a pawn just skipped over, or null
    private GameStatus status = GameStatus.ACTIVE;
    private Color winner;
    private final Deque<HistoryEntry> history = new ArrayDeque<>();

    public Game(Player white, Player black) {
        if (white.color() != Color.WHITE || black.color() != Color.BLACK) throw new IllegalArgumentException("colors mismatch");
        this.white = white;
        this.black = black;
    }

    /** The course's rule: each player is randomly assigned a side. Pass a seeded Random for repeatable tests. */
    public static Game withRandomSides(String nameA, String nameB, Random rng) {
        boolean aIsWhite = rng.nextBoolean();
        return aIsWhite ? new Game(new Player(nameA, Color.WHITE), new Player(nameB, Color.BLACK))
                        : new Game(new Player(nameB, Color.WHITE), new Player(nameA, Color.BLACK));
    }

    public synchronized Player player(Color c) { return c == Color.WHITE ? white : black; }

    public synchronized String move(String from, String to) { return move(from, to, null); }

    public synchronized String move(String fromSq, String toSq, PieceType promotion) {
        if (status != GameStatus.ACTIVE) throw new IllegalMoveException("game is over: " + status);
        Position from = Position.of(fromSq), to = Position.of(toSq);
        Piece piece = board.get(from);
        if (piece == null) throw new IllegalMoveException("no piece on " + from);
        if (piece.color() != turn) throw new IllegalMoveException("it is " + turn + "'s turn");

        List<Move> legal = Rules.legalMoves(board, turn, rights, enPassant);
        Optional<Move> chosen = legal.stream()
                .filter(m -> m.from().equals(from) && m.to().equals(to))
                .filter(m -> m.type() != MoveType.PROMOTION
                        || m.promotion() == (promotion == null ? PieceType.QUEEN : promotion))
                .findFirst();
        if (chosen.isEmpty()) {
            if (piece.type() == PieceType.KING && Math.abs(to.file() - from.file()) == 2)
                throw new IllegalMoveException("cannot castle " + fromSq + "-" + toSq + ": "
                        + Rules.whyCannotCastle(board, turn, rights, to));
            boolean reachable = Rules.pseudoLegalMoves(board, turn, rights, enPassant).stream()
                    .anyMatch(m -> m.from().equals(from) && m.to().equals(to));
            throw new IllegalMoveException(reachable
                    ? fromSq + "-" + toSq + " would leave the " + turn + " king in check"
                    : piece.type() + " on " + fromSq + " cannot move to " + toSq);
        }
        return play(chosen.get(), piece);
    }

    private String play(Move m, Piece piece) {
        Player mover = turn == Color.WHITE ? white : black;
        Snapshot before = new Snapshot(board.copy(), turn, rights, enPassant, status, winner);
        boolean capture = board.get(m.to()) != null || m.type() == MoveType.EN_PASSANT;

        Rules.apply(board, m);
        rights = rights.after(piece.type(), turn, m.from(), m.to());
        enPassant = (piece.type() == PieceType.PAWN && Math.abs(m.to().rank() - m.from().rank()) == 2)
                ? new Position(m.from().file(), (m.from().rank() + m.to().rank()) / 2) : null;
        turn = turn.opposite();

        // End-of-game detection: the side to move has no legal move -> checkmate if in check, else stalemate.
        boolean check = Rules.isInCheck(board, turn);
        boolean noMoves = Rules.legalMoves(board, turn, rights, enPassant).isEmpty();
        if (noMoves) {
            status = check ? GameStatus.CHECKMATE : GameStatus.STALEMATE;
            winner = check ? mover.color() : null;
        }
        String notation = notation(m, piece, capture) + (check ? (noMoves ? "#" : "+") : "");
        history.push(new HistoryEntry(mover, m, notation, before));
        return notation;
    }

    private static String notation(Move m, Piece piece, boolean capture) {
        return switch (m.type()) {
            case CASTLE_KINGSIDE -> "O-O";
            case CASTLE_QUEENSIDE -> "O-O-O";
            default -> {
                String prefix = piece.type() == PieceType.PAWN ? "" : String.valueOf(piece.type().letter());
                String s = prefix + m.from() + (capture ? "x" : "-") + m.to();
                if (m.type() == MoveType.PROMOTION) s += "=" + m.promotion().letter();
                if (m.type() == MoveType.EN_PASSANT) s += " e.p.";
                yield s;
            }
        };
    }

    /** Undo the last move (e.g. for analysis mode or a take-back both players agree on). */
    public synchronized String undo() {
        if (history.isEmpty()) throw new IllegalStateException("nothing to undo");
        HistoryEntry last = history.pop();
        Snapshot s = last.before();
        board = s.board(); turn = s.turn(); rights = s.rights(); enPassant = s.enPassant();
        status = s.status(); winner = s.winner();
        return last.notation();
    }

    public synchronized void resign(Color who) {
        if (status != GameStatus.ACTIVE) throw new IllegalStateException("game is over");
        status = GameStatus.RESIGNATION;
        winner = who.opposite();
    }

    /** Forfeit: the player loses without choosing to (left the game, ran out of time, broke the rules of the site). */
    public synchronized void forfeit(Color who) {
        if (status != GameStatus.ACTIVE) throw new IllegalStateException("game is over");
        status = GameStatus.FORFEIT;
        winner = who.opposite();
    }

    public synchronized boolean isInCheck() { return Rules.isInCheck(board, turn); }
    public synchronized int legalMoveCount() { return Rules.legalMoves(board, turn, rights, enPassant).size(); }
    public synchronized GameStatus status() { return status; }
    public synchronized Color winner() { return winner; }
    public synchronized Color turn() { return turn; }
    public synchronized String render() { return board.render(); }

    /** The move log, oldest first, numbered like a score sheet. */
    public synchronized String moveLog() {
        List<HistoryEntry> list = new java.util.ArrayList<>(history);
        java.util.Collections.reverse(list);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i % 2 == 0) sb.append(i / 2 + 1).append(". ");
            sb.append(list.get(i).notation()).append(' ');
        }
        return sb.toString().trim();
    }
}
