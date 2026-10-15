package lld.ch17.jigsaw;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class Main {
    private static void say(String what, PlaceResult r) { System.out.printf("  %-38s %s%n", what, r); }

    public static void main(String[] args) throws Exception {
        System.out.println("== 1. Sides and rotation ==");
        System.out.println("  T3 fits B3? " + Side.tab(3).fits(Side.blank(3))
                + " | T3 fits T3? " + Side.tab(3).fits(Side.tab(3))
                + " | T3 fits B4? " + Side.tab(3).fits(Side.blank(4))
                + " | F fits F? " + Side.FLAT.fits(Side.FLAT));
        Piece corner = new Piece(99, Side.FLAT, Side.tab(1), Side.blank(2), Side.FLAT);
        System.out.println("  piece " + corner + " is a " + corner.kind());
        for (int rot = 0; rot < 4; rot++) {
            StringBuilder sb = new StringBuilder("  rotation " + rot + ":");
            for (Direction d : Direction.values()) sb.append(' ').append(d).append('=').append(corner.side(d, rot));
            System.out.println(sb);
        }

        System.out.println("\n== 2. A 3 x 4 puzzle out of the box (seed 7) ==");
        Puzzle small = new PuzzleGenerator(new Random(7)).generate(3, 4, 0);
        for (Piece p : small.pieces()) System.out.println("  " + p + " " + p.kind());
        System.out.println("  validation problems: " + PuzzleValidator.validate(small));

        Board answer = new Board(3, 4);                                 // a solved copy, to script the player
        new EdgeFirstSolver(1_000_000).solve(small, answer);

        System.out.println("\n== 3. A player places pieces; the board enforces every rule ==");
        JigsawGame game = new JigsawGame(small);
        Board.Placement a00 = answer.at(0, 0), a01 = answer.at(0, 1), a11 = answer.at(1, 1);
        int id00 = a00.piece().id(), id01 = a01.piece().id(), id11 = a11.piece().id();
        say("piece " + id11 + " (interior) at corner (0,0)", game.place(id11, a11.rotation(), 0, 0));
        say("piece " + id00 + " at (0,0), wrong turn", game.place(id00, (a00.rotation() + 1) % 4, 0, 0));
        say("piece " + id00 + " at (0,0), right turn", game.place(id00, a00.rotation(), 0, 0));
        say("piece " + id01 + " at (0,0) too", game.place(id01, a01.rotation(), 0, 0));
        say("piece " + id00 + " again, at (2,3)", game.place(id00, a00.rotation(), 2, 3));
        say("piece " + id01 + " at (0,1), wrong turn", game.place(id01, (a01.rotation() + 2) % 4, 0, 1));
        say("piece " + id01 + " at (0,1), right turn", game.place(id01, a01.rotation(), 0, 1));
        say("piece " + id11 + " at (1,1), right turn", game.place(id11, a11.rotation(), 1, 1));
        say("piece 1 at (5,5)", game.place(1, 0, 5, 5));
        System.out.println("  progress " + game.progress() + ", status " + game.status());
        System.out.println(game.render());
        SolveResult rest = game.finishWith(new EdgeFirstSolver(1_000_000));
        System.out.println("  solver finishes the rest: " + rest);
        System.out.println("  progress " + game.progress() + ", status " + game.status());
        System.out.println(game.render());

        System.out.println("\n== 4. Validation catches a bad box ==");
        List<Piece> missing = new ArrayList<>(small.pieces());
        Piece lost = missing.remove(missing.size() - 1);
        System.out.println("  piece " + lost.id() + " lost: " + PuzzleValidator.validate(new Puzzle(3, 4, missing)));
        List<Piece> miscut = new ArrayList<>(small.pieces());
        Piece first = miscut.get(0);
        Side[] s = new Side[4];
        for (int i = 0; i < 4; i++) s[i] = first.side(i);
        for (int i = 0; i < 4; i++) if (!s[i].isFlat()) { s[i] = s[i].partner(); break; }  // flip one tab/blank
        miscut.set(0, new Piece(first.id(), s[0], s[1], s[2], s[3]));
        System.out.println("  one side mis-cut: " + PuzzleValidator.validate(new Puzzle(3, 4, miscut)));
        try {
            new JigsawGame(new Puzzle(3, 4, miscut));
        } catch (IllegalArgumentException e) {
            System.out.println("  new JigsawGame(...) refused: " + e.getMessage());
        }

        System.out.println("\n== 5. Same puzzles, two strategies ==");
        int[][] configs = {{4, 4, 0}, {6, 6, 0}, {6, 6, 2}, {10, 10, 0}};
        for (int[] cfg : configs) {
            System.out.println("  " + cfg[0] + " x " + cfg[1] + ", shapeVariety " + cfg[2]
                    + (cfg[2] == 0 ? " (every joint unique)" : " (look-alike pieces)"));
            for (SolverStrategy solver : List.of(new BruteForceSolver(5_000_000), new EdgeFirstSolver(5_000_000))) {
                Puzzle p = new PuzzleGenerator(new Random(11)).generate(cfg[0], cfg[1], cfg[2]);
                Board b = new Board(p.rows(), p.cols());
                SolveResult r = solver.solve(p, b);
                System.out.println("    " + r + "  verify=" + (r.solved() ? b.verify().isEmpty() : "-"));
            }
        }

        System.out.println("\n== 6. Co-op: four players place pieces of one 6 x 6 puzzle at once ==");
        Puzzle big = new PuzzleGenerator(new Random(5)).generate(6, 6, 0);
        Board key = new Board(6, 6);
        new EdgeFirstSolver(1_000_000).solve(big, key);
        JigsawGame coop = new JigsawGame(big);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (int t = 0; t < 4; t++) {
            int player = t;
            pool.submit(() -> {
                for (int cell = player; cell < 36; cell += 4) {
                    Board.Placement pl = key.at(cell / 6, cell % 6);
                    coop.place(pl.piece().id(), pl.rotation(), cell / 6, cell % 6);
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("  progress " + coop.progress() + ", moves " + coop.moves() + ", status " + coop.status());
    }
}
