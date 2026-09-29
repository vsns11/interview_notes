package lld.ch17.snakeladder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;
import java.util.stream.Collectors;

public final class Main {
    private static final List<Jump> JUMPS = List.of(
            new Jump(4, 14), new Jump(9, 31), new Jump(21, 42), new Jump(28, 84), new Jump(51, 67),
            new Jump(71, 91), new Jump(80, 99),                                       // ladders
            new Jump(17, 7), new Jump(54, 34), new Jump(62, 19), new Jump(64, 60),
            new Jump(87, 24), new Jump(93, 73), new Jump(95, 75), new Jump(98, 79));  // snakes

    public static void main(String[] args) throws Exception {
        Board board = new Board(100, JUMPS);

        System.out.println("== 1. Loaded dice: ladder, bonus roll, snake, three sixes ==");
        Game g1 = new Game(board, new LoadedDice(4, 6, 3, 3, 6, 6, 6, 2), Rules.classic(),
                List.of(new Player("Ann"), new Player("Bob")), System.out::println);
        for (int i = 0; i < 4; i++) g1.playTurn();

        System.out.println("\n== 2. Overflow rules: token on 97 of 100, rolls a 5 ==");
        Board plain = new Board(100, List.of());
        for (String variant : List.of("Exact", "Bounce")) {
            OverflowRule rule = variant.equals("Exact") ? OverflowRule.exactFinish() : OverflowRule.bounceBack();
            Player p = new Player(variant);
            p.moveTo(97);
            Game g = new Game(plain, new LoadedDice(5), new Rules(rule, WinStrategy.firstToFinish(), false, 3),
                    List.of(p, new Player("Q")), System.out::println);
            g.playTurn();
        }

        System.out.println("\n== 3. Bad configurations are rejected up front ==");
        for (List<Jump> bad : List.of(List.of(new Jump(100, 5)), List.of(new Jump(10, 30), new Jump(30, 5)))) {
            try { new Board(100, bad); }
            catch (IllegalArgumentException e) { System.out.println("  rejected: " + e.getMessage()); }
        }

        System.out.println("\n== 4. Seeded game (seed 2024), 3 players, first to finish wins ==");
        Game g4 = new Game(board, new RandomDice(1, 6, new Random(2024)), Rules.classic(),
                List.of(new Player("Ann"), new Player("Bob"), new Player("Cid")), System.out::println);
        System.out.println("  Winner: " + g4.play(500).get(0).name());

        System.out.println("\n== 5. Same seed, rank-everyone rule, bounce-back overflow (log hidden) ==");
        Game g5 = new Game(board, new RandomDice(1, 6, new Random(2024)),
                new Rules(OverflowRule.bounceBack(), WinStrategy.rankAll(), true, 3),
                List.of(new Player("Ann"), new Player("Bob"), new Player("Cid")), line -> { });
        System.out.println("  Standings: " + g5.play(1000).stream().map(Player::name).collect(Collectors.joining(" > ")));

        System.out.println("\n== 6. Eight independent sessions on eight threads at once ==");
        // Each session owns its players, turn queue and dice (its own seeded Random), and shares only the
        // read-only Board. So running them in parallel must give exactly the result each one gives alone.
        Map<Integer, String> parallel = new ConcurrentHashMap<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
            for (int seed = 1; seed <= 8; seed++) {
                int s0 = seed;
                pool.submit(() -> parallel.put(s0, winnerOf(board, s0)));
            }
        }
        List<String> mismatches = new ArrayList<>();
        for (int seed = 1; seed <= 8; seed++)
            if (!parallel.get(seed).equals(winnerOf(board, seed))) mismatches.add("seed " + seed);
        System.out.println("  winners by seed: " + new java.util.TreeMap<>(parallel));
        System.out.println("  same as running each session alone? " + (mismatches.isEmpty() ? "yes" : "NO " + mismatches));
    }

    private static String winnerOf(Board board, int seed) {
        Game g = new Game(board, new RandomDice(1, 6, new Random(seed)), Rules.classic(),
                List.of(new Player("Ann"), new Player("Bob"), new Player("Cid")), line -> { });
        return g.play(500).get(0).name();
    }
}
