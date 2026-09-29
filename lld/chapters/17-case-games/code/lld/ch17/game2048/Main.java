package lld.ch17.game2048;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class Main {
    public static void main(String[] args) {
        System.out.println("== 1. Merge rules for one line moving left ==");
        for (int[] line : List.of(new int[]{2, 2, 2, 2}, new int[]{2, 2, 2, 0}, new int[]{2, 2, 4, 0}, new int[]{4, 0, 4, 8},
                new int[]{2, 0, 0, 2}, new int[]{8, 4, 2, 2}, new int[]{2, 4, 8, 16})) {
            LineMerger.Result r = LineMerger.merge(line);
            System.out.println("  " + Arrays.toString(line) + " -> " + Arrays.toString(r.line()) + "  +" + r.points());
        }

        System.out.println("\n== 2. A move that changes nothing does not count and spawns nothing ==");
        Game stuckLeft = new Game(Board.of(new int[][]{{2, 4, 8, 16}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}),
                2048, new RandomTileSpawner(new Random(1), 0.9));
        System.out.println("  move LEFT changed board? " + stuckLeft.move(Direction.LEFT) + ", moves=" + stuckLeft.moves());
        System.out.println("  move DOWN changed board? " + stuckLeft.move(Direction.DOWN) + ", moves=" + stuckLeft.moves());
        System.out.println(stuckLeft.board().render());

        System.out.println("\n== 3. Game-over detection ==");
        Game full = new Game(Board.of(new int[][]{{2, 4, 2, 4}, {4, 2, 4, 2}, {2, 4, 2, 4}, {4, 2, 4, 2}}),
                2048, new RandomTileSpawner(new Random(1), 0.9));
        System.out.println("  checkerboard, no empty cell, no equal neighbours -> " + full.status());
        Game almost = new Game(Board.of(new int[][]{{2, 4, 2, 4}, {4, 2, 4, 2}, {2, 4, 2, 4}, {4, 2, 4, 4}}),
                2048, new RandomTileSpawner(new Random(1), 0.9));
        System.out.println("  same but last row ends 4,4 -> " + almost.status());

        System.out.println("\n== 4. Keyboard codes from the problem statement ==");
        for (int key = 0; key < 4; key++) System.out.print("  " + key + "=" + Direction.fromKey(key));
        System.out.println();

        System.out.println("\n== 5. Seeded game (seed 42), corner strategy LEFT > DOWN > RIGHT > UP, target 128 ==");
        Game g = new Game(4, 128, new RandomTileSpawner(new Random(42), 0.9));
        System.out.println("  start:\n" + g.board().render());
        List<Direction> priority = List.of(Direction.LEFT, Direction.DOWN, Direction.RIGHT, Direction.UP);
        boolean announced = false;
        while (g.status() != GameStatus.LOST) {
            for (Direction d : priority) if (g.move(d)) break;
            if (g.reachedTarget() && !announced) {
                announced = true;
                System.out.println("  reached 128 after " + g.moves() + " moves (status " + g.status() + "), score " + g.score());
                System.out.println(g.board().render());
            }
        }
        System.out.println("  game over after " + g.moves() + " moves, score " + g.score() + ", max tile " + g.board().maxTile());
        System.out.println(g.board().render());
    }
}
