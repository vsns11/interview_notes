package lld.ch18.cricketdashboard;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/** Prints the scorecard; kept apart so a web or JSON view could replace it (Single Responsibility). */
final class ScorecardPrinter {
    void print(TeamInnings in) {
        System.out.println("Scorecard for " + in.team() + ":");
        System.out.printf("%-8s %5s %3s %3s %5s %6s%n", "Player", "Score", "4s", "6s", "Balls", "SR");
        for (Batter b : in.batters()) {
            String name = b.name + (in.isAtCrease(b) ? "*" : "");
            System.out.printf("%-8s %5d %3d %3d %5d %6s%n", name, b.runs, b.fours, b.sixes, b.balls, b.strikeRate());
        }
        System.out.println("Total: " + in.total() + "/" + in.wickets() + "   Extras: " + in.extras());
        System.out.println("Overs: " + in.overs());
    }
}

/** Drives the whole match from the text input (input format of the Udaan machine-coding round). */
final class MatchRunner {
    private final ScorecardPrinter printer = new ScorecardPrinter();

    void run(String input) {
        Deque<String> lines = new ArrayDeque<>();
        Arrays.stream(input.split("\\R")).map(String::trim).filter(l -> !l.isEmpty()).forEach(lines::add);
        int players = Integer.parseInt(after(lines.poll()));
        int overs = Integer.parseInt(after(lines.poll()));

        TeamInnings first = playInnings("Team 1", players, overs, null, lines);
        TeamInnings second = playInnings("Team 2", players, overs, first.total() + 1, lines);

        if (second.total() > first.total()) System.out.println("Result: Team 2 won the match by " + second.wicketsInHand() + " wickets");
        else if (second.total() < first.total()) System.out.println("Result: Team 1 won the match by " + (first.total() - second.total()) + " runs");
        else System.out.println("Result: Match tied");
    }

    private TeamInnings playInnings(String team, int players, int overs, Integer target, Deque<String> lines) {
        lines.poll();                                            // "Batting Order for team N:"
        List<String> order = new ArrayList<>();
        for (int i = 0; i < players; i++) order.add(lines.poll());
        TeamInnings in = new TeamInnings(team, order, overs, target);
        System.out.println("Batting order for " + team + ": " + order);
        while (!in.isOver() && !lines.isEmpty() && lines.peek().startsWith("Over")) {
            System.out.println(lines.poll());
            int legalThisOver = 0;
            while (legalThisOver < 6 && !in.isOver()) {          // an over = 6 LEGAL balls
                BallEvent e = BallEvent.parse(lines.poll());
                in.play(e);
                if (e.isLegal()) legalThisOver++;
            }
            printer.print(in);
        }
        // skip any balls left in the input after the innings ended (e.g. target reached mid-over)
        while (!lines.isEmpty() && !lines.peek().startsWith("Batting Order")) lines.poll();
        return in;
    }

    private static String after(String line) { return line.substring(line.indexOf(':') + 1).trim(); }
}

public class Main {
    static final String SAMPLE = """
            No. of players for each team: 5
            No. of overs: 2
            Batting Order for team 1:
            P1
            P2
            P3
            P4
            P5
            Over 1:
            1
            1
            1
            1
            1
            2
            Over 2:
            W
            4
            4
            Wd
            W
            1
            6
            Batting Order for team 2:
            P6
            P7
            P8
            P9
            P10
            Over 1:
            4
            6
            W
            W
            1
            1
            Over 2:
            6
            1
            W
            W
            """;

    public static void main(String[] args) {
        new MatchRunner().run(SAMPLE);
    }
}
