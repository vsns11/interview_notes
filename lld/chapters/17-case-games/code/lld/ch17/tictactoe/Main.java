package lld.ch17.tictactoe;

import java.util.List;
import java.util.Random;

public final class Main {
    public static void main(String[] args) {
        System.out.println("== 1. 3x3 scripted game (with one invalid move) ==");
        Player x = new Player("Xena", 'X', new ScriptedStrategy(List.of(
                new Cell(0, 0), new Cell(1, 1), new Cell(2, 2))));
        Player o = new Player("Omar", 'O', new ScriptedStrategy(List.of(
                new Cell(0, 0), new Cell(0, 1), new Cell(0, 2))));   // first one is already taken
        new Game(3, List.of(x, o), System.out::println).play();

        System.out.println("\n== 2. 3x3 rule-based bot vs seeded random bot (seed 7) ==");
        new Game(3, List.of(new Player("Bot", 'X', new RuleBasedStrategy()),
                new Player("Rand", 'O', new RandomStrategy(new Random(7)))), System.out::println).play();

        System.out.println("\n== 3. 4x4 rule-based vs rule-based ==");
        new Game(4, List.of(new Player("A", 'X', new RuleBasedStrategy()),
                new Player("B", 'O', new RuleBasedStrategy())), line -> { if (!line.startsWith("  A") && !line.startsWith("  B")) System.out.println(line); }).play();

        System.out.println("\n== 4. 5x5 with three players (two random, seeds 1 and 2; one rule-based) ==");
        new Game(5, List.of(new Player("P1", 'X', new RandomStrategy(new Random(1))),
                new Player("P2", 'O', new RandomStrategy(new Random(2))),
                new Player("P3", 'Z', new RuleBasedStrategy())), line -> { if (!line.startsWith("  P")) System.out.println(line); }).play();
    }
}
