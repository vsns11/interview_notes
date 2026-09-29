package lld.ch17.chess;

import java.util.Random;

public final class Main {
    private static Game newGame() { return new Game(new Player("Ann", Color.WHITE), new Player("Ben", Color.BLACK)); }

    private static void play(Game g, String... moves) {
        for (String mv : moves) {
            String[] parts = mv.split("-");
            PieceType promo = parts[1].length() == 3 ? PieceType.valueOf(switch (parts[1].charAt(2)) {
                case 'Q' -> "QUEEN"; case 'R' -> "ROOK"; case 'B' -> "BISHOP"; default -> "KNIGHT"; }) : null;
            g.move(parts[0], parts[1].substring(0, 2), promo);
        }
    }

    private static void tryMove(Game g, String from, String to, PieceType promo) {
        try {
            System.out.println("  " + from + "-" + to + " played as " + g.move(from, to, promo));
        } catch (IllegalMoveException e) {
            System.out.println("  " + from + "-" + to + " rejected: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("== 1. Validation, Scholar's mate, undo ==");
        Game g = newGame();
        tryMove(g, "e7", "e5", null);                 // black out of turn
        tryMove(g, "e1", "e3", null);                 // king cannot jump two squares
        tryMove(g, "a1", "a4", null);                 // rook blocked by its own pawn
        play(g, "e2-e4", "e7-e5", "f1-c4", "b8-c6", "d1-h5", "g8-f6");
        tryMove(g, "h5", "f7", null);
        System.out.println("  Log: " + g.moveLog());
        System.out.println("  Status: " + g.status() + ", winner " + g.winner());
        System.out.println(g.render());
        System.out.println("  undo " + g.undo() + " -> status " + g.status() + ", " + g.turn() + " to move, "
                + g.legalMoveCount() + " legal moves");

        System.out.println("\n== 2. En passant, castling, promotion, and a move that ignores check ==");
        Game s = newGame();
        play(s, "e2-e4", "g8-f6", "e4-e5", "d7-d5");
        tryMove(s, "e5", "d6", null);                 // en passant
        play(s, "b8-c6", "g1-f3", "a7-a5", "f1-c4", "a5-a4");
        tryMove(s, "e1", "g1", null);                 // castle kingside
        play(s, "a4-a3", "d6-c7", "a3-b2");
        tryMove(s, "c7", "d8", PieceType.QUEEN);      // capture + promote, gives check
        System.out.println("  Black in check? " + s.isInCheck());
        tryMove(s, "b2", "a1", PieceType.KNIGHT);     // ignores the check -> illegal
        tryMove(s, "e8", "d8", null);                 // king takes the new queen
        System.out.println("  Log: " + s.moveLog());
        System.out.println(s.render());

        System.out.println("\n== 3. Stalemate (Sam Loyd's 10-move stalemate) ==");
        Game st = newGame();
        play(st, "e2-e3", "a7-a5", "d1-h5", "a8-a6", "h5-a5", "h7-h5", "h2-h4", "a6-h6",
                 "a5-c7", "f7-f6", "c7-d7", "e8-f7", "d7-b7", "d8-d3", "b7-b8", "d3-h7",
                 "b8-c8", "f7-g6", "c8-e6");
        System.out.println("  Log: " + st.moveLog());
        System.out.println("  Status: " + st.status() + ", black in check? " + st.isInCheck()
                + ", black legal moves: " + st.legalMoveCount());
        System.out.println(st.render());

        System.out.println("\n== 4. Castling rules: through an attacked square, and out of check ==");
        Game c1 = newGame();
        play(c1, "g2-g3", "b7-b6", "f1-h3", "c8-a6", "g1-f3", "b8-c6", "e2-e4", "g8-f6");
        tryMove(c1, "e1", "g1", null);                // f1 is attacked by the bishop on a6
        Game c2 = newGame();
        play(c2, "e2-e4", "d7-d6", "d2-d4", "g8-f6", "b1-c3", "g7-g6", "g1-f3", "f8-g7", "f1-b5");
        tryMove(c2, "e8", "g8", null);                // black is in check from the b5 bishop
        tryMove(c2, "c7", "c6", null);                // block the check
        tryMove(c2, "e1", "g1", null);
        tryMove(c2, "e8", "g8", null);                // no longer in check: now legal

        System.out.println("\n== 5. Random sides, resignation and forfeit ==");
        Game r = Game.withRandomSides("Ann", "Ben", new Random(7));
        System.out.println("  white = " + r.player(Color.WHITE).name() + ", black = " + r.player(Color.BLACK).name());
        play(r, "f2-f3", "e7-e5");
        r.resign(Color.WHITE);
        System.out.println("  Status: " + r.status() + ", winner " + r.winner());
        tryMove(r, "g2", "g4", null);
        Game f = newGame();
        f.forfeit(Color.BLACK);                        // e.g. black disconnected and did not come back
        System.out.println("  Status: " + f.status() + ", winner " + f.winner());
    }
}
